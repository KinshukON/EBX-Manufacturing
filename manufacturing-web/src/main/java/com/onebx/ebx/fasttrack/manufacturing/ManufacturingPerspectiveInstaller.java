package com.onebx.ebx.fasttrack.manufacturing;

import java.io.InputStream;
import java.nio.file.Files;
import java.util.Locale;

import com.onwbp.adaptation.Adaptation;
import com.onwbp.adaptation.AdaptationHome;
import com.onwbp.adaptation.AdaptationName;
import com.onwbp.adaptation.AdaptationReference;
import com.onwbp.base.text.UserMessage;
import com.orchestranetworks.instance.HomeKey;
import com.orchestranetworks.instance.Repository;
import com.orchestranetworks.schema.Path;
import com.orchestranetworks.service.ImportSpec;
import com.orchestranetworks.service.ImportSpecMode;
import com.orchestranetworks.service.LoggingCategory;
import com.orchestranetworks.service.Procedure;
import com.orchestranetworks.service.ProcedureContext;
import com.orchestranetworks.service.ProcedureResult;
import com.orchestranetworks.service.Profile;
import com.orchestranetworks.service.ProgrammaticService;
import com.orchestranetworks.service.Session;
import com.orchestranetworks.service.ValueContextForUpdate;

/** Creates the packaged Manufacturing perspective once and preserves later administrator edits. */
public final class ManufacturingPerspectiveInstaller {
    static final String KEY = "ebx-perspective-manufacturing-01";
    private static final HomeKey MANAGER_HOME = HomeKey.forBranchName("ebx-manager");
    private static final AdaptationName MANAGER_ROOT = AdaptationName.forName("ebx-manager");
    private static final Path ACTIVATED = Path.parse("/domain/properties/activated");
    private static final Path DEFAULT_SELECTION = Path.parse("/domain/properties/defaultSelection");
    private static final String RESOURCE = "/perspective/manufacturing.xml";

    private ManufacturingPerspectiveInstaller() { }

    public static void install(final Repository repository, final Session session, final LoggingCategory log) {
        if ("false".equalsIgnoreCase(System.getProperty("ebx.manufacturing.perspective", "true"))) {
            return;
        }
        if (attempt(repository, session, log)) {
            return;
        }
        final java.util.concurrent.ScheduledExecutorService executor =
            java.util.concurrent.Executors.newSingleThreadScheduledExecutor(task -> {
                Thread thread = new Thread(task, "ebx-manufacturing-bootstrap");
                thread.setDaemon(true);
                return thread;
            });
        executor.schedule(new Runnable() {
            private int attempts = 1;
            @Override public void run() {
                boolean complete = attempt(repository, session, log);
                if (complete || ++attempts >= 10) {
                    if (!complete) {
                        log.error("[manufacturing] automatic installation could not complete; fix the reported cause and restart EBX");
                    }
                    executor.shutdown();
                } else {
                    executor.schedule(this, 30, java.util.concurrent.TimeUnit.SECONDS);
                }
            }
        }, 30, java.util.concurrent.TimeUnit.SECONDS);
    }

    private static synchronized boolean attempt(final Repository repository, final Session session, final LoggingCategory log) {
        if ("false".equalsIgnoreCase(System.getProperty("ebx.manufacturing.perspective", "true"))) {
            log.info("[manufacturing] perspective installation disabled");
            return true;
        }
        final AdaptationHome home = repository.lookupHome(MANAGER_HOME);
        final Adaptation parent = home == null ? null : home.findAdaptationOrNull(MANAGER_ROOT);
        if (parent == null) {
            log.warn("[manufacturing] perspective not installed: ebx-manager is unavailable");
            return false;
        }
        try {
            new com.onebx.ebx.fasttrack.manufacturing.service.DemoDataSeederService()
                .provision(repository, session, new StringBuilder());
        } catch (Exception ex) {
            log.warn("[manufacturing] waiting for dataset provisioning: " + ex.getMessage());
            return false;
        }
        if (home.findAdaptationOrNull(AdaptationName.forName(KEY)) != null) {
            if (!repairStagingTargets(home, session, log)) return false;
            log.info("[manufacturing] perspective " + KEY + " already present, left unchanged");
            return true;
        }
        try (InputStream input = ManufacturingPerspectiveInstaller.class.getResourceAsStream(RESOURCE)) {
            if (input == null) {
                log.error("[manufacturing] perspective not installed: missing " + RESOURCE);
                return true;
            }
            final java.nio.file.Path xml = Files.createTempFile("ebx-manufacturing-perspective-", ".xml");
            try {
                Files.copy(input, xml, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                final ProcedureResult result = ProgrammaticService.createForSession(session, home).execute(new Procedure() {
                    @Override
                    public void execute(final ProcedureContext context) throws Exception {
                        context.setAllPrivileges(true);
                        final Adaptation child = context.doCreateChild(
                            parent.getAdaptationName(), AdaptationReference.forPersistentName(KEY), Profile.ADMINISTRATOR);
                        context.setInstanceLabel(child, UserMessage.createInfo("Manufacturing Domain"));
                        final ImportSpec spec = new ImportSpec();
                        spec.setTargetAdaptationTable(child.getTable(Path.parse("/domain/menuItem")));
                        spec.setSourceFile(xml.toFile());
                        spec.setImportMode(ImportSpecMode.UPDATE_OR_INSERT);
                        context.doImport(spec);
                        final ValueContextForUpdate properties = context.getContext(child.getAdaptationName());
                        properties.setValue(Boolean.TRUE, ACTIVATED);
                        properties.setValue("3046", DEFAULT_SELECTION);
                        context.doModifyContent(child, properties);
                    }
                });
                if (result.hasFailed()) {
                    log.error("[manufacturing] perspective not installed: "
                        + result.getExceptionFullMessage(Locale.ENGLISH));
                    return false;
                } else {
                    log.info("[manufacturing] perspective " + KEY + " installed and activated");
                }
            } finally {
                Files.deleteIfExists(xml);
            }
        } catch (final Exception ex) {
            log.error("[manufacturing] perspective installation failed", ex);
            return false;
        }
        return true;
    }

    /** Repair only the missing selectors produced by the first bootstrap revision. */
    private static boolean repairStagingTargets(final AdaptationHome home, final Session session,
        final LoggingCategory log) {
        final Adaptation perspective = home.findAdaptationOrNull(AdaptationName.forName(KEY));
        final String[][] targets = {
            {"3301", "SapBusinessPartnerSource", "/root/SapBusinessPartner"},
            {"3302", "SapSupplierSource", "/root/SapSupplier"},
            {"3303", "SalesforceCustomerSource", "/root/SfAccount"}
        };
        final ProcedureResult result = ProgrammaticService.createForSession(session, home).execute(context -> {
            context.setAllPrivileges(true);
            final com.onwbp.adaptation.AdaptationTable menu = perspective.getTable(Path.parse("/domain/menuItem"));
            for (String[] target : targets) {
                final Adaptation item = menu.lookupAdaptationByPrimaryKey(
                    com.onwbp.adaptation.PrimaryKey.parseString(target[0]));
                if (item == null) continue;
                final Path paramsPath = Path.parse("action/serviceParameters");
                final ValueContextForUpdate row = context.getContext(item.getAdaptationName());
                final com.orchestranetworks.schema.SchemaNode group = row.getNode(paramsPath);
                final java.util.List<?> existing = (java.util.List<?>) item.get(paramsPath);
                if (existing == null) continue;
                boolean matchingBranch = false, matchingDataset = false, hasSelector = false;
                for (Object parameter : existing) {
                    Object name = group.getNode(Path.parse("name")).executeRead(parameter);
                    Object value = group.getNode(Path.parse("value")).executeRead(parameter);
                    if ("branch".equals(name)) matchingBranch = target[1].equals(value);
                    if ("instance".equals(name)) matchingDataset = target[1].equals(value);
                    if ("xpath".equals(name)) hasSelector = true;
                }
                if (!matchingBranch || !matchingDataset || hasSelector) continue;
                final java.util.List<Object> parameters = new java.util.ArrayList<Object>(existing);
                final Object selector = group.createNewOccurrence();
                group.getNode(Path.parse("name")).executeWrite("xpath", selector);
                group.getNode(Path.parse("value")).executeWrite(target[2], selector);
                parameters.add(selector);
                row.setValue(parameters, paramsPath);
                context.doModifyContent(item, row);
            }
        });
        if (result.hasFailed()) {
            log.error("[manufacturing] staging target repair failed: " + result.getExceptionFullMessage(Locale.ENGLISH));
            return false;
        }
        return true;
    }
}

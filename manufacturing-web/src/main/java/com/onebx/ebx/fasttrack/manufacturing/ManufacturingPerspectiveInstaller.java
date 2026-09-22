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
            if (!ensureSourceHierarchy(home, session, log)) return false;
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

    /** Adds the source perspective's business-domain groups without overwriting administrator edits. */
    private static boolean ensureSourceHierarchy(final AdaptationHome home, final Session session,
        final LoggingCategory log) {
        final Adaptation perspective = home.findAdaptationOrNull(AdaptationName.forName(KEY));
        final String[][] groups = {
            {"3100", "Materials & Products", "3000", "10"},
            {"3101", "Plants & Facilities", "3000", "20"},
            {"3102", "Bill of Materials & Routing", "3000", "30"},
            {"3103", "Inventory & Warehousing", "3000", "40"},
            {"3104", "Quality & Compliance", "3000", "50"},
            {"3105", "Maintenance & Service", "3000", "60"},
            {"3106", "Logistics & Supply Chain", "3000", "70"},
            {"3107", "Customers & Commercial", "3000", "80"},
            {"3108", "Suppliers & Procurement", "3000", "90"}
        };
        final ProcedureResult result = ProgrammaticService.createForSession(session, home).execute(context -> {
            context.setAllPrivileges(true);
            final com.onwbp.adaptation.AdaptationTable menu = perspective.getTable(Path.parse("/domain/menuItem"));
            for (String[] group : groups) {
                if (menu.lookupAdaptationByPrimaryKey(com.onwbp.adaptation.PrimaryKey.parseString(group[0])) != null) continue;
                final ValueContextForUpdate row = context.getContextForNewOccurrence(menu);
                row.setValue("group", Path.parse("type"));
                row.setValue(group[2], Path.parse("parent"));
                row.setValue(Integer.valueOf(group[3]), Path.parse("order"));
                row.setValue(Boolean.FALSE, Path.parse("hasTopSeparator"));
                final com.orchestranetworks.schema.SchemaNode docs = row.getNode(Path.parse("label/localizedDocumentations"));
                final Object doc = docs.createNewOccurrence();
                docs.getNode(Path.parse("locale")).executeWrite("en_US", doc);
                docs.getNode(Path.parse("label")).executeWrite(group[1], doc);
                java.util.List<Object> labels = new java.util.ArrayList<Object>(); labels.add(doc);
                row.setValue(labels, Path.parse("label/localizedDocumentations"));
                context.doCreateOccurrence(row, menu);
            }
            final String[][] moves = {{"3046", "3108"}, {"3053", "3107"}, {"3060", "3100"}, {"3061", "3101"}};
            for (String[] move : moves) {
                final Adaptation item = menu.lookupAdaptationByPrimaryKey(com.onwbp.adaptation.PrimaryKey.parseString(move[0]));
                if (item == null) continue;
                final ValueContextForUpdate row = context.getContext(item.getAdaptationName());
                if (!move[1].equals(item.get(Path.parse("parent")))) {
                    row.setValue(move[1], Path.parse("parent"));
                    context.doModifyContent(item, row);
                }
            }
        });
        if (result.hasFailed()) {
            log.error("[manufacturing] source hierarchy repair failed: " + result.getExceptionFullMessage(Locale.ENGLISH));
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

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.orchestranetworks.instance.*" %>
<%@ page import="com.onwbp.adaptation.*" %>
<%@ page import="com.orchestranetworks.service.*" %>
<%@ page import="com.orchestranetworks.schema.*" %>
<%@ page import="java.util.*" %>
<%
    StringBuilder log = new StringBuilder();
    try {
        Repository repo = Repository.getDefault();
        Session session = SessionImpl.createSessionForSystemUser();

        AdaptationHome viewsHome = repo.lookupHome(HomeKey.forBranchName("ebx-views"));
        if (viewsHome == null) {
            viewsHome = repo.lookupHome(HomeKey.forBranchName("Bebx-views"));
        }

        if (viewsHome == null) {
            log.append("ERROR: ebx-views dataspace not found!\n");
        } else {
            Adaptation viewsDs = viewsHome.findAdaptationOrNull(AdaptationName.forName("ebx-views"));
            if (viewsDs == null) {
                log.append("ERROR: ebx-views dataset not found!\n");
            } else {
                AdaptationTable viewsTable = viewsDs.getTable(Path.parse("/preferences/tableViews"));
                log.append("Found /preferences/tableViews table in ebx-views.\n");

                String[][] viewDefs = {
                    {"media_mdm_global_supplier", "Global Supplier View", "GLOBAL_SUPPLIER"},
                    {"media_mdm_emea_supplier", "EMEA Supplier View", "EMEA_SUPPLIER"},
                    {"media_mdm_latam_supplier", "LATAM Supplier View", "LATAM_SUPPLIER"},
                    {"media_mdm_paid_social", "Paid Social Taxonomy", "PAID_SOCIAL"}
                };

                for (String[] v : viewDefs) {
                    final String pubName = v[0];
                    final String label = v[1];
                    final String hierCode = v[2];

                    PrimaryKey pk = PrimaryKey.parseString(pubName);
                    if (viewsTable.lookupAdaptationByPrimaryKey(pk) != null) {
                        log.append("View '").append(pubName).append("' already exists in ebx-views — skipping.\n");
                        continue;
                    }

                    ProgrammaticService ps = ProgrammaticService.createForSession(session, viewsHome);
                    ProcedureResult pr = ps.execute(new Procedure() {
                        @Override
                        public void execute(ProcedureContext context) throws Exception {
                            context.setAllPrivileges(true);
                            ValueContextForUpdate vc = context.getContextForNewOccurrence(viewsTable);
                            vc.setValue(pubName, Path.parse("./id"));
                            vc.setValue(pubName, Path.parse("./publicationName"));
                            vc.setValue("module: manufacturing-web, path: /WEB-INF/ebx/schemas/MfgSupplierManagement.xsd", Path.parse("./schemaKey"));
                            vc.setValue("/root/SupplierHierarchy", Path.parse("./tablePath"));
                            vc.setValue("h", Path.parse("./presentationMode"));
                            vc.setValue("Beveryone", Path.parse("./owner"));

                            String expr = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                "<dimension version=\"3\"><levelNode><dimensionTablePath>/root/SupplierHierarchy</dimensionTablePath><reference>target:/root/SupplierHierarchy</reference><labelRenderer><labelRendererLocalizedExpressions><locale>default</locale><expression>${./supplierHierarchyId}</expression></labelRendererLocalizedExpressions></labelRenderer><predicate>hierarchyCode = '" + hierCode + "'</predicate></levelNode></dimension>";

                            vc.setValue(expr, Path.parse("./hierarchicalView/configuration/hierarchyExpression"));
                            context.doCreateOccurrence(vc, viewsTable);
                        }
                    });

                    if (pr.hasFailed()) {
                        log.append("FAILED to create view ").append(pubName).append(": ").append(pr.getException().getMessage()).append("\n");
                    } else {
                        log.append("SUCCESS: Created view '").append(pubName).append("' (").append(label).append(")\n");
                    }
                }
            }
        }
    } catch (Exception e) {
        log.append("EXCEPTION: ").append(e.getMessage()).append("\n");
        java.io.StringWriter sw = new java.io.StringWriter();
        e.printStackTrace(new java.io.PrintWriter(sw));
        log.append(sw.toString());
    }
%>
<html>
<head><title>Seed Hierarchy Custom Views Result</title></head>
<body style="font-family: sans-serif; padding: 20px;">
<h2>Hierarchy Custom Views Seeding Log</h2>
<pre style="background: #222; color: #0f0; padding: 15px; border-radius: 5px;"><%= log.toString() %></pre>
</body>
</html>

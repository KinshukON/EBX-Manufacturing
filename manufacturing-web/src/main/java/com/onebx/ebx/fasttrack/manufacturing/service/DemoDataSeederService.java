package com.onebx.ebx.fasttrack.manufacturing.service;

import com.onebx.ebx.fasttrack.manufacturing.ModuleNames;
import com.onebx.ebx.fasttrack.manufacturing.ManufacturingPerspectiveInstaller;
import com.onwbp.adaptation.Adaptation;
import com.onwbp.adaptation.AdaptationHome;
import com.onwbp.adaptation.AdaptationTable;
import com.onwbp.adaptation.PrimaryKey;
import com.orchestranetworks.instance.HomeKey;
import com.orchestranetworks.instance.HomeCreationSpec;
import com.orchestranetworks.instance.Repository;
import com.orchestranetworks.schema.Path;
import com.orchestranetworks.schema.SchemaLocation;
import com.orchestranetworks.service.*;
import com.orchestranetworks.ui.selection.DatasetEntitySelection;
import com.orchestranetworks.userservice.*;

import java.util.Date;

public class DemoDataSeederService implements UserService<DatasetEntitySelection> {

    private int step = 1;
    private boolean seedSucceeded = false;
    private String seedLog = "";

    private static final String[][] SCHEMAS = {
        // Governance/Perspective
        { "BManufacturing", "Manufacturing", "Manufacturing.xsd", "Manufacturing Home" },

        // Reference / Customize Data
        { "BMfgReference", "MfgReferenceData", "ManufacturingReferenceData.xsd", "Manufacturing Reference Data" },

        // Staging sources
        { "CommercialSourceMapping", "CommercialSourceMapping", "sources/CommercialSourceMapping.xsd", "Commercial Source Mapping" },
        { "BMfgCommercialSourceReference", "MfgCommercialSourceReference", "sources/MfgCommercialSourceReference.xsd", "Commercial Source Reference" },
        { "SapBusinessPartnerSource", "SapBusinessPartnerSource", "sources/SapBusinessPartnerSource.xsd", "SAP Business Partner Source" },
        { "SapCustomerSource", "SapCustomerSource", "sources/SapCustomerSource.xsd", "SAP Customer Source" },
        { "SapReferenceSource", "SapReferenceSource", "sources/SapReferenceSource.xsd", "SAP Reference Source" },
        { "SapSupplierSource", "SapSupplierSource", "sources/SapSupplierSource.xsd", "SAP Supplier Source" },

        { "SapMaterialSource", "SapMaterialSource", "sources/SapMaterialSource.xsd", "SAP Material Source" },
        { "BMfgProductReference", "MfgProductReference", "MfgProductReference.xsd", "Product Reference" },
        { "BMfgProductManagement", "MfgProductManagement", "MfgProductManagement.xsd", "Product Management" },
        { "BMfgProductVariantReference", "MfgProductVariantReference", "MfgProductVariantReference.xsd", "Product Variant Reference" },
        { "BMfgProductVariantManagement", "MfgProductVariantManagement", "MfgProductVariantManagement.xsd", "Product Variant Management" },
        { "BMfgWorkOrderReference", "MfgWorkOrderReference", "MfgWorkOrderReference.xsd", "Work Order Reference" },

        // Salesforce CRM
        { "SalesforceCustomerSource", "SalesforceCustomerSource", "sources/SalesforceCustomerSource.xsd", "Salesforce Customer Source" },
        { "SalesforceReferenceSource", "SalesforceReferenceSource", "sources/SalesforceReferenceSource.xsd", "Salesforce Reference Source" },

        // SAP Ariba (SLP)
        { "AribaSupplierSource", "AribaSupplierSource", "sources/AribaSupplierSource.xsd", "Ariba Supplier Source" },
        { "AribaReferenceSource", "AribaReferenceSource", "sources/AribaReferenceSource.xsd", "Ariba Reference Source" },

        // Oracle E-Business Suite
        { "OracleEbsCustomerSource", "OracleEbsCustomerSource", "sources/OracleEbsCustomerSource.xsd", "Oracle EBS Customer Source" },
        { "OracleEbsSupplierSource", "OracleEbsSupplierSource", "sources/OracleEbsSupplierSource.xsd", "Oracle EBS Supplier Source" },
        { "OracleEbsItemSource", "OracleEbsItemSource", "sources/OracleEbsItemSource.xsd", "Oracle EBS Item Source" },
        { "OracleEbsReferenceSource", "OracleEbsReferenceSource", "sources/OracleEbsReferenceSource.xsd", "Oracle EBS Reference Source" },

        // Oracle Cloud ERP
        { "OracleCloudCustomerSource", "OracleCloudCustomerSource", "sources/OracleCloudCustomerSource.xsd", "Oracle Cloud Customer Source" },
        { "OracleCloudSupplierSource", "OracleCloudSupplierSource", "sources/OracleCloudSupplierSource.xsd", "Oracle Cloud Supplier Source" },
        { "OracleCloudItemSource", "OracleCloudItemSource", "sources/OracleCloudItemSource.xsd", "Oracle Cloud Item Source" },
        { "OracleCloudReferenceSource", "OracleCloudReferenceSource", "sources/OracleCloudReferenceSource.xsd", "Oracle Cloud Reference Source" },

        // Microsoft Dynamics 365
        { "DynamicsCustomerSource", "DynamicsCustomerSource", "sources/DynamicsCustomerSource.xsd", "Dynamics 365 Customer Source" },
        { "DynamicsReferenceSource", "DynamicsReferenceSource", "sources/DynamicsReferenceSource.xsd", "Dynamics 365 Reference Source" },

        // Siemens Teamcenter PLM
        { "TeamcenterProductSource", "TeamcenterProductSource", "sources/TeamcenterProductSource.xsd", "Teamcenter Product Source" },
        { "TeamcenterReferenceSource", "TeamcenterReferenceSource", "sources/TeamcenterReferenceSource.xsd", "Teamcenter Reference Source" },

        // PTC Windchill PLM
        { "WindchillProductSource", "WindchillProductSource", "sources/WindchillProductSource.xsd", "Windchill Product Source" },
        { "WindchillReferenceSource", "WindchillReferenceSource", "sources/WindchillReferenceSource.xsd", "Windchill Reference Source" },

        // EAM / Maximo
        { "EamAssetSource", "EamAssetSource", "sources/EamAssetSource.xsd", "EAM Asset Source" },
        { "EamReferenceSource", "EamReferenceSource", "sources/EamReferenceSource.xsd", "EAM Reference Source" },

        // Legacy Mainframe
        { "LegacyMainframeSource", "LegacyMainframeSource", "sources/LegacyMainframeSource.xsd", "Legacy Mainframe Source" },

        // Canonical MDG models relevant to Supplier, Advertiser, Material, and Plant
        { "BMfgSupplierManagement", "MfgSupplierManagement", "MfgSupplierManagement.xsd", "Manufacturing Supplier Management" },
        { "BMfgSupplierReference", "MfgSupplierReference", "MfgSupplierReference.xsd", "Manufacturing Supplier Reference" },
        { "BMfgCustomerManagement", "MfgCustomerManagement", "MfgCustomerManagement.xsd", "Manufacturing Customer Management" },
        { "BMfgCustomerReference", "MfgCustomerReference", "MfgCustomerReference.xsd", "Manufacturing Customer Reference" },
        { "BMfgMaterialManagement", "MfgMaterialManagement", "MfgMaterialManagement.xsd", "Manufacturing Material Management" },
        { "BMfgMaterialReference", "MfgMaterialReference", "MfgMaterialReference.xsd", "Manufacturing Material Reference" },
        { "BMfgSitePlantManagement", "MfgSitePlantManagement", "MfgSitePlantManagement.xsd", "Manufacturing Site/Plant Management" },
        { "BMfgSitePlantReference", "MfgSitePlantReference", "MfgSitePlantReference.xsd", "Manufacturing Site/Plant Reference" }
    };

    @Override
    public void setupObjectContext(UserServiceSetupObjectContext<DatasetEntitySelection> aContext, UserServiceObjectContextBuilder aBuilder) {
        // No additional object context needed.
    }

    @Override
    public void setupDisplay(UserServiceSetupDisplayContext<DatasetEntitySelection> aContext, UserServiceDisplayConfigurator aConfigurator) {
        if (step == 1) {
            aConfigurator.setContent(new UserServicePane() {
                @Override
                public void writePane(UserServicePaneContext aPaneContext, UserServicePaneWriter aWriter) {
                    aWriter.add("<div style=\"margin: 20px;\">");
                    aWriter.add("<h2 style=\"color:#2C5E8A; margin-bottom: 10px;\">🚀 Manufacturing Demo Data Seeder</h2>");
                    aWriter.add("<p>This service will programmatically initialize all Manufacturing master data models and populate them with standard seed data.</p>");
                    aWriter.add("<p><b>What will be created/verified:</b></p>");
                    aWriter.add("<ul>");
                    aWriter.add("<li><b>34 Master/Staging Dataspaces and Datasets</b> (e.g. <code>BMfgMaterialManagement</code>, <code>BMfgSitePlantManagement</code>, etc.)</li>");
                    aWriter.add("<li><b>Reference Data Catalogs</b> (Currencies, Country Key US, Region CA, Language EN)</li>");
                    aWriter.add("<li><b>Raw Staging Records</b> (SAP Business Partner staging record)</li>");
                    aWriter.add("<li><b>Canonical Master Data</b> (Plants, Materials, BOM headers)</li>");
                    aWriter.add("</ul>");
                    aWriter.add("<p style=\"color:#555;\">Existing records will not be duplicated. The operation is safe and idempotent.</p>");
                    aWriter.add("</div>");
                }
            });

            aConfigurator.setLeftButtons(aConfigurator.newCancelButton());
            aConfigurator.setRightButtons(aConfigurator.newSubmitButton("Seed Manufacturing Data", new UserServiceEvent() {
                @Override
                public UserServiceEventOutcome processEvent(UserServiceEventContext aEventContext) {
                    try {
                        Repository repo = aContext.getEntitySelection().getDataset().getHome().getRepository();
                        Session session = aEventContext.getSession();
                        executeSeed(repo, session);
                        seedSucceeded = true;
                    } catch (Exception ex) {
                        seedSucceeded = false;
                        seedLog = "❌ Error during seeding: " + ex.toString();
                    }
                    step = 2;
                    return null;
                }
            }));
        } else {
            aConfigurator.setContent(new UserServicePane() {
                @Override
                public void writePane(UserServicePaneContext aPaneContext, UserServicePaneWriter aWriter) {
                    aWriter.add("<div style=\"margin: 20px;\">");
                    if (seedSucceeded) {
                        aWriter.add("<h2 style=\"color:green; margin-bottom: 10px;\">✅ Seeding Completed Successfully!</h2>");
                        aWriter.add("<p>All 34 dataspaces and datasets have been verified/created, and initial staging/reference data has been populated.</p>");
                        aWriter.add("<pre style=\"background:#f5f5f5; padding:15px; border-radius:4px; border:1px solid #ddd; max-height: 400px; overflow-y: auto; font-family: monospace;\">" + seedLog + "</pre>");
                    } else {
                        aWriter.add("<h2 style=\"color:red; margin-bottom: 10px;\">❌ Seeding Failed</h2>");
                        aWriter.add("<pre style=\"background:#fff0f0; padding:15px; border-radius:4px; border:1px solid #fdd; color:red; font-family: monospace;\">" + seedLog + "</pre>");
                    }
                    aWriter.add("</div>");
                }
            });
            aConfigurator.setRightButtons(aConfigurator.newCloseButton());
        }
    }

    @Override
    public void validate(UserServiceValidateContext<DatasetEntitySelection> aContext) {
    }

    @Override
    public UserServiceEventOutcome processEventOutcome(UserServiceProcessEventOutcomeContext<DatasetEntitySelection> aContext, UserServiceEventOutcome anEventOutcome) {
        return anEventOutcome;
    }

    private void executeSeed(Repository repo, Session session) throws OperationException {
        StringBuilder log = new StringBuilder();
        provision(repo, session, log);
        seedReferenceData(repo, session, log);
        seedLog = log.toString();
    }

    /** Creates structure only; startup never inserts demo records. */
    public void provision(Repository repo, Session session, StringBuilder log) throws OperationException {

        for (String foundation : new String[] { "CommonReferenceData", "Geographies", "UOM" }) {
            AdaptationHome foundationHome = repo.lookupHome(HomeKey.forBranchName(foundation));
            if (foundationHome == null || foundationHome.findAdaptationOrNull(
                    com.onwbp.adaptation.AdaptationName.forName(foundation)) == null) {
                throw OperationException.createError("Reference Data foundation is not ready: " + foundation);
            }
        }

        // Resolve every foreign-key dataspace before the first schema is compiled.
        ensureDataspace(repo, "BMfgCommercialSourceManagement", "Commercial Source Management", null, session, log);
        for (String[] definition : SCHEMAS) {
            ensureDataspace(repo, definition[0], definition[3],
                definition[2].startsWith("sources/") ? "BMfgCommercialSourceManagement" : null, session, log);
        }

        // Ensure Commercial Source Management Parent Dataspace exists under Reference
        ensureDataspaceAndDataset(repo, "BMfgCommercialSourceManagement", "CommercialSourceManagement",
            ModuleNames.schema(ModuleNames.MANUFACTURING, "sources/CommercialSourceMapping.xsd"),
            "Commercial Source Management", null, session, log);

        // 2. Ensure all Manufacturing dataspaces & datasets exist (fail-safe)
        for (String[] schemaInfo : SCHEMAS) {
            String branchName = schemaInfo[0];
            String datasetName = schemaInfo[1];
            String schemaPath = schemaInfo[2];
            String label = schemaInfo[3];
            String schemaLocStr = ModuleNames.schema(ModuleNames.MANUFACTURING, schemaPath);

            String parentBranch = null;
            if (schemaPath.startsWith("sources/")) {
                parentBranch = "BMfgCommercialSourceManagement";
            }

            ensureDataspaceAndDataset(repo, branchName, datasetName, schemaLocStr, label, parentBranch, session, log);
        }

        // Earlier deployments may have cached errors while FK homes were missing.
        // EBX requires model refresh outside a Procedure; all writes above are committed.
        repo.refreshSchemas(true);
        for (String[] definition : SCHEMAS) {
            Adaptation dataset = repo.lookupHome(HomeKey.forBranchName(definition[0]))
                .findAdaptationOrNull(com.onwbp.adaptation.AdaptationName.forName(definition[1]));
            dataset.getSchemaNode(); // Fails bootstrap if the model remains unavailable.
        }

    }

    public void autoSeed(Repository repo, Session session) throws OperationException {
        executeSeed(repo, session);
        LoggingCategory.getKernel().info("[DemoDataSeederService] Auto-seed log:\n" + this.seedLog);
    }

    private void ensureDataspaceAndDataset(Repository repo, String branchName, String datasetName,
            String schemaLocStr, String label, Session session, StringBuilder log) throws OperationException {
        ensureDataspaceAndDataset(repo, branchName, datasetName, schemaLocStr, label, null, session, log);
    }

    private void ensureDataspaceAndDataset(Repository repo, String branchName, String datasetName,
            String schemaLocStr, String label, String parentBranchName, Session session, StringBuilder log) throws OperationException {
        AdaptationHome home = ensureDataspace(repo, branchName, label, parentBranchName, session, log);

        SchemaLocation schemaLoc = SchemaLocation.parse(schemaLocStr);
        com.onwbp.adaptation.AdaptationReference datasetRef = com.onwbp.adaptation.AdaptationReference.forPersistentName(datasetName);
        Adaptation dataset = home.findAdaptationOrNull(datasetRef);
        if (dataset != null) {
            String existingSchemaLoc = dataset.getSchemaLocation().format();
            if (!existingSchemaLoc.equals(schemaLocStr)) {
                throw OperationException.createError("Dataset " + branchName + "/" + datasetName
                    + " uses " + existingSchemaLoc + "; expected " + schemaLocStr
                    + ". Existing data was preserved. Migrate the schema before retrying.");
            }
        }

        if (dataset == null) {
            ProgrammaticService service = ProgrammaticService.createForSession(session, home);
            final com.onwbp.adaptation.AdaptationReference finalRef = datasetRef;
            final SchemaLocation finalSchema = schemaLoc;
            ProcedureResult pr = service.execute(new Procedure() {
                @Override
                public void execute(ProcedureContext context) throws Exception {
                    context.setAllPrivileges(true);
                    context.doCreateRoot(finalSchema, finalRef, Profile.ADMINISTRATOR);
                }
            });
            if (pr.hasFailed()) {
                throw pr.getException();
            }
            log.append("Created dataset: ").append(datasetName).append(" in dataspace: ").append(branchName).append("\n");
        } else {
            log.append("Verified dataset: ").append(datasetName).append(" (already exists)\n");
        }
    }

    private AdaptationHome ensureDataspace(Repository repo, String branchName, String label,
            String parentBranchName, Session session, StringBuilder log) throws OperationException {
        if (branchName != null && branchName.length() > 32) {
            throw OperationException.createError("Dataspace exceeds EBX 32 character limit: " + branchName);
        }
        HomeKey key = HomeKey.forBranchName(branchName);
        AdaptationHome home = repo.lookupHome(key);
        if (home == null) {
            HomeCreationSpec spec = new HomeCreationSpec();
            spec.setKey(key);
            spec.setLabel(com.onwbp.base.text.UserMessage.createInfo(label));
            spec.setOwner(Profile.ADMINISTRATOR);
            AdaptationHome parentHome = null;
            if (parentBranchName != null) {
                parentHome = repo.lookupHome(HomeKey.forBranchName(parentBranchName));
            }
            if (parentHome != null) {
                spec.setParent(parentHome);
                spec.setHomeToCopyPermissionsFrom(parentHome);
            } else {
                spec.setParent(repo.getReferenceBranch());
                spec.setHomeToCopyPermissionsFrom(repo.getReferenceBranch());
            }
            home = repo.createHome(spec, session);
            log.append("Created dataspace: ").append(branchName).append("\n");
        } else {
            log.append("Verified dataspace: ").append(branchName).append(" (already exists)\n");
        }

        return home;
    }

    private Adaptation findDataset(Repository repo, String branchName, String datasetName) {
        AdaptationHome home = repo.lookupHome(HomeKey.forBranchName(branchName));
        if (home == null && !branchName.startsWith("BB")) {
            home = repo.lookupHome(HomeKey.forBranchName("B" + branchName));
        }
        if (home == null) return null;
        return home.findAdaptationOrNull(com.onwbp.adaptation.AdaptationName.forName(datasetName));
    }

    private void seedReferenceData(Repository repo, Session session, StringBuilder log) throws OperationException {
        // Find datasets with dual-prefix fallback
        Adaptation csrDs = findDataset(repo, "BMfgCommercialSourceReference", "MfgCommercialSourceReference");
        Adaptation refDs = findDataset(repo, "SapReferenceSource", "SapReferenceSource");
        Adaptation bpDs = findDataset(repo, "SapBusinessPartnerSource", "SapBusinessPartnerSource");
        Adaptation siteDs = findDataset(repo, "BMfgSitePlantManagement", "MfgSitePlantManagement");
        Adaptation matDs = findDataset(repo, "BMfgMaterialManagement", "MfgMaterialManagement");

        // Seed Commercial Source Reference
        if (csrDs != null) {
            createRecordIfAbsent(session, csrDs, "/root/CommercialSourceSystem", "ECC_NA", new RecordPopulator() {
                @Override
                public void populate(ValueContextForUpdate vc) {
                    vc.setValue("ECC_NA", Path.parse("./code"));
                    vc.setValue("SAP ECC North America", Path.parse("./name"));
                    vc.setValue("Active", Path.parse("./status"));
                }
            }, log);

            createRecordIfAbsent(session, csrDs, "/root/CommercialSourceLoadStatus", "LOADED", new RecordPopulator() {
                @Override
                public void populate(ValueContextForUpdate vc) {
                    vc.setValue("LOADED", Path.parse("./code"));
                    vc.setValue("Loaded", Path.parse("./name"));
                    vc.setValue("Active", Path.parse("./status"));
                }
            }, log);

            createRecordIfAbsent(session, csrDs, "/root/CommercialSourceMappingStatus", "APPROVED", new RecordPopulator() {
                @Override
                public void populate(ValueContextForUpdate vc) {
                    vc.setValue("APPROVED", Path.parse("./code"));
                    vc.setValue("Approved", Path.parse("./name"));
                    vc.setValue("Active", Path.parse("./status"));
                }
            }, log);

            createRecordIfAbsent(session, csrDs, "/root/CommercialSourceBatch", "LOAD-20260501-001", new RecordPopulator() {
                @Override
                public void populate(ValueContextForUpdate vc) {
                    vc.setValue("LOAD-20260501-001", Path.parse("./batchId"));
                    vc.setValue("ECC_NA", Path.parse("./sourceSystem"));
                    vc.setValue("Acme Initial Load", Path.parse("./batchName"));
                    vc.setValue("COMPLETED", Path.parse("./loadStatus"));
                }
            }, log);
        }

        // Seed SAP Reference Source
        if (refDs != null) {
            createRecordIfAbsent(session, refDs, "/root/SapCountryKey", "US", new RecordPopulator() {
                @Override
                public void populate(ValueContextForUpdate vc) {
                    vc.setValue("US", Path.parse("./countryKey"));
                    vc.setValue("United States", Path.parse("./description"));
                    vc.setValue("Active", Path.parse("./status"));
                }
            }, log);

            createRecordIfAbsent(session, refDs, "/root/SapRegion", "CA", new RecordPopulator() {
                @Override
                public void populate(ValueContextForUpdate vc) {
                    vc.setValue("CA", Path.parse("./regionId"));
                    vc.setValue("California", Path.parse("./description"));
                    vc.setValue("Active", Path.parse("./status"));
                }
            }, log);

            createRecordIfAbsent(session, refDs, "/root/SapLanguageKey", "EN", new RecordPopulator() {
                @Override
                public void populate(ValueContextForUpdate vc) {
                    vc.setValue("EN", Path.parse("./languageCode"));
                    vc.setValue("English", Path.parse("./description"));
                    vc.setValue("Active", Path.parse("./status"));
                }
            }, log);

            createRecordIfAbsent(session, refDs, "/root/SapIndustrySector", "MFG_AUTO", new RecordPopulator() {
                @Override
                public void populate(ValueContextForUpdate vc) {
                    vc.setValue("MFG_AUTO", Path.parse("./industrySector"));
                    vc.setValue("Automotive Manufacturing", Path.parse("./description"));
                    vc.setValue("Active", Path.parse("./status"));
                }
            }, log);

            createRecordIfAbsent(session, refDs, "/root/SapAccountGroup", "ZCUST", new RecordPopulator() {
                @Override
                public void populate(ValueContextForUpdate vc) {
                    vc.setValue("ZCUST", Path.parse("./accountGroup"));
                    vc.setValue("Customer Account Group", Path.parse("./description"));
                    vc.setValue("Active", Path.parse("./status"));
                }
            }, log);

            createRecordIfAbsent(session, refDs, "/root/SapClassType", "B2B", new RecordPopulator() {
                @Override
                public void populate(ValueContextForUpdate vc) {
                    vc.setValue("B2B", Path.parse("./classType"));
                    vc.setValue("Business to Business", Path.parse("./description"));
                    vc.setValue("Active", Path.parse("./status"));
                }
            }, log);

            createRecordIfAbsent(session, refDs, "/root/SapPlant", "1001", new RecordPopulator() {
                @Override
                public void populate(ValueContextForUpdate vc) {
                    vc.setValue("1001", Path.parse("./plant"));
                    vc.setValue("Acme NA Assembly Plant", Path.parse("./description"));
                    vc.setValue("Active", Path.parse("./status"));
                }
            }, log);
        }

        // Seed SAP Business Partner staging records
        if (bpDs != null) {
            createRecordIfAbsent(session, bpDs, "/root/SapBusinessPartner", "BP00010001", new RecordPopulator() {
                @Override
                public void populate(ValueContextForUpdate vc) {
                    vc.setValue("BP00010001", Path.parse("./bpId"));
                    vc.setValue("200010001", Path.parse("./bpNumber"));
                    vc.setValue("ZCUST", Path.parse("./bpGrouping"));
                    vc.setValue("B2B", Path.parse("./bpCategory"));
                    vc.setValue("CUS", Path.parse("./bpType"));
                    vc.setValue("ECC_NA", Path.parse("./sourceSystem"));
                    vc.setValue("800", Path.parse("./sourceClient"));
                    vc.setValue("BUT000", Path.parse("./sourceTableName"));
                    vc.setValue("BP00010001", Path.parse("./sourceRecordId"));
                    vc.setValue("800|BP00010001", Path.parse("./sourceRecordCompositeKey"));
                    vc.setValue("LOAD-20260501-001", Path.parse("./sourceLoadBatchId"));
                    vc.setValue(new Date(), Path.parse("./inboundLoadDate"));
                    vc.setValue("LOADED", Path.parse("./inboundLoadStatus"));
                    vc.setValue("APPROVED", Path.parse("./mappingStatus"));
                }
            }, log);
        }

        // Seed Sites & Plants canonical master records
        if (siteDs != null) {
            createRecordIfAbsent(session, siteDs, "/root/Site", "1001", new RecordPopulator() {
                @Override
                public void populate(ValueContextForUpdate vc) {
                    vc.setValue("1001", Path.parse("./siteId"));
                    vc.setValue("PLANT", Path.parse("./siteType"));
                    vc.setValue("ACTIVE", Path.parse("./siteStatus"));
                    vc.setValue("ECC_NA", Path.parse("./sourceSystem"));
                    vc.setValue("1001", Path.parse("./externalCode"));
                }
            }, log);
        }

        // Seed Materials canonical master records
        if (matDs != null) {
            createRecordIfAbsent(session, matDs, "/root/Material", "MAT00010001", new RecordPopulator() {
                @Override
                public void populate(ValueContextForUpdate vc) {
                    vc.setValue("MAT00010001", Path.parse("./materialId"));
                    vc.setValue("RAW", Path.parse("./materialType"));
                    vc.setValue("ACTIVE", Path.parse("./materialStatus"));
                    vc.setValue("AUTO", Path.parse("./materialCategory"));
                    vc.setValue("MRP", Path.parse("./materialPlanningType"));
                    vc.setValue("BUY", Path.parse("./procurementType"));
                    vc.setValue("BATCH", Path.parse("./traceabilityLevel"));
                    vc.setValue("ECC_NA", Path.parse("./sourceSystem"));
                    vc.setValue("MAT10001", Path.parse("./externalCode"));
                }
            }, log);
        }

        // Seed Supplier Reference Data
        Adaptation supRefDs = findDataset(repo, "BMfgSupplierReference", "MfgSupplierReference");
        if (supRefDs != null) {
            createRecordIfAbsent(session, supRefDs, "/root/RefSupplierType", "SUPPLIER", new RecordPopulator() {
                @Override
                public void populate(ValueContextForUpdate vc) {
                    vc.setValue("SUPPLIER", Path.parse("./code"));
                    vc.setValue("Media Supplier", Path.parse("./label"));
                }
            }, log);
            createRecordIfAbsent(session, supRefDs, "/root/RefSupplierType", "ADVERTISER", new RecordPopulator() {
                @Override
                public void populate(ValueContextForUpdate vc) {
                    vc.setValue("ADVERTISER", Path.parse("./code"));
                    vc.setValue("Media Advertiser", Path.parse("./label"));
                }
            }, log);
            createRecordIfAbsent(session, supRefDs, "/root/RefSupplierStatus", "ACTIVE", new RecordPopulator() {
                @Override
                public void populate(ValueContextForUpdate vc) {
                    vc.setValue("ACTIVE", Path.parse("./code"));
                    vc.setValue("Active Governed Entity", Path.parse("./label"));
                }
            }, log);
            createRecordIfAbsent(session, supRefDs, "/root/RefPaymentTerms", "NET30", new RecordPopulator() {
                @Override
                public void populate(ValueContextForUpdate vc) {
                    vc.setValue("NET30", Path.parse("./code"));
                    vc.setValue("Net 30 Days", Path.parse("./label"));
                    vc.setValue("Standard 30 day commercial payment terms", Path.parse("./description"));
                }
            }, log);
            createRecordIfAbsent(session, supRefDs, "/root/RefIncoterms", "FOB", new RecordPopulator() {
                @Override
                public void populate(ValueContextForUpdate vc) {
                    vc.setValue("FOB", Path.parse("./code"));
                    vc.setValue("Free On Board", Path.parse("./label"));
                    vc.setValue("Free On Board shipping terms", Path.parse("./description"));
                }
            }, log);
            createRecordIfAbsent(session, supRefDs, "/root/RefCertificationType", "ISO9001", new RecordPopulator() {
                @Override
                public void populate(ValueContextForUpdate vc) {
                    vc.setValue("ISO9001", Path.parse("./code"));
                    vc.setValue("ISO 9001:2015 Quality Management", Path.parse("./label"));
                }
            }, log);
            createRecordIfAbsent(session, supRefDs, "/root/RefRiskType", "CYBER", new RecordPopulator() {
                @Override
                public void populate(ValueContextForUpdate vc) {
                    vc.setValue("CYBER", Path.parse("./code"));
                    vc.setValue("Cybersecurity Risk", Path.parse("./label"));
                }
            }, log);
            createRecordIfAbsent(session, supRefDs, "/root/RefRiskRating", "LOW", new RecordPopulator() {
                @Override
                public void populate(ValueContextForUpdate vc) {
                    vc.setValue("LOW", Path.parse("./code"));
                    vc.setValue("Low Risk Tier", Path.parse("./label"));
                }
            }, log);
        }

        // Seed Customer Reference Data
        Adaptation custRefDs = findDataset(repo, "BMfgCustomerReference", "MfgCustomerReference");
        if (custRefDs != null) {
            // Seed RefCustomerType
            createRecordIfAbsent(session, custRefDs, "/root/RefCustomerType", "ADVERTISER", new RecordPopulator() {
                @Override
                public void populate(ValueContextForUpdate vc) {
                    vc.setValue("ADVERTISER", Path.parse("./code"));
                    vc.setValue("Advertiser", Path.parse("./label"));
                }
            }, log);

            // Seed RefCustomerStatus
            createRecordIfAbsent(session, custRefDs, "/root/RefCustomerStatus", "ACTIVE", new RecordPopulator() {
                @Override
                public void populate(ValueContextForUpdate vc) {
                    vc.setValue("ACTIVE", Path.parse("./code"));
                    vc.setValue("Active", Path.parse("./label"));
                }
            }, log);
            createRecordIfAbsent(session, custRefDs, "/root/RefCustomerStatus", "INACTIVE", new RecordPopulator() {
                @Override
                public void populate(ValueContextForUpdate vc) {
                    vc.setValue("INACTIVE", Path.parse("./code"));
                    vc.setValue("Inactive", Path.parse("./label"));
                }
            }, log);

            // Seed RefCustomerSubtype
            String[][] subtypes = {
                {"CORPORATE_GROUP", "Corporate Group", "Consolidated global corporate holding parent"},
                {"LEGAL_ENTITY", "Legal Entity", "Official registered operating legal entity"},
                {"BRAND", "Brand", "Commercial brand or product line"},
                {"PLATFORM", "Media Platform", "Advertising media platform or channel"},
                {"ADVERTISER", "Advertiser", "Corporate brand advertising entity"},
                {"ADVERTISER_GROUP", "Advertiser Group", "Group parent of advertiser entities"}
            };
            for (String[] sub : subtypes) {
                final String[] sData = sub;
                createRecordIfAbsent(session, custRefDs, "/root/RefCustomerSubtype", sub[0], new RecordPopulator() {
                    @Override
                    public void populate(ValueContextForUpdate vc) {
                        vc.setValue(sData[0], Path.parse("./code"));
                        vc.setValue(sData[1], Path.parse("./label"));
                        vc.setValue(sData[2], Path.parse("./description"));
                        vc.setValue("Active", Path.parse("./status"));
                    }
                }, log);
            }

            // Seed RefCustomerHierarchyType
            String[][] hierTypes = {
                {"BRAND", "Brand Rollup", "Hierarchy rolling up brands under parents"},
                {"REGIONAL", "Regional Rollup", "Regional division rollup"},
                {"GLOBAL", "Global Rollup", "Global corporate rollup"}
            };
            for (String[] ht : hierTypes) {
                final String[] hData = ht;
                createRecordIfAbsent(session, custRefDs, "/root/RefCustomerHierarchyType", ht[0], new RecordPopulator() {
                    @Override
                    public void populate(ValueContextForUpdate vc) {
                        vc.setValue(hData[0], Path.parse("./code"));
                        vc.setValue(hData[1], Path.parse("./label"));
                        vc.setValue(hData[2], Path.parse("./description"));
                        vc.setValue("Active", Path.parse("./status"));
                    }
                }, log);
            }
        }

        // Seed Supplier Master canonical records
        Adaptation supDs = findDataset(repo, "BMfgSupplierManagement", "MfgSupplierManagement");
        if (supDs != null) {
            Object[][] suppliers = {
                {"GOLD_META_GROUP", "Meta Platforms Global Corporate Group", "Meta Platforms, Inc.", "Meta", null, "CORPORATE_GROUP", null, null, "US", "NA", true, "Consolidated global corporate holding parent for Meta Platforms.", "https://about.meta.com", "Media & Digital Advertising", "ACCEPTED", new java.math.BigDecimal("0.99"), "AI-verified corporate holding group", "SEC Filings, D&B Directory"},
                {"GOLD_META_US", "Meta Platforms, Inc.", "Meta Platforms, Inc.", "Meta US", "Facebook, Inc.", "LEGAL_ENTITY", "DEMO-US-987654", "DEMO-987654321", "US", "NA", true, "Canonical US operating legal entity for Meta Platforms.", "https://about.meta.com", "Media & Digital Advertising", "ACCEPTED", new java.math.BigDecimal("0.98"), "Steward approved canonical US entity", "Tax ID DEMO-US-987654, DUNS DEMO-987654321"},
                {"GOLD_META_IRELAND", "Meta Platforms Ireland Limited", "Meta Platforms Ireland Limited", "Meta Ireland", null, "LEGAL_ENTITY", "DEMO-IE-112233", null, "IE", "EMEA", true, "EMEA regional operating legal entity registered in Ireland.", "https://about.meta.com/ie", "Media & Digital Advertising", "ACCEPTED", new java.math.BigDecimal("0.97"), "EMEA corporate registry verified", "Ireland CRO Registry"},
                {"GOLD_FACEBOOK", "Facebook Ads", "Facebook Ads Platform", "Facebook", null, "PLATFORM", null, null, "US", "NA", true, "Global social media advertising platform channel.", "https://www.facebook.com/business", "Media & Digital Advertising", "ACCEPTED", new java.math.BigDecimal("0.96"), "Platform channel verification", "Meta Business Partner Portal"},
                {"GOLD_INSTAGRAM", "Instagram Ads", "Instagram Ads Platform", "Instagram", null, "PLATFORM", null, null, "US", "NA", true, "Global visual social advertising platform channel.", "https://business.instagram.com", "Media & Digital Advertising", "ACCEPTED", new java.math.BigDecimal("0.96"), "Platform channel verification", "Meta Business Partner Portal"},
                {"GOLD_GOOGLE_GROUP", "Alphabet / Google Global Group", "Alphabet Inc.", "Google Global", null, "CORPORATE_GROUP", null, null, "US", "NA", true, "Global parent technology and digital media holding group.", "https://abc.xyz", "Media & Digital Advertising", "ACCEPTED", new java.math.BigDecimal("0.99"), "Corporate registry verified", "SEC Filings"},
                {"GOLD_GOOGLE_US", "Google LLC", "Google LLC", "Google US", null, "LEGAL_ENTITY", "DEMO-US-123456", "DEMO-123456789", "US", "NA", true, "US primary operating legal entity for Google.", "https://www.google.com", "Media & Digital Advertising", "ACCEPTED", new java.math.BigDecimal("0.98"), "US corporate registry verified", "Delaware Corporate Registry"},
                {"GOLD_GOOGLE_IRELAND", "Google Ireland Ltd.", "Google Ireland Ltd.", "Google EMEA", null, "LEGAL_ENTITY", "DEMO-IE-998877", null, "IE", "EMEA", true, "EMEA primary operating legal entity for Google.", "https://www.google.ie", "Media & Digital Advertising", "ACCEPTED", new java.math.BigDecimal("0.97"), "EMEA corporate registry verified", "Ireland CRO Registry"},
                {"GOLD_YOUTUBE", "YouTube Ads Platform", "YouTube Ads", "YouTube", null, "PLATFORM", null, null, "US", "NA", true, "Global online video advertising platform channel.", "https://www.youtube.com/ads", "Media & Digital Advertising", "ACCEPTED", new java.math.BigDecimal("0.96"), "Platform channel verification", "Google Ads Portal"},
                {"GOLD_COCA_COLA", "The Coca-Cola Company", "The Coca-Cola Company", "Coca-Cola", null, "ADVERTISER", "DEMO-US-554433", null, "US", "NA", false, "Global beverage corporate advertiser enterprise.", "https://www.coca-colacompany.com", "Consumer Goods & Beverages", "ACCEPTED", new java.math.BigDecimal("0.98"), "Advertiser master record verified", "SEC Filings"}
            };

            for (Object[] s : suppliers) {
                final Object[] sData = s;
                createRecordIfAbsent(session, supDs, "/root/Supplier", (String) s[0], new RecordPopulator() {
                    @Override
                    public void populate(ValueContextForUpdate vc) {
                        vc.setValue(sData[0], Path.parse("./supplierId"));
                        vc.setValue(sData[1], Path.parse("./supplierName"));
                        vc.setValue(sData[2], Path.parse("./legalName"));
                        vc.setValue(sData[3], Path.parse("./tradeName"));
                        if (sData[4] != null) vc.setValue(sData[4], Path.parse("./legacyName"));
                        vc.setValue(sData[5], Path.parse("./supplierSubtype"));
                        if (sData[6] != null) vc.setValue(sData[6], Path.parse("./taxIdentifier"));
                        if (sData[7] != null) vc.setValue(sData[7], Path.parse("./dunsNumber"));
                        vc.setValue("SUPPLIER", Path.parse("./supplierType"));
                        vc.setValue("ACTIVE", Path.parse("./supplierStatus"));
                        vc.setValue(sData[8], Path.parse("./countryCode"));
                        vc.setValue(sData[9], Path.parse("./regionCode"));
                        vc.setValue(sData[10], Path.parse("./isMediaPlatform"));
                        vc.setValue(sData[11], Path.parse("./businessDescription"));
                        vc.setValue(sData[12], Path.parse("./websiteUrl"));
                        vc.setValue(sData[13], Path.parse("./industrySector"));
                        vc.setValue(sData[14], Path.parse("./aiEnrichmentStatus"));
                        vc.setValue(sData[15], Path.parse("./aiConfidenceScore"));
                        vc.setValue(sData[16], Path.parse("./aiEnrichmentSummary"));
                        vc.setValue(sData[17], Path.parse("./aiEvidenceSources"));
                        vc.setValue(new Date(), Path.parse("./lastEnrichedAt"));

                        // Phase 1 additive fields
                        vc.setValue(sData[0], Path.parse("./legalEntityId"));
                        vc.setValue("APPROVED", Path.parse("./supplierLifecycleStatus"));
                        vc.setValue("COMPLETED", Path.parse("./onboardingStatus"));
                        vc.setValue(Boolean.TRUE, Path.parse("./strategicSupplier"));
                        vc.setValue(Boolean.FALSE, Path.parse("./criticalSupplier"));
                        vc.setValue(Boolean.TRUE, Path.parse("./preferredSupplier"));
                        vc.setValue("LOW", Path.parse("./riskTier"));
                        vc.setValue("COMPLIANT", Path.parse("./complianceStatus"));
                        vc.setValue(new java.math.BigDecimal("0.98"), Path.parse("./dataQualityScore"));
                        vc.setValue(Boolean.TRUE, Path.parse("./demoDataFlag"));
                        vc.setValue("DEMO_PROFILES", Path.parse("./demoProfile"));

                        if ("GOLD_COCA_COLA".equals(sData[0])) {
                            vc.setValue("ADDR_COCA_COLA_HQ", Path.parse("./primaryAddressId"));
                        } else if ("GOLD_META_US".equals(sData[0])) {
                            vc.setValue("ADDR_META_US_HQ", Path.parse("./primaryAddressId"));
                        } else if ("GOLD_GOOGLE_US".equals(sData[0])) {
                            vc.setValue("ADDR_GOOGLE_US_HQ", Path.parse("./primaryAddressId"));
                        }
                    }
                }, log);
            }

            // Seed Structured Supplier Addresses (OrganizationAddress Pattern)
            createRecordIfAbsent(session, supDs, "/root/SupplierAddress", "ADDR_COCA_COLA_HQ", new RecordPopulator() {
                @Override
                public void populate(ValueContextForUpdate vc) {
                    vc.setValue("ADDR_COCA_COLA_HQ", Path.parse("./supplierAddressId"));
                    vc.setValue("GOLD_COCA_COLA", Path.parse("./supplierId"));
                    vc.setValue("HEADQUARTERS", Path.parse("./addressType"));
                    vc.setValue("1 Coca Cola Plaza NW", Path.parse("./addressLine1"));
                    vc.setValue("Suite 200", Path.parse("./addressLine2"));
                    vc.setValue("Atlanta", Path.parse("./city"));
                    vc.setValue("GA", Path.parse("./stateProvince"));
                    vc.setValue("30313", Path.parse("./postalCode"));
                    vc.setValue("US", Path.parse("./countryCode"));
                    vc.setValue("Fulton", Path.parse("./county"));
                    vc.setValue(new java.math.BigDecimal("33.7712"), Path.parse("./latitude"));
                    vc.setValue(new java.math.BigDecimal("-84.3970"), Path.parse("./longitude"));
                    vc.setValue("ROOFTOP", Path.parse("./geoAccuracy"));
                    vc.setValue("VERIFIED", Path.parse("./addressValidationStatus"));
                    vc.setValue(Boolean.TRUE, Path.parse("./primaryAddress"));
                    vc.setValue("ECC_NA", Path.parse("./sourceSystem"));
                }
            }, log);

            createRecordIfAbsent(session, supDs, "/root/SupplierAddress", "ADDR_META_US_HQ", new RecordPopulator() {
                @Override
                public void populate(ValueContextForUpdate vc) {
                    vc.setValue("ADDR_META_US_HQ", Path.parse("./supplierAddressId"));
                    vc.setValue("GOLD_META_US", Path.parse("./supplierId"));
                    vc.setValue("HEADQUARTERS", Path.parse("./addressType"));
                    vc.setValue("1 Hacker Way", Path.parse("./addressLine1"));
                    vc.setValue("Menlo Park", Path.parse("./city"));
                    vc.setValue("CA", Path.parse("./stateProvince"));
                    vc.setValue("94025", Path.parse("./postalCode"));
                    vc.setValue("US", Path.parse("./countryCode"));
                    vc.setValue("San Mateo", Path.parse("./county"));
                    vc.setValue(new java.math.BigDecimal("37.4848"), Path.parse("./latitude"));
                    vc.setValue(new java.math.BigDecimal("-122.1484"), Path.parse("./longitude"));
                    vc.setValue("ROOFTOP", Path.parse("./geoAccuracy"));
                    vc.setValue("VERIFIED", Path.parse("./addressValidationStatus"));
                    vc.setValue(Boolean.TRUE, Path.parse("./primaryAddress"));
                    vc.setValue("ECC_NA", Path.parse("./sourceSystem"));
                }
            }, log);

            createRecordIfAbsent(session, supDs, "/root/SupplierAddress", "ADDR_GOOGLE_US_HQ", new RecordPopulator() {
                @Override
                public void populate(ValueContextForUpdate vc) {
                    vc.setValue("ADDR_GOOGLE_US_HQ", Path.parse("./supplierAddressId"));
                    vc.setValue("GOLD_GOOGLE_US", Path.parse("./supplierId"));
                    vc.setValue("HEADQUARTERS", Path.parse("./addressType"));
                    vc.setValue("1600 Amphitheatre Parkway", Path.parse("./addressLine1"));
                    vc.setValue("Mountain View", Path.parse("./city"));
                    vc.setValue("CA", Path.parse("./stateProvince"));
                    vc.setValue("94043", Path.parse("./postalCode"));
                    vc.setValue("US", Path.parse("./countryCode"));
                    vc.setValue("Santa Clara", Path.parse("./county"));
                    vc.setValue(new java.math.BigDecimal("37.4220"), Path.parse("./latitude"));
                    vc.setValue(new java.math.BigDecimal("-122.0841"), Path.parse("./longitude"));
                    vc.setValue("ROOFTOP", Path.parse("./geoAccuracy"));
                    vc.setValue("VERIFIED", Path.parse("./addressValidationStatus"));
                    vc.setValue(Boolean.TRUE, Path.parse("./primaryAddress"));
                    vc.setValue("ECC_NA", Path.parse("./sourceSystem"));
                }
            }, log);

            // Seed Synthetic Demo Supplier Bank Accounts
            createRecordIfAbsent(session, supDs, "/root/SupplierBankAccount", "ACC_META_US_01", new RecordPopulator() {
                @Override
                public void populate(ValueContextForUpdate vc) {
                    vc.setValue("ACC_META_US_01", Path.parse("./accountId"));
                    vc.setValue("GOLD_META_US", Path.parse("./supplierId"));
                    vc.setValue("Meta Platforms, Inc.", Path.parse("./accountHolder"));
                    vc.setValue("JPMorgan Chase Bank, N.A.", Path.parse("./bankName"));
                    vc.setValue("US", Path.parse("./bankCountry"));
                    vc.setValue("USD", Path.parse("./currency"));
                    vc.setValue("XXXX-XXXX-9876", Path.parse("./maskedAccount"));
                    vc.setValue("US98CHAS12345678909876", Path.parse("./iban"));
                    vc.setValue("CHASUS33XXX", Path.parse("./swiftBic"));
                    vc.setValue("PRIMARY_REMITTANCE", Path.parse("./remitPurpose"));
                    vc.setValue(Boolean.TRUE, Path.parse("./primaryFlag"));
                    vc.setValue("VERIFIED", Path.parse("./verificationStatus"));
                    vc.setValue("demo.steward", Path.parse("./verifiedBy"));
                    vc.setValue(new Date(), Path.parse("./verifiedAt"));
                    vc.setValue(new java.math.BigDecimal("0.05"), Path.parse("./changeRiskScore"));
                }
            }, log);

            // Seed Supplier Business Continuity Plans
            createRecordIfAbsent(session, supDs, "/root/SupplierBusinessContinuityPlan", "BCP_META_01", new RecordPopulator() {
                @Override
                public void populate(ValueContextForUpdate vc) {
                    vc.setValue("BCP_META_01", Path.parse("./planId"));
                    vc.setValue("GOLD_META_US", Path.parse("./supplierId"));
                    vc.setValue("APPROVED", Path.parse("./planStatus"));
                    vc.setValue("Ad Delivery & API Services", Path.parse("./criticalServices"));
                    vc.setValue("Oregon Data Center Hub", Path.parse("./alternateSite"));
                    vc.setValue(Integer.valueOf(24), Path.parse("./rtoHours"));
                    vc.setValue(Integer.valueOf(4), Path.parse("./rpoHours"));
                    vc.setValue("LOW", Path.parse("./residualRisk"));
                }
            }, log);

            // Seed Performance Assessments
            createRecordIfAbsent(session, supDs, "/root/SupplierPerformanceAssessment", "PERF_META_2026Q2", new RecordPopulator() {
                @Override
                public void populate(ValueContextForUpdate vc) {
                    vc.setValue("PERF_META_2026Q2", Path.parse("./assessmentId"));
                    vc.setValue("GOLD_META_US", Path.parse("./supplierId"));
                    vc.setValue("2026-Q2", Path.parse("./period"));
                    vc.setValue(new java.math.BigDecimal("99.50"), Path.parse("./onTimeDeliveryPct"));
                    vc.setValue(new java.math.BigDecimal("0.00"), Path.parse("./qualityDefectPpm"));
                    vc.setValue(new java.math.BigDecimal("99.80"), Path.parse("./fillRatePct"));
                    vc.setValue(new java.math.BigDecimal("0.98"), Path.parse("./overallScore"));
                    vc.setValue("EXCELLENT", Path.parse("./scoreTrend"));
                    vc.setValue("demo.steward", Path.parse("./reviewer"));
                }
            }, log);

            // Seed Supplier Hierarchy Coexisting Perspectives
            Object[][] hierarchyNodes = {
                // Global Supplier View
                {"H_GLOB_01", "GLOBAL_SUPPLIER", "GLOBAL", "GOLD_META_GROUP", "GOLD_META_US", "ROLLS_UP_TO", 10, "ACTIVE"},
                {"H_GLOB_02", "GLOBAL_SUPPLIER", "GLOBAL", "GOLD_META_US", "GOLD_FACEBOOK", "REPORTED_UNDER", 10, "ACTIVE"},
                {"H_GLOB_03", "GLOBAL_SUPPLIER", "GLOBAL", "GOLD_META_US", "GOLD_INSTAGRAM", "REPORTED_UNDER", 20, "ACTIVE"},
                {"H_GLOB_04", "GLOBAL_SUPPLIER", "GLOBAL", "GOLD_GOOGLE_GROUP", "GOLD_GOOGLE_US", "ROLLS_UP_TO", 10, "ACTIVE"},
                {"H_GLOB_05", "GLOBAL_SUPPLIER", "GLOBAL", "GOLD_GOOGLE_US", "GOLD_YOUTUBE", "REPORTED_UNDER", 10, "ACTIVE"},

                // EMEA Supplier View
                {"H_EMEA_01", "EMEA_SUPPLIER", "REGIONAL", "GOLD_META_GROUP", "GOLD_META_IRELAND", "ROLLS_UP_TO", 10, "ACTIVE"},
                {"H_EMEA_02", "EMEA_SUPPLIER", "REGIONAL", "GOLD_META_IRELAND", "GOLD_FACEBOOK", "REPORTED_UNDER", 10, "ACTIVE"},
                {"H_EMEA_03", "EMEA_SUPPLIER", "REGIONAL", "GOLD_META_IRELAND", "GOLD_INSTAGRAM", "REPORTED_UNDER", 20, "ACTIVE"},
                {"H_EMEA_04", "EMEA_SUPPLIER", "REGIONAL", "GOLD_GOOGLE_GROUP", "GOLD_GOOGLE_IRELAND", "ROLLS_UP_TO", 10, "ACTIVE"},
                {"H_EMEA_05", "EMEA_SUPPLIER", "REGIONAL", "GOLD_GOOGLE_IRELAND", "GOLD_YOUTUBE", "REPORTED_UNDER", 10, "ACTIVE"},

                // LATAM Supplier View
                {"H_LATAM_01", "LATAM_SUPPLIER", "REGIONAL", "GOLD_META_GROUP", "GOLD_FACEBOOK", "REPORTED_UNDER", 10, "ACTIVE"},
                {"H_LATAM_02", "LATAM_SUPPLIER", "REGIONAL", "GOLD_META_GROUP", "GOLD_INSTAGRAM", "REPORTED_UNDER", 20, "ACTIVE"},

                // Paid Social Taxonomy
                {"H_SOCIAL_01", "PAID_SOCIAL", "TAXONOMY", "GOLD_META_GROUP", "GOLD_FACEBOOK", "REPORTED_UNDER", 10, "ACTIVE"},
                {"H_SOCIAL_02", "PAID_SOCIAL", "TAXONOMY", "GOLD_META_GROUP", "GOLD_INSTAGRAM", "REPORTED_UNDER", 20, "ACTIVE"}
            };

            for (Object[] h : hierarchyNodes) {
                final Object[] hData = h;
                createRecordIfAbsent(session, supDs, "/root/SupplierHierarchy", (String) h[0], new RecordPopulator() {
                    @Override
                    public void populate(ValueContextForUpdate vc) {
                        vc.setValue(hData[0], Path.parse("./supplierHierarchyId"));
                        vc.setValue(hData[1], Path.parse("./hierarchyCode"));
                        vc.setValue(hData[2], Path.parse("./hierarchyType"));
                        vc.setValue(hData[3], Path.parse("./parentSupplierId"));
                        vc.setValue(hData[4], Path.parse("./childSupplierId"));
                        vc.setValue(hData[5], Path.parse("./relationshipType"));
                        vc.setValue(hData[6], Path.parse("./sortOrder"));
                        vc.setValue(hData[7], Path.parse("./status"));
                    }
                }, log);
            }
        }

        // Programmatically seed Perspective Menu in ebx-manager
        // Soft-retire supplier GOLD_COCA_COLA
        if (supDs != null) {
            AdaptationTable supplierTable = supDs.getTable(Path.parse("/root/Supplier"));
            final Adaptation cocaColaRecord = supplierTable.lookupAdaptationByPrimaryKey(PrimaryKey.parseString("GOLD_COCA_COLA"));
            if (cocaColaRecord != null) {
                ProgrammaticService service = ProgrammaticService.createForSession(session, supDs.getHome());
                service.execute(new Procedure() {
                    @Override
                    public void execute(ProcedureContext context) throws Exception {
                        context.setAllPrivileges(true);
                        ValueContextForUpdate vc = context.getContext(cocaColaRecord.getAdaptationName());
                        vc.setValue("INACTIVE", Path.parse("./supplierStatus"));
                        vc.setValue("Relocated to Customer Master as CUST_COCA_COLA (advertisers are customers)", Path.parse("./deactivationReason"));
                        context.doModifyContent(cocaColaRecord, vc);
                    }
                });
                log.append("Soft-retired supplier GOLD_COCA_COLA.\n");
            }
        }

        // Seed Customer Master canonical records
        Adaptation custDs = findDataset(repo, "BMfgCustomerManagement", "MfgCustomerManagement");
        if (custDs != null) {
            // Seed Customer table
            Object[][] customers = {
                {"CUST_COCA_COLA", "The Coca-Cola Company", "The Coca-Cola Company", "Coca-Cola", "ADVERTISER_GROUP", "US", "NA", "Global beverage corporate advertiser group parent."},
                {"CUST_UNILEVER", "Unilever Global", "Unilever PLC", "Unilever", "CORPORATE_GROUP", "GB", "EMEA", "Global consumer goods corporate holding group parent."},
                {"CUST_PG", "Procter & Gamble Global", "The Procter & Gamble Company", "P&G", "CORPORATE_GROUP", "US", "NA", "Global consumer goods corporate holding group parent."},
                {"CUST_DOVE", "Dove", "Dove Brand Division", "Dove", "BRAND", "US", "NA", "Personal care brand division of Unilever."},
                {"CUST_AXE", "Axe", "Axe Brand Division", "Axe", "BRAND", "US", "NA", "Grooming brand division of Unilever."},
                {"CUST_KNORR", "Knorr", "Knorr Brand Division", "Knorr", "BRAND", "DE", "EMEA", "Food brand division of Unilever."},
                {"CUST_GILLETTE", "Gillette", "Gillette Brand Division", "Gillette", "BRAND", "US", "NA", "Grooming brand division of P&G."}
            };

            for (Object[] c : customers) {
                final Object[] cData = c;
                createRecordIfAbsent(session, custDs, "/root/Customer", (String) c[0], new RecordPopulator() {
                    @Override
                    public void populate(ValueContextForUpdate vc) throws Exception {
                        vc.setValue(cData[0], Path.parse("./customerId"));
                        vc.setValue("ADVERTISER", Path.parse("./customerType"));
                        vc.setValue("ACTIVE", Path.parse("./customerStatus"));
                        vc.setValue("ECC_NA", Path.parse("./sourceSystem"));
                        vc.setValue(cData[1], Path.parse("./customerName"));
                        vc.setValue(cData[2], Path.parse("./legalName"));
                        vc.setValue(cData[3], Path.parse("./tradeName"));
                        vc.setValue(cData[4], Path.parse("./customerSubtype"));
                        vc.setValue(cData[5], Path.parse("./countryCode"));
                        vc.setValue(cData[6], Path.parse("./regionCode"));
                        vc.setValue(Boolean.TRUE, Path.parse("./isAdvertiser"));
                        vc.setValue(cData[7], Path.parse("./businessDescription"));
                        vc.setValue("APPROVED", Path.parse("./customerLifecycleStatus"));
                        vc.setValue("COMPLETED", Path.parse("./onboardingStatus"));
                        vc.setValue(Boolean.TRUE, Path.parse("./strategicAccount"));
                        vc.setValue("LOW", Path.parse("./riskTier"));
                        vc.setValue("COMPLIANT", Path.parse("./complianceStatus"));
                        vc.setValue(new java.math.BigDecimal("0.98"), Path.parse("./dataQualityScore"));
                        vc.setValue(Boolean.TRUE, Path.parse("./demoDataFlag"));
                    }
                }, log);
            }

            // Seed Customer Hierarchy
            Object[][] custHierNodes = {
                {"CH_UNILEVER_DOVE", "CUST_UNILEVER", "CUST_DOVE", "BRAND"},
                {"CH_UNILEVER_AXE", "CUST_UNILEVER", "CUST_AXE", "BRAND"},
                {"CH_UNILEVER_KNORR", "CUST_UNILEVER", "CUST_KNORR", "BRAND"},
                {"CH_PG_GILLETTE", "CUST_PG", "CUST_GILLETTE", "BRAND"}
            };

            for (Object[] h : custHierNodes) {
                final Object[] hData = h;
                createRecordIfAbsent(session, custDs, "/root/CustomerHierarchy", (String) h[0], new RecordPopulator() {
                    @Override
                    public void populate(ValueContextForUpdate vc) throws Exception {
                        vc.setValue(hData[0], Path.parse("./customerHierarchyId"));
                        vc.setValue(hData[1], Path.parse("./parentCustomerId"));
                        vc.setValue(hData[2], Path.parse("./childCustomerId"));
                        vc.setValue(hData[3], Path.parse("./hierarchyType"));
                        vc.setValue("ECC_NA", Path.parse("./sourceSystem"));
                    }
                }, log);
            }
        }

        // Seed PaidSocialEcosystem dataset data
        Adaptation pseDs = findDataset(repo, "BMfgSupplierManagement", "PaidSocialEcosystem");
        if (pseDs != null) {
            // Seed TaxonomyLevel
            Object[][] levels = {
                {"L1", "Client", Integer.valueOf(1), "Top-most corporate client holding entity"},
                {"L2", "Brand", Integer.valueOf(2), "Product brand or operating division"},
                {"L3", "Sub-Brand", Integer.valueOf(3), "Product sub-brand category"},
                {"L4", "Region", Integer.valueOf(4), "Geographic advertising region rollup"},
                {"L5", "Country", Integer.valueOf(5), "Target country marketplace"},
                {"L6", "Ad Account", Integer.valueOf(6), "Media platform system ad account"},
                {"L7", "Campaign", Integer.valueOf(7), "Strategic marketing campaign budget folder"},
                {"L8", "Placement", Integer.valueOf(8), "Granular media placement target"}
            };
            for (Object[] lvl : levels) {
                final Object[] lData = lvl;
                createRecordIfAbsent(session, pseDs, "/root/TaxonomyLevel", (String) lvl[0], new RecordPopulator() {
                    @Override
                    public void populate(ValueContextForUpdate vc) throws Exception {
                        vc.setValue(lData[0], Path.parse("./levelId"));
                        vc.setValue(lData[1], Path.parse("./levelName"));
                        vc.setValue(lData[2], Path.parse("./levelDepth"));
                        vc.setValue(lData[3], Path.parse("./description"));
                    }
                }, log);
            }

            // Seed TaxonomyNode
            Object[][] nodes = {
                // Clients
                {"N_UNILEVER", "Unilever Global Client Node", "L1", null, "CUST_UNILEVER", null},
                {"N_PG", "P&G Global Client Node", "L1", null, "CUST_PG", null},
                {"N_COCA_COLA", "Coca-Cola Global Client Node", "L1", null, "CUST_COCA_COLA", null},
                
                // Brands under Unilever
                {"N_DOVE", "Dove Brand Node", "L2", "N_UNILEVER", "CUST_DOVE", null},
                {"N_AXE", "Axe Brand Node", "L2", "N_UNILEVER", "CUST_AXE", null},
                {"N_KNORR", "Knorr Brand Node", "L2", "N_UNILEVER", "CUST_KNORR", null},
                
                // Brands under P&G
                {"N_GILLETTE", "Gillette Brand Node", "L2", "N_PG", "CUST_GILLETTE", null},
                
                // Sub-brands
                {"N_DOVE_BODY", "Dove Body Care Sub-Brand Node", "L3", "N_DOVE", null, null},
                {"N_DOVE_HAIR", "Dove Hair Care Sub-Brand Node", "L3", "N_DOVE", null, null},
                {"N_AXE_DEO", "Axe Deodorants Sub-Brand Node", "L3", "N_AXE", null, null},
                {"N_GILLETTE_RAZOR", "Gillette Razors Sub-Brand Node", "L3", "N_GILLETTE", null, null},
                
                // Regions
                {"N_DOVE_BODY_NA", "Dove Body Care NA Region Node", "L4", "N_DOVE_BODY", null, null},
                {"N_DOVE_HAIR_EMEA", "Dove Hair Care EMEA Region Node", "L4", "N_DOVE_HAIR", null, null},
                
                // Countries
                {"N_DOVE_BODY_US", "Dove Body Care US Node", "L5", "N_DOVE_BODY_NA", null, null},
                {"N_DOVE_HAIR_UK", "Dove Hair Care UK Node", "L5", "N_DOVE_HAIR_EMEA", null, null},
                
                // Ad Accounts
                {"N_DOVE_BODY_US_FB", "Dove Body US FB Ad Account Node", "L6", "N_DOVE_BODY_US", null, "GOLD_FACEBOOK"},
                {"N_DOVE_HAIR_UK_IG", "Dove Hair UK IG Ad Account Node", "L6", "N_DOVE_HAIR_UK", null, "GOLD_INSTAGRAM"},
                
                // Campaigns
                {"N_DOVE_REAL_BEAUTY_2026", "Dove Real Beauty Campaign 2026 Node", "L7", "N_DOVE_BODY_US_FB", null, null},
                
                // Placements
                {"N_DOVE_REAL_BEAUTY_FB_FEED", "Dove Real Beauty Feed Placement Node", "L8", "N_DOVE_REAL_BEAUTY_2026", null, null},
                {"N_DOVE_REAL_BEAUTY_FB_STORY", "Dove Real Beauty Story Placement Node", "L8", "N_DOVE_REAL_BEAUTY_2026", null, null}
            };
            for (Object[] nd : nodes) {
                final Object[] nData = nd;
                createRecordIfAbsent(session, pseDs, "/root/TaxonomyNode", (String) nd[0], new RecordPopulator() {
                    @Override
                    public void populate(ValueContextForUpdate vc) throws Exception {
                        vc.setValue(nData[0], Path.parse("./nodeId"));
                        vc.setValue(nData[1], Path.parse("./nodeName"));
                        vc.setValue(nData[2], Path.parse("./levelId"));
                        if (nData[3] != null) vc.setValue(nData[3], Path.parse("./parentNodeId"));
                        if (nData[4] != null) vc.setValue(nData[4], Path.parse("./linkedCustomerId"));
                        if (nData[5] != null) vc.setValue(nData[5], Path.parse("./linkedSupplierId"));
                        vc.setValue("ACTIVE", Path.parse("./nodeStatus"));
                    }
                }, log);
            }

            // Seed SupplierTaxonomyAssignment
            createRecordIfAbsent(session, pseDs, "/root/SupplierTaxonomyAssignment", "ASG_COCA_COLA_META", new RecordPopulator() {
                @Override
                public void populate(ValueContextForUpdate vc) throws Exception {
                    vc.setValue("ASG_COCA_COLA_META", Path.parse("./assignmentId"));
                    vc.setValue("N_COCA_COLA", Path.parse("./nodeId"));
                    vc.setValue("CUST_COCA_COLA", Path.parse("./linkedCustomerId"));
                    vc.setValue("GOLD_META_US", Path.parse("./linkedSupplierId"));
                    vc.setValue("ACTIVE", Path.parse("./assignmentStatus"));
                }
            }, log);

            // Seed MediaBuyRelationship
            Object[][] mediaBuys = {
                {"MB_COCA_COLA_META", "Coca-Cola Meta Media Buy", "CUST_COCA_COLA", "GOLD_META_US"},
                {"MB_UNILEVER_META", "Unilever Meta Media Buy", "CUST_UNILEVER", "GOLD_META_US"},
                {"MB_PG_GOOGLE", "P&G Google Media Buy", "CUST_PG", "GOLD_GOOGLE_US"}
            };
            for (Object[] mb : mediaBuys) {
                final Object[] mData = mb;
                createRecordIfAbsent(session, pseDs, "/root/MediaBuyRelationship", (String) mb[0], new RecordPopulator() {
                    @Override
                    public void populate(ValueContextForUpdate vc) throws Exception {
                        vc.setValue(mData[0], Path.parse("./relationshipId"));
                        vc.setValue(mData[1], Path.parse("./mediaBuyName"));
                        vc.setValue(mData[2], Path.parse("./advertiserId"));
                        vc.setValue(mData[3], Path.parse("./platformId"));
                        vc.setValue("ACTIVE", Path.parse("./relationshipStatus"));
                    }
                }, log);
            }
        }

        seedPerspectiveData(repo, session, log);
    }

    private void seedPerspectiveData(Repository repo, Session session, StringBuilder log) {
        ManufacturingPerspectiveInstaller.install(repo, session, LoggingCategory.getKernel());
        log.append("Manufacturing perspective installation requested.\n");
    }

    private void createRecordIfAbsent(Session session, Adaptation dataset, String tablePathStr, String primaryKeyStr, final RecordPopulator populator, StringBuilder log) throws OperationException {
        AdaptationTable table = dataset.getTable(Path.parse(tablePathStr));
        PrimaryKey pk = PrimaryKey.parseString(primaryKeyStr);
        if (table.lookupAdaptationByPrimaryKey(pk) != null) {
            log.append("Record '").append(primaryKeyStr).append("' already exists in ").append(tablePathStr).append(" - skipping.\n");
            return;
        }

        ProgrammaticService service = ProgrammaticService.createForSession(session, dataset.getHome());
        ProcedureResult pr = service.execute(new Procedure() {
            @Override
            public void execute(ProcedureContext context) throws Exception {
                context.setAllPrivileges(true);
                ValueContextForUpdate vc = context.getContextForNewOccurrence(table);
                populator.populate(vc);
                context.doCreateOccurrence(vc, table);
            }
        });
        if (pr.hasFailed()) {
            throw pr.getException();
        }
        log.append("Created record '").append(primaryKeyStr).append("' in ").append(tablePathStr).append(".\n");
    }

    interface RecordPopulator {
        void populate(ValueContextForUpdate vc) throws Exception;
    }
}

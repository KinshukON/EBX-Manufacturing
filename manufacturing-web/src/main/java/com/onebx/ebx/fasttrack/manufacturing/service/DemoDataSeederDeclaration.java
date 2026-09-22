package com.onebx.ebx.fasttrack.manufacturing.service;

import com.orchestranetworks.service.ServiceKey;
import com.orchestranetworks.ui.selection.DatasetEntitySelection;
import com.orchestranetworks.userservice.UserService;
import com.orchestranetworks.schema.types.dataspace.DataspaceSet;
import com.orchestranetworks.userservice.declaration.ActivationContextOnDataset;
import com.orchestranetworks.userservice.declaration.UserServiceDeclaration;
import com.orchestranetworks.userservice.declaration.UserServicePropertiesDefinitionContext;
import com.orchestranetworks.userservice.declaration.WebComponentDeclarationContext;

public class DemoDataSeederDeclaration implements UserServiceDeclaration.OnDataset {

    public static final ServiceKey SERVICE_KEY = ServiceKey.forModuleServiceName("EBX Manufacturing Module", "MfgDemoDataSeeder");

    @Override
    public ServiceKey getServiceKey() {
        return SERVICE_KEY;
    }

    @Override
    public UserService<DatasetEntitySelection> createUserService() {
        return new DemoDataSeederService();
    }

    @Override
    public void defineActivation(ActivationContextOnDataset aContext) {
        aContext.includeAllDataspaces(DataspaceSet.DataspaceType.ALL);
        aContext.includeAllDatasets();
    }

    @Override
    public void defineProperties(UserServicePropertiesDefinitionContext aContext) {
        aContext.setLabel("🚀 Seed Manufacturing Demo Data");
        aContext.setDescription("Creates all required Manufacturing dataspaces/datasets and populates staging and canonical reference data.");
    }

    @Override
    public void declareWebComponent(WebComponentDeclarationContext aContext) {
        aContext.setAvailableAsPerspectiveAction(true);
    }
}

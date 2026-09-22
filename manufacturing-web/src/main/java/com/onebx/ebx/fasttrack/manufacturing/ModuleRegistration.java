package com.onebx.ebx.fasttrack.manufacturing;

import com.orchestranetworks.module.ModuleRegistrationListener;
import com.orchestranetworks.module.ModuleContextOnRepositoryStartup;
import com.orchestranetworks.module.ModuleServiceRegistrationContext;
import com.orchestranetworks.service.LoggingCategory;
import com.orchestranetworks.service.OperationException;
import com.onebx.ebx.fasttrack.manufacturing.service.DemoDataSeederDeclaration;
import jakarta.servlet.annotation.WebListener;

@WebListener
public final class ModuleRegistration extends ModuleRegistrationListener {
    @Override
    public void handleRepositoryStartup(final ModuleContextOnRepositoryStartup context) throws OperationException {
        super.handleRepositoryStartup(context);
        ManufacturingPerspectiveInstaller.install(
            context.getRepository(),
            context.createSystemUserSession("manufacturing-perspective"),
            context.getLoggingCategory());
    }

    @Override
    public void handleServiceRegistration(final ModuleServiceRegistrationContext context) {
        super.handleServiceRegistration(context);
        context.registerUserService(new DemoDataSeederDeclaration());
        LoggingCategory.getKernel().info("[manufacturing] Demo data seeder service registered.");
    }
}

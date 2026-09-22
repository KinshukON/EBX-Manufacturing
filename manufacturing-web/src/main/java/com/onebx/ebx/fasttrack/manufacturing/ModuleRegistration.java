package com.onebx.ebx.fasttrack.manufacturing;

import com.orchestranetworks.module.ModuleContextOnRepositoryStartup;
import com.orchestranetworks.module.ModuleRegistrationListener;
import com.orchestranetworks.module.ModuleServiceRegistrationContext;
import com.orchestranetworks.service.LoggingCategory;
import com.orchestranetworks.service.OperationException;
import com.onebx.ebx.fasttrack.manufacturing.service.DemoDataSeederDeclaration;
import com.onebx.ebx.fasttrack.manufacturing.service.DemoDataSeederService;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class ModuleRegistration extends ModuleRegistrationListener {
    @Override
    public void handleRepositoryStartup(ModuleContextOnRepositoryStartup context) throws OperationException {
        super.handleRepositoryStartup(context);
        LoggingCategory.getKernel().info("[manufacturing] Auto-seeding on repository startup...");
        try {
            DemoDataSeederService seeder = new DemoDataSeederService();
            seeder.autoSeed(context.getRepository(), context.createSystemUserSession(null));
            LoggingCategory.getKernel().info("[manufacturing] Auto-seeding execution completed!");
        } catch (Exception ex) {
            LoggingCategory.getKernel().error("[manufacturing] Auto-seeding failed during repository startup!", ex);
        }
    }

    @Override
    public void handleServiceRegistration(final ModuleServiceRegistrationContext context) {
        LoggingCategory.getKernel().info("[manufacturing] handleServiceRegistration START");
        context.registerUserService(new DemoDataSeederDeclaration());
        LoggingCategory.getKernel().info("[manufacturing] DemoDataSeederDeclaration registered");
        super.handleServiceRegistration(context);
    }
}

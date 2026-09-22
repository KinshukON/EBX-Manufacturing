package com.onebx.ebx.fasttrack.manufacturing;

import com.orchestranetworks.module.ModuleRegistrationListener;
import com.orchestranetworks.module.ModuleServiceRegistrationContext;
import com.orchestranetworks.service.LoggingCategory;
import com.onebx.ebx.fasttrack.manufacturing.service.DemoDataSeederDeclaration;
import jakarta.servlet.annotation.WebListener;

@WebListener
public final class ModuleRegistration extends ModuleRegistrationListener {
    @Override
    public void handleServiceRegistration(final ModuleServiceRegistrationContext context) {
        super.handleServiceRegistration(context);
        context.registerUserService(new DemoDataSeederDeclaration());
        LoggingCategory.getKernel().info("[manufacturing] Demo data seeder service registered.");
    }
}

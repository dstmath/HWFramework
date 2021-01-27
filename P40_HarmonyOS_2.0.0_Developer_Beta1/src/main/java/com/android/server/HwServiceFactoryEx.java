package com.android.server;

import com.android.server.security.securityprofile.ISecurityProfileController;

public class HwServiceFactoryEx {
    public static ISecurityProfileController getSecurityProfileController() {
        return HwServiceFactory.getSecurityProfileController();
    }
}

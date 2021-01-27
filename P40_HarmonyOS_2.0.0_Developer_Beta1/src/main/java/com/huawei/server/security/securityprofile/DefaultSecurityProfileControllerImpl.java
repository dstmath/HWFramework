package com.huawei.server.security.securityprofile;

import com.android.server.security.securityprofile.ISecurityProfileController;
import com.android.server.security.securityprofile.IntentCaller;
import java.io.File;

public class DefaultSecurityProfileControllerImpl implements ISecurityProfileController {
    public boolean shouldPreventInteraction(int type, String targetPackage, IntentCaller caller, int userId) {
        return false;
    }

    public boolean shouldPreventMediaProjection(int uid) {
        return false;
    }

    public void handleActivityResuming(String packageName) {
    }

    public boolean verifyPackage(String packageName, File path) {
        return false;
    }
}

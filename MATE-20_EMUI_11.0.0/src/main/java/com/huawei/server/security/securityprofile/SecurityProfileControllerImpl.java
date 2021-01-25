package com.huawei.server.security.securityprofile;

import android.os.Binder;
import com.android.server.security.securityprofile.IntentCaller;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.server.LocalServicesExt;
import com.huawei.hwpartsecurityservices.BuildConfig;
import java.io.File;

public class SecurityProfileControllerImpl extends DefaultSecurityProfileControllerImpl {
    private static final boolean IS_CHINA_AREA = "CN".equalsIgnoreCase(SystemPropertiesEx.get("ro.product.locale.region", BuildConfig.FLAVOR));
    private static final boolean SUPPORT_HW_SEAPP = "true".equalsIgnoreCase(SystemPropertiesEx.get("ro.config.support_iseapp", "false"));

    public boolean shouldPreventInteraction(int type, String targetPackage, IntentCaller caller, int userId) {
        SecurityProfileInternal securityProfileInternal = (SecurityProfileInternal) LocalServicesExt.getService(SecurityProfileInternal.class);
        if (securityProfileInternal == null) {
            return false;
        }
        long token = Binder.clearCallingIdentity();
        try {
            return securityProfileInternal.shouldPreventInteraction(type, targetPackage, caller, userId);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public boolean shouldPreventMediaProjection(int uid) {
        return false;
    }

    public void handleActivityResuming(String packageName) {
    }

    public boolean verifyPackage(String packageName, File path) {
        boolean result = true;
        SecurityProfileInternal securityProfileInternal = (SecurityProfileInternal) LocalServicesExt.getService(SecurityProfileInternal.class);
        if (securityProfileInternal != null) {
            long token = Binder.clearCallingIdentity();
            try {
                if (SUPPORT_HW_SEAPP && IS_CHINA_AREA) {
                    result = securityProfileInternal.verifyPackage(packageName, path);
                }
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }
        return result;
    }
}

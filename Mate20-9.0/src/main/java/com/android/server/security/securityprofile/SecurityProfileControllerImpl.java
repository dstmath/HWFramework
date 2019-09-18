package com.android.server.security.securityprofile;

import android.os.Binder;
import android.os.SystemProperties;
import com.android.server.LocalServices;
import com.android.server.wifipro.WifiProCommonUtils;

public class SecurityProfileControllerImpl implements ISecurityProfileController {
    private static final boolean IS_CHINA_AREA = "CN".equalsIgnoreCase(SystemProperties.get(WifiProCommonUtils.KEY_PROP_LOCALE, ""));
    private static final boolean SUPPORT_HW_SEAPP = "true".equalsIgnoreCase(SystemProperties.get("ro.config.support_iseapp", "false"));

    public boolean shouldPreventInteraction(int type, String targetPackage, int callerUid, int callerPid, String callerPackage, int userId) {
        SecurityProfileInternal mSecurityProfileInternal = (SecurityProfileInternal) LocalServices.getService(SecurityProfileInternal.class);
        if (mSecurityProfileInternal == null) {
            return false;
        }
        long token = Binder.clearCallingIdentity();
        try {
            boolean ret = mSecurityProfileInternal.shouldPreventInteraction(type, targetPackage, callerUid, callerPid, callerPackage, userId);
            Binder.restoreCallingIdentity(token);
            return ret;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(token);
            throw th;
        }
    }

    public boolean shouldPreventMediaProjection(int uid) {
        SecurityProfileInternal mSecurityProfileInternal = (SecurityProfileInternal) LocalServices.getService(SecurityProfileInternal.class);
        if (mSecurityProfileInternal == null) {
            return false;
        }
        long token = Binder.clearCallingIdentity();
        try {
            return mSecurityProfileInternal.shouldPreventMediaProjection(uid);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public void handleActivityResuming(String packageName) {
        SecurityProfileInternal mSecurityProfileInternal = (SecurityProfileInternal) LocalServices.getService(SecurityProfileInternal.class);
        if (mSecurityProfileInternal != null) {
            long token = Binder.clearCallingIdentity();
            try {
                mSecurityProfileInternal.handleActivityResuming(packageName);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }
    }

    public boolean verifyPackage(String packageName, String path) {
        boolean ret = true;
        SecurityProfileInternal mSecurityProfileInternal = (SecurityProfileInternal) LocalServices.getService(SecurityProfileInternal.class);
        if (mSecurityProfileInternal != null) {
            long token = Binder.clearCallingIdentity();
            try {
                if (SUPPORT_HW_SEAPP && IS_CHINA_AREA) {
                    ret = mSecurityProfileInternal.verifyPackage(packageName, path);
                }
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }
        return ret;
    }
}

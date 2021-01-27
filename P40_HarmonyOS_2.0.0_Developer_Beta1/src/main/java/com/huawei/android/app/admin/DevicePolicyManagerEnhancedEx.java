package com.huawei.android.app.admin;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.UserHandle;
import com.huawei.android.app.PackageManagerEx;
import com.huawei.android.content.pm.UserInfoEx;
import java.util.List;

public class DevicePolicyManagerEnhancedEx {
    public static final String EXTRA_RESTRICTION = "android.app.extra.RESTRICTION";
    public static final int KEYGUARD_DISABLE_FEATURES_NONE = 0;
    public static final int PROFILE_KEYGUARD_FEATURES_AFFECT_OWNER = 432;

    public static ComponentName getProfileOwner(Context context, DevicePolicyManager devicePolicyManager) throws IllegalArgumentException {
        if (!PackageManagerEx.hasSystemSignaturePermission(context)) {
            return null;
        }
        return devicePolicyManager.getProfileOwner();
    }

    public static UserHandle getDeviceOwnerUser(Context context, DevicePolicyManager devicePolicyManager) {
        if (!PackageManagerEx.hasSystemSignaturePermission(context)) {
            return null;
        }
        return devicePolicyManager.getDeviceOwnerUser();
    }

    public static ComponentName getDeviceOwnerComponentOnAnyUser(Context context, DevicePolicyManager devicePolicyManager) {
        if (!PackageManagerEx.hasSystemSignaturePermission(context)) {
            return null;
        }
        return devicePolicyManager.getDeviceOwnerComponentOnAnyUser();
    }

    public static List<ComponentName> getActiveAdminsAsUser(Context context, DevicePolicyManager devicePolicyManager, int userId) {
        if (!PackageManagerEx.hasSystemSignaturePermission(context)) {
            return null;
        }
        return devicePolicyManager.getActiveAdminsAsUser(userId);
    }

    public static boolean isInputMethodPermittedByAdmin(Context context, DevicePolicyManager devicePolicyManager, ComponentName admin, String packageName, int userHandle) {
        if (!PackageManagerEx.hasSystemSignaturePermission(context)) {
            return false;
        }
        return devicePolicyManager.isInputMethodPermittedByAdmin(admin, packageName, userHandle);
    }

    public static boolean isAccessibilityServicePermittedByAdmin(Context context, DevicePolicyManager devicePolicyManager, ComponentName admin, String packageName, int userHandle) {
        if (!PackageManagerEx.hasSystemSignaturePermission(context)) {
            return false;
        }
        return devicePolicyManager.isAccessibilityServicePermittedByAdmin(admin, packageName, userHandle);
    }

    public static String[] getAccountTypesWithManagementDisabledAsUser(Context context, DevicePolicyManager devicePolicyManager, int userId) {
        if (!PackageManagerEx.hasSystemSignaturePermission(context)) {
            return null;
        }
        return devicePolicyManager.getAccountTypesWithManagementDisabledAsUser(userId);
    }

    public static boolean isMeteredDataDisabledPackageForUser(Context context, DevicePolicyManager devicePolicyManager, ComponentName admin, String packageName, int userId) {
        if (!PackageManagerEx.hasSystemSignaturePermission(context)) {
            return false;
        }
        return devicePolicyManager.isMeteredDataDisabledPackageForUser(admin, packageName, userId);
    }

    public static ComponentName getDeviceOwnerComponentOnCallingUser(Context context, DevicePolicyManager devicePolicyManager) {
        if (!PackageManagerEx.hasSystemSignaturePermission(context)) {
            return null;
        }
        return devicePolicyManager.getDeviceOwnerComponentOnCallingUser();
    }

    public static ComponentName getProfileOwnerAsUser(Context context, DevicePolicyManager devicePolicyManager, int userId) {
        if (!PackageManagerEx.hasSystemSignaturePermission(context)) {
            return null;
        }
        return devicePolicyManager.getProfileOwnerAsUser(userId);
    }

    public static boolean isAdminActiveAsUser(Context context, DevicePolicyManager devicePolicyManager, ComponentName admin, int userId) {
        if (!PackageManagerEx.hasSystemSignaturePermission(context)) {
            return false;
        }
        return devicePolicyManager.isAdminActiveAsUser(admin, userId);
    }

    public static int getPasswordQuality(Context context, DevicePolicyManager devicePolicyManager, ComponentName admin, int userHandle) {
        if (!PackageManagerEx.hasSystemSignaturePermission(context)) {
            return 0;
        }
        return devicePolicyManager.getPasswordQuality(admin, userHandle);
    }

    public static long getMaximumTimeToLock(Context context, DevicePolicyManager devicePolicyManager, ComponentName admin, int userHandle) {
        if (!PackageManagerEx.hasSystemSignaturePermission(context)) {
            return 0;
        }
        return devicePolicyManager.getMaximumTimeToLock(admin, userHandle);
    }

    public static DevicePolicyManager getParentProfileInstance(Context context, DevicePolicyManager devicePolicyManager, UserInfoEx userInfo) {
        if (!PackageManagerEx.hasSystemSignaturePermission(context)) {
            return null;
        }
        return devicePolicyManager.getParentProfileInstance(userInfo.getUserInfo());
    }

    public static CharSequence getShortSupportMessageForUser(Context context, DevicePolicyManager devicePolicyManager, ComponentName admin, int userHandle) {
        if (!PackageManagerEx.hasSystemSignaturePermission(context)) {
            return null;
        }
        return devicePolicyManager.getShortSupportMessageForUser(admin, userHandle);
    }

    public static int getKeyguardDisabledFeatures(Context context, DevicePolicyManager devicePolicyManager, ComponentName admin, int userHandle) {
        if (!PackageManagerEx.hasSystemSignaturePermission(context)) {
            return 0;
        }
        return devicePolicyManager.getKeyguardDisabledFeatures(admin, userHandle);
    }

    public static boolean getCrossProfileContactsSearchDisabled(Context context, DevicePolicyManager devicePolicyManager, UserHandle userHandle) {
        if (!PackageManagerEx.hasSystemSignaturePermission(context)) {
            return false;
        }
        return devicePolicyManager.getCrossProfileContactsSearchDisabled(userHandle);
    }

    public static boolean getCrossProfileCallerIdDisabled(Context context, DevicePolicyManager devicePolicyManager, UserHandle userHandle) {
        if (!PackageManagerEx.hasSystemSignaturePermission(context)) {
            return false;
        }
        return devicePolicyManager.getCrossProfileCallerIdDisabled(userHandle);
    }
}

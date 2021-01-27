package com.android.server.devicepolicy.plugins;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.hdm.HwDeviceManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.UserHandle;
import android.text.TextUtils;
import com.android.server.devicepolicy.DevicePolicyPlugin;
import com.android.server.devicepolicy.HwDevicePolicyManagerServiceUtil;
import com.android.server.devicepolicy.HwLog;
import com.android.server.devicepolicy.PolicyStruct;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DeviceApplicationPlugin extends DevicePolicyPlugin {
    private static final String FIX_APP_RUNTIME_PERMISSION_LIST = "fix-app-runtime-permission-list/fix-app-runtime-permission-list-item";
    private static final String INSTALL_APKS_BLACK_LIST = "install-packages-black-list";
    private static final String INSTALL_APKS_BLACK_LIST_ITEM = "install-packages-black-list/install-packages-black-list-item";
    private static final String NODE_VALUE = "value";
    private static final String TAG = DeviceApplicationPlugin.class.getSimpleName();
    private static final String TASK_LOCK_APP_LIST_ITEM = "task-lock-app-list/task-lock-app-list-item";

    public DeviceApplicationPlugin(Context context) {
        super(context);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void procBroadCasts(BroadcastReceiver receiver, PolicyStruct struct) {
        HwLog.i(TAG, "Mdm singleApp or taskLockAppList onReceive");
        this.mContext.unregisterReceiver(receiver);
        String packageName = struct.getPolicyItem("policy-single-app").getAttrValue("value");
        if (!TextUtils.isEmpty(packageName)) {
            startMdmActivity(packageName);
            return;
        }
        List<String> taskLockAppLists = HwDeviceManager.getList(55);
        if (taskLockAppLists != null && !taskLockAppLists.isEmpty()) {
            String packageName2 = taskLockAppLists.get(0);
            if (!TextUtils.isEmpty(packageName2)) {
                startMdmActivity(packageName2);
            }
        }
    }

    private void initAppPolicy(final PolicyStruct struct) {
        if (struct.containsPolicyName("policy-single-app") || struct.containsPolicyName("task-lock-app-list")) {
            IntentFilter filter = new IntentFilter("android.intent.action.BOOT_COMPLETED");
            filter.setPriority(1000);
            this.mContext.registerReceiver(new BroadcastReceiver() {
                /* class com.android.server.devicepolicy.plugins.DeviceApplicationPlugin.AnonymousClass1 */

                @Override // android.content.BroadcastReceiver
                public void onReceive(Context context, Intent intent) {
                    DeviceApplicationPlugin.this.procBroadCasts(this, struct);
                }
            }, filter);
        }
    }

    public boolean onInit(PolicyStruct struct) {
        HwLog.i(TAG, "onInit");
        if (struct == null) {
            return false;
        }
        initAppPolicy(struct);
        return true;
    }

    public String getPluginName() {
        return getClass().getSimpleName();
    }

    public PolicyStruct getPolicyStruct() {
        HwLog.i(TAG, "getPolicyStruct");
        PolicyStruct struct = new PolicyStruct(this);
        struct.addStruct(INSTALL_APKS_BLACK_LIST_ITEM, PolicyStruct.PolicyType.LIST, new String[]{"value"});
        struct.addStruct(TASK_LOCK_APP_LIST_ITEM, PolicyStruct.PolicyType.LIST, new String[]{"value"});
        struct.addStruct("ignore-frequent-relaunch-app", PolicyStruct.PolicyType.LIST, new String[0]);
        struct.addStruct("ignore-frequent-relaunch-app/ignore-frequent-relaunch-app-item", PolicyStruct.PolicyType.LIST, new String[]{"value"});
        struct.addStruct("policy-single-app", PolicyStruct.PolicyType.CONFIGURATION, new String[]{"value"});
        struct.addStruct(FIX_APP_RUNTIME_PERMISSION_LIST, PolicyStruct.PolicyType.LIST, new String[]{"value"});
        return struct;
    }

    public boolean checkCallingPermission(ComponentName who, String policyName) {
        HwLog.i(TAG, "checkCallingPermission");
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_APP_MANAGEMENT", "does not have app_management MDM permission!");
        return true;
    }

    public boolean onSetPolicy(ComponentName who, String policyName, Bundle policyData, boolean isChanged) {
        HwLog.i(TAG, "onSetPolicy");
        if (policyData == null) {
            HwLog.i(TAG, "onSetPolicy policyData is null");
            return false;
        }
        char c = 65535;
        int hashCode = policyName.hashCode();
        if (hashCode != -414055785) {
            if (hashCode == -42495869 && policyName.equals("fix-app-runtime-permission-list")) {
                c = 1;
            }
        } else if (policyName.equals("policy-single-app")) {
            c = 0;
        }
        if (c != 0) {
            if (c == 1) {
                HwLog.i(TAG, "onSetPolicy FIX_APP_RUNTIME_PERMISSION_LIST");
                try {
                    fixRuntimePermission(policyData.getStringArrayList("value"));
                } catch (ArrayIndexOutOfBoundsException e) {
                    HwLog.e(TAG, "onSetPolicy FIX_APP_RUNTIME_PERMISSION_LIST exception");
                    return false;
                }
            } else if (!isEffective(who, policyName, policyData)) {
                return false;
            }
            return true;
        }
        HwLog.i(TAG, "onSetPolicy POLICY_SINGLE_APP");
        Intent launchIntent = this.mContext.getPackageManager().getLaunchIntentForPackage(policyData.getString("value"));
        if (launchIntent == null) {
            return false;
        }
        this.mContext.startActivity(launchIntent);
        return true;
    }

    public boolean onRemovePolicy(ComponentName who, String policyName, Bundle policyData, boolean isEffective) {
        HwLog.i(TAG, "onRemovePolicy");
        if (!isEffective(who, policyName, policyData)) {
            return false;
        }
        char c = 65535;
        if (policyName.hashCode() == -42495869 && policyName.equals("fix-app-runtime-permission-list")) {
            c = 0;
        }
        if (c != 0) {
            return true;
        }
        HwLog.i(TAG, "onRemovePolicy FIX_APP_RUNTIME_PERMISSION_LIST");
        try {
            unFixRuntimePermission(policyData.getStringArrayList("value"));
            return true;
        } catch (ArrayIndexOutOfBoundsException e) {
            HwLog.i(TAG, "onRemovePolicy FIX_APP_RUNTIME_PERMISSION_LIST exception");
            return false;
        }
    }

    public boolean onGetPolicy(ComponentName who, String policyName, Bundle policyData) {
        HwLog.i(TAG, "onGetPolicy");
        if (!isEffective(who, policyName, policyData)) {
            return false;
        }
        return true;
    }

    public boolean onActiveAdminRemoved(ComponentName who, ArrayList<PolicyStruct.PolicyItem> removedPolicies) {
        HwLog.i(TAG, "onActiveAdminRemoved");
        if (removedPolicies == null || removedPolicies.isEmpty()) {
            return true;
        }
        Iterator<PolicyStruct.PolicyItem> it = removedPolicies.iterator();
        while (it.hasNext()) {
            PolicyStruct.PolicyItem policy = it.next();
            if (policy != null) {
                String policyName = policy.getPolicyName();
                char c = 65535;
                if (policyName.hashCode() == -42495869 && policyName.equals("fix-app-runtime-permission-list")) {
                    c = 0;
                }
                if (c == 0) {
                    unFixRuntimePermission(policy.combineAllAttributes().getStringArrayList("value"));
                }
            }
        }
        return true;
    }

    public boolean isEffective(ComponentName who, String policyName, Bundle policyData) {
        if (policyData == null) {
            return true;
        }
        if (!INSTALL_APKS_BLACK_LIST.equals(policyName) && !"ignore-frequent-relaunch-app".equals(policyName)) {
            return true;
        }
        List<String> packageNames = null;
        try {
            packageNames = policyData.getStringArrayList("value");
        } catch (ArrayIndexOutOfBoundsException e) {
            HwLog.e(TAG, "isEffective exception.");
        }
        if (!HwDevicePolicyManagerServiceUtil.isValidatePackageNames(packageNames)) {
            return false;
        }
        return true;
    }

    private void startMdmActivity(String packageName) {
        String str = TAG;
        HwLog.i(str, "startMdmActivity: " + packageName);
        Intent launchIntent = this.mContext.getPackageManager().getLaunchIntentForPackage(packageName);
        if (launchIntent == null) {
            HwLog.i(TAG, "startMdmActivity the launchIntent is null.");
            return;
        }
        try {
            this.mContext.startActivity(launchIntent);
        } catch (ActivityNotFoundException e) {
            HwLog.e(TAG, "startMdmActivity failed the activity is not found.");
        }
    }

    private void fixRuntimePermission(ArrayList<String> packageNames) {
        if (packageNames == null || packageNames.isEmpty()) {
            HwLog.i(TAG, "fixRuntimePermission package list is null");
            return;
        }
        List<String> fixedAppList = HwDeviceManager.getList(56);
        long identityToken = Binder.clearCallingIdentity();
        try {
            Iterator<String> it = packageNames.iterator();
            while (it.hasNext()) {
                String packageName = it.next();
                if (fixedAppList == null || !fixedAppList.contains(packageName)) {
                    checkAndUpdateAppRuntimePermissions(packageName, true);
                } else {
                    String str = TAG;
                    HwLog.i(str, "the package is alreay in the fixed list, package " + packageName);
                }
            }
        } finally {
            Binder.restoreCallingIdentity(identityToken);
        }
    }

    private void unFixRuntimePermission(ArrayList<String> packageNames) {
        if (packageNames == null || packageNames.isEmpty()) {
            HwLog.i(TAG, "unFixRuntimePermission package list is null");
            return;
        }
        List<String> fixedAppList = HwDeviceManager.getList(56);
        long identityToken = Binder.clearCallingIdentity();
        try {
            Iterator<String> it = packageNames.iterator();
            while (it.hasNext()) {
                String packageName = it.next();
                if (fixedAppList != null) {
                    if (fixedAppList.contains(packageName)) {
                        checkAndUpdateAppRuntimePermissions(packageName, false);
                    }
                }
                String str = TAG;
                HwLog.i(str, "the unfixed app is not in fixed list for package " + packageName);
            }
        } finally {
            Binder.restoreCallingIdentity(identityToken);
        }
    }

    private void checkAndUpdateAppRuntimePermissions(String packageName, boolean isFixed) {
        PackageInfo packageInfo = null;
        PackageManager packageManager = this.mContext.getPackageManager();
        try {
            packageInfo = packageManager.getPackageInfo(packageName, 4096);
        } catch (PackageManager.NameNotFoundException e) {
            String str = TAG;
            HwLog.e(str, "not found package for " + packageName);
        }
        if (packageInfo != null && packageInfo.requestedPermissions != null && packageInfo.requestedPermissions.length != 0) {
            if (isSysComponentOrPersistentPlatformSignedPrivApp(packageInfo, packageManager)) {
                String str2 = TAG;
                HwLog.i(str2, "the app is default fixed by the system, package is " + packageName + " can not fix or unfix");
                return;
            }
            updatePermissionsFlag(packageInfo.requestedPermissions, isFixed, packageName);
        }
    }

    private void updatePermissionsFlag(String[] requestedPermissions, boolean isFixed, String packageName) {
        PermissionInfo permInfo;
        int operationFlagValue;
        if (requestedPermissions != null) {
            if (requestedPermissions.length != 0) {
                PackageManager packageManager = this.mContext.getPackageManager();
                for (String permission : requestedPermissions) {
                    try {
                        permInfo = packageManager.getPermissionInfo(permission, 0);
                    } catch (PackageManager.NameNotFoundException e) {
                        HwLog.e(TAG, "not found the permission " + permission + " package is " + packageName);
                        permInfo = null;
                    }
                    if (permInfo != null && permInfo.getProtection() == 1) {
                        if (packageManager.checkPermission(permInfo.name, packageName) != 0) {
                            HwLog.i(TAG, "runtime permission " + permInfo.name + " is not granted for " + packageName);
                        } else {
                            int permissionFlag = packageManager.getPermissionFlags(permInfo.name, packageName, UserHandle.SYSTEM);
                            if ((permissionFlag & 65536) != 0) {
                                HwLog.i(TAG, "runtime permission " + permInfo.name + " is granted just one time for " + packageName);
                            } else {
                                int permissonFixedValue = permissionFlag & 16;
                                if (isFixed) {
                                    if (permissonFixedValue != 0) {
                                        HwLog.i(TAG, "runtime permission " + permInfo.name + " is already fixed for " + packageName);
                                    } else {
                                        operationFlagValue = 16;
                                    }
                                } else if (permissonFixedValue == 0) {
                                    HwLog.i(TAG, "runtime permission " + permInfo.name + " is already unfixed for " + packageName);
                                } else {
                                    operationFlagValue = -17;
                                }
                                packageManager.updatePermissionFlags(permInfo.name, packageName, 16, operationFlagValue, UserHandle.SYSTEM);
                            }
                        }
                    }
                }
                return;
            }
        }
        HwLog.i(TAG, "the requested permissions or operation type is invalid");
    }

    private boolean isSysComponentOrPersistentPlatformSignedPrivApp(PackageInfo pkg, PackageManager packageManager) {
        if (UserHandle.getAppId(pkg.applicationInfo.uid) < 10000) {
            return true;
        }
        if (pkg.applicationInfo.isPrivilegedApp() && (pkg.applicationInfo.flags & 8) != 0 && packageManager.checkSignatures("android", pkg.packageName) == 0) {
            return true;
        }
        return false;
    }
}

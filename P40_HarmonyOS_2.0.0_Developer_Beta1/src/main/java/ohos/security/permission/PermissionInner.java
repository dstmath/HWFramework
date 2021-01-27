package ohos.security.permission;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.ServiceManager;
import android.text.TextUtils;
import com.android.internal.app.IAppOpsService;
import com.huawei.android.os.UserHandleEx;
import java.util.Objects;
import java.util.Set;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.RemoteException;
import ohos.security.SystemPermission;
import ohos.security.dpermissionkit.DPermissionKitProxy;
import ohos.utils.LightweightSet;
import ohos.utils.fastjson.JSONException;
import ohos.utils.zson.ZSONException;
import ohos.utils.zson.ZSONObject;

public final class PermissionInner {
    private static final String BUNDLE_NAME = "bundleName";
    private static final int DEFAULT_SIZE = 14;
    public static final int DENIED = -1;
    private static final String DEVICE_ID = "deviceID";
    private static final int DUSER_ID_ANDROID = 126;
    private static final int DUSER_ID_HARMONY = 125;
    private static final int FAILTURE_CODE = -1;
    public static final int GRANTED = 0;
    private static final HiLogLabel LABEL = new HiLogLabel(3, SUB_DOMAIN_SECURITY_DPERMISSION, "PermissionInner");
    private static final String MANAGE_APP_POLICY = "ohos.permission.MANAGE_APP_POLICY";
    private static final int PER_USER_RANGE = 100000;
    private static final String PID = "pid";
    private static final Set<String> RESTRICTED_PERMISSION_SET = new LightweightSet(14);
    private static final int SUB_DOMAIN_SECURITY_DPERMISSION = 218115841;
    private static final String UID = "uid";

    static {
        RESTRICTED_PERMISSION_SET.add(SystemPermission.ANSWER_CALL);
        RESTRICTED_PERMISSION_SET.add(SystemPermission.PLACE_CALL);
        RESTRICTED_PERMISSION_SET.add("ohos.permission.GET_PRIVILEGED_PHONE_STATE");
        RESTRICTED_PERMISSION_SET.add("ohos.permission.GET_PHONE_STATE");
        RESTRICTED_PERMISSION_SET.add(SystemPermission.READ_CALL_LOG);
        RESTRICTED_PERMISSION_SET.add(SystemPermission.WRITE_CALL_LOG);
        RESTRICTED_PERMISSION_SET.add(SystemPermission.READ_CELL_MESSAGES);
        RESTRICTED_PERMISSION_SET.add(SystemPermission.READ_MESSAGES);
        RESTRICTED_PERMISSION_SET.add(SystemPermission.RECEIVE_MMS);
        RESTRICTED_PERMISSION_SET.add(SystemPermission.RECEIVE_SMS);
        RESTRICTED_PERMISSION_SET.add(SystemPermission.RECEIVE_WAP_MESSAGES);
        RESTRICTED_PERMISSION_SET.add(SystemPermission.SEND_MESSAGES);
        RESTRICTED_PERMISSION_SET.add(SystemPermission.READ_CONTACTS);
        RESTRICTED_PERMISSION_SET.add(SystemPermission.WRITE_CONTACTS);
    }

    private PermissionInner() {
    }

    public static boolean isDuid(int i) {
        int i2 = i / 100000;
        return i2 >= DUSER_ID_HARMONY && i2 <= 126;
    }

    public static boolean isRestrictedPermission(String str) {
        if (str == null) {
            return false;
        }
        return RESTRICTED_PERMISSION_SET.contains(PermissionConversion.getZosPermissionNameIfPossible(str));
    }

    public static int registerUsingPermissionReminder(OnUsingPermissionReminder onUsingPermissionReminder) {
        try {
            return DPermissionKitProxy.getInstance().registerUsingPermissionReminder(onUsingPermissionReminder);
        } catch (RemoteException unused) {
            HiLog.error(LABEL, "Failed to registerUsingPermissionReminder because of RemoteException", new Object[0]);
            return -1;
        }
    }

    public static int unregisterUsingPermissionReminder(OnUsingPermissionReminder onUsingPermissionReminder) {
        try {
            return DPermissionKitProxy.getInstance().unregisterUsingPermissionReminder(onUsingPermissionReminder);
        } catch (RemoteException unused) {
            HiLog.error(LABEL, "Failed to unregisterUsingPermissionReminder because of RemoteException", new Object[0]);
            return -1;
        }
    }

    public static void addPermissionUsedRecord(String str, String str2, int i, int i2) {
        if (TextUtils.isEmpty(str) || TextUtils.isEmpty(str2)) {
            HiLog.error(LABEL, "invalid param for permName: %{public}s or appIdInfo: %{public}s", str, str2);
            return;
        }
        try {
            DPermissionKitProxy.getInstance().addPermissionUsedRecord(PermissionConversion.getAosPermissionNameIfPossible(str), str2, i, i2);
        } catch (RemoteException unused) {
            HiLog.error(LABEL, "Failed to call addPermissionUsedRecord because of RemoteException", new Object[0]);
        }
    }

    public static int getPermissionUsedRecords(QueryPermissionUsedRequest queryPermissionUsedRequest, QueryPermissionUsedResult queryPermissionUsedResult) {
        if (Objects.isNull(queryPermissionUsedRequest) || Objects.isNull(queryPermissionUsedResult)) {
            HiLog.error(LABEL, "invalid param for request: %{public}s or result: %{public}s", queryPermissionUsedRequest, queryPermissionUsedResult);
            return -1;
        }
        try {
            return DPermissionKitProxy.getInstance().getPermissionUsedRecords(queryPermissionUsedRequest, queryPermissionUsedResult);
        } catch (RemoteException unused) {
            HiLog.error(LABEL, "Failed to call getPermissionUsedRecords because of RemoteException", new Object[0]);
            return -1;
        }
    }

    public static int getPermissionUsedRecords(QueryPermissionUsedRequest queryPermissionUsedRequest, OnPermissionUsedRecord onPermissionUsedRecord) {
        if (Objects.isNull(queryPermissionUsedRequest) || Objects.isNull(onPermissionUsedRecord)) {
            HiLog.error(LABEL, "invalid param for request: %{public}s or callback: %{public}s", queryPermissionUsedRequest, onPermissionUsedRecord);
            return -1;
        }
        try {
            return DPermissionKitProxy.getInstance().getPermissionUsedRecordsAsync(queryPermissionUsedRequest, onPermissionUsedRecord);
        } catch (RemoteException unused) {
            HiLog.error(LABEL, "Failed to call getPermissionUsedRecordsAsync because of RemoteException", new Object[0]);
            return -1;
        }
    }

    public static int checkPermissionAndStartUsing(String str, String str2) {
        if (str != null && str2 != null) {
            return PermissionKitAdapter.checkPermissionAndStartUsing(str, str2);
        }
        HiLog.error(LABEL, "checkPermissionAndStartUsing::parameters(permName or appIdInfo) is illegal.", new Object[0]);
        return -1;
    }

    public static int checkCallerPermissionAndStartUsing(String str) {
        if (str != null) {
            return PermissionKitAdapter.checkCallerPermissionAndStartUsing(str);
        }
        HiLog.error(LABEL, "checkCallerPermissionAndStartUsing::permissionName is null.", new Object[0]);
        return -1;
    }

    public static void startUsingPermission(String str, String str2) {
        if (str == null || str2 == null) {
            HiLog.error(LABEL, "startUsingPermission::parameters(permName or appIdInfo) is illegal.", new Object[0]);
        } else {
            PermissionKitAdapter.startUsingPermission(str, str2);
        }
    }

    public static void stopUsingPermission(String str, String str2) {
        if (str == null || str2 == null) {
            HiLog.error(LABEL, "stopUsingPermission::parameters(permName or appIdInfo) is illegal.", new Object[0]);
        } else {
            PermissionKitAdapter.stopUsingPermission(str, str2);
        }
    }

    public static void postPermissionEvent(String str) {
        if (str == null || str.isEmpty()) {
            HiLog.error(LABEL, "PostPermissionEvent::event is illegal.", new Object[0]);
        } else {
            PermissionKitAdapter.nativePostPermissionEvent(str);
        }
    }

    public static int checkPermission(String str, String str2) {
        if (str != null && str2 != null) {
            return PermissionKitAdapter.checkPermission(str, str2);
        }
        HiLog.error(LABEL, "checkPermission::parameters(permName or appIdInfo) is illegal.", new Object[0]);
        return -1;
    }

    public static int checkPermissionAndUse(String str, String str2) {
        if (str != null && str2 != null) {
            return PermissionKitAdapter.checkPermissionAndUse(str, str2);
        }
        HiLog.error(LABEL, "checkPermissionAndUse::parameters(permName or appIdInfo) is illegal.", new Object[0]);
        return -1;
    }

    public static int checkCallerPermissionAndUse(String str) {
        if (str != null) {
            return PermissionKitAdapter.checkCallerPermissionAndUse(str);
        }
        HiLog.error(LABEL, "checkCallerPermissionAndUse::permissionName is null.", new Object[0]);
        return -1;
    }

    public static int checkCallerPermission(String str) {
        if (str != null) {
            return PermissionKitAdapter.checkCallerPermission(str);
        }
        HiLog.error(LABEL, "checkCallerPermission::permissionName is null.", new Object[0]);
        return -1;
    }

    public static int checkSelfPermission(String str) {
        if (str != null) {
            return PermissionKitAdapter.checkSelfPermission(str);
        }
        HiLog.error(LABEL, "checkSelfPermission::permissionName is null.", new Object[0]);
        return -1;
    }

    public static class AppIdInfoHelper {
        private static String appBundleName = null;
        private static String appDeviceId = null;
        private static int appPid = -1;
        private static int appUid = -1;

        public static String createAppIdInfo(int i, int i2) {
            ZSONObject zSONObject = new ZSONObject();
            zSONObject.put(PermissionInner.PID, (Object) Integer.valueOf(i));
            zSONObject.put(PermissionInner.UID, (Object) Integer.valueOf(i2));
            return zSONObject.toString();
        }

        public static String createAppIdInfo(int i, int i2, String str) {
            ZSONObject zSONObject = new ZSONObject();
            zSONObject.put(PermissionInner.PID, (Object) Integer.valueOf(i));
            zSONObject.put(PermissionInner.UID, (Object) Integer.valueOf(i2));
            if (!(str == null || str.length() == 0)) {
                zSONObject.put(PermissionInner.DEVICE_ID, (Object) str);
            }
            return zSONObject.toString();
        }

        public static String createAppIdInfo(int i, int i2, String str, String str2) {
            ZSONObject zSONObject = new ZSONObject();
            zSONObject.put(PermissionInner.PID, (Object) Integer.valueOf(i));
            zSONObject.put(PermissionInner.UID, (Object) Integer.valueOf(i2));
            if (!(str == null || str.length() == 0)) {
                zSONObject.put(PermissionInner.DEVICE_ID, (Object) str);
            }
            if (!(str2 == null || str2.length() == 0)) {
                zSONObject.put(PermissionInner.BUNDLE_NAME, (Object) str2);
            }
            return zSONObject.toString();
        }

        public static void parseAppidInfo(String str) {
            initAppidInfoData();
            try {
                ZSONObject stringToZSON = ZSONObject.stringToZSON(str);
                if (stringToZSON == null) {
                    HiLog.error(PermissionInner.LABEL, "stringToZSON failed: %{public}s", str);
                    return;
                }
                appUid = stringToZSON.getInteger(PermissionInner.UID).intValue();
                appPid = stringToZSON.getInteger(PermissionInner.PID).intValue();
                appBundleName = stringToZSON.getString(PermissionInner.BUNDLE_NAME);
                appDeviceId = stringToZSON.getString(PermissionInner.DEVICE_ID);
                HiLog.debug(PermissionInner.LABEL, "get : uid %{public}d, pid %{public}d, dev %{private}s, bundlename %{public}s", Integer.valueOf(appUid), Integer.valueOf(appPid), appDeviceId, appBundleName);
            } catch (JSONException | ZSONException e) {
                HiLog.error(PermissionInner.LABEL, "translation appidInfo JSONException: %{public}s", e.getMessage());
            } catch (Exception e2) {
                HiLog.error(PermissionInner.LABEL, "translation appidInfo unexpected exception: %{public}s", e2.getMessage());
            }
        }

        public static int getAppUid() {
            return appUid;
        }

        public static int getAppPid() {
            return appPid;
        }

        public static String getAppDeviceId() {
            return appDeviceId;
        }

        public static String getAppBundleName() {
            return appBundleName;
        }

        private static void initAppidInfoData() {
            appUid = -1;
            appPid = -1;
            appBundleName = null;
            appDeviceId = null;
        }
    }

    public static void grantSensitivePermission(Object obj, String str, String str2, int i) {
        if (isValidContext(obj) && str != null) {
            String aosPermissionName = getAosPermissionName(str);
            PackageManager packageManager = ((Context) obj).getPackageManager();
            if (packageManager != null) {
                packageManager.grantRuntimePermission(str2, aosPermissionName, UserHandleEx.getUserHandle(i));
            }
        }
    }

    public static void revokeSensitivePermission(Object obj, String str, String str2, int i) {
        if (isValidContext(obj) && str != null) {
            String aosPermissionName = getAosPermissionName(str);
            PackageManager packageManager = ((Context) obj).getPackageManager();
            if (packageManager != null) {
                packageManager.revokeRuntimePermission(str2, aosPermissionName, UserHandleEx.getUserHandle(i));
            }
        }
    }

    public static int getPermissionStatus(Object obj, String str, String str2, int i) {
        if (!isValidContext(obj) || str == null) {
            return 0;
        }
        String aosPermissionName = getAosPermissionName(str);
        PackageManager packageManager = ((Context) obj).getPackageManager();
        if (packageManager == null) {
            return 0;
        }
        return packageManager.getPermissionFlags(aosPermissionName, str2, UserHandleEx.getUserHandle(i));
    }

    public static void updatePermissionStatus(Object obj, String str, String str2, int i, int i2, int i3) {
        if (isValidContext(obj) && str != null) {
            String aosPermissionName = getAosPermissionName(str);
            PackageManager packageManager = ((Context) obj).getPackageManager();
            if (packageManager != null) {
                packageManager.updatePermissionFlags(aosPermissionName, str2, i, i2, UserHandleEx.getUserHandle(i3));
            }
        }
    }

    public static boolean isPermissionDiscarded(PermissionDef permissionDef) {
        return (permissionDef.permissionFlags & 1) != 0;
    }

    public static void setPermissionState(Object obj, String str, int i, int i2) {
        if (isValidContext(obj) && str != null) {
            String permissionToOp = AppOpsManager.permissionToOp(getAosPermissionName(str));
            if (permissionToOp == null) {
                HiLog.error(LABEL, "param error", new Object[0]);
                return;
            }
            AppOpsManager appOpsManager = (AppOpsManager) ((Context) obj).getSystemService(AppOpsManager.class);
            if (appOpsManager == null) {
                HiLog.error(LABEL, "get service error", new Object[0]);
            } else {
                appOpsManager.setUidMode(permissionToOp, i, i2);
            }
        }
    }

    public static int verifyBundlePermission(String str, String str2) {
        if (checkCallerPermission("ohos.permission.MANAGE_APP_POLICY") == -1) {
            throw new SecurityException("verifyBundle:Permission denied. You need ohos.permission.MANAGE_APP_POLICY permission.");
        } else if (TextUtils.isEmpty(str) || TextUtils.isEmpty(str2)) {
            HiLog.error(LABEL, "verifyBundle:invalid param error, permissionName %{public}s, appidInfo %{public}s", str, str2);
            return -1;
        } else {
            AppIdInfoHelper.parseAppidInfo(str2);
            int appUid = AppIdInfoHelper.getAppUid();
            String appBundleName = AppIdInfoHelper.getAppBundleName();
            HiLog.debug(LABEL, "verifyBundle:verify the permission %{public}s for :%{public}d,%{public}s", str, Integer.valueOf(appUid), appBundleName);
            IAppOpsService asInterface = IAppOpsService.Stub.asInterface(ServiceManager.getService("appops"));
            if (asInterface == null) {
                HiLog.error(LABEL, "verifyBundle:get appops service error", new Object[0]);
                return -1;
            }
            try {
                if (asInterface.checkPackage(appUid, appBundleName) == 2) {
                    HiLog.error(LABEL, "verifyBundle:The UID %{public}d does not match the package name %{public}s.", Integer.valueOf(appUid), appBundleName);
                    return -1;
                }
                int permissionToOpCode = asInterface.permissionToOpCode(getAosPermissionName(str));
                if (permissionToOpCode == -1) {
                    HiLog.error(LABEL, "verifyBundle:dont find the code for %{public}s", str);
                    return -1;
                }
                int noteOperation = asInterface.noteOperation(permissionToOpCode, appUid, appBundleName);
                HiLog.debug(LABEL, "verifyBundle:check ops result:%{public}d", Integer.valueOf(noteOperation));
                if (noteOperation == 0) {
                    return 0;
                }
                return -1;
            } catch (android.os.RemoteException unused) {
                HiLog.error(LABEL, "verifyBundle:failed to appops RemoteException", new Object[0]);
                return -1;
            }
        }
    }

    private static String getAosPermissionName(String str) {
        String aosPermissionNameIfPossible = PermissionConversion.getAosPermissionNameIfPossible(str);
        HiLog.debug(LABEL, "permissionName: %{public}s", aosPermissionNameIfPossible);
        return aosPermissionNameIfPossible;
    }

    private static boolean isValidContext(Object obj) {
        if (obj == null) {
            HiLog.error(LABEL, "the param should not be null", new Object[0]);
            return false;
        } else if (obj instanceof Context) {
            return true;
        } else {
            HiLog.error(LABEL, "param type error, %s", obj.toString());
            return false;
        }
    }
}

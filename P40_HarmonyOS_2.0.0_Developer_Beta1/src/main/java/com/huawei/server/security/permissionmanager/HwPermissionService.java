package com.huawei.server.security.permissionmanager;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.UserHandle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import com.huawei.android.content.ContextEx;
import com.huawei.android.provider.DeviceConfigEx;
import com.huawei.android.util.SlogEx;
import com.huawei.securitycenter.permission.ui.model.DbPermissionItem;
import com.huawei.server.security.core.IHwSecurityPlugin;
import com.huawei.server.security.permissionmanager.onetimepermission.OneTimePermissionInfoManager;
import com.huawei.server.security.permissionmanager.packageinstaller.PackageInstallerPermissionHelper;
import com.huawei.server.security.permissionmanager.recommendpermission.GetRecommendPermissionsCaller;
import com.huawei.server.security.permissionmanager.sms.SmsAuthenticateInfo;
import com.huawei.server.security.permissionmanager.sms.SmsAuthenticateManager;
import com.huawei.server.security.permissionmanager.sms.SmsSendBlockDataMgr;
import com.huawei.server.security.permissionmanager.sms.smsutils.Utils;
import com.huawei.server.security.permissionmanager.util.HwPermUtils;
import com.huawei.server.security.permissionmanager.util.PermConst;
import com.huawei.server.security.permissionmanager.util.PermissionClass;
import huawei.android.security.IHwPermissionManager;
import huawei.android.security.IOnHwPermissionChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class HwPermissionService extends IHwPermissionManager.Stub implements IHwSecurityPlugin {
    private static final int COUNT_DOWN_TIME = 1;
    public static final IHwSecurityPlugin.Creator CREATOR = new IHwSecurityPlugin.Creator() {
        /* class com.huawei.server.security.permissionmanager.HwPermissionService.AnonymousClass1 */

        @Override // com.huawei.server.security.core.IHwSecurityPlugin.Creator
        public IHwSecurityPlugin createPlugin(Context context) {
            return new HwPermissionService(context);
        }

        @Override // com.huawei.server.security.core.IHwSecurityPlugin.Creator
        public String getPluginPermission() {
            return null;
        }
    };
    private static final int MSG_RELEASE_HOLD_SERVICE = 1;
    private static final long PERMISSION_SEND_MMS = 8192;
    private static final int PERMISSION_SEND_MMS_OLD = 8192;
    private static final int PERMISSION_TYPE_BLOCKED = 2;
    private static final String SECURITY_CENTER_PERMISSION = "com.huawei.systemmanager.permission.ACCESS_INTERFACE";
    private static final String TAG = "HwPermissionService";
    private static final int UN_USED_PERMISSION = 0;
    private static final int UN_USED_UID = 0;
    private static final long WAIT_TIME_MILLIS = 20000;
    private Context mContext;
    private CountDownLatch mCountDownLatch;
    private GetRecommendPermissionsCaller mGetRecommendPermCaller;
    private Handler mHandler = new Handler() {
        /* class com.huawei.server.security.permissionmanager.HwPermissionService.AnonymousClass2 */

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                HwPermissionService.this.setUserAuthResult(0, 2, 0);
            }
            super.handleMessage(msg);
        }
    };
    private ServiceConnection mHoldShowServiceConnection = new ServiceConnection() {
        /* class com.huawei.server.security.permissionmanager.HwPermissionService.AnonymousClass3 */

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName className, IBinder service) {
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName className) {
            HwPermissionService.this.mHandler.sendEmptyMessage(1);
        }
    };
    private HwPermDbAdapter mHwPermDbAdapter;
    private HwSmsBlockDbAdapter mHwSmsBlockDbAdapter;
    private OneTimePermissionInfoManager mOneTimePermissionInfoManager;
    private PackageInstallerPermissionHelper mPackageInstallerPermHelper;
    private RecommendPermDbAdapter mRecommendPermDbAdapter;
    private int mUserSelection;

    public HwPermissionService(Context context) {
        try {
            this.mContext = context;
            SlogEx.i(TAG, "create HwPermissionService");
            this.mHwPermDbAdapter = HwPermDbAdapter.getInstance(context);
            this.mHwSmsBlockDbAdapter = HwSmsBlockDbAdapter.getInstance(context);
            this.mPackageInstallerPermHelper = new PackageInstallerPermissionHelper();
            this.mOneTimePermissionInfoManager = OneTimePermissionInfoManager.getInstance(context);
            this.mOneTimePermissionInfoManager.registerReceiver();
            this.mRecommendPermDbAdapter = RecommendPermDbAdapter.getInstance(context);
            this.mGetRecommendPermCaller = GetRecommendPermissionsCaller.getInstance(context);
        } catch (IllegalArgumentException e) {
            SlogEx.e(TAG, "illegal argument during create.");
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for r0v0, resolved type: com.huawei.server.security.permissionmanager.HwPermissionService */
    /* JADX WARN: Multi-variable type inference failed */
    @Override // com.huawei.server.security.core.IHwSecurityPlugin
    public IBinder asBinder() {
        return this;
    }

    @Override // com.huawei.server.security.core.IHwSecurityPlugin
    public void onStart() {
        SlogEx.i(TAG, "onStart");
    }

    @Override // com.huawei.server.security.core.IHwSecurityPlugin
    public void onStop() {
        SlogEx.i(TAG, "onStop");
    }

    public void addOnPermissionsChangeListener(IOnHwPermissionChangeListener listener) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.GRANT_RUNTIME_PERMISSIONS", "does not have add hwPermission listener permission!");
        this.mHwPermDbAdapter.addOnPermissionsChangeListener(listener);
    }

    public void removeOnPermissionsChangeListener(IOnHwPermissionChangeListener listener) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.GRANT_RUNTIME_PERMISSIONS", "does not have remove hwPermission listener permission!");
        this.mHwPermDbAdapter.removeOnPermissionsChangeListener(listener);
    }

    public Bundle getHwPermissionInfo(String pkgName, int userId, long permType, Bundle params) throws RemoteException {
        if (params == null || params.isEmpty()) {
            SlogEx.e(TAG, "getHwPermissionInfo params empty");
            return new Bundle();
        }
        String methodName = params.getString(PermConst.NAME_KEY);
        if (TextUtils.isEmpty(methodName)) {
            SlogEx.e(TAG, "getHwPermissionInfo methodName empty");
            return new Bundle();
        }
        char c = 65535;
        switch (methodName.hashCode()) {
            case -1761693118:
                if (methodName.equals(PermConst.GET_HW_PERM_INFO_FOR_PACKAGE_INSTALLER)) {
                    c = 4;
                    break;
                }
                break;
            case -136001660:
                if (methodName.equals(PermConst.GET_MMS_PERM_INFO)) {
                    c = 7;
                    break;
                }
                break;
            case 131109145:
                if (methodName.equals(PermConst.GET_PROPERTY_PERMISSIONS_HUB)) {
                    c = 5;
                    break;
                }
                break;
            case 198958484:
                if (methodName.equals(PermConst.GET_ALL_APP_PERM_INFO)) {
                    c = 3;
                    break;
                }
                break;
            case 455458759:
                if (methodName.equals(PermConst.CHECK_HW_PERM_INFO)) {
                    c = 2;
                    break;
                }
                break;
            case 809047573:
                if (methodName.equals(PermConst.CHECK_SMS_BLOCK_STATE)) {
                    c = 6;
                    break;
                }
                break;
            case 890824767:
                if (methodName.equals(PermConst.GET_ONE_TIME_PERMISSION_CONTROL_INFO)) {
                    c = '\b';
                    break;
                }
                break;
            case 901866407:
                if (methodName.equals(PermConst.GET_HW_PERM_APPS_INFO)) {
                    c = 1;
                    break;
                }
                break;
            case 902102499:
                if (methodName.equals(PermConst.GET_HW_PERM_INFO)) {
                    c = 0;
                    break;
                }
                break;
            case 980219381:
                if (methodName.equals(PermConst.GET_RECOMMEND_PERM_FOR_INSTALLER)) {
                    c = '\t';
                    break;
                }
                break;
        }
        switch (c) {
            case 0:
                return getHwPermInfoInner(pkgName, userId);
            case 1:
                return getHwPermAppsInner(userId, permType);
            case 2:
                return checkHwPermInner(pkgName, userId, permType);
            case 3:
                return getAppAppsPermInfoInner(userId);
            case 4:
                return getHwPermInfoForPackageInstaller(pkgName, params);
            case SmsAuthenticateManager.AUTHENTICATE_RESULT_LOCKED /* 5 */:
                return getPermissionHubProperty();
            case 6:
                return checkSmsBlockStateInner(userId, pkgName);
            case 7:
                return getMmsPermInfoInner();
            case '\b':
                return getOneTimePermissionControlInfo();
            case '\t':
                return getRecommendPermForInstaller(pkgName, params);
            default:
                return new Bundle();
        }
    }

    public void setHwPermissionInfo(int userId, Bundle params) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission("android.permission.GRANT_RUNTIME_PERMISSIONS", "does not have set hwPermission information permission!");
        if (params == null || params.isEmpty()) {
            SlogEx.e(TAG, "setHwPermissionInfo params empty");
            return;
        }
        String methodName = params.getString(PermConst.NAME_KEY);
        if (!TextUtils.isEmpty(methodName)) {
            char c = 65535;
            switch (methodName.hashCode()) {
                case -636926305:
                    if (methodName.equals(PermConst.SET_HW_PERM_INFO_FOR_PACKAGE_INSTALLER)) {
                        c = 4;
                        break;
                    }
                    break;
                case 553751529:
                    if (methodName.equals(PermConst.SET_RECOMMEND_PERMISSION)) {
                        c = 11;
                        break;
                    }
                    break;
                case 578392965:
                    if (methodName.equals(PermConst.REPLACE_PERM_ALL_APP)) {
                        c = 2;
                        break;
                    }
                    break;
                case 617877934:
                    if (methodName.equals(PermConst.INIT_SMS_BLOCK_DATA)) {
                        c = 5;
                        break;
                    }
                    break;
                case 805456715:
                    if (methodName.equals(PermConst.SET_ONE_TIME_PERMISSION_CONTROL_INFO)) {
                        c = '\n';
                        break;
                    }
                    break;
                case 1168995776:
                    if (methodName.equals(PermConst.SET_HW_PERM_INFO)) {
                        c = 0;
                        break;
                    }
                    break;
                case 1369237901:
                    if (methodName.equals(PermConst.SET_PROPERTY_PERMISSIONS_HUB)) {
                        c = '\t';
                        break;
                    }
                    break;
                case 1504314305:
                    if (methodName.equals(PermConst.REPLACE_HW_PERM_INFO)) {
                        c = 3;
                        break;
                    }
                    break;
                case 1738638548:
                    if (methodName.equals(PermConst.NOTIFY_SMS_USER_RESET)) {
                        c = '\b';
                        break;
                    }
                    break;
                case 1879130803:
                    if (methodName.equals(PermConst.SET_HW_PERMS_INFO)) {
                        c = 1;
                        break;
                    }
                    break;
                case 1979466939:
                    if (methodName.equals(PermConst.HOTA_UPDATE_SMS_BLOCK_DATA)) {
                        c = 6;
                        break;
                    }
                    break;
                case 2005051904:
                    if (methodName.equals(PermConst.NOTIFY_SMS_PKG_REMOVE)) {
                        c = 7;
                        break;
                    }
                    break;
            }
            switch (c) {
                case 0:
                    setHwPermissionInner(userId, params);
                    return;
                case 1:
                    setHwPermissionsInner(userId, params);
                    return;
                case 2:
                    replacePermForAllAppsInner(userId, params);
                    return;
                case 3:
                    replaceHwPermInfoInner(userId, params);
                    return;
                case 4:
                    this.mPackageInstallerPermHelper.setHwPermission(this.mContext, params);
                    return;
                case SmsAuthenticateManager.AUTHENTICATE_RESULT_LOCKED /* 5 */:
                    initSmsBlockDataInner(userId, params);
                    return;
                case 6:
                    hotaUpdateSmsBlockDataInner(userId, params);
                    return;
                case 7:
                    notifyPkgRemoveInner(params);
                    return;
                case '\b':
                    notifyUserResetInner();
                    return;
                case '\t':
                    setPermissionHubProperty(params);
                    return;
                case HwPermUtils.DEFAULT_LIST_CAPACITY /* 10 */:
                    setOneTimePermissionControlInfo(params);
                    return;
                case 11:
                    setRecommendPermission(params);
                    return;
                default:
                    SlogEx.e(TAG, "illegal methodName:" + methodName);
                    return;
            }
        }
    }

    public void removeHwPermissionInfo(String pkgName, int userId) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission("android.permission.GRANT_RUNTIME_PERMISSIONS", "does not have remove hwPermission information permission!");
        if (TextUtils.isEmpty(pkgName)) {
            SlogEx.e(TAG, "removeHwPermissionInfo pkgName empty");
        } else {
            this.mHwPermDbAdapter.removeHwPermission(pkgName, userId);
        }
    }

    public void authenticateSmsSend(IBinder notifyResult, int uidOf3RdApk, int smsId, String smsBody, String smsAddress) {
        SmsAuthenticateManager.getInstance(this.mContext).requestAuthenticate(notifyResult, uidOf3RdApk, smsId, smsBody, smsAddress);
    }

    public String getSmsAuthPackageName() {
        this.mContext.enforceCallingPermission(SECURITY_CENTER_PERMISSION, "does not have get sms authenticate package name permission!");
        SmsAuthenticateInfo smsInfo = SmsAuthenticateManager.getInstance(this.mContext).getSmsAuthInfo();
        if (smsInfo != null) {
            return smsInfo.getPackageName();
        }
        SlogEx.e(TAG, "getSmsAuthPackageName smsInfo is null");
        return null;
    }

    public synchronized void setUserAuthResult(int uid, int userSelection, long permissionType) {
        this.mContext.enforceCallingOrSelfPermission(SECURITY_CENTER_PERMISSION, "does not have set user authenticate result permission!");
        if (PermissionClass.isSmsGroup(permissionType)) {
            SmsAuthenticateManager.getInstance(this.mContext).onAuthenticateResult(userSelection);
        } else {
            SlogEx.i(TAG, "setUserAuthResult: uid is " + uid + " userSelection is " + userSelection + " permissionType is " + permissionType);
            this.mHandler.removeMessages(1);
            this.mUserSelection = userSelection;
            if (this.mCountDownLatch != null) {
                this.mCountDownLatch.countDown();
            }
        }
    }

    public int holdServiceByRequestPermission(int uid, int pid, long permissionType) {
        int i;
        long identity = Binder.clearCallingIdentity();
        synchronized (this) {
            SlogEx.i(TAG, "holdServiceByRequestPermission permissionType:" + permissionType);
            this.mUserSelection = 2;
            UserHandle userHandle = UserHandle.getUserHandleForUid(uid);
            SlogEx.i(TAG, "holdServiceByRequestPermission uid:" + uid + ",pid:" + pid + ",userhandle:" + userHandle);
            try {
                Intent intent = new Intent();
                intent.setClassName(PermConst.SYSTEM_MANAGER_PACKAGE_NAME, "com.huawei.securitycenter.permission.service.holddialog.HoldDialogShowService");
                intent.putExtra("permissionType", permissionType);
                intent.putExtra("appUid", uid);
                intent.putExtra("appPid", pid);
                this.mContext.bindService(intent, this.mHoldShowServiceConnection, 1);
                ContextEx.startServiceAsUser(this.mContext, intent, userHandle);
            } catch (IllegalArgumentException | IllegalStateException e) {
                SlogEx.e(TAG, "holdServiceByRequestPermission bindService IllegalException:" + e);
                return this.mUserSelection;
            } catch (Exception e2) {
                SlogEx.e(TAG, "holdServiceByRequestPermission bindService Exception!");
                return this.mUserSelection;
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }
        this.mCountDownLatch = new CountDownLatch(1);
        try {
            this.mCountDownLatch.await(WAIT_TIME_MILLIS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e3) {
            SlogEx.e(TAG, "holdServiceByRequestPermission InterruptedException:" + e3);
            this.mCountDownLatch.countDown();
        }
        synchronized (this) {
            long identity2 = Binder.clearCallingIdentity();
            try {
                this.mContext.unbindService(this.mHoldShowServiceConnection);
            } catch (IllegalArgumentException e4) {
                SlogEx.e(TAG, "holdServiceByRequestPermission unbindService IllegalArgumentException:" + e4);
            } catch (Exception e5) {
                SlogEx.e(TAG, "holdServiceByRequestPermission unbindService Exception!");
            } finally {
                Binder.restoreCallingIdentity(identity2);
            }
            SlogEx.i(TAG, "holdServiceByRequestPermission mUserSelection:" + this.mUserSelection);
            i = this.mUserSelection;
        }
        return i;
    }

    private Bundle getAppAppsPermInfoInner(int userId) {
        ArrayList<DbPermissionItem> list = this.mHwPermDbAdapter.getAllAppsPermInfo(userId);
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(PermConst.RETURN_RESULT_KEY, list);
        return bundle;
    }

    private Bundle getHwPermInfoInner(String pkgName, int userId) {
        if (TextUtils.isEmpty(pkgName)) {
            SlogEx.e(TAG, "getHwPermInfoInner pkgName empty");
            return new Bundle();
        }
        DbPermissionItem permInfo = this.mHwPermDbAdapter.getHwPermInfo(pkgName, userId);
        Bundle bundle = new Bundle();
        bundle.putParcelable(PermConst.RETURN_RESULT_KEY, permInfo);
        return bundle;
    }

    private Bundle getHwPermAppsInner(int userId, long permType) {
        ArrayList<DbPermissionItem> appList = this.mHwPermDbAdapter.getHwPermApps(permType, userId);
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(PermConst.RETURN_RESULT_KEY, appList);
        return bundle;
    }

    private Bundle checkHwPermInner(String pkgName, int userId, long permType) {
        if (TextUtils.isEmpty(pkgName)) {
            SlogEx.e(TAG, "checkHwPermInner pkgName empty");
            return new Bundle();
        }
        int result = this.mHwPermDbAdapter.checkHwPerm(pkgName, permType, userId);
        Bundle bundle = new Bundle();
        bundle.putInt(PermConst.RETURN_RESULT_KEY, result);
        return bundle;
    }

    private void setHwPermissionInner(int userId, @NonNull Bundle params) {
        long permType = params.getLong(PermConst.PERM_TYPE_KEY);
        String pkgName = params.getString(PermConst.PACKAGE_NAME);
        if (TextUtils.isEmpty(pkgName)) {
            SlogEx.e(TAG, "setHwPermissionInner pkgName empty");
            return;
        }
        this.mHwPermDbAdapter.setHwPermission(pkgName, userId, permType, params.getInt(PermConst.OPERATION_KEY), Binder.getCallingUid());
    }

    private void setHwPermissionsInner(int userId, @NonNull Bundle params) {
        long permType = params.getLong(PermConst.PERM_TYPE_KEY);
        HashMap<String, Integer> appMap = (HashMap) params.getSerializable(PermConst.APP_MAP_KEY);
        if (appMap == null || appMap.isEmpty()) {
            SlogEx.e(TAG, "setHwPermissionsInner appMap empty");
        } else {
            this.mHwPermDbAdapter.setHwPermission(appMap, userId, permType);
        }
    }

    private void replacePermForAllAppsInner(int userId, @NonNull Bundle params) {
        ArrayList<DbPermissionItem> appList = null;
        try {
            appList = params.getParcelableArrayList(PermConst.APP_LIST_KEY);
        } catch (ArrayIndexOutOfBoundsException e) {
            SlogEx.e(TAG, "replacePermForAllAppsInner ArrayIndexOutOfBoundsException: " + e.getMessage());
        }
        if (appList != null && !appList.isEmpty()) {
            this.mHwPermDbAdapter.replacePermForAllApps(appList, userId);
        }
    }

    private void replaceHwPermInfoInner(int userId, @NonNull Bundle params) {
        String pkgName = params.getString(PermConst.PACKAGE_NAME);
        if (TextUtils.isEmpty(pkgName)) {
            SlogEx.e(TAG, "replaceHwPermInfoInner pkgName empty");
            return;
        }
        long permCfg = params.getLong(PermConst.PERMISSION_CFG);
        this.mHwPermDbAdapter.replaceHwPermInfo(pkgName, userId, params.getLong(PermConst.PERMISSION_CODE), permCfg, params.getInt(PermConst.APP_UID, -1));
    }

    private Bundle getHwPermInfoForPackageInstaller(String pkgName, Bundle params) {
        if (TextUtils.isEmpty(pkgName) || params == null) {
            SlogEx.e(TAG, "getHwPermInfoForPackageInstaller params is null");
            return new Bundle();
        }
        this.mContext.enforceCallingOrSelfPermission("android.permission.GRANT_RUNTIME_PERMISSIONS", "does not have permission for getting permission information!");
        long identity = Binder.clearCallingIdentity();
        try {
            return this.mPackageInstallerPermHelper.getHwPermInfo(this.mContext, pkgName, params);
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    private Bundle getMmsPermInfoInner() {
        int result = checkHwPermission(Binder.getCallingUid(), Binder.getCallingPid(), PERMISSION_SEND_MMS_OLD);
        Bundle bundle = new Bundle();
        boolean isBlocked = false;
        if (result == 2) {
            isBlocked = true;
        }
        bundle.putBoolean(PermConst.RETURN_RESULT_KEY, isBlocked);
        return bundle;
    }

    public boolean shouldMonitor(int uid) {
        if (!Utils.IS_CHINA) {
            return false;
        }
        return this.mHwPermDbAdapter.shouldMonitor(uid);
    }

    public int checkHwPermission(int uid, int pid, int permissionType) {
        if (!Utils.IS_CHINA) {
            return 1;
        }
        return this.mHwPermDbAdapter.checkHwPermission(uid, pid, permissionType);
    }

    private void initSmsBlockDataInner(int userId, @NonNull Bundle params) {
        try {
            this.mHwSmsBlockDbAdapter.initSmsBlockData(userId, params.getParcelableArrayList(PermConst.SMS_BLOCK_DATA_KEY));
        } catch (ArrayIndexOutOfBoundsException e) {
            SlogEx.e(TAG, "initSmsBlockDataInner ArrayIndexOutOfBoundsException: " + e.getMessage());
        }
    }

    private void hotaUpdateSmsBlockDataInner(int userId, @NonNull Bundle params) {
        try {
            ArrayList<ContentValues> contentValuesList = params.getParcelableArrayList(PermConst.SMS_BLOCK_DATA_KEY);
            if (contentValuesList != null) {
                this.mHwSmsBlockDbAdapter.hotaUpdateSmsBlockData(userId, contentValuesList);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            SlogEx.e(TAG, "hotaUpdateSmsBlockDataInner ArrayIndexOutOfBoundsException: " + e.getMessage());
        }
    }

    private Bundle checkSmsBlockStateInner(int userId, @NonNull String pkgName) {
        Bundle bundle = new Bundle();
        try {
            bundle.putInt(PermConst.RETURN_RESULT_KEY, this.mHwSmsBlockDbAdapter.checkSmsBlockAuthResult(pkgName, userId));
        } catch (ArrayIndexOutOfBoundsException e) {
            SlogEx.e(TAG, "initSmsBlockDataInner ArrayIndexOutOfBoundsException: " + e.getMessage());
        }
        return bundle;
    }

    private void notifyPkgRemoveInner(@NonNull Bundle params) {
        try {
            String pkgName = params.getString(PermConst.PACKAGE_NAME);
            if (TextUtils.isEmpty(pkgName)) {
                SlogEx.e(TAG, "replaceHwPermInfoInner pkgName empty");
            } else {
                SmsSendBlockDataMgr.getInstance(this.mContext).onNotifyPkgRemove(pkgName);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            SlogEx.e(TAG, "notifyPkgRemoveInner ArrayIndexOutOfBoundsException: " + e.getMessage());
        }
    }

    private void notifyUserResetInner() {
        SmsSendBlockDataMgr.getInstance(this.mContext).onNotifyUserReset();
    }

    private Bundle getPermissionHubProperty() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.GRANT_RUNTIME_PERMISSIONS", "does not have get permission property!");
        long identity = Binder.clearCallingIdentity();
        try {
            boolean isEnabled = DeviceConfigEx.getBoolean("privacy", PermConst.PROPERTY_PERMISSIONS_HUB_ENABLED, false);
            Bundle bundle = new Bundle();
            bundle.putBoolean(PermConst.RETURN_RESULT_KEY, isEnabled);
            return bundle;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    private void setPermissionHubProperty(@NonNull Bundle params) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.GRANT_RUNTIME_PERMISSIONS", "does not have set permission property!");
        long identity = Binder.clearCallingIdentity();
        try {
            boolean isSuccess = DeviceConfigEx.setProperty("privacy", PermConst.PROPERTY_PERMISSIONS_HUB_ENABLED, params.getString(PermConst.PROPERTY_PERMISSIONS_HUB_ENABLED, "false"), false);
            SlogEx.i(TAG, "setProperty: " + isSuccess);
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    private void setOneTimePermissionControlInfo(@NonNull Bundle params) {
        SlogEx.i(TAG, "set control info of one time permission");
        long identity = Binder.clearCallingIdentity();
        try {
            String infoType = params.getString(PermConst.INFO_TYPE_KEY);
            if (!PermConst.ONE_TIME_PERM_TYPE_BLOCK.equals(infoType) && !PermConst.ONE_TIME_PERM_TYPE_SUPPORT.equals(infoType)) {
                SlogEx.w(TAG, "set one time permission info failed for invalid info.");
            } else if (this.mOneTimePermissionInfoManager == null) {
                SlogEx.e(TAG, "one time permission manager not initialized.");
                Binder.restoreCallingIdentity(identity);
            } else {
                this.mOneTimePermissionInfoManager.setOneTimePermissionInfo(params);
                Binder.restoreCallingIdentity(identity);
            }
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    private Bundle getOneTimePermissionControlInfo() {
        SlogEx.i(TAG, "get control info of one time permission");
        long identity = Binder.clearCallingIdentity();
        try {
            if (this.mOneTimePermissionInfoManager == null) {
                SlogEx.e(TAG, "one time permission manager not initialized.");
                return new Bundle();
            }
            Bundle oneTimePermissionInfo = this.mOneTimePermissionInfoManager.getOneTimePermissionInfo();
            Binder.restoreCallingIdentity(identity);
            return oneTimePermissionInfo;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    private Bundle getRecommendPermForInstaller(String pkgName, @NonNull Bundle params) {
        SlogEx.d(TAG, "get recommend permission in F interface");
        return this.mGetRecommendPermCaller.getRecommendPermissions(pkgName, params);
    }

    private void setRecommendPermission(@NonNull Bundle params) {
        SlogEx.d(TAG, "set recommend permission in F interface");
        this.mRecommendPermDbAdapter.setRecommendPermission(params);
    }
}

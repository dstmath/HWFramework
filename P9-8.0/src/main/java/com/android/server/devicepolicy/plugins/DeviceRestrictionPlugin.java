package com.android.server.devicepolicy.plugins;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncAdapterType;
import android.content.pm.ActivityInfo;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.UserInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.text.TextUtils;
import android.util.Log;
import android.view.IWindowManager;
import android.view.IWindowManager.Stub;
import com.android.server.devicepolicy.DevicePolicyPlugin;
import com.android.server.devicepolicy.HwLog;
import com.android.server.devicepolicy.PolicyStruct;
import com.android.server.devicepolicy.PolicyStruct.PolicyItem;
import com.android.server.devicepolicy.PolicyStruct.PolicyType;
import com.android.server.devicepolicy.StorageUtils;
import huawei.android.os.HwProtectAreaManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DeviceRestrictionPlugin extends DevicePolicyPlugin {
    private static final String DISABLE_APPLICATIONS_LIST = "disable-applications-list";
    private static final String DISABLE_APPLICATIONS_LIST_ITEM = "disable-applications-list/disable-applications-list-item";
    private static final String DISABLE_CHANGE_WALLPAPER = "disable-change-wallpaper";
    private static final String DISABLE_HEADPHONE = "disable-headphone";
    private static final String DISABLE_MICROPHONE = "disable-microphone";
    private static final String DISABLE_NAVIGATIONBAR = "disable-navigationbar";
    private static final String DISABLE_NOTIFICATION = "disable-notification";
    private static final String DISABLE_SCREEN_CAPTURE = "disable-screen-capture";
    private static final String DISABLE_SYSTEM_BROWSER = "disable-system-browser";
    private static final String DISABLE_SYSTEM_UPDATE = "disable-system-update";
    private static final String FLOAT_TASK_STATE = "float_task_state";
    private static final int FLOAT_TASK_STATE_OFF = 0;
    private static final int FLOAT_TASK_STATE_ON = 1;
    public static final String PERMISSION_MDM_UPDATESTATE_MANAGER = "com.huawei.permission.sec.MDM_UPDATESTATE_MANAGER";
    private static final String SETTINGS_FALLBACK_ACTIVITY_NAME = "com.android.settings.FallbackHome";
    private static final String SETTINGS_PACKAGE_NAME = "com.android.settings";
    public static final String TAG = DeviceRestrictionPlugin.class.getSimpleName();
    ArrayList<String> mDisableApplicationsList = new ArrayList();
    Handler mHandler = new Handler(Looper.myLooper());

    public DeviceRestrictionPlugin(Context context) {
        super(context);
    }

    public String getPluginName() {
        return getClass().getSimpleName();
    }

    public PolicyStruct getPolicyStruct() {
        HwLog.i(TAG, "getPolicyStruct");
        PolicyStruct struct = new PolicyStruct(this);
        struct.addStruct(DISABLE_SYSTEM_UPDATE, PolicyType.STATE, new String[]{"value"});
        struct.addStruct("disable-system-browser", PolicyType.STATE, new String[]{"value"});
        struct.addStruct("disable-screen-capture", PolicyType.STATE, new String[]{"value"});
        struct.addStruct("disable-notification", PolicyType.STATE, new String[]{"value"});
        struct.addStruct(DISABLE_APPLICATIONS_LIST_ITEM, PolicyType.LIST, new String[]{"value"});
        struct.addStruct("disable-clipboard", PolicyType.STATE, new String[]{"value"});
        struct.addStruct("disable-google-account-autosync", PolicyType.STATE, new String[]{"value"});
        struct.addStruct("disable-microphone", PolicyType.STATE, new String[]{"value"});
        struct.addStruct("disable-headphone", PolicyType.STATE, new String[]{"value"});
        struct.addStruct("disable-send-notification", PolicyType.STATE, new String[]{"value"});
        struct.addStruct(DISABLE_CHANGE_WALLPAPER, PolicyType.STATE, new String[]{"value"});
        struct.addStruct("disable-power-shutdown", PolicyType.STATE, new String[]{"value"});
        struct.addStruct("disable-shutdownmenu", PolicyType.STATE, new String[]{"value"});
        struct.addStruct("disable-volume", PolicyType.STATE, new String[]{"value"});
        struct.addStruct("disable-fingerprint-authentication", PolicyType.STATE, new String[]{"value"});
        struct.addStruct("force-enable-wifi", PolicyType.STATE, new String[]{"value"});
        struct.addStruct("force-enable-BT", PolicyType.STATE, new String[]{"value"});
        struct.addStruct("disable_voice_outgoing", PolicyType.STATE, new String[]{"value"});
        struct.addStruct("disable_voice_incoming", PolicyType.STATE, new String[]{"value"});
        struct.addStruct("disable-navigationbar", PolicyType.STATE, new String[]{"value"});
        struct.addStruct("disable_float_task", PolicyType.STATE, new String[]{"value"});
        return struct;
    }

    public boolean checkCallingPermission(ComponentName who, String policyName) {
        HwLog.i(TAG, "checkCallingPermission");
        if (policyName.equals(DISABLE_SYSTEM_UPDATE)) {
            this.mContext.enforceCallingOrSelfPermission(PERMISSION_MDM_UPDATESTATE_MANAGER, "does not have system_update_management MDM permission!");
        } else if (policyName.equals("disable-screen-capture")) {
            this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_CAPTURE_SCREEN", "does not have capture_screen_management MDM permission!");
        } else if (policyName.equals("disable-system-browser") || policyName.equals("disable-applications-list")) {
            this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_APP_MANAGEMENT", "does not have app_management MDM permission!");
        } else if (policyName.equals("disable-clipboard")) {
            this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_CLIPBOARD", "does not have clipboard MDM permission!");
        } else if (policyName.equals("disable-google-account-autosync")) {
            this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_GOOGLE_ACCOUNT", "does not have google account MDM permission!");
        } else if (policyName.equals("disable-headphone") || policyName.equals("disable-microphone") || policyName.equals("disable-send-notification") || policyName.equals(DISABLE_CHANGE_WALLPAPER)) {
            HwLog.i(TAG, "check the calling Permission");
            this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_DEVICE_MANAGER", "does not have device_manager MDM permission!");
            return true;
        } else if (policyName.equals("disable-power-shutdown")) {
            this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_DEVICE_MANAGER", "does not have device_manager MDM permission!");
        } else if (policyName.equals("disable-shutdownmenu")) {
            this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_DEVICE_MANAGER", "does not have device_manager MDM permission!");
        } else if (policyName.equals("disable-volume")) {
            this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_DEVICE_MANAGER", "does not have device_manager MDM permission!");
        } else if (policyName.equals("disable-fingerprint-authentication")) {
            this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_FINGERPRINT", "does not have fingerprint MDM permission!");
        } else if (policyName.equals("disable_voice_outgoing") || policyName.equals("disable_voice_incoming")) {
            this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_PHONE", "does not have google account MDM permission!");
        } else if (policyName.equals("disable-notification")) {
            this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_DEVICE_MANAGER", "does not have device_manager MDM permission!");
        } else if (policyName.equals("disable-navigationbar")) {
            this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_DEVICE_MANAGER", "does not have device_manager MDM permission!");
        } else if (policyName.equals("force-enable-wifi")) {
            this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_WIFI", "does not have wifi MDM permission!");
        } else if (policyName.equals("force-enable-BT")) {
            this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_BLUETOOTH", "does not have bluetooth MDM permission!");
        } else if (policyName.equals("disable_float_task")) {
            this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_DEVICE_MANAGER", "does not have device_manager MDM permission!");
        }
        return true;
    }

    public boolean onSetPolicy(ComponentName who, String policyName, Bundle policyData, boolean effective) {
        HwLog.i(TAG, "onSetPolicy");
        boolean isSetSucess = true;
        PackageManager pm = this.mContext.getPackageManager();
        long token = Binder.clearCallingIdentity();
        try {
            if (!policyName.equals(DISABLE_SYSTEM_UPDATE)) {
                if (policyName.equals("disable-applications-list")) {
                    ArrayList<String> list = policyData.getStringArrayList("value");
                    if (!(list == null || list.size() == 0)) {
                        int j = list.size();
                        for (int i = 0; i < j; i++) {
                            String app = (String) list.get(i);
                            isSetSucess = disableComponentForPackage(app, true, pm, 0);
                            this.mDisableApplicationsList.add(app);
                        }
                    }
                } else {
                    if (policyName.equals("disable-screen-capture")) {
                        int userHandle = UserHandle.getCallingUserId();
                        synchronized (this) {
                            isSetSucess = updateScreenCaptureDisabledInWindowManager(userHandle, policyData.getBoolean("value"));
                        }
                    } else {
                        if (!policyName.equals("disable-clipboard")) {
                            if (policyName.equals("disable-google-account-autosync")) {
                                boolean isDisable = policyData.getBoolean("value", false);
                                if (effective && isDisable) {
                                    new Thread(new Runnable() {
                                        public void run() {
                                            DeviceRestrictionPlugin.this.disableGoogleAccountSyncAutomatically();
                                        }
                                    }).start();
                                }
                            } else {
                                if (policyName.equals("force-enable-wifi")) {
                                    openWifi();
                                } else {
                                    if (policyName.equals("force-enable-BT")) {
                                        openBt();
                                    } else {
                                        if (policyName.equals("disable-navigationbar")) {
                                            isSetSucess = changeNavigationBarStatus(UserHandle.getCallingUserId(), policyData.getBoolean("value"));
                                        } else {
                                            if (policyName.equals("disable_float_task")) {
                                                boolean enable = isFloatTaskRunning();
                                                storeFloatTaskState(enable);
                                                if (enable) {
                                                    setFloatTaskEnabled(false);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (effective) {
                isSetSucess = disableSystemUpdate(policyData.getBoolean("value"));
            }
            Binder.restoreCallingIdentity(token);
            return isSetSucess;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(token);
        }
    }

    private boolean changeNavigationBarStatus(int navBarUserHandle, boolean nav_policyData) {
        String enableNavbar;
        String NAVIGATIONBAR_ENABLE = "1";
        String NAVIGATIONBAR_DISABLE = "0";
        if (nav_policyData) {
            enableNavbar = "0";
        } else {
            enableNavbar = "1";
        }
        List<UserInfo> userInfo = UserManager.get(this.mContext).getUsers();
        boolean changeResult = true;
        int usersSize = userInfo.size();
        for (int i = 0; i < usersSize; i++) {
            int userid = ((UserInfo) userInfo.get(i)).id;
            changeResult = changeResult ? System.putStringForUser(this.mContext.getContentResolver(), "enable_navbar", enableNavbar, userid) : false;
            HwLog.i(TAG, "change navigation bar database ,user is:" + userid + " ,changeResult: " + changeResult + ",policyData is: " + nav_policyData);
        }
        return changeResult;
    }

    private void openWifi() {
        WifiManager wifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        if (wifiManager != null) {
            wifiManager.setWifiEnabled(true);
        }
    }

    private void openBt() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null) {
            bluetoothAdapter.enable();
        }
    }

    public boolean onRemovePolicy(ComponentName who, String policyName, Bundle policyData, boolean effective) {
        HwLog.i(TAG, "onRemovePolicy");
        PackageManager pm = this.mContext.getPackageManager();
        long token = Binder.clearCallingIdentity();
        try {
            if (policyName.equals("disable-applications-list")) {
                ArrayList<String> list = policyData.getStringArrayList("value");
                if (!(list == null || list.size() == 0)) {
                    int j = list.size();
                    for (int i = 0; i < j; i++) {
                        disableComponentForPackage((String) list.get(i), false, pm, 0);
                    }
                }
            } else if (policyName.equals("disable_float_task") && isFloatTaskEnableBefore()) {
                setFloatTaskEnabled(true);
            }
            Binder.restoreCallingIdentity(token);
            return true;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(token);
        }
    }

    public boolean onActiveAdminRemoved(ComponentName who, ArrayList<PolicyItem> removedPolicies) {
        HwLog.i(TAG, "onActiveAdminRemoved");
        PackageManager pm = this.mContext.getPackageManager();
        long token = Binder.clearCallingIdentity();
        try {
            for (PolicyItem policyItem : removedPolicies) {
                String policyName = policyItem.getPolicyName();
                if (policyName.equals(DISABLE_SYSTEM_UPDATE)) {
                    disableSystemUpdate(false);
                } else if (policyName.equals("disable-screen-capture")) {
                    int userHandle = UserHandle.getCallingUserId();
                    synchronized (this) {
                        updateScreenCaptureDisabledInWindowManager(userHandle, false);
                    }
                } else if (policyName.equals("disable-applications-list")) {
                    if (!(this.mDisableApplicationsList == null || this.mDisableApplicationsList.size() == 0)) {
                        int j = this.mDisableApplicationsList.size();
                        for (int i = 0; i < j; i++) {
                            disableComponentForPackage((String) this.mDisableApplicationsList.get(i), false, pm, 0);
                        }
                    }
                } else if (policyName.equals("disable_float_task") && isFloatTaskEnableBefore()) {
                    setFloatTaskEnabled(true);
                }
            }
            Binder.restoreCallingIdentity(token);
            return true;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(token);
        }
    }

    private boolean disableSystemUpdate(boolean disable) {
        String[] readBuf = new String[]{"AA"};
        int[] errorRet = new int[1];
        String writeValue = disable ? "1" : "0";
        HwLog.i(TAG, "writeProtectArea :" + writeValue);
        if (HwProtectAreaManager.getInstance().writeProtectArea("SYSTEM_UPDATE_STATE", writeValue.length(), writeValue, errorRet) == 0) {
            int readRet = HwProtectAreaManager.getInstance().readProtectArea("SYSTEM_UPDATE_STATE", 4, readBuf, errorRet);
            HwLog.i(TAG, "readProtectArea: readRet = " + readRet + "ReadBuf = " + Arrays.toString(readBuf) + "errorRet = " + Arrays.toString(errorRet));
            if (readRet == 0 && readBuf.length >= 1 && writeValue.equals(readBuf[0])) {
                String updateState = disable ? StorageUtils.SDCARD_RWMOUNTED_STATE : StorageUtils.SDCARD_ROMOUNTED_STATE;
                Log.i(TAG, "writeProtectArea Success! Set sys.update.state to " + updateState);
                SystemProperties.set("sys.update.state", updateState);
                return true;
            }
        }
        return false;
    }

    private boolean disableComponentForPackage(String packageName, boolean disable, PackageManager pm, int userId) {
        boolean setSuccess = true;
        if (TextUtils.isEmpty(packageName)) {
            return false;
        }
        int newState = disable ? 2 : 0;
        LauncherApps launcherApps = (LauncherApps) this.mContext.getSystemService("launcherapps");
        try {
            int i;
            PackageInfo packageInfo = pm.getPackageInfo(packageName, 786959);
            if (!(packageInfo == null || packageInfo.receivers == null || packageInfo.receivers.length == 0)) {
                for (ActivityInfo activityInfo : packageInfo.receivers) {
                    pm.setComponentEnabledSetting(new ComponentName(packageName, activityInfo.name), newState, 0);
                }
            }
            if (!(packageInfo == null || packageInfo.services == null || packageInfo.services.length == 0)) {
                for (ServiceInfo serviceInfo : packageInfo.services) {
                    pm.setComponentEnabledSetting(new ComponentName(packageName, serviceInfo.name), newState, 0);
                }
            }
            if (!(packageInfo == null || packageInfo.providers == null || packageInfo.providers.length == 0)) {
                for (ProviderInfo providerInfo : packageInfo.providers) {
                    pm.setComponentEnabledSetting(new ComponentName(packageName, providerInfo.name), newState, 0);
                }
            }
            if (!(packageInfo == null || packageInfo.activities == null || packageInfo.activities.length == 0)) {
                i = 0;
                while (i < packageInfo.activities.length) {
                    ComponentName componentName = new ComponentName(packageName, packageInfo.activities[i].name);
                    if (disable) {
                        for (LauncherActivityInfo launcherApp : launcherApps.getActivityList(packageName, new UserHandle(UserHandle.myUserId()))) {
                            if (!(launcherApp.getComponentName().getClassName().contains(packageInfo.activities[i].name) || (packageInfo.activities[i].name.contains(SETTINGS_FALLBACK_ACTIVITY_NAME) ^ 1) == 0)) {
                                pm.setComponentEnabledSetting(componentName, newState, 0);
                            }
                        }
                    } else {
                        pm.setComponentEnabledSetting(componentName, newState, 0);
                    }
                    i++;
                }
            }
            if (disable) {
                pm.clearPackagePreferredActivities(packageName);
            }
        } catch (NameNotFoundException e) {
            setSuccess = false;
        }
        pm.flushPackageRestrictionsAsUser(userId);
        if (disable) {
            killApplicationInner(packageName);
        }
        return setSuccess;
    }

    private void killApplicationInner(String packageName) {
        long ident = Binder.clearCallingIdentity();
        try {
            ActivityManager am = (ActivityManager) this.mContext.getSystemService("activity");
            List<RunningTaskInfo> taskList = am.getRunningTasks(ActivityManager.getMaxRecentTasksStatic());
            if (!(taskList == null || (taskList.isEmpty() ^ 1) == 0)) {
                for (RunningTaskInfo ti : taskList) {
                    ComponentName baseActivity = ti.baseActivity;
                    if (baseActivity != null && TextUtils.equals(baseActivity.getPackageName(), packageName)) {
                        HwLog.d(TAG, "The killed packageName: " + packageName + " task id: " + ti.id);
                        am.removeTask(ti.id);
                    }
                }
            }
            Binder.restoreCallingIdentity(ident);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(ident);
        }
    }

    IWindowManager getIWindowManager() {
        return Stub.asInterface(ServiceManager.getService("window"));
    }

    private boolean updateScreenCaptureDisabledInWindowManager(int userHandle, boolean disabled) {
        try {
            getIWindowManager().setScreenCaptureDisabled(userHandle, disabled);
            return true;
        } catch (RemoteException e) {
            HwLog.e(TAG, "Unable to notify WindowManager.");
            return false;
        }
    }

    private void disableGoogleAccountSyncAutomatically() {
        String GOOGLE_ACCOUNT_TYPE = "com.google";
        long identityToken = Binder.clearCallingIdentity();
        try {
            SyncAdapterType[] syncs = ContentResolver.getSyncAdapterTypes();
            Account[] accounts = AccountManager.get(this.mContext).getAccountsByType("com.google");
            HwLog.i(TAG, "get google accounts, size=" + accounts.length);
            for (Account account : accounts) {
                for (SyncAdapterType adapter : syncs) {
                    HwLog.i(TAG, "getCurrentSyncs, type=" + adapter.accountType + ", authority=" + adapter.authority + ", visible=" + adapter.isUserVisible());
                    if ("com.google".equals(adapter.accountType) && adapter.isUserVisible() && ContentResolver.getSyncAutomatically(account, adapter.authority)) {
                        HwLog.i(TAG, "setSyncAutomatically to false for google account authority: " + adapter.authority);
                        ContentResolver.setSyncAutomatically(account, adapter.authority, false);
                    }
                }
            }
        } finally {
            Binder.restoreCallingIdentity(identityToken);
        }
    }

    private void setFloatTaskEnabled(boolean enable) {
        Secure.putIntForUser(this.mContext.getContentResolver(), FLOAT_TASK_STATE, enable ? 1 : 0, UserHandle.myUserId());
    }

    private boolean isFloatTaskRunning() {
        return Secure.getIntForUser(this.mContext.getContentResolver(), FLOAT_TASK_STATE, 0, UserHandle.myUserId()) == 1;
    }

    private boolean isFloatTaskEnableBefore() {
        return System.getInt(this.mContext.getContentResolver(), "float_task_state_before", 0) == 1;
    }

    private void storeFloatTaskState(boolean enable) {
        System.putInt(this.mContext.getContentResolver(), "float_task_state_before", enable ? 1 : 0);
    }
}

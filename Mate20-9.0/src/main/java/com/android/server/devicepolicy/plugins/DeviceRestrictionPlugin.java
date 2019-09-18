package com.android.server.devicepolicy.plugins;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SyncAdapterType;
import android.content.pm.ActivityInfo;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.UserInfo;
import android.hdm.HwDeviceManager;
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
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.IWindowManager;
import com.android.server.devicepolicy.DevicePolicyPlugin;
import com.android.server.devicepolicy.HwLog;
import com.android.server.devicepolicy.PolicyStruct;
import com.android.server.rms.iaware.feature.DevSchedFeatureRT;
import com.huawei.android.os.HwPowerManager;
import huawei.android.os.HwProtectAreaManager;
import huawei.com.android.server.fingerprint.FingerViewController;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
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
    private static final String KEY_BOOT_MDM = "boot_alarm_mdm";
    public static final String PERMISSION_MDM_UPDATESTATE_MANAGER = "com.huawei.permission.sec.MDM_UPDATESTATE_MANAGER";
    private static final String SETTINGS_FALLBACK_ACTIVITY_NAME = "com.android.settings.FallbackHome";
    private static final String SETTINGS_PACKAGE_NAME = "com.android.settings";
    private static final String SETTING_CRYPTKEEPER_ACTIVITY_NAME = "com.android.settings.CryptKeeper";
    public static final String TAG = DeviceRestrictionPlugin.class.getSimpleName();
    private AlarmManager mAlarmManager;
    private PendingIntent mBootPendingIntent;
    ArrayList<String> mDisableApplicationsList = new ArrayList<>();
    Handler mHandler = new Handler(Looper.myLooper());
    private PendingIntent mShutdownPendingIntent;

    public DeviceRestrictionPlugin(Context context) {
        super(context);
    }

    public String getPluginName() {
        return getClass().getSimpleName();
    }

    public PolicyStruct getPolicyStruct() {
        HwLog.i(TAG, "getPolicyStruct");
        PolicyStruct struct = new PolicyStruct(this);
        struct.addStruct(DISABLE_SYSTEM_UPDATE, PolicyStruct.PolicyType.STATE, new String[]{"value"});
        struct.addStruct("disable-system-browser", PolicyStruct.PolicyType.STATE, new String[]{"value"});
        struct.addStruct("disable-screen-capture", PolicyStruct.PolicyType.STATE, new String[]{"value"});
        struct.addStruct("disable-notification", PolicyStruct.PolicyType.STATE, new String[]{"value"});
        struct.addStruct(DISABLE_APPLICATIONS_LIST_ITEM, PolicyStruct.PolicyType.LIST, new String[]{"value"});
        struct.addStruct("disable-clipboard", PolicyStruct.PolicyType.STATE, new String[]{"value"});
        struct.addStruct("disable-google-account-autosync", PolicyStruct.PolicyType.STATE, new String[]{"value"});
        struct.addStruct("disable-microphone", PolicyStruct.PolicyType.STATE, new String[]{"value"});
        struct.addStruct("disable-headphone", PolicyStruct.PolicyType.STATE, new String[]{"value"});
        struct.addStruct("disable-send-notification", PolicyStruct.PolicyType.STATE, new String[]{"value"});
        struct.addStruct(DISABLE_CHANGE_WALLPAPER, PolicyStruct.PolicyType.STATE, new String[]{"value"});
        struct.addStruct("disable-power-shutdown", PolicyStruct.PolicyType.STATE, new String[]{"value"});
        struct.addStruct("disable-shutdownmenu", PolicyStruct.PolicyType.STATE, new String[]{"value"});
        struct.addStruct("disable-volume", PolicyStruct.PolicyType.STATE, new String[]{"value"});
        struct.addStruct("disable-fingerprint-authentication", PolicyStruct.PolicyType.STATE, new String[]{"value"});
        struct.addStruct("force-enable-wifi", PolicyStruct.PolicyType.STATE, new String[]{"value"});
        struct.addStruct("force-enable-BT", PolicyStruct.PolicyType.STATE, new String[]{"value"});
        struct.addStruct("disable_voice_outgoing", PolicyStruct.PolicyType.STATE, new String[]{"value"});
        struct.addStruct("disable_voice_incoming", PolicyStruct.PolicyType.STATE, new String[]{"value"});
        struct.addStruct("disable-navigationbar", PolicyStruct.PolicyType.STATE, new String[]{"value"});
        struct.addStruct("disable-charge", PolicyStruct.PolicyType.STATE, new String[]{"value"});
        struct.addStruct("force_scheduled_power_on", PolicyStruct.PolicyType.STATE, new String[]{"value"});
        struct.addStruct("force_scheduled_power_off", PolicyStruct.PolicyType.STATE, new String[]{"value"});
        struct.addStruct("disable_float_task", PolicyStruct.PolicyType.STATE, new String[]{"value"});
        struct.addStruct("policy-file-share-disabled", PolicyStruct.PolicyType.STATE, new String[]{"value"});
        struct.addStruct("force_scheduled_charge_limit", PolicyStruct.PolicyType.STATE, new String[]{"value"});
        struct.addStruct("disable_alarm", PolicyStruct.PolicyType.STATE, new String[]{"value"});
        return struct;
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    public boolean checkCallingPermission(ComponentName who, String policyName) {
        char c;
        HwLog.i(TAG, "checkCallingPermission");
        switch (policyName.hashCode()) {
            case -2065188737:
                if (policyName.equals("force_scheduled_power_on")) {
                    c = 11;
                    break;
                }
            case -1616043882:
                if (policyName.equals("force_scheduled_charge_limit")) {
                    c = 26;
                    break;
                }
            case -1462770845:
                if (policyName.equals("disable-applications-list")) {
                    c = 3;
                    break;
                }
            case -1418767509:
                if (policyName.equals("disable-send-notification")) {
                    c = 8;
                    break;
                }
            case -1333051633:
                if (policyName.equals("disable-system-browser")) {
                    c = 2;
                    break;
                }
            case -694001423:
                if (policyName.equals("disable-clipboard")) {
                    c = 4;
                    break;
                }
            case -595558097:
                if (policyName.equals("disable-microphone")) {
                    c = 7;
                    break;
                }
            case -515362246:
                if (policyName.equals("disable_alarm")) {
                    c = 13;
                    break;
                }
            case -304109734:
                if (policyName.equals("disable-navigationbar")) {
                    c = 23;
                    break;
                }
            case -168307399:
                if (policyName.equals("disable-charge")) {
                    c = 10;
                    break;
                }
            case -31729136:
                if (policyName.equals("disable_voice_outgoing")) {
                    c = 20;
                    break;
                }
            case -614710:
                if (policyName.equals("disable_voice_incoming")) {
                    c = 21;
                    break;
                }
            case 153563136:
                if (policyName.equals("policy-file-share-disabled")) {
                    c = 25;
                    break;
                }
            case 382441887:
                if (policyName.equals("disable-volume")) {
                    c = 16;
                    break;
                }
            case 403658447:
                if (policyName.equals("force_scheduled_power_off")) {
                    c = 12;
                    break;
                }
            case 458488698:
                if (policyName.equals("disable-shutdownmenu")) {
                    c = 15;
                    break;
                }
            case 476421226:
                if (policyName.equals("disable-screen-capture")) {
                    c = 1;
                    break;
                }
            case 539407267:
                if (policyName.equals("disable-power-shutdown")) {
                    c = 14;
                    break;
                }
            case 594183088:
                if (policyName.equals("disable-notification")) {
                    c = 22;
                    break;
                }
            case 702979817:
                if (policyName.equals("disable-headphone")) {
                    c = 6;
                    break;
                }
            case 731920490:
                if (policyName.equals(DISABLE_CHANGE_WALLPAPER)) {
                    c = 9;
                    break;
                }
            case 775851010:
                if (policyName.equals(DISABLE_SYSTEM_UPDATE)) {
                    c = 0;
                    break;
                }
            case 1389850009:
                if (policyName.equals("disable-google-account-autosync")) {
                    c = 5;
                    break;
                }
            case 1658369855:
                if (policyName.equals("disable_float_task")) {
                    c = 24;
                    break;
                }
            case 1785346365:
                if (policyName.equals("force-enable-wifi")) {
                    c = 18;
                    break;
                }
            case 1946452102:
                if (policyName.equals("disable-fingerprint-authentication")) {
                    c = 17;
                    break;
                }
            case 1981742202:
                if (policyName.equals("force-enable-BT")) {
                    c = 19;
                    break;
                }
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
                this.mContext.enforceCallingOrSelfPermission(PERMISSION_MDM_UPDATESTATE_MANAGER, "does not have system_update_management MDM permission!");
                break;
            case 1:
                this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_CAPTURE_SCREEN", "does not have capture_screen_management MDM permission!");
                break;
            case 2:
            case 3:
                this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_APP_MANAGEMENT", "does not have app_management MDM permission!");
                break;
            case 4:
                this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_CLIPBOARD", "does not have clipboard MDM permission!");
                break;
            case 5:
                this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_GOOGLE_ACCOUNT", "does not have google account MDM permission!");
                break;
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
                HwLog.i(TAG, "check the calling Permission");
                this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_DEVICE_MANAGER", "does not have device_manager MDM permission!");
                return true;
            case 14:
                this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_DEVICE_MANAGER", "does not have device_manager MDM permission!");
                break;
            case 15:
                this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_DEVICE_MANAGER", "does not have device_manager MDM permission!");
                break;
            case 16:
                this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_DEVICE_MANAGER", "does not have device_manager MDM permission!");
                break;
            case 17:
                this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_FINGERPRINT", "does not have fingerprint MDM permission!");
                break;
            case 18:
                this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_WIFI", "does not have wifi MDM permission!");
                break;
            case 19:
                this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_BLUETOOTH", "does not have bluetooth MDM permission!");
                break;
            case 20:
            case 21:
                this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_PHONE", "does not have google account MDM permission!");
                break;
            case 22:
                this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_DEVICE_MANAGER", "does not have device_manager MDM permission!");
                break;
            case 23:
                this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_DEVICE_MANAGER", "does not have device_manager MDM permission!");
                break;
            case 24:
                this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_DEVICE_MANAGER", "does not have device_manager MDM permission!");
                break;
            case 25:
                this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_DEVICE_MANAGER", "does not have device_manager MDM permission!");
                break;
            case 26:
                this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_DEVICE_MANAGER", "does not have device_manager MDM permission!");
                break;
        }
        return true;
    }

    public boolean onSetPolicy(ComponentName who, String policyName, Bundle policyData, boolean effective) {
        String str = policyName;
        Bundle bundle = policyData;
        HwLog.i(TAG, "onSetPolicy");
        boolean isSetSucess = true;
        PackageManager pm = this.mContext.getPackageManager();
        long token = Binder.clearCallingIdentity();
        char c = 65535;
        try {
            switch (policyName.hashCode()) {
                case -2065188737:
                    if (str.equals("force_scheduled_power_on")) {
                        c = 8;
                        break;
                    }
                    break;
                case -1616043882:
                    if (str.equals("force_scheduled_charge_limit")) {
                        c = 11;
                        break;
                    }
                    break;
                case -1462770845:
                    if (str.equals("disable-applications-list")) {
                        c = 1;
                        break;
                    }
                    break;
                case -694001423:
                    if (str.equals("disable-clipboard")) {
                        c = 3;
                        break;
                    }
                    break;
                case -168307399:
                    if (str.equals("disable-charge")) {
                        c = 7;
                        break;
                    }
                    break;
                case 403658447:
                    if (str.equals("force_scheduled_power_off")) {
                        c = 9;
                        break;
                    }
                    break;
                case 476421226:
                    if (str.equals("disable-screen-capture")) {
                        c = 2;
                        break;
                    }
                    break;
                case 775851010:
                    if (str.equals(DISABLE_SYSTEM_UPDATE)) {
                        c = 0;
                        break;
                    }
                    break;
                case 1389850009:
                    if (str.equals("disable-google-account-autosync")) {
                        c = 4;
                        break;
                    }
                    break;
                case 1658369855:
                    if (str.equals("disable_float_task")) {
                        c = 10;
                        break;
                    }
                    break;
                case 1785346365:
                    if (str.equals("force-enable-wifi")) {
                        c = 5;
                        break;
                    }
                    break;
                case 1981742202:
                    if (str.equals("force-enable-BT")) {
                        c = 6;
                        break;
                    }
                    break;
            }
            switch (c) {
                case 0:
                    if (effective) {
                        isSetSucess = disableSystemUpdate(bundle.getBoolean("value"));
                        break;
                    }
                    break;
                case 1:
                    ArrayList<String> list = bundle.getStringArrayList("value");
                    if (!(list == null || list.size() == 0)) {
                        int j = list.size();
                        for (int i = 0; i < j; i++) {
                            String app = list.get(i);
                            isSetSucess = disableComponentForPackage(app, true, pm, 0);
                            this.mDisableApplicationsList.add(app);
                        }
                        break;
                    }
                case 2:
                    int userHandle = UserHandle.getCallingUserId();
                    synchronized (this) {
                        isSetSucess = updateScreenCaptureDisabledInWindowManager(userHandle, bundle.getBoolean("value"));
                    }
                    break;
                case 3:
                    break;
                case 4:
                    boolean isDisable = bundle.getBoolean("value", false);
                    if (effective && isDisable) {
                        new Thread(new Runnable() {
                            public void run() {
                                DeviceRestrictionPlugin.this.disableGoogleAccountSyncAutomatically();
                            }
                        }).start();
                        break;
                    }
                case 5:
                    openWifi();
                    break;
                case 6:
                    openBt();
                    break;
                case 7:
                    if (!bundle.getBoolean("value", false)) {
                        HwPowerManager.setPowerState(true);
                        break;
                    } else {
                        HwPowerManager.setPowerState(false);
                        break;
                    }
                case 8:
                    if (!bundle.getBoolean("value", false)) {
                        HwLog.i(TAG, " ORCE_SCHEDULED_POWER_ON cancel");
                        cancelAlarm(this.mBootPendingIntent);
                        break;
                    } else {
                        long whenBoot = bundle.getLong("when", 0);
                        String boradcastBoot = bundle.getString("boradcast", "");
                        HwLog.i(TAG, " FORCE_SCHEDULED_POWER_ON boradcast:" + boradcastBoot + ",when:" + whenBoot);
                        Intent intent = new Intent(boradcastBoot);
                        intent.putExtra(KEY_BOOT_MDM, true);
                        this.mBootPendingIntent = PendingIntent.getBroadcast(this.mContext, 0, intent, 0);
                        setAlarm(whenBoot, this.mBootPendingIntent);
                        break;
                    }
                case 9:
                    if (!bundle.getBoolean("value", false)) {
                        HwLog.i(TAG, " FORCE_SCHEDULED_POWER_OFF cancel");
                        cancelAlarm(this.mShutdownPendingIntent);
                        break;
                    } else {
                        long whenShutdown = bundle.getLong("when", 0);
                        String boradcastShutdown = bundle.getString("boradcast", "");
                        HwLog.i(TAG, " FORCE_SCHEDULED_POWER_OFF boradcast:" + boradcastShutdown + ",when:" + whenShutdown);
                        this.mShutdownPendingIntent = PendingIntent.getBroadcast(this.mContext, 0, new Intent(boradcastShutdown), 0);
                        setAlarm(whenShutdown, this.mShutdownPendingIntent);
                        break;
                    }
                case 10:
                    boolean enable = isFloatTaskRunning();
                    storeFloatTaskState(enable);
                    if (enable) {
                        setFloatTaskEnabled(false);
                        break;
                    }
                    break;
                case 11:
                    HwPowerManager.setChargeLimit(bundle.getString("charge_limit", "0"));
                    break;
            }
            Binder.restoreCallingIdentity(token);
            return isSetSucess;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(token);
            throw th;
        }
    }

    public void onSetPolicyCompleted(ComponentName who, String policyName, boolean changed) {
        HwLog.i(TAG, "onSetPolicyCompleted");
        long token = Binder.clearCallingIdentity();
        char c = 65535;
        try {
            if (policyName.hashCode() == -304109734) {
                if (policyName.equals("disable-navigationbar")) {
                    c = 0;
                }
            }
            if (c == 0) {
                changeNavigationBarStatus(UserHandle.getCallingUserId(), HwDeviceManager.disallowOp(103));
            }
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    private void setAlarm(long triggerAtMillis, PendingIntent intent) {
        if (this.mAlarmManager == null) {
            this.mAlarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        }
        this.mAlarmManager.setExact(0, triggerAtMillis, intent);
    }

    private void cancelAlarm(PendingIntent intent) {
        if (this.mAlarmManager != null && intent != null) {
            this.mAlarmManager.cancel(intent);
        }
    }

    private void changeNavigationBarStatus(int navBarUserHandle, boolean nav_policyData) {
        String enableNavbar;
        String str = TAG;
        HwLog.i(str, "MDM policy changeNavigationBarStatus nav_policyData = " + nav_policyData);
        if (nav_policyData) {
            enableNavbar = "0";
        } else {
            enableNavbar = "1";
        }
        List<UserInfo> userInfo = UserManager.get(this.mContext).getUsers();
        Intent intent = new Intent("com.huawei.navigationbar.statuschange");
        intent.setPackage(FingerViewController.PKGNAME_OF_KEYGUARD);
        intent.putExtra("minNavigationBar", nav_policyData);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        int usersSize = userInfo.size();
        for (int i = 0; i < usersSize; i++) {
            Settings.System.putStringForUser(this.mContext.getContentResolver(), "enable_navbar", enableNavbar, userInfo.get(i).id);
        }
    }

    private void openWifi() {
        WifiManager wifiManager = (WifiManager) this.mContext.getSystemService(DevSchedFeatureRT.WIFI_FEATURE);
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
        char c = 65535;
        try {
            int hashCode = policyName.hashCode();
            if (hashCode != -1462770845) {
                if (hashCode == 1658369855) {
                    if (policyName.equals("disable_float_task")) {
                        c = 1;
                    }
                }
            } else if (policyName.equals("disable-applications-list")) {
                c = 0;
            }
            switch (c) {
                case 0:
                    ArrayList<String> list = policyData.getStringArrayList("value");
                    if (!(list == null || list.size() == 0)) {
                        int j = list.size();
                        for (int i = 0; i < j; i++) {
                            disableComponentForPackage(list.get(i), false, pm, 0);
                        }
                        break;
                    }
                case 1:
                    if (isFloatTaskEnableBefore()) {
                        setFloatTaskEnabled(true);
                        break;
                    }
                    break;
            }
            return true;
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:28:0x006e A[Catch:{ all -> 0x00b4 }] */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x006f A[Catch:{ all -> 0x00b4 }] */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x0079 A[Catch:{ all -> 0x00b4 }] */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x009c A[Catch:{ all -> 0x00b4 }] */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x00a9 A[Catch:{ all -> 0x00b4 }] */
    public boolean onActiveAdminRemoved(ComponentName who, ArrayList<PolicyStruct.PolicyItem> removedPolicies) {
        char c;
        HwLog.i(TAG, "onActiveAdminRemoved");
        PackageManager pm = this.mContext.getPackageManager();
        long token = Binder.clearCallingIdentity();
        try {
            Iterator<PolicyStruct.PolicyItem> it = removedPolicies.iterator();
            while (it.hasNext()) {
                String policyName = it.next().getPolicyName();
                int hashCode = policyName.hashCode();
                if (hashCode == -1462770845) {
                    if (policyName.equals("disable-applications-list")) {
                        c = 2;
                        switch (c) {
                            case 0:
                                break;
                            case 1:
                                break;
                            case 2:
                                break;
                            case 3:
                                break;
                        }
                    }
                } else if (hashCode == 476421226) {
                    if (policyName.equals("disable-screen-capture")) {
                        c = 1;
                        switch (c) {
                            case 0:
                                break;
                            case 1:
                                break;
                            case 2:
                                break;
                            case 3:
                                break;
                        }
                    }
                } else if (hashCode == 775851010) {
                    if (policyName.equals(DISABLE_SYSTEM_UPDATE)) {
                        c = 0;
                        switch (c) {
                            case 0:
                                break;
                            case 1:
                                break;
                            case 2:
                                break;
                            case 3:
                                break;
                        }
                    }
                } else if (hashCode == 1658369855) {
                    if (policyName.equals("disable_float_task")) {
                        c = 3;
                        switch (c) {
                            case 0:
                                disableSystemUpdate(false);
                                break;
                            case 1:
                                int i = UserHandle.getCallingUserId();
                                synchronized (this) {
                                    updateScreenCaptureDisabledInWindowManager(i, false);
                                }
                                break;
                            case 2:
                                if (!(this.mDisableApplicationsList == null || this.mDisableApplicationsList.size() == 0)) {
                                    int j = this.mDisableApplicationsList.size();
                                    for (int i2 = 0; i2 < j; i2++) {
                                        disableComponentForPackage(this.mDisableApplicationsList.get(i2), false, pm, 0);
                                    }
                                    break;
                                }
                            case 3:
                                if (!isFloatTaskEnableBefore()) {
                                    break;
                                } else {
                                    setFloatTaskEnabled(true);
                                    break;
                                }
                        }
                    }
                }
                c = 65535;
                switch (c) {
                    case 0:
                        break;
                    case 1:
                        break;
                    case 2:
                        break;
                    case 3:
                        break;
                }
            }
            Binder.restoreCallingIdentity(token);
            return true;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(token);
            throw th;
        }
    }

    private boolean disableSystemUpdate(boolean disable) {
        String[] readBuf = {"AA"};
        int[] errorRet = new int[1];
        String writeValue = disable ? "1" : "0";
        String str = TAG;
        HwLog.i(str, "writeProtectArea :" + writeValue);
        if (HwProtectAreaManager.getInstance().writeProtectArea("SYSTEM_UPDATE_STATE", writeValue.length(), writeValue, errorRet) == 0) {
            int readRet = HwProtectAreaManager.getInstance().readProtectArea("SYSTEM_UPDATE_STATE", 4, readBuf, errorRet);
            String str2 = TAG;
            HwLog.i(str2, "readProtectArea: readRet = " + readRet + "ReadBuf = " + Arrays.toString(readBuf) + "errorRet = " + Arrays.toString(errorRet));
            if (readRet == 0 && readBuf.length >= 1 && writeValue.equals(readBuf[0])) {
                String updateState = disable ? "false" : "true";
                String str3 = TAG;
                Log.i(str3, "writeProtectArea Success! Set sys.update.state to " + updateState);
                SystemProperties.set("sys.update.state", updateState);
                return true;
            }
        }
        return false;
    }

    private boolean disableComponentForPackage(String packageName, boolean disable, PackageManager pm, int userId) {
        String str = packageName;
        PackageManager packageManager = pm;
        boolean setSuccess = true;
        if (TextUtils.isEmpty(packageName)) {
            return false;
        }
        int newState = disable ? 2 : 0;
        LauncherApps launcherApps = (LauncherApps) this.mContext.getSystemService("launcherapps");
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(str, 786959);
            if (!(packageInfo == null || packageInfo.receivers == null || packageInfo.receivers.length == 0)) {
                for (ActivityInfo activityInfo : packageInfo.receivers) {
                    packageManager.setComponentEnabledSetting(new ComponentName(str, activityInfo.name), newState, 0);
                }
            }
            if (!(packageInfo == null || packageInfo.services == null || packageInfo.services.length == 0)) {
                for (ServiceInfo serviceInfo : packageInfo.services) {
                    packageManager.setComponentEnabledSetting(new ComponentName(str, serviceInfo.name), newState, 0);
                }
            }
            if (!(packageInfo == null || packageInfo.providers == null || packageInfo.providers.length == 0)) {
                for (ProviderInfo providerInfo : packageInfo.providers) {
                    packageManager.setComponentEnabledSetting(new ComponentName(str, providerInfo.name), newState, 0);
                }
            }
            if (!(packageInfo == null || packageInfo.activities == null || packageInfo.activities.length == 0)) {
                List<LauncherActivityInfo> launcherAppList = launcherApps.getActivityList(str, new UserHandle(UserHandle.myUserId()));
                for (int i = 0; i < packageInfo.activities.length; i++) {
                    ComponentName componentName = new ComponentName(str, packageInfo.activities[i].name);
                    for (LauncherActivityInfo launcherApp : launcherAppList) {
                        if (!launcherApp.getComponentName().getClassName().contains(packageInfo.activities[i].name) && !packageInfo.activities[i].name.contains(SETTINGS_FALLBACK_ACTIVITY_NAME) && !packageInfo.activities[i].name.contains(SETTING_CRYPTKEEPER_ACTIVITY_NAME)) {
                            packageManager.setComponentEnabledSetting(componentName, newState, 0);
                        }
                    }
                }
            }
            if (disable) {
                packageManager.clearPackagePreferredActivities(str);
            }
        } catch (PackageManager.NameNotFoundException e) {
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
            List<ActivityManager.RunningTaskInfo> taskList = ((ActivityManager) this.mContext.getSystemService("activity")).getRunningTasks(ActivityManager.getMaxRecentTasksStatic());
            if (taskList != null && !taskList.isEmpty()) {
                for (ActivityManager.RunningTaskInfo ti : taskList) {
                    ComponentName baseActivity = ti.baseActivity;
                    if (baseActivity != null && TextUtils.equals(baseActivity.getPackageName(), packageName)) {
                        String str = TAG;
                        HwLog.d(str, "The killed packageName: " + packageName + " task id: " + ti.id);
                        ActivityManager.getService().removeTask(ti.id);
                    }
                }
            }
        } catch (RemoteException e) {
            String str2 = TAG;
            HwLog.d(str2, "killApplicationInner pkg:" + packageName + ", RemoteException");
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(ident);
            throw th;
        }
        Binder.restoreCallingIdentity(ident);
    }

    /* access modifiers changed from: package-private */
    public IWindowManager getIWindowManager() {
        return IWindowManager.Stub.asInterface(ServiceManager.getService("window"));
    }

    private boolean updateScreenCaptureDisabledInWindowManager(int userHandle, boolean disabled) {
        try {
            getIWindowManager().refreshScreenCaptureDisabled(userHandle);
            return true;
        } catch (RemoteException e) {
            HwLog.e(TAG, "Unable to notify WindowManager.");
            return false;
        }
    }

    /* access modifiers changed from: private */
    public void disableGoogleAccountSyncAutomatically() {
        long identityToken = Binder.clearCallingIdentity();
        try {
            SyncAdapterType[] syncs = ContentResolver.getSyncAdapterTypes();
            try {
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
                Binder.restoreCallingIdentity(identityToken);
            } catch (Throwable th) {
                th = th;
                Binder.restoreCallingIdentity(identityToken);
                throw th;
            }
        } catch (Throwable th2) {
            th = th2;
            Binder.restoreCallingIdentity(identityToken);
            throw th;
        }
    }

    private void setFloatTaskEnabled(boolean enable) {
        Settings.Secure.putIntForUser(this.mContext.getContentResolver(), FLOAT_TASK_STATE, enable, UserHandle.myUserId());
    }

    private boolean isFloatTaskRunning() {
        return Settings.Secure.getIntForUser(this.mContext.getContentResolver(), FLOAT_TASK_STATE, 0, UserHandle.myUserId()) == 1;
    }

    private boolean isFloatTaskEnableBefore() {
        return Settings.System.getInt(this.mContext.getContentResolver(), "float_task_state_before", 0) == 1;
    }

    private void storeFloatTaskState(boolean enable) {
        Settings.System.putInt(this.mContext.getContentResolver(), "float_task_state_before", enable);
    }
}

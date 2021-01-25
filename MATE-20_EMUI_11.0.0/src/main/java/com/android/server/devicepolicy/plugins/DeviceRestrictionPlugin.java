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
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.UserInfo;
import android.hdm.HwDeviceManager;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.IWindowManager;
import com.android.server.devicepolicy.DevicePolicyPlugin;
import com.android.server.devicepolicy.HwDevicePolicyManagerService;
import com.android.server.devicepolicy.HwLog;
import com.android.server.devicepolicy.PolicyStruct;
import com.huawei.android.app.HwActivityTaskManager;
import com.huawei.android.os.HwPowerManager;
import huawei.android.os.HwProtectAreaManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class DeviceRestrictionPlugin extends DevicePolicyPlugin {
    private static final String DEVICE_POLICIES_XML = "device_policies.xml";
    private static final String DISABLE_APPLICATIONS_LIST = "disable-applications-list";
    private static final String DISABLE_APPLICATIONS_LIST_ITEM = "disable-applications-list/disable-applications-list-item";
    private static final String DISABLE_CHANGE_WALLPAPER = "disable-change-wallpaper";
    private static final String DISABLE_HEADPHONE = "disable-headphone";
    private static final String DISABLE_MANUAL_SCREEN_CAPTURE = "disable-manual-screen-capture";
    private static final String DISABLE_MICROPHONE = "disable-microphone";
    private static final String DISABLE_NAVIGATIONBAR = "disable-navigationbar";
    private static final String DISABLE_NOTIFICATION = "disable-notification";
    private static final String DISABLE_SCREEN_CAPTURE = "disable-screen-capture";
    private static final String DISABLE_SCREEN_TURN_OFF = "disable-screen-turn-off";
    private static final String DISABLE_SYSTEM_BROWSER = "disable-system-browser";
    private static final String DISABLE_SYSTEM_UPDATE = "disable-system-update";
    private static final String DISABLE_SYSTEM_UPDATE_SD = "disable-system-update-sd";
    private static final String ENCODING = "UTF-8";
    private static final String FLOAT_TASK_STATE = "float_task_state";
    private static final int FLOAT_TASK_STATE_OFF = 0;
    private static final int FLOAT_TASK_STATE_ON = 1;
    private static final int INDEX_OF_HISUITE_ERECOVERY = 0;
    private static final int INDEX_OF_SD = 1;
    private static final boolean IS_TV = "tv".equals(SystemProperties.get("ro.build.characteristics", "default"));
    private static final String KEY_BOOT_MDM = "boot_alarm_mdm";
    private static final String OEMINFO_SYSTEM_UPDATE_STATE = "SYSTEM_UPDATE_STATE";
    public static final String PERMISSION_MDM_UPDATESTATE_MANAGER = "com.huawei.permission.sec.MDM_UPDATESTATE_MANAGER";
    private static final String POLICY_DEPRECATED_ADMIN_INTERFACES_ENABLED = "policy-deprecated-admin-interfaces-enabled";
    private static final String POLICY_DISABLE_MULTIWINDOW = "disable-multi-window";
    private static final String PROP_SYSTEM_UPDATE_STATE = "sys.update.state";
    private static final int READ_LENGTH = 4;
    private static final String SETTINGS_FALLBACK_ACTIVITY_NAME = "com.android.settings.FallbackHome";
    private static final String SETTINGS_PACKAGE_NAME = "com.android.settings";
    private static final String SETTING_CRYPTKEEPER_ACTIVITY_NAME = "com.android.settings.CryptKeeper";
    public static final String TAG = DeviceRestrictionPlugin.class.getSimpleName();
    private static final String TV_SETTINGS_PACKAGE_NAME = "com.huawei.homevision.settings";
    private AlarmManager mAlarmManager;
    private PendingIntent mBootPendingIntent;
    ArrayList<String> mDisableApplicationsList = new ArrayList<>();
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
        struct.addStruct(DISABLE_SYSTEM_UPDATE, PolicyStruct.PolicyType.STATE, new String[]{SettingsMDMPlugin.STATE_VALUE});
        struct.addStruct(DISABLE_SYSTEM_UPDATE_SD, PolicyStruct.PolicyType.STATE, new String[]{SettingsMDMPlugin.STATE_VALUE});
        struct.addStruct(DISABLE_SYSTEM_BROWSER, PolicyStruct.PolicyType.STATE, new String[]{SettingsMDMPlugin.STATE_VALUE});
        struct.addStruct(DISABLE_SCREEN_CAPTURE, PolicyStruct.PolicyType.STATE, new String[]{SettingsMDMPlugin.STATE_VALUE});
        struct.addStruct(DISABLE_MANUAL_SCREEN_CAPTURE, PolicyStruct.PolicyType.STATE, new String[]{SettingsMDMPlugin.STATE_VALUE});
        struct.addStruct(DISABLE_NOTIFICATION, PolicyStruct.PolicyType.STATE, new String[]{SettingsMDMPlugin.STATE_VALUE});
        struct.addStruct(DISABLE_APPLICATIONS_LIST_ITEM, PolicyStruct.PolicyType.LIST, new String[]{SettingsMDMPlugin.STATE_VALUE});
        struct.addStruct("disable-clipboard", PolicyStruct.PolicyType.STATE, new String[]{SettingsMDMPlugin.STATE_VALUE});
        struct.addStruct("disable-google-account-autosync", PolicyStruct.PolicyType.STATE, new String[]{SettingsMDMPlugin.STATE_VALUE});
        struct.addStruct(DISABLE_MICROPHONE, PolicyStruct.PolicyType.STATE, new String[]{SettingsMDMPlugin.STATE_VALUE});
        struct.addStruct(DISABLE_HEADPHONE, PolicyStruct.PolicyType.STATE, new String[]{SettingsMDMPlugin.STATE_VALUE});
        struct.addStruct("disable-send-notification", PolicyStruct.PolicyType.STATE, new String[]{SettingsMDMPlugin.STATE_VALUE});
        struct.addStruct(DISABLE_CHANGE_WALLPAPER, PolicyStruct.PolicyType.STATE, new String[]{SettingsMDMPlugin.STATE_VALUE});
        struct.addStruct("disable-power-shutdown", PolicyStruct.PolicyType.STATE, new String[]{SettingsMDMPlugin.STATE_VALUE});
        struct.addStruct("disable-shutdownmenu", PolicyStruct.PolicyType.STATE, new String[]{SettingsMDMPlugin.STATE_VALUE});
        struct.addStruct("disable-volume", PolicyStruct.PolicyType.STATE, new String[]{SettingsMDMPlugin.STATE_VALUE});
        struct.addStruct("disable-fingerprint-authentication", PolicyStruct.PolicyType.STATE, new String[]{SettingsMDMPlugin.STATE_VALUE});
        struct.addStruct("force-enable-wifi", PolicyStruct.PolicyType.STATE, new String[]{SettingsMDMPlugin.STATE_VALUE});
        struct.addStruct("force-enable-BT", PolicyStruct.PolicyType.STATE, new String[]{SettingsMDMPlugin.STATE_VALUE});
        struct.addStruct("disable_voice_outgoing", PolicyStruct.PolicyType.STATE, new String[]{SettingsMDMPlugin.STATE_VALUE});
        struct.addStruct("disable_voice_incoming", PolicyStruct.PolicyType.STATE, new String[]{SettingsMDMPlugin.STATE_VALUE});
        struct.addStruct(DISABLE_NAVIGATIONBAR, PolicyStruct.PolicyType.STATE, new String[]{SettingsMDMPlugin.STATE_VALUE});
        struct.addStruct("disable-charge", PolicyStruct.PolicyType.STATE, new String[]{SettingsMDMPlugin.STATE_VALUE});
        struct.addStruct("force_scheduled_power_on", PolicyStruct.PolicyType.STATE, new String[]{SettingsMDMPlugin.STATE_VALUE});
        struct.addStruct("force_scheduled_power_off", PolicyStruct.PolicyType.STATE, new String[]{SettingsMDMPlugin.STATE_VALUE});
        struct.addStruct("disable_float_task", PolicyStruct.PolicyType.STATE, new String[]{SettingsMDMPlugin.STATE_VALUE});
        struct.addStruct("policy-file-share-disabled", PolicyStruct.PolicyType.STATE, new String[]{SettingsMDMPlugin.STATE_VALUE});
        struct.addStruct("force_scheduled_charge_limit", PolicyStruct.PolicyType.STATE, new String[]{SettingsMDMPlugin.STATE_VALUE});
        struct.addStruct("disable_alarm", PolicyStruct.PolicyType.STATE, new String[]{SettingsMDMPlugin.STATE_VALUE});
        struct.addStruct(POLICY_DEPRECATED_ADMIN_INTERFACES_ENABLED, PolicyStruct.PolicyType.STATE, true, new String[]{SettingsMDMPlugin.STATE_VALUE});
        struct.addStruct(POLICY_DISABLE_MULTIWINDOW, PolicyStruct.PolicyType.STATE, new String[]{SettingsMDMPlugin.STATE_VALUE});
        struct.addStruct(DISABLE_SCREEN_TURN_OFF, PolicyStruct.PolicyType.STATE, new String[]{SettingsMDMPlugin.STATE_VALUE});
        struct.addStruct("disable_status_bar", PolicyStruct.PolicyType.STATE, new String[]{SettingsMDMPlugin.STATE_VALUE});
        struct.addStruct("disable-voice-assistant-button", PolicyStruct.PolicyType.STATE, new String[]{SettingsMDMPlugin.STATE_VALUE});
        return struct;
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    public boolean checkCallingPermission(ComponentName who, String policyName) {
        char c;
        HwLog.i(TAG, "checkCallingPermission");
        switch (policyName.hashCode()) {
            case -2136501252:
                if (policyName.equals(DISABLE_SYSTEM_UPDATE_SD)) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case -2065188737:
                if (policyName.equals("force_scheduled_power_on")) {
                    c = '\r';
                    break;
                }
                c = 65535;
                break;
            case -1616043882:
                if (policyName.equals("force_scheduled_charge_limit")) {
                    c = 31;
                    break;
                }
                c = 65535;
                break;
            case -1462770845:
                if (policyName.equals(DISABLE_APPLICATIONS_LIST)) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case -1418767509:
                if (policyName.equals("disable-send-notification")) {
                    c = '\n';
                    break;
                }
                c = 65535;
                break;
            case -1371405315:
                if (policyName.equals("disable_status_bar")) {
                    c = 17;
                    break;
                }
                c = 65535;
                break;
            case -1333051633:
                if (policyName.equals(DISABLE_SYSTEM_BROWSER)) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case -694001423:
                if (policyName.equals("disable-clipboard")) {
                    c = 6;
                    break;
                }
                c = 65535;
                break;
            case -595558097:
                if (policyName.equals(DISABLE_MICROPHONE)) {
                    c = '\t';
                    break;
                }
                c = 65535;
                break;
            case -515362246:
                if (policyName.equals("disable_alarm")) {
                    c = 15;
                    break;
                }
                c = 65535;
                break;
            case -304109734:
                if (policyName.equals(DISABLE_NAVIGATIONBAR)) {
                    c = 28;
                    break;
                }
                c = 65535;
                break;
            case -168307399:
                if (policyName.equals("disable-charge")) {
                    c = '\f';
                    break;
                }
                c = 65535;
                break;
            case -31729136:
                if (policyName.equals("disable_voice_outgoing")) {
                    c = 24;
                    break;
                }
                c = 65535;
                break;
            case -614710:
                if (policyName.equals("disable_voice_incoming")) {
                    c = 25;
                    break;
                }
                c = 65535;
                break;
            case 153563136:
                if (policyName.equals("policy-file-share-disabled")) {
                    c = 30;
                    break;
                }
                c = 65535;
                break;
            case 382441887:
                if (policyName.equals("disable-volume")) {
                    c = 20;
                    break;
                }
                c = 65535;
                break;
            case 403658447:
                if (policyName.equals("force_scheduled_power_off")) {
                    c = 14;
                    break;
                }
                c = 65535;
                break;
            case 458488698:
                if (policyName.equals("disable-shutdownmenu")) {
                    c = 19;
                    break;
                }
                c = 65535;
                break;
            case 476421226:
                if (policyName.equals(DISABLE_SCREEN_CAPTURE)) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case 539407267:
                if (policyName.equals("disable-power-shutdown")) {
                    c = 18;
                    break;
                }
                c = 65535;
                break;
            case 568353227:
                if (policyName.equals(POLICY_DEPRECATED_ADMIN_INTERFACES_ENABLED)) {
                    c = ' ';
                    break;
                }
                c = 65535;
                break;
            case 594183088:
                if (policyName.equals(DISABLE_NOTIFICATION)) {
                    c = 27;
                    break;
                }
                c = 65535;
                break;
            case 665075905:
                if (policyName.equals("disable-voice-assistant-button")) {
                    c = 26;
                    break;
                }
                c = 65535;
                break;
            case 702979817:
                if (policyName.equals(DISABLE_HEADPHONE)) {
                    c = '\b';
                    break;
                }
                c = 65535;
                break;
            case 731920490:
                if (policyName.equals(DISABLE_CHANGE_WALLPAPER)) {
                    c = 11;
                    break;
                }
                c = 65535;
                break;
            case 775851010:
                if (policyName.equals(DISABLE_SYSTEM_UPDATE)) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case 1240910281:
                if (policyName.equals(POLICY_DISABLE_MULTIWINDOW)) {
                    c = 16;
                    break;
                }
                c = 65535;
                break;
            case 1389850009:
                if (policyName.equals("disable-google-account-autosync")) {
                    c = 7;
                    break;
                }
                c = 65535;
                break;
            case 1658369855:
                if (policyName.equals("disable_float_task")) {
                    c = 29;
                    break;
                }
                c = 65535;
                break;
            case 1732514471:
                if (policyName.equals(DISABLE_MANUAL_SCREEN_CAPTURE)) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case 1785346365:
                if (policyName.equals("force-enable-wifi")) {
                    c = 22;
                    break;
                }
                c = 65535;
                break;
            case 1946452102:
                if (policyName.equals("disable-fingerprint-authentication")) {
                    c = 21;
                    break;
                }
                c = 65535;
                break;
            case 1981742202:
                if (policyName.equals("force-enable-BT")) {
                    c = 23;
                    break;
                }
                c = 65535;
                break;
            case 2066934651:
                if (policyName.equals(DISABLE_SCREEN_TURN_OFF)) {
                    c = '!';
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
            case 1:
                this.mContext.enforceCallingOrSelfPermission(PERMISSION_MDM_UPDATESTATE_MANAGER, "does not have system_update_management MDM permission!");
                break;
            case 2:
            case 3:
                this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_CAPTURE_SCREEN", "does not have capture_screen_management MDM permission!");
                break;
            case 4:
            case HwDevicePolicyManagerService.SD_CRYPT_STATE_MISMATCH /* 5 */:
                this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_APP_MANAGEMENT", "does not have app_management MDM permission!");
                break;
            case HwDevicePolicyManagerService.SD_CRYPT_STATE_WAIT_UNLOCK /* 6 */:
                this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_CLIPBOARD", "does not have clipboard MDM permission!");
                break;
            case 7:
                this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_GOOGLE_ACCOUNT", "does not have google account MDM permission!");
                break;
            case '\b':
            case '\t':
            case '\n':
            case 11:
            case '\f':
            case '\r':
            case 14:
            case 15:
            case 16:
            case 17:
                HwLog.i(TAG, "check the calling Permission");
                this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_DEVICE_MANAGER", "does not have device_manager MDM permission!");
                return true;
            case 18:
                this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_DEVICE_MANAGER", "does not have device_manager MDM permission!");
                break;
            case 19:
                this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_DEVICE_MANAGER", "does not have device_manager MDM permission!");
                break;
            case 20:
                this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_DEVICE_MANAGER", "does not have device_manager MDM permission!");
                break;
            case 21:
                this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_FINGERPRINT", "does not have fingerprint MDM permission!");
                break;
            case 22:
                this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_WIFI", "does not have wifi MDM permission!");
                break;
            case 23:
                this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_BLUETOOTH", "does not have bluetooth MDM permission!");
                break;
            case 24:
            case 25:
                this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_PHONE", "does not have google account MDM permission!");
                break;
            case 26:
            case 27:
                this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_DEVICE_MANAGER", "does not have device_manager MDM permission!");
                break;
            case 28:
                this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_DEVICE_MANAGER", "does not have device_manager MDM permission!");
                break;
            case 29:
                this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_DEVICE_MANAGER", "does not have device_manager MDM permission!");
                break;
            case 30:
                this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_DEVICE_MANAGER", "does not have device_manager MDM permission!");
                break;
            case 31:
                this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_DEVICE_MANAGER", "does not have device_manager MDM permission!");
                break;
            case ' ':
                this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_DEVICE_MANAGER", "does not have device_manager MDM permission!");
                break;
            case '!':
                this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_DEVICE_MANAGER", "does not have device_manager MDM permission!");
                break;
        }
        return true;
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    public boolean onSetPolicy(ComponentName who, String policyName, Bundle policyData, boolean isEffective) {
        if (policyData == null) {
            HwLog.i(TAG, "onSetPolicy policyData is null");
            return false;
        }
        HwLog.i(TAG, "onSetPolicy");
        boolean isSetSucess = true;
        PackageManager pm = this.mContext.getPackageManager();
        long token = Binder.clearCallingIdentity();
        char c = 65535;
        try {
            switch (policyName.hashCode()) {
                case -2136501252:
                    if (policyName.equals(DISABLE_SYSTEM_UPDATE_SD)) {
                        c = 1;
                        break;
                    }
                    break;
                case -2065188737:
                    if (policyName.equals("force_scheduled_power_on")) {
                        c = '\b';
                        break;
                    }
                    break;
                case -1616043882:
                    if (policyName.equals("force_scheduled_charge_limit")) {
                        c = 11;
                        break;
                    }
                    break;
                case -1462770845:
                    if (policyName.equals(DISABLE_APPLICATIONS_LIST)) {
                        c = 2;
                        break;
                    }
                    break;
                case -694001423:
                    if (policyName.equals("disable-clipboard")) {
                        c = 3;
                        break;
                    }
                    break;
                case -168307399:
                    if (policyName.equals("disable-charge")) {
                        c = 7;
                        break;
                    }
                    break;
                case 403658447:
                    if (policyName.equals("force_scheduled_power_off")) {
                        c = '\t';
                        break;
                    }
                    break;
                case 775851010:
                    if (policyName.equals(DISABLE_SYSTEM_UPDATE)) {
                        c = 0;
                        break;
                    }
                    break;
                case 1240910281:
                    if (policyName.equals(POLICY_DISABLE_MULTIWINDOW)) {
                        c = '\f';
                        break;
                    }
                    break;
                case 1389850009:
                    if (policyName.equals("disable-google-account-autosync")) {
                        c = 4;
                        break;
                    }
                    break;
                case 1658369855:
                    if (policyName.equals("disable_float_task")) {
                        c = '\n';
                        break;
                    }
                    break;
                case 1785346365:
                    if (policyName.equals("force-enable-wifi")) {
                        c = 5;
                        break;
                    }
                    break;
                case 1981742202:
                    if (policyName.equals("force-enable-BT")) {
                        c = 6;
                        break;
                    }
                    break;
            }
            switch (c) {
                case 0:
                    if (isEffective) {
                        isSetSucess = disableSystemUpdate(policyData.getBoolean(SettingsMDMPlugin.STATE_VALUE));
                        break;
                    }
                    break;
                case 1:
                    if (isEffective) {
                        isSetSucess = disableSdCardUpdate(policyData.getBoolean(SettingsMDMPlugin.STATE_VALUE));
                        break;
                    }
                    break;
                case 2:
                    ArrayList<String> list = policyData.getStringArrayList(SettingsMDMPlugin.STATE_VALUE);
                    if (!(list == null || list.size() == 0)) {
                        int j = list.size();
                        for (int i = 0; i < j; i++) {
                            String app = list.get(i);
                            isSetSucess = disableComponentForPackage(app, true, pm, 0);
                            this.mDisableApplicationsList.add(app);
                        }
                        break;
                    }
                case 4:
                    boolean isDisable = policyData.getBoolean(SettingsMDMPlugin.STATE_VALUE, false);
                    if (isEffective && isDisable) {
                        new Thread(new Runnable() {
                            /* class com.android.server.devicepolicy.plugins.DeviceRestrictionPlugin.AnonymousClass1 */

                            @Override // java.lang.Runnable
                            public void run() {
                                DeviceRestrictionPlugin.this.disableGoogleAccountSyncAutomatically();
                            }
                        }).start();
                        break;
                    }
                case HwDevicePolicyManagerService.SD_CRYPT_STATE_MISMATCH /* 5 */:
                    openWifi();
                    break;
                case HwDevicePolicyManagerService.SD_CRYPT_STATE_WAIT_UNLOCK /* 6 */:
                    openBt();
                    break;
                case 7:
                    if (policyData.getBoolean(SettingsMDMPlugin.STATE_VALUE, false)) {
                        HwPowerManager.setPowerState(false);
                        break;
                    } else {
                        HwPowerManager.setPowerState(true);
                        break;
                    }
                case '\b':
                    if (policyData.getBoolean(SettingsMDMPlugin.STATE_VALUE, false)) {
                        long whenBoot = policyData.getLong("when", 0);
                        String boradcastBoot = policyData.getString("boradcast", SettingsMDMPlugin.EMPTY_STRING);
                        HwLog.i(TAG, " FORCE_SCHEDULED_POWER_ON boradcast:" + boradcastBoot + ",when:" + whenBoot);
                        Intent intent = new Intent(boradcastBoot);
                        intent.putExtra(KEY_BOOT_MDM, true);
                        this.mBootPendingIntent = PendingIntent.getBroadcast(this.mContext, 0, intent, 0);
                        setAlarm(whenBoot, this.mBootPendingIntent);
                        break;
                    } else {
                        HwLog.i(TAG, " ORCE_SCHEDULED_POWER_ON cancel");
                        cancelAlarm(this.mBootPendingIntent);
                        break;
                    }
                case '\t':
                    if (policyData.getBoolean(SettingsMDMPlugin.STATE_VALUE, false)) {
                        long whenShutdown = policyData.getLong("when", 0);
                        String boradcastShutdown = policyData.getString("boradcast", SettingsMDMPlugin.EMPTY_STRING);
                        HwLog.i(TAG, " FORCE_SCHEDULED_POWER_OFF boradcast:" + boradcastShutdown + ",when:" + whenShutdown);
                        this.mShutdownPendingIntent = PendingIntent.getBroadcast(this.mContext, 0, new Intent(boradcastShutdown), 0);
                        setAlarm(whenShutdown, this.mShutdownPendingIntent);
                        break;
                    } else {
                        HwLog.i(TAG, " FORCE_SCHEDULED_POWER_OFF cancel");
                        cancelAlarm(this.mShutdownPendingIntent);
                        break;
                    }
                case '\n':
                    boolean enable = isFloatTaskRunning();
                    storeFloatTaskState(enable);
                    if (enable) {
                        setFloatTaskEnabled(false);
                        break;
                    }
                    break;
                case 11:
                    HwPowerManager.setChargeLimit(policyData.getString("charge_limit", HwDevicePolicyManagerService.DYNAMIC_ROOT_STATE_SAFE));
                    break;
                case '\f':
                    boolean disabled = policyData.getBoolean(SettingsMDMPlugin.STATE_VALUE);
                    HwLog.i(TAG, "POLICY_DISABLE_MULTIWINDOW start set value: " + disabled);
                    return HwActivityTaskManager.setMultiWindowDisabled(disabled);
            }
            Binder.restoreCallingIdentity(token);
            return isSetSucess;
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:14:0x0031 A[Catch:{ all -> 0x004f }] */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x003c A[Catch:{ all -> 0x004f }] */
    public void onSetPolicyCompleted(ComponentName who, String policyName, boolean isChanged) {
        HwLog.i(TAG, "onSetPolicyCompleted");
        long token = Binder.clearCallingIdentity();
        char c = 65535;
        try {
            int hashCode = policyName.hashCode();
            if (hashCode != -304109734) {
                if (hashCode == 476421226 && policyName.equals(DISABLE_SCREEN_CAPTURE)) {
                    c = 1;
                    if (c == 0) {
                        changeNavigationBarStatus(UserHandle.getCallingUserId(), HwDeviceManager.disallowOp(103));
                    } else if (c == 1) {
                        updateScreenCaptureDisabledInWindowManager(UserHandle.getCallingUserId());
                    }
                }
            } else if (policyName.equals(DISABLE_NAVIGATIONBAR)) {
                c = 0;
                if (c == 0) {
                }
            }
            if (c == 0) {
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
        AlarmManager alarmManager = this.mAlarmManager;
        if (alarmManager != null && intent != null) {
            alarmManager.cancel(intent);
        }
    }

    private void changeNavigationBarStatus(int navBarUserHandle, boolean isNavPolicyData) {
        String enableNavbar;
        String str = TAG;
        HwLog.i(str, "MDM policy changeNavigationBarStatus nav_policyData = " + isNavPolicyData);
        if (isNavPolicyData) {
            enableNavbar = HwDevicePolicyManagerService.DYNAMIC_ROOT_STATE_SAFE;
        } else {
            enableNavbar = "1";
        }
        Intent intent = new Intent("com.huawei.navigationbar.statuschange");
        intent.setPackage("com.android.systemui");
        intent.putExtra("minNavigationBar", isNavPolicyData);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        List<UserInfo> userInfo = UserManager.get(this.mContext).getUsers();
        int usersSize = userInfo.size();
        for (int i = 0; i < usersSize; i++) {
            Settings.System.putStringForUser(this.mContext.getContentResolver(), "enable_navbar", enableNavbar, userInfo.get(i).id);
        }
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

    /* JADX WARNING: Removed duplicated region for block: B:18:0x0042 A[Catch:{ all -> 0x0075 }] */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x004f A[Catch:{ all -> 0x0075 }] */
    public boolean onRemovePolicy(ComponentName who, String policyName, Bundle policyData, boolean isEffective) {
        if (policyData == null) {
            HwLog.i(TAG, "onRemovePolicy policyData is null");
            return false;
        }
        HwLog.i(TAG, "onRemovePolicy");
        PackageManager pm = this.mContext.getPackageManager();
        long token = Binder.clearCallingIdentity();
        char c = 65535;
        try {
            int hashCode = policyName.hashCode();
            if (hashCode != -1462770845) {
                if (hashCode == 1658369855 && policyName.equals("disable_float_task")) {
                    c = 1;
                    if (c == 0) {
                        ArrayList<String> list = policyData.getStringArrayList(SettingsMDMPlugin.STATE_VALUE);
                        if (!(list == null || list.size() == 0)) {
                            int j = list.size();
                            for (int i = 0; i < j; i++) {
                                disableComponentForPackage(list.get(i), false, pm, 0);
                            }
                        }
                    } else if (c == 1) {
                        if (isFloatTaskEnableBefore()) {
                            setFloatTaskEnabled(true);
                        }
                    }
                    return true;
                }
            } else if (policyName.equals(DISABLE_APPLICATIONS_LIST)) {
                c = 0;
                if (c == 0) {
                }
                return true;
            }
            if (c == 0) {
            }
            return true;
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    public boolean onActiveAdminRemoved(ComponentName who, ArrayList<PolicyStruct.PolicyItem> removedPolicies) {
        Throwable th;
        ArrayList<String> list;
        HwLog.i(TAG, "onActiveAdminRemoved");
        PackageManager pm = this.mContext.getPackageManager();
        long token = Binder.clearCallingIdentity();
        try {
            int size = removedPolicies.size();
            for (int m = 0; m < size; m++) {
                try {
                    PolicyStruct.PolicyItem policyItem = removedPolicies.get(m);
                    if (policyItem != null) {
                        String policyName = policyItem.getPolicyName();
                        char c = 65535;
                        switch (policyName.hashCode()) {
                            case -2136501252:
                                if (policyName.equals(DISABLE_SYSTEM_UPDATE_SD)) {
                                    c = 1;
                                    break;
                                }
                                break;
                            case -1462770845:
                                if (policyName.equals(DISABLE_APPLICATIONS_LIST)) {
                                    c = 2;
                                    break;
                                }
                                break;
                            case 775851010:
                                if (policyName.equals(DISABLE_SYSTEM_UPDATE)) {
                                    c = 0;
                                    break;
                                }
                                break;
                            case 1240910281:
                                if (policyName.equals(POLICY_DISABLE_MULTIWINDOW)) {
                                    c = 4;
                                    break;
                                }
                                break;
                            case 1658369855:
                                if (policyName.equals("disable_float_task")) {
                                    c = 3;
                                    break;
                                }
                                break;
                        }
                        if (c == 0) {
                            disableSystemUpdate(false);
                        } else if (c == 1) {
                            disableSdCardUpdate(false);
                        } else if (c == 2) {
                            if (this.mDisableApplicationsList == null || this.mDisableApplicationsList.size() == 0) {
                                list = policyItem.combineAllAttributes().getStringArrayList(SettingsMDMPlugin.STATE_VALUE);
                            } else {
                                list = this.mDisableApplicationsList;
                            }
                            if (!(list == null || list.size() == 0)) {
                                int j = list.size();
                                for (int i = 0; i < j; i++) {
                                    disableComponentForPackage(list.get(i), false, pm, 0);
                                }
                            }
                        } else if (c != 3) {
                            if (c == 4) {
                                HwActivityTaskManager.setMultiWindowDisabled(false);
                            }
                        } else if (isFloatTaskEnableBefore()) {
                            setFloatTaskEnabled(true);
                        }
                    }
                } catch (Throwable th2) {
                    th = th2;
                    Binder.restoreCallingIdentity(token);
                    throw th;
                }
            }
            Binder.restoreCallingIdentity(token);
            return true;
        } catch (Throwable th3) {
            th = th3;
            Binder.restoreCallingIdentity(token);
            throw th;
        }
    }

    public void onActiveAdminRemovedCompleted(ComponentName who, ArrayList<PolicyStruct.PolicyItem> removedPolicies) {
        HwLog.i(TAG, "onActiveAdminRemovedCompleted");
        if (removedPolicies != null) {
            Iterator<PolicyStruct.PolicyItem> it = removedPolicies.iterator();
            while (it.hasNext()) {
                PolicyStruct.PolicyItem policyItem = it.next();
                if (policyItem != null) {
                    String policyName = policyItem.getPolicyName();
                    char c = 65535;
                    if (policyName.hashCode() == 476421226 && policyName.equals(DISABLE_SCREEN_CAPTURE)) {
                        c = 0;
                    }
                    if (c == 0) {
                        updateScreenCaptureDisabledInWindowManager(UserHandle.getCallingUserId());
                    }
                }
            }
        }
    }

    private void initUpdateState() {
        String updateState = "true";
        if (updateState.equals(PluginUtils.readValueFromXml(DISABLE_SYSTEM_UPDATE))) {
            updateState = "false";
        }
        SystemProperties.set(PROP_SYSTEM_UPDATE_STATE, updateState);
        String str = TAG;
        HwLog.i(str, "Init update state prop to " + updateState);
    }

    public boolean onInit(PolicyStruct struct) {
        HwLog.i(TAG, "onInit");
        initUpdateState();
        return true;
    }

    private int getBitForString(String str, int bitIndex) {
        try {
            return (Integer.parseInt(str) >> bitIndex) & 1;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("str: " + str + " is invalid.");
        }
    }

    private String setBitForString(String str, int bitIndex, boolean isDisabled) {
        int value;
        try {
            int value2 = Integer.parseInt(str);
            if (isDisabled) {
                value = value2 | (1 << bitIndex);
            } else {
                value = value2 & (~(1 << bitIndex));
            }
            return String.valueOf(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("str: " + str + " is invalid.");
        }
    }

    private String disableBitForString(String str, int bitIndex, boolean isDisabled) {
        try {
            boolean isBitCurrntValue = true;
            if (getBitForString(str, bitIndex) != 1) {
                isBitCurrntValue = false;
            }
            if (isBitCurrntValue == isDisabled) {
                return str;
            }
            return setBitForString(str, bitIndex, isDisabled);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("str: " + str + " is invalid.");
        }
    }

    private int getValueFromUpdatePolicy(int originValue, String policyName, int bitIndex) {
        boolean policyValue = false;
        PolicyStruct.PolicyItem item = this.mPolicyStruct.getPolicyItem(policyName);
        if (item != null) {
            policyValue = item.combineAllAttributes().getBoolean(SettingsMDMPlugin.STATE_VALUE);
        }
        if (policyValue) {
            return originValue | (1 << bitIndex);
        }
        return originValue;
    }

    private void getUpdateStatusFromPolicy(String[] readBuf) {
        readBuf[0] = String.valueOf(getValueFromUpdatePolicy(getValueFromUpdatePolicy(0, DISABLE_SYSTEM_UPDATE, 0), DISABLE_SYSTEM_UPDATE_SD, 1));
        String str = TAG;
        HwLog.i(str, "readUpdateStatusFromPolicy: policy = " + readBuf[0]);
    }

    private void readUpdateStatus(String optItem, int readBufLen, String[] readBuf, int[] errorNum) {
        int readRet = HwProtectAreaManager.getInstance().readProtectArea(optItem, readBufLen, readBuf, errorNum);
        String str = TAG;
        HwLog.i(str, "readUpdateStatus: readRet = " + readRet + " ReadBuf = " + Arrays.toString(readBuf) + " errorRet = " + Arrays.toString(errorNum));
        if (readRet != 0 || readBuf.length <= 0) {
            getUpdateStatusFromPolicy(readBuf);
        }
    }

    private boolean testBitForOemInfo(String optItem, int bitIndex, boolean isDisabled) {
        String[] readBuf = {"AA"};
        readUpdateStatus(optItem, 4, readBuf, new int[1]);
        try {
            if ((getBitForString(readBuf[0], bitIndex) == 1) == isDisabled) {
                return true;
            }
            return false;
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("str: " + readBuf[0] + " is invalid.");
        }
    }

    private boolean setBitForOemInfo(String optItem, int bitIndex, boolean isDisabled) {
        String[] readBuf = {"AA"};
        int[] errorRet = new int[1];
        readUpdateStatus(optItem, 4, readBuf, errorRet);
        try {
            String writeValue = disableBitForString(readBuf[0], bitIndex, isDisabled);
            String str = TAG;
            HwLog.i(str, "writeValue: " + writeValue);
            if (HwProtectAreaManager.getInstance().writeProtectArea(optItem, writeValue.length(), writeValue, errorRet) != 0) {
                return false;
            }
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private boolean disableBitForOemInfo(String optItem, int bitIndex, boolean isDisabled) {
        boolean isSuccess = setBitForOemInfo(optItem, bitIndex, isDisabled);
        if (!isSuccess) {
            return false;
        }
        try {
            return testBitForOemInfo(optItem, bitIndex, isDisabled);
        } catch (IllegalArgumentException e) {
            HwLog.i(TAG, "disableBitForOemInfo exception");
            return isSuccess;
        }
    }

    private boolean disableSdCardUpdate(boolean isDisabled) {
        boolean isSuccess = disableBitForOemInfo(OEMINFO_SYSTEM_UPDATE_STATE, 1, isDisabled);
        if (!isSuccess) {
            HwLog.i(TAG, "disableSdCardUpdate return fail");
        }
        return isSuccess;
    }

    private boolean disableSystemUpdate(boolean isDisabled) {
        if (!disableBitForOemInfo(OEMINFO_SYSTEM_UPDATE_STATE, 0, isDisabled)) {
            HwLog.i(TAG, "disableSystemUpdate return fail");
            return false;
        }
        String updateState = isDisabled ? "false" : "true";
        SystemProperties.set(PROP_SYSTEM_UPDATE_STATE, updateState);
        String str = TAG;
        HwLog.i(str, "Set update state prop to " + updateState);
        return true;
    }

    private boolean disableComponentForPackage(String packageName, boolean isDisable, PackageManager pm, int userId) {
        boolean isSetSuccess = true;
        if (TextUtils.isEmpty(packageName)) {
            return false;
        }
        int newState = isDisable ? 2 : 0;
        LauncherApps launcherApps = (LauncherApps) this.mContext.getSystemService("launcherapps");
        boolean tvSettingsFlag = IS_TV && TV_SETTINGS_PACKAGE_NAME.equals(packageName);
        try {
            PackageInfo packageInfo = pm.getPackageInfo(packageName, 786959);
            if (!(tvSettingsFlag || packageInfo == null || packageInfo.receivers == null || packageInfo.receivers.length == 0)) {
                for (int i = 0; i < packageInfo.receivers.length; i++) {
                    pm.setComponentEnabledSetting(new ComponentName(packageName, packageInfo.receivers[i].name), newState, 0);
                }
            }
            if (!(tvSettingsFlag || packageInfo == null || packageInfo.services == null || packageInfo.services.length == 0)) {
                for (int i2 = 0; i2 < packageInfo.services.length; i2++) {
                    pm.setComponentEnabledSetting(new ComponentName(packageName, packageInfo.services[i2].name), newState, 0);
                }
            }
            if (!(tvSettingsFlag || packageInfo == null || packageInfo.providers == null || packageInfo.providers.length == 0)) {
                for (int i3 = 0; i3 < packageInfo.providers.length; i3++) {
                    pm.setComponentEnabledSetting(new ComponentName(packageName, packageInfo.providers[i3].name), newState, 0);
                }
            }
            if (!(packageInfo == null || packageInfo.activities == null || packageInfo.activities.length == 0)) {
                List<LauncherActivityInfo> launcherAppList = launcherApps.getActivityList(packageName, new UserHandle(UserHandle.myUserId()));
                for (int i4 = 0; i4 < packageInfo.activities.length; i4++) {
                    ComponentName componentName = new ComponentName(packageName, packageInfo.activities[i4].name);
                    for (LauncherActivityInfo launcherApp : launcherAppList) {
                        if (!launcherApp.getComponentName().getClassName().contains(packageInfo.activities[i4].name) && !packageInfo.activities[i4].name.contains(SETTINGS_FALLBACK_ACTIVITY_NAME) && !packageInfo.activities[i4].name.contains(SETTING_CRYPTKEEPER_ACTIVITY_NAME)) {
                            pm.setComponentEnabledSetting(componentName, newState, tvSettingsFlag ? 1 : 0);
                        }
                    }
                }
            }
            if (isDisable) {
                pm.clearPackagePreferredActivities(packageName);
            }
        } catch (PackageManager.NameNotFoundException e) {
            isSetSuccess = false;
        }
        pm.flushPackageRestrictionsAsUser(userId);
        if (isDisable && !tvSettingsFlag) {
            killApplicationInner(packageName);
        }
        return isSetSuccess;
    }

    private void killApplicationInner(String packageName) {
        long ident = Binder.clearCallingIdentity();
        try {
            List<ActivityManager.RunningTaskInfo> taskList = ((ActivityManager) this.mContext.getSystemService("activity")).getRunningTasks(ActivityManager.getMaxRecentTasksStatic());
            if (taskList != null && !taskList.isEmpty()) {
                for (ActivityManager.RunningTaskInfo ti : taskList) {
                    ComponentName baseActivity = ti.baseActivity;
                    if (baseActivity != null && TextUtils.equals(baseActivity.getPackageName(), packageName)) {
                        ActivityManager.getService().removeTask(ti.id);
                    }
                }
            }
        } catch (RemoteException e) {
            HwLog.d(TAG, "killApplicationInner pkg RemoteException");
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

    private boolean updateScreenCaptureDisabledInWindowManager(int userHandle) {
        try {
            getIWindowManager().refreshScreenCaptureDisabled(userHandle);
            return true;
        } catch (RemoteException e) {
            HwLog.e(TAG, "Unable to notify WindowManager.");
            return false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void disableGoogleAccountSyncAutomatically() {
        Throwable th;
        String GOOGLE_ACCOUNT_TYPE = "com.google";
        long identityToken = Binder.clearCallingIdentity();
        try {
            SyncAdapterType[] syncs = ContentResolver.getSyncAdapterTypes();
            try {
                Account[] accounts = AccountManager.get(this.mContext).getAccountsByType("com.google");
                HwLog.i(TAG, "get google accounts, size=" + accounts.length);
                for (Account account : accounts) {
                    int length = syncs.length;
                    int i = 0;
                    while (i < length) {
                        SyncAdapterType adapter = syncs[i];
                        String str = TAG;
                        StringBuilder sb = new StringBuilder();
                        try {
                            sb.append("getCurrentSyncs, type=");
                            sb.append(adapter.accountType);
                            sb.append(", authority=");
                            sb.append(adapter.authority);
                            sb.append(", visible=");
                            sb.append(adapter.isUserVisible());
                            HwLog.i(str, sb.toString());
                            if ("com.google".equals(adapter.accountType) && adapter.isUserVisible()) {
                                if (ContentResolver.getSyncAutomatically(account, adapter.authority)) {
                                    HwLog.i(TAG, "setSyncAutomatically to false for google account authority: " + adapter.authority);
                                    ContentResolver.setSyncAutomatically(account, adapter.authority, false);
                                }
                            }
                            i++;
                            GOOGLE_ACCOUNT_TYPE = GOOGLE_ACCOUNT_TYPE;
                        } catch (Throwable th2) {
                            th = th2;
                            Binder.restoreCallingIdentity(identityToken);
                            throw th;
                        }
                    }
                }
                Binder.restoreCallingIdentity(identityToken);
            } catch (Throwable th3) {
                th = th3;
                Binder.restoreCallingIdentity(identityToken);
                throw th;
            }
        } catch (Throwable th4) {
            th = th4;
            Binder.restoreCallingIdentity(identityToken);
            throw th;
        }
    }

    private void setFloatTaskEnabled(boolean isEnable) {
        Settings.Secure.putIntForUser(this.mContext.getContentResolver(), FLOAT_TASK_STATE, isEnable ? 1 : 0, UserHandle.myUserId());
    }

    private boolean isFloatTaskRunning() {
        return Settings.Secure.getIntForUser(this.mContext.getContentResolver(), FLOAT_TASK_STATE, 0, UserHandle.myUserId()) == 1;
    }

    private boolean isFloatTaskEnableBefore() {
        return Settings.System.getInt(this.mContext.getContentResolver(), "float_task_state_before", 0) == 1;
    }

    private void storeFloatTaskState(boolean isEnable) {
        Settings.System.putInt(this.mContext.getContentResolver(), "float_task_state_before", isEnable ? 1 : 0);
    }
}

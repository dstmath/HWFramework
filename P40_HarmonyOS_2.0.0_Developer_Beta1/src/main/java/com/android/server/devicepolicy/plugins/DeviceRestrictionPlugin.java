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
    private static final String GOOGLE_ACCOUNT_TYPE = "com.google";
    private static final int INDEX_OF_HISUITE_ERECOVERY = 0;
    private static final int INDEX_OF_SD = 1;
    private static final boolean IS_TV = "tv".equals(SystemProperties.get("ro.build.characteristics", "default"));
    private static final String KEY_BOOT_MDM = "boot_alarm_mdm";
    private static final String NAVIGATIONBAR_DISABLE = "0";
    private static final String NAVIGATIONBAR_ENABLE = "1";
    private static final String NODE_VALUE = "value";
    private static final String OEMINFO_SYSTEM_UPDATE_STATE = "SYSTEM_UPDATE_STATE";
    private static final String POLICY_DEPRECATED_ADMIN_INTERFACES_ENABLED = "policy-deprecated-admin-interfaces-enabled";
    private static final String POLICY_DISABLE_MULTIWINDOW = "disable-multi-window";
    private static final String PROP_SYSTEM_UPDATE_STATE = "sys.update.state";
    private static final int READ_LENGTH = 4;
    private static final String SETTINGS_FALLBACK_ACTIVITY_NAME = "com.android.settings.FallbackHome";
    private static final String SETTINGS_PACKAGE_NAME = "com.android.settings";
    private static final String SETTING_CRYPTKEEPER_ACTIVITY_NAME = "com.android.settings.CryptKeeper";
    private static final String TAG = DeviceRestrictionPlugin.class.getSimpleName();
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
        String[] policyNames = {DISABLE_SYSTEM_UPDATE, DISABLE_SYSTEM_UPDATE_SD, DISABLE_SYSTEM_BROWSER, DISABLE_SCREEN_CAPTURE, DISABLE_MANUAL_SCREEN_CAPTURE, DISABLE_NOTIFICATION, "disable-clipboard", "disable-google-account-autosync", DISABLE_MICROPHONE, DISABLE_HEADPHONE, "disable-send-notification", DISABLE_CHANGE_WALLPAPER, "disable-power-shutdown", "disable-shutdownmenu", "disable-volume", "disable-fingerprint-authentication", "force-enable-wifi", "force-enable-BT", "disable_voice_outgoing", "disable_voice_incoming", DISABLE_NAVIGATIONBAR, "disable-charge", "force_scheduled_power_on", "force_scheduled_power_off", "disable_float_task", "policy-file-share-disabled", "force_scheduled_charge_limit", "disable_alarm", POLICY_DISABLE_MULTIWINDOW, DISABLE_SCREEN_TURN_OFF, "disable_status_bar", "disable-voice-assistant-button"};
        PolicyStruct struct = new PolicyStruct(this);
        for (String policyName : policyNames) {
            struct.addStruct(policyName, PolicyStruct.PolicyType.STATE, new String[]{"value"});
        }
        struct.addStruct(DISABLE_APPLICATIONS_LIST_ITEM, PolicyStruct.PolicyType.LIST, new String[]{"value"});
        struct.addStruct(POLICY_DEPRECATED_ADMIN_INTERFACES_ENABLED, PolicyStruct.PolicyType.STATE, true, new String[]{"value"});
        return struct;
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    public boolean checkCallingPermission(ComponentName who, String policyName) {
        char c;
        switch (policyName.hashCode()) {
            case -2136501252:
                if (policyName.equals(DISABLE_SYSTEM_UPDATE_SD)) {
                    c = 1;
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
            case -31729136:
                if (policyName.equals("disable_voice_outgoing")) {
                    c = 11;
                    break;
                }
                c = 65535;
                break;
            case -614710:
                if (policyName.equals("disable_voice_incoming")) {
                    c = '\f';
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
            case 775851010:
                if (policyName.equals(DISABLE_SYSTEM_UPDATE)) {
                    c = 0;
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
            case 1732514471:
                if (policyName.equals(DISABLE_MANUAL_SCREEN_CAPTURE)) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case 1785346365:
                if (policyName.equals("force-enable-wifi")) {
                    c = '\t';
                    break;
                }
                c = 65535;
                break;
            case 1946452102:
                if (policyName.equals("disable-fingerprint-authentication")) {
                    c = '\b';
                    break;
                }
                c = 65535;
                break;
            case 1981742202:
                if (policyName.equals("force-enable-BT")) {
                    c = '\n';
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
                this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_UPDATESTATE_MANAGER", "does not have system_update_management MDM permission!");
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
                this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_FINGERPRINT", "does not have fingerprint MDM permission!");
                break;
            case '\t':
                this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_WIFI", "does not have wifi MDM permission!");
                break;
            case '\n':
                this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_BLUETOOTH", "does not have bluetooth MDM permission!");
                break;
            case 11:
            case '\f':
                this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_PHONE", "does not have phone MDM permission!");
                break;
            default:
                this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_DEVICE_MANAGER", "does not have device_manager MDM permission!");
                break;
        }
        return true;
    }

    private void forceScheduledPowerOn(Bundle policyData) {
        if (policyData.getBoolean("value", IS_TV)) {
            long whenBoot = policyData.getLong("when", 0);
            String boradcastBoot = policyData.getString("boradcast", DeviceSettingsPlugin.EMPTY_STRING);
            String str = TAG;
            HwLog.i(str, " FORCE_SCHEDULED_POWER_ON boradcast:" + boradcastBoot + ",when:" + whenBoot);
            Intent intent = new Intent(boradcastBoot);
            intent.putExtra(KEY_BOOT_MDM, true);
            this.mBootPendingIntent = PendingIntent.getBroadcast(this.mContext, 0, intent, 0);
            setAlarm(whenBoot, this.mBootPendingIntent);
            return;
        }
        HwLog.i(TAG, " ORCE_SCHEDULED_POWER_ON cancel");
        cancelAlarm(this.mBootPendingIntent);
    }

    private void forceScheduledPowerOff(Bundle policyData) {
        if (policyData.getBoolean("value", IS_TV)) {
            long whenShutdown = policyData.getLong("when", 0);
            String boradcastShutdown = policyData.getString("boradcast", DeviceSettingsPlugin.EMPTY_STRING);
            String str = TAG;
            HwLog.i(str, " FORCE_SCHEDULED_POWER_OFF boradcast:" + boradcastShutdown + ",when:" + whenShutdown);
            this.mShutdownPendingIntent = PendingIntent.getBroadcast(this.mContext, 0, new Intent(boradcastShutdown), 0);
            setAlarm(whenShutdown, this.mShutdownPendingIntent);
            return;
        }
        HwLog.i(TAG, " FORCE_SCHEDULED_POWER_OFF cancel");
        cancelAlarm(this.mShutdownPendingIntent);
    }

    private void disableGoogleAccountAutoSync(Bundle policyData, boolean isEffective) {
        boolean isDisable = policyData.getBoolean("value", IS_TV);
        if (isEffective && isDisable) {
            new Thread(new Runnable() {
                /* class com.android.server.devicepolicy.plugins.DeviceRestrictionPlugin.AnonymousClass1 */

                @Override // java.lang.Runnable
                public void run() {
                    DeviceRestrictionPlugin.this.disableGoogleAccountSyncAutomatically();
                }
            }).start();
        }
    }

    private boolean disableApplicationList(Bundle policyData) {
        List<String> lists = null;
        try {
            lists = policyData.getStringArrayList("value");
        } catch (ArrayIndexOutOfBoundsException e) {
            HwLog.e(TAG, "disableApplicationList exception.");
        }
        if (lists == null) {
            return true;
        }
        PackageManager pm = this.mContext.getPackageManager();
        boolean isSetSucess = true;
        for (String item : lists) {
            isSetSucess = disableComponentForPackage(item, true, pm, 0);
            if (!isSetSucess) {
                HwLog.w(TAG, " disableComponentForPackage fail");
            }
            this.mDisableApplicationsList.add(item);
        }
        return isSetSucess;
    }

    private void disableFloatTask() {
        boolean isEnabled = isFloatTaskRunning();
        storeFloatTaskState(isEnabled);
        if (isEnabled) {
            setFloatTaskEnabled(IS_TV);
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    public boolean onSetPolicy(ComponentName who, String policyName, Bundle policyData, boolean isEffective) {
        boolean z = IS_TV;
        if (policyData == null) {
            HwLog.i(TAG, "onSetPolicy policyData is null");
            return IS_TV;
        }
        HwLog.i(TAG, "onSetPolicy");
        boolean isSetSucess = true;
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
                        c = 7;
                        break;
                    }
                    break;
                case -1616043882:
                    if (policyName.equals("force_scheduled_charge_limit")) {
                        c = '\n';
                        break;
                    }
                    break;
                case -1462770845:
                    if (policyName.equals(DISABLE_APPLICATIONS_LIST)) {
                        c = 2;
                        break;
                    }
                    break;
                case -168307399:
                    if (policyName.equals("disable-charge")) {
                        c = 6;
                        break;
                    }
                    break;
                case 403658447:
                    if (policyName.equals("force_scheduled_power_off")) {
                        c = '\b';
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
                        c = 11;
                        break;
                    }
                    break;
                case 1389850009:
                    if (policyName.equals("disable-google-account-autosync")) {
                        c = 3;
                        break;
                    }
                    break;
                case 1658369855:
                    if (policyName.equals("disable_float_task")) {
                        c = '\t';
                        break;
                    }
                    break;
                case 1785346365:
                    if (policyName.equals("force-enable-wifi")) {
                        c = 4;
                        break;
                    }
                    break;
                case 1981742202:
                    if (policyName.equals("force-enable-BT")) {
                        c = 5;
                        break;
                    }
                    break;
            }
            switch (c) {
                case 0:
                    if (isEffective) {
                        isSetSucess = disableSystemUpdate(policyData.getBoolean("value"));
                        break;
                    }
                    break;
                case 1:
                    if (isEffective) {
                        isSetSucess = disableSdCardUpdate(policyData.getBoolean("value"));
                        break;
                    }
                    break;
                case 2:
                    isSetSucess = disableApplicationList(policyData);
                    break;
                case 3:
                    disableGoogleAccountAutoSync(policyData, isEffective);
                    break;
                case 4:
                    openWifi();
                    break;
                case HwDevicePolicyManagerService.SD_CRYPT_STATE_MISMATCH /* 5 */:
                    openBt();
                    break;
                case HwDevicePolicyManagerService.SD_CRYPT_STATE_WAIT_UNLOCK /* 6 */:
                    if (!policyData.getBoolean("value", IS_TV)) {
                        z = true;
                    }
                    HwPowerManager.setPowerState(z);
                    break;
                case 7:
                    forceScheduledPowerOn(policyData);
                    break;
                case '\b':
                    forceScheduledPowerOff(policyData);
                    break;
                case '\t':
                    disableFloatTask();
                    break;
                case '\n':
                    HwPowerManager.setChargeLimit(policyData.getString("charge_limit", "0"));
                    break;
                case 11:
                    isSetSucess = HwActivityTaskManager.setMultiWindowDisabled(policyData.getBoolean("value"));
                    break;
            }
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
            enableNavbar = "0";
        } else {
            enableNavbar = NAVIGATIONBAR_ENABLE;
        }
        Intent intent = new Intent("com.huawei.navigationbar.statuschange");
        intent.setPackage("com.android.systemui");
        intent.putExtra("minNavigationBar", isNavPolicyData);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        for (UserInfo info : UserManager.get(this.mContext).getUsers()) {
            if (info != null) {
                Settings.System.putStringForUser(this.mContext.getContentResolver(), "enable_navbar", enableNavbar, info.id);
            }
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

    private void removeDisableApplicationList(Bundle policyData) {
        ArrayList<String> lists = null;
        try {
            lists = policyData.getStringArrayList("value");
        } catch (ArrayIndexOutOfBoundsException e) {
            HwLog.e(TAG, "removeDisableApplicationList exception.");
        }
        if (lists != null) {
            PackageManager pm = this.mContext.getPackageManager();
            Iterator<String> it = lists.iterator();
            while (it.hasNext()) {
                disableComponentForPackage(it.next(), IS_TV, pm, 0);
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:19:0x003d A[Catch:{ all -> 0x0053 }] */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x004a A[Catch:{ all -> 0x0053 }] */
    public boolean onRemovePolicy(ComponentName who, String policyName, Bundle policyData, boolean isEffective) {
        boolean z = IS_TV;
        if (policyData == null) {
            HwLog.i(TAG, "onRemovePolicy policyData is null");
            return IS_TV;
        }
        HwLog.i(TAG, "onRemovePolicy");
        long token = Binder.clearCallingIdentity();
        try {
            int hashCode = policyName.hashCode();
            if (hashCode != -1462770845) {
                if (hashCode == 1658369855 && policyName.equals("disable_float_task")) {
                    z = true;
                    if (!z) {
                        removeDisableApplicationList(policyData);
                    } else if (z) {
                        if (isFloatTaskEnableBefore()) {
                            setFloatTaskEnabled(true);
                        }
                    }
                    return true;
                }
            } else if (policyName.equals(DISABLE_APPLICATIONS_LIST)) {
                if (!z) {
                }
                return true;
            }
            z = true;
            if (!z) {
            }
            return true;
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    private void removeDisableApplicationsList(PolicyStruct.PolicyItem policyItem) {
        ArrayList<String> list = null;
        ArrayList<String> arrayList = this.mDisableApplicationsList;
        if (arrayList == null || arrayList.isEmpty()) {
            try {
                list = policyItem.combineAllAttributes().getStringArrayList("value");
            } catch (ArrayIndexOutOfBoundsException e) {
                HwLog.e(TAG, "removeDisableApplicationsList exception.");
            }
        } else {
            list = this.mDisableApplicationsList;
        }
        if (list != null) {
            PackageManager pm = this.mContext.getPackageManager();
            Iterator<String> it = list.iterator();
            while (it.hasNext()) {
                disableComponentForPackage(it.next(), IS_TV, pm, 0);
            }
        }
    }

    private void removeDisableFloatTask() {
        if (isFloatTaskEnableBefore()) {
            setFloatTaskEnabled(true);
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    public boolean onActiveAdminRemoved(ComponentName who, ArrayList<PolicyStruct.PolicyItem> removedPolicies) {
        HwLog.i(TAG, "onActiveAdminRemoved");
        long token = Binder.clearCallingIdentity();
        try {
            Iterator<PolicyStruct.PolicyItem> it = removedPolicies.iterator();
            while (it.hasNext()) {
                PolicyStruct.PolicyItem policyItem = it.next();
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
                        disableSystemUpdate(IS_TV);
                    } else if (c == 1) {
                        disableSdCardUpdate(IS_TV);
                    } else if (c == 2) {
                        removeDisableApplicationsList(policyItem);
                    } else if (c == 3) {
                        removeDisableFloatTask();
                    } else if (c == 4) {
                        HwActivityTaskManager.setMultiWindowDisabled((boolean) IS_TV);
                    }
                }
            }
            return true;
        } finally {
            Binder.restoreCallingIdentity(token);
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
                isBitCurrntValue = IS_TV;
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
        boolean isDisabled = IS_TV;
        PolicyStruct.PolicyItem item = this.mPolicyStruct.getPolicyItem(policyName);
        if (item != null) {
            isDisabled = item.combineAllAttributes().getBoolean("value");
        }
        if (isDisabled) {
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
        String[] readBufs = {"AA"};
        readUpdateStatus(optItem, 4, readBufs, new int[1]);
        try {
            if ((getBitForString(readBufs[0], bitIndex) == 1) == isDisabled) {
                return true;
            }
            return IS_TV;
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("str: " + readBufs[0] + " is invalid.");
        }
    }

    private boolean setBitForOemInfo(String optItem, int bitIndex, boolean isDisabled) {
        String[] readBufs = {"AA"};
        int[] errorRets = new int[1];
        readUpdateStatus(optItem, 4, readBufs, errorRets);
        try {
            String writeValue = disableBitForString(readBufs[0], bitIndex, isDisabled);
            String str = TAG;
            HwLog.i(str, "writeValue: " + writeValue);
            if (HwProtectAreaManager.getInstance().writeProtectArea(optItem, writeValue.length(), writeValue, errorRets) != 0) {
                return IS_TV;
            }
            return true;
        } catch (IllegalArgumentException e) {
            return IS_TV;
        }
    }

    private boolean disableBitForOemInfo(String optItem, int bitIndex, boolean isDisabled) {
        boolean isSuccess = setBitForOemInfo(optItem, bitIndex, isDisabled);
        if (!isSuccess) {
            return IS_TV;
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
            return IS_TV;
        }
        String updateState = isDisabled ? "false" : "true";
        SystemProperties.set(PROP_SYSTEM_UPDATE_STATE, updateState);
        String str = TAG;
        HwLog.i(str, "Set update state prop to " + updateState);
        return true;
    }

    private void disableActivity(PackageInfo packageInfo, String packageName, PackageManager pm, int newState) {
        if (packageInfo.activities != null) {
            boolean isTvSettingsFlag = (!IS_TV || !TV_SETTINGS_PACKAGE_NAME.equals(packageName)) ? IS_TV : true;
            List<LauncherActivityInfo> launcherAppList = ((LauncherApps) this.mContext.getSystemService("launcherapps")).getActivityList(packageName, new UserHandle(UserHandle.myUserId()));
            ActivityInfo[] activityInfoArr = packageInfo.activities;
            for (ActivityInfo item : activityInfoArr) {
                ComponentName componentName = new ComponentName(packageName, item.name);
                for (LauncherActivityInfo launcherApp : launcherAppList) {
                    if (!launcherApp.getComponentName().getClassName().contains(item.name)) {
                        if (!item.name.contains(SETTINGS_FALLBACK_ACTIVITY_NAME)) {
                            if (!item.name.contains(SETTING_CRYPTKEEPER_ACTIVITY_NAME)) {
                                pm.setComponentEnabledSetting(componentName, newState, isTvSettingsFlag ? 1 : 0);
                            }
                        }
                    }
                }
            }
        }
    }

    private void disableComponent(PackageInfo packageInfo, String packageName, PackageManager pm, int newState) {
        boolean isTvSettingsFlag = IS_TV && TV_SETTINGS_PACKAGE_NAME.equals(packageName);
        if (!isTvSettingsFlag && packageInfo.receivers != null) {
            for (ActivityInfo item : packageInfo.receivers) {
                pm.setComponentEnabledSetting(new ComponentName(packageName, item.name), newState, 0);
            }
        }
        if (!isTvSettingsFlag && packageInfo.services != null) {
            for (ServiceInfo item2 : packageInfo.services) {
                pm.setComponentEnabledSetting(new ComponentName(packageName, item2.name), newState, 0);
            }
        }
        if (!isTvSettingsFlag && packageInfo.providers != null) {
            for (ProviderInfo item3 : packageInfo.providers) {
                pm.setComponentEnabledSetting(new ComponentName(packageName, item3.name), newState, 0);
            }
        }
        disableActivity(packageInfo, packageName, pm, newState);
    }

    private boolean disableComponentForPackage(String packageName, boolean isDisabled, PackageManager pm, int userId) {
        int newState;
        boolean isSetSuccess = true;
        boolean isEmpty = TextUtils.isEmpty(packageName);
        boolean isTvSettingsFlag = IS_TV;
        if (isEmpty) {
            return IS_TV;
        }
        if (isDisabled) {
            newState = 2;
        } else {
            newState = 0;
        }
        if (IS_TV && TV_SETTINGS_PACKAGE_NAME.equals(packageName)) {
            isTvSettingsFlag = true;
        }
        try {
            PackageInfo packageInfo = pm.getPackageInfo(packageName, 786959);
            if (packageInfo != null) {
                disableComponent(packageInfo, packageName, pm, newState);
            }
            if (isDisabled) {
                pm.clearPackagePreferredActivities(packageName);
            }
        } catch (PackageManager.NameNotFoundException e) {
            isSetSuccess = IS_TV;
        }
        pm.flushPackageRestrictionsAsUser(userId);
        if (isDisabled && !isTvSettingsFlag) {
            killApplicationInner(packageName);
        }
        return isSetSuccess;
    }

    private void killApplicationInner(String packageName) {
        long ident = Binder.clearCallingIdentity();
        try {
            List<ActivityManager.RunningTaskInfo> taskList = ((ActivityManager) this.mContext.getSystemService("activity")).getRunningTasks(ActivityManager.getMaxRecentTasksStatic());
            if (taskList != null) {
                if (!taskList.isEmpty()) {
                    for (ActivityManager.RunningTaskInfo ti : taskList) {
                        ComponentName baseActivity = ti.baseActivity;
                        if (baseActivity != null && TextUtils.equals(baseActivity.getPackageName(), packageName)) {
                            ActivityManager.getService().removeTask(ti.id);
                        }
                    }
                    Binder.restoreCallingIdentity(ident);
                    return;
                }
            }
            Binder.restoreCallingIdentity(ident);
        } catch (RemoteException e) {
            HwLog.d(TAG, "killApplicationInner pkg RemoteException");
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(ident);
            throw th;
        }
    }

    /* access modifiers changed from: package-private */
    public IWindowManager getWindowManager() {
        return IWindowManager.Stub.asInterface(ServiceManager.getService("window"));
    }

    private boolean updateScreenCaptureDisabledInWindowManager(int userHandle) {
        try {
            getWindowManager().refreshScreenCaptureDisabled(userHandle);
            return true;
        } catch (RemoteException e) {
            HwLog.e(TAG, "Unable to notify WindowManager.");
            return IS_TV;
        }
    }

    private void disableEveryItem(Account account, SyncAdapterType adapter) {
        String str = TAG;
        HwLog.i(str, "getCurrentSyncs, type=" + adapter.accountType + ", authority=" + adapter.authority + ", visible=" + adapter.isUserVisible());
        if (GOOGLE_ACCOUNT_TYPE.equals(adapter.accountType) && adapter.isUserVisible() && ContentResolver.getSyncAutomatically(account, adapter.authority)) {
            String str2 = TAG;
            HwLog.i(str2, "setSyncAutomatically to false for google account authority: " + adapter.authority);
            ContentResolver.setSyncAutomatically(account, adapter.authority, IS_TV);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void disableGoogleAccountSyncAutomatically() {
        long identityToken = Binder.clearCallingIdentity();
        try {
            SyncAdapterType[] syncs = ContentResolver.getSyncAdapterTypes();
            Account[] accounts = AccountManager.get(this.mContext).getAccountsByType(GOOGLE_ACCOUNT_TYPE);
            HwLog.i(TAG, "get google accounts, size=" + accounts.length);
            int length = accounts.length;
            for (int i = 0; i < length; i++) {
                Account account = accounts[i];
                for (SyncAdapterType adapter : syncs) {
                    disableEveryItem(account, adapter);
                }
            }
        } finally {
            Binder.restoreCallingIdentity(identityToken);
        }
    }

    private void setFloatTaskEnabled(boolean isEnable) {
        Settings.Secure.putIntForUser(this.mContext.getContentResolver(), FLOAT_TASK_STATE, isEnable ? 1 : 0, UserHandle.myUserId());
    }

    private boolean isFloatTaskRunning() {
        if (Settings.Secure.getIntForUser(this.mContext.getContentResolver(), FLOAT_TASK_STATE, 0, UserHandle.myUserId()) == 1) {
            return true;
        }
        return IS_TV;
    }

    private boolean isFloatTaskEnableBefore() {
        if (Settings.System.getInt(this.mContext.getContentResolver(), "float_task_state_before", 0) == 1) {
            return true;
        }
        return IS_TV;
    }

    private void storeFloatTaskState(boolean isEnable) {
        Settings.System.putInt(this.mContext.getContentResolver(), "float_task_state_before", isEnable ? 1 : 0);
    }
}

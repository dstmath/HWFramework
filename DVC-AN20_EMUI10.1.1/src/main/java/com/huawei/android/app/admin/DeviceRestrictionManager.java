package com.huawei.android.app.admin;

import android.app.ActivityThread;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.UserHandle;
import com.huawei.android.media.AudioManagerEx;
import huawei.android.app.admin.HwDevicePolicyManagerEx;
import java.util.ArrayList;

public class DeviceRestrictionManager {
    public static final String DISABLE_ALARM = "disable_alarm";
    private static final String DISABLE_APPLICATIONS_LIST = "disable-applications-list";
    public static final String DISABLE_CHANGE_WALLPAPER = "disable-change-wallpaper";
    public static final String DISABLE_CHARGING = "disable-charge";
    public static final String DISABLE_FINGERPRINT_AUTHENTICATION = "disable-fingerprint-authentication";
    public static final String DISABLE_FLOAT_TASK = "disable_float_task";
    private static final String DISABLE_GOOGLE_PLAY_STORE = "disable-google-play-store";
    private static final String DISABLE_HEADPHONE = "disable-headphone";
    private static final String DISABLE_MANUAL_SCREEN_CAPTURE = "disable-manual-screen-capture";
    private static final String DISABLE_MICROPHONE = "disable-microphone";
    public static final String DISABLE_POWER_SHUTDOWN = "disable-power-shutdown";
    private static final String DISABLE_SCREEN_CAPTURE = "disable-screen-capture";
    private static final String DISABLE_SCREEN_TURN_OFF = "disable-screen-turn-off";
    public static final String DISABLE_SEND_NOTIFICATION = "disable-send-notification";
    private static final String DISABLE_SETTINGS = "disable-settings";
    public static final String DISABLE_SHUTDOWNMENU = "disable-shutdownmenu";
    public static final String DISABLE_STATUS_BAR = "disable_status_bar";
    private static final String DISABLE_SYSTEM_BROWSER = "disable-system-browser";
    private static final String DISABLE_SYSTEM_UPDATE = "disable-system-update";
    private static final String DISABLE_SYSTEM_UPDATE_SD = "disable-system-update-sd";
    public static final String DISABLE_VOICE_INCOMING = "disable_voice_incoming";
    public static final String DISABLE_VOICE_OUTGOING = "disable_voice_outgoing";
    public static final String DISABLE_VOLUME = "disable-volume";
    public static final String FORCE_ENABLE_BT = "force-enable-BT";
    public static final String FORCE_ENABLE_WIFI = "force-enable-wifi";
    public static final String FORCE_SCHEDULED_CHARGE_LIMIT = "force_scheduled_charge_limit";
    public static final String FORCE_SCHEDULED_POWER_OFF = "force_scheduled_power_off";
    public static final String FORCE_SCHEDULED_POWER_ON = "force_scheduled_power_on";
    private static final String GOOGLE_STORE_PACKAGE_NAME = "com.android.vending";
    public static final String POLICY_DEPRECATED_ADMIN_INTERFACES_ENABLED = "policy-deprecated-admin-interfaces-enabled";
    private static final String POLICY_DISABLE_MULTIWINDOW = "disable-multi-window";
    public static final String POLICY_FILE_SHARE = "policy-file-share-disabled";
    private static final String SETTINGS_PACKAGE_NAME = "com.android.settings";
    public static final String STATUS_DISABLE_CLIPBOARD = "disable-clipboard";
    public static final String STATUS_DISABLE_GOOGLE_ACCOUNT_AUTOSYNC = "disable-google-account-autosync";
    public static final String STATUS_KEY = "value";
    private static final String TAG = "DeviceRestrictionManager";
    private static final String YOUTUBE_PACKAGE_NAME = "com.google.android.youtube";
    private final HwDevicePolicyManagerEx mDpm = new HwDevicePolicyManagerEx();

    public boolean forceEnableWifi(ComponentName admin, boolean open) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", open);
        return this.mDpm.setPolicy(admin, FORCE_ENABLE_WIFI, bundle);
    }

    public boolean isForceEnableWifi(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, FORCE_ENABLE_WIFI);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return false;
    }

    public boolean forceEnableBluetooth(ComponentName admin, boolean open) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", open);
        return this.mDpm.setPolicy(admin, FORCE_ENABLE_BT, bundle);
    }

    public boolean isForceEnableBluetooth(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, FORCE_ENABLE_BT);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return false;
    }

    public void setWifiDisabled(ComponentName admin, boolean disabled) {
        this.mDpm.setWifiDisabled(admin, disabled);
    }

    public boolean isWifiDisabled(ComponentName admin) {
        return this.mDpm.isWifiDisabled(admin);
    }

    public void setBluetoothDisabled(ComponentName admin, boolean disabled) {
        this.mDpm.setBluetoothDisabled(admin, disabled);
    }

    public boolean isBluetoothDisabled(ComponentName admin) {
        return this.mDpm.isBluetoothDisabled(admin);
    }

    public void setWifiApDisabled(ComponentName admin, boolean disabled) {
        this.mDpm.setWifiApDisabled(admin, disabled);
    }

    public boolean isWifiApDisabled(ComponentName admin) {
        return this.mDpm.isWifiApDisabled(admin);
    }

    public void setUSBDataDisabled(ComponentName admin, boolean disabled) {
        this.mDpm.setUSBDataDisabled(admin, disabled);
    }

    public boolean isUSBDataDisabled(ComponentName admin) {
        return this.mDpm.isUSBDataDisabled(admin);
    }

    public void setExternalStorageDisabled(ComponentName admin, boolean disabled) {
        this.mDpm.setExternalStorageDisabled(admin, disabled);
    }

    public boolean isExternalStorageDisabled(ComponentName admin) {
        return this.mDpm.isExternalStorageDisabled(admin);
    }

    public void setNFCDisabled(ComponentName admin, boolean disabled) {
        this.mDpm.setNFCDisabled(admin, disabled);
    }

    public boolean isNFCDisabled(ComponentName admin) {
        return this.mDpm.isNFCDisabled(admin);
    }

    public void setDataConnectivityDisabled(ComponentName admin, boolean disabled) {
        this.mDpm.setDataConnectivityDisabled(admin, disabled);
    }

    public boolean isDataConnectivityDisabled(ComponentName admin) {
        return this.mDpm.isDataConnectivityDisabled(admin);
    }

    public void setVoiceDisabled(ComponentName admin, boolean disabled) {
        this.mDpm.setVoiceDisabled(admin, disabled);
    }

    public boolean isVoiceDisabled(ComponentName admin) {
        return this.mDpm.isVoiceDisabled(admin);
    }

    public void setSMSDisabled(ComponentName admin, boolean disabled) {
        this.mDpm.setSMSDisabled(admin, disabled);
    }

    public boolean isSMSDisabled(ComponentName admin) {
        return this.mDpm.isSMSDisabled(admin);
    }

    public void setStatusBarExpandPanelDisabled(ComponentName admin, boolean disabled) {
        this.mDpm.setStatusBarExpandPanelDisabled(admin, disabled);
    }

    public boolean isStatusBarExpandPanelDisabled(ComponentName admin) {
        return this.mDpm.isStatusBarExpandPanelDisabled(admin);
    }

    public void setSafeModeDisabled(ComponentName admin, boolean disabled) {
        this.mDpm.setSafeModeDisabled(admin, disabled);
    }

    public boolean isSafeModeDisabled(ComponentName admin) {
        return this.mDpm.isSafeModeDisabled(admin);
    }

    public void setAdbDisabled(ComponentName admin, boolean disabled) {
        this.mDpm.setAdbDisabled(admin, disabled);
    }

    public boolean isAdbDisabled(ComponentName admin) {
        return this.mDpm.isAdbDisabled(admin);
    }

    public void setUSBOtgDisabled(ComponentName admin, boolean disabled) {
        this.mDpm.setUSBOtgDisabled(admin, disabled);
    }

    public boolean isUSBOtgDisabled(ComponentName admin) {
        return this.mDpm.isUSBOtgDisabled(admin);
    }

    public void setGPSDisabled(ComponentName admin, boolean disabled) {
        this.mDpm.setGPSDisabled(admin, disabled);
    }

    public boolean isGPSDisabled(ComponentName admin) {
        return this.mDpm.isGPSDisabled(admin);
    }

    public void setHomeButtonDisabled(ComponentName admin, boolean disabled) {
        this.mDpm.setHomeButtonDisabled(admin, disabled);
    }

    public boolean isHomeButtonDisabled(ComponentName admin) {
        return this.mDpm.isHomeButtonDisabled(admin);
    }

    public void setTaskButtonDisabled(ComponentName admin, boolean disabled) {
        this.mDpm.setTaskButtonDisabled(admin, disabled);
    }

    public boolean isTaskButtonDisabled(ComponentName admin) {
        return this.mDpm.isTaskButtonDisabled(admin);
    }

    public void setBackButtonDisabled(ComponentName admin, boolean disabled) {
        this.mDpm.setBackButtonDisabled(admin, disabled);
    }

    public boolean isBackButtonDisabled(ComponentName admin) {
        return this.mDpm.isBackButtonDisabled(admin);
    }

    public boolean setScreenCaptureDisabled(ComponentName admin, boolean disable) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", disable);
        return this.mDpm.setPolicy(admin, DISABLE_SCREEN_CAPTURE, bundle);
    }

    public boolean isScreenCaptureDisabled(ComponentName admin) {
        ActivityThread activityThread;
        Context context;
        DevicePolicyManager dpm;
        Bundle bundle = this.mDpm.getPolicy(admin, DISABLE_SCREEN_CAPTURE);
        if (bundle == null) {
            return false;
        }
        boolean isDisabled = bundle.getBoolean("value");
        if (admin != null || isDisabled || (activityThread = ActivityThread.currentActivityThread()) == null || (context = activityThread.getSystemContext()) == null || (dpm = (DevicePolicyManager) context.getSystemService("device_policy")) == null) {
            return isDisabled;
        }
        return dpm.getScreenCaptureDisabled(null);
    }

    public boolean setManualScreenCaptureDisabled(ComponentName admin, boolean disabled) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", disabled);
        return this.mDpm.setPolicy(admin, DISABLE_MANUAL_SCREEN_CAPTURE, bundle);
    }

    public boolean isManualScreenCaptureDisabled(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, DISABLE_MANUAL_SCREEN_CAPTURE);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return false;
    }

    public boolean setSystemBrowserDisabled(ComponentName admin, boolean disable) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", disable);
        Bundle bundle1 = new Bundle();
        bundle1.putStringArrayList("value", resolveSystemBrowserApps());
        if (disable) {
            this.mDpm.setPolicy(admin, DISABLE_APPLICATIONS_LIST, bundle1);
        } else {
            this.mDpm.removePolicy(admin, DISABLE_APPLICATIONS_LIST, bundle1);
        }
        return this.mDpm.setPolicy(admin, DISABLE_SYSTEM_BROWSER, bundle);
    }

    public boolean isSystemBrowserDisabled(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, DISABLE_SYSTEM_BROWSER);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return false;
    }

    public boolean setSettingsApplicationDisabled(ComponentName admin, boolean disable) {
        Bundle bundle = new Bundle();
        ArrayList<String> packageNames = new ArrayList<>();
        packageNames.add("com.android.settings");
        bundle.putStringArrayList("value", packageNames);
        if (disable) {
            return this.mDpm.setPolicy(admin, DISABLE_APPLICATIONS_LIST, bundle);
        }
        return this.mDpm.removePolicy(admin, DISABLE_APPLICATIONS_LIST, bundle);
    }

    public boolean isSettingsApplicationDisabled(ComponentName admin) {
        return isApplicationDisabled(admin, "com.android.settings");
    }

    public boolean setGooglePlayStoreDisabled(ComponentName admin, boolean disable) {
        Bundle bundle = new Bundle();
        ArrayList<String> packageNames = new ArrayList<>();
        packageNames.add(GOOGLE_STORE_PACKAGE_NAME);
        bundle.putStringArrayList("value", packageNames);
        if (disable) {
            return this.mDpm.setPolicy(admin, DISABLE_APPLICATIONS_LIST, bundle);
        }
        return this.mDpm.removePolicy(admin, DISABLE_APPLICATIONS_LIST, bundle);
    }

    public boolean isGooglePlayStoreDisabled(ComponentName admin) {
        return isApplicationDisabled(admin, GOOGLE_STORE_PACKAGE_NAME);
    }

    public boolean isApplicationDisabled(ComponentName admin, String packageName) {
        ArrayList<String> list;
        Bundle bundle = this.mDpm.getPolicy(admin, DISABLE_APPLICATIONS_LIST);
        if (bundle == null || (list = bundle.getStringArrayList("value")) == null || list.size() == 0 || !list.contains(packageName)) {
            return false;
        }
        return true;
    }

    private ArrayList<String> resolveSystemBrowserApps() {
        Context context;
        ArrayList<String> systemBrowserList = new ArrayList<>();
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.BROWSABLE");
        intent.setData(Uri.parse("http:"));
        ActivityThread activityThread = ActivityThread.currentActivityThread();
        if (!(activityThread == null || (context = activityThread.getSystemContext()) == null)) {
            for (ResolveInfo info : context.getPackageManager().queryIntentActivitiesAsUser(intent, 1049088, UserHandle.getCallingUserId())) {
                if (info.activityInfo != null && !systemBrowserList.contains(info.activityInfo.packageName) && info.handleAllWebDataURI) {
                    systemBrowserList.add(info.activityInfo.packageName);
                }
            }
        }
        return systemBrowserList;
    }

    public void setGoogleAccountDisabled(ComponentName admin, boolean disabled) {
        this.mDpm.setAccountDisabled(admin, "com.google", disabled);
    }

    public boolean isGoogleAccountDisabled(ComponentName admin) {
        return this.mDpm.isAccountDisabled(admin, "com.google");
    }

    public boolean setMicrophoneDisabled(ComponentName admin, boolean disabled) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", disabled);
        return this.mDpm.setPolicy(admin, DISABLE_MICROPHONE, bundle);
    }

    public boolean isMicrophoneDisabled(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, DISABLE_MICROPHONE);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return false;
    }

    public boolean setClipboardDisabled(ComponentName admin, boolean disable) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", disable);
        return this.mDpm.setPolicy(admin, STATUS_DISABLE_CLIPBOARD, bundle);
    }

    public boolean isClipboardDisabled(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, STATUS_DISABLE_CLIPBOARD);
        if (bundle == null) {
            return false;
        }
        return bundle.getBoolean("value");
    }

    public boolean setGoogleAccountAutoSyncDisabled(ComponentName admin, boolean disable) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", disable);
        return this.mDpm.setPolicy(admin, STATUS_DISABLE_GOOGLE_ACCOUNT_AUTOSYNC, bundle);
    }

    public boolean isGoogleAccountAutoSyncDisabled(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, STATUS_DISABLE_GOOGLE_ACCOUNT_AUTOSYNC);
        if (bundle == null) {
            return false;
        }
        return bundle.getBoolean("value");
    }

    public boolean setSystemUpdateDisabled(ComponentName who, boolean disable) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", disable);
        return this.mDpm.setPolicy(who, DISABLE_SYSTEM_UPDATE, bundle);
    }

    public boolean isSystemUpdateDisabled(ComponentName who) {
        Bundle bundle = this.mDpm.getPolicy(who, DISABLE_SYSTEM_UPDATE);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return false;
    }

    public boolean setSdCardUpdateDisabled(ComponentName admin, boolean disabled) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", disabled);
        return this.mDpm.setPolicy(admin, DISABLE_SYSTEM_UPDATE_SD, bundle);
    }

    public boolean isSdCardUpdateDisabled(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, DISABLE_SYSTEM_UPDATE_SD);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return false;
    }

    public boolean setHeadphoneDisabled(ComponentName admin, boolean disable) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", disable);
        boolean result = this.mDpm.setPolicy(admin, DISABLE_HEADPHONE, bundle);
        if (result) {
            AudioManagerEx.disableHeadPhone(disable);
        }
        return result;
    }

    public boolean isHeadphoneDisabled(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, DISABLE_HEADPHONE);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return false;
    }

    public boolean setSendNotificationDisabled(ComponentName admin, boolean disable) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", disable);
        return this.mDpm.setPolicy(admin, DISABLE_SEND_NOTIFICATION, bundle);
    }

    public boolean isSendNotificationDisabled(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, DISABLE_SEND_NOTIFICATION);
        if (bundle == null) {
            return false;
        }
        return bundle.getBoolean("value");
    }

    public boolean setVolumeAdjustDisabled(ComponentName admin, boolean disable) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", disable);
        return this.mDpm.setPolicy(admin, DISABLE_VOLUME, bundle);
    }

    public boolean isVolumeAdjustDisabled(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, DISABLE_VOLUME);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return false;
    }

    public boolean setPowerDisabled(ComponentName admin, boolean disable) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", disable);
        return this.mDpm.setPolicy(admin, DISABLE_POWER_SHUTDOWN, bundle);
    }

    public boolean isPowerDisabled(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, DISABLE_POWER_SHUTDOWN);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return false;
    }

    public boolean setShutdownMenuDisabled(ComponentName admin, boolean disable) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", disable);
        return this.mDpm.setPolicy(admin, DISABLE_SHUTDOWNMENU, bundle);
    }

    public boolean isShutdownMenuDisable(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, DISABLE_SHUTDOWNMENU);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return false;
    }

    public boolean setYoutubeDisabled(ComponentName admin, boolean disable) {
        Bundle bundle = new Bundle();
        ArrayList<String> packageNames = new ArrayList<>();
        packageNames.add(YOUTUBE_PACKAGE_NAME);
        bundle.putStringArrayList("value", packageNames);
        if (disable) {
            return this.mDpm.setPolicy(admin, DISABLE_APPLICATIONS_LIST, bundle);
        }
        return this.mDpm.removePolicy(admin, DISABLE_APPLICATIONS_LIST, bundle);
    }

    public boolean isYoutubeDisabled(ComponentName admin) {
        return isApplicationDisabled(admin, YOUTUBE_PACKAGE_NAME);
    }

    public boolean setChangeWallpaperDisabled(ComponentName admin, boolean disable) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", disable);
        return this.mDpm.setPolicy(admin, DISABLE_CHANGE_WALLPAPER, bundle);
    }

    public boolean isChangeWallpaperDisabled(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, DISABLE_CHANGE_WALLPAPER);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return false;
    }

    public boolean setVoiceOutgoingDisabled(ComponentName admin, boolean disable) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", disable);
        if (disable) {
            return this.mDpm.setPolicy(admin, DISABLE_VOICE_OUTGOING, bundle);
        }
        return this.mDpm.removePolicy(admin, DISABLE_VOICE_OUTGOING, bundle);
    }

    public boolean isVoiceOutgoingDisabled(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, DISABLE_VOICE_OUTGOING);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return false;
    }

    public boolean setVoiceIncomingDisabled(ComponentName admin, boolean disable) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", disable);
        if (disable) {
            return this.mDpm.setPolicy(admin, DISABLE_VOICE_INCOMING, bundle);
        }
        return this.mDpm.removePolicy(admin, DISABLE_VOICE_INCOMING, bundle);
    }

    public boolean isVoiceIncomingDisabled(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, DISABLE_VOICE_INCOMING);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return false;
    }

    public boolean setFingerprintAuthenticationDisabled(ComponentName admin, boolean disable) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", disable);
        return this.mDpm.setPolicy(admin, DISABLE_FINGERPRINT_AUTHENTICATION, bundle);
    }

    public boolean isFingerprintAuthenticationDisabled(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, DISABLE_FINGERPRINT_AUTHENTICATION);
        if (bundle == null) {
            return false;
        }
        return bundle.getBoolean("value");
    }

    public boolean setChargingDisabled(ComponentName admin, boolean disable) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", disable);
        return this.mDpm.setPolicy(admin, DISABLE_CHARGING, bundle);
    }

    public boolean isChargingDisabled(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, DISABLE_CHARGING);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return false;
    }

    public boolean setScheduledPowerOn(ComponentName admin, boolean set, String boradcast, long when) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", set);
        if (set) {
            bundle.putString("boradcast", boradcast);
            bundle.putLong("when", when);
        }
        return this.mDpm.setPolicy(admin, FORCE_SCHEDULED_POWER_ON, bundle);
    }

    public boolean isScheduledPowerOn(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, FORCE_SCHEDULED_POWER_ON);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return false;
    }

    public boolean setScheduledPowerOff(ComponentName admin, boolean set, String boradcast, long when) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", set);
        if (set) {
            bundle.putString("boradcast", boradcast);
            bundle.putLong("when", when);
        }
        return this.mDpm.setPolicy(admin, FORCE_SCHEDULED_POWER_OFF, bundle);
    }

    public boolean isScheduledPowerOff(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, FORCE_SCHEDULED_POWER_OFF);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return false;
    }

    public boolean setFloatTaskDisabled(ComponentName admin, boolean disable) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", disable);
        if (disable) {
            return this.mDpm.setPolicy(admin, DISABLE_FLOAT_TASK, bundle);
        }
        return this.mDpm.removePolicy(admin, DISABLE_FLOAT_TASK, bundle);
    }

    public boolean isFloatTaskDisabled(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, DISABLE_FLOAT_TASK);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return false;
    }

    public boolean isFileShareDisabled(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, POLICY_FILE_SHARE);
        if (bundle == null) {
            return false;
        }
        return bundle.getBoolean("value");
    }

    public boolean setFileShareDisabled(ComponentName admin, boolean disabled) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", disabled);
        return this.mDpm.setPolicy(admin, POLICY_FILE_SHARE, bundle);
    }

    public boolean setChargeLimit(ComponentName admin, String limitValue) {
        Bundle bundle = new Bundle();
        bundle.putString("charge_limit", limitValue);
        return this.mDpm.setPolicy(admin, FORCE_SCHEDULED_CHARGE_LIMIT, bundle);
    }

    public boolean setAlarmDisabled(ComponentName admin, boolean disabled) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", disabled);
        if (disabled) {
            return this.mDpm.setPolicy(admin, DISABLE_ALARM, bundle);
        }
        return this.mDpm.removePolicy(admin, DISABLE_ALARM, bundle);
    }

    public boolean isAlarmDisabled(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, DISABLE_ALARM);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return false;
    }

    public boolean setDeprecatedAdminInterfacesEnabled(ComponentName admin, boolean enabled) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", enabled);
        if (enabled) {
            return this.mDpm.setPolicy(admin, POLICY_DEPRECATED_ADMIN_INTERFACES_ENABLED, bundle);
        }
        return this.mDpm.removePolicy(admin, POLICY_DEPRECATED_ADMIN_INTERFACES_ENABLED, bundle);
    }

    public boolean isDeprecatedAdminInterfacesEnabled(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, POLICY_DEPRECATED_ADMIN_INTERFACES_ENABLED);
        if (bundle == null) {
            return false;
        }
        return bundle.getBoolean("value");
    }

    public boolean setMultiWindowDisabled(ComponentName admin, boolean disabled) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", disabled);
        return this.mDpm.setPolicy(admin, POLICY_DISABLE_MULTIWINDOW, bundle);
    }

    public boolean isMultiWindowDisabled(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, POLICY_DISABLE_MULTIWINDOW);
        if (bundle == null) {
            return false;
        }
        return bundle.getBoolean("value");
    }

    public boolean setSleepByPowerButtonDisabled(ComponentName admin, boolean disabled) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", disabled);
        if (disabled) {
            return this.mDpm.setPolicy(admin, DISABLE_SCREEN_TURN_OFF, bundle);
        }
        return this.mDpm.removePolicy(admin, DISABLE_SCREEN_TURN_OFF, bundle);
    }

    public boolean isSleepByPowerButtonDisabled(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, DISABLE_SCREEN_TURN_OFF);
        if (bundle == null) {
            return false;
        }
        return bundle.getBoolean("value");
    }

    public boolean setStatusBarDisabled(ComponentName admin, boolean disabled) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", disabled);
        if (disabled) {
            return this.mDpm.setPolicy(admin, DISABLE_STATUS_BAR, bundle);
        }
        return this.mDpm.removePolicy(admin, DISABLE_STATUS_BAR, bundle);
    }

    public boolean isStatusBarDisabled(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, DISABLE_STATUS_BAR);
        if (bundle == null) {
            return false;
        }
        return bundle.getBoolean("value");
    }
}

package com.huawei.android.app.admin;

import android.app.ActivityThread;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.Log;
import com.huawei.android.media.AudioManagerEx;
import huawei.android.app.admin.HwDevicePolicyManagerEx;
import java.util.ArrayList;
import java.util.List;

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
    public static final String DISABLE_VOICE_ASSISTANT_BUTTON = "disable-voice-assistant-button";
    public static final String DISABLE_VOICE_INCOMING = "disable_voice_incoming";
    public static final String DISABLE_VOICE_OUTGOING = "disable_voice_outgoing";
    public static final String DISABLE_VOLUME = "disable-volume";
    public static final String FORCE_ENABLE_BT = "force-enable-BT";
    public static final String FORCE_ENABLE_WIFI = "force-enable-wifi";
    public static final String FORCE_SCHEDULED_CHARGE_LIMIT = "force_scheduled_charge_limit";
    public static final String FORCE_SCHEDULED_POWER_OFF = "force_scheduled_power_off";
    public static final String FORCE_SCHEDULED_POWER_ON = "force_scheduled_power_on";
    private static final String GOOGLE_STORE_PACKAGE_NAME = "com.android.vending";
    private static final boolean IS_TV = "tv".equals(SystemProperties.get("ro.build.characteristics", "default"));
    public static final String POLICY_DEPRECATED_ADMIN_INTERFACES_ENABLED = "policy-deprecated-admin-interfaces-enabled";
    private static final String POLICY_DISABLE_MULTIWINDOW = "disable-multi-window";
    public static final String POLICY_FILE_SHARE = "policy-file-share-disabled";
    private static final String SETTINGS_PACKAGE_NAME = "com.android.settings";
    public static final String STATUS_DISABLE_CLIPBOARD = "disable-clipboard";
    public static final String STATUS_DISABLE_GOOGLE_ACCOUNT_AUTOSYNC = "disable-google-account-autosync";
    public static final String STATUS_KEY = "value";
    private static final String TAG = "DeviceRestrictionManager";
    private static final String TV_SETTINGS_PACKAGE_NAME = "com.huawei.homevision.settings";
    private static final String YOUTUBE_PACKAGE_NAME = "com.google.android.youtube";
    private final HwDevicePolicyManagerEx mDpm = new HwDevicePolicyManagerEx();

    public boolean forceEnableWifi(ComponentName admin, boolean isEnabled) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", isEnabled);
        return this.mDpm.setPolicy(admin, FORCE_ENABLE_WIFI, bundle);
    }

    public boolean isForceEnableWifi(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, FORCE_ENABLE_WIFI);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return IS_TV;
    }

    public boolean forceEnableBluetooth(ComponentName admin, boolean isEnabled) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", isEnabled);
        return this.mDpm.setPolicy(admin, FORCE_ENABLE_BT, bundle);
    }

    public boolean isForceEnableBluetooth(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, FORCE_ENABLE_BT);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return IS_TV;
    }

    public void setWifiDisabled(ComponentName admin, boolean isDisabled) {
        this.mDpm.setWifiDisabled(admin, isDisabled);
    }

    public boolean isWifiDisabled(ComponentName admin) {
        return this.mDpm.isWifiDisabled(admin);
    }

    public void setBluetoothDisabled(ComponentName admin, boolean isDisabled) {
        this.mDpm.setBluetoothDisabled(admin, isDisabled);
    }

    public boolean isBluetoothDisabled(ComponentName admin) {
        return this.mDpm.isBluetoothDisabled(admin);
    }

    public void setWifiApDisabled(ComponentName admin, boolean isDisabled) {
        this.mDpm.setWifiApDisabled(admin, isDisabled);
    }

    public boolean isWifiApDisabled(ComponentName admin) {
        return this.mDpm.isWifiApDisabled(admin);
    }

    public void setUSBDataDisabled(ComponentName admin, boolean isDisabled) {
        this.mDpm.setUsbDataDisabled(admin, isDisabled);
    }

    public boolean isUSBDataDisabled(ComponentName admin) {
        return this.mDpm.isUsbDataDisabled(admin);
    }

    public void setExternalStorageDisabled(ComponentName admin, boolean isDisabled) {
        this.mDpm.setExternalStorageDisabled(admin, isDisabled);
    }

    public boolean isExternalStorageDisabled(ComponentName admin) {
        return this.mDpm.isExternalStorageDisabled(admin);
    }

    public void setNFCDisabled(ComponentName admin, boolean isDisabled) {
        this.mDpm.setNfcDisabled(admin, isDisabled);
    }

    public boolean isNFCDisabled(ComponentName admin) {
        return this.mDpm.isNfcDisabled(admin);
    }

    public void setDataConnectivityDisabled(ComponentName admin, boolean isDisabled) {
        this.mDpm.setDataConnectivityDisabled(admin, isDisabled);
    }

    public boolean isDataConnectivityDisabled(ComponentName admin) {
        return this.mDpm.isDataConnectivityDisabled(admin);
    }

    public void setVoiceDisabled(ComponentName admin, boolean isDisabled) {
        this.mDpm.setVoiceDisabled(admin, isDisabled);
    }

    public boolean isVoiceDisabled(ComponentName admin) {
        return this.mDpm.isVoiceDisabled(admin);
    }

    public void setSMSDisabled(ComponentName admin, boolean isDisabled) {
        this.mDpm.setSmsDisabled(admin, isDisabled);
    }

    public boolean isSMSDisabled(ComponentName admin) {
        return this.mDpm.isSmsDisabled(admin);
    }

    public void setStatusBarExpandPanelDisabled(ComponentName admin, boolean isDisabled) {
        this.mDpm.setStatusBarExpandPanelDisabled(admin, isDisabled);
    }

    public boolean isStatusBarExpandPanelDisabled(ComponentName admin) {
        return this.mDpm.isStatusBarExpandPanelDisabled(admin);
    }

    public void setSafeModeDisabled(ComponentName admin, boolean isDisabled) {
        this.mDpm.setSafeModeDisabled(admin, isDisabled);
    }

    public boolean isSafeModeDisabled(ComponentName admin) {
        return this.mDpm.isSafeModeDisabled(admin);
    }

    public void setAdbDisabled(ComponentName admin, boolean isDisabled) {
        this.mDpm.setAdbDisabled(admin, isDisabled);
    }

    public boolean isAdbDisabled(ComponentName admin) {
        return this.mDpm.isAdbDisabled(admin);
    }

    public void setUSBOtgDisabled(ComponentName admin, boolean isDisabled) {
        this.mDpm.setUsbOtgDisabled(admin, isDisabled);
    }

    public boolean isUSBOtgDisabled(ComponentName admin) {
        return this.mDpm.isUsbOtgDisabled(admin);
    }

    public void setGPSDisabled(ComponentName admin, boolean isDisabled) {
        this.mDpm.setGpsDisabled(admin, isDisabled);
    }

    public boolean isGPSDisabled(ComponentName admin) {
        return this.mDpm.isGpsDisabled(admin);
    }

    public void setHomeButtonDisabled(ComponentName admin, boolean isDisabled) {
        this.mDpm.setHomeButtonDisabled(admin, isDisabled);
    }

    public boolean isHomeButtonDisabled(ComponentName admin) {
        return this.mDpm.isHomeButtonDisabled(admin);
    }

    public void setTaskButtonDisabled(ComponentName admin, boolean isDisabled) {
        this.mDpm.setTaskButtonDisabled(admin, isDisabled);
    }

    public boolean isTaskButtonDisabled(ComponentName admin) {
        return this.mDpm.isTaskButtonDisabled(admin);
    }

    public void setBackButtonDisabled(ComponentName admin, boolean isDisabled) {
        this.mDpm.setBackButtonDisabled(admin, isDisabled);
    }

    public boolean isBackButtonDisabled(ComponentName admin) {
        return this.mDpm.isBackButtonDisabled(admin);
    }

    public boolean setScreenCaptureDisabled(ComponentName admin, boolean isDisabled) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", isDisabled);
        return this.mDpm.setPolicy(admin, DISABLE_SCREEN_CAPTURE, bundle);
    }

    public boolean isScreenCaptureDisabled(ComponentName admin) {
        ActivityThread activityThread;
        Context context;
        DevicePolicyManager dpm;
        Bundle bundle = this.mDpm.getPolicy(admin, DISABLE_SCREEN_CAPTURE);
        if (bundle == null) {
            return IS_TV;
        }
        boolean isDisabled = bundle.getBoolean("value");
        if (admin != null || isDisabled || (activityThread = ActivityThread.currentActivityThread()) == null || (context = activityThread.getSystemContext()) == null || (dpm = (DevicePolicyManager) context.getSystemService("device_policy")) == null) {
            return isDisabled;
        }
        return dpm.getScreenCaptureDisabled(null);
    }

    public boolean setManualScreenCaptureDisabled(ComponentName admin, boolean isDisabled) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", isDisabled);
        return this.mDpm.setPolicy(admin, DISABLE_MANUAL_SCREEN_CAPTURE, bundle);
    }

    public boolean isManualScreenCaptureDisabled(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, DISABLE_MANUAL_SCREEN_CAPTURE);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return IS_TV;
    }

    public boolean setSystemBrowserDisabled(ComponentName admin, boolean isDisabled) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", isDisabled);
        Bundle bundle1 = new Bundle();
        bundle1.putStringArrayList("value", resolveSystemBrowserApps());
        if (isDisabled) {
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
        return IS_TV;
    }

    public boolean setSettingsApplicationDisabled(ComponentName admin, boolean isDisabled) {
        Bundle bundle = new Bundle();
        ArrayList<String> packageNames = new ArrayList<>();
        packageNames.add(IS_TV ? TV_SETTINGS_PACKAGE_NAME : SETTINGS_PACKAGE_NAME);
        bundle.putStringArrayList("value", packageNames);
        if (isDisabled) {
            return this.mDpm.setPolicy(admin, DISABLE_APPLICATIONS_LIST, bundle);
        }
        return this.mDpm.removePolicy(admin, DISABLE_APPLICATIONS_LIST, bundle);
    }

    public boolean isSettingsApplicationDisabled(ComponentName admin) {
        return isApplicationDisabled(admin, IS_TV ? TV_SETTINGS_PACKAGE_NAME : SETTINGS_PACKAGE_NAME);
    }

    public boolean setGooglePlayStoreDisabled(ComponentName admin, boolean isDisabled) {
        Bundle bundle = new Bundle();
        ArrayList<String> packageNames = new ArrayList<>();
        packageNames.add(GOOGLE_STORE_PACKAGE_NAME);
        bundle.putStringArrayList("value", packageNames);
        if (isDisabled) {
            return this.mDpm.setPolicy(admin, DISABLE_APPLICATIONS_LIST, bundle);
        }
        return this.mDpm.removePolicy(admin, DISABLE_APPLICATIONS_LIST, bundle);
    }

    public boolean isGooglePlayStoreDisabled(ComponentName admin) {
        return isApplicationDisabled(admin, GOOGLE_STORE_PACKAGE_NAME);
    }

    public boolean isApplicationDisabled(ComponentName admin, String packageName) {
        Bundle bundle = this.mDpm.getPolicy(admin, DISABLE_APPLICATIONS_LIST);
        if (bundle == null) {
            return IS_TV;
        }
        List<String> lists = null;
        try {
            lists = bundle.getStringArrayList("value");
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.e(TAG, "isApplicationDisabled exception.");
        }
        if (lists == null || lists.isEmpty() || !lists.contains(packageName)) {
            return IS_TV;
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

    public void setGoogleAccountDisabled(ComponentName admin, boolean isDisabled) {
        this.mDpm.setAccountDisabled(admin, "com.google", isDisabled);
    }

    public boolean isGoogleAccountDisabled(ComponentName admin) {
        return this.mDpm.isAccountDisabled(admin, "com.google");
    }

    public boolean setMicrophoneDisabled(ComponentName admin, boolean isDisabled) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", isDisabled);
        return this.mDpm.setPolicy(admin, DISABLE_MICROPHONE, bundle);
    }

    public boolean isMicrophoneDisabled(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, DISABLE_MICROPHONE);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return IS_TV;
    }

    public boolean setClipboardDisabled(ComponentName admin, boolean isDisabled) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", isDisabled);
        return this.mDpm.setPolicy(admin, STATUS_DISABLE_CLIPBOARD, bundle);
    }

    public boolean isClipboardDisabled(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, STATUS_DISABLE_CLIPBOARD);
        if (bundle == null) {
            return IS_TV;
        }
        return bundle.getBoolean("value");
    }

    public boolean setGoogleAccountAutoSyncDisabled(ComponentName admin, boolean isDisabled) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", isDisabled);
        return this.mDpm.setPolicy(admin, STATUS_DISABLE_GOOGLE_ACCOUNT_AUTOSYNC, bundle);
    }

    public boolean isGoogleAccountAutoSyncDisabled(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, STATUS_DISABLE_GOOGLE_ACCOUNT_AUTOSYNC);
        if (bundle == null) {
            return IS_TV;
        }
        return bundle.getBoolean("value");
    }

    public boolean setSystemUpdateDisabled(ComponentName admin, boolean isDisabled) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", isDisabled);
        return this.mDpm.setPolicy(admin, DISABLE_SYSTEM_UPDATE, bundle);
    }

    public boolean isSystemUpdateDisabled(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, DISABLE_SYSTEM_UPDATE);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return IS_TV;
    }

    public boolean setSdCardUpdateDisabled(ComponentName admin, boolean isDisabled) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", isDisabled);
        return this.mDpm.setPolicy(admin, DISABLE_SYSTEM_UPDATE_SD, bundle);
    }

    public boolean isSdCardUpdateDisabled(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, DISABLE_SYSTEM_UPDATE_SD);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return IS_TV;
    }

    public boolean setHeadphoneDisabled(ComponentName admin, boolean isDisabled) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", isDisabled);
        boolean isSuccess = this.mDpm.setPolicy(admin, DISABLE_HEADPHONE, bundle);
        if (isSuccess) {
            AudioManagerEx.disableHeadPhone(isDisabled);
        }
        return isSuccess;
    }

    public boolean isHeadphoneDisabled(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, DISABLE_HEADPHONE);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return IS_TV;
    }

    public boolean setSendNotificationDisabled(ComponentName admin, boolean isDisabled) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", isDisabled);
        return this.mDpm.setPolicy(admin, DISABLE_SEND_NOTIFICATION, bundle);
    }

    public boolean isSendNotificationDisabled(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, DISABLE_SEND_NOTIFICATION);
        if (bundle == null) {
            return IS_TV;
        }
        return bundle.getBoolean("value");
    }

    public boolean setVolumeAdjustDisabled(ComponentName admin, boolean isDisabled) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", isDisabled);
        return this.mDpm.setPolicy(admin, DISABLE_VOLUME, bundle);
    }

    public boolean isVolumeAdjustDisabled(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, DISABLE_VOLUME);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return IS_TV;
    }

    public boolean setPowerDisabled(ComponentName admin, boolean isDisabled) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", isDisabled);
        return this.mDpm.setPolicy(admin, DISABLE_POWER_SHUTDOWN, bundle);
    }

    public boolean isPowerDisabled(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, DISABLE_POWER_SHUTDOWN);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return IS_TV;
    }

    public boolean setShutdownMenuDisabled(ComponentName admin, boolean isDisabled) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", isDisabled);
        return this.mDpm.setPolicy(admin, DISABLE_SHUTDOWNMENU, bundle);
    }

    public boolean isShutdownMenuDisable(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, DISABLE_SHUTDOWNMENU);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return IS_TV;
    }

    public boolean setYoutubeDisabled(ComponentName admin, boolean isDisabled) {
        Bundle bundle = new Bundle();
        ArrayList<String> packageNames = new ArrayList<>();
        packageNames.add(YOUTUBE_PACKAGE_NAME);
        bundle.putStringArrayList("value", packageNames);
        if (isDisabled) {
            return this.mDpm.setPolicy(admin, DISABLE_APPLICATIONS_LIST, bundle);
        }
        return this.mDpm.removePolicy(admin, DISABLE_APPLICATIONS_LIST, bundle);
    }

    public boolean isYoutubeDisabled(ComponentName admin) {
        return isApplicationDisabled(admin, YOUTUBE_PACKAGE_NAME);
    }

    public boolean setChangeWallpaperDisabled(ComponentName admin, boolean isDisabled) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", isDisabled);
        return this.mDpm.setPolicy(admin, DISABLE_CHANGE_WALLPAPER, bundle);
    }

    public boolean isChangeWallpaperDisabled(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, DISABLE_CHANGE_WALLPAPER);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return IS_TV;
    }

    public boolean setVoiceOutgoingDisabled(ComponentName admin, boolean isDisabled) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", isDisabled);
        if (isDisabled) {
            return this.mDpm.setPolicy(admin, DISABLE_VOICE_OUTGOING, bundle);
        }
        return this.mDpm.removePolicy(admin, DISABLE_VOICE_OUTGOING, bundle);
    }

    public boolean isVoiceOutgoingDisabled(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, DISABLE_VOICE_OUTGOING);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return IS_TV;
    }

    public boolean setVoiceIncomingDisabled(ComponentName admin, boolean isDisabled) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", isDisabled);
        if (isDisabled) {
            return this.mDpm.setPolicy(admin, DISABLE_VOICE_INCOMING, bundle);
        }
        return this.mDpm.removePolicy(admin, DISABLE_VOICE_INCOMING, bundle);
    }

    public boolean isVoiceIncomingDisabled(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, DISABLE_VOICE_INCOMING);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return IS_TV;
    }

    public boolean setFingerprintAuthenticationDisabled(ComponentName admin, boolean isDisabled) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", isDisabled);
        return this.mDpm.setPolicy(admin, DISABLE_FINGERPRINT_AUTHENTICATION, bundle);
    }

    public boolean isFingerprintAuthenticationDisabled(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, DISABLE_FINGERPRINT_AUTHENTICATION);
        if (bundle == null) {
            return IS_TV;
        }
        return bundle.getBoolean("value");
    }

    public boolean setChargingDisabled(ComponentName admin, boolean isDisabled) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", isDisabled);
        return this.mDpm.setPolicy(admin, DISABLE_CHARGING, bundle);
    }

    public boolean isChargingDisabled(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, DISABLE_CHARGING);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return IS_TV;
    }

    public boolean setScheduledPowerOn(ComponentName admin, boolean isOn, String boradcast, long when) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", isOn);
        if (isOn) {
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
        return IS_TV;
    }

    public boolean setScheduledPowerOff(ComponentName admin, boolean isOff, String boradcast, long when) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", isOff);
        if (isOff) {
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
        return IS_TV;
    }

    public boolean setFloatTaskDisabled(ComponentName admin, boolean isDisabled) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", isDisabled);
        if (isDisabled) {
            return this.mDpm.setPolicy(admin, DISABLE_FLOAT_TASK, bundle);
        }
        return this.mDpm.removePolicy(admin, DISABLE_FLOAT_TASK, bundle);
    }

    public boolean isFloatTaskDisabled(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, DISABLE_FLOAT_TASK);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return IS_TV;
    }

    public boolean isFileShareDisabled(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, POLICY_FILE_SHARE);
        if (bundle == null) {
            return IS_TV;
        }
        return bundle.getBoolean("value");
    }

    public boolean setFileShareDisabled(ComponentName admin, boolean isDisabled) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", isDisabled);
        return this.mDpm.setPolicy(admin, POLICY_FILE_SHARE, bundle);
    }

    public boolean setChargeLimit(ComponentName admin, String limitValue) {
        Bundle bundle = new Bundle();
        bundle.putString("charge_limit", limitValue);
        return this.mDpm.setPolicy(admin, FORCE_SCHEDULED_CHARGE_LIMIT, bundle);
    }

    public boolean setAlarmDisabled(ComponentName admin, boolean isDisabled) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", isDisabled);
        if (isDisabled) {
            return this.mDpm.setPolicy(admin, DISABLE_ALARM, bundle);
        }
        return this.mDpm.removePolicy(admin, DISABLE_ALARM, bundle);
    }

    public boolean isAlarmDisabled(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, DISABLE_ALARM);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return IS_TV;
    }

    public boolean setDeprecatedAdminInterfacesEnabled(ComponentName admin, boolean isEnabled) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", isEnabled);
        if (isEnabled) {
            return this.mDpm.setPolicy(admin, POLICY_DEPRECATED_ADMIN_INTERFACES_ENABLED, bundle);
        }
        return this.mDpm.removePolicy(admin, POLICY_DEPRECATED_ADMIN_INTERFACES_ENABLED, bundle);
    }

    public boolean isDeprecatedAdminInterfacesEnabled(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, POLICY_DEPRECATED_ADMIN_INTERFACES_ENABLED);
        return bundle == null ? IS_TV : bundle.getBoolean("value");
    }

    public boolean setMultiWindowDisabled(ComponentName admin, boolean isDisabled) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", isDisabled);
        return this.mDpm.setPolicy(admin, POLICY_DISABLE_MULTIWINDOW, bundle);
    }

    public boolean isMultiWindowDisabled(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, POLICY_DISABLE_MULTIWINDOW);
        if (bundle == null) {
            return IS_TV;
        }
        return bundle.getBoolean("value");
    }

    public boolean setSleepByPowerButtonDisabled(ComponentName admin, boolean isDisabled) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", isDisabled);
        if (isDisabled) {
            return this.mDpm.setPolicy(admin, DISABLE_SCREEN_TURN_OFF, bundle);
        }
        return this.mDpm.removePolicy(admin, DISABLE_SCREEN_TURN_OFF, bundle);
    }

    public boolean isSleepByPowerButtonDisabled(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, DISABLE_SCREEN_TURN_OFF);
        return bundle == null ? IS_TV : bundle.getBoolean("value");
    }

    public boolean setStatusBarDisabled(ComponentName admin, boolean isDisabled) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", isDisabled);
        if (isDisabled) {
            return this.mDpm.setPolicy(admin, DISABLE_STATUS_BAR, bundle);
        }
        return this.mDpm.removePolicy(admin, DISABLE_STATUS_BAR, bundle);
    }

    public boolean isStatusBarDisabled(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, DISABLE_STATUS_BAR);
        if (bundle == null) {
            return IS_TV;
        }
        return bundle.getBoolean("value");
    }

    public boolean setVoiceAssistantButtonDisabled(ComponentName admin, boolean isDisabled) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", isDisabled);
        return this.mDpm.setPolicy(admin, DISABLE_VOICE_ASSISTANT_BUTTON, bundle);
    }

    public boolean isVoiceAssistantButtonDisabled(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, DISABLE_VOICE_ASSISTANT_BUTTON);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return IS_TV;
    }
}

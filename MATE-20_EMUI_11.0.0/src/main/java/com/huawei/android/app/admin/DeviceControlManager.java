package com.huawei.android.app.admin;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.AndroidRuntimeException;
import android.util.Log;
import huawei.android.app.admin.HwDevicePolicyManagerEx;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class DeviceControlManager {
    private static final String BUNDLE_VALUE_NAME = "value";
    public static final int CERTIFICATE_DEFAULT = 0;
    public static final int CERTIFICATE_PEM_BASE64 = 1;
    public static final int CERTIFICATE_PKCS12 = 0;
    public static final int CERTIFICATE_WIFI = 1;
    public static final String DEACTIVE_TIME = "deactive_time";
    private static final int DEFAULT_SLEEP_TIME_INTERVAL = 300000;
    public static final String DEFAULT_VOICE_ASSISTANT = "default-voice-assistant";
    public static final String DELAY_TIME = "delay_time";
    public static final String EXTRA_DELAY_TIME = "extra_delay_time";
    private static final int EYES_PROTECT_TYPE_DISTANCE = 0;
    private static final int EYES_PROTECT_TYPE_FLIP = 1;
    private static final int EYES_PROTECT_TYPE_LIGHT = 2;
    private static final String FRONT_APPS_BUNDLE_NAME = "front-apps";
    private static final String FRONT_APP_SPERATOR = ",";
    public static final String GET_ADMIN_DEACTIVE_TIME = "get_admin_deactive_time";
    public static final String IS_FORCED_ACTIVE = "is_forced_active";
    public static final String IS_FORCED_ACTIVE_ADMIN = "is_forced_active_admin";
    private static final int NOT_SLEEP_TIME_INTERVAL = 0;
    public static final String POLICY_ENABLE_DISTANCE_EYES_PROTECT = "policy_enable_distance_eyes_protect";
    public static final String POLICY_ENABLE_FLIP_EYES_PROTECT = "policy_enable_flip_eyes_protect";
    public static final String POLICY_ENABLE_LIGHT_EYES_PROTECT = "policy_enable_light_eyes_protect";
    public static final String POLICY_SET_MEDIA_CONTROL_DISABLED = "device_control_set_media_control_disabled";
    public static final String POLICY_TURN_ON_CC_MODE = "device_control_turn_on_cc_mode";
    public static final String POLICY_TURN_ON_EYE_COMFORT = "device_control_turn_on_eye_comfort";
    public static final String POLICY_TURN_ON_USB_DEBUG_MODE = "device_control_turn_on_usb_debug_mode";
    public static final String POLICY_UNMOUNT_USB_DEVICES = "device_control_unmount_usb_devices";
    public static final String REMOVE_ACTIVE_ADMIN = "remove_active_admin";
    private static final String SETTINGS_CLASS_NAME = "com.android.settings.DeviceAdminAdd";
    private static final String SETTINGS_PACKAGE_NAME = "com.android.settings";
    public static final String SET_DELAY_DEACTIVE_ADMIN = "set_delay_deactive_admin";
    public static final String SET_FORCED_ACTIVE_ADMIN = "set_forced_active_admin";
    private static final String SET_SYSTEM_LANGUAGE = "set-system-language";
    public static final String SLEEP_TIME_INTERVAL_AFTER_POWER_ON = "sleep-time-interval-after-power-on";
    private static final String TAG = "DeviceControlManager";
    private static final String VOICE_ASSISTANT_BUNDLE_NAME = "voice-assistant";
    private final HwDevicePolicyManagerEx mDpm = new HwDevicePolicyManagerEx();

    public void shutdownDevice(ComponentName admin) {
        this.mDpm.shutdownDevice(admin);
    }

    public void rebootDevice(ComponentName admin) {
        this.mDpm.rebootDevice(admin);
    }

    public boolean isRooted(ComponentName admin) {
        return this.mDpm.isRooted(admin);
    }

    public void turnOnGPS(ComponentName admin, boolean on) {
        this.mDpm.turnOnGPS(admin, on);
    }

    public boolean isGPSTurnOn(ComponentName admin) {
        return this.mDpm.isGPSTurnOn(admin);
    }

    public void setSysTime(ComponentName admin, long millis) {
        this.mDpm.setSysTime(admin, millis);
    }

    public void setCustomSettingsMenu(ComponentName admin, List<String> menusToDelete) {
        this.mDpm.setCustomSettingsMenu(admin, menusToDelete);
    }

    public void setDefaultLauncher(ComponentName admin, String packageName, String className) {
        this.mDpm.setDefaultLauncher(admin, packageName, className);
    }

    public void clearDefaultLauncher(ComponentName admin) {
        this.mDpm.clearDefaultLauncher(admin);
    }

    public Bitmap captureScreen(ComponentName admin) {
        return this.mDpm.captureScreen(admin);
    }

    @Deprecated
    public void setSilentActiveAdmin(ComponentName admin) {
    }

    public boolean formatSDCard(ComponentName who, String diskId) {
        return this.mDpm.formatSDCard(who, diskId);
    }

    public boolean installCertificateWithType(ComponentName who, int type, byte[] certBuffer, String name, String password, int flag, boolean requestAccess) {
        return this.mDpm.installCertificateWithType(who, type, certBuffer, name, password, flag, requestAccess);
    }

    public boolean setSystemLanguage(ComponentName who, Locale locale) {
        Bundle bundle = new Bundle();
        bundle.putString("locale", locale.toLanguageTag());
        return this.mDpm.setCustomPolicy(who, SET_SYSTEM_LANGUAGE, bundle);
    }

    public void setDeviceOwnerApp(ComponentName admin, String ownerName) {
        this.mDpm.setDeviceOwnerApp(admin, ownerName);
    }

    public void clearDeviceOwnerApp() {
        this.mDpm.clearDeviceOwnerApp();
    }

    public void turnOnMobiledata(ComponentName admin, boolean on) {
        this.mDpm.turnOnMobiledata(admin, on);
    }

    public void forceMobiledataOn(ComponentName admin) {
        this.mDpm.setDataConnectivityDisabled(admin, true);
        this.mDpm.turnOnMobiledata(admin, true);
    }

    public boolean turnOnCCMode(ComponentName admin, boolean on) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", on);
        return this.mDpm.setPolicy(admin, POLICY_TURN_ON_CC_MODE, bundle);
    }

    public boolean isCCModeTurnedOn(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, POLICY_TURN_ON_CC_MODE);
        if (bundle != null) {
            return bundle.getBoolean("value");
        }
        return false;
    }

    public boolean turnOnEyeComfort(ComponentName admin, boolean on) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", on);
        return this.mDpm.setPolicy(admin, POLICY_TURN_ON_EYE_COMFORT, bundle);
    }

    public boolean isEyeComfortTurnedOn(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, POLICY_TURN_ON_EYE_COMFORT);
        if (bundle == null) {
            return false;
        }
        return bundle.getBoolean("value");
    }

    public boolean setCarrierLockScreenPassword(ComponentName who, String password, String phoneNumber) {
        return this.mDpm.setCarrierLockScreenPassword(who, password, phoneNumber);
    }

    public boolean clearCarrierLockScreenPassword(ComponentName who, String password) {
        return this.mDpm.clearCarrierLockScreenPassword(who, password);
    }

    public boolean setDefaultDataCard(ComponentName admin, int slotId, Message response) {
        return this.mDpm.setDefaultDataCard(admin, slotId, response);
    }

    public boolean unmountUsbDevices(ComponentName admin, String uuid) {
        Bundle bundle = new Bundle();
        bundle.putString("value", uuid);
        return this.mDpm.setPolicy(admin, POLICY_UNMOUNT_USB_DEVICES, bundle);
    }

    public boolean turnOnUsbDebugMode(ComponentName admin, boolean on) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", on);
        return this.mDpm.setPolicy(admin, POLICY_TURN_ON_USB_DEBUG_MODE, bundle);
    }

    public boolean setMediaControlDisabled(ComponentName admin, boolean disabled) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", disabled);
        return this.mDpm.setPolicy(admin, POLICY_SET_MEDIA_CONTROL_DISABLED, bundle);
    }

    public boolean isMediaControlDisabled(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, POLICY_SET_MEDIA_CONTROL_DISABLED);
        if (bundle == null) {
            return false;
        }
        return bundle.getBoolean("value");
    }

    public boolean setForcedActiveDeviceAdmin(ComponentName admin, Context context) {
        if (context == null || !(context instanceof Activity)) {
            return false;
        }
        boolean result = this.mDpm.setCustomPolicy(admin, SET_FORCED_ACTIVE_ADMIN, null);
        Log.d(TAG, "setForcedActivateDeviceAdmin " + result);
        if (!result) {
            return result;
        }
        try {
            Intent intent = new Intent();
            intent.setClassName("com.android.settings", SETTINGS_CLASS_NAME);
            intent.putExtra("android.app.extra.DEVICE_ADMIN", admin);
            context.startActivity(intent);
            return result;
        } catch (ActivityNotFoundException | AndroidRuntimeException | SecurityException e) {
            Log.e(TAG, "setForcedActiveDeviceAdmin startActivity failed");
            return false;
        }
    }

    public boolean removeActiveDeviceAdmin(ComponentName admin) {
        return this.mDpm.removeCustomPolicy(admin, REMOVE_ACTIVE_ADMIN, null);
    }

    public boolean isForcedActiveDeviceAdmin(ComponentName admin) {
        Bundle bundle = this.mDpm.getCustomPolicy(admin, IS_FORCED_ACTIVE_ADMIN, null);
        if (bundle == null) {
            return false;
        }
        boolean result = bundle.getBoolean(IS_FORCED_ACTIVE);
        Log.d(TAG, "isForcedActivateDeviceAdmin " + result);
        return result;
    }

    public boolean setDelayDeactiveDeviceAdmin(ComponentName admin, int delayTime, Context context) {
        if (context == null || !(context instanceof Activity)) {
            return false;
        }
        Bundle bundle = new Bundle();
        bundle.putInt(DELAY_TIME, delayTime);
        boolean result = this.mDpm.setCustomPolicy(admin, SET_DELAY_DEACTIVE_ADMIN, bundle);
        Log.d(TAG, "setDelayDeactiveDeviceAdmin " + result);
        if (!result) {
            return result;
        }
        try {
            Intent intent = new Intent();
            intent.setClassName("com.android.settings", SETTINGS_CLASS_NAME);
            intent.putExtra("android.app.extra.DEVICE_ADMIN", admin);
            intent.putExtra(EXTRA_DELAY_TIME, delayTime);
            context.startActivity(intent);
            return result;
        } catch (ActivityNotFoundException | AndroidRuntimeException | SecurityException e) {
            Log.e(TAG, "setDelayDeactiveDeviceAdmin startActivity failed");
            return false;
        }
    }

    public long getDeviceAdminDeactiveTime(ComponentName admin) {
        Bundle bundle = this.mDpm.getCustomPolicy(admin, GET_ADMIN_DEACTIVE_TIME, null);
        if (bundle == null) {
            return 0;
        }
        String time = bundle.getString(DEACTIVE_TIME);
        Log.d(TAG, "getDeviceAdminDeactivateTime time " + time);
        if (TextUtils.isEmpty(time)) {
            return 0;
        }
        try {
            return Long.parseLong(time);
        } catch (NumberFormatException e) {
            Log.e(TAG, "getDeviceAdminDeactiveTime : NumberFormatException");
            return 0;
        }
    }

    public boolean setSleepTimeIntervalAfterPowerOn(ComponentName admin, int intervalTime) {
        if (intervalTime < DEFAULT_SLEEP_TIME_INTERVAL && intervalTime != 0) {
            return false;
        }
        String sleepTimeInterval = String.valueOf(intervalTime);
        Bundle bundle = new Bundle();
        bundle.putString("value", sleepTimeInterval);
        return this.mDpm.setPolicy(admin, SLEEP_TIME_INTERVAL_AFTER_POWER_ON, bundle);
    }

    public int getSleepTimeIntervalAfterPowerOn(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, SLEEP_TIME_INTERVAL_AFTER_POWER_ON);
        if (bundle == null) {
            return DEFAULT_SLEEP_TIME_INTERVAL;
        }
        String value = bundle.getString("value");
        if (TextUtils.isEmpty(value)) {
            return DEFAULT_SLEEP_TIME_INTERVAL;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            Log.e(TAG, "getRePowerExtinguishIntervalTime NumberFormatException!");
            return DEFAULT_SLEEP_TIME_INTERVAL;
        }
    }

    public boolean setDefaultVoiceAssistant(ComponentName admin, String packageName, ArrayList<String> apps) {
        Bundle bundle = new Bundle();
        bundle.putString(VOICE_ASSISTANT_BUNDLE_NAME, packageName);
        bundle.putString(FRONT_APPS_BUNDLE_NAME, String.join(FRONT_APP_SPERATOR, apps));
        return this.mDpm.setPolicy(admin, DEFAULT_VOICE_ASSISTANT, bundle);
    }

    public HashMap<String, String> getDefaultVoiceAssistant(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, DEFAULT_VOICE_ASSISTANT);
        if (bundle == null) {
            return new HashMap<>();
        }
        String packageName = bundle.getString(VOICE_ASSISTANT_BUNDLE_NAME);
        String frontApp = bundle.getString(FRONT_APPS_BUNDLE_NAME);
        if (TextUtils.isEmpty(packageName) || TextUtils.isEmpty(frontApp)) {
            return new HashMap<>();
        }
        List<String> apps = Arrays.asList(frontApp.split(FRONT_APP_SPERATOR));
        if (apps == null || apps.isEmpty()) {
            return new HashMap<>();
        }
        HashMap<String, String> map = new HashMap<>();
        for (String app : apps) {
            map.put(app, packageName);
        }
        return map;
    }

    public boolean turnOnEyesProtect(ComponentName admin, int eyesProtectType, boolean isOn) {
        String policy = DeviceSettingsManager.EMPTY_STRING;
        if (eyesProtectType == 0) {
            policy = POLICY_ENABLE_DISTANCE_EYES_PROTECT;
        } else if (eyesProtectType == 1) {
            policy = POLICY_ENABLE_FLIP_EYES_PROTECT;
        } else if (eyesProtectType == 2) {
            policy = POLICY_ENABLE_LIGHT_EYES_PROTECT;
        }
        if (!TextUtils.isEmpty(policy)) {
            Bundle bundle = new Bundle();
            bundle.putBoolean("value", isOn);
            return this.mDpm.setPolicy(admin, policy, bundle);
        }
        throw new IllegalArgumentException("eyesProtectType is error");
    }
}

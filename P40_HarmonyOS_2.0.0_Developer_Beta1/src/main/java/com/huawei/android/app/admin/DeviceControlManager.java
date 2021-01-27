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
    public static final int CONTROL_MODE = 1;
    public static final String DEACTIVE_TIME = "deactive_time";
    private static final int DEFAULT_SLEEP_TIME_INTERVAL = 300000;
    public static final String DEFAULT_VOICE_ASSISTANT = "default-voice-assistant";
    public static final String DELAY_TIME = "delay_time";
    public static final String DESC_CONTROL_MODE = "control";
    public static final String DESC_NORMAL_MODE = "normal";
    public static final String DESC_RELEASE_MODE = "release";
    public static final String EXTRA_DELAY_TIME = "extra_delay_time";
    private static final int EYES_PROTECT_TYPE_DISTANCE = 0;
    private static final int EYES_PROTECT_TYPE_FLIP = 1;
    private static final int EYES_PROTECT_TYPE_LIGHT = 2;
    private static final String FRONT_APPS_BUNDLE_NAME = "front-apps";
    private static final String FRONT_APP_SPERATOR = ",";
    public static final String GET_ADMIN_DEACTIVE_TIME = "get_admin_deactive_time";
    public static final String IS_FORCED_ACTIVE = "is_forced_active";
    public static final String IS_FORCED_ACTIVE_ADMIN = "is_forced_active_admin";
    public static final int NORMAL_MODE = 0;
    private static final int NOT_SLEEP_TIME_INTERVAL = 0;
    public static final String POLICY_CONTROL_MODE = "device_control_control_mode";
    public static final String POLICY_DEVICE_NAME = "device_name";
    public static final String POLICY_ENABLE_DISTANCE_EYES_PROTECT = "policy_enable_distance_eyes_protect";
    public static final String POLICY_ENABLE_FLIP_EYES_PROTECT = "policy_enable_flip_eyes_protect";
    public static final String POLICY_ENABLE_LIGHT_EYES_PROTECT = "policy_enable_light_eyes_protect";
    public static final String POLICY_SCREEN_CAST = "policy_screen_cast";
    public static final String POLICY_SET_MEDIA_CONTROL_DISABLED = "device_control_set_media_control_disabled";
    public static final String POLICY_TURN_ON_CC_MODE = "device_control_turn_on_cc_mode";
    public static final String POLICY_TURN_ON_EYE_COMFORT = "device_control_turn_on_eye_comfort";
    public static final String POLICY_TURN_ON_USB_DEBUG_MODE = "device_control_turn_on_usb_debug_mode";
    public static final String POLICY_TURN_ON_USB_TETHERING = "device_control_turn_on_usb_tethering";
    public static final String POLICY_UNMOUNT_USB_DEVICES = "device_control_unmount_usb_devices";
    public static final int RELEASE_MODE = 2;
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

    public void turnOnGPS(ComponentName admin, boolean isOn) {
        this.mDpm.turnOnGps(admin, isOn);
    }

    public boolean isGPSTurnOn(ComponentName admin) {
        return this.mDpm.isGpsTurnOn(admin);
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

    public boolean formatSDCard(ComponentName admin, String diskId) {
        return this.mDpm.formatSdCard(admin, diskId);
    }

    public boolean installCertificateWithType(ComponentName admin, int type, byte[] certBuffer, String name, String password, int flag, boolean isRequestAccess) {
        Bundle bundle = new Bundle();
        bundle.putInt("type", type);
        bundle.putString("name", name);
        bundle.putString("password", password);
        bundle.putInt("flag", flag);
        bundle.putBoolean("isRequestAccess", isRequestAccess);
        return this.mDpm.installCertificateWithType(admin, bundle, certBuffer);
    }

    public boolean setSystemLanguage(ComponentName admin, Locale locale) {
        Bundle bundle = new Bundle();
        bundle.putString("locale", locale.toLanguageTag());
        return this.mDpm.setCustomPolicy(admin, SET_SYSTEM_LANGUAGE, bundle);
    }

    public void setDeviceOwnerApp(ComponentName admin, String ownerName) {
        this.mDpm.setDeviceOwnerApp(admin, ownerName);
    }

    public void clearDeviceOwnerApp() {
        this.mDpm.clearDeviceOwnerApp();
    }

    public void turnOnMobiledata(ComponentName admin, boolean isOn) {
        this.mDpm.turnOnMobiledata(admin, isOn);
    }

    public void forceMobiledataOn(ComponentName admin) {
        this.mDpm.setDataConnectivityDisabled(admin, true);
        this.mDpm.turnOnMobiledata(admin, true);
    }

    public boolean turnOnCCMode(ComponentName admin, boolean isOn) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", isOn);
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

    @Deprecated
    public boolean setCarrierLockScreenPassword(ComponentName admin, String password, String phoneNumber) {
        return false;
    }

    @Deprecated
    public boolean clearCarrierLockScreenPassword(ComponentName admin, String password) {
        return false;
    }

    public boolean setDefaultDataCard(ComponentName admin, int slotId, Message response) {
        return this.mDpm.setDefaultDataCard(admin, slotId, response);
    }

    public boolean unmountUsbDevices(ComponentName admin, String uuid) {
        Bundle bundle = new Bundle();
        bundle.putString("value", uuid);
        return this.mDpm.setPolicy(admin, POLICY_UNMOUNT_USB_DEVICES, bundle);
    }

    public boolean turnOnUsbDebugMode(ComponentName admin, boolean isOn) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", isOn);
        return this.mDpm.setPolicy(admin, POLICY_TURN_ON_USB_DEBUG_MODE, bundle);
    }

    public boolean turnOnUsbTethering(ComponentName admin, boolean isOn) {
        Bundle bundleAll = this.mDpm.getPolicy(admin, "settings_policy_forbidden_network_share");
        if (bundleAll == null || !bundleAll.getBoolean("value", false)) {
            Bundle bundleUsb = this.mDpm.getPolicy(admin, "settings_policy_forbidden_usb_net_share");
            if (bundleUsb == null || !bundleUsb.getBoolean("value", false)) {
                Bundle bundle = new Bundle();
                bundle.putBoolean("value", isOn);
                return this.mDpm.setPolicy(admin, POLICY_TURN_ON_USB_TETHERING, bundle);
            }
            Log.i(TAG, "turnOnUsbTethering : USB tethering is disabled");
            return false;
        }
        Log.i(TAG, "turnOnUsbTethering : All tethering is disabled");
        return false;
    }

    public boolean setMediaControlDisabled(ComponentName admin, boolean isDisabled) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", isDisabled);
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
        boolean isSuccess = this.mDpm.setCustomPolicy(admin, SET_FORCED_ACTIVE_ADMIN, null);
        Log.d(TAG, "setForcedActivateDeviceAdmin " + isSuccess);
        if (!isSuccess) {
            return isSuccess;
        }
        try {
            Intent intent = new Intent();
            intent.setClassName(SETTINGS_PACKAGE_NAME, SETTINGS_CLASS_NAME);
            intent.putExtra("android.app.extra.DEVICE_ADMIN", admin);
            context.startActivity(intent);
            return isSuccess;
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
        boolean isSuccess = bundle.getBoolean(IS_FORCED_ACTIVE);
        Log.d(TAG, "isForcedActivateDeviceAdmin " + isSuccess);
        return isSuccess;
    }

    public boolean setDelayDeactiveDeviceAdmin(ComponentName admin, int delayTime, Context context) {
        if (context == null || !(context instanceof Activity)) {
            return false;
        }
        Bundle bundle = new Bundle();
        bundle.putInt(DELAY_TIME, delayTime);
        boolean isSuccess = this.mDpm.setCustomPolicy(admin, SET_DELAY_DEACTIVE_ADMIN, bundle);
        Log.d(TAG, "setDelayDeactiveDeviceAdmin " + isSuccess);
        if (!isSuccess) {
            return isSuccess;
        }
        try {
            Intent intent = new Intent();
            intent.setClassName(SETTINGS_PACKAGE_NAME, SETTINGS_CLASS_NAME);
            intent.putExtra("android.app.extra.DEVICE_ADMIN", admin);
            intent.putExtra(EXTRA_DELAY_TIME, delayTime);
            context.startActivity(intent);
            return isSuccess;
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
        String policy = "";
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

    public boolean setControlMode(ComponentName admin, int controlMode) {
        String mode = null;
        if (controlMode == 0) {
            mode = DESC_NORMAL_MODE;
        } else if (controlMode == 1) {
            mode = DESC_CONTROL_MODE;
        } else if (controlMode == 2) {
            mode = DESC_RELEASE_MODE;
        }
        if (!TextUtils.isEmpty(mode)) {
            Bundle bundle = new Bundle();
            bundle.putString("value", mode);
            return this.mDpm.setPolicy(admin, POLICY_CONTROL_MODE, bundle);
        }
        throw new IllegalArgumentException("setControlMode control mode is illegal");
    }

    public int getControlMode(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, POLICY_CONTROL_MODE);
        if (bundle == null) {
            return 0;
        }
        String value = bundle.getString("value");
        if (TextUtils.isEmpty(value)) {
            Log.e(TAG, "getControlMode control mode is empty!");
            return 0;
        }
        char c = 65535;
        int hashCode = value.hashCode();
        if (hashCode != -1039745817) {
            if (hashCode != 951543133) {
                if (hashCode == 1090594823 && value.equals(DESC_RELEASE_MODE)) {
                    c = 2;
                }
            } else if (value.equals(DESC_CONTROL_MODE)) {
                c = 1;
            }
        } else if (value.equals(DESC_NORMAL_MODE)) {
            c = 0;
        }
        if (c == 0) {
            return 0;
        }
        if (c == 1) {
            return 1;
        }
        if (c == 2) {
            return 2;
        }
        Log.e(TAG, "getControlMode control mode is illegal!");
        return 0;
    }

    public boolean setDeviceName(ComponentName admin, String name) {
        Bundle bundle = new Bundle();
        bundle.putString("value", name);
        return this.mDpm.setPolicy(admin, POLICY_DEVICE_NAME, bundle);
    }

    public String getDeviceName(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, POLICY_DEVICE_NAME);
        if (bundle != null) {
            return bundle.getString("value");
        }
        return "UNKNOWN";
    }

    public boolean turnOnScreenCast(ComponentName admin, boolean isTurnOn) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", isTurnOn);
        return this.mDpm.setPolicy(admin, POLICY_SCREEN_CAST, bundle);
    }

    public boolean isScreenCastTurnOn(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, POLICY_SCREEN_CAST);
        if (bundle == null) {
            return true;
        }
        return bundle.getBoolean("value");
    }
}

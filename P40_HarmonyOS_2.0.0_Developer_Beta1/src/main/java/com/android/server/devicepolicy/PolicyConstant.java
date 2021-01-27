package com.android.server.devicepolicy;

import com.android.server.devicepolicy.plugins.DeviceSettingsPlugin;
import java.util.HashMap;

public class PolicyConstant {
    private static final HashMap<Integer, String> POLICY_BUNDLE_2_BOOLEAN_MAP = new HashMap<Integer, String>() {
        /* class com.android.server.devicepolicy.PolicyConstant.AnonymousClass1 */

        {
            put(4021, "disable-screen-capture");
            put(4022, "disable-system-browser");
            put(4023, "disable-settings");
            put(4024, "disable-google-play-store");
            put(4025, "disable-clipboard");
            put(4026, "disable-google-account-autosync");
        }
    };
    private static final HashMap<Integer, String> POLICY_BUNDLE_2_LIST_MAP = new HashMap<Integer, String>() {
        /* class com.android.server.devicepolicy.PolicyConstant.AnonymousClass2 */

        {
            put(4020, "install-packages-black-list");
            put(4028, "disable-applications-list");
            put(4027, "ignore-frequent-relaunch-app");
        }
    };
    private static final String[] POLICY_BUNDLE_ARRAY = {"disable-sdwriting", "disable-notification", "disable-microphone", "disable-navigationbar", "super-whitelist-hwsystemmanager", DeviceSettingsPlugin.POLICY_FORBIDDEN_NETWORK_LOCATION, "disable-headphone", "disable-send-notification", "policy-single-app", "set-default-launcher", "disable-change-wallpaper", DeviceSettingsPlugin.POLICY_FORBIDDEN_SCREEN_OFF, "disable-power-shutdown", "disable-shutdownmenu", "disable-volume", DeviceSettingsPlugin.POLICY_FORBIDDEN_LOCATION_SERVICE, DeviceSettingsPlugin.POLICY_FORBIDDEN_LOCATION_MODE, "disable-sync", "passive_location_disallow_item", "wifi_p2p_item_policy_name", "infrared_item_policy_name", "disable-screen-turn-off", "disable-fingerprint-authentication", "force-enable-BT", "force-enable-wifi", DeviceSettingsPlugin.POLICY_ACCESSIBILITY_SERVICES_WHITE_LIST, "policy-file-share-disabled", DeviceSettingsPlugin.POLICY_PHONE_FIND, DeviceSettingsPlugin.POLICY_PARENT_CONTROL, DeviceSettingsPlugin.POLICY_SIM_LOCK, DeviceSettingsPlugin.POLICY_APPLICATION_LOCK, DeviceSettingsPlugin.DISABLED_ANDROID_ANIMATION, DeviceSettingsPlugin.POLICY_FORCE_ENCRYPT_SDCARD, "disable-multi-window", "task-lock-app-list", "disable_status_bar", "unavailable-ssid-list", "network-white-ip-list", "network-white-domain-list", "network-black-ip-list", "network-black-domain-list", "network-trust-app-list", "device_control_set_media_control_disabled", "disable-voice-assistant-button", "sleep-time-interval-after-power-on", "fix-app-runtime-permission-list"};

    private PolicyConstant() {
    }

    public static int getCodeByPolicyName(String policyName, boolean isList) {
        HashMap<Integer, String> map = isList ? POLICY_BUNDLE_2_LIST_MAP : POLICY_BUNDLE_2_BOOLEAN_MAP;
        for (Integer key : map.keySet()) {
            if (map.get(key).equals(policyName)) {
                return key.intValue();
            }
        }
        return -1;
    }

    public static HashMap<Integer, String> getPolicyBundle2BooleanMap() {
        return POLICY_BUNDLE_2_BOOLEAN_MAP;
    }

    public static HashMap<Integer, String> getPolicyBundle2ListMap() {
        return POLICY_BUNDLE_2_LIST_MAP;
    }

    public static String[] getPolicyBundleArray() {
        return POLICY_BUNDLE_ARRAY;
    }
}

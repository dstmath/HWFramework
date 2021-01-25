package ohos.event.commonevent;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ActionMapper {
    private static final Map<String, String> ACTION_MAP = Collections.unmodifiableMap(new HashMap<String, String>() {
        /* class ohos.event.commonevent.ActionMapper.AnonymousClass1 */

        {
            put("android.intent.action.BOOT_COMPLETED", CommonEventSupport.COMMON_EVENT_BOOT_COMPLETED);
            put("android.intent.action.LOCKED_BOOT_COMPLETED", CommonEventSupport.COMMON_EVENT_LOCKED_BOOT_COMPLETED);
            put("android.intent.action.ACTION_SHUTDOWN", CommonEventSupport.COMMON_EVENT_SHUTDOWN);
            put("android.intent.action.BATTERY_CHANGED", CommonEventSupport.COMMON_EVENT_BATTERY_CHANGED);
            put("android.intent.action.BATTERY_LOW", CommonEventSupport.COMMON_EVENT_BATTERY_LOW);
            put("android.intent.action.BATTERY_OKAY", CommonEventSupport.COMMON_EVENT_BATTERY_OKAY);
            put("android.intent.action.ACTION_POWER_CONNECTED", CommonEventSupport.COMMON_EVENT_POWER_CONNECTED);
            put("android.intent.action.ACTION_POWER_DISCONNECTED", CommonEventSupport.COMMON_EVENT_POWER_DISCONNECTED);
            put("android.intent.action.SCREEN_OFF", CommonEventSupport.COMMON_EVENT_SCREEN_OFF);
            put("android.intent.action.SCREEN_ON", CommonEventSupport.COMMON_EVENT_SCREEN_ON);
            put("android.intent.action.USER_PRESENT", CommonEventSupport.COMMON_EVENT_USER_PRESENT);
            put("android.intent.action.TIME_TICK", CommonEventSupport.COMMON_EVENT_TIME_TICK);
            put("android.intent.action.TIME_SET", CommonEventSupport.COMMON_EVENT_TIME_CHANGED);
            put("android.intent.action.DATE_CHANGED", CommonEventSupport.COMMON_EVENT_DATE_CHANGED);
            put("android.intent.action.TIMEZONE_CHANGED", CommonEventSupport.COMMON_EVENT_TIMEZONE_CHANGED);
            put("android.intent.action.CLOSE_SYSTEM_DIALOGS", CommonEventSupport.COMMON_EVENT_CLOSE_SYSTEM_DIALOGS);
            put("android.intent.action.PACKAGE_ADDED", CommonEventSupport.COMMON_EVENT_PACKAGE_ADDED);
            put("android.intent.action.PACKAGE_REPLACED", CommonEventSupport.COMMON_EVENT_PACKAGE_REPLACED);
            put("android.intent.action.MY_PACKAGE_REPLACED", CommonEventSupport.COMMON_EVENT_MY_PACKAGE_REPLACED);
            put("android.intent.action.PACKAGE_REMOVED", CommonEventSupport.COMMON_EVENT_PACKAGE_REMOVED);
            put("android.intent.action.PACKAGE_FULLY_REMOVED", CommonEventSupport.COMMON_EVENT_PACKAGE_FULLY_REMOVED);
            put("android.intent.action.PACKAGE_CHANGED", CommonEventSupport.COMMON_EVENT_PACKAGE_CHANGED);
            put("android.intent.action.PACKAGE_RESTARTED", CommonEventSupport.COMMON_EVENT_PACKAGE_RESTARTED);
            put("android.intent.action.PACKAGE_DATA_CLEARED", CommonEventSupport.COMMON_EVENT_PACKAGE_DATA_CLEARED);
            put("android.intent.action.PACKAGES_SUSPENDED", CommonEventSupport.COMMON_EVENT_PACKAGES_SUSPENDED);
            put("android.intent.action.PACKAGES_UNSUSPENDED", CommonEventSupport.COMMON_EVENT_PACKAGES_UNSUSPENDED);
            put("android.intent.action.MY_PACKAGE_SUSPENDED", CommonEventSupport.COMMON_EVENT_MY_PACKAGE_SUSPENDED);
            put("android.intent.action.MY_PACKAGE_UNSUSPENDED", CommonEventSupport.COMMON_EVENT_MY_PACKAGE_UNSUSPENDED);
            put("android.intent.action.UID_REMOVED", CommonEventSupport.COMMON_EVENT_UID_REMOVED);
            put("android.intent.action.PACKAGE_FIRST_LAUNCH", CommonEventSupport.COMMON_EVENT_PACKAGE_FIRST_LAUNCH);
            put("android.intent.action.PACKAGE_NEEDS_VERIFICATION", CommonEventSupport.COMMON_EVENT_PACKAGE_NEEDS_VERIFICATION);
            put("android.intent.action.PACKAGE_VERIFIED", CommonEventSupport.COMMON_EVENT_PACKAGE_VERIFIED);
            put("android.intent.action.EXTERNAL_APPLICATIONS_AVAILABLE", CommonEventSupport.COMMON_EVENT_EXTERNAL_APPLICATIONS_AVAILABLE);
            put("android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE", CommonEventSupport.COMMON_EVENT_EXTERNAL_APPLICATIONS_UNAVAILABLE);
            put("android.intent.action.CONFIGURATION_CHANGED", CommonEventSupport.COMMON_EVENT_CONFIGURATION_CHANGED);
            put("android.intent.action.LOCALE_CHANGED", CommonEventSupport.COMMON_EVENT_LOCALE_CHANGED);
            put("android.intent.action.MANAGE_PACKAGE_STORAGE", CommonEventSupport.COMMON_EVENT_MANAGE_PACKAGE_STORAGE);
            put("android.intent.action.USER_STARTED", CommonEventSupport.COMMON_EVENT_USER_STARTED);
            put("android.intent.action.USER_BACKGROUND", CommonEventSupport.COMMON_EVENT_USER_BACKGROUND);
            put("android.intent.action.USER_FOREGROUND", CommonEventSupport.COMMON_EVENT_USER_FOREGROUND);
            put("android.intent.action.USER_SWITCHED", CommonEventSupport.COMMON_EVENT_USER_SWITCHED);
            put("android.intent.action.USER_STARTING", CommonEventSupport.COMMON_EVENT_USER_STARTING);
            put("android.intent.action.USER_UNLOCKED", CommonEventSupport.COMMON_EVENT_USER_UNLOCKED);
            put("android.intent.action.USER_STOPPING", CommonEventSupport.COMMON_EVENT_USER_STOPPING);
            put("android.intent.action.USER_STOPPED", CommonEventSupport.COMMON_EVENT_USER_STOPPED);
            put(ActionMapper.ACTION_USER_ADDED, CommonEventSupport.COMMON_EVENT_USER_ADDED);
            put(ActionMapper.ACTION_USER_REMOVED, CommonEventSupport.COMMON_EVENT_USER_REMOVED);
            put("android.accounts.action.VISIBLE_ACCOUNTS_CHANGED", CommonEventSupport.COMMON_EVENT_VISIBLE_ACCOUNTS_UPDATED);
            put("android.accounts.action.ACCOUNT_REMOVED", CommonEventSupport.COMMON_EVENT_ACCOUNT_DELETED);
            put("android.net.wifi.WIFI_STATE_CHANGED", CommonEventSupport.COMMON_EVENT_WIFI_POWER_STATE);
            put("android.net.wifi.SCAN_RESULTS", CommonEventSupport.COMMON_EVENT_WIFI_SCAN_FINISHED);
            put("android.net.wifi.RSSI_CHANGED", CommonEventSupport.COMMON_EVENT_WIFI_RSSI_VALUE);
            put("android.net.wifi.STATE_CHANGE", CommonEventSupport.COMMON_EVENT_WIFI_CONN_STATE);
            put("android.net.wifi.WIFI_AP_STATE_CHANGED", CommonEventSupport.COMMON_EVENT_WIFI_HOTSPOT_STATE);
            put(ActionMapper.BROADCAST_KEY, CommonEventSupport.COMMON_EVENT_WIFI_MPLINK_STATE_CHANGE);
            put("android.net.wifi.p2p.CONNECTION_STATE_CHANGE", CommonEventSupport.COMMON_EVENT_WIFI_P2P_CONN_STATE);
            put(ActionMapper.ACTION_WIFI_AP_STA_JOIN, CommonEventSupport.COMMON_EVENT_WIFI_AP_STAT_JOIN);
            put(ActionMapper.ACTION_WIFI_AP_STA_LEAVE, CommonEventSupport.COMMON_EVENT_WIFI_AP_STAT_LEAVE);
            put("android.net.wifi.p2p.STATE_CHANGED", CommonEventSupport.COMMON_EVENT_WIFI_P2P_STATE_CHANGED);
            put("android.net.wifi.p2p.PEERS_CHANGED", CommonEventSupport.COMMON_EVENT_WIFI_P2P_PEERS_STATE_CHANGED);
            put("android.net.wifi.p2p.DISCOVERY_STATE_CHANGE", CommonEventSupport.COMMON_EVENT_WIFI_P2P_PEERS_DISCOVERY_STATE_CHANGED);
            put("android.net.wifi.p2p.THIS_DEVICE_CHANGED", CommonEventSupport.COMMON_EVENT_WIFI_P2P_CURRENT_DEVICE_STATE_CHANGED);
            put("android.net.wifi.p2p.PERSISTENT_GROUPS_CHANGED", CommonEventSupport.COMMON_EVENT_WIFI_P2P_GROUP_STATE_CHANGED);
            put("android.bluetooth.headset.profile.action.CONNECTION_STATE_CHANGED", CommonEventSupport.COMMON_EVENT_BLUETOOTH_HANDSFREE_AG_CONNECT_STATE_UPDATE);
            put("android.bluetooth.headset.profile.action.ACTIVE_DEVICE_CHANGED", CommonEventSupport.COMMON_EVENT_BLUETOOTH_HANDSFREE_AG_CURRENT_DEVICE_UPDATE);
            put("android.bluetooth.headset.profile.action.AUDIO_STATE_CHANGED", "usual.event.bluetooth.handsfree.ag.AUDIO_STATE_UPDATE");
            put("android.bluetooth.a2dp.profile.action.CONNECTION_STATE_CHANGED", CommonEventSupport.COMMON_EVENT_BLUETOOTH_A2DPSOURCE_CONNECT_STATE_UPDATE);
            put("android.bluetooth.a2dp.profile.action.ACTIVE_DEVICE_CHANGED", CommonEventSupport.COMMON_EVENT_BLUETOOTH_A2DPSOURCE_CURRENT_DEVICE_UPDATE);
            put("android.bluetooth.a2dp.profile.action.PLAYING_STATE_CHANGED", CommonEventSupport.COMMON_EVENT_BLUETOOTH_A2DPSOURCE_PLAYING_STATE_UPDATE);
            put("android.bluetooth.a2dp.profile.action.AVRCP_CONNECTION_STATE_CHANGED", CommonEventSupport.COMMON_EVENT_BLUETOOTH_A2DPSOURCE_AVRCP_CONNECT_STATE_UPDATE);
            put("android.bluetooth.a2dp.profile.action.CODEC_CONFIG_CHANGED", CommonEventSupport.COMMON_EVENT_BLUETOOTH_A2DPSOURCE_CODEC_VALUE_UPDATE);
            put("android.bluetooth.device.action.FOUND", "usual.event.bluetooth.remotedevice.DISCOVERED");
            put("android.bluetooth.device.action.CLASS_CHANGED", CommonEventSupport.COMMON_EVENT_BLUETOOTH_REMOTEDEVICE_CLASS_VALUE_UPDATE);
            put("android.bluetooth.device.action.ACL_CONNECTED", CommonEventSupport.COMMON_EVENT_BLUETOOTH_REMOTEDEVICE_ACL_CONNECTED);
            put("android.bluetooth.device.action.ACL_DISCONNECTED", CommonEventSupport.COMMON_EVENT_BLUETOOTH_REMOTEDEVICE_ACL_DISCONNECTED);
            put("android.bluetooth.device.action.NAME_CHANGED", CommonEventSupport.COMMON_EVENT_BLUETOOTH_REMOTEDEVICE_NAME_UPDATE);
            put("android.bluetooth.device.action.BOND_STATE_CHANGED", "usual.event.bluetooth.remotedevice.PAIR_STATE");
            put("android.bluetooth.device.action.BATTERY_LEVEL_CHANGED", CommonEventSupport.COMMON_EVENT_BLUETOOTH_REMOTEDEVICE_BATTERY_VALUE_UPDATE);
            put("android.bluetooth.device.action.SDP_RECORD", CommonEventSupport.COMMON_EVENT_BLUETOOTH_REMOTEDEVICE_SDP_RESULT);
            put("android.bluetooth.device.action.UUID", CommonEventSupport.COMMON_EVENT_BLUETOOTH_REMOTEDEVICE_UUID_VALUE);
            put("android.bluetooth.device.action.PAIRING_REQUEST", "usual.event.bluetooth.remotedevice.PAIRING_REQ");
            put("android.bluetooth.device.action.PAIRING_CANCEL", CommonEventSupport.COMMON_EVENT_BLUETOOTH_REMOTEDEVICE_PAIRING_CANCEL);
            put("android.bluetooth.device.action.CONNECTION_ACCESS_REQUEST", CommonEventSupport.COMMON_EVENT_BLUETOOTH_REMOTEDEVICE_CONNECT_REQ);
            put("android.bluetooth.device.action.CONNECTION_ACCESS_REPLY", CommonEventSupport.COMMON_EVENT_BLUETOOTH_REMOTEDEVICE_CONNECT_REPLY);
            put("android.bluetooth.device.action.CONNECTION_ACCESS_CANCEL", CommonEventSupport.COMMON_EVENT_BLUETOOTH_REMOTEDEVICE_CONNECT_CANCEL);
            put("android.bluetooth.headsetclient.profile.action.CONNECTION_STATE_CHANGED", "usual.event.bluetooth.handsfreeunit.CONNECT_STATE_UPDATE");
            put("android.bluetooth.headsetclient.profile.action.AUDIO_STATE_CHANGED", "usual.event.bluetooth.handsfreeunit.AUDIO_STATE_UPDATE");
            put("android.bluetooth.headsetclient.profile.action.AG_EVENT", CommonEventSupport.COMMON_EVENT_BLUETOOTH_HANDSFREEUNIT_AG_COMMON_EVENT);
            put("android.bluetooth.headsetclient.profile.action.AG_CALL_CHANGED", "usual.event.bluetooth.handsfreeunit.AG_CALL_STATE_UPDATE");
            put("android.bluetooth.adapter.action.STATE_CHANGED", "usual.event.bluetooth.host.STATE_UPDATE");
            put("android.bluetooth.adapter.action.REQUEST_DISCOVERABLE", CommonEventSupport.COMMON_EVENT_BLUETOOTH_HOST_REQ_DISCOVERABLE);
            put("android.bluetooth.adapter.action.REQUEST_ENABLE", CommonEventSupport.COMMON_EVENT_BLUETOOTH_HOST_REQ_ENABLE);
            put("android.bluetooth.adapter.action.REQUEST_DISABLE", CommonEventSupport.COMMON_EVENT_BLUETOOTH_HOST_REQ_DISABLE);
            put("android.bluetooth.adapter.action.SCAN_MODE_CHANGED", "usual.event.bluetooth.host.SCAN_MODE_UPDATE");
            put("android.bluetooth.adapter.action.DISCOVERY_STARTED", "usual.event.bluetooth.host.DISCOVERY_STARTED");
            put("android.bluetooth.adapter.action.DISCOVERY_FINISHED", "usual.event.bluetooth.host.DISCOVERY_FINISHED");
            put("android.bluetooth.adapter.action.LOCAL_NAME_CHANGED", "usual.event.bluetooth.host.NAME_UPDATE");
            put("android.bluetooth.a2dp-sink.profile.action.CONNECTION_STATE_CHANGED", CommonEventSupport.COMMON_EVENT_BLUETOOTH_A2DPSINK_CONNECT_STATE_UPDATE);
            put("android.bluetooth.a2dp-sink.profile.action.PLAYING_STATE_CHANGED", CommonEventSupport.COMMON_EVENT_BLUETOOTH_A2DPSINK_PLAYING_STATE_UPDATE);
            put("android.bluetooth.a2dp-sink.profile.action.AUDIO_CONFIG_CHANGED", CommonEventSupport.COMMON_EVENT_BLUETOOTH_A2DPSINK_AUDIO_STATE_UPDATE);
            put("android.nfc.action.ADAPTER_STATE_CHANGED", CommonEventSupport.COMMON_EVENT_NFC_ACTION_ADAPTER_STATE_CHANGED);
            put(ActionMapper.ACTION_RF_FIELD_ON_DETECTED, CommonEventSupport.COMMON_EVENT_NFC_ACTION_RF_FIELD_ON_DETECTED);
            put(ActionMapper.ACTION_RF_FIELD_OFF_DETECTED, CommonEventSupport.COMMON_EVENT_NFC_ACTION_RF_FIELD_OFF_DETECTED);
            put("android.location.MODE_CHANGED", CommonEventSupport.COMMON_EVENT_LOCATION_MODE_STATE_CHANGED);
            put("android.os.action.DISCHARGING", CommonEventSupport.COMMON_EVENT_DISCHARGING);
            put("android.os.action.CHARGING", CommonEventSupport.COMMON_EVENT_CHARGING);
            put("android.os.action.DEVICE_IDLE_MODE_CHANGED", CommonEventSupport.COMMON_EVENT_DEVICE_IDLE_MODE_CHANGED);
            put("android.os.action.POWER_SAVE_MODE_CHANGED", CommonEventSupport.COMMON_EVENT_POWER_SAVE_MODE_CHANGED);
            put("android.hardware.usb.action.USB_STATE", CommonEventSupport.COMMON_EVENT_USB_STATE);
            put("android.hardware.usb.action.USB_PORT_CHANGED", CommonEventSupport.COMMON_EVENT_USB_PORT_CHANGED);
            put("android.hardware.usb.action.USB_DEVICE_ATTACHED", CommonEventSupport.COMMON_EVENT_USB_DEVICE_ATTACHED);
            put("android.hardware.usb.action.USB_DEVICE_DETACHED", CommonEventSupport.COMMON_EVENT_USB_DEVICE_DETACHED);
            put("android.hardware.usb.action.USB_ACCESSORY_ATTACHED", CommonEventSupport.COMMON_EVENT_USB_ACCESSORY_ATTACHED);
            put("android.hardware.usb.action.USB_ACCESSORY_DETACHED", CommonEventSupport.COMMON_EVENT_USB_ACCESSORY_DETACHED);
            put("android.intent.action.DEVICE_STORAGE_LOW", CommonEventSupport.COMMON_EVENT_DEVICE_STORAGE_LOW);
            put("android.intent.action.DEVICE_STORAGE_OK", CommonEventSupport.COMMON_EVENT_DEVICE_STORAGE_OK);
            put("android.intent.action.DEVICE_STORAGE_FULL", CommonEventSupport.COMMON_EVENT_DEVICE_STORAGE_FULL);
            put("android.intent.action.MEDIA_REMOVED", CommonEventSupport.COMMON_EVENT_DISK_REMOVED);
            put("android.intent.action.MEDIA_UNMOUNTED", CommonEventSupport.COMMON_EVENT_DISK_UNMOUNTED);
            put("android.intent.action.MEDIA_MOUNTED", CommonEventSupport.COMMON_EVENT_DISK_MOUNTED);
            put("android.intent.action.MEDIA_BAD_REMOVAL", CommonEventSupport.COMMON_EVENT_DISK_BAD_REMOVAL);
            put("android.intent.action.MEDIA_UNMOUNTABLE", CommonEventSupport.COMMON_EVENT_DISK_UNMOUNTABLE);
            put("android.intent.action.MEDIA_EJECT", CommonEventSupport.COMMON_EVENT_DISK_EJECT);
            put(ActionMapper.CONNECTIVITY_ACTION, CommonEventSupport.COMMON_EVENT_CONNECTIVITY_CHANGE);
            put(CommonEventSupport.COMMON_EVENT_TEST_ACTION1, CommonEventSupport.COMMON_EVENT_TEST_ACTION2);
        }
    });
    public static final String ACTION_RF_FIELD_OFF_DETECTED = "com.android.nfc_extras.action.RF_FIELD_OFF_DETECTED";
    public static final String ACTION_RF_FIELD_ON_DETECTED = "com.android.nfc_extras.action.RF_FIELD_ON_DETECTED";
    public static final String ACTION_USER_ADDED = "android.intent.action.USER_ADDED";
    public static final String ACTION_USER_REMOVED = "android.intent.action.USER_REMOVED";
    public static final String ACTION_WIFI_AP_STA_JOIN = "com.huawei.net.wifi.WIFI_AP_STA_JOIN";
    public static final String ACTION_WIFI_AP_STA_LEAVE = "com.huawei.net.wifi.WIFI_AP_STA_LEAVE";
    public static final String BROADCAST_KEY = "com.android.server.hidata.arbitration.HwArbitrationStateMachine";
    public static final String CONNECTIVITY_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";

    public static Optional<String> convertToAndroidAction(String str) {
        if (str == null) {
            return Optional.empty();
        }
        if (str.isEmpty()) {
            return Optional.of(str);
        }
        if (!str.startsWith("usual.event.")) {
            return Optional.of(str);
        }
        for (Map.Entry<String, String> entry : ACTION_MAP.entrySet()) {
            if (str.equals(entry.getValue())) {
                return Optional.of(entry.getKey());
            }
        }
        return Optional.of(str);
    }

    public static Optional<String> convertToZidaneAction(String str) {
        if (str == null) {
            return Optional.empty();
        }
        if (str.isEmpty()) {
            return Optional.of(str);
        }
        if (ACTION_MAP.containsKey(str)) {
            return Optional.of(ACTION_MAP.get(str));
        }
        return Optional.of(str);
    }

    static boolean queryZAction(String str) {
        return ACTION_MAP.containsValue(str);
    }

    static String getZAction(String str) {
        return ACTION_MAP.get(str);
    }

    private ActionMapper() {
    }
}

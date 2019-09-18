package com.huawei.opcollect.utils;

import com.huawei.opcollect.collector.receivercollection.SysEventUtil;

public final class OPCollectConstant {
    public static final String AIR_MODE_ACTION_NAME = "AirModeAction";
    public static final String AR_ACTION_NAME = "RawARStatus";
    public static final String BLUETOOTH_CONNECT_MODE_ACTION_NAME = "BluetoothConnectAction";
    public static final String BLUETOOTH_STATE_MODE_ACTION_NAME = "BluetoothStateAction";
    public static final String BOOT_MODE_ACTION_NAME = "BootCompleteAction";
    public static final String CAMERA_TAKE_ACTION_NAME = "CameraAction";
    public static final String CONTACTS_ACTION_NAME = "DSContactsInfo";
    public static final String DESKCLOCK_ACTION_NAME = "DeskClockAction";
    public static final String DEVICE_ACTION_NAME = "RawDeviceInfo";
    public static final String DEVICE_STATUS_INFO_ACTION_NAME = "RawDeviceStatusInfo";
    public static final String EYE_MODE_ACTION_NAME = "EyeComfortStateAction";
    public static final String FG_APP_ACTION_NAME = "HwForegroundAppAction";
    public static final String GPS_MODE_ACTION_NAME = "GpsStateAction";
    public static final String HEADSET_ACTION_NAME = "HeadsetPlugStateAction";
    public static final String HOTEL_ACTION_NAME = "RawHotelInfo";
    public static final String LOCATION_ACTION_NAME = "RawLocationRecord";
    public static final String LOCATION_MODE_ACTION_NAME = "AwareLocationAction";
    public static final String LOW_BATTERY_ACTION_NAME = "LowBatteryAction";
    public static final String MEDIA_ACTION_NAME = "RawMediaAppStastic";
    public static final String PACKAGE_INSTALL_ACTION_NAME = "PackageInstallAction";
    public static final String PACKAGE_NAME = "com.huawei.bd";
    public static final String PACKAGE_UNINSTALL_ACTION_NAME = "PackageUninstallAction";
    public static final String PACKAGE_UPDATE_ACTION_NAME = "PackageUpdateAction";
    public static final String PERMISSION = "com.huawei.permission.OP_COLLECT";
    public static final String POWER_CONNECT_ACTION_NAME = "PowerConnectedAction";
    public static final String POWER_DISCONNECT_ACTION_NAME = "PowerDisConnectedAction";
    public static final String REBOOT_ACTION_NAME = "RebootAction";
    public static final String ROTATE_MODE_ACTION_NAME = "RotationStateAction";
    public static final String SCREEN_OFF_ACTION_NAME = "ScreenOffAction";
    public static final String SCREEN_ON_ACTION_NAME = "ScreenOnAction";
    public static final String SHUTDOWN_ACTION_NAME = "ShutdownAction";
    public static final String TIMEZONE_ACTION_NAME = "TimeZoneAction";
    public static final String TRIP_ACTION_NAME = "RawTrainFlightTickInfo";
    public static final String USER_PRESENT_ACTION_NAME = "UserPresentAction";
    public static final String WEATHER_ACTION_NAME = "RawWeatherInfo";
    public static final String WIFI_BSSID = "wifiBssid";
    public static final String WIFI_CONNECT_ACTION_NAME = "WifiConnectAction";
    public static final String WIFI_IP = "ip";
    public static final String WIFI_LEVEL = "wifiLevel";
    public static final String WIFI_NAME = "name";
    public static final String WIFI_SSID = "wifiSsid";
    public static final String WIFI_STATE_ACTION_NAME = "WifiStateAction";

    public static String getActionNameFromSwitchName(String switchName) {
        if (switchName == null || "".equalsIgnoreCase(switchName)) {
            return null;
        }
        if (WEATHER_ACTION_NAME.equalsIgnoreCase(switchName)) {
            return WEATHER_ACTION_NAME;
        }
        if (AR_ACTION_NAME.equalsIgnoreCase(switchName)) {
            return AR_ACTION_NAME;
        }
        if (LOCATION_ACTION_NAME.equalsIgnoreCase(switchName)) {
            return LOCATION_ACTION_NAME;
        }
        if (MEDIA_ACTION_NAME.equalsIgnoreCase(switchName)) {
            return MEDIA_ACTION_NAME;
        }
        if (HOTEL_ACTION_NAME.equalsIgnoreCase(switchName)) {
            return HOTEL_ACTION_NAME;
        }
        if (TRIP_ACTION_NAME.equalsIgnoreCase(switchName)) {
            return TRIP_ACTION_NAME;
        }
        if (DEVICE_ACTION_NAME.equalsIgnoreCase(switchName)) {
            return DEVICE_ACTION_NAME;
        }
        if (CONTACTS_ACTION_NAME.equalsIgnoreCase(switchName)) {
            return CONTACTS_ACTION_NAME;
        }
        if (DEVICE_STATUS_INFO_ACTION_NAME.equalsIgnoreCase(switchName)) {
            return DEVICE_STATUS_INFO_ACTION_NAME;
        }
        if (SysEventUtil.EVENT_WIFI_OFF.equalsIgnoreCase(switchName) || SysEventUtil.EVENT_WIFI_ON.equalsIgnoreCase(switchName)) {
            return WIFI_STATE_ACTION_NAME;
        }
        if (SysEventUtil.EVENT_WIFI_CONNECTED.equalsIgnoreCase(switchName) || SysEventUtil.EVENT_WIFI_DISCONNECTED.equalsIgnoreCase(switchName)) {
            return WIFI_CONNECT_ACTION_NAME;
        }
        if (SysEventUtil.EVENT_TIMEZONE_CHANGE.equalsIgnoreCase(switchName)) {
            return TIMEZONE_ACTION_NAME;
        }
        if (SysEventUtil.EVENT_SHUTDOWN_PHONE.equalsIgnoreCase(switchName)) {
            return SHUTDOWN_ACTION_NAME;
        }
        if (SysEventUtil.EVENT_SCREEN_OFF.equalsIgnoreCase(switchName)) {
            return SCREEN_OFF_ACTION_NAME;
        }
        if (SysEventUtil.EVENT_SCREEN_ON.equalsIgnoreCase(switchName)) {
            return SCREEN_ON_ACTION_NAME;
        }
        if (SysEventUtil.EVENT_REBOOT.equalsIgnoreCase(switchName)) {
            return REBOOT_ACTION_NAME;
        }
        if (SysEventUtil.EVENT_POWER_DISCONNECTED.equalsIgnoreCase(switchName)) {
            return POWER_DISCONNECT_ACTION_NAME;
        }
        if (SysEventUtil.EVENT_POWER_CONNECTED.equalsIgnoreCase(switchName)) {
            return POWER_CONNECT_ACTION_NAME;
        }
        if (SysEventUtil.EVENT_APP_UPDATE.equalsIgnoreCase(switchName)) {
            return PACKAGE_UPDATE_ACTION_NAME;
        }
        if (SysEventUtil.EVENT_APP_UNINSTALL.equalsIgnoreCase(switchName)) {
            return PACKAGE_UNINSTALL_ACTION_NAME;
        }
        if (SysEventUtil.EVENT_APP_INSTALL.equalsIgnoreCase(switchName)) {
            return PACKAGE_INSTALL_ACTION_NAME;
        }
        if (SysEventUtil.EVENT_LOW_POWER.equalsIgnoreCase(switchName)) {
            return LOW_BATTERY_ACTION_NAME;
        }
        if (SysEventUtil.EVENT_FOREGROUND_APP_CHANGE.equalsIgnoreCase(switchName)) {
            return FG_APP_ACTION_NAME;
        }
        if (SysEventUtil.EVENT_HEADSET_PLUG.equalsIgnoreCase(switchName) || SysEventUtil.EVENT_HEADSET_UNPLUG.equalsIgnoreCase(switchName)) {
            return HEADSET_ACTION_NAME;
        }
        if (SysEventUtil.EVENT_DESKCLOCK_ALARM.equalsIgnoreCase(switchName)) {
            return DESKCLOCK_ACTION_NAME;
        }
        if (SysEventUtil.EVENT_TAKE_PICTURE.equalsIgnoreCase(switchName)) {
            return CAMERA_TAKE_ACTION_NAME;
        }
        if (SysEventUtil.EVENT_BOOT_COMPLETED.equalsIgnoreCase(switchName)) {
            return BOOT_MODE_ACTION_NAME;
        }
        if (SysEventUtil.EVENT_BLUETOOTH_OFF.equalsIgnoreCase(switchName) || SysEventUtil.EVENT_BLUETOOTH_ON.equalsIgnoreCase(switchName)) {
            return BLUETOOTH_STATE_MODE_ACTION_NAME;
        }
        if (SysEventUtil.EVENT_BLUETOOTH_CONNECTED.equalsIgnoreCase(switchName) || SysEventUtil.EVENT_BLUETOOTH_DISCONNECTED.equalsIgnoreCase(switchName)) {
            return BLUETOOTH_CONNECT_MODE_ACTION_NAME;
        }
        if (SysEventUtil.EVENT_LOCATION_CHANGE.equalsIgnoreCase(switchName)) {
            return LOCATION_MODE_ACTION_NAME;
        }
        if (SysEventUtil.EVENT_ROTATE_OFF.equalsIgnoreCase(switchName) || SysEventUtil.EVENT_ROTATE_ON.equalsIgnoreCase(switchName)) {
            return ROTATE_MODE_ACTION_NAME;
        }
        if (SysEventUtil.EVENT_GPS_OFF.equalsIgnoreCase(switchName) || SysEventUtil.EVENT_GPS_ON.equalsIgnoreCase(switchName)) {
            return GPS_MODE_ACTION_NAME;
        }
        if (SysEventUtil.EVENT_EYECOMFORT_OFF.equalsIgnoreCase(switchName) || SysEventUtil.EVENT_EYECOMFORT_ON.equalsIgnoreCase(switchName)) {
            return EYE_MODE_ACTION_NAME;
        }
        if (SysEventUtil.EVENT_AIRPLANE_OFF.equalsIgnoreCase(switchName) || SysEventUtil.EVENT_AIRPLANE_ON.equalsIgnoreCase(switchName)) {
            return AIR_MODE_ACTION_NAME;
        }
        if (SysEventUtil.EVENT_USER_PRESENT.equalsIgnoreCase(switchName)) {
            return USER_PRESENT_ACTION_NAME;
        }
        if (SysEventUtil.BATTERY_LEFT.equalsIgnoreCase(switchName)) {
            return SysEventUtil.BATTERY_LEFT;
        }
        if (SysEventUtil.POWER_SAVING_STATUS.equalsIgnoreCase(switchName)) {
            return SysEventUtil.POWER_SAVING_STATUS;
        }
        if (SysEventUtil.DISTURB_STATUS.equalsIgnoreCase(switchName)) {
            return SysEventUtil.DISTURB_STATUS;
        }
        if (SysEventUtil.NFC_STATUS.equalsIgnoreCase(switchName)) {
            return SysEventUtil.NFC_STATUS;
        }
        return null;
    }
}

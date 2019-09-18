package com.huawei.opcollect.strategy;

import android.content.Context;
import com.huawei.opcollect.collector.observercollection.AirModeAction;
import com.huawei.opcollect.collector.observercollection.EyeComfortStateAction;
import com.huawei.opcollect.collector.observercollection.GpsStateAction;
import com.huawei.opcollect.collector.observercollection.IntelligentAction;
import com.huawei.opcollect.collector.observercollection.PowerSavingAction;
import com.huawei.opcollect.collector.observercollection.RotationStateAction;
import com.huawei.opcollect.collector.observercollection.ZenModeAction;
import com.huawei.opcollect.collector.pullcollection.ContactsAction;
import com.huawei.opcollect.collector.pullcollection.DeviceStaticAction;
import com.huawei.opcollect.collector.pullcollection.DeviceStatusInfoAction;
import com.huawei.opcollect.collector.pullcollection.MediaAppAction;
import com.huawei.opcollect.collector.receivercollection.AwareLocationAction;
import com.huawei.opcollect.collector.receivercollection.BatteryAction;
import com.huawei.opcollect.collector.receivercollection.BluetoothConnectAction;
import com.huawei.opcollect.collector.receivercollection.BluetoothStateAction;
import com.huawei.opcollect.collector.receivercollection.BootCompleteAction;
import com.huawei.opcollect.collector.receivercollection.CameraAction;
import com.huawei.opcollect.collector.receivercollection.DeskClockAction;
import com.huawei.opcollect.collector.receivercollection.HeadsetPlugStateAction;
import com.huawei.opcollect.collector.receivercollection.HwForegroundAppAction;
import com.huawei.opcollect.collector.receivercollection.LowBatteryAction;
import com.huawei.opcollect.collector.receivercollection.NfcStateAction;
import com.huawei.opcollect.collector.receivercollection.PackageInstallAction;
import com.huawei.opcollect.collector.receivercollection.PackageUninstallAction;
import com.huawei.opcollect.collector.receivercollection.PackageUpdateAction;
import com.huawei.opcollect.collector.receivercollection.PowerConnectedAction;
import com.huawei.opcollect.collector.receivercollection.PowerDisConnectedAction;
import com.huawei.opcollect.collector.receivercollection.RebootAction;
import com.huawei.opcollect.collector.receivercollection.ScreenOffAction;
import com.huawei.opcollect.collector.receivercollection.ScreenOnAction;
import com.huawei.opcollect.collector.receivercollection.ShutdownAction;
import com.huawei.opcollect.collector.receivercollection.SysEventUtil;
import com.huawei.opcollect.collector.receivercollection.TimeZoneAction;
import com.huawei.opcollect.collector.receivercollection.UserPresentAction;
import com.huawei.opcollect.collector.receivercollection.WifiConnectAction;
import com.huawei.opcollect.collector.receivercollection.WifiStateAction;
import com.huawei.opcollect.collector.servicecollection.ARStatusAction;
import com.huawei.opcollect.collector.servicecollection.LocationRecordAction;
import com.huawei.opcollect.collector.servicecollection.WeatherInfoAction;
import com.huawei.opcollect.odmf.OdmfHelper;
import com.huawei.opcollect.utils.OPCollectUtils;

public class ActionFactory {
    public static Action getSysInfoAction(Context context, String name) {
        if (context == null || name == null) {
            return null;
        }
        if (ActionTableName.RAW_DEVICE_INFO.getValue().equals(name)) {
            return DeviceStaticAction.getInstance(context);
        }
        if (ActionTableName.RAW_TRAIN_FLIGHT_TICK_INFO.getValue().equals(name)) {
            return IntelligentAction.getInstance(context, IntelligentAction.TYPE_TRIP);
        }
        if (ActionTableName.RAW_HOTEL_INFO.getValue().equals(name)) {
            return IntelligentAction.getInstance(context, IntelligentAction.TYPE_HOTEL);
        }
        if (ActionTableName.RAW_MEDIA_APP_STASTIC.getValue().equals(name)) {
            return MediaAppAction.getInstance(context);
        }
        if (ActionTableName.RAW_LOCATION_RECORD.getValue().equals(name)) {
            return LocationRecordAction.getInstance(context);
        }
        if (ActionTableName.RAW_AR_STATUS.getValue().equals(name)) {
            return ARStatusAction.getInstance(context);
        }
        if (ActionTableName.RAW_WEATHER_INFO.getValue().equals(name)) {
            return WeatherInfoAction.getInstance(context);
        }
        if (ActionTableName.RAW_DEVICE_STATUS_INFO.getValue().equals(name)) {
            return DeviceStatusInfoAction.getInstance(context);
        }
        if (!ActionTableName.DS_CONTACTS_INFO.getValue().equals(name) || !OPCollectUtils.checkODMFApiVersion(context, OdmfHelper.ODMF_API_VERSION_2_11_6)) {
            return null;
        }
        return ContactsAction.getInstance(context);
    }

    public static Action getSysEventAction(Context context, String name) {
        if (context == null || name == null) {
            return null;
        }
        if (SysEventUtil.EVENT_BOOT_COMPLETED.equals(name)) {
            return BootCompleteAction.getInstance(context);
        }
        if (SysEventUtil.EVENT_SHUTDOWN_PHONE.equals(name)) {
            return ShutdownAction.getInstance(context);
        }
        if (SysEventUtil.EVENT_REBOOT.equals(name)) {
            return RebootAction.getInstance(context);
        }
        if (SysEventUtil.EVENT_AIRPLANE_ON.equals(name)) {
            return AirModeAction.getInstance(context);
        }
        if (SysEventUtil.EVENT_AIRPLANE_OFF.equals(name)) {
            return AirModeAction.getInstance(context);
        }
        if (SysEventUtil.EVENT_POWER_CONNECTED.equals(name)) {
            return PowerConnectedAction.getInstance(context);
        }
        if (SysEventUtil.EVENT_POWER_DISCONNECTED.equals(name)) {
            return PowerDisConnectedAction.getInstance(context);
        }
        if (SysEventUtil.EVENT_WIFI_CONNECTED.equals(name)) {
            return WifiConnectAction.getInstance(context);
        }
        if (SysEventUtil.EVENT_WIFI_DISCONNECTED.equals(name)) {
            return WifiConnectAction.getInstance(context);
        }
        if (SysEventUtil.EVENT_DESKCLOCK_ALARM.equals(name)) {
            return DeskClockAction.getInstance(context);
        }
        if (SysEventUtil.EVENT_TAKE_PICTURE.equals(name)) {
            return CameraAction.getInstance(context);
        }
        if (SysEventUtil.EVENT_LOW_POWER.equals(name)) {
            return LowBatteryAction.getInstance(context);
        }
        if (SysEventUtil.EVENT_TIMEZONE_CHANGE.equals(name)) {
            return TimeZoneAction.getInstance(context);
        }
        if (SysEventUtil.EVENT_SCREEN_ON.equals(name)) {
            return ScreenOnAction.getInstance(context);
        }
        if (SysEventUtil.EVENT_SCREEN_OFF.equals(name)) {
            return ScreenOffAction.getInstance(context);
        }
        if (SysEventUtil.EVENT_BLUETOOTH_CONNECTED.equals(name)) {
            return BluetoothConnectAction.getInstance(context);
        }
        if (SysEventUtil.EVENT_BLUETOOTH_DISCONNECTED.equals(name)) {
            return BluetoothConnectAction.getInstance(context);
        }
        if (SysEventUtil.EVENT_APP_INSTALL.equals(name)) {
            return PackageInstallAction.getInstance(context);
        }
        if (SysEventUtil.EVENT_APP_UNINSTALL.equals(name)) {
            return PackageUninstallAction.getInstance(context);
        }
        if (SysEventUtil.EVENT_APP_UPDATE.equals(name)) {
            return PackageUpdateAction.getInstance(context);
        }
        if (SysEventUtil.EVENT_HEADSET_PLUG.equals(name)) {
            return HeadsetPlugStateAction.getInstance(context);
        }
        if (SysEventUtil.EVENT_HEADSET_UNPLUG.equals(name)) {
            return HeadsetPlugStateAction.getInstance(context);
        }
        if (SysEventUtil.EVENT_ROTATE_ON.equals(name)) {
            return RotationStateAction.getInstance(context);
        }
        if (SysEventUtil.EVENT_ROTATE_OFF.equals(name)) {
            return RotationStateAction.getInstance(context);
        }
        if (SysEventUtil.EVENT_BLUETOOTH_ON.equals(name)) {
            return BluetoothStateAction.getInstance(context);
        }
        if (SysEventUtil.EVENT_BLUETOOTH_OFF.equals(name)) {
            return BluetoothStateAction.getInstance(context);
        }
        if (SysEventUtil.EVENT_GPS_ON.equals(name)) {
            return GpsStateAction.getInstance(context);
        }
        if (SysEventUtil.EVENT_GPS_OFF.equals(name)) {
            return GpsStateAction.getInstance(context);
        }
        if (SysEventUtil.EVENT_EYECOMFORT_ON.equals(name)) {
            return EyeComfortStateAction.getInstance(context);
        }
        if (SysEventUtil.EVENT_EYECOMFORT_OFF.equals(name)) {
            return EyeComfortStateAction.getInstance(context);
        }
        if (SysEventUtil.EVENT_FOREGROUND_APP_CHANGE.equals(name)) {
            return HwForegroundAppAction.getInstance(context);
        }
        if (SysEventUtil.EVENT_LOCATION_CHANGE.equals(name)) {
            return AwareLocationAction.getInstance(context);
        }
        if (SysEventUtil.EVENT_WIFI_ON.equals(name)) {
            return WifiStateAction.getInstance(context);
        }
        if (SysEventUtil.EVENT_WIFI_OFF.equals(name)) {
            return WifiStateAction.getInstance(context);
        }
        if (SysEventUtil.EVENT_USER_PRESENT.equals(name)) {
            return UserPresentAction.getInstance(context);
        }
        if (SysEventUtil.BATTERY_LEFT.equals(name)) {
            return BatteryAction.getInstance(context);
        }
        if (SysEventUtil.POWER_SAVING_STATUS.equals(name)) {
            return PowerSavingAction.getInstance(context);
        }
        if (SysEventUtil.DISTURB_STATUS.equals(name)) {
            return ZenModeAction.getInstance(context);
        }
        if (SysEventUtil.NFC_STATUS.equals(name)) {
            return NfcStateAction.getInstance(context);
        }
        return null;
    }
}

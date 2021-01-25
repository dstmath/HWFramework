package android.os;

import android.annotation.SystemApi;
import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.os.IBatteryPropertiesRegistrar;
import com.android.internal.app.IBatteryStats;

public class BatteryManager {
    public static final String ACTION_CHARGING = "android.os.action.CHARGING";
    public static final String ACTION_DISCHARGING = "android.os.action.DISCHARGING";
    public static final int BATTERY_HEALTH_COLD = 7;
    public static final int BATTERY_HEALTH_DEAD = 4;
    public static final int BATTERY_HEALTH_GOOD = 2;
    public static final int BATTERY_HEALTH_OVERHEAT = 3;
    public static final int BATTERY_HEALTH_OVER_VOLTAGE = 5;
    public static final int BATTERY_HEALTH_UNKNOWN = 1;
    public static final int BATTERY_HEALTH_UNSPECIFIED_FAILURE = 6;
    public static final int BATTERY_PLUGGED_AC = 1;
    public static final int BATTERY_PLUGGED_ANY = 7;
    public static final int BATTERY_PLUGGED_USB = 2;
    public static final int BATTERY_PLUGGED_WIRELESS = 4;
    public static final int BATTERY_PROPERTY_CAPACITY = 4;
    public static final int BATTERY_PROPERTY_CHARGE_COUNTER = 1;
    public static final int BATTERY_PROPERTY_CURRENT_AVERAGE = 3;
    public static final int BATTERY_PROPERTY_CURRENT_NOW = 2;
    public static final int BATTERY_PROPERTY_ENERGY_COUNTER = 5;
    public static final int BATTERY_PROPERTY_STATUS = 6;
    public static final int BATTERY_STATUS_CHARGING = 2;
    public static final int BATTERY_STATUS_DISCHARGING = 3;
    public static final int BATTERY_STATUS_FULL = 5;
    public static final int BATTERY_STATUS_NOT_CHARGING = 4;
    public static final int BATTERY_STATUS_UNKNOWN = 1;
    public static final String EXTRA_BATTERY_LOW = "battery_low";
    @UnsupportedAppUsage
    public static final String EXTRA_CHARGE_COUNTER = "charge_counter";
    @SystemApi
    public static final String EXTRA_EVENTS = "android.os.extra.EVENTS";
    @SystemApi
    public static final String EXTRA_EVENT_TIMESTAMP = "android.os.extra.EVENT_TIMESTAMP";
    public static final String EXTRA_HEALTH = "health";
    public static final String EXTRA_ICON_SMALL = "icon-small";
    @UnsupportedAppUsage
    public static final String EXTRA_INVALID_CHARGER = "invalid_charger";
    public static final String EXTRA_LEVEL = "level";
    @UnsupportedAppUsage
    public static final String EXTRA_MAX_CHARGING_CURRENT = "max_charging_current";
    @UnsupportedAppUsage
    public static final String EXTRA_MAX_CHARGING_VOLTAGE = "max_charging_voltage";
    public static final String EXTRA_PLUGGED = "plugged";
    public static final String EXTRA_PRESENT = "present";
    public static final String EXTRA_SCALE = "scale";
    public static final String EXTRA_SEQUENCE = "seq";
    public static final String EXTRA_STATUS = "status";
    public static final String EXTRA_TECHNOLOGY = "technology";
    public static final String EXTRA_TEMPERATURE = "temperature";
    public static final String EXTRA_VOLTAGE = "voltage";
    public static final String HW_ACTION_BATTERY_LEV_JOB_ALLOWED = "com.huawei.intent.action.BATTERY_LEV_JOB_ALLOWED";
    public static final String HW_ACTION_BATTERY_LEV_JOB_NOT_ALLOWED = "com.huawei.intent.action.BATTERY_LEV_JOB_NOT_ALLOWED";
    public static final int HW_BATTERY_LEV_JOB_ALLOWED = SystemProperties.getInt("ro.config.hw_BatteryLevel_job_allowed", 0);
    private final IBatteryPropertiesRegistrar mBatteryPropertiesRegistrar;
    private final IBatteryStats mBatteryStats;
    private final Context mContext;

    public BatteryManager() {
        this.mContext = null;
        this.mBatteryStats = IBatteryStats.Stub.asInterface(ServiceManager.getService(BatteryStats.SERVICE_NAME));
        this.mBatteryPropertiesRegistrar = IBatteryPropertiesRegistrar.Stub.asInterface(ServiceManager.getService("batteryproperties"));
    }

    public BatteryManager(Context context, IBatteryStats batteryStats, IBatteryPropertiesRegistrar batteryPropertiesRegistrar) {
        this.mContext = context;
        this.mBatteryStats = batteryStats;
        this.mBatteryPropertiesRegistrar = batteryPropertiesRegistrar;
    }

    public boolean isCharging() {
        try {
            return this.mBatteryStats.isCharging();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    private long queryProperty(int id) {
        if (this.mBatteryPropertiesRegistrar == null) {
            return Long.MIN_VALUE;
        }
        try {
            BatteryProperty prop = new BatteryProperty();
            if (this.mBatteryPropertiesRegistrar.getProperty(id, prop) == 0) {
                return prop.getLong();
            }
            return Long.MIN_VALUE;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getIntProperty(int id) {
        Context context;
        long value = queryProperty(id);
        if (value != Long.MIN_VALUE || (context = this.mContext) == null || context.getApplicationInfo().targetSdkVersion < 28) {
            return (int) value;
        }
        return Integer.MIN_VALUE;
    }

    public long getLongProperty(int id) {
        return queryProperty(id);
    }

    public static boolean isPlugWired(int plugType) {
        return plugType == 2 || plugType == 1;
    }

    public long computeChargeTimeRemaining() {
        try {
            return this.mBatteryStats.computeChargeTimeRemaining();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int alterWirelessTxSwitch(int status) {
        try {
            return IBatteryPropertiesRegistrar.Stub.asInterface(ServiceManager.getService("batteryproperties")).alterWirelessTxSwitch(status);
        } catch (RemoteException e) {
            return 0;
        }
    }

    public int getWirelessTxSwitch() {
        try {
            return IBatteryPropertiesRegistrar.Stub.asInterface(ServiceManager.getService("batteryproperties")).getWirelessTxSwitch();
        } catch (RemoteException e) {
            return 0;
        }
    }

    public boolean supportWirelessTxCharge() {
        try {
            return IBatteryPropertiesRegistrar.Stub.asInterface(ServiceManager.getService("batteryproperties")).supportWirelessTxCharge();
        } catch (RemoteException e) {
            return false;
        }
    }

    @SystemApi
    public boolean setChargingStateUpdateDelayMillis(int delayMillis) {
        try {
            return this.mBatteryStats.setChargingStateUpdateDelayMillis(delayMillis);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }
}

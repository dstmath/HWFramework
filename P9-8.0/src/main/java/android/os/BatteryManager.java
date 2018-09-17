package android.os;

import com.android.internal.app.IBatteryStats;
import com.android.internal.app.IBatteryStats.Stub;

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
    public static final String EXTRA_CHARGE_COUNTER = "charge_counter";
    public static final String EXTRA_HEALTH = "health";
    public static final String EXTRA_ICON_SMALL = "icon-small";
    public static final String EXTRA_INVALID_CHARGER = "invalid_charger";
    public static final String EXTRA_LEVEL = "level";
    public static final String EXTRA_MAX_CHARGING_CURRENT = "max_charging_current";
    public static final String EXTRA_MAX_CHARGING_VOLTAGE = "max_charging_voltage";
    public static final String EXTRA_PLUGGED = "plugged";
    public static final String EXTRA_PRESENT = "present";
    public static final String EXTRA_SCALE = "scale";
    public static final String EXTRA_SEQUENCE = "seq";
    public static final String EXTRA_STATUS = "status";
    public static final String EXTRA_TECHNOLOGY = "technology";
    public static final String EXTRA_TEMPERATURE = "temperature";
    public static final String EXTRA_VOLTAGE = "voltage";
    private final IBatteryPropertiesRegistrar mBatteryPropertiesRegistrar;
    private final IBatteryStats mBatteryStats;

    public BatteryManager() {
        this.mBatteryStats = Stub.asInterface(ServiceManager.getService(BatteryStats.SERVICE_NAME));
        this.mBatteryPropertiesRegistrar = IBatteryPropertiesRegistrar.Stub.asInterface(ServiceManager.getService("batteryproperties"));
    }

    public BatteryManager(IBatteryStats batteryStats, IBatteryPropertiesRegistrar batteryPropertiesRegistrar) {
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
            long ret;
            BatteryProperty prop = new BatteryProperty();
            if (this.mBatteryPropertiesRegistrar.getProperty(id, prop) == 0) {
                ret = prop.getLong();
            } else {
                ret = Long.MIN_VALUE;
            }
            return ret;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getIntProperty(int id) {
        return (int) queryProperty(id);
    }

    public long getLongProperty(int id) {
        return queryProperty(id);
    }
}

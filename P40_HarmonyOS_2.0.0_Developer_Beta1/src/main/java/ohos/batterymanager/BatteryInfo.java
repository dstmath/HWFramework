package ohos.batterymanager;

import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class BatteryInfo {
    private static final int BATTERY_EMERGENCY_THRESHOLD = 4;
    private static final int BATTERY_HIGH_THRESHOLD = 100;
    private static final int BATTERY_LOW_THRESHOLD = 20;
    private static final int BATTERY_NORMAL_THRESHOLD = 85;
    private static final int DBG = 218114307;
    private static final HiLogLabel DEBUG = new HiLogLabel(3, (int) DBG, TAG);
    public static final String OHOS_BATTERY_CAPACITY = "batteryCapacity";
    public static final String OHOS_BATTERY_LOW = "batteryLow";
    public static final String OHOS_BATTERY_PRESENT = "batteryPresent";
    public static final String OHOS_BATTERY_TECHNOLOGY = "batteryTechnology";
    public static final String OHOS_BATTERY_TEMPERATURE = "batteryTemperature";
    public static final String OHOS_BATTERY_VOLTAGE = "batteryVoltage";
    public static final String OHOS_CHARGE_COUNTER = "chargeCounter";
    public static final String OHOS_CHARGE_STATE = "chargeState";
    public static final String OHOS_CHARGE_TYPE = "chargeType";
    public static final String OHOS_CHARGING_CURRENT_MAX = "maxCurrent";
    public static final String OHOS_CHARGING_VOLTAGE_MAX = "maxVoltage";
    public static final String OHOS_TEMPERATURE_STATE = "temperatureState";
    private static final String TAG = "BatterySrvKit";
    private int batteryTemperature;
    private BatteryChargeState chargingStatus;
    private BatteryHealthState healthStatus;
    private BatteryPluggedType pluggedType;
    private String technology;
    private int value;
    private int voltage;

    public enum BatteryChargeState {
        NONE,
        ENABLE,
        DISABLE,
        FULL,
        RESERVED
    }

    public enum BatteryHealthState {
        UNKNOWN,
        GOOD,
        OVERHEAT,
        OVERVOLTAGE,
        COLD,
        DEAD,
        RESERVED
    }

    public enum BatteryLevel {
        NONE,
        HIGH,
        NORMAL,
        LOW,
        EMERGENCY,
        RESERVED
    }

    public enum BatteryPluggedType {
        NONE,
        AC,
        USB,
        WIRELESS,
        RESERVED
    }

    private static native int nativeGetBatteryTemperature();

    private static native int nativeGetCapacity();

    private static native int nativeGetChargingStatus();

    private static native int nativeGetHealthStatus();

    private static native int nativeGetPluggedType();

    private static native String nativeGetTechnology();

    private static native int nativeGetVoltage();

    static {
        try {
            HiLog.info(DEBUG, "load libbatterykit_jni.z.so", new Object[0]);
            System.loadLibrary("batterykit_jni.z");
        } catch (UnsatisfiedLinkError unused) {
            HiLog.error(DEBUG, "Could not load libbatterykit_jni.z.so", new Object[0]);
        }
    }

    public int getCapacity() {
        return nativeGetCapacity();
    }

    public BatteryChargeState getChargingStatus() {
        int nativeGetChargingStatus = nativeGetChargingStatus();
        if (nativeGetChargingStatus > BatteryChargeState.RESERVED.ordinal() || nativeGetChargingStatus < BatteryChargeState.NONE.ordinal()) {
            return BatteryChargeState.RESERVED;
        }
        return BatteryChargeState.values()[nativeGetChargingStatus];
    }

    public BatteryHealthState getHealthStatus() {
        int nativeGetHealthStatus = nativeGetHealthStatus();
        if (nativeGetHealthStatus > BatteryHealthState.RESERVED.ordinal() || nativeGetHealthStatus < BatteryHealthState.UNKNOWN.ordinal()) {
            return BatteryHealthState.RESERVED;
        }
        return BatteryHealthState.values()[nativeGetHealthStatus];
    }

    public BatteryPluggedType getPluggedType() {
        int nativeGetPluggedType = nativeGetPluggedType();
        if (nativeGetPluggedType > BatteryPluggedType.RESERVED.ordinal() || nativeGetPluggedType < BatteryPluggedType.NONE.ordinal()) {
            return BatteryPluggedType.RESERVED;
        }
        return BatteryPluggedType.values()[nativeGetPluggedType];
    }

    public int getVoltage() {
        return nativeGetVoltage();
    }

    public String getTechnology() {
        return nativeGetTechnology();
    }

    public int getBatteryTemperature() {
        return nativeGetBatteryTemperature();
    }

    public BatteryLevel getBatteryLevel() {
        BatteryLevel batteryLevel = BatteryLevel.NORMAL;
        int nativeGetCapacity = nativeGetCapacity();
        if (nativeGetCapacity < 4) {
            return BatteryLevel.EMERGENCY;
        }
        if (nativeGetCapacity <= 20) {
            return BatteryLevel.LOW;
        }
        if (nativeGetCapacity <= 85) {
            return BatteryLevel.NORMAL;
        }
        if (nativeGetCapacity <= 100) {
            return BatteryLevel.HIGH;
        }
        return BatteryLevel.NONE;
    }
}

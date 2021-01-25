package ohos.batteryadapter;

import java.util.Optional;
import ohos.aafwk.content.Intent;
import ohos.batterymanager.BatteryInfo;
import ohos.event.commonevent.CommonEventBaseConverter;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class BatteryAdapter extends CommonEventBaseConverter {
    private static final String ANDROID_BATTERY_ACTION = "android.intent.action.BATTERY_CHANGED";
    private static final int ANDROID_HEALTH_COLD = 7;
    private static final int ANDROID_HEALTH_DEAD = 4;
    private static final int ANDROID_HEALTH_GOOD = 2;
    private static final int ANDROID_HEALTH_OVERHEAT = 3;
    private static final int ANDROID_HEALTH_OVER_VOLTAGE = 5;
    private static final int ANDROID_HEALTH_UNKNOWN = 1;
    private static final int ANDROID_HEALTH_UNSPECIFIED_FAILURE = 6;
    private static final int ANDROID_PLUGGED_AC = 1;
    private static final int ANDROID_PLUGGED_NONE = 0;
    private static final int ANDROID_PLUGGED_USB = 2;
    private static final int ANDROID_PLUGGED_WIRELESS = 4;
    private static final int ANDROID_STATUS_CHARGING = 2;
    private static final int ANDROID_STATUS_DISCHARGING = 3;
    private static final int ANDROID_STATUS_FULL = 5;
    private static final int ANDROID_STATUS_NOT_CHARGING = 4;
    private static final int ANDROID_STATUS_UNKNOWN = 1;
    private static final int DBG = 218114307;
    private static final HiLogLabel DEBUG = new HiLogLabel(3, (int) DBG, TAG);
    private static final String EXTRA_BATTERY_LOW = "battery_low";
    private static final String EXTRA_CHARGE_COUNTER = "charge_counter";
    private static final String EXTRA_HEALTH = "health";
    private static final String EXTRA_LEVEL = "level";
    private static final String EXTRA_MAX_CHARGING_CURRENT = "max_charging_current";
    private static final String EXTRA_MAX_CHARGING_VOLTAGE = "max_charging_voltage";
    private static final String EXTRA_PLUGGED = "plugged";
    private static final String EXTRA_PRESENT = "present";
    private static final String EXTRA_STATUS = "status";
    private static final String EXTRA_TECHNOLOGY = "technology";
    private static final String EXTRA_TEMPERATURE = "temperature";
    private static final String EXTRA_VOLTAGE = "voltage";
    private static final String HARMONY_BATTERY_ACTION = "usual.event.BATTERY_CHANGED";
    private static final String TAG = "BatteryAdapter";

    @Override // ohos.event.commonevent.CommonEventBaseConverter
    public Optional<Intent> convertAospIntentToIntent(android.content.Intent intent) {
        if (intent == null) {
            HiLog.error(DEBUG, "aIntent is null", new Object[0]);
            return Optional.ofNullable(new Intent());
        }
        String action = intent.getAction();
        HiLog.error(DEBUG, "aAction = %{public}s", new Object[]{action});
        Intent intent2 = new Intent();
        if (ANDROID_BATTERY_ACTION.equals(action)) {
            intent2.setAction("usual.event.BATTERY_CHANGED");
            int intExtra = intent.getIntExtra("level", -1);
            intent2.setParam(BatteryInfo.HARMONY_BATTERY_CAPACITY, intExtra);
            intent2.setParam(BatteryInfo.HARMONY_CHARGE_STATE, chargingStatusSwitch(intent.getIntExtra("status", -1)));
            intent2.setParam(BatteryInfo.HARMONY_TEMPERATURE_STATE, healthStatusSwitch(intent.getIntExtra(EXTRA_HEALTH, -1)));
            intent2.setParam(BatteryInfo.HARMONY_CHARGE_TYPE, chargerTypeSwitch(intent.getIntExtra(EXTRA_PLUGGED, -1)));
            int intExtra2 = intent.getIntExtra(EXTRA_VOLTAGE, -1);
            intent2.setParam(BatteryInfo.HARMONY_BATTERY_VOLTAGE, intExtra2);
            int intExtra3 = intent.getIntExtra(EXTRA_TEMPERATURE, -1);
            intent2.setParam(BatteryInfo.HARMONY_BATTERY_TEMPERATURE, intExtra3);
            intent2.setParam(BatteryInfo.HARMONY_BATTERY_TECHNOLOGY, intent.getStringExtra(EXTRA_TECHNOLOGY));
            intent2.setParam(BatteryInfo.HARMONY_CHARGING_CURRENT_MAX, intent.getIntExtra(EXTRA_MAX_CHARGING_CURRENT, -1));
            intent2.setParam(BatteryInfo.HARMONY_CHARGING_VOLTAGE_MAX, intent.getIntExtra(EXTRA_MAX_CHARGING_CURRENT, -1));
            intent2.setParam(BatteryInfo.HARMONY_CHARGE_COUNTER, intent.getIntExtra(EXTRA_CHARGE_COUNTER, -1));
            intent2.setParam(BatteryInfo.HARMONY_BATTERY_PRESENT, intent.getBooleanExtra(EXTRA_PRESENT, false));
            intent2.setParam(BatteryInfo.HARMONY_BATTERY_LOW, intent.getBooleanExtra(EXTRA_BATTERY_LOW, false));
            HiLog.error(DEBUG, "soc=%{public}d, vol=%{public}d, temp=%{public}d", new Object[]{Integer.valueOf(intExtra), Integer.valueOf(intExtra2), Integer.valueOf(intExtra3)});
        } else {
            HiLog.error(DEBUG, "other action, do nothing", new Object[0]);
        }
        return Optional.ofNullable(intent2);
    }

    private int chargerTypeSwitch(int i) {
        int i2;
        BatteryInfo.BatteryPluggedType.RESERVED.ordinal();
        if (i == 1) {
            i2 = BatteryInfo.BatteryPluggedType.AC.ordinal();
        } else if (i == 2) {
            i2 = BatteryInfo.BatteryPluggedType.USB.ordinal();
        } else if (i == 4) {
            i2 = BatteryInfo.BatteryPluggedType.WIRELESS.ordinal();
        } else if (i == 0) {
            i2 = BatteryInfo.BatteryPluggedType.NONE.ordinal();
        } else {
            i2 = BatteryInfo.BatteryPluggedType.RESERVED.ordinal();
        }
        HiLog.error(DEBUG, "charger type switch, aType=%{public}d, zType=%{public}d", new Object[]{Integer.valueOf(i), Integer.valueOf(i2)});
        return i2;
    }

    private int chargingStatusSwitch(int i) {
        int i2;
        BatteryInfo.BatteryChargeState.RESERVED.ordinal();
        if (i == 1) {
            i2 = BatteryInfo.BatteryChargeState.RESERVED.ordinal();
        } else if (i == 2) {
            i2 = BatteryInfo.BatteryChargeState.ENABLE.ordinal();
        } else if (i == 3) {
            i2 = BatteryInfo.BatteryChargeState.NONE.ordinal();
        } else if (i == 4) {
            i2 = BatteryInfo.BatteryChargeState.DISABLE.ordinal();
        } else if (i == 5) {
            i2 = BatteryInfo.BatteryChargeState.FULL.ordinal();
        } else {
            i2 = BatteryInfo.BatteryChargeState.RESERVED.ordinal();
        }
        HiLog.error(DEBUG, "charge status switch, aStatus=%{public}d, zStatus=%{public}d", new Object[]{Integer.valueOf(i), Integer.valueOf(i2)});
        return i2;
    }

    private int healthStatusSwitch(int i) {
        int i2;
        BatteryInfo.BatteryHealthState.UNKNOWN.ordinal();
        if (i == 1) {
            i2 = BatteryInfo.BatteryHealthState.UNKNOWN.ordinal();
        } else if (i == 2) {
            i2 = BatteryInfo.BatteryHealthState.GOOD.ordinal();
        } else if (i == 3) {
            i2 = BatteryInfo.BatteryHealthState.OVERHEAT.ordinal();
        } else if (i == 4) {
            i2 = BatteryInfo.BatteryHealthState.DEAD.ordinal();
        } else if (i == 5) {
            i2 = BatteryInfo.BatteryHealthState.OVERVOLTAGE.ordinal();
        } else if (i == 6) {
            i2 = BatteryInfo.BatteryHealthState.RESERVED.ordinal();
        } else if (i == 7) {
            i2 = BatteryInfo.BatteryHealthState.COLD.ordinal();
        } else {
            i2 = BatteryInfo.BatteryHealthState.UNKNOWN.ordinal();
        }
        HiLog.error(DEBUG, "health status switch, aStatus=%{public}d, zStatus=%{public}d", new Object[]{Integer.valueOf(i), Integer.valueOf(i2)});
        return i2;
    }
}

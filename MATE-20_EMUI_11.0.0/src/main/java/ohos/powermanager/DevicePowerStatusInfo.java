package ohos.powermanager;

import ohos.batterymanager.BatteryInfo;
import ohos.powermanager.PowerManager;

public class DevicePowerStatusInfo {
    private BatteryInfo batteryInfo = new BatteryInfo();
    private BatteryInfo.BatteryLevel batteryLevel = BatteryInfo.BatteryLevel.NONE;
    private BatteryInfo.BatteryChargeState chargingState = BatteryInfo.BatteryChargeState.NONE;
    private PowerManager.PowerState powerState = PowerManager.PowerState.NONE;

    public DevicePowerStatusInfo() {
        if (this.batteryInfo == null) {
            this.batteryInfo = new BatteryInfo();
        }
    }

    public PowerManager.PowerState getPowerState() {
        return PowerManager.PowerState.AWAKE;
    }

    public BatteryInfo.BatteryLevel getBatteryLevel() {
        BatteryInfo batteryInfo2 = this.batteryInfo;
        if (batteryInfo2 != null) {
            this.batteryLevel = batteryInfo2.getBatteryLevel();
        }
        return this.batteryLevel;
    }

    public BatteryInfo.BatteryChargeState getBatteryChargingStatus() {
        BatteryInfo batteryInfo2 = this.batteryInfo;
        if (batteryInfo2 != null) {
            this.chargingState = batteryInfo2.getChargingStatus();
        }
        return this.chargingState;
    }

    private void setPowerState(PowerManager.PowerState powerState2) {
        this.powerState = powerState2;
    }

    private void setBatteryLevel(BatteryInfo.BatteryLevel batteryLevel2) {
        this.batteryLevel = batteryLevel2;
    }

    private void setBatteryChargeState(BatteryInfo.BatteryChargeState batteryChargeState) {
        this.chargingState = batteryChargeState;
    }

    public static DevicePowerStatusInfo getDevicePowerStatusInfo() {
        DevicePowerStatusInfo devicePowerStatusInfo = new DevicePowerStatusInfo();
        BatteryInfo batteryInfo2 = new BatteryInfo();
        devicePowerStatusInfo.setPowerState(devicePowerStatusInfo.getPowerState());
        devicePowerStatusInfo.setBatteryLevel(batteryInfo2.getBatteryLevel());
        devicePowerStatusInfo.setBatteryChargeState(batteryInfo2.getChargingStatus());
        return devicePowerStatusInfo;
    }
}

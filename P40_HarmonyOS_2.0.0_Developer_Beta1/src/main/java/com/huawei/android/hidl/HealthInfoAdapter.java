package com.huawei.android.hidl;

import android.hardware.health.V1_0.HealthInfo;

public class HealthInfoAdapter {
    private HealthInfo mHealthInfo;

    public void setHealthInfo(HealthInfo healthInfo) {
        this.mHealthInfo = healthInfo;
    }

    public boolean getChargerAcOnline() {
        return this.mHealthInfo.chargerAcOnline;
    }

    public boolean getChargerUsbOnline() {
        return this.mHealthInfo.chargerUsbOnline;
    }

    public boolean getChargerWirelessOnline() {
        return this.mHealthInfo.chargerWirelessOnline;
    }

    public int getBatteryLevel() {
        return this.mHealthInfo.batteryLevel;
    }

    public int getBatteryStatus() {
        return this.mHealthInfo.batteryStatus;
    }

    public int getBatteryHealth() {
        return this.mHealthInfo.batteryHealth;
    }

    public boolean getBatteryPresent() {
        return this.mHealthInfo.batteryPresent;
    }

    public String getBatteryTechnology() {
        return this.mHealthInfo.batteryTechnology;
    }

    public int getBatteryVoltage() {
        return this.mHealthInfo.batteryVoltage;
    }

    public int getBatteryTemperature() {
        return this.mHealthInfo.batteryTemperature;
    }

    public void setHealthInfo(Object object) {
        if (object instanceof HealthInfo) {
            this.mHealthInfo = (HealthInfo) object;
        }
    }

    public int getMaxChargingCurrent() {
        return this.mHealthInfo.maxChargingCurrent;
    }

    public int getMaxChargingVoltage() {
        return this.mHealthInfo.maxChargingVoltage;
    }

    public int getBatteryChargeCounter() {
        return this.mHealthInfo.batteryChargeCounter;
    }
}

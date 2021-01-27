package com.android.server.hidata.appqoe;

public class HwPowerParameterConfig {
    private String mPowerParameterName = HwAppQoeUtils.INVALID_STRING_VALUE;
    private double mPowerParameterValue = -1.0d;

    public void setPowerParameterName(String powerParameterName) {
        this.mPowerParameterName = powerParameterName;
    }

    public String getPowerParameterName() {
        return this.mPowerParameterName;
    }

    public void setPowerParameterValue(double powerParameterValue) {
        this.mPowerParameterValue = powerParameterValue;
    }

    public double getPowerParameterValue() {
        return this.mPowerParameterValue;
    }
}

package com.huawei.android.hardware.fmradio;

public class FmConfig {
    public static BaseFmConfig mBaseFmConfig;

    public FmConfig() {
        mBaseFmConfig = new BaseFmConfig();
    }

    public int getRadioBand() {
        return mBaseFmConfig.getRadioBand();
    }

    public void setRadioBand(int band) {
        mBaseFmConfig.setRadioBand(band);
    }

    public int getEmphasis() {
        return mBaseFmConfig.getEmphasis();
    }

    public void setEmphasis(int emp) {
        mBaseFmConfig.setEmphasis(emp);
    }

    public int getChSpacing() {
        return mBaseFmConfig.getChSpacing();
    }

    public void setChSpacing(int spacing) {
        mBaseFmConfig.setChSpacing(spacing);
    }

    public int getRdsStd() {
        return mBaseFmConfig.getRdsStd();
    }

    public void setRdsStd(int rdsStandard) {
        mBaseFmConfig.setRdsStd(rdsStandard);
    }

    public int getLowerLimit() {
        return mBaseFmConfig.getLowerLimit();
    }

    public void setLowerLimit(int lowLimit) {
        mBaseFmConfig.setLowerLimit(lowLimit);
    }

    public int getUpperLimit() {
        return mBaseFmConfig.getUpperLimit();
    }

    public void setUpperLimit(int upLimit) {
        mBaseFmConfig.setUpperLimit(upLimit);
    }

    protected static boolean fmConfigure(int fd, FmConfig configSettings) {
        return BaseFmConfig.fmConfigure(fd, mBaseFmConfig);
    }
}

package com.huawei.android.hardware.fmradio;

import com.huawei.android.hardware.fmradio.common.BaseFmConfig;
import com.huawei.android.hardware.fmradio.common.FmUtils;
import com.huawei.android.hardware.hisifmradio.HisiFmConfig;
import com.huawei.android.hardware.mtkfmradio.MtkFmConfig;
import com.huawei.android.hardware.qcomfmradio.QcomFmConfig;

public class FmConfig {
    static BaseFmConfig mBaseFmConfig;

    public FmConfig() {
        if (FmUtils.isMtkPlatform()) {
            mBaseFmConfig = new MtkFmConfig();
        } else if (FmUtils.isQcomPlatform()) {
            mBaseFmConfig = new QcomFmConfig();
        } else {
            mBaseFmConfig = new HisiFmConfig();
        }
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

    protected static boolean fmConfigure(int fd, FmConfig fmconfig) {
        return false;
    }
}

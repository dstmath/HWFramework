package com.huawei.android.hardware.fmradio;

import android.util.Log;

public class FmConfig {
    private static final int FM_EU_BAND = 1;
    private static final int FM_JAPAN_STANDARD_BAND = 3;
    private static final int FM_JAPAN_WIDE_BAND = 2;
    private static final int FM_USER_DEFINED_BAND = 4;
    private static final int FM_US_BAND = 0;
    private static final String TAG = "FmConfig";
    private static final int V4L2_CID_PRIVATE_BASE = 134217728;
    private static final int V4L2_CID_PRIVATE_TAVARUA_EMPHASIS = 134217740;
    private static final int V4L2_CID_PRIVATE_TAVARUA_RDS_STD = 134217741;
    private static final int V4L2_CID_PRIVATE_TAVARUA_REGION = 134217735;
    private static final int V4L2_CID_PRIVATE_TAVARUA_SPACING = 134217742;
    private int mBandLowerLimit;
    private int mBandUpperLimit;
    private int mChSpacing;
    private int mEmphasis;
    private int mRadioBand;
    private int mRdsStd;

    public int getRadioBand() {
        return this.mRadioBand;
    }

    public void setRadioBand(int band) {
        this.mRadioBand = band;
    }

    public int getEmphasis() {
        return this.mEmphasis;
    }

    public void setEmphasis(int emp) {
        this.mEmphasis = emp;
    }

    public int getChSpacing() {
        return this.mChSpacing;
    }

    public void setChSpacing(int spacing) {
        this.mChSpacing = spacing;
    }

    public int getRdsStd() {
        return this.mRdsStd;
    }

    public void setRdsStd(int rdsStandard) {
        this.mRdsStd = rdsStandard;
    }

    public int getLowerLimit() {
        return this.mBandLowerLimit;
    }

    public void setLowerLimit(int lowLimit) {
        this.mBandLowerLimit = lowLimit;
    }

    public int getUpperLimit() {
        return this.mBandUpperLimit;
    }

    public void setUpperLimit(int upLimit) {
        this.mBandUpperLimit = upLimit;
    }

    protected static boolean fmConfigure(int fd, FmConfig configSettings) {
        Log.d(TAG, "In fmConfigure");
        int re = FmReceiverWrapper.setControlNative(fd, V4L2_CID_PRIVATE_TAVARUA_EMPHASIS, configSettings.getEmphasis());
        re = FmReceiverWrapper.setControlNative(fd, V4L2_CID_PRIVATE_TAVARUA_RDS_STD, configSettings.getRdsStd());
        re = FmReceiverWrapper.setControlNative(fd, V4L2_CID_PRIVATE_TAVARUA_SPACING, configSettings.getChSpacing());
        re = FmReceiverWrapper.setBandNative(fd, configSettings.getLowerLimit(), configSettings.getUpperLimit());
        if (FmReceiverWrapper.setControlNative(fd, V4L2_CID_PRIVATE_TAVARUA_REGION, 4) < 0) {
            return false;
        }
        return true;
    }
}

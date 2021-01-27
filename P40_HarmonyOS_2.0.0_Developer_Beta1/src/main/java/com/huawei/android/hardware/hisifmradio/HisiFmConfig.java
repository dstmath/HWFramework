package com.huawei.android.hardware.hisifmradio;

import android.util.Log;
import com.huawei.android.hardware.fmradio.common.BaseFmConfig;

public class HisiFmConfig implements BaseFmConfig {
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

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmConfig
    public int getRadioBand() {
        return this.mRadioBand;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmConfig
    public void setRadioBand(int band) {
        this.mRadioBand = band;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmConfig
    public int getEmphasis() {
        return this.mEmphasis;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmConfig
    public void setEmphasis(int emp) {
        this.mEmphasis = emp;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmConfig
    public int getChSpacing() {
        return this.mChSpacing;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmConfig
    public void setChSpacing(int spacing) {
        this.mChSpacing = spacing;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmConfig
    public int getRdsStd() {
        return this.mRdsStd;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmConfig
    public void setRdsStd(int rdsStandard) {
        this.mRdsStd = rdsStandard;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmConfig
    public int getLowerLimit() {
        return this.mBandLowerLimit;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmConfig
    public void setLowerLimit(int lowLimit) {
        this.mBandLowerLimit = lowLimit;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmConfig
    public int getUpperLimit() {
        return this.mBandUpperLimit;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmConfig
    public void setUpperLimit(int upLimit) {
        this.mBandUpperLimit = upLimit;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseFmConfig
    public boolean fmConfigure(int fd) {
        Log.d(TAG, "In fmConfigure");
        FmReceiverWrapper.setControlNative(fd, V4L2_CID_PRIVATE_TAVARUA_EMPHASIS, getEmphasis());
        FmReceiverWrapper.setControlNative(fd, V4L2_CID_PRIVATE_TAVARUA_RDS_STD, getRdsStd());
        FmReceiverWrapper.setControlNative(fd, V4L2_CID_PRIVATE_TAVARUA_SPACING, getChSpacing());
        FmReceiverWrapper.setBandNative(fd, getLowerLimit(), getUpperLimit());
        if (FmReceiverWrapper.setControlNative(fd, V4L2_CID_PRIVATE_TAVARUA_REGION, 4) < 0) {
            return false;
        }
        return true;
    }
}

package com.huawei.android.hardware.fmradio;

import android.util.Log;

public class FmRxRdsData {
    private static final String LOGTAG = "FmRxRdsData";
    private static final int RDS_AF_AUTO = 64;
    private static final int RDS_GROUP_AF = 4;
    private static final int RDS_GROUP_PS = 2;
    private static final int RDS_GROUP_RT = 1;
    private static final int RDS_PS_ALL = 16;
    private static final int V4L2_CID_PRIVATE_BASE = 134217728;
    private static final int V4L2_CID_PRIVATE_TAVARUA_PSALL = 134217748;
    private static final int V4L2_CID_PRIVATE_TAVARUA_RDSD_BUF = 134217747;
    private static final int V4L2_CID_PRIVATE_TAVARUA_RDSGROUP_MASK = 134217734;
    private static final int V4L2_CID_PRIVATE_TAVARUA_RDSGROUP_PROC = 134217744;
    private static final int V4L2_CID_PRIVATE_TAVARUA_RDSON = 134217743;
    private int mFd;
    private int mPrgmId;
    private String mPrgmServices;
    private int mPrgmType;
    private String mRadioText;

    public FmRxRdsData(int fd) {
        this.mFd = fd;
    }

    public int rdsOn(boolean on) {
        Log.d(LOGTAG, "In rdsOn: RDS is " + on);
        if (on) {
            return FmReceiverWrapper.setControlNative(this.mFd, V4L2_CID_PRIVATE_TAVARUA_RDSON, 1);
        }
        return FmReceiverWrapper.setControlNative(this.mFd, V4L2_CID_PRIVATE_TAVARUA_RDSON, 0);
    }

    public int rdsGrpOptions(int grpMask, int buffSize, boolean rdsFilter) {
        int rdsFilt;
        byte rds_group_mask = (byte) (((byte) FmReceiverWrapper.getControlNative(this.mFd, V4L2_CID_PRIVATE_TAVARUA_RDSGROUP_PROC)) & 254);
        if (rdsFilter) {
            rdsFilt = 1;
        } else {
            rdsFilt = 0;
        }
        int re = FmReceiverWrapper.setControlNative(this.mFd, V4L2_CID_PRIVATE_TAVARUA_RDSGROUP_PROC, (byte) (rds_group_mask | rdsFilt));
        if (re != 0) {
            return re;
        }
        re = FmReceiverWrapper.setControlNative(this.mFd, V4L2_CID_PRIVATE_TAVARUA_RDSD_BUF, buffSize);
        if (re != 0) {
            return re;
        }
        return FmReceiverWrapper.setControlNative(this.mFd, V4L2_CID_PRIVATE_TAVARUA_RDSGROUP_MASK, grpMask);
    }

    public int rdsOptions(int rdsMask) {
        byte rds_group_mask = (byte) FmReceiverWrapper.getControlNative(this.mFd, V4L2_CID_PRIVATE_TAVARUA_RDSGROUP_PROC);
        int psAllVal = rdsMask & 16;
        Log.d(LOGTAG, "In rdsOptions: rdsMask: " + rdsMask);
        int re = FmReceiverWrapper.setControlNative(this.mFd, V4L2_CID_PRIVATE_TAVARUA_RDSGROUP_PROC, (byte) (((rdsMask & 7) << 3) | ((byte) (rds_group_mask & 199))));
        return FmReceiverWrapper.setControlNative(this.mFd, V4L2_CID_PRIVATE_TAVARUA_PSALL, psAllVal >> 4);
    }

    public int enableAFjump(boolean AFenable) {
        Log.d(LOGTAG, "In enableAFjump: AFenable: " + AFenable);
        int rds_group_mask = FmReceiverWrapper.getControlNative(this.mFd, V4L2_CID_PRIVATE_TAVARUA_RDSGROUP_PROC);
        Log.d(LOGTAG, "In enableAFjump: rds_group_mask: " + rds_group_mask);
        if (AFenable) {
            FmReceiverWrapper.setControlNative(this.mFd, V4L2_CID_PRIVATE_TAVARUA_RDSGROUP_PROC, rds_group_mask | 64);
        } else {
            FmReceiverWrapper.setControlNative(this.mFd, V4L2_CID_PRIVATE_TAVARUA_RDSGROUP_PROC, rds_group_mask & -65);
        }
        return 1;
    }

    public String getRadioText() {
        return this.mRadioText;
    }

    public void setRadioText(String x) {
        this.mRadioText = x;
    }

    public String getPrgmServices() {
        return this.mPrgmServices;
    }

    public void setPrgmServices(String x) {
        this.mPrgmServices = x;
    }

    public int getPrgmId() {
        return this.mPrgmId;
    }

    public void setPrgmId(int x) {
        this.mPrgmId = x;
    }

    public int getPrgmType() {
        return this.mPrgmType;
    }

    public void setPrgmType(int x) {
        this.mPrgmType = x;
    }
}

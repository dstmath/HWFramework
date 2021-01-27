package com.huawei.android.hardware.qcomfmradio;

import android.media.BuildConfig;
import android.util.Log;

/* access modifiers changed from: package-private */
public class FmRxRdsData {
    private static final String LOGTAG = "FmRxRdsData";
    private static final int MAX_NUM_TAG = 2;
    private static final int MAX_TAG_CODES = 64;
    private static final int RDS_AF_AUTO = 64;
    private static final int RDS_AF_JUMP = 1;
    private static final int RDS_GROUP_AF = 4;
    private static final int RDS_GROUP_PS = 2;
    private static final int RDS_GROUP_RT = 1;
    private static final int RDS_PS_ALL = 16;
    private static final int V4L2_CID_PRIVATE_BASE = 134217728;
    private static final int V4L2_CID_PRIVATE_TAVARUA_AF_JUMP = 134217755;
    private static final int V4L2_CID_PRIVATE_TAVARUA_PSALL = 134217748;
    private static final int V4L2_CID_PRIVATE_TAVARUA_RDSD_BUF = 134217747;
    private static final int V4L2_CID_PRIVATE_TAVARUA_RDSGROUP_MASK = 134217734;
    private static final int V4L2_CID_PRIVATE_TAVARUA_RDSGROUP_PROC = 134217744;
    private static final int V4L2_CID_PRIVATE_TAVARUA_RDSON = 134217743;
    private boolean formatting_dir = false;
    private int mECountryCode;
    private String mERadioText = BuildConfig.FLAVOR;
    private int mFd;
    private int mPrgmId;
    private String mPrgmServices;
    private int mPrgmType;
    private String mRadioText = BuildConfig.FLAVOR;
    private String[] mTag = new String[2];
    private byte[] mTagCode = new byte[2];
    private boolean rt_ert_flag;
    private int tag_nums = 0;

    public FmRxRdsData(int fd) {
        this.mFd = fd;
    }

    public int rdsOn(boolean on) {
        Log.d(LOGTAG, "In rdsOn: RDS is " + on);
        if (on) {
            return FmReceiverJNI.setControlNative(this.mFd, V4L2_CID_PRIVATE_TAVARUA_RDSON, 1);
        }
        return FmReceiverJNI.setControlNative(this.mFd, V4L2_CID_PRIVATE_TAVARUA_RDSON, 0);
    }

    public int rdsGrpOptions(int grpMask, int buffSize, boolean rdsFilter) {
        int rdsFilt;
        int rds_group_mask = (byte) (((byte) FmReceiverJNI.getControlNative(this.mFd, V4L2_CID_PRIVATE_TAVARUA_RDSGROUP_PROC)) & 254);
        if (rdsFilter) {
            rdsFilt = 1;
        } else {
            rdsFilt = 0;
        }
        int re = FmReceiverJNI.setControlNative(this.mFd, V4L2_CID_PRIVATE_TAVARUA_RDSGROUP_PROC, (byte) (rds_group_mask | rdsFilt));
        if (re != 0) {
            return re;
        }
        int re2 = FmReceiverJNI.setControlNative(this.mFd, V4L2_CID_PRIVATE_TAVARUA_RDSD_BUF, buffSize);
        if (re2 != 0) {
            return re2;
        }
        return FmReceiverJNI.setControlNative(this.mFd, V4L2_CID_PRIVATE_TAVARUA_RDSGROUP_MASK, grpMask);
    }

    public int rdsOptions(int rdsMask) {
        FmReceiverJNI.getControlNative(this.mFd, V4L2_CID_PRIVATE_TAVARUA_RDSGROUP_PROC);
        int i = rdsMask & 16;
        Log.d(LOGTAG, "In rdsOptions: rdsMask: " + rdsMask);
        int rds_group_mask = rdsMask & 255;
        Log.d(LOGTAG, "In rdsOptions: rds_group_mask : " + rds_group_mask);
        return FmReceiverJNI.setControlNative(this.mFd, V4L2_CID_PRIVATE_TAVARUA_RDSGROUP_PROC, rds_group_mask);
    }

    public int enableAFjump(boolean AFenable) {
        int re;
        Log.d(LOGTAG, "In enableAFjump: AFenable : " + AFenable);
        int rds_group_mask = FmReceiverJNI.getControlNative(this.mFd, V4L2_CID_PRIVATE_TAVARUA_RDSGROUP_PROC);
        Log.d(LOGTAG, "Currently set rds_group_mask : " + rds_group_mask);
        if (AFenable) {
            re = FmReceiverJNI.setControlNative(this.mFd, V4L2_CID_PRIVATE_TAVARUA_AF_JUMP, 1);
        } else {
            re = FmReceiverJNI.setControlNative(this.mFd, V4L2_CID_PRIVATE_TAVARUA_AF_JUMP, 0);
        }
        int rds_group_mask2 = FmReceiverJNI.getControlNative(this.mFd, V4L2_CID_PRIVATE_TAVARUA_RDSGROUP_PROC);
        if (AFenable) {
            Log.d(LOGTAG, "After enabling the rds_group_mask is : " + rds_group_mask2);
        } else {
            Log.d(LOGTAG, "After disabling the rds_group_mask is : " + rds_group_mask2);
        }
        return re;
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

    public String getERadioText() {
        return this.mERadioText;
    }

    public void setERadioText(String x) {
        this.mERadioText = x;
    }

    public void setECountryCode(int x) {
        this.mECountryCode = x;
    }

    public int getECountryCode() {
        return this.mECountryCode;
    }

    public boolean getFormatDir() {
        return this.formatting_dir;
    }

    public void setFormatDir(boolean dir) {
        this.formatting_dir = dir;
    }

    public void setTagValue(String x, int tag_num) {
        if (tag_num > 0 && tag_num <= 2) {
            this.mTag[tag_num - 1] = x;
            this.tag_nums++;
        }
    }

    public void setTagCode(byte tag_code, int tag_num) {
        if (tag_num > 0 && tag_num <= 2) {
            this.mTagCode[tag_num - 1] = tag_code;
        }
    }

    public String getTagValue(int tag_num) {
        if (tag_num <= 0 || tag_num > 2) {
            return BuildConfig.FLAVOR;
        }
        return this.mTag[tag_num - 1];
    }

    public byte getTagCode(int tag_num) {
        if (tag_num <= 0 || tag_num > 2) {
            return 0;
        }
        return this.mTagCode[tag_num - 1];
    }

    public int getTagNums() {
        return this.tag_nums;
    }

    public void setTagNums(int x) {
        if (x >= 0 && x <= 2) {
            this.tag_nums = x;
        }
    }
}

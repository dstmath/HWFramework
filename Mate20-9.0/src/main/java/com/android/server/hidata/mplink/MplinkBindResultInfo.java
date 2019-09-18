package com.android.server.hidata.mplink;

import huawei.android.net.hwmplink.MpLinkCommonUtils;

public class MplinkBindResultInfo {
    private static final int BIND_FAIL_REASON_BASE = 100;
    public static final int BIND_FAIL_REASON_NO_CERTIFIED = 102;
    public static final int BIND_FAIL_REASON_UNCOEXIST = 101;
    public static final int MPLINK_BIND_FAIL = 2;
    public static final int MPLINK_BIND_SUCCESS = 1;
    public static final int MPLINK_UNBIND_FAIL = 4;
    public static final int MPLINK_UNBIND_SUCCESS = 3;
    private static final String TAG = "HiData_MplinkBindResultInfo";
    int mFailReason = -1;
    int mNetwork = -1;
    int mResult = -1;
    int mType = -1;
    int mUid = -1;

    public MplinkBindResultInfo() {
    }

    public MplinkBindResultInfo(int result, int network, int uid) {
    }

    public MplinkBindResultInfo(int result, int network, int uid, int failReason) {
    }

    public int getResult() {
        return this.mResult;
    }

    public void setResult(int result) {
        this.mResult = result;
    }

    public int getNetwork() {
        return this.mNetwork;
    }

    public void setNetwork(int network) {
        this.mNetwork = network;
    }

    public int getUid() {
        return this.mUid;
    }

    public void setUid(int uid) {
        this.mUid = uid;
    }

    public int getType() {
        return this.mType;
    }

    public void setType(int type) {
        this.mType = type;
    }

    public int getFailReason() {
        return this.mFailReason;
    }

    public void setFailReason(int failReason) {
        this.mFailReason = failReason;
    }

    public void reset() {
        this.mResult = -1;
        this.mNetwork = -1;
        this.mUid = -1;
        this.mType = -1;
        this.mFailReason = -1;
    }

    public void dump() {
        MpLinkCommonUtils.logI(TAG, "dump!");
    }
}

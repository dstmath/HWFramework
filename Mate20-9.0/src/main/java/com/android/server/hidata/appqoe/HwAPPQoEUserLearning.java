package com.android.server.hidata.appqoe;

import android.content.Context;

public class HwAPPQoEUserLearning {
    private static final String TAG = "HiData_HwAPPQoEUserLearning";
    private static HwAPPQoEUserLearning mUserLerning = null;
    public HwAPPQoEUserAction mUserAction = null;

    private HwAPPQoEUserLearning(Context context) {
        HwAPPQoEUtils.logD(TAG, "HwAPPQoEUserLearning, Init");
        this.mUserAction = HwAPPQoEUserAction.createHwAPPQoEUserAction(context);
    }

    public static HwAPPQoEUserLearning createHwAPPQoEUserLearning(Context context) {
        if (mUserLerning == null) {
            mUserLerning = new HwAPPQoEUserLearning(context);
        }
        return mUserLerning;
    }

    public static HwAPPQoEUserLearning getInstance() {
        return mUserLerning;
    }

    public int getUserTypeByAppId(int appId) {
        return this.mUserAction.getUserActionType(appId);
    }

    public void notifyAPPStateChange(long apkStartTime, long apkEndTime, int appId) {
        this.mUserAction.notifyAPPStateChange(apkStartTime, apkEndTime, appId);
    }

    public void setLatestAPPScenceId(int appScenceId) {
        this.mUserAction.setLatestAPPScenceId(appScenceId);
    }
}

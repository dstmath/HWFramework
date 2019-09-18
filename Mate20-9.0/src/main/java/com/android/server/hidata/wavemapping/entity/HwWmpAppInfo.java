package com.android.server.hidata.wavemapping.entity;

import com.android.server.hidata.appqoe.HwAPPStateInfo;
import com.android.server.hidata.wavemapping.cons.Constant;

public class HwWmpAppInfo extends HwAPPStateInfo {
    private String mAppName;
    private int mBadThreshold;
    private int mFullId;
    private boolean mMoniterApp;
    private long mStartTime;

    public HwWmpAppInfo(int AppId, int ScenceId, int AppUID, int AppType, int AppState, int NetworkType) {
        this.mAppId = AppId;
        this.mScenceId = ScenceId;
        this.mAppUID = AppUID;
        this.mAppType = AppType;
        this.mAppState = AppState;
        this.mNetworkType = NetworkType;
        if (2000 == AppType) {
            this.mFullId = Constant.transferGameId2FullId(AppId, ScenceId);
        } else {
            this.mFullId = ScenceId;
        }
        if (Constant.getSavedQoeAppList().containsKey(Integer.valueOf(this.mFullId))) {
            this.mAppName = Constant.USERDB_APP_NAME_PREFIX + this.mFullId;
            this.mMoniterApp = true;
        } else {
            this.mAppName = Constant.USERDB_APP_NAME_NONE;
            this.mMoniterApp = false;
        }
        this.mStartTime = 0;
    }

    public HwWmpAppInfo(int networkFakeApp) {
        if (1 == networkFakeApp) {
            this.mAppName = Constant.USERDB_APP_NAME_WIFI;
            this.mNetworkType = 800;
            this.mMoniterApp = true;
        } else if (networkFakeApp == 0) {
            this.mAppName = Constant.USERDB_APP_NAME_MOBILE;
            this.mNetworkType = 801;
            this.mMoniterApp = true;
        } else {
            this.mAppName = Constant.USERDB_APP_NAME_NONE;
            this.mNetworkType = 802;
            this.mMoniterApp = false;
        }
        this.mStartTime = 0;
    }

    public void setStartTime(long time) {
        this.mStartTime = time;
    }

    public long getStartTime() {
        return this.mStartTime;
    }

    public int getScenceId() {
        return this.mScenceId;
    }

    public int getAppUid() {
        return this.mAppUID;
    }

    public boolean isNormalApp() {
        if (-1 == this.mAppId) {
            return false;
        }
        return true;
    }

    public boolean isMonitorApp() {
        return this.mMoniterApp;
    }

    public String getAppName() {
        return this.mAppName;
    }

    public int getAppFullId() {
        return this.mFullId;
    }

    public int getConMgrNetworkType() {
        if (800 == this.mNetworkType) {
            return 1;
        }
        if (801 == this.mNetworkType) {
            return 0;
        }
        return 8;
    }

    public void setConMgrNetworkType(int net) {
        if (1 == net) {
            this.mNetworkType = 800;
        } else if (net == 0) {
            this.mNetworkType = 801;
        } else {
            this.mNetworkType = 802;
        }
    }

    public void copyObjectValue(HwWmpAppInfo tempAPPState) {
        if (tempAPPState != null) {
            this.mAppId = tempAPPState.mAppId;
            this.mScenceId = tempAPPState.mScenceId;
            this.mAction = tempAPPState.mAction;
            this.mAppUID = tempAPPState.mAppUID;
            this.mAppType = tempAPPState.mAppType;
            this.mAppState = tempAPPState.mAppState;
            this.mAppRTT = tempAPPState.mAppRTT;
            this.mNetworkType = tempAPPState.mNetworkType;
            this.mUserType = tempAPPState.mUserType;
            this.mAppName = tempAPPState.mAppName;
            this.mStartTime = tempAPPState.mStartTime;
        }
    }

    public String toString() {
        return "HwWmpAppInfo - mAppName:" + this.mAppName + " mAppId:" + this.mAppId + ", mScenceId:" + this.mScenceId + ", mAppUID:" + this.mAppUID + ", mAppType:" + this.mAppType + ", mAppState:" + this.mAppState + ", mNetworkType:" + this.mNetworkType + ", mStartTime:" + this.mStartTime;
    }
}

package com.android.server.hidata.wavemapping.entity;

import com.android.server.hidata.appqoe.HwAppStateInfo;
import com.android.server.hidata.wavemapping.cons.Constant;

public class HwWmpAppInfo extends HwAppStateInfo {
    private boolean isMonitorApp;
    private String mAppName;
    private int mBadThreshold;
    private int mFullId;
    private long mStartTime;

    public HwWmpAppInfo(int appId, int scenesId, int appUid, int appType, int appState, int networkType) {
        this.mAppId = appId;
        this.mScenesId = scenesId;
        this.mAppUid = appUid;
        this.mAppType = appType;
        this.mAppState = appState;
        this.mNetworkType = networkType;
        if (networkType == 2000) {
            this.mFullId = Constant.transferGameId2FullId(appId, scenesId);
        } else {
            this.mFullId = scenesId;
        }
        if (Constant.getSavedQoeAppList().containsKey(Integer.valueOf(this.mFullId))) {
            this.mAppName = Constant.USERDB_APP_NAME_PREFIX + this.mFullId;
            this.isMonitorApp = true;
        } else {
            this.mAppName = Constant.USERDB_APP_NAME_NONE;
            this.isMonitorApp = false;
        }
        this.mStartTime = 0;
    }

    public HwWmpAppInfo(int networkFakeApp) {
        if (networkFakeApp == 1) {
            this.mAppName = Constant.USERDB_APP_NAME_WIFI;
            this.mNetworkType = 800;
            this.isMonitorApp = true;
        } else if (networkFakeApp == 0) {
            this.mAppName = Constant.USERDB_APP_NAME_MOBILE;
            this.mNetworkType = 801;
            this.isMonitorApp = true;
        } else {
            this.mAppName = Constant.USERDB_APP_NAME_NONE;
            this.mNetworkType = 802;
            this.isMonitorApp = false;
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
        return this.mScenesId;
    }

    public int getAppUid() {
        return this.mAppUid;
    }

    public boolean isNormalApp() {
        if (this.mAppId == -1) {
            return false;
        }
        return true;
    }

    public boolean isMonitorApp() {
        return this.isMonitorApp;
    }

    public String getAppName() {
        return this.mAppName;
    }

    public int getAppFullId() {
        return this.mFullId;
    }

    public int getConMgrNetworkType() {
        if (this.mNetworkType == 800) {
            return 1;
        }
        if (this.mNetworkType == 801) {
            return 0;
        }
        return 8;
    }

    public void setConMgrNetworkType(int net) {
        if (net == 1) {
            this.mNetworkType = 800;
        } else if (net == 0) {
            this.mNetworkType = 801;
        } else {
            this.mNetworkType = 802;
        }
    }

    public void copyObjectValue(HwWmpAppInfo tempAppState) {
        if (tempAppState != null) {
            this.mAppId = tempAppState.mAppId;
            this.mScenesId = tempAppState.mScenesId;
            this.mAction = tempAppState.mAction;
            this.mAppUid = tempAppState.mAppUid;
            this.mAppType = tempAppState.mAppType;
            this.mAppState = tempAppState.mAppState;
            this.mAppRtt = tempAppState.mAppRtt;
            this.mNetworkType = tempAppState.mNetworkType;
            this.mUserType = tempAppState.mUserType;
            this.mAppName = tempAppState.mAppName;
            this.mStartTime = tempAppState.mStartTime;
        }
    }

    @Override // com.android.server.hidata.appqoe.HwAppStateInfo
    public String toString() {
        return "HwWmpAppInfo - mAppName:" + this.mAppName + " mAppId:" + this.mAppId + ", mScenceId:" + this.mScenesId + ", mAppUID:" + this.mAppUid + ", mAppType:" + this.mAppType + ", mAppState:" + this.mAppState + ", mNetworkType:" + this.mNetworkType + ", mStartTime:" + this.mStartTime;
    }
}

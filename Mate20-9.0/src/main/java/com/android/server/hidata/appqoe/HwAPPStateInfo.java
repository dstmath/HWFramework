package com.android.server.hidata.appqoe;

import com.android.server.hidata.mplink.MpLinkQuickSwitchConfiguration;

public class HwAPPStateInfo {
    public int mAction = -1;
    public int mAppId = -1;
    public int mAppPeriod = -1;
    public int mAppRTT = -1;
    public int mAppState = -1;
    public int mAppType = -1;
    public int mAppUID = -1;
    public boolean mIsAppFirstStart = false;
    public boolean mIsVideoStart = false;
    private MpLinkQuickSwitchConfiguration mMpLinkQuickSwitchConfiguration = new MpLinkQuickSwitchConfiguration();
    private int mNetworkStrategy = -1;
    public int mNetworkType = -1;
    public int mScenceId = -1;
    public int mScenceType = 0;
    private int mSocketStrategy = -1;
    public int mUserType = -1;

    public void setSocketStrategy(int mSocketStrategy2) {
        this.mSocketStrategy = mSocketStrategy2;
        this.mMpLinkQuickSwitchConfiguration.setSocketStrategy(mSocketStrategy2);
    }

    public void setNetworkStrategy(int mNetworkStrategy2) {
        this.mNetworkStrategy = mNetworkStrategy2;
        this.mMpLinkQuickSwitchConfiguration.setNetworkStrategy(mNetworkStrategy2);
    }

    public void setMpLinkQuickSwitchConfiguration(MpLinkQuickSwitchConfiguration configuration) {
        this.mMpLinkQuickSwitchConfiguration.copyObjectValue(configuration);
        if (configuration != null) {
            this.mSocketStrategy = configuration.getSocketStrategy();
            this.mNetworkStrategy = configuration.getNetworkStrategy();
        }
    }

    public int getSocketStrategy() {
        return this.mSocketStrategy;
    }

    public int getNetworkStrategy() {
        return this.mNetworkStrategy;
    }

    public MpLinkQuickSwitchConfiguration getQuickSwitchConfiguration() {
        this.mMpLinkQuickSwitchConfiguration.setAppId(this.mAppId);
        this.mMpLinkQuickSwitchConfiguration.setScenceId(this.mScenceId);
        return this.mMpLinkQuickSwitchConfiguration;
    }

    public boolean isObjectValueEqual(HwAPPStateInfo tempAPPState) {
        if (tempAPPState != null && this.mAppId == tempAPPState.mAppId && this.mScenceId == tempAPPState.mScenceId && this.mAppUID == tempAPPState.mAppUID && this.mAppType == tempAPPState.mAppType && this.mAppState == tempAPPState.mAppState && this.mAppRTT == tempAPPState.mAppRTT && this.mNetworkType == tempAPPState.mNetworkType && this.mAction == tempAPPState.mAction && this.mUserType == tempAPPState.mUserType && this.mScenceType == tempAPPState.mScenceType && this.mSocketStrategy == tempAPPState.getSocketStrategy() && this.mNetworkStrategy == tempAPPState.getNetworkStrategy() && this.mIsAppFirstStart == tempAPPState.mIsAppFirstStart && this.mIsVideoStart == tempAPPState.mIsVideoStart) {
            return true;
        }
        return false;
    }

    public void copyObjectValue(HwAPPStateInfo tempAPPState) {
        if (tempAPPState != null) {
            this.mAppId = tempAPPState.mAppId;
            this.mScenceId = tempAPPState.mScenceId;
            this.mScenceType = tempAPPState.mScenceType;
            this.mAction = tempAPPState.mAction;
            this.mAppUID = tempAPPState.mAppUID;
            this.mAppPeriod = tempAPPState.mAppPeriod;
            this.mAppType = tempAPPState.mAppType;
            this.mAppState = tempAPPState.mAppState;
            this.mAppRTT = tempAPPState.mAppRTT;
            this.mNetworkType = tempAPPState.mNetworkType;
            this.mUserType = tempAPPState.mUserType;
            this.mSocketStrategy = tempAPPState.getSocketStrategy();
            this.mNetworkStrategy = tempAPPState.getNetworkStrategy();
            this.mMpLinkQuickSwitchConfiguration.setSocketStrategy(this.mSocketStrategy);
            this.mMpLinkQuickSwitchConfiguration.setNetworkStrategy(this.mNetworkStrategy);
            this.mMpLinkQuickSwitchConfiguration.setScenceId(this.mScenceId);
            this.mMpLinkQuickSwitchConfiguration.setAppId(this.mAppId);
            this.mIsAppFirstStart = tempAPPState.mIsAppFirstStart;
            this.mIsVideoStart = tempAPPState.mIsVideoStart;
        }
    }

    public String toString() {
        return "HwAPPStateInfo  mAppId:" + this.mAppId + ", mScenceId:" + this.mScenceId + ", mScenceType:" + this.mScenceType + ", mAction" + this.mAction + ", mAppUID:" + this.mAppUID + ", mAppType:" + this.mAppType + ", mAppState:" + this.mAppState + ", mAppRTT:" + this.mAppRTT + " ,mNetworkType: " + this.mNetworkType + ",mUserType" + this.mUserType + ", SocketStrategy:" + this.mSocketStrategy + " ,NetworkStrategy: " + this.mNetworkStrategy + " ,mAppPeriod:" + this.mAppPeriod + " ,mIsAppFirstStart:" + this.mIsAppFirstStart + " ,mIsVideoStart:" + this.mIsVideoStart;
    }
}

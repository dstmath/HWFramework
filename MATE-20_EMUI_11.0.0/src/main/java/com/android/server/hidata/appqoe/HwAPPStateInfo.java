package com.android.server.hidata.appqoe;

public class HwAPPStateInfo {
    private int isMplinkEnteredFromCell = -1;
    public int mAction = -1;
    public int mAppId = -1;
    public int mAppPeriod = -1;
    public int mAppRTT = -1;
    private int mAppRegion = 0;
    public int mAppState = -1;
    public int mAppType = -1;
    public int mAppUID = -1;
    private int mCheckedCellChannelQuality = -1;
    private int mExperience = -1;
    private boolean mIsAppFirstStart = false;
    private boolean mIsVideoStart = false;
    private int mNetworkStrategy = -1;
    public int mNetworkType = -1;
    public int mScenceId = -1;
    public int mScenceType = 0;
    private int mSocketStrategy = -1;
    public int mUserType = -1;

    public int getAppRegion() {
        return this.mAppRegion;
    }

    public void setAppRegion(int appRegion) {
        this.mAppRegion = appRegion;
    }

    public void setSocketStrategy(int mSocketStrategy2) {
        this.mSocketStrategy = mSocketStrategy2;
    }

    public void setNetworkStrategy(int mNetworkStrategy2) {
        this.mNetworkStrategy = mNetworkStrategy2;
    }

    public void setCheckedCellChannelQuality(int mCheckedCellChannelQuality2) {
        this.mCheckedCellChannelQuality = mCheckedCellChannelQuality2;
    }

    public int getCheckedCellChannelQuality() {
        return this.mCheckedCellChannelQuality;
    }

    public void setIsMplinkEnteredFromCell(int isMplinkEnteredFromCell2) {
        this.isMplinkEnteredFromCell = isMplinkEnteredFromCell2;
    }

    public int getIsMplinkEnteredFromCell() {
        return this.isMplinkEnteredFromCell;
    }

    public int getExperience() {
        return this.mExperience;
    }

    public void setExperience(int experience) {
        this.mExperience = experience;
    }

    public int getSocketStrategy() {
        return this.mSocketStrategy;
    }

    public int getNetworkStrategy() {
        return this.mNetworkStrategy;
    }

    public void setIsAppFirstStart(boolean isAppFirstStart) {
        this.mIsAppFirstStart = isAppFirstStart;
    }

    public boolean getIsAppFirstStart() {
        return this.mIsAppFirstStart;
    }

    public void setIsVideoStart(boolean isVideoStart) {
        this.mIsVideoStart = isVideoStart;
    }

    public boolean getIsVideoStart() {
        return this.mIsVideoStart;
    }

    public boolean isObjectValueEqual(HwAPPStateInfo tempAPPState) {
        if (tempAPPState != null && this.mAppId == tempAPPState.mAppId && this.mScenceId == tempAPPState.mScenceId && this.mAppUID == tempAPPState.mAppUID && this.mAppType == tempAPPState.mAppType && this.mAppState == tempAPPState.mAppState && this.mAppRTT == tempAPPState.mAppRTT && this.mNetworkType == tempAPPState.mNetworkType && this.mAction == tempAPPState.mAction && this.mUserType == tempAPPState.mUserType && this.mScenceType == tempAPPState.mScenceType && this.mSocketStrategy == tempAPPState.getSocketStrategy() && this.mNetworkStrategy == tempAPPState.getNetworkStrategy() && this.mIsAppFirstStart == tempAPPState.getIsAppFirstStart() && this.mIsVideoStart == tempAPPState.getIsVideoStart()) {
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
            this.mCheckedCellChannelQuality = tempAPPState.mCheckedCellChannelQuality;
            this.mAppRTT = tempAPPState.mAppRTT;
            this.mNetworkType = tempAPPState.mNetworkType;
            this.mUserType = tempAPPState.mUserType;
            this.mSocketStrategy = tempAPPState.getSocketStrategy();
            this.mNetworkStrategy = tempAPPState.getNetworkStrategy();
            this.mAppRegion = tempAPPState.getAppRegion();
            this.mIsAppFirstStart = tempAPPState.getIsAppFirstStart();
            this.mIsVideoStart = tempAPPState.getIsVideoStart();
            this.mExperience = tempAPPState.getExperience();
        }
    }

    public String toString() {
        return "HwAPPStateInfo  mAppId:" + this.mAppId + ", mScenceId:" + this.mScenceId + ", mScenceType:" + this.mScenceType + ", mAction" + this.mAction + ", mAppUID:" + this.mAppUID + ", mAppType:" + this.mAppType + ", mAppState:" + this.mAppState + ", mAppRTT:" + this.mAppRTT + " ,mNetworkType: " + this.mNetworkType + ",mUserType" + this.mUserType + ", SocketStrategy:" + this.mSocketStrategy + " ,NetworkStrategy: " + this.mNetworkStrategy + ", mAppPeriod:" + this.mAppPeriod + ", mIsAppFirstStart:" + this.mIsAppFirstStart + ", mIsVideoStart:" + this.mIsVideoStart + ", mExperience: " + this.mExperience;
    }
}

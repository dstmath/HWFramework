package com.android.server.hidata.appqoe;

public class HwAppStateInfo {
    private int isMpLinkEnteredFromCell = -1;
    public int mAction = -1;
    public int mAppId = -1;
    public int mAppPeriod = -1;
    private int mAppRegion = 0;
    public int mAppRtt = -1;
    public int mAppState = -1;
    public int mAppType = -1;
    public int mAppUid = -1;
    private int mCheckedCellChannelQuality = -1;
    private int mExperience = -1;
    private boolean mIsAppFirstStart = false;
    private boolean mIsVideoStart = false;
    private int mNetworkStrategy = -1;
    public int mNetworkType = -1;
    public int mScenesId = -1;
    public int mScenesType = 0;
    private int mSocketStrategy = -1;
    public int mUserType = -1;

    public int getAppRegion() {
        return this.mAppRegion;
    }

    public void setAppRegion(int appRegion) {
        this.mAppRegion = appRegion;
    }

    public void setCheckedCellChannelQuality(int mCheckedCellChannelQuality2) {
        this.mCheckedCellChannelQuality = mCheckedCellChannelQuality2;
    }

    public int getCheckedCellChannelQuality() {
        return this.mCheckedCellChannelQuality;
    }

    public void setIsMpLinkEnteredFromCell(int isMpLinkEnteredFromCell2) {
        this.isMpLinkEnteredFromCell = isMpLinkEnteredFromCell2;
    }

    public int getIsMpLinkEnteredFromCell() {
        return this.isMpLinkEnteredFromCell;
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

    public boolean isObjectValueEqual(HwAppStateInfo tempAppState) {
        if (tempAppState != null && this.mAppId == tempAppState.mAppId && this.mScenesId == tempAppState.mScenesId && this.mAppUid == tempAppState.mAppUid && this.mAppType == tempAppState.mAppType && this.mAppState == tempAppState.mAppState && this.mAppRtt == tempAppState.mAppRtt && this.mNetworkType == tempAppState.mNetworkType && this.mAction == tempAppState.mAction && this.mUserType == tempAppState.mUserType && this.mScenesType == tempAppState.mScenesType && this.mSocketStrategy == tempAppState.getSocketStrategy() && this.mNetworkStrategy == tempAppState.getNetworkStrategy() && this.mIsAppFirstStart == tempAppState.getIsAppFirstStart() && this.mIsVideoStart == tempAppState.getIsVideoStart()) {
            return true;
        }
        return false;
    }

    public void copyObjectValue(HwAppStateInfo tempAppState) {
        if (tempAppState != null) {
            this.mAppId = tempAppState.mAppId;
            this.mScenesId = tempAppState.mScenesId;
            this.mScenesType = tempAppState.mScenesType;
            this.mAction = tempAppState.mAction;
            this.mAppUid = tempAppState.mAppUid;
            this.mAppPeriod = tempAppState.mAppPeriod;
            this.mAppType = tempAppState.mAppType;
            this.mAppState = tempAppState.mAppState;
            this.mCheckedCellChannelQuality = tempAppState.mCheckedCellChannelQuality;
            this.mAppRtt = tempAppState.mAppRtt;
            this.mNetworkType = tempAppState.mNetworkType;
            this.mUserType = tempAppState.mUserType;
            this.mSocketStrategy = tempAppState.getSocketStrategy();
            this.mNetworkStrategy = tempAppState.getNetworkStrategy();
            this.mAppRegion = tempAppState.getAppRegion();
            this.mIsAppFirstStart = tempAppState.getIsAppFirstStart();
            this.mIsVideoStart = tempAppState.getIsVideoStart();
            this.mExperience = tempAppState.getExperience();
        }
    }

    public String toString() {
        return "HwAPPStateInfo  mAppId:" + this.mAppId + ", mScenesId:" + this.mScenesId + ", mScenesType:" + this.mScenesType + ", mAction" + this.mAction + ", mAppUID:" + this.mAppUid + ", mAppType:" + this.mAppType + ", mAppState:" + this.mAppState + ", mAppRTT:" + this.mAppRtt + " ,mNetworkType: " + this.mNetworkType + ",mUserType" + this.mUserType + ", SocketStrategy:" + this.mSocketStrategy + " ,NetworkStrategy: " + this.mNetworkStrategy + ", mAppPeriod:" + this.mAppPeriod + ", mIsAppFirstStart:" + this.mIsAppFirstStart + ", mIsVideoStart:" + this.mIsVideoStart + ", mExperience: " + this.mExperience;
    }
}

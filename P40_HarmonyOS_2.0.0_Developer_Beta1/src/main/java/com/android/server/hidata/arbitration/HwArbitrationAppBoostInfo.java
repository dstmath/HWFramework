package com.android.server.hidata.arbitration;

import com.android.server.hidata.appqoe.HwAppStateInfo;

public class HwArbitrationAppBoostInfo {
    public boolean isCoexist = false;
    public boolean isInMpLink = false;
    public int mAppId = -1;
    private HwAppStateInfo mHwAppStateInfo = null;
    public int mNetwork = -1;
    public int mSceneId = -1;
    public int mSolution = -1;
    public int mUid = -1;

    public void setHwAppStateInfo(HwAppStateInfo mHwAppStateInfo2) {
        this.mHwAppStateInfo = mHwAppStateInfo2;
    }

    public HwAppStateInfo getHwAppStateInfo() {
        return this.mHwAppStateInfo;
    }

    public String toString() {
        return "HwArbitrationAppBoostInfo AppID " + this.mAppId + " mUID, " + this.mUid + ", mSceneId:" + this.mSceneId + ", mNetwork:" + this.mNetwork + ", mIsCoex:" + this.isCoexist + ", mIsMPLink:" + this.isInMpLink + ",  mSolution:" + this.mSolution;
    }

    public int getAppId() {
        return this.mAppId;
    }

    public void setAppId(int AppID) {
        this.mAppId = AppID;
    }

    public int getBoostUid() {
        return this.mUid;
    }

    public void setBoostUid(int UID) {
        this.mUid = UID;
    }

    public int getSceneId() {
        return this.mSceneId;
    }

    public void setSceneId(int sceneId) {
        this.mSceneId = sceneId;
    }

    public int getNetwork() {
        return this.mNetwork;
    }

    public void setNetwork(int network) {
        this.mNetwork = network;
    }

    public boolean isInMpLink() {
        return this.isInMpLink;
    }

    public void setInMpLink(boolean isMpLink) {
        this.isInMpLink = isMpLink;
    }

    public int getSolution() {
        return this.mSolution;
    }

    public void setSolution(int solution) {
        this.mSolution = solution;
    }

    public boolean isCoexist() {
        return this.isCoexist;
    }

    public void setCoexist(boolean isCoexist2) {
        this.isCoexist = isCoexist2;
    }
}

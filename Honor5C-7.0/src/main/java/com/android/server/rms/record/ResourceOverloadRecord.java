package com.android.server.rms.record;

public class ResourceOverloadRecord {
    private int mCountOverLoadNum;
    private int mPid;
    private String mPkg;
    private int mResourceType;
    private int mSpeedOverLoadPeroid;
    private int mSpeedOverloadNum;
    private int mUid;

    ResourceOverloadRecord() {
    }

    public int getUid() {
        return this.mUid;
    }

    public int getPid() {
        return this.mPid;
    }

    public String getPackageName() {
        return this.mPkg;
    }

    public int getResourceType() {
        return this.mResourceType;
    }

    public int getSpeedOverloadNum() {
        return this.mSpeedOverloadNum;
    }

    public int getSpeedOverLoadPeroid() {
        return this.mSpeedOverLoadPeroid;
    }

    public int getCountOverLoadNum() {
        return this.mCountOverLoadNum;
    }

    public void setUid(int uid) {
        this.mUid = uid;
    }

    public void setPid(int pid) {
        this.mPid = pid;
    }

    public void setPackageName(String pkg) {
        if (pkg != null) {
            this.mPkg = pkg;
        }
    }

    public void setResourceType(int resourceType) {
        this.mResourceType = resourceType;
    }

    public void setSpeedOverloadNum(int speedOverloadNum) {
        this.mSpeedOverloadNum = speedOverloadNum;
    }

    public void setSpeedOverLoadPeroid(int speedOverLoadPeroid) {
        this.mSpeedOverLoadPeroid = speedOverLoadPeroid;
    }

    public void setCountOverLoadNum(int countOverLoadNum) {
        this.mCountOverLoadNum = countOverLoadNum;
    }
}

package tmsdk.common.module.optimus.impl.bean;

import tmsdk.common.module.optimus.BsFakeType;

public class BsCloudResult {
    public BsFakeType cloudFakeType = BsFakeType.UNKNOW;
    public float cloudScore;
    public boolean lastSmsIsFake;
    public short smsType;

    public BsFakeType getCloudFakeType() {
        return this.cloudFakeType;
    }

    public float getCloudScore() {
        return this.cloudScore;
    }

    public short getSmsType() {
        return this.smsType;
    }

    public boolean isLastSmsIsFake() {
        return this.lastSmsIsFake;
    }

    public void setCloudFakeType(int i) {
        this.cloudFakeType = BsFakeType.values()[i];
    }

    public void setCloudFakeType(BsFakeType bsFakeType) {
        this.cloudFakeType = bsFakeType;
    }

    public void setCloudScore(float f) {
        this.cloudScore = f;
    }

    public void setLastSmsIsFake(boolean z) {
        this.lastSmsIsFake = z;
    }

    public void setSmsType(short s) {
        this.smsType = (short) s;
    }

    public String toString() {
        return "BsCloudResult [cloudFakeType=" + this.cloudFakeType + ", cloudScore=" + this.cloudScore + ", smsType=" + this.smsType + ", lastSmsIsFake=" + this.lastSmsIsFake + "]";
    }
}

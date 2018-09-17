package tmsdk.common.module.optimus.impl.bean;

import tmsdk.common.module.optimus.BsFakeType;

public class BsResult {
    public BsFakeType fakeType = BsFakeType.UNKNOW;
    public int lastSmsIsFake;

    public void setFakeType(int i) {
        this.fakeType = BsFakeType.values()[i];
    }

    public void setFakeType(BsFakeType bsFakeType) {
        this.fakeType = bsFakeType;
    }

    public void setLastSmsIsFake(int i) {
        this.lastSmsIsFake = i;
    }

    public String toString() {
        return "BsResult [fakeType=" + this.fakeType + ", lastSmsIsFake=" + this.lastSmsIsFake + "]";
    }
}

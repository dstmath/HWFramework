package com.huawei.nearbysdk;

public class SDKTlvData {
    private byte[] data;
    private int type;

    public int getType() {
        return this.type;
    }

    public void setType(int type2) {
        this.type = type2;
    }

    public byte[] getData() {
        return (byte[]) this.data.clone();
    }

    public void setData(byte[] data2) {
        this.data = (byte[]) data2.clone();
    }
}

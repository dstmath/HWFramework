package com.huawei.nearbysdk;

public class SDKTlvData {
    private byte[] data;
    private int type;

    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public byte[] getData() {
        return (byte[]) this.data.clone();
    }

    public void setData(byte[] data) {
        this.data = (byte[]) data.clone();
    }
}

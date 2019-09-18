package com.huawei.device.connectivitychrlog;

public class LogByte {
    private static final String LOG_TAG = "LogByte";
    private int length = 1;
    private byte value = 0;

    public byte getValue() {
        return this.value;
    }

    public void setValue(byte value2) {
        this.value = value2;
    }

    public void setByByteArray(byte[] src, int len, boolean bIsLittleEndian) {
        if (this.length != len) {
            ChrLog.chrLogE(LOG_TAG, "setByByteArray failed ,not support len = " + len);
        }
        this.value = src[0];
        ChrLog.chrLogD(LOG_TAG, "setByByteArray value = " + this.value);
    }

    public void setValue(int value2) {
        this.value = (byte) value2;
    }

    public int getLength() {
        return this.length;
    }

    public byte[] toByteArray() {
        return new byte[]{this.value};
    }
}

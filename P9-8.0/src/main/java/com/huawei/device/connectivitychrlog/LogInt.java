package com.huawei.device.connectivitychrlog;

public class LogInt {
    private static final String LOG_TAG = "LogInt";
    private byte[] bytesValue = null;
    private int length = 4;
    private int value = 0;

    public int getValue() {
        return this.value;
    }

    public void setValue(int value) {
        this.value = value;
        this.bytesValue = ByteConvert.intToBytes(this.value);
    }

    public void setByByteArray(byte[] src, int len, boolean bIsLittleEndian) {
        if (this.length != len) {
            ChrLog.chrLogE(LOG_TAG, "setByByteArray failed ,not support len = " + len);
        }
        this.value = ByteConvert.littleEndianBytesToInt(src);
        this.bytesValue = ByteConvert.intToBytes(this.value);
        ChrLog.chrLogD(LOG_TAG, "setByByteArray value = " + this.value);
    }

    public int getLength() {
        return this.length;
    }

    public byte[] toByteArray() {
        if (this.bytesValue != null) {
            return (byte[]) this.bytesValue.clone();
        }
        return ByteConvert.intToBytes(this.value);
    }
}

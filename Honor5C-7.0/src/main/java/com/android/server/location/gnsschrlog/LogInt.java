package com.android.server.location.gnsschrlog;

public class LogInt {
    private static final String LOG_TAG = "LogInt";
    private byte[] bytesValue;
    private int length;
    private int value;

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

    public LogInt() {
        this.length = 4;
        this.bytesValue = null;
        this.value = 0;
    }

    public byte[] toByteArray() {
        if (this.bytesValue != null) {
            return (byte[]) this.bytesValue.clone();
        }
        return ByteConvert.intToBytes(this.value);
    }
}

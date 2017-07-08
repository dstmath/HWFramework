package com.android.server.location.gnsschrlog;

public class LogShort {
    private static final String LOG_TAG = "LogShort";
    private byte[] bytesValue;
    private int length;
    private short value;

    public short getValue() {
        return this.value;
    }

    public void setValue(short value) {
        this.value = value;
        this.bytesValue = ByteConvert.shortToBytes(this.value);
    }

    public void setValue(int value) {
        this.value = (short) value;
        this.bytesValue = ByteConvert.shortToBytes(this.value);
    }

    public void setByByteArray(byte[] src, int len, boolean bIsLittleEndian) {
        if (this.length != len) {
            ChrLog.chrLogE(LOG_TAG, "setByByteArray failed ,not support len = " + len);
        }
        this.value = ByteConvert.littleEndianBytesToShort(src);
        this.bytesValue = ByteConvert.shortToBytes(this.value);
        ChrLog.chrLogD(LOG_TAG, "setByByteArray this.value = " + this.value);
    }

    public int getLength() {
        return this.length;
    }

    public LogShort() {
        this.length = 2;
        this.bytesValue = null;
        this.value = (short) 0;
    }

    public byte[] toByteArray() {
        if (this.bytesValue != null) {
            return (byte[]) this.bytesValue.clone();
        }
        return ByteConvert.shortToBytes(this.value);
    }
}

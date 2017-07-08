package com.android.server.location.gnsschrlog;

public class LogLong {
    private static final String LOG_TAG = "LogLong";
    private byte[] bytesValue;
    private int length;
    private long value;

    public long getValue() {
        return this.value;
    }

    public void setValue(long value) {
        this.value = value;
        this.bytesValue = ByteConvert.longToBytes(this.value);
    }

    public void setValue(int value) {
        this.value = (long) value;
        this.bytesValue = ByteConvert.longToBytes(this.value);
    }

    public void setByByteArray(byte[] src, int len, boolean bIsLittleEndian) {
        if (this.length != len) {
            ChrLog.chrLogE(LOG_TAG, "setByByteArray failed ,not support len = " + len);
        }
        this.value = ByteConvert.littleEndianbytesToLong(src);
        this.bytesValue = ByteConvert.longToBytes(this.value);
        ChrLog.chrLogD(LOG_TAG, "setByByteArray value = " + this.value);
    }

    public int getLength() {
        return this.length;
    }

    public LogLong() {
        this.length = 8;
        this.bytesValue = null;
        this.value = 0;
    }

    public byte[] toByteArray() {
        if (this.bytesValue != null) {
            return (byte[]) this.bytesValue.clone();
        }
        return ByteConvert.longToBytes(this.value);
    }
}

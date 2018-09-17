package com.huawei.device.connectivitychrlog;

import java.nio.ByteBuffer;

public class LogByteArray {
    private static final String LOG_TAG = "LogByteArray";
    private int length;
    private byte[] value;

    public int getLength() {
        return this.length;
    }

    public byte[] getValue() {
        return (byte[]) this.value.clone();
    }

    public void setValue(byte[] value) {
        this.value = (byte[]) value.clone();
    }

    public void setByByteArray(byte[] src, int len, boolean bIsLittleEndian) {
        if (this.length != len) {
            ChrLog.chrLogE(LOG_TAG, "setByByteArray failed ,not support len = " + len);
        }
        this.value = ByteConvert.littleEndianBytesToBigEndianBytes(this.value, this.length);
        ChrLog.chrLogI(LOG_TAG, "setByByteArray ");
    }

    public LogByteArray(int length) {
        this.length = length;
        this.value = new byte[length];
    }

    public byte[] toByteArray() {
        ByteBuffer bytebuf = ByteBuffer.wrap(new byte[this.length]);
        if (this.value.length > this.length) {
            bytebuf.put(this.value, 0, this.length);
        } else {
            bytebuf.put(this.value);
        }
        return bytebuf.array();
    }
}

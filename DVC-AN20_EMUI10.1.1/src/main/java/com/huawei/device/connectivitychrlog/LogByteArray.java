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

    public void setValue(byte[] value2) {
        this.value = (byte[]) value2.clone();
    }

    public void setByByteArray(byte[] src, int len, boolean bIsLittleEndian) {
        if (this.length != len) {
            ChrLog.chrLogE(LOG_TAG, false, "setByByteArray failed ,not support len = %{public}d", Integer.valueOf(len));
        }
        this.value = ByteConvert.littleEndianBytesToBigEndianBytes(this.value, this.length);
        ChrLog.chrLogI(LOG_TAG, false, "setByByteArray ", new Object[0]);
    }

    public LogByteArray(int length2) {
        this.length = length2;
        this.value = new byte[length2];
    }

    public byte[] toByteArray() {
        ByteBuffer bytebuf = ByteBuffer.wrap(new byte[this.length]);
        byte[] bArr = this.value;
        int length2 = bArr.length;
        int i = this.length;
        if (length2 > i) {
            bytebuf.put(bArr, 0, i);
        } else {
            bytebuf.put(bArr);
        }
        return bytebuf.array();
    }
}

package com.huawei.device.connectivitychrlog;

public class LogInt {
    private static final String LOG_TAG = "LogInt";
    private byte[] bytesValue = null;
    private int length = 4;
    private int value = 0;

    public int getValue() {
        return this.value;
    }

    public void setValue(int value2) {
        this.value = value2;
        this.bytesValue = ByteConvert.intToBytes(this.value);
    }

    public void setByByteArray(byte[] src, int len, boolean bIsLittleEndian) {
        if (this.length != len) {
            ChrLog.chrLogE(LOG_TAG, false, "setByByteArray failed ,not support len = %{public}d", Integer.valueOf(len));
        }
        this.value = ByteConvert.littleEndianBytesToInt(src);
        this.bytesValue = ByteConvert.intToBytes(this.value);
        ChrLog.chrLogD(LOG_TAG, false, "setByByteArray value = %{public}d", Integer.valueOf(this.value));
    }

    public int getLength() {
        return this.length;
    }

    public byte[] toByteArray() {
        byte[] bArr = this.bytesValue;
        if (bArr != null) {
            return (byte[]) bArr.clone();
        }
        return ByteConvert.intToBytes(this.value);
    }
}

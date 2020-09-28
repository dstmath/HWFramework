package com.huawei.device.connectivitychrlog;

public class LogShort {
    private static final String LOG_TAG = "LogShort";
    private byte[] bytesValue = null;
    private int length = 2;
    private short value = 0;

    public short getValue() {
        return this.value;
    }

    public void setValue(short value2) {
        this.value = value2;
        this.bytesValue = ByteConvert.shortToBytes(this.value);
    }

    public void setValue(int value2) {
        this.value = (short) value2;
        this.bytesValue = ByteConvert.shortToBytes(this.value);
    }

    public void setByByteArray(byte[] src, int len, boolean bIsLittleEndian) {
        if (this.length != len) {
            ChrLog.chrLogE(LOG_TAG, false, "setByByteArray failed ,not support len = %{public}d", Integer.valueOf(len));
        }
        this.value = ByteConvert.littleEndianBytesToShort(src);
        this.bytesValue = ByteConvert.shortToBytes(this.value);
        ChrLog.chrLogD(LOG_TAG, false, "setByByteArray this.value = %{public}d", Short.valueOf(this.value));
    }

    public int getLength() {
        return this.length;
    }

    public byte[] toByteArray() {
        byte[] bArr = this.bytesValue;
        if (bArr != null) {
            return (byte[]) bArr.clone();
        }
        return ByteConvert.shortToBytes(this.value);
    }
}

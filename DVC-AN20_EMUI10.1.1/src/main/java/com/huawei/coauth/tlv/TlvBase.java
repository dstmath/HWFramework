package com.huawei.coauth.tlv;

import java.util.Arrays;

public class TlvBase {
    private int len;
    private int type;
    private byte[] value;

    private TlvBase() {
    }

    public TlvBase(int type2, int len2, byte[] value2) {
        this.type = type2;
        this.len = len2;
        if (value2 == null || value2.length == 0) {
            this.value = new byte[0];
            return;
        }
        byte[] out = new byte[value2.length];
        System.arraycopy(value2, 0, out, 0, value2.length);
        this.value = out;
    }

    public boolean isEmpty() {
        byte[] bArr;
        return this.len == 0 || (bArr = this.value) == null || bArr.length == 0;
    }

    public int getType() {
        return this.type;
    }

    public void setType(int type2) {
        this.type = type2;
    }

    public int getLen() {
        return this.len;
    }

    public void setLen(int len2) {
        this.len = len2;
    }

    public byte[] getValue() {
        byte[] bArr = this.value;
        if (bArr == null || bArr.length == 0) {
            return new byte[0];
        }
        byte[] out = new byte[bArr.length];
        System.arraycopy(bArr, 0, out, 0, bArr.length);
        return out;
    }

    public void setValue(byte[] value2) {
        if (value2 == null || value2.length == 0) {
            this.value = new byte[0];
            return;
        }
        byte[] out = new byte[value2.length];
        System.arraycopy(value2, 0, out, 0, value2.length);
        this.value = out;
    }

    public String toString() {
        return "TlvBase{type=" + this.type + ", len=" + this.len + ", value=" + Arrays.toString(this.value) + '}';
    }
}

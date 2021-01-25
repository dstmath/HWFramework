package com.huawei.coauth.auth.authentity;

import java.util.Arrays;

public class CoAuthPropertyEntity extends CoAuthRspBaseEntity {
    private int result;
    private byte[] value;

    public int getResult() {
        return this.result;
    }

    public void setResult(int result2) {
        this.result = result2;
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
        if (value2 != null) {
            this.value = Arrays.copyOf(value2, value2.length);
        }
    }
}

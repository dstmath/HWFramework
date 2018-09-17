package com.huawei.internal.telephony.uicc;

import com.android.internal.telephony.uicc.IccIoResult;

public class IccIoResultEx {
    private IccIoResult mIccIoResult;
    public byte[] payload;
    public int sw1;
    public int sw2;

    public IccIoResultEx(Object obj) {
        this((IccIoResult) obj);
    }

    public IccIoResultEx(IccIoResult iir) {
        this.sw1 = iir.sw1;
        this.sw2 = iir.sw2;
        this.payload = iir.payload;
        this.mIccIoResult = new IccIoResult(this.sw1, this.sw2, this.payload);
    }

    public IccIoResultEx(int sw1, int sw2, byte[] payload) {
        this.mIccIoResult = new IccIoResult(sw1, sw2, payload);
    }

    public IccIoResultEx(int sw1, int sw2, String hexString) {
        this.mIccIoResult = new IccIoResult(sw1, sw2, hexString);
    }

    public String toString() {
        return this.mIccIoResult.toString();
    }

    public boolean success() {
        return this.mIccIoResult.success();
    }

    public IccIoResult makeNewIccIoResult(int sw1, int sw2, byte[] payload) {
        return new IccIoResult(sw1, sw2, payload);
    }
}

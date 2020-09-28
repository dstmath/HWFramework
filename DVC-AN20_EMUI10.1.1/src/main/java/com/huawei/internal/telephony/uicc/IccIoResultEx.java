package com.huawei.internal.telephony.uicc;

import com.android.internal.telephony.uicc.IccIoResult;

public class IccIoResultEx {
    private IccIoResult mIccIoResult;
    public byte[] payload;
    public int sw1;
    public int sw2;

    public IccIoResultEx() {
    }

    public IccIoResultEx(Object obj) {
        this((IccIoResult) obj);
    }

    public IccIoResultEx(IccIoResult iir) {
        this.sw1 = iir.sw1;
        this.sw2 = iir.sw2;
        this.payload = iir.payload;
        this.mIccIoResult = new IccIoResult(this.sw1, this.sw2, this.payload);
    }

    public IccIoResultEx(int sw12, int sw22, byte[] payload2) {
        this.mIccIoResult = new IccIoResult(sw12, sw22, payload2);
    }

    public IccIoResultEx(int sw12, int sw22, String hexString) {
        this.mIccIoResult = new IccIoResult(sw12, sw22, hexString);
    }

    public String toString() {
        return this.mIccIoResult.toString();
    }

    public boolean success() {
        return this.mIccIoResult.success();
    }

    public IccIoResult makeNewIccIoResult(int sw12, int sw22, byte[] payload2) {
        return new IccIoResult(sw12, sw22, payload2);
    }
}

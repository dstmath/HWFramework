package com.huawei.internal.telephony;

import com.android.internal.telephony.uicc.IccIoResult;

public class IccIoResultExt {
    private static int INVALID_TYPE = -1;
    private IccIoResult mIccIoResult;

    public IccIoResultExt() {
    }

    public IccIoResultExt(Object obj) {
        this((IccIoResult) obj);
    }

    public IccIoResultExt(IccIoResult iir) {
        this.mIccIoResult = new IccIoResult(iir.sw1, iir.sw2, iir.payload);
    }

    public boolean success() {
        IccIoResult iccIoResult = this.mIccIoResult;
        if (iccIoResult != null) {
            return iccIoResult.success();
        }
        return false;
    }

    public int getIccIoSw1() {
        IccIoResult iccIoResult = this.mIccIoResult;
        if (iccIoResult != null) {
            return iccIoResult.sw1;
        }
        return INVALID_TYPE;
    }

    public int getIccIoSw2() {
        IccIoResult iccIoResult = this.mIccIoResult;
        if (iccIoResult != null) {
            return iccIoResult.sw2;
        }
        return INVALID_TYPE;
    }

    public byte[] getPayload() {
        IccIoResult iccIoResult = this.mIccIoResult;
        if (iccIoResult != null) {
            return iccIoResult.payload;
        }
        return null;
    }
}

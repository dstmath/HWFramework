package com.huawei.internal.telephony;

import com.android.internal.telephony.uicc.IccIoResult;
import com.android.internal.telephony.uicc.IccUtils;

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

    public void setIccIoResult(Object object) {
        if (object instanceof IccIoResult) {
            this.mIccIoResult = (IccIoResult) object;
        }
    }

    public Exception getException() {
        IccIoResult iccIoResult = this.mIccIoResult;
        if (iccIoResult != null) {
            return iccIoResult.getException();
        }
        return null;
    }

    public boolean isValidIccioResult() {
        IccIoResult iccIoResult = this.mIccIoResult;
        if (iccIoResult != null) {
            return iccIoResult.isValidIccioResult();
        }
        return false;
    }

    public int getFileId() {
        IccIoResult iccIoResult = this.mIccIoResult;
        if (iccIoResult != null) {
            return iccIoResult.getFileId();
        }
        return 0;
    }

    public Object makeIccRecords() {
        IccIoResult iccIoResult = this.mIccIoResult;
        if (iccIoResult != null) {
            return new IccIoResult(iccIoResult.sw1, this.mIccIoResult.sw2, IccUtils.bytesToHexString(this.mIccIoResult.payload));
        }
        return null;
    }
}

package com.huawei.internal.telephony;

import android.os.Message;
import com.android.internal.telephony.uicc.IccFileHandler;

public class IccFileHandlerEx {
    private IccFileHandler mIccFileHandler;

    public void setIccFileHandlerEx(IccFileHandler iccFileHandler) {
        this.mIccFileHandler = iccFileHandler;
    }

    public void getSmscAddress(Message result) {
        IccFileHandler iccFileHandler = this.mIccFileHandler;
        if (iccFileHandler != null) {
            iccFileHandler.getSmscAddress(result);
        }
    }

    public void setSmscAddress(String address, Message result) {
        IccFileHandler iccFileHandler = this.mIccFileHandler;
        if (iccFileHandler != null) {
            iccFileHandler.setSmscAddress(address, result);
        }
    }

    public void isUimSupportMeidValue(Message result) {
        IccFileHandler iccFileHandler = this.mIccFileHandler;
        if (iccFileHandler != null) {
            iccFileHandler.isUimSupportMeidValue(result);
        }
    }

    public void getMeidOrPesnValue(Message result) {
        IccFileHandler iccFileHandler = this.mIccFileHandler;
        if (iccFileHandler != null) {
            iccFileHandler.getMeidOrPesnValue(result);
        }
    }

    public void setMeidOrPesnValue(String meid, String pesn, Message result) {
        IccFileHandler iccFileHandler = this.mIccFileHandler;
        if (iccFileHandler != null) {
            iccFileHandler.setMeidOrPesnValue(meid, pesn, result);
        }
    }
}

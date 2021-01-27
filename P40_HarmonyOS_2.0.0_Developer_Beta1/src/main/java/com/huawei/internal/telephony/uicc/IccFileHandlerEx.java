package com.huawei.internal.telephony.uicc;

import android.os.Message;
import com.android.internal.telephony.PhoneConfigurationManager;
import com.android.internal.telephony.uicc.IccFileHandler;
import com.android.internal.telephony.uicc.SIMFileHandler;
import com.android.internal.telephony.uicc.UsimFileHandler;

public class IccFileHandlerEx {
    private IccFileHandler mIccFileHandle;

    public void setIccFileHandle(IccFileHandler iccFileHandle) {
        this.mIccFileHandle = iccFileHandle;
    }

    public void loadEFTransparent(int fileid, Message onLoaded) {
        IccFileHandler iccFileHandler = this.mIccFileHandle;
        if (iccFileHandler != null) {
            iccFileHandler.loadEFTransparent(fileid, onLoaded);
        }
    }

    public void loadEFTransparent(String filePath, int fileid, Message onLoaded) {
        IccFileHandler iccFileHandler = this.mIccFileHandle;
        if (iccFileHandler != null) {
            iccFileHandler.loadEFTransparent(filePath, fileid, onLoaded);
        }
    }

    public void loadEFTransparent(String filePath, int fileid, Message onLoaded, boolean isForApp) {
        IccFileHandler iccFileHandler = this.mIccFileHandle;
        if (iccFileHandler != null) {
            iccFileHandler.loadEFTransparent(filePath, fileid, onLoaded, isForApp);
        }
    }

    public void loadEFLinearFixedAll(int fileid, Message onLoaded) {
        IccFileHandler iccFileHandler = this.mIccFileHandle;
        if (iccFileHandler != null) {
            iccFileHandler.loadEFLinearFixedAll(fileid, onLoaded);
        }
    }

    public void updateEFLinearFixed(int fileid, int recordNum, byte[] data, String pin2, Message onComplete) {
        IccFileHandler iccFileHandler = this.mIccFileHandle;
        if (iccFileHandler != null) {
            iccFileHandler.updateEFLinearFixed(fileid, recordNum, data, pin2, onComplete);
        }
    }

    public String getEFPath(int efid) {
        IccFileHandler iccFileHandler = this.mIccFileHandle;
        if (iccFileHandler != null) {
            return iccFileHandler.getEFPathHw(efid);
        }
        return PhoneConfigurationManager.SSSS;
    }

    public boolean isInstanceOfUsimFileHandler() {
        return this.mIccFileHandle instanceof UsimFileHandler;
    }

    public boolean isInstanceOfSimFileHandler() {
        return this.mIccFileHandle instanceof SIMFileHandler;
    }
}

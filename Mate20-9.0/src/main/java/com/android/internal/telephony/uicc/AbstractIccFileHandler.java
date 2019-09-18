package com.android.internal.telephony.uicc;

import android.os.Handler;
import android.os.Message;
import com.android.internal.telephony.HwTelephonyFactory;
import java.util.ArrayList;

public class AbstractIccFileHandler extends Handler {
    protected IccFileHandlerReference mReference = HwTelephonyFactory.getHwUiccManager().createHwIccFileHandlerReference(this);

    public interface IccFileHandlerReference {
        void getSmscAddress(Message message);

        void handleMessage(Message message);

        void loadEFLinearFixedPartHW(int i, ArrayList<Integer> arrayList, Message message);

        void loadEFTransparent(String str, int i, Message message);

        void loadEFTransparent(String str, int i, Message message, boolean z);

        void setSmscAddress(String str, Message message);
    }

    public void loadEFTransparent(String filePath, int fileid, Message onLoaded) {
        this.mReference.loadEFTransparent(filePath, fileid, onLoaded);
    }

    public void loadEFTransparent(String filePath, int fileid, Message onLoaded, boolean isForApp) {
        this.mReference.loadEFTransparent(filePath, fileid, onLoaded, isForApp);
    }

    public void getSmscAddress(Message result) {
        this.mReference.getSmscAddress(result);
    }

    public void setSmscAddress(String address, Message result) {
        this.mReference.setSmscAddress(address, result);
    }

    public void handleMessage(Message msg) {
        this.mReference.handleMessage(msg);
    }

    public void loadEFLinearFixedPartHW(int fileid, ArrayList<Integer> recordNums, Message onLoaded) {
        this.mReference.loadEFLinearFixedPartHW(fileid, recordNums, onLoaded);
    }

    public IccRecords getIccRecords() {
        if (this instanceof IccFileHandler) {
            return IccFileHandlerUtils.getParentApp((IccFileHandler) this).getIccRecords();
        }
        return null;
    }
}

package com.android.internal.telephony.uicc;

import android.os.Message;

public interface IAdnRecordLoaderInner {
    void handleMessageForEx(Message message);

    void loadAllAdnFromEFHw(int i, int i2, Message message);

    void setLoaderPara(int i, int i2, Message message);

    static IAdnRecordLoaderInner makeAdnRecordLoaderInstance(IIccFileHandlerInner iIccFileHandlerInner) {
        if (iIccFileHandlerInner instanceof IccFileHandler) {
            return new AdnRecordLoader((IccFileHandler) iIccFileHandlerInner);
        }
        return null;
    }
}

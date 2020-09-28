package com.android.internal.telephony.uicc;

import android.os.Message;

public interface IHwAdnRecordLoaderInner {
    void handleMessageForEx(Message message);

    void setLoaderPara(int i, int i2, Message message);
}

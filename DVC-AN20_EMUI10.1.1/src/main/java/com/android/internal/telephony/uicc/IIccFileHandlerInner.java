package com.android.internal.telephony.uicc;

import android.os.Message;

public interface IIccFileHandlerInner {
    String getEFPathHw(int i);

    void getSmscAddress(Message message);

    void loadEFLinearFixedAllExcludeEmpty(int i, Message message);

    void loadEFTransparent(String str, int i, Message message);

    void loadEFTransparent(String str, int i, Message message, boolean z);

    void setSmscAddress(String str, Message message);
}

package com.android.internal.telephony.imsphone;

import android.os.Message;

public interface IHwImsPhoneEx {
    boolean beforeHandleMessage(Message message);

    boolean isBusy();

    boolean isSupportCFT();

    boolean isUtEnable();

    Message popUtMessage(int i);

    void setCallForwardingUncondTimerOption(int i, int i2, int i3, int i4, int i5, int i6, String str, Message message);

    void setIsBusy(boolean z);
}

package com.android.ims;

import android.os.Message;

public interface AbstractImsUtInterface {
    boolean isSupportCFT();

    boolean isUtEnable();

    Message popUtMessage(int i);

    void updateCallBarringOption(String str, int i, boolean z, Message message, String[] strArr);

    void updateCallForwardUncondTimer(int i, int i2, int i3, int i4, int i5, int i6, String str, Message message);
}

package com.android.ims;

import android.os.Message;

public interface IHwImsUtEx {
    String getUtIMPUFromNetwork(ImsUt imsUt);

    boolean isSupportCFT();

    boolean isUtEnable();

    void processECT(ImsUt imsUt);

    void queryCallForwardForServiceClass(int i, String str, int i2, Message message, ImsUt imsUt);

    void updateCallBarringOption(String str, int i, boolean z, int i2, Message message, String[] strArr, ImsUt imsUt);

    void updateCallForwardUncondTimer(int i, int i2, int i3, int i4, int i5, int i6, String str, Message message, ImsUt imsUt);
}

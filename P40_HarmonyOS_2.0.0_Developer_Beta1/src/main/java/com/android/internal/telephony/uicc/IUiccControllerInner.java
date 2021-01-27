package com.android.internal.telephony.uicc;

import android.os.Handler;
import android.os.Message;
import com.huawei.internal.telephony.CommandsInterfaceEx;
import com.huawei.internal.telephony.uicc.UiccCardExt;

public interface IUiccControllerInner {
    void disposeCard(int i);

    CommandsInterfaceEx[] getCis();

    void getUiccCardStatus(Message message, int i);

    UiccCardExt[] getUiccCards();

    void notifyFdnStatusChange();

    void onRefresh(int i, int[] iArr);

    void registerForFdnStatusChange(Handler handler, int i, Object obj);

    void unregisterForFdnStatusChange(Handler handler);
}

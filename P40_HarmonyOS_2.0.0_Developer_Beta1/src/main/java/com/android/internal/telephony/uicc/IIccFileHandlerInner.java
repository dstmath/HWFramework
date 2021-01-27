package com.android.internal.telephony.uicc;

import android.os.Message;
import com.huawei.internal.telephony.uicc.UiccProfileEx;

public interface IIccFileHandlerInner {
    void getEFLinearRecordSize(int i, Message message);

    String getEFPathHw(int i);

    void getSmscAddress(Message message);

    UiccProfileEx getUiccProfileEx();

    boolean has3Gphonebook();

    boolean isInstanceOfCsimFileHandler();

    boolean isInstanceOfIsimFileHandler();

    boolean isInstanceOfUsimFileHandler();

    void loadEFLinearFixedAll(int i, Message message);

    void loadEFLinearFixedAllExcludeEmpty(int i, Message message);

    void loadEFTransparent(String str, int i, Message message);

    void loadEFTransparent(String str, int i, Message message, boolean z);

    void setSmscAddress(String str, Message message);

    void updateEFLinearFixed(int i, int i2, byte[] bArr, String str, Message message);
}

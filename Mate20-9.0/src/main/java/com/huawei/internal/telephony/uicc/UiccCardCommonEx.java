package com.huawei.internal.telephony.uicc;

import android.os.Message;
import com.android.internal.telephony.uicc.UiccCard;

public class UiccCardCommonEx {
    public static void getAtr(UiccCard obj, Message response) {
        if (obj != null) {
            obj.iccGetATR(response);
        }
    }
}

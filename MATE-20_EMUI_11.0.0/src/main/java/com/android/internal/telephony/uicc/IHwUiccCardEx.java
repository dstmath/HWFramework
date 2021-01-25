package com.android.internal.telephony.uicc;

import android.content.Context;
import android.os.Message;

public interface IHwUiccCardEx {
    default void iccGetATR(Message onComplete) {
    }

    default void displayUimTipDialog(Context context, int resId) {
    }
}

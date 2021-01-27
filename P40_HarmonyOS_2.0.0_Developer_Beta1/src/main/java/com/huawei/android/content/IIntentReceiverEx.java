package com.huawei.android.content;

import android.content.IIntentReceiver;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class IIntentReceiverEx {
    private IIntentReceiver mReceiver;

    private IIntentReceiverEx() {
    }

    public IIntentReceiverEx(IIntentReceiver receiver) {
        this.mReceiver = receiver;
    }

    public IIntentReceiver getIntentReceiver() {
        return this.mReceiver;
    }

    public void resetReceiver() {
        this.mReceiver = null;
    }

    public boolean isReceiverNull() {
        return this.mReceiver == null;
    }
}

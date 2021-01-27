package com.huawei.android.view;

import android.os.Looper;
import android.view.InputChannel;
import android.view.InputEvent;
import android.view.InputEventReceiver;

public abstract class InputEventReceiverBridge extends InputEventReceiver {
    private InputEventReceiverEx mInputEventReceiverEx;

    public InputEventReceiverBridge(InputChannel inputChannel, Looper looper) {
        super(inputChannel, looper);
    }

    public void setInputEventReceiverEx(InputEventReceiverEx inputEventReceiverEx) {
        this.mInputEventReceiverEx = inputEventReceiverEx;
    }

    public void onInputEvent(InputEvent event) {
        InputEventReceiverEx inputEventReceiverEx = this.mInputEventReceiverEx;
        if (inputEventReceiverEx != null) {
            inputEventReceiverEx.onInputEvent(event);
        }
    }
}

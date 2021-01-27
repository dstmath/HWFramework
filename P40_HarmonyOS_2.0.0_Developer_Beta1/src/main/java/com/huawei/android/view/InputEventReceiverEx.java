package com.huawei.android.view;

import android.os.Looper;
import android.view.InputEvent;

public class InputEventReceiverEx {
    private InputEventReceiverBridge mInputEventReceiverBridge;

    public InputEventReceiverEx(InputChannelEx inputChannel, Looper looper) {
        this.mInputEventReceiverBridge = new InputEventReceiverBridge(inputChannel.getInputChannel(), looper) {
            /* class com.huawei.android.view.InputEventReceiverEx.AnonymousClass1 */
        };
        this.mInputEventReceiverBridge.setInputEventReceiverEx(this);
    }

    public void onInputEvent(InputEvent event) {
    }

    public void dispose() {
        InputEventReceiverBridge inputEventReceiverBridge = this.mInputEventReceiverBridge;
        if (inputEventReceiverBridge != null) {
            inputEventReceiverBridge.dispose();
        }
    }

    public void finishInputEvent(InputEvent event, boolean handled) {
        InputEventReceiverBridge inputEventReceiverBridge = this.mInputEventReceiverBridge;
        if (inputEventReceiverBridge != null) {
            inputEventReceiverBridge.finishInputEvent(event, handled);
        }
    }
}

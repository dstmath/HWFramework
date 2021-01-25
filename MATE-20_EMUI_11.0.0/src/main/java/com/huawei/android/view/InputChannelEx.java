package com.huawei.android.view;

import android.view.InputChannel;

public class InputChannelEx {
    private InputChannel mInputChannel = new InputChannel();

    public void setInputChannel(InputChannel inputChannel) {
        this.mInputChannel = inputChannel;
    }

    public InputChannel getInputChannel() {
        return this.mInputChannel;
    }
}

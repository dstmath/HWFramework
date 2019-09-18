package com.huawei.android.util;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import com.android.internal.util.AsyncChannel;

public class AsyncChannelEx {
    AsyncChannel mAsyncChannel = new AsyncChannel();

    public void connect(Context srcContext, Handler srcHandler, Messenger dstMessenger) {
        if (this.mAsyncChannel != null) {
            this.mAsyncChannel.connect(srcContext, srcHandler, dstMessenger);
        }
    }

    public void sendMessage(Message msg) {
        if (this.mAsyncChannel != null) {
            this.mAsyncChannel.sendMessage(msg);
        }
    }

    public static final int getCmdChannelHalfConnected() {
        return 69632;
    }

    public static final int getStatusSuccessful() {
        return 0;
    }
}

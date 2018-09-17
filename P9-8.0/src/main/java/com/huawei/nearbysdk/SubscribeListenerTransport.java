package com.huawei.nearbysdk;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import com.huawei.nearbysdk.ISubscribeListener.Stub;

public class SubscribeListenerTransport extends Stub {
    static final String TAG = "SubscribeListenerTransport";
    private static final int TYPE_STATUS_CHANGED = 1;
    private SubscribeListener mListener;
    private final Handler mListenerHandler;

    SubscribeListenerTransport(SubscribeListener listener, Looper looper) {
        this.mListener = listener;
        this.mListenerHandler = new Handler(looper) {
            public void handleMessage(Message msg) {
                SubscribeListenerTransport.this._handleMessage(msg);
            }
        };
    }

    public void onStatusChanged(int status) {
        HwLog.d(TAG, "onStatusChanged status = " + status);
        Message msg = Message.obtain();
        msg.what = 1;
        msg.arg1 = status;
        if (!this.mListenerHandler.sendMessage(msg)) {
            HwLog.e(TAG, "onStatusChanged: handler quitting,remove the listener. ");
        }
    }

    private void _handleMessage(Message msg) {
        HwLog.d(TAG, "_handleMessage: " + msg.toString());
        switch (msg.what) {
            case 1:
                HwLog.d(TAG, "TYPE_STATUS_CHANGED");
                this.mListener.onStatusChanged(msg.arg1);
                return;
            default:
                HwLog.e(TAG, "Unknow message id:" + msg.what + ", can not be here!");
                return;
        }
    }
}

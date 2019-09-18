package com.huawei.nearbysdk.closeRange;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import com.huawei.nearbysdk.HwLog;
import com.huawei.nearbysdk.closeRange.ICloseRangeEventListener;

public class CloseRangeEventListenerTransport extends ICloseRangeEventListener.Stub {
    private static final int MSG_BASE = 100;
    private static final int MSG_ONEVENT = 101;
    private static final String TAG = "CloseRangeEventListenerTransport";
    /* access modifiers changed from: private */
    public final CloseRangeEventListener listener;
    private Handler listenerHandler;

    private class EventListenerHandler extends Handler implements CloseRangeEventListener {
        EventListenerHandler(Looper looper) {
            super(looper);
        }

        public void onEvent(CloseRangeResult result) {
            CloseRangeEventListenerTransport.this.listener.onEvent(result);
        }

        public void handleMessage(Message msg) {
            if (msg.what != 101) {
                HwLog.e(CloseRangeEventListenerTransport.TAG, "unknown message " + msg.what);
                return;
            }
            onEvent((CloseRangeResult) msg.obj);
        }
    }

    CloseRangeEventListenerTransport(CloseRangeEventListener eventListener, Looper looper) {
        this.listenerHandler = new EventListenerHandler(looper);
        this.listener = eventListener;
    }

    public void onEvent(CloseRangeResult result) throws RemoteException {
        sendMessage(101, result);
    }

    private void sendMessage(int msgWhat, CloseRangeResult result) {
        this.listenerHandler.sendMessage(this.listenerHandler.obtainMessage(msgWhat, result));
    }
}

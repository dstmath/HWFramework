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
    private final CloseRangeEventListener listener;
    private Handler listenerHandler;

    CloseRangeEventListenerTransport(CloseRangeEventListener eventListener, Looper looper) {
        this.listenerHandler = new EventListenerHandler(looper);
        this.listener = eventListener;
    }

    @Override // com.huawei.nearbysdk.closeRange.ICloseRangeEventListener
    public void onEvent(CloseRangeResult result) throws RemoteException {
        sendMessage(101, result);
    }

    private void sendMessage(int msgWhat, CloseRangeResult result) {
        this.listenerHandler.sendMessage(this.listenerHandler.obtainMessage(msgWhat, result));
    }

    private class EventListenerHandler extends Handler implements CloseRangeEventListener {
        EventListenerHandler(Looper looper) {
            super(looper);
        }

        @Override // com.huawei.nearbysdk.closeRange.CloseRangeEventListener
        public void onEvent(CloseRangeResult result) {
            CloseRangeEventListenerTransport.this.listener.onEvent(result);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 101:
                    onEvent((CloseRangeResult) msg.obj);
                    return;
                default:
                    HwLog.e(CloseRangeEventListenerTransport.TAG, "unknown message " + msg.what);
                    return;
            }
        }
    }
}

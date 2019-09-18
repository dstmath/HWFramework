package com.huawei.nearbysdk;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import com.huawei.nearbysdk.IDevConnectListen;

public class DevConnectListenTransport extends IDevConnectListen.Stub {
    static final String TAG = "ListenerTransport";
    private static final int TYPE_CONNECT_FAIL = 2;
    private static final int TYPE_CONNECT_SUCC = 1;
    private static final int TYPE_DEV_DISCONNECT = 3;
    private DevConnectListener mListener;
    private final Handler mListenerHandler;

    DevConnectListenTransport(DevConnectListener listener, Looper looper) {
        this.mListener = listener;
        this.mListenerHandler = new Handler(looper) {
            public void handleMessage(Message msg) {
                DevConnectListenTransport.this._handleMessage(msg);
            }
        };
    }

    public void onConnectSuccess(String ip) {
        HwLog.d(TAG, "onConnectSuccess");
        Message msg = Message.obtain();
        msg.what = 1;
        msg.obj = ip;
        if (!this.mListenerHandler.sendMessage(msg)) {
            HwLog.e(TAG, "onDeviceLost: handler quitting,remove the listener. ");
        }
    }

    public void onConnectFail(int ret) {
        HwLog.d(TAG, "onConnectFail");
        Message msg = Message.obtain();
        msg.what = 2;
        msg.obj = Integer.valueOf(ret);
        if (!this.mListenerHandler.sendMessage(msg)) {
            HwLog.e(TAG, "onConnectFail: handler quitting,remove the listener. ");
        }
    }

    public void onDevDisconnected(NearbyDevice dev) {
        HwLog.d(TAG, "onDevDisconnected");
        Message msg = Message.obtain();
        msg.what = 3;
        msg.obj = dev;
        if (!this.mListenerHandler.sendMessage(msg)) {
            HwLog.e(TAG, "onDevDisconnected: handler quitting,remove the listener. ");
        }
    }

    /* access modifiers changed from: private */
    public void _handleMessage(Message msg) {
        if (msg == null) {
            HwLog.e(TAG, "_handleMessage,msg is null ");
        }
        HwLog.d(TAG, "_handleMessage: " + msg.what);
        switch (msg.what) {
            case 1:
                HwLog.d(TAG, "TYPE_CONNECT_SUCC Listener.onConnectSuccess");
                this.mListener.onConnectSuccess((String) msg.obj);
                return;
            case 2:
                HwLog.d(TAG, "TYPE_CONNECT_FAIL Listener.onConnectFail");
                this.mListener.onConnectFail(((Integer) msg.obj).intValue());
                return;
            case 3:
                HwLog.d(TAG, "TYPE_DEV_DISCONNECT Listener.onDevDisconnected");
                this.mListener.onDevDisconnected((NearbyDevice) msg.obj);
                return;
            default:
                HwLog.e(TAG, "Unknow message id:" + msg.what + ", can not be here!");
                return;
        }
    }
}

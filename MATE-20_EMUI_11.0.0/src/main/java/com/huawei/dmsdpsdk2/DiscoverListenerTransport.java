package com.huawei.dmsdpsdk2;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import com.huawei.dmsdpsdk2.IDiscoverListener;
import java.util.Map;

public class DiscoverListenerTransport extends IDiscoverListener.Stub {
    private static final int EVENT_DEVICE_FOUND = 1;
    private static final int EVENT_DEVICE_LOST = 2;
    private static final int EVENT_DEVICE_UPDATE = 3;
    private static final int EVENT_STATE_CHANGE = 4;
    private static final String TAG = "DiscoverListenerTransport";
    private final Handler mHandler;
    private DiscoverListener mListener;

    public DiscoverListenerTransport(DiscoverListener listener, Looper looper) {
        this.mListener = listener;
        this.mHandler = new Handler(looper) {
            /* class com.huawei.dmsdpsdk2.DiscoverListenerTransport.AnonymousClass1 */

            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                DiscoverListenerTransport.this.handleMessage(msg);
            }
        };
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleMessage(Message msg) {
        HwLog.d(TAG, "handleMessage: " + msg.what);
        DMSDPDevice tmpInstance = getDeviceInstance(msg);
        int i = msg.what;
        if (i != 1) {
            if (i != 2) {
                if (i != 3) {
                    if (i != 4) {
                        HwLog.e(TAG, "Unknown message id:" + msg.what + ", can not be here!");
                        return;
                    }
                    dealStateChangeEvent(msg);
                } else if (tmpInstance != null) {
                    this.mListener.onDeviceUpdate(tmpInstance, msg.arg1);
                }
            } else if (tmpInstance != null) {
                this.mListener.onDeviceLost(tmpInstance);
            }
        } else if (tmpInstance != null) {
            this.mListener.onDeviceFound(tmpInstance);
        }
    }

    private DMSDPDevice getDeviceInstance(Message msg) {
        Object obj = msg.obj;
        if (obj instanceof DMSDPDevice) {
            return (DMSDPDevice) obj;
        }
        HwLog.e(TAG, "is not DMSDPDevice instance");
        return null;
    }

    private void dealStateChangeEvent(Message msg) {
        Object obj = msg.obj;
        if (obj instanceof Map) {
            this.mListener.onStateChanged(msg.arg1, (Map) obj);
        } else {
            HwLog.e(TAG, "is not map instance");
        }
    }

    @Override // com.huawei.dmsdpsdk2.IDiscoverListener
    public void onDeviceFound(DMSDPDevice device) throws RemoteException {
        HwLog.d(TAG, "onDeviceFound");
        sendMessage(1, device, 0);
    }

    @Override // com.huawei.dmsdpsdk2.IDiscoverListener
    public void onDeviceLost(DMSDPDevice device) throws RemoteException {
        HwLog.d(TAG, "onDeviceLost");
        sendMessage(2, device, 0);
    }

    @Override // com.huawei.dmsdpsdk2.IDiscoverListener
    public void onDeviceUpdate(DMSDPDevice device, int action) throws RemoteException {
        HwLog.d(TAG, "onDeviceUpdate:" + action);
        sendMessage(3, device, 0);
    }

    @Override // com.huawei.dmsdpsdk2.IDiscoverListener
    public void onStateChanged(int state, Map info) throws RemoteException {
        HwLog.d(TAG, "onStateChanged:" + state);
        sendMessage(4, state, -1, info, 0);
    }

    private void sendMessage(int msgWhat, Object obj, long delayMillis) {
        sendMessage(msgWhat, -1, -1, obj, delayMillis);
    }

    private void sendMessage(int msgWhat, int arg1, int arg2, Object obj, long delayMillis) {
        if (!this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(msgWhat, arg1, arg2, obj), delayMillis)) {
            HwLog.e(TAG, "sendMessage error here");
        }
    }
}

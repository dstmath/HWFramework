package com.huawei.nearbysdk.closeRange;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import com.huawei.nearbysdk.HwLog;
import com.huawei.nearbysdk.closeRange.ICloseRangeDeviceListener;

public class CloseRangeDeviceListenerTransport extends ICloseRangeDeviceListener.Stub {
    private static final int MSG_BASE = 100;
    private static final int MSG_ONDEVICE = 101;
    private static final String TAG = "CloseRangeDeviceListenerTransport";
    /* access modifiers changed from: private */
    public final CloseRangeDeviceListener listener;
    private Handler listenerHandler;

    private class DeviceListenerHandler extends Handler implements CloseRangeDeviceListener {
        DeviceListenerHandler(Looper looper) {
            super(looper);
        }

        public void onDevice(CloseRangeResult result) {
            CloseRangeDeviceListenerTransport.this.listener.onDevice(result);
        }

        public void handleMessage(Message msg) {
            if (msg.what != 101) {
                HwLog.e(CloseRangeDeviceListenerTransport.TAG, "unknown message " + msg.what);
                return;
            }
            onDevice((CloseRangeResult) msg.obj);
        }
    }

    CloseRangeDeviceListenerTransport(CloseRangeDeviceListener deviceListener, Looper looper) {
        this.listenerHandler = new DeviceListenerHandler(looper);
        this.listener = deviceListener;
    }

    public void onDevice(CloseRangeResult result) throws RemoteException {
        sendMessage(101, result);
    }

    private void sendMessage(int msgWhat, CloseRangeResult result) {
        this.listenerHandler.sendMessage(this.listenerHandler.obtainMessage(msgWhat, result));
    }
}

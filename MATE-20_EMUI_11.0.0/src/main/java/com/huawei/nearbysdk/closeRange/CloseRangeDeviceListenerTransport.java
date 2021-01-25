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
    private final CloseRangeDeviceListener listener;
    private Handler listenerHandler;

    CloseRangeDeviceListenerTransport(CloseRangeDeviceListener deviceListener, Looper looper) {
        this.listenerHandler = new DeviceListenerHandler(looper);
        this.listener = deviceListener;
    }

    @Override // com.huawei.nearbysdk.closeRange.ICloseRangeDeviceListener
    public void onDevice(CloseRangeResult result) throws RemoteException {
        sendMessage(101, result);
    }

    private void sendMessage(int msgWhat, CloseRangeResult result) {
        this.listenerHandler.sendMessage(this.listenerHandler.obtainMessage(msgWhat, result));
    }

    private class DeviceListenerHandler extends Handler implements CloseRangeDeviceListener {
        DeviceListenerHandler(Looper looper) {
            super(looper);
        }

        @Override // com.huawei.nearbysdk.closeRange.CloseRangeDeviceListener
        public void onDevice(CloseRangeResult result) {
            CloseRangeDeviceListenerTransport.this.listener.onDevice(result);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg.what != 101) {
                HwLog.e(CloseRangeDeviceListenerTransport.TAG, "unknown message " + msg.what);
                return;
            }
            onDevice((CloseRangeResult) msg.obj);
        }
    }
}

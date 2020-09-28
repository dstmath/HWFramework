package com.huawei.dmsdpsdk2.sensor;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import com.huawei.dmsdpsdk2.HwLog;
import com.huawei.dmsdpsdk2.sensor.ISensorDataListener;

public class SensorDataListenerTransport extends ISensorDataListener.Stub {
    private static final int EVENT_REC_DATA = 1;
    private static final String TAG = "SensorDataListenerTransport";
    private final Handler mHandler;
    private SensorDataListener mListener;

    public SensorDataListenerTransport(SensorDataListener listener, Looper looper) {
        this.mListener = listener;
        this.mHandler = new Handler(looper) {
            /* class com.huawei.dmsdpsdk2.sensor.SensorDataListenerTransport.AnonymousClass1 */

            public void handleMessage(Message msg) {
                SensorDataListenerTransport.this.handleMessage(msg);
            }
        };
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleMessage(Message msg) {
        HwLog.d(TAG, "handleMessage: " + msg.what);
        if (msg.what != 1) {
            HwLog.e(TAG, "Unknown message id:" + msg.what + ", can not be here!");
        } else if (msg.obj instanceof SensorData) {
            this.mListener.onSensorChanged((SensorData) msg.obj);
        }
    }

    @Override // com.huawei.dmsdpsdk2.sensor.ISensorDataListener
    public void onSensorChanged(SensorData data) throws RemoteException {
        HwLog.d(TAG, "onSensorChanged");
        sendMessage(1, data);
    }

    private void sendMessage(int msgWhat, Object obj) {
        sendMessageDelay(msgWhat, -1, -1, obj, 0);
    }

    private void sendMessageDelay(int msgWhat, int arg1, int arg2, Object obj, long delayMillis) {
        if (!this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(msgWhat, arg1, arg2, obj), delayMillis)) {
            HwLog.e(TAG, "sendMessage error here");
        }
    }
}

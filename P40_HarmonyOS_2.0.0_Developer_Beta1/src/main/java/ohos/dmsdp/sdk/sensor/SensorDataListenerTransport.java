package ohos.dmsdp.sdk.sensor;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import ohos.dmsdp.sdk.HwLog;
import ohos.dmsdp.sdk.sensor.ISensorDataListener;

public class SensorDataListenerTransport extends ISensorDataListener.Stub {
    private static final int EVENT_REC_DATA = 1;
    private static final String TAG = "SensorDataListenerTransport";
    private final Handler mHandler;
    private SensorDataListener mListener;

    public SensorDataListenerTransport(SensorDataListener sensorDataListener, Looper looper) {
        this.mListener = sensorDataListener;
        this.mHandler = new Handler(looper) {
            /* class ohos.dmsdp.sdk.sensor.SensorDataListenerTransport.AnonymousClass1 */

            @Override // android.os.Handler
            public void handleMessage(Message message) {
                SensorDataListenerTransport.this.handleMessage(message);
            }
        };
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleMessage(Message message) {
        HwLog.d(TAG, "handleMessage: " + message.what);
        if (message.what != 1) {
            HwLog.e(TAG, "Unknown message id:" + message.what + ", can not be here!");
        } else if (message.obj instanceof SensorData) {
            this.mListener.onSensorChanged((SensorData) message.obj);
        }
    }

    @Override // ohos.dmsdp.sdk.sensor.ISensorDataListener
    public void onSensorChanged(SensorData sensorData) throws RemoteException {
        HwLog.d(TAG, "onSensorChanged");
        sendMessage(1, sensorData);
    }

    private void sendMessage(int i, Object obj) {
        sendMessageDelay(i, -1, -1, obj, 0);
    }

    private void sendMessageDelay(int i, int i2, int i3, Object obj, long j) {
        if (!this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(i, i2, i3, obj), j)) {
            HwLog.e(TAG, "sendMessage error here");
        }
    }
}

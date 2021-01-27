package ohos.dmsdp.sdk;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import ohos.dmsdp.sdk.IDataListener;

public class DataListenerTransport extends IDataListener.Stub {
    private static final String DATA = "data";
    private static final String DATA_TYPE = "dataType";
    private static final int EVENT_DATA_RECEIVED = 1;
    private static final String REMOTE_DEVICE = "remoteDevice";
    private static final String TAG = "DataListenerTransport";
    private final Handler mHandler;
    private DataListener mListener;

    public DataListenerTransport(DataListener dataListener, Looper looper) {
        this.mListener = dataListener;
        this.mHandler = new Handler(looper) {
            /* class ohos.dmsdp.sdk.DataListenerTransport.AnonymousClass1 */

            @Override // android.os.Handler
            public void handleMessage(Message message) {
                DataListenerTransport.this.handleMessage(message);
            }
        };
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleMessage(Message message) {
        HwLog.d(TAG, "handleMessage: " + message.what);
        if (message.what != 1) {
            HwLog.e(TAG, "Unknown message id:" + message.what + ", can not be here!");
        } else if (message.obj instanceof Bundle) {
            Bundle bundle = (Bundle) message.obj;
            int i = bundle.getInt(DATA_TYPE);
            byte[] byteArray = bundle.getByteArray(DATA);
            this.mListener.onDataReceive((DMSDPDevice) bundle.getParcelable(REMOTE_DEVICE), i, byteArray);
        }
    }

    @Override // ohos.dmsdp.sdk.IDataListener
    public void onDataReceive(DMSDPDevice dMSDPDevice, int i, byte[] bArr) throws RemoteException {
        HwLog.d(TAG, "onDataReceive");
        Bundle bundle = new Bundle();
        bundle.putParcelable(REMOTE_DEVICE, dMSDPDevice);
        bundle.putInt(DATA_TYPE, i);
        bundle.putByteArray(DATA, bArr);
        sendMessage(1, bundle);
    }

    private void sendMessage(int i, Object obj) {
        if (!this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(i, -1, -1, obj), 0)) {
            HwLog.e(TAG, "sendMessage error here");
        }
    }
}

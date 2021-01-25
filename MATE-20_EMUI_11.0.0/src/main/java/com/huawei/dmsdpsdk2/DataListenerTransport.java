package com.huawei.dmsdpsdk2;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import com.huawei.dmsdpsdk2.IDataListener;

public class DataListenerTransport extends IDataListener.Stub {
    private static final String DATA = "data";
    private static final String DATA_TYPE = "dataType";
    private static final int EVENT_DATA_RECEIVED = 1;
    private static final String REMOTE_DEVICE = "remoteDevice";
    private static final String TAG = "DataListenerTransport";
    private final Handler mHandler;
    private DataListener mListener;

    public DataListenerTransport(DataListener listener, Looper looper) {
        this.mListener = listener;
        this.mHandler = new Handler(looper) {
            /* class com.huawei.dmsdpsdk2.DataListenerTransport.AnonymousClass1 */

            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                DataListenerTransport.this.handleMessage(msg);
            }
        };
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleMessage(Message msg) {
        HwLog.d(TAG, "handleMessage: " + msg.what);
        if (msg.what != 1) {
            HwLog.e(TAG, "Unknown message id:" + msg.what + ", can not be here!");
            return;
        }
        try {
            if (msg.obj instanceof Bundle) {
                Bundle bundle = (Bundle) msg.obj;
                if (bundle == null) {
                    HwLog.e(TAG, "in handleMessage bundle is null");
                    return;
                }
                int dataType = bundle.getInt(DATA_TYPE);
                byte[] data = bundle.getByteArray(DATA);
                if (data == null) {
                    HwLog.e(TAG, "in handleMessage data is null");
                } else if (dataType == 26) {
                    HwLog.d(TAG, "dataType is " + dataType);
                    this.mListener.onDataReceive(null, dataType, data);
                } else {
                    DMSDPDevice device = (DMSDPDevice) bundle.getParcelable(REMOTE_DEVICE);
                    if (device == null) {
                        HwLog.e(TAG, "in handleMessage device is null");
                    } else {
                        this.mListener.onDataReceive(device, dataType, data);
                    }
                }
            } else {
                HwLog.e(TAG, "is not bundle instance");
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            HwLog.e(TAG, "handleMessage exception with array index out of bounds");
        }
    }

    @Override // com.huawei.dmsdpsdk2.IDataListener
    public void onDataReceive(DMSDPDevice device, int dataType, byte[] data) throws RemoteException {
        HwLog.d(TAG, "onDataReceive");
        Bundle bundle = new Bundle();
        bundle.putParcelable(REMOTE_DEVICE, device);
        bundle.putInt(DATA_TYPE, dataType);
        bundle.putByteArray(DATA, data);
        sendMessage(1, bundle);
    }

    private void sendMessage(int msgWhat, Object obj) {
        if (!this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(msgWhat, -1, -1, obj), 0)) {
            HwLog.e(TAG, "sendMessage error here");
        }
    }
}

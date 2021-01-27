package com.huawei.dmsdpsdk2.publisher;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import com.huawei.dmsdp.publishercenter.IPublisherListener;
import com.huawei.dmsdpsdk2.HwLog;

public class PublisherListenerTransport extends IPublisherListener.Stub {
    private static final String DATA = "data";
    private static final int EVENT_DATA_RECEIVED = 1;
    private static final String TAG = "PubListenerTrans";
    private final Handler mHandler;
    private PublisherListener mListener;

    public PublisherListenerTransport(PublisherListener listener, Looper lopper) {
        this.mListener = listener;
        this.mHandler = new Handler(lopper) {
            /* class com.huawei.dmsdpsdk2.publisher.PublisherListenerTransport.AnonymousClass1 */

            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                PublisherListenerTransport.this.handleMessage(msg);
            }
        };
    }

    @Override // com.huawei.dmsdp.publishercenter.IPublisherListener
    public void onMsgReceive(String msg) throws RemoteException {
        HwLog.d(TAG, "receive msg: " + msg);
        Bundle bundle = new Bundle();
        bundle.putString(DATA, msg);
        if (!this.mHandler.sendMessage(this.mHandler.obtainMessage(1, bundle))) {
            HwLog.e(TAG, "send message failed");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleMessage(Message msg) {
        HwLog.d(TAG, "handle publisher msg: " + msg.what);
        if (msg.what != 1) {
            HwLog.e(TAG, "unknown msg id: " + msg.what);
        } else if (!(msg.obj instanceof Bundle)) {
            HwLog.e(TAG, "msg object invalid");
        } else {
            Bundle bundle = (Bundle) msg.obj;
            if (bundle == null) {
                HwLog.e(TAG, "msg bundle is null");
                return;
            }
            this.mListener.onMsgReceive(bundle.getString(DATA));
        }
    }
}

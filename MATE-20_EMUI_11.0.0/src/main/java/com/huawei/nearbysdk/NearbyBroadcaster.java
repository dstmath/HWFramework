package com.huawei.nearbysdk;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import com.huawei.nearbysdk.IBroadcastScanResultCallBack;

public class NearbyBroadcaster {
    private static final String TAG = "NearbyServiceJar";
    private INearbyBroadcaster mBroadcaster;
    private Looper mLooper;

    NearbyBroadcaster(INearbyAdapter nearbyService, Looper looper) throws RemoteException {
        this.mBroadcaster = nearbyService.getBroadcaster();
        this.mLooper = looper;
    }

    /* access modifiers changed from: private */
    public class BroadScanResultCallBack extends IBroadcastScanResultCallBack.Stub {
        private static final int MSG_REPORT_MSG = 1000;
        private Handler mHandler;
        private ReceiveBroadcastCallback mReceiveBroadcastCallback;

        BroadScanResultCallBack(ReceiveBroadcastCallback receiveBroadcastCallback) {
            this.mReceiveBroadcastCallback = receiveBroadcastCallback;
            this.mHandler = new Handler(NearbyBroadcaster.this.mLooper, NearbyBroadcaster.this) {
                /* class com.huawei.nearbysdk.NearbyBroadcaster.BroadScanResultCallBack.AnonymousClass1 */

                @Override // android.os.Handler
                public void handleMessage(Message msg) {
                    BroadScanResultCallBack.this.processMessage(msg);
                }
            };
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void processMessage(Message msg) {
            if (msg.what == 1000) {
                this.mReceiveBroadcastCallback.onReceive((byte[]) msg.obj);
            }
        }

        @Override // com.huawei.nearbysdk.IBroadcastScanResultCallBack
        public void onScanResult(byte[] recvMsg) throws RemoteException {
            HwLog.d(NearbyBroadcaster.TAG, "adapter onScanResult");
            Message message = this.mHandler.obtainMessage();
            message.what = 1000;
            message.obj = recvMsg;
            message.sendToTarget();
        }
    }

    public void broadcast(int bussinessId, int type, byte[] message) {
        broadcast(bussinessId, -1, type, message);
    }

    public void broadcast(int bussinessId, int timeout, int type, byte[] message) {
        try {
            HwLog.d(TAG, "NearbyService startAdv");
            this.mBroadcaster.broadcast(message, bussinessId, timeout, type);
        } catch (RemoteException e) {
            HwLog.e(TAG, "broadcast exception " + e.getMessage());
        }
    }

    public void broadcast(int bussinessId, int type, byte[] message, IBroadcastAdvResultCallBack callback) {
        broadcast(bussinessId, -1, type, message, callback);
    }

    public void broadcast(int bussinessId, int timeout, int type, byte[] message, IBroadcastAdvResultCallBack callback) {
        try {
            HwLog.d(TAG, "NearbyService startAdv new");
            this.mBroadcaster.broadcastWithCallback(callback, message, bussinessId, timeout, type);
        } catch (RemoteException e) {
            HwLog.e(TAG, "broadcast exception " + e.getMessage());
        }
    }

    public void receiveBroadcast(int bussinessId, int type, ReceiveBroadcastCallback receiveBroadcastCallback) {
        receiveBroadcast(bussinessId, -1, type, receiveBroadcastCallback);
    }

    public void receiveBroadcast(int businessId, int timeout, int type, ReceiveBroadcastCallback receiveBroadcastCallback) {
        BroadScanResultCallBack scanResultCallBack = new BroadScanResultCallBack(receiveBroadcastCallback);
        try {
            HwLog.d(TAG, "NearbyService receiveBroadcast");
            this.mBroadcaster.receiveBroadcast(scanResultCallBack, businessId, timeout, type);
        } catch (RemoteException e) {
            HwLog.e(TAG, "receiveBroadcast exception" + e.getMessage());
        }
    }

    public void stopBroadcast(int bussinessId, int type) {
        try {
            HwLog.d(TAG, "NearbyService stopBroadcast");
            this.mBroadcaster.stopBroadcast(bussinessId, type);
        } catch (RemoteException e) {
            HwLog.e(TAG, "remote exception" + e.getMessage());
        }
    }

    public void stopReceiveBroadcast(int bussinessId, int type) {
        try {
            HwLog.d(TAG, "NearbyService stopReceiveBroadcast");
            this.mBroadcaster.stopReceiveBroadcast(bussinessId, type);
        } catch (RemoteException e) {
            HwLog.e(TAG, "remote exception" + e.getMessage());
        }
    }

    public boolean changeBroadcastContent(int businessId, int type, byte[] message) {
        try {
            HwLog.d(TAG, "changeBroadcastContent");
            return this.mBroadcaster.changeBroadcastContent(businessId, type, message);
        } catch (RemoteException e) {
            HwLog.e(TAG, "remote exception" + e.getMessage());
            return false;
        }
    }

    public void setHivoiceBinding(boolean isBinding) {
        try {
            HwLog.d(TAG, "setHivoiceBinding : " + isBinding);
            this.mBroadcaster.setHivoiceBinding(isBinding);
        } catch (RemoteException e) {
            HwLog.e(TAG, "remote exception" + e.getMessage());
        }
    }
}

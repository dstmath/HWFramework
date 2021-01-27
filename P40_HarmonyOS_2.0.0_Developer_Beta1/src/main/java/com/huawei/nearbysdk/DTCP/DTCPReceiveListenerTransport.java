package com.huawei.nearbysdk.DTCP;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import com.huawei.nearbysdk.DTCP.IDTCPReceiveListener;
import com.huawei.nearbysdk.DTCP.fileinfo.BaseShareInfo;
import com.huawei.nearbysdk.DTCP.fileinfo.FileShareInfo;
import com.huawei.nearbysdk.HwLog;

class DTCPReceiveListenerTransport extends IDTCPReceiveListener.Stub implements Handler.Callback {
    private static final int MSG_ERROR = 4;
    private static final int MSG_PREVIEW_RECEIVE = 2;
    private static final int MSG_SEND_CANCEL = 3;
    private static final int MSG_STATUS_CHANGED = 1;
    private static final String TAG = "DTCPReceiveListenerTransport";
    private Handler mHandler = null;
    private DTCPReceiveListener mReceiveListener = null;

    DTCPReceiveListenerTransport(DTCPReceiveListener listener, Looper looper) {
        this.mReceiveListener = listener;
        this.mHandler = new Handler(looper, this);
    }

    @Override // com.huawei.nearbysdk.DTCP.IDTCPReceiveListener
    public void onStatusChanged(int state) throws RemoteException {
        Message.obtain(this.mHandler, 1, state, 0).sendToTarget();
    }

    @Override // com.huawei.nearbysdk.DTCP.IDTCPReceiveListener
    public void onPreviewReceive(BaseShareInfo shareInfo, IDTCPReceiver dtcpRecv) throws RemoteException {
        if (!(shareInfo instanceof FileShareInfo)) {
            HwLog.e(TAG, "Now only support file share");
            dtcpRecv.reject();
            return;
        }
        PreviewDataWarpper pdw = new PreviewDataWarpper();
        pdw.filesInfo = new FilesInfo((FileShareInfo) shareInfo);
        pdw.dtcpRecver = new DTCPReceiver(dtcpRecv);
        Message.obtain(this.mHandler, 2, pdw).sendToTarget();
    }

    @Override // com.huawei.nearbysdk.DTCP.IDTCPReceiveListener
    public void onSendCancelBeforeConfirm(IDTCPReceiver dtcpRecv) throws RemoteException {
        Message.obtain(this.mHandler, 3, new DTCPReceiver(dtcpRecv)).sendToTarget();
    }

    @Override // com.huawei.nearbysdk.DTCP.IDTCPReceiveListener
    public void onErrorBeforeConfirm(IDTCPReceiver dtcpRecv, int errcode) throws RemoteException {
        Message.obtain(this.mHandler, 4, errcode, 0, new DTCPReceiver(dtcpRecv)).sendToTarget();
    }

    @Override // android.os.Handler.Callback
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case 1:
                this.mReceiveListener.onStatusChanged(msg.arg1);
                return true;
            case 2:
                PreviewDataWarpper pdw = (PreviewDataWarpper) msg.obj;
                this.mReceiveListener.onPreviewReceive(pdw.filesInfo, pdw.dtcpRecver);
                return true;
            case 3:
                this.mReceiveListener.onSendCancelBeforeConfirm((DTCPReceiver) msg.obj);
                return true;
            case 4:
                this.mReceiveListener.onErrorBeforeConfirm((DTCPReceiver) msg.obj, msg.arg1);
                return true;
            default:
                HwLog.e(TAG, "Can not be here!Unknow msg=" + msg.what);
                return true;
        }
    }

    private final class PreviewDataWarpper {
        DTCPReceiver dtcpRecver;
        FilesInfo filesInfo;

        private PreviewDataWarpper() {
        }
    }
}

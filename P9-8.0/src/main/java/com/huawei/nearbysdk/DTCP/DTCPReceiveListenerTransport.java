package com.huawei.nearbysdk.DTCP;

import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import com.huawei.nearbysdk.DTCP.IDTCPReceiveListener.Stub;
import com.huawei.nearbysdk.DTCP.fileinfo.BaseShareInfo;
import com.huawei.nearbysdk.DTCP.fileinfo.FileShareInfo;
import com.huawei.nearbysdk.HwLog;

class DTCPReceiveListenerTransport extends Stub implements Callback {
    private static final int MSG_ERROR = 4;
    private static final int MSG_PREVIEW_RECEIVE = 2;
    private static final int MSG_SEND_CANCEL = 3;
    private static final int MSG_STATUS_CHANGED = 1;
    private static final String TAG = "DTCPReceiveListenerTransport";
    private Handler mHandler = null;
    private DTCPReceiveListener mReceiveListener = null;

    private final class PreviewDataWarpper {
        DTCPReceiver dtcpRecver;
        FilesInfo filesInfo;

        /* synthetic */ PreviewDataWarpper(DTCPReceiveListenerTransport this$0, PreviewDataWarpper -this1) {
            this();
        }

        private PreviewDataWarpper() {
        }
    }

    DTCPReceiveListenerTransport(DTCPReceiveListener listener, Looper looper) {
        this.mReceiveListener = listener;
        this.mHandler = new Handler(looper, this);
    }

    public void onStatusChanged(int state) throws RemoteException {
        Message.obtain(this.mHandler, 1, state, 0).sendToTarget();
    }

    public void onPreviewReceive(BaseShareInfo shareInfo, IDTCPReceiver dtcpRecv) throws RemoteException {
        if (shareInfo instanceof FileShareInfo) {
            PreviewDataWarpper pdw = new PreviewDataWarpper(this, null);
            pdw.filesInfo = new FilesInfo((FileShareInfo) shareInfo);
            pdw.dtcpRecver = new DTCPReceiver(dtcpRecv);
            Message.obtain(this.mHandler, 2, pdw).sendToTarget();
            return;
        }
        HwLog.e(TAG, "Now only support file share");
        dtcpRecv.reject();
    }

    public void onSendCancelBeforeConfirm(IDTCPReceiver dtcpRecv) throws RemoteException {
        Message.obtain(this.mHandler, 3, new DTCPReceiver(dtcpRecv)).sendToTarget();
    }

    public void onErrorBeforeConfirm(IDTCPReceiver dtcpRecv, int errcode) throws RemoteException {
        Message.obtain(this.mHandler, 4, errcode, 0, new DTCPReceiver(dtcpRecv)).sendToTarget();
    }

    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case 1:
                this.mReceiveListener.onStatusChanged(msg.arg1);
                break;
            case 2:
                PreviewDataWarpper pdw = msg.obj;
                this.mReceiveListener.onPreviewReceive(pdw.filesInfo, pdw.dtcpRecver);
                break;
            case 3:
                this.mReceiveListener.onSendCancelBeforeConfirm((DTCPReceiver) msg.obj);
                break;
            case 4:
                this.mReceiveListener.onErrorBeforeConfirm((DTCPReceiver) msg.obj, msg.arg1);
                break;
            default:
                HwLog.e(TAG, "Can not be here!Unknow msg=" + msg.what);
                break;
        }
        return true;
    }
}

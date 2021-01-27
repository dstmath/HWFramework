package com.huawei.chr;

import java.util.ArrayList;
import vendor.huawei.hardware.modemlogcat.V1_0.IMsgCallback;

public class MsgCallbackEx {
    private IMsgCallback msgCallback = new IMsgCallback.Stub() {
        /* class com.huawei.chr.MsgCallbackEx.AnonymousClass1 */

        public void onMsgReceived(ArrayList<Byte> msg) {
            MsgCallbackEx.this.onMsgReceived(msg);
        }
    };

    /* access modifiers changed from: package-private */
    public IMsgCallback getMsgCallback() {
        return this.msgCallback;
    }

    public void onMsgReceived(ArrayList<Byte> arrayList) {
    }
}

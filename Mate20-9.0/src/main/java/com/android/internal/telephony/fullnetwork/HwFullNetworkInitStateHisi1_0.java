package com.android.internal.telephony.fullnetwork;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.telephony.Rlog;
import com.android.internal.telephony.CommandsInterface;

public class HwFullNetworkInitStateHisi1_0 extends HwFullNetworkInitStateHisiBase {
    private static final String LOG_TAG = "InitStateHisi1_0";

    public HwFullNetworkInitStateHisi1_0(Context c, CommandsInterface[] ci, Handler h) {
        super(c, ci, h);
        logd("HwFullNetworkInitStateHisi1_0 constructor");
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v15, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v7, resolved type: android.os.AsyncResult} */
    /* JADX WARNING: Multi-variable type inference failed */
    public void handleMessage(Message msg) {
        if (msg == null) {
            loge("msg is null, return!");
            return;
        }
        Integer index = mChipCommon.getCiIndex(msg);
        if (index.intValue() < 0 || index.intValue() >= this.mCis.length) {
            loge("Invalid index : " + index + " received with event " + msg.what);
            return;
        }
        int i = msg.what;
        if (i == 1001) {
            onIccStatusChanged(index);
        } else if (i != 1005) {
            super.handleMessage(msg);
        } else {
            AsyncResult ar = null;
            if (msg.obj != null && (msg.obj instanceof AsyncResult)) {
                ar = msg.obj;
            }
            logd("Received EVENT_QUERY_CARD_TYPE_DONE on index " + index);
            if (ar == null || ar.exception != null) {
                logd("Received EVENT_QUERY_CARD_TYPE_DONE got exception, ar  = " + ar);
            } else {
                onQueryCardTypeDone(ar, index);
            }
        }
    }

    public void onIccStatusChanged(Integer index) {
        super.onIccStatusChanged(index);
    }

    public synchronized void onQueryCardTypeDone(AsyncResult ar, Integer index) {
        super.onQueryCardTypeDone(ar, index);
    }

    /* access modifiers changed from: protected */
    public void logd(String msg) {
        Rlog.d(LOG_TAG, msg);
    }

    /* access modifiers changed from: protected */
    public void loge(String msg) {
        Rlog.e(LOG_TAG, msg);
    }
}

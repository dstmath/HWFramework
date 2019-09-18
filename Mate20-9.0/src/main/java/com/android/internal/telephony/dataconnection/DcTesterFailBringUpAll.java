package com.android.internal.telephony.dataconnection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.telephony.Rlog;
import com.android.internal.telephony.Phone;

public class DcTesterFailBringUpAll {
    private static final boolean DBG = true;
    private static final String LOG_TAG = "DcTesterFailBrinupAll";
    /* access modifiers changed from: private */
    public String mActionFailBringUp = (DcFailBringUp.INTENT_BASE + "." + "action_fail_bringup");
    /* access modifiers changed from: private */
    public DcFailBringUp mFailBringUp = new DcFailBringUp();
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null) {
                String action = intent.getAction();
                DcTesterFailBringUpAll dcTesterFailBringUpAll = DcTesterFailBringUpAll.this;
                dcTesterFailBringUpAll.log("sIntentReceiver.onReceive: action=" + action);
                if (action.equals(DcTesterFailBringUpAll.this.mActionFailBringUp)) {
                    DcTesterFailBringUpAll.this.mFailBringUp.saveParameters(intent, "sFailBringUp");
                } else if (action.equals(DcTesterFailBringUpAll.this.mPhone.getActionDetached())) {
                    DcTesterFailBringUpAll.this.log("simulate detaching");
                    DcTesterFailBringUpAll.this.mFailBringUp.saveParameters(KeepaliveStatus.INVALID_HANDLE, DcFailCause.LOST_CONNECTION.getErrorCode(), -1);
                } else if (action.equals(DcTesterFailBringUpAll.this.mPhone.getActionAttached())) {
                    DcTesterFailBringUpAll.this.log("simulate attaching");
                    DcTesterFailBringUpAll.this.mFailBringUp.saveParameters(0, DcFailCause.NONE.getErrorCode(), -1);
                } else {
                    DcTesterFailBringUpAll dcTesterFailBringUpAll2 = DcTesterFailBringUpAll.this;
                    dcTesterFailBringUpAll2.log("onReceive: unknown action=" + action);
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public Phone mPhone;

    DcTesterFailBringUpAll(Phone phone, Handler handler) {
        this.mPhone = phone;
        if (Build.IS_DEBUGGABLE) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(this.mActionFailBringUp);
            log("register for intent action=" + this.mActionFailBringUp);
            filter.addAction(this.mPhone.getActionDetached());
            log("register for intent action=" + this.mPhone.getActionDetached());
            filter.addAction(this.mPhone.getActionAttached());
            log("register for intent action=" + this.mPhone.getActionAttached());
            phone.getContext().registerReceiver(this.mIntentReceiver, filter, null, handler);
        }
    }

    /* access modifiers changed from: package-private */
    public void dispose() {
        if (Build.IS_DEBUGGABLE) {
            this.mPhone.getContext().unregisterReceiver(this.mIntentReceiver);
        }
    }

    public DcFailBringUp getDcFailBringUp() {
        return this.mFailBringUp;
    }

    /* access modifiers changed from: private */
    public void log(String s) {
        Rlog.d(LOG_TAG, s);
    }
}

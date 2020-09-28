package com.android.internal.telephony.uicc;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.telephony.Rlog;
import android.widget.Button;
import com.huawei.internal.telephony.uicc.UiccProfileEx;

public class HwUiccCardReference implements IHwUiccCardEx {
    protected static final boolean DBG = false;
    private static final int EVENT_GET_ATR_DONE = 105;
    private static final String LOG_TAG = "HwUiccCardReference";
    private static final int ONE_SECOND_TIME_MILLISECONDS = 1000;
    private static final int REBOOT_TOTAL_TIME_MILLISECONDS = 30000;
    private boolean bShowedTipDlg = false;
    Button mCoutDownRootButton = null;
    private Handler mHandler = new MyHandler();
    AlertDialog mSimAddDialog = null;
    private IUiccCardInner mUiccCardInner;
    Resources r = Resources.getSystem();

    public HwUiccCardReference(IUiccCardInner uiccCard) {
        this.mUiccCardInner = uiccCard;
    }

    private static class MyHandler extends Handler {
        private MyHandler() {
        }

        public void handleMessage(Message msg) {
            if (msg.what != HwUiccCardReference.EVENT_GET_ATR_DONE) {
                Rlog.e(HwUiccCardReference.LOG_TAG, "Unknown Event " + msg.what);
                return;
            }
            AsyncResult ar = (AsyncResult) msg.obj;
            if (ar.exception != null) {
                Rlog.e(HwUiccCardReference.LOG_TAG, "Error in SIM access with exception" + ar.exception);
            }
            AsyncResult.forMessage((Message) ar.userObj, ar.result, ar.exception);
            ((Message) ar.userObj).sendToTarget();
        }
    }

    public void iccGetATR(Message onComplete) {
        UiccProfileEx uiccProfileEx = this.mUiccCardInner.getUiccCard().getUiccProfile();
        if (uiccProfileEx != null && uiccProfileEx.getCiHw() != null) {
            uiccProfileEx.getCiHw().iccGetATR(this.mHandler.obtainMessage(EVENT_GET_ATR_DONE, onComplete));
        }
    }

    public void displayUimTipDialog(Context context, int resId) {
        if (!this.bShowedTipDlg) {
            if (context == null) {
                Rlog.e(LOG_TAG, "context ==null");
                return;
            }
            try {
                this.bShowedTipDlg = true;
                AlertDialog dialog = new AlertDialog.Builder(context, 33947691).setTitle(33685797).setMessage(resId).setPositiveButton(17039370, (DialogInterface.OnClickListener) null).setCancelable(false).create();
                dialog.getWindow().setType(2003);
                dialog.show();
            } catch (NullPointerException e) {
                this.bShowedTipDlg = false;
                Rlog.e(LOG_TAG, "displayUimTipDialog NullPointerException");
            }
        }
    }
}

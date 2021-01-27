package com.android.internal.telephony.uicc;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import com.huawei.android.os.AsyncResultEx;
import com.huawei.android.telephony.RlogEx;
import com.huawei.internal.telephony.uicc.UiccProfileEx;
import com.huawei.utils.HwPartResourceUtils;

public class HwUiccCardReference implements IHwUiccCardEx {
    protected static final boolean DBG = false;
    private static final int EVENT_GET_ATR_DONE = 105;
    private static final String LOG_TAG = "HwUiccCardReference";
    private static final int ONE_SECOND_TIME_MILLISECONDS = 1000;
    private static final int REBOOT_TOTAL_TIME_MILLISECONDS = 30000;
    private boolean isShowedTipDlg = false;
    private Handler mHandler = new MyHandler();
    private IUiccCardInner mUiccCardInner;

    public HwUiccCardReference(IUiccCardInner uiccCard) {
        this.mUiccCardInner = uiccCard;
    }

    public void iccGetATR(Message onComplete) {
        UiccProfileEx uiccProfileEx = this.mUiccCardInner.getUiccCard().getUiccProfile();
        if (uiccProfileEx != null && uiccProfileEx.getCiHw() != null) {
            uiccProfileEx.getCiHw().iccGetATR(this.mHandler.obtainMessage(EVENT_GET_ATR_DONE, onComplete));
        }
    }

    public void displayUimTipDialog(Context context, int resId) {
        if (!this.isShowedTipDlg) {
            if (context == null) {
                RlogEx.e(LOG_TAG, "context ==null");
                return;
            }
            try {
                this.isShowedTipDlg = true;
                AlertDialog dialog = new AlertDialog.Builder(context, 33947691).setTitle(33685797).setMessage(resId).setPositiveButton(HwPartResourceUtils.getResourceId("uim_tip_ok"), (DialogInterface.OnClickListener) null).setCancelable(false).create();
                dialog.getWindow().setType(2003);
                dialog.show();
            } catch (NullPointerException e) {
                this.isShowedTipDlg = false;
                RlogEx.e(LOG_TAG, "displayUimTipDialog NullPointerException");
            }
        }
    }

    private static class MyHandler extends Handler {
        private MyHandler() {
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg.what != HwUiccCardReference.EVENT_GET_ATR_DONE) {
                RlogEx.e(HwUiccCardReference.LOG_TAG, "Unknown Event " + msg.what);
                return;
            }
            AsyncResultEx ar = AsyncResultEx.from(msg.obj);
            if (ar != null) {
                if (ar.getException() != null) {
                    RlogEx.e(HwUiccCardReference.LOG_TAG, "Error in SIM access with exception");
                }
                AsyncResultEx.forMessage((Message) ar.getUserObj(), ar.getResult(), ar.getException());
                ((Message) ar.getUserObj()).sendToTarget();
            }
        }
    }
}

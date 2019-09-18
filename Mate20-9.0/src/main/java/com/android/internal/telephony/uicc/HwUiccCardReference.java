package com.android.internal.telephony.uicc;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncResult;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import com.android.internal.telephony.fullnetwork.HwFullNetworkConstants;
import com.android.internal.telephony.uicc.AbstractUiccCard;
import com.android.internal.telephony.vsim.HwVSimConstants;

public class HwUiccCardReference implements AbstractUiccCard.UiccCardReference {
    protected static final boolean DBG = false;
    private static final int EVENT_GET_ATR_DONE = 105;
    private static final String LOG_TAG = "HwUiccCardReference";
    private static final int ONE_SECOND_TIME_MILLISECONDS = 1000;
    private static final int REBOOT_TOTAL_TIME_MILLISECONDS = 30000;
    private boolean bShowedTipDlg = false;
    Button mCoutDownRootButton = null;
    private Handler mHandler = new MyHandler();
    AlertDialog mSimAddDialog = null;
    public UiccCard mUiccCard;
    Resources r = Resources.getSystem();

    static class ListenerSimAddDialog implements View.OnClickListener {
        Context mContext;

        ListenerSimAddDialog(Context mContext2) {
            this.mContext = mContext2;
        }

        public void onClick(View v) {
            Intent reboot = new Intent("android.intent.action.REBOOT");
            reboot.putExtra("android.intent.extra.KEY_CONFIRM", false);
            reboot.setFlags(268435456);
            this.mContext.startActivity(reboot);
        }
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

    public HwUiccCardReference(UiccCard uiccCard) {
        this.mUiccCard = uiccCard;
    }

    public boolean hasAppActived() {
        return false;
    }

    public int getNumApplications() {
        return 0;
    }

    public void iccGetATR(Message onComplete) {
        UiccProfile uiccProfile = this.mUiccCard.getUiccProfile();
        if (uiccProfile != null) {
            uiccProfile.getCiHw().iccGetATR(this.mHandler.obtainMessage(EVENT_GET_ATR_DONE, onComplete));
        }
    }

    public AlertDialog getSimAddDialog(Context mContext, String title, String message, String buttonTxt, View.OnClickListener listener) {
        Button button = new Button(new ContextThemeWrapper(mContext, mContext.getResources().getIdentifier("androidhwext:style/Theme.Emui", null, null)));
        button.setText(buttonTxt);
        button.setOnClickListener(listener);
        AlertDialog dialog = new AlertDialog.Builder(mContext, 33947691).setTitle(title).setMessage(message).create();
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setView(button, Dp2Px(mContext, 15.0f), Dp2Px(mContext, 12.0f), Dp2Px(mContext, 15.0f), Dp2Px(mContext, 12.0f));
        return dialog;
    }

    private int Dp2Px(Context context, float dp) {
        return (int) ((dp * context.getResources().getDisplayMetrics().density) + 0.5f);
    }

    public void displayUimTipDialog(Context context, int resId) {
        if (!this.bShowedTipDlg) {
            if (context == null) {
                Rlog.e(LOG_TAG, "context ==null");
                return;
            }
            try {
                this.bShowedTipDlg = true;
                AlertDialog dialog = new AlertDialog.Builder(context, 33947691).setTitle(33685797).setMessage(resId).setPositiveButton(17039370, null).setCancelable(false).create();
                dialog.getWindow().setType(HwFullNetworkConstants.EVENT_GET_PREF_NETWORK_MODE_DONE);
                dialog.show();
            } catch (NullPointerException e) {
                this.bShowedTipDlg = false;
                Rlog.e(LOG_TAG, "displayUimTipDialog NullPointerException");
            }
        }
    }

    public boolean isAllAndCardRemoved(boolean isAdded) {
        boolean result = false;
        if (SystemProperties.getBoolean("ro.hwpp.hot_swap_restart_remov", false) && !isAdded) {
            result = true;
        }
        return result;
    }

    private AlertDialog getSimAddDialogPlk(Context mContext, String title, String message, Button btn, View.OnClickListener listener) {
        this.mSimAddDialog = new AlertDialog.Builder(mContext, 33947691).setTitle(title).setMessage(message).create();
        this.mSimAddDialog.setCancelable(false);
        this.mSimAddDialog.setCanceledOnTouchOutside(false);
        this.mSimAddDialog.setView(btn, Dp2Px(mContext, 15.0f), Dp2Px(mContext, 12.0f), Dp2Px(mContext, 15.0f), Dp2Px(mContext, 12.0f));
        return this.mSimAddDialog;
    }

    public void displayRestartDialog(Context mContext) {
        Context context = mContext;
        View.OnClickListener listener = new ListenerSimAddDialog(context);
        String title = this.r.getString(33685824);
        String message = this.r.getString(33685825);
        String buttonTxt = this.r.getString(33685826);
        this.mCoutDownRootButton = new Button(new ContextThemeWrapper(context, mContext.getResources().getIdentifier("androidhwext:style/Theme.Emui", null, null)));
        this.mCoutDownRootButton.setText(buttonTxt);
        this.mCoutDownRootButton.setOnClickListener(listener);
        this.mSimAddDialog = getSimAddDialogPlk(context, title, message, this.mCoutDownRootButton, listener);
        final Context context2 = context;
        AnonymousClass1 r0 = new CountDownTimer(HwVSimConstants.WAIT_FOR_NV_CFG_MATCH_TIMEOUT, 1000) {
            public void onTick(long millisUntilFinished) {
                if (HwUiccCardReference.this.mSimAddDialog != null && HwUiccCardReference.this.mCoutDownRootButton != null) {
                    HwUiccCardReference.this.mCoutDownRootButton.setText(String.format(HwUiccCardReference.this.r.getString(33685826), new Object[]{Integer.valueOf((int) (millisUntilFinished / 1000))}));
                }
            }

            public void onFinish() {
                HwUiccCardReference.this.mSimAddDialog.dismiss();
                Intent reboot = new Intent("android.intent.action.REBOOT");
                reboot.setFlags(268435456);
                context2.startActivity(reboot);
            }
        };
        r0.start();
        this.mSimAddDialog.getWindow().setType(HwFullNetworkConstants.EVENT_GET_PREF_NETWORK_MODE_DONE);
        this.mSimAddDialog.show();
    }
}

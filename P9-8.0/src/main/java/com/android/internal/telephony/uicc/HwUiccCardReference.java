package com.android.internal.telephony.uicc;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
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
import android.view.View.OnClickListener;
import android.widget.Button;
import com.android.internal.telephony.uicc.AbstractUiccCard.UiccCardReference;
import com.android.internal.telephony.uicc.IccCardApplicationStatus.AppState;
import com.android.internal.telephony.vsim.HwVSimConstants;
import com.huawei.utils.reflect.EasyInvokeFactory;

public class HwUiccCardReference implements UiccCardReference {
    protected static final boolean DBG = false;
    private static final int EVENT_GET_ATR_DONE = 105;
    private static final String LOG_TAG = "HwUiccCardReference";
    private static final int ONE_SECOND_TIME_MILLISECONDS = 1000;
    private static final int REBOOT_TOTAL_TIME_MILLISECONDS = 30000;
    private static UiccCardUtils uiccCardUtils = ((UiccCardUtils) EasyInvokeFactory.getInvokeUtils(UiccCardUtils.class));
    private boolean bShowedTipDlg = false;
    Button mCoutDownRootButton = null;
    private Handler mHandler = new MyHandler();
    AlertDialog mSimAddDialog = null;
    public UiccCard mUiccCard;
    Resources r = Resources.getSystem();

    static class ListenerSimAddDialog implements OnClickListener {
        Context mContext;

        ListenerSimAddDialog(Context mContext) {
            this.mContext = mContext;
        }

        public void onClick(View v) {
            Intent reboot = new Intent("android.intent.action.REBOOT");
            reboot.putExtra("android.intent.extra.KEY_CONFIRM", false);
            reboot.setFlags(268435456);
            this.mContext.startActivity(reboot);
        }
    }

    private static class MyHandler extends Handler {
        /* synthetic */ MyHandler(MyHandler -this0) {
            this();
        }

        private MyHandler() {
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HwUiccCardReference.EVENT_GET_ATR_DONE /*105*/:
                    AsyncResult ar = msg.obj;
                    if (ar.exception != null) {
                        Rlog.e(HwUiccCardReference.LOG_TAG, "Error in SIM access with exception" + ar.exception);
                    }
                    AsyncResult.forMessage((Message) ar.userObj, ar.result, ar.exception);
                    ((Message) ar.userObj).sendToTarget();
                    return;
                default:
                    Rlog.e(HwUiccCardReference.LOG_TAG, "Unknown Event " + msg.what);
                    return;
            }
        }
    }

    public HwUiccCardReference(UiccCard uiccCard) {
        this.mUiccCard = uiccCard;
    }

    public boolean hasAppActived() {
        int uiccApplicationLenght = 0;
        if (uiccCardUtils.getUiccApplications(this.mUiccCard) != null) {
            uiccApplicationLenght = uiccCardUtils.getUiccApplications(this.mUiccCard).length;
        }
        int i = 0;
        while (i < uiccApplicationLenght) {
            if (uiccCardUtils.getUiccApplications(this.mUiccCard)[i] != null && uiccCardUtils.getUiccApplications(this.mUiccCard)[i].getState() == AppState.APPSTATE_READY) {
                return true;
            }
            i++;
        }
        return false;
    }

    public int getNumApplications() {
        int count = 0;
        for (UiccCardApplication a : uiccCardUtils.getUiccApplications(this.mUiccCard)) {
            if (a != null) {
                count++;
            }
        }
        return count;
    }

    public void iccGetATR(Message onComplete) {
        uiccCardUtils.getCi(this.mUiccCard).iccGetATR(this.mHandler.obtainMessage(EVENT_GET_ATR_DONE, onComplete));
    }

    public AlertDialog getSimAddDialog(Context mContext, String title, String message, String buttonTxt, OnClickListener listener) {
        Button button = new Button(new ContextThemeWrapper(mContext, mContext.getResources().getIdentifier("androidhwext:style/Theme.Emui", null, null)));
        button.setText(buttonTxt);
        button.setOnClickListener(listener);
        AlertDialog dialog = new Builder(mContext, 33947691).setTitle(title).setMessage(message).create();
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
                AlertDialog dialog = new Builder(context, 33947691).setTitle(33685797).setMessage(resId).setPositiveButton(17039370, null).setCancelable(false).create();
                dialog.getWindow().setType(2003);
                dialog.show();
            } catch (NullPointerException e) {
                this.bShowedTipDlg = false;
                Rlog.e(LOG_TAG, "displayUimTipDialog NullPointerException");
            }
        }
    }

    public boolean isAllAndCardRemoved(boolean isAdded) {
        return SystemProperties.getBoolean("ro.hwpp.hot_swap_restart_remov", false) ? isAdded ^ 1 : false;
    }

    private AlertDialog getSimAddDialogPlk(Context mContext, String title, String message, Button btn, OnClickListener listener) {
        this.mSimAddDialog = new Builder(mContext, 33947691).setTitle(title).setMessage(message).create();
        this.mSimAddDialog.setCancelable(false);
        this.mSimAddDialog.setCanceledOnTouchOutside(false);
        this.mSimAddDialog.setView(btn, Dp2Px(mContext, 15.0f), Dp2Px(mContext, 12.0f), Dp2Px(mContext, 15.0f), Dp2Px(mContext, 12.0f));
        return this.mSimAddDialog;
    }

    public void displayRestartDialog(Context mContext) {
        OnClickListener listener = new ListenerSimAddDialog(mContext);
        String title = " ";
        String message = " ";
        String buttonTxt = " ";
        title = this.r.getString(33685824);
        message = this.r.getString(33685825);
        buttonTxt = this.r.getString(33685826);
        this.mCoutDownRootButton = new Button(new ContextThemeWrapper(mContext, mContext.getResources().getIdentifier("androidhwext:style/Theme.Emui", null, null)));
        this.mCoutDownRootButton.setText(buttonTxt);
        this.mCoutDownRootButton.setOnClickListener(listener);
        this.mSimAddDialog = getSimAddDialogPlk(mContext, title, message, this.mCoutDownRootButton, listener);
        final Context context = mContext;
        new CountDownTimer(HwVSimConstants.WAIT_FOR_NV_CFG_MATCH_TIMEOUT, 1000) {
            public void onTick(long millisUntilFinished) {
                if (HwUiccCardReference.this.mSimAddDialog != null && HwUiccCardReference.this.mCoutDownRootButton != null) {
                    HwUiccCardReference.this.mCoutDownRootButton.setText(String.format(HwUiccCardReference.this.r.getString(33685826), new Object[]{Integer.valueOf((int) (millisUntilFinished / 1000))}));
                }
            }

            public void onFinish() {
                HwUiccCardReference.this.mSimAddDialog.dismiss();
                Intent reboot = new Intent("android.intent.action.REBOOT");
                reboot.setFlags(268435456);
                context.startActivity(reboot);
            }
        }.start();
        this.mSimAddDialog.getWindow().setType(2003);
        this.mSimAddDialog.show();
    }
}

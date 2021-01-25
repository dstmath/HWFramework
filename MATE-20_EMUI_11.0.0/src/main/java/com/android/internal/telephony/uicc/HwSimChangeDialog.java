package com.android.internal.telephony.uicc;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.telephony.RlogEx;
import com.huawei.android.telephony.TelephonyManagerEx;
import com.huawei.internal.telephony.PhoneFactoryExt;
import com.huawei.internal.telephony.uicc.IccCardStatusExt;
import com.huawei.utils.HwPartResourceUtils;

public class HwSimChangeDialog {
    private static final int EVENT_CARD_ADDED = 14;
    private static final int EVENT_CARD_REMOVED = 13;
    private static final String INTERVAl = "interval";
    private static boolean IS_SHOW_TIME = SystemPropertiesEx.getBoolean("ro.config.show_count_down_time", false);
    private static final int NEGATIVE_VALUE = 0;
    private static final String NOWAIT = "nowait";
    private static final int POSITIVE_VALUE = 1;
    private static final String TAG = "HwSimChangeDialog";
    private static final long TIME_UNIT = 1000;
    private static final int WAITING_TIME = 3;
    private static final String WINDOW = "window";
    private static HwSimChangeDialog df = null;
    AlertDialog dialog;
    private Context mContext;
    private CountDownTimer mCountdownTimer;
    private int mCurrentTime = 3;

    private HwSimChangeDialog() {
    }

    public static synchronized HwSimChangeDialog getInstance() {
        HwSimChangeDialog hwSimChangeDialog;
        synchronized (HwSimChangeDialog.class) {
            if (df == null) {
                df = new HwSimChangeDialog();
            }
            hwSimChangeDialog = df;
        }
        return hwSimChangeDialog;
    }

    public AlertDialog getSimAddDialog(Context c, boolean isAdded, int mSlotId) {
        String title;
        String message;
        String buttonTxt;
        String str;
        String message2;
        this.mContext = c;
        if (IS_SHOW_TIME) {
            log("getDialogWithCountDownTimer");
            return getDialogWithCountDownTimer(c, isAdded, mSlotId);
        }
        ContextThemeWrapper themeContext = new ContextThemeWrapper(this.mContext, this.mContext.getResources().getIdentifier("androidhwext:style/Theme.Emui", null, null));
        AlertDialog alertDialog = this.dialog;
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
        Resources r = Resources.getSystem();
        if (isAdded) {
            title = r.getString(HwPartResourceUtils.getResourceId("sim_added_title"));
        } else {
            title = r.getString(HwPartResourceUtils.getResourceId("sim_removed_title"));
        }
        if (TelephonyManagerEx.isMultiSimEnabled()) {
            if (isAdded) {
                message2 = r.getString(33685757);
            } else {
                message2 = r.getString(33685758);
            }
            message = String.format(message2, Integer.valueOf(mSlotId + 1));
        } else {
            if (isAdded) {
                str = r.getString(33685759);
            } else {
                str = r.getString(33685761);
            }
            message = str;
        }
        if (isAdded) {
            buttonTxt = r.getString(33685762);
        } else {
            buttonTxt = r.getString(33685763);
        }
        this.dialog = new AlertDialog.Builder(this.mContext, 33947691).setTitle(title).setMessage(message).create();
        View.OnClickListener listenerCancel = new View.OnClickListener() {
            /* class com.android.internal.telephony.uicc.HwSimChangeDialog.AnonymousClass1 */

            @Override // android.view.View.OnClickListener
            public void onClick(View arg0) {
                if (HwSimChangeDialog.this.dialog != null) {
                    HwSimChangeDialog.this.dialog.dismiss();
                }
            }
        };
        LinearLayout layout = new LinearLayout(this.mContext);
        layout.setOrientation(0);
        if (!isAdded) {
            Button buttonConfirm = new Button(themeContext);
            buttonConfirm.setText(buttonTxt);
            buttonConfirm.setOnClickListener(listenerCancel);
            LinearLayout.LayoutParams lpButtonConfirm = new LinearLayout.LayoutParams(-2, -2, 1.0f);
            buttonConfirm.setLayoutParams(lpButtonConfirm);
            layout.addView(buttonConfirm, lpButtonConfirm);
        } else {
            Button buttonRestart = new Button(themeContext);
            buttonRestart.setText(buttonTxt);
            buttonRestart.setOnClickListener(new View.OnClickListener() {
                /* class com.android.internal.telephony.uicc.HwSimChangeDialog.AnonymousClass2 */

                @Override // android.view.View.OnClickListener
                public void onClick(View arg0) {
                    if (HwSimChangeDialog.this.dialog != null) {
                        HwSimChangeDialog.this.dialog.dismiss();
                    }
                    HwSimChangeDialog.this.rebootDevice();
                }
            });
            LinearLayout.LayoutParams lpButtonRestart = new LinearLayout.LayoutParams(-2, -2, 1.0f);
            buttonRestart.setLayoutParams(lpButtonRestart);
            Button buttonIgnore = new Button(themeContext);
            buttonIgnore.setText(r.getString(33685764));
            buttonIgnore.setOnClickListener(listenerCancel);
            LinearLayout.LayoutParams lpButtonIgnore = new LinearLayout.LayoutParams(-2, -2, 1.0f);
            buttonIgnore.setLayoutParams(lpButtonIgnore);
            layout.addView(buttonIgnore, lpButtonIgnore);
            layout.addView(buttonRestart, lpButtonRestart);
        }
        this.dialog.setCancelable(true);
        this.dialog.setCanceledOnTouchOutside(false);
        this.dialog.setView(layout, dp2Px(this.mContext, 15.0f), dp2Px(this.mContext, 12.0f), dp2Px(this.mContext, 15.0f), dp2Px(this.mContext, 12.0f));
        this.dialog.getWindow().setType(2003);
        return this.dialog;
    }

    private AlertDialog getDialogWithCountDownTimer(Context context, boolean isAdded, int mSlotId) {
        String buttonTxt;
        String message;
        String message2;
        ContextThemeWrapper themeContext = new ContextThemeWrapper(context, context.getResources().getIdentifier("androidhwext:style/Theme.Emui", null, null));
        AlertDialog alertDialog = this.dialog;
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
        if (isAdded) {
            buttonTxt = context.getString(33685762);
        } else {
            buttonTxt = context.getString(33685945, Integer.valueOf(this.mCurrentTime));
        }
        String title = context.getString(33685946);
        if (TelephonyManagerEx.isMultiSimEnabled()) {
            if (isAdded) {
                message2 = context.getString(33685757);
            } else {
                message2 = context.getString(33685758);
            }
            message = String.format(message2, Integer.valueOf(mSlotId + 1));
        } else {
            message = context.getString(33685952);
        }
        this.dialog = new AlertDialog.Builder(context, 33947691).setTitle(title).setMessage(message).create();
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(0);
        Button restartButton = new Button(themeContext);
        restartButton.setText(buttonTxt);
        restartButton.setOnClickListener(new View.OnClickListener() {
            /* class com.android.internal.telephony.uicc.HwSimChangeDialog.AnonymousClass3 */

            @Override // android.view.View.OnClickListener
            public void onClick(View arg0) {
                if (HwSimChangeDialog.this.mCountdownTimer != null) {
                    HwSimChangeDialog.this.mCountdownTimer.cancel();
                }
                if (HwSimChangeDialog.this.dialog != null) {
                    HwSimChangeDialog.this.dialog.dismiss();
                }
                HwSimChangeDialog.this.rebootDevice();
            }
        });
        LinearLayout.LayoutParams lpButtonRestart = new LinearLayout.LayoutParams(-2, -2, 1.0f);
        restartButton.setLayoutParams(lpButtonRestart);
        layout.addView(restartButton, lpButtonRestart);
        this.dialog.setCancelable(false);
        this.dialog.setCanceledOnTouchOutside(false);
        this.dialog.setView(layout, dp2Px(context, 15.0f), dp2Px(context, 12.0f), dp2Px(context, 15.0f), dp2Px(context, 12.0f));
        this.dialog.getWindow().setType(2009);
        if (!isAdded) {
            log("handleCountdown invoked.");
            handleCountdown(context, restartButton);
        }
        return this.dialog;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void rebootDevice() {
        log("rebootDevice now...");
        Intent reboot = new Intent("android.intent.action.REBOOT");
        reboot.putExtra(NOWAIT, 1);
        reboot.putExtra(INTERVAl, 1);
        reboot.putExtra(WINDOW, 0);
        Context context = this.mContext;
        if (context != null) {
            context.sendBroadcast(reboot);
        }
    }

    private void handleCountdown(final Context context, final Button restart) {
        AlertDialog alertDialog;
        if (restart != null && context != null && (alertDialog = this.dialog) != null) {
            alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                /* class com.android.internal.telephony.uicc.HwSimChangeDialog.AnonymousClass4 */

                @Override // android.content.DialogInterface.OnShowListener
                public void onShow(DialogInterface dialog) {
                    if (HwSimChangeDialog.this.mCountdownTimer != null) {
                        HwSimChangeDialog.this.mCountdownTimer.start();
                    }
                }
            });
            this.mCountdownTimer = new CountDownTimer(3000, TIME_UNIT) {
                /* class com.android.internal.telephony.uicc.HwSimChangeDialog.AnonymousClass5 */

                @Override // android.os.CountDownTimer
                public void onTick(long millisUntilFinished) {
                    HwSimChangeDialog.this.mCurrentTime = (int) (millisUntilFinished / HwSimChangeDialog.TIME_UNIT);
                    restart.setText(context.getString(33685945, Integer.valueOf(HwSimChangeDialog.this.mCurrentTime)));
                }

                @Override // android.os.CountDownTimer
                public void onFinish() {
                    restart.callOnClick();
                }
            };
            if (this.dialog.isShowing()) {
                this.mCountdownTimer.start();
            }
        }
    }

    private int dp2Px(Context context, float dp) {
        return (int) ((dp * context.getResources().getDisplayMetrics().density) + 0.5f);
    }

    public void isGoingToshowCountDownTimerDialog(int radioState, int lastRadioState, IccCardStatusExt.CardStateEx oldState, IccCardStatusExt.CardStateEx cardState, Handler handler, int phoneId) {
        if (IS_SHOW_TIME) {
            log("isGoingToshowCountDownTimerDialog radioState=" + radioState + " lastRadioState=" + lastRadioState);
            if (oldState == null || cardState == null || handler == null) {
                log("para null error!");
                return;
            }
            if (radioState == 1 && lastRadioState == 1 && oldState == IccCardStatusExt.CardStateEx.CARDSTATE_ABSENT && cardState != IccCardStatusExt.CardStateEx.CARDSTATE_ABSENT) {
                PhoneFactoryExt.getPhone(phoneId).setRadioPower(false);
            }
            if (radioState != 2 && lastRadioState == 0) {
                if (oldState == IccCardStatusExt.CardStateEx.CARDSTATE_PRESENT && cardState == IccCardStatusExt.CardStateEx.CARDSTATE_ABSENT) {
                    handler.sendMessage(handler.obtainMessage(EVENT_CARD_REMOVED, null));
                } else if (oldState == IccCardStatusExt.CardStateEx.CARDSTATE_ABSENT && cardState == IccCardStatusExt.CardStateEx.CARDSTATE_PRESENT) {
                    handler.sendMessage(handler.obtainMessage(EVENT_CARD_ADDED, null));
                }
            }
        }
    }

    private void log(String s) {
        RlogEx.i(TAG, s);
    }
}

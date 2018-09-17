package com.android.internal.telephony.uicc;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.content.res.Resources;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import com.android.internal.telephony.CommandsInterface.RadioState;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.uicc.IccCardStatus.CardState;

public class HwSimChangeDialog {
    private static final int EVENT_CARD_ADDED = 14;
    private static final int EVENT_CARD_REMOVED = 13;
    private static final String INTERVAl = "interval";
    private static boolean IS_SHOW_TIME = SystemProperties.getBoolean("ro.config.show_count_down_time", false);
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
        this.mContext = c;
        if (IS_SHOW_TIME) {
            Rlog.d(TAG, "getDialogWithCountDownTimer");
            return getDialogWithCountDownTimer(c, isAdded, mSlotId);
        }
        String title;
        String message;
        String buttonTxt;
        Context contextThemeWrapper = new ContextThemeWrapper(this.mContext, this.mContext.getResources().getIdentifier("androidhwext:style/Theme.Emui", null, null));
        if (this.dialog != null) {
            this.dialog.dismiss();
        }
        Resources r = Resources.getSystem();
        if (isAdded) {
            title = r.getString(17040991);
        } else {
            title = r.getString(17040994);
        }
        if (TelephonyManager.getDefault().isMultiSimEnabled()) {
            if (isAdded) {
                message = r.getString(33685757);
            } else {
                message = r.getString(33685758);
            }
            message = String.format(message, new Object[]{Integer.valueOf(mSlotId + 1)});
        } else if (isAdded) {
            message = r.getString(33685759);
        } else {
            message = r.getString(33685761);
        }
        if (isAdded) {
            buttonTxt = r.getString(33685762);
        } else {
            buttonTxt = r.getString(33685763);
        }
        this.dialog = new Builder(this.mContext, 33947691).setTitle(title).setMessage(message).create();
        OnClickListener listenerCancel = new OnClickListener() {
            public void onClick(View arg0) {
                if (HwSimChangeDialog.this.dialog != null) {
                    HwSimChangeDialog.this.dialog.dismiss();
                }
            }
        };
        LinearLayout layout = new LinearLayout(this.mContext);
        layout.setOrientation(0);
        if (isAdded) {
            Button buttonRestart = new Button(contextThemeWrapper);
            buttonRestart.setText(buttonTxt);
            buttonRestart.setOnClickListener(new OnClickListener() {
                public void onClick(View arg0) {
                    if (HwSimChangeDialog.this.dialog != null) {
                        HwSimChangeDialog.this.dialog.dismiss();
                    }
                    HwSimChangeDialog.this.rebootDevice();
                }
            });
            LayoutParams lpButtonRestart = new LayoutParams(-2, -2, 1.0f);
            buttonRestart.setLayoutParams(lpButtonRestart);
            Button buttonIgnore = new Button(contextThemeWrapper);
            buttonIgnore.setText(r.getString(33685764));
            buttonIgnore.setOnClickListener(listenerCancel);
            LayoutParams lpButtonIgnore = new LayoutParams(-2, -2, 1.0f);
            buttonIgnore.setLayoutParams(lpButtonIgnore);
            layout.addView(buttonIgnore, lpButtonIgnore);
            layout.addView(buttonRestart, lpButtonRestart);
        } else {
            Button buttonConfirm = new Button(contextThemeWrapper);
            buttonConfirm.setText(buttonTxt);
            buttonConfirm.setOnClickListener(listenerCancel);
            LayoutParams lpButtonConfirm = new LayoutParams(-2, -2, 1.0f);
            buttonConfirm.setLayoutParams(lpButtonConfirm);
            layout.addView(buttonConfirm, lpButtonConfirm);
        }
        this.dialog.setCancelable(true);
        this.dialog.setCanceledOnTouchOutside(false);
        this.dialog.setView(layout, Dp2Px(this.mContext, 15.0f), Dp2Px(this.mContext, 12.0f), Dp2Px(this.mContext, 15.0f), Dp2Px(this.mContext, 12.0f));
        this.dialog.getWindow().setType(2003);
        return this.dialog;
    }

    private AlertDialog getDialogWithCountDownTimer(Context context, boolean isAdded, int mSlotId) {
        String buttonTxt;
        String message;
        ContextThemeWrapper themeContext = new ContextThemeWrapper(context, context.getResources().getIdentifier("androidhwext:style/Theme.Emui", null, null));
        if (this.dialog != null) {
            this.dialog.dismiss();
        }
        if (isAdded) {
            buttonTxt = context.getString(33685762);
        } else {
            buttonTxt = context.getString(33685945, new Object[]{Integer.valueOf(this.mCurrentTime)});
        }
        String title = context.getString(33685946);
        if (TelephonyManager.getDefault().isMultiSimEnabled()) {
            if (isAdded) {
                message = context.getString(33685757);
            } else {
                message = context.getString(33685758);
            }
            message = String.format(message, new Object[]{Integer.valueOf(mSlotId + 1)});
        } else {
            message = context.getString(33685952);
        }
        this.dialog = new Builder(context, 33947691).setTitle(title).setMessage(message).create();
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(0);
        Button restartButton = new Button(themeContext);
        restartButton.setText(buttonTxt);
        restartButton.setOnClickListener(new OnClickListener() {
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
        LayoutParams lpButtonRestart = new LayoutParams(-2, -2, 1.0f);
        restartButton.setLayoutParams(lpButtonRestart);
        layout.addView(restartButton, lpButtonRestart);
        this.dialog.setCancelable(false);
        this.dialog.setCanceledOnTouchOutside(false);
        this.dialog.setView(layout, Dp2Px(context, 15.0f), Dp2Px(context, 12.0f), Dp2Px(context, 15.0f), Dp2Px(context, 12.0f));
        this.dialog.getWindow().setType(2009);
        if (!isAdded) {
            Rlog.d(TAG, "handleCountdown invoked.");
            handleCountdown(context, restartButton);
        }
        return this.dialog;
    }

    private void rebootDevice() {
        Rlog.d(TAG, "rebootDevice now...");
        Intent reboot = new Intent("android.intent.action.REBOOT");
        reboot.putExtra(NOWAIT, 1);
        reboot.putExtra(INTERVAl, 1);
        reboot.putExtra(WINDOW, 0);
        this.mContext.sendBroadcast(reboot);
    }

    private void handleCountdown(Context context, Button restart) {
        if (restart != null && context != null && this.dialog != null) {
            this.dialog.setOnShowListener(new OnShowListener() {
                public void onShow(DialogInterface dialog) {
                    if (HwSimChangeDialog.this.mCountdownTimer != null) {
                        HwSimChangeDialog.this.mCountdownTimer.start();
                    }
                }
            });
            final Button button = restart;
            final Context context2 = context;
            this.mCountdownTimer = new CountDownTimer(3000, TIME_UNIT) {
                public void onTick(long millisUntilFinished) {
                    HwSimChangeDialog.this.mCurrentTime = (int) (millisUntilFinished / HwSimChangeDialog.TIME_UNIT);
                    button.setText(context2.getString(33685945, new Object[]{Integer.valueOf(HwSimChangeDialog.this.mCurrentTime)}));
                }

                public void onFinish() {
                    button.callOnClick();
                }
            };
            if (this.dialog.isShowing()) {
                this.mCountdownTimer.start();
            }
        }
    }

    private int Dp2Px(Context context, float dp) {
        return (int) ((dp * context.getResources().getDisplayMetrics().density) + 0.5f);
    }

    public void isGoingToshowCountDownTimerDialog(RadioState radioState, RadioState lastRadioState, CardState oldState, CardState cardState, Handler handler, int phoneId) {
        if (IS_SHOW_TIME) {
            Rlog.d(TAG, "isGoingToshowCountDownTimerDialog radioState=" + radioState + " lastRadioState=" + lastRadioState);
            if (radioState == RadioState.RADIO_ON && lastRadioState == RadioState.RADIO_ON && oldState == CardState.CARDSTATE_ABSENT && cardState != CardState.CARDSTATE_ABSENT) {
                PhoneFactory.getPhone(phoneId).setRadioPower(false);
            }
            if (radioState != RadioState.RADIO_UNAVAILABLE && lastRadioState == RadioState.RADIO_OFF) {
                if (oldState == CardState.CARDSTATE_PRESENT && cardState == CardState.CARDSTATE_ABSENT) {
                    handler.sendMessage(handler.obtainMessage(13, null));
                } else if (oldState == CardState.CARDSTATE_ABSENT && cardState == CardState.CARDSTATE_PRESENT) {
                    handler.sendMessage(handler.obtainMessage(14, null));
                }
            }
        }
    }
}

package com.android.server.retaildemo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.os.CountDownTimer;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;

public class UserInactivityCountdownDialog extends AlertDialog {
    private long mCountDownDuration;
    private CountDownTimer mCountDownTimer;
    private OnCountDownExpiredListener mOnCountDownExpiredListener;
    private long mRefreshInterval;

    interface OnCountDownExpiredListener {
        void onCountDownExpired();
    }

    UserInactivityCountdownDialog(Context context, long duration, long refreshInterval) {
        super(context);
        this.mCountDownDuration = duration;
        this.mRefreshInterval = refreshInterval;
        getWindow().setType(2010);
        LayoutParams attrs = getWindow().getAttributes();
        attrs.privateFlags = 16;
        getWindow().setAttributes(attrs);
        setTitle(17039890);
        setMessage(getContext().getString(17039887, new Object[]{Long.valueOf(duration)}));
    }

    public void setOnCountDownExpiredListener(OnCountDownExpiredListener onCountDownExpiredListener) {
        this.mOnCountDownExpiredListener = onCountDownExpiredListener;
    }

    public void setPositiveButtonClickListener(OnClickListener onClickListener) {
        setButton(-1, getContext().getString(17039889), onClickListener);
    }

    public void setNegativeButtonClickListener(OnClickListener onClickListener) {
        setButton(-2, getContext().getString(17039888), onClickListener);
    }

    public void show() {
        super.show();
        final TextView messageView = (TextView) findViewById(16908299);
        messageView.post(new Runnable() {
            public void run() {
                UserInactivityCountdownDialog userInactivityCountdownDialog = UserInactivityCountdownDialog.this;
                long -get0 = UserInactivityCountdownDialog.this.mCountDownDuration;
                long -get2 = UserInactivityCountdownDialog.this.mRefreshInterval;
                final TextView textView = messageView;
                userInactivityCountdownDialog.mCountDownTimer = new CountDownTimer(-get0, -get2) {
                    public void onTick(long millisUntilFinished) {
                        textView.setText(UserInactivityCountdownDialog.this.getContext().getString(17039887, new Object[]{Long.valueOf(millisUntilFinished / 1000)}));
                    }

                    public void onFinish() {
                        UserInactivityCountdownDialog.this.dismiss();
                        if (UserInactivityCountdownDialog.this.mOnCountDownExpiredListener != null) {
                            UserInactivityCountdownDialog.this.mOnCountDownExpiredListener.onCountDownExpired();
                        }
                    }
                }.start();
            }
        });
    }

    public void onStop() {
        if (this.mCountDownTimer != null) {
            this.mCountDownTimer.cancel();
        }
    }
}

package com.android.server.am;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.WindowManager.LayoutParams;
import android.widget.Toast;

public class LockTaskNotify {
    private static final String TAG = "LockTaskNotify";
    private final Context mContext;
    private final H mHandler = new H(this, null);
    private Toast mLastToast;

    private final class H extends Handler {
        private static final int SHOW_TOAST = 3;

        /* synthetic */ H(LockTaskNotify this$0, H -this1) {
            this();
        }

        private H() {
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 3:
                    LockTaskNotify.this.handleShowToast(msg.arg1);
                    return;
                default:
                    return;
            }
        }
    }

    public LockTaskNotify(Context context) {
        this.mContext = context;
    }

    public void showToast(int lockTaskModeState) {
        this.mHandler.obtainMessage(3, lockTaskModeState, 0).sendToTarget();
    }

    public void handleShowToast(int lockTaskModeState) {
        String text = null;
        if (lockTaskModeState == 1) {
            text = this.mContext.getString(17040268);
        } else if (lockTaskModeState == 2) {
            text = this.mContext.getString(17040266);
        }
        if (text != null) {
            if (this.mLastToast != null) {
                this.mLastToast.cancel();
            }
            this.mLastToast = makeAllUserToastAndShow(text);
        }
    }

    public void show(boolean starting) {
        int showString = 17040264;
        if (starting) {
            showString = 17040265;
        }
        makeAllUserToastAndShow(this.mContext.getString(showString));
    }

    private Toast makeAllUserToastAndShow(String text) {
        Toast toast = Toast.makeText(this.mContext, text, 1);
        LayoutParams windowParams = toast.getWindowParams();
        windowParams.privateFlags |= 16;
        toast.show();
        return toast;
    }
}

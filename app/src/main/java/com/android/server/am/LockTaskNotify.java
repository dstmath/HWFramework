package com.android.server.am;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.WindowManager.LayoutParams;
import android.widget.Toast;

public class LockTaskNotify {
    private static final String TAG = "LockTaskNotify";
    private final Context mContext;
    private final H mHandler;
    private Toast mLastToast;

    private final class H extends Handler {
        private static final int SHOW_TOAST = 3;

        private H() {
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SHOW_TOAST /*3*/:
                    LockTaskNotify.this.handleShowToast(msg.arg1);
                default:
            }
        }
    }

    public LockTaskNotify(Context context) {
        this.mContext = context;
        this.mHandler = new H();
    }

    public void showToast(int lockTaskModeState) {
        this.mHandler.obtainMessage(3, lockTaskModeState, 0).sendToTarget();
    }

    public void handleShowToast(int lockTaskModeState) {
        String text = null;
        if (lockTaskModeState == 1) {
            text = this.mContext.getString(17040792);
        } else if (lockTaskModeState == 2) {
            text = this.mContext.getString(17040790);
        }
        if (text != null) {
            if (this.mLastToast != null) {
                this.mLastToast.cancel();
            }
            this.mLastToast = makeAllUserToastAndShow(text);
        }
    }

    public void show(boolean starting) {
        int showString = 17040794;
        if (starting) {
            showString = 17040793;
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

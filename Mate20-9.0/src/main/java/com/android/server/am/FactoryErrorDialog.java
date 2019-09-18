package com.android.server.am;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.WindowManager;

final class FactoryErrorDialog extends BaseErrorDialog {
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            throw new RuntimeException("Rebooting from failed factory test");
        }
    };

    public FactoryErrorDialog(Context context, CharSequence msg) {
        super(context);
        setCancelable(false);
        setTitle(context.getText(17040050));
        setMessage(msg);
        setButton(-1, context.getText(17040053), this.mHandler.obtainMessage(0));
        WindowManager.LayoutParams attrs = getWindow().getAttributes();
        attrs.setTitle("Factory Error");
        getWindow().setAttributes(attrs);
    }

    public void onStop() {
    }
}

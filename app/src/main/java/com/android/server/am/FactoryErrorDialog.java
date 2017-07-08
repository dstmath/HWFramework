package com.android.server.am;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.WindowManager.LayoutParams;

final class FactoryErrorDialog extends BaseErrorDialog {
    private final Handler mHandler;

    public FactoryErrorDialog(Context context, CharSequence msg) {
        super(context);
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                throw new RuntimeException("Rebooting from failed factory test");
            }
        };
        setCancelable(false);
        setTitle(context.getText(17040096));
        setMessage(msg);
        setButton(-1, context.getText(17040099), this.mHandler.obtainMessage(0));
        LayoutParams attrs = getWindow().getAttributes();
        attrs.setTitle("Factory Error");
        getWindow().setAttributes(attrs);
    }

    public void onStop() {
    }
}

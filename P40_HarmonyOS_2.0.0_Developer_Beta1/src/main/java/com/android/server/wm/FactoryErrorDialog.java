package com.android.server.wm;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.WindowManager;
import com.android.server.am.BaseErrorDialog;

/* access modifiers changed from: package-private */
public final class FactoryErrorDialog extends BaseErrorDialog {
    private final Handler mHandler = new Handler() {
        /* class com.android.server.wm.FactoryErrorDialog.AnonymousClass1 */

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            throw new RuntimeException("Rebooting from failed factory test");
        }
    };

    public FactoryErrorDialog(Context context, CharSequence msg) {
        super(context);
        setCancelable(false);
        setTitle(context.getText(17040136));
        setMessage(msg);
        setButton(-1, context.getText(17040139), this.mHandler.obtainMessage(0));
        WindowManager.LayoutParams attrs = getWindow().getAttributes();
        attrs.setTitle("Factory Error");
        getWindow().setAttributes(attrs);
    }

    public void onStop() {
    }
}

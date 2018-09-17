package com.android.server.am;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;

class BaseErrorDialog extends AlertDialog {
    private static final int DISABLE_BUTTONS = 1;
    private static final int ENABLE_BUTTONS = 0;
    private boolean mConsuming = true;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                BaseErrorDialog.this.mConsuming = false;
                BaseErrorDialog.this.setEnabled(true);
            } else if (msg.what == 1) {
                BaseErrorDialog.this.setEnabled(false);
            }
        }
    };

    public BaseErrorDialog(Context context) {
        super(context, 33947677);
        context.assertRuntimeOverlayThemable();
        getWindow().setType(2003);
        getWindow().setFlags(DumpState.DUMP_INTENT_FILTER_VERIFIERS, DumpState.DUMP_INTENT_FILTER_VERIFIERS);
        LayoutParams attrs = getWindow().getAttributes();
        attrs.setTitle("Error Dialog");
        getWindow().setAttributes(attrs);
    }

    public void onStart() {
        super.onStart();
        this.mHandler.sendEmptyMessage(1);
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(0), 1000);
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        if (this.mConsuming) {
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    private void setEnabled(boolean enabled) {
        Button b = (Button) findViewById(16908313);
        if (b != null) {
            b.setEnabled(enabled);
        }
        b = (Button) findViewById(16908314);
        if (b != null) {
            b.setEnabled(enabled);
        }
        b = (Button) findViewById(16908315);
        if (b != null) {
            b.setEnabled(enabled);
        }
    }
}

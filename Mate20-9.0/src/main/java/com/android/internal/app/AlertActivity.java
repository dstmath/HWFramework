package com.android.internal.app;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.hwcontrol.HwWidgetFactory;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import com.android.internal.app.AlertController;

public abstract class AlertActivity extends Activity implements DialogInterface {
    protected AlertController mAlert;
    protected AlertController.AlertParams mAlertParams;

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mAlert = HwWidgetFactory.getHwAlertController(this, this, getWindow());
        this.mAlertParams = new AlertController.AlertParams(this);
    }

    public void cancel() {
        finish();
    }

    public void dismiss() {
        if (!isFinishing()) {
            finish();
        }
    }

    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        return dispatchPopulateAccessibilityEvent(this, event);
    }

    public static boolean dispatchPopulateAccessibilityEvent(Activity act, AccessibilityEvent event) {
        event.setClassName(Dialog.class.getName());
        event.setPackageName(act.getPackageName());
        ViewGroup.LayoutParams params = act.getWindow().getAttributes();
        event.setFullScreen(params.width == -1 && params.height == -1);
        return false;
    }

    /* access modifiers changed from: protected */
    public void setupAlert() {
        this.mAlert.installContent(this.mAlertParams);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (this.mAlert.onKeyDown(keyCode, event)) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (this.mAlert.onKeyUp(keyCode, event)) {
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }
}

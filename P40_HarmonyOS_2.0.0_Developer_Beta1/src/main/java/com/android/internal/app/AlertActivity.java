package com.android.internal.app;

import android.annotation.UnsupportedAppUsage;
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
    @UnsupportedAppUsage
    protected AlertController mAlert;
    @UnsupportedAppUsage
    protected AlertController.AlertParams mAlertParams;

    /* access modifiers changed from: protected */
    @Override // android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mAlert = HwWidgetFactory.getHwAlertController(this, this, getWindow());
        this.mAlertParams = new AlertController.AlertParams(this);
    }

    @Override // android.content.DialogInterface
    public void cancel() {
        finish();
    }

    @Override // android.content.DialogInterface
    public void dismiss() {
        if (!isFinishing()) {
            finish();
        }
    }

    @Override // android.app.Activity, android.view.Window.Callback
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
    @UnsupportedAppUsage
    public void setupAlert() {
        this.mAlert.installContent(this.mAlertParams);
    }

    @Override // android.app.Activity, android.view.KeyEvent.Callback
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (this.mAlert.onKeyDown(keyCode, event)) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override // android.app.Activity, android.view.KeyEvent.Callback
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (this.mAlert.onKeyUp(keyCode, event)) {
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override // android.app.Activity
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }
}

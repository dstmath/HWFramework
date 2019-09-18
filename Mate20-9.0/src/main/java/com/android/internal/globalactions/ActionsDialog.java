package com.android.internal.globalactions;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.widget.ListView;
import com.android.internal.app.AlertController;

public final class ActionsDialog extends Dialog implements DialogInterface {
    private final ActionsAdapter mAdapter;
    private final AlertController mAlert = AlertController.create(this.mContext, this, getWindow());
    private final Context mContext = getContext();

    public ActionsDialog(Context context, AlertController.AlertParams params) {
        super(context, getDialogTheme(context));
        this.mAdapter = (ActionsAdapter) params.mAdapter;
        params.apply(this.mAlert);
    }

    private static int getDialogTheme(Context context) {
        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(16843529, outValue, true);
        return outValue.resourceId;
    }

    /* access modifiers changed from: protected */
    public void onStart() {
        super.setCanceledOnTouchOutside(true);
        super.onStart();
    }

    public ListView getListView() {
        return this.mAlert.getListView();
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mAlert.installContent();
    }

    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == 32) {
            for (int i = 0; i < this.mAdapter.getCount(); i++) {
                CharSequence label = this.mAdapter.getItem(i).getLabelForAccessibility(getContext());
                if (label != null) {
                    event.getText().add(label);
                }
            }
        }
        return super.dispatchPopulateAccessibilityEvent(event);
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
}

package com.android.server.wm;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;

public class UnsupportedDisplaySizeDialog {
    private final AlertDialog mDialog;
    private final String mPackageName;

    public UnsupportedDisplaySizeDialog(AppWarnings manager, Context context, ApplicationInfo appInfo) {
        this.mPackageName = appInfo.packageName;
        this.mDialog = new AlertDialog.Builder(context).setPositiveButton(17039370, (DialogInterface.OnClickListener) null).setMessage(context.getString(17041402, appInfo.loadSafeLabel(context.getPackageManager(), 500.0f, 5))).setView(17367338).create();
        this.mDialog.create();
        Window window = this.mDialog.getWindow();
        window.setType(2002);
        window.getAttributes().setTitle("UnsupportedDisplaySizeDialog");
        CheckBox alwaysShow = (CheckBox) this.mDialog.findViewById(16908774);
        alwaysShow.setChecked(true);
        alwaysShow.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(manager) {
            /* class com.android.server.wm.$$Lambda$UnsupportedDisplaySizeDialog$kNTis_qoFUeRNp68lF_xmreusJU */
            private final /* synthetic */ AppWarnings f$1;

            {
                this.f$1 = r2;
            }

            @Override // android.widget.CompoundButton.OnCheckedChangeListener
            public final void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                UnsupportedDisplaySizeDialog.this.lambda$new$0$UnsupportedDisplaySizeDialog(this.f$1, compoundButton, z);
            }
        });
    }

    public /* synthetic */ void lambda$new$0$UnsupportedDisplaySizeDialog(AppWarnings manager, CompoundButton buttonView, boolean isChecked) {
        manager.setPackageFlag(this.mPackageName, 1, !isChecked);
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public void show() {
        this.mDialog.show();
    }

    public void dismiss() {
        this.mDialog.dismiss();
    }
}

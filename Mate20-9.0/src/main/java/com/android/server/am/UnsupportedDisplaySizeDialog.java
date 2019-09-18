package com.android.server.am;

import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;

public class UnsupportedDisplaySizeDialog {
    private final AlertDialog mDialog;
    private final String mPackageName;

    public UnsupportedDisplaySizeDialog(AppWarnings manager, Context context, ApplicationInfo appInfo) {
        this.mPackageName = appInfo.packageName;
        this.mDialog = new AlertDialog.Builder(context).setPositiveButton(17039370, null).setMessage(context.getString(17041274, new Object[]{appInfo.loadSafeLabel(context.getPackageManager())})).setView(17367326).create();
        this.mDialog.create();
        Window window = this.mDialog.getWindow();
        window.setType(2002);
        window.getAttributes().setTitle("UnsupportedDisplaySizeDialog");
        CheckBox alwaysShow = (CheckBox) this.mDialog.findViewById(16908743);
        alwaysShow.setChecked(true);
        alwaysShow.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(manager) {
            private final /* synthetic */ AppWarnings f$1;

            {
                this.f$1 = r2;
            }

            public final void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                this.f$1.setPackageFlag(UnsupportedDisplaySizeDialog.this.mPackageName, 1, !z);
            }
        });
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

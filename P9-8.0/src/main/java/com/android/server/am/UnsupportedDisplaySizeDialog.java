package com.android.server.am;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;

public class UnsupportedDisplaySizeDialog {
    private final AlertDialog mDialog;
    private final String mPackageName;

    public UnsupportedDisplaySizeDialog(ActivityManagerService service, Context context, ApplicationInfo appInfo) {
        this.mPackageName = appInfo.packageName;
        this.mDialog = new Builder(context).setPositiveButton(17039370, null).setMessage(context.getString(17041139, new Object[]{appInfo.loadSafeLabel(context.getPackageManager())})).setView(17367308).create();
        this.mDialog.create();
        Window window = this.mDialog.getWindow();
        window.setType(2002);
        window.getAttributes().setTitle("UnsupportedDisplaySizeDialog");
        CheckBox alwaysShow = (CheckBox) this.mDialog.findViewById(16908729);
        alwaysShow.setChecked(true);
        alwaysShow.setOnCheckedChangeListener(new -$Lambda$FR3W4DSTdODBY_LnoYu7lDAj41U(this, service));
    }

    /* synthetic */ void lambda$-com_android_server_am_UnsupportedDisplaySizeDialog_2234(ActivityManagerService service, CompoundButton buttonView, boolean isChecked) {
        synchronized (service) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                service.mCompatModePackages.setPackageNotifyUnsupportedZoomLocked(this.mPackageName, isChecked);
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
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

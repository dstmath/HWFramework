package com.android.server.am;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import com.android.server.utils.AppInstallerUtil;

public class UnsupportedCompileSdkDialog {
    private final AlertDialog mDialog;
    private final String mPackageName;

    public UnsupportedCompileSdkDialog(AppWarnings manager, Context context, ApplicationInfo appInfo) {
        this.mPackageName = appInfo.packageName;
        AlertDialog.Builder builder = new AlertDialog.Builder(context).setPositiveButton(17039370, null).setMessage(context.getString(17041272, new Object[]{appInfo.loadSafeLabel(context.getPackageManager())})).setView(17367325);
        Intent installerIntent = AppInstallerUtil.createIntent(context, appInfo.packageName);
        if (installerIntent != null) {
            builder.setNeutralButton(17041271, new DialogInterface.OnClickListener(context, installerIntent) {
                private final /* synthetic */ Context f$0;
                private final /* synthetic */ Intent f$1;

                {
                    this.f$0 = r1;
                    this.f$1 = r2;
                }

                public final void onClick(DialogInterface dialogInterface, int i) {
                    this.f$0.startActivity(this.f$1);
                }
            });
        }
        this.mDialog = builder.create();
        this.mDialog.create();
        Window window = this.mDialog.getWindow();
        window.setType(2002);
        window.getAttributes().setTitle("UnsupportedCompileSdkDialog");
        CheckBox alwaysShow = (CheckBox) this.mDialog.findViewById(16908743);
        alwaysShow.setChecked(true);
        alwaysShow.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(manager) {
            private final /* synthetic */ AppWarnings f$1;

            {
                this.f$1 = r2;
            }

            public final void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                this.f$1.setPackageFlag(UnsupportedCompileSdkDialog.this.mPackageName, 2, !z);
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

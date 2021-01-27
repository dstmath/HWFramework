package com.android.server.wm;

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
        AlertDialog.Builder builder = new AlertDialog.Builder(context).setPositiveButton(17039370, (DialogInterface.OnClickListener) null).setMessage(context.getString(17041400, appInfo.loadSafeLabel(context.getPackageManager(), 500.0f, 5))).setView(17367337);
        Intent installerIntent = AppInstallerUtil.createIntent(context, appInfo.packageName);
        if (installerIntent != null) {
            builder.setNeutralButton(17041399, new DialogInterface.OnClickListener(context, installerIntent) {
                /* class com.android.server.wm.$$Lambda$UnsupportedCompileSdkDialog$s08IFWLhWLXfzf3tlanuXzZZzN8 */
                private final /* synthetic */ Context f$0;
                private final /* synthetic */ Intent f$1;

                {
                    this.f$0 = r1;
                    this.f$1 = r2;
                }

                @Override // android.content.DialogInterface.OnClickListener
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
        CheckBox alwaysShow = (CheckBox) this.mDialog.findViewById(16908774);
        alwaysShow.setChecked(true);
        alwaysShow.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(manager) {
            /* class com.android.server.wm.$$Lambda$UnsupportedCompileSdkDialog$UMRp9pktAbDwIyCxd4tnMBne_so */
            private final /* synthetic */ AppWarnings f$1;

            {
                this.f$1 = r2;
            }

            @Override // android.widget.CompoundButton.OnCheckedChangeListener
            public final void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                UnsupportedCompileSdkDialog.this.lambda$new$1$UnsupportedCompileSdkDialog(this.f$1, compoundButton, z);
            }
        });
    }

    public /* synthetic */ void lambda$new$1$UnsupportedCompileSdkDialog(AppWarnings manager, CompoundButton buttonView, boolean isChecked) {
        manager.setPackageFlag(this.mPackageName, 2, !isChecked);
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

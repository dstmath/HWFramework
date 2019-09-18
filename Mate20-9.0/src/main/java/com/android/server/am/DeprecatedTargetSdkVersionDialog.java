package com.android.server.am;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.util.Log;
import android.view.Window;
import com.android.server.utils.AppInstallerUtil;

public class DeprecatedTargetSdkVersionDialog {
    private static final String TAG = "ActivityManager";
    private final AlertDialog mDialog;
    private final String mPackageName;

    public DeprecatedTargetSdkVersionDialog(AppWarnings manager, Context context, ApplicationInfo appInfo) {
        this.mPackageName = appInfo.packageName;
        AlertDialog.Builder builder = new AlertDialog.Builder(context).setPositiveButton(17039370, new DialogInterface.OnClickListener(manager) {
            private final /* synthetic */ AppWarnings f$1;

            {
                this.f$1 = r2;
            }

            public final void onClick(DialogInterface dialogInterface, int i) {
                this.f$1.setPackageFlag(DeprecatedTargetSdkVersionDialog.this.mPackageName, 4, true);
            }
        }).setMessage(context.getString(17039935)).setTitle(appInfo.loadSafeLabel(context.getPackageManager()));
        Intent installerIntent = AppInstallerUtil.createIntent(context, appInfo.packageName);
        if (installerIntent != null) {
            builder.setNeutralButton(17039934, new DialogInterface.OnClickListener(context, installerIntent) {
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
        window.getAttributes().setTitle("DeprecatedTargetSdkVersionDialog");
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public void show() {
        Log.w("ActivityManager", "Showing SDK deprecation warning for package " + this.mPackageName);
        this.mDialog.show();
    }

    public void dismiss() {
        this.mDialog.dismiss();
    }
}

package com.android.server.wm;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.util.Log;
import android.view.Window;
import com.android.server.utils.AppInstallerUtil;

public class DeprecatedTargetSdkVersionDialog {
    private static final String TAG = "ActivityTaskManager";
    private final AlertDialog mDialog;
    private final String mPackageName;

    public DeprecatedTargetSdkVersionDialog(AppWarnings manager, Context context, ApplicationInfo appInfo) {
        this.mPackageName = appInfo.packageName;
        AlertDialog.Builder builder = new AlertDialog.Builder(context).setPositiveButton(17039370, new DialogInterface.OnClickListener(manager) {
            /* class com.android.server.wm.$$Lambda$DeprecatedTargetSdkVersionDialog$TaeLH3pyy18K9h_WuSYLeQFy9Io */
            private final /* synthetic */ AppWarnings f$1;

            {
                this.f$1 = r2;
            }

            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i) {
                DeprecatedTargetSdkVersionDialog.this.lambda$new$0$DeprecatedTargetSdkVersionDialog(this.f$1, dialogInterface, i);
            }
        }).setMessage(context.getString(17039974)).setTitle(appInfo.loadSafeLabel(context.getPackageManager(), 500.0f, 5));
        Intent installerIntent = AppInstallerUtil.createIntent(context, appInfo.packageName);
        if (installerIntent != null) {
            builder.setNeutralButton(17039973, new DialogInterface.OnClickListener(context, installerIntent) {
                /* class com.android.server.wm.$$Lambda$DeprecatedTargetSdkVersionDialog$ZkWArfvd086vsF78_zwSd67uSUs */
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
        window.getAttributes().setTitle("DeprecatedTargetSdkVersionDialog");
    }

    public /* synthetic */ void lambda$new$0$DeprecatedTargetSdkVersionDialog(AppWarnings manager, DialogInterface dialog, int which) {
        manager.setPackageFlag(this.mPackageName, 4, true);
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public void show() {
        Log.w(TAG, "Showing SDK deprecation warning for package " + this.mPackageName);
        this.mDialog.show();
    }

    public void dismiss() {
        this.mDialog.dismiss();
    }
}

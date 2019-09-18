package com.android.server.am;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Switch;

public final class CompatModeDialog extends Dialog {
    final CheckBox mAlwaysShow;
    final ApplicationInfo mAppInfo;
    final Switch mCompatEnabled = ((Switch) findViewById(16908830));
    final View mHint;
    final ActivityManagerService mService;

    public CompatModeDialog(ActivityManagerService service, Context context, ApplicationInfo appInfo) {
        super(context, 16973936);
        setCancelable(true);
        setCanceledOnTouchOutside(true);
        getWindow().requestFeature(1);
        getWindow().setType(2002);
        getWindow().setGravity(81);
        this.mService = service;
        this.mAppInfo = appInfo;
        setContentView(17367091);
        this.mCompatEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int i;
                synchronized (CompatModeDialog.this.mService) {
                    try {
                        ActivityManagerService.boostPriorityForLockedSection();
                        CompatModePackages compatModePackages = CompatModeDialog.this.mService.mCompatModePackages;
                        String str = CompatModeDialog.this.mAppInfo.packageName;
                        if (CompatModeDialog.this.mCompatEnabled.isChecked()) {
                            i = 1;
                        } else {
                            i = 0;
                        }
                        compatModePackages.setPackageScreenCompatModeLocked(str, i);
                        CompatModeDialog.this.updateControls();
                    } catch (Throwable th) {
                        while (true) {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                            throw th;
                        }
                    }
                }
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        });
        this.mAlwaysShow = (CheckBox) findViewById(16908743);
        this.mAlwaysShow.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                synchronized (CompatModeDialog.this.mService) {
                    try {
                        ActivityManagerService.boostPriorityForLockedSection();
                        CompatModeDialog.this.mService.mCompatModePackages.setPackageAskCompatModeLocked(CompatModeDialog.this.mAppInfo.packageName, CompatModeDialog.this.mAlwaysShow.isChecked());
                        CompatModeDialog.this.updateControls();
                    } catch (Throwable th) {
                        while (true) {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                            throw th;
                        }
                    }
                }
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        });
        this.mHint = findViewById(16909240);
        updateControls();
    }

    /* access modifiers changed from: package-private */
    public void updateControls() {
        synchronized (this.mService) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                int mode = this.mService.mCompatModePackages.computeCompatModeLocked(this.mAppInfo);
                Switch switchR = this.mCompatEnabled;
                int i = 0;
                boolean z = true;
                if (mode != 1) {
                    z = false;
                }
                switchR.setChecked(z);
                boolean ask = this.mService.mCompatModePackages.getPackageAskCompatModeLocked(this.mAppInfo.packageName);
                this.mAlwaysShow.setChecked(ask);
                View view = this.mHint;
                if (ask) {
                    i = 4;
                }
                view.setVisibility(i);
            } catch (Throwable th) {
                while (true) {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        ActivityManagerService.resetPriorityAfterLockedSection();
    }
}

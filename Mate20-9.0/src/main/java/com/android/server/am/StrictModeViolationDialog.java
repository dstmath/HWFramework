package com.android.server.am;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import android.view.Window;

final class StrictModeViolationDialog extends BaseErrorDialog {
    static final int ACTION_OK = 0;
    static final int ACTION_OK_AND_REPORT = 1;
    static final long DISMISS_TIMEOUT = 60000;
    private static final String TAG = "StrictModeViolationDialog";
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            synchronized (StrictModeViolationDialog.this.mService) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    if (StrictModeViolationDialog.this.mProc != null && StrictModeViolationDialog.this.mProc.crashDialog == StrictModeViolationDialog.this) {
                        StrictModeViolationDialog.this.mProc.crashDialog = null;
                    }
                } catch (Throwable th) {
                    while (true) {
                        ActivityManagerService.resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
            }
            ActivityManagerService.resetPriorityAfterLockedSection();
            StrictModeViolationDialog.this.mResult.set(msg.what);
            StrictModeViolationDialog.this.dismiss();
        }
    };
    /* access modifiers changed from: private */
    public final ProcessRecord mProc;
    /* access modifiers changed from: private */
    public final AppErrorResult mResult;
    /* access modifiers changed from: private */
    public final ActivityManagerService mService;

    /* JADX WARNING: Removed duplicated region for block: B:8:0x0071  */
    public StrictModeViolationDialog(Context context, ActivityManagerService service, AppErrorResult result, ProcessRecord app) {
        super(context);
        Resources res = context.getResources();
        this.mService = service;
        this.mProc = app;
        this.mResult = result;
        if (app.pkgList.size() == 1) {
            CharSequence applicationLabel = context.getPackageManager().getApplicationLabel(app.info);
            CharSequence name = applicationLabel;
            if (applicationLabel != null) {
                setMessage(res.getString(17041141, new Object[]{name.toString(), app.info.processName}));
                setCancelable(false);
                setButton(-1, res.getText(17039952), this.mHandler.obtainMessage(0));
                if (app.errorReportReceiver != null) {
                    setButton(-2, res.getText(17041000), this.mHandler.obtainMessage(1));
                }
                getWindow().addPrivateFlags(256);
                Window window = getWindow();
                window.setTitle("Strict Mode Violation: " + app.info.processName);
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(0), 60000);
            }
        }
        setMessage(res.getString(17041142, new Object[]{app.processName.toString()}));
        setCancelable(false);
        setButton(-1, res.getText(17039952), this.mHandler.obtainMessage(0));
        if (app.errorReportReceiver != null) {
        }
        getWindow().addPrivateFlags(256);
        Window window2 = getWindow();
        window2.setTitle("Strict Mode Violation: " + app.info.processName);
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(0), 60000);
    }
}

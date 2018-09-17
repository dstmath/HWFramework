package com.android.server.am;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;

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
                } finally {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
            StrictModeViolationDialog.this.mResult.set(msg.what);
            StrictModeViolationDialog.this.dismiss();
        }
    };
    private final ProcessRecord mProc;
    private final AppErrorResult mResult;
    private final ActivityManagerService mService;

    public StrictModeViolationDialog(Context context, ActivityManagerService service, AppErrorResult result, ProcessRecord app) {
        super(context);
        Resources res = context.getResources();
        this.mService = service;
        this.mProc = app;
        this.mResult = result;
        if (app.pkgList.size() != 1 || context.getPackageManager().getApplicationLabel(app.info) == null) {
            setMessage(res.getString(17041016, new Object[]{app.processName.toString()}));
        } else {
            setMessage(res.getString(17041015, new Object[]{context.getPackageManager().getApplicationLabel(app.info).toString(), app.info.processName}));
        }
        setCancelable(false);
        setButton(-1, res.getText(17039905), this.mHandler.obtainMessage(0));
        if (app.errorReportReceiver != null) {
            setButton(-2, res.getText(17040884), this.mHandler.obtainMessage(1));
        }
        getWindow().addPrivateFlags(256);
        getWindow().setTitle("Strict Mode Violation: " + app.info.processName);
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(0), 60000);
    }
}

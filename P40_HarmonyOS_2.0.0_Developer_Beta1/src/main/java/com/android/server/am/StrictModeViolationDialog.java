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
        /* class com.android.server.am.StrictModeViolationDialog.AnonymousClass1 */

        /* JADX INFO: finally extract failed */
        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            synchronized (StrictModeViolationDialog.this.mService) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    if (StrictModeViolationDialog.this.mProc != null && StrictModeViolationDialog.this.mProc.crashDialog == StrictModeViolationDialog.this) {
                        StrictModeViolationDialog.this.mProc.crashDialog = null;
                    }
                } catch (Throwable th) {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
            ActivityManagerService.resetPriorityAfterLockedSection();
            StrictModeViolationDialog.this.mResult.set(msg.what);
            StrictModeViolationDialog.this.dismiss();
        }
    };
    private final ProcessRecord mProc;
    private final AppErrorResult mResult;
    private final ActivityManagerService mService;

    public StrictModeViolationDialog(Context context, ActivityManagerService service, AppErrorResult result, ProcessRecord app) {
        super(context);
        CharSequence name;
        Resources res = context.getResources();
        this.mService = service;
        this.mProc = app;
        this.mResult = result;
        if (app.pkgList.size() != 1 || (name = context.getPackageManager().getApplicationLabel(app.info)) == null) {
            setMessage(res.getString(17041265, app.processName.toString()));
        } else {
            setMessage(res.getString(17041264, name.toString(), app.info.processName));
        }
        setCancelable(false);
        setButton(-1, res.getText(17039991), this.mHandler.obtainMessage(0));
        if (app.errorReportReceiver != null) {
            setButton(-2, res.getText(17041126), this.mHandler.obtainMessage(1));
        }
        getWindow().addPrivateFlags(256);
        Window window = getWindow();
        window.setTitle("Strict Mode Violation: " + app.info.processName);
        Handler handler = this.mHandler;
        handler.sendMessageDelayed(handler.obtainMessage(0), 60000);
    }
}

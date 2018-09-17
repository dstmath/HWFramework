package com.android.server.am;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.WindowManager.LayoutParams;
import com.android.server.wm.WindowState;

final class AppWaitingForDebuggerDialog extends BaseErrorDialog {
    private CharSequence mAppName;
    private final Handler mHandler;
    final ProcessRecord mProc;
    final ActivityManagerService mService;

    public AppWaitingForDebuggerDialog(ActivityManagerService service, Context context, ProcessRecord app) {
        super(context);
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case WindowState.LOW_RESOLUTION_COMPOSITION_OFF /*1*/:
                        AppWaitingForDebuggerDialog.this.mService.killAppAtUsersRequest(AppWaitingForDebuggerDialog.this.mProc, AppWaitingForDebuggerDialog.this);
                    default:
                }
            }
        };
        this.mService = service;
        this.mProc = app;
        this.mAppName = context.getPackageManager().getApplicationLabel(app.info);
        setCancelable(false);
        StringBuilder text = new StringBuilder();
        if (this.mAppName == null || this.mAppName.length() <= 0) {
            text.append("Process ");
            text.append(app.processName);
        } else {
            text.append("Application ");
            text.append(this.mAppName);
            text.append(" (process ");
            text.append(app.processName);
            text.append(")");
        }
        text.append(" is waiting for the debugger to attach.");
        setMessage(text.toString());
        setButton(-1, "Force Close", this.mHandler.obtainMessage(1, app));
        setTitle("Waiting For Debugger");
        LayoutParams attrs = getWindow().getAttributes();
        attrs.setTitle("Waiting For Debugger: " + app.info.processName);
        getWindow().setAttributes(attrs);
    }

    public void onStop() {
    }
}

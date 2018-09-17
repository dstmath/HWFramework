package com.android.server.am;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.BidiFormatter;
import android.util.Slog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.android.internal.logging.MetricsLogger;
import com.android.server.input.InputManagerService;

final class AppNotRespondingDialog extends BaseErrorDialog implements OnClickListener {
    public static final int ALREADY_SHOWING = -2;
    public static final int CANT_SHOW = -1;
    static final int FORCE_CLOSE = 1;
    private static final String TAG = "AppNotRespondingDialog";
    static final int WAIT = 2;
    static final int WAIT_AND_REPORT = 3;
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            Intent appErrorIntent = null;
            MetricsLogger.action(AppNotRespondingDialog.this.getContext(), 317, msg.what);
            switch (msg.what) {
                case 1:
                    AppNotRespondingDialog.this.mService.killAppAtUsersRequest(AppNotRespondingDialog.this.mProc, AppNotRespondingDialog.this);
                    break;
                case 2:
                case 3:
                    synchronized (AppNotRespondingDialog.this.mService) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            ProcessRecord app = AppNotRespondingDialog.this.mProc;
                            if (msg.what == 3) {
                                appErrorIntent = AppNotRespondingDialog.this.mService.mAppErrors.createAppErrorIntentLocked(app, System.currentTimeMillis(), null);
                            }
                            app.notResponding = false;
                            app.notRespondingReport = null;
                            if (app.anrDialog == AppNotRespondingDialog.this) {
                                app.anrDialog = null;
                            }
                            AppNotRespondingDialog.this.mService.mServices.scheduleServiceTimeoutLocked(app);
                        } finally {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    break;
            }
            if (appErrorIntent != null) {
                try {
                    AppNotRespondingDialog.this.getContext().startActivity(appErrorIntent);
                } catch (ActivityNotFoundException e) {
                    Slog.w(AppNotRespondingDialog.TAG, "bug report receiver dissappeared", e);
                }
            }
            AppNotRespondingDialog.this.dismiss();
        }
    };
    private final ProcessRecord mProc;
    private final ActivityManagerService mService;

    /* JADX WARNING: Removed duplicated region for block: B:22:0x00b2  */
    /* JADX WARNING: Removed duplicated region for block: B:11:0x0043  */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x0063  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public AppNotRespondingDialog(ActivityManagerService service, Context context, ProcessRecord app, ActivityRecord activity, boolean aboveSystem) {
        CharSequence name1;
        int resid;
        BidiFormatter bidi;
        CharSequence string;
        LayoutParams attrs;
        super(context);
        this.mService = service;
        this.mProc = app;
        Resources res = context.getResources();
        setCancelable(false);
        if (activity != null) {
            name1 = activity.info.loadLabel(context.getPackageManager());
        } else {
            name1 = null;
        }
        CharSequence name2 = null;
        if (app.pkgList.size() == 1) {
            name2 = context.getPackageManager().getApplicationLabel(app.info);
            if (name2 != null) {
                if (name1 != null) {
                    resid = 17039581;
                } else {
                    name1 = name2;
                    name2 = app.processName;
                    resid = 17039583;
                }
                bidi = BidiFormatter.getInstance();
                if (name2 == null) {
                    string = res.getString(resid, new Object[]{bidi.unicodeWrap(name1.toString()), bidi.unicodeWrap(name2.toString())});
                } else {
                    string = res.getString(resid, new Object[]{bidi.unicodeWrap(name1.toString())});
                }
                setTitle(string);
                if (aboveSystem) {
                    getWindow().setType(2010);
                }
                attrs = getWindow().getAttributes();
                attrs.setTitle("Application Not Responding: " + app.info.processName);
                attrs.privateFlags = InputManagerService.BTN_MOUSE;
                getWindow().setAttributes(attrs);
            }
        }
        if (name1 != null) {
            name2 = app.processName;
            resid = 17039582;
        } else {
            name1 = app.processName;
            resid = 17039584;
        }
        bidi = BidiFormatter.getInstance();
        if (name2 == null) {
        }
        setTitle(string);
        if (aboveSystem) {
        }
        attrs = getWindow().getAttributes();
        attrs.setTitle("Application Not Responding: " + app.info.processName);
        attrs.privateFlags = InputManagerService.BTN_MOUSE;
        getWindow().setAttributes(attrs);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater.from(getContext()).inflate(17367092, (FrameLayout) findViewById(16908331), true);
        TextView report = (TextView) findViewById(16908703);
        report.setOnClickListener(this);
        report.setVisibility(this.mProc.errorReportReceiver != null ? 0 : 8);
        ((TextView) findViewById(16908701)).setOnClickListener(this);
        ((TextView) findViewById(16908705)).setOnClickListener(this);
        findViewById(16908817).setVisibility(0);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case 16908701:
                this.mHandler.obtainMessage(1).sendToTarget();
                return;
            case 16908703:
                this.mHandler.obtainMessage(3).sendToTarget();
                return;
            case 16908705:
                this.mHandler.obtainMessage(2).sendToTarget();
                return;
            default:
                return;
        }
    }
}

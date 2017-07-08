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
    private final Handler mHandler;
    private final ProcessRecord mProc;
    private final ActivityManagerService mService;

    public AppNotRespondingDialog(ActivityManagerService service, Context context, ProcessRecord app, ActivityRecord activity, boolean aboveSystem) {
        CharSequence name1;
        int resid;
        BidiFormatter bidi;
        Object[] objArr;
        CharSequence string;
        LayoutParams attrs;
        super(context);
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                Intent intent = null;
                MetricsLogger.action(AppNotRespondingDialog.this.getContext(), 317, msg.what);
                switch (msg.what) {
                    case AppNotRespondingDialog.FORCE_CLOSE /*1*/:
                        AppNotRespondingDialog.this.mService.killAppAtUsersRequest(AppNotRespondingDialog.this.mProc, AppNotRespondingDialog.this);
                        break;
                    case AppNotRespondingDialog.WAIT /*2*/:
                    case AppNotRespondingDialog.WAIT_AND_REPORT /*3*/:
                        synchronized (AppNotRespondingDialog.this.mService) {
                            try {
                                ActivityManagerService.boostPriorityForLockedSection();
                                ProcessRecord app = AppNotRespondingDialog.this.mProc;
                                if (msg.what == AppNotRespondingDialog.WAIT_AND_REPORT) {
                                    intent = AppNotRespondingDialog.this.mService.mAppErrors.createAppErrorIntentLocked(app, System.currentTimeMillis(), null);
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
                if (intent != null) {
                    try {
                        AppNotRespondingDialog.this.getContext().startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        Slog.w(AppNotRespondingDialog.TAG, "bug report receiver dissappeared", e);
                    }
                }
                AppNotRespondingDialog.this.dismiss();
            }
        };
        this.mService = service;
        this.mProc = app;
        Resources res = context.getResources();
        setCancelable(false);
        if (activity != null) {
            name1 = activity.info.loadLabel(context.getPackageManager());
        } else {
            name1 = null;
        }
        CharSequence charSequence = null;
        if (app.pkgList.size() == FORCE_CLOSE) {
            charSequence = context.getPackageManager().getApplicationLabel(app.info);
            if (charSequence != null) {
                if (name1 != null) {
                    resid = 17040270;
                } else {
                    name1 = charSequence;
                    charSequence = app.processName;
                    resid = 17040272;
                }
                bidi = BidiFormatter.getInstance();
                if (charSequence == null) {
                    objArr = new Object[WAIT];
                    objArr[0] = bidi.unicodeWrap(name1.toString());
                    objArr[FORCE_CLOSE] = bidi.unicodeWrap(charSequence.toString());
                    string = res.getString(resid, objArr);
                } else {
                    objArr = new Object[FORCE_CLOSE];
                    objArr[0] = bidi.unicodeWrap(name1.toString());
                    string = res.getString(resid, objArr);
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
            charSequence = app.processName;
            resid = 17040271;
        } else {
            name1 = app.processName;
            resid = 17040273;
        }
        bidi = BidiFormatter.getInstance();
        if (charSequence == null) {
            objArr = new Object[FORCE_CLOSE];
            objArr[0] = bidi.unicodeWrap(name1.toString());
            string = res.getString(resid, objArr);
        } else {
            objArr = new Object[WAIT];
            objArr[0] = bidi.unicodeWrap(name1.toString());
            objArr[FORCE_CLOSE] = bidi.unicodeWrap(charSequence.toString());
            string = res.getString(resid, objArr);
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

    protected void onCreate(Bundle savedInstanceState) {
        boolean hasReceiver = true;
        super.onCreate(savedInstanceState);
        LayoutInflater.from(getContext()).inflate(17367091, (FrameLayout) findViewById(16908331), true);
        TextView report = (TextView) findViewById(16909105);
        report.setOnClickListener(this);
        if (this.mProc.errorReportReceiver == null) {
            hasReceiver = false;
        }
        report.setVisibility(hasReceiver ? 0 : 8);
        ((TextView) findViewById(16909103)).setOnClickListener(this);
        ((TextView) findViewById(16909104)).setOnClickListener(this);
        findViewById(16909091).setVisibility(0);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case 16909103:
                this.mHandler.obtainMessage(FORCE_CLOSE).sendToTarget();
            case 16909104:
                this.mHandler.obtainMessage(WAIT).sendToTarget();
            case 16909105:
                this.mHandler.obtainMessage(WAIT_AND_REPORT).sendToTarget();
            default:
        }
    }
}

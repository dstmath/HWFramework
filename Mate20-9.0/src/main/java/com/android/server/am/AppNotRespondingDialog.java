package com.android.server.am;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.BidiFormatter;
import android.util.Slog;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.android.internal.logging.MetricsLogger;
import com.android.server.zrhung.IZRHungService;

final class AppNotRespondingDialog extends BaseErrorDialog implements View.OnClickListener {
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
                    AppNotRespondingDialog.this.mService.zrHungSendEvent(IZRHungService.EVENT_SETTIME, 0, 0, AppNotRespondingDialog.this.mProc.info.packageName, null, "Close");
                    if (AppNotRespondingDialog.this.mProc.anrType == 2 && !AppNotRespondingDialog.this.mService.zrHungSendEvent(IZRHungService.EVENT_RECOVERRESULT, 0, 0, AppNotRespondingDialog.this.mProc.info.packageName, null, "Close")) {
                        Slog.e(AppNotRespondingDialog.TAG, "send APPEYE RECOVER failed!");
                    }
                    AppNotRespondingDialog.this.mProc.anrType = 0;
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
                            AppNotRespondingDialog.this.mService.zrHungSendEvent(IZRHungService.EVENT_SETTIME, 0, 0, AppNotRespondingDialog.this.mProc.info.packageName, null, "Wait");
                            if (AppNotRespondingDialog.this.mProc.anrType == 2 && !AppNotRespondingDialog.this.mService.zrHungSendEvent(IZRHungService.EVENT_RECOVERRESULT, 0, 0, AppNotRespondingDialog.this.mProc.info.packageName, null, "Wait")) {
                                Slog.e(AppNotRespondingDialog.TAG, "send APPEYE RECOVER failed!");
                            }
                            AppNotRespondingDialog.this.mProc.anrType = 0;
                        } catch (Throwable th) {
                            while (true) {
                                ActivityManagerService.resetPriorityAfterLockedSection();
                                throw th;
                                break;
                            }
                        }
                    }
                    ActivityManagerService.resetPriorityAfterLockedSection();
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
    /* access modifiers changed from: private */
    public final ProcessRecord mProc;
    /* access modifiers changed from: private */
    public final ActivityManagerService mService;
    private long mShowTime = 0;

    static class Data {
        final boolean aboveSystem;
        final ActivityRecord activity;
        final ProcessRecord proc;

        Data(ProcessRecord proc2, ActivityRecord activity2, boolean aboveSystem2) {
            this.proc = proc2;
            this.activity = activity2;
            this.aboveSystem = aboveSystem2;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:16:0x006f  */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x008b  */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x00a2  */
    public AppNotRespondingDialog(ActivityManagerService service, Context context, Data data) {
        super(context);
        CharSequence name1;
        int resid;
        String str;
        this.mService = service;
        this.mProc = data.proc;
        Resources res = context.getResources();
        setCancelable(false);
        if (data.activity != null) {
            name1 = data.activity.info.loadLabel(context.getPackageManager());
        } else {
            name1 = null;
        }
        CharSequence name2 = null;
        if (this.mProc.pkgList.size() == 1) {
            CharSequence applicationLabel = context.getPackageManager().getApplicationLabel(this.mProc.info);
            name2 = applicationLabel;
            if (applicationLabel != null) {
                if (name1 != null) {
                    resid = 17039588;
                } else {
                    name1 = name2;
                    name2 = this.mProc.processName;
                    resid = 17039590;
                }
                BidiFormatter bidi = BidiFormatter.getInstance();
                if (name2 == null) {
                    str = res.getString(resid, new Object[]{bidi.unicodeWrap(name1.toString()), bidi.unicodeWrap(name2.toString())});
                } else {
                    str = res.getString(resid, new Object[]{bidi.unicodeWrap(name1.toString())});
                }
                setTitle(str);
                if (data.aboveSystem) {
                    getWindow().setType(2010);
                }
                WindowManager.LayoutParams attrs = getWindow().getAttributes();
                attrs.setTitle("Application Not Responding: " + this.mProc.info.processName);
                attrs.privateFlags = 272;
                getWindow().setAttributes(attrs);
            }
        }
        if (name1 != null) {
            name2 = this.mProc.processName;
            resid = 17039589;
        } else {
            name1 = this.mProc.processName;
            resid = 17039591;
        }
        BidiFormatter bidi2 = BidiFormatter.getInstance();
        if (name2 == null) {
        }
        setTitle(str);
        if (data.aboveSystem) {
        }
        WindowManager.LayoutParams attrs2 = getWindow().getAttributes();
        attrs2.setTitle("Application Not Responding: " + this.mProc.info.processName);
        attrs2.privateFlags = 272;
        getWindow().setAttributes(attrs2);
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean hasReceiver = true;
        LayoutInflater.from(getContext()).inflate(17367092, (FrameLayout) findViewById(16908331), true);
        TextView report = (TextView) findViewById(16908716);
        report.setOnClickListener(this);
        if (this.mProc.errorReportReceiver == null) {
            hasReceiver = false;
        }
        report.setVisibility(hasReceiver ? 0 : 8);
        ((TextView) findViewById(16908714)).setOnClickListener(this);
        ((TextView) findViewById(16908718)).setOnClickListener(this);
        findViewById(16908839).setVisibility(0);
    }

    public void onClick(View v) {
        int id = v.getId();
        if (id == 16908714) {
            this.mHandler.obtainMessage(1).sendToTarget();
        } else if (id == 16908716) {
            this.mHandler.obtainMessage(3).sendToTarget();
        } else if (id == 16908718) {
            this.mHandler.obtainMessage(2).sendToTarget();
        }
    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getEventTime() >= this.mShowTime) {
            return super.dispatchTouchEvent(ev);
        }
        Slog.i(TAG, "Dropping the MotionEvent before the anr dialog was shown, eventTime:" + ev.getEventTime());
        return true;
    }

    public void show() {
        this.mShowTime = SystemClock.uptimeMillis();
        super.show();
    }
}

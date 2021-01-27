package com.android.server.am;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
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
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.zrhung.ZrHungData;
import com.android.internal.logging.MetricsLogger;
import com.android.server.HwServiceFactory;
import com.android.server.zrhung.IZRHungService;

/* access modifiers changed from: package-private */
public final class AppNotRespondingDialog extends BaseErrorDialog implements View.OnClickListener {
    public static final int ALREADY_SHOWING = -2;
    public static final int CANT_SHOW = -1;
    static final int FORCE_CLOSE = 1;
    private static final String TAG = "AppNotRespondingDialog";
    static final int WAIT = 2;
    static final int WAIT_AND_REPORT = 3;
    private final Handler mHandler = new Handler() {
        /* class com.android.server.am.AppNotRespondingDialog.AnonymousClass1 */

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            Intent appErrorIntent = null;
            MetricsLogger.action(AppNotRespondingDialog.this.getContext(), 317, msg.what);
            int i = msg.what;
            if (i == 1) {
                AppNotRespondingDialog.this.mService.killAppAtUsersRequest(AppNotRespondingDialog.this.mProc, AppNotRespondingDialog.this);
            } else if (i == 2 || i == 3) {
                synchronized (AppNotRespondingDialog.this.mService) {
                    try {
                        ActivityManagerService.boostPriorityForLockedSection();
                        ProcessRecord app = AppNotRespondingDialog.this.mProc;
                        if (msg.what == 3) {
                            appErrorIntent = AppNotRespondingDialog.this.mService.mAppErrors.createAppErrorIntentLocked(app, System.currentTimeMillis(), null);
                        }
                        app.setNotResponding(false);
                        app.notRespondingReport = null;
                        if (app.anrDialog == AppNotRespondingDialog.this) {
                            app.anrDialog = null;
                        }
                        AppNotRespondingDialog.this.mService.mServices.scheduleServiceTimeoutLocked(app);
                        AppNotRespondingDialog.this.startFaultNotify(app.processName);
                    } finally {
                        ActivityManagerService.resetPriorityAfterLockedSection();
                    }
                }
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
    private long mShowTime = 0;

    /* JADX WARNING: Removed duplicated region for block: B:16:0x006e  */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x008a  */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x00a1  */
    public AppNotRespondingDialog(ActivityManagerService service, Context context, Data data) {
        super(context);
        CharSequence name1;
        int resid;
        String str;
        this.mService = service;
        this.mProc = data.proc;
        Resources res = context.getResources();
        setCancelable(false);
        if (data.aInfo != null) {
            name1 = data.aInfo.loadLabel(context.getPackageManager());
        } else {
            name1 = null;
        }
        CharSequence name2 = null;
        if (this.mProc.pkgList.size() == 1) {
            CharSequence applicationLabel = context.getPackageManager().getApplicationLabel(this.mProc.info);
            name2 = applicationLabel;
            if (applicationLabel != null) {
                if (name1 != null) {
                    resid = 17039599;
                } else {
                    name1 = name2;
                    name2 = this.mProc.processName;
                    resid = 17039601;
                }
                BidiFormatter bidi = BidiFormatter.getInstance();
                if (name2 == null) {
                    str = res.getString(resid, bidi.unicodeWrap(name1.toString()), bidi.unicodeWrap(name2.toString()));
                } else {
                    str = res.getString(resid, bidi.unicodeWrap(name1.toString()));
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
            resid = 17039600;
        } else {
            name1 = this.mProc.processName;
            resid = 17039602;
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
    @Override // android.app.AlertDialog, android.app.Dialog
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean hasReceiver = true;
        LayoutInflater.from(getContext()).inflate(17367092, (ViewGroup) ((FrameLayout) findViewById(16908331)), true);
        TextView report = (TextView) findViewById(16908745);
        report.setOnClickListener(this);
        if (this.mProc.errorReportReceiver == null) {
            hasReceiver = false;
        }
        report.setVisibility(hasReceiver ? 0 : 8);
        ((TextView) findViewById(16908743)).setOnClickListener(this);
        ((TextView) findViewById(16908747)).setOnClickListener(this);
        findViewById(16908890).setVisibility(0);
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View v) {
        int id = v.getId();
        if (id == 16908743) {
            this.mHandler.obtainMessage(1).sendToTarget();
        } else if (id == 16908745) {
            this.mHandler.obtainMessage(3).sendToTarget();
        } else if (id == 16908747) {
            this.mHandler.obtainMessage(2).sendToTarget();
        }
    }

    @Override // android.app.Dialog, android.view.Window.Callback
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getEventTime() >= this.mShowTime) {
            return super.dispatchTouchEvent(ev);
        }
        Slog.i(TAG, "Dropping the MotionEvent before the anr dialog was shown, eventTime:" + ev.getEventTime());
        return true;
    }

    @Override // android.app.Dialog
    public void show() {
        this.mShowTime = SystemClock.uptimeMillis();
        super.show();
    }

    /* access modifiers changed from: package-private */
    public static class Data {
        final ApplicationInfo aInfo;
        final boolean aboveSystem;
        final ProcessRecord proc;

        Data(ProcessRecord proc2, ApplicationInfo aInfo2, boolean aboveSystem2) {
            this.proc = proc2;
            this.aInfo = aInfo2;
            this.aboveSystem = aboveSystem2;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startFaultNotify(String processName) {
        IZRHungService zrHungService = HwServiceFactory.getZRHungService();
        if (zrHungService != null) {
            ZrHungData beginData = new ZrHungData();
            beginData.putString("eventtype", "notifyapp");
            beginData.putString("faulttype", "anr");
            beginData.putString("processName", processName);
            zrHungService.sendEvent(beginData);
        }
    }
}

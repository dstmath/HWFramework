package com.android.server.am;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings.Global;
import android.text.BidiFormatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.android.server.input.InputManagerService;

final class AppErrorDialog extends BaseErrorDialog implements OnClickListener {
    static int ALREADY_SHOWING = 0;
    static int BACKGROUND_USER = 0;
    static final int CANCEL = 7;
    static int CANT_SHOW = 0;
    static final long DISMISS_TIMEOUT = 300000;
    static final int FORCE_QUIT = 1;
    static final int FORCE_QUIT_AND_REPORT = 2;
    static final int MUTE = 5;
    static final int RESTART = 3;
    static final int TIMEOUT = 6;
    private final boolean mForeground;
    private final Handler mHandler;
    private CharSequence mName;
    private final ProcessRecord mProc;
    private final BroadcastReceiver mReceiver;
    private final boolean mRepeating;
    private final AppErrorResult mResult;
    private final ActivityManagerService mService;

    static class Data {
        ProcessRecord proc;
        boolean repeating;
        AppErrorResult result;
        TaskRecord task;

        Data() {
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.am.AppErrorDialog.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.am.AppErrorDialog.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.am.AppErrorDialog.<clinit>():void");
    }

    public AppErrorDialog(Context context, ActivityManagerService service, Data data) {
        boolean z;
        int i;
        Object[] objArr;
        LayoutParams attrs;
        super(context);
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                int result = msg.what;
                synchronized (AppErrorDialog.this.mService) {
                    try {
                        ActivityManagerService.boostPriorityForLockedSection();
                        if (AppErrorDialog.this.mProc != null && AppErrorDialog.this.mProc.crashDialog == AppErrorDialog.this) {
                            AppErrorDialog.this.mProc.crashDialog = null;
                        }
                    } finally {
                        ActivityManagerService.resetPriorityAfterLockedSection();
                    }
                }
                AppErrorDialog.this.mResult.set(result);
                removeMessages(AppErrorDialog.TIMEOUT);
                AppErrorDialog.this.dismiss();
            }
        };
        this.mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if ("android.intent.action.CLOSE_SYSTEM_DIALOGS".equals(intent.getAction())) {
                    AppErrorDialog.this.cancel();
                }
            }
        };
        Resources res = context.getResources();
        this.mService = service;
        this.mProc = data.proc;
        this.mResult = data.result;
        this.mRepeating = data.repeating;
        if (data.task != null) {
            z = true;
        } else {
            z = false;
        }
        this.mForeground = z;
        BidiFormatter bidi = BidiFormatter.getInstance();
        if (this.mProc.pkgList.size() == FORCE_QUIT) {
            CharSequence applicationLabel = context.getPackageManager().getApplicationLabel(this.mProc.info);
            this.mName = applicationLabel;
            if (applicationLabel != null) {
                if (this.mRepeating) {
                    i = 17040260;
                } else {
                    i = 17040258;
                }
                objArr = new Object[FORCE_QUIT_AND_REPORT];
                objArr[0] = bidi.unicodeWrap(this.mName.toString());
                objArr[FORCE_QUIT] = bidi.unicodeWrap(this.mProc.info.processName);
                setTitle(res.getString(i, objArr));
                setCancelable(true);
                setCancelMessage(this.mHandler.obtainMessage(CANCEL));
                attrs = getWindow().getAttributes();
                attrs.setTitle("Application Error: " + this.mProc.info.processName);
                attrs.privateFlags |= InputManagerService.BTN_MOUSE;
                getWindow().setAttributes(attrs);
                if (this.mProc.persistent) {
                    getWindow().setType(2010);
                }
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(TIMEOUT), DISMISS_TIMEOUT);
            }
        }
        this.mName = this.mProc.processName;
        if (this.mRepeating) {
            i = 17040261;
        } else {
            i = 17040259;
        }
        objArr = new Object[FORCE_QUIT];
        objArr[0] = bidi.unicodeWrap(this.mName.toString());
        setTitle(res.getString(i, objArr));
        setCancelable(true);
        setCancelMessage(this.mHandler.obtainMessage(CANCEL));
        attrs = getWindow().getAttributes();
        attrs.setTitle("Application Error: " + this.mProc.info.processName);
        attrs.privateFlags |= InputManagerService.BTN_MOUSE;
        getWindow().setAttributes(attrs);
        if (this.mProc.persistent) {
            getWindow().setType(2010);
        }
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(TIMEOUT), DISMISS_TIMEOUT);
    }

    protected void onCreate(Bundle savedInstanceState) {
        int i;
        int i2 = 8;
        super.onCreate(savedInstanceState);
        FrameLayout frame = (FrameLayout) findViewById(16908331);
        Context context = getContext();
        LayoutInflater.from(context).inflate(17367092, frame, true);
        boolean z = !this.mRepeating ? this.mForeground : false;
        boolean hasReceiver = this.mProc.errorReportReceiver != null;
        TextView restart = (TextView) findViewById(16909106);
        restart.setOnClickListener(this);
        if (z) {
            i = 0;
        } else {
            i = 8;
        }
        restart.setVisibility(i);
        TextView report = (TextView) findViewById(16909105);
        report.setOnClickListener(this);
        if (hasReceiver) {
            i = 0;
        } else {
            i = 8;
        }
        report.setVisibility(i);
        TextView close = (TextView) findViewById(16909103);
        if (z) {
            i = 8;
        } else {
            i = 0;
        }
        close.setVisibility(i);
        close.setOnClickListener(this);
        boolean showMute = (ActivityManagerService.IS_USER_BUILD || Global.getInt(context.getContentResolver(), "development_settings_enabled", 0) == 0) ? false : true;
        TextView mute = (TextView) findViewById(16909108);
        mute.setOnClickListener(this);
        if (showMute) {
            i2 = 0;
        }
        mute.setVisibility(i2);
        findViewById(16909091).setVisibility(0);
    }

    public void onStart() {
        super.onStart();
        getContext().registerReceiver(this.mReceiver, new IntentFilter("android.intent.action.CLOSE_SYSTEM_DIALOGS"));
    }

    protected void onStop() {
        super.onStop();
        getContext().unregisterReceiver(this.mReceiver);
    }

    public void dismiss() {
        if (!this.mResult.mHasResult) {
            this.mResult.set(FORCE_QUIT);
        }
        super.dismiss();
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case 16909103:
                this.mHandler.obtainMessage(FORCE_QUIT).sendToTarget();
            case 16909105:
                this.mHandler.obtainMessage(FORCE_QUIT_AND_REPORT).sendToTarget();
            case 16909106:
                this.mHandler.obtainMessage(RESTART).sendToTarget();
            case 16909108:
                this.mHandler.obtainMessage(MUTE).sendToTarget();
            default:
        }
    }
}

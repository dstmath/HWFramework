package com.android.internal.util;

import android.app.AlarmManager;
import android.app.AlarmManager.OnAlarmListener;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

public class WakeupMessage implements OnAlarmListener {
    private final AlarmManager mAlarmManager;
    protected final int mArg1;
    protected final int mArg2;
    protected final int mCmd;
    protected final String mCmdName;
    protected final Handler mHandler;
    protected final Object mObj;
    private boolean mScheduled;

    public WakeupMessage(Context context, Handler handler, String cmdName, int cmd, int arg1, int arg2, Object obj) {
        this.mAlarmManager = (AlarmManager) context.getSystemService("alarm");
        this.mHandler = handler;
        this.mCmdName = cmdName;
        this.mCmd = cmd;
        this.mArg1 = arg1;
        this.mArg2 = arg2;
        this.mObj = obj;
    }

    public WakeupMessage(Context context, Handler handler, String cmdName, int cmd, int arg1) {
        this(context, handler, cmdName, cmd, arg1, 0, null);
    }

    public WakeupMessage(Context context, Handler handler, String cmdName, int cmd, int arg1, int arg2) {
        this(context, handler, cmdName, cmd, arg1, arg2, null);
    }

    public WakeupMessage(Context context, Handler handler, String cmdName, int cmd) {
        this(context, handler, cmdName, cmd, 0, 0, null);
    }

    public synchronized void schedule(long when) {
        this.mAlarmManager.setExact(2, when, this.mCmdName, this, this.mHandler);
        this.mScheduled = true;
    }

    public synchronized void cancel() {
        if (this.mScheduled) {
            this.mAlarmManager.cancel(this);
            this.mScheduled = false;
        }
    }

    public void onAlarm() {
        boolean stillScheduled;
        synchronized (this) {
            stillScheduled = this.mScheduled;
            this.mScheduled = false;
        }
        if (stillScheduled) {
            Message msg = this.mHandler.obtainMessage(this.mCmd, this.mArg1, this.mArg2, this.mObj);
            this.mHandler.dispatchMessage(msg);
            msg.recycle();
        }
    }
}

package com.android.server.rms.listener;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemClock;
import android.util.Log;
import com.android.server.rms.IStateChangedListener;
import com.android.server.rms.utils.Utils;

public class IdleListener {
    private static final String ACTION_TRIGGER_IDLE = "com.android.server.rms.controllers.IdleListener.ACTION_TRIGGER_IDLE";
    private static final long DURATION_TIME = 20000;
    private static final long START_TIME = 20000;
    private static final String TAG = "RMS.IdleListener";
    private Context mContext;
    private IdlenessTracker mIdleTracker;
    private IStateChangedListener mStateChangedListener;

    class IdlenessTracker extends BroadcastReceiver {
        private AlarmManager mAlarm;
        boolean mIdle;
        private PendingIntent mIdleTriggerIntent;

        public IdlenessTracker() {
            this.mAlarm = (AlarmManager) IdleListener.this.mContext.getSystemService("alarm");
            this.mIdleTriggerIntent = PendingIntent.getBroadcast(IdleListener.this.mContext, 0, new Intent(IdleListener.ACTION_TRIGGER_IDLE).setPackage("android").setFlags(1073741824), 0);
            this.mIdle = false;
        }

        public void startTracking() {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.SCREEN_ON");
            filter.addAction("android.intent.action.SCREEN_OFF");
            filter.addAction(IdleListener.ACTION_TRIGGER_IDLE);
            IdleListener.this.mContext.registerReceiver(this, filter, "android.permission.SET_TIME", null);
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.intent.action.SCREEN_ON".equals(action)) {
                if (this.mIdle) {
                    if (Utils.HWFLOW) {
                        Log.i(IdleListener.TAG, "exiting idle : " + action);
                    }
                    this.mAlarm.cancel(this.mIdleTriggerIntent);
                    this.mIdle = false;
                    IdleListener.this.onInterrupt();
                }
            } else if ("android.intent.action.SCREEN_OFF".equals(action)) {
                long nowElapsed = SystemClock.elapsedRealtime();
                long when = nowElapsed + IdleListener.START_TIME;
                if (Utils.HWFLOW) {
                    Log.i(IdleListener.TAG, "Scheduling idle : " + action + " now:" + nowElapsed);
                }
                this.mAlarm.setWindow(2, when, IdleListener.START_TIME, this.mIdleTriggerIntent);
            } else if (IdleListener.ACTION_TRIGGER_IDLE.equals(action) && !this.mIdle) {
                if (Utils.HWFLOW) {
                    Log.i(IdleListener.TAG, "Idle trigger fired @ " + SystemClock.elapsedRealtime());
                }
                this.mIdle = true;
                IdleListener.this.onTrigger();
            }
        }
    }

    public IdleListener(IStateChangedListener stateChangedListener, Context context) {
        this.mContext = context;
        this.mStateChangedListener = stateChangedListener;
        initIdleStateTracking();
    }

    public void onTrigger() {
        this.mStateChangedListener.onTrigger();
    }

    public void onInterrupt() {
        this.mStateChangedListener.onInterrupt();
    }

    private void initIdleStateTracking() {
        this.mIdleTracker = new IdlenessTracker();
        this.mIdleTracker.startTracking();
    }
}

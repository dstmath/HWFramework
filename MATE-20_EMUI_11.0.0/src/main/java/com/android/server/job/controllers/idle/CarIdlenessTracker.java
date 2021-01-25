package com.android.server.job.controllers.idle;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.util.Slog;
import com.android.server.am.ActivityManagerService;
import com.android.server.job.JobSchedulerService;
import java.io.PrintWriter;

public final class CarIdlenessTracker extends BroadcastReceiver implements IdlenessTracker {
    public static final String ACTION_FORCE_IDLE = "com.android.server.jobscheduler.FORCE_IDLE";
    public static final String ACTION_GARAGE_MODE_OFF = "com.android.server.jobscheduler.GARAGE_MODE_OFF";
    public static final String ACTION_GARAGE_MODE_ON = "com.android.server.jobscheduler.GARAGE_MODE_ON";
    public static final String ACTION_UNFORCE_IDLE = "com.android.server.jobscheduler.UNFORCE_IDLE";
    private static final boolean DEBUG = (JobSchedulerService.DEBUG || Log.isLoggable(TAG, 3));
    private static final String TAG = "JobScheduler.CarIdlenessTracker";
    private boolean mForced = false;
    private boolean mGarageModeOn = false;
    private boolean mIdle = false;
    private IdlenessListener mIdleListener;

    @Override // com.android.server.job.controllers.idle.IdlenessTracker
    public boolean isIdle() {
        return this.mIdle;
    }

    @Override // com.android.server.job.controllers.idle.IdlenessTracker
    public void startTracking(Context context, IdlenessListener listener) {
        this.mIdleListener = listener;
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SCREEN_ON");
        filter.addAction(ACTION_GARAGE_MODE_ON);
        filter.addAction(ACTION_GARAGE_MODE_OFF);
        filter.addAction(ACTION_FORCE_IDLE);
        filter.addAction(ACTION_UNFORCE_IDLE);
        filter.addAction(ActivityManagerService.ACTION_TRIGGER_IDLE);
        context.registerReceiver(this, filter);
    }

    @Override // com.android.server.job.controllers.idle.IdlenessTracker
    public void dump(PrintWriter pw) {
        pw.print("  mIdle: ");
        pw.println(this.mIdle);
        pw.print("  mGarageModeOn: ");
        pw.println(this.mGarageModeOn);
    }

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        logIfDebug("Received action: " + action);
        if (action.equals(ACTION_FORCE_IDLE)) {
            logIfDebug("Forcing idle...");
            setForceIdleState(true);
        } else if (action.equals(ACTION_UNFORCE_IDLE)) {
            logIfDebug("Unforcing idle...");
            setForceIdleState(false);
        } else if (action.equals("android.intent.action.SCREEN_ON")) {
            logIfDebug("Screen is on...");
            handleScreenOn();
        } else if (action.equals(ACTION_GARAGE_MODE_ON)) {
            logIfDebug("GarageMode is on...");
            this.mGarageModeOn = true;
            updateIdlenessState();
        } else if (action.equals(ACTION_GARAGE_MODE_OFF)) {
            logIfDebug("GarageMode is off...");
            this.mGarageModeOn = false;
            updateIdlenessState();
        } else if (!action.equals(ActivityManagerService.ACTION_TRIGGER_IDLE)) {
        } else {
            if (!this.mGarageModeOn) {
                logIfDebug("Idle trigger fired...");
                triggerIdlenessOnce();
                return;
            }
            logIfDebug("TRIGGER_IDLE received but not changing state; idle=" + this.mIdle + " screen=" + this.mGarageModeOn);
        }
    }

    private void setForceIdleState(boolean forced) {
        this.mForced = forced;
        updateIdlenessState();
    }

    private void updateIdlenessState() {
        boolean newState = this.mForced || this.mGarageModeOn;
        if (this.mIdle != newState) {
            logIfDebug("Device idleness changed. New idle=" + newState);
            this.mIdle = newState;
            this.mIdleListener.reportNewIdleState(this.mIdle);
            return;
        }
        logIfDebug("Device idleness is the same. Current idle=" + newState);
    }

    private void triggerIdlenessOnce() {
        if (this.mIdle) {
            logIfDebug("Device is already idle");
            return;
        }
        logIfDebug("Device is going idle once");
        this.mIdle = true;
        this.mIdleListener.reportNewIdleState(this.mIdle);
    }

    private void handleScreenOn() {
        if (this.mForced || this.mGarageModeOn) {
            logIfDebug("Screen is on, but device cannot exit idle");
        } else if (this.mIdle) {
            logIfDebug("Device is exiting idle");
            this.mIdle = false;
        } else {
            logIfDebug("Device is already non-idle");
        }
    }

    private static void logIfDebug(String msg) {
        if (DEBUG) {
            Slog.v(TAG, msg);
        }
    }
}

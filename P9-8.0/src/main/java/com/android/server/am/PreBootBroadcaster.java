package com.android.server.am;

import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.IIntentReceiver.Stub;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.UserHandle;
import android.util.Slog;
import com.android.internal.notification.SystemNotificationChannels;
import com.android.internal.util.ProgressReporter;
import com.android.server.UiThread;
import java.util.List;

public abstract class PreBootBroadcaster extends Stub {
    private static final int MSG_HIDE = 2;
    private static final int MSG_SHOW = 1;
    private static final String TAG = "PreBootBroadcaster";
    private Handler mHandler = new Handler(UiThread.get().getLooper(), null, true) {
        public void handleMessage(Message msg) {
            Context context = PreBootBroadcaster.this.mService.mContext;
            NotificationManager notifManager = (NotificationManager) context.getSystemService(NotificationManager.class);
            int max = msg.arg1;
            int index = msg.arg2;
            switch (msg.what) {
                case 1:
                    PendingIntent contentIntent;
                    CharSequence title = context.getText(17039578);
                    Intent intent = new Intent();
                    intent.setClassName("com.android.settings", "com.android.settings.HelpTrampoline");
                    intent.putExtra("android.intent.extra.TEXT", "help_url_upgrading");
                    if (context.getPackageManager().resolveActivity(intent, 0) != null) {
                        contentIntent = PendingIntent.getActivity(context, 0, intent, 0);
                    } else {
                        contentIntent = null;
                    }
                    notifManager.notifyAsUser(PreBootBroadcaster.TAG, 13, new Builder(PreBootBroadcaster.this.mService.mContext, SystemNotificationChannels.UPDATES).setSmallIcon(17303329).setWhen(0).setOngoing(true).setTicker(title).setColor(context.getColor(17170769)).setContentTitle(title).setContentIntent(contentIntent).setVisibility(1).setProgress(max, index, false).build(), UserHandle.of(PreBootBroadcaster.this.mUserId));
                    return;
                case 2:
                    notifManager.cancelAsUser(PreBootBroadcaster.TAG, 13, UserHandle.of(PreBootBroadcaster.this.mUserId));
                    return;
                default:
                    return;
            }
        }
    };
    private int mIndex = 0;
    private final Intent mIntent;
    private final ProgressReporter mProgress;
    private final boolean mQuiet;
    private final ActivityManagerService mService;
    private final List<ResolveInfo> mTargets;
    private final int mUserId;

    public abstract void onFinished();

    public PreBootBroadcaster(ActivityManagerService service, int userId, ProgressReporter progress, boolean quiet) {
        this.mService = service;
        this.mUserId = userId;
        this.mProgress = progress;
        this.mQuiet = quiet;
        this.mIntent = new Intent("android.intent.action.PRE_BOOT_COMPLETED");
        this.mIntent.addFlags(33554688);
        this.mTargets = this.mService.mContext.getPackageManager().queryBroadcastReceiversAsUser(this.mIntent, DumpState.DUMP_DEXOPT, UserHandle.of(userId));
    }

    public void sendNext() {
        if (this.mIndex >= this.mTargets.size()) {
            this.mHandler.obtainMessage(2).sendToTarget();
            onFinished();
        } else if (this.mService.isUserRunning(this.mUserId, 0)) {
            if (!this.mQuiet) {
                this.mHandler.obtainMessage(1, this.mTargets.size(), this.mIndex).sendToTarget();
            }
            List list = this.mTargets;
            int i = this.mIndex;
            this.mIndex = i + 1;
            ResolveInfo ri = (ResolveInfo) list.get(i);
            ComponentName componentName = ri.activityInfo.getComponentName();
            if (this.mProgress != null) {
                CharSequence label = ri.activityInfo.loadLabel(this.mService.mContext.getPackageManager());
                this.mProgress.setProgress(this.mIndex, this.mTargets.size(), this.mService.mContext.getString(17039571, new Object[]{label}));
            }
            Slog.i(TAG, "Pre-boot of " + componentName.toShortString() + " for user " + this.mUserId);
            EventLogTags.writeAmPreBoot(this.mUserId, componentName.getPackageName());
            this.mIntent.setComponent(componentName);
            this.mService.broadcastIntentLocked(null, null, this.mIntent, null, this, 0, null, null, null, -1, null, true, false, ActivityManagerService.MY_PID, 1000, this.mUserId);
        } else {
            Slog.i(TAG, "User " + this.mUserId + " is no longer running; skipping remaining receivers");
            this.mHandler.obtainMessage(2).sendToTarget();
            onFinished();
        }
    }

    public void performReceive(Intent intent, int resultCode, String data, Bundle extras, boolean ordered, boolean sticky, int sendingUser) {
        sendNext();
    }
}

package com.android.server.backup.internal;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Slog;
import com.android.server.backup.BackupManagerService;
import com.android.server.backup.UserBackupManagerService;
import java.util.Set;

public class RunInitializeReceiver extends BroadcastReceiver {
    private final UserBackupManagerService mUserBackupManagerService;

    public RunInitializeReceiver(UserBackupManagerService userBackupManagerService) {
        this.mUserBackupManagerService = userBackupManagerService;
    }

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        if (UserBackupManagerService.RUN_INITIALIZE_ACTION.equals(intent.getAction())) {
            synchronized (this.mUserBackupManagerService.getQueueLock()) {
                Set<String> pendingInits = this.mUserBackupManagerService.getPendingInits();
                Slog.v(BackupManagerService.TAG, "Running a device init; " + pendingInits.size() + " pending");
                if (pendingInits.size() > 0) {
                    this.mUserBackupManagerService.clearPendingInits();
                    PowerManager.WakeLock wakelock = this.mUserBackupManagerService.getWakelock();
                    wakelock.acquire();
                    OnTaskFinishedListener listener = new OnTaskFinishedListener(wakelock) {
                        /* class com.android.server.backup.internal.$$Lambda$RunInitializeReceiver$6NFkS59RniyJ8xe_gfe6oyt63HQ */
                        private final /* synthetic */ PowerManager.WakeLock f$0;

                        {
                            this.f$0 = r1;
                        }

                        @Override // com.android.server.backup.internal.OnTaskFinishedListener
                        public final void onFinished(String str) {
                            RunInitializeReceiver.lambda$onReceive$0(this.f$0, str);
                        }
                    };
                    this.mUserBackupManagerService.getBackupHandler().post(new PerformInitializeTask(this.mUserBackupManagerService, (String[]) pendingInits.toArray(new String[pendingInits.size()]), null, listener));
                }
            }
        }
    }
}

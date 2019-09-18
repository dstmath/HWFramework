package com.android.server.backup.internal;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.ArraySet;
import android.util.Slog;
import com.android.server.backup.BackupManagerService;

public class RunInitializeReceiver extends BroadcastReceiver {
    private final BackupManagerService mBackupManagerService;

    public RunInitializeReceiver(BackupManagerService backupManagerService) {
        this.mBackupManagerService = backupManagerService;
    }

    public void onReceive(Context context, Intent intent) {
        if (BackupManagerService.RUN_INITIALIZE_ACTION.equals(intent.getAction())) {
            synchronized (this.mBackupManagerService.getQueueLock()) {
                ArraySet<String> pendingInits = this.mBackupManagerService.getPendingInits();
                Slog.v(BackupManagerService.TAG, "Running a device init; " + pendingInits.size() + " pending");
                if (pendingInits.size() > 0) {
                    this.mBackupManagerService.clearPendingInits();
                    PowerManager.WakeLock wakelock = this.mBackupManagerService.getWakelock();
                    wakelock.acquire();
                    OnTaskFinishedListener listener = new OnTaskFinishedListener(wakelock) {
                        private final /* synthetic */ PowerManager.WakeLock f$0;

                        {
                            this.f$0 = r1;
                        }

                        public final void onFinished(String str) {
                            this.f$0.release();
                        }
                    };
                    this.mBackupManagerService.getBackupHandler().post(new PerformInitializeTask(this.mBackupManagerService, (String[]) pendingInits.toArray(new String[pendingInits.size()]), null, listener));
                }
            }
        }
    }
}

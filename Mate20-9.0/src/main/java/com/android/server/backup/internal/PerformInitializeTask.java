package com.android.server.backup.internal;

import android.app.backup.IBackupObserver;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.EventLog;
import android.util.Slog;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.backup.IBackupTransport;
import com.android.server.EventLogTags;
import com.android.server.backup.BackupManagerService;
import com.android.server.backup.TransportManager;
import com.android.server.backup.transport.TransportClient;
import com.android.server.job.JobSchedulerShellCommand;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PerformInitializeTask implements Runnable {
    private final BackupManagerService mBackupManagerService;
    private final File mBaseStateDir;
    private final OnTaskFinishedListener mListener;
    private IBackupObserver mObserver;
    private final String[] mQueue;
    private final TransportManager mTransportManager;

    public PerformInitializeTask(BackupManagerService backupManagerService, String[] transportNames, IBackupObserver observer, OnTaskFinishedListener listener) {
        this(backupManagerService, backupManagerService.getTransportManager(), transportNames, observer, listener, backupManagerService.getBaseStateDir());
    }

    @VisibleForTesting
    PerformInitializeTask(BackupManagerService backupManagerService, TransportManager transportManager, String[] transportNames, IBackupObserver observer, OnTaskFinishedListener listener, File baseStateDir) {
        this.mBackupManagerService = backupManagerService;
        this.mTransportManager = transportManager;
        this.mQueue = transportNames;
        this.mObserver = observer;
        this.mListener = listener;
        this.mBaseStateDir = baseStateDir;
    }

    private void notifyResult(String target, int status) {
        try {
            if (this.mObserver != null) {
                this.mObserver.onResult(target, status);
            }
        } catch (RemoteException e) {
            this.mObserver = null;
        }
    }

    private void notifyFinished(int status) {
        try {
            if (this.mObserver != null) {
                this.mObserver.backupFinished(status);
            }
        } catch (RemoteException e) {
            this.mObserver = null;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:49:0x017c A[LOOP:2: B:47:0x0176->B:49:0x017c, LOOP_END] */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x0193 A[LOOP:3: B:52:0x018d->B:54:0x0193, LOOP_END] */
    public void run() {
        int result;
        int i;
        String[] strArr;
        boolean z;
        long delay;
        StringBuilder sb;
        int result2;
        List<TransportClient> transportClientsToDisposeOf = new ArrayList<>(this.mQueue.length);
        int result3 = 0;
        try {
            String[] strArr2 = this.mQueue;
            int length = strArr2.length;
            result = 0;
            int result4 = 0;
            while (result4 < length) {
                try {
                    String transportName = strArr2[result4];
                    TransportClient transportClient = this.mTransportManager.getTransportClient(transportName, "PerformInitializeTask.run()");
                    if (transportClient == null) {
                        Slog.e(BackupManagerService.TAG, "Requested init for " + transportName + " but not found");
                        strArr = strArr2;
                        i = length;
                    } else {
                        transportClientsToDisposeOf.add(transportClient);
                        Slog.i(BackupManagerService.TAG, "Initializing (wiping) backup transport storage: " + transportName);
                        String transportDirName = this.mTransportManager.getTransportDirName(transportClient.getTransportComponent());
                        EventLog.writeEvent(EventLogTags.BACKUP_START, transportDirName);
                        long startRealtime = SystemClock.elapsedRealtime();
                        IBackupTransport transport = transportClient.connectOrThrow("PerformInitializeTask.run()");
                        int status = transport.initializeDevice();
                        if (status == 0) {
                            status = transport.finishBackup();
                        }
                        if (status == 0) {
                            Slog.i(BackupManagerService.TAG, "Device init successful");
                            strArr = strArr2;
                            i = length;
                            int millis = (int) (SystemClock.elapsedRealtime() - startRealtime);
                            EventLog.writeEvent(EventLogTags.BACKUP_INITIALIZE, new Object[0]);
                            this.mBackupManagerService.resetBackupState(new File(this.mBaseStateDir, transportDirName));
                            EventLog.writeEvent(EventLogTags.BACKUP_SUCCESS, new Object[]{0, Integer.valueOf(millis)});
                            this.mBackupManagerService.recordInitPending(false, transportName, transportDirName);
                            notifyResult(transportName, 0);
                        } else {
                            strArr = strArr2;
                            i = length;
                            Slog.e(BackupManagerService.TAG, "Transport error in initializeDevice()");
                            EventLog.writeEvent(EventLogTags.BACKUP_TRANSPORT_FAILURE, "(initialize)");
                            this.mBackupManagerService.recordInitPending(true, transportName, transportDirName);
                            notifyResult(transportName, status);
                            int result5 = status;
                            try {
                                delay = transport.requestBackupTime();
                                sb = new StringBuilder();
                                result2 = result5;
                            } catch (Exception e) {
                                e = e;
                                result3 = result5;
                                try {
                                    Slog.e(BackupManagerService.TAG, "Unexpected error performing init", e);
                                    result = JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
                                    for (TransportClient transportClient2 : transportClientsToDisposeOf) {
                                        this.mTransportManager.disposeOfTransportClient(transportClient2, "PerformInitializeTask.run()");
                                    }
                                    notifyFinished(result);
                                    this.mListener.onFinished("PerformInitializeTask.run()");
                                } catch (Throwable th) {
                                    th = th;
                                    result = result3;
                                    for (TransportClient transportClient3 : transportClientsToDisposeOf) {
                                        this.mTransportManager.disposeOfTransportClient(transportClient3, "PerformInitializeTask.run()");
                                    }
                                    notifyFinished(result);
                                    this.mListener.onFinished("PerformInitializeTask.run()");
                                    throw th;
                                }
                            } catch (Throwable th2) {
                                th = th2;
                                result = result5;
                                while (r4.hasNext()) {
                                }
                                notifyFinished(result);
                                this.mListener.onFinished("PerformInitializeTask.run()");
                                throw th;
                            }
                            try {
                                sb.append("Init failed on ");
                                sb.append(transportName);
                                sb.append(" resched in ");
                                sb.append(delay);
                                Slog.w(BackupManagerService.TAG, sb.toString());
                                String str = transportName;
                                TransportClient transportClient4 = transportClient;
                                z = false;
                                this.mBackupManagerService.getAlarmManager().set(0, System.currentTimeMillis() + delay, this.mBackupManagerService.getRunInitIntent());
                                result = result2;
                                result4++;
                                boolean z2 = z;
                                strArr2 = strArr;
                                length = i;
                            } catch (Exception e2) {
                                e = e2;
                                result3 = result2;
                                Slog.e(BackupManagerService.TAG, "Unexpected error performing init", e);
                                result = JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
                                while (r0.hasNext()) {
                                }
                                notifyFinished(result);
                                this.mListener.onFinished("PerformInitializeTask.run()");
                            } catch (Throwable th3) {
                                th = th3;
                                result = result2;
                                while (r4.hasNext()) {
                                }
                                notifyFinished(result);
                                this.mListener.onFinished("PerformInitializeTask.run()");
                                throw th;
                            }
                        }
                    }
                    z = false;
                    result4++;
                    boolean z22 = z;
                    strArr2 = strArr;
                    length = i;
                } catch (Exception e3) {
                    e = e3;
                    result3 = result;
                    Slog.e(BackupManagerService.TAG, "Unexpected error performing init", e);
                    result = JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
                    while (r0.hasNext()) {
                    }
                    notifyFinished(result);
                    this.mListener.onFinished("PerformInitializeTask.run()");
                } catch (Throwable th4) {
                    th = th4;
                    while (r4.hasNext()) {
                    }
                    notifyFinished(result);
                    this.mListener.onFinished("PerformInitializeTask.run()");
                    throw th;
                }
            }
            for (TransportClient transportClient5 : transportClientsToDisposeOf) {
                this.mTransportManager.disposeOfTransportClient(transportClient5, "PerformInitializeTask.run()");
            }
        } catch (Exception e4) {
            e = e4;
            Slog.e(BackupManagerService.TAG, "Unexpected error performing init", e);
            result = JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE;
            while (r0.hasNext()) {
            }
            notifyFinished(result);
            this.mListener.onFinished("PerformInitializeTask.run()");
        }
        notifyFinished(result);
        this.mListener.onFinished("PerformInitializeTask.run()");
    }
}

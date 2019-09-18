package com.android.server.backup.internal;

import android.app.backup.RestoreSet;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.EventLog;
import android.util.Pair;
import android.util.Slog;
import com.android.internal.backup.IBackupTransport;
import com.android.internal.util.Preconditions;
import com.android.server.EventLogTags;
import com.android.server.backup.BackupAgentTimeoutParameters;
import com.android.server.backup.BackupManagerService;
import com.android.server.backup.BackupRestoreTask;
import com.android.server.backup.DataChangedJournal;
import com.android.server.backup.TransportManager;
import com.android.server.backup.fullbackup.PerformAdbBackupTask;
import com.android.server.backup.fullbackup.PerformFullTransportBackupTask;
import com.android.server.backup.params.AdbBackupParams;
import com.android.server.backup.params.AdbParams;
import com.android.server.backup.params.AdbRestoreParams;
import com.android.server.backup.params.BackupParams;
import com.android.server.backup.params.ClearParams;
import com.android.server.backup.params.ClearRetryParams;
import com.android.server.backup.params.RestoreGetSetsParams;
import com.android.server.backup.params.RestoreParams;
import com.android.server.backup.restore.ActiveRestoreSession;
import com.android.server.backup.restore.PerformAdbRestoreTask;
import com.android.server.backup.restore.PerformUnifiedRestoreTask;
import com.android.server.backup.transport.TransportClient;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;

public class BackupHandler extends Handler {
    public static final int MSG_BACKUP_OPERATION_TIMEOUT = 17;
    public static final int MSG_BACKUP_RESTORE_STEP = 20;
    public static final int MSG_FULL_CONFIRMATION_TIMEOUT = 9;
    public static final int MSG_OP_COMPLETE = 21;
    public static final int MSG_REQUEST_BACKUP = 15;
    public static final int MSG_RESTORE_OPERATION_TIMEOUT = 18;
    public static final int MSG_RESTORE_SESSION_TIMEOUT = 8;
    public static final int MSG_RETRY_CLEAR = 12;
    public static final int MSG_RETRY_INIT = 11;
    public static final int MSG_RUN_ADB_BACKUP = 2;
    public static final int MSG_RUN_ADB_RESTORE = 10;
    public static final int MSG_RUN_BACKUP = 1;
    public static final int MSG_RUN_CLEAR = 4;
    public static final int MSG_RUN_FULL_TRANSPORT_BACKUP = 14;
    public static final int MSG_RUN_GET_RESTORE_SETS = 6;
    public static final int MSG_RUN_RESTORE = 3;
    public static final int MSG_SCHEDULE_BACKUP_PACKAGE = 16;
    public static final int MSG_WIDGET_BROADCAST = 13;
    private final BackupManagerService backupManagerService;
    private final BackupAgentTimeoutParameters mAgentTimeoutParameters;

    public BackupHandler(BackupManagerService backupManagerService2, Looper looper) {
        super(looper);
        this.backupManagerService = backupManagerService2;
        this.mAgentTimeoutParameters = (BackupAgentTimeoutParameters) Preconditions.checkNotNull(backupManagerService2.getAgentTimeoutParameters(), "Timeout parameters cannot be null");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:146:0x043d, code lost:
        r21 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:147:0x0443, code lost:
        if (r14.size() <= 0) goto L_0x0488;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:150:0x0459, code lost:
        r7 = r7;
        r23 = r14;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:152:?, code lost:
        r7 = new com.android.server.backup.internal.PerformBackupTask(r1.backupManagerService, r4, r5.transportDirName(), r14, r19, null, null, new com.android.server.backup.internal.$$Lambda$BackupHandler$TJcRazGYTaUxjeiX6mPLlipfZUI(r3, r4), java.util.Collections.emptyList(), false, false);
        sendMessage(obtainMessage(20, r7));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:153:0x0479, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:154:0x047b, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:155:0x047c, code lost:
        r23 = r14;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:156:0x047e, code lost:
        android.util.Slog.e(com.android.server.backup.BackupManagerService.TAG, "Transport became unavailable attempting backup or error initializing backup task", r0);
        r21 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:157:0x0488, code lost:
        r23 = r14;
        android.util.Slog.v(com.android.server.backup.BackupManagerService.TAG, "Backup requested but nothing pending");
        r21 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:158:0x0493, code lost:
        if (r21 == false) goto L_0x0495;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:159:0x0495, code lost:
        r3.disposeOfTransportClient(r4, "BH/MSG_RUN_BACKUP");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:160:0x049e, code lost:
        monitor-enter(r1.backupManagerService.getQueueLock());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:162:?, code lost:
        r1.backupManagerService.setBackupRunning(false);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:164:0x04a5, code lost:
        r1.backupManagerService.getWakelock().release();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:203:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:204:?, code lost:
        return;
     */
    public void handleMessage(Message msg) {
        IBackupTransport transport;
        String str;
        StringBuilder sb;
        Message message = msg;
        TransportManager transportManager = this.backupManagerService.getTransportManager();
        switch (message.what) {
            case 1:
                this.backupManagerService.setLastBackupPass(System.currentTimeMillis());
                TransportManager transportManager2 = transportManager;
                TransportClient transportClient = transportManager2.getCurrentTransportClient("BH/MSG_RUN_BACKUP");
                if (transportClient != null) {
                    transport = transportClient.connect("BH/MSG_RUN_BACKUP");
                } else {
                    transport = null;
                }
                if (transport == null) {
                    if (transportClient != null) {
                        transportManager2.disposeOfTransportClient(transportClient, "BH/MSG_RUN_BACKUP");
                    }
                    Slog.v(BackupManagerService.TAG, "Backup requested but no transport available");
                    synchronized (this.backupManagerService.getQueueLock()) {
                        this.backupManagerService.setBackupRunning(false);
                    }
                    this.backupManagerService.getWakelock().release();
                    return;
                }
                ArrayList<BackupRequest> queue = new ArrayList<>();
                DataChangedJournal oldJournal = this.backupManagerService.getJournal();
                synchronized (this.backupManagerService.getQueueLock()) {
                    try {
                        if (this.backupManagerService.getPendingBackups().size() > 0) {
                            try {
                                for (BackupRequest b : this.backupManagerService.getPendingBackups().values()) {
                                    queue.add(b);
                                }
                                Slog.v(BackupManagerService.TAG, "clearing pending backups");
                                this.backupManagerService.getPendingBackups().clear();
                                this.backupManagerService.setJournal(null);
                            } catch (Throwable th) {
                                th = th;
                                ArrayList<BackupRequest> arrayList = queue;
                                while (true) {
                                    try {
                                        break;
                                    } catch (Throwable th2) {
                                        th = th2;
                                    }
                                }
                                throw th;
                            }
                        }
                        break;
                    } catch (Throwable th3) {
                        th = th3;
                        ArrayList<BackupRequest> arrayList2 = queue;
                        while (true) {
                            break;
                        }
                        throw th;
                    }
                }
            case 2:
                AdbBackupParams params = (AdbBackupParams) message.obj;
                PerformAdbBackupTask task = new PerformAdbBackupTask(this.backupManagerService, params.fd, params.observer, params.includeApks, params.includeObbs, params.includeShared, params.doWidgets, params.curPassword, params.encryptPassword, params.allApps, params.includeSystem, params.doCompress, params.includeKeyValue, params.packages, params.latch);
                new Thread(task, "adb-backup").start();
                TransportManager transportManager3 = transportManager;
                return;
            case 3:
                RestoreParams params2 = (RestoreParams) message.obj;
                Slog.d(BackupManagerService.TAG, "MSG_RUN_RESTORE observer=" + params2.observer);
                PerformUnifiedRestoreTask performUnifiedRestoreTask = new PerformUnifiedRestoreTask(this.backupManagerService, params2.transportClient, params2.observer, params2.monitor, params2.token, params2.packageInfo, params2.pmToken, params2.isSystemRestore, params2.filterSet, params2.listener);
                PerformUnifiedRestoreTask task2 = performUnifiedRestoreTask;
                synchronized (this.backupManagerService.getPendingRestores()) {
                    if (this.backupManagerService.isRestoreInProgress()) {
                        Slog.d(BackupManagerService.TAG, "Restore in progress, queueing.");
                        this.backupManagerService.getPendingRestores().add(task2);
                    } else {
                        Slog.d(BackupManagerService.TAG, "Starting restore.");
                        this.backupManagerService.setRestoreInProgress(true);
                        sendMessage(obtainMessage(20, task2));
                    }
                }
                return;
            case 4:
                ClearParams params3 = (ClearParams) message.obj;
                new PerformClearTask(this.backupManagerService, params3.transportClient, params3.packageInfo, params3.listener).run();
                return;
            case 6:
                RestoreSet[] sets = null;
                RestoreGetSetsParams params4 = (RestoreGetSetsParams) message.obj;
                String callerLogString = "BH/MSG_RUN_GET_RESTORE_SETS";
                try {
                    sets = params4.transportClient.connectOrThrow(callerLogString).getAvailableRestoreSets();
                    synchronized (params4.session) {
                        params4.session.setRestoreSets(sets);
                    }
                    if (sets == null) {
                        EventLog.writeEvent(EventLogTags.RESTORE_TRANSPORT_FAILURE, new Object[0]);
                    }
                    if (params4.observer != null) {
                        try {
                            params4.observer.restoreSetsAvailable(sets);
                        } catch (RemoteException e) {
                            Slog.e(BackupManagerService.TAG, "Unable to report listing to observer");
                        } catch (Exception e2) {
                            e = e2;
                            str = BackupManagerService.TAG;
                            sb = new StringBuilder();
                            sb.append("Restore observer threw: ");
                            sb.append(e.getMessage());
                            Slog.e(str, sb.toString());
                        }
                    }
                } catch (Exception e3) {
                    try {
                        Slog.e(BackupManagerService.TAG, "Error from transport getting set list: " + e3.getMessage());
                        if (params4.observer != null) {
                            try {
                                params4.observer.restoreSetsAvailable(sets);
                            } catch (RemoteException e4) {
                                Slog.e(BackupManagerService.TAG, "Unable to report listing to observer");
                            } catch (Exception e5) {
                                e = e5;
                                str = BackupManagerService.TAG;
                                sb = new StringBuilder();
                                sb.append("Restore observer threw: ");
                                sb.append(e.getMessage());
                                Slog.e(str, sb.toString());
                            }
                        }
                    } catch (Throwable th4) {
                        RestoreSet[] sets2 = sets;
                        Throwable th5 = th4;
                        if (params4.observer != null) {
                            try {
                                params4.observer.restoreSetsAvailable(sets2);
                            } catch (RemoteException e6) {
                                Slog.e(BackupManagerService.TAG, "Unable to report listing to observer");
                            } catch (Exception e7) {
                                Slog.e(BackupManagerService.TAG, "Restore observer threw: " + e7.getMessage());
                            }
                        }
                        removeMessages(8);
                        sendEmptyMessageDelayed(8, this.mAgentTimeoutParameters.getRestoreAgentTimeoutMillis());
                        params4.listener.onFinished(callerLogString);
                        throw th5;
                    }
                }
                removeMessages(8);
                sendEmptyMessageDelayed(8, this.mAgentTimeoutParameters.getRestoreAgentTimeoutMillis());
                params4.listener.onFinished(callerLogString);
                return;
            case 8:
                synchronized (this.backupManagerService) {
                    if (this.backupManagerService.getActiveRestoreSession() != null) {
                        Slog.w(BackupManagerService.TAG, "Restore session timed out; aborting");
                        this.backupManagerService.getActiveRestoreSession().markTimedOut();
                        ActiveRestoreSession activeRestoreSession = this.backupManagerService.getActiveRestoreSession();
                        Objects.requireNonNull(activeRestoreSession);
                        post(new ActiveRestoreSession.EndRestoreRunnable(this.backupManagerService, this.backupManagerService.getActiveRestoreSession()));
                    }
                }
                return;
            case 9:
                synchronized (this.backupManagerService.getAdbBackupRestoreConfirmations()) {
                    AdbParams params5 = this.backupManagerService.getAdbBackupRestoreConfirmations().get(message.arg1);
                    if (params5 != null) {
                        Slog.i(BackupManagerService.TAG, "Full backup/restore timed out waiting for user confirmation");
                        this.backupManagerService.signalAdbBackupRestoreCompletion(params5);
                        this.backupManagerService.getAdbBackupRestoreConfirmations().delete(message.arg1);
                        if (params5.observer != null) {
                            try {
                                params5.observer.onTimeout();
                            } catch (RemoteException e8) {
                            }
                        }
                    } else {
                        Slog.d(BackupManagerService.TAG, "couldn't find params for token " + message.arg1);
                    }
                }
                return;
            case 10:
                AdbRestoreParams params6 = (AdbRestoreParams) message.obj;
                PerformAdbRestoreTask task3 = new PerformAdbRestoreTask(this.backupManagerService, params6.fd, params6.curPassword, params6.encryptPassword, params6.observer, params6.latch);
                new Thread(task3, "adb-restore").start();
                return;
            case 12:
                ClearRetryParams params7 = (ClearRetryParams) message.obj;
                this.backupManagerService.clearBackupData(params7.transportName, params7.packageName);
                return;
            case 13:
                this.backupManagerService.getContext().sendBroadcastAsUser((Intent) message.obj, UserHandle.SYSTEM);
                return;
            case 14:
                new Thread((PerformFullTransportBackupTask) message.obj, "transport-backup").start();
                return;
            case 15:
                BackupParams params8 = (BackupParams) message.obj;
                ArrayList<BackupRequest> kvQueue = new ArrayList<>();
                Iterator<String> it = params8.kvPackages.iterator();
                while (it.hasNext()) {
                    kvQueue.add(new BackupRequest(it.next()));
                }
                this.backupManagerService.setBackupRunning(true);
                this.backupManagerService.getWakelock().acquire();
                PerformBackupTask performBackupTask = new PerformBackupTask(this.backupManagerService, params8.transportClient, params8.dirName, kvQueue, null, params8.observer, params8.monitor, params8.listener, params8.fullPackages, true, params8.nonIncrementalBackup);
                sendMessage(obtainMessage(20, performBackupTask));
                return;
            case 16:
                this.backupManagerService.dataChangedImpl((String) message.obj);
                return;
            case 17:
            case 18:
                Slog.d(BackupManagerService.TAG, "Timeout message received for token=" + Integer.toHexString(message.arg1));
                this.backupManagerService.handleCancel(message.arg1, false);
                return;
            case 20:
                try {
                    ((BackupRestoreTask) message.obj).execute();
                    return;
                } catch (ClassCastException e9) {
                    Slog.e(BackupManagerService.TAG, "Invalid backup/restore task in flight, obj=" + message.obj);
                    return;
                }
            case MSG_OP_COMPLETE /*21*/:
                try {
                    Pair<BackupRestoreTask, Long> taskWithResult = (Pair) message.obj;
                    ((BackupRestoreTask) taskWithResult.first).operationComplete(((Long) taskWithResult.second).longValue());
                    return;
                } catch (ClassCastException e10) {
                    Slog.e(BackupManagerService.TAG, "Invalid completion in flight, obj=" + message.obj);
                    return;
                }
            default:
                return;
        }
    }
}

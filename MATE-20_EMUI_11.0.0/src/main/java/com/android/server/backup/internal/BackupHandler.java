package com.android.server.backup.internal;

import android.app.backup.RestoreSet;
import android.content.Intent;
import android.hardware.biometrics.face.V1_0.FaceAcquiredInfo;
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
import com.android.server.backup.UserBackupManagerService;
import com.android.server.backup.fullbackup.PerformAdbBackupTask;
import com.android.server.backup.fullbackup.PerformFullTransportBackupTask;
import com.android.server.backup.keyvalue.BackupRequest;
import com.android.server.backup.keyvalue.KeyValueBackupTask;
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
import java.util.Collections;
import java.util.List;
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
    private final UserBackupManagerService backupManagerService;
    private final BackupAgentTimeoutParameters mAgentTimeoutParameters;

    public BackupHandler(UserBackupManagerService backupManagerService2, Looper looper) {
        super(looper);
        this.backupManagerService = backupManagerService2;
        this.mAgentTimeoutParameters = (BackupAgentTimeoutParameters) Preconditions.checkNotNull(backupManagerService2.getAgentTimeoutParameters(), "Timeout parameters cannot be null");
    }

    /* JADX WARNING: Removed duplicated region for block: B:147:0x044e  */
    /* JADX WARNING: Removed duplicated region for block: B:180:? A[RETURN, SYNTHETIC] */
    @Override // android.os.Handler
    public void handleMessage(Message msg) {
        Throwable th;
        Exception e;
        StringBuilder sb;
        TransportManager transportManager = this.backupManagerService.getTransportManager();
        switch (msg.what) {
            case 1:
                this.backupManagerService.setLastBackupPass(System.currentTimeMillis());
                TransportClient transportClient = transportManager.getCurrentTransportClient("BH/MSG_RUN_BACKUP");
                IBackupTransport transport = transportClient != null ? transportClient.connect("BH/MSG_RUN_BACKUP") : null;
                if (transport == null) {
                    if (transportClient != null) {
                        transportManager.disposeOfTransportClient(transportClient, "BH/MSG_RUN_BACKUP");
                    }
                    Slog.v(BackupManagerService.TAG, "Backup requested but no transport available");
                    synchronized (this.backupManagerService.getQueueLock()) {
                        this.backupManagerService.setBackupRunning(false);
                    }
                    this.backupManagerService.getWakelock().release();
                    return;
                }
                List<String> queue = new ArrayList<>();
                DataChangedJournal oldJournal = this.backupManagerService.getJournal();
                synchronized (this.backupManagerService.getQueueLock()) {
                    try {
                        if (this.backupManagerService.getPendingBackups().size() > 0) {
                            try {
                                for (BackupRequest b : this.backupManagerService.getPendingBackups().values()) {
                                    queue.add(b.packageName);
                                }
                                Slog.v(BackupManagerService.TAG, "clearing pending backups");
                                this.backupManagerService.getPendingBackups().clear();
                                this.backupManagerService.setJournal(null);
                            } catch (Throwable th2) {
                                th = th2;
                                while (true) {
                                    try {
                                        break;
                                    } catch (Throwable th3) {
                                        th = th3;
                                    }
                                }
                                throw th;
                            }
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        while (true) {
                            break;
                        }
                        throw th;
                    }
                }
                boolean staged = true;
                if (queue.size() > 0) {
                    try {
                        try {
                            KeyValueBackupTask.start(this.backupManagerService, transportClient, transport.transportDirName(), queue, oldJournal, null, null, new OnTaskFinishedListener(transportClient) {
                                /* class com.android.server.backup.internal.$$Lambda$BackupHandler$TJcRazGYTaUxjeiX6mPLlipfZUI */
                                private final /* synthetic */ TransportClient f$1;

                                {
                                    this.f$1 = r2;
                                }

                                @Override // com.android.server.backup.internal.OnTaskFinishedListener
                                public final void onFinished(String str) {
                                    BackupHandler.lambda$handleMessage$0(TransportManager.this, this.f$1, str);
                                }
                            }, Collections.emptyList(), false, false);
                        } catch (Exception e2) {
                            e = e2;
                        }
                    } catch (Exception e3) {
                        e = e3;
                        Slog.e(BackupManagerService.TAG, "Transport became unavailable attempting backup or error initializing backup task", e);
                        staged = false;
                        if (staged) {
                        }
                    }
                } else {
                    Slog.v(BackupManagerService.TAG, "Backup requested but nothing pending");
                    staged = false;
                }
                if (staged) {
                    transportManager.disposeOfTransportClient(transportClient, "BH/MSG_RUN_BACKUP");
                    synchronized (this.backupManagerService.getQueueLock()) {
                        this.backupManagerService.setBackupRunning(false);
                    }
                    this.backupManagerService.getWakelock().release();
                    return;
                }
                return;
            case 2:
                AdbBackupParams params = (AdbBackupParams) msg.obj;
                new Thread(new PerformAdbBackupTask(this.backupManagerService, params.fd, params.observer, params.includeApks, params.includeObbs, params.includeShared, params.doWidgets, params.curPassword, params.encryptPassword, params.allApps, params.includeSystem, params.doCompress, params.includeKeyValue, params.packages, params.latch), "adb-backup").start();
                return;
            case 3:
                RestoreParams params2 = (RestoreParams) msg.obj;
                Slog.d(BackupManagerService.TAG, "MSG_RUN_RESTORE observer=" + params2.observer);
                PerformUnifiedRestoreTask task = new PerformUnifiedRestoreTask(this.backupManagerService, params2.transportClient, params2.observer, params2.monitor, params2.token, params2.packageInfo, params2.pmToken, params2.isSystemRestore, params2.filterSet, params2.listener);
                synchronized (this.backupManagerService.getPendingRestores()) {
                    if (this.backupManagerService.isRestoreInProgress()) {
                        Slog.d(BackupManagerService.TAG, "Restore in progress, queueing.");
                        this.backupManagerService.getPendingRestores().add(task);
                    } else {
                        Slog.d(BackupManagerService.TAG, "Starting restore.");
                        this.backupManagerService.setRestoreInProgress(true);
                        sendMessage(obtainMessage(20, task));
                    }
                }
                return;
            case 4:
                ClearParams params3 = (ClearParams) msg.obj;
                new PerformClearTask(this.backupManagerService, params3.transportClient, params3.packageInfo, params3.listener).run();
                return;
            case 5:
            case 7:
            case 11:
            case FaceAcquiredInfo.FACE_OBSCURED /* 19 */:
            default:
                return;
            case 6:
                RestoreGetSetsParams params4 = (RestoreGetSetsParams) msg.obj;
                try {
                    RestoreSet[] sets = params4.transportClient.connectOrThrow("BH/MSG_RUN_GET_RESTORE_SETS").getAvailableRestoreSets();
                    synchronized (params4.session) {
                        params4.session.setRestoreSets(sets);
                    }
                    if (sets == null) {
                        EventLog.writeEvent((int) EventLogTags.RESTORE_TRANSPORT_FAILURE, new Object[0]);
                    }
                    if (params4.observer != null) {
                        try {
                            params4.observer.restoreSetsAvailable(sets);
                        } catch (RemoteException e4) {
                            Slog.e(BackupManagerService.TAG, "Unable to report listing to observer");
                        } catch (Exception e5) {
                            e = e5;
                            sb = new StringBuilder();
                            sb.append("Restore observer threw: ");
                            sb.append(e.getMessage());
                            Slog.e(BackupManagerService.TAG, sb.toString());
                        }
                    }
                } catch (Exception e6) {
                    Slog.e(BackupManagerService.TAG, "Error from transport getting set list: " + e6.getMessage());
                    if (params4.observer != null) {
                        try {
                            params4.observer.restoreSetsAvailable((RestoreSet[]) null);
                        } catch (RemoteException e7) {
                            Slog.e(BackupManagerService.TAG, "Unable to report listing to observer");
                        } catch (Exception e8) {
                            e = e8;
                            sb = new StringBuilder();
                            sb.append("Restore observer threw: ");
                            sb.append(e.getMessage());
                            Slog.e(BackupManagerService.TAG, sb.toString());
                        }
                    }
                } catch (Throwable th5) {
                    if (params4.observer != null) {
                        try {
                            params4.observer.restoreSetsAvailable((RestoreSet[]) null);
                        } catch (RemoteException e9) {
                            Slog.e(BackupManagerService.TAG, "Unable to report listing to observer");
                        } catch (Exception e10) {
                            Slog.e(BackupManagerService.TAG, "Restore observer threw: " + e10.getMessage());
                        }
                    }
                    removeMessages(8);
                    sendEmptyMessageDelayed(8, this.mAgentTimeoutParameters.getRestoreAgentTimeoutMillis());
                    params4.listener.onFinished("BH/MSG_RUN_GET_RESTORE_SETS");
                    throw th5;
                }
                removeMessages(8);
                sendEmptyMessageDelayed(8, this.mAgentTimeoutParameters.getRestoreAgentTimeoutMillis());
                params4.listener.onFinished("BH/MSG_RUN_GET_RESTORE_SETS");
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
                    AdbParams params5 = this.backupManagerService.getAdbBackupRestoreConfirmations().get(msg.arg1);
                    if (params5 != null) {
                        Slog.i(BackupManagerService.TAG, "Full backup/restore timed out waiting for user confirmation");
                        this.backupManagerService.signalAdbBackupRestoreCompletion(params5);
                        this.backupManagerService.getAdbBackupRestoreConfirmations().delete(msg.arg1);
                        if (params5.observer != null) {
                            try {
                                params5.observer.onTimeout();
                            } catch (RemoteException e11) {
                            }
                        }
                    } else {
                        Slog.d(BackupManagerService.TAG, "couldn't find params for token " + msg.arg1);
                    }
                }
                return;
            case 10:
                AdbRestoreParams params6 = (AdbRestoreParams) msg.obj;
                new Thread(new PerformAdbRestoreTask(this.backupManagerService, params6.fd, params6.curPassword, params6.encryptPassword, params6.observer, params6.latch), "adb-restore").start();
                return;
            case 12:
                ClearRetryParams params7 = (ClearRetryParams) msg.obj;
                this.backupManagerService.clearBackupData(params7.transportName, params7.packageName);
                return;
            case 13:
                this.backupManagerService.getContext().sendBroadcastAsUser((Intent) msg.obj, UserHandle.SYSTEM);
                return;
            case 14:
                new Thread((PerformFullTransportBackupTask) msg.obj, "transport-backup").start();
                return;
            case 15:
                BackupParams params8 = (BackupParams) msg.obj;
                this.backupManagerService.setBackupRunning(true);
                this.backupManagerService.getWakelock().acquire();
                KeyValueBackupTask.start(this.backupManagerService, params8.transportClient, params8.dirName, params8.kvPackages, null, params8.observer, params8.monitor, params8.listener, params8.fullPackages, true, params8.nonIncrementalBackup);
                return;
            case 16:
                this.backupManagerService.dataChangedImpl((String) msg.obj);
                return;
            case 17:
            case 18:
                Slog.d(BackupManagerService.TAG, "Timeout message received for token=" + Integer.toHexString(msg.arg1));
                this.backupManagerService.handleCancel(msg.arg1, false);
                return;
            case 20:
                try {
                    ((BackupRestoreTask) msg.obj).execute();
                    return;
                } catch (ClassCastException e12) {
                    Slog.e(BackupManagerService.TAG, "Invalid backup/restore task in flight, obj=" + msg.obj);
                    return;
                }
            case 21:
                try {
                    Pair<BackupRestoreTask, Long> taskWithResult = (Pair) msg.obj;
                    ((BackupRestoreTask) taskWithResult.first).operationComplete(((Long) taskWithResult.second).longValue());
                    return;
                } catch (ClassCastException e13) {
                    Slog.e(BackupManagerService.TAG, "Invalid completion in flight, obj=" + msg.obj);
                    return;
                }
        }
    }
}

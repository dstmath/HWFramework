package com.android.server.backup.restore;

import android.app.backup.IBackupManagerMonitor;
import android.app.backup.IRestoreObserver;
import android.app.backup.IRestoreSession;
import android.app.backup.RestoreSet;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.Slog;
import com.android.server.backup.BackupManagerService;
import com.android.server.backup.TransportManager;
import com.android.server.backup.internal.OnTaskFinishedListener;
import com.android.server.backup.params.RestoreGetSetsParams;
import com.android.server.backup.params.RestoreParams;
import com.android.server.backup.transport.TransportClient;
import java.util.function.BiFunction;

public class ActiveRestoreSession extends IRestoreSession.Stub {
    private static final String TAG = "RestoreSession";
    private final BackupManagerService mBackupManagerService;
    boolean mEnded = false;
    private final String mPackageName;
    public RestoreSet[] mRestoreSets = null;
    boolean mTimedOut = false;
    private final TransportManager mTransportManager;
    private final String mTransportName;

    public class EndRestoreRunnable implements Runnable {
        BackupManagerService mBackupManager;
        ActiveRestoreSession mSession;

        public EndRestoreRunnable(BackupManagerService manager, ActiveRestoreSession session) {
            this.mBackupManager = manager;
            this.mSession = session;
        }

        public void run() {
            synchronized (this.mSession) {
                this.mSession.mEnded = true;
            }
            this.mBackupManager.clearRestoreSession(this.mSession);
        }
    }

    public ActiveRestoreSession(BackupManagerService backupManagerService, String packageName, String transportName) {
        this.mBackupManagerService = backupManagerService;
        this.mPackageName = packageName;
        this.mTransportManager = backupManagerService.getTransportManager();
        this.mTransportName = transportName;
    }

    public void markTimedOut() {
        this.mTimedOut = true;
    }

    public synchronized int getAvailableRestoreSets(IRestoreObserver observer, IBackupManagerMonitor monitor) {
        synchronized (this) {
            this.mBackupManagerService.getContext().enforceCallingOrSelfPermission("android.permission.BACKUP", "getAvailableRestoreSets");
            if (observer == null) {
                throw new IllegalArgumentException("Observer must not be null");
            } else if (this.mEnded) {
                throw new IllegalStateException("Restore session already ended");
            } else if (this.mTimedOut) {
                Slog.i(TAG, "Session already timed out");
                return -1;
            } else {
                long oldId = Binder.clearCallingIdentity();
                try {
                    TransportClient transportClient = this.mTransportManager.getTransportClient(this.mTransportName, "RestoreSession.getAvailableRestoreSets()");
                    if (transportClient == null) {
                        Slog.w(TAG, "Null transport client getting restore sets");
                        Binder.restoreCallingIdentity(oldId);
                        return -1;
                    }
                    this.mBackupManagerService.getBackupHandler().removeMessages(8);
                    PowerManager.WakeLock wakelock = this.mBackupManagerService.getWakelock();
                    wakelock.acquire();
                    OnTaskFinishedListener listener = new OnTaskFinishedListener(transportClient, wakelock) {
                        private final /* synthetic */ TransportClient f$1;
                        private final /* synthetic */ PowerManager.WakeLock f$2;

                        {
                            this.f$1 = r2;
                            this.f$2 = r3;
                        }

                        public final void onFinished(String str) {
                            ActiveRestoreSession.lambda$getAvailableRestoreSets$0(TransportManager.this, this.f$1, this.f$2, str);
                        }
                    };
                    Handler backupHandler = this.mBackupManagerService.getBackupHandler();
                    RestoreGetSetsParams restoreGetSetsParams = r1;
                    RestoreGetSetsParams restoreGetSetsParams2 = new RestoreGetSetsParams(transportClient, this, observer, monitor, listener);
                    this.mBackupManagerService.getBackupHandler().sendMessage(backupHandler.obtainMessage(6, restoreGetSetsParams));
                    Binder.restoreCallingIdentity(oldId);
                    return 0;
                } catch (Exception e) {
                    try {
                        Slog.e(TAG, "Error in getAvailableRestoreSets", e);
                        return -1;
                    } finally {
                        Binder.restoreCallingIdentity(oldId);
                    }
                }
            }
        }
    }

    static /* synthetic */ void lambda$getAvailableRestoreSets$0(TransportManager transportManager, TransportClient transportClient, PowerManager.WakeLock wakelock, String caller) {
        transportManager.disposeOfTransportClient(transportClient, caller);
        wakelock.release();
    }

    public synchronized int restoreAll(long token, IRestoreObserver observer, IBackupManagerMonitor monitor) {
        this.mBackupManagerService.getContext().enforceCallingOrSelfPermission("android.permission.BACKUP", "performRestore");
        Slog.d(TAG, "restoreAll token=" + Long.toHexString(token) + " observer=" + observer);
        if (this.mEnded) {
            throw new IllegalStateException("Restore session already ended");
        } else if (this.mTimedOut) {
            Slog.i(TAG, "Session already timed out");
            return -1;
        } else if (this.mRestoreSets == null) {
            Slog.e(TAG, "Ignoring restoreAll() with no restore set");
            return -1;
        } else if (this.mPackageName != null) {
            Slog.e(TAG, "Ignoring restoreAll() on single-package session");
            return -1;
        } else if (!this.mTransportManager.isTransportRegistered(this.mTransportName)) {
            Slog.e(TAG, "Transport " + this.mTransportName + " not registered");
            return -1;
        } else {
            synchronized (this.mBackupManagerService.getQueueLock()) {
                int i = 0;
                while (i < this.mRestoreSets.length) {
                    if (token == this.mRestoreSets[i].token) {
                        long oldId = Binder.clearCallingIdentity();
                        try {
                            int sendRestoreToHandlerLocked = sendRestoreToHandlerLocked(new BiFunction(observer, monitor, token) {
                                private final /* synthetic */ IRestoreObserver f$0;
                                private final /* synthetic */ IBackupManagerMonitor f$1;
                                private final /* synthetic */ long f$2;

                                {
                                    this.f$0 = r1;
                                    this.f$1 = r2;
                                    this.f$2 = r3;
                                }

                                public final Object apply(Object obj, Object obj2) {
                                    return RestoreParams.createForRestoreAll((TransportClient) obj, this.f$0, this.f$1, this.f$2, (OnTaskFinishedListener) obj2);
                                }
                            }, "RestoreSession.restoreAll()");
                            return sendRestoreToHandlerLocked;
                        } finally {
                            Binder.restoreCallingIdentity(oldId);
                        }
                    } else {
                        i++;
                    }
                }
                Slog.w(TAG, "Restore token " + Long.toHexString(token) + " not found");
                return -1;
            }
        }
    }

    public synchronized int restoreSome(long token, IRestoreObserver observer, IBackupManagerMonitor monitor, String[] packages) {
        String[] strArr = packages;
        synchronized (this) {
            this.mBackupManagerService.getContext().enforceCallingOrSelfPermission("android.permission.BACKUP", "performRestore");
            StringBuilder b = new StringBuilder(128);
            b.append("restoreSome token=");
            b.append(Long.toHexString(token));
            b.append(" observer=");
            b.append(observer.toString());
            b.append(" monitor=");
            if (monitor == null) {
                b.append("null");
            } else {
                b.append(monitor.toString());
            }
            b.append(" packages=");
            int i = 0;
            if (strArr == null) {
                b.append("null");
            } else {
                b.append('{');
                boolean first = true;
                for (String s : strArr) {
                    if (!first) {
                        b.append(", ");
                    } else {
                        first = false;
                    }
                    b.append(s);
                }
                b.append('}');
            }
            Slog.d(TAG, b.toString());
            if (this.mEnded) {
                throw new IllegalStateException("Restore session already ended");
            } else if (this.mTimedOut) {
                Slog.i(TAG, "Session already timed out");
                return -1;
            } else if (this.mRestoreSets == null) {
                Slog.e(TAG, "Ignoring restoreAll() with no restore set");
                return -1;
            } else if (this.mPackageName != null) {
                Slog.e(TAG, "Ignoring restoreAll() on single-package session");
                return -1;
            } else if (!this.mTransportManager.isTransportRegistered(this.mTransportName)) {
                Slog.e(TAG, "Transport " + this.mTransportName + " not registered");
                return -1;
            } else {
                synchronized (this.mBackupManagerService.getQueueLock()) {
                    while (true) {
                        int i2 = i;
                        if (i2 >= this.mRestoreSets.length) {
                            Slog.w(TAG, "Restore token " + Long.toHexString(token) + " not found");
                            return -1;
                        } else if (token == this.mRestoreSets[i2].token) {
                            long oldId = Binder.clearCallingIdentity();
                            try {
                                $$Lambda$ActiveRestoreSession$amDGbcwA180LGcZKUosvhspMk2E r2 = new BiFunction(observer, monitor, token, strArr) {
                                    private final /* synthetic */ IRestoreObserver f$0;
                                    private final /* synthetic */ IBackupManagerMonitor f$1;
                                    private final /* synthetic */ long f$2;
                                    private final /* synthetic */ String[] f$3;

                                    {
                                        this.f$0 = r1;
                                        this.f$1 = r2;
                                        this.f$2 = r3;
                                        this.f$3 = r5;
                                    }

                                    public final Object apply(Object obj, Object obj2) {
                                        return ActiveRestoreSession.lambda$restoreSome$2(this.f$0, this.f$1, this.f$2, this.f$3, (TransportClient) obj, (OnTaskFinishedListener) obj2);
                                    }
                                };
                                int sendRestoreToHandlerLocked = sendRestoreToHandlerLocked(r2, "RestoreSession.restoreSome(" + strArr.length + " packages)");
                                return sendRestoreToHandlerLocked;
                            } finally {
                                Binder.restoreCallingIdentity(oldId);
                            }
                        } else {
                            i = i2 + 1;
                        }
                    }
                }
            }
        }
    }

    static /* synthetic */ RestoreParams lambda$restoreSome$2(IRestoreObserver observer, IBackupManagerMonitor monitor, long token, String[] packages, TransportClient transportClient, OnTaskFinishedListener listener) {
        return RestoreParams.createForRestoreSome(transportClient, observer, monitor, token, packages, packages.length > 1, listener);
    }

    public synchronized int restorePackage(String packageName, IRestoreObserver observer, IBackupManagerMonitor monitor) {
        String str = packageName;
        synchronized (this) {
            StringBuilder sb = new StringBuilder();
            sb.append("restorePackage pkg=");
            sb.append(str);
            sb.append(" obs=");
            IRestoreObserver iRestoreObserver = observer;
            sb.append(iRestoreObserver);
            sb.append("monitor=");
            IBackupManagerMonitor iBackupManagerMonitor = monitor;
            sb.append(iBackupManagerMonitor);
            Slog.v(TAG, sb.toString());
            if (this.mEnded) {
                throw new IllegalStateException("Restore session already ended");
            } else if (this.mTimedOut) {
                Slog.i(TAG, "Session already timed out");
                return -1;
            } else if (this.mPackageName == null || this.mPackageName.equals(str)) {
                try {
                    PackageInfo app = this.mBackupManagerService.getPackageManager().getPackageInfo(str, 0);
                    if (this.mBackupManagerService.getContext().checkPermission("android.permission.BACKUP", Binder.getCallingPid(), Binder.getCallingUid()) == -1) {
                        if (app.applicationInfo.uid != Binder.getCallingUid()) {
                            Slog.w(TAG, "restorePackage: bad packageName=" + str + " or calling uid=" + Binder.getCallingUid());
                            throw new SecurityException("No permission to restore other packages");
                        }
                    }
                    if (!this.mTransportManager.isTransportRegistered(this.mTransportName)) {
                        Slog.e(TAG, "Transport " + this.mTransportName + " not registered");
                        return -1;
                    }
                    long oldId = Binder.clearCallingIdentity();
                    try {
                        long token = this.mBackupManagerService.getAvailableRestoreToken(str);
                        Slog.v(TAG, "restorePackage pkg=" + str + " token=" + Long.toHexString(token));
                        if (token == 0) {
                            Slog.w(TAG, "No data available for this package; not restoring");
                            return -1;
                        }
                        long j = token;
                        $$Lambda$ActiveRestoreSession$tb1mCMujBEuhHsxQ6tX_mYJVCII r5 = new BiFunction(iRestoreObserver, iBackupManagerMonitor, token, app) {
                            private final /* synthetic */ IRestoreObserver f$0;
                            private final /* synthetic */ IBackupManagerMonitor f$1;
                            private final /* synthetic */ long f$2;
                            private final /* synthetic */ PackageInfo f$3;

                            {
                                this.f$0 = r1;
                                this.f$1 = r2;
                                this.f$2 = r3;
                                this.f$3 = r5;
                            }

                            public final Object apply(Object obj, Object obj2) {
                                return RestoreParams.createForSinglePackage((TransportClient) obj, this.f$0, this.f$1, this.f$2, this.f$3, (OnTaskFinishedListener) obj2);
                            }
                        };
                        int sendRestoreToHandlerLocked = sendRestoreToHandlerLocked(r5, "RestoreSession.restorePackage(" + str + ")");
                        Binder.restoreCallingIdentity(oldId);
                        return sendRestoreToHandlerLocked;
                    } finally {
                        Binder.restoreCallingIdentity(oldId);
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    Slog.w(TAG, "Asked to restore nonexistent pkg " + str);
                    return -1;
                }
            } else {
                Slog.e(TAG, "Ignoring attempt to restore pkg=" + str + " on session for package " + this.mPackageName);
                return -1;
            }
        }
    }

    public void setRestoreSets(RestoreSet[] restoreSets) {
        this.mRestoreSets = restoreSets;
    }

    private int sendRestoreToHandlerLocked(BiFunction<TransportClient, OnTaskFinishedListener, RestoreParams> restoreParamsBuilder, String callerLogString) {
        TransportClient transportClient = this.mTransportManager.getTransportClient(this.mTransportName, callerLogString);
        if (transportClient == null) {
            Slog.e(TAG, "Transport " + this.mTransportName + " got unregistered");
            return -1;
        }
        Handler backupHandler = this.mBackupManagerService.getBackupHandler();
        backupHandler.removeMessages(8);
        PowerManager.WakeLock wakelock = this.mBackupManagerService.getWakelock();
        wakelock.acquire();
        OnTaskFinishedListener listener = new OnTaskFinishedListener(transportClient, wakelock) {
            private final /* synthetic */ TransportClient f$1;
            private final /* synthetic */ PowerManager.WakeLock f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void onFinished(String str) {
                ActiveRestoreSession.lambda$sendRestoreToHandlerLocked$4(TransportManager.this, this.f$1, this.f$2, str);
            }
        };
        Message msg = backupHandler.obtainMessage(3);
        msg.obj = restoreParamsBuilder.apply(transportClient, listener);
        backupHandler.sendMessage(msg);
        return 0;
    }

    static /* synthetic */ void lambda$sendRestoreToHandlerLocked$4(TransportManager transportManager, TransportClient transportClient, PowerManager.WakeLock wakelock, String caller) {
        transportManager.disposeOfTransportClient(transportClient, caller);
        wakelock.release();
    }

    public synchronized void endRestoreSession() {
        Slog.d(TAG, "endRestoreSession");
        if (this.mTimedOut) {
            Slog.i(TAG, "Session already timed out");
        } else if (!this.mEnded) {
            this.mBackupManagerService.getBackupHandler().post(new EndRestoreRunnable(this.mBackupManagerService, this));
        } else {
            throw new IllegalStateException("Restore session already ended");
        }
    }
}

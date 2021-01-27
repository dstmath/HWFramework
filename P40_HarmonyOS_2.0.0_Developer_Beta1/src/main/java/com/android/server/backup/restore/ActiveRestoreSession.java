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
import com.android.server.backup.TransportManager;
import com.android.server.backup.UserBackupManagerService;
import com.android.server.backup.internal.OnTaskFinishedListener;
import com.android.server.backup.params.RestoreGetSetsParams;
import com.android.server.backup.params.RestoreParams;
import com.android.server.backup.transport.TransportClient;
import java.util.function.BiFunction;

public class ActiveRestoreSession extends IRestoreSession.Stub {
    private static final String TAG = "RestoreSession";
    private final UserBackupManagerService mBackupManagerService;
    boolean mEnded = false;
    private final String mPackageName;
    public RestoreSet[] mRestoreSets = null;
    boolean mTimedOut = false;
    private final TransportManager mTransportManager;
    private final String mTransportName;
    private final int mUserId;

    public ActiveRestoreSession(UserBackupManagerService backupManagerService, String packageName, String transportName) {
        this.mBackupManagerService = backupManagerService;
        this.mPackageName = packageName;
        this.mTransportManager = backupManagerService.getTransportManager();
        this.mTransportName = transportName;
        this.mUserId = backupManagerService.getUserId();
    }

    public void markTimedOut() {
        this.mTimedOut = true;
    }

    public synchronized int getAvailableRestoreSets(IRestoreObserver observer, IBackupManagerMonitor monitor) {
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
                    return -1;
                }
                this.mBackupManagerService.getBackupHandler().removeMessages(8);
                PowerManager.WakeLock wakelock = this.mBackupManagerService.getWakelock();
                wakelock.acquire();
                this.mBackupManagerService.getBackupHandler().sendMessage(this.mBackupManagerService.getBackupHandler().obtainMessage(6, new RestoreGetSetsParams(transportClient, this, observer, monitor, new OnTaskFinishedListener(transportClient, wakelock) {
                    /* class com.android.server.backup.restore.$$Lambda$ActiveRestoreSession$0wzV_GqtA0thM1WxLthNBKD3Ygw */
                    private final /* synthetic */ TransportClient f$1;
                    private final /* synthetic */ PowerManager.WakeLock f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    @Override // com.android.server.backup.internal.OnTaskFinishedListener
                    public final void onFinished(String str) {
                        ActiveRestoreSession.lambda$getAvailableRestoreSets$0(TransportManager.this, this.f$1, this.f$2, str);
                    }
                })));
                Binder.restoreCallingIdentity(oldId);
                return 0;
            } catch (Exception e) {
                Slog.e(TAG, "Error in getAvailableRestoreSets", e);
                return -1;
            } finally {
                Binder.restoreCallingIdentity(oldId);
            }
        }
    }

    static /* synthetic */ void lambda$getAvailableRestoreSets$0(TransportManager transportManager, TransportClient transportClient, PowerManager.WakeLock wakelock, String caller) {
        transportManager.disposeOfTransportClient(transportClient, caller);
        wakelock.release();
    }

    public synchronized int restoreAll(long token, IRestoreObserver observer, IBackupManagerMonitor monitor) {
        Throwable th;
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
                for (int i = 0; i < this.mRestoreSets.length; i++) {
                    try {
                        try {
                            if (token == this.mRestoreSets[i].token) {
                                long oldId = Binder.clearCallingIdentity();
                                try {
                                    return sendRestoreToHandlerLocked(new BiFunction(observer, monitor, token) {
                                        /* class com.android.server.backup.restore.$$Lambda$ActiveRestoreSession$iPMdVI7x_J8xmayWzH6Euhd5674 */
                                        private final /* synthetic */ IRestoreObserver f$0;
                                        private final /* synthetic */ IBackupManagerMonitor f$1;
                                        private final /* synthetic */ long f$2;

                                        {
                                            this.f$0 = r1;
                                            this.f$1 = r2;
                                            this.f$2 = r3;
                                        }

                                        @Override // java.util.function.BiFunction
                                        public final Object apply(Object obj, Object obj2) {
                                            return RestoreParams.createForRestoreAll((TransportClient) obj, this.f$0, this.f$1, this.f$2, (OnTaskFinishedListener) obj2);
                                        }
                                    }, "RestoreSession.restoreAll()");
                                } finally {
                                    Binder.restoreCallingIdentity(oldId);
                                }
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        throw th;
                    }
                }
                Slog.w(TAG, "Restore token " + Long.toHexString(token) + " not found");
                return -1;
            }
        }
    }

    public synchronized int restorePackages(long token, IRestoreObserver observer, String[] packages, IBackupManagerMonitor monitor) {
        Throwable th;
        this.mBackupManagerService.getContext().enforceCallingOrSelfPermission("android.permission.BACKUP", "performRestore");
        StringBuilder b = new StringBuilder(128);
        b.append("restorePackages token=");
        b.append(Long.toHexString(token));
        b.append(" observer=");
        if (observer == null) {
            b.append("null");
        } else {
            b.append(observer.toString());
        }
        b.append(" monitor=");
        if (monitor == null) {
            b.append("null");
        } else {
            b.append(monitor.toString());
        }
        b.append(" packages=");
        if (packages == null) {
            b.append("null");
        } else {
            b.append('{');
            boolean first = true;
            for (String s : packages) {
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
                for (int i = 0; i < this.mRestoreSets.length; i++) {
                    try {
                        try {
                            if (token == this.mRestoreSets[i].token) {
                                long oldId = Binder.clearCallingIdentity();
                                try {
                                    return sendRestoreToHandlerLocked(new BiFunction(observer, monitor, token, packages) {
                                        /* class com.android.server.backup.restore.$$Lambda$ActiveRestoreSession$gXVTdFUn9LSjuKEXaGOyKBxki6Q */
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

                                        @Override // java.util.function.BiFunction
                                        public final Object apply(Object obj, Object obj2) {
                                            return ActiveRestoreSession.lambda$restorePackages$2(this.f$0, this.f$1, this.f$2, this.f$3, (TransportClient) obj, (OnTaskFinishedListener) obj2);
                                        }
                                    }, "RestoreSession.restorePackages(" + packages.length + " packages)");
                                } finally {
                                    Binder.restoreCallingIdentity(oldId);
                                }
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        throw th;
                    }
                }
                Slog.w(TAG, "Restore token " + Long.toHexString(token) + " not found");
                return -1;
            }
        }
    }

    static /* synthetic */ RestoreParams lambda$restorePackages$2(IRestoreObserver observer, IBackupManagerMonitor monitor, long token, String[] packages, TransportClient transportClient, OnTaskFinishedListener listener) {
        return RestoreParams.createForRestorePackages(transportClient, observer, monitor, token, packages, packages.length > 1, listener);
    }

    public synchronized int restorePackage(String packageName, IRestoreObserver observer, IBackupManagerMonitor monitor) {
        Slog.v(TAG, "restorePackage pkg=" + packageName + " obs=" + observer + "monitor=" + monitor);
        if (this.mEnded) {
            throw new IllegalStateException("Restore session already ended");
        } else if (this.mTimedOut) {
            Slog.i(TAG, "Session already timed out");
            return -1;
        } else if (this.mPackageName == null || this.mPackageName.equals(packageName)) {
            try {
                PackageInfo app = this.mBackupManagerService.getPackageManager().getPackageInfoAsUser(packageName, 0, this.mUserId);
                if (this.mBackupManagerService.getContext().checkPermission("android.permission.BACKUP", Binder.getCallingPid(), Binder.getCallingUid()) == -1 && app.applicationInfo.uid != Binder.getCallingUid()) {
                    Slog.w(TAG, "restorePackage: bad packageName=" + packageName + " or calling uid=" + Binder.getCallingUid());
                    throw new SecurityException("No permission to restore other packages");
                } else if (!this.mTransportManager.isTransportRegistered(this.mTransportName)) {
                    Slog.e(TAG, "Transport " + this.mTransportName + " not registered");
                    return -1;
                } else {
                    long oldId = Binder.clearCallingIdentity();
                    try {
                        long token = this.mBackupManagerService.getAvailableRestoreToken(packageName);
                        Slog.v(TAG, "restorePackage pkg=" + packageName + " token=" + Long.toHexString(token));
                        if (token == 0) {
                            Slog.w(TAG, "No data available for this package; not restoring");
                            return -1;
                        }
                        $$Lambda$ActiveRestoreSession$tb1mCMujBEuhHsxQ6tX_mYJVCII r0 = new BiFunction(observer, monitor, token, app) {
                            /* class com.android.server.backup.restore.$$Lambda$ActiveRestoreSession$tb1mCMujBEuhHsxQ6tX_mYJVCII */
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

                            @Override // java.util.function.BiFunction
                            public final Object apply(Object obj, Object obj2) {
                                return RestoreParams.createForSinglePackage((TransportClient) obj, this.f$0, this.f$1, this.f$2, this.f$3, (OnTaskFinishedListener) obj2);
                            }
                        };
                        int sendRestoreToHandlerLocked = sendRestoreToHandlerLocked(r0, "RestoreSession.restorePackage(" + packageName + ")");
                        Binder.restoreCallingIdentity(oldId);
                        return sendRestoreToHandlerLocked;
                    } finally {
                        Binder.restoreCallingIdentity(oldId);
                    }
                }
            } catch (PackageManager.NameNotFoundException e) {
                Slog.w(TAG, "Asked to restore nonexistent pkg " + packageName);
                return -1;
            }
        } else {
            Slog.e(TAG, "Ignoring attempt to restore pkg=" + packageName + " on session for package " + this.mPackageName);
            return -1;
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
            /* class com.android.server.backup.restore.$$Lambda$ActiveRestoreSession$0QlkHke0fYNRb0nGuyNs6WmyPDM */
            private final /* synthetic */ TransportClient f$1;
            private final /* synthetic */ PowerManager.WakeLock f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // com.android.server.backup.internal.OnTaskFinishedListener
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

    public class EndRestoreRunnable implements Runnable {
        UserBackupManagerService mBackupManager;
        ActiveRestoreSession mSession;

        public EndRestoreRunnable(UserBackupManagerService manager, ActiveRestoreSession session) {
            this.mBackupManager = manager;
            this.mSession = session;
        }

        @Override // java.lang.Runnable
        public void run() {
            synchronized (this.mSession) {
                this.mSession.mEnded = true;
            }
            this.mBackupManager.clearRestoreSession(this.mSession);
        }
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

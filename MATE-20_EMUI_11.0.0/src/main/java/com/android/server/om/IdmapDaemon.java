package com.android.server.om;

import android.os.IBinder;
import android.os.IIdmap2;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.util.Slog;
import com.android.server.FgThread;
import com.android.server.job.controllers.JobStatus;
import com.android.server.om.IdmapDaemon;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

class IdmapDaemon {
    private static final String IDMAP_DAEMON = "idmap2d";
    private static final Object IDMAP_TOKEN = new Object();
    private static final int SERVICE_CONNECT_TIMEOUT_MS = 5000;
    private static final int SERVICE_TIMEOUT_MS = 10000;
    private static IdmapDaemon sInstance;
    private final AtomicInteger mOpenedCount = new AtomicInteger();
    private volatile IIdmap2 mService;

    IdmapDaemon() {
    }

    /* access modifiers changed from: private */
    public class Connection implements AutoCloseable {
        private boolean mOpened;

        private Connection() {
            this.mOpened = true;
            synchronized (IdmapDaemon.IDMAP_TOKEN) {
                IdmapDaemon.this.mOpenedCount.incrementAndGet();
            }
        }

        @Override // java.lang.AutoCloseable
        public void close() {
            synchronized (IdmapDaemon.IDMAP_TOKEN) {
                if (this.mOpened) {
                    this.mOpened = false;
                    if (IdmapDaemon.this.mOpenedCount.decrementAndGet() == 0) {
                        FgThread.getHandler().postDelayed(new Runnable() {
                            /* class com.android.server.om.$$Lambda$IdmapDaemon$Connection$4Un0RSv1BPv15mvu8B8zXARcpk */

                            @Override // java.lang.Runnable
                            public final void run() {
                                IdmapDaemon.Connection.this.lambda$close$0$IdmapDaemon$Connection();
                            }
                        }, IdmapDaemon.IDMAP_TOKEN, JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY);
                    }
                }
            }
        }

        public /* synthetic */ void lambda$close$0$IdmapDaemon$Connection() {
            synchronized (IdmapDaemon.IDMAP_TOKEN) {
                if (IdmapDaemon.this.mService != null) {
                    if (IdmapDaemon.this.mOpenedCount.get() == 0) {
                        IdmapDaemon.stopIdmapService();
                        IdmapDaemon.this.mService = null;
                    }
                }
            }
        }
    }

    static IdmapDaemon getInstance() {
        if (sInstance == null) {
            sInstance = new IdmapDaemon();
        }
        return sInstance;
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0019, code lost:
        if (r0 != null) goto L_0x001b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x001b, code lost:
        $closeResource(r1, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x001e, code lost:
        throw r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0018, code lost:
        r2 = move-exception;
     */
    public String createIdmap(String targetPath, String overlayPath, int policies, boolean enforce, int userId) throws Exception {
        Connection connection = connect();
        String createIdmap = this.mService.createIdmap(targetPath, overlayPath, policies, enforce, userId);
        if (connection != null) {
            $closeResource(null, connection);
        }
        return createIdmap;
    }

    private static /* synthetic */ void $closeResource(Throwable x0, AutoCloseable x1) {
        if (x0 != null) {
            try {
                x1.close();
            } catch (Throwable th) {
                x0.addSuppressed(th);
            }
        } else {
            x1.close();
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0014, code lost:
        if (r0 != null) goto L_0x0016;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0016, code lost:
        $closeResource(r1, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0019, code lost:
        throw r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0013, code lost:
        r2 = move-exception;
     */
    public boolean removeIdmap(String overlayPath, int userId) throws Exception {
        Connection connection = connect();
        boolean removeIdmap = this.mService.removeIdmap(overlayPath, userId);
        if (connection != null) {
            $closeResource(null, connection);
        }
        return removeIdmap;
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0014, code lost:
        if (r0 != null) goto L_0x0016;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0016, code lost:
        $closeResource(r1, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0019, code lost:
        throw r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0013, code lost:
        r2 = move-exception;
     */
    public boolean verifyIdmap(String overlayPath, int policies, boolean enforce, int userId) throws Exception {
        Connection connection = connect();
        boolean verifyIdmap = this.mService.verifyIdmap(overlayPath, policies, enforce, userId);
        if (connection != null) {
            $closeResource(null, connection);
        }
        return verifyIdmap;
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0014, code lost:
        if (r0 != null) goto L_0x0016;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0016, code lost:
        $closeResource(r1, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0019, code lost:
        throw r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0013, code lost:
        r2 = move-exception;
     */
    public String getIdmapPath(String overlayPath, int userId) throws Exception {
        Connection connection = connect();
        String idmapPath = this.mService.getIdmapPath(overlayPath, userId);
        if (connection != null) {
            $closeResource(null, connection);
        }
        return idmapPath;
    }

    private static void startIdmapService() {
        SystemProperties.set("ctl.start", IDMAP_DAEMON);
    }

    /* access modifiers changed from: private */
    public static void stopIdmapService() {
        SystemProperties.set("ctl.stop", IDMAP_DAEMON);
    }

    private Connection connect() throws Exception {
        synchronized (IDMAP_TOKEN) {
            FgThread.getHandler().removeCallbacksAndMessages(IDMAP_TOKEN);
            if (this.mService != null) {
                return new Connection();
            }
            startIdmapService();
            FutureTask<IBinder> bindIdmap = new FutureTask<>($$Lambda$IdmapDaemon$u_1qfM2VGzol3UUX0R4mwNZs9gY.INSTANCE);
            try {
                FgThread.getHandler().postAtFrontOfQueue(bindIdmap);
                IBinder binder = bindIdmap.get(5000, TimeUnit.MILLISECONDS);
                try {
                    binder.linkToDeath($$Lambda$IdmapDaemon$hZvlb8B5bMAnD3h9mHLjOQXKSTI.INSTANCE, 0);
                    this.mService = IIdmap2.Stub.asInterface(binder);
                    return new Connection();
                } catch (RemoteException rethrow) {
                    Slog.e("OverlayManager", "service 'idmap' failed to be bound");
                    throw rethrow;
                }
            } catch (Exception rethrow2) {
                Slog.e("OverlayManager", "service 'idmap' not found;");
                throw rethrow2;
            }
        }
    }

    static /* synthetic */ IBinder lambda$connect$0() throws Exception {
        while (true) {
            try {
                IBinder binder = ServiceManager.getService("idmap");
                if (binder != null) {
                    return binder;
                }
                Thread.sleep(100);
            } catch (Exception e) {
                Slog.e("OverlayManager", "service 'idmap' not retrieved; " + e.getMessage());
            }
        }
    }
}

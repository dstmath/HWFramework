package com.android.server.pm;

import android.app.IInstantAppResolver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.InstantAppResolveInfo;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.IRemoteCallback;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Slog;
import android.util.TimedRemoteCaller;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.os.BackgroundThread;
import com.android.server.pm.InstantAppResolverConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeoutException;

final class InstantAppResolverConnection implements IBinder.DeathRecipient {
    private static final long BIND_SERVICE_TIMEOUT_MS = (Build.IS_ENG ? 500 : 300);
    /* access modifiers changed from: private */
    public static final long CALL_SERVICE_TIMEOUT_MS = (Build.IS_ENG ? 200 : 100);
    /* access modifiers changed from: private */
    public static final boolean DEBUG_INSTANT = Build.IS_DEBUGGABLE;
    private static final int STATE_BINDING = 1;
    private static final int STATE_IDLE = 0;
    private static final int STATE_PENDING = 2;
    private static final String TAG = "PackageManager";
    private final Handler mBgHandler;
    /* access modifiers changed from: private */
    @GuardedBy("mLock")
    public int mBindState = 0;
    private final Context mContext;
    private final GetInstantAppResolveInfoCaller mGetInstantAppResolveInfoCaller = new GetInstantAppResolveInfoCaller();
    private final Intent mIntent;
    /* access modifiers changed from: private */
    public final Object mLock = new Object();
    /* access modifiers changed from: private */
    @GuardedBy("mLock")
    public IInstantAppResolver mRemoteInstance;
    private final ServiceConnection mServiceConnection = new MyServiceConnection();

    public static class ConnectionException extends Exception {
        public static final int FAILURE_BIND = 1;
        public static final int FAILURE_CALL = 2;
        public static final int FAILURE_INTERRUPTED = 3;
        public final int failure;

        public ConnectionException(int _failure) {
            this.failure = _failure;
        }
    }

    private static final class GetInstantAppResolveInfoCaller extends TimedRemoteCaller<List<InstantAppResolveInfo>> {
        private final IRemoteCallback mCallback = new IRemoteCallback.Stub() {
            public void sendResult(Bundle data) throws RemoteException {
                GetInstantAppResolveInfoCaller.this.onRemoteMethodResult(data.getParcelableArrayList("android.app.extra.RESOLVE_INFO"), data.getInt("android.app.extra.SEQUENCE", -1));
            }
        };

        public GetInstantAppResolveInfoCaller() {
            super(InstantAppResolverConnection.CALL_SERVICE_TIMEOUT_MS);
        }

        public List<InstantAppResolveInfo> getInstantAppResolveInfoList(IInstantAppResolver target, Intent sanitizedIntent, int[] hashPrefix, String token) throws RemoteException, TimeoutException {
            int sequence = onBeforeRemoteCall();
            target.getInstantAppResolveInfoList(sanitizedIntent, hashPrefix, token, sequence, this.mCallback);
            return (List) getResultTimed(sequence);
        }
    }

    private final class MyServiceConnection implements ServiceConnection {
        private MyServiceConnection() {
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            if (InstantAppResolverConnection.DEBUG_INSTANT) {
                Slog.d(InstantAppResolverConnection.TAG, "Connected to instant app resolver");
            }
            synchronized (InstantAppResolverConnection.this.mLock) {
                IInstantAppResolver unused = InstantAppResolverConnection.this.mRemoteInstance = IInstantAppResolver.Stub.asInterface(service);
                if (InstantAppResolverConnection.this.mBindState == 2) {
                    int unused2 = InstantAppResolverConnection.this.mBindState = 0;
                }
                try {
                    service.linkToDeath(InstantAppResolverConnection.this, 0);
                } catch (RemoteException e) {
                    InstantAppResolverConnection.this.handleBinderDiedLocked();
                }
                InstantAppResolverConnection.this.mLock.notifyAll();
            }
        }

        public void onServiceDisconnected(ComponentName name) {
            if (InstantAppResolverConnection.DEBUG_INSTANT) {
                Slog.d(InstantAppResolverConnection.TAG, "Disconnected from instant app resolver");
            }
            synchronized (InstantAppResolverConnection.this.mLock) {
                InstantAppResolverConnection.this.handleBinderDiedLocked();
            }
        }
    }

    public static abstract class PhaseTwoCallback {
        /* access modifiers changed from: package-private */
        public abstract void onPhaseTwoResolved(List<InstantAppResolveInfo> list, long j);
    }

    public InstantAppResolverConnection(Context context, ComponentName componentName, String action) {
        this.mContext = context;
        this.mIntent = new Intent(action).setComponent(componentName);
        this.mBgHandler = BackgroundThread.getHandler();
    }

    public final List<InstantAppResolveInfo> getInstantAppResolveInfoList(Intent sanitizedIntent, int[] hashPrefix, String token) throws ConnectionException {
        throwIfCalledOnMainThread();
        try {
            List<InstantAppResolveInfo> instantAppResolveInfoList = this.mGetInstantAppResolveInfoCaller.getInstantAppResolveInfoList(getRemoteInstanceLazy(token), sanitizedIntent, hashPrefix, token);
            synchronized (this.mLock) {
                this.mLock.notifyAll();
            }
            return instantAppResolveInfoList;
        } catch (TimeoutException e) {
            throw new ConnectionException(1);
        } catch (InterruptedException e2) {
            throw new ConnectionException(3);
        } catch (TimeoutException e3) {
            throw new ConnectionException(2);
        } catch (RemoteException e4) {
            synchronized (this.mLock) {
                this.mLock.notifyAll();
                return null;
            }
        } catch (Throwable e5) {
            synchronized (this.mLock) {
                this.mLock.notifyAll();
                throw e5;
            }
        }
    }

    public final void getInstantAppIntentFilterList(Intent sanitizedIntent, int[] hashPrefix, String token, PhaseTwoCallback callback, Handler callbackHandler, long startTime) throws ConnectionException {
        final Handler handler = callbackHandler;
        final PhaseTwoCallback phaseTwoCallback = callback;
        final long j = startTime;
        AnonymousClass1 r0 = new IRemoteCallback.Stub() {
            public void sendResult(Bundle data) throws RemoteException {
                handler.post(new Runnable(data.getParcelableArrayList("android.app.extra.RESOLVE_INFO"), j) {
                    private final /* synthetic */ ArrayList f$1;
                    private final /* synthetic */ long f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    public final void run() {
                        InstantAppResolverConnection.PhaseTwoCallback.this.onPhaseTwoResolved(this.f$1, this.f$2);
                    }
                });
            }
        };
        try {
            getRemoteInstanceLazy(token).getInstantAppIntentFilterList(sanitizedIntent, hashPrefix, token, r0);
        } catch (TimeoutException e) {
            throw new ConnectionException(1);
        } catch (InterruptedException e2) {
            throw new ConnectionException(3);
        } catch (RemoteException e3) {
        }
    }

    private IInstantAppResolver getRemoteInstanceLazy(String token) throws ConnectionException, TimeoutException, InterruptedException {
        long binderToken = Binder.clearCallingIdentity();
        try {
            return bind(token);
        } finally {
            Binder.restoreCallingIdentity(binderToken);
        }
    }

    @GuardedBy("mLock")
    private void waitForBindLocked(String token) throws TimeoutException, InterruptedException {
        long startMillis = SystemClock.uptimeMillis();
        while (this.mBindState != 0 && this.mRemoteInstance == null) {
            long remainingMillis = BIND_SERVICE_TIMEOUT_MS - (SystemClock.uptimeMillis() - startMillis);
            if (remainingMillis > 0) {
                this.mLock.wait(remainingMillis);
            } else {
                throw new TimeoutException("[" + token + "] Didn't bind to resolver in time!");
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:37:0x0076, code lost:
        r1 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x0079, code lost:
        if (r0 == false) goto L_0x00a7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x007d, code lost:
        if (DEBUG_INSTANT == false) goto L_0x009a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x007f, code lost:
        android.util.Slog.i(TAG, "[" + r13 + "] Previous connection never established; rebinding");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x009a, code lost:
        r12.mContext.unbindService(r12.mServiceConnection);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x00a2, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x00a3, code lost:
        r2 = r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x00a9, code lost:
        if (DEBUG_INSTANT == false) goto L_0x00c6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x00ab, code lost:
        android.util.Slog.v(TAG, "[" + r13 + "] Binding to instant app resolver");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x00c6, code lost:
        r1 = r12.mContext.bindServiceAsUser(r12.mIntent, r12.mServiceConnection, 67108865, android.os.UserHandle.SYSTEM);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x00d9, code lost:
        if (r1 == false) goto L_0x00fe;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x00db, code lost:
        r4 = r12.mLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x00dd, code lost:
        monitor-enter(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:?, code lost:
        waitForBindLocked(r13);
        r2 = r12.mRemoteInstance;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x00e4, code lost:
        monitor-exit(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x00e5, code lost:
        r7 = r12.mLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x00e7, code lost:
        monitor-enter(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x00e8, code lost:
        if (r1 == false) goto L_0x00ef;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x00ea, code lost:
        if (r2 != null) goto L_0x00ef;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:?, code lost:
        r12.mBindState = 2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x00ef, code lost:
        r12.mBindState = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x00f1, code lost:
        r12.mLock.notifyAll();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x00f6, code lost:
        monitor-exit(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:65:0x00f7, code lost:
        return r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:74:0x00fe, code lost:
        android.util.Slog.w(TAG, "[" + r13 + "] Failed to bind to: " + r12.mIntent);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:75:0x0123, code lost:
        throw new com.android.server.pm.InstantAppResolverConnection.ConnectionException(1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:77:0x0126, code lost:
        monitor-enter(r12.mLock);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:78:0x0127, code lost:
        if (r2 == false) goto L_0x012e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:81:?, code lost:
        r12.mBindState = 2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:82:0x012e, code lost:
        r12.mBindState = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:83:0x0130, code lost:
        r12.mLock.notifyAll();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:85:0x0136, code lost:
        throw r4;
     */
    private IInstantAppResolver bind(String token) throws ConnectionException, TimeoutException, InterruptedException {
        boolean doUnbind = false;
        synchronized (this.mLock) {
            if (this.mRemoteInstance != null) {
                IInstantAppResolver iInstantAppResolver = this.mRemoteInstance;
                return iInstantAppResolver;
            }
            if (this.mBindState == 2) {
                if (DEBUG_INSTANT) {
                    Slog.i(TAG, "[" + token + "] Previous bind timed out; waiting for connection");
                }
                try {
                    waitForBindLocked(token);
                    if (this.mRemoteInstance != null) {
                        IInstantAppResolver iInstantAppResolver2 = this.mRemoteInstance;
                        return iInstantAppResolver2;
                    }
                } catch (TimeoutException e) {
                    doUnbind = true;
                }
            }
            if (this.mBindState == 1) {
                if (DEBUG_INSTANT) {
                    Slog.i(TAG, "[" + token + "] Another thread is binding; waiting for connection");
                }
                waitForBindLocked(token);
                if (this.mRemoteInstance != null) {
                    IInstantAppResolver iInstantAppResolver3 = this.mRemoteInstance;
                    return iInstantAppResolver3;
                }
                throw new ConnectionException(1);
            }
            this.mBindState = 1;
        }
    }

    private void throwIfCalledOnMainThread() {
        if (Thread.currentThread() == this.mContext.getMainLooper().getThread()) {
            throw new RuntimeException("Cannot invoke on the main thread");
        }
    }

    /* access modifiers changed from: package-private */
    public void optimisticBind() {
        this.mBgHandler.post(new Runnable() {
            public final void run() {
                InstantAppResolverConnection.lambda$optimisticBind$0(InstantAppResolverConnection.this);
            }
        });
    }

    public static /* synthetic */ void lambda$optimisticBind$0(InstantAppResolverConnection instantAppResolverConnection) {
        try {
            if (instantAppResolverConnection.bind("Optimistic Bind") != null && DEBUG_INSTANT) {
                Slog.i(TAG, "Optimistic bind succeeded.");
            }
        } catch (ConnectionException | InterruptedException | TimeoutException e) {
            Slog.e(TAG, "Optimistic bind failed.", e);
        }
    }

    public void binderDied() {
        if (DEBUG_INSTANT) {
            Slog.d(TAG, "Binder to instant app resolver died");
        }
        synchronized (this.mLock) {
            handleBinderDiedLocked();
        }
        optimisticBind();
    }

    /* access modifiers changed from: private */
    @GuardedBy("mLock")
    public void handleBinderDiedLocked() {
        if (this.mRemoteInstance != null) {
            try {
                this.mRemoteInstance.asBinder().unlinkToDeath(this, 0);
            } catch (NoSuchElementException e) {
            }
        }
        this.mRemoteInstance = null;
    }
}

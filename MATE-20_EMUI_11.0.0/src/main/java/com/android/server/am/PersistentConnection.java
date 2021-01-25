package com.android.server.am;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.os.UserHandle;
import android.util.Log;
import android.util.TimeUtils;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import java.io.PrintWriter;

public abstract class PersistentConnection<T> {
    private static final boolean DEBUG = false;
    private final Runnable mBindForBackoffRunnable = new Runnable() {
        /* class com.android.server.am.$$Lambda$PersistentConnection$xTWhnA2hSnEFuF87mUe85RYnfE */

        @Override // java.lang.Runnable
        public final void run() {
            PersistentConnection.this.lambda$new$0$PersistentConnection();
        }
    };
    @GuardedBy({"mLock"})
    private boolean mBound;
    private final ComponentName mComponentName;
    private final Context mContext;
    private final Handler mHandler;
    @GuardedBy({"mLock"})
    private boolean mIsConnected;
    @GuardedBy({"mLock"})
    private long mLastConnectedTime;
    private final Object mLock = new Object();
    private long mNextBackoffMs;
    @GuardedBy({"mLock"})
    private int mNumBindingDied;
    @GuardedBy({"mLock"})
    private int mNumConnected;
    @GuardedBy({"mLock"})
    private int mNumDisconnected;
    private final double mRebindBackoffIncrease;
    private final long mRebindBackoffMs;
    private final long mRebindMaxBackoffMs;
    @GuardedBy({"mLock"})
    private boolean mRebindScheduled;
    private long mReconnectTime;
    private final long mResetBackoffDelay;
    @GuardedBy({"mLock"})
    private T mService;
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        /* class com.android.server.am.PersistentConnection.AnonymousClass1 */

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName name, IBinder service) {
            synchronized (PersistentConnection.this.mLock) {
                if (!PersistentConnection.this.mBound) {
                    String str = PersistentConnection.this.mTag;
                    Log.w(str, "Connected: " + PersistentConnection.this.mComponentName.flattenToShortString() + " u" + PersistentConnection.this.mUserId + " but not bound, ignore.");
                    return;
                }
                String str2 = PersistentConnection.this.mTag;
                Log.i(str2, "Connected: " + PersistentConnection.this.mComponentName.flattenToShortString() + " u" + PersistentConnection.this.mUserId);
                PersistentConnection.access$508(PersistentConnection.this);
                PersistentConnection.this.mIsConnected = true;
                PersistentConnection.this.mLastConnectedTime = PersistentConnection.this.injectUptimeMillis();
                PersistentConnection.this.mService = PersistentConnection.this.asInterface(service);
                PersistentConnection.this.scheduleStableCheckLocked();
            }
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName name) {
            synchronized (PersistentConnection.this.mLock) {
                String str = PersistentConnection.this.mTag;
                Log.i(str, "Disconnected: " + PersistentConnection.this.mComponentName.flattenToShortString() + " u" + PersistentConnection.this.mUserId);
                PersistentConnection.access$1008(PersistentConnection.this);
                PersistentConnection.this.cleanUpConnectionLocked();
            }
        }

        @Override // android.content.ServiceConnection
        public void onBindingDied(ComponentName name) {
            synchronized (PersistentConnection.this.mLock) {
                if (!PersistentConnection.this.mBound) {
                    String str = PersistentConnection.this.mTag;
                    Log.w(str, "Binding died: " + PersistentConnection.this.mComponentName.flattenToShortString() + " u" + PersistentConnection.this.mUserId + " but not bound, ignore.");
                    return;
                }
                String str2 = PersistentConnection.this.mTag;
                Log.w(str2, "Binding died: " + PersistentConnection.this.mComponentName.flattenToShortString() + " u" + PersistentConnection.this.mUserId);
                PersistentConnection.access$1208(PersistentConnection.this);
                PersistentConnection.this.scheduleRebindLocked();
            }
        }
    };
    @GuardedBy({"mLock"})
    private boolean mShouldBeBound;
    private final Runnable mStableCheck = new Runnable() {
        /* class com.android.server.am.$$Lambda$PersistentConnection$rkvbuN0FQdQUv1hqSwDvmwwh6Uk */

        @Override // java.lang.Runnable
        public final void run() {
            PersistentConnection.lambda$rkvbuN0FQdQUv1hqSwDvmwwh6Uk(PersistentConnection.this);
        }
    };
    private final String mTag;
    private final int mUserId;

    /* access modifiers changed from: protected */
    public abstract T asInterface(IBinder iBinder);

    /* access modifiers changed from: protected */
    public abstract int getBindFlags();

    static /* synthetic */ int access$1008(PersistentConnection x0) {
        int i = x0.mNumDisconnected;
        x0.mNumDisconnected = i + 1;
        return i;
    }

    static /* synthetic */ int access$1208(PersistentConnection x0) {
        int i = x0.mNumBindingDied;
        x0.mNumBindingDied = i + 1;
        return i;
    }

    static /* synthetic */ int access$508(PersistentConnection x0) {
        int i = x0.mNumConnected;
        x0.mNumConnected = i + 1;
        return i;
    }

    public PersistentConnection(String tag, Context context, Handler handler, int userId, ComponentName componentName, long rebindBackoffSeconds, double rebindBackoffIncrease, long rebindMaxBackoffSeconds, long resetBackoffDelay) {
        this.mTag = tag;
        this.mContext = context;
        this.mHandler = handler;
        this.mUserId = userId;
        this.mComponentName = componentName;
        this.mRebindBackoffMs = rebindBackoffSeconds * 1000;
        this.mRebindBackoffIncrease = rebindBackoffIncrease;
        this.mRebindMaxBackoffMs = rebindMaxBackoffSeconds * 1000;
        this.mResetBackoffDelay = 1000 * resetBackoffDelay;
        this.mNextBackoffMs = this.mRebindBackoffMs;
    }

    public final ComponentName getComponentName() {
        return this.mComponentName;
    }

    public final int getUserId() {
        return this.mUserId;
    }

    public final boolean isBound() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mBound;
        }
        return z;
    }

    public final boolean isRebindScheduled() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mRebindScheduled;
        }
        return z;
    }

    public final boolean isConnected() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mIsConnected;
        }
        return z;
    }

    public final T getServiceBinder() {
        T t;
        synchronized (this.mLock) {
            t = this.mService;
        }
        return t;
    }

    public final void bind() {
        synchronized (this.mLock) {
            this.mShouldBeBound = true;
            bindInnerLocked(true);
        }
    }

    public long getNextBackoffMs() {
        long j;
        synchronized (this.mLock) {
            j = this.mNextBackoffMs;
        }
        return j;
    }

    public int getNumConnected() {
        int i;
        synchronized (this.mLock) {
            i = this.mNumConnected;
        }
        return i;
    }

    public int getNumDisconnected() {
        int i;
        synchronized (this.mLock) {
            i = this.mNumDisconnected;
        }
        return i;
    }

    public int getNumBindingDied() {
        int i;
        synchronized (this.mLock) {
            i = this.mNumBindingDied;
        }
        return i;
    }

    @GuardedBy({"mLock"})
    private void resetBackoffLocked() {
        long j = this.mNextBackoffMs;
        long j2 = this.mRebindBackoffMs;
        if (j != j2) {
            this.mNextBackoffMs = j2;
            String str = this.mTag;
            Log.i(str, "Backoff reset to " + this.mNextBackoffMs);
        }
    }

    @GuardedBy({"mLock"})
    public final void bindInnerLocked(boolean resetBackoff) {
        unscheduleRebindLocked();
        if (!this.mBound) {
            this.mBound = true;
            unscheduleStableCheckLocked();
            if (resetBackoff) {
                resetBackoffLocked();
            }
            Intent service = new Intent().setComponent(this.mComponentName);
            if (!this.mContext.bindServiceAsUser(service, this.mServiceConnection, getBindFlags() | 1, this.mHandler, UserHandle.of(this.mUserId))) {
                String str = this.mTag;
                Log.e(str, "Binding: " + service.getComponent() + " u" + this.mUserId + " failed.");
            }
        }
    }

    /* access modifiers changed from: package-private */
    /* renamed from: bindForBackoff */
    public final void lambda$new$0$PersistentConnection() {
        synchronized (this.mLock) {
            if (this.mShouldBeBound) {
                bindInnerLocked(false);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"mLock"})
    private void cleanUpConnectionLocked() {
        this.mIsConnected = false;
        this.mService = null;
        unscheduleStableCheckLocked();
    }

    public final void unbind() {
        synchronized (this.mLock) {
            this.mShouldBeBound = false;
            unbindLocked();
            unscheduleStableCheckLocked();
        }
    }

    @GuardedBy({"mLock"})
    private final void unbindLocked() {
        unscheduleRebindLocked();
        if (this.mBound) {
            String str = this.mTag;
            Log.i(str, "Stopping: " + this.mComponentName.flattenToShortString() + " u" + this.mUserId);
            this.mBound = false;
            this.mContext.unbindService(this.mServiceConnection);
            cleanUpConnectionLocked();
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mLock"})
    public void unscheduleRebindLocked() {
        injectRemoveCallbacks(this.mBindForBackoffRunnable);
        this.mRebindScheduled = false;
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mLock"})
    public void scheduleRebindLocked() {
        unbindLocked();
        if (!this.mRebindScheduled) {
            String str = this.mTag;
            Log.i(str, "Scheduling to reconnect in " + this.mNextBackoffMs + " ms (uptime)");
            this.mReconnectTime = injectUptimeMillis() + this.mNextBackoffMs;
            injectPostAtTime(this.mBindForBackoffRunnable, this.mReconnectTime);
            this.mNextBackoffMs = Math.min(this.mRebindMaxBackoffMs, (long) (((double) this.mNextBackoffMs) * this.mRebindBackoffIncrease));
            this.mRebindScheduled = true;
        }
    }

    /* access modifiers changed from: private */
    public void stableConnectionCheck() {
        synchronized (this.mLock) {
            long timeRemaining = (this.mLastConnectedTime + this.mResetBackoffDelay) - injectUptimeMillis();
            if (this.mBound && this.mIsConnected && timeRemaining <= 0) {
                resetBackoffLocked();
            }
        }
    }

    @GuardedBy({"mLock"})
    private void unscheduleStableCheckLocked() {
        injectRemoveCallbacks(this.mStableCheck);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"mLock"})
    private void scheduleStableCheckLocked() {
        unscheduleStableCheckLocked();
        injectPostAtTime(this.mStableCheck, injectUptimeMillis() + this.mResetBackoffDelay);
    }

    public void dump(String prefix, PrintWriter pw) {
        synchronized (this.mLock) {
            pw.print(prefix);
            pw.print(this.mComponentName.flattenToShortString());
            pw.print(" u");
            pw.print(this.mUserId);
            pw.print(this.mBound ? " [bound]" : " [not bound]");
            pw.print(this.mIsConnected ? " [connected]" : " [not connected]");
            if (this.mRebindScheduled) {
                pw.print(" reconnect in ");
                TimeUtils.formatDuration(this.mReconnectTime - injectUptimeMillis(), pw);
            }
            pw.println();
            pw.print(prefix);
            pw.print("  Next backoff(sec): ");
            pw.print(this.mNextBackoffMs / 1000);
            pw.println();
            pw.print(prefix);
            pw.print("  Connected: ");
            pw.print(this.mNumConnected);
            pw.print("  Disconnected: ");
            pw.print(this.mNumDisconnected);
            pw.print("  Died: ");
            pw.print(this.mNumBindingDied);
            if (this.mIsConnected) {
                pw.print("  Duration: ");
                TimeUtils.formatDuration(injectUptimeMillis() - this.mLastConnectedTime, pw);
            }
            pw.println();
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void injectRemoveCallbacks(Runnable r) {
        this.mHandler.removeCallbacks(r);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void injectPostAtTime(Runnable r, long uptimeMillis) {
        this.mHandler.postAtTime(r, uptimeMillis);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public long injectUptimeMillis() {
        return SystemClock.uptimeMillis();
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public long getNextBackoffMsForTest() {
        return this.mNextBackoffMs;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public long getReconnectTimeForTest() {
        return this.mReconnectTime;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public ServiceConnection getServiceConnectionForTest() {
        return this.mServiceConnection;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public Runnable getBindForBackoffRunnableForTest() {
        return this.mBindForBackoffRunnable;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public Runnable getStableCheckRunnableForTest() {
        return this.mStableCheck;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public boolean shouldBeBoundForTest() {
        return this.mShouldBeBound;
    }
}

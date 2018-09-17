package com.android.server.am;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.os.UserHandle;
import android.util.Slog;
import android.util.TimeUtils;
import com.android.internal.annotations.GuardedBy;
import java.io.PrintWriter;

public abstract class PersistentConnection<T> {
    private static final boolean DEBUG = false;
    private final Runnable mBindForBackoffRunnable = new -$Lambda$OG32q1kCzvkvvDCMRUhlgG4vNOc(this);
    @GuardedBy("mLock")
    private boolean mBound;
    private final ComponentName mComponentName;
    private final Context mContext;
    private final Handler mHandler;
    @GuardedBy("mLock")
    private boolean mIsConnected;
    private final Object mLock = new Object();
    private long mNextBackoffMs;
    private final double mRebindBackoffIncrease;
    private final long mRebindBackoffMs;
    private final long mRebindMaxBackoffMs;
    @GuardedBy("mLock")
    private boolean mRebindScheduled;
    private long mReconnectTime;
    @GuardedBy("mLock")
    private T mService;
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            synchronized (PersistentConnection.this.mLock) {
                if (PersistentConnection.this.mBound) {
                    Slog.i(PersistentConnection.this.mTag, "Connected: " + PersistentConnection.this.mComponentName.flattenToShortString() + " u" + PersistentConnection.this.mUserId);
                    PersistentConnection.this.mIsConnected = true;
                    PersistentConnection.this.mService = PersistentConnection.this.asInterface(service);
                    return;
                }
                Slog.w(PersistentConnection.this.mTag, "Connected: " + PersistentConnection.this.mComponentName.flattenToShortString() + " u" + PersistentConnection.this.mUserId + " but not bound, ignore.");
            }
        }

        public void onServiceDisconnected(ComponentName name) {
            synchronized (PersistentConnection.this.mLock) {
                Slog.i(PersistentConnection.this.mTag, "Disconnected: " + PersistentConnection.this.mComponentName.flattenToShortString() + " u" + PersistentConnection.this.mUserId);
                PersistentConnection.this.cleanUpConnectionLocked();
            }
        }

        public void onBindingDied(ComponentName name) {
            synchronized (PersistentConnection.this.mLock) {
                if (PersistentConnection.this.mBound) {
                    Slog.w(PersistentConnection.this.mTag, "Binding died: " + PersistentConnection.this.mComponentName.flattenToShortString() + " u" + PersistentConnection.this.mUserId);
                    PersistentConnection.this.scheduleRebindLocked();
                    return;
                }
                Slog.w(PersistentConnection.this.mTag, "Binding died: " + PersistentConnection.this.mComponentName.flattenToShortString() + " u" + PersistentConnection.this.mUserId + " but not bound, ignore.");
            }
        }
    };
    @GuardedBy("mLock")
    private boolean mShouldBeBound;
    private final String mTag;
    private final int mUserId;

    protected abstract T asInterface(IBinder iBinder);

    public PersistentConnection(String tag, Context context, Handler handler, int userId, ComponentName componentName, long rebindBackoffSeconds, double rebindBackoffIncrease, long rebindMaxBackoffSeconds) {
        this.mTag = tag;
        this.mContext = context;
        this.mHandler = handler;
        this.mUserId = userId;
        this.mComponentName = componentName;
        this.mRebindBackoffMs = rebindBackoffSeconds * 1000;
        this.mRebindBackoffIncrease = rebindBackoffIncrease;
        this.mRebindMaxBackoffMs = rebindMaxBackoffSeconds * 1000;
        this.mNextBackoffMs = this.mRebindBackoffMs;
    }

    public final ComponentName getComponentName() {
        return this.mComponentName;
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

    public final void bindInnerLocked(boolean resetBackoff) {
        unscheduleRebindLocked();
        if (!this.mBound) {
            this.mBound = true;
            if (resetBackoff) {
                this.mNextBackoffMs = this.mRebindBackoffMs;
            }
            Intent service = new Intent().setComponent(this.mComponentName);
            if (!this.mContext.bindServiceAsUser(service, this.mServiceConnection, 67108865, this.mHandler, UserHandle.of(this.mUserId))) {
                Slog.e(this.mTag, "Binding: " + service.getComponent() + " u" + this.mUserId + " failed.");
            }
        }
    }

    /* renamed from: bindForBackoff */
    final void lambda$-com_android_server_am_PersistentConnection_5645() {
        synchronized (this.mLock) {
            if (this.mShouldBeBound) {
                bindInnerLocked(false);
                return;
            }
        }
    }

    private void cleanUpConnectionLocked() {
        this.mIsConnected = false;
        this.mService = null;
    }

    public final void unbind() {
        synchronized (this.mLock) {
            this.mShouldBeBound = false;
            unbindLocked();
        }
    }

    private final void unbindLocked() {
        unscheduleRebindLocked();
        if (this.mBound) {
            Slog.i(this.mTag, "Stopping: " + this.mComponentName.flattenToShortString() + " u" + this.mUserId);
            this.mBound = false;
            this.mContext.unbindService(this.mServiceConnection);
            cleanUpConnectionLocked();
        }
    }

    void unscheduleRebindLocked() {
        injectRemoveCallbacks(this.mBindForBackoffRunnable);
        this.mRebindScheduled = false;
    }

    void scheduleRebindLocked() {
        unbindLocked();
        if (!this.mRebindScheduled) {
            Slog.i(this.mTag, "Scheduling to reconnect in " + this.mNextBackoffMs + " ms (uptime)");
            this.mReconnectTime = injectUptimeMillis() + this.mNextBackoffMs;
            injectPostAtTime(this.mBindForBackoffRunnable, this.mReconnectTime);
            this.mNextBackoffMs = Math.min(this.mRebindMaxBackoffMs, (long) (((double) this.mNextBackoffMs) * this.mRebindBackoffIncrease));
            this.mRebindScheduled = true;
        }
    }

    public void dump(String prefix, PrintWriter pw) {
        synchronized (this.mLock) {
            pw.print(prefix);
            pw.print(this.mComponentName.flattenToShortString());
            pw.print(this.mBound ? "  [bound]" : "  [not bound]");
            pw.print(this.mIsConnected ? "  [connected]" : "  [not connected]");
            if (this.mRebindScheduled) {
                pw.print("  reconnect in ");
                TimeUtils.formatDuration(this.mReconnectTime - injectUptimeMillis(), pw);
            }
            pw.println();
            pw.print(prefix);
            pw.print("  Next backoff(sec): ");
            pw.print(this.mNextBackoffMs / 1000);
        }
    }

    void injectRemoveCallbacks(Runnable r) {
        this.mHandler.removeCallbacks(r);
    }

    void injectPostAtTime(Runnable r, long uptimeMillis) {
        this.mHandler.postAtTime(r, uptimeMillis);
    }

    long injectUptimeMillis() {
        return SystemClock.uptimeMillis();
    }

    long getNextBackoffMsForTest() {
        return this.mNextBackoffMs;
    }

    long getReconnectTimeForTest() {
        return this.mReconnectTime;
    }

    ServiceConnection getServiceConnectionForTest() {
        return this.mServiceConnection;
    }

    Runnable getBindForBackoffRunnableForTest() {
        return this.mBindForBackoffRunnable;
    }

    boolean shouldBeBoundForTest() {
        return this.mShouldBeBound;
    }
}

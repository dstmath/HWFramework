package com.android.server.utils;

import android.os.Handler;
import android.os.IBinder;
import android.os.TokenWatcher;
import android.util.SparseArray;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.IndentingPrintWriter;
import java.io.PrintWriter;

public final class UserTokenWatcher {
    private final Callback mCallback;
    private final Handler mHandler;
    private final String mTag;
    @GuardedBy({"mWatchers"})
    private final SparseArray<TokenWatcher> mWatchers = new SparseArray<>(1);

    public interface Callback {
        void acquired(int i);

        void released(int i);
    }

    public UserTokenWatcher(Callback callback, Handler handler, String tag) {
        this.mCallback = callback;
        this.mHandler = handler;
        this.mTag = tag;
    }

    public void acquire(IBinder token, String tag, int userId) {
        synchronized (this.mWatchers) {
            TokenWatcher watcher = this.mWatchers.get(userId);
            if (watcher == null) {
                watcher = new InnerTokenWatcher(userId, this.mHandler, this.mTag);
                this.mWatchers.put(userId, watcher);
            }
            watcher.acquire(token, tag);
        }
    }

    public void release(IBinder token, int userId) {
        synchronized (this.mWatchers) {
            TokenWatcher watcher = this.mWatchers.get(userId);
            if (watcher != null) {
                watcher.release(token);
            }
        }
    }

    public boolean isAcquired(int userId) {
        boolean z;
        synchronized (this.mWatchers) {
            TokenWatcher watcher = this.mWatchers.get(userId);
            z = watcher != null && watcher.isAcquired();
        }
        return z;
    }

    public void dump(PrintWriter pw) {
        synchronized (this.mWatchers) {
            for (int i = 0; i < this.mWatchers.size(); i++) {
                int userId = this.mWatchers.keyAt(i);
                TokenWatcher watcher = this.mWatchers.valueAt(i);
                if (watcher.isAcquired()) {
                    pw.print("User ");
                    pw.print(userId);
                    pw.println(":");
                    watcher.dump(new IndentingPrintWriter(pw, " "));
                }
            }
        }
    }

    private final class InnerTokenWatcher extends TokenWatcher {
        private final int mUserId;

        private InnerTokenWatcher(int userId, Handler handler, String tag) {
            super(handler, tag);
            this.mUserId = userId;
        }

        @Override // android.os.TokenWatcher
        public void acquired() {
            UserTokenWatcher.this.mCallback.acquired(this.mUserId);
        }

        @Override // android.os.TokenWatcher
        public void released() {
            UserTokenWatcher.this.mCallback.released(this.mUserId);
            synchronized (UserTokenWatcher.this.mWatchers) {
                TokenWatcher watcher = (TokenWatcher) UserTokenWatcher.this.mWatchers.get(this.mUserId);
                if (watcher != null && !watcher.isAcquired()) {
                    UserTokenWatcher.this.mWatchers.remove(this.mUserId);
                }
            }
        }
    }
}

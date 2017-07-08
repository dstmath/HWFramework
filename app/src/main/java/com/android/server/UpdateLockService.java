package com.android.server;

import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.IUpdateLock.Stub;
import android.os.RemoteException;
import android.os.TokenWatcher;
import android.os.UserHandle;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class UpdateLockService extends Stub {
    static final boolean DEBUG = false;
    static final String PERMISSION = "android.permission.UPDATE_LOCK";
    static final String TAG = "UpdateLockService";
    Context mContext;
    LockWatcher mLocks;

    class LockWatcher extends TokenWatcher {
        LockWatcher(Handler h, String tag) {
            super(h, tag);
        }

        public void acquired() {
            UpdateLockService.this.sendLockChangedBroadcast(UpdateLockService.DEBUG);
        }

        public void released() {
            UpdateLockService.this.sendLockChangedBroadcast(true);
        }
    }

    UpdateLockService(Context context) {
        this.mContext = context;
        this.mLocks = new LockWatcher(new Handler(), "UpdateLocks");
        sendLockChangedBroadcast(true);
    }

    void sendLockChangedBroadcast(boolean state) {
        long oldIdent = Binder.clearCallingIdentity();
        try {
            this.mContext.sendStickyBroadcastAsUser(new Intent("android.os.UpdateLock.UPDATE_LOCK_CHANGED").putExtra("nowisconvenient", state).putExtra("timestamp", System.currentTimeMillis()).addFlags(67108864), UserHandle.ALL);
        } finally {
            Binder.restoreCallingIdentity(oldIdent);
        }
    }

    public void acquireUpdateLock(IBinder token, String tag) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, "acquireUpdateLock");
        this.mLocks.acquire(token, makeTag(tag));
    }

    public void releaseUpdateLock(IBinder token) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, "releaseUpdateLock");
        this.mLocks.release(token);
    }

    private String makeTag(String tag) {
        return "{tag=" + tag + " uid=" + Binder.getCallingUid() + " pid=" + Binder.getCallingPid() + '}';
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
            pw.println("Permission Denial: can't dump update lock service from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
        } else {
            this.mLocks.dump(pw);
        }
    }
}

package android.os;

import android.os.IUpdateLock;
import android.util.Log;

public class UpdateLock {
    private static final boolean DEBUG = false;
    public static final String NOW_IS_CONVENIENT = "nowisconvenient";
    private static final String TAG = "UpdateLock";
    public static final String TIMESTAMP = "timestamp";
    public static final String UPDATE_LOCK_CHANGED = "android.os.UpdateLock.UPDATE_LOCK_CHANGED";
    private static IUpdateLock sService;
    int mCount = 0;
    boolean mHeld = false;
    boolean mRefCounted = true;
    final String mTag;
    IBinder mToken;

    private static void checkService() {
        if (sService == null) {
            sService = IUpdateLock.Stub.asInterface(ServiceManager.getService("updatelock"));
        }
    }

    public UpdateLock(String tag) {
        this.mTag = tag;
        this.mToken = new Binder();
    }

    public void setReferenceCounted(boolean isRefCounted) {
        this.mRefCounted = isRefCounted;
    }

    public boolean isHeld() {
        boolean z;
        synchronized (this.mToken) {
            z = this.mHeld;
        }
        return z;
    }

    public void acquire() {
        checkService();
        synchronized (this.mToken) {
            acquireLocked();
        }
    }

    private void acquireLocked() {
        if (this.mRefCounted) {
            int i = this.mCount;
            this.mCount = i + 1;
            if (i != 0) {
                return;
            }
        }
        if (sService != null) {
            try {
                sService.acquireUpdateLock(this.mToken, this.mTag);
            } catch (RemoteException e) {
                Log.e(TAG, "Unable to contact service to acquire");
            }
        }
        this.mHeld = true;
    }

    public void release() {
        checkService();
        synchronized (this.mToken) {
            releaseLocked();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:3:0x000a, code lost:
        if (r0 == 0) goto L_0x000c;
     */
    private void releaseLocked() {
        if (this.mRefCounted) {
            int i = this.mCount - 1;
            this.mCount = i;
        }
        if (sService != null) {
            try {
                sService.releaseUpdateLock(this.mToken);
            } catch (RemoteException e) {
                Log.e(TAG, "Unable to contact service to release");
            }
        }
        this.mHeld = false;
        if (this.mCount < 0) {
            throw new RuntimeException("UpdateLock under-locked");
        }
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        synchronized (this.mToken) {
            if (this.mHeld) {
                Log.wtf(TAG, "UpdateLock finalized while still held");
                try {
                    sService.releaseUpdateLock(this.mToken);
                } catch (RemoteException e) {
                    Log.e(TAG, "Unable to contact service to release");
                }
            }
        }
    }
}

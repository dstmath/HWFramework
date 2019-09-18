package com.android.server;

import android.app.ActivityManager;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.inputmethod.IInputContentUriToken;

final class InputContentUriTokenHandler extends IInputContentUriToken.Stub {
    private final Object mLock = new Object();
    @GuardedBy("mLock")
    private IBinder mPermissionOwnerToken = null;
    private final int mSourceUid;
    private final int mSourceUserId;
    private final String mTargetPackage;
    private final int mTargetUserId;
    private final Uri mUri;

    InputContentUriTokenHandler(Uri contentUri, int sourceUid, String targetPackage, int sourceUserId, int targetUserId) {
        this.mUri = contentUri;
        this.mSourceUid = sourceUid;
        this.mTargetPackage = targetPackage;
        this.mSourceUserId = sourceUserId;
        this.mTargetUserId = targetUserId;
    }

    public void take() {
        synchronized (this.mLock) {
            if (this.mPermissionOwnerToken == null) {
                try {
                    this.mPermissionOwnerToken = ActivityManager.getService().newUriPermissionOwner("InputContentUriTokenHandler");
                } catch (RemoteException e) {
                    e.rethrowFromSystemServer();
                }
                doTakeLocked(this.mPermissionOwnerToken);
            }
        }
    }

    private void doTakeLocked(IBinder permissionOwner) {
        long origId = Binder.clearCallingIdentity();
        try {
            ActivityManager.getService().grantUriPermissionFromOwner(permissionOwner, this.mSourceUid, this.mTargetPackage, this.mUri, 1, this.mSourceUserId, this.mTargetUserId);
        } catch (RemoteException e) {
            e.rethrowFromSystemServer();
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(origId);
            throw th;
        }
        Binder.restoreCallingIdentity(origId);
    }

    public void release() {
        synchronized (this.mLock) {
            if (this.mPermissionOwnerToken != null) {
                try {
                    ActivityManager.getService().revokeUriPermissionFromOwner(this.mPermissionOwnerToken, this.mUri, 1, this.mSourceUserId);
                    this.mPermissionOwnerToken = null;
                } catch (RemoteException e) {
                    try {
                        e.rethrowFromSystemServer();
                    } finally {
                        this.mPermissionOwnerToken = null;
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            release();
        } finally {
            InputContentUriTokenHandler.super.finalize();
        }
    }
}

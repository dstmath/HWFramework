package com.android.server.inputmethod;

import android.app.UriGrantsManager;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.inputmethod.IInputContentUriToken;
import com.android.server.LocalServices;
import com.android.server.uri.UriGrantsManagerInternal;

/* access modifiers changed from: package-private */
public final class InputContentUriTokenHandler extends IInputContentUriToken.Stub {
    private final Object mLock = new Object();
    @GuardedBy({"mLock"})
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
                this.mPermissionOwnerToken = ((UriGrantsManagerInternal) LocalServices.getService(UriGrantsManagerInternal.class)).newUriPermissionOwner("InputContentUriTokenHandler");
                doTakeLocked(this.mPermissionOwnerToken);
            }
        }
    }

    private void doTakeLocked(IBinder permissionOwner) {
        long origId = Binder.clearCallingIdentity();
        try {
            UriGrantsManager.getService().grantUriPermissionFromOwner(permissionOwner, this.mSourceUid, this.mTargetPackage, this.mUri, 1, this.mSourceUserId, this.mTargetUserId);
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
                    ((UriGrantsManagerInternal) LocalServices.getService(UriGrantsManagerInternal.class)).revokeUriPermissionFromOwner(this.mPermissionOwnerToken, this.mUri, 1, this.mSourceUserId);
                } finally {
                    this.mPermissionOwnerToken = null;
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

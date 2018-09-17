package com.android.server.wm;

import android.app.ActivityManager;
import android.content.ClipData;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.RemoteException;
import com.android.internal.view.IDragAndDropPermissions.Stub;
import java.util.ArrayList;

class DragAndDropPermissionsHandler extends Stub implements DeathRecipient {
    private IBinder mActivityToken = null;
    private final int mMode;
    private IBinder mPermissionOwnerToken = null;
    private final int mSourceUid;
    private final int mSourceUserId;
    private final String mTargetPackage;
    private final int mTargetUserId;
    private IBinder mTransientToken = null;
    private final ArrayList<Uri> mUris = new ArrayList();

    DragAndDropPermissionsHandler(ClipData clipData, int sourceUid, String targetPackage, int mode, int sourceUserId, int targetUserId) {
        this.mSourceUid = sourceUid;
        this.mTargetPackage = targetPackage;
        this.mMode = mode;
        this.mSourceUserId = sourceUserId;
        this.mTargetUserId = targetUserId;
        clipData.collectUris(this.mUris);
    }

    public void take(IBinder activityToken) throws RemoteException {
        if (this.mActivityToken == null && this.mPermissionOwnerToken == null) {
            this.mActivityToken = activityToken;
            doTake(ActivityManager.getService().getUriPermissionOwnerForActivity(this.mActivityToken));
        }
    }

    private void doTake(IBinder permissionOwner) throws RemoteException {
        long origId = Binder.clearCallingIdentity();
        int i = 0;
        while (i < this.mUris.size()) {
            try {
                ActivityManager.getService().grantUriPermissionFromOwner(permissionOwner, this.mSourceUid, this.mTargetPackage, (Uri) this.mUris.get(i), this.mMode, this.mSourceUserId, this.mTargetUserId);
                i++;
            } finally {
                Binder.restoreCallingIdentity(origId);
            }
        }
    }

    public void takeTransient(IBinder transientToken) throws RemoteException {
        if (this.mActivityToken == null && this.mPermissionOwnerToken == null) {
            this.mPermissionOwnerToken = ActivityManager.getService().newUriPermissionOwner("drop");
            this.mTransientToken = transientToken;
            this.mTransientToken.linkToDeath(this, 0);
            doTake(this.mPermissionOwnerToken);
        }
    }

    public void release() throws RemoteException {
        if (this.mActivityToken != null || this.mPermissionOwnerToken != null) {
            IBinder permissionOwner = null;
            if (this.mActivityToken != null) {
                try {
                    permissionOwner = ActivityManager.getService().getUriPermissionOwnerForActivity(this.mActivityToken);
                } catch (Exception e) {
                    return;
                } finally {
                    this.mActivityToken = null;
                }
            } else {
                permissionOwner = this.mPermissionOwnerToken;
                this.mPermissionOwnerToken = null;
                this.mTransientToken.unlinkToDeath(this, 0);
                this.mTransientToken = null;
            }
            for (int i = 0; i < this.mUris.size(); i++) {
                ActivityManager.getService().revokeUriPermissionFromOwner(permissionOwner, (Uri) this.mUris.get(i), this.mMode, this.mSourceUserId);
            }
        }
    }

    public void binderDied() {
        try {
            release();
        } catch (RemoteException e) {
        }
    }
}

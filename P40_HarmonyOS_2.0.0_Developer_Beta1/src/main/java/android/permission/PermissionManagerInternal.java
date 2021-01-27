package android.permission;

import android.os.UserHandle;

public abstract class PermissionManagerInternal {

    public interface OnRuntimePermissionStateChangedListener {
        void onRuntimePermissionStateChanged(String str, int i);
    }

    public abstract void addOnRuntimePermissionStateChangedListener(OnRuntimePermissionStateChangedListener onRuntimePermissionStateChangedListener);

    public abstract byte[] backupRuntimePermissions(UserHandle userHandle);

    public abstract void removeOnRuntimePermissionStateChangedListener(OnRuntimePermissionStateChangedListener onRuntimePermissionStateChangedListener);

    public abstract void restoreDelayedRuntimePermissions(String str, UserHandle userHandle);

    public abstract void restoreRuntimePermissions(byte[] bArr, UserHandle userHandle);
}

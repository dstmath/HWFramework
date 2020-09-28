package android.accounts;

import android.os.RemoteCallback;

public abstract class AccountManagerInternal {

    public interface OnAppPermissionChangeListener {
        void onAppPermissionChanged(Account account, int i);
    }

    public abstract void addOnAppPermissionChangeListener(OnAppPermissionChangeListener onAppPermissionChangeListener);

    public abstract byte[] backupAccountAccessPermissions(int i);

    public abstract boolean hasAccountAccess(Account account, int i);

    public abstract void requestAccountAccess(Account account, String str, int i, RemoteCallback remoteCallback);

    public abstract void restoreAccountAccessPermissions(byte[] bArr, int i);
}

package com.android.server.oemlock;

import android.content.Context;
import android.os.UserHandle;
import android.os.UserManager;
import android.service.persistentdata.PersistentDataBlockManager;
import android.util.Slog;

class PersistentDataBlockLock extends OemLock {
    private static final String TAG = "OemLock";
    private Context mContext;

    PersistentDataBlockLock(Context context) {
        this.mContext = context;
    }

    /* access modifiers changed from: package-private */
    public void setOemUnlockAllowedByCarrier(boolean allowed, byte[] signature) {
        if (signature != null) {
            Slog.w(TAG, "Signature provided but is not being used");
        }
        UserManager.get(this.mContext).setUserRestriction("no_oem_unlock", !allowed, UserHandle.SYSTEM);
        if (!allowed) {
            disallowUnlockIfNotUnlocked();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isOemUnlockAllowedByCarrier() {
        return !UserManager.get(this.mContext).hasUserRestriction("no_oem_unlock", UserHandle.SYSTEM);
    }

    /* access modifiers changed from: package-private */
    public void setOemUnlockAllowedByDevice(boolean allowedByDevice) {
        ((PersistentDataBlockManager) this.mContext.getSystemService("persistent_data_block")).setOemUnlockEnabled(allowedByDevice);
    }

    /* access modifiers changed from: package-private */
    public boolean isOemUnlockAllowedByDevice() {
        return ((PersistentDataBlockManager) this.mContext.getSystemService("persistent_data_block")).getOemUnlockEnabled();
    }

    private void disallowUnlockIfNotUnlocked() {
        PersistentDataBlockManager pdbm = (PersistentDataBlockManager) this.mContext.getSystemService("persistent_data_block");
        if (pdbm.getFlashLockState() != 0) {
            pdbm.setOemUnlockEnabled(false);
        }
    }
}

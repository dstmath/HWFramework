package com.android.server;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Binder;
import android.os.IBinder;
import android.os.UserHandle;
import android.os.UserManager;
import android.service.oemlock.IOemLockService.Stub;
import android.service.persistentdata.PersistentDataBlockManager;

public class OemLockService extends SystemService {
    private Context mContext;
    private final IBinder mService = new Stub() {
        public void setOemUnlockAllowedByCarrier(boolean allowed, byte[] signature) {
            OemLockService.this.enforceManageCarrierOemUnlockPermission();
            OemLockService.this.enforceUserIsAdmin();
            UserHandle userHandle = UserHandle.of(UserHandle.getCallingUserId());
            long token = Binder.clearCallingIdentity();
            try {
                UserManager.get(OemLockService.this.mContext).setUserRestriction("no_oem_unlock", allowed ^ 1, userHandle);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public boolean isOemUnlockAllowedByCarrier() {
            OemLockService.this.enforceManageCarrierOemUnlockPermission();
            return OemLockService.this.doIsOemUnlockAllowedByCarrier();
        }

        public void setOemUnlockAllowedByUser(boolean allowedByUser) {
            if (!ActivityManager.isUserAMonkey()) {
                OemLockService.this.enforceManageUserOemUnlockPermission();
                OemLockService.this.enforceUserIsAdmin();
                PersistentDataBlockManager pdbm = (PersistentDataBlockManager) OemLockService.this.mContext.getSystemService("persistent_data_block");
                long token = Binder.clearCallingIdentity();
                try {
                    pdbm.setOemUnlockEnabled(allowedByUser);
                } finally {
                    Binder.restoreCallingIdentity(token);
                }
            }
        }

        public boolean isOemUnlockAllowedByUser() {
            OemLockService.this.enforceManageUserOemUnlockPermission();
            return OemLockService.this.doIsOemUnlockAllowedByUser();
        }
    };

    public OemLockService(Context context) {
        super(context);
        this.mContext = context;
    }

    public void onStart() {
        publishBinderService("oem_lock", this.mService);
    }

    private boolean doIsOemUnlockAllowedByCarrier() {
        return UserManager.get(this.mContext).hasUserRestriction("no_oem_unlock") ^ 1;
    }

    private boolean doIsOemUnlockAllowedByUser() {
        PersistentDataBlockManager pdbm = (PersistentDataBlockManager) this.mContext.getSystemService("persistent_data_block");
        long token = Binder.clearCallingIdentity();
        try {
            boolean oemUnlockEnabled = pdbm.getOemUnlockEnabled();
            return oemUnlockEnabled;
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    private void enforceManageCarrierOemUnlockPermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_CARRIER_OEM_UNLOCK_STATE", "Can't manage OEM unlock allowed by carrier");
    }

    private void enforceManageUserOemUnlockPermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_USER_OEM_UNLOCK_STATE", "Can't manage OEM unlock allowed by user");
    }

    private void enforceUserIsAdmin() {
        int userId = UserHandle.getCallingUserId();
        long token = Binder.clearCallingIdentity();
        try {
            if (!UserManager.get(this.mContext).isUserAdmin(userId)) {
                throw new SecurityException("Must be an admin user");
            }
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }
}

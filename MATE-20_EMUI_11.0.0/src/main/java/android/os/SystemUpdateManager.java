package android.os;

import android.annotation.SystemApi;
import com.android.internal.util.Preconditions;

@SystemApi
public class SystemUpdateManager {
    public static final String KEY_IS_SECURITY_UPDATE = "is_security_update";
    public static final String KEY_STATUS = "status";
    public static final String KEY_TARGET_BUILD_FINGERPRINT = "target_build_fingerprint";
    public static final String KEY_TARGET_SECURITY_PATCH_LEVEL = "target_security_patch_level";
    public static final String KEY_TITLE = "title";
    public static final int STATUS_IDLE = 1;
    public static final int STATUS_IN_PROGRESS = 3;
    public static final int STATUS_UNKNOWN = 0;
    public static final int STATUS_WAITING_DOWNLOAD = 2;
    public static final int STATUS_WAITING_INSTALL = 4;
    public static final int STATUS_WAITING_REBOOT = 5;
    private static final String TAG = "SystemUpdateManager";
    private final ISystemUpdateManager mService;

    public SystemUpdateManager(ISystemUpdateManager service) {
        this.mService = (ISystemUpdateManager) Preconditions.checkNotNull(service, "missing ISystemUpdateManager");
    }

    public Bundle retrieveSystemUpdateInfo() {
        try {
            return this.mService.retrieveSystemUpdateInfo();
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public void updateSystemUpdateInfo(PersistableBundle infoBundle) {
        if (infoBundle == null || !infoBundle.containsKey("status")) {
            throw new IllegalArgumentException("Missing status in the bundle");
        }
        try {
            this.mService.updateSystemUpdateInfo(infoBundle);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }
}

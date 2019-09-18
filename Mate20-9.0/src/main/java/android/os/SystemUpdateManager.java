package android.os;

import android.annotation.SystemApi;
import com.android.internal.util.Preconditions;

@SystemApi
public class SystemUpdateManager {
    @SystemApi
    public static final String KEY_IS_SECURITY_UPDATE = "is_security_update";
    @SystemApi
    public static final String KEY_STATUS = "status";
    @SystemApi
    public static final String KEY_TARGET_BUILD_FINGERPRINT = "target_build_fingerprint";
    @SystemApi
    public static final String KEY_TARGET_SECURITY_PATCH_LEVEL = "target_security_patch_level";
    @SystemApi
    public static final String KEY_TITLE = "title";
    @SystemApi
    public static final int STATUS_IDLE = 1;
    @SystemApi
    public static final int STATUS_IN_PROGRESS = 3;
    @SystemApi
    public static final int STATUS_UNKNOWN = 0;
    @SystemApi
    public static final int STATUS_WAITING_DOWNLOAD = 2;
    @SystemApi
    public static final int STATUS_WAITING_INSTALL = 4;
    @SystemApi
    public static final int STATUS_WAITING_REBOOT = 5;
    private static final String TAG = "SystemUpdateManager";
    private final ISystemUpdateManager mService;

    public SystemUpdateManager(ISystemUpdateManager service) {
        this.mService = (ISystemUpdateManager) Preconditions.checkNotNull(service, "missing ISystemUpdateManager");
    }

    @SystemApi
    public Bundle retrieveSystemUpdateInfo() {
        try {
            return this.mService.retrieveSystemUpdateInfo();
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    @SystemApi
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

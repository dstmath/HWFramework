package android.securityprofile;

import android.os.Bundle;
import huawei.android.security.securityprofile.SecurityProfileManagerImpl;
import java.util.List;

public class SecurityProfileManager {
    public static final int ACTION_ADD = 2;
    public static final int ACTION_REMOVE = 3;
    public static final int ACTION_UPDATE_ALL = 1;
    private static SecurityProfileManager sSelf = null;
    private SecurityProfileManagerImpl mManagerImpl;

    private SecurityProfileManager() {
        this.mManagerImpl = null;
        this.mManagerImpl = SecurityProfileManagerImpl.getDefault();
    }

    public static SecurityProfileManager getDefault() {
        SecurityProfileManager securityProfileManager;
        synchronized (SecurityProfileManager.class) {
            if (sSelf == null) {
                sSelf = new SecurityProfileManager();
            }
            securityProfileManager = sSelf;
        }
        return securityProfileManager;
    }

    public void updateBlackApp(List<String> blackListedPackages, int action) {
        this.mManagerImpl.updateBlackApp(blackListedPackages, action);
    }

    public boolean updateMdmCertBlacklist(List<String> blacklist, int action) {
        return this.mManagerImpl.updateMdmCertBlacklist(blacklist, action);
    }

    public boolean isBlackApp(String packageName) {
        return this.mManagerImpl.isBlackApp(packageName);
    }

    public Bundle getHwSignedInfo(String packageName, Bundle extraParams) {
        return this.mManagerImpl.getHwSignedInfo(packageName, extraParams);
    }

    public Bundle setHwSignedInfoToSEAPP(Bundle params) {
        return this.mManagerImpl.setHwSignedInfoToSEAPP(params);
    }
}

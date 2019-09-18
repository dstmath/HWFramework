package com.android.server.om;

import android.content.om.OverlayInfo;
import android.content.pm.PackageInfo;
import android.os.UserHandle;
import android.util.Slog;
import com.android.server.pm.Installer;
import java.io.File;

class IdmapManager {
    private final Installer mInstaller;

    IdmapManager(Installer installer) {
        this.mInstaller = installer;
    }

    /* access modifiers changed from: package-private */
    public boolean createIdmap(PackageInfo targetPackage, PackageInfo overlayPackage, int userId) {
        int sharedGid = UserHandle.getSharedAppGid(targetPackage.applicationInfo.uid);
        String targetPath = targetPackage.applicationInfo.getBaseCodePath();
        String overlayPath = overlayPackage.applicationInfo.getBaseCodePath();
        try {
            this.mInstaller.idmap(targetPath, overlayPath, sharedGid);
            return true;
        } catch (Installer.InstallerException e) {
            Slog.w("OverlayManager", "failed to generate idmap for " + targetPath + " and " + overlayPath + ": " + e.getMessage());
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean removeIdmap(OverlayInfo oi, int userId) {
        try {
            this.mInstaller.removeIdmap(oi.baseCodePath);
            return true;
        } catch (Installer.InstallerException e) {
            Slog.w("OverlayManager", "failed to remove idmap for " + oi.baseCodePath + ": " + e.getMessage());
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean idmapExists(OverlayInfo oi) {
        return new File(getIdmapPath(oi.baseCodePath)).isFile();
    }

    /* access modifiers changed from: package-private */
    public boolean idmapExists(PackageInfo overlayPackage, int userId) {
        return new File(getIdmapPath(overlayPackage.applicationInfo.getBaseCodePath())).isFile();
    }

    private String getIdmapPath(String baseCodePath) {
        return "/data/resource-cache/" + baseCodePath.substring(1).replace('/', '@') + "@idmap";
    }
}

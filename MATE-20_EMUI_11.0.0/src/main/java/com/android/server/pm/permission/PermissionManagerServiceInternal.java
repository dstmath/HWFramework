package com.android.server.pm.permission;

import android.content.pm.PackageParser;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.permission.PermissionManagerInternal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class PermissionManagerServiceInternal extends PermissionManagerInternal {
    public abstract void addAllPermissionGroups(PackageParser.Package v, boolean z);

    public abstract void addAllPermissions(PackageParser.Package v, boolean z);

    public abstract boolean addDynamicPermission(PermissionInfo permissionInfo, boolean z, int i, PermissionCallback permissionCallback);

    public abstract int checkPermission(String str, String str2, int i, int i2);

    public abstract int checkUidPermission(String str, PackageParser.Package v, int i, int i2);

    public abstract void enforceCrossUserPermission(int i, int i2, boolean z, boolean z2, String str);

    public abstract void enforceCrossUserPermission(int i, int i2, boolean z, boolean z2, boolean z3, String str);

    public abstract void enforceGrantRevokeRuntimePermissionPermissions(String str);

    public abstract List<PermissionGroupInfo> getAllPermissionGroups(int i, int i2);

    public abstract ArrayList<PermissionInfo> getAllPermissionWithProtectionLevel(int i);

    public abstract String[] getAppOpPermissionPackages(String str);

    public abstract DefaultPermissionGrantPolicy getDefaultPermissionGrantPolicy();

    public abstract int getPermissionFlags(String str, String str2, int i, int i2);

    public abstract PermissionGroupInfo getPermissionGroupInfo(String str, int i, int i2);

    public abstract PermissionInfo getPermissionInfo(String str, String str2, int i, int i2);

    public abstract List<PermissionInfo> getPermissionInfoByGroup(String str, int i, int i2);

    public abstract PermissionSettings getPermissionSettings();

    public abstract BasePermission getPermissionTEMP(String str);

    public abstract List<String> getWhitelistedRestrictedPermissions(PackageParser.Package v, int i, int i2);

    public abstract void grantRequestedRuntimePermissions(PackageParser.Package v, int[] iArr, String[] strArr, int i, PermissionCallback permissionCallback);

    public abstract void grantRuntimePermission(String str, String str2, boolean z, int i, int i2, PermissionCallback permissionCallback);

    public abstract void grantRuntimePermissionsGrantedToDisabledPackage(PackageParser.Package v, int i, PermissionCallback permissionCallback);

    public abstract boolean isPermissionsReviewRequired(PackageParser.Package v, int i);

    public abstract void removeAllPermissions(PackageParser.Package v, boolean z);

    public abstract void removeDynamicPermission(String str, int i, PermissionCallback permissionCallback);

    public abstract void revokeRuntimePermission(String str, String str2, boolean z, int i, PermissionCallback permissionCallback);

    public abstract void revokeRuntimePermissionsIfGroupChanged(PackageParser.Package v, PackageParser.Package v2, ArrayList<String> arrayList, PermissionCallback permissionCallback);

    public abstract void setWhitelistedRestrictedPermissions(PackageParser.Package v, int[] iArr, List<String> list, int i, int i2, PermissionCallback permissionCallback);

    public abstract void systemReady();

    public abstract void updateAllPermissions(String str, boolean z, Collection<PackageParser.Package> collection, PermissionCallback permissionCallback);

    public abstract void updatePermissionFlags(String str, String str2, int i, int i2, int i3, int i4, boolean z, PermissionCallback permissionCallback);

    public abstract boolean updatePermissionFlagsForAllApps(int i, int i2, int i3, int i4, Collection<PackageParser.Package> collection, PermissionCallback permissionCallback);

    public abstract void updatePermissions(String str, PackageParser.Package v, boolean z, Collection<PackageParser.Package> collection, PermissionCallback permissionCallback);

    public static class PermissionCallback {
        public void onGidsChanged(int appId, int userId) {
        }

        public void onPermissionChanged() {
        }

        public void onPermissionGranted(int uid, int userId) {
        }

        public void onInstallPermissionGranted() {
        }

        public void onPermissionRevoked(int uid, int userId) {
        }

        public void onInstallPermissionRevoked() {
        }

        public void onPermissionUpdated(int[] updatedUserIds, boolean sync) {
        }

        public void onPermissionRemoved() {
        }

        public void onInstallPermissionUpdated() {
        }

        public void onOneTimePermissionFlagUpdated(int uid) {
        }
    }
}

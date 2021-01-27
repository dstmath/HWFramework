package com.android.server.pm.permission;

import android.content.Context;
import android.content.pm.PackageParserEx;
import android.os.Looper;
import java.util.Set;

public class DefaultPermissionGrantPolicyEx {
    private DefaultPermissionGrantPolicyBridge defaultPermissionGrantPolicy;

    public DefaultPermissionGrantPolicyEx(Context context, Looper looper, PermissionManagerServiceEx permissionManager) {
        this.defaultPermissionGrantPolicy = new DefaultPermissionGrantPolicyBridge(context, looper, permissionManager.getPermissionManagerService());
        this.defaultPermissionGrantPolicy.setDefaultPermissionGrantPolicyEx(this);
    }

    public DefaultPermissionGrantPolicyBridge getDefaultPermissionGrantPolicy() {
        return this.defaultPermissionGrantPolicy;
    }

    public void setDefaultPermissionGrantPolicy(DefaultPermissionGrantPolicyBridge defaultPermissionGrantPolicy2) {
        this.defaultPermissionGrantPolicy = defaultPermissionGrantPolicy2;
    }

    public void grantDefaultPermissions(int userId) {
        this.defaultPermissionGrantPolicy.grantDefaultPermissions(userId);
    }

    public void grantCustDefaultPermissions(int uid) {
    }

    /* access modifiers changed from: protected */
    public PackageParserEx.PackageEx getSystemPackage(String packageName) {
        return new PackageParserEx.PackageEx(this.defaultPermissionGrantPolicy.getSystemPackage(packageName));
    }

    public final void grantPermissionsToPackage(String packageName, int userId, boolean ignoreSystemPackage, boolean whitelistRestrictedPermissions, Set<String>... permissionGroups) {
        this.defaultPermissionGrantPolicy.grantPermissionsToPackage(packageName, userId, ignoreSystemPackage, whitelistRestrictedPermissions, permissionGroups);
    }

    /* access modifiers changed from: protected */
    public final void grantPermissionsToPackage(String packageName, int userId, boolean systemFixed, boolean ignoreSystemPackage, boolean whitelistRestrictedPermissions, Set<String>... permissionGroups) {
        this.defaultPermissionGrantPolicy.grantPermissionsToPackage(packageName, userId, systemFixed, ignoreSystemPackage, whitelistRestrictedPermissions, permissionGroups);
    }
}

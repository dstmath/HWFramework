package com.android.server.pm.permission;

import android.content.Context;
import android.os.Looper;

public class DefaultPermissionGrantPolicyBridge extends DefaultPermissionGrantPolicy {
    private DefaultPermissionGrantPolicyEx defaultPermissionGrantPolicyEx;

    public DefaultPermissionGrantPolicyBridge(Context context, Looper looper, PermissionManagerService permissionManager) {
        super(context, looper, permissionManager);
    }

    public void setDefaultPermissionGrantPolicyEx(DefaultPermissionGrantPolicyEx defaultPermissionGrantPolicyEx2) {
        this.defaultPermissionGrantPolicyEx = defaultPermissionGrantPolicyEx2;
    }

    public void grantDefaultPermissions(int userId) {
        DefaultPermissionGrantPolicyBridge.super.grantDefaultPermissions(userId);
        this.defaultPermissionGrantPolicyEx.grantDefaultPermissions(userId);
    }

    public void grantCustDefaultPermissions(int uid) {
        this.defaultPermissionGrantPolicyEx.grantCustDefaultPermissions(uid);
    }
}

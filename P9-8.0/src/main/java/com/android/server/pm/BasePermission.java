package com.android.server.pm;

import android.content.pm.PackageParser.Permission;
import android.content.pm.PermissionInfo;
import android.os.UserHandle;

final class BasePermission {
    static final int TYPE_BUILTIN = 1;
    static final int TYPE_DYNAMIC = 2;
    static final int TYPE_NORMAL = 0;
    private int[] gids;
    final String name;
    PackageSettingBase packageSetting;
    PermissionInfo pendingInfo;
    private boolean perUser;
    Permission perm;
    int protectionLevel = 2;
    String sourcePackage;
    final int type;
    int uid;

    BasePermission(String _name, String _sourcePackage, int _type) {
        this.name = _name;
        this.sourcePackage = _sourcePackage;
        this.type = _type;
    }

    public String toString() {
        return "BasePermission{" + Integer.toHexString(System.identityHashCode(this)) + " " + this.name + "}";
    }

    public void setGids(int[] gids, boolean perUser) {
        this.gids = gids;
        this.perUser = perUser;
    }

    public int[] computeGids(int userId) {
        if (!this.perUser) {
            return this.gids;
        }
        int[] userGids = new int[this.gids.length];
        for (int i = 0; i < this.gids.length; i++) {
            userGids[i] = UserHandle.getUid(userId, this.gids[i]);
        }
        return userGids;
    }

    public boolean isRuntime() {
        return (this.protectionLevel & 15) == 1;
    }

    public boolean isDevelopment() {
        if ((this.protectionLevel & 15) != 2 || (this.protectionLevel & 32) == 0) {
            return false;
        }
        return true;
    }

    public boolean isInstant() {
        return (this.protectionLevel & 4096) != 0;
    }

    public boolean isRuntimeOnly() {
        return (this.protectionLevel & 8192) != 0;
    }
}

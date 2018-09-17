package com.android.server.pm;

abstract class SettingBase {
    protected final PermissionsState mPermissionsState;
    int pkgFlags;
    int pkgPrivateFlags;

    SettingBase(int pkgFlags, int pkgPrivateFlags) {
        setFlags(pkgFlags);
        setPrivateFlags(pkgPrivateFlags);
        this.mPermissionsState = new PermissionsState();
    }

    SettingBase(SettingBase base) {
        this.pkgFlags = base.pkgFlags;
        this.pkgPrivateFlags = base.pkgPrivateFlags;
        this.mPermissionsState = new PermissionsState(base.mPermissionsState);
    }

    public PermissionsState getPermissionsState() {
        return this.mPermissionsState;
    }

    void setFlags(int pkgFlags) {
        this.pkgFlags = 262145 & pkgFlags;
    }

    void setPrivateFlags(int pkgPrivateFlags) {
        this.pkgPrivateFlags = pkgPrivateFlags & 1036;
    }
}

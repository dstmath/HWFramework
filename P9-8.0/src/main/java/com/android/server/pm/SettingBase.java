package com.android.server.pm;

abstract class SettingBase {
    protected final PermissionsState mPermissionsState = new PermissionsState();
    int pkgFlags;
    int pkgPrivateFlags;

    SettingBase(int pkgFlags, int pkgPrivateFlags) {
        setFlags(pkgFlags);
        setPrivateFlags(pkgPrivateFlags);
    }

    SettingBase(SettingBase orig) {
        doCopy(orig);
    }

    public void copyFrom(SettingBase orig) {
        doCopy(orig);
    }

    private void doCopy(SettingBase orig) {
        this.pkgFlags = orig.pkgFlags;
        this.pkgPrivateFlags = orig.pkgPrivateFlags;
        this.mPermissionsState.copyFrom(orig.mPermissionsState);
    }

    public PermissionsState getPermissionsState() {
        return this.mPermissionsState;
    }

    void setFlags(int pkgFlags) {
        this.pkgFlags = 262145 & pkgFlags;
    }

    void setPrivateFlags(int pkgPrivateFlags) {
        this.pkgPrivateFlags = pkgPrivateFlags & 524;
    }
}

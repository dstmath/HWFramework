package com.android.server.pm;

import com.android.server.pm.permission.PermissionsState;

/* access modifiers changed from: package-private */
public abstract class SettingBase {
    protected final PermissionsState mPermissionsState = new PermissionsState();
    int pkgFlags;
    int pkgPrivateFlags;

    SettingBase(int pkgFlags2, int pkgPrivateFlags2) {
        setFlags(pkgFlags2);
        setPrivateFlags(pkgPrivateFlags2);
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

    /* access modifiers changed from: package-private */
    public void setFlags(int pkgFlags2) {
        this.pkgFlags = 262145 & pkgFlags2;
    }

    /* access modifiers changed from: package-private */
    public void setPrivateFlags(int pkgPrivateFlags2) {
        this.pkgPrivateFlags = 1076757000 & pkgPrivateFlags2;
    }
}

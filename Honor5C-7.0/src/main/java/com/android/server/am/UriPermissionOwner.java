package com.android.server.am;

import android.os.Binder;
import android.os.IBinder;
import android.util.ArraySet;
import com.android.server.am.ActivityManagerService.GrantUri;
import com.google.android.collect.Sets;
import java.io.PrintWriter;
import java.util.Iterator;

final class UriPermissionOwner {
    Binder externalToken;
    private ArraySet<UriPermission> mReadPerms;
    private ArraySet<UriPermission> mWritePerms;
    final Object owner;
    final ActivityManagerService service;

    class ExternalToken extends Binder {
        ExternalToken() {
        }

        UriPermissionOwner getOwner() {
            return UriPermissionOwner.this;
        }
    }

    UriPermissionOwner(ActivityManagerService service, Object owner) {
        this.service = service;
        this.owner = owner;
    }

    Binder getExternalTokenLocked() {
        if (this.externalToken == null) {
            this.externalToken = new ExternalToken();
        }
        return this.externalToken;
    }

    static UriPermissionOwner fromExternalToken(IBinder token) {
        if (token instanceof ExternalToken) {
            return ((ExternalToken) token).getOwner();
        }
        return null;
    }

    void removeUriPermissionsLocked() {
        removeUriPermissionsLocked(3);
    }

    void removeUriPermissionsLocked(int mode) {
        removeUriPermissionLocked(null, mode);
    }

    void removeUriPermissionLocked(GrantUri grantUri, int mode) {
        Iterator<UriPermission> it;
        UriPermission perm;
        if (!((mode & 1) == 0 || this.mReadPerms == null)) {
            it = this.mReadPerms.iterator();
            while (it.hasNext()) {
                perm = (UriPermission) it.next();
                if (grantUri == null || grantUri.equals(perm.uri)) {
                    perm.removeReadOwner(this);
                    this.service.removeUriPermissionIfNeededLocked(perm);
                    it.remove();
                }
            }
            if (this.mReadPerms.isEmpty()) {
                this.mReadPerms = null;
            }
        }
        if ((mode & 2) != 0 && this.mWritePerms != null) {
            it = this.mWritePerms.iterator();
            while (it.hasNext()) {
                perm = (UriPermission) it.next();
                if (grantUri == null || grantUri.equals(perm.uri)) {
                    perm.removeWriteOwner(this);
                    this.service.removeUriPermissionIfNeededLocked(perm);
                    it.remove();
                }
            }
            if (this.mWritePerms.isEmpty()) {
                this.mWritePerms = null;
            }
        }
    }

    public void addReadPermission(UriPermission perm) {
        if (this.mReadPerms == null) {
            this.mReadPerms = Sets.newArraySet();
        }
        this.mReadPerms.add(perm);
    }

    public void addWritePermission(UriPermission perm) {
        if (this.mWritePerms == null) {
            this.mWritePerms = Sets.newArraySet();
        }
        this.mWritePerms.add(perm);
    }

    public void removeReadPermission(UriPermission perm) {
        this.mReadPerms.remove(perm);
        if (this.mReadPerms.isEmpty()) {
            this.mReadPerms = null;
        }
    }

    public void removeWritePermission(UriPermission perm) {
        this.mWritePerms.remove(perm);
        if (this.mWritePerms.isEmpty()) {
            this.mWritePerms = null;
        }
    }

    public void dump(PrintWriter pw, String prefix) {
        if (this.mReadPerms != null) {
            pw.print(prefix);
            pw.print("readUriPermissions=");
            pw.println(this.mReadPerms);
        }
        if (this.mWritePerms != null) {
            pw.print(prefix);
            pw.print("writeUriPermissions=");
            pw.println(this.mWritePerms);
        }
    }

    public String toString() {
        return this.owner.toString();
    }
}

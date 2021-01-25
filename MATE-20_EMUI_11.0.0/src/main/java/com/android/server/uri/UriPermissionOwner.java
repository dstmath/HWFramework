package com.android.server.uri;

import android.os.Binder;
import android.os.IBinder;
import android.util.ArraySet;
import android.util.proto.ProtoOutputStream;
import com.google.android.collect.Sets;
import java.io.PrintWriter;
import java.util.Iterator;

public class UriPermissionOwner {
    private Binder externalToken;
    private final Object mOwner;
    private ArraySet<UriPermission> mReadPerms;
    private final UriGrantsManagerInternal mService;
    private ArraySet<UriPermission> mWritePerms;

    class ExternalToken extends Binder {
        ExternalToken() {
        }

        /* access modifiers changed from: package-private */
        public UriPermissionOwner getOwner() {
            return UriPermissionOwner.this;
        }
    }

    public UriPermissionOwner(UriGrantsManagerInternal service, Object owner) {
        this.mService = service;
        this.mOwner = owner;
    }

    public Binder getExternalToken() {
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

    public void removeUriPermissions() {
        removeUriPermissions(3);
    }

    /* access modifiers changed from: package-private */
    public void removeUriPermissions(int mode) {
        removeUriPermission(null, mode);
    }

    /* access modifiers changed from: package-private */
    public void removeUriPermission(GrantUri grantUri, int mode) {
        ArraySet<UriPermission> arraySet;
        ArraySet<UriPermission> arraySet2;
        if (!((mode & 1) == 0 || (arraySet2 = this.mReadPerms) == null)) {
            Iterator<UriPermission> it = arraySet2.iterator();
            while (it.hasNext()) {
                UriPermission perm = it.next();
                if (grantUri == null || grantUri.equals(perm.uri)) {
                    perm.removeReadOwner(this);
                    this.mService.removeUriPermissionIfNeeded(perm);
                    it.remove();
                }
            }
            if (this.mReadPerms.isEmpty()) {
                this.mReadPerms = null;
            }
        }
        if ((mode & 2) != 0 && (arraySet = this.mWritePerms) != null) {
            Iterator<UriPermission> it2 = arraySet.iterator();
            while (it2.hasNext()) {
                UriPermission perm2 = it2.next();
                if (grantUri == null || grantUri.equals(perm2.uri)) {
                    perm2.removeWriteOwner(this);
                    this.mService.removeUriPermissionIfNeeded(perm2);
                    it2.remove();
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

    public void writeToProto(ProtoOutputStream proto, long fieldId) {
        long token = proto.start(fieldId);
        proto.write(1138166333441L, this.mOwner.toString());
        ArraySet<UriPermission> arraySet = this.mReadPerms;
        if (arraySet != null) {
            synchronized (arraySet) {
                Iterator<UriPermission> it = this.mReadPerms.iterator();
                while (it.hasNext()) {
                    it.next().uri.writeToProto(proto, 2246267895810L);
                }
            }
        }
        ArraySet<UriPermission> arraySet2 = this.mWritePerms;
        if (arraySet2 != null) {
            synchronized (arraySet2) {
                Iterator<UriPermission> it2 = this.mWritePerms.iterator();
                while (it2.hasNext()) {
                    it2.next().uri.writeToProto(proto, 2246267895811L);
                }
            }
        }
        proto.end(token);
    }

    public String toString() {
        return this.mOwner.toString();
    }
}

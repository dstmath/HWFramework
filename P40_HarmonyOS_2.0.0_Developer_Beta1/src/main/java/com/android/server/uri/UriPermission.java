package com.android.server.uri;

import android.app.GrantedUriPermission;
import android.os.Binder;
import android.os.UserHandle;
import android.util.ArraySet;
import android.util.Log;
import android.util.Slog;
import com.google.android.collect.Sets;
import java.io.PrintWriter;
import java.util.Comparator;
import java.util.Iterator;

/* access modifiers changed from: package-private */
public final class UriPermission {
    private static final long INVALID_TIME = Long.MIN_VALUE;
    public static final int STRENGTH_GLOBAL = 2;
    public static final int STRENGTH_NONE = 0;
    public static final int STRENGTH_OWNED = 1;
    public static final int STRENGTH_PERSISTABLE = 3;
    private static final String TAG = "UriPermission";
    int globalModeFlags = 0;
    private ArraySet<UriPermissionOwner> mReadOwners;
    private ArraySet<UriPermissionOwner> mWriteOwners;
    int modeFlags = 0;
    int ownedModeFlags = 0;
    int persistableModeFlags = 0;
    long persistedCreateTime = INVALID_TIME;
    int persistedModeFlags = 0;
    final String sourcePkg;
    private String stringName;
    final String targetPkg;
    final int targetUid;
    final int targetUserId;
    final GrantUri uri;

    UriPermission(String sourcePkg2, String targetPkg2, int targetUid2, GrantUri uri2) {
        this.targetUserId = UserHandle.getUserId(targetUid2);
        this.sourcePkg = sourcePkg2;
        this.targetPkg = targetPkg2;
        this.targetUid = targetUid2;
        this.uri = uri2;
    }

    private void updateModeFlags() {
        int oldModeFlags = this.modeFlags;
        this.modeFlags = this.ownedModeFlags | this.globalModeFlags | this.persistableModeFlags | this.persistedModeFlags;
        if (Log.isLoggable(TAG, 2) && this.modeFlags != oldModeFlags) {
            Slog.d(TAG, "Permission for " + this.targetPkg + " to " + this.uri + " is changing from 0x" + Integer.toHexString(oldModeFlags) + " to 0x" + Integer.toHexString(this.modeFlags) + " via calling UID " + Binder.getCallingUid() + " PID " + Binder.getCallingPid(), new Throwable());
        }
    }

    /* access modifiers changed from: package-private */
    public void initPersistedModes(int modeFlags2, long createdTime) {
        int modeFlags3 = modeFlags2 & 3;
        this.persistableModeFlags = modeFlags3;
        this.persistedModeFlags = modeFlags3;
        this.persistedCreateTime = createdTime;
        updateModeFlags();
    }

    /* access modifiers changed from: package-private */
    public void grantModes(int modeFlags2, UriPermissionOwner owner) {
        boolean persistable = (modeFlags2 & 64) != 0;
        int modeFlags3 = modeFlags2 & 3;
        if (persistable) {
            this.persistableModeFlags |= modeFlags3;
        }
        if (owner == null) {
            this.globalModeFlags |= modeFlags3;
        } else {
            if ((modeFlags3 & 1) != 0) {
                addReadOwner(owner);
            }
            if ((modeFlags3 & 2) != 0) {
                addWriteOwner(owner);
            }
        }
        updateModeFlags();
    }

    /* access modifiers changed from: package-private */
    public boolean takePersistableModes(int modeFlags2) {
        int modeFlags3 = modeFlags2 & 3;
        int i = this.persistableModeFlags;
        if ((modeFlags3 & i) != modeFlags3) {
            Slog.w(TAG, "Requested flags 0x" + Integer.toHexString(modeFlags3) + ", but only 0x" + Integer.toHexString(this.persistableModeFlags) + " are allowed");
            return false;
        }
        int before = this.persistedModeFlags;
        this.persistedModeFlags = (i & modeFlags3) | this.persistedModeFlags;
        if (this.persistedModeFlags != 0) {
            this.persistedCreateTime = System.currentTimeMillis();
        }
        updateModeFlags();
        if (this.persistedModeFlags != before) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean releasePersistableModes(int modeFlags2) {
        int modeFlags3 = modeFlags2 & 3;
        int before = this.persistedModeFlags;
        this.persistableModeFlags &= ~modeFlags3;
        this.persistedModeFlags &= ~modeFlags3;
        if (this.persistedModeFlags == 0) {
            this.persistedCreateTime = INVALID_TIME;
        }
        updateModeFlags();
        return this.persistedModeFlags != before;
    }

    /* access modifiers changed from: package-private */
    public boolean revokeModes(int modeFlags2, boolean includingOwners) {
        boolean persistable = (modeFlags2 & 64) != 0;
        int modeFlags3 = modeFlags2 & 3;
        int before = this.persistedModeFlags;
        if ((modeFlags3 & 1) != 0) {
            if (persistable) {
                this.persistableModeFlags &= -2;
                this.persistedModeFlags &= -2;
            }
            this.globalModeFlags &= -2;
            ArraySet<UriPermissionOwner> arraySet = this.mReadOwners;
            if (arraySet != null && includingOwners) {
                this.ownedModeFlags &= -2;
                Iterator<UriPermissionOwner> it = arraySet.iterator();
                while (it.hasNext()) {
                    it.next().removeReadPermission(this);
                }
                this.mReadOwners = null;
            }
        }
        if ((modeFlags3 & 2) != 0) {
            if (persistable) {
                this.persistableModeFlags &= -3;
                this.persistedModeFlags &= -3;
            }
            this.globalModeFlags &= -3;
            ArraySet<UriPermissionOwner> arraySet2 = this.mWriteOwners;
            if (arraySet2 != null && includingOwners) {
                this.ownedModeFlags &= -3;
                Iterator<UriPermissionOwner> it2 = arraySet2.iterator();
                while (it2.hasNext()) {
                    it2.next().removeWritePermission(this);
                }
                this.mWriteOwners = null;
            }
        }
        if (this.persistedModeFlags == 0) {
            this.persistedCreateTime = INVALID_TIME;
        }
        updateModeFlags();
        return this.persistedModeFlags != before;
    }

    public int getStrength(int modeFlags2) {
        int modeFlags3 = modeFlags2 & 3;
        if ((this.persistableModeFlags & modeFlags3) == modeFlags3) {
            return 3;
        }
        if ((this.globalModeFlags & modeFlags3) == modeFlags3) {
            return 2;
        }
        if ((this.ownedModeFlags & modeFlags3) == modeFlags3) {
            return 1;
        }
        return 0;
    }

    private void addReadOwner(UriPermissionOwner owner) {
        if (this.mReadOwners == null) {
            this.mReadOwners = Sets.newArraySet();
            this.ownedModeFlags |= 1;
            updateModeFlags();
        }
        if (this.mReadOwners.add(owner)) {
            owner.addReadPermission(this);
        }
    }

    /* access modifiers changed from: package-private */
    public void removeReadOwner(UriPermissionOwner owner) {
        if (!this.mReadOwners.remove(owner)) {
            Slog.wtf(TAG, "Unknown read owner " + owner + " in " + this);
        }
        if (this.mReadOwners.size() == 0) {
            this.mReadOwners = null;
            this.ownedModeFlags &= -2;
            updateModeFlags();
        }
    }

    private void addWriteOwner(UriPermissionOwner owner) {
        if (this.mWriteOwners == null) {
            this.mWriteOwners = Sets.newArraySet();
            this.ownedModeFlags |= 2;
            updateModeFlags();
        }
        if (this.mWriteOwners.add(owner)) {
            owner.addWritePermission(this);
        }
    }

    /* access modifiers changed from: package-private */
    public void removeWriteOwner(UriPermissionOwner owner) {
        if (!this.mWriteOwners.remove(owner)) {
            Slog.wtf(TAG, "Unknown write owner " + owner + " in " + this);
        }
        if (this.mWriteOwners.size() == 0) {
            this.mWriteOwners = null;
            this.ownedModeFlags &= -3;
            updateModeFlags();
        }
    }

    public String toString() {
        String str = this.stringName;
        if (str != null) {
            return str;
        }
        StringBuilder sb = new StringBuilder(128);
        sb.append("UriPermission{");
        sb.append(Integer.toHexString(System.identityHashCode(this)));
        sb.append(' ');
        sb.append(this.uri);
        sb.append('}');
        String sb2 = sb.toString();
        this.stringName = sb2;
        return sb2;
    }

    /* access modifiers changed from: package-private */
    public void dump(PrintWriter pw, String prefix) {
        pw.print(prefix);
        pw.print("targetUserId=" + this.targetUserId);
        pw.print(" sourcePkg=" + this.sourcePkg);
        pw.println(" targetPkg=" + this.targetPkg);
        pw.print(prefix);
        pw.print("mode=0x" + Integer.toHexString(this.modeFlags));
        pw.print(" owned=0x" + Integer.toHexString(this.ownedModeFlags));
        pw.print(" global=0x" + Integer.toHexString(this.globalModeFlags));
        pw.print(" persistable=0x" + Integer.toHexString(this.persistableModeFlags));
        pw.print(" persisted=0x" + Integer.toHexString(this.persistedModeFlags));
        if (this.persistedCreateTime != INVALID_TIME) {
            pw.print(" persistedCreate=" + this.persistedCreateTime);
        }
        pw.println();
        if (this.mReadOwners != null) {
            pw.print(prefix);
            pw.println("readOwners:");
            Iterator<UriPermissionOwner> it = this.mReadOwners.iterator();
            while (it.hasNext()) {
                pw.print(prefix);
                pw.println("  * " + it.next());
            }
        }
        if (this.mWriteOwners != null) {
            pw.print(prefix);
            pw.println("writeOwners:");
            Iterator<UriPermissionOwner> it2 = this.mReadOwners.iterator();
            while (it2.hasNext()) {
                pw.print(prefix);
                pw.println("  * " + it2.next());
            }
        }
    }

    public static class PersistedTimeComparator implements Comparator<UriPermission> {
        public int compare(UriPermission lhs, UriPermission rhs) {
            return Long.compare(lhs.persistedCreateTime, rhs.persistedCreateTime);
        }
    }

    public static class Snapshot {
        final long persistedCreateTime;
        final int persistedModeFlags;
        final String sourcePkg;
        final String targetPkg;
        final int targetUserId;
        final GrantUri uri;

        private Snapshot(UriPermission perm) {
            this.targetUserId = perm.targetUserId;
            this.sourcePkg = perm.sourcePkg;
            this.targetPkg = perm.targetPkg;
            this.uri = perm.uri;
            this.persistedModeFlags = perm.persistedModeFlags;
            this.persistedCreateTime = perm.persistedCreateTime;
        }
    }

    public Snapshot snapshot() {
        return new Snapshot();
    }

    public android.content.UriPermission buildPersistedPublicApiObject() {
        return new android.content.UriPermission(this.uri.uri, this.persistedModeFlags, this.persistedCreateTime);
    }

    public GrantedUriPermission buildGrantedUriPermission() {
        return new GrantedUriPermission(this.uri.uri, this.targetPkg);
    }
}

package com.android.server.am;

import android.os.UserHandle;
import android.util.ArraySet;
import android.util.Log;
import android.util.Slog;
import com.android.server.am.ActivityManagerService.GrantUri;
import com.google.android.collect.Sets;
import java.io.PrintWriter;
import java.util.Comparator;

final class UriPermission {
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

        /* synthetic */ Snapshot(UriPermission perm, Snapshot -this1) {
            this(perm);
        }

        private Snapshot(UriPermission perm) {
            this.targetUserId = perm.targetUserId;
            this.sourcePkg = perm.sourcePkg;
            this.targetPkg = perm.targetPkg;
            this.uri = perm.uri;
            this.persistedModeFlags = perm.persistedModeFlags;
            this.persistedCreateTime = perm.persistedCreateTime;
        }
    }

    UriPermission(String sourcePkg, String targetPkg, int targetUid, GrantUri uri) {
        this.targetUserId = UserHandle.getUserId(targetUid);
        this.sourcePkg = sourcePkg;
        this.targetPkg = targetPkg;
        this.targetUid = targetUid;
        this.uri = uri;
    }

    private void updateModeFlags() {
        int oldModeFlags = this.modeFlags;
        this.modeFlags = ((this.ownedModeFlags | this.globalModeFlags) | this.persistableModeFlags) | this.persistedModeFlags;
        if (Log.isLoggable(TAG, 2) && this.modeFlags != oldModeFlags) {
            Slog.d(TAG, "Permission for " + this.targetPkg + " to " + this.uri + " is changing from 0x" + Integer.toHexString(oldModeFlags) + " to 0x" + Integer.toHexString(this.modeFlags), new Throwable());
        }
    }

    void initPersistedModes(int modeFlags, long createdTime) {
        modeFlags &= 3;
        this.persistableModeFlags = modeFlags;
        this.persistedModeFlags = modeFlags;
        this.persistedCreateTime = createdTime;
        updateModeFlags();
    }

    void grantModes(int modeFlags, UriPermissionOwner owner) {
        boolean persistable = (modeFlags & 64) != 0;
        modeFlags &= 3;
        if (persistable) {
            this.persistableModeFlags |= modeFlags;
        }
        if (owner == null) {
            this.globalModeFlags |= modeFlags;
        } else {
            if ((modeFlags & 1) != 0) {
                addReadOwner(owner);
            }
            if ((modeFlags & 2) != 0) {
                addWriteOwner(owner);
            }
        }
        updateModeFlags();
    }

    boolean takePersistableModes(int modeFlags) {
        boolean z = false;
        modeFlags &= 3;
        if ((this.persistableModeFlags & modeFlags) != modeFlags) {
            Slog.w(TAG, "Requested flags 0x" + Integer.toHexString(modeFlags) + ", but only 0x" + Integer.toHexString(this.persistableModeFlags) + " are allowed");
            return false;
        }
        int before = this.persistedModeFlags;
        this.persistedModeFlags |= this.persistableModeFlags & modeFlags;
        if (this.persistedModeFlags != 0) {
            this.persistedCreateTime = System.currentTimeMillis();
        }
        updateModeFlags();
        if (this.persistedModeFlags != before) {
            z = true;
        }
        return z;
    }

    boolean releasePersistableModes(int modeFlags) {
        modeFlags &= 3;
        int before = this.persistedModeFlags;
        this.persistableModeFlags &= ~modeFlags;
        this.persistedModeFlags &= ~modeFlags;
        if (this.persistedModeFlags == 0) {
            this.persistedCreateTime = INVALID_TIME;
        }
        updateModeFlags();
        if (this.persistedModeFlags != before) {
            return true;
        }
        return false;
    }

    boolean revokeModes(int modeFlags, boolean includingOwners) {
        boolean persistable = (modeFlags & 64) != 0;
        modeFlags &= 3;
        int before = this.persistedModeFlags;
        if ((modeFlags & 1) != 0) {
            if (persistable) {
                this.persistableModeFlags &= -2;
                this.persistedModeFlags &= -2;
            }
            this.globalModeFlags &= -2;
            if (this.mReadOwners != null && includingOwners) {
                this.ownedModeFlags &= -2;
                for (UriPermissionOwner r : this.mReadOwners) {
                    r.removeReadPermission(this);
                }
                this.mReadOwners = null;
            }
        }
        if ((modeFlags & 2) != 0) {
            if (persistable) {
                this.persistableModeFlags &= -3;
                this.persistedModeFlags &= -3;
            }
            this.globalModeFlags &= -3;
            if (this.mWriteOwners != null && includingOwners) {
                this.ownedModeFlags &= -3;
                for (UriPermissionOwner r2 : this.mWriteOwners) {
                    r2.removeWritePermission(this);
                }
                this.mWriteOwners = null;
            }
        }
        if (this.persistedModeFlags == 0) {
            this.persistedCreateTime = INVALID_TIME;
        }
        updateModeFlags();
        if (this.persistedModeFlags != before) {
            return true;
        }
        return false;
    }

    public int getStrength(int modeFlags) {
        modeFlags &= 3;
        if ((this.persistableModeFlags & modeFlags) == modeFlags) {
            return 3;
        }
        if ((this.globalModeFlags & modeFlags) == modeFlags) {
            return 2;
        }
        if ((this.ownedModeFlags & modeFlags) == modeFlags) {
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

    void removeReadOwner(UriPermissionOwner owner) {
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

    void removeWriteOwner(UriPermissionOwner owner) {
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
        if (this.stringName != null) {
            return this.stringName;
        }
        StringBuilder sb = new StringBuilder(128);
        sb.append("UriPermission{");
        sb.append(Integer.toHexString(System.identityHashCode(this)));
        sb.append(' ');
        sb.append(this.uri);
        sb.append('}');
        String stringBuilder = sb.toString();
        this.stringName = stringBuilder;
        return stringBuilder;
    }

    void dump(PrintWriter pw, String prefix) {
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
            for (UriPermissionOwner owner : this.mReadOwners) {
                pw.print(prefix);
                pw.println("  * " + owner);
            }
        }
        if (this.mWriteOwners != null) {
            pw.print(prefix);
            pw.println("writeOwners:");
            for (UriPermissionOwner owner2 : this.mReadOwners) {
                pw.print(prefix);
                pw.println("  * " + owner2);
            }
        }
    }

    public Snapshot snapshot() {
        return new Snapshot(this, null);
    }

    public android.content.UriPermission buildPersistedPublicApiObject() {
        return new android.content.UriPermission(this.uri.uri, this.persistedModeFlags, this.persistedCreateTime);
    }
}

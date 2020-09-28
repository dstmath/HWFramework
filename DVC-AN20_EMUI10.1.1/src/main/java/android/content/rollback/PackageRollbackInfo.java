package android.content.rollback;

import android.annotation.SystemApi;
import android.content.pm.VersionedPackage;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.IntArray;
import android.util.SparseLongArray;
import java.util.ArrayList;
import java.util.Iterator;

@SystemApi
public final class PackageRollbackInfo implements Parcelable {
    public static final Parcelable.Creator<PackageRollbackInfo> CREATOR = new Parcelable.Creator<PackageRollbackInfo>() {
        /* class android.content.rollback.PackageRollbackInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public PackageRollbackInfo createFromParcel(Parcel in) {
            return new PackageRollbackInfo(in);
        }

        @Override // android.os.Parcelable.Creator
        public PackageRollbackInfo[] newArray(int size) {
            return new PackageRollbackInfo[size];
        }
    };
    private final SparseLongArray mCeSnapshotInodes;
    private final IntArray mInstalledUsers;
    private final boolean mIsApex;
    private final IntArray mPendingBackups;
    private final ArrayList<RestoreInfo> mPendingRestores;
    private final VersionedPackage mVersionRolledBackFrom;
    private final VersionedPackage mVersionRolledBackTo;

    public static class RestoreInfo {
        public final int appId;
        public final String seInfo;
        public final int userId;

        public RestoreInfo(int userId2, int appId2, String seInfo2) {
            this.userId = userId2;
            this.appId = appId2;
            this.seInfo = seInfo2;
        }
    }

    public String getPackageName() {
        return this.mVersionRolledBackFrom.getPackageName();
    }

    public VersionedPackage getVersionRolledBackFrom() {
        return this.mVersionRolledBackFrom;
    }

    public VersionedPackage getVersionRolledBackTo() {
        return this.mVersionRolledBackTo;
    }

    public void addPendingBackup(int userId) {
        this.mPendingBackups.add(userId);
    }

    public IntArray getPendingBackups() {
        return this.mPendingBackups;
    }

    public ArrayList<RestoreInfo> getPendingRestores() {
        return this.mPendingRestores;
    }

    public RestoreInfo getRestoreInfo(int userId) {
        Iterator<RestoreInfo> it = this.mPendingRestores.iterator();
        while (it.hasNext()) {
            RestoreInfo ri = it.next();
            if (ri.userId == userId) {
                return ri;
            }
        }
        return null;
    }

    public void removeRestoreInfo(RestoreInfo ri) {
        this.mPendingRestores.remove(ri);
    }

    public boolean isApex() {
        return this.mIsApex;
    }

    public IntArray getInstalledUsers() {
        return this.mInstalledUsers;
    }

    public SparseLongArray getCeSnapshotInodes() {
        return this.mCeSnapshotInodes;
    }

    public void putCeSnapshotInode(int userId, long ceSnapshotInode) {
        this.mCeSnapshotInodes.put(userId, ceSnapshotInode);
    }

    public void removePendingBackup(int userId) {
        int idx = this.mPendingBackups.indexOf(userId);
        if (idx != -1) {
            this.mPendingBackups.remove(idx);
        }
    }

    public void removePendingRestoreInfo(int userId) {
        removeRestoreInfo(getRestoreInfo(userId));
    }

    public PackageRollbackInfo(VersionedPackage packageRolledBackFrom, VersionedPackage packageRolledBackTo, IntArray pendingBackups, ArrayList<RestoreInfo> pendingRestores, boolean isApex, IntArray installedUsers, SparseLongArray ceSnapshotInodes) {
        this.mVersionRolledBackFrom = packageRolledBackFrom;
        this.mVersionRolledBackTo = packageRolledBackTo;
        this.mPendingBackups = pendingBackups;
        this.mPendingRestores = pendingRestores;
        this.mIsApex = isApex;
        this.mInstalledUsers = installedUsers;
        this.mCeSnapshotInodes = ceSnapshotInodes;
    }

    private PackageRollbackInfo(Parcel in) {
        this.mVersionRolledBackFrom = VersionedPackage.CREATOR.createFromParcel(in);
        this.mVersionRolledBackTo = VersionedPackage.CREATOR.createFromParcel(in);
        this.mIsApex = in.readBoolean();
        this.mPendingRestores = null;
        this.mPendingBackups = null;
        this.mInstalledUsers = null;
        this.mCeSnapshotInodes = null;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        this.mVersionRolledBackFrom.writeToParcel(out, flags);
        this.mVersionRolledBackTo.writeToParcel(out, flags);
        out.writeBoolean(this.mIsApex);
    }
}

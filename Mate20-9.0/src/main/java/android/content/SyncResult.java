package android.content;

import android.app.backup.FullBackup;
import android.os.Parcel;
import android.os.Parcelable;

public final class SyncResult implements Parcelable {
    public static final SyncResult ALREADY_IN_PROGRESS = new SyncResult(true);
    public static final Parcelable.Creator<SyncResult> CREATOR = new Parcelable.Creator<SyncResult>() {
        public SyncResult createFromParcel(Parcel in) {
            return new SyncResult(in);
        }

        public SyncResult[] newArray(int size) {
            return new SyncResult[size];
        }
    };
    public boolean databaseError;
    public long delayUntil;
    public boolean fullSyncRequested;
    public boolean moreRecordsToGet;
    public boolean partialSyncUnavailable;
    public final SyncStats stats;
    public final boolean syncAlreadyInProgress;
    public boolean tooManyDeletions;
    public boolean tooManyRetries;

    public SyncResult() {
        this(false);
    }

    private SyncResult(boolean syncAlreadyInProgress2) {
        this.syncAlreadyInProgress = syncAlreadyInProgress2;
        this.tooManyDeletions = false;
        this.tooManyRetries = false;
        this.fullSyncRequested = false;
        this.partialSyncUnavailable = false;
        this.moreRecordsToGet = false;
        this.delayUntil = 0;
        this.stats = new SyncStats();
    }

    private SyncResult(Parcel parcel) {
        boolean z = false;
        this.syncAlreadyInProgress = parcel.readInt() != 0;
        this.tooManyDeletions = parcel.readInt() != 0;
        this.tooManyRetries = parcel.readInt() != 0;
        this.databaseError = parcel.readInt() != 0;
        this.fullSyncRequested = parcel.readInt() != 0;
        this.partialSyncUnavailable = parcel.readInt() != 0;
        this.moreRecordsToGet = parcel.readInt() != 0 ? true : z;
        this.delayUntil = parcel.readLong();
        this.stats = new SyncStats(parcel);
    }

    public boolean hasHardError() {
        return this.stats.numParseExceptions > 0 || this.stats.numConflictDetectedExceptions > 0 || this.stats.numAuthExceptions > 0 || this.tooManyDeletions || this.tooManyRetries || this.databaseError;
    }

    public boolean hasSoftError() {
        return this.syncAlreadyInProgress || this.stats.numIoExceptions > 0;
    }

    public boolean hasError() {
        return hasSoftError() || hasHardError();
    }

    public boolean madeSomeProgress() {
        return (this.stats.numDeletes > 0 && !this.tooManyDeletions) || this.stats.numInserts > 0 || this.stats.numUpdates > 0;
    }

    public void clear() {
        if (!this.syncAlreadyInProgress) {
            this.tooManyDeletions = false;
            this.tooManyRetries = false;
            this.databaseError = false;
            this.fullSyncRequested = false;
            this.partialSyncUnavailable = false;
            this.moreRecordsToGet = false;
            this.delayUntil = 0;
            this.stats.clear();
            return;
        }
        throw new UnsupportedOperationException("you are not allowed to clear the ALREADY_IN_PROGRESS SyncStats");
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(this.syncAlreadyInProgress ? 1 : 0);
        parcel.writeInt(this.tooManyDeletions ? 1 : 0);
        parcel.writeInt(this.tooManyRetries ? 1 : 0);
        parcel.writeInt(this.databaseError ? 1 : 0);
        parcel.writeInt(this.fullSyncRequested ? 1 : 0);
        parcel.writeInt(this.partialSyncUnavailable ? 1 : 0);
        parcel.writeInt(this.moreRecordsToGet ? 1 : 0);
        parcel.writeLong(this.delayUntil);
        this.stats.writeToParcel(parcel, flags);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SyncResult:");
        if (this.syncAlreadyInProgress) {
            sb.append(" syncAlreadyInProgress: ");
            sb.append(this.syncAlreadyInProgress);
        }
        if (this.tooManyDeletions) {
            sb.append(" tooManyDeletions: ");
            sb.append(this.tooManyDeletions);
        }
        if (this.tooManyRetries) {
            sb.append(" tooManyRetries: ");
            sb.append(this.tooManyRetries);
        }
        if (this.databaseError) {
            sb.append(" databaseError: ");
            sb.append(this.databaseError);
        }
        if (this.fullSyncRequested) {
            sb.append(" fullSyncRequested: ");
            sb.append(this.fullSyncRequested);
        }
        if (this.partialSyncUnavailable) {
            sb.append(" partialSyncUnavailable: ");
            sb.append(this.partialSyncUnavailable);
        }
        if (this.moreRecordsToGet) {
            sb.append(" moreRecordsToGet: ");
            sb.append(this.moreRecordsToGet);
        }
        if (this.delayUntil > 0) {
            sb.append(" delayUntil: ");
            sb.append(this.delayUntil);
        }
        sb.append(this.stats);
        return sb.toString();
    }

    public String toDebugString() {
        StringBuffer sb = new StringBuffer();
        if (this.fullSyncRequested) {
            sb.append("f1");
        }
        if (this.partialSyncUnavailable) {
            sb.append("r1");
        }
        if (hasHardError()) {
            sb.append("X1");
        }
        if (this.stats.numParseExceptions > 0) {
            sb.append("e");
            sb.append(this.stats.numParseExceptions);
        }
        if (this.stats.numConflictDetectedExceptions > 0) {
            sb.append(FullBackup.CACHE_TREE_TOKEN);
            sb.append(this.stats.numConflictDetectedExceptions);
        }
        if (this.stats.numAuthExceptions > 0) {
            sb.append(FullBackup.APK_TREE_TOKEN);
            sb.append(this.stats.numAuthExceptions);
        }
        if (this.tooManyDeletions) {
            sb.append("D1");
        }
        if (this.tooManyRetries) {
            sb.append("R1");
        }
        if (this.databaseError) {
            sb.append("b1");
        }
        if (hasSoftError()) {
            sb.append("x1");
        }
        if (this.syncAlreadyInProgress) {
            sb.append("l1");
        }
        if (this.stats.numIoExceptions > 0) {
            sb.append("I");
            sb.append(this.stats.numIoExceptions);
        }
        return sb.toString();
    }
}

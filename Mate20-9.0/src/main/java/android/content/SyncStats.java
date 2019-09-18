package android.content;

import android.os.Parcel;
import android.os.Parcelable;

public class SyncStats implements Parcelable {
    public static final Parcelable.Creator<SyncStats> CREATOR = new Parcelable.Creator<SyncStats>() {
        public SyncStats createFromParcel(Parcel in) {
            return new SyncStats(in);
        }

        public SyncStats[] newArray(int size) {
            return new SyncStats[size];
        }
    };
    public long numAuthExceptions;
    public long numConflictDetectedExceptions;
    public long numDeletes;
    public long numEntries;
    public long numInserts;
    public long numIoExceptions;
    public long numParseExceptions;
    public long numSkippedEntries;
    public long numUpdates;

    public SyncStats() {
        this.numAuthExceptions = 0;
        this.numIoExceptions = 0;
        this.numParseExceptions = 0;
        this.numConflictDetectedExceptions = 0;
        this.numInserts = 0;
        this.numUpdates = 0;
        this.numDeletes = 0;
        this.numEntries = 0;
        this.numSkippedEntries = 0;
    }

    public SyncStats(Parcel in) {
        this.numAuthExceptions = in.readLong();
        this.numIoExceptions = in.readLong();
        this.numParseExceptions = in.readLong();
        this.numConflictDetectedExceptions = in.readLong();
        this.numInserts = in.readLong();
        this.numUpdates = in.readLong();
        this.numDeletes = in.readLong();
        this.numEntries = in.readLong();
        this.numSkippedEntries = in.readLong();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(" stats [");
        if (this.numAuthExceptions > 0) {
            sb.append(" numAuthExceptions: ");
            sb.append(this.numAuthExceptions);
        }
        if (this.numIoExceptions > 0) {
            sb.append(" numIoExceptions: ");
            sb.append(this.numIoExceptions);
        }
        if (this.numParseExceptions > 0) {
            sb.append(" numParseExceptions: ");
            sb.append(this.numParseExceptions);
        }
        if (this.numConflictDetectedExceptions > 0) {
            sb.append(" numConflictDetectedExceptions: ");
            sb.append(this.numConflictDetectedExceptions);
        }
        if (this.numInserts > 0) {
            sb.append(" numInserts: ");
            sb.append(this.numInserts);
        }
        if (this.numUpdates > 0) {
            sb.append(" numUpdates: ");
            sb.append(this.numUpdates);
        }
        if (this.numDeletes > 0) {
            sb.append(" numDeletes: ");
            sb.append(this.numDeletes);
        }
        if (this.numEntries > 0) {
            sb.append(" numEntries: ");
            sb.append(this.numEntries);
        }
        if (this.numSkippedEntries > 0) {
            sb.append(" numSkippedEntries: ");
            sb.append(this.numSkippedEntries);
        }
        sb.append("]");
        return sb.toString();
    }

    public void clear() {
        this.numAuthExceptions = 0;
        this.numIoExceptions = 0;
        this.numParseExceptions = 0;
        this.numConflictDetectedExceptions = 0;
        this.numInserts = 0;
        this.numUpdates = 0;
        this.numDeletes = 0;
        this.numEntries = 0;
        this.numSkippedEntries = 0;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.numAuthExceptions);
        dest.writeLong(this.numIoExceptions);
        dest.writeLong(this.numParseExceptions);
        dest.writeLong(this.numConflictDetectedExceptions);
        dest.writeLong(this.numInserts);
        dest.writeLong(this.numUpdates);
        dest.writeLong(this.numDeletes);
        dest.writeLong(this.numEntries);
        dest.writeLong(this.numSkippedEntries);
    }
}

package android.net.metrics;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public final class ApfStats implements Parcelable {
    public static final Creator<ApfStats> CREATOR = new Creator<ApfStats>() {
        public ApfStats createFromParcel(Parcel in) {
            return new ApfStats(in, null);
        }

        public ApfStats[] newArray(int size) {
            return new ApfStats[size];
        }
    };
    public int droppedRas;
    public long durationMs;
    public int matchingRas;
    public int maxProgramSize;
    public int parseErrors;
    public int programUpdates;
    public int programUpdatesAll;
    public int programUpdatesAllowingMulticast;
    public int receivedRas;
    public int zeroLifetimeRas;

    /* synthetic */ ApfStats(Parcel in, ApfStats -this1) {
        this(in);
    }

    private ApfStats(Parcel in) {
        this.durationMs = in.readLong();
        this.receivedRas = in.readInt();
        this.matchingRas = in.readInt();
        this.droppedRas = in.readInt();
        this.zeroLifetimeRas = in.readInt();
        this.parseErrors = in.readInt();
        this.programUpdates = in.readInt();
        this.programUpdatesAll = in.readInt();
        this.programUpdatesAllowingMulticast = in.readInt();
        this.maxProgramSize = in.readInt();
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(this.durationMs);
        out.writeInt(this.receivedRas);
        out.writeInt(this.matchingRas);
        out.writeInt(this.droppedRas);
        out.writeInt(this.zeroLifetimeRas);
        out.writeInt(this.parseErrors);
        out.writeInt(this.programUpdates);
        out.writeInt(this.programUpdatesAll);
        out.writeInt(this.programUpdatesAllowingMulticast);
        out.writeInt(this.maxProgramSize);
    }

    public int describeContents() {
        return 0;
    }

    public String toString() {
        return "ApfStats(" + String.format("%dms ", new Object[]{Long.valueOf(this.durationMs)}) + String.format("%dB RA: {", new Object[]{Integer.valueOf(this.maxProgramSize)}) + String.format("%d received, ", new Object[]{Integer.valueOf(this.receivedRas)}) + String.format("%d matching, ", new Object[]{Integer.valueOf(this.matchingRas)}) + String.format("%d dropped, ", new Object[]{Integer.valueOf(this.droppedRas)}) + String.format("%d zero lifetime, ", new Object[]{Integer.valueOf(this.zeroLifetimeRas)}) + String.format("%d parse errors}, ", new Object[]{Integer.valueOf(this.parseErrors)}) + String.format("updates: {all: %d, RAs: %d, allow multicast: %d})", new Object[]{Integer.valueOf(this.programUpdatesAll), Integer.valueOf(this.programUpdates), Integer.valueOf(this.programUpdatesAllowingMulticast)});
    }
}

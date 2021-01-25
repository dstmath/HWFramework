package android.net.metrics;

import android.annotation.SystemApi;
import android.annotation.UnsupportedAppUsage;
import android.net.metrics.IpConnectivityLog;
import android.os.Parcel;
import android.os.Parcelable;

@SystemApi
public final class ApfStats implements IpConnectivityLog.Event {
    public static final Parcelable.Creator<ApfStats> CREATOR = new Parcelable.Creator<ApfStats>() {
        /* class android.net.metrics.ApfStats.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ApfStats createFromParcel(Parcel in) {
            return new ApfStats(in);
        }

        @Override // android.os.Parcelable.Creator
        public ApfStats[] newArray(int size) {
            return new ApfStats[size];
        }
    };
    @UnsupportedAppUsage
    public final int droppedRas;
    @UnsupportedAppUsage
    public final long durationMs;
    @UnsupportedAppUsage
    public final int matchingRas;
    @UnsupportedAppUsage
    public final int maxProgramSize;
    @UnsupportedAppUsage
    public final int parseErrors;
    @UnsupportedAppUsage
    public final int programUpdates;
    @UnsupportedAppUsage
    public final int programUpdatesAll;
    @UnsupportedAppUsage
    public final int programUpdatesAllowingMulticast;
    @UnsupportedAppUsage
    public final int receivedRas;
    @UnsupportedAppUsage
    public final int zeroLifetimeRas;

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

    private ApfStats(long durationMs2, int receivedRas2, int matchingRas2, int droppedRas2, int zeroLifetimeRas2, int parseErrors2, int programUpdates2, int programUpdatesAll2, int programUpdatesAllowingMulticast2, int maxProgramSize2) {
        this.durationMs = durationMs2;
        this.receivedRas = receivedRas2;
        this.matchingRas = matchingRas2;
        this.droppedRas = droppedRas2;
        this.zeroLifetimeRas = zeroLifetimeRas2;
        this.parseErrors = parseErrors2;
        this.programUpdates = programUpdates2;
        this.programUpdatesAll = programUpdatesAll2;
        this.programUpdatesAllowingMulticast = programUpdatesAllowingMulticast2;
        this.maxProgramSize = maxProgramSize2;
    }

    @SystemApi
    public static final class Builder {
        private int mDroppedRas;
        private long mDurationMs;
        private int mMatchingRas;
        private int mMaxProgramSize;
        private int mParseErrors;
        private int mProgramUpdates;
        private int mProgramUpdatesAll;
        private int mProgramUpdatesAllowingMulticast;
        private int mReceivedRas;
        private int mZeroLifetimeRas;

        public Builder setDurationMs(long durationMs) {
            this.mDurationMs = durationMs;
            return this;
        }

        public Builder setReceivedRas(int receivedRas) {
            this.mReceivedRas = receivedRas;
            return this;
        }

        public Builder setMatchingRas(int matchingRas) {
            this.mMatchingRas = matchingRas;
            return this;
        }

        public Builder setDroppedRas(int droppedRas) {
            this.mDroppedRas = droppedRas;
            return this;
        }

        public Builder setZeroLifetimeRas(int zeroLifetimeRas) {
            this.mZeroLifetimeRas = zeroLifetimeRas;
            return this;
        }

        public Builder setParseErrors(int parseErrors) {
            this.mParseErrors = parseErrors;
            return this;
        }

        public Builder setProgramUpdates(int programUpdates) {
            this.mProgramUpdates = programUpdates;
            return this;
        }

        public Builder setProgramUpdatesAll(int programUpdatesAll) {
            this.mProgramUpdatesAll = programUpdatesAll;
            return this;
        }

        public Builder setProgramUpdatesAllowingMulticast(int programUpdatesAllowingMulticast) {
            this.mProgramUpdatesAllowingMulticast = programUpdatesAllowingMulticast;
            return this;
        }

        public Builder setMaxProgramSize(int maxProgramSize) {
            this.mMaxProgramSize = maxProgramSize;
            return this;
        }

        public ApfStats build() {
            return new ApfStats(this.mDurationMs, this.mReceivedRas, this.mMatchingRas, this.mDroppedRas, this.mZeroLifetimeRas, this.mParseErrors, this.mProgramUpdates, this.mProgramUpdatesAll, this.mProgramUpdatesAllowingMulticast, this.mMaxProgramSize);
        }
    }

    @Override // android.os.Parcelable
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

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public String toString() {
        return "ApfStats(" + String.format("%dms ", Long.valueOf(this.durationMs)) + String.format("%dB RA: {", Integer.valueOf(this.maxProgramSize)) + String.format("%d received, ", Integer.valueOf(this.receivedRas)) + String.format("%d matching, ", Integer.valueOf(this.matchingRas)) + String.format("%d dropped, ", Integer.valueOf(this.droppedRas)) + String.format("%d zero lifetime, ", Integer.valueOf(this.zeroLifetimeRas)) + String.format("%d parse errors}, ", Integer.valueOf(this.parseErrors)) + String.format("updates: {all: %d, RAs: %d, allow multicast: %d})", Integer.valueOf(this.programUpdatesAll), Integer.valueOf(this.programUpdates), Integer.valueOf(this.programUpdatesAllowingMulticast));
    }

    public boolean equals(Object obj) {
        if (obj == null || !obj.getClass().equals(ApfStats.class)) {
            return false;
        }
        ApfStats other = (ApfStats) obj;
        if (this.durationMs == other.durationMs && this.receivedRas == other.receivedRas && this.matchingRas == other.matchingRas && this.droppedRas == other.droppedRas && this.zeroLifetimeRas == other.zeroLifetimeRas && this.parseErrors == other.parseErrors && this.programUpdates == other.programUpdates && this.programUpdatesAll == other.programUpdatesAll && this.programUpdatesAllowingMulticast == other.programUpdatesAllowingMulticast && this.maxProgramSize == other.maxProgramSize) {
            return true;
        }
        return false;
    }
}

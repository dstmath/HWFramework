package android.net.metrics;

import android.annotation.SystemApi;
import android.annotation.UnsupportedAppUsage;
import android.net.metrics.IpConnectivityLog;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.SparseArray;
import com.android.internal.util.MessageUtils;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

@SystemApi
public final class ApfProgramEvent implements IpConnectivityLog.Event {
    public static final Parcelable.Creator<ApfProgramEvent> CREATOR = new Parcelable.Creator<ApfProgramEvent>() {
        /* class android.net.metrics.ApfProgramEvent.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ApfProgramEvent createFromParcel(Parcel in) {
            return new ApfProgramEvent(in);
        }

        @Override // android.os.Parcelable.Creator
        public ApfProgramEvent[] newArray(int size) {
            return new ApfProgramEvent[size];
        }
    };
    public static final int FLAG_HAS_IPV4_ADDRESS = 1;
    public static final int FLAG_MULTICAST_FILTER_ON = 0;
    @UnsupportedAppUsage
    public final long actualLifetime;
    @UnsupportedAppUsage
    public final int currentRas;
    @UnsupportedAppUsage
    public final int filteredRas;
    @UnsupportedAppUsage
    public final int flags;
    @UnsupportedAppUsage
    public final long lifetime;
    @UnsupportedAppUsage
    public final int programLength;

    @Retention(RetentionPolicy.SOURCE)
    public @interface Flags {
    }

    private ApfProgramEvent(long lifetime2, long actualLifetime2, int filteredRas2, int currentRas2, int programLength2, int flags2) {
        this.lifetime = lifetime2;
        this.actualLifetime = actualLifetime2;
        this.filteredRas = filteredRas2;
        this.currentRas = currentRas2;
        this.programLength = programLength2;
        this.flags = flags2;
    }

    private ApfProgramEvent(Parcel in) {
        this.lifetime = in.readLong();
        this.actualLifetime = in.readLong();
        this.filteredRas = in.readInt();
        this.currentRas = in.readInt();
        this.programLength = in.readInt();
        this.flags = in.readInt();
    }

    public static final class Builder {
        private long mActualLifetime;
        private int mCurrentRas;
        private int mFilteredRas;
        private int mFlags;
        private long mLifetime;
        private int mProgramLength;

        public Builder setLifetime(long lifetime) {
            this.mLifetime = lifetime;
            return this;
        }

        public Builder setActualLifetime(long lifetime) {
            this.mActualLifetime = lifetime;
            return this;
        }

        public Builder setFilteredRas(int filteredRas) {
            this.mFilteredRas = filteredRas;
            return this;
        }

        public Builder setCurrentRas(int currentRas) {
            this.mCurrentRas = currentRas;
            return this;
        }

        public Builder setProgramLength(int programLength) {
            this.mProgramLength = programLength;
            return this;
        }

        public Builder setFlags(boolean hasIPv4, boolean multicastFilterOn) {
            this.mFlags = ApfProgramEvent.flagsFor(hasIPv4, multicastFilterOn);
            return this;
        }

        public ApfProgramEvent build() {
            return new ApfProgramEvent(this.mLifetime, this.mActualLifetime, this.mFilteredRas, this.mCurrentRas, this.mProgramLength, this.mFlags);
        }
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags2) {
        out.writeLong(this.lifetime);
        out.writeLong(this.actualLifetime);
        out.writeInt(this.filteredRas);
        out.writeInt(this.currentRas);
        out.writeInt(this.programLength);
        out.writeInt(this.flags);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public String toString() {
        String lifetimeString;
        if (this.lifetime < Long.MAX_VALUE) {
            lifetimeString = this.lifetime + "s";
        } else {
            lifetimeString = "forever";
        }
        return String.format("ApfProgramEvent(%d/%d RAs %dB %ds/%s %s)", Integer.valueOf(this.filteredRas), Integer.valueOf(this.currentRas), Integer.valueOf(this.programLength), Long.valueOf(this.actualLifetime), lifetimeString, namesOf(this.flags));
    }

    public boolean equals(Object obj) {
        if (obj == null || !obj.getClass().equals(ApfProgramEvent.class)) {
            return false;
        }
        ApfProgramEvent other = (ApfProgramEvent) obj;
        if (this.lifetime == other.lifetime && this.actualLifetime == other.actualLifetime && this.filteredRas == other.filteredRas && this.currentRas == other.currentRas && this.programLength == other.programLength && this.flags == other.flags) {
            return true;
        }
        return false;
    }

    @UnsupportedAppUsage
    public static int flagsFor(boolean hasIPv4, boolean multicastFilterOn) {
        int bitfield = 0;
        if (hasIPv4) {
            bitfield = 0 | 2;
        }
        if (multicastFilterOn) {
            return bitfield | 1;
        }
        return bitfield;
    }

    private static String namesOf(int bitfield) {
        List<String> names = new ArrayList<>(Integer.bitCount(bitfield));
        BitSet set = BitSet.valueOf(new long[]{(long) (Integer.MAX_VALUE & bitfield)});
        for (int bit = set.nextSetBit(0); bit >= 0; bit = set.nextSetBit(bit + 1)) {
            names.add(Decoder.constants.get(bit));
        }
        return TextUtils.join("|", names);
    }

    /* access modifiers changed from: package-private */
    public static final class Decoder {
        static final SparseArray<String> constants = MessageUtils.findMessageNames(new Class[]{ApfProgramEvent.class}, new String[]{"FLAG_"});

        Decoder() {
        }
    }
}

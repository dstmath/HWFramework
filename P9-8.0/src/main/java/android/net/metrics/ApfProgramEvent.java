package android.net.metrics;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.TextUtils;
import android.util.SparseArray;
import com.android.internal.util.MessageUtils;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public final class ApfProgramEvent implements Parcelable {
    public static final Creator<ApfProgramEvent> CREATOR = new Creator<ApfProgramEvent>() {
        public ApfProgramEvent createFromParcel(Parcel in) {
            return new ApfProgramEvent(in, null);
        }

        public ApfProgramEvent[] newArray(int size) {
            return new ApfProgramEvent[size];
        }
    };
    public static final int FLAG_HAS_IPV4_ADDRESS = 1;
    public static final int FLAG_MULTICAST_FILTER_ON = 0;
    public long actualLifetime;
    public int currentRas;
    public int filteredRas;
    public int flags;
    public long lifetime;
    public int programLength;

    static final class Decoder {
        static final SparseArray<String> constants = MessageUtils.findMessageNames(new Class[]{ApfProgramEvent.class}, new String[]{"FLAG_"});

        Decoder() {
        }
    }

    /* synthetic */ ApfProgramEvent(Parcel in, ApfProgramEvent -this1) {
        this(in);
    }

    private ApfProgramEvent(Parcel in) {
        this.lifetime = in.readLong();
        this.actualLifetime = in.readLong();
        this.filteredRas = in.readInt();
        this.currentRas = in.readInt();
        this.programLength = in.readInt();
        this.flags = in.readInt();
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(this.lifetime);
        out.writeLong(this.actualLifetime);
        out.writeInt(this.filteredRas);
        out.writeInt(this.currentRas);
        out.writeInt(this.programLength);
        out.writeInt(flags);
    }

    public int describeContents() {
        return 0;
    }

    public String toString() {
        String lifetimeString = this.lifetime < Long.MAX_VALUE ? this.lifetime + "s" : "forever";
        return String.format("ApfProgramEvent(%d/%d RAs %dB %ds/%s %s)", new Object[]{Integer.valueOf(this.filteredRas), Integer.valueOf(this.currentRas), Integer.valueOf(this.programLength), Long.valueOf(this.actualLifetime), lifetimeString, namesOf(this.flags)});
    }

    public static int flagsFor(boolean hasIPv4, boolean multicastFilterOn) {
        int bitfield = 0;
        if (hasIPv4) {
            bitfield = 2;
        }
        if (multicastFilterOn) {
            return bitfield | 1;
        }
        return bitfield;
    }

    private static String namesOf(int bitfield) {
        List<String> names = new ArrayList(Integer.bitCount(bitfield));
        BitSet set = BitSet.valueOf(new long[]{(long) (Integer.MAX_VALUE & bitfield)});
        for (int bit = set.nextSetBit(0); bit >= 0; bit = set.nextSetBit(bit + 1)) {
            names.add((String) Decoder.constants.get(bit));
        }
        return TextUtils.join("|", names);
    }
}

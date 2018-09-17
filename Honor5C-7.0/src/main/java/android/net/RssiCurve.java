package android.net;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.Arrays;
import java.util.Objects;

public class RssiCurve implements Parcelable {
    public static final Creator<RssiCurve> CREATOR = null;
    private static final int DEFAULT_ACTIVE_NETWORK_RSSI_BOOST = 25;
    public final int activeNetworkRssiBoost;
    public final int bucketWidth;
    public final byte[] rssiBuckets;
    public final int start;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.RssiCurve.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.RssiCurve.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.RssiCurve.<clinit>():void");
    }

    public RssiCurve(int start, int bucketWidth, byte[] rssiBuckets) {
        this(start, bucketWidth, rssiBuckets, DEFAULT_ACTIVE_NETWORK_RSSI_BOOST);
    }

    public RssiCurve(int start, int bucketWidth, byte[] rssiBuckets, int activeNetworkRssiBoost) {
        this.start = start;
        this.bucketWidth = bucketWidth;
        if (rssiBuckets == null || rssiBuckets.length == 0) {
            throw new IllegalArgumentException("rssiBuckets must be at least one element large.");
        }
        this.rssiBuckets = rssiBuckets;
        this.activeNetworkRssiBoost = activeNetworkRssiBoost;
    }

    private RssiCurve(Parcel in) {
        this.start = in.readInt();
        this.bucketWidth = in.readInt();
        this.rssiBuckets = new byte[in.readInt()];
        in.readByteArray(this.rssiBuckets);
        this.activeNetworkRssiBoost = in.readInt();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.start);
        out.writeInt(this.bucketWidth);
        out.writeInt(this.rssiBuckets.length);
        out.writeByteArray(this.rssiBuckets);
        out.writeInt(this.activeNetworkRssiBoost);
    }

    public byte lookupScore(int rssi) {
        return lookupScore(rssi, false);
    }

    public byte lookupScore(int rssi, boolean isActiveNetwork) {
        if (isActiveNetwork) {
            rssi += this.activeNetworkRssiBoost;
        }
        int index = (rssi - this.start) / this.bucketWidth;
        if (index < 0) {
            index = 0;
        } else if (index > this.rssiBuckets.length - 1) {
            index = this.rssiBuckets.length - 1;
        }
        return this.rssiBuckets[index];
    }

    public boolean equals(Object o) {
        boolean z = true;
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RssiCurve rssiCurve = (RssiCurve) o;
        if (this.start != rssiCurve.start || this.bucketWidth != rssiCurve.bucketWidth || !Arrays.equals(this.rssiBuckets, rssiCurve.rssiBuckets)) {
            z = false;
        } else if (this.activeNetworkRssiBoost != rssiCurve.activeNetworkRssiBoost) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(this.start), Integer.valueOf(this.bucketWidth), this.rssiBuckets, Integer.valueOf(this.activeNetworkRssiBoost)});
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("RssiCurve[start=").append(this.start).append(",bucketWidth=").append(this.bucketWidth).append(",activeNetworkRssiBoost=").append(this.activeNetworkRssiBoost);
        sb.append(",buckets=");
        for (int i = 0; i < this.rssiBuckets.length; i++) {
            sb.append(this.rssiBuckets[i]);
            if (i < this.rssiBuckets.length - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}

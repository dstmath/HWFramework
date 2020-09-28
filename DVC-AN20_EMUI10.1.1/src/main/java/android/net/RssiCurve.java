package android.net;

import android.annotation.SystemApi;
import android.os.Parcel;
import android.os.Parcelable;
import android.telephony.SmsManager;
import java.util.Arrays;
import java.util.Objects;

@SystemApi
public class RssiCurve implements Parcelable {
    public static final Parcelable.Creator<RssiCurve> CREATOR = new Parcelable.Creator<RssiCurve>() {
        /* class android.net.RssiCurve.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public RssiCurve createFromParcel(Parcel in) {
            return new RssiCurve(in);
        }

        @Override // android.os.Parcelable.Creator
        public RssiCurve[] newArray(int size) {
            return new RssiCurve[size];
        }
    };
    private static final int DEFAULT_ACTIVE_NETWORK_RSSI_BOOST = 25;
    public final int activeNetworkRssiBoost;
    public final int bucketWidth;
    public final byte[] rssiBuckets;
    public final int start;

    public RssiCurve(int start2, int bucketWidth2, byte[] rssiBuckets2) {
        this(start2, bucketWidth2, rssiBuckets2, 25);
    }

    public RssiCurve(int start2, int bucketWidth2, byte[] rssiBuckets2, int activeNetworkRssiBoost2) {
        this.start = start2;
        this.bucketWidth = bucketWidth2;
        if (rssiBuckets2 == null || rssiBuckets2.length == 0) {
            throw new IllegalArgumentException("rssiBuckets must be at least one element large.");
        }
        this.rssiBuckets = rssiBuckets2;
        this.activeNetworkRssiBoost = activeNetworkRssiBoost2;
    }

    private RssiCurve(Parcel in) {
        this.start = in.readInt();
        this.bucketWidth = in.readInt();
        this.rssiBuckets = new byte[in.readInt()];
        in.readByteArray(this.rssiBuckets);
        this.activeNetworkRssiBoost = in.readInt();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
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
        } else {
            byte[] bArr = this.rssiBuckets;
            if (index > bArr.length - 1) {
                index = bArr.length - 1;
            }
        }
        return this.rssiBuckets[index];
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RssiCurve rssiCurve = (RssiCurve) o;
        if (this.start == rssiCurve.start && this.bucketWidth == rssiCurve.bucketWidth && Arrays.equals(this.rssiBuckets, rssiCurve.rssiBuckets) && this.activeNetworkRssiBoost == rssiCurve.activeNetworkRssiBoost) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return Objects.hash(Integer.valueOf(this.start), Integer.valueOf(this.bucketWidth), Integer.valueOf(this.activeNetworkRssiBoost)) ^ Arrays.hashCode(this.rssiBuckets);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("RssiCurve[start=");
        sb.append(this.start);
        sb.append(",bucketWidth=");
        sb.append(this.bucketWidth);
        sb.append(",activeNetworkRssiBoost=");
        sb.append(this.activeNetworkRssiBoost);
        sb.append(",buckets=");
        int i = 0;
        while (true) {
            byte[] bArr = this.rssiBuckets;
            if (i < bArr.length) {
                sb.append((int) bArr[i]);
                if (i < this.rssiBuckets.length - 1) {
                    sb.append(SmsManager.REGEX_PREFIX_DELIMITER);
                }
                i++;
            } else {
                sb.append("]");
                return sb.toString();
            }
        }
    }
}

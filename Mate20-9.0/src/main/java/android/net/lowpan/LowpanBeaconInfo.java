package android.net.lowpan;

import android.net.lowpan.LowpanIdentity;
import android.os.Parcel;
import android.os.Parcelable;
import com.android.internal.util.HexDump;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.TreeSet;

public class LowpanBeaconInfo implements Parcelable {
    public static final Parcelable.Creator<LowpanBeaconInfo> CREATOR = new Parcelable.Creator<LowpanBeaconInfo>() {
        public LowpanBeaconInfo createFromParcel(Parcel in) {
            Builder builder = new Builder();
            builder.setLowpanIdentity(LowpanIdentity.CREATOR.createFromParcel(in));
            builder.setRssi(in.readInt());
            builder.setLqi(in.readInt());
            builder.setBeaconAddress(in.createByteArray());
            for (int i = in.readInt(); i > 0; i--) {
                builder.setFlag(in.readInt());
            }
            return builder.build();
        }

        public LowpanBeaconInfo[] newArray(int size) {
            return new LowpanBeaconInfo[size];
        }
    };
    public static final int FLAG_CAN_ASSIST = 1;
    public static final int UNKNOWN_LQI = 0;
    public static final int UNKNOWN_RSSI = Integer.MAX_VALUE;
    /* access modifiers changed from: private */
    public byte[] mBeaconAddress;
    /* access modifiers changed from: private */
    public final TreeSet<Integer> mFlags;
    /* access modifiers changed from: private */
    public LowpanIdentity mIdentity;
    /* access modifiers changed from: private */
    public int mLqi;
    /* access modifiers changed from: private */
    public int mRssi;

    public static class Builder {
        final LowpanBeaconInfo mBeaconInfo = new LowpanBeaconInfo();
        final LowpanIdentity.Builder mIdentityBuilder = new LowpanIdentity.Builder();

        public Builder setLowpanIdentity(LowpanIdentity x) {
            this.mIdentityBuilder.setLowpanIdentity(x);
            return this;
        }

        public Builder setName(String x) {
            this.mIdentityBuilder.setName(x);
            return this;
        }

        public Builder setXpanid(byte[] x) {
            this.mIdentityBuilder.setXpanid(x);
            return this;
        }

        public Builder setPanid(int x) {
            this.mIdentityBuilder.setPanid(x);
            return this;
        }

        public Builder setChannel(int x) {
            this.mIdentityBuilder.setChannel(x);
            return this;
        }

        public Builder setType(String x) {
            this.mIdentityBuilder.setType(x);
            return this;
        }

        public Builder setRssi(int x) {
            int unused = this.mBeaconInfo.mRssi = x;
            return this;
        }

        public Builder setLqi(int x) {
            int unused = this.mBeaconInfo.mLqi = x;
            return this;
        }

        public Builder setBeaconAddress(byte[] x) {
            byte[] unused = this.mBeaconInfo.mBeaconAddress = x != null ? (byte[]) x.clone() : null;
            return this;
        }

        public Builder setFlag(int x) {
            this.mBeaconInfo.mFlags.add(Integer.valueOf(x));
            return this;
        }

        public Builder setFlags(Collection<Integer> x) {
            this.mBeaconInfo.mFlags.addAll(x);
            return this;
        }

        public LowpanBeaconInfo build() {
            LowpanIdentity unused = this.mBeaconInfo.mIdentity = this.mIdentityBuilder.build();
            if (this.mBeaconInfo.mBeaconAddress == null) {
                byte[] unused2 = this.mBeaconInfo.mBeaconAddress = new byte[0];
            }
            return this.mBeaconInfo;
        }
    }

    private LowpanBeaconInfo() {
        this.mRssi = Integer.MAX_VALUE;
        this.mLqi = 0;
        this.mBeaconAddress = null;
        this.mFlags = new TreeSet<>();
    }

    public LowpanIdentity getLowpanIdentity() {
        return this.mIdentity;
    }

    public int getRssi() {
        return this.mRssi;
    }

    public int getLqi() {
        return this.mLqi;
    }

    public byte[] getBeaconAddress() {
        return (byte[]) this.mBeaconAddress.clone();
    }

    public Collection<Integer> getFlags() {
        return (Collection) this.mFlags.clone();
    }

    public boolean isFlagSet(int flag) {
        return this.mFlags.contains(Integer.valueOf(flag));
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.mIdentity.toString());
        if (this.mRssi != Integer.MAX_VALUE) {
            sb.append(", RSSI:");
            sb.append(this.mRssi);
            sb.append("dBm");
        }
        if (this.mLqi != 0) {
            sb.append(", LQI:");
            sb.append(this.mLqi);
        }
        if (this.mBeaconAddress.length > 0) {
            sb.append(", BeaconAddress:");
            sb.append(HexDump.toHexString(this.mBeaconAddress));
        }
        Iterator<Integer> it = this.mFlags.iterator();
        while (it.hasNext()) {
            Integer flag = it.next();
            if (flag.intValue() != 1) {
                sb.append(", FLAG_");
                sb.append(Integer.toHexString(flag.intValue()));
            } else {
                sb.append(", CAN_ASSIST");
            }
        }
        return sb.toString();
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.mIdentity, Integer.valueOf(this.mRssi), Integer.valueOf(this.mLqi), Integer.valueOf(Arrays.hashCode(this.mBeaconAddress)), this.mFlags});
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (!(obj instanceof LowpanBeaconInfo)) {
            return false;
        }
        LowpanBeaconInfo rhs = (LowpanBeaconInfo) obj;
        if (this.mIdentity.equals(rhs.mIdentity) && Arrays.equals(this.mBeaconAddress, rhs.mBeaconAddress) && this.mRssi == rhs.mRssi && this.mLqi == rhs.mLqi && this.mFlags.equals(rhs.mFlags)) {
            z = true;
        }
        return z;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        this.mIdentity.writeToParcel(dest, flags);
        dest.writeInt(this.mRssi);
        dest.writeInt(this.mLqi);
        dest.writeByteArray(this.mBeaconAddress);
        dest.writeInt(this.mFlags.size());
        Iterator<Integer> it = this.mFlags.iterator();
        while (it.hasNext()) {
            dest.writeInt(it.next().intValue());
        }
    }
}

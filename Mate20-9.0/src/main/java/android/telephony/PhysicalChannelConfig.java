package android.telephony;

import android.os.Parcel;
import android.os.Parcelable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public final class PhysicalChannelConfig implements Parcelable {
    public static final int CONNECTION_PRIMARY_SERVING = 1;
    public static final int CONNECTION_SECONDARY_SERVING = 2;
    public static final int CONNECTION_UNKNOWN = Integer.MAX_VALUE;
    public static final Parcelable.Creator<PhysicalChannelConfig> CREATOR = new Parcelable.Creator<PhysicalChannelConfig>() {
        public PhysicalChannelConfig createFromParcel(Parcel in) {
            return new PhysicalChannelConfig(in);
        }

        public PhysicalChannelConfig[] newArray(int size) {
            return new PhysicalChannelConfig[size];
        }
    };
    private int mCellBandwidthDownlinkKhz;
    private int mCellConnectionStatus;

    @Retention(RetentionPolicy.SOURCE)
    public @interface ConnectionStatus {
    }

    public PhysicalChannelConfig(int status, int bandwidth) {
        this.mCellConnectionStatus = status;
        this.mCellBandwidthDownlinkKhz = bandwidth;
    }

    public PhysicalChannelConfig(Parcel in) {
        this.mCellConnectionStatus = in.readInt();
        this.mCellBandwidthDownlinkKhz = in.readInt();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mCellConnectionStatus);
        dest.writeInt(this.mCellBandwidthDownlinkKhz);
    }

    public int getCellBandwidthDownlink() {
        return this.mCellBandwidthDownlinkKhz;
    }

    public int getConnectionStatus() {
        return this.mCellConnectionStatus;
    }

    private String getConnectionStatusString() {
        int i = this.mCellConnectionStatus;
        if (i == Integer.MAX_VALUE) {
            return "Unknown";
        }
        switch (i) {
            case 1:
                return "PrimaryServing";
            case 2:
                return "SecondaryServing";
            default:
                return "Invalid(" + this.mCellConnectionStatus + ")";
        }
    }

    public boolean equals(Object o) {
        boolean z = true;
        if (this == o) {
            return true;
        }
        if (!(o instanceof PhysicalChannelConfig)) {
            return false;
        }
        PhysicalChannelConfig config = (PhysicalChannelConfig) o;
        if (!(this.mCellConnectionStatus == config.mCellConnectionStatus && this.mCellBandwidthDownlinkKhz == config.mCellBandwidthDownlinkKhz)) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return (this.mCellBandwidthDownlinkKhz * 29) + (this.mCellConnectionStatus * 31);
    }

    public String toString() {
        return "{mConnectionStatus=" + getConnectionStatusString() + ",mCellBandwidthDownlinkKhz=" + this.mCellBandwidthDownlinkKhz + "}";
    }
}

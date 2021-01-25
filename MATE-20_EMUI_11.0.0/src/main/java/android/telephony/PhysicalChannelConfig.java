package android.telephony;

import android.os.Parcel;
import android.os.Parcelable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.Objects;

public final class PhysicalChannelConfig implements Parcelable {
    public static final int CONNECTION_PRIMARY_SERVING = 1;
    public static final int CONNECTION_SECONDARY_SERVING = 2;
    public static final int CONNECTION_UNKNOWN = Integer.MAX_VALUE;
    public static final Parcelable.Creator<PhysicalChannelConfig> CREATOR = new Parcelable.Creator<PhysicalChannelConfig>() {
        /* class android.telephony.PhysicalChannelConfig.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public PhysicalChannelConfig createFromParcel(Parcel in) {
            return new PhysicalChannelConfig(in);
        }

        @Override // android.os.Parcelable.Creator
        public PhysicalChannelConfig[] newArray(int size) {
            return new PhysicalChannelConfig[size];
        }
    };
    private int mCellBandwidthDownlinkKhz;
    private int mCellConnectionStatus;
    private int mChannelNumber;
    private int[] mContextIds;
    private int mFrequencyRange;
    private int mPhysicalCellId;
    private int mRat;

    @Retention(RetentionPolicy.SOURCE)
    public @interface ConnectionStatus {
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mCellConnectionStatus);
        dest.writeInt(this.mCellBandwidthDownlinkKhz);
        dest.writeInt(this.mRat);
        dest.writeInt(this.mChannelNumber);
        dest.writeInt(this.mFrequencyRange);
        dest.writeIntArray(this.mContextIds);
        dest.writeInt(this.mPhysicalCellId);
    }

    public int getCellBandwidthDownlink() {
        return this.mCellBandwidthDownlinkKhz;
    }

    public int[] getContextIds() {
        return this.mContextIds;
    }

    public int getFrequencyRange() {
        return this.mFrequencyRange;
    }

    public int getChannelNumber() {
        return this.mChannelNumber;
    }

    public int getPhysicalCellId() {
        return this.mPhysicalCellId;
    }

    public int getRat() {
        return this.mRat;
    }

    public int getConnectionStatus() {
        return this.mCellConnectionStatus;
    }

    private String getConnectionStatusString() {
        int i = this.mCellConnectionStatus;
        if (i == 1) {
            return "PrimaryServing";
        }
        if (i == 2) {
            return "SecondaryServing";
        }
        if (i == Integer.MAX_VALUE) {
            return "Unknown";
        }
        return "Invalid(" + this.mCellConnectionStatus + ")";
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PhysicalChannelConfig)) {
            return false;
        }
        PhysicalChannelConfig config = (PhysicalChannelConfig) o;
        if (this.mCellConnectionStatus == config.mCellConnectionStatus && this.mCellBandwidthDownlinkKhz == config.mCellBandwidthDownlinkKhz && this.mRat == config.mRat && this.mFrequencyRange == config.mFrequencyRange && this.mChannelNumber == config.mChannelNumber && this.mPhysicalCellId == config.mPhysicalCellId && Arrays.equals(this.mContextIds, config.mContextIds)) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return Objects.hash(Integer.valueOf(this.mCellConnectionStatus), Integer.valueOf(this.mCellBandwidthDownlinkKhz), Integer.valueOf(this.mRat), Integer.valueOf(this.mFrequencyRange), Integer.valueOf(this.mChannelNumber), Integer.valueOf(this.mPhysicalCellId), this.mContextIds);
    }

    public String toString() {
        return "{mConnectionStatus=" + getConnectionStatusString() + ",mCellBandwidthDownlinkKhz=" + this.mCellBandwidthDownlinkKhz + ",mRat=" + this.mRat + ",mFrequencyRange=" + this.mFrequencyRange + ",mChannelNumber=" + this.mChannelNumber + ",mContextIds=" + this.mContextIds.toString() + ",mPhysicalCellId=" + this.mPhysicalCellId + "}";
    }

    private PhysicalChannelConfig(Parcel in) {
        this.mCellConnectionStatus = in.readInt();
        this.mCellBandwidthDownlinkKhz = in.readInt();
        this.mRat = in.readInt();
        this.mChannelNumber = in.readInt();
        this.mFrequencyRange = in.readInt();
        this.mContextIds = in.createIntArray();
        this.mPhysicalCellId = in.readInt();
    }

    private PhysicalChannelConfig(Builder builder) {
        this.mCellConnectionStatus = builder.mCellConnectionStatus;
        this.mCellBandwidthDownlinkKhz = builder.mCellBandwidthDownlinkKhz;
        this.mRat = builder.mRat;
        this.mChannelNumber = builder.mChannelNumber;
        this.mFrequencyRange = builder.mFrequencyRange;
        this.mContextIds = builder.mContextIds;
        this.mPhysicalCellId = builder.mPhysicalCellId;
    }

    public static final class Builder {
        private int mCellBandwidthDownlinkKhz = 0;
        private int mCellConnectionStatus = Integer.MAX_VALUE;
        private int mChannelNumber = Integer.MAX_VALUE;
        private int[] mContextIds = new int[0];
        private int mFrequencyRange = -1;
        private int mPhysicalCellId = Integer.MAX_VALUE;
        private int mRat = 0;

        public PhysicalChannelConfig build() {
            return new PhysicalChannelConfig(this);
        }

        public Builder setRat(int rat) {
            this.mRat = rat;
            return this;
        }

        public Builder setFrequencyRange(int frequencyRange) {
            this.mFrequencyRange = frequencyRange;
            return this;
        }

        public Builder setChannelNumber(int channelNumber) {
            this.mChannelNumber = channelNumber;
            return this;
        }

        public Builder setCellBandwidthDownlinkKhz(int cellBandwidthDownlinkKhz) {
            this.mCellBandwidthDownlinkKhz = cellBandwidthDownlinkKhz;
            return this;
        }

        public Builder setCellConnectionStatus(int connectionStatus) {
            this.mCellConnectionStatus = connectionStatus;
            return this;
        }

        public Builder setContextIds(int[] contextIds) {
            if (contextIds != null) {
                Arrays.sort(contextIds);
            }
            this.mContextIds = contextIds;
            return this;
        }

        public Builder setPhysicalCellId(int physicalCellId) {
            this.mPhysicalCellId = physicalCellId;
            return this;
        }
    }
}

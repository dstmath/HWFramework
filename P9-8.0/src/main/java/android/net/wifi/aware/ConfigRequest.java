package android.net.wifi.aware;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.Arrays;

public final class ConfigRequest implements Parcelable {
    public static final int CLUSTER_ID_MAX = 65535;
    public static final int CLUSTER_ID_MIN = 0;
    public static final Creator<ConfigRequest> CREATOR = new Creator<ConfigRequest>() {
        public ConfigRequest[] newArray(int size) {
            return new ConfigRequest[size];
        }

        public ConfigRequest createFromParcel(Parcel in) {
            return new ConfigRequest(in.readInt() != 0, in.readInt(), in.readInt(), in.readInt(), in.createIntArray(), null);
        }
    };
    public static final int DW_DISABLE = 0;
    public static final int DW_INTERVAL_NOT_INIT = -1;
    public static final int NAN_BAND_24GHZ = 0;
    public static final int NAN_BAND_5GHZ = 1;
    public final int mClusterHigh;
    public final int mClusterLow;
    public final int[] mDiscoveryWindowInterval;
    public final int mMasterPreference;
    public final boolean mSupport5gBand;

    public static final class Builder {
        private int mClusterHigh = 65535;
        private int mClusterLow = 0;
        private int[] mDiscoveryWindowInterval = new int[]{-1, -1};
        private int mMasterPreference = 0;
        private boolean mSupport5gBand = false;

        public Builder setSupport5gBand(boolean support5gBand) {
            this.mSupport5gBand = support5gBand;
            return this;
        }

        public Builder setMasterPreference(int masterPreference) {
            if (masterPreference < 0) {
                throw new IllegalArgumentException("Master Preference specification must be non-negative");
            } else if (masterPreference == 1 || masterPreference == 255 || masterPreference > 255) {
                throw new IllegalArgumentException("Master Preference specification must not exceed 255 or use 1 or 255 (reserved values)");
            } else {
                this.mMasterPreference = masterPreference;
                return this;
            }
        }

        public Builder setClusterLow(int clusterLow) {
            if (clusterLow < 0) {
                throw new IllegalArgumentException("Cluster specification must be non-negative");
            } else if (clusterLow > 65535) {
                throw new IllegalArgumentException("Cluster specification must not exceed 0xFFFF");
            } else {
                this.mClusterLow = clusterLow;
                return this;
            }
        }

        public Builder setClusterHigh(int clusterHigh) {
            if (clusterHigh < 0) {
                throw new IllegalArgumentException("Cluster specification must be non-negative");
            } else if (clusterHigh > 65535) {
                throw new IllegalArgumentException("Cluster specification must not exceed 0xFFFF");
            } else {
                this.mClusterHigh = clusterHigh;
                return this;
            }
        }

        public Builder setDiscoveryWindowInterval(int band, int interval) {
            if (band != 0 && band != 1) {
                throw new IllegalArgumentException("Invalid band value");
            } else if ((band != 0 || (interval >= 1 && interval <= 5)) && (band != 1 || (interval >= 0 && interval <= 5))) {
                this.mDiscoveryWindowInterval[band] = interval;
                return this;
            } else {
                throw new IllegalArgumentException("Invalid interval value: 2.4 GHz [1,5] or 5GHz [0,5]");
            }
        }

        public ConfigRequest build() {
            if (this.mClusterLow <= this.mClusterHigh) {
                return new ConfigRequest(this.mSupport5gBand, this.mMasterPreference, this.mClusterLow, this.mClusterHigh, this.mDiscoveryWindowInterval, null);
            }
            throw new IllegalArgumentException("Invalid argument combination - must have Cluster Low <= Cluster High");
        }
    }

    /* synthetic */ ConfigRequest(boolean support5gBand, int masterPreference, int clusterLow, int clusterHigh, int[] discoveryWindowInterval, ConfigRequest -this5) {
        this(support5gBand, masterPreference, clusterLow, clusterHigh, discoveryWindowInterval);
    }

    private ConfigRequest(boolean support5gBand, int masterPreference, int clusterLow, int clusterHigh, int[] discoveryWindowInterval) {
        this.mSupport5gBand = support5gBand;
        this.mMasterPreference = masterPreference;
        this.mClusterLow = clusterLow;
        this.mClusterHigh = clusterHigh;
        this.mDiscoveryWindowInterval = discoveryWindowInterval;
    }

    public String toString() {
        return "ConfigRequest [mSupport5gBand=" + this.mSupport5gBand + ", mMasterPreference=" + this.mMasterPreference + ", mClusterLow=" + this.mClusterLow + ", mClusterHigh=" + this.mClusterHigh + ", mDiscoveryWindowInterval=" + Arrays.toString(this.mDiscoveryWindowInterval) + "]";
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mSupport5gBand ? 1 : 0);
        dest.writeInt(this.mMasterPreference);
        dest.writeInt(this.mClusterLow);
        dest.writeInt(this.mClusterHigh);
        dest.writeIntArray(this.mDiscoveryWindowInterval);
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (this == o) {
            return true;
        }
        if (!(o instanceof ConfigRequest)) {
            return false;
        }
        ConfigRequest lhs = (ConfigRequest) o;
        if (this.mSupport5gBand == lhs.mSupport5gBand && this.mMasterPreference == lhs.mMasterPreference && this.mClusterLow == lhs.mClusterLow && this.mClusterHigh == lhs.mClusterHigh) {
            z = Arrays.equals(this.mDiscoveryWindowInterval, lhs.mDiscoveryWindowInterval);
        }
        return z;
    }

    public int hashCode() {
        return (((((((((this.mSupport5gBand ? 1 : 0) + 527) * 31) + this.mMasterPreference) * 31) + this.mClusterLow) * 31) + this.mClusterHigh) * 31) + Arrays.hashCode(this.mDiscoveryWindowInterval);
    }

    public void validate() throws IllegalArgumentException {
        if (this.mMasterPreference < 0) {
            throw new IllegalArgumentException("Master Preference specification must be non-negative");
        } else if (this.mMasterPreference == 1 || this.mMasterPreference == 255 || this.mMasterPreference > 255) {
            throw new IllegalArgumentException("Master Preference specification must not exceed 255 or use 1 or 255 (reserved values)");
        } else if (this.mClusterLow < 0) {
            throw new IllegalArgumentException("Cluster specification must be non-negative");
        } else if (this.mClusterLow > 65535) {
            throw new IllegalArgumentException("Cluster specification must not exceed 0xFFFF");
        } else if (this.mClusterHigh < 0) {
            throw new IllegalArgumentException("Cluster specification must be non-negative");
        } else if (this.mClusterHigh > 65535) {
            throw new IllegalArgumentException("Cluster specification must not exceed 0xFFFF");
        } else if (this.mClusterLow > this.mClusterHigh) {
            throw new IllegalArgumentException("Invalid argument combination - must have Cluster Low <= Cluster High");
        } else if (this.mDiscoveryWindowInterval.length != 2) {
            throw new IllegalArgumentException("Invalid discovery window interval: must have 2 elements (2.4 & 5");
        } else if (this.mDiscoveryWindowInterval[0] != -1 && (this.mDiscoveryWindowInterval[0] < 1 || this.mDiscoveryWindowInterval[0] > 5)) {
            throw new IllegalArgumentException("Invalid discovery window interval for 2.4GHz: valid is UNSET or [1,5]");
        } else if (this.mDiscoveryWindowInterval[1] == -1) {
        } else {
            if (this.mDiscoveryWindowInterval[1] < 0 || this.mDiscoveryWindowInterval[1] > 5) {
                throw new IllegalArgumentException("Invalid discovery window interval for 5GHz: valid is UNSET or [0,5]");
            }
        }
    }
}

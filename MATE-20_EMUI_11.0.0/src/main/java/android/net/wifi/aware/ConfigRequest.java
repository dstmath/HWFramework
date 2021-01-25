package android.net.wifi.aware;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Arrays;

public final class ConfigRequest implements Parcelable {
    public static final int CLUSTER_ID_MAX = 65535;
    public static final int CLUSTER_ID_MIN = 0;
    public static final Parcelable.Creator<ConfigRequest> CREATOR = new Parcelable.Creator<ConfigRequest>() {
        /* class android.net.wifi.aware.ConfigRequest.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ConfigRequest[] newArray(int size) {
            return new ConfigRequest[size];
        }

        @Override // android.os.Parcelable.Creator
        public ConfigRequest createFromParcel(Parcel in) {
            return new ConfigRequest(in.readInt() != 0, in.readInt(), in.readInt(), in.readInt(), in.createIntArray());
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

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mSupport5gBand ? 1 : 0);
        dest.writeInt(this.mMasterPreference);
        dest.writeInt(this.mClusterLow);
        dest.writeInt(this.mClusterHigh);
        dest.writeIntArray(this.mDiscoveryWindowInterval);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ConfigRequest)) {
            return false;
        }
        ConfigRequest lhs = (ConfigRequest) o;
        if (this.mSupport5gBand == lhs.mSupport5gBand && this.mMasterPreference == lhs.mMasterPreference && this.mClusterLow == lhs.mClusterLow && this.mClusterHigh == lhs.mClusterHigh && Arrays.equals(this.mDiscoveryWindowInterval, lhs.mDiscoveryWindowInterval)) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return (((((((((17 * 31) + (this.mSupport5gBand ? 1 : 0)) * 31) + this.mMasterPreference) * 31) + this.mClusterLow) * 31) + this.mClusterHigh) * 31) + Arrays.hashCode(this.mDiscoveryWindowInterval);
    }

    public void validate() throws IllegalArgumentException {
        int i = this.mMasterPreference;
        if (i < 0) {
            throw new IllegalArgumentException("Master Preference specification must be non-negative");
        } else if (i == 1 || i == 255 || i > 255) {
            throw new IllegalArgumentException("Master Preference specification must not exceed 255 or use 1 or 255 (reserved values)");
        } else {
            int i2 = this.mClusterLow;
            if (i2 < 0) {
                throw new IllegalArgumentException("Cluster specification must be non-negative");
            } else if (i2 <= 65535) {
                int i3 = this.mClusterHigh;
                if (i3 < 0) {
                    throw new IllegalArgumentException("Cluster specification must be non-negative");
                } else if (i3 > 65535) {
                    throw new IllegalArgumentException("Cluster specification must not exceed 0xFFFF");
                } else if (i2 <= i3) {
                    int[] iArr = this.mDiscoveryWindowInterval;
                    if (iArr.length != 2) {
                        throw new IllegalArgumentException("Invalid discovery window interval: must have 2 elements (2.4 & 5");
                    } else if (iArr[0] == -1 || (iArr[0] >= 1 && iArr[0] <= 5)) {
                        int[] iArr2 = this.mDiscoveryWindowInterval;
                        if (iArr2[1] == -1) {
                            return;
                        }
                        if (iArr2[1] < 0 || iArr2[1] > 5) {
                            throw new IllegalArgumentException("Invalid discovery window interval for 5GHz: valid is UNSET or [0,5]");
                        }
                    } else {
                        throw new IllegalArgumentException("Invalid discovery window interval for 2.4GHz: valid is UNSET or [1,5]");
                    }
                } else {
                    throw new IllegalArgumentException("Invalid argument combination - must have Cluster Low <= Cluster High");
                }
            } else {
                throw new IllegalArgumentException("Cluster specification must not exceed 0xFFFF");
            }
        }
    }

    public static final class Builder {
        private int mClusterHigh = 65535;
        private int mClusterLow = 0;
        private int[] mDiscoveryWindowInterval = {-1, -1};
        private int mMasterPreference = 0;
        private boolean mSupport5gBand = true;

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
            } else if (clusterLow <= 65535) {
                this.mClusterLow = clusterLow;
                return this;
            } else {
                throw new IllegalArgumentException("Cluster specification must not exceed 0xFFFF");
            }
        }

        public Builder setClusterHigh(int clusterHigh) {
            if (clusterHigh < 0) {
                throw new IllegalArgumentException("Cluster specification must be non-negative");
            } else if (clusterHigh <= 65535) {
                this.mClusterHigh = clusterHigh;
                return this;
            } else {
                throw new IllegalArgumentException("Cluster specification must not exceed 0xFFFF");
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
            int i = this.mClusterLow;
            int i2 = this.mClusterHigh;
            if (i <= i2) {
                return new ConfigRequest(this.mSupport5gBand, this.mMasterPreference, i, i2, this.mDiscoveryWindowInterval);
            }
            throw new IllegalArgumentException("Invalid argument combination - must have Cluster Low <= Cluster High");
        }
    }
}

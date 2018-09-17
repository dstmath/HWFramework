package android.net.wifi.aware;

import android.net.wifi.aware.TlvBufferUtils.TlvConstructor;
import android.net.wifi.aware.TlvBufferUtils.TlvIterable;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import libcore.util.HexEncoding;

public final class SubscribeConfig implements Parcelable {
    public static final Creator<SubscribeConfig> CREATOR = new Creator<SubscribeConfig>() {
        public SubscribeConfig[] newArray(int size) {
            return new SubscribeConfig[size];
        }

        public SubscribeConfig createFromParcel(Parcel in) {
            return new SubscribeConfig(in.createByteArray(), in.createByteArray(), in.createByteArray(), in.readInt(), in.readInt(), in.readInt() != 0);
        }
    };
    public static final int SUBSCRIBE_TYPE_ACTIVE = 1;
    public static final int SUBSCRIBE_TYPE_PASSIVE = 0;
    public final boolean mEnableTerminateNotification;
    public final byte[] mMatchFilter;
    public final byte[] mServiceName;
    public final byte[] mServiceSpecificInfo;
    public final int mSubscribeType;
    public final int mTtlSec;

    public static final class Builder {
        private boolean mEnableTerminateNotification = true;
        private byte[] mMatchFilter;
        private byte[] mServiceName;
        private byte[] mServiceSpecificInfo;
        private int mSubscribeType = 0;
        private int mTtlSec = 0;

        public Builder setServiceName(String serviceName) {
            if (serviceName == null) {
                throw new IllegalArgumentException("Invalid service name - must be non-null");
            }
            this.mServiceName = serviceName.getBytes(StandardCharsets.UTF_8);
            return this;
        }

        public Builder setServiceSpecificInfo(byte[] serviceSpecificInfo) {
            this.mServiceSpecificInfo = serviceSpecificInfo;
            return this;
        }

        public Builder setMatchFilter(List<byte[]> matchFilter) {
            this.mMatchFilter = new TlvConstructor(0, 1).allocateAndPut(matchFilter).getArray();
            return this;
        }

        public Builder setSubscribeType(int subscribeType) {
            if (subscribeType < 0 || subscribeType > 1) {
                throw new IllegalArgumentException("Invalid subscribeType - " + subscribeType);
            }
            this.mSubscribeType = subscribeType;
            return this;
        }

        public Builder setTtlSec(int ttlSec) {
            if (ttlSec < 0) {
                throw new IllegalArgumentException("Invalid ttlSec - must be non-negative");
            }
            this.mTtlSec = ttlSec;
            return this;
        }

        public Builder setTerminateNotificationEnabled(boolean enable) {
            this.mEnableTerminateNotification = enable;
            return this;
        }

        public SubscribeConfig build() {
            return new SubscribeConfig(this.mServiceName, this.mServiceSpecificInfo, this.mMatchFilter, this.mSubscribeType, this.mTtlSec, this.mEnableTerminateNotification);
        }
    }

    public SubscribeConfig(byte[] serviceName, byte[] serviceSpecificInfo, byte[] matchFilter, int subscribeType, int ttlSec, boolean enableTerminateNotification) {
        this.mServiceName = serviceName;
        this.mServiceSpecificInfo = serviceSpecificInfo;
        this.mMatchFilter = matchFilter;
        this.mSubscribeType = subscribeType;
        this.mTtlSec = ttlSec;
        this.mEnableTerminateNotification = enableTerminateNotification;
    }

    public String toString() {
        return "SubscribeConfig [mServiceName='" + this.mServiceName + ", mServiceSpecificInfo='" + (this.mServiceSpecificInfo == null ? "null" : HexEncoding.encode(this.mServiceSpecificInfo)) + ", mMatchFilter=" + new TlvIterable(0, 1, this.mMatchFilter).toString() + ", mSubscribeType=" + this.mSubscribeType + ", mTtlSec=" + this.mTtlSec + ", mEnableTerminateNotification=" + this.mEnableTerminateNotification + "]";
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByteArray(this.mServiceName);
        dest.writeByteArray(this.mServiceSpecificInfo);
        dest.writeByteArray(this.mMatchFilter);
        dest.writeInt(this.mSubscribeType);
        dest.writeInt(this.mTtlSec);
        dest.writeInt(this.mEnableTerminateNotification ? 1 : 0);
    }

    public boolean equals(Object o) {
        boolean z = true;
        if (this == o) {
            return true;
        }
        if (!(o instanceof SubscribeConfig)) {
            return false;
        }
        SubscribeConfig lhs = (SubscribeConfig) o;
        if (!Arrays.equals(this.mServiceName, lhs.mServiceName) || !Arrays.equals(this.mServiceSpecificInfo, lhs.mServiceSpecificInfo) || !Arrays.equals(this.mMatchFilter, lhs.mMatchFilter) || this.mSubscribeType != lhs.mSubscribeType || this.mTtlSec != lhs.mTtlSec) {
            z = false;
        } else if (this.mEnableTerminateNotification != lhs.mEnableTerminateNotification) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return ((((((((((Arrays.hashCode(this.mServiceName) + 527) * 31) + Arrays.hashCode(this.mServiceSpecificInfo)) * 31) + Arrays.hashCode(this.mMatchFilter)) * 31) + this.mSubscribeType) * 31) + this.mTtlSec) * 31) + (this.mEnableTerminateNotification ? 1 : 0);
    }

    public void assertValid(Characteristics characteristics) throws IllegalArgumentException {
        WifiAwareUtils.validateServiceName(this.mServiceName);
        if (!TlvBufferUtils.isValid(this.mMatchFilter, 0, 1)) {
            throw new IllegalArgumentException("Invalid matchFilter configuration - LV fields do not match up to length");
        } else if (this.mSubscribeType < 0 || this.mSubscribeType > 1) {
            throw new IllegalArgumentException("Invalid subscribeType - " + this.mSubscribeType);
        } else if (this.mTtlSec < 0) {
            throw new IllegalArgumentException("Invalid ttlSec - must be non-negative");
        } else if (characteristics != null) {
            int maxServiceNameLength = characteristics.getMaxServiceNameLength();
            if (maxServiceNameLength == 0 || this.mServiceName.length <= maxServiceNameLength) {
                int maxServiceSpecificInfoLength = characteristics.getMaxServiceSpecificInfoLength();
                if (maxServiceSpecificInfoLength == 0 || this.mServiceSpecificInfo == null || this.mServiceSpecificInfo.length <= maxServiceSpecificInfoLength) {
                    int maxMatchFilterLength = characteristics.getMaxMatchFilterLength();
                    if (maxMatchFilterLength != 0 && this.mMatchFilter != null && this.mMatchFilter.length > maxMatchFilterLength) {
                        throw new IllegalArgumentException("Match filter longer than supported by device characteristics");
                    }
                    return;
                }
                throw new IllegalArgumentException("Service specific info longer than supported by device characteristics");
            }
            throw new IllegalArgumentException("Service name longer than supported by device characteristics");
        }
    }
}

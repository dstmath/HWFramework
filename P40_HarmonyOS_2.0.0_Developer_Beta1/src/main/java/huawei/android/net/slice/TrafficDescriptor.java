package huawei.android.net.slice;

import android.os.Parcel;
import android.os.Parcelable;
import java.net.InetAddress;

public class TrafficDescriptor implements Parcelable {
    public static final Parcelable.Creator<TrafficDescriptor> CREATOR = new Parcelable.Creator<TrafficDescriptor>() {
        /* class huawei.android.net.slice.TrafficDescriptor.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public TrafficDescriptor createFromParcel(Parcel source) {
            return new TrafficDescriptor(source);
        }

        @Override // android.os.Parcelable.Creator
        public TrafficDescriptor[] newArray(int size) {
            return new TrafficDescriptor[size];
        }
    };
    private final String mAppId;
    private final int mConnectionCapability;
    private final String mDnn;
    private final String mFqdn;
    private final InetAddress mIp;
    private final int mProtocolId;
    private final String mRemotePort;

    public TrafficDescriptor(Builder builder) {
        this.mAppId = builder.mAppId;
        this.mDnn = builder.mDnn;
        this.mFqdn = builder.mFqdn;
        this.mIp = builder.mIp;
        this.mProtocolId = builder.mProtocolId;
        this.mRemotePort = builder.mRemotePort;
        this.mConnectionCapability = builder.mConnectionCapability;
    }

    public TrafficDescriptor(Parcel parcel) {
        this.mAppId = parcel.readString();
        this.mDnn = parcel.readString();
        this.mFqdn = parcel.readString();
        this.mIp = (InetAddress) parcel.readSerializable();
        this.mProtocolId = parcel.readInt();
        this.mRemotePort = parcel.readString();
        this.mConnectionCapability = parcel.readInt();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mAppId);
        dest.writeString(this.mDnn);
        dest.writeString(this.mFqdn);
        dest.writeSerializable(this.mIp);
        dest.writeInt(this.mProtocolId);
        dest.writeString(this.mRemotePort);
        dest.writeInt(this.mConnectionCapability);
    }

    public String getAppId() {
        return this.mAppId;
    }

    public String getDnn() {
        return this.mDnn;
    }

    public String getFqdn() {
        return this.mFqdn;
    }

    public InetAddress getIp() {
        return this.mIp;
    }

    public int getProtocolId() {
        return this.mProtocolId;
    }

    public String getRemotePort() {
        return this.mRemotePort;
    }

    public int getConnectionCapability() {
        return this.mConnectionCapability;
    }

    public static final class Builder {
        private String mAppId;
        private int mConnectionCapability;
        private String mDnn;
        private String mFqdn;
        private InetAddress mIp;
        private int mProtocolId;
        private String mRemotePort;

        public Builder setAppId(String appId) {
            this.mAppId = appId;
            return this;
        }

        public Builder setDnn(String dnn) {
            this.mDnn = dnn;
            return this;
        }

        public Builder setFqdn(String fqdn) {
            this.mFqdn = fqdn;
            return this;
        }

        public Builder setIp(InetAddress ip) {
            this.mIp = ip;
            return this;
        }

        public Builder setProtocolId(int protocolId) {
            this.mProtocolId = protocolId;
            return this;
        }

        public Builder setRemotePort(String remotePort) {
            this.mRemotePort = remotePort;
            return this;
        }

        public Builder setConnectionCapability(int connectionCapability) {
            this.mConnectionCapability = this.mConnectionCapability;
            return this;
        }

        public TrafficDescriptor build() {
            return new TrafficDescriptor(this);
        }
    }
}

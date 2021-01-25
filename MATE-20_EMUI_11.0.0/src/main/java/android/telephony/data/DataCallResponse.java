package android.telephony.data;

import android.annotation.SystemApi;
import android.net.LinkAddress;
import android.os.Parcel;
import android.os.Parcelable;
import com.android.internal.annotations.VisibleForTesting;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@SystemApi
public final class DataCallResponse implements Parcelable {
    public static final Parcelable.Creator<DataCallResponse> CREATOR = new Parcelable.Creator<DataCallResponse>() {
        /* class android.telephony.data.DataCallResponse.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public DataCallResponse createFromParcel(Parcel source) {
            return new DataCallResponse(source);
        }

        @Override // android.os.Parcelable.Creator
        public DataCallResponse[] newArray(int size) {
            return new DataCallResponse[size];
        }
    };
    public static final int LINK_STATUS_ACTIVE = 2;
    public static final int LINK_STATUS_DORMANT = 1;
    public static final int LINK_STATUS_INACTIVE = 0;
    public static final int LINK_STATUS_UNKNOWN = -1;
    private final List<LinkAddress> mAddresses;
    private final int mCause;
    private final List<InetAddress> mDnsAddresses;
    private final List<InetAddress> mGatewayAddresses;
    private final int mId;
    private final String mInterfaceName;
    private final int mLinkStatus;
    private final int mMtu;
    private final List<InetAddress> mPcscfAddresses;
    private final int mProtocolType;
    private final int mSuggestedRetryTime;

    @Retention(RetentionPolicy.SOURCE)
    public @interface LinkStatus {
    }

    public DataCallResponse(int cause, int suggestedRetryTime, int id, int linkStatus, int protocolType, String interfaceName, List<LinkAddress> addresses, List<InetAddress> dnsAddresses, List<InetAddress> gatewayAddresses, List<InetAddress> pcscfAddresses, int mtu) {
        this.mCause = cause;
        this.mSuggestedRetryTime = suggestedRetryTime;
        this.mId = id;
        this.mLinkStatus = linkStatus;
        this.mProtocolType = protocolType;
        this.mInterfaceName = interfaceName == null ? "" : interfaceName;
        this.mAddresses = addresses == null ? new ArrayList() : new ArrayList(addresses);
        this.mDnsAddresses = dnsAddresses == null ? new ArrayList() : new ArrayList(dnsAddresses);
        this.mGatewayAddresses = gatewayAddresses == null ? new ArrayList() : new ArrayList(gatewayAddresses);
        this.mPcscfAddresses = pcscfAddresses == null ? new ArrayList() : new ArrayList(pcscfAddresses);
        this.mMtu = mtu;
    }

    @VisibleForTesting
    public DataCallResponse(Parcel source) {
        this.mCause = source.readInt();
        this.mSuggestedRetryTime = source.readInt();
        this.mId = source.readInt();
        this.mLinkStatus = source.readInt();
        this.mProtocolType = source.readInt();
        this.mInterfaceName = source.readString();
        this.mAddresses = new ArrayList();
        source.readList(this.mAddresses, LinkAddress.class.getClassLoader());
        this.mDnsAddresses = new ArrayList();
        source.readList(this.mDnsAddresses, InetAddress.class.getClassLoader());
        this.mGatewayAddresses = new ArrayList();
        source.readList(this.mGatewayAddresses, InetAddress.class.getClassLoader());
        this.mPcscfAddresses = new ArrayList();
        source.readList(this.mPcscfAddresses, InetAddress.class.getClassLoader());
        this.mMtu = source.readInt();
    }

    public int getCause() {
        return this.mCause;
    }

    public int getSuggestedRetryTime() {
        return this.mSuggestedRetryTime;
    }

    public int getId() {
        return this.mId;
    }

    public int getLinkStatus() {
        return this.mLinkStatus;
    }

    public int getProtocolType() {
        return this.mProtocolType;
    }

    public String getInterfaceName() {
        return this.mInterfaceName;
    }

    public List<LinkAddress> getAddresses() {
        return this.mAddresses;
    }

    public List<InetAddress> getDnsAddresses() {
        return this.mDnsAddresses;
    }

    public List<InetAddress> getGatewayAddresses() {
        return this.mGatewayAddresses;
    }

    public List<InetAddress> getPcscfAddresses() {
        return this.mPcscfAddresses;
    }

    public int getMtu() {
        return this.mMtu;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("DataCallResponse: {");
        sb.append(" cause=");
        sb.append(this.mCause);
        sb.append(" retry=");
        sb.append(this.mSuggestedRetryTime);
        sb.append(" cid=");
        sb.append(this.mId);
        sb.append(" linkStatus=");
        sb.append(this.mLinkStatus);
        sb.append(" protocolType=");
        sb.append(this.mProtocolType);
        sb.append(" ifname=");
        sb.append(this.mInterfaceName);
        sb.append(" addresses = *");
        sb.append(" dnses=");
        sb.append(this.mDnsAddresses);
        sb.append(" gateways = *");
        sb.append(" pcscf=");
        sb.append(this.mPcscfAddresses);
        sb.append(" mtu=");
        sb.append(this.mMtu);
        sb.append("}");
        return sb.toString();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DataCallResponse)) {
            return false;
        }
        DataCallResponse other = (DataCallResponse) o;
        if (this.mCause == other.mCause && this.mSuggestedRetryTime == other.mSuggestedRetryTime && this.mId == other.mId && this.mLinkStatus == other.mLinkStatus && this.mProtocolType == other.mProtocolType && this.mInterfaceName.equals(other.mInterfaceName) && this.mAddresses.size() == other.mAddresses.size() && this.mAddresses.containsAll(other.mAddresses) && this.mDnsAddresses.size() == other.mDnsAddresses.size() && this.mDnsAddresses.containsAll(other.mDnsAddresses) && this.mGatewayAddresses.size() == other.mGatewayAddresses.size() && this.mGatewayAddresses.containsAll(other.mGatewayAddresses) && this.mPcscfAddresses.size() == other.mPcscfAddresses.size() && this.mPcscfAddresses.containsAll(other.mPcscfAddresses) && this.mMtu == other.mMtu) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return Objects.hash(Integer.valueOf(this.mCause), Integer.valueOf(this.mSuggestedRetryTime), Integer.valueOf(this.mId), Integer.valueOf(this.mLinkStatus), Integer.valueOf(this.mProtocolType), this.mInterfaceName, this.mAddresses, this.mDnsAddresses, this.mGatewayAddresses, this.mPcscfAddresses, Integer.valueOf(this.mMtu));
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mCause);
        dest.writeInt(this.mSuggestedRetryTime);
        dest.writeInt(this.mId);
        dest.writeInt(this.mLinkStatus);
        dest.writeInt(this.mProtocolType);
        dest.writeString(this.mInterfaceName);
        dest.writeList(this.mAddresses);
        dest.writeList(this.mDnsAddresses);
        dest.writeList(this.mGatewayAddresses);
        dest.writeList(this.mPcscfAddresses);
        dest.writeInt(this.mMtu);
    }

    public static final class Builder {
        private List<LinkAddress> mAddresses;
        private int mCause;
        private List<InetAddress> mDnsAddresses;
        private List<InetAddress> mGatewayAddresses;
        private int mId;
        private String mInterfaceName;
        private int mLinkStatus;
        private int mMtu;
        private List<InetAddress> mPcscfAddresses;
        private int mProtocolType;
        private int mSuggestedRetryTime;

        public Builder setCause(int cause) {
            this.mCause = cause;
            return this;
        }

        public Builder setSuggestedRetryTime(int suggestedRetryTime) {
            this.mSuggestedRetryTime = suggestedRetryTime;
            return this;
        }

        public Builder setId(int id) {
            this.mId = id;
            return this;
        }

        public Builder setLinkStatus(int linkStatus) {
            this.mLinkStatus = linkStatus;
            return this;
        }

        public Builder setProtocolType(int protocolType) {
            this.mProtocolType = protocolType;
            return this;
        }

        public Builder setInterfaceName(String interfaceName) {
            this.mInterfaceName = interfaceName;
            return this;
        }

        public Builder setAddresses(List<LinkAddress> addresses) {
            this.mAddresses = addresses;
            return this;
        }

        public Builder setDnsAddresses(List<InetAddress> dnsAddresses) {
            this.mDnsAddresses = dnsAddresses;
            return this;
        }

        public Builder setGatewayAddresses(List<InetAddress> gatewayAddresses) {
            this.mGatewayAddresses = gatewayAddresses;
            return this;
        }

        public Builder setPcscfAddresses(List<InetAddress> pcscfAddresses) {
            this.mPcscfAddresses = pcscfAddresses;
            return this;
        }

        public Builder setMtu(int mtu) {
            this.mMtu = mtu;
            return this;
        }

        public DataCallResponse build() {
            return new DataCallResponse(this.mCause, this.mSuggestedRetryTime, this.mId, this.mLinkStatus, this.mProtocolType, this.mInterfaceName, this.mAddresses, this.mDnsAddresses, this.mGatewayAddresses, this.mPcscfAddresses, this.mMtu);
        }
    }
}

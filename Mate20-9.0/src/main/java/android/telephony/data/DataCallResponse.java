package android.telephony.data;

import android.net.LinkAddress;
import android.os.Parcel;
import android.os.Parcelable;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class DataCallResponse implements Parcelable {
    public static final Parcelable.Creator<DataCallResponse> CREATOR = new Parcelable.Creator<DataCallResponse>() {
        public DataCallResponse createFromParcel(Parcel source) {
            return new DataCallResponse(source);
        }

        public DataCallResponse[] newArray(int size) {
            return new DataCallResponse[size];
        }
    };
    private final int mActive;
    private final List<LinkAddress> mAddresses;
    private final int mCid;
    private final List<InetAddress> mDnses;
    private final List<InetAddress> mGateways;
    private final String mIfname;
    private final int mMtu;
    private final List<String> mPcscfs;
    private final int mStatus;
    private final int mSuggestedRetryTime;
    private final String mType;

    public DataCallResponse(int status, int suggestedRetryTime, int cid, int active, String type, String ifname, List<LinkAddress> addresses, List<InetAddress> dnses, List<InetAddress> gateways, List<String> pcscfs, int mtu) {
        this.mStatus = status;
        this.mSuggestedRetryTime = suggestedRetryTime;
        this.mCid = cid;
        this.mActive = active;
        this.mType = type == null ? "" : type;
        this.mIfname = ifname == null ? "" : ifname;
        this.mAddresses = addresses == null ? new ArrayList<>() : addresses;
        this.mDnses = dnses == null ? new ArrayList<>() : dnses;
        this.mGateways = gateways == null ? new ArrayList<>() : gateways;
        this.mPcscfs = pcscfs == null ? new ArrayList<>() : pcscfs;
        this.mMtu = mtu;
    }

    public DataCallResponse(Parcel source) {
        this.mStatus = source.readInt();
        this.mSuggestedRetryTime = source.readInt();
        this.mCid = source.readInt();
        this.mActive = source.readInt();
        this.mType = source.readString();
        this.mIfname = source.readString();
        this.mAddresses = new ArrayList();
        source.readList(this.mAddresses, LinkAddress.class.getClassLoader());
        this.mDnses = new ArrayList();
        source.readList(this.mDnses, InetAddress.class.getClassLoader());
        this.mGateways = new ArrayList();
        source.readList(this.mGateways, InetAddress.class.getClassLoader());
        this.mPcscfs = new ArrayList();
        source.readList(this.mPcscfs, InetAddress.class.getClassLoader());
        this.mMtu = source.readInt();
    }

    public int getStatus() {
        return this.mStatus;
    }

    public int getSuggestedRetryTime() {
        return this.mSuggestedRetryTime;
    }

    public int getCallId() {
        return this.mCid;
    }

    public int getActive() {
        return this.mActive;
    }

    public String getType() {
        return this.mType;
    }

    public String getIfname() {
        return this.mIfname;
    }

    public List<LinkAddress> getAddresses() {
        return this.mAddresses;
    }

    public List<InetAddress> getDnses() {
        return this.mDnses;
    }

    public List<InetAddress> getGateways() {
        return this.mGateways;
    }

    public List<String> getPcscfs() {
        return this.mPcscfs;
    }

    public int getMtu() {
        return this.mMtu;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("DataCallResponse: {");
        sb.append(" status=");
        sb.append(this.mStatus);
        sb.append(" retry=");
        sb.append(this.mSuggestedRetryTime);
        sb.append(" cid=");
        sb.append(this.mCid);
        sb.append(" active=");
        sb.append(this.mActive);
        sb.append(" type=");
        sb.append(this.mType);
        sb.append(" ifname=");
        sb.append(this.mIfname);
        sb.append(" addresses= *");
        sb.append(" dnses=");
        sb.append(this.mDnses);
        sb.append(" gateways= *");
        sb.append(" pcscf=");
        sb.append(this.mPcscfs);
        sb.append(" mtu=");
        sb.append(this.mMtu);
        sb.append("}");
        return sb.toString();
    }

    public boolean equals(Object o) {
        boolean z = true;
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof DataCallResponse)) {
            return false;
        }
        DataCallResponse other = (DataCallResponse) o;
        if (!(this.mStatus == other.mStatus && this.mSuggestedRetryTime == other.mSuggestedRetryTime && this.mCid == other.mCid && this.mActive == other.mActive && this.mType.equals(other.mType) && this.mIfname.equals(other.mIfname) && this.mAddresses.size() == other.mAddresses.size() && this.mAddresses.containsAll(other.mAddresses) && this.mDnses.size() == other.mDnses.size() && this.mDnses.containsAll(other.mDnses) && this.mGateways.size() == other.mGateways.size() && this.mGateways.containsAll(other.mGateways) && this.mPcscfs.size() == other.mPcscfs.size() && this.mPcscfs.containsAll(other.mPcscfs) && this.mMtu == other.mMtu)) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(this.mStatus), Integer.valueOf(this.mSuggestedRetryTime), Integer.valueOf(this.mCid), Integer.valueOf(this.mActive), this.mType, this.mIfname, this.mAddresses, this.mDnses, this.mGateways, this.mPcscfs, Integer.valueOf(this.mMtu)});
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mStatus);
        dest.writeInt(this.mSuggestedRetryTime);
        dest.writeInt(this.mCid);
        dest.writeInt(this.mActive);
        dest.writeString(this.mType);
        dest.writeString(this.mIfname);
        dest.writeList(this.mAddresses);
        dest.writeList(this.mDnses);
        dest.writeList(this.mGateways);
        dest.writeList(this.mPcscfs);
        dest.writeInt(this.mMtu);
    }
}

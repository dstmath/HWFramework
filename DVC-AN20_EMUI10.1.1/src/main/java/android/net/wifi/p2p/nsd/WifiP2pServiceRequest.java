package android.net.wifi.p2p.nsd;

import android.annotation.UnsupportedAppUsage;
import android.os.Parcel;
import android.os.Parcelable;
import java.util.Locale;

public class WifiP2pServiceRequest implements Parcelable {
    @UnsupportedAppUsage
    public static final Parcelable.Creator<WifiP2pServiceRequest> CREATOR = new Parcelable.Creator<WifiP2pServiceRequest>() {
        /* class android.net.wifi.p2p.nsd.WifiP2pServiceRequest.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public WifiP2pServiceRequest createFromParcel(Parcel in) {
            return new WifiP2pServiceRequest(in.readInt(), in.readInt(), in.readInt(), in.readString());
        }

        @Override // android.os.Parcelable.Creator
        public WifiP2pServiceRequest[] newArray(int size) {
            return new WifiP2pServiceRequest[size];
        }
    };
    private int mLength;
    private int mProtocolType;
    private String mQuery;
    private int mTransId;

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    protected WifiP2pServiceRequest(int protocolType, String query) {
        validateQuery(query);
        this.mProtocolType = protocolType;
        this.mQuery = query;
        if (query != null) {
            this.mLength = (query.length() / 2) + 2;
        } else {
            this.mLength = 2;
        }
    }

    private WifiP2pServiceRequest(int serviceType, int length, int transId, String query) {
        this.mProtocolType = serviceType;
        this.mLength = length;
        this.mTransId = transId;
        this.mQuery = query;
    }

    public int getTransactionId() {
        return this.mTransId;
    }

    public void setTransactionId(int id) {
        this.mTransId = id;
    }

    public String getSupplicantQuery() {
        StringBuffer sb = new StringBuffer();
        sb.append(String.format(Locale.US, "%02x", Integer.valueOf(this.mLength & 255)));
        sb.append(String.format(Locale.US, "%02x", Integer.valueOf((this.mLength >> 8) & 255)));
        sb.append(String.format(Locale.US, "%02x", Integer.valueOf(this.mProtocolType)));
        sb.append(String.format(Locale.US, "%02x", Integer.valueOf(this.mTransId)));
        String str = this.mQuery;
        if (str != null) {
            sb.append(str);
        }
        return sb.toString();
    }

    private void validateQuery(String query) {
        if (query != null) {
            if (query.length() % 2 == 1) {
                throw new IllegalArgumentException("query size is invalid. query=" + query);
            } else if (query.length() / 2 <= 65535) {
                String query2 = query.toLowerCase(Locale.ROOT);
                char[] chars = query2.toCharArray();
                for (char c : chars) {
                    if ((c < '0' || c > '9') && (c < 'a' || c > 'f')) {
                        throw new IllegalArgumentException("query should be hex string. query=" + query2);
                    }
                }
            } else {
                throw new IllegalArgumentException("query size is too large. len=" + query.length());
            }
        }
    }

    public static WifiP2pServiceRequest newInstance(int protocolType, String queryData) {
        return new WifiP2pServiceRequest(protocolType, queryData);
    }

    public static WifiP2pServiceRequest newInstance(int protocolType) {
        return new WifiP2pServiceRequest(protocolType, null);
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof WifiP2pServiceRequest)) {
            return false;
        }
        WifiP2pServiceRequest req = (WifiP2pServiceRequest) o;
        if (req.mProtocolType != this.mProtocolType || req.mLength != this.mLength) {
            return false;
        }
        if (req.mQuery == null && this.mQuery == null) {
            return true;
        }
        String str = req.mQuery;
        if (str != null) {
            return str.equals(this.mQuery);
        }
        return false;
    }

    public int hashCode() {
        int result = ((((17 * 31) + this.mProtocolType) * 31) + this.mLength) * 31;
        String str = this.mQuery;
        return result + (str == null ? 0 : str.hashCode());
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mProtocolType);
        dest.writeInt(this.mLength);
        dest.writeInt(this.mTransId);
        dest.writeString(this.mQuery);
    }
}

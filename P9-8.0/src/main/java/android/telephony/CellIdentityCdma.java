package android.telephony;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.Objects;

public final class CellIdentityCdma implements Parcelable {
    public static final Creator<CellIdentityCdma> CREATOR = new Creator<CellIdentityCdma>() {
        public CellIdentityCdma createFromParcel(Parcel in) {
            return new CellIdentityCdma(in, null);
        }

        public CellIdentityCdma[] newArray(int size) {
            return new CellIdentityCdma[size];
        }
    };
    private static final boolean DBG = false;
    private static final String LOG_TAG = "CellSignalStrengthCdma";
    private final int mBasestationId;
    private final int mLatitude;
    private final int mLongitude;
    private final int mNetworkId;
    private final int mSystemId;

    /* synthetic */ CellIdentityCdma(Parcel in, CellIdentityCdma -this1) {
        this(in);
    }

    public CellIdentityCdma() {
        this.mNetworkId = Integer.MAX_VALUE;
        this.mSystemId = Integer.MAX_VALUE;
        this.mBasestationId = Integer.MAX_VALUE;
        this.mLongitude = Integer.MAX_VALUE;
        this.mLatitude = Integer.MAX_VALUE;
    }

    public CellIdentityCdma(int nid, int sid, int bid, int lon, int lat) {
        this.mNetworkId = nid;
        this.mSystemId = sid;
        this.mBasestationId = bid;
        this.mLongitude = lon;
        this.mLatitude = lat;
    }

    private CellIdentityCdma(CellIdentityCdma cid) {
        this.mNetworkId = cid.mNetworkId;
        this.mSystemId = cid.mSystemId;
        this.mBasestationId = cid.mBasestationId;
        this.mLongitude = cid.mLongitude;
        this.mLatitude = cid.mLatitude;
    }

    CellIdentityCdma copy() {
        return new CellIdentityCdma(this);
    }

    public int getNetworkId() {
        return this.mNetworkId;
    }

    public int getSystemId() {
        return this.mSystemId;
    }

    public int getBasestationId() {
        return this.mBasestationId;
    }

    public int getLongitude() {
        return this.mLongitude;
    }

    public int getLatitude() {
        return this.mLatitude;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(this.mNetworkId), Integer.valueOf(this.mSystemId), Integer.valueOf(this.mBasestationId), Integer.valueOf(this.mLatitude), Integer.valueOf(this.mLongitude)});
    }

    public boolean equals(Object other) {
        boolean z = true;
        if (this == other) {
            return true;
        }
        if (!(other instanceof CellIdentityCdma)) {
            return false;
        }
        CellIdentityCdma o = (CellIdentityCdma) other;
        if (this.mNetworkId != o.mNetworkId || this.mSystemId != o.mSystemId || this.mBasestationId != o.mBasestationId || this.mLatitude != o.mLatitude) {
            z = false;
        } else if (this.mLongitude != o.mLongitude) {
            z = false;
        }
        return z;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("CellIdentityCdma:{");
        sb.append(" mNetworkId=");
        sb.append(this.mNetworkId);
        sb.append(" mSystemId=");
        sb.append(this.mSystemId);
        sb.append(" mBasestationId=");
        sb.append(this.mBasestationId);
        sb.append(" mLongitude=");
        sb.append(this.mLongitude);
        sb.append(" mLatitude=");
        sb.append(this.mLatitude);
        sb.append("}");
        return sb.toString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mNetworkId);
        dest.writeInt(this.mSystemId);
        dest.writeInt(this.mBasestationId);
        dest.writeInt(this.mLongitude);
        dest.writeInt(this.mLatitude);
    }

    private CellIdentityCdma(Parcel in) {
        this.mNetworkId = in.readInt();
        this.mSystemId = in.readInt();
        this.mBasestationId = in.readInt();
        this.mLongitude = in.readInt();
        this.mLatitude = in.readInt();
    }

    private static void log(String s) {
        Rlog.w(LOG_TAG, s);
    }
}

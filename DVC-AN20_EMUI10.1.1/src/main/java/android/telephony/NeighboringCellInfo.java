package android.telephony;

import android.annotation.UnsupportedAppUsage;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Parcel;
import android.os.Parcelable;
import com.android.internal.content.NativeLibraryHelper;

@Deprecated
public class NeighboringCellInfo implements Parcelable {
    public static final Parcelable.Creator<NeighboringCellInfo> CREATOR = new Parcelable.Creator<NeighboringCellInfo>() {
        /* class android.telephony.NeighboringCellInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public NeighboringCellInfo createFromParcel(Parcel in) {
            return new NeighboringCellInfo(in);
        }

        @Override // android.os.Parcelable.Creator
        public NeighboringCellInfo[] newArray(int size) {
            return new NeighboringCellInfo[size];
        }
    };
    public static final int UNKNOWN_CID = -1;
    public static final int UNKNOWN_RSSI = 99;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    private int mCid;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    private int mLac;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    private int mNetworkType;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    private int mPsc;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    private int mRssi;

    @Deprecated
    public NeighboringCellInfo() {
        this.mRssi = 99;
        this.mLac = -1;
        this.mCid = -1;
        this.mPsc = -1;
        this.mNetworkType = 0;
    }

    @Deprecated
    public NeighboringCellInfo(int rssi, int cid) {
        this.mRssi = rssi;
        this.mCid = cid;
    }

    public NeighboringCellInfo(CellInfoGsm info) {
        this.mNetworkType = 1;
        this.mRssi = info.getCellSignalStrength().getAsuLevel();
        if (this.mRssi == Integer.MAX_VALUE) {
            this.mRssi = 99;
        }
        this.mLac = info.getCellIdentity().getLac();
        if (this.mLac == Integer.MAX_VALUE) {
            this.mLac = -1;
        }
        this.mCid = info.getCellIdentity().getCid();
        if (this.mCid == Integer.MAX_VALUE) {
            this.mCid = -1;
        }
        this.mPsc = -1;
    }

    public NeighboringCellInfo(CellInfoWcdma info) {
        this.mNetworkType = 3;
        this.mRssi = info.getCellSignalStrength().getAsuLevel();
        if (this.mRssi == Integer.MAX_VALUE) {
            this.mRssi = 99;
        }
        this.mLac = info.getCellIdentity().getLac();
        if (this.mLac == Integer.MAX_VALUE) {
            this.mLac = -1;
        }
        this.mCid = info.getCellIdentity().getCid();
        if (this.mCid == Integer.MAX_VALUE) {
            this.mCid = -1;
        }
        this.mPsc = info.getCellIdentity().getPsc();
        if (this.mPsc == Integer.MAX_VALUE) {
            this.mPsc = -1;
        }
    }

    public NeighboringCellInfo(int rssi, String location, int radioType) {
        this.mRssi = rssi;
        this.mNetworkType = 0;
        this.mPsc = -1;
        this.mLac = -1;
        this.mCid = -1;
        int l = location.length();
        if (l <= 8) {
            if (l < 8) {
                for (int i = 0; i < 8 - l; i++) {
                    location = WifiEnterpriseConfig.ENGINE_DISABLE + location;
                }
            }
            if (radioType == 1 || radioType == 2) {
                this.mNetworkType = radioType;
                if (!location.equalsIgnoreCase("FFFFFFFF")) {
                    this.mCid = Integer.parseInt(location.substring(4), 16);
                    this.mLac = Integer.parseInt(location.substring(0, 4), 16);
                    return;
                }
                return;
            }
            if (radioType != 3) {
                switch (radioType) {
                    case 8:
                    case 9:
                    case 10:
                        break;
                    default:
                        return;
                }
            }
            try {
                this.mNetworkType = radioType;
                this.mPsc = Integer.parseInt(location, 16);
            } catch (NumberFormatException e) {
                this.mPsc = -1;
                this.mLac = -1;
                this.mCid = -1;
                this.mNetworkType = 0;
            }
        }
    }

    public NeighboringCellInfo(Parcel in) {
        this.mRssi = in.readInt();
        this.mLac = in.readInt();
        this.mCid = in.readInt();
        this.mPsc = in.readInt();
        this.mNetworkType = in.readInt();
    }

    public int getRssi() {
        return this.mRssi;
    }

    public int getLac() {
        return this.mLac;
    }

    public int getCid() {
        return this.mCid;
    }

    public int getPsc() {
        return this.mPsc;
    }

    public int getNetworkType() {
        return this.mNetworkType;
    }

    @Deprecated
    public void setCid(int cid) {
        this.mCid = cid;
    }

    @Deprecated
    public void setRssi(int rssi) {
        this.mRssi = rssi;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        int i = this.mPsc;
        Object obj = NativeLibraryHelper.CLEAR_ABI_OVERRIDE;
        if (i != -1) {
            sb.append(Integer.toHexString(i));
            sb.append("@");
            int i2 = this.mRssi;
            if (i2 != 99) {
                obj = Integer.valueOf(i2);
            }
            sb.append(obj);
        } else {
            int i3 = this.mLac;
            if (!(i3 == -1 || this.mCid == -1)) {
                sb.append(Integer.toHexString(i3));
                sb.append(Integer.toHexString(this.mCid));
                sb.append("@");
                int i4 = this.mRssi;
                if (i4 != 99) {
                    obj = Integer.valueOf(i4);
                }
                sb.append(obj);
            }
        }
        sb.append("]");
        return sb.toString();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mRssi);
        dest.writeInt(this.mLac);
        dest.writeInt(this.mCid);
        dest.writeInt(this.mPsc);
        dest.writeInt(this.mNetworkType);
    }
}

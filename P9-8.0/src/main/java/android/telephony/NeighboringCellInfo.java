package android.telephony;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.android.internal.content.NativeLibraryHelper;

public class NeighboringCellInfo implements Parcelable {
    public static final Creator<NeighboringCellInfo> CREATOR = new Creator<NeighboringCellInfo>() {
        public NeighboringCellInfo createFromParcel(Parcel in) {
            return new NeighboringCellInfo(in);
        }

        public NeighboringCellInfo[] newArray(int size) {
            return new NeighboringCellInfo[size];
        }
    };
    public static final int UNKNOWN_CID = -1;
    public static final int UNKNOWN_RSSI = 99;
    private int mCid;
    private int mLac;
    private int mNetworkType;
    private int mPsc;
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

    /* Code decompiled incorrectly, please refer to instructions dump. */
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
                    location = "0" + location;
                }
            }
            switch (radioType) {
                case 1:
                case 2:
                    try {
                        this.mNetworkType = radioType;
                        if (!location.equalsIgnoreCase("FFFFFFFF")) {
                            this.mCid = Integer.parseInt(location.substring(4), 16);
                            this.mLac = Integer.parseInt(location.substring(0, 4), 16);
                            break;
                        }
                    } catch (NumberFormatException e) {
                        this.mPsc = -1;
                        this.mLac = -1;
                        this.mCid = -1;
                        this.mNetworkType = 0;
                        break;
                    }
                    break;
                case 3:
                case 8:
                case 9:
                case 10:
                    this.mNetworkType = radioType;
                    this.mPsc = Integer.parseInt(location, 16);
                    break;
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
        if (this.mPsc != -1) {
            sb.append(Integer.toHexString(this.mPsc)).append("@").append(this.mRssi == 99 ? NativeLibraryHelper.CLEAR_ABI_OVERRIDE : Integer.valueOf(this.mRssi));
        } else if (!(this.mLac == -1 || this.mCid == -1)) {
            sb.append(Integer.toHexString(this.mLac)).append(Integer.toHexString(this.mCid)).append("@").append(this.mRssi == 99 ? NativeLibraryHelper.CLEAR_ABI_OVERRIDE : Integer.valueOf(this.mRssi));
        }
        sb.append("]");
        return sb.toString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mRssi);
        dest.writeInt(this.mLac);
        dest.writeInt(this.mCid);
        dest.writeInt(this.mPsc);
        dest.writeInt(this.mNetworkType);
    }
}

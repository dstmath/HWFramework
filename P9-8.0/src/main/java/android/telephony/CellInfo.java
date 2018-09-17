package android.telephony;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.android.internal.telephony.SmsConstants;

public abstract class CellInfo implements Parcelable {
    public static final Creator<CellInfo> CREATOR = new Creator<CellInfo>() {
        public CellInfo createFromParcel(Parcel in) {
            switch (in.readInt()) {
                case 1:
                    return CellInfoGsm.createFromParcelBody(in);
                case 2:
                    return CellInfoCdma.createFromParcelBody(in);
                case 3:
                    return CellInfoLte.createFromParcelBody(in);
                case 4:
                    return CellInfoWcdma.createFromParcelBody(in);
                default:
                    throw new RuntimeException("Bad CellInfo Parcel");
            }
        }

        public CellInfo[] newArray(int size) {
            return new CellInfo[size];
        }
    };
    public static final int TIMESTAMP_TYPE_ANTENNA = 1;
    public static final int TIMESTAMP_TYPE_JAVA_RIL = 4;
    public static final int TIMESTAMP_TYPE_MODEM = 2;
    public static final int TIMESTAMP_TYPE_OEM_RIL = 3;
    public static final int TIMESTAMP_TYPE_UNKNOWN = 0;
    protected static final int TYPE_CDMA = 2;
    protected static final int TYPE_GSM = 1;
    protected static final int TYPE_LTE = 3;
    protected static final int TYPE_WCDMA = 4;
    private boolean mRegistered;
    private long mTimeStamp;
    private int mTimeStampType;

    public abstract void writeToParcel(Parcel parcel, int i);

    protected CellInfo() {
        this.mRegistered = false;
        this.mTimeStampType = 0;
        this.mTimeStamp = Long.MAX_VALUE;
    }

    protected CellInfo(CellInfo ci) {
        this.mRegistered = ci.mRegistered;
        this.mTimeStampType = ci.mTimeStampType;
        this.mTimeStamp = ci.mTimeStamp;
    }

    public boolean isRegistered() {
        return this.mRegistered;
    }

    public void setRegistered(boolean registered) {
        this.mRegistered = registered;
    }

    public long getTimeStamp() {
        return this.mTimeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.mTimeStamp = timeStamp;
    }

    public int getTimeStampType() {
        return this.mTimeStampType;
    }

    public void setTimeStampType(int timeStampType) {
        if (timeStampType < 0 || timeStampType > 4) {
            this.mTimeStampType = 0;
        } else {
            this.mTimeStampType = timeStampType;
        }
    }

    public int hashCode() {
        return (((this.mRegistered ? 0 : 1) * 31) + (((int) (this.mTimeStamp / 1000)) * 31)) + (this.mTimeStampType * 31);
    }

    public boolean equals(Object other) {
        boolean z = true;
        if (other == null) {
            return false;
        }
        if (this == other) {
            return true;
        }
        try {
            CellInfo o = (CellInfo) other;
            if (this.mRegistered != o.mRegistered || this.mTimeStamp != o.mTimeStamp) {
                z = false;
            } else if (this.mTimeStampType != o.mTimeStampType) {
                z = false;
            }
            return z;
        } catch (ClassCastException e) {
            return false;
        }
    }

    private static String timeStampTypeToString(int type) {
        switch (type) {
            case 1:
                return "antenna";
            case 2:
                return "modem";
            case 3:
                return "oem_ril";
            case 4:
                return "java_ril";
            default:
                return SmsConstants.FORMAT_UNKNOWN;
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("mRegistered=").append(this.mRegistered ? "YES" : "NO");
        sb.append(" mTimeStampType=").append(timeStampTypeToString(this.mTimeStampType));
        sb.append(" mTimeStamp=").append(this.mTimeStamp).append("ns");
        return sb.toString();
    }

    public int describeContents() {
        return 0;
    }

    protected void writeToParcel(Parcel dest, int flags, int type) {
        dest.writeInt(type);
        dest.writeInt(this.mRegistered ? 1 : 0);
        dest.writeInt(this.mTimeStampType);
        dest.writeLong(this.mTimeStamp);
    }

    protected CellInfo(Parcel in) {
        boolean z = true;
        if (in.readInt() != 1) {
            z = false;
        }
        this.mRegistered = z;
        this.mTimeStampType = in.readInt();
        this.mTimeStamp = in.readLong();
    }
}

package android.telephony;

import android.os.Parcel;
import android.os.Parcelable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public abstract class CellInfo implements Parcelable {
    public static final int CONNECTION_NONE = 0;
    public static final int CONNECTION_PRIMARY_SERVING = 1;
    public static final int CONNECTION_SECONDARY_SERVING = 2;
    public static final int CONNECTION_UNKNOWN = Integer.MAX_VALUE;
    public static final Parcelable.Creator<CellInfo> CREATOR = new Parcelable.Creator<CellInfo>() {
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
    private int mCellConnectionStatus;
    private boolean mRegistered;
    private long mTimeStamp;
    private int mTimeStampType;

    @Retention(RetentionPolicy.SOURCE)
    public @interface CellConnectionStatus {
    }

    public abstract void writeToParcel(Parcel parcel, int i);

    protected CellInfo() {
        this.mCellConnectionStatus = 0;
        this.mRegistered = false;
        this.mTimeStampType = 0;
        this.mTimeStamp = SubscriptionPlan.BYTES_UNLIMITED;
    }

    protected CellInfo(CellInfo ci) {
        this.mCellConnectionStatus = 0;
        this.mRegistered = ci.mRegistered;
        this.mTimeStampType = ci.mTimeStampType;
        this.mTimeStamp = ci.mTimeStamp;
        this.mCellConnectionStatus = ci.mCellConnectionStatus;
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

    public int getCellConnectionStatus() {
        return this.mCellConnectionStatus;
    }

    public void setCellConnectionStatus(int cellConnectionStatus) {
        this.mCellConnectionStatus = cellConnectionStatus;
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
        return ((this.mRegistered ^ true ? 1 : 0) * true) + (((int) (this.mTimeStamp / 1000)) * 31) + (this.mTimeStampType * 31) + (this.mCellConnectionStatus * 31);
    }

    public boolean equals(Object other) {
        boolean z = false;
        if (other == null) {
            return false;
        }
        if (this == other) {
            return true;
        }
        try {
            CellInfo o = (CellInfo) other;
            if (this.mRegistered == o.mRegistered && this.mTimeStamp == o.mTimeStamp && this.mTimeStampType == o.mTimeStampType && this.mCellConnectionStatus == o.mCellConnectionStatus) {
                z = true;
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
                return "unknown";
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("mRegistered=");
        sb.append(this.mRegistered ? "YES" : "NO");
        String timeStampType = timeStampTypeToString(this.mTimeStampType);
        sb.append(" mTimeStampType=");
        sb.append(timeStampType);
        sb.append(" mTimeStamp=");
        sb.append(this.mTimeStamp);
        sb.append("ns");
        sb.append(" mCellConnectionStatus=");
        sb.append(this.mCellConnectionStatus);
        return sb.toString();
    }

    public int describeContents() {
        return 0;
    }

    /* access modifiers changed from: protected */
    public void writeToParcel(Parcel dest, int flags, int type) {
        dest.writeInt(type);
        dest.writeInt(this.mRegistered ? 1 : 0);
        dest.writeInt(this.mTimeStampType);
        dest.writeLong(this.mTimeStamp);
        dest.writeInt(this.mCellConnectionStatus);
    }

    protected CellInfo(Parcel in) {
        boolean z = false;
        this.mCellConnectionStatus = 0;
        this.mRegistered = in.readInt() == 1 ? true : z;
        this.mTimeStampType = in.readInt();
        this.mTimeStamp = in.readLong();
        this.mCellConnectionStatus = in.readInt();
    }
}

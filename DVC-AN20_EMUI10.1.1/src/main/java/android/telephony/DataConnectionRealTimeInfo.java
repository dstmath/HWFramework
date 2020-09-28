package android.telephony;

import android.os.Parcel;
import android.os.Parcelable;

public class DataConnectionRealTimeInfo implements Parcelable {
    public static final Parcelable.Creator<DataConnectionRealTimeInfo> CREATOR = new Parcelable.Creator<DataConnectionRealTimeInfo>() {
        /* class android.telephony.DataConnectionRealTimeInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public DataConnectionRealTimeInfo createFromParcel(Parcel in) {
            return new DataConnectionRealTimeInfo(in);
        }

        @Override // android.os.Parcelable.Creator
        public DataConnectionRealTimeInfo[] newArray(int size) {
            return new DataConnectionRealTimeInfo[size];
        }
    };
    public static final int DC_POWER_STATE_HIGH = 3;
    public static final int DC_POWER_STATE_LOW = 1;
    public static final int DC_POWER_STATE_MEDIUM = 2;
    public static final int DC_POWER_STATE_UNKNOWN = Integer.MAX_VALUE;
    private int mDcPowerState;
    private long mTime;

    public DataConnectionRealTimeInfo(long time, int dcPowerState) {
        this.mTime = time;
        this.mDcPowerState = dcPowerState;
    }

    public DataConnectionRealTimeInfo() {
        this.mTime = Long.MAX_VALUE;
        this.mDcPowerState = Integer.MAX_VALUE;
    }

    private DataConnectionRealTimeInfo(Parcel in) {
        this.mTime = in.readLong();
        this.mDcPowerState = in.readInt();
    }

    public long getTime() {
        return this.mTime;
    }

    public int getDcPowerState() {
        return this.mDcPowerState;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(this.mTime);
        out.writeInt(this.mDcPowerState);
    }

    public int hashCode() {
        long result = (1 * 17) + this.mTime;
        return (int) (result + (17 * result) + ((long) this.mDcPowerState));
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        DataConnectionRealTimeInfo other = (DataConnectionRealTimeInfo) obj;
        if (this.mTime == other.mTime && this.mDcPowerState == other.mDcPowerState) {
            return true;
        }
        return false;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("mTime=");
        sb.append(this.mTime);
        sb.append(" mDcPowerState=");
        sb.append(this.mDcPowerState);
        return sb.toString();
    }
}

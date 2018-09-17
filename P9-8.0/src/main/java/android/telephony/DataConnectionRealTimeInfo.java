package android.telephony;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class DataConnectionRealTimeInfo implements Parcelable {
    public static final Creator<DataConnectionRealTimeInfo> CREATOR = new Creator<DataConnectionRealTimeInfo>() {
        public DataConnectionRealTimeInfo createFromParcel(Parcel in) {
            return new DataConnectionRealTimeInfo(in, null);
        }

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

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(this.mTime);
        out.writeInt(this.mDcPowerState);
    }

    public int hashCode() {
        long result = 17 + this.mTime;
        return (int) (result + ((17 * result) + ((long) this.mDcPowerState)));
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        DataConnectionRealTimeInfo other = (DataConnectionRealTimeInfo) obj;
        if (this.mTime != other.mTime) {
            z = false;
        } else if (this.mDcPowerState != other.mDcPowerState) {
            z = false;
        }
        return z;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("mTime=").append(this.mTime);
        sb.append(" mDcPowerState=").append(this.mDcPowerState);
        return sb.toString();
    }
}

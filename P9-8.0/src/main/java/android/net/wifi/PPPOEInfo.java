package android.net.wifi;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class PPPOEInfo implements Parcelable {
    public static final Creator<PPPOEInfo> CREATOR = new Creator<PPPOEInfo>() {
        public PPPOEInfo createFromParcel(Parcel source) {
            return new PPPOEInfo(Status.fromInt(source.readInt()), source.readLong());
        }

        public PPPOEInfo[] newArray(int size) {
            return new PPPOEInfo[size];
        }
    };
    public long mOnlineTime;
    public Status mStatus = Status.OFFLINE;

    public enum Status {
        OFFLINE,
        CONNECTING,
        ONLINE;

        public static Status fromInt(int value) {
            if (OFFLINE.ordinal() == value) {
                return OFFLINE;
            }
            if (CONNECTING.ordinal() == value) {
                return CONNECTING;
            }
            if (ONLINE.ordinal() == value) {
                return ONLINE;
            }
            throw new IllegalArgumentException("Invalid value: " + value);
        }
    }

    public PPPOEInfo(Status status, long time) {
        this.mStatus = status;
        this.mOnlineTime = time;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mStatus.ordinal());
        dest.writeLong(this.mOnlineTime);
    }
}

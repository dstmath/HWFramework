package android.net.wifi;

import android.os.Parcel;
import android.os.Parcelable;

public class PPPOEInfo implements Parcelable {
    public static final Parcelable.Creator<PPPOEInfo> CREATOR = new Parcelable.Creator<PPPOEInfo>() {
        /* class android.net.wifi.PPPOEInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public PPPOEInfo createFromParcel(Parcel source) {
            return new PPPOEInfo(Status.fromInt(source.readInt()), source.readLong());
        }

        @Override // android.os.Parcelable.Creator
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

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mStatus.ordinal());
        dest.writeLong(this.mOnlineTime);
    }
}

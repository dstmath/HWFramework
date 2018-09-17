package android.net;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.UserHandle;

public final class UidRange implements Parcelable {
    public static final Creator<UidRange> CREATOR = new Creator<UidRange>() {
        public UidRange createFromParcel(Parcel in) {
            return new UidRange(in.readInt(), in.readInt());
        }

        public UidRange[] newArray(int size) {
            return new UidRange[size];
        }
    };
    public final int start;
    public final int stop;

    public UidRange(int startUid, int stopUid) {
        if (startUid < 0) {
            throw new IllegalArgumentException("Invalid start UID.");
        } else if (stopUid < 0) {
            throw new IllegalArgumentException("Invalid stop UID.");
        } else if (startUid > stopUid) {
            throw new IllegalArgumentException("Invalid UID range.");
        } else {
            this.start = startUid;
            this.stop = stopUid;
        }
    }

    public static UidRange createForUser(int userId) {
        return new UidRange(userId * UserHandle.PER_USER_RANGE, ((userId + 1) * UserHandle.PER_USER_RANGE) - 1);
    }

    public int getStartUser() {
        return this.start / UserHandle.PER_USER_RANGE;
    }

    public boolean contains(int uid) {
        return this.start <= uid && uid <= this.stop;
    }

    public boolean containsRange(UidRange other) {
        return this.start <= other.start && other.stop <= this.stop;
    }

    public int hashCode() {
        return ((this.start + 527) * 31) + this.stop;
    }

    public boolean equals(Object o) {
        boolean z = true;
        if (this == o) {
            return true;
        }
        if (!(o instanceof UidRange)) {
            return false;
        }
        UidRange other = (UidRange) o;
        if (!(this.start == other.start && this.stop == other.stop)) {
            z = false;
        }
        return z;
    }

    public String toString() {
        return this.start + "-" + this.stop;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.start);
        dest.writeInt(this.stop);
    }
}

package android.net;

import android.os.Parcel;
import android.os.Parcelable;
import com.android.internal.content.NativeLibraryHelper;
import java.util.Collection;

public final class UidRange implements Parcelable {
    public static final Parcelable.Creator<UidRange> CREATOR = new Parcelable.Creator<UidRange>() {
        /* class android.net.UidRange.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public UidRange createFromParcel(Parcel in) {
            return new UidRange(in.readInt(), in.readInt());
        }

        @Override // android.os.Parcelable.Creator
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
        } else if (startUid <= stopUid) {
            this.start = startUid;
            this.stop = stopUid;
        } else {
            throw new IllegalArgumentException("Invalid UID range.");
        }
    }

    public static UidRange createForUser(int userId) {
        return new UidRange(userId * 100000, ((userId + 1) * 100000) - 1);
    }

    public int getStartUser() {
        return this.start / 100000;
    }

    public int getEndUser() {
        return this.stop / 100000;
    }

    public boolean contains(int uid) {
        return this.start <= uid && uid <= this.stop;
    }

    public int count() {
        return (this.stop + 1) - this.start;
    }

    public boolean containsRange(UidRange other) {
        return this.start <= other.start && other.stop <= this.stop;
    }

    public int hashCode() {
        return (((17 * 31) + this.start) * 31) + this.stop;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UidRange)) {
            return false;
        }
        UidRange other = (UidRange) o;
        if (this.start == other.start && this.stop == other.stop) {
            return true;
        }
        return false;
    }

    public String toString() {
        return this.start + NativeLibraryHelper.CLEAR_ABI_OVERRIDE + this.stop;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.start);
        dest.writeInt(this.stop);
    }

    public static boolean containsUid(Collection<UidRange> ranges, int uid) {
        if (ranges == null) {
            return false;
        }
        for (UidRange range : ranges) {
            if (range.contains(uid)) {
                return true;
            }
        }
        return false;
    }
}

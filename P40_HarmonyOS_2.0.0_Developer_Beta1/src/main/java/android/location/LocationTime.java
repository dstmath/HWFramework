package android.location;

import android.os.Parcel;
import android.os.Parcelable;

public final class LocationTime implements Parcelable {
    public static final Parcelable.Creator<LocationTime> CREATOR = new Parcelable.Creator<LocationTime>() {
        /* class android.location.LocationTime.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public LocationTime createFromParcel(Parcel in) {
            return new LocationTime(in.readLong(), in.readLong());
        }

        @Override // android.os.Parcelable.Creator
        public LocationTime[] newArray(int size) {
            return new LocationTime[size];
        }
    };
    private final long mElapsedRealtimeNanos;
    private final long mTime;

    public LocationTime(long time, long elapsedRealtimeNanos) {
        this.mTime = time;
        this.mElapsedRealtimeNanos = elapsedRealtimeNanos;
    }

    public long getTime() {
        return this.mTime;
    }

    public long getElapsedRealtimeNanos() {
        return this.mElapsedRealtimeNanos;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(this.mTime);
        out.writeLong(this.mElapsedRealtimeNanos);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }
}

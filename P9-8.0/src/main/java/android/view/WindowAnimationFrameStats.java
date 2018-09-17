package android.view;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public final class WindowAnimationFrameStats extends FrameStats implements Parcelable {
    public static final Creator<WindowAnimationFrameStats> CREATOR = new Creator<WindowAnimationFrameStats>() {
        public WindowAnimationFrameStats createFromParcel(Parcel parcel) {
            return new WindowAnimationFrameStats(parcel, null);
        }

        public WindowAnimationFrameStats[] newArray(int size) {
            return new WindowAnimationFrameStats[size];
        }
    };

    /* synthetic */ WindowAnimationFrameStats(Parcel parcel, WindowAnimationFrameStats -this1) {
        this(parcel);
    }

    public void init(long refreshPeriodNano, long[] framesPresentedTimeNano) {
        this.mRefreshPeriodNano = refreshPeriodNano;
        this.mFramesPresentedTimeNano = framesPresentedTimeNano;
    }

    private WindowAnimationFrameStats(Parcel parcel) {
        this.mRefreshPeriodNano = parcel.readLong();
        this.mFramesPresentedTimeNano = parcel.createLongArray();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeLong(this.mRefreshPeriodNano);
        parcel.writeLongArray(this.mFramesPresentedTimeNano);
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("WindowAnimationFrameStats[");
        builder.append("frameCount:").append(getFrameCount());
        builder.append(", fromTimeNano:").append(getStartTimeNano());
        builder.append(", toTimeNano:").append(getEndTimeNano());
        builder.append(']');
        return builder.toString();
    }
}

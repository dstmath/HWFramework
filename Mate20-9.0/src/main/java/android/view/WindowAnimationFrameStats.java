package android.view;

import android.os.Parcel;
import android.os.Parcelable;

public final class WindowAnimationFrameStats extends FrameStats implements Parcelable {
    public static final Parcelable.Creator<WindowAnimationFrameStats> CREATOR = new Parcelable.Creator<WindowAnimationFrameStats>() {
        public WindowAnimationFrameStats createFromParcel(Parcel parcel) {
            return new WindowAnimationFrameStats(parcel);
        }

        public WindowAnimationFrameStats[] newArray(int size) {
            return new WindowAnimationFrameStats[size];
        }
    };

    public WindowAnimationFrameStats() {
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
        return "WindowAnimationFrameStats[" + ("frameCount:" + getFrameCount()) + (", fromTimeNano:" + getStartTimeNano()) + (", toTimeNano:" + getEndTimeNano()) + ']';
    }
}

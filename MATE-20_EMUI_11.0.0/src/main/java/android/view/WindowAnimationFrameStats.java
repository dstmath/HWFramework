package android.view;

import android.annotation.UnsupportedAppUsage;
import android.os.Parcel;
import android.os.Parcelable;

public final class WindowAnimationFrameStats extends FrameStats implements Parcelable {
    public static final Parcelable.Creator<WindowAnimationFrameStats> CREATOR = new Parcelable.Creator<WindowAnimationFrameStats>() {
        /* class android.view.WindowAnimationFrameStats.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public WindowAnimationFrameStats createFromParcel(Parcel parcel) {
            return new WindowAnimationFrameStats(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public WindowAnimationFrameStats[] newArray(int size) {
            return new WindowAnimationFrameStats[size];
        }
    };

    public WindowAnimationFrameStats() {
    }

    @UnsupportedAppUsage
    public void init(long refreshPeriodNano, long[] framesPresentedTimeNano) {
        this.mRefreshPeriodNano = refreshPeriodNano;
        this.mFramesPresentedTimeNano = framesPresentedTimeNano;
    }

    private WindowAnimationFrameStats(Parcel parcel) {
        this.mRefreshPeriodNano = parcel.readLong();
        this.mFramesPresentedTimeNano = parcel.createLongArray();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeLong(this.mRefreshPeriodNano);
        parcel.writeLongArray(this.mFramesPresentedTimeNano);
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("WindowAnimationFrameStats[");
        builder.append("frameCount:" + getFrameCount());
        builder.append(", fromTimeNano:" + getStartTimeNano());
        builder.append(", toTimeNano:" + getEndTimeNano());
        builder.append(']');
        return builder.toString();
    }
}

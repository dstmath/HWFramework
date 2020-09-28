package android.app.timedetector;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.DateFormat;
import android.util.TimestampedValue;
import java.util.Objects;

public final class TimeSignal implements Parcelable {
    public static final Parcelable.Creator<TimeSignal> CREATOR = new Parcelable.Creator<TimeSignal>() {
        /* class android.app.timedetector.TimeSignal.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public TimeSignal createFromParcel(Parcel in) {
            return TimeSignal.createFromParcel(in);
        }

        @Override // android.os.Parcelable.Creator
        public TimeSignal[] newArray(int size) {
            return new TimeSignal[size];
        }
    };
    public static final String SOURCE_ID_NITZ = "nitz";
    private final String mSourceId;
    private final TimestampedValue<Long> mUtcTime;

    public TimeSignal(String sourceId, TimestampedValue<Long> utcTime) {
        this.mSourceId = (String) Objects.requireNonNull(sourceId);
        this.mUtcTime = (TimestampedValue) Objects.requireNonNull(utcTime);
    }

    /* access modifiers changed from: private */
    public static TimeSignal createFromParcel(Parcel in) {
        return new TimeSignal(in.readString(), TimestampedValue.readFromParcel(in, null, Long.class));
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mSourceId);
        TimestampedValue.writeToParcel(dest, this.mUtcTime);
    }

    public String getSourceId() {
        return this.mSourceId;
    }

    public TimestampedValue<Long> getUtcTime() {
        return this.mUtcTime;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TimeSignal that = (TimeSignal) o;
        if (!Objects.equals(this.mSourceId, that.mSourceId) || !Objects.equals(this.mUtcTime, that.mUtcTime)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return Objects.hash(this.mSourceId, this.mUtcTime);
    }

    public String toString() {
        return "TimeSignal{mSourceId='" + this.mSourceId + DateFormat.QUOTE + ", mUtcTime=" + this.mUtcTime + '}';
    }
}

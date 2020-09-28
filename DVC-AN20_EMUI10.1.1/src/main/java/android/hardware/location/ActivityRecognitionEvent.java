package android.hardware.location;

import android.os.Parcel;
import android.os.Parcelable;

public class ActivityRecognitionEvent implements Parcelable {
    public static final Parcelable.Creator<ActivityRecognitionEvent> CREATOR = new Parcelable.Creator<ActivityRecognitionEvent>() {
        /* class android.hardware.location.ActivityRecognitionEvent.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ActivityRecognitionEvent createFromParcel(Parcel source) {
            return new ActivityRecognitionEvent(source.readString(), source.readInt(), source.readLong());
        }

        @Override // android.os.Parcelable.Creator
        public ActivityRecognitionEvent[] newArray(int size) {
            return new ActivityRecognitionEvent[size];
        }
    };
    private final String mActivity;
    private final int mEventType;
    private final long mTimestampNs;

    public ActivityRecognitionEvent(String activity, int eventType, long timestampNs) {
        this.mActivity = activity;
        this.mEventType = eventType;
        this.mTimestampNs = timestampNs;
    }

    public String getActivity() {
        return this.mActivity;
    }

    public int getEventType() {
        return this.mEventType;
    }

    public long getTimestampNs() {
        return this.mTimestampNs;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(this.mActivity);
        parcel.writeInt(this.mEventType);
        parcel.writeLong(this.mTimestampNs);
    }

    public String toString() {
        return String.format("Activity='%s', EventType=%s, TimestampNs=%s", this.mActivity, Integer.valueOf(this.mEventType), Long.valueOf(this.mTimestampNs));
    }
}

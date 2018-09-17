package android.hardware.location;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class ActivityRecognitionEvent implements Parcelable {
    public static final Creator<ActivityRecognitionEvent> CREATOR = new Creator<ActivityRecognitionEvent>() {
        public ActivityRecognitionEvent createFromParcel(Parcel source) {
            return new ActivityRecognitionEvent(source.readString(), source.readInt(), source.readLong());
        }

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

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(this.mActivity);
        parcel.writeInt(this.mEventType);
        parcel.writeLong(this.mTimestampNs);
    }

    public String toString() {
        return String.format("Activity='%s', EventType=%s, TimestampNs=%s", new Object[]{this.mActivity, Integer.valueOf(this.mEventType), Long.valueOf(this.mTimestampNs)});
    }
}

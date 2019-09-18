package com.huawei.android.location.activityrecognition;

import android.os.Parcel;
import android.os.Parcelable;

public class HwActivityRecognitionEvent implements Parcelable {
    public static final Parcelable.Creator<HwActivityRecognitionEvent> CREATOR = new Parcelable.Creator<HwActivityRecognitionEvent>() {
        public HwActivityRecognitionEvent createFromParcel(Parcel source) {
            String activity = source.readString();
            int eventType = source.readInt();
            long timestampNs = source.readLong();
            if (HwActivityRecognition.getARServiceVersion() == 1) {
                return new HwActivityRecognitionEvent(activity, eventType, timestampNs, source.readInt());
            }
            return new HwActivityRecognitionEvent(activity, eventType, timestampNs);
        }

        public HwActivityRecognitionEvent[] newArray(int size) {
            return new HwActivityRecognitionEvent[size];
        }
    };
    private final String mActivity;
    private int mConfidence;
    private final int mEventType;
    private final long mTimestampNs;

    public HwActivityRecognitionEvent(String activity, int eventType, long timestampNs) {
        this.mActivity = activity;
        this.mEventType = eventType;
        this.mTimestampNs = timestampNs;
        this.mConfidence = -2;
    }

    public HwActivityRecognitionEvent(String activity, int eventType, long timestampNs, int confidence) {
        this.mActivity = activity;
        this.mEventType = eventType;
        this.mTimestampNs = timestampNs;
        this.mConfidence = confidence;
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

    public int getConfidence() {
        return this.mConfidence;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(this.mActivity);
        parcel.writeInt(this.mEventType);
        parcel.writeLong(this.mTimestampNs);
        if (HwActivityRecognition.getARServiceVersion() == 1) {
            parcel.writeInt(this.mConfidence);
        }
    }

    public String toString() {
        return String.format("Activity='%s', EventType=%s, TimestampNs=%s, Confidence=%s", new Object[]{this.mActivity, Integer.valueOf(this.mEventType), Long.valueOf(this.mTimestampNs), Integer.valueOf(this.mConfidence)});
    }
}

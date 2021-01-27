package com.android.location.provider;

public class ActivityRecognitionEvent {
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

    public String toString() {
        String eventString;
        int i = this.mEventType;
        if (i == 0) {
            eventString = "FlushComplete";
        } else if (i == 1) {
            eventString = "Enter";
        } else if (i != 2) {
            eventString = "<Invalid>";
        } else {
            eventString = "Exit";
        }
        return String.format("Activity='%s', EventType=%s(%s), TimestampNs=%s", this.mActivity, eventString, Integer.valueOf(this.mEventType), Long.valueOf(this.mTimestampNs));
    }
}

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
        switch (this.mEventType) {
            case 0:
                eventString = "FlushComplete";
                break;
            case 1:
                eventString = "Enter";
                break;
            case 2:
                eventString = "Exit";
                break;
            default:
                eventString = "<Invalid>";
                break;
        }
        return String.format("Activity='%s', EventType=%s(%s), TimestampNs=%s", new Object[]{this.mActivity, eventString, Integer.valueOf(this.mEventType), Long.valueOf(this.mTimestampNs)});
    }
}

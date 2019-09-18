package com.huawei.android.location.activityrecognition;

import android.os.Parcel;
import android.os.Parcelable;

public class HwActivityRecognitionExtendEvent implements Parcelable {
    public static final Parcelable.Creator<HwActivityRecognitionExtendEvent> CREATOR = new Parcelable.Creator<HwActivityRecognitionExtendEvent>() {
        public HwActivityRecognitionExtendEvent createFromParcel(Parcel source) {
            String activity = source.readString();
            int eventType = source.readInt();
            long timestampNs = source.readLong();
            OtherParameters otherParams = (OtherParameters) source.readParcelable(OtherParameters.class.getClassLoader());
            if (HwActivityRecognition.getARServiceVersion() == 1) {
                return new HwActivityRecognitionExtendEvent(activity, eventType, timestampNs, otherParams, source.readInt());
            }
            return new HwActivityRecognitionExtendEvent(activity, eventType, timestampNs, otherParams);
        }

        public HwActivityRecognitionExtendEvent[] newArray(int size) {
            return new HwActivityRecognitionExtendEvent[size];
        }
    };
    private final String mActivity;
    private int mConfidence;
    private final int mEventType;
    private final OtherParameters mOtherParams;
    private final long mTimestampNs;

    public HwActivityRecognitionExtendEvent(String activity, int eventType, long timestampNs, OtherParameters otherParams) {
        this.mActivity = activity;
        this.mEventType = eventType;
        this.mTimestampNs = timestampNs;
        this.mOtherParams = otherParams;
        this.mConfidence = -2;
    }

    public HwActivityRecognitionExtendEvent(String activity, int eventType, long timestampNs, OtherParameters otherParams, int confidence) {
        this.mActivity = activity;
        this.mEventType = eventType;
        this.mTimestampNs = timestampNs;
        this.mOtherParams = otherParams;
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

    public OtherParameters getOtherParams() {
        return this.mOtherParams;
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
        parcel.writeParcelable(this.mOtherParams, flags);
        if (HwActivityRecognition.getARServiceVersion() == 1) {
            parcel.writeInt(this.mConfidence);
        }
    }

    public String toString() {
        if (this.mOtherParams != null) {
            return String.format("Activity='%s', EventType=%s, TimestampNs=%s, Param1=%s, Param2=%s, Param3=%s, Param4=%s, Param5=%s, Confidence=%s", new Object[]{this.mActivity, Integer.valueOf(this.mEventType), Long.valueOf(this.mTimestampNs), Double.valueOf(this.mOtherParams.getmParam1()), Double.valueOf(this.mOtherParams.getmParam2()), Double.valueOf(this.mOtherParams.getmParam3()), Double.valueOf(this.mOtherParams.getmParam4()), this.mOtherParams.getmParam5(), Integer.valueOf(this.mConfidence)});
        }
        return String.format("Activity='%s', EventType=%s, TimestampNs=%s, Confidence=%s", new Object[]{this.mActivity, Integer.valueOf(this.mEventType), Long.valueOf(this.mTimestampNs), Integer.valueOf(this.mConfidence)});
    }
}

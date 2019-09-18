package com.huawei.android.location.activityrecognition;

import android.os.Parcel;
import android.os.Parcelable;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.List;

public class HwActivityChangedEvent implements Parcelable {
    public static final Parcelable.Creator<HwActivityChangedEvent> CREATOR = new Parcelable.Creator<HwActivityChangedEvent>() {
        public HwActivityChangedEvent createFromParcel(Parcel source) {
            HwActivityRecognitionEvent[] activityRecognitionEvents = new HwActivityRecognitionEvent[source.readInt()];
            source.readTypedArray(activityRecognitionEvents, HwActivityRecognitionEvent.CREATOR);
            return new HwActivityChangedEvent(activityRecognitionEvents);
        }

        public HwActivityChangedEvent[] newArray(int size) {
            return new HwActivityChangedEvent[size];
        }
    };
    private final List<HwActivityRecognitionEvent> mActivityRecognitionEvents;

    public HwActivityChangedEvent(HwActivityRecognitionEvent[] activityRecognitionEvents) {
        if (activityRecognitionEvents == null) {
            throw new InvalidParameterException("Parameter 'activityRecognitionEvents' must not be null.");
        }
        this.mActivityRecognitionEvents = Arrays.asList(activityRecognitionEvents);
    }

    public Iterable<HwActivityRecognitionEvent> getActivityRecognitionEvents() {
        return this.mActivityRecognitionEvents;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        HwActivityRecognitionEvent[] activityRecognitionEventArray = (HwActivityRecognitionEvent[]) this.mActivityRecognitionEvents.toArray(new HwActivityRecognitionEvent[0]);
        parcel.writeInt(activityRecognitionEventArray.length);
        parcel.writeTypedArray(activityRecognitionEventArray, flags);
    }

    public String toString() {
        StringBuilder builder = new StringBuilder("[ ActivityChangedEvent:");
        for (HwActivityRecognitionEvent event : this.mActivityRecognitionEvents) {
            builder.append("\n    ");
            builder.append(event.toString());
        }
        builder.append("\n]");
        return builder.toString();
    }
}

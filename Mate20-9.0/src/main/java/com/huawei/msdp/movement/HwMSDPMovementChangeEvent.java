package com.huawei.msdp.movement;

import android.os.Parcel;
import android.os.Parcelable;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.List;

public class HwMSDPMovementChangeEvent implements Parcelable {
    public static final Parcelable.Creator<HwMSDPMovementChangeEvent> CREATOR = new Parcelable.Creator<HwMSDPMovementChangeEvent>() {
        public HwMSDPMovementChangeEvent createFromParcel(Parcel source) {
            HwMSDPMovementEvent[] activityRecognitionEvents = new HwMSDPMovementEvent[source.readInt()];
            source.readTypedArray(activityRecognitionEvents, HwMSDPMovementEvent.CREATOR);
            return new HwMSDPMovementChangeEvent(activityRecognitionEvents);
        }

        public HwMSDPMovementChangeEvent[] newArray(int size) {
            return new HwMSDPMovementChangeEvent[size];
        }
    };
    private final List<HwMSDPMovementEvent> mMovementEvents;

    public HwMSDPMovementChangeEvent(HwMSDPMovementEvent[] movementChangeEvents) {
        if (movementChangeEvents == null) {
            throw new InvalidParameterException("Parameter 'movementChangeEvents' must not be null.");
        }
        this.mMovementEvents = Arrays.asList(movementChangeEvents);
    }

    public Iterable<HwMSDPMovementEvent> getMovementEvents() {
        return this.mMovementEvents;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        HwMSDPMovementEvent[] activityRecognitionEventArray = (HwMSDPMovementEvent[]) this.mMovementEvents.toArray(new HwMSDPMovementEvent[0]);
        parcel.writeInt(activityRecognitionEventArray.length);
        parcel.writeTypedArray(activityRecognitionEventArray, flags);
    }

    public String toString() {
        StringBuilder builder = new StringBuilder("[ HwMSDPMovementChangeEvent:");
        for (HwMSDPMovementEvent event : this.mMovementEvents) {
            builder.append("\n    ");
            builder.append(event.toString());
        }
        builder.append("\n]");
        return builder.toString();
    }
}

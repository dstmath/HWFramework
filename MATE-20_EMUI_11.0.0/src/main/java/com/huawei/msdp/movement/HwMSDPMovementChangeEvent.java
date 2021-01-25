package com.huawei.msdp.movement;

import android.os.Parcel;
import android.os.Parcelable;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.List;

public class HwMSDPMovementChangeEvent implements Parcelable {
    public static final Parcelable.Creator<HwMSDPMovementChangeEvent> CREATOR = new Parcelable.Creator<HwMSDPMovementChangeEvent>() {
        /* class com.huawei.msdp.movement.HwMSDPMovementChangeEvent.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public HwMSDPMovementChangeEvent createFromParcel(Parcel source) {
            HwMSDPMovementEvent[] activityRecognitionEvents = new HwMSDPMovementEvent[source.readInt()];
            source.readTypedArray(activityRecognitionEvents, HwMSDPMovementEvent.CREATOR);
            return new HwMSDPMovementChangeEvent(activityRecognitionEvents);
        }

        @Override // android.os.Parcelable.Creator
        public HwMSDPMovementChangeEvent[] newArray(int size) {
            return new HwMSDPMovementChangeEvent[size];
        }
    };
    private final List<HwMSDPMovementEvent> mMovementEvents;

    public HwMSDPMovementChangeEvent(HwMSDPMovementEvent[] movementChangeEvents) {
        if (movementChangeEvents != null) {
            this.mMovementEvents = Arrays.asList(movementChangeEvents);
            return;
        }
        throw new InvalidParameterException("Parameter 'movementChangeEvents' must not be null.");
    }

    public Iterable<HwMSDPMovementEvent> getMovementEvents() {
        return this.mMovementEvents;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int flags) {
        HwMSDPMovementEvent[] activityRecognitionEventArray = (HwMSDPMovementEvent[]) this.mMovementEvents.toArray(new HwMSDPMovementEvent[0]);
        parcel.writeInt(activityRecognitionEventArray.length);
        parcel.writeTypedArray(activityRecognitionEventArray, flags);
    }

    @Override // java.lang.Object
    public String toString() {
        StringBuilder builder = new StringBuilder("[ HwMSDPMovementChangeEvent:");
        for (HwMSDPMovementEvent event : this.mMovementEvents) {
            builder.append(System.lineSeparator());
            builder.append(event.toString());
        }
        builder.append(System.lineSeparator() + "]");
        return builder.toString();
    }
}

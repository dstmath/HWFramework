package com.huawei.msdp.devicestatus;

import android.os.Parcel;
import android.os.Parcelable;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.List;

public class HwMSDPDeviceStatusChangeEvent implements Parcelable {
    public static final Parcelable.Creator<HwMSDPDeviceStatusChangeEvent> CREATOR = new Parcelable.Creator<HwMSDPDeviceStatusChangeEvent>() {
        public HwMSDPDeviceStatusChangeEvent createFromParcel(Parcel source) {
            HwMSDPDeviceStatusEvent[] activityRecognitionEvents = new HwMSDPDeviceStatusEvent[source.readInt()];
            source.readTypedArray(activityRecognitionEvents, HwMSDPDeviceStatusEvent.CREATOR);
            return new HwMSDPDeviceStatusChangeEvent(activityRecognitionEvents);
        }

        public HwMSDPDeviceStatusChangeEvent[] newArray(int size) {
            return new HwMSDPDeviceStatusChangeEvent[size];
        }
    };
    private final List<HwMSDPDeviceStatusEvent> mDeviceStatusRecognitionEvents;

    public HwMSDPDeviceStatusChangeEvent(HwMSDPDeviceStatusEvent[] deviceStatusRecognitionEvents) {
        if (deviceStatusRecognitionEvents != null) {
            this.mDeviceStatusRecognitionEvents = Arrays.asList(deviceStatusRecognitionEvents);
            return;
        }
        throw new InvalidParameterException("Parameter 'deviceStatusRecognitionEvents' maybe is null");
    }

    public Iterable<HwMSDPDeviceStatusEvent> getDeviceStatusRecognitionEvents() {
        return this.mDeviceStatusRecognitionEvents;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        HwMSDPDeviceStatusEvent[] activityRecognitionEventArray = (HwMSDPDeviceStatusEvent[]) this.mDeviceStatusRecognitionEvents.toArray(new HwMSDPDeviceStatusEvent[0]);
        dest.writeInt(activityRecognitionEventArray.length);
        dest.writeTypedArray(activityRecognitionEventArray, flags);
    }

    public String toString() {
        return "HwMSDPDeviceStatusChangeEvent{mDeviceStatusRecognitionEvents=" + this.mDeviceStatusRecognitionEvents + '}';
    }
}

package ohos.msdp.devicestatus;

import android.os.Parcel;
import android.os.Parcelable;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.List;

public class HwMSDPDeviceStatusChangeEvent implements Parcelable {
    public static final Parcelable.Creator<HwMSDPDeviceStatusChangeEvent> CREATOR = new Parcelable.Creator<HwMSDPDeviceStatusChangeEvent>() {
        /* class ohos.msdp.devicestatus.HwMSDPDeviceStatusChangeEvent.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public HwMSDPDeviceStatusChangeEvent createFromParcel(Parcel parcel) {
            if (parcel == null) {
                return new HwMSDPDeviceStatusChangeEvent(new HwMSDPDeviceStatusEvent[0]);
            }
            int readInt = parcel.readInt();
            if (!HwMSDPDeviceStatusChangeEvent.checkDeviceEventLen(readInt)) {
                readInt = 0;
            }
            HwMSDPDeviceStatusEvent[] hwMSDPDeviceStatusEventArr = new HwMSDPDeviceStatusEvent[Math.max(readInt, 0)];
            parcel.readTypedArray(hwMSDPDeviceStatusEventArr, HwMSDPDeviceStatusEvent.CREATOR);
            return new HwMSDPDeviceStatusChangeEvent(hwMSDPDeviceStatusEventArr);
        }

        @Override // android.os.Parcelable.Creator
        public HwMSDPDeviceStatusChangeEvent[] newArray(int i) {
            return new HwMSDPDeviceStatusChangeEvent[i];
        }
    };
    private static final int MAX_DEVICE_LEN = 1000;
    private final List<HwMSDPDeviceStatusEvent> mDeviceStatusRecognitionEvents;

    /* access modifiers changed from: private */
    public static boolean checkDeviceEventLen(int i) {
        return i >= 0 && i <= 1000;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public HwMSDPDeviceStatusChangeEvent(HwMSDPDeviceStatusEvent[] hwMSDPDeviceStatusEventArr) {
        if (hwMSDPDeviceStatusEventArr != null) {
            this.mDeviceStatusRecognitionEvents = Arrays.asList(hwMSDPDeviceStatusEventArr);
            return;
        }
        throw new InvalidParameterException("Parameter 'deviceStatusRecognitionEvents' maybe is null");
    }

    public Iterable<HwMSDPDeviceStatusEvent> getDeviceStatusRecognitionEvents() {
        return this.mDeviceStatusRecognitionEvents;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        HwMSDPDeviceStatusEvent[] hwMSDPDeviceStatusEventArr = (HwMSDPDeviceStatusEvent[]) this.mDeviceStatusRecognitionEvents.toArray(new HwMSDPDeviceStatusEvent[0]);
        parcel.writeInt(hwMSDPDeviceStatusEventArr.length);
        parcel.writeTypedArray(hwMSDPDeviceStatusEventArr, i);
    }

    @Override // java.lang.Object
    public String toString() {
        return "HwMSDPDeviceStatusChangeEvent{mDeviceStatusRecognitionEvents=" + this.mDeviceStatusRecognitionEvents + '}';
    }
}

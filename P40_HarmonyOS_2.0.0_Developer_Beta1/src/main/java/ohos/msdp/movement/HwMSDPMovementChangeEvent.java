package ohos.msdp.movement;

import android.os.Parcel;
import android.os.Parcelable;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.List;

public class HwMSDPMovementChangeEvent implements Parcelable {
    public static final Parcelable.Creator<HwMSDPMovementChangeEvent> CREATOR = new Parcelable.Creator<HwMSDPMovementChangeEvent>() {
        /* class ohos.msdp.movement.HwMSDPMovementChangeEvent.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public HwMSDPMovementChangeEvent createFromParcel(Parcel parcel) {
            int i = 0;
            if (parcel == null) {
                return new HwMSDPMovementChangeEvent(new HwMSDPMovementEvent[0]);
            }
            int readInt = parcel.readInt();
            if (HwMSDPMovementChangeEvent.checkMSDPEventLen(readInt)) {
                i = readInt;
            }
            HwMSDPMovementEvent[] hwMSDPMovementEventArr = new HwMSDPMovementEvent[i];
            parcel.readTypedArray(hwMSDPMovementEventArr, HwMSDPMovementEvent.CREATOR);
            return new HwMSDPMovementChangeEvent(hwMSDPMovementEventArr);
        }

        @Override // android.os.Parcelable.Creator
        public HwMSDPMovementChangeEvent[] newArray(int i) {
            return new HwMSDPMovementChangeEvent[i];
        }
    };
    private static final int MAX_AR_LEN = 1000;
    private final List<HwMSDPMovementEvent> mMovementEvents;

    /* access modifiers changed from: private */
    public static boolean checkMSDPEventLen(int i) {
        return i >= 0 && i <= 1000;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public HwMSDPMovementChangeEvent(HwMSDPMovementEvent[] hwMSDPMovementEventArr) {
        if (hwMSDPMovementEventArr != null) {
            this.mMovementEvents = Arrays.asList(hwMSDPMovementEventArr);
            return;
        }
        throw new InvalidParameterException("Parameter 'movementChangeEvents' must not be null.");
    }

    public Iterable<HwMSDPMovementEvent> getMovementEvents() {
        return this.mMovementEvents;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        HwMSDPMovementEvent[] hwMSDPMovementEventArr = (HwMSDPMovementEvent[]) this.mMovementEvents.toArray(new HwMSDPMovementEvent[0]);
        parcel.writeInt(hwMSDPMovementEventArr.length);
        parcel.writeTypedArray(hwMSDPMovementEventArr, i);
    }

    @Override // java.lang.Object
    public String toString() {
        StringBuilder sb = new StringBuilder("[HwMSDPMovementChangeEvent:");
        for (HwMSDPMovementEvent hwMSDPMovementEvent : this.mMovementEvents) {
            sb.append(System.lineSeparator());
            sb.append("    ");
            sb.append(hwMSDPMovementEvent);
        }
        sb.append(System.lineSeparator());
        sb.append("]");
        return sb.toString();
    }
}

package ohos.msdp.movement;

import android.os.Parcel;
import android.os.Parcelable;

public class HwMSDPMovementEvent implements Parcelable {
    public static final Parcelable.Creator<HwMSDPMovementEvent> CREATOR = new Parcelable.Creator<HwMSDPMovementEvent>() {
        /* class ohos.msdp.movement.HwMSDPMovementEvent.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public HwMSDPMovementEvent createFromParcel(Parcel parcel) {
            return new HwMSDPMovementEvent(parcel.readString(), parcel.readInt(), parcel.readLong(), parcel.readInt(), null);
        }

        @Override // android.os.Parcelable.Creator
        public HwMSDPMovementEvent[] newArray(int i) {
            return new HwMSDPMovementEvent[i];
        }
    };
    private final int mConfidence;
    private final int mEventType;
    private final String mMovement;
    private final HwMSDPOtherParameters mOtherParams;
    private final long mTimestampNs;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public HwMSDPMovementEvent(String str, int i, long j, int i2, HwMSDPOtherParameters hwMSDPOtherParameters) {
        this.mMovement = str;
        this.mEventType = i;
        this.mTimestampNs = j;
        this.mConfidence = i2;
        this.mOtherParams = hwMSDPOtherParameters;
    }

    public String getMovement() {
        return this.mMovement;
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

    public HwMSDPOtherParameters getOtherParams() {
        return this.mOtherParams;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.mMovement);
        parcel.writeInt(this.mEventType);
        parcel.writeLong(this.mTimestampNs);
        parcel.writeInt(this.mConfidence);
        parcel.writeParcelable(this.mOtherParams, i);
    }
}

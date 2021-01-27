package ohos.dmsdp.sdk.vibrate;

import android.os.Parcel;
import android.os.Parcelable;

public class VirtualVibrator implements Parcelable {
    public static final Parcelable.Creator<VirtualVibrator> CREATOR = new Parcelable.Creator<VirtualVibrator>() {
        /* class ohos.dmsdp.sdk.vibrate.VirtualVibrator.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public VirtualVibrator createFromParcel(Parcel parcel) {
            return new VirtualVibrator(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public VirtualVibrator[] newArray(int i) {
            return new VirtualVibrator[i];
        }
    };
    private String mDeviceId;
    private int mVibrateId;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public VirtualVibrator() {
    }

    public VirtualVibrator(String str, int i) {
        this.mDeviceId = str;
        this.mVibrateId = i;
    }

    protected VirtualVibrator(Parcel parcel) {
        this.mDeviceId = parcel.readString();
        this.mVibrateId = parcel.readInt();
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.mDeviceId);
        parcel.writeInt(this.mVibrateId);
    }

    public int getVibrateId() {
        return this.mVibrateId;
    }

    public void setVibrateId(int i) {
        this.mVibrateId = i;
    }

    public String getDeviceId() {
        return this.mDeviceId;
    }

    public void setDeviceId(String str) {
        this.mDeviceId = str;
    }
}

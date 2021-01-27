package com.huawei.dmsdpsdk2.vibrate;

import android.os.Parcel;
import android.os.Parcelable;

public class VirtualVibrator implements Parcelable {
    public static final Parcelable.Creator<VirtualVibrator> CREATOR = new Parcelable.Creator<VirtualVibrator>() {
        /* class com.huawei.dmsdpsdk2.vibrate.VirtualVibrator.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public VirtualVibrator createFromParcel(Parcel in) {
            return new VirtualVibrator(in);
        }

        @Override // android.os.Parcelable.Creator
        public VirtualVibrator[] newArray(int size) {
            return new VirtualVibrator[size];
        }
    };
    private String mDeviceId;
    private int mVibrateId;

    public VirtualVibrator() {
    }

    public VirtualVibrator(String deviceId, int vibrateId) {
        this.mDeviceId = deviceId;
        this.mVibrateId = vibrateId;
    }

    protected VirtualVibrator(Parcel in) {
        this.mDeviceId = in.readString();
        this.mVibrateId = in.readInt();
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mDeviceId);
        dest.writeInt(this.mVibrateId);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public int getVibrateId() {
        return this.mVibrateId;
    }

    public void setVibrateId(int vibrateId) {
        this.mVibrateId = vibrateId;
    }

    public String getDeviceId() {
        return this.mDeviceId;
    }

    public void setDeviceId(String deviceId) {
        this.mDeviceId = deviceId;
    }
}

package com.android.internal.location;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public final class ProviderProperties implements Parcelable {
    public static final Creator<ProviderProperties> CREATOR = new Creator<ProviderProperties>() {
        public ProviderProperties createFromParcel(Parcel in) {
            return new ProviderProperties(in.readInt() == 1, in.readInt() == 1, in.readInt() == 1, in.readInt() == 1, in.readInt() == 1, in.readInt() == 1, in.readInt() == 1, in.readInt(), in.readInt());
        }

        public ProviderProperties[] newArray(int size) {
            return new ProviderProperties[size];
        }
    };
    public final int mAccuracy;
    public final boolean mHasMonetaryCost;
    public final int mPowerRequirement;
    public final boolean mRequiresCell;
    public final boolean mRequiresNetwork;
    public final boolean mRequiresSatellite;
    public final boolean mSupportsAltitude;
    public final boolean mSupportsBearing;
    public final boolean mSupportsSpeed;

    public ProviderProperties(boolean mRequiresNetwork, boolean mRequiresSatellite, boolean mRequiresCell, boolean mHasMonetaryCost, boolean mSupportsAltitude, boolean mSupportsSpeed, boolean mSupportsBearing, int mPowerRequirement, int mAccuracy) {
        this.mRequiresNetwork = mRequiresNetwork;
        this.mRequiresSatellite = mRequiresSatellite;
        this.mRequiresCell = mRequiresCell;
        this.mHasMonetaryCost = mHasMonetaryCost;
        this.mSupportsAltitude = mSupportsAltitude;
        this.mSupportsSpeed = mSupportsSpeed;
        this.mSupportsBearing = mSupportsBearing;
        this.mPowerRequirement = mPowerRequirement;
        this.mAccuracy = mAccuracy;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        int i;
        int i2 = 1;
        parcel.writeInt(this.mRequiresNetwork ? 1 : 0);
        if (this.mRequiresSatellite) {
            i = 1;
        } else {
            i = 0;
        }
        parcel.writeInt(i);
        if (this.mRequiresCell) {
            i = 1;
        } else {
            i = 0;
        }
        parcel.writeInt(i);
        if (this.mHasMonetaryCost) {
            i = 1;
        } else {
            i = 0;
        }
        parcel.writeInt(i);
        if (this.mSupportsAltitude) {
            i = 1;
        } else {
            i = 0;
        }
        parcel.writeInt(i);
        if (this.mSupportsSpeed) {
            i = 1;
        } else {
            i = 0;
        }
        parcel.writeInt(i);
        if (!this.mSupportsBearing) {
            i2 = 0;
        }
        parcel.writeInt(i2);
        parcel.writeInt(this.mPowerRequirement);
        parcel.writeInt(this.mAccuracy);
    }
}

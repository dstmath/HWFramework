package com.android.internal.location;

import android.os.Parcel;
import android.os.Parcelable;

public final class ProviderProperties implements Parcelable {
    public static final Parcelable.Creator<ProviderProperties> CREATOR = new Parcelable.Creator<ProviderProperties>() {
        public ProviderProperties createFromParcel(Parcel in) {
            ProviderProperties providerProperties = new ProviderProperties(in.readInt() == 1, in.readInt() == 1, in.readInt() == 1, in.readInt() == 1, in.readInt() == 1, in.readInt() == 1, in.readInt() == 1, in.readInt(), in.readInt());
            return providerProperties;
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

    public ProviderProperties(boolean mRequiresNetwork2, boolean mRequiresSatellite2, boolean mRequiresCell2, boolean mHasMonetaryCost2, boolean mSupportsAltitude2, boolean mSupportsSpeed2, boolean mSupportsBearing2, int mPowerRequirement2, int mAccuracy2) {
        this.mRequiresNetwork = mRequiresNetwork2;
        this.mRequiresSatellite = mRequiresSatellite2;
        this.mRequiresCell = mRequiresCell2;
        this.mHasMonetaryCost = mHasMonetaryCost2;
        this.mSupportsAltitude = mSupportsAltitude2;
        this.mSupportsSpeed = mSupportsSpeed2;
        this.mSupportsBearing = mSupportsBearing2;
        this.mPowerRequirement = mPowerRequirement2;
        this.mAccuracy = mAccuracy2;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(this.mRequiresNetwork ? 1 : 0);
        parcel.writeInt(this.mRequiresSatellite ? 1 : 0);
        parcel.writeInt(this.mRequiresCell ? 1 : 0);
        parcel.writeInt(this.mHasMonetaryCost ? 1 : 0);
        parcel.writeInt(this.mSupportsAltitude ? 1 : 0);
        parcel.writeInt(this.mSupportsSpeed ? 1 : 0);
        parcel.writeInt(this.mSupportsBearing ? 1 : 0);
        parcel.writeInt(this.mPowerRequirement);
        parcel.writeInt(this.mAccuracy);
    }
}

package com.android.phone.ecc.nano.android;

import android.os.Parcel;
import android.os.Parcelable;
import com.android.phone.ecc.nano.ExtendableMessageNano;

public abstract class ParcelableExtendableMessageNano<M extends ExtendableMessageNano<M>> extends ExtendableMessageNano<M> implements Parcelable {
    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        ParcelableMessageNanoCreator.writeToParcel(getClass(), this, out);
    }
}

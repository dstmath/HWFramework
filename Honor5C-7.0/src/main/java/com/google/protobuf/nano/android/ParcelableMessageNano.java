package com.google.protobuf.nano.android;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.protobuf.nano.MessageNano;

public abstract class ParcelableMessageNano extends MessageNano implements Parcelable {
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        ParcelableMessageNanoCreator.writeToParcel(getClass(), this, out);
    }
}

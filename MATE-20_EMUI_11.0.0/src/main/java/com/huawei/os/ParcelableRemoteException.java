package com.huawei.os;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import com.huawei.android.os.storage.StorageManagerExt;

public class ParcelableRemoteException extends RemoteException implements Parcelable {
    public static final Parcelable.Creator<ParcelableRemoteException> CREATOR = new Parcelable.Creator<ParcelableRemoteException>() {
        /* class com.huawei.os.ParcelableRemoteException.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ParcelableRemoteException createFromParcel(Parcel in) {
            return new ParcelableRemoteException(in);
        }

        @Override // android.os.Parcelable.Creator
        public ParcelableRemoteException[] newArray(int size) {
            return new ParcelableRemoteException[size];
        }
    };

    public ParcelableRemoteException() {
    }

    public ParcelableRemoteException(String message) {
        super(message);
    }

    protected ParcelableRemoteException(Parcel in) {
        super(in.readString());
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        String message = getMessage();
        dest.writeString(message != null ? message : StorageManagerExt.INVALID_KEY_DESC);
    }
}

package com.android.internal.os;

import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import com.android.internal.util.Preconditions;

public class AppFuseMount implements Parcelable {
    public static final Parcelable.Creator<AppFuseMount> CREATOR = new Parcelable.Creator<AppFuseMount>() {
        public AppFuseMount createFromParcel(Parcel in) {
            return new AppFuseMount(in.readInt(), (ParcelFileDescriptor) in.readParcelable(null));
        }

        public AppFuseMount[] newArray(int size) {
            return new AppFuseMount[size];
        }
    };
    public final ParcelFileDescriptor fd;
    public final int mountPointId;

    public AppFuseMount(int mountPointId2, ParcelFileDescriptor fd2) {
        Preconditions.checkNotNull(fd2);
        this.mountPointId = mountPointId2;
        this.fd = fd2;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mountPointId);
        dest.writeParcelable(this.fd, flags);
    }
}

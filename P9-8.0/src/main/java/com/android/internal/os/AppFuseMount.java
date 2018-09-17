package com.android.internal.os;

import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.android.internal.util.Preconditions;

public class AppFuseMount implements Parcelable {
    public static final Creator<AppFuseMount> CREATOR = new Creator<AppFuseMount>() {
        public AppFuseMount createFromParcel(Parcel in) {
            return new AppFuseMount(in.readInt(), (ParcelFileDescriptor) in.readParcelable(null));
        }

        public AppFuseMount[] newArray(int size) {
            return new AppFuseMount[size];
        }
    };
    public final ParcelFileDescriptor fd;
    public final int mountPointId;

    public AppFuseMount(int mountPointId, ParcelFileDescriptor fd) {
        Preconditions.checkNotNull(fd);
        this.mountPointId = mountPointId;
        this.fd = fd;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mountPointId);
        dest.writeParcelable(this.fd, flags);
    }
}

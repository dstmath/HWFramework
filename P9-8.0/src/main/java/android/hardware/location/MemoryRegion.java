package android.hardware.location;

import android.app.backup.FullBackup;
import android.net.ProxyInfo;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class MemoryRegion implements Parcelable {
    public static final Creator<MemoryRegion> CREATOR = new Creator<MemoryRegion>() {
        public MemoryRegion createFromParcel(Parcel in) {
            return new MemoryRegion(in);
        }

        public MemoryRegion[] newArray(int size) {
            return new MemoryRegion[size];
        }
    };
    private boolean mIsExecutable;
    private boolean mIsReadable;
    private boolean mIsWritable;
    private int mSizeBytes;
    private int mSizeBytesFree;

    public int getCapacityBytes() {
        return this.mSizeBytes;
    }

    public int getFreeCapacityBytes() {
        return this.mSizeBytesFree;
    }

    public boolean isReadable() {
        return this.mIsReadable;
    }

    public boolean isWritable() {
        return this.mIsWritable;
    }

    public boolean isExecutable() {
        return this.mIsExecutable;
    }

    public String toString() {
        String mask = ProxyInfo.LOCAL_EXCL_LIST;
        if (isReadable()) {
            mask = mask + FullBackup.ROOT_TREE_TOKEN;
        } else {
            mask = mask + "-";
        }
        if (isWritable()) {
            mask = mask + "w";
        } else {
            mask = mask + "-";
        }
        if (isExecutable()) {
            mask = mask + "x";
        } else {
            mask = mask + "-";
        }
        return "[ " + this.mSizeBytesFree + "/ " + this.mSizeBytes + " ] : " + mask;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        int i;
        int i2 = 1;
        dest.writeInt(this.mSizeBytes);
        dest.writeInt(this.mSizeBytesFree);
        if (this.mIsReadable) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.mIsWritable) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (!this.mIsExecutable) {
            i2 = 0;
        }
        dest.writeInt(i2);
    }

    public MemoryRegion(Parcel source) {
        boolean z;
        boolean z2 = true;
        this.mSizeBytes = source.readInt();
        this.mSizeBytesFree = source.readInt();
        if (source.readInt() != 0) {
            z = true;
        } else {
            z = false;
        }
        this.mIsReadable = z;
        if (source.readInt() != 0) {
            z = true;
        } else {
            z = false;
        }
        this.mIsWritable = z;
        if (source.readInt() == 0) {
            z2 = false;
        }
        this.mIsExecutable = z2;
    }
}

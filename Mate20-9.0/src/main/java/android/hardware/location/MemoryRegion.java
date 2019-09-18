package android.hardware.location;

import android.annotation.SystemApi;
import android.app.backup.FullBackup;
import android.os.Parcel;
import android.os.Parcelable;

@SystemApi
public class MemoryRegion implements Parcelable {
    public static final Parcelable.Creator<MemoryRegion> CREATOR = new Parcelable.Creator<MemoryRegion>() {
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
        String mask;
        String mask2;
        String mask3;
        if (isReadable()) {
            mask = "" + FullBackup.ROOT_TREE_TOKEN;
        } else {
            mask = "" + "-";
        }
        if (isWritable()) {
            mask2 = mask + "w";
        } else {
            mask2 = mask + "-";
        }
        if (isExecutable()) {
            mask3 = mask2 + "x";
        } else {
            mask3 = mask2 + "-";
        }
        return "[ " + this.mSizeBytesFree + "/ " + this.mSizeBytes + " ] : " + mask3;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mSizeBytes);
        dest.writeInt(this.mSizeBytesFree);
        dest.writeInt(this.mIsReadable ? 1 : 0);
        dest.writeInt(this.mIsWritable ? 1 : 0);
        dest.writeInt(this.mIsExecutable ? 1 : 0);
    }

    public MemoryRegion(Parcel source) {
        this.mSizeBytes = source.readInt();
        this.mSizeBytesFree = source.readInt();
        boolean z = false;
        this.mIsReadable = source.readInt() != 0;
        this.mIsWritable = source.readInt() != 0;
        this.mIsExecutable = source.readInt() != 0 ? true : z;
    }
}

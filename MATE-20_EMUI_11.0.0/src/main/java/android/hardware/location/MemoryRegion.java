package android.hardware.location;

import android.annotation.SystemApi;
import android.os.Parcel;
import android.os.Parcelable;
import com.android.internal.content.NativeLibraryHelper;

@SystemApi
public class MemoryRegion implements Parcelable {
    public static final Parcelable.Creator<MemoryRegion> CREATOR = new Parcelable.Creator<MemoryRegion>() {
        /* class android.hardware.location.MemoryRegion.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public MemoryRegion createFromParcel(Parcel in) {
            return new MemoryRegion(in);
        }

        @Override // android.os.Parcelable.Creator
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
            mask = "r";
        } else {
            mask = "" + NativeLibraryHelper.CLEAR_ABI_OVERRIDE;
        }
        if (isWritable()) {
            mask2 = mask + "w";
        } else {
            mask2 = mask + NativeLibraryHelper.CLEAR_ABI_OVERRIDE;
        }
        if (isExecutable()) {
            mask3 = mask2 + "x";
        } else {
            mask3 = mask2 + NativeLibraryHelper.CLEAR_ABI_OVERRIDE;
        }
        return "[ " + this.mSizeBytesFree + "/ " + this.mSizeBytes + " ] : " + mask3;
    }

    public boolean equals(Object object) {
        boolean isEqual = true;
        if (object == this) {
            return true;
        }
        if (!(object instanceof MemoryRegion)) {
            return false;
        }
        MemoryRegion other = (MemoryRegion) object;
        if (!(other.getCapacityBytes() == this.mSizeBytes && other.getFreeCapacityBytes() == this.mSizeBytesFree && other.isReadable() == this.mIsReadable && other.isWritable() == this.mIsWritable && other.isExecutable() == this.mIsExecutable)) {
            isEqual = false;
        }
        return isEqual;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
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
        boolean z = true;
        this.mIsReadable = source.readInt() != 0;
        this.mIsWritable = source.readInt() != 0;
        this.mIsExecutable = source.readInt() == 0 ? false : z;
    }
}

package android.app.usage;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public final class StorageStats implements Parcelable {
    public static final Creator<StorageStats> CREATOR = new Creator<StorageStats>() {
        public StorageStats createFromParcel(Parcel in) {
            return new StorageStats(in);
        }

        public StorageStats[] newArray(int size) {
            return new StorageStats[size];
        }
    };
    public long cacheBytes;
    public long codeBytes;
    public long dataBytes;

    public long getAppBytes() {
        return this.codeBytes;
    }

    @Deprecated
    public long getCodeBytes() {
        return getAppBytes();
    }

    public long getDataBytes() {
        return this.dataBytes;
    }

    public long getCacheBytes() {
        return this.cacheBytes;
    }

    public StorageStats(Parcel in) {
        this.codeBytes = in.readLong();
        this.dataBytes = in.readLong();
        this.cacheBytes = in.readLong();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.codeBytes);
        dest.writeLong(this.dataBytes);
        dest.writeLong(this.cacheBytes);
    }
}

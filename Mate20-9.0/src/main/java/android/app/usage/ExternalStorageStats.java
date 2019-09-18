package android.app.usage;

import android.os.Parcel;
import android.os.Parcelable;

public final class ExternalStorageStats implements Parcelable {
    public static final Parcelable.Creator<ExternalStorageStats> CREATOR = new Parcelable.Creator<ExternalStorageStats>() {
        public ExternalStorageStats createFromParcel(Parcel in) {
            return new ExternalStorageStats(in);
        }

        public ExternalStorageStats[] newArray(int size) {
            return new ExternalStorageStats[size];
        }
    };
    public long appBytes;
    public long audioBytes;
    public long imageBytes;
    public long obbBytes;
    public long totalBytes;
    public long videoBytes;

    public long getTotalBytes() {
        return this.totalBytes;
    }

    public long getAudioBytes() {
        return this.audioBytes;
    }

    public long getVideoBytes() {
        return this.videoBytes;
    }

    public long getImageBytes() {
        return this.imageBytes;
    }

    public long getAppBytes() {
        return this.appBytes;
    }

    public long getObbBytes() {
        return this.obbBytes;
    }

    public ExternalStorageStats() {
    }

    public ExternalStorageStats(Parcel in) {
        this.totalBytes = in.readLong();
        this.audioBytes = in.readLong();
        this.videoBytes = in.readLong();
        this.imageBytes = in.readLong();
        this.appBytes = in.readLong();
        this.obbBytes = in.readLong();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.totalBytes);
        dest.writeLong(this.audioBytes);
        dest.writeLong(this.videoBytes);
        dest.writeLong(this.imageBytes);
        dest.writeLong(this.appBytes);
        dest.writeLong(this.obbBytes);
    }
}

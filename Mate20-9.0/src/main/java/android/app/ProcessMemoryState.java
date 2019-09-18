package android.app;

import android.os.Parcel;
import android.os.Parcelable;

public class ProcessMemoryState implements Parcelable {
    public static final Parcelable.Creator<ProcessMemoryState> CREATOR = new Parcelable.Creator<ProcessMemoryState>() {
        public ProcessMemoryState createFromParcel(Parcel in) {
            return new ProcessMemoryState(in);
        }

        public ProcessMemoryState[] newArray(int size) {
            return new ProcessMemoryState[size];
        }
    };
    public long cacheInBytes;
    public int oomScore;
    public long pgfault;
    public long pgmajfault;
    public String processName;
    public long rssInBytes;
    public long swapInBytes;
    public int uid;

    public ProcessMemoryState(int uid2, String processName2, int oomScore2, long pgfault2, long pgmajfault2, long rssInBytes2, long cacheInBytes2, long swapInBytes2) {
        this.uid = uid2;
        this.processName = processName2;
        this.oomScore = oomScore2;
        this.pgfault = pgfault2;
        this.pgmajfault = pgmajfault2;
        this.rssInBytes = rssInBytes2;
        this.cacheInBytes = cacheInBytes2;
        this.swapInBytes = swapInBytes2;
    }

    private ProcessMemoryState(Parcel in) {
        this.uid = in.readInt();
        this.processName = in.readString();
        this.oomScore = in.readInt();
        this.pgfault = in.readLong();
        this.pgmajfault = in.readLong();
        this.rssInBytes = in.readLong();
        this.cacheInBytes = in.readLong();
        this.swapInBytes = in.readLong();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(this.uid);
        parcel.writeString(this.processName);
        parcel.writeInt(this.oomScore);
        parcel.writeLong(this.pgfault);
        parcel.writeLong(this.pgmajfault);
        parcel.writeLong(this.rssInBytes);
        parcel.writeLong(this.cacheInBytes);
        parcel.writeLong(this.swapInBytes);
    }
}

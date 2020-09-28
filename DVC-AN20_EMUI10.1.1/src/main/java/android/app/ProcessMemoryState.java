package android.app;

import android.os.Parcel;
import android.os.Parcelable;

public final class ProcessMemoryState implements Parcelable {
    public static final Parcelable.Creator<ProcessMemoryState> CREATOR = new Parcelable.Creator<ProcessMemoryState>() {
        /* class android.app.ProcessMemoryState.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ProcessMemoryState createFromParcel(Parcel in) {
            return new ProcessMemoryState(in);
        }

        @Override // android.os.Parcelable.Creator
        public ProcessMemoryState[] newArray(int size) {
            return new ProcessMemoryState[size];
        }
    };
    public final int oomScore;
    public final int pid;
    public final String processName;
    public final int uid;

    public ProcessMemoryState(int uid2, int pid2, String processName2, int oomScore2) {
        this.uid = uid2;
        this.pid = pid2;
        this.processName = processName2;
        this.oomScore = oomScore2;
    }

    private ProcessMemoryState(Parcel in) {
        this.uid = in.readInt();
        this.pid = in.readInt();
        this.processName = in.readString();
        this.oomScore = in.readInt();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(this.uid);
        parcel.writeInt(this.pid);
        parcel.writeString(this.processName);
        parcel.writeInt(this.oomScore);
    }
}

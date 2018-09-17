package android.app.backup;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class BackupProgress implements Parcelable {
    public static final Creator<BackupProgress> CREATOR = new Creator<BackupProgress>() {
        public BackupProgress createFromParcel(Parcel in) {
            return new BackupProgress(in, null);
        }

        public BackupProgress[] newArray(int size) {
            return new BackupProgress[size];
        }
    };
    public final long bytesExpected;
    public final long bytesTransferred;

    public BackupProgress(long _bytesExpected, long _bytesTransferred) {
        this.bytesExpected = _bytesExpected;
        this.bytesTransferred = _bytesTransferred;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(this.bytesExpected);
        out.writeLong(this.bytesTransferred);
    }

    private BackupProgress(Parcel in) {
        this.bytesExpected = in.readLong();
        this.bytesTransferred = in.readLong();
    }
}

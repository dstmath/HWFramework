package android.app.backup;

import android.annotation.SystemApi;
import android.os.Parcel;
import android.os.Parcelable;

@SystemApi
public class BackupProgress implements Parcelable {
    public static final Parcelable.Creator<BackupProgress> CREATOR = new Parcelable.Creator<BackupProgress>() {
        /* class android.app.backup.BackupProgress.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public BackupProgress createFromParcel(Parcel in) {
            return new BackupProgress(in);
        }

        @Override // android.os.Parcelable.Creator
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

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(this.bytesExpected);
        out.writeLong(this.bytesTransferred);
    }

    private BackupProgress(Parcel in) {
        this.bytesExpected = in.readLong();
        this.bytesTransferred = in.readLong();
    }
}

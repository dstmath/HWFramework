package android.telephony.mbms;

import android.annotation.SystemApi;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import java.util.Objects;

public final class FileInfo implements Parcelable {
    public static final Parcelable.Creator<FileInfo> CREATOR = new Parcelable.Creator<FileInfo>() {
        /* class android.telephony.mbms.FileInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public FileInfo createFromParcel(Parcel source) {
            return new FileInfo(source);
        }

        @Override // android.os.Parcelable.Creator
        public FileInfo[] newArray(int size) {
            return new FileInfo[size];
        }
    };
    private final String mimeType;
    private final Uri uri;

    @SystemApi
    public FileInfo(Uri uri2, String mimeType2) {
        this.uri = uri2;
        this.mimeType = mimeType2;
    }

    private FileInfo(Parcel in) {
        this.uri = (Uri) in.readParcelable(null);
        this.mimeType = in.readString();
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.uri, flags);
        dest.writeString(this.mimeType);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public Uri getUri() {
        return this.uri;
    }

    public String getMimeType() {
        return this.mimeType;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FileInfo fileInfo = (FileInfo) o;
        if (!Objects.equals(this.uri, fileInfo.uri) || !Objects.equals(this.mimeType, fileInfo.mimeType)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return Objects.hash(this.uri, this.mimeType);
    }
}

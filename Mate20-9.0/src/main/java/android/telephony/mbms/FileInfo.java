package android.telephony.mbms;

import android.annotation.SystemApi;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import java.util.Objects;

public final class FileInfo implements Parcelable {
    public static final Parcelable.Creator<FileInfo> CREATOR = new Parcelable.Creator<FileInfo>() {
        public FileInfo createFromParcel(Parcel source) {
            return new FileInfo(source);
        }

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

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.uri, flags);
        dest.writeString(this.mimeType);
    }

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
        boolean z = true;
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FileInfo fileInfo = (FileInfo) o;
        if (!Objects.equals(this.uri, fileInfo.uri) || !Objects.equals(this.mimeType, fileInfo.mimeType)) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.uri, this.mimeType});
    }
}

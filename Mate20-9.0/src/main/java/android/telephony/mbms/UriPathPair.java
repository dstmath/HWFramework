package android.telephony.mbms;

import android.annotation.SystemApi;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

@SystemApi
public final class UriPathPair implements Parcelable {
    public static final Parcelable.Creator<UriPathPair> CREATOR = new Parcelable.Creator<UriPathPair>() {
        public UriPathPair createFromParcel(Parcel in) {
            return new UriPathPair(in);
        }

        public UriPathPair[] newArray(int size) {
            return new UriPathPair[size];
        }
    };
    private final Uri mContentUri;
    private final Uri mFilePathUri;

    public UriPathPair(Uri fileUri, Uri contentUri) {
        if (fileUri == null || !"file".equals(fileUri.getScheme())) {
            throw new IllegalArgumentException("File URI must have file scheme");
        } else if (contentUri == null || !"content".equals(contentUri.getScheme())) {
            throw new IllegalArgumentException("Content URI must have content scheme");
        } else {
            this.mFilePathUri = fileUri;
            this.mContentUri = contentUri;
        }
    }

    private UriPathPair(Parcel in) {
        this.mFilePathUri = (Uri) in.readParcelable(Uri.class.getClassLoader());
        this.mContentUri = (Uri) in.readParcelable(Uri.class.getClassLoader());
    }

    public Uri getFilePathUri() {
        return this.mFilePathUri;
    }

    public Uri getContentUri() {
        return this.mContentUri;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.mFilePathUri, flags);
        dest.writeParcelable(this.mContentUri, flags);
    }
}

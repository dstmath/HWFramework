package android.content;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public final class UriPermission implements Parcelable {
    public static final Creator<UriPermission> CREATOR = new Creator<UriPermission>() {
        public UriPermission createFromParcel(Parcel source) {
            return new UriPermission(source);
        }

        public UriPermission[] newArray(int size) {
            return new UriPermission[size];
        }
    };
    public static final long INVALID_TIME = Long.MIN_VALUE;
    private final int mModeFlags;
    private final long mPersistedTime;
    private final Uri mUri;

    public UriPermission(Uri uri, int modeFlags, long persistedTime) {
        this.mUri = uri;
        this.mModeFlags = modeFlags;
        this.mPersistedTime = persistedTime;
    }

    public UriPermission(Parcel in) {
        this.mUri = (Uri) in.readParcelable(null);
        this.mModeFlags = in.readInt();
        this.mPersistedTime = in.readLong();
    }

    public Uri getUri() {
        return this.mUri;
    }

    public boolean isReadPermission() {
        return (this.mModeFlags & 1) != 0;
    }

    public boolean isWritePermission() {
        return (this.mModeFlags & 2) != 0;
    }

    public long getPersistedTime() {
        return this.mPersistedTime;
    }

    public String toString() {
        return "UriPermission {uri=" + this.mUri + ", modeFlags=" + this.mModeFlags + ", persistedTime=" + this.mPersistedTime + "}";
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.mUri, flags);
        dest.writeInt(this.mModeFlags);
        dest.writeLong(this.mPersistedTime);
    }
}

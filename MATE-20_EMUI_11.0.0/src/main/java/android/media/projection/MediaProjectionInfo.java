package android.media.projection;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.UserHandle;
import java.util.Objects;

public final class MediaProjectionInfo implements Parcelable {
    public static final Parcelable.Creator<MediaProjectionInfo> CREATOR = new Parcelable.Creator<MediaProjectionInfo>() {
        /* class android.media.projection.MediaProjectionInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public MediaProjectionInfo createFromParcel(Parcel in) {
            return new MediaProjectionInfo(in);
        }

        @Override // android.os.Parcelable.Creator
        public MediaProjectionInfo[] newArray(int size) {
            return new MediaProjectionInfo[size];
        }
    };
    private final String mPackageName;
    private final UserHandle mUserHandle;

    public MediaProjectionInfo(String packageName, UserHandle handle) {
        this.mPackageName = packageName;
        this.mUserHandle = handle;
    }

    public MediaProjectionInfo(Parcel in) {
        this.mPackageName = in.readString();
        this.mUserHandle = UserHandle.readFromParcel(in);
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public UserHandle getUserHandle() {
        return this.mUserHandle;
    }

    public boolean equals(Object o) {
        if (!(o instanceof MediaProjectionInfo)) {
            return false;
        }
        MediaProjectionInfo other = (MediaProjectionInfo) o;
        if (!Objects.equals(other.mPackageName, this.mPackageName) || !Objects.equals(other.mUserHandle, this.mUserHandle)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return Objects.hash(this.mPackageName, this.mUserHandle);
    }

    public String toString() {
        return "MediaProjectionInfo{mPackageName=" + this.mPackageName + ", mUserHandle=" + this.mUserHandle + "}";
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.mPackageName);
        UserHandle.writeToParcel(this.mUserHandle, out);
    }
}

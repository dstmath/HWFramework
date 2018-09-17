package android.media.projection;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.UserHandle;
import java.util.Objects;

public final class MediaProjectionInfo implements Parcelable {
    public static final Creator<MediaProjectionInfo> CREATOR = new Creator<MediaProjectionInfo>() {
        public MediaProjectionInfo createFromParcel(Parcel in) {
            return new MediaProjectionInfo(in);
        }

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
        boolean z = false;
        if (!(o instanceof MediaProjectionInfo)) {
            return false;
        }
        MediaProjectionInfo other = (MediaProjectionInfo) o;
        if (Objects.equals(other.mPackageName, this.mPackageName)) {
            z = Objects.equals(other.mUserHandle, this.mUserHandle);
        }
        return z;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.mPackageName, this.mUserHandle});
    }

    public String toString() {
        return "MediaProjectionInfo{mPackageName=" + this.mPackageName + ", mUserHandle=" + this.mUserHandle + "}";
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.mPackageName);
        UserHandle.writeToParcel(this.mUserHandle, out);
    }
}

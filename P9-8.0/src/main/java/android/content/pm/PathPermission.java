package android.content.pm;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import android.os.PatternMatcher;

public class PathPermission extends PatternMatcher {
    public static final Creator<PathPermission> CREATOR = new Creator<PathPermission>() {
        public PathPermission createFromParcel(Parcel source) {
            return new PathPermission(source);
        }

        public PathPermission[] newArray(int size) {
            return new PathPermission[size];
        }
    };
    private final String mReadPermission;
    private final String mWritePermission;

    public PathPermission(String pattern, int type, String readPermission, String writePermission) {
        super(pattern, type);
        this.mReadPermission = readPermission;
        this.mWritePermission = writePermission;
    }

    public String getReadPermission() {
        return this.mReadPermission;
    }

    public String getWritePermission() {
        return this.mWritePermission;
    }

    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(this.mReadPermission);
        dest.writeString(this.mWritePermission);
    }

    public PathPermission(Parcel src) {
        super(src);
        this.mReadPermission = src.readString();
        this.mWritePermission = src.readString();
    }
}

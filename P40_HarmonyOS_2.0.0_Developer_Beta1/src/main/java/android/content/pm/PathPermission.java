package android.content.pm;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.PatternMatcher;

public class PathPermission extends PatternMatcher {
    public static final Parcelable.Creator<PathPermission> CREATOR = new Parcelable.Creator<PathPermission>() {
        /* class android.content.pm.PathPermission.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public PathPermission createFromParcel(Parcel source) {
            return new PathPermission(source);
        }

        @Override // android.os.Parcelable.Creator
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

    @Override // android.os.PatternMatcher, android.os.Parcelable
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

package android.content.res;

import android.annotation.UnsupportedAppUsage;
import android.os.Parcel;
import android.os.Parcelable;

public class ObbInfo implements Parcelable {
    public static final Parcelable.Creator<ObbInfo> CREATOR = new Parcelable.Creator<ObbInfo>() {
        /* class android.content.res.ObbInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ObbInfo createFromParcel(Parcel source) {
            return new ObbInfo(source);
        }

        @Override // android.os.Parcelable.Creator
        public ObbInfo[] newArray(int size) {
            return new ObbInfo[size];
        }
    };
    public static final int OBB_OVERLAY = 1;
    public String filename;
    public int flags;
    public String packageName;
    @UnsupportedAppUsage
    public byte[] salt;
    public int version;

    ObbInfo() {
    }

    public String toString() {
        return "ObbInfo{" + Integer.toHexString(System.identityHashCode(this)) + " packageName=" + this.packageName + ",version=" + this.version + ",flags=" + this.flags + '}';
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int parcelableFlags) {
        dest.writeString(this.filename);
        dest.writeString(this.packageName);
        dest.writeInt(this.version);
        dest.writeInt(this.flags);
        dest.writeByteArray(this.salt);
    }

    private ObbInfo(Parcel source) {
        this.filename = source.readString();
        this.packageName = source.readString();
        this.version = source.readInt();
        this.flags = source.readInt();
        this.salt = source.createByteArray();
    }
}

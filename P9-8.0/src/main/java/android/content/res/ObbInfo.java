package android.content.res;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class ObbInfo implements Parcelable {
    public static final Creator<ObbInfo> CREATOR = new Creator<ObbInfo>() {
        public ObbInfo createFromParcel(Parcel source) {
            return new ObbInfo(source, null);
        }

        public ObbInfo[] newArray(int size) {
            return new ObbInfo[size];
        }
    };
    public static final int OBB_OVERLAY = 1;
    public String filename;
    public int flags;
    public String packageName;
    public byte[] salt;
    public int version;

    /* synthetic */ ObbInfo(Parcel source, ObbInfo -this1) {
        this(source);
    }

    ObbInfo() {
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ObbInfo{");
        sb.append(Integer.toHexString(System.identityHashCode(this)));
        sb.append(" packageName=");
        sb.append(this.packageName);
        sb.append(",version=");
        sb.append(this.version);
        sb.append(",flags=");
        sb.append(this.flags);
        sb.append('}');
        return sb.toString();
    }

    public int describeContents() {
        return 0;
    }

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

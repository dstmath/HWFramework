package android.content.pm;

import android.net.wifi.WifiEnterpriseConfig;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.proto.ProtoOutputStream;

public class FeatureInfo implements Parcelable {
    public static final Parcelable.Creator<FeatureInfo> CREATOR = new Parcelable.Creator<FeatureInfo>() {
        /* class android.content.pm.FeatureInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public FeatureInfo createFromParcel(Parcel source) {
            return new FeatureInfo(source);
        }

        @Override // android.os.Parcelable.Creator
        public FeatureInfo[] newArray(int size) {
            return new FeatureInfo[size];
        }
    };
    public static final int FLAG_REQUIRED = 1;
    public static final int GL_ES_VERSION_UNDEFINED = 0;
    public int flags;
    public String name;
    public int reqGlEsVersion;
    public int version;

    public FeatureInfo() {
    }

    public FeatureInfo(FeatureInfo orig) {
        this.name = orig.name;
        this.version = orig.version;
        this.reqGlEsVersion = orig.reqGlEsVersion;
        this.flags = orig.flags;
    }

    public String toString() {
        if (this.name != null) {
            return "FeatureInfo{" + Integer.toHexString(System.identityHashCode(this)) + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + this.name + " v=" + this.version + " fl=0x" + Integer.toHexString(this.flags) + "}";
        }
        return "FeatureInfo{" + Integer.toHexString(System.identityHashCode(this)) + " glEsVers=" + getGlEsVersion() + " fl=0x" + Integer.toHexString(this.flags) + "}";
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int parcelableFlags) {
        dest.writeString(this.name);
        dest.writeInt(this.version);
        dest.writeInt(this.reqGlEsVersion);
        dest.writeInt(this.flags);
    }

    public void writeToProto(ProtoOutputStream proto, long fieldId) {
        long token = proto.start(fieldId);
        String str = this.name;
        if (str != null) {
            proto.write(1138166333441L, str);
        }
        proto.write(1120986464258L, this.version);
        proto.write(1138166333443L, getGlEsVersion());
        proto.write(1120986464260L, this.flags);
        proto.end(token);
    }

    private FeatureInfo(Parcel source) {
        this.name = source.readString();
        this.version = source.readInt();
        this.reqGlEsVersion = source.readInt();
        this.flags = source.readInt();
    }

    public String getGlEsVersion() {
        int i = this.reqGlEsVersion;
        return String.valueOf((-65536 & i) >> 16) + "." + String.valueOf(i & 65535);
    }
}

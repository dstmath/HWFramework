package android.content.pm;

import android.graphics.Color;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class FeatureInfo implements Parcelable {
    public static final Creator<FeatureInfo> CREATOR = new Creator<FeatureInfo>() {
        public FeatureInfo createFromParcel(Parcel source) {
            return new FeatureInfo(source, null);
        }

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

    /* synthetic */ FeatureInfo(Parcel source, FeatureInfo -this1) {
        this(source);
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

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int parcelableFlags) {
        dest.writeString(this.name);
        dest.writeInt(this.version);
        dest.writeInt(this.reqGlEsVersion);
        dest.writeInt(this.flags);
    }

    private FeatureInfo(Parcel source) {
        this.name = source.readString();
        this.version = source.readInt();
        this.reqGlEsVersion = source.readInt();
        this.flags = source.readInt();
    }

    public String getGlEsVersion() {
        return String.valueOf((this.reqGlEsVersion & Color.RED) >> 16) + "." + String.valueOf(this.reqGlEsVersion & 65535);
    }
}

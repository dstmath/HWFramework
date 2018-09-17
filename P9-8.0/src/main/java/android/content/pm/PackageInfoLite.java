package android.content.pm;

import android.net.wifi.WifiEnterpriseConfig;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class PackageInfoLite implements Parcelable {
    public static final Creator<PackageInfoLite> CREATOR = new Creator<PackageInfoLite>() {
        public PackageInfoLite createFromParcel(Parcel source) {
            return new PackageInfoLite(source, null);
        }

        public PackageInfoLite[] newArray(int size) {
            return new PackageInfoLite[size];
        }
    };
    public int baseRevisionCode;
    public int installLocation;
    public boolean multiArch;
    public String packageName;
    public int recommendedInstallLocation;
    public String[] splitNames;
    public int[] splitRevisionCodes;
    public VerifierInfo[] verifiers;
    public int versionCode;

    /* synthetic */ PackageInfoLite(Parcel source, PackageInfoLite -this1) {
        this(source);
    }

    public String toString() {
        return "PackageInfoLite{" + Integer.toHexString(System.identityHashCode(this)) + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + this.packageName + "}";
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int parcelableFlags) {
        dest.writeString(this.packageName);
        dest.writeStringArray(this.splitNames);
        dest.writeInt(this.versionCode);
        dest.writeInt(this.baseRevisionCode);
        dest.writeIntArray(this.splitRevisionCodes);
        dest.writeInt(this.recommendedInstallLocation);
        dest.writeInt(this.installLocation);
        dest.writeInt(this.multiArch ? 1 : 0);
        if (this.verifiers == null || this.verifiers.length == 0) {
            dest.writeInt(0);
            return;
        }
        dest.writeInt(this.verifiers.length);
        dest.writeTypedArray(this.verifiers, parcelableFlags);
    }

    private PackageInfoLite(Parcel source) {
        boolean z;
        this.packageName = source.readString();
        this.splitNames = source.createStringArray();
        this.versionCode = source.readInt();
        this.baseRevisionCode = source.readInt();
        this.splitRevisionCodes = source.createIntArray();
        this.recommendedInstallLocation = source.readInt();
        this.installLocation = source.readInt();
        if (source.readInt() != 0) {
            z = true;
        } else {
            z = false;
        }
        this.multiArch = z;
        int verifiersLength = source.readInt();
        if (verifiersLength == 0) {
            this.verifiers = new VerifierInfo[0];
            return;
        }
        this.verifiers = new VerifierInfo[verifiersLength];
        source.readTypedArray(this.verifiers, VerifierInfo.CREATOR);
    }
}

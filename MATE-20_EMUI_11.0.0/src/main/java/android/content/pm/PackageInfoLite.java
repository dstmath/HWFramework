package android.content.pm;

import android.annotation.UnsupportedAppUsage;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Parcel;
import android.os.Parcelable;

public class PackageInfoLite implements Parcelable {
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    public static final Parcelable.Creator<PackageInfoLite> CREATOR = new Parcelable.Creator<PackageInfoLite>() {
        /* class android.content.pm.PackageInfoLite.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public PackageInfoLite createFromParcel(Parcel source) {
            return new PackageInfoLite(source);
        }

        @Override // android.os.Parcelable.Creator
        public PackageInfoLite[] newArray(int size) {
            return new PackageInfoLite[size];
        }
    };
    public int baseRevisionCode;
    public boolean hasPlugin;
    public int installLocation;
    public boolean multiArch;
    public String packageName;
    public int recommendedInstallLocation;
    public String[] splitNames;
    public int[] splitRevisionCodes;
    public int[] splitVersionCodes;
    public VerifierInfo[] verifiers;
    @Deprecated
    public int versionCode;
    public int versionCodeMajor;

    public long getLongVersionCode() {
        return PackageInfo.composeLongVersionCode(this.versionCodeMajor, this.versionCode);
    }

    public PackageInfoLite() {
    }

    public String toString() {
        return "PackageInfoLite{" + Integer.toHexString(System.identityHashCode(this)) + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + this.packageName + "}";
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int parcelableFlags) {
        dest.writeString(this.packageName);
        dest.writeStringArray(this.splitNames);
        dest.writeInt(this.versionCode);
        dest.writeInt(this.versionCodeMajor);
        dest.writeInt(this.baseRevisionCode);
        dest.writeIntArray(this.splitRevisionCodes);
        dest.writeInt(this.recommendedInstallLocation);
        dest.writeInt(this.installLocation);
        dest.writeInt(this.multiArch ? 1 : 0);
        VerifierInfo[] verifierInfoArr = this.verifiers;
        if (verifierInfoArr == null || verifierInfoArr.length == 0) {
            dest.writeInt(0);
        } else {
            dest.writeInt(verifierInfoArr.length);
            dest.writeTypedArray(this.verifiers, parcelableFlags);
        }
        dest.writeBoolean(this.hasPlugin);
        dest.writeIntArray(this.splitVersionCodes);
    }

    private PackageInfoLite(Parcel source) {
        this.packageName = source.readString();
        this.splitNames = source.createStringArray();
        this.versionCode = source.readInt();
        this.versionCodeMajor = source.readInt();
        this.baseRevisionCode = source.readInt();
        this.splitRevisionCodes = source.createIntArray();
        this.recommendedInstallLocation = source.readInt();
        this.installLocation = source.readInt();
        this.multiArch = source.readInt() != 0;
        int verifiersLength = source.readInt();
        if (verifiersLength == 0) {
            this.verifiers = new VerifierInfo[0];
        } else {
            this.verifiers = new VerifierInfo[verifiersLength];
            source.readTypedArray(this.verifiers, VerifierInfo.CREATOR);
        }
        this.hasPlugin = source.readBoolean();
        this.splitVersionCodes = source.createIntArray();
    }
}

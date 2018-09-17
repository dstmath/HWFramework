package android.content.pm;

import android.net.wifi.WifiEnterpriseConfig;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.SparseArray;

public class InstrumentationInfo extends PackageItemInfo implements Parcelable {
    public static final Creator<InstrumentationInfo> CREATOR = new Creator<InstrumentationInfo>() {
        public InstrumentationInfo createFromParcel(Parcel source) {
            return new InstrumentationInfo(source, null);
        }

        public InstrumentationInfo[] newArray(int size) {
            return new InstrumentationInfo[size];
        }
    };
    public String credentialProtectedDataDir;
    public String dataDir;
    public String deviceProtectedDataDir;
    public boolean functionalTest;
    public boolean handleProfiling;
    public String nativeLibraryDir;
    public String publicSourceDir;
    public String secondaryNativeLibraryDir;
    public String sourceDir;
    public SparseArray<int[]> splitDependencies;
    public String[] splitNames;
    public String[] splitPublicSourceDirs;
    public String[] splitSourceDirs;
    public String targetPackage;
    public String targetProcesses;

    /* synthetic */ InstrumentationInfo(Parcel source, InstrumentationInfo -this1) {
        this(source);
    }

    public InstrumentationInfo(InstrumentationInfo orig) {
        super((PackageItemInfo) orig);
        this.targetPackage = orig.targetPackage;
        this.targetProcesses = orig.targetProcesses;
        this.sourceDir = orig.sourceDir;
        this.publicSourceDir = orig.publicSourceDir;
        this.splitNames = orig.splitNames;
        this.splitSourceDirs = orig.splitSourceDirs;
        this.splitPublicSourceDirs = orig.splitPublicSourceDirs;
        this.splitDependencies = orig.splitDependencies;
        this.dataDir = orig.dataDir;
        this.deviceProtectedDataDir = orig.deviceProtectedDataDir;
        this.credentialProtectedDataDir = orig.credentialProtectedDataDir;
        this.nativeLibraryDir = orig.nativeLibraryDir;
        this.secondaryNativeLibraryDir = orig.secondaryNativeLibraryDir;
        this.handleProfiling = orig.handleProfiling;
        this.functionalTest = orig.functionalTest;
    }

    public String toString() {
        return "InstrumentationInfo{" + Integer.toHexString(System.identityHashCode(this)) + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + this.packageName + "}";
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int parcelableFlags) {
        int i;
        int i2 = 0;
        super.writeToParcel(dest, parcelableFlags);
        dest.writeString(this.targetPackage);
        dest.writeString(this.targetProcesses);
        dest.writeString(this.sourceDir);
        dest.writeString(this.publicSourceDir);
        dest.writeStringArray(this.splitNames);
        dest.writeStringArray(this.splitSourceDirs);
        dest.writeStringArray(this.splitPublicSourceDirs);
        dest.writeSparseArray(this.splitDependencies);
        dest.writeString(this.dataDir);
        dest.writeString(this.deviceProtectedDataDir);
        dest.writeString(this.credentialProtectedDataDir);
        dest.writeString(this.nativeLibraryDir);
        dest.writeString(this.secondaryNativeLibraryDir);
        if (this.handleProfiling) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.functionalTest) {
            i2 = 1;
        }
        dest.writeInt(i2);
    }

    private InstrumentationInfo(Parcel source) {
        boolean z;
        boolean z2 = true;
        super(source);
        this.targetPackage = source.readString();
        this.targetProcesses = source.readString();
        this.sourceDir = source.readString();
        this.publicSourceDir = source.readString();
        this.splitNames = source.readStringArray();
        this.splitSourceDirs = source.readStringArray();
        this.splitPublicSourceDirs = source.readStringArray();
        this.splitDependencies = source.readSparseArray(null);
        this.dataDir = source.readString();
        this.deviceProtectedDataDir = source.readString();
        this.credentialProtectedDataDir = source.readString();
        this.nativeLibraryDir = source.readString();
        this.secondaryNativeLibraryDir = source.readString();
        if (source.readInt() != 0) {
            z = true;
        } else {
            z = false;
        }
        this.handleProfiling = z;
        if (source.readInt() == 0) {
            z2 = false;
        }
        this.functionalTest = z2;
    }

    public void copyTo(ApplicationInfo ai) {
        ai.packageName = this.packageName;
        ai.sourceDir = this.sourceDir;
        ai.publicSourceDir = this.publicSourceDir;
        ai.splitNames = this.splitNames;
        ai.splitSourceDirs = this.splitSourceDirs;
        ai.splitPublicSourceDirs = this.splitPublicSourceDirs;
        ai.splitDependencies = this.splitDependencies;
        ai.dataDir = this.dataDir;
        ai.deviceProtectedDataDir = this.deviceProtectedDataDir;
        ai.credentialProtectedDataDir = this.credentialProtectedDataDir;
        ai.nativeLibraryDir = this.nativeLibraryDir;
        ai.secondaryNativeLibraryDir = this.secondaryNativeLibraryDir;
    }
}

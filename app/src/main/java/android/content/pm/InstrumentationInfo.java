package android.content.pm;

import android.net.wifi.WifiEnterpriseConfig;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class InstrumentationInfo extends PackageItemInfo implements Parcelable {
    public static final Creator<InstrumentationInfo> CREATOR = null;
    public String credentialProtectedDataDir;
    public String dataDir;
    public String deviceProtectedDataDir;
    public boolean functionalTest;
    public boolean handleProfiling;
    public String nativeLibraryDir;
    public String publicSourceDir;
    public String secondaryNativeLibraryDir;
    public String sourceDir;
    public String[] splitPublicSourceDirs;
    public String[] splitSourceDirs;
    public String targetPackage;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.content.pm.InstrumentationInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.content.pm.InstrumentationInfo.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.content.pm.InstrumentationInfo.<clinit>():void");
    }

    public InstrumentationInfo(InstrumentationInfo orig) {
        super((PackageItemInfo) orig);
        this.targetPackage = orig.targetPackage;
        this.sourceDir = orig.sourceDir;
        this.publicSourceDir = orig.publicSourceDir;
        this.splitSourceDirs = orig.splitSourceDirs;
        this.splitPublicSourceDirs = orig.splitPublicSourceDirs;
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
        int i = 0;
        super.writeToParcel(dest, parcelableFlags);
        dest.writeString(this.targetPackage);
        dest.writeString(this.sourceDir);
        dest.writeString(this.publicSourceDir);
        dest.writeStringArray(this.splitSourceDirs);
        dest.writeStringArray(this.splitPublicSourceDirs);
        dest.writeString(this.dataDir);
        dest.writeString(this.deviceProtectedDataDir);
        dest.writeString(this.credentialProtectedDataDir);
        dest.writeString(this.nativeLibraryDir);
        dest.writeString(this.secondaryNativeLibraryDir);
        dest.writeInt(!this.handleProfiling ? 0 : 1);
        if (this.functionalTest) {
            i = 1;
        }
        dest.writeInt(i);
    }

    private InstrumentationInfo(Parcel source) {
        boolean z = true;
        super(source);
        this.targetPackage = source.readString();
        this.sourceDir = source.readString();
        this.publicSourceDir = source.readString();
        this.splitSourceDirs = source.readStringArray();
        this.splitPublicSourceDirs = source.readStringArray();
        this.dataDir = source.readString();
        this.deviceProtectedDataDir = source.readString();
        this.credentialProtectedDataDir = source.readString();
        this.nativeLibraryDir = source.readString();
        this.secondaryNativeLibraryDir = source.readString();
        this.handleProfiling = source.readInt() != 0;
        if (source.readInt() == 0) {
            z = false;
        }
        this.functionalTest = z;
    }

    public void copyTo(ApplicationInfo ai) {
        ai.packageName = this.packageName;
        ai.sourceDir = this.sourceDir;
        ai.publicSourceDir = this.publicSourceDir;
        ai.splitSourceDirs = this.splitSourceDirs;
        ai.splitPublicSourceDirs = this.splitPublicSourceDirs;
        ai.dataDir = this.dataDir;
        ai.deviceProtectedDataDir = this.deviceProtectedDataDir;
        ai.credentialProtectedDataDir = this.credentialProtectedDataDir;
        ai.nativeLibraryDir = this.nativeLibraryDir;
        ai.secondaryNativeLibraryDir = this.secondaryNativeLibraryDir;
    }
}

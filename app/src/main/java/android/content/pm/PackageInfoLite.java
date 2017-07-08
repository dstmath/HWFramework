package android.content.pm;

import android.net.wifi.WifiEnterpriseConfig;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class PackageInfoLite implements Parcelable {
    public static final Creator<PackageInfoLite> CREATOR = null;
    public int baseRevisionCode;
    public int installLocation;
    public boolean multiArch;
    public String packageName;
    public int recommendedInstallLocation;
    public String[] splitNames;
    public int[] splitRevisionCodes;
    public VerifierInfo[] verifiers;
    public int versionCode;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.content.pm.PackageInfoLite.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.content.pm.PackageInfoLite.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.content.pm.PackageInfoLite.<clinit>():void");
    }

    public String toString() {
        return "PackageInfoLite{" + Integer.toHexString(System.identityHashCode(this)) + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + this.packageName + "}";
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int parcelableFlags) {
        int i;
        dest.writeString(this.packageName);
        dest.writeStringArray(this.splitNames);
        dest.writeInt(this.versionCode);
        dest.writeInt(this.baseRevisionCode);
        dest.writeIntArray(this.splitRevisionCodes);
        dest.writeInt(this.recommendedInstallLocation);
        dest.writeInt(this.installLocation);
        if (this.multiArch) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
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

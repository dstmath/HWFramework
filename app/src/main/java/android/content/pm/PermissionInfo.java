package android.content.pm;

import android.net.wifi.WifiEnterpriseConfig;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.TextUtils;

public class PermissionInfo extends PackageItemInfo implements Parcelable {
    public static final Creator<PermissionInfo> CREATOR = null;
    public static final int FLAG_COSTS_MONEY = 1;
    public static final int FLAG_INSTALLED = 1073741824;
    public static final int FLAG_REMOVED = 2;
    public static final int PROTECTION_DANGEROUS = 1;
    public static final int PROTECTION_FLAG_APPOP = 64;
    public static final int PROTECTION_FLAG_DEVELOPMENT = 32;
    public static final int PROTECTION_FLAG_INSTALLER = 256;
    public static final int PROTECTION_FLAG_PRE23 = 128;
    public static final int PROTECTION_FLAG_PREINSTALLED = 1024;
    public static final int PROTECTION_FLAG_PRIVILEGED = 16;
    public static final int PROTECTION_FLAG_SETUP = 2048;
    @Deprecated
    public static final int PROTECTION_FLAG_SYSTEM = 16;
    public static final int PROTECTION_FLAG_VERIFIER = 512;
    public static final int PROTECTION_MASK_BASE = 15;
    public static final int PROTECTION_MASK_FLAGS = 4080;
    public static final int PROTECTION_NORMAL = 0;
    public static final int PROTECTION_SIGNATURE = 2;
    @Deprecated
    public static final int PROTECTION_SIGNATURE_OR_SYSTEM = 3;
    public int descriptionRes;
    public int flags;
    public String group;
    public CharSequence nonLocalizedDescription;
    public int protectionLevel;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.content.pm.PermissionInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.content.pm.PermissionInfo.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.content.pm.PermissionInfo.<clinit>():void");
    }

    public static int fixProtectionLevel(int level) {
        if (level == PROTECTION_SIGNATURE_OR_SYSTEM) {
            return 18;
        }
        return level;
    }

    public static String protectionToString(int level) {
        String protLevel = "????";
        switch (level & PROTECTION_MASK_BASE) {
            case PROTECTION_NORMAL /*0*/:
                protLevel = "normal";
                break;
            case PROTECTION_DANGEROUS /*1*/:
                protLevel = "dangerous";
                break;
            case PROTECTION_SIGNATURE /*2*/:
                protLevel = "signature";
                break;
            case PROTECTION_SIGNATURE_OR_SYSTEM /*3*/:
                protLevel = "signatureOrSystem";
                break;
        }
        if ((level & PROTECTION_FLAG_SYSTEM) != 0) {
            protLevel = protLevel + "|privileged";
        }
        if ((level & PROTECTION_FLAG_DEVELOPMENT) != 0) {
            protLevel = protLevel + "|development";
        }
        if ((level & PROTECTION_FLAG_APPOP) != 0) {
            protLevel = protLevel + "|appop";
        }
        if ((level & PROTECTION_FLAG_PRE23) != 0) {
            protLevel = protLevel + "|pre23";
        }
        if ((level & PROTECTION_FLAG_INSTALLER) != 0) {
            protLevel = protLevel + "|installer";
        }
        if ((level & PROTECTION_FLAG_VERIFIER) != 0) {
            protLevel = protLevel + "|verifier";
        }
        if ((level & PROTECTION_FLAG_PREINSTALLED) != 0) {
            protLevel = protLevel + "|preinstalled";
        }
        if ((level & PROTECTION_FLAG_SETUP) != 0) {
            return protLevel + "|setup";
        }
        return protLevel;
    }

    public PermissionInfo(PermissionInfo orig) {
        super((PackageItemInfo) orig);
        this.protectionLevel = orig.protectionLevel;
        this.flags = orig.flags;
        this.group = orig.group;
        this.descriptionRes = orig.descriptionRes;
        this.nonLocalizedDescription = orig.nonLocalizedDescription;
    }

    public CharSequence loadDescription(PackageManager pm) {
        if (this.nonLocalizedDescription != null) {
            return this.nonLocalizedDescription;
        }
        if (this.descriptionRes != 0) {
            CharSequence label = pm.getText(this.packageName, this.descriptionRes, null);
            if (label != null) {
                return label;
            }
        }
        return null;
    }

    public String toString() {
        return "PermissionInfo{" + Integer.toHexString(System.identityHashCode(this)) + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + this.name + "}";
    }

    public int describeContents() {
        return PROTECTION_NORMAL;
    }

    public void writeToParcel(Parcel dest, int parcelableFlags) {
        super.writeToParcel(dest, parcelableFlags);
        dest.writeInt(this.protectionLevel);
        dest.writeInt(this.flags);
        dest.writeString(this.group);
        dest.writeInt(this.descriptionRes);
        TextUtils.writeToParcel(this.nonLocalizedDescription, dest, parcelableFlags);
    }

    private PermissionInfo(Parcel source) {
        super(source);
        this.protectionLevel = source.readInt();
        this.flags = source.readInt();
        this.group = source.readString();
        this.descriptionRes = source.readInt();
        this.nonLocalizedDescription = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(source);
    }
}

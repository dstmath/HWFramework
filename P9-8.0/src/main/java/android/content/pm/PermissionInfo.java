package android.content.pm;

import android.net.wifi.WifiEnterpriseConfig;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.TextUtils;

public class PermissionInfo extends PackageItemInfo implements Parcelable {
    public static final Creator<PermissionInfo> CREATOR = new Creator<PermissionInfo>() {
        public PermissionInfo createFromParcel(Parcel source) {
            return new PermissionInfo(source, null);
        }

        public PermissionInfo[] newArray(int size) {
            return new PermissionInfo[size];
        }
    };
    public static final int FLAG_COSTS_MONEY = 1;
    public static final int FLAG_INSTALLED = 1073741824;
    public static final int FLAG_REMOVED = 2;
    public static final int PROTECTION_DANGEROUS = 1;
    public static final int PROTECTION_FLAG_APPOP = 64;
    public static final int PROTECTION_FLAG_DEVELOPMENT = 32;
    public static final int PROTECTION_FLAG_EPHEMERAL = 4096;
    public static final int PROTECTION_FLAG_INSTALLER = 256;
    public static final int PROTECTION_FLAG_PRE23 = 128;
    public static final int PROTECTION_FLAG_PREINSTALLED = 1024;
    public static final int PROTECTION_FLAG_PRIVILEGED = 16;
    public static final int PROTECTION_FLAG_RUNTIME_ONLY = 8192;
    public static final int PROTECTION_FLAG_SETUP = 2048;
    @Deprecated
    public static final int PROTECTION_FLAG_SYSTEM = 16;
    public static final int PROTECTION_FLAG_VERIFIER = 512;
    public static final int PROTECTION_MASK_BASE = 15;
    public static final int PROTECTION_MASK_FLAGS = 65520;
    public static final int PROTECTION_NORMAL = 0;
    public static final int PROTECTION_SIGNATURE = 2;
    @Deprecated
    public static final int PROTECTION_SIGNATURE_OR_SYSTEM = 3;
    public int descriptionRes;
    public int flags;
    public String group;
    public CharSequence nonLocalizedDescription;
    public int protectionLevel;

    /* synthetic */ PermissionInfo(Parcel source, PermissionInfo -this1) {
        this(source);
    }

    public static int fixProtectionLevel(int level) {
        if (level == 3) {
            return 18;
        }
        return level;
    }

    public static String protectionToString(int level) {
        String protLevel = "????";
        switch (level & 15) {
            case 0:
                protLevel = "normal";
                break;
            case 1:
                protLevel = "dangerous";
                break;
            case 2:
                protLevel = "signature";
                break;
            case 3:
                protLevel = "signatureOrSystem";
                break;
        }
        if ((level & 16) != 0) {
            protLevel = protLevel + "|privileged";
        }
        if ((level & 32) != 0) {
            protLevel = protLevel + "|development";
        }
        if ((level & 64) != 0) {
            protLevel = protLevel + "|appop";
        }
        if ((level & 128) != 0) {
            protLevel = protLevel + "|pre23";
        }
        if ((level & 256) != 0) {
            protLevel = protLevel + "|installer";
        }
        if ((level & 512) != 0) {
            protLevel = protLevel + "|verifier";
        }
        if ((level & 1024) != 0) {
            protLevel = protLevel + "|preinstalled";
        }
        if ((level & 2048) != 0) {
            protLevel = protLevel + "|setup";
        }
        if ((level & 4096) != 0) {
            protLevel = protLevel + "|ephemeral";
        }
        if ((level & 8192) != 0) {
            return protLevel + "|runtime";
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
        return 0;
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

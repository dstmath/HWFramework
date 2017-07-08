package android.content.pm;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;

public class UserInfo implements Parcelable {
    public static final Creator<UserInfo> CREATOR = null;
    public static final int FLAG_ADMIN = 2;
    public static final int FLAG_DISABLED = 64;
    public static final int FLAG_EPHEMERAL = 256;
    public static final int FLAG_GUEST = 4;
    public static final int FLAG_HW_HIDDENSPACE = 33554432;
    public static final int FLAG_HW_TRUSTSPACE = 16777216;
    public static final int FLAG_INITIALIZED = 16;
    public static final int FLAG_MANAGED_PROFILE = 32;
    public static final int FLAG_MASK_USER_TYPE = 255;
    public static final int FLAG_PRIMARY = 1;
    public static final int FLAG_QUIET_MODE = 128;
    public static final int FLAG_RESTRICTED = 8;
    public static final int NO_PROFILE_GROUP_ID = -10000;
    public long creationTime;
    public int flags;
    public boolean guestToRemove;
    public String iconPath;
    public int id;
    public String lastLoggedInFingerprint;
    public long lastLoggedInTime;
    public String name;
    public boolean partial;
    public int profileGroupId;
    public int restrictedProfileParentId;
    public int serialNumber;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.content.pm.UserInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.content.pm.UserInfo.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.content.pm.UserInfo.<clinit>():void");
    }

    public UserInfo(int id, String name, int flags) {
        this(id, name, null, flags);
    }

    public UserInfo(int id, String name, String iconPath, int flags) {
        this.id = id;
        this.name = name;
        this.flags = flags;
        this.iconPath = iconPath;
        this.profileGroupId = NO_PROFILE_GROUP_ID;
        this.restrictedProfileParentId = NO_PROFILE_GROUP_ID;
    }

    public boolean isPrimary() {
        return (this.flags & FLAG_PRIMARY) == FLAG_PRIMARY;
    }

    public boolean isAdmin() {
        return (this.flags & FLAG_ADMIN) == FLAG_ADMIN;
    }

    public boolean isGuest() {
        return (this.flags & FLAG_GUEST) == FLAG_GUEST;
    }

    public boolean isRestricted() {
        return (this.flags & FLAG_RESTRICTED) == FLAG_RESTRICTED;
    }

    public boolean isManagedProfile() {
        return (this.flags & FLAG_MANAGED_PROFILE) == FLAG_MANAGED_PROFILE;
    }

    public boolean isHwTrustSpace() {
        return (this.flags & FLAG_HW_TRUSTSPACE) == FLAG_HW_TRUSTSPACE;
    }

    public boolean isEnabled() {
        return (this.flags & FLAG_DISABLED) != FLAG_DISABLED;
    }

    public boolean isQuietModeEnabled() {
        return (this.flags & FLAG_QUIET_MODE) == FLAG_QUIET_MODE;
    }

    public boolean isEphemeral() {
        return (this.flags & FLAG_EPHEMERAL) == FLAG_EPHEMERAL;
    }

    public boolean isInitialized() {
        return (this.flags & FLAG_INITIALIZED) == FLAG_INITIALIZED;
    }

    public boolean isHwHiddenSpace() {
        return (this.flags & FLAG_HW_HIDDENSPACE) == FLAG_HW_HIDDENSPACE;
    }

    public boolean isSystemOnly() {
        return isSystemOnly(this.id);
    }

    public static boolean isSystemOnly(int userId) {
        return userId == 0 ? UserManager.isSplitSystemUser() : false;
    }

    public boolean supportsSwitchTo() {
        if ((isEphemeral() && !isEnabled()) || isHwTrustSpace()) {
            return false;
        }
        return isManagedProfile() ? SystemProperties.getBoolean("fw.show_hidden_users", false) : true;
    }

    public boolean supportsSwitchToByUser() {
        if (UserManager.isSplitSystemUser() && this.id == 0) {
            return false;
        }
        return supportsSwitchTo();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean canHaveProfile() {
        boolean z = true;
        if (isManagedProfile() || isGuest() || isRestricted() || isHwTrustSpace()) {
            return false;
        }
        if (UserManager.isSplitSystemUser()) {
            if (this.id == 0) {
                z = false;
            }
            return z;
        }
        if (this.id != 0) {
            z = false;
        }
        return z;
    }

    public UserInfo(UserInfo orig) {
        this.name = orig.name;
        this.iconPath = orig.iconPath;
        this.id = orig.id;
        this.flags = orig.flags;
        this.serialNumber = orig.serialNumber;
        this.creationTime = orig.creationTime;
        this.lastLoggedInTime = orig.lastLoggedInTime;
        this.lastLoggedInFingerprint = orig.lastLoggedInFingerprint;
        this.partial = orig.partial;
        this.profileGroupId = orig.profileGroupId;
        this.restrictedProfileParentId = orig.restrictedProfileParentId;
        this.guestToRemove = orig.guestToRemove;
    }

    public UserHandle getUserHandle() {
        return new UserHandle(this.id);
    }

    public String toString() {
        return "UserInfo{" + this.id + ":" + this.name + ":" + Integer.toHexString(this.flags) + "}";
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int parcelableFlags) {
        int i;
        int i2 = FLAG_PRIMARY;
        dest.writeInt(this.id);
        dest.writeString(this.name);
        dest.writeString(this.iconPath);
        dest.writeInt(this.flags);
        dest.writeInt(this.serialNumber);
        dest.writeLong(this.creationTime);
        dest.writeLong(this.lastLoggedInTime);
        dest.writeString(this.lastLoggedInFingerprint);
        if (this.partial) {
            i = FLAG_PRIMARY;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        dest.writeInt(this.profileGroupId);
        if (!this.guestToRemove) {
            i2 = 0;
        }
        dest.writeInt(i2);
        dest.writeInt(this.restrictedProfileParentId);
    }

    private UserInfo(Parcel source) {
        boolean z;
        boolean z2 = true;
        this.id = source.readInt();
        this.name = source.readString();
        this.iconPath = source.readString();
        this.flags = source.readInt();
        this.serialNumber = source.readInt();
        this.creationTime = source.readLong();
        this.lastLoggedInTime = source.readLong();
        this.lastLoggedInFingerprint = source.readString();
        if (source.readInt() != 0) {
            z = true;
        } else {
            z = false;
        }
        this.partial = z;
        this.profileGroupId = source.readInt();
        if (source.readInt() == 0) {
            z2 = false;
        }
        this.guestToRemove = z2;
        this.restrictedProfileParentId = source.readInt();
    }
}

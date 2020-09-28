package android.content.pm;

import android.annotation.UnsupportedAppUsage;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.SettingsStringUtil;

public class UserInfo implements Parcelable {
    @UnsupportedAppUsage
    public static final Parcelable.Creator<UserInfo> CREATOR = new Parcelable.Creator<UserInfo>() {
        /* class android.content.pm.UserInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public UserInfo createFromParcel(Parcel source) {
            return new UserInfo(source);
        }

        @Override // android.os.Parcelable.Creator
        public UserInfo[] newArray(int size) {
            return new UserInfo[size];
        }
    };
    public static final int FLAG_ADMIN = 2;
    public static final int FLAG_CLONED_PROFILE = 67108864;
    public static final int FLAG_DEMO = 512;
    public static final int FLAG_DISABLED = 64;
    public static final int FLAG_EPHEMERAL = 256;
    public static final int FLAG_GUEST = 4;
    public static final int FLAG_HW_HIDDENSPACE = 33554432;
    public static final int FLAG_HW_KIDSUSER = 2048;
    public static final int FLAG_HW_REPAIR_MODE = 134217728;
    public static final int FLAG_HW_TRUSTSPACE = 16777216;
    public static final int FLAG_INITIALIZED = 16;
    public static final int FLAG_MANAGED_PROFILE = 32;
    public static final int FLAG_MASK_USER_TYPE = 65535;
    @UnsupportedAppUsage
    public static final int FLAG_PRIMARY = 1;
    public static final int FLAG_QUIET_MODE = 128;
    public static final int FLAG_RESTRICTED = 8;
    public static final int NO_PROFILE_GROUP_ID = -10000;
    @UnsupportedAppUsage
    public long creationTime;
    @UnsupportedAppUsage
    public int flags;
    @UnsupportedAppUsage
    public boolean guestToRemove;
    @UnsupportedAppUsage
    public String iconPath;
    @UnsupportedAppUsage
    public int id;
    public String lastLoggedInFingerprint;
    public String lastLoggedInFingerprintEx;
    @UnsupportedAppUsage
    public long lastLoggedInTime;
    @UnsupportedAppUsage
    public String name;
    @UnsupportedAppUsage
    public boolean partial;
    public int profileBadge;
    @UnsupportedAppUsage
    public int profileGroupId;
    public int restrictedProfileParentId;
    @UnsupportedAppUsage
    public int serialNumber;

    @UnsupportedAppUsage
    public UserInfo(int id2, String name2, int flags2) {
        this(id2, name2, null, flags2);
    }

    @UnsupportedAppUsage
    public UserInfo(int id2, String name2, String iconPath2, int flags2) {
        this.id = id2;
        this.name = name2;
        this.flags = flags2;
        this.iconPath = iconPath2;
        this.profileGroupId = -10000;
        this.restrictedProfileParentId = -10000;
    }

    @UnsupportedAppUsage
    public boolean isPrimary() {
        return (this.flags & 1) == 1;
    }

    @UnsupportedAppUsage
    public boolean isAdmin() {
        return (this.flags & 2) == 2;
    }

    @UnsupportedAppUsage
    public boolean isGuest() {
        return (this.flags & 4) == 4;
    }

    @UnsupportedAppUsage
    public boolean isRestricted() {
        return (this.flags & 8) == 8;
    }

    @UnsupportedAppUsage
    public boolean isManagedProfile() {
        return (this.flags & 32) == 32;
    }

    public boolean isClonedProfile() {
        return (this.flags & 67108864) == 67108864;
    }

    public boolean isHwTrustSpace() {
        return (this.flags & 16777216) == 16777216;
    }

    @UnsupportedAppUsage
    public boolean isEnabled() {
        return (this.flags & 64) != 64;
    }

    public boolean isQuietModeEnabled() {
        return (this.flags & 128) == 128;
    }

    public boolean isEphemeral() {
        return (this.flags & 256) == 256;
    }

    public boolean isInitialized() {
        return (this.flags & 16) == 16;
    }

    public boolean isDemo() {
        return (this.flags & 512) == 512;
    }

    public boolean isHwHiddenSpace() {
        return (this.flags & 33554432) == 33554432;
    }

    public boolean isRepairMode() {
        return (this.flags & 134217728) == 134217728;
    }

    public boolean isHwKidsUser() {
        return (this.flags & 2048) == 2048;
    }

    public boolean isSystemOnly() {
        return isSystemOnly(this.id);
    }

    public static boolean isSystemOnly(int userId) {
        return userId == 0 && UserManager.isSplitSystemUser();
    }

    public boolean supportsSwitchTo() {
        if (!isEphemeral() || isEnabled()) {
            return !isManagedProfile();
        }
        return false;
    }

    public boolean supportsSwitchToByUser() {
        return (!UserManager.isSplitSystemUser() || this.id != 0) && supportsSwitchTo();
    }

    public boolean canHaveProfile() {
        if (isManagedProfile() || isGuest() || isRestricted()) {
            return false;
        }
        if (UserManager.isSplitSystemUser()) {
            if (this.id != 0) {
                return true;
            }
            return false;
        } else if (this.id == 0) {
            return true;
        } else {
            return false;
        }
    }

    public UserInfo() {
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
        this.lastLoggedInFingerprintEx = orig.lastLoggedInFingerprintEx;
        this.partial = orig.partial;
        this.profileGroupId = orig.profileGroupId;
        this.restrictedProfileParentId = orig.restrictedProfileParentId;
        this.guestToRemove = orig.guestToRemove;
        this.profileBadge = orig.profileBadge;
    }

    @UnsupportedAppUsage
    public UserHandle getUserHandle() {
        return new UserHandle(this.id);
    }

    public String toString() {
        return "UserInfo{" + this.id + SettingsStringUtil.DELIMITER + this.name + SettingsStringUtil.DELIMITER + Integer.toHexString(this.flags) + "}";
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int parcelableFlags) {
        dest.writeInt(this.id);
        dest.writeString(this.name);
        dest.writeString(this.iconPath);
        dest.writeInt(this.flags);
        dest.writeInt(this.serialNumber);
        dest.writeLong(this.creationTime);
        dest.writeLong(this.lastLoggedInTime);
        dest.writeString(this.lastLoggedInFingerprint);
        dest.writeString(this.lastLoggedInFingerprintEx);
        dest.writeInt(this.partial ? 1 : 0);
        dest.writeInt(this.profileGroupId);
        dest.writeInt(this.guestToRemove ? 1 : 0);
        dest.writeInt(this.restrictedProfileParentId);
        dest.writeInt(this.profileBadge);
    }

    private UserInfo(Parcel source) {
        this.id = source.readInt();
        this.name = source.readString();
        this.iconPath = source.readString();
        this.flags = source.readInt();
        this.serialNumber = source.readInt();
        this.creationTime = source.readLong();
        this.lastLoggedInTime = source.readLong();
        this.lastLoggedInFingerprint = source.readString();
        this.lastLoggedInFingerprintEx = source.readString();
        boolean z = true;
        this.partial = source.readInt() != 0;
        this.profileGroupId = source.readInt();
        this.guestToRemove = source.readInt() == 0 ? false : z;
        this.restrictedProfileParentId = source.readInt();
        this.profileBadge = source.readInt();
    }
}

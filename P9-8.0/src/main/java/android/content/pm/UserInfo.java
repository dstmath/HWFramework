package android.content.pm;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.UserHandle;
import android.os.UserManager;

public class UserInfo implements Parcelable {
    public static final Creator<UserInfo> CREATOR = new Creator<UserInfo>() {
        public UserInfo createFromParcel(Parcel source) {
            return new UserInfo(source, null);
        }

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
    public static final int FLAG_HW_REPAIR_MODE = 134217728;
    public static final int FLAG_HW_TRUSTSPACE = 16777216;
    public static final int FLAG_INITIALIZED = 16;
    public static final int FLAG_MANAGED_PROFILE = 32;
    public static final int FLAG_MASK_USER_TYPE = 65535;
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
    public int profileBadge;
    public int profileGroupId;
    public int restrictedProfileParentId;
    public int serialNumber;

    /* synthetic */ UserInfo(Parcel source, UserInfo -this1) {
        this(source);
    }

    public UserInfo(int id, String name, int flags) {
        this(id, name, null, flags);
    }

    public UserInfo(int id, String name, String iconPath, int flags) {
        this.id = id;
        this.name = name;
        this.flags = flags;
        this.iconPath = iconPath;
        this.profileGroupId = -10000;
        this.restrictedProfileParentId = -10000;
    }

    public boolean isPrimary() {
        return (this.flags & 1) == 1;
    }

    public boolean isAdmin() {
        return (this.flags & 2) == 2;
    }

    public boolean isGuest() {
        return (this.flags & 4) == 4;
    }

    public boolean isRestricted() {
        return (this.flags & 8) == 8;
    }

    public boolean isManagedProfile() {
        return (this.flags & 32) == 32;
    }

    public boolean isClonedProfile() {
        return (this.flags & 67108864) == 67108864;
    }

    public boolean isHwTrustSpace() {
        return (this.flags & 16777216) == 16777216;
    }

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

    public boolean isSystemOnly() {
        return isSystemOnly(this.id);
    }

    public static boolean isSystemOnly(int userId) {
        return userId == 0 ? UserManager.isSplitSystemUser() : false;
    }

    public boolean supportsSwitchTo() {
        if ((!isEphemeral() || (isEnabled() ^ 1) == 0) && !isHwTrustSpace()) {
            return isManagedProfile() ^ 1;
        }
        return false;
    }

    public boolean supportsSwitchToByUser() {
        if (UserManager.isSplitSystemUser() && this.id == 0) {
            return false;
        }
        return supportsSwitchTo();
    }

    /* JADX WARNING: Missing block: B:6:0x0014, code:
            return false;
     */
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
        this.profileBadge = orig.profileBadge;
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
        int i = 1;
        dest.writeInt(this.id);
        dest.writeString(this.name);
        dest.writeString(this.iconPath);
        dest.writeInt(this.flags);
        dest.writeInt(this.serialNumber);
        dest.writeLong(this.creationTime);
        dest.writeLong(this.lastLoggedInTime);
        dest.writeString(this.lastLoggedInFingerprint);
        dest.writeInt(this.partial ? 1 : 0);
        dest.writeInt(this.profileGroupId);
        if (!this.guestToRemove) {
            i = 0;
        }
        dest.writeInt(i);
        dest.writeInt(this.restrictedProfileParentId);
        dest.writeInt(this.profileBadge);
    }

    private UserInfo(Parcel source) {
        boolean z = true;
        this.id = source.readInt();
        this.name = source.readString();
        this.iconPath = source.readString();
        this.flags = source.readInt();
        this.serialNumber = source.readInt();
        this.creationTime = source.readLong();
        this.lastLoggedInTime = source.readLong();
        this.lastLoggedInFingerprint = source.readString();
        this.partial = source.readInt() != 0;
        this.profileGroupId = source.readInt();
        if (source.readInt() == 0) {
            z = false;
        }
        this.guestToRemove = z;
        this.restrictedProfileParentId = source.readInt();
        this.profileBadge = source.readInt();
    }
}

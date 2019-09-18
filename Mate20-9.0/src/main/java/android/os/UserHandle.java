package android.os;

import android.annotation.SystemApi;
import android.os.Parcelable;
import android.provider.Telephony;
import android.text.format.DateFormat;
import java.io.PrintWriter;

public final class UserHandle implements Parcelable {
    public static final int AID_APP_END = 19999;
    public static final int AID_APP_START = 10000;
    public static final int AID_CACHE_GID_START = 20000;
    public static final int AID_ROOT = 0;
    public static final int AID_SHARED_GID_START = 50000;
    public static final UserHandle ALL = new UserHandle(-1);
    public static final int CLONEDPROFILE_USER_ID_MAX = 148;
    public static final int CLONEDPROFILE_USER_ID_MIN = 128;
    public static final Parcelable.Creator<UserHandle> CREATOR = new Parcelable.Creator<UserHandle>() {
        public UserHandle createFromParcel(Parcel in) {
            return new UserHandle(in);
        }

        public UserHandle[] newArray(int size) {
            return new UserHandle[size];
        }
    };
    public static final UserHandle CURRENT = new UserHandle(-2);
    public static final UserHandle CURRENT_OR_SELF = new UserHandle(-3);
    @Deprecated
    public static final int EMUI_UID = 2147383647;
    public static final int ERR_GID = -1;
    public static final boolean MU_ENABLED = true;
    @Deprecated
    public static final UserHandle OWNER = new UserHandle(0);
    public static final int PER_USER_RANGE = 100000;
    public static final UserHandle SYSTEM = new UserHandle(0);
    public static final int USER_ALL = -1;
    public static final int USER_CURRENT = -2;
    public static final int USER_CURRENT_OR_SELF = -3;
    public static final int USER_NULL = -10000;
    @Deprecated
    public static final int USER_OWNER = 0;
    public static final int USER_SERIAL_SYSTEM = 0;
    public static final int USER_SYSTEM = 0;
    public static final int VIRTUAL_USER_ID = 2147483646;
    final int mHandle;

    public static boolean isSameUser(int uid1, int uid2) {
        return getUserId(uid1) == getUserId(uid2);
    }

    public static boolean isSameApp(int uid1, int uid2) {
        return getAppId(uid1) == getAppId(uid2);
    }

    public static boolean isIsolated(int uid) {
        boolean z = false;
        if (uid <= 0) {
            return false;
        }
        int appId = getAppId(uid);
        if (appId >= 99000 && appId <= 99999) {
            z = true;
        }
        return z;
    }

    public static boolean isApp(int uid) {
        boolean z = false;
        if (uid <= 0) {
            return false;
        }
        int appId = getAppId(uid);
        if (appId >= 10000 && appId <= 19999) {
            z = true;
        }
        return z;
    }

    public static boolean isCore(int uid) {
        boolean z = false;
        if (uid < 0) {
            return false;
        }
        if (getAppId(uid) < 10000) {
            z = true;
        }
        return z;
    }

    public static UserHandle getUserHandleForUid(int uid) {
        return of(getUserId(uid));
    }

    public static int getUserId(int uid) {
        return uid / PER_USER_RANGE;
    }

    public static int getCallingUserId() {
        return getUserId(Binder.getCallingUid());
    }

    public static int getCallingAppId() {
        return getAppId(Binder.getCallingUid());
    }

    @SystemApi
    public static UserHandle of(int userId) {
        return userId == 0 ? SYSTEM : new UserHandle(userId);
    }

    public static int getUid(int userId, int appId) {
        return (userId * PER_USER_RANGE) + (appId % PER_USER_RANGE);
    }

    public static int getAppId(int uid) {
        return uid % PER_USER_RANGE;
    }

    public static int getUserGid(int userId) {
        return getUid(userId, Process.SHARED_USER_GID);
    }

    public static int getSharedAppGid(int uid) {
        return getSharedAppGid(getUserId(uid), getAppId(uid));
    }

    public static int getSharedAppGid(int userId, int appId) {
        if (appId >= 10000 && appId <= 19999) {
            return (appId - 10000) + 50000;
        }
        if (appId < 0 || appId > 10000) {
            return -1;
        }
        return appId;
    }

    public static int getAppIdFromSharedAppGid(int gid) {
        int appId = (getAppId(gid) + 10000) - 50000;
        if (appId < 0 || appId >= 50000) {
            return -1;
        }
        return appId;
    }

    public static int getCacheAppGid(int uid) {
        return getCacheAppGid(getUserId(uid), getAppId(uid));
    }

    public static int getCacheAppGid(int userId, int appId) {
        if (appId < 10000 || appId > 19999) {
            return -1;
        }
        return getUid(userId, (appId - 10000) + 20000);
    }

    public static void formatUid(StringBuilder sb, int uid) {
        if (uid < 10000) {
            sb.append(uid);
            return;
        }
        sb.append('u');
        sb.append(getUserId(uid));
        int appId = getAppId(uid);
        if (appId >= 99000 && appId <= 99999) {
            sb.append('i');
            sb.append(appId - Process.FIRST_ISOLATED_UID);
        } else if (appId >= 10000) {
            sb.append(DateFormat.AM_PM);
            sb.append(appId - 10000);
        } else {
            sb.append(DateFormat.SECONDS);
            sb.append(appId);
        }
    }

    public static String formatUid(int uid) {
        StringBuilder sb = new StringBuilder();
        formatUid(sb, uid);
        return sb.toString();
    }

    public static void formatUid(PrintWriter pw, int uid) {
        if (uid < 10000) {
            pw.print(uid);
            return;
        }
        pw.print('u');
        pw.print(getUserId(uid));
        int appId = getAppId(uid);
        if (appId >= 99000 && appId <= 99999) {
            pw.print('i');
            pw.print(appId - Process.FIRST_ISOLATED_UID);
        } else if (appId >= 10000) {
            pw.print(DateFormat.AM_PM);
            pw.print(appId - 10000);
        } else {
            pw.print(DateFormat.SECONDS);
            pw.print(appId);
        }
    }

    public static int parseUserArg(String arg) {
        if ("all".equals(arg)) {
            return -1;
        }
        if (Telephony.Carriers.CURRENT.equals(arg) || "cur".equals(arg)) {
            return -2;
        }
        try {
            return Integer.parseInt(arg);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Bad user number: " + arg);
        }
    }

    @SystemApi
    public static int myUserId() {
        return getUserId(Process.myUid());
    }

    @SystemApi
    @Deprecated
    public boolean isOwner() {
        return equals(OWNER);
    }

    @SystemApi
    public boolean isSystem() {
        return equals(SYSTEM);
    }

    public UserHandle(int h) {
        this.mHandle = h;
    }

    @SystemApi
    public int getIdentifier() {
        return this.mHandle;
    }

    public String toString() {
        return "UserHandle{" + this.mHandle + "}";
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj != null) {
            try {
                if (this.mHandle == ((UserHandle) obj).mHandle) {
                    z = true;
                }
                return z;
            } catch (ClassCastException e) {
            }
        }
        return false;
    }

    public int hashCode() {
        return this.mHandle;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.mHandle);
    }

    public static void writeToParcel(UserHandle h, Parcel out) {
        if (h != null) {
            h.writeToParcel(out, 0);
        } else {
            out.writeInt(-10000);
        }
    }

    public static UserHandle readFromParcel(Parcel in) {
        int h = in.readInt();
        if (h != -10000) {
            return new UserHandle(h);
        }
        return null;
    }

    public UserHandle(Parcel in) {
        this.mHandle = in.readInt();
    }

    public static boolean isClonedProfile(int userId) {
        return 128 <= userId && userId < 148;
    }
}

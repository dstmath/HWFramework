package android.os;

import android.app.SearchManager;
import android.os.Parcelable.Creator;
import android.os.health.HealthKeys;
import android.rms.iaware.Events;
import java.io.PrintWriter;

public final class UserHandle implements Parcelable {
    public static final UserHandle ALL = null;
    public static final Creator<UserHandle> CREATOR = null;
    public static final UserHandle CURRENT = null;
    public static final UserHandle CURRENT_OR_SELF = null;
    public static final int EMUI_UID = 2147383647;
    public static final boolean MU_ENABLED = true;
    public static final UserHandle OWNER = null;
    public static final int PER_USER_RANGE = 100000;
    public static final UserHandle SYSTEM = null;
    public static final int USER_ALL = -1;
    public static final int USER_CURRENT = -2;
    public static final int USER_CURRENT_OR_SELF = -3;
    public static final int USER_NULL = -10000;
    public static final int USER_OWNER = 0;
    public static final int USER_SERIAL_SYSTEM = 0;
    public static final int USER_SYSTEM = 0;
    public static final int VIRTUAL_USER_ID = 2147483646;
    final int mHandle;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.os.UserHandle.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.os.UserHandle.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.os.UserHandle.<clinit>():void");
    }

    public static boolean isSameUser(int uid1, int uid2) {
        return getUserId(uid1) == getUserId(uid2) ? MU_ENABLED : false;
    }

    public static boolean isSameApp(int uid1, int uid2) {
        return getAppId(uid1) == getAppId(uid2) ? MU_ENABLED : false;
    }

    public static boolean isIsolated(int uid) {
        boolean z = false;
        if (uid <= 0) {
            return false;
        }
        int appId = getAppId(uid);
        if (appId >= Process.FIRST_ISOLATED_UID && appId <= Process.LAST_ISOLATED_UID) {
            z = MU_ENABLED;
        }
        return z;
    }

    public static boolean isApp(int uid) {
        boolean z = false;
        if (uid <= 0) {
            return false;
        }
        int appId = getAppId(uid);
        if (appId >= Events.EVENT_FLAG_START && appId <= Process.LAST_APPLICATION_UID) {
            z = MU_ENABLED;
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

    public static int getSharedAppGid(int id) {
        return ((id % PER_USER_RANGE) + HealthKeys.BASE_SERVICE) + USER_NULL;
    }

    public static int getAppIdFromSharedAppGid(int gid) {
        int appId = (getAppId(gid) + Events.EVENT_FLAG_START) - HealthKeys.BASE_SERVICE;
        if (appId < 0 || appId >= HealthKeys.BASE_SERVICE) {
            return USER_ALL;
        }
        return appId;
    }

    public static void formatUid(StringBuilder sb, int uid) {
        if (uid < Events.EVENT_FLAG_START) {
            sb.append(uid);
            return;
        }
        sb.append('u');
        sb.append(getUserId(uid));
        int appId = getAppId(uid);
        if (appId >= Process.FIRST_ISOLATED_UID && appId <= Process.LAST_ISOLATED_UID) {
            sb.append('i');
            sb.append(appId - Process.FIRST_ISOLATED_UID);
        } else if (appId >= Events.EVENT_FLAG_START) {
            sb.append('a');
            sb.append(appId + USER_NULL);
        } else {
            sb.append(SearchManager.MENU_KEY);
            sb.append(appId);
        }
    }

    public static String formatUid(int uid) {
        StringBuilder sb = new StringBuilder();
        formatUid(sb, uid);
        return sb.toString();
    }

    public static void formatUid(PrintWriter pw, int uid) {
        if (uid < Events.EVENT_FLAG_START) {
            pw.print(uid);
            return;
        }
        pw.print('u');
        pw.print(getUserId(uid));
        int appId = getAppId(uid);
        if (appId >= Process.FIRST_ISOLATED_UID && appId <= Process.LAST_ISOLATED_UID) {
            pw.print('i');
            pw.print(appId - Process.FIRST_ISOLATED_UID);
        } else if (appId >= Events.EVENT_FLAG_START) {
            pw.print('a');
            pw.print(appId + USER_NULL);
        } else {
            pw.print(SearchManager.MENU_KEY);
            pw.print(appId);
        }
    }

    public static int parseUserArg(String arg) {
        if ("all".equals(arg)) {
            return USER_ALL;
        }
        if ("current".equals(arg) || "cur".equals(arg)) {
            return USER_CURRENT;
        }
        try {
            return Integer.parseInt(arg);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Bad user number: " + arg);
        }
    }

    public static int myUserId() {
        return getUserId(Process.myUid());
    }

    public boolean isOwner() {
        return equals(OWNER);
    }

    public boolean isSystem() {
        return equals(SYSTEM);
    }

    public UserHandle(int h) {
        this.mHandle = h;
    }

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
                    z = MU_ENABLED;
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
        return USER_SYSTEM;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.mHandle);
    }

    public static void writeToParcel(UserHandle h, Parcel out) {
        if (h != null) {
            h.writeToParcel(out, (int) USER_SYSTEM);
        } else {
            out.writeInt(USER_NULL);
        }
    }

    public static UserHandle readFromParcel(Parcel in) {
        int h = in.readInt();
        return h != USER_NULL ? new UserHandle(h) : null;
    }

    public UserHandle(Parcel in) {
        this.mHandle = in.readInt();
    }
}

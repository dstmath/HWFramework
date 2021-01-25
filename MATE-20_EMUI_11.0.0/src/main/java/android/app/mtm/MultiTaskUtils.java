package android.app.mtm;

import android.content.pm.ApplicationInfo;
import android.os.Process;
import android.os.RemoteException;
import com.huawei.android.content.pm.ApplicationInfoEx;
import com.huawei.android.content.pm.IPackageManagerExt;
import com.huawei.android.os.UserHandleEx;
import com.huawei.android.util.SlogEx;
import com.huawei.util.LogEx;

public final class MultiTaskUtils {
    private static final boolean DEBUG = false;
    public static final int HW_INSTALL = 3;
    public static final String STRING_ALL = "ALL";
    public static final int SYSTEM_APP = 2;
    public static final int SYSTEM_SERVER = 1;
    private static final String TAG = "MultiTaskUtils";
    public static final int THIRD_PARTY = 4;

    public static int getAppType(int pid, int uid, String pkgName) {
        ApplicationInfo info = null;
        try {
            info = IPackageManagerExt.getApplicationInfo(pkgName, 0, UserHandleEx.getCallingUserId());
        } catch (RemoteException e) {
            SlogEx.e(TAG, "can not get packagemanagerservice!");
        }
        if (info == null) {
            if (!LogEx.getLogHWInfo()) {
                return -1;
            }
            SlogEx.e(TAG, "can not get applicationinfo for pid:" + pid + " uid:" + uid);
            return -1;
        } else if (pid == Process.myPid()) {
            return 1;
        } else {
            if ((info.flags & 1) != 0 && ((new ApplicationInfoEx(info).getHwFlags() & 33554432) != 0 || (info.flags & 33554432) != 0)) {
                return 3;
            }
            if ((info.flags & 1) != 0) {
                return 2;
            }
            return 4;
        }
    }

    public static int getAppType(int pid, ApplicationInfo info) {
        if (info == null) {
            SlogEx.e(TAG, "getAppType app info is null");
            return 0;
        }
        int flags = info.flags;
        try {
            int hwFlags = ((Integer) Class.forName("android.content.pm.ApplicationInfo").getField("hwFlags").get(info)).intValue();
            if (!((flags & 1) == 0 || (hwFlags & 100663296) == 0)) {
                return 3;
            }
        } catch (ClassNotFoundException e) {
            SlogEx.e(TAG, "getAppType exception: ClassNotFoundException");
        } catch (NoSuchFieldException e2) {
            SlogEx.e(TAG, "getAppType exception: NoSuchFieldException");
        } catch (IllegalArgumentException e3) {
            SlogEx.e(TAG, "getAppType exception: IllegalArgumentException");
        } catch (IllegalAccessException e4) {
            SlogEx.e(TAG, "getAppType exception: IllegalAccessException");
        }
        if (pid == Process.myPid()) {
            return 1;
        }
        if ((flags & 1) != 0) {
            return 2;
        }
        return 4;
    }
}

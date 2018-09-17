package android.app.mtm;

import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.IPackageManager.Stub;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.util.Log;
import android.util.Slog;
import com.huawei.hsm.permission.StubController;

public final class MultiTaskUtils {
    static final boolean DEBUG = false;
    public static final boolean HWFLOW;
    public static final boolean HWLOGW_E = true;
    public static final int HW_INSTALL = 3;
    public static final int MultiTask_POLICY_ChangeStatus = 512;
    public static final int MultiTask_POLICY_Delay = 4;
    public static final int MultiTask_POLICY_Forbid = 2;
    public static final int MultiTask_POLICY_MemoryDropCache = 256;
    public static final int MultiTask_POLICY_MemoryShrink = 128;
    public static final int MultiTask_POLICY_ProcessCpuset = 16;
    public static final int MultiTask_POLICY_ProcessFreeze = 64;
    public static final int MultiTask_POLICY_ProcessKill = 32;
    public static final int MultiTask_POLICY_ProcessShrink = 1024;
    public static final int MultiTask_POLICY_Proxy = 8;
    public static final int MultiTask_POLICY_UNDO = 1;
    public static final int NORMAL = 1;
    public static final int SYSTEM_APP = 2;
    public static final int SYSTEM_SERVER = 1;
    public static final String StringALL = "ALL";
    static final String TAG = "MultiTaskUtils";
    public static final int THIRDPARTY = 4;
    public static final int URGENT = 4;
    public static final int WARNING = 2;

    static {
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(TAG, 4) : false : true;
        HWFLOW = isLoggable;
    }

    public static int getAppType(int pid, int uid, String pkgName) {
        ApplicationInfo info = null;
        try {
            IPackageManager pm = Stub.asInterface(ServiceManager.getService("package"));
            if (pm != null) {
                info = pm.getApplicationInfo(pkgName, 0, UserHandle.getCallingUserId());
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "can not get packagemanagerservice!");
        }
        if (info == null) {
            if (Log.HWINFO) {
                Slog.e(TAG, "can not get applicationinfo for pid:" + pid + " uid:" + uid);
            }
            return -1;
        } else if (pid == Process.myPid()) {
            return 1;
        } else {
            try {
                int hwFlags = ((Integer) Class.forName("android.content.pm.ApplicationInfo").getField("hwFlags").get(info)).intValue();
                if (!((info.flags & 1) == 0 || (StubController.PERMISSION_GET_PACKAGE_LIST & hwFlags) == 0)) {
                    return 3;
                }
            } catch (ClassNotFoundException e2) {
                Slog.e(TAG, "getAppType exception: ClassNotFoundException");
            } catch (NoSuchFieldException e3) {
                Slog.e(TAG, "getAppType exception: NoSuchFieldException");
            } catch (IllegalArgumentException e4) {
                Slog.e(TAG, "getAppType exception: IllegalArgumentException");
            } catch (IllegalAccessException e5) {
                Slog.e(TAG, "getAppType exception: IllegalAccessException");
            }
            if ((info.flags & 1) != 0 && (info.hwFlags & StubController.PERMISSION_GET_PACKAGE_LIST) != 0) {
                return 3;
            }
            if ((info.flags & 1) != 0 && (info.flags & StubController.PERMISSION_GET_PACKAGE_LIST) != 0) {
                return 3;
            }
            if ((info.flags & 1) != 0) {
                return 2;
            }
            return 4;
        }
    }
}

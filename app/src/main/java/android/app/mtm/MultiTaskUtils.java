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
    public static final boolean HWFLOW = false;
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
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.app.mtm.MultiTaskUtils.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.app.mtm.MultiTaskUtils.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.app.mtm.MultiTaskUtils.<clinit>():void");
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
            return SYSTEM_SERVER;
        } else {
            try {
                int hwFlags = ((Integer) Class.forName("android.content.pm.ApplicationInfo").getField("hwFlags").get(info)).intValue();
                if (!((info.flags & SYSTEM_SERVER) == 0 || (StubController.PERMISSION_GET_PACKAGE_LIST & hwFlags) == 0)) {
                    return HW_INSTALL;
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
            if ((info.flags & SYSTEM_SERVER) != 0 && (info.hwFlags & StubController.PERMISSION_GET_PACKAGE_LIST) != 0) {
                return HW_INSTALL;
            }
            if ((info.flags & SYSTEM_SERVER) != 0 && (info.flags & StubController.PERMISSION_GET_PACKAGE_LIST) != 0) {
                return HW_INSTALL;
            }
            if ((info.flags & SYSTEM_SERVER) != 0) {
                return WARNING;
            }
            return URGENT;
        }
    }
}

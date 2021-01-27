package ohos.security.permission;

import android.os.Process;
import java.util.HashMap;
import java.util.Map;

public class PermissionConversion {
    private static int pid = Process.myPid();
    private static Map<Integer, String[]> registeredPermissions = new HashMap();

    private static native String nativeGetAosPermissionNameIfPossible(String str);

    private static native String nativeGetZosPermissionNameIfPossible(String str);

    static {
        System.loadLibrary("permission_conversion_jni.z");
    }

    public static String getAosPermissionNameIfPossible(String str) {
        String nativeGetAosPermissionNameIfPossible;
        return (str == null || (nativeGetAosPermissionNameIfPossible = nativeGetAosPermissionNameIfPossible(str)) == null) ? "" : nativeGetAosPermissionNameIfPossible;
    }

    public static String getZosPermissionNameIfPossible(String str) {
        String nativeGetZosPermissionNameIfPossible;
        return (str == null || (nativeGetZosPermissionNameIfPossible = nativeGetZosPermissionNameIfPossible(str)) == null) ? "" : nativeGetZosPermissionNameIfPossible;
    }

    public static void registerRequestPermssions(int i, String[] strArr) {
        int myPid = Process.myPid();
        if (myPid != pid) {
            pid = myPid;
            registeredPermissions.clear();
        }
        registeredPermissions.put(Integer.valueOf(i), strArr);
    }

    public static String[] fetchRequestPermissions(int i) {
        String[] orDefault = registeredPermissions.getOrDefault(Integer.valueOf(i), new String[0]);
        registeredPermissions.remove(Integer.valueOf(i));
        return orDefault;
    }
}

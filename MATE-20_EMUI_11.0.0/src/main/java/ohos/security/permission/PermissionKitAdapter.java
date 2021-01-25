package ohos.security.permission;

public class PermissionKitAdapter {
    public static native int checkCallerPermission(String str);

    public static native int checkCallerPermissionAndStartUsing(String str);

    public static native int checkCallerPermissionAndUse(String str);

    public static native int checkPermission(String str, String str2);

    public static native int checkPermissionAndStartUsing(String str, String str2);

    public static native int checkPermissionAndUse(String str, String str2);

    public static native int checkSelfPermission(String str);

    public static native int nativePostPermissionEvent(String str);

    public static native void startUsingPermission(String str, String str2);

    public static native void stopUsingPermission(String str, String str2);

    static {
        System.loadLibrary("permission_kit_jni.z");
    }

    private PermissionKitAdapter() {
    }
}

package ohos.rpc;

import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class IPCSkeleton {
    private static final HiLogLabel TAG = new HiLogLabel(3, 0, "IPCSkeleton");

    private static native int nativeFlushCommands(IRemoteObject iRemoteObject);

    private static native String nativeGetCallingDeviceID();

    private static native int nativeGetCallingPid();

    private static native int nativeGetCallingUid();

    private static native IRemoteObject nativeGetContextObject();

    private static native String nativeGetLocalDeviceID();

    private static native boolean nativeIsLocalCalling();

    private static native String nativeResetCallingIdentity();

    private static native boolean nativeSetCallingIdentity(String str, int i);

    static {
        try {
            System.loadLibrary("ipc_core.z");
        } catch (NullPointerException | UnsatisfiedLinkError unused) {
            HiLog.error(TAG, "fail to load libipc_core.z.so", new Object[0]);
        }
    }

    public static IRemoteObject getContextObject() {
        return nativeGetContextObject();
    }

    public static int getCallingPid() {
        return nativeGetCallingPid();
    }

    public static int getCallingUid() {
        return nativeGetCallingUid();
    }

    public static String getCallingDeviceID() {
        return nativeGetCallingDeviceID();
    }

    public static String getLocalDeviceID() {
        return nativeGetLocalDeviceID();
    }

    public static boolean isLocalCalling() {
        return nativeIsLocalCalling();
    }

    public static int flushCommands(IRemoteObject iRemoteObject) {
        return nativeFlushCommands(iRemoteObject);
    }

    public static String resetCallingIdentity() {
        return nativeResetCallingIdentity();
    }

    public static boolean setCallingIdentity(String str) {
        if (str == null) {
            return false;
        }
        return nativeSetCallingIdentity(str, str.length());
    }
}

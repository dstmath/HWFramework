package ohos.sysability.samgr;

import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteObject;

public class SystemAbilityManagerClient {
    private static final HiLogLabel TAG = new HiLogLabel(3, 218109952, "SystemAbilityManagerClient");

    private static native void nativeDestroySystemAbilityManagerObject();

    private static native IRemoteObject nativeGetSystemAbilityManagerObject();

    static {
        try {
            System.loadLibrary("samgr_proxy.z");
            System.loadLibrary("ipc_core.z");
        } catch (UnsatisfiedLinkError e) {
            HiLog.warn(TAG, "Could not load so, %{public}s", e.getMessage());
        }
    }

    private SystemAbilityManagerClient() {
    }

    public static IRemoteObject getSystemAbilityManagerObject() {
        return nativeGetSystemAbilityManagerObject();
    }

    public static void destroySystemAbilityManagerObject() {
        nativeDestroySystemAbilityManagerObject();
    }
}

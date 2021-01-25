package ohos.smartcomm;

import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteObject;

public class SmartComm {
    private static final int MP_SERVICE_TYPE_ANDROID = 1;
    private static final HiLogLabel TAG = new HiLogLabel(3, SmartCommConstant.SMART_COMM_DOMAIN, "SmartComm");
    private final Object mLock = new Object();

    private static native IRemoteObject nativeGetMPService();

    static {
        System.loadLibrary("smartcomm_jni.z");
    }

    public int getMPServiceType() {
        HiLog.info(TAG, "call the remote GetMPServiceType method of SmartCommService", new Object[0]);
        return 1;
    }

    public IRemoteObject getMPService() {
        HiLog.info(TAG, "call the remote GetMPService method of SmartCommService", new Object[0]);
        return nativeGetMPService();
    }
}

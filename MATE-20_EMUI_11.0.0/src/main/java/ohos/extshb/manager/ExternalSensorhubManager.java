package ohos.extshb.manager;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import ohos.extshb.agent.CommandResult;
import ohos.extshb.agent.IExternalSensorhubDataListener;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class ExternalSensorhubManager {
    private static final byte ALL_COMMAND_ID = -1;
    private static final int CHANNEL_RES = 0;
    private static final int CLASS_INIT_RES = 0;
    private static final int DATA_MOVE_FAC = 256;
    private static final Object FIRST_INIT_LOCK = new Object();
    private static final int INVALID_RESULT = -22;
    private static final int RESULT_OK = 0;
    private static final HiLogLabel TAG = new HiLogLabel(3, 218113808, "ExternalSensorhubManager");
    private static volatile ExternalSensorhubManager instance;
    private static boolean isInitNative = false;
    private final Map<Integer, Set<IExternalSensorhubDataListener>> extshbListener = new ConcurrentHashMap(0);
    private boolean isChannelInited = false;

    private int makeListenerKey(byte b, byte b2) {
        return (b * 256) + b2;
    }

    private static native int nativeClassInit();

    private static native int nativeCreateExternalSensorhubChannel();

    private static native int nativeDestroyExternalSensorhubChannel();

    private static native int nativeQueryMaxDataLen();

    private static native int nativeRegiserCallback(byte b, byte b2);

    private static native CommandResult nativeSendCommand(byte b, byte b2, byte[] bArr, boolean z);

    private static native int nativeUnregiserCallback(byte b, byte b2);

    static {
        HiLog.info(TAG, "loadLibrary begin", new Object[0]);
        System.loadLibrary("extshb_jni.z");
        HiLog.info(TAG, "loadLibrary end", new Object[0]);
    }

    private ExternalSensorhubManager() {
    }

    public static ExternalSensorhubManager getInstance() {
        if (instance == null) {
            HiLog.info(TAG, "start native class load", new Object[0]);
            synchronized (ExternalSensorhubManager.class) {
                if (instance == null) {
                    instance = new ExternalSensorhubManager();
                }
            }
            HiLog.info(TAG, "end native class load", new Object[0]);
        }
        return instance;
    }

    public int queryMaxDataLen() {
        return nativeQueryMaxDataLen();
    }

    public CommandResult sendCommand(byte b, byte b2, byte[] bArr, boolean z) {
        if (!checkNativeInit()) {
            HiLog.error(TAG, "native init failed", new Object[0]);
            return null;
        } else if (hasCreateChannel()) {
            return nativeSendCommand(b, b2, bArr, z);
        } else {
            HiLog.error(TAG, "create channel error", new Object[0]);
            return null;
        }
    }

    public int subscribeDataListener(byte b, byte b2, IExternalSensorhubDataListener iExternalSensorhubDataListener) {
        if (iExternalSensorhubDataListener == null) {
            HiLog.error(TAG, "subscribe listener cannot be null", new Object[0]);
            return INVALID_RESULT;
        } else if (!checkNativeInit()) {
            HiLog.error(TAG, "native init failed", new Object[0]);
            return INVALID_RESULT;
        } else if (!hasCreateChannel()) {
            HiLog.error(TAG, "create channel error", new Object[0]);
            return INVALID_RESULT;
        } else {
            int nativeRegiserCallback = nativeRegiserCallback(b, b2);
            if (nativeRegiserCallback < 0) {
                HiLog.error(TAG, "register callback failed, error: %{public}d", new Object[]{Integer.valueOf(nativeRegiserCallback)});
                return nativeRegiserCallback;
            }
            int makeListenerKey = makeListenerKey(b, b2);
            if (this.extshbListener.containsKey(Integer.valueOf(makeListenerKey))) {
                this.extshbListener.get(Integer.valueOf(makeListenerKey)).add(iExternalSensorhubDataListener);
            } else {
                HashSet hashSet = new HashSet(0);
                hashSet.add(iExternalSensorhubDataListener);
                this.extshbListener.put(Integer.valueOf(makeListenerKey), hashSet);
            }
            return 0;
        }
    }

    public int unsubscribeDataListener(byte b, byte b2, IExternalSensorhubDataListener iExternalSensorhubDataListener) {
        if (iExternalSensorhubDataListener == null || !checkNativeInit()) {
            return INVALID_RESULT;
        }
        int makeListenerKey = makeListenerKey(b, b2);
        if (!this.extshbListener.containsKey(Integer.valueOf(makeListenerKey))) {
            return 0;
        }
        this.extshbListener.get(Integer.valueOf(makeListenerKey)).remove(iExternalSensorhubDataListener);
        if (this.extshbListener.get(Integer.valueOf(makeListenerKey)).isEmpty()) {
            int nativeUnregiserCallback = nativeUnregiserCallback(b, b2);
            if (nativeUnregiserCallback < 0) {
                return nativeUnregiserCallback;
            }
            this.extshbListener.remove(Integer.valueOf(makeListenerKey));
        }
        if (this.extshbListener.isEmpty()) {
            hasDestroyChannel();
        }
        return 0;
    }

    private boolean checkNativeInit() {
        synchronized (FIRST_INIT_LOCK) {
            if (isInitNative) {
                return true;
            }
            if (nativeClassInit() == 0) {
                isInitNative = true;
                HiLog.debug(TAG, "nativeClassInit success", new Object[0]);
                return true;
            }
            HiLog.error(TAG, "nativeClassInit failed", new Object[0]);
            return false;
        }
    }

    private boolean hasCreateChannel() {
        if (!this.isChannelInited) {
            if (nativeCreateExternalSensorhubChannel() == 0) {
                HiLog.info(TAG, "create channel success", new Object[0]);
                this.isChannelInited = true;
            } else {
                HiLog.error(TAG, "create channel failed", new Object[0]);
                return false;
            }
        }
        return true;
    }

    private void hasDestroyChannel() {
        if (this.isChannelInited) {
            if (nativeDestroyExternalSensorhubChannel() == 0) {
                HiLog.info(TAG, "destroy channel success", new Object[0]);
                this.isChannelInited = false;
                return;
            }
            HiLog.error(TAG, "destroy channel failed", new Object[0]);
        }
    }

    public void processData(byte b, byte b2, byte[] bArr) {
        if (bArr == null) {
            HiLog.error(TAG, "begin process data cannot be null", new Object[0]);
            return;
        }
        Set<IExternalSensorhubDataListener> set = this.extshbListener.get(Integer.valueOf(makeListenerKey(b, b2)));
        if (set != null) {
            HiLog.info(TAG, "listener will callback", new Object[0]);
            for (IExternalSensorhubDataListener iExternalSensorhubDataListener : set) {
                if (iExternalSensorhubDataListener != null) {
                    iExternalSensorhubDataListener.onDataReceived(b, b2, bArr);
                }
            }
        }
        Set<IExternalSensorhubDataListener> set2 = this.extshbListener.get(Integer.valueOf(makeListenerKey(b, (byte) -1)));
        if (set2 != null) {
            HiLog.info(TAG, "listener will callback", new Object[0]);
            for (IExternalSensorhubDataListener iExternalSensorhubDataListener2 : set2) {
                if (iExternalSensorhubDataListener2 != null) {
                    iExternalSensorhubDataListener2.onDataReceived(b, b2, bArr);
                }
            }
        }
    }
}

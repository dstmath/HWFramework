package ohos.system;

import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public final class Events {
    public static final int DEFAULT_TIMEOUT = 3000;
    public static final int HILOG_DOMAIN = 218115072;
    private static final HiLogLabel TAG = new HiLogLabel(3, 218115072, Events.class.getSimpleName());

    private static native void nativePublish(EventResult eventResult, String str);

    private static native void nativeSubscribe(EventResult eventResult, String str, int i);

    static {
        try {
            System.loadLibrary("sysevent_jni.z");
        } catch (UnsatisfiedLinkError e) {
            HiLog.error(TAG, "load sysevent_jni.z UnsatisfiedLinkError: %{public}s", e);
        }
    }

    public static boolean subscribe(String str) throws EventException {
        return subscribe(str, 3000);
    }

    public static boolean subscribe(String str, int i) throws EventException {
        if (str == null || str.trim().length() == 0 || i <= 0) {
            return false;
        }
        EventResult eventResult = new EventResult();
        nativeSubscribe(eventResult, str.trim(), i);
        if (eventResult.isOk()) {
            return true;
        }
        throw new EventException(eventResult.getErrorInfo());
    }

    public static boolean publish(String str) throws EventException {
        if (str == null || str.trim().length() == 0) {
            return false;
        }
        EventResult eventResult = new EventResult();
        nativePublish(eventResult, str.trim());
        if (eventResult.isOk()) {
            return true;
        }
        throw new EventException(eventResult.getErrorInfo());
    }
}

package android.os;

import android.rms.iaware.HwIAwareHandler;

public class HwHandlerImpl implements IHwHandler {
    private static final String TAG = "HwHandlerImpl";
    private static HwHandlerImpl mInstance = null;

    public static synchronized HwHandlerImpl getDefault() {
        HwHandlerImpl hwHandlerImpl;
        synchronized (HwHandlerImpl.class) {
            if (mInstance == null) {
                mInstance = new HwHandlerImpl();
            }
            hwHandlerImpl = mInstance;
        }
        return hwHandlerImpl;
    }

    private HwHandlerImpl() {
    }

    public long resetMessageDelayMillis(Message msg, long delayMillis) {
        if (delayMillis <= 100 || delayMillis >= 300 || msg == null) {
            return delayMillis;
        }
        return HwIAwareHandler.getInstance().resetDelayMills(msg, delayMillis);
    }
}

package huawei.android.os;

public class HwDeviceIdleManager {
    private static final String TAG = "HwDeviceIdleManager";
    private static volatile HwDeviceIdleManager mInstance = null;

    public static synchronized HwDeviceIdleManager getInstance() {
        HwDeviceIdleManager hwDeviceIdleManager;
        synchronized (HwDeviceIdleManager.class) {
            if (mInstance == null) {
                mInstance = new HwDeviceIdleManager();
            }
            hwDeviceIdleManager = mInstance;
        }
        return hwDeviceIdleManager;
    }

    public int forceIdle() {
        return HwGeneralManager.getInstance().forceIdle();
    }
}

package huawei.android.os;

public class HwLocalDevManager {
    private static final String TAG = "HwLocalDevManager";
    private static HwLocalDevManager sInstance = null;

    public static synchronized HwLocalDevManager getInstance() {
        HwLocalDevManager hwLocalDevManager;
        synchronized (HwLocalDevManager.class) {
            if (sInstance == null) {
                sInstance = new HwLocalDevManager();
            }
            hwLocalDevManager = sInstance;
        }
        return hwLocalDevManager;
    }

    public int getLocalDevStat(int dev) {
        return HwGeneralManager.getInstance().getLocalDevStat(dev);
    }

    public String getDeviceId(int dev) {
        return HwGeneralManager.getInstance().getDeviceId(dev);
    }

    public int doSdcardCheckRW() {
        return HwGeneralManager.getInstance().doSdcardCheckRW();
    }
}

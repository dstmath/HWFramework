package huawei.android.os;

public class HwLocalDevManager {
    private static final String TAG = "HwLocalDevManager";
    private static volatile HwLocalDevManager mInstance = null;

    public static synchronized HwLocalDevManager getInstance() {
        HwLocalDevManager hwLocalDevManager;
        synchronized (HwLocalDevManager.class) {
            if (mInstance == null) {
                mInstance = new HwLocalDevManager();
            }
            hwLocalDevManager = mInstance;
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

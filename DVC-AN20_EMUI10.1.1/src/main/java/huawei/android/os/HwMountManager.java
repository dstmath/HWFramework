package huawei.android.os;

import android.os.IBinder;

public class HwMountManager {
    private static final String TAG = "HwMountManager";
    private static volatile HwMountManager mInstance = null;

    public static synchronized HwMountManager getInstance() {
        HwMountManager hwMountManager;
        synchronized (HwMountManager.class) {
            if (mInstance == null) {
                mInstance = new HwMountManager();
            }
            hwMountManager = mInstance;
        }
        return hwMountManager;
    }

    public String mountCifs(String source, String option, IBinder binder) {
        return HwGeneralManager.getInstance().mountCifs(source, option, binder);
    }

    public void unmountCifs(String mountPoint) {
        HwGeneralManager.getInstance().unmountCifs(mountPoint);
    }

    public int isSupportedCifs() {
        return HwGeneralManager.getInstance().isSupportedCifs();
    }
}

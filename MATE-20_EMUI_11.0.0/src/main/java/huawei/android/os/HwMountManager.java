package huawei.android.os;

import android.os.IBinder;

public class HwMountManager {
    private static final String TAG = "HwMountManager";
    private static HwMountManager sInstance = null;

    public static synchronized HwMountManager getInstance() {
        HwMountManager hwMountManager;
        synchronized (HwMountManager.class) {
            if (sInstance == null) {
                sInstance = new HwMountManager();
            }
            hwMountManager = sInstance;
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

package huawei.android.hardware.hwtouchweight;

import huawei.android.os.HwGeneralManager;

public class HwTouchWeightManager {
    private static volatile HwTouchWeightManager mInstance = null;

    public static synchronized HwTouchWeightManager getInstance() {
        HwTouchWeightManager hwTouchWeightManager;
        synchronized (HwTouchWeightManager.class) {
            if (mInstance == null) {
                mInstance = new HwTouchWeightManager();
            }
            hwTouchWeightManager = mInstance;
        }
        return hwTouchWeightManager;
    }

    public void resetTouchWeight() {
        HwGeneralManager.getInstance().resetTouchWeight();
    }

    public String getTouchWeightValue() {
        return HwGeneralManager.getInstance().getTouchWeightValue();
    }
}

package huawei.android.os;

public class HwProtectAreaManager {
    private static final String TAG = "HwProtectAreaManager";
    private static HwProtectAreaManager sInstance = null;

    public static synchronized HwProtectAreaManager getInstance() {
        HwProtectAreaManager hwProtectAreaManager;
        synchronized (HwProtectAreaManager.class) {
            if (sInstance == null) {
                sInstance = new HwProtectAreaManager();
            }
            hwProtectAreaManager = sInstance;
        }
        return hwProtectAreaManager;
    }

    public int readProtectArea(String optItem, int readBufLen, String[] readBuf, int[] errorNum) {
        return HwGeneralManager.getInstance().readProtectArea(optItem, readBufLen, readBuf, errorNum);
    }

    public int writeProtectArea(String optItem, int writeLen, String writeBuf, int[] errorNum) {
        return HwGeneralManager.getInstance().writeProtectArea(optItem, writeLen, writeBuf, errorNum);
    }
}

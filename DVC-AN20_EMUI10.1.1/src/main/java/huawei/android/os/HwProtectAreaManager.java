package huawei.android.os;

public class HwProtectAreaManager {
    private static final String TAG = "HwProtectAreaManager";
    private static volatile HwProtectAreaManager mInstance = null;

    public static synchronized HwProtectAreaManager getInstance() {
        HwProtectAreaManager hwProtectAreaManager;
        synchronized (HwProtectAreaManager.class) {
            if (mInstance == null) {
                mInstance = new HwProtectAreaManager();
            }
            hwProtectAreaManager = mInstance;
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

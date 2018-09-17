package huawei.android.os;

public class HwSdCardCryptdManager {
    private static final String TAG = "HwSdCardCryptdManager";
    private static volatile HwSdCardCryptdManager mInstance = null;

    public static synchronized HwSdCardCryptdManager getInstance() {
        HwSdCardCryptdManager hwSdCardCryptdManager;
        synchronized (HwSdCardCryptdManager.class) {
            if (mInstance == null) {
                mInstance = new HwSdCardCryptdManager();
            }
            hwSdCardCryptdManager = mInstance;
        }
        return hwSdCardCryptdManager;
    }

    public int setSdCardCryptdEnable(boolean enable, String volId) {
        return HwGeneralManager.getInstance().setSdCardCryptdEnable(enable, volId);
    }

    public int unlockSdCardKey(int userId, int serialNumber, byte[] token, byte[] secret) {
        return HwGeneralManager.getInstance().unlockSdCardKey(userId, serialNumber, token, secret);
    }

    public int addSdCardUserKeyAuth(int userId, int serialNumber, byte[] token, byte[] secret) {
        return HwGeneralManager.getInstance().addSdCardUserKeyAuth(userId, serialNumber, token, secret);
    }

    public int backupSecretkey() {
        return HwGeneralManager.getInstance().backupSecretkey();
    }
}

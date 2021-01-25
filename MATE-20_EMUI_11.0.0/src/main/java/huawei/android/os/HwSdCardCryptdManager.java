package huawei.android.os;

public class HwSdCardCryptdManager {
    private static final String TAG = "HwSdCardCryptdManager";
    private static HwSdCardCryptdManager sInstance = null;

    public static synchronized HwSdCardCryptdManager getInstance() {
        HwSdCardCryptdManager hwSdCardCryptdManager;
        synchronized (HwSdCardCryptdManager.class) {
            if (sInstance == null) {
                sInstance = new HwSdCardCryptdManager();
            }
            hwSdCardCryptdManager = sInstance;
        }
        return hwSdCardCryptdManager;
    }

    public int setSdCardCryptdEnable(boolean isEnable, String volId) {
        return HwGeneralManager.getInstance().setSdCardCryptdEnable(isEnable, volId);
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

package android.encrypt;

import android.os.SystemProperties;
import huawei.android.os.HwGeneralManager;

public class SDCardCryptedHelper implements ISDCardCryptedHelper {
    private static final String SDCARD_STATUS_UNLOCK = "unlock";

    public int addUserKeyAuth(int userId, int serialNumber, byte[] token, byte[] secret) {
        return HwGeneralManager.getInstance().addSdCardUserKeyAuth(userId, serialNumber, token, secret);
    }

    public int unlockKey(int userId, int serialNumber, byte[] token, byte[] secret) {
        if (userId == 0 && !SystemProperties.get("vold.cryptsd.keystate", "").equals(SDCARD_STATUS_UNLOCK)) {
            return HwGeneralManager.getInstance().unlockSdCardKey(userId, serialNumber, token, secret);
        }
        return -1;
    }
}

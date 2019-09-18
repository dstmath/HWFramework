package com.huawei.android.os;

import huawei.android.os.HwSdCardCryptdManager;

public class HwSdCardCryptdEx {
    public static final int RESULT_ERROR = 500;
    public static final int RESULT_ERROR_BACKUP = 400;
    public static final int RESULT_ERROR_NO_ANSWER = -1;
    public static final int RESULT_NOT_SUPPORT = -10;
    public static final int RESULT_OK = 200;

    public static int setSdCardCryptdEnable(boolean enable, String volId) {
        return HwSdCardCryptdManager.getInstance().setSdCardCryptdEnable(enable, volId);
    }

    public static int backupSecretkey() {
        return HwSdCardCryptdManager.getInstance().backupSecretkey();
    }
}

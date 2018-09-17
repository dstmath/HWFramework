package com.android.internal.telephony;

import android.os.SystemProperties;
import android.telephony.Rlog;
import java.io.FileInputStream;
import java.io.IOException;

public class HwCardTrayUtil {
    private static final int CARDTRAY_OUT_SLOT = 0;
    private static final String CARDTRAY_STATE_FILE = "/sys/kernel/sim/sim_hotplug_state";
    public static final boolean IS_SINGLE_CARD_TRAY = SystemProperties.getBoolean("persist.radio.single_card_tray", true);
    private static final String TAG = "HwCardTrayUtil";

    private static String getStateFile() {
        return CARDTRAY_STATE_FILE;
    }

    /* JADX WARNING: Removed duplicated region for block: B:28:0x0045 A:{SYNTHETIC, Splitter: B:28:0x0045} */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x004e A:{SYNTHETIC, Splitter: B:35:0x004e} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean isCardTrayOut(int SlotId) {
        Throwable th;
        boolean z = true;
        byte[] cardTrayState = new byte[4];
        FileInputStream fis = null;
        try {
            FileInputStream fis2 = new FileInputStream(getStateFile());
            try {
                int length = fis2.read(cardTrayState, 0, 4);
                fis2.close();
                if (length < 4) {
                    loge("isCardTrayOut read byte fail.");
                    if (fis2 != null) {
                        try {
                            fis2.close();
                        } catch (IOException e) {
                            return false;
                        }
                    }
                    return false;
                }
                if (fis2 != null) {
                    try {
                        fis2.close();
                    } catch (IOException e2) {
                        return false;
                    }
                }
                if (SlotId < 0 || SlotId > 1) {
                    return false;
                }
                if (cardTrayState[(SlotId * 2) + 1] != (byte) 0) {
                    z = false;
                }
                return z;
            } catch (IOException e3) {
                fis = fis2;
                try {
                    loge("isCardTrayOut Exception");
                    if (fis != null) {
                    }
                    return false;
                } catch (Throwable th2) {
                    th = th2;
                    if (fis != null) {
                        try {
                            fis.close();
                        } catch (IOException e4) {
                            return false;
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                fis = fis2;
                if (fis != null) {
                }
                throw th;
            }
        } catch (IOException e5) {
            loge("isCardTrayOut Exception");
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e6) {
                    return false;
                }
            }
            return false;
        }
    }

    private static void loge(String message) {
        Rlog.e(TAG, message);
    }
}

package com.android.internal.telephony;

import android.os.SystemProperties;
import android.util.Log;
import java.util.Locale;

public class CsgSearchFactory {
    private static final String LOG_TAG = "CsgSearchFactory";
    public static final int PLATFORM_HISI = 1;
    public static final String PLATFORM_KEY = "ro.board.platform";
    public static final int PLATFORM_NONE = 0;
    public static final int PLATFORM_QCOM = 2;

    private static int getChipsetType() {
        String stringtmp = SystemProperties.get(PLATFORM_KEY, "").toLowerCase(Locale.getDefault());
        if (stringtmp.startsWith("k3") || stringtmp.startsWith("hi") || stringtmp.startsWith("kirin")) {
            return 1;
        }
        if (stringtmp.startsWith("msm") || stringtmp.startsWith("qsc") || stringtmp.startsWith("titanium")) {
            return 2;
        }
        return 0;
    }

    public static CsgSearch createCsgSearch(GsmCdmaPhone phone) {
        CsgSearch csgSearch = null;
        int type = getChipsetType();
        switch (type) {
            case 1:
                csgSearch = new HwHisiCsgSearch(phone);
                break;
            case 2:
                csgSearch = new HwQualcommCsgSearch(phone);
                break;
        }
        Log.i(LOG_TAG, "platform type: " + type);
        return csgSearch;
    }

    public static boolean isHisiChipset() {
        return 1 == getChipsetType();
    }
}

package android.telephony;

import android.os.SystemProperties;

public class HwCustSignalStrengthImpl extends HwCustSignalStrength {
    private static final boolean IS_DOCOMO = SystemProperties.get("ro.product.custom", "NULL").contains("docomo");

    public int getGsmSignalStrength(int mGsmSignalStrength) {
        if (!isDocomo()) {
            return mGsmSignalStrength;
        }
        int GsmDbm;
        if (mGsmSignalStrength == 0) {
            GsmDbm = 99;
        } else {
            GsmDbm = (mGsmSignalStrength + 113) / 2;
        }
        return GsmDbm;
    }

    public int getGsmDbm(int mGsmSignalStrength) {
        if (!isDocomo()) {
            return 0;
        }
        int asu = mGsmSignalStrength == 99 ? -1 : mGsmSignalStrength;
        if (asu != -1) {
            return (asu * 2) - 113;
        }
        return -1;
    }

    public boolean isDocomo() {
        return IS_DOCOMO;
    }
}

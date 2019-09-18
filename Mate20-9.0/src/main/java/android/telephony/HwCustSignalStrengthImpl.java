package android.telephony;

import android.os.SystemProperties;

public class HwCustSignalStrengthImpl extends HwCustSignalStrength {
    private static final boolean IS_DOCOMO = SystemProperties.get("ro.product.custom", "NULL").contains("docomo");

    public int getGsmSignalStrength(int mGsmSignalStrength) {
        if (!IS_DOCOMO || mGsmSignalStrength != 0) {
            return mGsmSignalStrength;
        }
        return 99;
    }

    public int getGsmDbm(int mGsmSignalStrength) {
        if (!IS_DOCOMO) {
            return 0;
        }
        int asu = mGsmSignalStrength == 99 ? -1 : mGsmSignalStrength;
        if (asu != -1) {
            return -113 + (2 * asu);
        }
        return -1;
    }
}

package vendor.huawei.hardware.hisiradio.V1_1;

import android.security.keystore.KeyProperties;
import java.util.ArrayList;

public final class HwCellInfoType_1_1 {
    public static final int CDMA = 2;
    public static final int GSM = 1;
    public static final int LTE = 3;
    public static final int NONE = 0;
    public static final int NR = 6;
    public static final int TD_SCDMA = 5;
    public static final int WCDMA = 4;

    public static final String toString(int o) {
        if (o == 0) {
            return KeyProperties.DIGEST_NONE;
        }
        if (o == 1) {
            return "GSM";
        }
        if (o == 2) {
            return "CDMA";
        }
        if (o == 3) {
            return "LTE";
        }
        if (o == 4) {
            return "WCDMA";
        }
        if (o == 5) {
            return "TD_SCDMA";
        }
        if (o == 6) {
            return "NR";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add(KeyProperties.DIGEST_NONE);
        if ((o & 1) == 1) {
            list.add("GSM");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("CDMA");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("LTE");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("WCDMA");
            flipped |= 4;
        }
        if ((o & 5) == 5) {
            list.add("TD_SCDMA");
            flipped |= 5;
        }
        if ((o & 6) == 6) {
            list.add("NR");
            flipped |= 6;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}

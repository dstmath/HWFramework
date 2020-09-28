package android.hardware.contexthub.V1_0;

import android.net.wifi.WifiManager;
import java.util.ArrayList;

public final class TransactionResult {
    public static final int FAILURE = 1;
    public static final int SUCCESS = 0;

    public static final String toString(int o) {
        if (o == 0) {
            return WifiManager.PPPOE_RESULT_SUCCESS;
        }
        if (o == 1) {
            return WifiManager.PPPOE_RESULT_FAILED;
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add(WifiManager.PPPOE_RESULT_SUCCESS);
        if ((o & 1) == 1) {
            list.add(WifiManager.PPPOE_RESULT_FAILED);
            flipped = 0 | 1;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}

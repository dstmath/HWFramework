package android.hardware.thermal.V2_0;

import android.security.keystore.KeyProperties;
import java.util.ArrayList;

public final class ThrottlingSeverity {
    public static final int CRITICAL = 4;
    public static final int EMERGENCY = 5;
    public static final int LIGHT = 1;
    public static final int MODERATE = 2;
    public static final int NONE = 0;
    public static final int SEVERE = 3;
    public static final int SHUTDOWN = 6;

    public static final String toString(int o) {
        if (o == 0) {
            return KeyProperties.DIGEST_NONE;
        }
        if (o == 1) {
            return "LIGHT";
        }
        if (o == 2) {
            return "MODERATE";
        }
        if (o == 3) {
            return "SEVERE";
        }
        if (o == 4) {
            return "CRITICAL";
        }
        if (o == 5) {
            return "EMERGENCY";
        }
        if (o == 6) {
            return "SHUTDOWN";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add(KeyProperties.DIGEST_NONE);
        if ((o & 1) == 1) {
            list.add("LIGHT");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("MODERATE");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("SEVERE");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("CRITICAL");
            flipped |= 4;
        }
        if ((o & 5) == 5) {
            list.add("EMERGENCY");
            flipped |= 5;
        }
        if ((o & 6) == 6) {
            list.add("SHUTDOWN");
            flipped |= 6;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}

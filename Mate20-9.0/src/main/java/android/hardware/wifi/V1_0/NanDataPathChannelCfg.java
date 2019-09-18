package android.hardware.wifi.V1_0;

import java.util.ArrayList;

public final class NanDataPathChannelCfg {
    public static final int CHANNEL_NOT_REQUESTED = 0;
    public static final int FORCE_CHANNEL_SETUP = 2;
    public static final int REQUEST_CHANNEL_SETUP = 1;

    public static final String toString(int o) {
        if (o == 0) {
            return "CHANNEL_NOT_REQUESTED";
        }
        if (o == 1) {
            return "REQUEST_CHANNEL_SETUP";
        }
        if (o == 2) {
            return "FORCE_CHANNEL_SETUP";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("CHANNEL_NOT_REQUESTED");
        if ((o & 1) == 1) {
            list.add("REQUEST_CHANNEL_SETUP");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("FORCE_CHANNEL_SETUP");
            flipped |= 2;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}

package android.hardware.broadcastradio.V2_0;

import java.util.ArrayList;

public final class ProgramInfoFlags {
    public static final int LIVE = 1;
    public static final int MUTED = 2;
    public static final int STEREO = 32;
    public static final int TRAFFIC_ANNOUNCEMENT = 8;
    public static final int TRAFFIC_PROGRAM = 4;
    public static final int TUNED = 16;

    public static final String toString(int o) {
        if (o == 1) {
            return "LIVE";
        }
        if (o == 2) {
            return "MUTED";
        }
        if (o == 4) {
            return "TRAFFIC_PROGRAM";
        }
        if (o == 8) {
            return "TRAFFIC_ANNOUNCEMENT";
        }
        if (o == 16) {
            return "TUNED";
        }
        if (o == 32) {
            return "STEREO";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        if ((o & 1) == 1) {
            list.add("LIVE");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("MUTED");
            flipped |= 2;
        }
        if ((o & 4) == 4) {
            list.add("TRAFFIC_PROGRAM");
            flipped |= 4;
        }
        if ((o & 8) == 8) {
            list.add("TRAFFIC_ANNOUNCEMENT");
            flipped |= 8;
        }
        if ((o & 16) == 16) {
            list.add("TUNED");
            flipped |= 16;
        }
        if ((o & 32) == 32) {
            list.add("STEREO");
            flipped |= 32;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}

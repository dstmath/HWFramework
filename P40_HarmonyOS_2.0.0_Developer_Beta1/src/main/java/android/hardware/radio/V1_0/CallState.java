package android.hardware.radio.V1_0;

import java.util.ArrayList;

public final class CallState {
    public static final int ACTIVE = 0;
    public static final int ALERTING = 3;
    public static final int DIALING = 2;
    public static final int HOLDING = 1;
    public static final int INCOMING = 4;
    public static final int WAITING = 5;

    public static final String toString(int o) {
        if (o == 0) {
            return "ACTIVE";
        }
        if (o == 1) {
            return "HOLDING";
        }
        if (o == 2) {
            return "DIALING";
        }
        if (o == 3) {
            return "ALERTING";
        }
        if (o == 4) {
            return "INCOMING";
        }
        if (o == 5) {
            return "WAITING";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("ACTIVE");
        if ((o & 1) == 1) {
            list.add("HOLDING");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("DIALING");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("ALERTING");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("INCOMING");
            flipped |= 4;
        }
        if ((o & 5) == 5) {
            list.add("WAITING");
            flipped |= 5;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}

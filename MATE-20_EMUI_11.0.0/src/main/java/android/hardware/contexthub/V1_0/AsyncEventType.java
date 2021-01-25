package android.hardware.contexthub.V1_0;

import java.util.ArrayList;

public final class AsyncEventType {
    public static final int RESTARTED = 1;

    public static final String toString(int o) {
        if (o == 1) {
            return "RESTARTED";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        if ((o & 1) == 1) {
            list.add("RESTARTED");
            flipped = 0 | 1;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}

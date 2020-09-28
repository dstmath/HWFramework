package vendor.mediatek.hardware.fm.V1_0;

import java.util.ArrayList;

public final class Direction {
    public static final int DOWN = 0;
    public static final int UP = 1;

    public static final String toString(int o) {
        if (o == 0) {
            return "DOWN";
        }
        if (o == 1) {
            return "UP";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("DOWN");
        if ((o & 1) == 1) {
            list.add("UP");
            flipped = 0 | 1;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}

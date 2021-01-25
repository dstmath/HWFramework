package android.hardware.wifi.V1_0;

import java.util.ArrayList;

public final class WifiChannelWidthInMhz {
    public static final int WIDTH_10 = 6;
    public static final int WIDTH_160 = 3;
    public static final int WIDTH_20 = 0;
    public static final int WIDTH_40 = 1;
    public static final int WIDTH_5 = 5;
    public static final int WIDTH_80 = 2;
    public static final int WIDTH_80P80 = 4;
    public static final int WIDTH_INVALID = -1;

    public static final String toString(int o) {
        if (o == 0) {
            return "WIDTH_20";
        }
        if (o == 1) {
            return "WIDTH_40";
        }
        if (o == 2) {
            return "WIDTH_80";
        }
        if (o == 3) {
            return "WIDTH_160";
        }
        if (o == 4) {
            return "WIDTH_80P80";
        }
        if (o == 5) {
            return "WIDTH_5";
        }
        if (o == 6) {
            return "WIDTH_10";
        }
        if (o == -1) {
            return "WIDTH_INVALID";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("WIDTH_20");
        if ((o & 1) == 1) {
            list.add("WIDTH_40");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("WIDTH_80");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("WIDTH_160");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("WIDTH_80P80");
            flipped |= 4;
        }
        if ((o & 5) == 5) {
            list.add("WIDTH_5");
            flipped |= 5;
        }
        if ((o & 6) == 6) {
            list.add("WIDTH_10");
            flipped |= 6;
        }
        if ((o & -1) == -1) {
            list.add("WIDTH_INVALID");
            flipped |= -1;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}

package android.hardware.gnss.V1_0;

import java.util.ArrayList;

public final class GnssLocationFlags {
    public static final short HAS_ALTITUDE = 2;
    public static final short HAS_BEARING = 8;
    public static final short HAS_BEARING_ACCURACY = 128;
    public static final short HAS_HORIZONTAL_ACCURACY = 16;
    public static final short HAS_LAT_LONG = 1;
    public static final short HAS_SPEED = 4;
    public static final short HAS_SPEED_ACCURACY = 64;
    public static final short HAS_VERTICAL_ACCURACY = 32;

    public static final String toString(short o) {
        if (o == 1) {
            return "HAS_LAT_LONG";
        }
        if (o == 2) {
            return "HAS_ALTITUDE";
        }
        if (o == 4) {
            return "HAS_SPEED";
        }
        if (o == 8) {
            return "HAS_BEARING";
        }
        if (o == 16) {
            return "HAS_HORIZONTAL_ACCURACY";
        }
        if (o == 32) {
            return "HAS_VERTICAL_ACCURACY";
        }
        if (o == 64) {
            return "HAS_SPEED_ACCURACY";
        }
        if (o == 128) {
            return "HAS_BEARING_ACCURACY";
        }
        return "0x" + Integer.toHexString(Short.toUnsignedInt(o));
    }

    public static final String dumpBitfield(short o) {
        ArrayList<String> list = new ArrayList<>();
        short flipped = 0;
        if ((o & 1) == 1) {
            list.add("HAS_LAT_LONG");
            flipped = (short) (0 | 1);
        }
        if ((o & 2) == 2) {
            list.add("HAS_ALTITUDE");
            flipped = (short) (flipped | 2);
        }
        if ((o & 4) == 4) {
            list.add("HAS_SPEED");
            flipped = (short) (flipped | 4);
        }
        if ((o & 8) == 8) {
            list.add("HAS_BEARING");
            flipped = (short) (flipped | 8);
        }
        if ((o & 16) == 16) {
            list.add("HAS_HORIZONTAL_ACCURACY");
            flipped = (short) (flipped | 16);
        }
        if ((o & 32) == 32) {
            list.add("HAS_VERTICAL_ACCURACY");
            flipped = (short) (flipped | 32);
        }
        if ((o & 64) == 64) {
            list.add("HAS_SPEED_ACCURACY");
            flipped = (short) (flipped | 64);
        }
        if ((o & 128) == 128) {
            list.add("HAS_BEARING_ACCURACY");
            flipped = (short) (flipped | 128);
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString(Short.toUnsignedInt((short) ((~flipped) & o))));
        }
        return String.join(" | ", list);
    }
}

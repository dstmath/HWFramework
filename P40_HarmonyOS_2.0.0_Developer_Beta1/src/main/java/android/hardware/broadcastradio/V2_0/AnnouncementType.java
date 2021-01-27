package android.hardware.broadcastradio.V2_0;

import java.util.ArrayList;

public final class AnnouncementType {
    public static final byte EMERGENCY = 1;
    public static final byte EVENT = 6;
    public static final byte MISC = 8;
    public static final byte NEWS = 5;
    public static final byte SPORT = 7;
    public static final byte TRAFFIC = 3;
    public static final byte WARNING = 2;
    public static final byte WEATHER = 4;

    public static final String toString(byte o) {
        if (o == 1) {
            return "EMERGENCY";
        }
        if (o == 2) {
            return "WARNING";
        }
        if (o == 3) {
            return "TRAFFIC";
        }
        if (o == 4) {
            return "WEATHER";
        }
        if (o == 5) {
            return "NEWS";
        }
        if (o == 6) {
            return "EVENT";
        }
        if (o == 7) {
            return "SPORT";
        }
        if (o == 8) {
            return "MISC";
        }
        return "0x" + Integer.toHexString(Byte.toUnsignedInt(o));
    }

    public static final String dumpBitfield(byte o) {
        ArrayList<String> list = new ArrayList<>();
        byte flipped = 0;
        if ((o & 1) == 1) {
            list.add("EMERGENCY");
            flipped = (byte) (0 | 1);
        }
        if ((o & 2) == 2) {
            list.add("WARNING");
            flipped = (byte) (flipped | 2);
        }
        if ((o & 3) == 3) {
            list.add("TRAFFIC");
            flipped = (byte) (flipped | 3);
        }
        if ((o & 4) == 4) {
            list.add("WEATHER");
            flipped = (byte) (flipped | 4);
        }
        if ((o & 5) == 5) {
            list.add("NEWS");
            flipped = (byte) (flipped | 5);
        }
        if ((o & 6) == 6) {
            list.add("EVENT");
            flipped = (byte) (flipped | 6);
        }
        if ((o & 7) == 7) {
            list.add("SPORT");
            flipped = (byte) (flipped | 7);
        }
        if ((o & 8) == 8) {
            list.add("MISC");
            flipped = (byte) (flipped | 8);
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString(Byte.toUnsignedInt((byte) ((~flipped) & o))));
        }
        return String.join(" | ", list);
    }
}

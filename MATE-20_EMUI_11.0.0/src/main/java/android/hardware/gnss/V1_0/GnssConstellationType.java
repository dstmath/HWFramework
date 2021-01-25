package android.hardware.gnss.V1_0;

import java.util.ArrayList;

public final class GnssConstellationType {
    public static final byte BEIDOU = 5;
    public static final byte GALILEO = 6;
    public static final byte GLONASS = 3;
    public static final byte GPS = 1;
    public static final byte QZSS = 4;
    public static final byte SBAS = 2;
    public static final byte UNKNOWN = 0;

    public static final String toString(byte o) {
        if (o == 0) {
            return "UNKNOWN";
        }
        if (o == 1) {
            return "GPS";
        }
        if (o == 2) {
            return "SBAS";
        }
        if (o == 3) {
            return "GLONASS";
        }
        if (o == 4) {
            return "QZSS";
        }
        if (o == 5) {
            return "BEIDOU";
        }
        if (o == 6) {
            return "GALILEO";
        }
        return "0x" + Integer.toHexString(Byte.toUnsignedInt(o));
    }

    public static final String dumpBitfield(byte o) {
        ArrayList<String> list = new ArrayList<>();
        byte flipped = 0;
        list.add("UNKNOWN");
        if ((o & 1) == 1) {
            list.add("GPS");
            flipped = (byte) (0 | 1);
        }
        if ((o & 2) == 2) {
            list.add("SBAS");
            flipped = (byte) (flipped | 2);
        }
        if ((o & 3) == 3) {
            list.add("GLONASS");
            flipped = (byte) (flipped | 3);
        }
        if ((o & 4) == 4) {
            list.add("QZSS");
            flipped = (byte) (flipped | 4);
        }
        if ((o & 5) == 5) {
            list.add("BEIDOU");
            flipped = (byte) (flipped | 5);
        }
        if ((o & 6) == 6) {
            list.add("GALILEO");
            flipped = (byte) (flipped | 6);
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString(Byte.toUnsignedInt((byte) ((~flipped) & o))));
        }
        return String.join(" | ", list);
    }
}

package android.hardware.vibrator.V1_0;

import java.util.ArrayList;

public final class EffectStrength {
    public static final byte LIGHT = 0;
    public static final byte MEDIUM = 1;
    public static final byte STRONG = 2;

    public static final String toString(byte o) {
        if (o == 0) {
            return "LIGHT";
        }
        if (o == 1) {
            return "MEDIUM";
        }
        if (o == 2) {
            return "STRONG";
        }
        return "0x" + Integer.toHexString(Byte.toUnsignedInt(o));
    }

    public static final String dumpBitfield(byte o) {
        ArrayList<String> list = new ArrayList<>();
        byte flipped = 0;
        list.add("LIGHT");
        if ((o & 1) == 1) {
            list.add("MEDIUM");
            flipped = (byte) (0 | 1);
        }
        if ((o & 2) == 2) {
            list.add("STRONG");
            flipped = (byte) (flipped | 2);
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString(Byte.toUnsignedInt((byte) ((~flipped) & o))));
        }
        return String.join(" | ", list);
    }
}

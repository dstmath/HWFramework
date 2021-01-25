package android.hardware.radio.V1_4;

import java.util.ArrayList;

public final class EmergencyServiceCategory {
    public static final int AIEC = 64;
    public static final int AMBULANCE = 2;
    public static final int FIRE_BRIGADE = 4;
    public static final int MARINE_GUARD = 8;
    public static final int MIEC = 32;
    public static final int MOUNTAIN_RESCUE = 16;
    public static final int POLICE = 1;
    public static final int UNSPECIFIED = 0;

    public static final String toString(int o) {
        if (o == 0) {
            return "UNSPECIFIED";
        }
        if (o == 1) {
            return "POLICE";
        }
        if (o == 2) {
            return "AMBULANCE";
        }
        if (o == 4) {
            return "FIRE_BRIGADE";
        }
        if (o == 8) {
            return "MARINE_GUARD";
        }
        if (o == 16) {
            return "MOUNTAIN_RESCUE";
        }
        if (o == 32) {
            return "MIEC";
        }
        if (o == 64) {
            return "AIEC";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("UNSPECIFIED");
        if ((o & 1) == 1) {
            list.add("POLICE");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("AMBULANCE");
            flipped |= 2;
        }
        if ((o & 4) == 4) {
            list.add("FIRE_BRIGADE");
            flipped |= 4;
        }
        if ((o & 8) == 8) {
            list.add("MARINE_GUARD");
            flipped |= 8;
        }
        if ((o & 16) == 16) {
            list.add("MOUNTAIN_RESCUE");
            flipped |= 16;
        }
        if ((o & 32) == 32) {
            list.add("MIEC");
            flipped |= 32;
        }
        if ((o & 64) == 64) {
            list.add("AIEC");
            flipped |= 64;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}

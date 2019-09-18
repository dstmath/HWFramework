package android.hardware.radio.V1_0;

import java.util.ArrayList;

public final class ApnTypes {
    public static final int ALL = 1023;
    public static final int CBS = 128;
    public static final int DEFAULT = 1;
    public static final int DUN = 8;
    public static final int EMERGENCY = 512;
    public static final int FOTA = 32;
    public static final int HIPRI = 16;
    public static final int IA = 256;
    public static final int IMS = 64;
    public static final int MMS = 2;
    public static final int NONE = 0;
    public static final int SUPL = 4;

    public static final String toString(int o) {
        if (o == 0) {
            return "NONE";
        }
        if (o == 1) {
            return "DEFAULT";
        }
        if (o == 2) {
            return "MMS";
        }
        if (o == 4) {
            return "SUPL";
        }
        if (o == 8) {
            return "DUN";
        }
        if (o == 16) {
            return "HIPRI";
        }
        if (o == 32) {
            return "FOTA";
        }
        if (o == 64) {
            return "IMS";
        }
        if (o == 128) {
            return "CBS";
        }
        if (o == 256) {
            return "IA";
        }
        if (o == 512) {
            return "EMERGENCY";
        }
        if (o == 1023) {
            return "ALL";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("NONE");
        if ((o & 1) == 1) {
            list.add("DEFAULT");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("MMS");
            flipped |= 2;
        }
        if ((o & 4) == 4) {
            list.add("SUPL");
            flipped |= 4;
        }
        if ((o & 8) == 8) {
            list.add("DUN");
            flipped |= 8;
        }
        if ((o & 16) == 16) {
            list.add("HIPRI");
            flipped |= 16;
        }
        if ((o & 32) == 32) {
            list.add("FOTA");
            flipped |= 32;
        }
        if ((o & 64) == 64) {
            list.add("IMS");
            flipped |= 64;
        }
        if ((o & 128) == 128) {
            list.add("CBS");
            flipped |= 128;
        }
        if ((o & 256) == 256) {
            list.add("IA");
            flipped |= 256;
        }
        if ((o & 512) == 512) {
            list.add("EMERGENCY");
            flipped |= 512;
        }
        if ((o & ALL) == 1023) {
            list.add("ALL");
            flipped |= ALL;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}

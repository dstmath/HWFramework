package android.hardware.radio.V1_0;

import java.util.ArrayList;

public final class RadioCdmaSmsConst {
    public static final int ADDRESS_MAX = 36;
    public static final int BEARER_DATA_MAX = 255;
    public static final int IP_ADDRESS_SIZE = 4;
    public static final int MAX_UD_HEADERS = 7;
    public static final int SUBADDRESS_MAX = 36;
    public static final int UDH_ANIM_NUM_BITMAPS = 4;
    public static final int UDH_EO_DATA_SEGMENT_MAX = 131;
    public static final int UDH_LARGE_BITMAP_SIZE = 32;
    public static final int UDH_LARGE_PIC_SIZE = 128;
    public static final int UDH_MAX_SND_SIZE = 128;
    public static final int UDH_OTHER_SIZE = 226;
    public static final int UDH_SMALL_BITMAP_SIZE = 8;
    public static final int UDH_SMALL_PIC_SIZE = 32;
    public static final int UDH_VAR_PIC_SIZE = 134;
    public static final int USER_DATA_MAX = 229;

    public static final String toString(int o) {
        if (o == 36) {
            return "ADDRESS_MAX";
        }
        if (o == 36) {
            return "SUBADDRESS_MAX";
        }
        if (o == 255) {
            return "BEARER_DATA_MAX";
        }
        if (o == 128) {
            return "UDH_MAX_SND_SIZE";
        }
        if (o == 131) {
            return "UDH_EO_DATA_SEGMENT_MAX";
        }
        if (o == 7) {
            return "MAX_UD_HEADERS";
        }
        if (o == 229) {
            return "USER_DATA_MAX";
        }
        if (o == 128) {
            return "UDH_LARGE_PIC_SIZE";
        }
        if (o == 32) {
            return "UDH_SMALL_PIC_SIZE";
        }
        if (o == 134) {
            return "UDH_VAR_PIC_SIZE";
        }
        if (o == 4) {
            return "UDH_ANIM_NUM_BITMAPS";
        }
        if (o == 32) {
            return "UDH_LARGE_BITMAP_SIZE";
        }
        if (o == 8) {
            return "UDH_SMALL_BITMAP_SIZE";
        }
        if (o == 226) {
            return "UDH_OTHER_SIZE";
        }
        if (o == 4) {
            return "IP_ADDRESS_SIZE";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        if ((o & 36) == 36) {
            list.add("ADDRESS_MAX");
            flipped = 0 | 36;
        }
        if ((o & 36) == 36) {
            list.add("SUBADDRESS_MAX");
            flipped |= 36;
        }
        if ((o & 255) == 255) {
            list.add("BEARER_DATA_MAX");
            flipped |= 255;
        }
        if ((o & 128) == 128) {
            list.add("UDH_MAX_SND_SIZE");
            flipped |= 128;
        }
        if ((o & 131) == 131) {
            list.add("UDH_EO_DATA_SEGMENT_MAX");
            flipped |= 131;
        }
        if ((o & 7) == 7) {
            list.add("MAX_UD_HEADERS");
            flipped |= 7;
        }
        if ((o & 229) == 229) {
            list.add("USER_DATA_MAX");
            flipped |= 229;
        }
        if ((o & 128) == 128) {
            list.add("UDH_LARGE_PIC_SIZE");
            flipped |= 128;
        }
        if ((o & 32) == 32) {
            list.add("UDH_SMALL_PIC_SIZE");
            flipped |= 32;
        }
        if ((o & 134) == 134) {
            list.add("UDH_VAR_PIC_SIZE");
            flipped |= 134;
        }
        if ((o & 4) == 4) {
            list.add("UDH_ANIM_NUM_BITMAPS");
            flipped |= 4;
        }
        if ((o & 32) == 32) {
            list.add("UDH_LARGE_BITMAP_SIZE");
            flipped |= 32;
        }
        if ((o & 8) == 8) {
            list.add("UDH_SMALL_BITMAP_SIZE");
            flipped |= 8;
        }
        if ((o & 226) == 226) {
            list.add("UDH_OTHER_SIZE");
            flipped |= 226;
        }
        if ((o & 4) == 4) {
            list.add("IP_ADDRESS_SIZE");
            flipped |= 4;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}

package android.hardware.wifi.V1_0;

import java.util.ArrayList;

public final class WifiDebugRxPacketFate {
    public static final int DRV_DROP_FILTER = 7;
    public static final int DRV_DROP_INVALID = 8;
    public static final int DRV_DROP_NOBUFS = 9;
    public static final int DRV_DROP_OTHER = 10;
    public static final int DRV_QUEUED = 6;
    public static final int FW_DROP_FILTER = 2;
    public static final int FW_DROP_INVALID = 3;
    public static final int FW_DROP_NOBUFS = 4;
    public static final int FW_DROP_OTHER = 5;
    public static final int FW_QUEUED = 1;
    public static final int SUCCESS = 0;

    public static final String toString(int o) {
        if (o == 0) {
            return "SUCCESS";
        }
        if (o == 1) {
            return "FW_QUEUED";
        }
        if (o == 2) {
            return "FW_DROP_FILTER";
        }
        if (o == 3) {
            return "FW_DROP_INVALID";
        }
        if (o == 4) {
            return "FW_DROP_NOBUFS";
        }
        if (o == 5) {
            return "FW_DROP_OTHER";
        }
        if (o == 6) {
            return "DRV_QUEUED";
        }
        if (o == 7) {
            return "DRV_DROP_FILTER";
        }
        if (o == 8) {
            return "DRV_DROP_INVALID";
        }
        if (o == 9) {
            return "DRV_DROP_NOBUFS";
        }
        if (o == 10) {
            return "DRV_DROP_OTHER";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("SUCCESS");
        if ((o & 1) == 1) {
            list.add("FW_QUEUED");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("FW_DROP_FILTER");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("FW_DROP_INVALID");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("FW_DROP_NOBUFS");
            flipped |= 4;
        }
        if ((o & 5) == 5) {
            list.add("FW_DROP_OTHER");
            flipped |= 5;
        }
        if ((o & 6) == 6) {
            list.add("DRV_QUEUED");
            flipped |= 6;
        }
        if ((o & 7) == 7) {
            list.add("DRV_DROP_FILTER");
            flipped |= 7;
        }
        if ((o & 8) == 8) {
            list.add("DRV_DROP_INVALID");
            flipped |= 8;
        }
        if ((o & 9) == 9) {
            list.add("DRV_DROP_NOBUFS");
            flipped |= 9;
        }
        if ((o & 10) == 10) {
            list.add("DRV_DROP_OTHER");
            flipped |= 10;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}

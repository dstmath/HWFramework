package vendor.huawei.hardware.mtkradio.V1_0;

import java.util.ArrayList;

public final class SimHotSwap {
    public static final int SIM_HOTSWAP_COMMONSLOT_NO_CHANGED = 6;
    public static final int SIM_HOTSWAP_MISSING = 3;
    public static final int SIM_HOTSWAP_PLUG_IN = 0;
    public static final int SIM_HOTSWAP_PLUG_OUT = 1;
    public static final int SIM_HOTSWAP_RECOVERY = 2;
    public static final int SIM_HOTSWAP_TRAY_PLUG_IN = 4;
    public static final int SIM_HOTSWAP_TRAY_PLUG_OUT = 5;

    public static final String toString(int o) {
        if (o == 0) {
            return "SIM_HOTSWAP_PLUG_IN";
        }
        if (o == 1) {
            return "SIM_HOTSWAP_PLUG_OUT";
        }
        if (o == 2) {
            return "SIM_HOTSWAP_RECOVERY";
        }
        if (o == 3) {
            return "SIM_HOTSWAP_MISSING";
        }
        if (o == 4) {
            return "SIM_HOTSWAP_TRAY_PLUG_IN";
        }
        if (o == 5) {
            return "SIM_HOTSWAP_TRAY_PLUG_OUT";
        }
        if (o == 6) {
            return "SIM_HOTSWAP_COMMONSLOT_NO_CHANGED";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("SIM_HOTSWAP_PLUG_IN");
        if ((o & 1) == 1) {
            list.add("SIM_HOTSWAP_PLUG_OUT");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("SIM_HOTSWAP_RECOVERY");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("SIM_HOTSWAP_MISSING");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("SIM_HOTSWAP_TRAY_PLUG_IN");
            flipped |= 4;
        }
        if ((o & 5) == 5) {
            list.add("SIM_HOTSWAP_TRAY_PLUG_OUT");
            flipped |= 5;
        }
        if ((o & 6) == 6) {
            list.add("SIM_HOTSWAP_COMMONSLOT_NO_CHANGED");
            flipped |= 6;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}

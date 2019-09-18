package vendor.huawei.hardware.hisiradio.V1_0;

import java.util.ArrayList;

public final class RilSysInforType {
    public static final int ATTACH_EPS_INFO = 4;
    public static final int DETACH_REATTACH_INFO = 1;
    public static final int SIB1_ECC_INFO = 2;
    public static final int SIB2_AC_ECC_INFO = 3;

    public static final String toString(int o) {
        if (o == 1) {
            return "DETACH_REATTACH_INFO";
        }
        if (o == 2) {
            return "SIB1_ECC_INFO";
        }
        if (o == 3) {
            return "SIB2_AC_ECC_INFO";
        }
        if (o == 4) {
            return "ATTACH_EPS_INFO";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        if ((o & 1) == 1) {
            list.add("DETACH_REATTACH_INFO");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("SIB1_ECC_INFO");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("SIB2_AC_ECC_INFO");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("ATTACH_EPS_INFO");
            flipped |= 4;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}

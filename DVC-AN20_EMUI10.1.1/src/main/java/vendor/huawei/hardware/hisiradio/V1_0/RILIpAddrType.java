package vendor.huawei.hardware.hisiradio.V1_0;

import java.util.ArrayList;

public final class RILIpAddrType {
    public static final int IP_ADDR_TYPE_V4 = 0;
    public static final int IP_ADDR_TYPE_V6 = 1;

    public static final String toString(int o) {
        if (o == 0) {
            return "IP_ADDR_TYPE_V4";
        }
        if (o == 1) {
            return "IP_ADDR_TYPE_V6";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("IP_ADDR_TYPE_V4");
        if ((o & 1) == 1) {
            list.add("IP_ADDR_TYPE_V6");
            flipped = 0 | 1;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}

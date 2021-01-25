package vendor.huawei.hardware.qcomradio.V1_0;

import java.util.ArrayList;

public final class RILImsCallDomain {
    public static final int CALL_DOMAIN_AUTOMATIC = 3;
    public static final int CALL_DOMAIN_CS = 1;
    public static final int CALL_DOMAIN_PS = 2;
    public static final int CALL_DOMAIN_UNKNOWN = 0;

    public static final String toString(int o) {
        if (o == 0) {
            return "CALL_DOMAIN_UNKNOWN";
        }
        if (o == 1) {
            return "CALL_DOMAIN_CS";
        }
        if (o == 2) {
            return "CALL_DOMAIN_PS";
        }
        if (o == 3) {
            return "CALL_DOMAIN_AUTOMATIC";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("CALL_DOMAIN_UNKNOWN");
        if ((o & 1) == 1) {
            list.add("CALL_DOMAIN_CS");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("CALL_DOMAIN_PS");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("CALL_DOMAIN_AUTOMATIC");
            flipped |= 3;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}

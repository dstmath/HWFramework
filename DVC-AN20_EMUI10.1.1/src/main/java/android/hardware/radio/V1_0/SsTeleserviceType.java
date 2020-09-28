package android.hardware.radio.V1_0;

import java.util.ArrayList;

public final class SsTeleserviceType {
    public static final int ALL_DATA_TELESERVICES = 3;
    public static final int ALL_TELESERVICES_EXCEPT_SMS = 5;
    public static final int ALL_TELESEVICES = 1;
    public static final int ALL_TELE_AND_BEARER_SERVICES = 0;
    public static final int SMS_SERVICES = 4;
    public static final int TELEPHONY = 2;

    public static final String toString(int o) {
        if (o == 0) {
            return "ALL_TELE_AND_BEARER_SERVICES";
        }
        if (o == 1) {
            return "ALL_TELESEVICES";
        }
        if (o == 2) {
            return "TELEPHONY";
        }
        if (o == 3) {
            return "ALL_DATA_TELESERVICES";
        }
        if (o == 4) {
            return "SMS_SERVICES";
        }
        if (o == 5) {
            return "ALL_TELESERVICES_EXCEPT_SMS";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("ALL_TELE_AND_BEARER_SERVICES");
        if ((o & 1) == 1) {
            list.add("ALL_TELESEVICES");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("TELEPHONY");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("ALL_DATA_TELESERVICES");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("SMS_SERVICES");
            flipped |= 4;
        }
        if ((o & 5) == 5) {
            list.add("ALL_TELESERVICES_EXCEPT_SMS");
            flipped |= 5;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}

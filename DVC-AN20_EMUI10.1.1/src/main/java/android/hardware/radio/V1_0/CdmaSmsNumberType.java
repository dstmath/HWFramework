package android.hardware.radio.V1_0;

import com.android.internal.telephony.IccCardConstants;
import java.util.ArrayList;

public final class CdmaSmsNumberType {
    public static final int ABBREVIATED = 6;
    public static final int ALPHANUMERIC = 5;
    public static final int INTERNATIONAL_OR_DATA_IP = 1;
    public static final int NATIONAL_OR_INTERNET_MAIL = 2;
    public static final int NETWORK = 3;
    public static final int RESERVED_7 = 7;
    public static final int SUBSCRIBER = 4;
    public static final int UNKNOWN = 0;

    public static final String toString(int o) {
        if (o == 0) {
            return IccCardConstants.INTENT_VALUE_ICC_UNKNOWN;
        }
        if (o == 1) {
            return "INTERNATIONAL_OR_DATA_IP";
        }
        if (o == 2) {
            return "NATIONAL_OR_INTERNET_MAIL";
        }
        if (o == 3) {
            return IccCardConstants.INTENT_VALUE_LOCKED_NETWORK;
        }
        if (o == 4) {
            return "SUBSCRIBER";
        }
        if (o == 5) {
            return "ALPHANUMERIC";
        }
        if (o == 6) {
            return "ABBREVIATED";
        }
        if (o == 7) {
            return "RESERVED_7";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add(IccCardConstants.INTENT_VALUE_ICC_UNKNOWN);
        if ((o & 1) == 1) {
            list.add("INTERNATIONAL_OR_DATA_IP");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("NATIONAL_OR_INTERNET_MAIL");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add(IccCardConstants.INTENT_VALUE_LOCKED_NETWORK);
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("SUBSCRIBER");
            flipped |= 4;
        }
        if ((o & 5) == 5) {
            list.add("ALPHANUMERIC");
            flipped |= 5;
        }
        if ((o & 6) == 6) {
            list.add("ABBREVIATED");
            flipped |= 6;
        }
        if ((o & 7) == 7) {
            list.add("RESERVED_7");
            flipped |= 7;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}

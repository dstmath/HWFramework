package android.hardware.radio.V1_0;

import java.util.ArrayList;

public final class SmsWriteArgsStatus {
    public static final int REC_READ = 1;
    public static final int REC_UNREAD = 0;
    public static final int STO_SENT = 3;
    public static final int STO_UNSENT = 2;

    public static final String toString(int o) {
        if (o == 0) {
            return "REC_UNREAD";
        }
        if (o == 1) {
            return "REC_READ";
        }
        if (o == 2) {
            return "STO_UNSENT";
        }
        if (o == 3) {
            return "STO_SENT";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("REC_UNREAD");
        if ((o & 1) == 1) {
            list.add("REC_READ");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("STO_UNSENT");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("STO_SENT");
            flipped |= 3;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}

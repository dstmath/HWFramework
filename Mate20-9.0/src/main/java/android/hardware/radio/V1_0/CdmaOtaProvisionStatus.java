package android.hardware.radio.V1_0;

import java.util.ArrayList;

public final class CdmaOtaProvisionStatus {
    public static final int A_KEY_EXCHANGED = 2;
    public static final int COMMITTED = 8;
    public static final int IMSI_DOWNLOADED = 6;
    public static final int MDN_DOWNLOADED = 5;
    public static final int NAM_DOWNLOADED = 4;
    public static final int OTAPA_ABORTED = 11;
    public static final int OTAPA_STARTED = 9;
    public static final int OTAPA_STOPPED = 10;
    public static final int PRL_DOWNLOADED = 7;
    public static final int SPC_RETRIES_EXCEEDED = 1;
    public static final int SPL_UNLOCKED = 0;
    public static final int SSD_UPDATED = 3;

    public static final String toString(int o) {
        if (o == 0) {
            return "SPL_UNLOCKED";
        }
        if (o == 1) {
            return "SPC_RETRIES_EXCEEDED";
        }
        if (o == 2) {
            return "A_KEY_EXCHANGED";
        }
        if (o == 3) {
            return "SSD_UPDATED";
        }
        if (o == 4) {
            return "NAM_DOWNLOADED";
        }
        if (o == 5) {
            return "MDN_DOWNLOADED";
        }
        if (o == 6) {
            return "IMSI_DOWNLOADED";
        }
        if (o == 7) {
            return "PRL_DOWNLOADED";
        }
        if (o == 8) {
            return "COMMITTED";
        }
        if (o == 9) {
            return "OTAPA_STARTED";
        }
        if (o == 10) {
            return "OTAPA_STOPPED";
        }
        if (o == 11) {
            return "OTAPA_ABORTED";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("SPL_UNLOCKED");
        if ((o & 1) == 1) {
            list.add("SPC_RETRIES_EXCEEDED");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("A_KEY_EXCHANGED");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("SSD_UPDATED");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("NAM_DOWNLOADED");
            flipped |= 4;
        }
        if ((o & 5) == 5) {
            list.add("MDN_DOWNLOADED");
            flipped |= 5;
        }
        if ((o & 6) == 6) {
            list.add("IMSI_DOWNLOADED");
            flipped |= 6;
        }
        if ((o & 7) == 7) {
            list.add("PRL_DOWNLOADED");
            flipped |= 7;
        }
        if ((o & 8) == 8) {
            list.add("COMMITTED");
            flipped |= 8;
        }
        if ((o & 9) == 9) {
            list.add("OTAPA_STARTED");
            flipped |= 9;
        }
        if ((o & 10) == 10) {
            list.add("OTAPA_STOPPED");
            flipped |= 10;
        }
        if ((o & 11) == 11) {
            list.add("OTAPA_ABORTED");
            flipped |= 11;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}

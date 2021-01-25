package android.hardware.broadcastradio.V2_0;

import java.util.ArrayList;

public final class IdentifierType {
    public static final int AMFM_FREQUENCY = 1;
    public static final int DAB_ENSEMBLE = 6;
    public static final int DAB_FREQUENCY = 8;
    public static final int DAB_SCID = 7;
    public static final int DAB_SID_EXT = 5;
    public static final int DRMO_FREQUENCY = 10;
    public static final int DRMO_SERVICE_ID = 9;
    public static final int HD_STATION_ID_EXT = 3;
    public static final int HD_STATION_NAME = 4;
    public static final int INVALID = 0;
    public static final int RDS_PI = 2;
    public static final int SXM_CHANNEL = 13;
    public static final int SXM_SERVICE_ID = 12;
    public static final int VENDOR_END = 1999;
    public static final int VENDOR_START = 1000;

    public static final String toString(int o) {
        if (o == 1000) {
            return "VENDOR_START";
        }
        if (o == 1999) {
            return "VENDOR_END";
        }
        if (o == 0) {
            return "INVALID";
        }
        if (o == 1) {
            return "AMFM_FREQUENCY";
        }
        if (o == 2) {
            return "RDS_PI";
        }
        if (o == 3) {
            return "HD_STATION_ID_EXT";
        }
        if (o == 4) {
            return "HD_STATION_NAME";
        }
        if (o == 5) {
            return "DAB_SID_EXT";
        }
        if (o == 6) {
            return "DAB_ENSEMBLE";
        }
        if (o == 7) {
            return "DAB_SCID";
        }
        if (o == 8) {
            return "DAB_FREQUENCY";
        }
        if (o == 9) {
            return "DRMO_SERVICE_ID";
        }
        if (o == 10) {
            return "DRMO_FREQUENCY";
        }
        if (o == 12) {
            return "SXM_SERVICE_ID";
        }
        if (o == 13) {
            return "SXM_CHANNEL";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        if ((o & 1000) == 1000) {
            list.add("VENDOR_START");
            flipped = 0 | 1000;
        }
        if ((o & VENDOR_END) == 1999) {
            list.add("VENDOR_END");
            flipped |= VENDOR_END;
        }
        list.add("INVALID");
        if ((o & 1) == 1) {
            list.add("AMFM_FREQUENCY");
            flipped |= 1;
        }
        if ((o & 2) == 2) {
            list.add("RDS_PI");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("HD_STATION_ID_EXT");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("HD_STATION_NAME");
            flipped |= 4;
        }
        if ((o & 5) == 5) {
            list.add("DAB_SID_EXT");
            flipped |= 5;
        }
        if ((o & 6) == 6) {
            list.add("DAB_ENSEMBLE");
            flipped |= 6;
        }
        if ((o & 7) == 7) {
            list.add("DAB_SCID");
            flipped |= 7;
        }
        if ((o & 8) == 8) {
            list.add("DAB_FREQUENCY");
            flipped |= 8;
        }
        if ((o & 9) == 9) {
            list.add("DRMO_SERVICE_ID");
            flipped |= 9;
        }
        if ((o & 10) == 10) {
            list.add("DRMO_FREQUENCY");
            flipped |= 10;
        }
        if ((o & 12) == 12) {
            list.add("SXM_SERVICE_ID");
            flipped |= 12;
        }
        if ((o & 13) == 13) {
            list.add("SXM_CHANNEL");
            flipped |= 13;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}

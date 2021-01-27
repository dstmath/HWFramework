package android.hardware.broadcastradio.V2_0;

import java.util.ArrayList;

public final class MetadataKey {
    public static final int ALBUM_ART = 9;
    public static final int DAB_COMPONENT_NAME = 15;
    public static final int DAB_COMPONENT_NAME_SHORT = 16;
    public static final int DAB_ENSEMBLE_NAME = 11;
    public static final int DAB_ENSEMBLE_NAME_SHORT = 12;
    public static final int DAB_SERVICE_NAME = 13;
    public static final int DAB_SERVICE_NAME_SHORT = 14;
    public static final int PROGRAM_NAME = 10;
    public static final int RBDS_PTY = 3;
    public static final int RDS_PS = 1;
    public static final int RDS_PTY = 2;
    public static final int RDS_RT = 4;
    public static final int SONG_ALBUM = 7;
    public static final int SONG_ARTIST = 6;
    public static final int SONG_TITLE = 5;
    public static final int STATION_ICON = 8;

    public static final String toString(int o) {
        if (o == 1) {
            return "RDS_PS";
        }
        if (o == 2) {
            return "RDS_PTY";
        }
        if (o == 3) {
            return "RBDS_PTY";
        }
        if (o == 4) {
            return "RDS_RT";
        }
        if (o == 5) {
            return "SONG_TITLE";
        }
        if (o == 6) {
            return "SONG_ARTIST";
        }
        if (o == 7) {
            return "SONG_ALBUM";
        }
        if (o == 8) {
            return "STATION_ICON";
        }
        if (o == 9) {
            return "ALBUM_ART";
        }
        if (o == 10) {
            return "PROGRAM_NAME";
        }
        if (o == 11) {
            return "DAB_ENSEMBLE_NAME";
        }
        if (o == 12) {
            return "DAB_ENSEMBLE_NAME_SHORT";
        }
        if (o == 13) {
            return "DAB_SERVICE_NAME";
        }
        if (o == 14) {
            return "DAB_SERVICE_NAME_SHORT";
        }
        if (o == 15) {
            return "DAB_COMPONENT_NAME";
        }
        if (o == 16) {
            return "DAB_COMPONENT_NAME_SHORT";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        if ((o & 1) == 1) {
            list.add("RDS_PS");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("RDS_PTY");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("RBDS_PTY");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("RDS_RT");
            flipped |= 4;
        }
        if ((o & 5) == 5) {
            list.add("SONG_TITLE");
            flipped |= 5;
        }
        if ((o & 6) == 6) {
            list.add("SONG_ARTIST");
            flipped |= 6;
        }
        if ((o & 7) == 7) {
            list.add("SONG_ALBUM");
            flipped |= 7;
        }
        if ((o & 8) == 8) {
            list.add("STATION_ICON");
            flipped |= 8;
        }
        if ((o & 9) == 9) {
            list.add("ALBUM_ART");
            flipped |= 9;
        }
        if ((o & 10) == 10) {
            list.add("PROGRAM_NAME");
            flipped |= 10;
        }
        if ((o & 11) == 11) {
            list.add("DAB_ENSEMBLE_NAME");
            flipped |= 11;
        }
        if ((o & 12) == 12) {
            list.add("DAB_ENSEMBLE_NAME_SHORT");
            flipped |= 12;
        }
        if ((o & 13) == 13) {
            list.add("DAB_SERVICE_NAME");
            flipped |= 13;
        }
        if ((o & 14) == 14) {
            list.add("DAB_SERVICE_NAME_SHORT");
            flipped |= 14;
        }
        if ((o & 15) == 15) {
            list.add("DAB_COMPONENT_NAME");
            flipped |= 15;
        }
        if ((o & 16) == 16) {
            list.add("DAB_COMPONENT_NAME_SHORT");
            flipped |= 16;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}

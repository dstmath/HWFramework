package android.hardware.radio.V1_0;

import java.util.ArrayList;

public final class CdmaInfoRecName {
    public static final int CALLED_PARTY_NUMBER = 1;
    public static final int CALLING_PARTY_NUMBER = 2;
    public static final int CONNECTED_NUMBER = 3;
    public static final int DISPLAY = 0;
    public static final int EXTENDED_DISPLAY = 7;
    public static final int LINE_CONTROL = 6;
    public static final int REDIRECTING_NUMBER = 5;
    public static final int SIGNAL = 4;
    public static final int T53_AUDIO_CONTROL = 10;
    public static final int T53_CLIR = 8;
    public static final int T53_RELEASE = 9;

    public static final String toString(int o) {
        if (o == 0) {
            return "DISPLAY";
        }
        if (o == 1) {
            return "CALLED_PARTY_NUMBER";
        }
        if (o == 2) {
            return "CALLING_PARTY_NUMBER";
        }
        if (o == 3) {
            return "CONNECTED_NUMBER";
        }
        if (o == 4) {
            return "SIGNAL";
        }
        if (o == 5) {
            return "REDIRECTING_NUMBER";
        }
        if (o == 6) {
            return "LINE_CONTROL";
        }
        if (o == 7) {
            return "EXTENDED_DISPLAY";
        }
        if (o == 8) {
            return "T53_CLIR";
        }
        if (o == 9) {
            return "T53_RELEASE";
        }
        if (o == 10) {
            return "T53_AUDIO_CONTROL";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("DISPLAY");
        if ((o & 1) == 1) {
            list.add("CALLED_PARTY_NUMBER");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("CALLING_PARTY_NUMBER");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("CONNECTED_NUMBER");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("SIGNAL");
            flipped |= 4;
        }
        if ((o & 5) == 5) {
            list.add("REDIRECTING_NUMBER");
            flipped |= 5;
        }
        if ((o & 6) == 6) {
            list.add("LINE_CONTROL");
            flipped |= 6;
        }
        if ((o & 7) == 7) {
            list.add("EXTENDED_DISPLAY");
            flipped |= 7;
        }
        if ((o & 8) == 8) {
            list.add("T53_CLIR");
            flipped |= 8;
        }
        if ((o & 9) == 9) {
            list.add("T53_RELEASE");
            flipped |= 9;
        }
        if ((o & 10) == 10) {
            list.add("T53_AUDIO_CONTROL");
            flipped |= 10;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}

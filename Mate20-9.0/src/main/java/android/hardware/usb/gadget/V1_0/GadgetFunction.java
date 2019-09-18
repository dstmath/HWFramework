package android.hardware.usb.gadget.V1_0;

import java.util.ArrayList;

public final class GadgetFunction {
    public static final long ACCESSORY = 2;
    public static final long ADB = 1;
    public static final long AUDIO_SOURCE = 64;
    public static final long MIDI = 8;
    public static final long MTP = 4;
    public static final long NONE = 0;
    public static final long PTP = 16;
    public static final long RNDIS = 32;

    public static final String toString(long o) {
        if (o == 0) {
            return "NONE";
        }
        if (o == 1) {
            return "ADB";
        }
        if (o == 2) {
            return "ACCESSORY";
        }
        if (o == 4) {
            return "MTP";
        }
        if (o == 8) {
            return "MIDI";
        }
        if (o == 16) {
            return "PTP";
        }
        if (o == 32) {
            return "RNDIS";
        }
        if (o == 64) {
            return "AUDIO_SOURCE";
        }
        return "0x" + Long.toHexString(o);
    }

    public static final String dumpBitfield(long o) {
        ArrayList<String> list = new ArrayList<>();
        long flipped = 0;
        list.add("NONE");
        if ((o & 1) == 1) {
            list.add("ADB");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("ACCESSORY");
            flipped |= 2;
        }
        if ((o & 4) == 4) {
            list.add("MTP");
            flipped |= 4;
        }
        if ((o & 8) == 8) {
            list.add("MIDI");
            flipped |= 8;
        }
        if ((o & 16) == 16) {
            list.add("PTP");
            flipped |= 16;
        }
        if ((o & 32) == 32) {
            list.add("RNDIS");
            flipped |= 32;
        }
        if ((o & 64) == 64) {
            list.add("AUDIO_SOURCE");
            flipped |= 64;
        }
        if (o != flipped) {
            list.add("0x" + Long.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}

package android.hardware.wifi.supplicant.V1_0;

import java.util.ArrayList;

public final class WpsConfigMethods {
    public static final short DISPLAY = (short) 8;
    public static final short ETHERNET = (short) 2;
    public static final short EXT_NFC_TOKEN = (short) 16;
    public static final short INT_NFC_TOKEN = (short) 32;
    public static final short KEYPAD = (short) 256;
    public static final short LABEL = (short) 4;
    public static final short NFC_INTERFACE = (short) 64;
    public static final short P2PS = (short) 4096;
    public static final short PHY_DISPLAY = (short) 16392;
    public static final short PHY_PUSHBUTTON = (short) 1152;
    public static final short PUSHBUTTON = (short) 128;
    public static final short USBA = (short) 1;
    public static final short VIRT_DISPLAY = (short) 8200;
    public static final short VIRT_PUSHBUTTON = (short) 640;

    public static final String toString(short o) {
        if (o == (short) 1) {
            return "USBA";
        }
        if (o == (short) 2) {
            return "ETHERNET";
        }
        if (o == (short) 4) {
            return "LABEL";
        }
        if (o == (short) 8) {
            return "DISPLAY";
        }
        if (o == (short) 16) {
            return "EXT_NFC_TOKEN";
        }
        if (o == (short) 32) {
            return "INT_NFC_TOKEN";
        }
        if (o == (short) 64) {
            return "NFC_INTERFACE";
        }
        if (o == PUSHBUTTON) {
            return "PUSHBUTTON";
        }
        if (o == KEYPAD) {
            return "KEYPAD";
        }
        if (o == VIRT_PUSHBUTTON) {
            return "VIRT_PUSHBUTTON";
        }
        if (o == PHY_PUSHBUTTON) {
            return "PHY_PUSHBUTTON";
        }
        if (o == P2PS) {
            return "P2PS";
        }
        if (o == VIRT_DISPLAY) {
            return "VIRT_DISPLAY";
        }
        if (o == PHY_DISPLAY) {
            return "PHY_DISPLAY";
        }
        return "0x" + Integer.toHexString(Short.toUnsignedInt(o));
    }

    public static final String dumpBitfield(short o) {
        ArrayList<String> list = new ArrayList();
        short flipped = (short) 0;
        if ((o & 1) == 1) {
            list.add("USBA");
            flipped = (short) 1;
        }
        if ((o & 2) == 2) {
            list.add("ETHERNET");
            flipped = (short) (flipped | 2);
        }
        if ((o & 4) == 4) {
            list.add("LABEL");
            flipped = (short) (flipped | 4);
        }
        if ((o & 8) == 8) {
            list.add("DISPLAY");
            flipped = (short) (flipped | 8);
        }
        if ((o & 16) == 16) {
            list.add("EXT_NFC_TOKEN");
            flipped = (short) (flipped | 16);
        }
        if ((o & 32) == 32) {
            list.add("INT_NFC_TOKEN");
            flipped = (short) (flipped | 32);
        }
        if ((o & 64) == 64) {
            list.add("NFC_INTERFACE");
            flipped = (short) (flipped | 64);
        }
        if ((o & 128) == 128) {
            list.add("PUSHBUTTON");
            flipped = (short) (flipped | 128);
        }
        if ((o & 256) == 256) {
            list.add("KEYPAD");
            flipped = (short) (flipped | 256);
        }
        if ((o & 640) == 640) {
            list.add("VIRT_PUSHBUTTON");
            flipped = (short) (flipped | 640);
        }
        if ((o & 1152) == 1152) {
            list.add("PHY_PUSHBUTTON");
            flipped = (short) (flipped | 1152);
        }
        if ((o & 4096) == 4096) {
            list.add("P2PS");
            flipped = (short) (flipped | 4096);
        }
        if ((o & 8200) == 8200) {
            list.add("VIRT_DISPLAY");
            flipped = (short) (flipped | 8200);
        }
        if ((o & 16392) == 16392) {
            list.add("PHY_DISPLAY");
            flipped = (short) (flipped | 16392);
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString(Short.toUnsignedInt((short) ((~flipped) & o))));
        }
        return String.join(" | ", list);
    }
}

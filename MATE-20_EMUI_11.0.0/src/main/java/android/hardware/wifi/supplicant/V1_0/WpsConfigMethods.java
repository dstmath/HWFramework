package android.hardware.wifi.supplicant.V1_0;

import java.util.ArrayList;

public final class WpsConfigMethods {
    public static final short DISPLAY = 8;
    public static final short ETHERNET = 2;
    public static final short EXT_NFC_TOKEN = 16;
    public static final short INT_NFC_TOKEN = 32;
    public static final short KEYPAD = 256;
    public static final short LABEL = 4;
    public static final short NFC_INTERFACE = 64;
    public static final short P2PS = 4096;
    public static final short PHY_DISPLAY = 16392;
    public static final short PHY_PUSHBUTTON = 1152;
    public static final short PUSHBUTTON = 128;
    public static final short USBA = 1;
    public static final short VIRT_DISPLAY = 8200;
    public static final short VIRT_PUSHBUTTON = 640;

    public static final String toString(short o) {
        if (o == 1) {
            return "USBA";
        }
        if (o == 2) {
            return "ETHERNET";
        }
        if (o == 4) {
            return "LABEL";
        }
        if (o == 8) {
            return "DISPLAY";
        }
        if (o == 16) {
            return "EXT_NFC_TOKEN";
        }
        if (o == 32) {
            return "INT_NFC_TOKEN";
        }
        if (o == 64) {
            return "NFC_INTERFACE";
        }
        if (o == 128) {
            return "PUSHBUTTON";
        }
        if (o == 256) {
            return "KEYPAD";
        }
        if (o == 640) {
            return "VIRT_PUSHBUTTON";
        }
        if (o == 1152) {
            return "PHY_PUSHBUTTON";
        }
        if (o == 4096) {
            return "P2PS";
        }
        if (o == 8200) {
            return "VIRT_DISPLAY";
        }
        if (o == 16392) {
            return "PHY_DISPLAY";
        }
        return "0x" + Integer.toHexString(Short.toUnsignedInt(o));
    }

    public static final String dumpBitfield(short o) {
        ArrayList<String> list = new ArrayList<>();
        short flipped = 0;
        if ((o & 1) == 1) {
            list.add("USBA");
            flipped = (short) (0 | 1);
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
        if ((o & PUSHBUTTON) == 128) {
            list.add("PUSHBUTTON");
            flipped = (short) (flipped | PUSHBUTTON);
        }
        if ((o & KEYPAD) == 256) {
            list.add("KEYPAD");
            flipped = (short) (flipped | KEYPAD);
        }
        if ((o & VIRT_PUSHBUTTON) == 640) {
            list.add("VIRT_PUSHBUTTON");
            flipped = (short) (flipped | VIRT_PUSHBUTTON);
        }
        if ((o & PHY_PUSHBUTTON) == 1152) {
            list.add("PHY_PUSHBUTTON");
            flipped = (short) (flipped | PHY_PUSHBUTTON);
        }
        if ((o & P2PS) == 4096) {
            list.add("P2PS");
            flipped = (short) (flipped | P2PS);
        }
        if ((o & VIRT_DISPLAY) == 8200) {
            list.add("VIRT_DISPLAY");
            flipped = (short) (flipped | VIRT_DISPLAY);
        }
        if ((o & PHY_DISPLAY) == 16392) {
            list.add("PHY_DISPLAY");
            flipped = (short) (flipped | PHY_DISPLAY);
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString(Short.toUnsignedInt((short) ((~flipped) & o))));
        }
        return String.join(" | ", list);
    }
}

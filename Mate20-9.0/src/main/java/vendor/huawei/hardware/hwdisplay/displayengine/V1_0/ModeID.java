package vendor.huawei.hardware.hwdisplay.displayengine.V1_0;

import java.util.ArrayList;

public final class ModeID {
    public static final int MODE_3D_COLORTMP = 17;
    public static final int MODE_AMBIENT_LUX_CHANGE = 28;
    public static final int MODE_AMBIENT_LUX_OFFSET = 29;
    public static final int MODE_BROWSER = 20;
    public static final int MODE_CAMERA = 10;
    public static final int MODE_CAMERA_PRO = 11;
    public static final int MODE_DEFAULT = 1;
    public static final int MODE_DISABLE = 0;
    public static final int MODE_EBOOK = 13;
    public static final int MODE_GAME = 16;
    public static final int MODE_HDR10 = 6;
    public static final int MODE_HDR10_DISABLE = 7;
    public static final int MODE_IM = 18;
    public static final int MODE_IMAGE = 8;
    public static final int MODE_IMAGE_DISABLE = 9;
    public static final int MODE_IM_DISABLE = 19;
    public static final int MODE_INVALID = -1;
    public static final int MODE_MARKET = 27;
    public static final int MODE_MAX = 35;
    public static final int MODE_MONEY = 25;
    public static final int MODE_NAVIGATION = 24;
    public static final int MODE_NEWS_CLIENT = 21;
    public static final int MODE_RGLED = 32;
    public static final int MODE_SCALING_DOWN = 15;
    public static final int MODE_SCALING_UP = 14;
    public static final int MODE_SCREEN_OFF = 34;
    public static final int MODE_SCREEN_ON = 33;
    public static final int MODE_SHOP = 22;
    public static final int MODE_SMS = 23;
    public static final int MODE_SPORTS = 26;
    public static final int MODE_UI = 3;
    public static final int MODE_USER = 2;
    public static final int MODE_VIDEO = 4;
    public static final int MODE_VIDEO_DISABLE = 5;
    public static final int MODE_WEB = 12;
    public static final int MODE_XCC_CHANGE = 30;
    public static final int MODE_XNIT_CHANGE_INTEGRATION = 31;

    public static final String toString(int o) {
        if (o == -1) {
            return "MODE_INVALID";
        }
        if (o == 0) {
            return "MODE_DISABLE";
        }
        if (o == 1) {
            return "MODE_DEFAULT";
        }
        if (o == 2) {
            return "MODE_USER";
        }
        if (o == 3) {
            return "MODE_UI";
        }
        if (o == 4) {
            return "MODE_VIDEO";
        }
        if (o == 5) {
            return "MODE_VIDEO_DISABLE";
        }
        if (o == 6) {
            return "MODE_HDR10";
        }
        if (o == 7) {
            return "MODE_HDR10_DISABLE";
        }
        if (o == 8) {
            return "MODE_IMAGE";
        }
        if (o == 9) {
            return "MODE_IMAGE_DISABLE";
        }
        if (o == 10) {
            return "MODE_CAMERA";
        }
        if (o == 11) {
            return "MODE_CAMERA_PRO";
        }
        if (o == 12) {
            return "MODE_WEB";
        }
        if (o == 13) {
            return "MODE_EBOOK";
        }
        if (o == 14) {
            return "MODE_SCALING_UP";
        }
        if (o == 15) {
            return "MODE_SCALING_DOWN";
        }
        if (o == 16) {
            return "MODE_GAME";
        }
        if (o == 17) {
            return "MODE_3D_COLORTMP";
        }
        if (o == 18) {
            return "MODE_IM";
        }
        if (o == 19) {
            return "MODE_IM_DISABLE";
        }
        if (o == 20) {
            return "MODE_BROWSER";
        }
        if (o == 21) {
            return "MODE_NEWS_CLIENT";
        }
        if (o == 22) {
            return "MODE_SHOP";
        }
        if (o == 23) {
            return "MODE_SMS";
        }
        if (o == 24) {
            return "MODE_NAVIGATION";
        }
        if (o == 25) {
            return "MODE_MONEY";
        }
        if (o == 26) {
            return "MODE_SPORTS";
        }
        if (o == 27) {
            return "MODE_MARKET";
        }
        if (o == 28) {
            return "MODE_AMBIENT_LUX_CHANGE";
        }
        if (o == 29) {
            return "MODE_AMBIENT_LUX_OFFSET";
        }
        if (o == 30) {
            return "MODE_XCC_CHANGE";
        }
        if (o == 31) {
            return "MODE_XNIT_CHANGE_INTEGRATION";
        }
        if (o == 32) {
            return "MODE_RGLED";
        }
        if (o == 33) {
            return "MODE_SCREEN_ON";
        }
        if (o == 34) {
            return "MODE_SCREEN_OFF";
        }
        if (o == 35) {
            return "MODE_MAX";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        if ((o & -1) == -1) {
            list.add("MODE_INVALID");
            flipped = 0 | -1;
        }
        list.add("MODE_DISABLE");
        if ((o & 1) == 1) {
            list.add("MODE_DEFAULT");
            flipped |= 1;
        }
        if ((o & 2) == 2) {
            list.add("MODE_USER");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("MODE_UI");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("MODE_VIDEO");
            flipped |= 4;
        }
        if ((o & 5) == 5) {
            list.add("MODE_VIDEO_DISABLE");
            flipped |= 5;
        }
        if ((o & 6) == 6) {
            list.add("MODE_HDR10");
            flipped |= 6;
        }
        if ((o & 7) == 7) {
            list.add("MODE_HDR10_DISABLE");
            flipped |= 7;
        }
        if ((o & 8) == 8) {
            list.add("MODE_IMAGE");
            flipped |= 8;
        }
        if ((o & 9) == 9) {
            list.add("MODE_IMAGE_DISABLE");
            flipped |= 9;
        }
        if ((o & 10) == 10) {
            list.add("MODE_CAMERA");
            flipped |= 10;
        }
        if ((o & 11) == 11) {
            list.add("MODE_CAMERA_PRO");
            flipped |= 11;
        }
        if ((o & 12) == 12) {
            list.add("MODE_WEB");
            flipped |= 12;
        }
        if ((o & 13) == 13) {
            list.add("MODE_EBOOK");
            flipped |= 13;
        }
        if ((o & 14) == 14) {
            list.add("MODE_SCALING_UP");
            flipped |= 14;
        }
        if ((o & 15) == 15) {
            list.add("MODE_SCALING_DOWN");
            flipped |= 15;
        }
        if ((o & 16) == 16) {
            list.add("MODE_GAME");
            flipped |= 16;
        }
        if ((o & 17) == 17) {
            list.add("MODE_3D_COLORTMP");
            flipped |= 17;
        }
        if ((o & 18) == 18) {
            list.add("MODE_IM");
            flipped |= 18;
        }
        if ((o & 19) == 19) {
            list.add("MODE_IM_DISABLE");
            flipped |= 19;
        }
        if ((o & 20) == 20) {
            list.add("MODE_BROWSER");
            flipped |= 20;
        }
        if ((o & 21) == 21) {
            list.add("MODE_NEWS_CLIENT");
            flipped |= 21;
        }
        if ((o & 22) == 22) {
            list.add("MODE_SHOP");
            flipped |= 22;
        }
        if ((o & 23) == 23) {
            list.add("MODE_SMS");
            flipped |= 23;
        }
        if ((o & 24) == 24) {
            list.add("MODE_NAVIGATION");
            flipped |= 24;
        }
        if ((o & 25) == 25) {
            list.add("MODE_MONEY");
            flipped |= 25;
        }
        if ((o & 26) == 26) {
            list.add("MODE_SPORTS");
            flipped |= 26;
        }
        if ((o & 27) == 27) {
            list.add("MODE_MARKET");
            flipped |= 27;
        }
        if ((o & 28) == 28) {
            list.add("MODE_AMBIENT_LUX_CHANGE");
            flipped |= 28;
        }
        if ((o & 29) == 29) {
            list.add("MODE_AMBIENT_LUX_OFFSET");
            flipped |= 29;
        }
        if ((o & 30) == 30) {
            list.add("MODE_XCC_CHANGE");
            flipped |= 30;
        }
        if ((o & 31) == 31) {
            list.add("MODE_XNIT_CHANGE_INTEGRATION");
            flipped |= 31;
        }
        if ((o & 32) == 32) {
            list.add("MODE_RGLED");
            flipped |= 32;
        }
        if ((o & 33) == 33) {
            list.add("MODE_SCREEN_ON");
            flipped |= 33;
        }
        if ((o & 34) == 34) {
            list.add("MODE_SCREEN_OFF");
            flipped |= 34;
        }
        if ((o & 35) == 35) {
            list.add("MODE_MAX");
            flipped |= 35;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}

package android.hdm;

public class HwDeviceManagerEx {
    public static final int DISABLE_CHANGE_WALLPAPER = 35;
    public static final int IS_ALLOWED_INSTALL_PACKAGE = 7;

    public static boolean disallowOp(int type) {
        return HwDeviceManager.disallowOp(type);
    }

    public static boolean disallowOp(int type, String param) {
        return HwDeviceManager.disallowOp(type, param);
    }
}

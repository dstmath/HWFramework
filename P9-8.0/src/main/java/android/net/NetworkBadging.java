package android.net;

import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.graphics.drawable.Drawable;

@Deprecated
public class NetworkBadging {
    public static final int BADGING_4K = 30;
    public static final int BADGING_HD = 20;
    public static final int BADGING_NONE = 0;
    public static final int BADGING_SD = 10;

    private NetworkBadging() {
    }

    public static Drawable getWifiIcon(int signalLevel, int badging, Theme theme) {
        return Resources.getSystem().getDrawable(getWifiSignalResource(signalLevel), theme);
    }

    private static int getWifiSignalResource(int signalLevel) {
        switch (signalLevel) {
            case 0:
                return 17302647;
            case 1:
                return 17302648;
            case 2:
                return 17302649;
            case 3:
                return 17302650;
            case 4:
                return 17302651;
            default:
                throw new IllegalArgumentException("Invalid signal level: " + signalLevel);
        }
    }

    private static int getBadgedWifiSignalResource(int signalLevel) {
        switch (signalLevel) {
            case 0:
                return 17302610;
            case 1:
                return 17302611;
            case 2:
                return 17302612;
            case 3:
                return 17302613;
            case 4:
                return 17302614;
            default:
                throw new IllegalArgumentException("Invalid signal level: " + signalLevel);
        }
    }
}

package android.net;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import com.android.internal.R;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Deprecated
public class NetworkBadging {
    public static final int BADGING_4K = 30;
    public static final int BADGING_HD = 20;
    public static final int BADGING_NONE = 0;
    public static final int BADGING_SD = 10;

    @Retention(RetentionPolicy.SOURCE)
    public @interface Badging {
    }

    private NetworkBadging() {
    }

    public static Drawable getWifiIcon(int signalLevel, int badging, Resources.Theme theme) {
        return Resources.getSystem().getDrawable(getWifiSignalResource(signalLevel), theme);
    }

    private static int getWifiSignalResource(int signalLevel) {
        switch (signalLevel) {
            case 0:
                return R.drawable.ic_wifi_signal_0;
            case 1:
                return R.drawable.ic_wifi_signal_1;
            case 2:
                return R.drawable.ic_wifi_signal_2;
            case 3:
                return R.drawable.ic_wifi_signal_3;
            case 4:
                return R.drawable.ic_wifi_signal_4;
            default:
                throw new IllegalArgumentException("Invalid signal level: " + signalLevel);
        }
    }

    private static int getBadgedWifiSignalResource(int signalLevel) {
        switch (signalLevel) {
            case 0:
                return R.drawable.ic_signal_wifi_badged_0_bars;
            case 1:
                return R.drawable.ic_signal_wifi_badged_1_bar;
            case 2:
                return R.drawable.ic_signal_wifi_badged_2_bars;
            case 3:
                return R.drawable.ic_signal_wifi_badged_3_bars;
            case 4:
                return R.drawable.ic_signal_wifi_badged_4_bars;
            default:
                throw new IllegalArgumentException("Invalid signal level: " + signalLevel);
        }
    }
}

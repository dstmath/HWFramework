package huawei.android.widget;

import android.content.Context;
import android.os.SystemProperties;

public class RadiusSizeUtils {
    private static final int DEFAULT_SIZE = 0;
    private static final int RADIUS_SIZE;
    private static final String SYSTEM_RADIUS_KEY = "ro.config.fillet_radius_size";

    static {
        if (SystemProperties.getInt(SYSTEM_RADIUS_KEY, 0) > 0) {
            RADIUS_SIZE = 0;
        } else {
            RADIUS_SIZE = 0;
        }
    }

    private RadiusSizeUtils() {
    }

    public static int getRadiusSize(Context context) {
        if (RADIUS_SIZE <= 0) {
            return 0;
        }
        return (RADIUS_SIZE * context.getResources().getDisplayMetrics().densityDpi) / 160;
    }
}

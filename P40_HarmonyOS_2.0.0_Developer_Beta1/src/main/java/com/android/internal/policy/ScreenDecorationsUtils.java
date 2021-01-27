package com.android.internal.policy;

import android.content.res.Resources;
import com.android.internal.R;

public class ScreenDecorationsUtils {
    public static float getWindowCornerRadius(Resources resources) {
        if (!supportsRoundedCornersOnWindows(resources)) {
            return 0.0f;
        }
        float defaultRadius = resources.getDimension(R.dimen.rounded_corner_radius);
        float topRadius = resources.getDimension(R.dimen.rounded_corner_radius_top);
        if (topRadius == 0.0f) {
            topRadius = defaultRadius;
        }
        float bottomRadius = resources.getDimension(R.dimen.rounded_corner_radius_bottom);
        if (bottomRadius == 0.0f) {
            bottomRadius = defaultRadius;
        }
        return Math.min(topRadius, bottomRadius);
    }

    public static boolean supportsRoundedCornersOnWindows(Resources resources) {
        return resources.getBoolean(R.bool.config_supportsRoundedCornersOnWindows);
    }
}

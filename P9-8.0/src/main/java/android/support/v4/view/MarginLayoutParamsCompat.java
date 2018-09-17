package android.support.v4.view;

import android.os.Build.VERSION;
import android.view.ViewGroup.MarginLayoutParams;

public final class MarginLayoutParamsCompat {
    public static int getMarginStart(MarginLayoutParams lp) {
        if (VERSION.SDK_INT >= 17) {
            return lp.getMarginStart();
        }
        return lp.leftMargin;
    }

    public static int getMarginEnd(MarginLayoutParams lp) {
        if (VERSION.SDK_INT >= 17) {
            return lp.getMarginEnd();
        }
        return lp.rightMargin;
    }

    public static void setMarginStart(MarginLayoutParams lp, int marginStart) {
        if (VERSION.SDK_INT >= 17) {
            lp.setMarginStart(marginStart);
        } else {
            lp.leftMargin = marginStart;
        }
    }

    public static void setMarginEnd(MarginLayoutParams lp, int marginEnd) {
        if (VERSION.SDK_INT >= 17) {
            lp.setMarginEnd(marginEnd);
        } else {
            lp.rightMargin = marginEnd;
        }
    }

    public static boolean isMarginRelative(MarginLayoutParams lp) {
        if (VERSION.SDK_INT >= 17) {
            return lp.isMarginRelative();
        }
        return false;
    }

    public static int getLayoutDirection(MarginLayoutParams lp) {
        int result;
        if (VERSION.SDK_INT >= 17) {
            result = lp.getLayoutDirection();
        } else {
            result = 0;
        }
        if (result == 0 || result == 1) {
            return result;
        }
        return 0;
    }

    public static void setLayoutDirection(MarginLayoutParams lp, int layoutDirection) {
        if (VERSION.SDK_INT >= 17) {
            lp.setLayoutDirection(layoutDirection);
        }
    }

    public static void resolveLayoutDirection(MarginLayoutParams lp, int layoutDirection) {
        if (VERSION.SDK_INT >= 17) {
            lp.resolveLayoutDirection(layoutDirection);
        }
    }

    private MarginLayoutParamsCompat() {
    }
}

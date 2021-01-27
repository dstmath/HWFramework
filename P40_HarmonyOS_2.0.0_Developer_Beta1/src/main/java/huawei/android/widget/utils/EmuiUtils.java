package huawei.android.widget.utils;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.os.SystemProperties;

public final class EmuiUtils {
    private static final String TAG = EmuiUtils.class.getSimpleName();

    private EmuiUtils() {
    }

    public static boolean isEmuiLite() {
        return SystemProperties.getBoolean("ro.build.hw_emui_lite.enable", false);
    }

    public static boolean isNovaPerformance() {
        return SystemProperties.getBoolean("ro.config.hw_nova_performance", false);
    }

    public static int getAttrColor(Context context, int attrResId, int defValue) {
        return getAttrColorStateList(context, attrResId, defValue).getDefaultColor();
    }

    public static ColorStateList getAttrColorStateList(Context context, int attrResId, int defValue) {
        return getAttrColorStateList(context, attrResId, ColorStateList.valueOf(defValue));
    }

    public static ColorStateList getAttrColorStateList(Context context, int attrResId, ColorStateList defValue) {
        ColorStateList colorStateList = defValue;
        TypedArray typedArray = context.obtainStyledAttributes(new int[]{attrResId});
        if (typedArray.length() > 0) {
            colorStateList = typedArray.getColorStateList(0);
        }
        typedArray.recycle();
        return colorStateList;
    }
}

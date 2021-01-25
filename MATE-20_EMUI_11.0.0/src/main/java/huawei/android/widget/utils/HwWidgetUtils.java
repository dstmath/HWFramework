package huawei.android.widget.utils;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import huawei.android.widget.utils.graphics.drawable.HwAnimatedGradientDrawable;

public class HwWidgetUtils {
    private static final String RESOURCE_TYPE_STYLEABLE = "styleable";
    private static final String TAG = "HwWidgetUtils";

    private HwWidgetUtils() {
    }

    public static HwAnimatedGradientDrawable getHwAnimatedGradientDrawable(Context context, ClickEffectEntry clickEffectEntry) {
        HwAnimatedGradientDrawable drawable = new HwAnimatedGradientDrawable(context);
        drawable.setColor(clickEffectEntry.getColor());
        drawable.setMaxRectAlpha(clickEffectEntry.getAlpha());
        drawable.setMinRectScale(clickEffectEntry.getMinRecScale());
        drawable.setMaxRectScale(clickEffectEntry.getMaxRecScale());
        drawable.setForceDoScaleAnim(clickEffectEntry.isForceDoScaleAnim());
        drawable.setCornerRadius(clickEffectEntry.getCornerRadius());
        return drawable;
    }

    public static HwAnimatedGradientDrawable getHwAnimatedGradientDrawable(Context context, int defStyleAttr) {
        return getHwAnimatedGradientDrawable(context, getCleckEffectEntry(context, defStyleAttr));
    }

    public static ClickEffectEntry getCleckEffectEntry(Context context, int defStyleAttr) {
        ClickEffectEntry clickEffectEntry = new ClickEffectEntry();
        ResLoader resLoader = ResLoader.getInstance();
        int[] identifiers = resLoader.getIdentifierArray(context, RESOURCE_TYPE_STYLEABLE, "Theme");
        Resources.Theme theme = ResLoader.getInstance().getTheme(context);
        if (theme != null) {
            TypedArray typedArray = theme.obtainStyledAttributes(null, identifiers, defStyleAttr, 0);
            clickEffectEntry.setColor(typedArray.getColor(resLoader.getIdentifier(context, RESOURCE_TYPE_STYLEABLE, "Theme_clickEffectColor"), clickEffectEntry.getColor()));
            clickEffectEntry.setAlpha(typedArray.getFloat(resLoader.getIdentifier(context, RESOURCE_TYPE_STYLEABLE, "Theme_clickEffectAlpha"), clickEffectEntry.getAlpha()));
            clickEffectEntry.setMinRecScale(typedArray.getFloat(resLoader.getIdentifier(context, RESOURCE_TYPE_STYLEABLE, "Theme_clickEffectMinRecScale"), clickEffectEntry.getMinRecScale()));
            clickEffectEntry.setMaxRecScale(typedArray.getFloat(resLoader.getIdentifier(context, RESOURCE_TYPE_STYLEABLE, "Theme_clickEffectMaxRecScale"), clickEffectEntry.getMaxRecScale()));
            clickEffectEntry.setCornerRadius(typedArray.getFloat(resLoader.getIdentifier(context, RESOURCE_TYPE_STYLEABLE, "Theme_clickEffectCornerRadius"), clickEffectEntry.getCornerRadius()));
            clickEffectEntry.setIsForceDoScaleAnim(typedArray.getBoolean(resLoader.getIdentifier(context, RESOURCE_TYPE_STYLEABLE, "Theme_clickEffectForceDoScaleAnim"), clickEffectEntry.isForceDoScaleAnim()));
            typedArray.recycle();
        }
        return clickEffectEntry;
    }
}

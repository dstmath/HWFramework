package huawei.android.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import huawei.android.graphics.drawable.HwAnimatedGradientDrawable;
import huawei.android.widget.loader.ResLoader;
import huawei.android.widget.loader.ResLoaderUtil;
import java.lang.reflect.Field;
import java.util.Locale;

public class HwWidgetUtils {
    private static final int ACTIONBAR_BACKGROUND_THEMED_FLAG = 0;
    private static final int HUAWEI_THEME_CALCULATE_INDEX = 3;
    private static final int HUAWEI_THEME_DARK_INDEX = 2;
    private static final int HUAWEI_THEME_EMPHASIZE_INDEX = 0;
    private static final int HUAWEI_THEME_LIGHT_INDEX = 1;
    private static final String TAG = "HwWidgetUtils";

    private HwWidgetUtils() {
    }

    public static final boolean isActionbarBackgroundThemed(Context context) {
        if (context != null && context.getResources().getColor(33882153) == 0) {
            return true;
        }
        return false;
    }

    public static boolean isHwTheme(Context context) {
        TypedValue typedValue = getThemeOfEmui(context);
        if (typedValue != null && typedValue.type == 16) {
            return true;
        }
        return false;
    }

    public static boolean isHwLightTheme(Context context) {
        TypedValue typedValue = getThemeOfEmui(context);
        if (typedValue != null && typedValue.type == 16 && typedValue.data > 0 && typedValue.data % 3 == 1) {
            return true;
        }
        return false;
    }

    public static boolean isHwDarkTheme(Context context) {
        TypedValue typedValue = getThemeOfEmui(context);
        if (typedValue != null && typedValue.type == 16 && typedValue.data > 0 && typedValue.data % 3 == 2) {
            return true;
        }
        return false;
    }

    public static boolean isHwEmphasizeTheme(Context context) {
        TypedValue typedValue = getThemeOfEmui(context);
        if (typedValue != null && typedValue.type == 16 && typedValue.data > 0 && typedValue.data % 3 == 0) {
            return true;
        }
        return false;
    }

    private static TypedValue getThemeOfEmui(Context context) {
        int themeOfEmuiId;
        if (context == null || (themeOfEmuiId = context.getResources().getIdentifier("themeOfEmui", "attr", "androidhwext")) == 0) {
            return null;
        }
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(themeOfEmuiId, typedValue, true);
        return typedValue;
    }

    public static final int[] getResourceDeclareStyleableIntArray(String pkgName, String name) {
        if (TextUtils.isEmpty(pkgName) || TextUtils.isEmpty(name)) {
            return new int[0];
        }
        try {
            return (int[]) Class.forName(pkgName + ".R$styleable").getField(name).get(null);
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchFieldException e) {
            Log.e(TAG, "getResourceDeclareStyleableIntArray, exception");
            return new int[0];
        }
    }

    public static final int getResourceDeclareStyleableInt(String pkgName, String declareStyleable, String indexName) {
        if (TextUtils.isEmpty(pkgName) || TextUtils.isEmpty(declareStyleable) || TextUtils.isEmpty(indexName)) {
            return 0;
        }
        try {
            Class<?> clazz = Class.forName(pkgName + ".R$styleable");
            Field fieldStyle = clazz.getField(declareStyleable);
            Field fieldIndex = clazz.getField(indexName);
            int[] idArray = (int[]) fieldStyle.get(null);
            int index = ((Integer) fieldIndex.get(null)).intValue();
            if (idArray != null && index >= 0) {
                if (index < idArray.length) {
                    return idArray[index];
                }
            }
            return 0;
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchFieldException e) {
            Log.e(TAG, "getResourceDeclareStyleableInt, exception");
            return 0;
        }
    }

    public static final int getResourceDeclareStyleableIndex(String pkgName, String indexName) {
        if (TextUtils.isEmpty(pkgName) || TextUtils.isEmpty(indexName)) {
            return 0;
        }
        try {
            return ((Integer) Class.forName(pkgName + ".R$styleable").getField(indexName).get(null)).intValue();
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchFieldException e) {
            Log.e(TAG, "getResourceDeclareStyleableIndex, exception");
            return 0;
        }
    }

    public static boolean isRtlLocale(View view) {
        String currentLang = Locale.getDefault().getLanguage();
        boolean isLayoutRtl = false;
        if (view != null) {
            isLayoutRtl = view.isLayoutRtl();
        }
        return (currentLang.contains("ar") || currentLang.contains("fa") || currentLang.contains("iw") || currentLang.contains("ug") || currentLang.contains("ur")) || isLayoutRtl;
    }

    public static HwAnimatedGradientDrawable getHwAnimatedGradientDrawable(Context context, ClickEffectEntry clickEffectEntry) {
        HwAnimatedGradientDrawable drawable = new HwAnimatedGradientDrawable(context);
        drawable.setColor(clickEffectEntry.mClickEffectColor);
        drawable.setMaxRectAlpha(clickEffectEntry.mClickEffectAlpha);
        drawable.setMinRectScale(clickEffectEntry.mClickEffectMinRecScale);
        drawable.setMaxRectScale(clickEffectEntry.mClickEffectMaxRecScale);
        drawable.setForceDoScaleAnim(clickEffectEntry.mClickEffectForceDoScaleAnim);
        drawable.setCornerRadius(clickEffectEntry.mClickEffectCornerRadius);
        return drawable;
    }

    public static HwAnimatedGradientDrawable getHwAnimatedGradientDrawable(Context context, int defStyleAttr) {
        return getHwAnimatedGradientDrawable(context, getCleckEffectEntry(context, defStyleAttr));
    }

    public static ClickEffectEntry getCleckEffectEntry(Context context, int defStyleAttr) {
        ClickEffectEntry clickEffectEntry = new ClickEffectEntry();
        ResLoader resLoader = ResLoader.getInstance();
        int[] identifierArray = resLoader.getIdentifierArray(context, ResLoaderUtil.STAYLEABLE, "Theme");
        Resources.Theme theme = ResLoader.getInstance().getTheme(context);
        if (theme != null) {
            TypedArray typedArray = theme.obtainStyledAttributes(null, identifierArray, defStyleAttr, 0);
            clickEffectEntry.mClickEffectColor = typedArray.getColor(resLoader.getIdentifier(context, ResLoaderUtil.STAYLEABLE, "Theme_clickEffectColor"), clickEffectEntry.mClickEffectColor);
            clickEffectEntry.mClickEffectAlpha = typedArray.getFloat(resLoader.getIdentifier(context, ResLoaderUtil.STAYLEABLE, "Theme_clickEffectAlpha"), clickEffectEntry.mClickEffectAlpha);
            clickEffectEntry.mClickEffectMinRecScale = typedArray.getFloat(resLoader.getIdentifier(context, ResLoaderUtil.STAYLEABLE, "Theme_clickEffectMinRecScale"), clickEffectEntry.mClickEffectMinRecScale);
            clickEffectEntry.mClickEffectMaxRecScale = typedArray.getFloat(resLoader.getIdentifier(context, ResLoaderUtil.STAYLEABLE, "Theme_clickEffectMaxRecScale"), clickEffectEntry.mClickEffectMaxRecScale);
            clickEffectEntry.mClickEffectCornerRadius = typedArray.getFloat(resLoader.getIdentifier(context, ResLoaderUtil.STAYLEABLE, "Theme_clickEffectCornerRadius"), clickEffectEntry.mClickEffectCornerRadius);
            clickEffectEntry.mClickEffectForceDoScaleAnim = typedArray.getBoolean(resLoader.getIdentifier(context, ResLoaderUtil.STAYLEABLE, "Theme_clickEffectForceDoScaleAnim"), clickEffectEntry.mClickEffectForceDoScaleAnim);
            typedArray.recycle();
        }
        return clickEffectEntry;
    }
}

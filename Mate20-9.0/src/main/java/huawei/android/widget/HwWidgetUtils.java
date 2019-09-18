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
    private static final String TAG = "HwWidgetUtils";

    public static final boolean isActionbarBackgroundThemed(Context context) {
        boolean z = false;
        if (context == null) {
            return false;
        }
        if (context.getResources().getColor(33882153) == 0) {
            z = true;
        }
        return z;
    }

    public static boolean isHwTheme(Context context) {
        TypedValue tv = getThemeOfEmui(context);
        boolean z = false;
        if (tv == null) {
            return false;
        }
        if (tv.type == 16) {
            z = true;
        }
        return z;
    }

    public static boolean isHwLightTheme(Context context) {
        TypedValue tv = getThemeOfEmui(context);
        boolean z = false;
        if (tv == null) {
            return false;
        }
        if (tv.type == 16 && tv.data > 0 && tv.data % 3 == 1) {
            z = true;
        }
        return z;
    }

    public static boolean isHwDarkTheme(Context context) {
        TypedValue tv = getThemeOfEmui(context);
        boolean z = false;
        if (tv == null) {
            return false;
        }
        if (tv.type == 16 && tv.data > 0 && tv.data % 3 == 2) {
            z = true;
        }
        return z;
    }

    public static boolean isHwEmphasizeTheme(Context context) {
        TypedValue tv = getThemeOfEmui(context);
        boolean z = false;
        if (tv == null) {
            return false;
        }
        if (tv.type == 16 && tv.data > 0 && tv.data % 3 == 0) {
            z = true;
        }
        return z;
    }

    private static TypedValue getThemeOfEmui(Context context) {
        if (context == null) {
            return null;
        }
        int themeOfEmuiId = context.getResources().getIdentifier("themeOfEmui", "attr", "androidhwext");
        if (themeOfEmuiId == 0) {
            return null;
        }
        TypedValue tv = new TypedValue();
        context.getTheme().resolveAttribute(themeOfEmuiId, tv, true);
        return tv;
    }

    public static final int[] getResourceDeclareStyleableIntArray(String pkgName, String name) {
        if (TextUtils.isEmpty(pkgName) || TextUtils.isEmpty(name)) {
            return null;
        }
        try {
            return (int[]) Class.forName(pkgName + ".R$styleable").getField(name).get(null);
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchFieldException e) {
            Log.e(TAG, "getResourceDeclareStyleableIntArray, exception");
            return null;
        }
    }

    public static final int getResourceDeclareStyleableInt(String pkgName, String declareStyleable, String indexName) {
        if (TextUtils.isEmpty(pkgName) || TextUtils.isEmpty(declareStyleable) || TextUtils.isEmpty(indexName)) {
            return 0;
        }
        try {
            Class<?> cls = Class.forName(pkgName + ".R$styleable");
            Field fieldStyle = cls.getField(declareStyleable);
            Field fieldIndex = cls.getField(indexName);
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
        return currentLang.contains("ar") || currentLang.contains("fa") || currentLang.contains("iw") || currentLang.contains("ug") || currentLang.contains("ur") || isLayoutRtl;
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
        ResLoader mResLoader = ResLoader.getInstance();
        int[] identifierArray = mResLoader.getIdentifierArray(context, ResLoaderUtil.STAYLEABLE, "Theme");
        Resources.Theme theme = ResLoader.getInstance().getTheme(context);
        if (theme != null) {
            TypedArray ta = theme.obtainStyledAttributes(null, identifierArray, defStyleAttr, 0);
            int color = mResLoader.getIdentifier(context, ResLoaderUtil.STAYLEABLE, "Theme_clickEffectColor");
            int alpha = mResLoader.getIdentifier(context, ResLoaderUtil.STAYLEABLE, "Theme_clickEffectAlpha");
            int minRecScale = mResLoader.getIdentifier(context, ResLoaderUtil.STAYLEABLE, "Theme_clickEffectMinRecScale");
            int maxRecScale = mResLoader.getIdentifier(context, ResLoaderUtil.STAYLEABLE, "Theme_clickEffectMaxRecScale");
            int cornerRadius = mResLoader.getIdentifier(context, ResLoaderUtil.STAYLEABLE, "Theme_clickEffectCornerRadius");
            int forceDoScaleAnim = mResLoader.getIdentifier(context, ResLoaderUtil.STAYLEABLE, "Theme_clickEffectForceDoScaleAnim");
            clickEffectEntry.mClickEffectColor = ta.getColor(color, clickEffectEntry.mClickEffectColor);
            clickEffectEntry.mClickEffectAlpha = ta.getFloat(alpha, clickEffectEntry.mClickEffectAlpha);
            clickEffectEntry.mClickEffectMinRecScale = ta.getFloat(minRecScale, clickEffectEntry.mClickEffectMinRecScale);
            clickEffectEntry.mClickEffectMaxRecScale = ta.getFloat(maxRecScale, clickEffectEntry.mClickEffectMaxRecScale);
            clickEffectEntry.mClickEffectCornerRadius = ta.getFloat(cornerRadius, clickEffectEntry.mClickEffectCornerRadius);
            clickEffectEntry.mClickEffectForceDoScaleAnim = ta.getBoolean(forceDoScaleAnim, clickEffectEntry.mClickEffectForceDoScaleAnim);
            ta.recycle();
        }
        return clickEffectEntry;
    }
}

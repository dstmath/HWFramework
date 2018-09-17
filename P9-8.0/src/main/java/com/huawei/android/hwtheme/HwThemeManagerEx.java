package com.huawei.android.hwtheme;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.hwtheme.HwThemeManager;

public class HwThemeManagerEx {
    public static final String DIR_WALLPAPER = "/data/skin/wallpaper/";

    public static void updateConfiguration() {
        HwThemeManager.updateConfiguration();
    }

    public static boolean makeIconCache(boolean clearall) {
        return HwThemeManager.makeIconCache(clearall);
    }

    public static boolean saveIconToCache(Bitmap bitmap, String fn, boolean clearold) {
        return HwThemeManager.saveIconToCache(bitmap, fn, clearold);
    }

    public static boolean installHwTheme(String themePath) {
        return HwThemeManager.installHwTheme(themePath, false);
    }

    public static boolean installHwTheme(String themePath, boolean setwallpaper) {
        return HwThemeManager.installHwTheme(themePath, setwallpaper);
    }

    public static Drawable getJoinBitmap(Context context, Drawable srcDraw, int backgroudId) {
        return HwThemeManager.getJoinBitmap(context, srcDraw, backgroudId);
    }
}

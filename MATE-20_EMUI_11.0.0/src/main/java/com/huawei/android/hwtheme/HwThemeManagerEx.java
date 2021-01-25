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

    public static boolean makeIconCache(boolean isClearall) {
        return HwThemeManager.makeIconCache(isClearall);
    }

    public static boolean saveIconToCache(Bitmap bitmap, String fn, boolean isClearold) {
        return HwThemeManager.saveIconToCache(bitmap, fn, isClearold);
    }

    public static boolean installHwTheme(String themePath) {
        return HwThemeManager.installHwTheme(themePath, false);
    }

    public static boolean installHwTheme(String themePath, boolean isSetwallpaper) {
        return HwThemeManager.installHwTheme(themePath, isSetwallpaper);
    }

    public static Drawable getJoinBitmap(Context context, Drawable srcDraw, int backgroudId) {
        return HwThemeManager.getJoinBitmap(context, srcDraw, backgroudId);
    }
}

package com.huawei.android.content.res;

import android.content.res.Configuration;
import android.content.res.IHwConfiguration;
import android.graphics.Rect;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class ConfigurationAdapter {
    @HwSystemApi
    public static final Configuration EMPTY = Configuration.EMPTY;

    private ConfigurationAdapter() {
    }

    public static IHwConfiguration getExtraConfig(Configuration configuration) {
        return configuration.extraConfig;
    }

    public static int getNonFullScreen(Configuration configuration) {
        return configuration.nonFullScreen;
    }

    @HwSystemApi
    public static int getWindowingMode(Configuration configuration) {
        return configuration.windowConfiguration.getWindowingMode();
    }

    @HwSystemApi
    public static boolean inHwMagicWindowingMode(Configuration configuration) {
        return configuration.windowConfiguration.inHwMagicWindowingMode();
    }

    @HwSystemApi
    public static boolean isEmpty(Configuration configuration) {
        return configuration.equals(Configuration.EMPTY);
    }

    @HwSystemApi
    public static Rect getBounds(Configuration configuration) {
        return configuration.windowConfiguration.getBounds();
    }

    @HwSystemApi
    public static void setAppBounds(Configuration configuration, Rect bounds) {
        configuration.windowConfiguration.setAppBounds(bounds);
    }

    @HwSystemApi
    public static void setBounds(Configuration configuration, Rect rect) {
        configuration.windowConfiguration.setBounds(rect);
    }

    @HwSystemApi
    public static void setCompatScreenWidthDp(Configuration configuration, int widthDp) {
        configuration.compatScreenWidthDp = widthDp;
    }

    @HwSystemApi
    public static void setCompatScreenHeightDp(Configuration configuration, int heightDp) {
        configuration.compatScreenHeightDp = heightDp;
    }

    @HwSystemApi
    public static void setCompatSmallestScreenWidthDp(Configuration configuration, int widthDp) {
        configuration.compatSmallestScreenWidthDp = widthDp;
    }

    @HwSystemApi
    public static void setWindowingMode(Configuration configuration, int windowingMode) {
        configuration.windowConfiguration.setWindowingMode(windowingMode);
    }

    @HwSystemApi
    public static int resetScreenLayout(int screenLayout) {
        return Configuration.resetScreenLayout(screenLayout);
    }

    @HwSystemApi
    public static int reduceScreenLayout(int curLayout, int longSizeDp, int shortSizeDp) {
        return Configuration.reduceScreenLayout(curLayout, longSizeDp, shortSizeDp);
    }

    @HwSystemApi
    public static int getRotation(Configuration configuration) {
        return configuration.windowConfiguration.getRotation();
    }
}

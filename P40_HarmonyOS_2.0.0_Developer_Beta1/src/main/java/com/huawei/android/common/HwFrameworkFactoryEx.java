package com.huawei.android.common;

import android.common.HwFrameworkFactory;
import android.content.Context;
import android.content.pm.IHwPackageParser;
import android.hwtheme.HwThemeManager;
import android.perf.HwOptPackageParser;
import com.huawei.android.perf.HwOptPackageParserEx;

public class HwFrameworkFactoryEx {
    public static HwOptPackageParserEx getHwOptPackageParser() {
        HwOptPackageParser hwOptPackageParser = HwFrameworkFactory.getHwOptPackageParser();
        HwOptPackageParserEx hwOptPackageParserEx = new HwOptPackageParserEx();
        hwOptPackageParserEx.setOptPackageParser(hwOptPackageParser);
        return hwOptPackageParserEx;
    }

    public static String getHuaweiResolverActivity(Context context) {
        return HwFrameworkFactory.getHuaweiResolverActivity(context);
    }

    public static HwThemeManager.IHwThemeManager getThemeManagerInstance() {
        return HwFrameworkFactory.getHwThemeManagerFactory().getThemeManagerInstance();
    }

    public static IHwPackageParser getHwPackageParser() {
        return HwFrameworkFactory.getHwPackageParser();
    }
}

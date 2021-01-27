package com.huawei.android.perf;

import android.perf.HwOptPackageParser;

public class HwOptPackageParserEx {
    private HwOptPackageParser mOptPackageParser;

    public void setOptPackageParser(HwOptPackageParser optPackageParser) {
        this.mOptPackageParser = optPackageParser;
    }

    public HwOptPackageParser getOptPackageParser() {
        return this.mOptPackageParser;
    }

    public void getOptPackages() {
        this.mOptPackageParser.getOptPackages();
    }

    public boolean isPerfOptEnable(String pkgName, int optTypeId) {
        return this.mOptPackageParser.isPerfOptEnable(pkgName, optTypeId);
    }
}

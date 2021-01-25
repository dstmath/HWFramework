package com.android.server.hidata.appqoe;

public class HwAppQoeWhiteListConfig {
    private String packageName = HwAPPQoEUtils.INVALID_STRING_VALUE;

    public void setPackageName(String packageName2) {
        this.packageName = packageName2;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public String toString() {
        return "HwAppQoeWhiteListConfig mWhiteListName = " + this.packageName;
    }
}

package com.android.server.util;

public class AppInfo {
    private String appName;
    private int count;
    private String packageName;

    public AppInfo(String packageName) {
        this.packageName = packageName;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public int getCount() {
        return this.count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public static String exitReson(String packageName, String backreson) {
        return "PN:" + packageName + ",REASON:" + backreson;
    }

    public String toString() {
        return "PN:" + this.packageName + ",COUNT:" + this.count;
    }
}

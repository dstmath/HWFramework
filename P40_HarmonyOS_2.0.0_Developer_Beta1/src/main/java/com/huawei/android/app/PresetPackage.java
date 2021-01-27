package com.huawei.android.app;

public class PresetPackage {
    private String packageName;
    private String packagePath;
    private AppType type;

    public enum AppType {
        PRIV,
        SYS,
        NOSYS;

        public static AppType valueOf(int ordinal) {
            if (ordinal < 0 || ordinal >= values().length) {
                return values()[0];
            }
            return values()[ordinal];
        }
    }

    public void setPackageName(String packageName2) {
        this.packageName = packageName2;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public void setPackagePath(String packagePath2) {
        this.packagePath = packagePath2;
    }

    public String getPackagePath() {
        return this.packagePath;
    }

    public AppType getType() {
        return this.type;
    }

    public void setType(AppType type2) {
        this.type = type2;
    }

    public String toString() {
        return "PresetPackage: packageName:" + this.packageName + " packagePath:" + this.packagePath + " type:" + this.type;
    }
}

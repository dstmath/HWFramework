package ohos.bundlemgr.webability;

public class WebPackageInfo {
    private String appName;
    private String appType;
    private String dataPath;
    private String icon;
    private String packageName;

    public String getPackageName() {
        return this.packageName;
    }

    public void setPackageName(String str) {
        this.packageName = str;
    }

    public String getAppName() {
        return this.appName;
    }

    public void setAppName(String str) {
        this.appName = str;
    }

    public String getIcon() {
        return this.icon;
    }

    public void setIcon(String str) {
        this.icon = str;
    }

    public String getAppType() {
        return this.appType;
    }

    public void setAppType(String str) {
        this.appType = str;
    }

    public String getDataPath() {
        return this.dataPath;
    }

    public void setDataPath(String str) {
        this.dataPath = str;
    }
}

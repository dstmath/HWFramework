package ohos.data.search.model;

public class SearchableEntity {
    private String appId;
    private String bundleName;
    private String componentName;
    private String intentAction;
    private boolean isAllowGlobalSearch;
    private String permission;
    private int versionCode;
    private String versionName;

    public SearchableEntity(String str, String str2, String str3, String str4, String str5, boolean z, int i, String str6) {
        this.bundleName = str;
        this.appId = str2;
        this.permission = str3;
        this.intentAction = str4;
        this.componentName = str5;
        this.isAllowGlobalSearch = z;
        this.versionCode = i;
        this.versionName = str6;
    }

    public String toString() {
        return "SearchableEntity{" + this.bundleName + "," + this.appId + "," + this.permission + "," + this.intentAction + "," + this.componentName + "," + this.isAllowGlobalSearch + "," + this.versionCode + "," + this.versionName + "}";
    }

    public String getBundleName() {
        return this.bundleName;
    }

    public String getAppId() {
        return this.appId;
    }

    public String getPermission() {
        return this.permission;
    }

    public String getIntentAction() {
        return this.intentAction;
    }

    public String getComponentName() {
        return this.componentName;
    }

    public boolean isAllowGlobalSearch() {
        return this.isAllowGlobalSearch;
    }

    public int getVersionCode() {
        return this.versionCode;
    }

    public String getVersionName() {
        return this.versionName;
    }
}

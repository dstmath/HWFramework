package com.android.server.pm;

public class PackagePublicityInfo {
    private String mAuthor;
    private String mCategory;
    private String mFeature;
    private String mIsLauncher;
    private String mIsUninstall;
    private String mLabel;
    private String mPackageFileName;
    private String mPackageName;
    private String mSignature;
    private String mUsePermission;

    public String getPackageName() {
        return this.mPackageName;
    }

    public void setPackageName(String packageName) {
        this.mPackageName = packageName;
    }

    public String getLabel() {
        return this.mLabel;
    }

    public void setLabel(String label) {
        this.mLabel = label;
    }

    public String getFeature() {
        return this.mFeature;
    }

    public void setFeature(String feature) {
        this.mFeature = feature;
    }

    public String getAuthor() {
        return this.mAuthor;
    }

    public void setAuthor(String author) {
        this.mAuthor = author;
    }

    public String getIsLauncher() {
        return this.mIsLauncher;
    }

    public void setIsLauncher(String isLauncher) {
        this.mIsLauncher = isLauncher;
    }

    public String getIsUninstall() {
        return this.mIsUninstall;
    }

    public void setIsUninstall(String isUninstall) {
        this.mIsUninstall = isUninstall;
    }

    public String getPackageFileName() {
        return this.mPackageFileName;
    }

    public void setPackageFileName(String packageFileName) {
        this.mPackageFileName = packageFileName;
    }

    public String getUsePermission() {
        return this.mUsePermission;
    }

    public void setUsePermission(String usePermission) {
        this.mUsePermission = usePermission;
    }

    public String getCategory() {
        return this.mCategory;
    }

    public void setCategory(String category) {
        this.mCategory = category;
    }

    public String getSignature() {
        return this.mSignature;
    }

    public void setSignature(String signature) {
        this.mSignature = signature;
    }
}

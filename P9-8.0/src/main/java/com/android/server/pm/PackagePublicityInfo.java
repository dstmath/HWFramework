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

    public void setPackageName(String mPackageName) {
        this.mPackageName = mPackageName;
    }

    public String getLabel() {
        return this.mLabel;
    }

    public void setLabel(String mLabel) {
        this.mLabel = mLabel;
    }

    public String getFeature() {
        return this.mFeature;
    }

    public void setFeature(String mFeature) {
        this.mFeature = mFeature;
    }

    public String getAuthor() {
        return this.mAuthor;
    }

    public void setAuthor(String mAuthor) {
        this.mAuthor = mAuthor;
    }

    public String getIsLauncher() {
        return this.mIsLauncher;
    }

    public void setIsLauncher(String mIsLauncher) {
        this.mIsLauncher = mIsLauncher;
    }

    public String getIsUninstall() {
        return this.mIsUninstall;
    }

    public void setIsUninstall(String mIsUninstall) {
        this.mIsUninstall = mIsUninstall;
    }

    public String getPackageFileName() {
        return this.mPackageFileName;
    }

    public void setPackageFileName(String mPackageFileName) {
        this.mPackageFileName = mPackageFileName;
    }

    public String getUsePermission() {
        return this.mUsePermission;
    }

    public void setUsePermission(String mUsePermission) {
        this.mUsePermission = mUsePermission;
    }

    public String getCategory() {
        return this.mCategory;
    }

    public void setCategory(String mCategory) {
        this.mCategory = mCategory;
    }

    public String getSignature() {
        return this.mSignature;
    }

    public void setSignature(String mSignature) {
        this.mSignature = mSignature;
    }
}

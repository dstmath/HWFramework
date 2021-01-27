package com.android.server.pm;

public class HwRenamedPackagePolicyEx {
    public static final int EXCLUSIVE_INSTALL = 4;
    private HwRenamedPackagePolicy mPackagePolicy;

    public HwRenamedPackagePolicy getmPackagePolicy() {
        return this.mPackagePolicy;
    }

    public void setmPackagePolicy(HwRenamedPackagePolicy mPackagePolicy2) {
        this.mPackagePolicy = mPackagePolicy2;
    }

    public String getOriginalPackageName() {
        return this.mPackagePolicy.getOriginalPackageName();
    }

    public String getNewPackageName() {
        return this.mPackagePolicy.getNewPackageName();
    }
}

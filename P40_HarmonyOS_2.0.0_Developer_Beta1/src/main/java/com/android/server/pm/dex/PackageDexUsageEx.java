package com.android.server.pm.dex;

import com.android.server.pm.dex.PackageDexUsage;

public class PackageDexUsageEx {
    private PackageDexUsage mPackageDexUsage;

    public static class PackageUseInfoEx {
        private PackageDexUsage.PackageUseInfo mPackageUseInfo;

        public PackageDexUsage.PackageUseInfo getPackageUseInfo() {
            return this.mPackageUseInfo;
        }

        public void setPackageUseInfo(PackageDexUsage.PackageUseInfo mPackageUseInfo2) {
            this.mPackageUseInfo = mPackageUseInfo2;
        }
    }

    public PackageDexUsage getPackageDexUsage() {
        return this.mPackageDexUsage;
    }

    public void setPackageDexUsage(PackageDexUsage packageDexUsage) {
        this.mPackageDexUsage = packageDexUsage;
    }
}

package com.huawei.android.content.pm;

import android.content.pm.PackageParser;
import android.os.Bundle;
import com.huawei.annotation.HwSystemApi;

public class PackageParserEx {
    public static final int FLAG_UPDATED_REMOVEABLE_APP = 67108864;
    @HwSystemApi
    public static final Bundle INVALID_META_DATA = new Bundle();
    @HwSystemApi
    public static final String INVALID_PACKAGE_NAME = "";
    public static final int PARSE_IS_REMOVABLE_PREINSTALLED_APK = 33554432;

    @HwSystemApi
    public static class Package {
        private PackageParser.Package mPackage;

        public void setPackage(PackageParser.Package pkg) {
            this.mPackage = pkg;
        }

        public String getPackageName() {
            PackageParser.Package r0 = this.mPackage;
            if (r0 != null) {
                return r0.packageName;
            }
            return "";
        }

        public Bundle getAppMetaData() {
            PackageParser.Package r0 = this.mPackage;
            if (r0 != null) {
                return r0.mAppMetaData;
            }
            return PackageParserEx.INVALID_META_DATA;
        }
    }
}

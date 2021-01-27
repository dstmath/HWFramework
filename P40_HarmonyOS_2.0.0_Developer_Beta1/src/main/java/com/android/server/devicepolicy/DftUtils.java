package com.android.server.devicepolicy;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import com.android.server.pm.HwMdmDFTUtilImpl;
import com.android.server.pm.HwMdmDoPackageInfo;
import com.android.server.pm.HwMdmWpPackageInfo;

public class DftUtils {
    private static final String TAG = "DftUtils";

    public static void collectMdmDoSuccessDftData(Context context, String pkgName) {
        String versionName = getVersionName(context, pkgName);
        if (versionName != null) {
            HwMdmDFTUtilImpl.handleMdmDftUploadEvent(907504002, new HwMdmDoPackageInfo(pkgName, versionName, HwMdmDFTUtilImpl.getCertificateSHA256Fingerprint(context, pkgName)));
        }
    }

    public static void collectMdmWpSuccessDftData(Context context, String pkgName) {
        HwMdmWpPackageInfo wpPackageInfo = new HwMdmWpPackageInfo();
        buildDftData(context, pkgName, wpPackageInfo, true);
        getDeivePackagesIfExist(context, wpPackageInfo);
        HwMdmDFTUtilImpl.handleMdmDftUploadEvent(907504003, wpPackageInfo);
    }

    private static void buildData(String packageName, String versionName, String sign, HwMdmWpPackageInfo wpPackageInfo, boolean isBuildWpData) {
        if (isBuildWpData) {
            wpPackageInfo.setPkg(packageName);
            wpPackageInfo.setVersion(versionName);
            wpPackageInfo.setSighash(sign);
            return;
        }
        wpPackageInfo.setDopkg(packageName);
        wpPackageInfo.setDopkgver(versionName);
        wpPackageInfo.setDopkgsighash(sign);
    }

    private static void buildDftData(Context context, String pkgName, HwMdmWpPackageInfo wpPackageInfo, boolean isBuildWpData) {
        String versionName = getVersionName(context, pkgName);
        if (versionName != null) {
            buildData(pkgName, versionName, HwMdmDFTUtilImpl.getCertificateSHA256Fingerprint(context, pkgName), wpPackageInfo, true);
        }
    }

    private static void getDeivePackagesIfExist(Context context, HwMdmWpPackageInfo wpPackageInfo) {
        String pkgName;
        if (context != null && (pkgName = ((DevicePolicyManager) context.getSystemService("device_policy")).getDeviceOwner()) != null) {
            buildDftData(context, pkgName, wpPackageInfo, false);
        }
    }

    private static String getVersionName(Context context, String packageName) {
        PackageInfo info;
        if (context == null) {
            return null;
        }
        try {
            PackageManager pm = context.getPackageManager();
            if (!(pm == null || (info = pm.getPackageInfo(packageName, 0)) == null)) {
                return info.versionName;
            }
        } catch (PackageManager.NameNotFoundException e) {
            HwLog.e(TAG, "can not get pkg info");
        }
        return null;
    }
}

package com.huawei.secure.android.common;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

@Deprecated
public class HiPkgSignManager {
    private static final String TAG = "HiPkgSignManager";

    @Deprecated
    public static byte[] getInstalledAPPSignature(Context context, String packageName) {
        PackageInfo foundPkgInfo;
        if (context == null || TextUtils.isEmpty(packageName)) {
            Log.e(TAG, "packageName is null or context is null");
            return new byte[0];
        }
        try {
            PackageManager mPackageManager = context.getPackageManager();
            if (!(mPackageManager == null || (foundPkgInfo = mPackageManager.getPackageInfo(packageName, 64)) == null)) {
                return foundPkgInfo.signatures[0].toByteArray();
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "PackageManager.NameNotFoundException : " + e.getMessage());
        }
        return new byte[0];
    }

    @Deprecated
    public static byte[] getUnInstalledAPPSignature(Context context, String archiveFilePath) {
        PackageInfo foundPkgInfo;
        if (context == null || TextUtils.isEmpty(archiveFilePath)) {
            Log.e(TAG, "archiveFilePath is null or context is null");
            return new byte[0];
        }
        try {
            PackageManager mPackageManager = context.getPackageManager();
            if (!(mPackageManager == null || (foundPkgInfo = mPackageManager.getPackageArchiveInfo(archiveFilePath, 64)) == null)) {
                return foundPkgInfo.signatures[0].toByteArray();
            }
        } catch (Exception e) {
            Log.e(TAG, "getUnInstalledAPPSignature exception : " + e.getMessage());
        }
        return new byte[0];
    }

    @Deprecated
    public static String getInstalledAppHash(Context context, String targetPackageName) {
        byte[] signatures = getInstalledAPPSignature(context, targetPackageName);
        if (signatures == null || signatures.length <= 0) {
            return "";
        }
        return encryByteBySHA256(signatures);
    }

    @Deprecated
    public static String getUnInstalledAppHash(Context context, String targetArchivePath) {
        byte[] signatures = getUnInstalledAPPSignature(context, targetArchivePath);
        if (signatures == null || signatures.length <= 0) {
            return "";
        }
        return encryByteBySHA256(signatures);
    }

    private static String encryByteBySHA256(byte[] data) {
        try {
            return byteArrayToHexString(MessageDigest.getInstance("SHA-256").digest(data));
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "NoSuchAlgorithmException" + e.getMessage());
            return "";
        }
    }

    private static String byteArrayToHexString(byte[] data) {
        if (data == null) {
            return null;
        }
        int len = data.length;
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < len; i++) {
            if ((data[i] & 255) < 16) {
                sb.append("0" + Integer.toHexString(data[i] & 255));
            } else {
                sb.append(Integer.toHexString(data[i] & 255));
            }
        }
        return sb.toString().toUpperCase(Locale.ENGLISH);
    }

    @Deprecated
    public static boolean doCheckInstalled(Context context, String preSign, String pkgName) {
        if (TextUtils.isEmpty(pkgName) || TextUtils.isEmpty(preSign) || context == null) {
            return false;
        }
        return preSign.equals(getInstalledAppHash(context, pkgName));
    }

    @Deprecated
    public static boolean doCheckArchiveApk(Context context, String preSign, String apkArchivePath, String pkgName) {
        if (TextUtils.isEmpty(pkgName) || TextUtils.isEmpty(apkArchivePath) || TextUtils.isEmpty(preSign) || context == null) {
            return false;
        }
        String packageName = context.getPackageName();
        String unInstallAppHash = getUnInstalledAppHash(context, apkArchivePath);
        if (TextUtils.isEmpty(packageName) || TextUtils.isEmpty(unInstallAppHash) || !preSign.equals(unInstallAppHash.toUpperCase(Locale.ENGLISH))) {
            return false;
        }
        return true;
    }
}

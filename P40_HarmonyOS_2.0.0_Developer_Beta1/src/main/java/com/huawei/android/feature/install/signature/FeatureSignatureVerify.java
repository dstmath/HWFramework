package com.huawei.android.feature.install.signature;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.text.TextUtils;
import android.util.Log;
import java.io.File;
import java.security.MessageDigest;
import java.util.Locale;

public class FeatureSignatureVerify {
    private static final String TAG = FeatureSignatureVerify.class.getSimpleName();

    public static boolean checkArchiveApk(Context context, String str, String str2) {
        PackageInfo packageArchiveInfo;
        MessageDigest messageDigest;
        if (context == null || TextUtils.isEmpty(str) || TextUtils.isEmpty(str2)) {
            return false;
        }
        File file = new File(str2);
        if (!file.exists() || (packageArchiveInfo = context.getPackageManager().getPackageArchiveInfo(file.getAbsolutePath(), 64)) == null) {
            return false;
        }
        byte[] byteArray = packageArchiveInfo.signatures[0].toByteArray();
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            messageDigest = null;
        }
        if (messageDigest == null) {
            return false;
        }
        byte[] digest = messageDigest.digest(byteArray);
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            String upperCase = Integer.toHexString(b & 255).toUpperCase(Locale.getDefault());
            if (upperCase.length() == 1) {
                sb.append("0");
            }
            sb.append(upperCase);
        }
        return TextUtils.equals(sb.toString(), str);
    }

    @SuppressLint({"PackageManagerGetSignatures"})
    public static boolean checkArchiveApkWithSelf(Context context, String str) {
        if (context == null || TextUtils.isEmpty(str)) {
            return false;
        }
        File file = new File(str);
        if (!file.exists()) {
            return false;
        }
        PackageInfo packageInfo = null;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 64);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, e.getMessage());
        }
        PackageInfo packageArchiveInfo = context.getPackageManager().getPackageArchiveInfo(file.getAbsolutePath(), 64);
        if (packageInfo == null || packageArchiveInfo == null) {
            return false;
        }
        Signature[] signatureArr = packageInfo.signatures;
        Signature[] signatureArr2 = packageArchiveInfo.signatures;
        if (signatureArr == null || signatureArr.length == 0 || signatureArr2 == null || signatureArr2.length == 0) {
            return false;
        }
        return TextUtils.equals(signatureArr[0].toCharsString(), signatureArr2[0].toCharsString());
    }
}

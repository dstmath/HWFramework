package com.msic.qarth.Utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageParser;
import android.content.pm.Signature;
import android.cover.CoverManager;
import android.util.apk.ApkSignatureVerifier;
import com.msic.qarth.PatchFile;
import com.msic.qarth.QarthLog;

public final class SignatureUtil {
    private static final String TAG = "SignatureUtil";

    private SignatureUtil() {
    }

    public static boolean checkSignature(Context context, PatchFile patchFile) {
        if (context == null || patchFile == null) {
            return false;
        }
        return checkApkSignatureIsQarth(context, patchFile.getPath());
    }

    public static boolean checkApkSignatureIsPlatform(Context context, String apkPath) {
        boolean z = false;
        if (context == null || apkPath == null) {
            return false;
        }
        String apkSign = getApkSignature(apkPath);
        String platformSign = getPlatformSignature(context);
        if (apkSign != null && apkSign.equals(platformSign)) {
            z = true;
        }
        return z;
    }

    public static boolean checkApkSignatureIsQarth(Context context, String apkPath) {
        boolean z = false;
        if (context == null || apkPath == null) {
            return false;
        }
        String apkSign = getApkSignature(apkPath);
        String qarthSign = getQarthSignature(context);
        if (apkSign != null && apkSign.equals(qarthSign)) {
            z = true;
        }
        return z;
    }

    public static boolean checkPackageSignature(Context context, String pkg) {
        boolean z = false;
        if (context == null || pkg == null) {
            return false;
        }
        String s1 = getPackageSignature(context, pkg);
        String s2 = getPlatformSignature(context);
        if (s1 != null && s1.equals(s2)) {
            z = true;
        }
        return z;
    }

    private static String getQarthSignature(Context context) {
        return getPackageSignature(context, "com.qihoo.qarth");
    }

    private static String getApkSignature(String apkPath) {
        try {
            PackageParser.SigningDetails signDetail = ApkSignatureVerifier.verify(apkPath, 1);
            if (signDetail == null || signDetail.signatures == null || signDetail.signatures.length == 0) {
                QarthLog.e(TAG, "get qarth file signature is null");
                return null;
            }
            StringBuilder signStrBuilder = new StringBuilder();
            for (Signature signature : signDetail.signatures) {
                signStrBuilder.append(signature.toCharsString());
            }
            return signStrBuilder.toString();
        } catch (PackageParser.PackageParserException e) {
            QarthLog.e(TAG, "verify the qarth file signature exception");
            return null;
        }
    }

    private static String getPackageSignature(Context context, String pkg) {
        PackageInfo pi = null;
        try {
            pi = context.getPackageManager().getPackageInfo(pkg, 64);
        } catch (PackageManager.NameNotFoundException e) {
            QarthLog.e(TAG, "get package signature NameNotFoundException");
        }
        if (pi == null) {
            return null;
        }
        return getSignature(pi);
    }

    private static String getPlatformSignature(Context context) {
        return getPackageSignature(context, CoverManager.HALL_STATE_RECEIVER_DEFINE);
    }

    private static String getSignature(PackageInfo pi) {
        if (pi == null) {
            return null;
        }
        Signature[] signatures = pi.signatures;
        if (signatures == null || signatures.length == 0) {
            return null;
        }
        StringBuilder signSB = new StringBuilder();
        for (Signature signature : signatures) {
            signSB.append(signature.toCharsString());
        }
        return signSB.toString();
    }
}

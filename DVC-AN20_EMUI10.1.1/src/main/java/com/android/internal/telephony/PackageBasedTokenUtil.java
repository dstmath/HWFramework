package com.android.internal.telephony;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.util.Base64;
import android.util.Log;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class PackageBasedTokenUtil {
    private static final Charset CHARSET_UTF_8 = Charset.forName("UTF-8");
    private static final String HASH_TYPE = "SHA-256";
    static final int NUM_BASE64_CHARS = 11;
    private static final int NUM_HASHED_BYTES = 9;
    private static final String TAG = "PackageBasedTokenUtil";

    public static String generateToken(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();
        String token = generatePackageBasedToken(packageManager, packageName);
        for (PackageInfo packageInfo : packageManager.getInstalledPackages(128)) {
            String otherPackageName = packageInfo.packageName;
            if (!packageName.equals(otherPackageName)) {
                String otherToken = generatePackageBasedToken(packageManager, otherPackageName);
                if (token != null && token.equals(otherToken)) {
                    Log.e(TAG, "token collides with other installed app.");
                    token = null;
                }
            }
        }
        return token;
    }

    private static String generatePackageBasedToken(PackageManager packageManager, String packageName) {
        try {
            Signature[] signatures = packageManager.getPackageInfo(packageName, 64).signatures;
            if (signatures == null) {
                Log.e(TAG, "The certificates is missing.");
                return null;
            }
            try {
                MessageDigest messageDigest = MessageDigest.getInstance(HASH_TYPE);
                messageDigest.update(packageName.getBytes(CHARSET_UTF_8));
                messageDigest.update(" ".getBytes(CHARSET_UTF_8));
                for (Signature signature : signatures) {
                    messageDigest.update(signature.toCharsString().getBytes(CHARSET_UTF_8));
                }
                String token = Base64.encodeToString(Arrays.copyOf(messageDigest.digest(), 9), 3);
                if (token != null) {
                    return token.substring(0, 11);
                }
                return token;
            } catch (NoSuchAlgorithmException e) {
                Log.e(TAG, "NoSuchAlgorithmException" + e);
                return null;
            }
        } catch (PackageManager.NameNotFoundException e2) {
            Log.e(TAG, "Failed to find package with package name: " + packageName);
            return null;
        }
    }
}

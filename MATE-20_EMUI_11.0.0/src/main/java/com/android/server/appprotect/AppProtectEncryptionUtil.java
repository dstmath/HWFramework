package com.android.server.appprotect;

import android.text.TextUtils;
import android.util.Log;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import libcore.util.HexEncoding;

class AppProtectEncryptionUtil {
    private static final String STRING_EMPTY = "";
    private static final String TAG = "AppProtectEncryptionUtil";

    AppProtectEncryptionUtil() {
    }

    public static String getHashCodeForPackageName(String data) {
        if (TextUtils.isEmpty(data)) {
            return "";
        }
        try {
            return sha256(data.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "getHashCodeForString UnsupportedEncodingException");
            return "";
        }
    }

    private static String sha256(byte[] data) {
        if (data == null) {
            return "";
        }
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(data);
            return HexEncoding.encodeToString(messageDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "sha256 NoSuchAlgorithmException");
            return "";
        }
    }
}

package com.huawei.secure.android.common.encrypt.hash;

import android.text.TextUtils;
import android.util.Log;
import com.huawei.secure.android.common.util.HexUtil;
import com.huawei.wallet.sdk.common.utils.crypto.AES;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class SHA {
    private static final String EMPTY = "";
    private static final String[] SAFE_ALGORITHM = {"SHA-256", "SHA-384", "SHA-512"};
    private static final String SHA256_ALGORITHM = "SHA-256";
    private static final String TAG = "SHA";

    private SHA() {
    }

    public static String sha256Encrypt(String content) {
        return shaEncrypt(content, "SHA-256");
    }

    public static String shaEncrypt(String content, String algorithm) {
        if (TextUtils.isEmpty(content) || TextUtils.isEmpty(algorithm)) {
            Log.e(TAG, "content or algorithm is null.");
            return "";
        } else if (!isLegalAlgorithm(algorithm)) {
            Log.e(TAG, "algorithm is not safe or legal");
            return "";
        } else {
            try {
                MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
                messageDigest.update(content.getBytes(AES.CHAR_ENCODING));
                return HexUtil.byteArray2HexStr(messageDigest.digest());
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, "Error in generate SHA UnsupportedEncodingException");
                return "";
            } catch (NoSuchAlgorithmException e2) {
                Log.e(TAG, "Error in generate SHA NoSuchAlgorithmException");
                return "";
            }
        }
    }

    public static boolean validateSHA256(String content, String encryptContent) {
        if (TextUtils.isEmpty(content) || TextUtils.isEmpty(encryptContent)) {
            return false;
        }
        return encryptContent.equals(sha256Encrypt(content));
    }

    public static boolean validateSHA(String content, String encryptContent, String algorithm) {
        if (TextUtils.isEmpty(content) || TextUtils.isEmpty(encryptContent) || TextUtils.isEmpty(algorithm)) {
            return false;
        }
        return encryptContent.equals(shaEncrypt(content, algorithm));
    }

    private static boolean isLegalAlgorithm(String algorithm) {
        for (String alg : SAFE_ALGORITHM) {
            if (alg.equals(algorithm)) {
                return true;
            }
        }
        return false;
    }
}

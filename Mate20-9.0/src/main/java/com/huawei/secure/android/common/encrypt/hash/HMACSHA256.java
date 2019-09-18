package com.huawei.secure.android.common.encrypt.hash;

import android.text.TextUtils;
import android.util.Log;
import com.huawei.secure.android.common.util.HexUtil;
import com.huawei.wallet.sdk.common.utils.crypto.AES;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public final class HMACSHA256 {
    private static final String ALGORITHM = "HmacSHA256";
    private static final String EMPTY = "";
    private static final int HMACSHA256_KEY_LEN = 32;
    private static final String TAG = "HMACSHA256";

    public static String hmacSHA256Encrypt(String content, String key) {
        if (TextUtils.isEmpty(content) || TextUtils.isEmpty(key)) {
            return "";
        }
        return hmacSHA256Encrypt(content, HexUtil.hexStr2ByteArray(key));
    }

    public static String hmacSHA256Encrypt(String content, byte[] key) {
        if (TextUtils.isEmpty(content) || key == null) {
            return "";
        }
        if (key.length < 32) {
            Log.e(TAG, "hmac key length is not right");
            return "";
        }
        try {
            SecretKeySpec secretKey = new SecretKeySpec(key, ALGORITHM);
            Mac mac = Mac.getInstance(secretKey.getAlgorithm());
            mac.init(secretKey);
            return HexUtil.byteArray2HexStr(mac.doFinal(content.getBytes(AES.CHAR_ENCODING)));
        } catch (UnsupportedEncodingException | InvalidKeyException | NoSuchAlgorithmException e) {
            Log.e(TAG, "hmacsha256 encrypt exception" + e.getMessage());
            return "";
        }
    }
}

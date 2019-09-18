package com.huawei.wallet.sdk.common.utils.crypto;

import android.text.TextUtils;
import com.huawei.wallet.sdk.common.log.LogC;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class HMACSHA256 {
    private static String endValue = "HmacSHA256";

    public static String hmac_256(String data, String key) {
        String sHmac = "";
        try {
            if (!TextUtils.isEmpty(data)) {
                if (!TextUtils.isEmpty(key)) {
                    Mac hmac_256 = Mac.getInstance(endValue);
                    hmac_256.init(new SecretKeySpec(AES.asBin(key), endValue));
                    sHmac = SHA_256.bytes2Hex(hmac_256.doFinal(data.getBytes(AES.CHAR_ENCODING)));
                    return sHmac;
                }
            }
            return "";
        } catch (RuntimeException e) {
            LogC.e("hmac_256 data exception", false);
        } catch (Exception e2) {
            LogC.e("encrypt Exception::", (Throwable) e2, false);
        }
    }
}

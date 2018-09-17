package tmsdkobf;

import android.content.Context;
import android.text.TextUtils;
import java.io.UnsupportedEncodingException;
import tmsdk.common.tcc.TccCryptor;
import tmsdk.common.utils.b;
import tmsdk.common.utils.f;

public class kk {
    private static String TAG = "CryptorUtils";

    public static String c(Context context, String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        try {
            byte[] encrypt = TccCryptor.encrypt(context, str.getBytes("gbk"), null);
            if (encrypt != null) {
                return b.encodeToString(encrypt, 0);
            }
        } catch (UnsupportedEncodingException e) {
            f.e(TAG, "getEncodeString, UnsupportedEncodingException: " + e);
        } catch (Exception e2) {
            f.e(TAG, "getEncodeString, Exception: " + e2);
        }
        return null;
    }

    public static String d(Context context, String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        try {
            byte[] decrypt = TccCryptor.decrypt(context, b.decode(str, 0), null);
            if (decrypt != null) {
                return new String(decrypt, "gbk");
            }
        } catch (UnsupportedEncodingException e) {
            f.e(TAG, "getDecodeString, UnsupportedEncodingException: " + e);
        } catch (Exception e2) {
            f.e(TAG, "getDecodeString, Exception: " + e2);
        }
        return null;
    }
}

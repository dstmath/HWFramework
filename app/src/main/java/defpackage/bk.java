package defpackage;

import android.util.Base64;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.Cipher;

/* renamed from: bk */
class bk {
    static byte[] b(byte[] bArr, String str) {
        Cipher instance = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
        instance.init(1, bk.x(str));
        return instance.doFinal(bArr);
    }

    private static PublicKey x(String str) {
        return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(Base64.decode(str, 0)));
    }
}

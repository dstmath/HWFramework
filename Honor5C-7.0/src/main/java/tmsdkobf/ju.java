package tmsdkobf;

import java.security.interfaces.RSAPublicKey;
import javax.crypto.Cipher;
import javax.security.cert.CertificateException;
import javax.security.cert.X509Certificate;

/* compiled from: Unknown */
final class ju {
    public static byte[] a(byte[] bArr, RSAPublicKey rSAPublicKey) {
        if (rSAPublicKey != null) {
            try {
                Cipher instance = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                instance.init(2, rSAPublicKey);
                return instance.doFinal(bArr);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static RSAPublicKey h(byte[] bArr) {
        try {
            return (RSAPublicKey) X509Certificate.getInstance(bArr).getPublicKey();
        } catch (CertificateException e) {
            e.printStackTrace();
            return null;
        }
    }
}

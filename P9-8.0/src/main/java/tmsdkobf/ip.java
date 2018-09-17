package tmsdkobf;

import java.security.interfaces.RSAPublicKey;
import javax.crypto.Cipher;
import javax.security.cert.CertificateException;
import javax.security.cert.X509Certificate;

final class ip {
    public static byte[] a(byte[] bArr, RSAPublicKey rSAPublicKey) {
        byte[] bArr2 = null;
        if (rSAPublicKey == null) {
            return bArr2;
        }
        try {
            Cipher instance = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            instance.init(2, rSAPublicKey);
            return instance.doFinal(bArr);
        } catch (Exception e) {
            e.printStackTrace();
            return bArr2;
        }
    }

    public static RSAPublicKey h(byte[] bArr) {
        RSAPublicKey rSAPublicKey = null;
        try {
            return (RSAPublicKey) X509Certificate.getInstance(bArr).getPublicKey();
        } catch (CertificateException e) {
            e.printStackTrace();
            return rSAPublicKey;
        }
    }
}

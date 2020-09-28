package android.security.keystore.recovery;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;

public class X509CertificateParsingUtils {
    private static final String CERT_FORMAT = "X.509";

    public static X509Certificate decodeBase64Cert(String string) throws CertificateException {
        try {
            return decodeCert(decodeBase64(string));
        } catch (IllegalArgumentException e) {
            throw new CertificateException(e);
        }
    }

    private static byte[] decodeBase64(String string) {
        return Base64.getDecoder().decode(string);
    }

    private static X509Certificate decodeCert(byte[] certBytes) throws CertificateException {
        return decodeCert(new ByteArrayInputStream(certBytes));
    }

    private static X509Certificate decodeCert(InputStream inStream) throws CertificateException {
        try {
            return (X509Certificate) CertificateFactory.getInstance(CERT_FORMAT).generateCertificate(inStream);
        } catch (CertificateException e) {
            throw new RuntimeException(e);
        }
    }
}

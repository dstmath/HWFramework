package org.bouncycastle.pkix.jcajce;

import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.pkix.PKIXIdentity;

public class JcaPKIXIdentity extends PKIXIdentity {
    private final X509Certificate[] certs;
    private final PrivateKey privKey;

    public JcaPKIXIdentity(PrivateKey privateKey, X509Certificate[] x509CertificateArr) {
        super(getPrivateKeyInfo(privateKey), getCertificates(x509CertificateArr));
        this.privKey = privateKey;
        this.certs = new X509Certificate[x509CertificateArr.length];
        System.arraycopy(x509CertificateArr, 0, this.certs, 0, x509CertificateArr.length);
    }

    private static X509CertificateHolder[] getCertificates(X509Certificate[] x509CertificateArr) {
        X509CertificateHolder[] x509CertificateHolderArr = new X509CertificateHolder[x509CertificateArr.length];
        int i = 0;
        while (i != x509CertificateHolderArr.length) {
            try {
                x509CertificateHolderArr[i] = new JcaX509CertificateHolder(x509CertificateArr[i]);
                i++;
            } catch (CertificateEncodingException e) {
                throw new IllegalArgumentException("Unable to process certificates: " + e.getMessage());
            }
        }
        return x509CertificateHolderArr;
    }

    private static PrivateKeyInfo getPrivateKeyInfo(PrivateKey privateKey) {
        try {
            return PrivateKeyInfo.getInstance(privateKey.getEncoded());
        } catch (Exception e) {
            return null;
        }
    }

    public PrivateKey getPrivateKey() {
        return this.privKey;
    }

    public X509Certificate getX509Certificate() {
        return this.certs[0];
    }
}

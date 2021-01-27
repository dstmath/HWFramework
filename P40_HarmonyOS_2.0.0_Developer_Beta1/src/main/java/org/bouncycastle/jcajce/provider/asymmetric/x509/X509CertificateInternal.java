package org.bouncycastle.jcajce.provider.asymmetric.x509;

import java.security.cert.CertificateEncodingException;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Certificate;
import org.bouncycastle.jcajce.util.JcaJceHelper;

/* access modifiers changed from: package-private */
public class X509CertificateInternal extends X509CertificateImpl {
    private final byte[] encoding;

    X509CertificateInternal(JcaJceHelper jcaJceHelper, Certificate certificate, BasicConstraints basicConstraints, boolean[] zArr, String str, byte[] bArr, byte[] bArr2) {
        super(jcaJceHelper, certificate, basicConstraints, zArr, str, bArr);
        this.encoding = bArr2;
    }

    @Override // org.bouncycastle.jcajce.provider.asymmetric.x509.X509CertificateImpl, java.security.cert.Certificate
    public byte[] getEncoded() throws CertificateEncodingException {
        byte[] bArr = this.encoding;
        if (bArr != null) {
            return bArr;
        }
        throw new CertificateEncodingException();
    }
}

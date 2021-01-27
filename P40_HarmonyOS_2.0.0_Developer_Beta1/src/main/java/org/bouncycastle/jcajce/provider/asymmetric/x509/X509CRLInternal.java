package org.bouncycastle.jcajce.provider.asymmetric.x509;

import java.security.cert.CRLException;
import org.bouncycastle.asn1.x509.CertificateList;
import org.bouncycastle.jcajce.util.JcaJceHelper;

/* access modifiers changed from: package-private */
public class X509CRLInternal extends X509CRLImpl {
    private final byte[] encoding;

    X509CRLInternal(JcaJceHelper jcaJceHelper, CertificateList certificateList, String str, byte[] bArr, boolean z, byte[] bArr2) {
        super(jcaJceHelper, certificateList, str, bArr, z);
        this.encoding = bArr2;
    }

    @Override // org.bouncycastle.jcajce.provider.asymmetric.x509.X509CRLImpl, java.security.cert.X509CRL
    public byte[] getEncoded() throws CRLException {
        byte[] bArr = this.encoding;
        if (bArr != null) {
            return bArr;
        }
        throw new CRLException();
    }
}

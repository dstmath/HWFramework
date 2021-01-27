package org.bouncycastle.cert.crmf.jcajce;

import java.io.IOException;
import java.security.Provider;
import java.security.PublicKey;
import javax.security.auth.x500.X500Principal;
import org.bouncycastle.asn1.ASN1Encoding;
import org.bouncycastle.asn1.crmf.CertReqMsg;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.crmf.CRMFException;
import org.bouncycastle.cert.crmf.CertificateRequestMessage;
import org.bouncycastle.jcajce.util.DefaultJcaJceHelper;
import org.bouncycastle.jcajce.util.NamedJcaJceHelper;
import org.bouncycastle.jcajce.util.ProviderJcaJceHelper;

public class JcaCertificateRequestMessage extends CertificateRequestMessage {
    private CRMFHelper helper;

    public JcaCertificateRequestMessage(CertReqMsg certReqMsg) {
        super(certReqMsg);
        this.helper = new CRMFHelper(new DefaultJcaJceHelper());
    }

    public JcaCertificateRequestMessage(CertificateRequestMessage certificateRequestMessage) {
        this(certificateRequestMessage.toASN1Structure());
    }

    public JcaCertificateRequestMessage(byte[] bArr) {
        this(CertReqMsg.getInstance(bArr));
    }

    public PublicKey getPublicKey() throws CRMFException {
        SubjectPublicKeyInfo publicKey = getCertTemplate().getPublicKey();
        if (publicKey != null) {
            return this.helper.toPublicKey(publicKey);
        }
        return null;
    }

    public X500Principal getSubjectX500Principal() {
        X500Name subject = getCertTemplate().getSubject();
        if (subject == null) {
            return null;
        }
        try {
            return new X500Principal(subject.getEncoded(ASN1Encoding.DER));
        } catch (IOException e) {
            throw new IllegalStateException("unable to construct DER encoding of name: " + e.getMessage());
        }
    }

    public JcaCertificateRequestMessage setProvider(String str) {
        this.helper = new CRMFHelper(new NamedJcaJceHelper(str));
        return this;
    }

    public JcaCertificateRequestMessage setProvider(Provider provider) {
        this.helper = new CRMFHelper(new ProviderJcaJceHelper(provider));
        return this;
    }
}

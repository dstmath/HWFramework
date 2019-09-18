package org.bouncycastle.cert.cmp;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.cmp.CertConfirmContent;
import org.bouncycastle.asn1.cmp.CertStatus;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DigestCalculator;
import org.bouncycastle.operator.DigestCalculatorProvider;
import org.bouncycastle.operator.OperatorCreationException;

public class CertificateConfirmationContentBuilder {
    private List acceptedCerts;
    private List acceptedReqIds;
    private DigestAlgorithmIdentifierFinder digestAlgFinder;

    public CertificateConfirmationContentBuilder() {
        this(new DefaultDigestAlgorithmIdentifierFinder());
    }

    public CertificateConfirmationContentBuilder(DigestAlgorithmIdentifierFinder digestAlgorithmIdentifierFinder) {
        this.acceptedCerts = new ArrayList();
        this.acceptedReqIds = new ArrayList();
        this.digestAlgFinder = digestAlgorithmIdentifierFinder;
    }

    public CertificateConfirmationContentBuilder addAcceptedCertificate(X509CertificateHolder x509CertificateHolder, BigInteger bigInteger) {
        this.acceptedCerts.add(x509CertificateHolder);
        this.acceptedReqIds.add(bigInteger);
        return this;
    }

    public CertificateConfirmationContent build(DigestCalculatorProvider digestCalculatorProvider) throws CMPException {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        int i = 0;
        while (i != this.acceptedCerts.size()) {
            X509CertificateHolder x509CertificateHolder = (X509CertificateHolder) this.acceptedCerts.get(i);
            BigInteger bigInteger = (BigInteger) this.acceptedReqIds.get(i);
            AlgorithmIdentifier find = this.digestAlgFinder.find(x509CertificateHolder.toASN1Structure().getSignatureAlgorithm());
            if (find != null) {
                try {
                    DigestCalculator digestCalculator = digestCalculatorProvider.get(find);
                    CMPUtil.derEncodeToStream(x509CertificateHolder.toASN1Structure(), digestCalculator.getOutputStream());
                    aSN1EncodableVector.add(new CertStatus(digestCalculator.getDigest(), bigInteger));
                    i++;
                } catch (OperatorCreationException e) {
                    throw new CMPException("unable to create digest: " + e.getMessage(), e);
                }
            } else {
                throw new CMPException("cannot find algorithm for digest from signature");
            }
        }
        return new CertificateConfirmationContent(CertConfirmContent.getInstance(new DERSequence(aSN1EncodableVector)), this.digestAlgFinder);
    }
}

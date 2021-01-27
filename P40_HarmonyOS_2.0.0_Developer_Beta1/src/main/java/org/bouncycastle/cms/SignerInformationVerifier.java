package org.bouncycastle.cms;

import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.operator.ContentVerifier;
import org.bouncycastle.operator.ContentVerifierProvider;
import org.bouncycastle.operator.DigestCalculator;
import org.bouncycastle.operator.DigestCalculatorProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.SignatureAlgorithmIdentifierFinder;

public class SignerInformationVerifier {
    private DigestCalculatorProvider digestProvider;
    private SignatureAlgorithmIdentifierFinder sigAlgorithmFinder;
    private CMSSignatureAlgorithmNameGenerator sigNameGenerator;
    private ContentVerifierProvider verifierProvider;

    public SignerInformationVerifier(CMSSignatureAlgorithmNameGenerator cMSSignatureAlgorithmNameGenerator, SignatureAlgorithmIdentifierFinder signatureAlgorithmIdentifierFinder, ContentVerifierProvider contentVerifierProvider, DigestCalculatorProvider digestCalculatorProvider) {
        this.sigNameGenerator = cMSSignatureAlgorithmNameGenerator;
        this.sigAlgorithmFinder = signatureAlgorithmIdentifierFinder;
        this.verifierProvider = contentVerifierProvider;
        this.digestProvider = digestCalculatorProvider;
    }

    public X509CertificateHolder getAssociatedCertificate() {
        return this.verifierProvider.getAssociatedCertificate();
    }

    public ContentVerifier getContentVerifier(AlgorithmIdentifier algorithmIdentifier, AlgorithmIdentifier algorithmIdentifier2) throws OperatorCreationException {
        return this.verifierProvider.get(new AlgorithmIdentifier(this.sigAlgorithmFinder.find(this.sigNameGenerator.getSignatureName(algorithmIdentifier2, algorithmIdentifier)).getAlgorithm(), algorithmIdentifier.getParameters()));
    }

    public DigestCalculator getDigestCalculator(AlgorithmIdentifier algorithmIdentifier) throws OperatorCreationException {
        return this.digestProvider.get(algorithmIdentifier);
    }

    public boolean hasAssociatedCertificate() {
        return this.verifierProvider.hasAssociatedCertificate();
    }
}

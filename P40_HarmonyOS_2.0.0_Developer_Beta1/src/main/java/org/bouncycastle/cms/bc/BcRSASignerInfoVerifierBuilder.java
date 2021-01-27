package org.bouncycastle.cms.bc;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.CMSSignatureAlgorithmNameGenerator;
import org.bouncycastle.cms.SignerInformationVerifier;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.operator.DigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DigestCalculatorProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.SignatureAlgorithmIdentifierFinder;
import org.bouncycastle.operator.bc.BcRSAContentVerifierProviderBuilder;

public class BcRSASignerInfoVerifierBuilder {
    private BcRSAContentVerifierProviderBuilder contentVerifierProviderBuilder;
    private DigestCalculatorProvider digestCalculatorProvider;
    private SignatureAlgorithmIdentifierFinder sigAlgIdFinder;
    private CMSSignatureAlgorithmNameGenerator sigAlgNameGen;

    public BcRSASignerInfoVerifierBuilder(CMSSignatureAlgorithmNameGenerator cMSSignatureAlgorithmNameGenerator, SignatureAlgorithmIdentifierFinder signatureAlgorithmIdentifierFinder, DigestAlgorithmIdentifierFinder digestAlgorithmIdentifierFinder, DigestCalculatorProvider digestCalculatorProvider2) {
        this.sigAlgNameGen = cMSSignatureAlgorithmNameGenerator;
        this.sigAlgIdFinder = signatureAlgorithmIdentifierFinder;
        this.contentVerifierProviderBuilder = new BcRSAContentVerifierProviderBuilder(digestAlgorithmIdentifierFinder);
        this.digestCalculatorProvider = digestCalculatorProvider2;
    }

    public SignerInformationVerifier build(X509CertificateHolder x509CertificateHolder) throws OperatorCreationException {
        return new SignerInformationVerifier(this.sigAlgNameGen, this.sigAlgIdFinder, this.contentVerifierProviderBuilder.build(x509CertificateHolder), this.digestCalculatorProvider);
    }

    public SignerInformationVerifier build(AsymmetricKeyParameter asymmetricKeyParameter) throws OperatorCreationException {
        return new SignerInformationVerifier(this.sigAlgNameGen, this.sigAlgIdFinder, this.contentVerifierProviderBuilder.build(asymmetricKeyParameter), this.digestCalculatorProvider);
    }
}

package org.bouncycastle.cms.jcajce;

import java.security.Provider;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.CMSSignatureAlgorithmNameGenerator;
import org.bouncycastle.cms.DefaultCMSSignatureAlgorithmNameGenerator;
import org.bouncycastle.cms.SignerInformationVerifier;
import org.bouncycastle.operator.ContentVerifierProvider;
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DigestCalculatorProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.SignatureAlgorithmIdentifierFinder;
import org.bouncycastle.operator.jcajce.JcaContentVerifierProviderBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;

public class JcaSignerInfoVerifierBuilder {
    private DigestCalculatorProvider digestProvider;
    private Helper helper = new Helper();
    private SignatureAlgorithmIdentifierFinder sigAlgIDFinder = new DefaultSignatureAlgorithmIdentifierFinder();
    private CMSSignatureAlgorithmNameGenerator sigAlgNameGen = new DefaultCMSSignatureAlgorithmNameGenerator();

    private class Helper {
        private Helper() {
        }

        /* access modifiers changed from: package-private */
        public ContentVerifierProvider createContentVerifierProvider(PublicKey publicKey) throws OperatorCreationException {
            return new JcaContentVerifierProviderBuilder().build(publicKey);
        }

        /* access modifiers changed from: package-private */
        public ContentVerifierProvider createContentVerifierProvider(X509Certificate x509Certificate) throws OperatorCreationException {
            return new JcaContentVerifierProviderBuilder().build(x509Certificate);
        }

        /* access modifiers changed from: package-private */
        public ContentVerifierProvider createContentVerifierProvider(X509CertificateHolder x509CertificateHolder) throws OperatorCreationException, CertificateException {
            return new JcaContentVerifierProviderBuilder().build(x509CertificateHolder);
        }

        /* access modifiers changed from: package-private */
        public DigestCalculatorProvider createDigestCalculatorProvider() throws OperatorCreationException {
            return new JcaDigestCalculatorProviderBuilder().build();
        }
    }

    private class NamedHelper extends Helper {
        private final String providerName;

        public NamedHelper(String str) {
            super();
            this.providerName = str;
        }

        /* access modifiers changed from: package-private */
        public ContentVerifierProvider createContentVerifierProvider(PublicKey publicKey) throws OperatorCreationException {
            return new JcaContentVerifierProviderBuilder().setProvider(this.providerName).build(publicKey);
        }

        /* access modifiers changed from: package-private */
        public ContentVerifierProvider createContentVerifierProvider(X509Certificate x509Certificate) throws OperatorCreationException {
            return new JcaContentVerifierProviderBuilder().setProvider(this.providerName).build(x509Certificate);
        }

        /* access modifiers changed from: package-private */
        public ContentVerifierProvider createContentVerifierProvider(X509CertificateHolder x509CertificateHolder) throws OperatorCreationException, CertificateException {
            return new JcaContentVerifierProviderBuilder().setProvider(this.providerName).build(x509CertificateHolder);
        }

        /* access modifiers changed from: package-private */
        public DigestCalculatorProvider createDigestCalculatorProvider() throws OperatorCreationException {
            return new JcaDigestCalculatorProviderBuilder().setProvider(this.providerName).build();
        }
    }

    private class ProviderHelper extends Helper {
        private final Provider provider;

        public ProviderHelper(Provider provider2) {
            super();
            this.provider = provider2;
        }

        /* access modifiers changed from: package-private */
        public ContentVerifierProvider createContentVerifierProvider(PublicKey publicKey) throws OperatorCreationException {
            return new JcaContentVerifierProviderBuilder().setProvider(this.provider).build(publicKey);
        }

        /* access modifiers changed from: package-private */
        public ContentVerifierProvider createContentVerifierProvider(X509Certificate x509Certificate) throws OperatorCreationException {
            return new JcaContentVerifierProviderBuilder().setProvider(this.provider).build(x509Certificate);
        }

        /* access modifiers changed from: package-private */
        public ContentVerifierProvider createContentVerifierProvider(X509CertificateHolder x509CertificateHolder) throws OperatorCreationException, CertificateException {
            return new JcaContentVerifierProviderBuilder().setProvider(this.provider).build(x509CertificateHolder);
        }

        /* access modifiers changed from: package-private */
        public DigestCalculatorProvider createDigestCalculatorProvider() throws OperatorCreationException {
            return new JcaDigestCalculatorProviderBuilder().setProvider(this.provider).build();
        }
    }

    public JcaSignerInfoVerifierBuilder(DigestCalculatorProvider digestCalculatorProvider) {
        this.digestProvider = digestCalculatorProvider;
    }

    public SignerInformationVerifier build(PublicKey publicKey) throws OperatorCreationException {
        return new SignerInformationVerifier(this.sigAlgNameGen, this.sigAlgIDFinder, this.helper.createContentVerifierProvider(publicKey), this.digestProvider);
    }

    public SignerInformationVerifier build(X509Certificate x509Certificate) throws OperatorCreationException {
        return new SignerInformationVerifier(this.sigAlgNameGen, this.sigAlgIDFinder, this.helper.createContentVerifierProvider(x509Certificate), this.digestProvider);
    }

    public SignerInformationVerifier build(X509CertificateHolder x509CertificateHolder) throws OperatorCreationException, CertificateException {
        return new SignerInformationVerifier(this.sigAlgNameGen, this.sigAlgIDFinder, this.helper.createContentVerifierProvider(x509CertificateHolder), this.digestProvider);
    }

    public JcaSignerInfoVerifierBuilder setProvider(String str) {
        this.helper = new NamedHelper(str);
        return this;
    }

    public JcaSignerInfoVerifierBuilder setProvider(Provider provider) {
        this.helper = new ProviderHelper(provider);
        return this;
    }

    public JcaSignerInfoVerifierBuilder setSignatureAlgorithmFinder(SignatureAlgorithmIdentifierFinder signatureAlgorithmIdentifierFinder) {
        this.sigAlgIDFinder = signatureAlgorithmIdentifierFinder;
        return this;
    }

    public JcaSignerInfoVerifierBuilder setSignatureAlgorithmNameGenerator(CMSSignatureAlgorithmNameGenerator cMSSignatureAlgorithmNameGenerator) {
        this.sigAlgNameGen = cMSSignatureAlgorithmNameGenerator;
        return this;
    }
}

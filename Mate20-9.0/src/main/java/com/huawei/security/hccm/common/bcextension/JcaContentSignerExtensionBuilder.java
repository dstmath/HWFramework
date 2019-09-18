package com.huawei.security.hccm.common.bcextension;

import java.io.IOException;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.OperatorStreamException;
import org.bouncycastle.operator.RuntimeOperatorException;

public class JcaContentSignerExtensionBuilder {
    public static final String SHA256PSS = "SHA256withRSA/PSS";
    private Provider provider;
    private SecureRandom random;
    private AlgorithmIdentifier sigAlgId;
    private String signatureAlgorithm;

    private static class ContentSignerImpl implements ContentSigner {
        private AlgorithmIdentifier mAlgorithmIdentifier = null;
        private Signature mSignature = null;
        private SignatureOutputStream mStream = null;

        public ContentSignerImpl(Signature sign, AlgorithmIdentifier signatureAlgId) {
            this.mSignature = sign;
            this.mAlgorithmIdentifier = signatureAlgId;
            this.mStream = new SignatureOutputStream(this.mSignature);
        }

        public AlgorithmIdentifier getAlgorithmIdentifier() {
            return this.mAlgorithmIdentifier;
        }

        public OutputStream getOutputStream() {
            return this.mStream;
        }

        public byte[] getSignature() {
            try {
                return this.mStream.getSignature();
            } catch (SignatureException e) {
                throw new RuntimeOperatorException("exception obtaining signature: " + e.getMessage(), e);
            }
        }
    }

    private static class SignatureOutputStream extends OutputStream {
        private Signature sig;

        SignatureOutputStream(Signature sig2) {
            this.sig = sig2;
        }

        public void write(byte[] bytes, int off, int len) throws IOException {
            try {
                this.sig.update(bytes, off, len);
            } catch (SignatureException e) {
                throw new OperatorStreamException("exception in content signer: " + e.getMessage(), e);
            }
        }

        public void write(byte[] bytes) throws IOException {
            try {
                this.sig.update(bytes);
            } catch (SignatureException e) {
                throw new OperatorStreamException("exception in content signer: " + e.getMessage(), e);
            }
        }

        public void write(int b) throws IOException {
            try {
                this.sig.update((byte) b);
            } catch (SignatureException e) {
                throw new OperatorStreamException("exception in content signer: " + e.getMessage(), e);
            }
        }

        /* access modifiers changed from: package-private */
        public byte[] getSignature() throws SignatureException {
            return this.sig.sign();
        }
    }

    public JcaContentSignerExtensionBuilder(String signatureAlgorithm2) {
        this.signatureAlgorithm = signatureAlgorithm2;
        if (SHA256PSS.equals(signatureAlgorithm2)) {
            this.sigAlgId = new AlgorithmIdentifier(PKCSObjectIdentifiers.id_RSASSA_PSS);
        } else {
            this.sigAlgId = new DefaultSignatureAlgorithmIdentifierFinder().find(signatureAlgorithm2);
        }
    }

    public JcaContentSignerExtensionBuilder setProvider(Provider provider2) {
        this.provider = provider2;
        return this;
    }

    public JcaContentSignerExtensionBuilder setProvider(String provider2) {
        this.provider = Security.getProvider(provider2);
        return this;
    }

    public JcaContentSignerExtensionBuilder setSecureRandom(SecureRandom random2) {
        this.random = random2;
        return this;
    }

    public ContentSigner build(PrivateKey privateKey) throws OperatorCreationException {
        try {
            Signature sig = Signature.getInstance(this.signatureAlgorithm, this.provider);
            AlgorithmIdentifier signatureAlgId = this.sigAlgId;
            if (this.random != null) {
                sig.initSign(privateKey, this.random);
            } else {
                sig.initSign(privateKey);
            }
            return new ContentSignerImpl(sig, signatureAlgId);
        } catch (GeneralSecurityException e) {
            throw new OperatorCreationException("cannot create signer: " + e.getMessage(), e);
        }
    }
}

package com.huawei.security.hccm.common.bcextension;

import android.support.annotation.NonNull;
import java.io.IOException;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
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
    public static final String SHA256_PSS = "SHA256withRSA/PSS";
    private Provider mProvider;
    private SecureRandom mRandom;
    private AlgorithmIdentifier mSigAlgoId;
    private String mSignatureAlgorithm;

    public JcaContentSignerExtensionBuilder(String signatureAlgorithm) {
        this.mSignatureAlgorithm = signatureAlgorithm;
        if (SHA256_PSS.equals(signatureAlgorithm)) {
            this.mSigAlgoId = new AlgorithmIdentifier(PKCSObjectIdentifiers.id_RSASSA_PSS);
        } else {
            this.mSigAlgoId = new DefaultSignatureAlgorithmIdentifierFinder().find(signatureAlgorithm);
        }
    }

    public JcaContentSignerExtensionBuilder setProvider(Provider provider) {
        this.mProvider = provider;
        return this;
    }

    public JcaContentSignerExtensionBuilder setProvider(String provider) {
        this.mProvider = Security.getProvider(provider);
        return this;
    }

    public JcaContentSignerExtensionBuilder setSecureRandom(SecureRandom random) {
        this.mRandom = random;
        return this;
    }

    public ContentSigner build(PrivateKey privateKey) throws OperatorCreationException {
        try {
            Signature signature = Signature.getInstance(this.mSignatureAlgorithm, this.mProvider);
            AlgorithmIdentifier signatureAlgId = this.mSigAlgoId;
            if (this.mRandom != null) {
                signature.initSign(privateKey, this.mRandom);
            } else {
                signature.initSign(privateKey);
            }
            return new ContentSignerImpl(signature, signatureAlgId);
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            throw new OperatorCreationException("Cannot create signer: " + e.getMessage(), e);
        }
    }

    private static class ContentSignerImpl implements ContentSigner {
        private AlgorithmIdentifier mAlgorithmIdentifier = null;
        private Signature mSignature = null;
        private SignatureOutputStream mStream = null;

        ContentSignerImpl(Signature signature, AlgorithmIdentifier signatureAlgId) {
            this.mSignature = signature;
            this.mAlgorithmIdentifier = signatureAlgId;
            this.mStream = new SignatureOutputStream(this.mSignature);
        }

        @Override // org.bouncycastle.operator.ContentSigner
        public AlgorithmIdentifier getAlgorithmIdentifier() {
            return this.mAlgorithmIdentifier;
        }

        @Override // org.bouncycastle.operator.ContentSigner
        public OutputStream getOutputStream() {
            return this.mStream;
        }

        @Override // org.bouncycastle.operator.ContentSigner
        public byte[] getSignature() {
            try {
                return this.mStream.getSignature();
            } catch (SignatureException e) {
                throw new RuntimeOperatorException("exception obtaining signature: " + e.getMessage(), e);
            }
        }
    }

    private static class SignatureOutputStream extends OutputStream {
        private Signature mSignature;

        SignatureOutputStream(Signature signature) {
            this.mSignature = signature;
        }

        @Override // java.io.OutputStream
        public void write(@NonNull byte[] bytes, int off, int len) throws IOException {
            try {
                this.mSignature.update(bytes, off, len);
            } catch (SignatureException e) {
                throw new OperatorStreamException("exception in content signer: " + e.getMessage(), e);
            }
        }

        @Override // java.io.OutputStream
        public void write(@NonNull byte[] bytes) throws IOException {
            try {
                this.mSignature.update(bytes);
            } catch (SignatureException e) {
                throw new OperatorStreamException("exception in content signer: " + e.getMessage(), e);
            }
        }

        @Override // java.io.OutputStream
        public void write(int b) throws IOException {
            try {
                this.mSignature.update((byte) b);
            } catch (SignatureException e) {
                throw new OperatorStreamException("exception in content signer: " + e.getMessage(), e);
            }
        }

        /* access modifiers changed from: package-private */
        public byte[] getSignature() throws SignatureException {
            return this.mSignature.sign();
        }
    }
}

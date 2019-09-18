package org.bouncycastle.jcajce.provider.asymmetric.rsa;

import java.io.ByteArrayOutputStream;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.SignatureSpi;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.engines.RSABlindedEngine;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.crypto.signers.PSSSigner;
import org.bouncycastle.jcajce.provider.util.DigestFactory;
import org.bouncycastle.jcajce.util.BCJcaJceHelper;
import org.bouncycastle.jcajce.util.JcaJceHelper;
import org.bouncycastle.pqc.jcajce.spec.McElieceCCA2KeyGenParameterSpec;
import org.bouncycastle.pqc.jcajce.spec.SPHINCS256KeyGenParameterSpec;

public class PSSSignatureSpi extends SignatureSpi {
    private Digest contentDigest;
    private AlgorithmParameters engineParams;
    private final JcaJceHelper helper;
    private boolean isRaw;
    private Digest mgfDigest;
    private PSSParameterSpec originalSpec;
    private PSSParameterSpec paramSpec;
    private PSSSigner pss;
    private int saltLength;
    private AsymmetricBlockCipher signer;
    private byte trailer;

    private class NullPssDigest implements Digest {
        private ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        private Digest baseDigest;
        private boolean oddTime = true;

        public NullPssDigest(Digest digest) {
            this.baseDigest = digest;
        }

        public int doFinal(byte[] bArr, int i) {
            byte[] byteArray = this.bOut.toByteArray();
            if (this.oddTime) {
                System.arraycopy(byteArray, 0, bArr, i, byteArray.length);
            } else {
                this.baseDigest.update(byteArray, 0, byteArray.length);
                this.baseDigest.doFinal(bArr, i);
            }
            reset();
            this.oddTime = !this.oddTime;
            return byteArray.length;
        }

        public String getAlgorithmName() {
            return "NULL";
        }

        public int getByteLength() {
            return 0;
        }

        public int getDigestSize() {
            return this.baseDigest.getDigestSize();
        }

        public void reset() {
            this.bOut.reset();
            this.baseDigest.reset();
        }

        public void update(byte b) {
            this.bOut.write(b);
        }

        public void update(byte[] bArr, int i, int i2) {
            this.bOut.write(bArr, i, i2);
        }
    }

    public static class PSSwithRSA extends PSSSignatureSpi {
        public PSSwithRSA() {
            super(new RSABlindedEngine(), null);
        }
    }

    public static class SHA1withRSA extends PSSSignatureSpi {
        public SHA1withRSA() {
            super(new RSABlindedEngine(), PSSParameterSpec.DEFAULT);
        }
    }

    public static class SHA224withRSA extends PSSSignatureSpi {
        /* JADX WARNING: Illegal instructions before constructor call */
        public SHA224withRSA() {
            super(r0, r1);
            RSABlindedEngine rSABlindedEngine = new RSABlindedEngine();
            PSSParameterSpec pSSParameterSpec = new PSSParameterSpec(McElieceCCA2KeyGenParameterSpec.SHA224, "MGF1", new MGF1ParameterSpec(McElieceCCA2KeyGenParameterSpec.SHA224), 28, 1);
        }
    }

    public static class SHA256withRSA extends PSSSignatureSpi {
        /* JADX WARNING: Illegal instructions before constructor call */
        public SHA256withRSA() {
            super(r0, r1);
            RSABlindedEngine rSABlindedEngine = new RSABlindedEngine();
            PSSParameterSpec pSSParameterSpec = new PSSParameterSpec(McElieceCCA2KeyGenParameterSpec.SHA256, "MGF1", new MGF1ParameterSpec(McElieceCCA2KeyGenParameterSpec.SHA256), 32, 1);
        }
    }

    public static class SHA384withRSA extends PSSSignatureSpi {
        /* JADX WARNING: Illegal instructions before constructor call */
        public SHA384withRSA() {
            super(r0, r1);
            RSABlindedEngine rSABlindedEngine = new RSABlindedEngine();
            PSSParameterSpec pSSParameterSpec = new PSSParameterSpec(McElieceCCA2KeyGenParameterSpec.SHA384, "MGF1", new MGF1ParameterSpec(McElieceCCA2KeyGenParameterSpec.SHA384), 48, 1);
        }
    }

    public static class SHA3_224withRSA extends PSSSignatureSpi {
        /* JADX WARNING: Illegal instructions before constructor call */
        public SHA3_224withRSA() {
            super(r0, r1);
            RSABlindedEngine rSABlindedEngine = new RSABlindedEngine();
            PSSParameterSpec pSSParameterSpec = new PSSParameterSpec("SHA3-224", "MGF1", new MGF1ParameterSpec("SHA3-224"), 28, 1);
        }
    }

    public static class SHA3_256withRSA extends PSSSignatureSpi {
        /* JADX WARNING: Illegal instructions before constructor call */
        public SHA3_256withRSA() {
            super(r0, r1);
            RSABlindedEngine rSABlindedEngine = new RSABlindedEngine();
            PSSParameterSpec pSSParameterSpec = new PSSParameterSpec(SPHINCS256KeyGenParameterSpec.SHA3_256, "MGF1", new MGF1ParameterSpec(SPHINCS256KeyGenParameterSpec.SHA3_256), 32, 1);
        }
    }

    public static class SHA3_384withRSA extends PSSSignatureSpi {
        /* JADX WARNING: Illegal instructions before constructor call */
        public SHA3_384withRSA() {
            super(r0, r1);
            RSABlindedEngine rSABlindedEngine = new RSABlindedEngine();
            PSSParameterSpec pSSParameterSpec = new PSSParameterSpec("SHA3-384", "MGF1", new MGF1ParameterSpec("SHA3-384"), 48, 1);
        }
    }

    public static class SHA3_512withRSA extends PSSSignatureSpi {
        /* JADX WARNING: Illegal instructions before constructor call */
        public SHA3_512withRSA() {
            super(r0, r1);
            RSABlindedEngine rSABlindedEngine = new RSABlindedEngine();
            PSSParameterSpec pSSParameterSpec = new PSSParameterSpec("SHA3-512", "MGF1", new MGF1ParameterSpec("SHA3-512"), 64, 1);
        }
    }

    public static class SHA512_224withRSA extends PSSSignatureSpi {
        /* JADX WARNING: Illegal instructions before constructor call */
        public SHA512_224withRSA() {
            super(r0, r1);
            RSABlindedEngine rSABlindedEngine = new RSABlindedEngine();
            PSSParameterSpec pSSParameterSpec = new PSSParameterSpec("SHA-512(224)", "MGF1", new MGF1ParameterSpec("SHA-512(224)"), 28, 1);
        }
    }

    public static class SHA512_256withRSA extends PSSSignatureSpi {
        /* JADX WARNING: Illegal instructions before constructor call */
        public SHA512_256withRSA() {
            super(r0, r1);
            RSABlindedEngine rSABlindedEngine = new RSABlindedEngine();
            PSSParameterSpec pSSParameterSpec = new PSSParameterSpec("SHA-512(256)", "MGF1", new MGF1ParameterSpec("SHA-512(256)"), 32, 1);
        }
    }

    public static class SHA512withRSA extends PSSSignatureSpi {
        /* JADX WARNING: Illegal instructions before constructor call */
        public SHA512withRSA() {
            super(r0, r1);
            RSABlindedEngine rSABlindedEngine = new RSABlindedEngine();
            PSSParameterSpec pSSParameterSpec = new PSSParameterSpec(McElieceCCA2KeyGenParameterSpec.SHA512, "MGF1", new MGF1ParameterSpec(McElieceCCA2KeyGenParameterSpec.SHA512), 64, 1);
        }
    }

    public static class nonePSS extends PSSSignatureSpi {
        public nonePSS() {
            super(new RSABlindedEngine(), null, true);
        }
    }

    protected PSSSignatureSpi(AsymmetricBlockCipher asymmetricBlockCipher, PSSParameterSpec pSSParameterSpec) {
        this(asymmetricBlockCipher, pSSParameterSpec, false);
    }

    protected PSSSignatureSpi(AsymmetricBlockCipher asymmetricBlockCipher, PSSParameterSpec pSSParameterSpec, boolean z) {
        this.helper = new BCJcaJceHelper();
        this.signer = asymmetricBlockCipher;
        this.originalSpec = pSSParameterSpec;
        if (pSSParameterSpec == null) {
            this.paramSpec = PSSParameterSpec.DEFAULT;
        } else {
            this.paramSpec = pSSParameterSpec;
        }
        this.mgfDigest = DigestFactory.getDigest(this.paramSpec.getDigestAlgorithm());
        this.saltLength = this.paramSpec.getSaltLength();
        this.trailer = getTrailer(this.paramSpec.getTrailerField());
        this.isRaw = z;
        setupContentDigest();
    }

    private byte getTrailer(int i) {
        if (i == 1) {
            return PSSSigner.TRAILER_IMPLICIT;
        }
        throw new IllegalArgumentException("unknown trailer field");
    }

    private void setupContentDigest() {
        this.contentDigest = this.isRaw ? new NullPssDigest(this.mgfDigest) : this.mgfDigest;
    }

    /* access modifiers changed from: protected */
    public Object engineGetParameter(String str) {
        throw new UnsupportedOperationException("engineGetParameter unsupported");
    }

    /* access modifiers changed from: protected */
    public AlgorithmParameters engineGetParameters() {
        if (this.engineParams == null && this.paramSpec != null) {
            try {
                this.engineParams = this.helper.createAlgorithmParameters("PSS");
                this.engineParams.init(this.paramSpec);
            } catch (Exception e) {
                throw new RuntimeException(e.toString());
            }
        }
        return this.engineParams;
    }

    /* access modifiers changed from: protected */
    public void engineInitSign(PrivateKey privateKey) throws InvalidKeyException {
        if (privateKey instanceof RSAPrivateKey) {
            PSSSigner pSSSigner = new PSSSigner(this.signer, this.contentDigest, this.mgfDigest, this.saltLength, this.trailer);
            this.pss = pSSSigner;
            this.pss.init(true, RSAUtil.generatePrivateKeyParameter((RSAPrivateKey) privateKey));
            return;
        }
        throw new InvalidKeyException("Supplied key is not a RSAPrivateKey instance");
    }

    /* access modifiers changed from: protected */
    public void engineInitSign(PrivateKey privateKey, SecureRandom secureRandom) throws InvalidKeyException {
        if (privateKey instanceof RSAPrivateKey) {
            PSSSigner pSSSigner = new PSSSigner(this.signer, this.contentDigest, this.mgfDigest, this.saltLength, this.trailer);
            this.pss = pSSSigner;
            this.pss.init(true, new ParametersWithRandom(RSAUtil.generatePrivateKeyParameter((RSAPrivateKey) privateKey), secureRandom));
            return;
        }
        throw new InvalidKeyException("Supplied key is not a RSAPrivateKey instance");
    }

    /* access modifiers changed from: protected */
    public void engineInitVerify(PublicKey publicKey) throws InvalidKeyException {
        if (publicKey instanceof RSAPublicKey) {
            PSSSigner pSSSigner = new PSSSigner(this.signer, this.contentDigest, this.mgfDigest, this.saltLength, this.trailer);
            this.pss = pSSSigner;
            this.pss.init(false, RSAUtil.generatePublicKeyParameter((RSAPublicKey) publicKey));
            return;
        }
        throw new InvalidKeyException("Supplied key is not a RSAPublicKey instance");
    }

    /* access modifiers changed from: protected */
    public void engineSetParameter(String str, Object obj) {
        throw new UnsupportedOperationException("engineSetParameter unsupported");
    }

    /* access modifiers changed from: protected */
    public void engineSetParameter(AlgorithmParameterSpec algorithmParameterSpec) throws InvalidAlgorithmParameterException {
        if (algorithmParameterSpec instanceof PSSParameterSpec) {
            PSSParameterSpec pSSParameterSpec = (PSSParameterSpec) algorithmParameterSpec;
            if (this.originalSpec != null && !DigestFactory.isSameDigest(this.originalSpec.getDigestAlgorithm(), pSSParameterSpec.getDigestAlgorithm())) {
                throw new InvalidAlgorithmParameterException("parameter must be using " + this.originalSpec.getDigestAlgorithm());
            } else if (!pSSParameterSpec.getMGFAlgorithm().equalsIgnoreCase("MGF1") && !pSSParameterSpec.getMGFAlgorithm().equals(PKCSObjectIdentifiers.id_mgf1.getId())) {
                throw new InvalidAlgorithmParameterException("unknown mask generation function specified");
            } else if (pSSParameterSpec.getMGFParameters() instanceof MGF1ParameterSpec) {
                MGF1ParameterSpec mGF1ParameterSpec = (MGF1ParameterSpec) pSSParameterSpec.getMGFParameters();
                if (DigestFactory.isSameDigest(mGF1ParameterSpec.getDigestAlgorithm(), pSSParameterSpec.getDigestAlgorithm())) {
                    Digest digest = DigestFactory.getDigest(mGF1ParameterSpec.getDigestAlgorithm());
                    if (digest != null) {
                        this.engineParams = null;
                        this.paramSpec = pSSParameterSpec;
                        this.mgfDigest = digest;
                        this.saltLength = this.paramSpec.getSaltLength();
                        this.trailer = getTrailer(this.paramSpec.getTrailerField());
                        setupContentDigest();
                        return;
                    }
                    throw new InvalidAlgorithmParameterException("no match on MGF digest algorithm: " + mGF1ParameterSpec.getDigestAlgorithm());
                }
                throw new InvalidAlgorithmParameterException("digest algorithm for MGF should be the same as for PSS parameters.");
            } else {
                throw new InvalidAlgorithmParameterException("unknown MGF parameters");
            }
        } else {
            throw new InvalidAlgorithmParameterException("Only PSSParameterSpec supported");
        }
    }

    /* access modifiers changed from: protected */
    public byte[] engineSign() throws SignatureException {
        try {
            return this.pss.generateSignature();
        } catch (CryptoException e) {
            throw new SignatureException(e.getMessage());
        }
    }

    /* access modifiers changed from: protected */
    public void engineUpdate(byte b) throws SignatureException {
        this.pss.update(b);
    }

    /* access modifiers changed from: protected */
    public void engineUpdate(byte[] bArr, int i, int i2) throws SignatureException {
        this.pss.update(bArr, i, i2);
    }

    /* access modifiers changed from: protected */
    public boolean engineVerify(byte[] bArr) throws SignatureException {
        return this.pss.verifySignature(bArr);
    }
}

package com.android.org.conscrypt;

import com.android.org.conscrypt.EvpMdRef;
import com.android.org.conscrypt.NativeRef;
import java.nio.ByteBuffer;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.ProviderException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.SignatureSpi;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;

public class OpenSSLSignature extends SignatureSpi {
    private NativeRef.EVP_MD_CTX ctx;
    private final EngineType engineType;
    private final long evpMdRef;
    private long evpPkeyCtx;
    private OpenSSLKey key;
    private boolean signing;
    private final byte[] singleByte;

    private enum EngineType {
        RSA,
        EC
    }

    public static final class MD5RSA extends RSAPKCS1Padding {
        public MD5RSA() {
            super(EvpMdRef.MD5.EVP_MD);
        }
    }

    static abstract class RSAPKCS1Padding extends OpenSSLSignature {
        RSAPKCS1Padding(long evpMdRef) {
            super(evpMdRef, EngineType.RSA);
        }

        /* access modifiers changed from: protected */
        public final void configureEVP_PKEY_CTX(long ctx) throws InvalidAlgorithmParameterException {
            NativeCrypto.EVP_PKEY_CTX_set_rsa_padding(ctx, 1);
        }
    }

    static abstract class RSAPSSPadding extends OpenSSLSignature {
        private static final int TRAILER_FIELD_BC_ID = 1;
        private final String contentDigestAlgorithm;
        private String mgf1DigestAlgorithm;
        private long mgf1EvpMdRef;
        private int saltSizeBytes;

        RSAPSSPadding(long contentDigestEvpMdRef, String contentDigestAlgorithm2, int saltSizeBytes2) {
            super(contentDigestEvpMdRef, EngineType.RSA);
            this.contentDigestAlgorithm = contentDigestAlgorithm2;
            this.mgf1DigestAlgorithm = contentDigestAlgorithm2;
            this.mgf1EvpMdRef = contentDigestEvpMdRef;
            this.saltSizeBytes = saltSizeBytes2;
        }

        /* access modifiers changed from: protected */
        public final void configureEVP_PKEY_CTX(long ctx) throws InvalidAlgorithmParameterException {
            NativeCrypto.EVP_PKEY_CTX_set_rsa_padding(ctx, 6);
            NativeCrypto.EVP_PKEY_CTX_set_rsa_mgf1_md(ctx, this.mgf1EvpMdRef);
            NativeCrypto.EVP_PKEY_CTX_set_rsa_pss_saltlen(ctx, this.saltSizeBytes);
        }

        /* access modifiers changed from: protected */
        public final void engineSetParameter(AlgorithmParameterSpec params) throws InvalidAlgorithmParameterException {
            if (params instanceof PSSParameterSpec) {
                PSSParameterSpec spec = (PSSParameterSpec) params;
                String specContentDigest = EvpMdRef.getJcaDigestAlgorithmStandardName(spec.getDigestAlgorithm());
                if (specContentDigest == null) {
                    throw new InvalidAlgorithmParameterException("Unsupported content digest algorithm: " + spec.getDigestAlgorithm());
                } else if (this.contentDigestAlgorithm.equalsIgnoreCase(specContentDigest)) {
                    String specMgfAlgorithm = spec.getMGFAlgorithm();
                    if ("MGF1".equalsIgnoreCase(specMgfAlgorithm) || "1.2.840.113549.1.1.8".equals(specMgfAlgorithm)) {
                        AlgorithmParameterSpec mgfSpec = spec.getMGFParameters();
                        if (mgfSpec instanceof MGF1ParameterSpec) {
                            MGF1ParameterSpec specMgf1Spec = (MGF1ParameterSpec) spec.getMGFParameters();
                            String specMgf1Digest = EvpMdRef.getJcaDigestAlgorithmStandardName(specMgf1Spec.getDigestAlgorithm());
                            if (specMgf1Digest != null) {
                                try {
                                    long specMgf1EvpMdRef = EvpMdRef.getEVP_MDByJcaDigestAlgorithmStandardName(specMgf1Digest);
                                    int specSaltSizeBytes = spec.getSaltLength();
                                    if (specSaltSizeBytes >= 0) {
                                        int specTrailer = spec.getTrailerField();
                                        if (specTrailer == 1) {
                                            this.mgf1DigestAlgorithm = specMgf1Digest;
                                            this.mgf1EvpMdRef = specMgf1EvpMdRef;
                                            this.saltSizeBytes = specSaltSizeBytes;
                                            long ctx = getEVP_PKEY_CTX();
                                            if (ctx != 0) {
                                                configureEVP_PKEY_CTX(ctx);
                                                return;
                                            }
                                            return;
                                        }
                                        throw new InvalidAlgorithmParameterException("Unsupported trailer field: " + specTrailer + ". Only " + 1 + " supported");
                                    }
                                    throw new InvalidAlgorithmParameterException("Salt length must be non-negative: " + specSaltSizeBytes);
                                } catch (NoSuchAlgorithmException e) {
                                    throw new ProviderException("Failed to obtain EVP_MD for " + specMgf1Digest, e);
                                }
                            } else {
                                throw new InvalidAlgorithmParameterException("Unsupported MGF1 digest algorithm: " + specMgf1Spec.getDigestAlgorithm());
                            }
                        } else {
                            throw new InvalidAlgorithmParameterException("Unsupported MGF parameters: " + mgfSpec + ". Only " + MGF1ParameterSpec.class.getName() + " supported");
                        }
                    } else {
                        throw new InvalidAlgorithmParameterException("Unsupported MGF algorithm: " + specMgfAlgorithm + ". Only " + "MGF1" + " supported");
                    }
                } else {
                    throw new InvalidAlgorithmParameterException("Changing content digest algorithm not supported");
                }
            } else {
                throw new InvalidAlgorithmParameterException("Unsupported parameter: " + params + ". Only " + PSSParameterSpec.class.getName() + " supported");
            }
        }

        /* access modifiers changed from: protected */
        public final AlgorithmParameters engineGetParameters() {
            try {
                AlgorithmParameters result = AlgorithmParameters.getInstance("PSS");
                PSSParameterSpec pSSParameterSpec = new PSSParameterSpec(this.contentDigestAlgorithm, "MGF1", new MGF1ParameterSpec(this.mgf1DigestAlgorithm), this.saltSizeBytes, 1);
                result.init(pSSParameterSpec);
                return result;
            } catch (NoSuchAlgorithmException e) {
                throw new ProviderException("Failed to create PSS AlgorithmParameters", e);
            } catch (InvalidParameterSpecException e2) {
                throw new ProviderException("Failed to create PSS AlgorithmParameters", e2);
            }
        }
    }

    public static final class SHA1ECDSA extends OpenSSLSignature {
        public SHA1ECDSA() {
            super(EvpMdRef.SHA1.EVP_MD, EngineType.EC);
        }
    }

    public static final class SHA1RSA extends RSAPKCS1Padding {
        public SHA1RSA() {
            super(EvpMdRef.SHA1.EVP_MD);
        }
    }

    public static final class SHA1RSAPSS extends RSAPSSPadding {
        public SHA1RSAPSS() {
            super(EvpMdRef.SHA1.EVP_MD, "SHA-1", EvpMdRef.SHA1.SIZE_BYTES);
        }
    }

    public static final class SHA224ECDSA extends OpenSSLSignature {
        public SHA224ECDSA() {
            super(EvpMdRef.SHA224.EVP_MD, EngineType.EC);
        }
    }

    public static final class SHA224RSA extends RSAPKCS1Padding {
        public SHA224RSA() {
            super(EvpMdRef.SHA224.EVP_MD);
        }
    }

    public static final class SHA224RSAPSS extends RSAPSSPadding {
        public SHA224RSAPSS() {
            super(EvpMdRef.SHA224.EVP_MD, "SHA-224", EvpMdRef.SHA224.SIZE_BYTES);
        }
    }

    public static final class SHA256ECDSA extends OpenSSLSignature {
        public SHA256ECDSA() {
            super(EvpMdRef.SHA256.EVP_MD, EngineType.EC);
        }
    }

    public static final class SHA256RSA extends RSAPKCS1Padding {
        public SHA256RSA() {
            super(EvpMdRef.SHA256.EVP_MD);
        }
    }

    public static final class SHA256RSAPSS extends RSAPSSPadding {
        public SHA256RSAPSS() {
            super(EvpMdRef.SHA256.EVP_MD, "SHA-256", EvpMdRef.SHA256.SIZE_BYTES);
        }
    }

    public static final class SHA384ECDSA extends OpenSSLSignature {
        public SHA384ECDSA() {
            super(EvpMdRef.SHA384.EVP_MD, EngineType.EC);
        }
    }

    public static final class SHA384RSA extends RSAPKCS1Padding {
        public SHA384RSA() {
            super(EvpMdRef.SHA384.EVP_MD);
        }
    }

    public static final class SHA384RSAPSS extends RSAPSSPadding {
        public SHA384RSAPSS() {
            super(EvpMdRef.SHA384.EVP_MD, "SHA-384", EvpMdRef.SHA384.SIZE_BYTES);
        }
    }

    public static final class SHA512ECDSA extends OpenSSLSignature {
        public SHA512ECDSA() {
            super(EvpMdRef.SHA512.EVP_MD, EngineType.EC);
        }
    }

    public static final class SHA512RSA extends RSAPKCS1Padding {
        public SHA512RSA() {
            super(EvpMdRef.SHA512.EVP_MD);
        }
    }

    public static final class SHA512RSAPSS extends RSAPSSPadding {
        public SHA512RSAPSS() {
            super(EvpMdRef.SHA512.EVP_MD, "SHA-512", EvpMdRef.SHA512.SIZE_BYTES);
        }
    }

    private OpenSSLSignature(long evpMdRef2, EngineType engineType2) {
        this.singleByte = new byte[1];
        this.engineType = engineType2;
        this.evpMdRef = evpMdRef2;
    }

    private void resetContext() throws InvalidAlgorithmParameterException {
        NativeRef.EVP_MD_CTX ctxLocal = new NativeRef.EVP_MD_CTX(NativeCrypto.EVP_MD_CTX_create());
        if (this.signing) {
            this.evpPkeyCtx = NativeCrypto.EVP_DigestSignInit(ctxLocal, this.evpMdRef, this.key.getNativeRef());
        } else {
            this.evpPkeyCtx = NativeCrypto.EVP_DigestVerifyInit(ctxLocal, this.evpMdRef, this.key.getNativeRef());
        }
        configureEVP_PKEY_CTX(this.evpPkeyCtx);
        this.ctx = ctxLocal;
    }

    /* access modifiers changed from: protected */
    public void configureEVP_PKEY_CTX(long ctx2) throws InvalidAlgorithmParameterException {
    }

    /* access modifiers changed from: protected */
    public void engineUpdate(byte input) {
        this.singleByte[0] = input;
        engineUpdate(this.singleByte, 0, 1);
    }

    /* access modifiers changed from: protected */
    public void engineUpdate(byte[] input, int offset, int len) {
        NativeRef.EVP_MD_CTX ctxLocal = this.ctx;
        if (this.signing) {
            NativeCrypto.EVP_DigestSignUpdate(ctxLocal, input, offset, len);
        } else {
            NativeCrypto.EVP_DigestVerifyUpdate(ctxLocal, input, offset, len);
        }
    }

    /* access modifiers changed from: protected */
    public void engineUpdate(ByteBuffer input) {
        if (input.hasRemaining()) {
            if (!input.isDirect()) {
                super.engineUpdate(input);
                return;
            }
            long baseAddress = NativeCrypto.getDirectBufferAddress(input);
            if (baseAddress == 0) {
                super.engineUpdate(input);
                return;
            }
            int position = input.position();
            if (position >= 0) {
                long ptr = ((long) position) + baseAddress;
                int len = input.remaining();
                if (len >= 0) {
                    NativeRef.EVP_MD_CTX ctxLocal = this.ctx;
                    if (this.signing) {
                        NativeCrypto.EVP_DigestSignUpdateDirect(ctxLocal, ptr, len);
                    } else {
                        NativeCrypto.EVP_DigestVerifyUpdateDirect(ctxLocal, ptr, len);
                    }
                    input.position(position + len);
                    return;
                }
                throw new RuntimeException("Negative remaining amount");
            }
            throw new RuntimeException("Negative position");
        }
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public Object engineGetParameter(String param) throws InvalidParameterException {
        return null;
    }

    private void checkEngineType(OpenSSLKey pkey) throws InvalidKeyException {
        int pkeyType = NativeCrypto.EVP_PKEY_type(pkey.getNativeRef());
        switch (this.engineType) {
            case RSA:
                if (pkeyType != 6) {
                    throw new InvalidKeyException("Signature initialized as " + this.engineType + " (not RSA)");
                }
                return;
            case EC:
                if (pkeyType != 408) {
                    throw new InvalidKeyException("Signature initialized as " + this.engineType + " (not EC)");
                }
                return;
            default:
                throw new InvalidKeyException("Key must be of type " + this.engineType);
        }
    }

    private void initInternal(OpenSSLKey newKey, boolean signing2) throws InvalidKeyException {
        checkEngineType(newKey);
        this.key = newKey;
        this.signing = signing2;
        try {
            resetContext();
        } catch (InvalidAlgorithmParameterException e) {
            throw new InvalidKeyException(e);
        }
    }

    /* access modifiers changed from: protected */
    public void engineInitSign(PrivateKey privateKey) throws InvalidKeyException {
        initInternal(OpenSSLKey.fromPrivateKey(privateKey), true);
    }

    /* access modifiers changed from: protected */
    public void engineInitVerify(PublicKey publicKey) throws InvalidKeyException {
        initInternal(OpenSSLKey.fromPublicKey(publicKey), false);
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public void engineSetParameter(String param, Object value) throws InvalidParameterException {
    }

    /* access modifiers changed from: protected */
    public byte[] engineSign() throws SignatureException {
        try {
            byte[] EVP_DigestSignFinal = NativeCrypto.EVP_DigestSignFinal(this.ctx);
            try {
                resetContext();
                return EVP_DigestSignFinal;
            } catch (InvalidAlgorithmParameterException e) {
                throw new AssertionError("Reset of context failed after it was successful once");
            }
        } catch (Exception ex) {
            throw new SignatureException(ex);
        } catch (Throwable e2) {
            try {
                resetContext();
                throw e2;
            } catch (InvalidAlgorithmParameterException e3) {
                throw new AssertionError("Reset of context failed after it was successful once");
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean engineVerify(byte[] sigBytes) throws SignatureException {
        try {
            boolean EVP_DigestVerifyFinal = NativeCrypto.EVP_DigestVerifyFinal(this.ctx, sigBytes, 0, sigBytes.length);
            try {
                resetContext();
                return EVP_DigestVerifyFinal;
            } catch (InvalidAlgorithmParameterException e) {
                throw new AssertionError("Reset of context failed after it was successful once");
            }
        } catch (Exception ex) {
            throw new SignatureException(ex);
        } catch (Throwable e2) {
            try {
                resetContext();
                throw e2;
            } catch (InvalidAlgorithmParameterException e3) {
                throw new AssertionError("Reset of context failed after it was successful once");
            }
        }
    }

    /* access modifiers changed from: protected */
    public final long getEVP_PKEY_CTX() {
        return this.evpPkeyCtx;
    }
}

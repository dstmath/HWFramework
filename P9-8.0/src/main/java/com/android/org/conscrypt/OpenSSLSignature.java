package com.android.org.conscrypt;

import com.android.org.conscrypt.EvpMdRef.MD5;
import com.android.org.conscrypt.EvpMdRef.SHA1;
import com.android.org.conscrypt.EvpMdRef.SHA224;
import com.android.org.conscrypt.EvpMdRef.SHA256;
import com.android.org.conscrypt.EvpMdRef.SHA384;
import com.android.org.conscrypt.EvpMdRef.SHA512;
import com.android.org.conscrypt.NativeRef.EVP_MD_CTX;
import java.nio.ByteBuffer;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
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
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;

public class OpenSSLSignature extends SignatureSpi {
    private static final /* synthetic */ int[] -com-android-org-conscrypt-OpenSSLSignature$EngineTypeSwitchesValues = null;
    private EVP_MD_CTX ctx;
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

    static abstract class RSAPKCS1Padding extends OpenSSLSignature {
        RSAPKCS1Padding(long evpMdRef) {
            super(evpMdRef, EngineType.RSA, null);
        }

        protected final void configureEVP_PKEY_CTX(long ctx) throws InvalidAlgorithmParameterException {
            NativeCrypto.EVP_PKEY_CTX_set_rsa_padding(ctx, 1);
        }
    }

    public static final class MD5RSA extends RSAPKCS1Padding {
        public MD5RSA() {
            super(MD5.EVP_MD);
        }
    }

    static abstract class RSAPSSPadding extends OpenSSLSignature {
        private static final int TRAILER_FIELD_BC_ID = 1;
        private final String contentDigestAlgorithm;
        private String mgf1DigestAlgorithm;
        private long mgf1EvpMdRef;
        private int saltSizeBytes;

        public RSAPSSPadding(long contentDigestEvpMdRef, String contentDigestAlgorithm, int saltSizeBytes) {
            super(contentDigestEvpMdRef, EngineType.RSA, null);
            this.contentDigestAlgorithm = contentDigestAlgorithm;
            this.mgf1DigestAlgorithm = contentDigestAlgorithm;
            this.mgf1EvpMdRef = contentDigestEvpMdRef;
            this.saltSizeBytes = saltSizeBytes;
        }

        protected final void configureEVP_PKEY_CTX(long ctx) throws InvalidAlgorithmParameterException {
            NativeCrypto.EVP_PKEY_CTX_set_rsa_padding(ctx, 6);
            NativeCrypto.EVP_PKEY_CTX_set_rsa_mgf1_md(ctx, this.mgf1EvpMdRef);
            NativeCrypto.EVP_PKEY_CTX_set_rsa_pss_saltlen(ctx, this.saltSizeBytes);
        }

        protected final void engineSetParameter(AlgorithmParameterSpec params) throws InvalidAlgorithmParameterException {
            if (params instanceof PSSParameterSpec) {
                PSSParameterSpec spec = (PSSParameterSpec) params;
                String specContentDigest = EvpMdRef.getJcaDigestAlgorithmStandardName(spec.getDigestAlgorithm());
                if (specContentDigest == null) {
                    throw new InvalidAlgorithmParameterException("Unsupported content digest algorithm: " + spec.getDigestAlgorithm());
                } else if (this.contentDigestAlgorithm.equalsIgnoreCase(specContentDigest)) {
                    String specMgfAlgorithm = spec.getMGFAlgorithm();
                    if (EvpMdRef.MGF1_ALGORITHM_NAME.equalsIgnoreCase(specMgfAlgorithm) || (EvpMdRef.MGF1_OID.equals(specMgfAlgorithm) ^ 1) == 0) {
                        AlgorithmParameterSpec mgfSpec = spec.getMGFParameters();
                        if (mgfSpec instanceof MGF1ParameterSpec) {
                            MGF1ParameterSpec specMgf1Spec = (MGF1ParameterSpec) spec.getMGFParameters();
                            String specMgf1Digest = EvpMdRef.getJcaDigestAlgorithmStandardName(specMgf1Spec.getDigestAlgorithm());
                            if (specMgf1Digest == null) {
                                throw new InvalidAlgorithmParameterException("Unsupported MGF1 digest algorithm: " + specMgf1Spec.getDigestAlgorithm());
                            }
                            try {
                                long specMgf1EvpMdRef = EvpMdRef.getEVP_MDByJcaDigestAlgorithmStandardName(specMgf1Digest);
                                int specSaltSizeBytes = spec.getSaltLength();
                                if (specSaltSizeBytes < 0) {
                                    throw new InvalidAlgorithmParameterException("Salt length must be non-negative: " + specSaltSizeBytes);
                                }
                                int specTrailer = spec.getTrailerField();
                                if (specTrailer != 1) {
                                    throw new InvalidAlgorithmParameterException("Unsupported trailer field: " + specTrailer + ". Only " + 1 + " supported");
                                }
                                this.mgf1DigestAlgorithm = specMgf1Digest;
                                this.mgf1EvpMdRef = specMgf1EvpMdRef;
                                this.saltSizeBytes = specSaltSizeBytes;
                                long ctx = getEVP_PKEY_CTX();
                                if (ctx != 0) {
                                    configureEVP_PKEY_CTX(ctx);
                                    return;
                                }
                                return;
                            } catch (NoSuchAlgorithmException e) {
                                throw new ProviderException("Failed to obtain EVP_MD for " + specMgf1Digest, e);
                            }
                        }
                        throw new InvalidAlgorithmParameterException("Unsupported MGF parameters: " + mgfSpec + ". Only " + MGF1ParameterSpec.class.getName() + " supported");
                    }
                    throw new InvalidAlgorithmParameterException("Unsupported MGF algorithm: " + specMgfAlgorithm + ". Only " + EvpMdRef.MGF1_ALGORITHM_NAME + " supported");
                } else {
                    throw new InvalidAlgorithmParameterException("Changing content digest algorithm not supported");
                }
            }
            throw new InvalidAlgorithmParameterException("Unsupported parameter: " + params + ". Only " + PSSParameterSpec.class.getName() + " supported");
        }

        /* JADX WARNING: Removed duplicated region for block: B:3:0x001f A:{ExcHandler: java.security.NoSuchAlgorithmException (r6_0 'e' java.security.GeneralSecurityException), Splitter: B:0:0x0000} */
        /* JADX WARNING: Missing block: B:3:0x001f, code:
            r6 = move-exception;
     */
        /* JADX WARNING: Missing block: B:5:0x0028, code:
            throw new java.security.ProviderException("Failed to create PSS AlgorithmParameters", r6);
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        protected final AlgorithmParameters engineGetParameters() {
            try {
                AlgorithmParameters result = AlgorithmParameters.getInstance("PSS");
                result.init(new PSSParameterSpec(this.contentDigestAlgorithm, EvpMdRef.MGF1_ALGORITHM_NAME, new MGF1ParameterSpec(this.mgf1DigestAlgorithm), this.saltSizeBytes, 1));
                return result;
            } catch (GeneralSecurityException e) {
            }
        }
    }

    public static final class SHA1ECDSA extends OpenSSLSignature {
        public SHA1ECDSA() {
            super(SHA1.EVP_MD, EngineType.EC, null);
        }
    }

    public static final class SHA1RSA extends RSAPKCS1Padding {
        public SHA1RSA() {
            super(SHA1.EVP_MD);
        }
    }

    public static final class SHA1RSAPSS extends RSAPSSPadding {
        public SHA1RSAPSS() {
            super(SHA1.EVP_MD, SHA1.JCA_NAME, SHA1.SIZE_BYTES);
        }
    }

    public static final class SHA224ECDSA extends OpenSSLSignature {
        public SHA224ECDSA() {
            super(SHA224.EVP_MD, EngineType.EC, null);
        }
    }

    public static final class SHA224RSA extends RSAPKCS1Padding {
        public SHA224RSA() {
            super(SHA224.EVP_MD);
        }
    }

    public static final class SHA224RSAPSS extends RSAPSSPadding {
        public SHA224RSAPSS() {
            super(SHA224.EVP_MD, SHA224.JCA_NAME, SHA224.SIZE_BYTES);
        }
    }

    public static final class SHA256ECDSA extends OpenSSLSignature {
        public SHA256ECDSA() {
            super(SHA256.EVP_MD, EngineType.EC, null);
        }
    }

    public static final class SHA256RSA extends RSAPKCS1Padding {
        public SHA256RSA() {
            super(SHA256.EVP_MD);
        }
    }

    public static final class SHA256RSAPSS extends RSAPSSPadding {
        public SHA256RSAPSS() {
            super(SHA256.EVP_MD, SHA256.JCA_NAME, SHA256.SIZE_BYTES);
        }
    }

    public static final class SHA384ECDSA extends OpenSSLSignature {
        public SHA384ECDSA() {
            super(SHA384.EVP_MD, EngineType.EC, null);
        }
    }

    public static final class SHA384RSA extends RSAPKCS1Padding {
        public SHA384RSA() {
            super(SHA384.EVP_MD);
        }
    }

    public static final class SHA384RSAPSS extends RSAPSSPadding {
        public SHA384RSAPSS() {
            super(SHA384.EVP_MD, SHA384.JCA_NAME, SHA384.SIZE_BYTES);
        }
    }

    public static final class SHA512ECDSA extends OpenSSLSignature {
        public SHA512ECDSA() {
            super(SHA512.EVP_MD, EngineType.EC, null);
        }
    }

    public static final class SHA512RSA extends RSAPKCS1Padding {
        public SHA512RSA() {
            super(SHA512.EVP_MD);
        }
    }

    public static final class SHA512RSAPSS extends RSAPSSPadding {
        public SHA512RSAPSS() {
            super(SHA512.EVP_MD, SHA512.JCA_NAME, SHA512.SIZE_BYTES);
        }
    }

    private static /* synthetic */ int[] -getcom-android-org-conscrypt-OpenSSLSignature$EngineTypeSwitchesValues() {
        if (-com-android-org-conscrypt-OpenSSLSignature$EngineTypeSwitchesValues != null) {
            return -com-android-org-conscrypt-OpenSSLSignature$EngineTypeSwitchesValues;
        }
        int[] iArr = new int[EngineType.values().length];
        try {
            iArr[EngineType.EC.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[EngineType.RSA.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        -com-android-org-conscrypt-OpenSSLSignature$EngineTypeSwitchesValues = iArr;
        return iArr;
    }

    /* synthetic */ OpenSSLSignature(long evpMdRef, EngineType engineType, OpenSSLSignature -this2) {
        this(evpMdRef, engineType);
    }

    private OpenSSLSignature(long evpMdRef, EngineType engineType) {
        this.singleByte = new byte[1];
        this.engineType = engineType;
        this.evpMdRef = evpMdRef;
    }

    private final void resetContext() throws InvalidAlgorithmParameterException {
        EVP_MD_CTX ctxLocal = new EVP_MD_CTX(NativeCrypto.EVP_MD_CTX_create());
        if (this.signing) {
            this.evpPkeyCtx = NativeCrypto.EVP_DigestSignInit(ctxLocal, this.evpMdRef, this.key.getNativeRef());
        } else {
            this.evpPkeyCtx = NativeCrypto.EVP_DigestVerifyInit(ctxLocal, this.evpMdRef, this.key.getNativeRef());
        }
        configureEVP_PKEY_CTX(this.evpPkeyCtx);
        this.ctx = ctxLocal;
    }

    protected void configureEVP_PKEY_CTX(long ctx) throws InvalidAlgorithmParameterException {
    }

    protected void engineUpdate(byte input) {
        this.singleByte[0] = input;
        engineUpdate(this.singleByte, 0, 1);
    }

    protected void engineUpdate(byte[] input, int offset, int len) {
        EVP_MD_CTX ctxLocal = this.ctx;
        if (this.signing) {
            NativeCrypto.EVP_DigestSignUpdate(ctxLocal, input, offset, len);
        } else {
            NativeCrypto.EVP_DigestVerifyUpdate(ctxLocal, input, offset, len);
        }
    }

    protected void engineUpdate(ByteBuffer input) {
        if (!input.hasRemaining()) {
            return;
        }
        if (input.isDirect()) {
            long baseAddress = NativeCrypto.getDirectBufferAddress(input);
            if (baseAddress == 0) {
                super.engineUpdate(input);
                return;
            }
            int position = input.position();
            if (position < 0) {
                throw new RuntimeException("Negative position");
            }
            long ptr = baseAddress + ((long) position);
            int len = input.remaining();
            if (len < 0) {
                throw new RuntimeException("Negative remaining amount");
            }
            EVP_MD_CTX ctxLocal = this.ctx;
            if (this.signing) {
                NativeCrypto.EVP_DigestSignUpdateDirect(ctxLocal, ptr, len);
            } else {
                NativeCrypto.EVP_DigestVerifyUpdateDirect(ctxLocal, ptr, len);
            }
            input.position(position + len);
            return;
        }
        super.engineUpdate(input);
    }

    @Deprecated
    protected Object engineGetParameter(String param) throws InvalidParameterException {
        return null;
    }

    private void checkEngineType(OpenSSLKey pkey) throws InvalidKeyException {
        int pkeyType = NativeCrypto.EVP_PKEY_type(pkey.getNativeRef());
        switch (-getcom-android-org-conscrypt-OpenSSLSignature$EngineTypeSwitchesValues()[this.engineType.ordinal()]) {
            case 1:
                if (pkeyType != NativeConstants.EVP_PKEY_EC) {
                    throw new InvalidKeyException("Signature initialized as " + this.engineType + " (not EC)");
                }
                return;
            case 2:
                if (pkeyType != 6) {
                    throw new InvalidKeyException("Signature initialized as " + this.engineType + " (not RSA)");
                }
                return;
            default:
                throw new InvalidKeyException("Key must be of type " + this.engineType);
        }
    }

    private void initInternal(OpenSSLKey newKey, boolean signing) throws InvalidKeyException {
        checkEngineType(newKey);
        this.key = newKey;
        this.signing = signing;
        try {
            resetContext();
        } catch (InvalidAlgorithmParameterException e) {
            throw new InvalidKeyException(e);
        }
    }

    protected void engineInitSign(PrivateKey privateKey) throws InvalidKeyException {
        initInternal(OpenSSLKey.fromPrivateKey(privateKey), true);
    }

    protected void engineInitVerify(PublicKey publicKey) throws InvalidKeyException {
        initInternal(OpenSSLKey.fromPublicKey(publicKey), false);
    }

    @Deprecated
    protected void engineSetParameter(String param, Object value) throws InvalidParameterException {
    }

    protected byte[] engineSign() throws SignatureException {
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
        } catch (Throwable th) {
            try {
                resetContext();
            } catch (InvalidAlgorithmParameterException e2) {
                throw new AssertionError("Reset of context failed after it was successful once");
            }
        }
    }

    protected boolean engineVerify(byte[] sigBytes) throws SignatureException {
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
        } catch (Throwable th) {
            try {
                resetContext();
            } catch (InvalidAlgorithmParameterException e2) {
                throw new AssertionError("Reset of context failed after it was successful once");
            }
        }
    }

    protected final long getEVP_PKEY_CTX() {
        return this.evpPkeyCtx;
    }
}

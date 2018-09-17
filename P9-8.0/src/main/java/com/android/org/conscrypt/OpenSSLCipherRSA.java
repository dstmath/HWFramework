package com.android.org.conscrypt;

import com.android.org.conscrypt.NativeRef.EVP_PKEY_CTX;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Locale;
import javax.crypto.BadPaddingException;
import javax.crypto.CipherSpi;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import javax.crypto.spec.PSource.PSpecified;
import javax.crypto.spec.SecretKeySpec;

abstract class OpenSSLCipherRSA extends CipherSpi {
    private byte[] buffer;
    private int bufferOffset;
    protected boolean encrypting;
    private boolean inputTooLarge;
    protected OpenSSLKey key;
    protected int padding = 1;
    protected boolean usingPrivateKey;

    public static abstract class DirectRSA extends OpenSSLCipherRSA {
        public DirectRSA(int padding) {
            super(padding);
        }

        protected int doCryptoOperation(byte[] tmpBuf, byte[] output) throws BadPaddingException, IllegalBlockSizeException {
            if (!this.encrypting) {
                try {
                    if (this.usingPrivateKey) {
                        return NativeCrypto.RSA_private_decrypt(tmpBuf.length, tmpBuf, output, this.key.getNativeRef(), this.padding);
                    }
                    return NativeCrypto.RSA_public_decrypt(tmpBuf.length, tmpBuf, output, this.key.getNativeRef(), this.padding);
                } catch (SignatureException e) {
                    IllegalBlockSizeException newE = new IllegalBlockSizeException();
                    newE.initCause(e);
                    throw newE;
                }
            } else if (this.usingPrivateKey) {
                return NativeCrypto.RSA_private_encrypt(tmpBuf.length, tmpBuf, output, this.key.getNativeRef(), this.padding);
            } else {
                return NativeCrypto.RSA_public_encrypt(tmpBuf.length, tmpBuf, output, this.key.getNativeRef(), this.padding);
            }
        }
    }

    protected static class OAEP extends OpenSSLCipherRSA {
        private byte[] label;
        private long mgf1Md;
        private long oaepMd;
        private int oaepMdSizeBytes;
        private EVP_PKEY_CTX pkeyCtx;

        public static final class SHA1 extends OAEP {
            public SHA1() {
                super(com.android.org.conscrypt.EvpMdRef.SHA1.EVP_MD, com.android.org.conscrypt.EvpMdRef.SHA1.SIZE_BYTES);
            }
        }

        public static final class SHA224 extends OAEP {
            public SHA224() {
                super(com.android.org.conscrypt.EvpMdRef.SHA224.EVP_MD, com.android.org.conscrypt.EvpMdRef.SHA224.SIZE_BYTES);
            }
        }

        public static final class SHA256 extends OAEP {
            public SHA256() {
                super(com.android.org.conscrypt.EvpMdRef.SHA256.EVP_MD, com.android.org.conscrypt.EvpMdRef.SHA256.SIZE_BYTES);
            }
        }

        public static final class SHA384 extends OAEP {
            public SHA384() {
                super(com.android.org.conscrypt.EvpMdRef.SHA384.EVP_MD, com.android.org.conscrypt.EvpMdRef.SHA384.SIZE_BYTES);
            }
        }

        public static final class SHA512 extends OAEP {
            public SHA512() {
                super(com.android.org.conscrypt.EvpMdRef.SHA512.EVP_MD, com.android.org.conscrypt.EvpMdRef.SHA512.SIZE_BYTES);
            }
        }

        public OAEP(long defaultMd, int defaultMdSizeBytes) {
            super(4);
            this.mgf1Md = defaultMd;
            this.oaepMd = defaultMd;
            this.oaepMdSizeBytes = defaultMdSizeBytes;
        }

        /* JADX WARNING: Removed duplicated region for block: B:10:0x003a A:{ExcHandler: java.security.NoSuchAlgorithmException (e java.security.NoSuchAlgorithmException), Splitter: B:3:0x0008} */
        /* JADX WARNING: Missing block: B:12:0x0043, code:
            throw new java.lang.RuntimeException("No providers of AlgorithmParameters.OAEP available");
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        protected AlgorithmParameters engineGetParameters() {
            if (!isInitialized()) {
                return null;
            }
            try {
                PSource pSrc;
                AlgorithmParameters params = AlgorithmParameters.getInstance("OAEP");
                if (this.label == null) {
                    pSrc = PSpecified.DEFAULT;
                } else {
                    pSrc = new PSpecified(this.label);
                }
                params.init(new OAEPParameterSpec(EvpMdRef.getJcaDigestAlgorithmStandardNameFromEVP_MD(this.oaepMd), EvpMdRef.MGF1_ALGORITHM_NAME, new MGF1ParameterSpec(EvpMdRef.getJcaDigestAlgorithmStandardNameFromEVP_MD(this.mgf1Md)), pSrc));
                return params;
            } catch (NoSuchAlgorithmException e) {
            }
        }

        protected void engineSetPadding(String padding) throws NoSuchPaddingException {
            if (padding.toUpperCase(Locale.US).equals("OAEPPadding")) {
                this.padding = 4;
                return;
            }
            throw new NoSuchPaddingException("Only OAEP padding is supported");
        }

        protected void engineInit(int opmode, Key key, AlgorithmParameterSpec spec, SecureRandom random) throws InvalidKeyException, InvalidAlgorithmParameterException {
            if (spec == null || ((spec instanceof OAEPParameterSpec) ^ 1) == 0) {
                engineInitInternal(opmode, key, spec);
                return;
            }
            throw new InvalidAlgorithmParameterException("Only OAEPParameterSpec accepted in OAEP mode");
        }

        protected void engineInit(int opmode, Key key, AlgorithmParameters params, SecureRandom random) throws InvalidKeyException, InvalidAlgorithmParameterException {
            AlgorithmParameterSpec algorithmParameterSpec = null;
            if (params != null) {
                try {
                    algorithmParameterSpec = (OAEPParameterSpec) params.getParameterSpec(OAEPParameterSpec.class);
                } catch (InvalidParameterSpecException e) {
                    throw new InvalidAlgorithmParameterException("Only OAEP parameters are supported", e);
                }
            }
            engineInitInternal(opmode, key, algorithmParameterSpec);
        }

        protected void doCryptoInit(AlgorithmParameterSpec spec) throws InvalidAlgorithmParameterException {
            long EVP_PKEY_encrypt_init;
            if (this.encrypting) {
                EVP_PKEY_encrypt_init = NativeCrypto.EVP_PKEY_encrypt_init(this.key.getNativeRef());
            } else {
                EVP_PKEY_encrypt_init = NativeCrypto.EVP_PKEY_decrypt_init(this.key.getNativeRef());
            }
            this.pkeyCtx = new EVP_PKEY_CTX(EVP_PKEY_encrypt_init);
            if (spec instanceof OAEPParameterSpec) {
                readOAEPParameters((OAEPParameterSpec) spec);
            }
            NativeCrypto.EVP_PKEY_CTX_set_rsa_padding(this.pkeyCtx.context, 4);
            NativeCrypto.EVP_PKEY_CTX_set_rsa_oaep_md(this.pkeyCtx.context, this.oaepMd);
            NativeCrypto.EVP_PKEY_CTX_set_rsa_mgf1_md(this.pkeyCtx.context, this.mgf1Md);
            if (this.label != null && this.label.length > 0) {
                NativeCrypto.EVP_PKEY_CTX_set_rsa_oaep_label(this.pkeyCtx.context, this.label);
            }
        }

        protected int paddedBlockSizeBytes() {
            return keySizeBytes() - ((this.oaepMdSizeBytes * 2) + 2);
        }

        private void readOAEPParameters(OAEPParameterSpec spec) throws InvalidAlgorithmParameterException {
            String mgfAlgUpper = spec.getMGFAlgorithm().toUpperCase(Locale.US);
            AlgorithmParameterSpec mgfSpec = spec.getMGFParameters();
            if ((EvpMdRef.MGF1_ALGORITHM_NAME.equals(mgfAlgUpper) || (EvpMdRef.MGF1_OID.equals(mgfAlgUpper) ^ 1) == 0) && ((mgfSpec instanceof MGF1ParameterSpec) ^ 1) == 0) {
                MGF1ParameterSpec mgf1spec = (MGF1ParameterSpec) mgfSpec;
                String oaepAlgUpper = spec.getDigestAlgorithm().toUpperCase(Locale.US);
                try {
                    this.oaepMd = EvpMdRef.getEVP_MDByJcaDigestAlgorithmStandardName(oaepAlgUpper);
                    this.oaepMdSizeBytes = EvpMdRef.getDigestSizeBytesByJcaDigestAlgorithmStandardName(oaepAlgUpper);
                    this.mgf1Md = EvpMdRef.getEVP_MDByJcaDigestAlgorithmStandardName(mgf1spec.getDigestAlgorithm());
                    PSource pSource = spec.getPSource();
                    if ("PSpecified".equals(pSource.getAlgorithm()) && ((pSource instanceof PSpecified) ^ 1) == 0) {
                        this.label = ((PSpecified) pSource).getValue();
                        return;
                    }
                    throw new InvalidAlgorithmParameterException("Only PSpecified accepted for PSource");
                } catch (NoSuchAlgorithmException e) {
                    throw new InvalidAlgorithmParameterException(e);
                }
            }
            throw new InvalidAlgorithmParameterException("Only MGF1 supported as mask generation function");
        }

        protected int doCryptoOperation(byte[] tmpBuf, byte[] output) throws BadPaddingException, IllegalBlockSizeException {
            if (this.encrypting) {
                return NativeCrypto.EVP_PKEY_encrypt(this.pkeyCtx, output, 0, tmpBuf, 0, tmpBuf.length);
            }
            return NativeCrypto.EVP_PKEY_decrypt(this.pkeyCtx, output, 0, tmpBuf, 0, tmpBuf.length);
        }
    }

    public static final class PKCS1 extends DirectRSA {
        public PKCS1() {
            super(1);
        }
    }

    public static final class Raw extends DirectRSA {
        public Raw() {
            super(3);
        }
    }

    protected abstract int doCryptoOperation(byte[] bArr, byte[] bArr2) throws BadPaddingException, IllegalBlockSizeException;

    protected OpenSSLCipherRSA(int padding) {
        this.padding = padding;
    }

    protected void engineSetMode(String mode) throws NoSuchAlgorithmException {
        String modeUpper = mode.toUpperCase(Locale.ROOT);
        if (!"NONE".equals(modeUpper) && !"ECB".equals(modeUpper)) {
            throw new NoSuchAlgorithmException("mode not supported: " + mode);
        }
    }

    protected void engineSetPadding(String padding) throws NoSuchPaddingException {
        String paddingUpper = padding.toUpperCase(Locale.ROOT);
        if ("PKCS1PADDING".equals(paddingUpper)) {
            this.padding = 1;
        } else if ("NOPADDING".equals(paddingUpper)) {
            this.padding = 3;
        } else {
            throw new NoSuchPaddingException("padding not supported: " + padding);
        }
    }

    protected int engineGetBlockSize() {
        if (this.encrypting) {
            return paddedBlockSizeBytes();
        }
        return keySizeBytes();
    }

    protected int engineGetOutputSize(int inputLen) {
        if (this.encrypting) {
            return keySizeBytes();
        }
        return paddedBlockSizeBytes();
    }

    protected int paddedBlockSizeBytes() {
        int paddedBlockSizeBytes = keySizeBytes();
        if (this.padding == 1) {
            return (paddedBlockSizeBytes - 1) - 10;
        }
        return paddedBlockSizeBytes;
    }

    protected int keySizeBytes() {
        if (isInitialized()) {
            return NativeCrypto.RSA_size(this.key.getNativeRef());
        }
        throw new IllegalStateException("cipher is not initialized");
    }

    protected boolean isInitialized() {
        return this.key != null;
    }

    protected byte[] engineGetIV() {
        return null;
    }

    protected AlgorithmParameters engineGetParameters() {
        return null;
    }

    protected void doCryptoInit(AlgorithmParameterSpec spec) throws InvalidAlgorithmParameterException {
    }

    protected void engineInitInternal(int opmode, Key key, AlgorithmParameterSpec spec) throws InvalidKeyException, InvalidAlgorithmParameterException {
        if (opmode == 1 || opmode == 3) {
            this.encrypting = true;
        } else if (opmode == 2 || opmode == 4) {
            this.encrypting = false;
        } else {
            throw new InvalidParameterException("Unsupported opmode " + opmode);
        }
        if (key instanceof OpenSSLRSAPrivateKey) {
            OpenSSLRSAPrivateKey rsaPrivateKey = (OpenSSLRSAPrivateKey) key;
            this.usingPrivateKey = true;
            this.key = rsaPrivateKey.getOpenSSLKey();
        } else if (key instanceof RSAPrivateCrtKey) {
            RSAPrivateCrtKey rsaPrivateKey2 = (RSAPrivateCrtKey) key;
            this.usingPrivateKey = true;
            this.key = OpenSSLRSAPrivateCrtKey.getInstance(rsaPrivateKey2);
        } else if (key instanceof RSAPrivateKey) {
            RSAPrivateKey rsaPrivateKey3 = (RSAPrivateKey) key;
            this.usingPrivateKey = true;
            this.key = OpenSSLRSAPrivateKey.getInstance(rsaPrivateKey3);
        } else if (key instanceof OpenSSLRSAPublicKey) {
            OpenSSLRSAPublicKey rsaPublicKey = (OpenSSLRSAPublicKey) key;
            this.usingPrivateKey = false;
            this.key = rsaPublicKey.getOpenSSLKey();
        } else if (key instanceof RSAPublicKey) {
            RSAPublicKey rsaPublicKey2 = (RSAPublicKey) key;
            this.usingPrivateKey = false;
            this.key = OpenSSLRSAPublicKey.getInstance(rsaPublicKey2);
        } else {
            throw new InvalidKeyException("Need RSA private or public key");
        }
        this.buffer = new byte[NativeCrypto.RSA_size(this.key.getNativeRef())];
        this.bufferOffset = 0;
        this.inputTooLarge = false;
        doCryptoInit(spec);
    }

    protected void engineInit(int opmode, Key key, SecureRandom random) throws InvalidKeyException {
        try {
            engineInitInternal(opmode, key, null);
        } catch (InvalidAlgorithmParameterException e) {
            throw new InvalidKeyException("Algorithm parameters rejected when none supplied", e);
        }
    }

    protected void engineInit(int opmode, Key key, AlgorithmParameterSpec params, SecureRandom random) throws InvalidKeyException, InvalidAlgorithmParameterException {
        if (params != null) {
            throw new InvalidAlgorithmParameterException("unknown param type: " + params.getClass().getName());
        }
        engineInitInternal(opmode, key, params);
    }

    protected void engineInit(int opmode, Key key, AlgorithmParameters params, SecureRandom random) throws InvalidKeyException, InvalidAlgorithmParameterException {
        if (params != null) {
            throw new InvalidAlgorithmParameterException("unknown param type: " + params.getClass().getName());
        }
        engineInitInternal(opmode, key, null);
    }

    protected byte[] engineUpdate(byte[] input, int inputOffset, int inputLen) {
        if (this.bufferOffset + inputLen > this.buffer.length) {
            this.inputTooLarge = true;
            return EmptyArray.BYTE;
        }
        System.arraycopy(input, inputOffset, this.buffer, this.bufferOffset, inputLen);
        this.bufferOffset += inputLen;
        return EmptyArray.BYTE;
    }

    protected int engineUpdate(byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset) throws ShortBufferException {
        engineUpdate(input, inputOffset, inputLen);
        return 0;
    }

    protected byte[] engineDoFinal(byte[] input, int inputOffset, int inputLen) throws IllegalBlockSizeException, BadPaddingException {
        if (input != null) {
            engineUpdate(input, inputOffset, inputLen);
        }
        if (this.inputTooLarge) {
            throw new IllegalBlockSizeException("input must be under " + this.buffer.length + " bytes");
        }
        byte[] tmpBuf;
        if (this.bufferOffset == this.buffer.length) {
            tmpBuf = this.buffer;
        } else if (this.padding == 3) {
            tmpBuf = new byte[this.buffer.length];
            System.arraycopy(this.buffer, 0, tmpBuf, this.buffer.length - this.bufferOffset, this.bufferOffset);
        } else {
            tmpBuf = Arrays.copyOf(this.buffer, this.bufferOffset);
        }
        byte[] output = new byte[this.buffer.length];
        int resultSize = doCryptoOperation(tmpBuf, output);
        if (!(this.encrypting || resultSize == output.length)) {
            output = Arrays.copyOf(output, resultSize);
        }
        this.bufferOffset = 0;
        return output;
    }

    protected int engineDoFinal(byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset) throws ShortBufferException, IllegalBlockSizeException, BadPaddingException {
        byte[] b = engineDoFinal(input, inputOffset, inputLen);
        int lastOffset = outputOffset + b.length;
        if (lastOffset > output.length) {
            throw new ShortBufferException("output buffer is too small " + output.length + " < " + lastOffset);
        }
        System.arraycopy(b, 0, output, outputOffset, b.length);
        return b.length;
    }

    protected byte[] engineWrap(Key key) throws IllegalBlockSizeException, InvalidKeyException {
        try {
            byte[] encoded = key.getEncoded();
            return engineDoFinal(encoded, 0, encoded.length);
        } catch (BadPaddingException e) {
            IllegalBlockSizeException newE = new IllegalBlockSizeException();
            newE.initCause(e);
            throw newE;
        }
    }

    protected Key engineUnwrap(byte[] wrappedKey, String wrappedKeyAlgorithm, int wrappedKeyType) throws InvalidKeyException, NoSuchAlgorithmException {
        try {
            byte[] encoded = engineDoFinal(wrappedKey, 0, wrappedKey.length);
            if (wrappedKeyType == 1) {
                return KeyFactory.getInstance(wrappedKeyAlgorithm).generatePublic(new X509EncodedKeySpec(encoded));
            }
            if (wrappedKeyType == 2) {
                return KeyFactory.getInstance(wrappedKeyAlgorithm).generatePrivate(new PKCS8EncodedKeySpec(encoded));
            }
            if (wrappedKeyType == 3) {
                return new SecretKeySpec(encoded, wrappedKeyAlgorithm);
            }
            throw new UnsupportedOperationException("wrappedKeyType == " + wrappedKeyType);
        } catch (IllegalBlockSizeException e) {
            throw new InvalidKeyException(e);
        } catch (BadPaddingException e2) {
            throw new InvalidKeyException(e2);
        } catch (InvalidKeySpecException e3) {
            throw new InvalidKeyException(e3);
        }
    }
}

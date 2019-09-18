package com.android.org.conscrypt;

import com.android.org.conscrypt.NativeRef;
import com.android.org.conscrypt.OpenSSLX509CertificateFactory;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.ECParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

final class OpenSSLKey {
    private final NativeRef.EVP_PKEY ctx;
    private final boolean wrapped;

    OpenSSLKey(long ctx2) {
        this(ctx2, false);
    }

    OpenSSLKey(long ctx2, boolean wrapped2) {
        this.ctx = new NativeRef.EVP_PKEY(ctx2);
        this.wrapped = wrapped2;
    }

    /* access modifiers changed from: package-private */
    public NativeRef.EVP_PKEY getNativeRef() {
        return this.ctx;
    }

    /* access modifiers changed from: package-private */
    public boolean isWrapped() {
        return this.wrapped;
    }

    static OpenSSLKey fromPrivateKey(PrivateKey key) throws InvalidKeyException {
        if (key instanceof OpenSSLKeyHolder) {
            return ((OpenSSLKeyHolder) key).getOpenSSLKey();
        }
        String keyFormat = key.getFormat();
        if (keyFormat == null) {
            return wrapPrivateKey(key);
        }
        if (!"PKCS#8".equals(key.getFormat())) {
            throw new InvalidKeyException("Unknown key format " + keyFormat);
        } else if (key.getEncoded() != null) {
            try {
                return new OpenSSLKey(NativeCrypto.EVP_parse_private_key(key.getEncoded()));
            } catch (OpenSSLX509CertificateFactory.ParsingException e) {
                throw new InvalidKeyException(e);
            }
        } else {
            throw new InvalidKeyException("Key encoding is null");
        }
    }

    static OpenSSLKey fromPrivateKeyPemInputStream(InputStream is) throws InvalidKeyException {
        OpenSSLBIOInputStream bis = new OpenSSLBIOInputStream(is, true);
        try {
            long keyCtx = NativeCrypto.PEM_read_bio_PrivateKey(bis.getBioContext());
            if (keyCtx == 0) {
                bis.release();
                return null;
            }
            OpenSSLKey openSSLKey = new OpenSSLKey(keyCtx);
            bis.release();
            return openSSLKey;
        } catch (Exception e) {
            throw new InvalidKeyException(e);
        } catch (Throwable th) {
            bis.release();
            throw th;
        }
    }

    static OpenSSLKey fromPrivateKeyForTLSStackOnly(PrivateKey privateKey, PublicKey publicKey) throws InvalidKeyException {
        OpenSSLKey result = getOpenSSLKey(privateKey);
        if (result != null) {
            return result;
        }
        OpenSSLKey result2 = fromKeyMaterial(privateKey);
        if (result2 != null) {
            return result2;
        }
        return wrapJCAPrivateKeyForTLSStackOnly(privateKey, publicKey);
    }

    static OpenSSLKey fromECPrivateKeyForTLSStackOnly(PrivateKey key, ECParameterSpec ecParams) throws InvalidKeyException {
        OpenSSLKey result = getOpenSSLKey(key);
        if (result != null) {
            return result;
        }
        OpenSSLKey result2 = fromKeyMaterial(key);
        if (result2 != null) {
            return result2;
        }
        return OpenSSLECPrivateKey.wrapJCAPrivateKeyForTLSStackOnly(key, ecParams);
    }

    private static OpenSSLKey getOpenSSLKey(PrivateKey key) {
        if (key instanceof OpenSSLKeyHolder) {
            return ((OpenSSLKeyHolder) key).getOpenSSLKey();
        }
        if ("RSA".equals(key.getAlgorithm())) {
            return Platform.wrapRsaKey(key);
        }
        return null;
    }

    private static OpenSSLKey fromKeyMaterial(PrivateKey key) throws InvalidKeyException {
        if (!"PKCS#8".equals(key.getFormat())) {
            return null;
        }
        byte[] encoded = key.getEncoded();
        if (encoded == null) {
            return null;
        }
        try {
            return new OpenSSLKey(NativeCrypto.EVP_parse_private_key(encoded));
        } catch (OpenSSLX509CertificateFactory.ParsingException e) {
            throw new InvalidKeyException(e);
        }
    }

    private static OpenSSLKey wrapJCAPrivateKeyForTLSStackOnly(PrivateKey privateKey, PublicKey publicKey) throws InvalidKeyException {
        String keyAlgorithm = privateKey.getAlgorithm();
        if ("RSA".equals(keyAlgorithm)) {
            return OpenSSLRSAPrivateKey.wrapJCAPrivateKeyForTLSStackOnly(privateKey, publicKey);
        }
        if ("EC".equals(keyAlgorithm)) {
            return OpenSSLECPrivateKey.wrapJCAPrivateKeyForTLSStackOnly(privateKey, publicKey);
        }
        throw new InvalidKeyException("Unsupported key algorithm: " + keyAlgorithm);
    }

    private static OpenSSLKey wrapPrivateKey(PrivateKey key) throws InvalidKeyException {
        if (key instanceof RSAPrivateKey) {
            return OpenSSLRSAPrivateKey.wrapPlatformKey((RSAPrivateKey) key);
        }
        if (key instanceof ECPrivateKey) {
            return OpenSSLECPrivateKey.wrapPlatformKey((ECPrivateKey) key);
        }
        throw new InvalidKeyException("Unknown key type: " + key.toString());
    }

    static OpenSSLKey fromPublicKey(PublicKey key) throws InvalidKeyException {
        if (key instanceof OpenSSLKeyHolder) {
            return ((OpenSSLKeyHolder) key).getOpenSSLKey();
        }
        if (!"X.509".equals(key.getFormat())) {
            throw new InvalidKeyException("Unknown key format " + key.getFormat());
        } else if (key.getEncoded() != null) {
            try {
                return new OpenSSLKey(NativeCrypto.EVP_parse_public_key(key.getEncoded()));
            } catch (Exception e) {
                throw new InvalidKeyException(e);
            }
        } else {
            throw new InvalidKeyException("Key encoding is null");
        }
    }

    static OpenSSLKey fromPublicKeyPemInputStream(InputStream is) throws InvalidKeyException {
        OpenSSLBIOInputStream bis = new OpenSSLBIOInputStream(is, true);
        try {
            long keyCtx = NativeCrypto.PEM_read_bio_PUBKEY(bis.getBioContext());
            if (keyCtx == 0) {
                bis.release();
                return null;
            }
            OpenSSLKey openSSLKey = new OpenSSLKey(keyCtx);
            bis.release();
            return openSSLKey;
        } catch (Exception e) {
            throw new InvalidKeyException(e);
        } catch (Throwable th) {
            bis.release();
            throw th;
        }
    }

    /* access modifiers changed from: package-private */
    public PublicKey getPublicKey() throws NoSuchAlgorithmException {
        int EVP_PKEY_type = NativeCrypto.EVP_PKEY_type(this.ctx);
        if (EVP_PKEY_type == 6) {
            return new OpenSSLRSAPublicKey(this);
        }
        if (EVP_PKEY_type == 408) {
            return new OpenSSLECPublicKey(this);
        }
        throw new NoSuchAlgorithmException("unknown PKEY type");
    }

    static PublicKey getPublicKey(X509EncodedKeySpec keySpec, int type) throws InvalidKeySpecException {
        try {
            OpenSSLKey key = new OpenSSLKey(NativeCrypto.EVP_parse_public_key(keySpec.getEncoded()));
            if (NativeCrypto.EVP_PKEY_type(key.getNativeRef()) == type) {
                try {
                    return key.getPublicKey();
                } catch (NoSuchAlgorithmException e) {
                    throw new InvalidKeySpecException(e);
                }
            } else {
                throw new InvalidKeySpecException("Unexpected key type");
            }
        } catch (Exception e2) {
            throw new InvalidKeySpecException(e2);
        }
    }

    /* access modifiers changed from: package-private */
    public PrivateKey getPrivateKey() throws NoSuchAlgorithmException {
        int EVP_PKEY_type = NativeCrypto.EVP_PKEY_type(this.ctx);
        if (EVP_PKEY_type == 6) {
            return new OpenSSLRSAPrivateKey(this);
        }
        if (EVP_PKEY_type == 408) {
            return new OpenSSLECPrivateKey(this);
        }
        throw new NoSuchAlgorithmException("unknown PKEY type");
    }

    static PrivateKey getPrivateKey(PKCS8EncodedKeySpec keySpec, int type) throws InvalidKeySpecException {
        try {
            OpenSSLKey key = new OpenSSLKey(NativeCrypto.EVP_parse_private_key(keySpec.getEncoded()));
            if (NativeCrypto.EVP_PKEY_type(key.getNativeRef()) == type) {
                try {
                    return key.getPrivateKey();
                } catch (NoSuchAlgorithmException e) {
                    throw new InvalidKeySpecException(e);
                }
            } else {
                throw new InvalidKeySpecException("Unexpected key type");
            }
        } catch (Exception e2) {
            throw new InvalidKeySpecException(e2);
        }
    }

    public boolean equals(Object o) {
        boolean z = true;
        if (o == this) {
            return true;
        }
        if (!(o instanceof OpenSSLKey)) {
            return false;
        }
        OpenSSLKey other = (OpenSSLKey) o;
        if (this.ctx.equals(other.getNativeRef())) {
            return true;
        }
        if (NativeCrypto.EVP_PKEY_cmp(this.ctx, other.getNativeRef()) != 1) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return this.ctx.hashCode();
    }
}

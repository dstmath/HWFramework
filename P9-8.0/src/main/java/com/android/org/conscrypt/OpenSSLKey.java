package com.android.org.conscrypt;

import com.android.org.conscrypt.NativeRef.EVP_PKEY;
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

public class OpenSSLKey {
    private final EVP_PKEY ctx;
    private final boolean wrapped;

    public OpenSSLKey(long ctx) {
        this(ctx, false);
    }

    public OpenSSLKey(long ctx, boolean wrapped) {
        this.ctx = new EVP_PKEY(ctx);
        this.wrapped = wrapped;
    }

    public EVP_PKEY getNativeRef() {
        return this.ctx;
    }

    public boolean isWrapped() {
        return this.wrapped;
    }

    public static OpenSSLKey fromPrivateKey(PrivateKey key) throws InvalidKeyException {
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
            return new OpenSSLKey(NativeCrypto.d2i_PKCS8_PRIV_KEY_INFO(key.getEncoded()));
        } else {
            throw new InvalidKeyException("Key encoding is null");
        }
    }

    public static OpenSSLKey fromPrivateKeyPemInputStream(InputStream is) throws InvalidKeyException {
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
        }
    }

    public static OpenSSLKey fromPrivateKeyForTLSStackOnly(PrivateKey privateKey, PublicKey publicKey) throws InvalidKeyException {
        OpenSSLKey result = getOpenSSLKey(privateKey);
        if (result != null) {
            return result;
        }
        result = fromKeyMaterial(privateKey);
        if (result != null) {
            return result;
        }
        return wrapJCAPrivateKeyForTLSStackOnly(privateKey, publicKey);
    }

    public static OpenSSLKey fromECPrivateKeyForTLSStackOnly(PrivateKey key, ECParameterSpec ecParams) throws InvalidKeyException {
        OpenSSLKey result = getOpenSSLKey(key);
        if (result != null) {
            return result;
        }
        result = fromKeyMaterial(key);
        if (result != null) {
            return result;
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

    private static OpenSSLKey fromKeyMaterial(PrivateKey key) {
        if (!"PKCS#8".equals(key.getFormat())) {
            return null;
        }
        byte[] encoded = key.getEncoded();
        if (encoded == null) {
            return null;
        }
        return new OpenSSLKey(NativeCrypto.d2i_PKCS8_PRIV_KEY_INFO(encoded));
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

    public static OpenSSLKey fromPublicKey(PublicKey key) throws InvalidKeyException {
        if (key instanceof OpenSSLKeyHolder) {
            return ((OpenSSLKeyHolder) key).getOpenSSLKey();
        }
        if (!"X.509".equals(key.getFormat())) {
            throw new InvalidKeyException("Unknown key format " + key.getFormat());
        } else if (key.getEncoded() == null) {
            throw new InvalidKeyException("Key encoding is null");
        } else {
            try {
                return new OpenSSLKey(NativeCrypto.d2i_PUBKEY(key.getEncoded()));
            } catch (Exception e) {
                throw new InvalidKeyException(e);
            }
        }
    }

    public static OpenSSLKey fromPublicKeyPemInputStream(InputStream is) throws InvalidKeyException {
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
        }
    }

    public PublicKey getPublicKey() throws NoSuchAlgorithmException {
        switch (NativeCrypto.EVP_PKEY_type(this.ctx)) {
            case 6:
                return new OpenSSLRSAPublicKey(this);
            case NativeConstants.EVP_PKEY_EC /*408*/:
                return new OpenSSLECPublicKey(this);
            default:
                throw new NoSuchAlgorithmException("unknown PKEY type");
        }
    }

    static PublicKey getPublicKey(X509EncodedKeySpec keySpec, int type) throws InvalidKeySpecException {
        X509EncodedKeySpec x509KeySpec = keySpec;
        try {
            OpenSSLKey key = new OpenSSLKey(NativeCrypto.d2i_PUBKEY(keySpec.getEncoded()));
            if (NativeCrypto.EVP_PKEY_type(key.getNativeRef()) != type) {
                throw new InvalidKeySpecException("Unexpected key type");
            }
            try {
                return key.getPublicKey();
            } catch (NoSuchAlgorithmException e) {
                throw new InvalidKeySpecException(e);
            }
        } catch (Exception e2) {
            throw new InvalidKeySpecException(e2);
        }
    }

    public PrivateKey getPrivateKey() throws NoSuchAlgorithmException {
        switch (NativeCrypto.EVP_PKEY_type(this.ctx)) {
            case 6:
                return new OpenSSLRSAPrivateKey(this);
            case NativeConstants.EVP_PKEY_EC /*408*/:
                return new OpenSSLECPrivateKey(this);
            default:
                throw new NoSuchAlgorithmException("unknown PKEY type");
        }
    }

    static PrivateKey getPrivateKey(PKCS8EncodedKeySpec keySpec, int type) throws InvalidKeySpecException {
        PKCS8EncodedKeySpec pkcs8KeySpec = keySpec;
        try {
            OpenSSLKey key = new OpenSSLKey(NativeCrypto.d2i_PKCS8_PRIV_KEY_INFO(keySpec.getEncoded()));
            if (NativeCrypto.EVP_PKEY_type(key.getNativeRef()) != type) {
                throw new InvalidKeySpecException("Unexpected key type");
            }
            try {
                return key.getPrivateKey();
            } catch (NoSuchAlgorithmException e) {
                throw new InvalidKeySpecException(e);
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

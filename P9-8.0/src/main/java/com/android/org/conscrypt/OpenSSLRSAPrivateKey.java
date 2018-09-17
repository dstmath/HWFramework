package com.android.org.conscrypt;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;

public class OpenSSLRSAPrivateKey implements RSAPrivateKey, OpenSSLKeyHolder {
    private static final long serialVersionUID = 4872170254439578735L;
    protected transient boolean fetchedParams;
    protected transient OpenSSLKey key;
    protected BigInteger modulus;
    protected BigInteger privateExponent;

    OpenSSLRSAPrivateKey(OpenSSLKey key) {
        this.key = key;
    }

    OpenSSLRSAPrivateKey(OpenSSLKey key, byte[][] params) {
        this(key);
        readParams(params);
        this.fetchedParams = true;
    }

    public OpenSSLKey getOpenSSLKey() {
        return this.key;
    }

    public OpenSSLRSAPrivateKey(RSAPrivateKeySpec rsaKeySpec) throws InvalidKeySpecException {
        this(init(rsaKeySpec));
    }

    private static OpenSSLKey init(RSAPrivateKeySpec rsaKeySpec) throws InvalidKeySpecException {
        BigInteger modulus = rsaKeySpec.getModulus();
        BigInteger privateExponent = rsaKeySpec.getPrivateExponent();
        if (modulus == null) {
            throw new InvalidKeySpecException("modulus == null");
        } else if (privateExponent == null) {
            throw new InvalidKeySpecException("privateExponent == null");
        } else {
            try {
                return new OpenSSLKey(NativeCrypto.EVP_PKEY_new_RSA(modulus.toByteArray(), null, privateExponent.toByteArray(), null, null, null, null, null));
            } catch (Exception e) {
                throw new InvalidKeySpecException(e);
            }
        }
    }

    static OpenSSLRSAPrivateKey getInstance(OpenSSLKey key) {
        byte[][] params = NativeCrypto.get_RSA_private_params(key.getNativeRef());
        if (params[1] != null) {
            return new OpenSSLRSAPrivateCrtKey(key, params);
        }
        return new OpenSSLRSAPrivateKey(key, params);
    }

    protected static OpenSSLKey wrapPlatformKey(RSAPrivateKey rsaPrivateKey) throws InvalidKeyException {
        OpenSSLKey wrapper = Platform.wrapRsaKey(rsaPrivateKey);
        if (wrapper != null) {
            return wrapper;
        }
        return new OpenSSLKey(NativeCrypto.getRSAPrivateKeyWrapper(rsaPrivateKey, rsaPrivateKey.getModulus().toByteArray()), true);
    }

    static OpenSSLKey wrapJCAPrivateKeyForTLSStackOnly(PrivateKey privateKey, PublicKey publicKey) throws InvalidKeyException {
        BigInteger modulus = null;
        if (privateKey instanceof RSAKey) {
            modulus = ((RSAKey) privateKey).getModulus();
        } else if (publicKey instanceof RSAKey) {
            modulus = ((RSAKey) publicKey).getModulus();
        }
        if (modulus != null) {
            return new OpenSSLKey(NativeCrypto.getRSAPrivateKeyWrapper(privateKey, modulus.toByteArray()), true);
        }
        throw new InvalidKeyException("RSA modulus not available. Private: " + privateKey + ", public: " + publicKey);
    }

    static OpenSSLKey getInstance(RSAPrivateKey rsaPrivateKey) throws InvalidKeyException {
        if (rsaPrivateKey.getFormat() == null) {
            return wrapPlatformKey(rsaPrivateKey);
        }
        BigInteger modulus = rsaPrivateKey.getModulus();
        BigInteger privateExponent = rsaPrivateKey.getPrivateExponent();
        if (modulus == null) {
            throw new InvalidKeyException("modulus == null");
        } else if (privateExponent == null) {
            throw new InvalidKeyException("privateExponent == null");
        } else {
            try {
                return new OpenSSLKey(NativeCrypto.EVP_PKEY_new_RSA(modulus.toByteArray(), null, privateExponent.toByteArray(), null, null, null, null, null));
            } catch (Exception e) {
                throw new InvalidKeyException(e);
            }
        }
    }

    final synchronized void ensureReadParams() {
        if (!this.fetchedParams) {
            readParams(NativeCrypto.get_RSA_private_params(this.key.getNativeRef()));
            this.fetchedParams = true;
        }
    }

    void readParams(byte[][] params) {
        if (params[0] == null) {
            throw new NullPointerException("modulus == null");
        } else if (params[2] == null) {
            throw new NullPointerException("privateExponent == null");
        } else {
            this.modulus = new BigInteger(params[0]);
            if (params[2] != null) {
                this.privateExponent = new BigInteger(params[2]);
            }
        }
    }

    public final BigInteger getPrivateExponent() {
        ensureReadParams();
        return this.privateExponent;
    }

    public final BigInteger getModulus() {
        ensureReadParams();
        return this.modulus;
    }

    public final byte[] getEncoded() {
        return NativeCrypto.i2d_PKCS8_PRIV_KEY_INFO(this.key.getNativeRef());
    }

    public final String getFormat() {
        return "PKCS#8";
    }

    public final String getAlgorithm() {
        return "RSA";
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (o == this) {
            return true;
        }
        if (o instanceof OpenSSLRSAPrivateKey) {
            return this.key.equals(((OpenSSLRSAPrivateKey) o).getOpenSSLKey());
        } else if (!(o instanceof RSAPrivateKey)) {
            return false;
        } else {
            ensureReadParams();
            RSAPrivateKey other = (RSAPrivateKey) o;
            if (this.modulus.equals(other.getModulus())) {
                z = this.privateExponent.equals(other.getPrivateExponent());
            }
            return z;
        }
    }

    public int hashCode() {
        ensureReadParams();
        int hash = this.modulus.hashCode() + 3;
        if (this.privateExponent != null) {
            return (hash * 7) + this.privateExponent.hashCode();
        }
        return hash;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("OpenSSLRSAPrivateKey{");
        ensureReadParams();
        sb.append("modulus=");
        sb.append(this.modulus.toString(16));
        return sb.toString();
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.key = new OpenSSLKey(NativeCrypto.EVP_PKEY_new_RSA(this.modulus.toByteArray(), null, this.privateExponent.toByteArray(), null, null, null, null, null));
        this.fetchedParams = true;
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        ensureReadParams();
        stream.defaultWriteObject();
    }
}

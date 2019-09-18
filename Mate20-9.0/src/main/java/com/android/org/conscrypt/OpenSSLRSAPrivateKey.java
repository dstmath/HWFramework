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

class OpenSSLRSAPrivateKey implements RSAPrivateKey, OpenSSLKeyHolder {
    private static final long serialVersionUID = 4872170254439578735L;
    transient boolean fetchedParams;
    transient OpenSSLKey key;
    BigInteger modulus;
    BigInteger privateExponent;

    OpenSSLRSAPrivateKey(OpenSSLKey key2) {
        this.key = key2;
    }

    OpenSSLRSAPrivateKey(OpenSSLKey key2, byte[][] params) {
        this(key2);
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
        BigInteger modulus2 = rsaKeySpec.getModulus();
        BigInteger privateExponent2 = rsaKeySpec.getPrivateExponent();
        if (modulus2 == null) {
            throw new InvalidKeySpecException("modulus == null");
        } else if (privateExponent2 != null) {
            try {
                return new OpenSSLKey(NativeCrypto.EVP_PKEY_new_RSA(modulus2.toByteArray(), null, privateExponent2.toByteArray(), null, null, null, null, null));
            } catch (Exception e) {
                throw new InvalidKeySpecException(e);
            }
        } else {
            throw new InvalidKeySpecException("privateExponent == null");
        }
    }

    static OpenSSLRSAPrivateKey getInstance(OpenSSLKey key2) {
        byte[][] params = NativeCrypto.get_RSA_private_params(key2.getNativeRef());
        if (params[1] != null) {
            return new OpenSSLRSAPrivateCrtKey(key2, params);
        }
        return new OpenSSLRSAPrivateKey(key2, params);
    }

    static OpenSSLKey wrapPlatformKey(RSAPrivateKey rsaPrivateKey) throws InvalidKeyException {
        OpenSSLKey wrapper = Platform.wrapRsaKey(rsaPrivateKey);
        if (wrapper != null) {
            return wrapper;
        }
        return new OpenSSLKey(NativeCrypto.getRSAPrivateKeyWrapper(rsaPrivateKey, rsaPrivateKey.getModulus().toByteArray()), true);
    }

    static OpenSSLKey wrapJCAPrivateKeyForTLSStackOnly(PrivateKey privateKey, PublicKey publicKey) throws InvalidKeyException {
        BigInteger modulus2 = null;
        if (privateKey instanceof RSAKey) {
            modulus2 = ((RSAKey) privateKey).getModulus();
        } else if (publicKey instanceof RSAKey) {
            modulus2 = ((RSAKey) publicKey).getModulus();
        }
        if (modulus2 != null) {
            return new OpenSSLKey(NativeCrypto.getRSAPrivateKeyWrapper(privateKey, modulus2.toByteArray()), true);
        }
        throw new InvalidKeyException("RSA modulus not available. Private: " + privateKey + ", public: " + publicKey);
    }

    static OpenSSLKey getInstance(RSAPrivateKey rsaPrivateKey) throws InvalidKeyException {
        if (rsaPrivateKey.getFormat() == null) {
            return wrapPlatformKey(rsaPrivateKey);
        }
        BigInteger modulus2 = rsaPrivateKey.getModulus();
        BigInteger privateExponent2 = rsaPrivateKey.getPrivateExponent();
        if (modulus2 == null) {
            throw new InvalidKeyException("modulus == null");
        } else if (privateExponent2 != null) {
            try {
                return new OpenSSLKey(NativeCrypto.EVP_PKEY_new_RSA(modulus2.toByteArray(), null, privateExponent2.toByteArray(), null, null, null, null, null));
            } catch (Exception e) {
                throw new InvalidKeyException(e);
            }
        } else {
            throw new InvalidKeyException("privateExponent == null");
        }
    }

    /* access modifiers changed from: package-private */
    public final synchronized void ensureReadParams() {
        if (!this.fetchedParams) {
            readParams(NativeCrypto.get_RSA_private_params(this.key.getNativeRef()));
            this.fetchedParams = true;
        }
    }

    /* access modifiers changed from: package-private */
    public void readParams(byte[][] params) {
        if (params[0] == null) {
            throw new NullPointerException("modulus == null");
        } else if (params[2] != null) {
            this.modulus = new BigInteger(params[0]);
            if (params[2] != null) {
                this.privateExponent = new BigInteger(params[2]);
            }
        } else {
            throw new NullPointerException("privateExponent == null");
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
        return NativeCrypto.EVP_marshal_private_key(this.key.getNativeRef());
    }

    public final String getFormat() {
        return "PKCS#8";
    }

    public final String getAlgorithm() {
        return "RSA";
    }

    public boolean equals(Object o) {
        boolean z = true;
        if (o == this) {
            return true;
        }
        if (o instanceof OpenSSLRSAPrivateKey) {
            return this.key.equals(((OpenSSLRSAPrivateKey) o).getOpenSSLKey());
        }
        if (!(o instanceof RSAPrivateKey)) {
            return false;
        }
        ensureReadParams();
        RSAPrivateKey other = (RSAPrivateKey) o;
        if (!this.modulus.equals(other.getModulus()) || !this.privateExponent.equals(other.getPrivateExponent())) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        ensureReadParams();
        int hash = (1 * 3) + this.modulus.hashCode();
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

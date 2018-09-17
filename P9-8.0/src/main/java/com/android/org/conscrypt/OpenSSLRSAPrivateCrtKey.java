package com.android.org.conscrypt;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateCrtKeySpec;

public class OpenSSLRSAPrivateCrtKey extends OpenSSLRSAPrivateKey implements RSAPrivateCrtKey {
    private static final long serialVersionUID = 3785291944868707197L;
    private BigInteger crtCoefficient;
    private BigInteger primeExponentP;
    private BigInteger primeExponentQ;
    private BigInteger primeP;
    private BigInteger primeQ;
    private BigInteger publicExponent;

    OpenSSLRSAPrivateCrtKey(OpenSSLKey key) {
        super(key);
    }

    OpenSSLRSAPrivateCrtKey(OpenSSLKey key, byte[][] params) {
        super(key, params);
    }

    public OpenSSLRSAPrivateCrtKey(RSAPrivateCrtKeySpec rsaKeySpec) throws InvalidKeySpecException {
        super(init(rsaKeySpec));
    }

    private static OpenSSLKey init(RSAPrivateCrtKeySpec rsaKeySpec) throws InvalidKeySpecException {
        BigInteger modulus = rsaKeySpec.getModulus();
        BigInteger privateExponent = rsaKeySpec.getPrivateExponent();
        if (modulus == null) {
            throw new InvalidKeySpecException("modulus == null");
        } else if (privateExponent == null) {
            throw new InvalidKeySpecException("privateExponent == null");
        } else {
            try {
                byte[] bArr;
                BigInteger publicExponent = rsaKeySpec.getPublicExponent();
                BigInteger primeP = rsaKeySpec.getPrimeP();
                BigInteger primeQ = rsaKeySpec.getPrimeQ();
                BigInteger primeExponentP = rsaKeySpec.getPrimeExponentP();
                BigInteger primeExponentQ = rsaKeySpec.getPrimeExponentQ();
                BigInteger crtCoefficient = rsaKeySpec.getCrtCoefficient();
                byte[] toByteArray = modulus.toByteArray();
                byte[] toByteArray2 = publicExponent == null ? null : publicExponent.toByteArray();
                byte[] toByteArray3 = privateExponent.toByteArray();
                byte[] toByteArray4 = primeP == null ? null : primeP.toByteArray();
                byte[] toByteArray5 = primeQ == null ? null : primeQ.toByteArray();
                byte[] toByteArray6 = primeExponentP == null ? null : primeExponentP.toByteArray();
                byte[] toByteArray7 = primeExponentQ == null ? null : primeExponentQ.toByteArray();
                if (crtCoefficient == null) {
                    bArr = null;
                } else {
                    bArr = crtCoefficient.toByteArray();
                }
                return new OpenSSLKey(NativeCrypto.EVP_PKEY_new_RSA(toByteArray, toByteArray2, toByteArray3, toByteArray4, toByteArray5, toByteArray6, toByteArray7, bArr));
            } catch (Exception e) {
                throw new InvalidKeySpecException(e);
            }
        }
    }

    static OpenSSLKey getInstance(RSAPrivateCrtKey rsaPrivateKey) throws InvalidKeyException {
        if (rsaPrivateKey.getFormat() == null) {
            return OpenSSLRSAPrivateKey.wrapPlatformKey(rsaPrivateKey);
        }
        BigInteger modulus = rsaPrivateKey.getModulus();
        BigInteger privateExponent = rsaPrivateKey.getPrivateExponent();
        if (modulus == null) {
            throw new InvalidKeyException("modulus == null");
        } else if (privateExponent == null) {
            throw new InvalidKeyException("privateExponent == null");
        } else {
            try {
                byte[] bArr;
                BigInteger publicExponent = rsaPrivateKey.getPublicExponent();
                BigInteger primeP = rsaPrivateKey.getPrimeP();
                BigInteger primeQ = rsaPrivateKey.getPrimeQ();
                BigInteger primeExponentP = rsaPrivateKey.getPrimeExponentP();
                BigInteger primeExponentQ = rsaPrivateKey.getPrimeExponentQ();
                BigInteger crtCoefficient = rsaPrivateKey.getCrtCoefficient();
                byte[] toByteArray = modulus.toByteArray();
                byte[] toByteArray2 = publicExponent == null ? null : publicExponent.toByteArray();
                byte[] toByteArray3 = privateExponent.toByteArray();
                byte[] toByteArray4 = primeP == null ? null : primeP.toByteArray();
                byte[] toByteArray5 = primeQ == null ? null : primeQ.toByteArray();
                byte[] toByteArray6 = primeExponentP == null ? null : primeExponentP.toByteArray();
                byte[] toByteArray7 = primeExponentQ == null ? null : primeExponentQ.toByteArray();
                if (crtCoefficient == null) {
                    bArr = null;
                } else {
                    bArr = crtCoefficient.toByteArray();
                }
                return new OpenSSLKey(NativeCrypto.EVP_PKEY_new_RSA(toByteArray, toByteArray2, toByteArray3, toByteArray4, toByteArray5, toByteArray6, toByteArray7, bArr));
            } catch (Exception e) {
                throw new InvalidKeyException(e);
            }
        }
    }

    synchronized void readParams(byte[][] params) {
        super.readParams(params);
        if (params[1] != null) {
            this.publicExponent = new BigInteger(params[1]);
        }
        if (params[3] != null) {
            this.primeP = new BigInteger(params[3]);
        }
        if (params[4] != null) {
            this.primeQ = new BigInteger(params[4]);
        }
        if (params[5] != null) {
            this.primeExponentP = new BigInteger(params[5]);
        }
        if (params[6] != null) {
            this.primeExponentQ = new BigInteger(params[6]);
        }
        if (params[7] != null) {
            this.crtCoefficient = new BigInteger(params[7]);
        }
    }

    public BigInteger getPublicExponent() {
        ensureReadParams();
        return this.publicExponent;
    }

    public BigInteger getPrimeP() {
        ensureReadParams();
        return this.primeP;
    }

    public BigInteger getPrimeQ() {
        ensureReadParams();
        return this.primeQ;
    }

    public BigInteger getPrimeExponentP() {
        ensureReadParams();
        return this.primeExponentP;
    }

    public BigInteger getPrimeExponentQ() {
        ensureReadParams();
        return this.primeExponentQ;
    }

    public BigInteger getCrtCoefficient() {
        ensureReadParams();
        return this.crtCoefficient;
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (o == this) {
            return true;
        }
        if (o instanceof OpenSSLRSAPrivateKey) {
            return getOpenSSLKey().equals(((OpenSSLRSAPrivateKey) o).getOpenSSLKey());
        } else if (o instanceof RSAPrivateCrtKey) {
            ensureReadParams();
            RSAPrivateCrtKey other = (RSAPrivateCrtKey) o;
            if (getModulus().equals(other.getModulus()) && this.publicExponent.equals(other.getPublicExponent()) && getPrivateExponent().equals(other.getPrivateExponent()) && this.primeP.equals(other.getPrimeP()) && this.primeQ.equals(other.getPrimeQ()) && this.primeExponentP.equals(other.getPrimeExponentP()) && this.primeExponentQ.equals(other.getPrimeExponentQ())) {
                z = this.crtCoefficient.equals(other.getCrtCoefficient());
            }
            return z;
        } else if (!(o instanceof RSAPrivateKey)) {
            return false;
        } else {
            ensureReadParams();
            RSAPrivateKey other2 = (RSAPrivateKey) o;
            if (getModulus().equals(other2.getModulus())) {
                z = getPrivateExponent().equals(other2.getPrivateExponent());
            }
            return z;
        }
    }

    public final int hashCode() {
        int hashCode = super.hashCode();
        if (this.publicExponent != null) {
            return hashCode ^ this.publicExponent.hashCode();
        }
        return hashCode;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("OpenSSLRSAPrivateCrtKey{");
        ensureReadParams();
        sb.append("modulus=");
        sb.append(getModulus().toString(16));
        if (this.publicExponent != null) {
            sb.append(',');
            sb.append("publicExponent=");
            sb.append(this.publicExponent.toString(16));
        }
        sb.append('}');
        return sb.toString();
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        byte[] bArr = null;
        stream.defaultReadObject();
        byte[] toByteArray = this.modulus.toByteArray();
        byte[] toByteArray2 = this.publicExponent == null ? null : this.publicExponent.toByteArray();
        byte[] toByteArray3 = this.privateExponent.toByteArray();
        byte[] toByteArray4 = this.primeP == null ? null : this.primeP.toByteArray();
        byte[] toByteArray5 = this.primeQ == null ? null : this.primeQ.toByteArray();
        byte[] toByteArray6 = this.primeExponentP == null ? null : this.primeExponentP.toByteArray();
        byte[] toByteArray7 = this.primeExponentQ == null ? null : this.primeExponentQ.toByteArray();
        if (this.crtCoefficient != null) {
            bArr = this.crtCoefficient.toByteArray();
        }
        this.key = new OpenSSLKey(NativeCrypto.EVP_PKEY_new_RSA(toByteArray, toByteArray2, toByteArray3, toByteArray4, toByteArray5, toByteArray6, toByteArray7, bArr));
        this.fetchedParams = true;
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        ensureReadParams();
        stream.defaultWriteObject();
    }
}

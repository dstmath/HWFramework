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

final class OpenSSLRSAPrivateCrtKey extends OpenSSLRSAPrivateKey implements RSAPrivateCrtKey {
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

    OpenSSLRSAPrivateCrtKey(RSAPrivateCrtKeySpec rsaKeySpec) throws InvalidKeySpecException {
        super(init(rsaKeySpec));
    }

    private static OpenSSLKey init(RSAPrivateCrtKeySpec rsaKeySpec) throws InvalidKeySpecException {
        byte[] bArr;
        byte[] bArr2;
        byte[] bArr3;
        byte[] bArr4;
        byte[] bArr5;
        BigInteger modulus = rsaKeySpec.getModulus();
        BigInteger privateExponent = rsaKeySpec.getPrivateExponent();
        if (modulus == null) {
            throw new InvalidKeySpecException("modulus == null");
        } else if (privateExponent != null) {
            try {
                BigInteger publicExponent2 = rsaKeySpec.getPublicExponent();
                BigInteger primeP2 = rsaKeySpec.getPrimeP();
                BigInteger primeQ2 = rsaKeySpec.getPrimeQ();
                BigInteger primeExponentP2 = rsaKeySpec.getPrimeExponentP();
                BigInteger primeExponentQ2 = rsaKeySpec.getPrimeExponentQ();
                BigInteger crtCoefficient2 = rsaKeySpec.getCrtCoefficient();
                byte[] byteArray = modulus.toByteArray();
                byte[] bArr6 = null;
                if (publicExponent2 == null) {
                    bArr = null;
                } else {
                    bArr = publicExponent2.toByteArray();
                }
                byte[] byteArray2 = privateExponent.toByteArray();
                if (primeP2 == null) {
                    bArr2 = null;
                } else {
                    bArr2 = primeP2.toByteArray();
                }
                if (primeQ2 == null) {
                    bArr3 = null;
                } else {
                    bArr3 = primeQ2.toByteArray();
                }
                if (primeExponentP2 == null) {
                    bArr4 = null;
                } else {
                    bArr4 = primeExponentP2.toByteArray();
                }
                if (primeExponentQ2 == null) {
                    bArr5 = null;
                } else {
                    bArr5 = primeExponentQ2.toByteArray();
                }
                if (crtCoefficient2 != null) {
                    bArr6 = crtCoefficient2.toByteArray();
                }
                return new OpenSSLKey(NativeCrypto.EVP_PKEY_new_RSA(byteArray, bArr, byteArray2, bArr2, bArr3, bArr4, bArr5, bArr6));
            } catch (Exception e) {
                throw new InvalidKeySpecException(e);
            }
        } else {
            throw new InvalidKeySpecException("privateExponent == null");
        }
    }

    static OpenSSLKey getInstance(RSAPrivateCrtKey rsaPrivateKey) throws InvalidKeyException {
        byte[] bArr;
        byte[] bArr2;
        byte[] bArr3;
        byte[] bArr4;
        byte[] bArr5;
        if (rsaPrivateKey.getFormat() == null) {
            return wrapPlatformKey(rsaPrivateKey);
        }
        BigInteger modulus = rsaPrivateKey.getModulus();
        BigInteger privateExponent = rsaPrivateKey.getPrivateExponent();
        if (modulus == null) {
            throw new InvalidKeyException("modulus == null");
        } else if (privateExponent != null) {
            try {
                BigInteger publicExponent2 = rsaPrivateKey.getPublicExponent();
                BigInteger primeP2 = rsaPrivateKey.getPrimeP();
                BigInteger primeQ2 = rsaPrivateKey.getPrimeQ();
                BigInteger primeExponentP2 = rsaPrivateKey.getPrimeExponentP();
                BigInteger primeExponentQ2 = rsaPrivateKey.getPrimeExponentQ();
                BigInteger crtCoefficient2 = rsaPrivateKey.getCrtCoefficient();
                byte[] byteArray = modulus.toByteArray();
                byte[] bArr6 = null;
                if (publicExponent2 == null) {
                    bArr = null;
                } else {
                    bArr = publicExponent2.toByteArray();
                }
                byte[] byteArray2 = privateExponent.toByteArray();
                if (primeP2 == null) {
                    bArr2 = null;
                } else {
                    bArr2 = primeP2.toByteArray();
                }
                if (primeQ2 == null) {
                    bArr3 = null;
                } else {
                    bArr3 = primeQ2.toByteArray();
                }
                if (primeExponentP2 == null) {
                    bArr4 = null;
                } else {
                    bArr4 = primeExponentP2.toByteArray();
                }
                if (primeExponentQ2 == null) {
                    bArr5 = null;
                } else {
                    bArr5 = primeExponentQ2.toByteArray();
                }
                if (crtCoefficient2 != null) {
                    bArr6 = crtCoefficient2.toByteArray();
                }
                return new OpenSSLKey(NativeCrypto.EVP_PKEY_new_RSA(byteArray, bArr, byteArray2, bArr2, bArr3, bArr4, bArr5, bArr6));
            } catch (Exception e) {
                throw new InvalidKeyException(e);
            }
        } else {
            throw new InvalidKeyException("privateExponent == null");
        }
    }

    /* access modifiers changed from: package-private */
    public synchronized void readParams(byte[][] params) {
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
        boolean z = true;
        if (o == this) {
            return true;
        }
        if (o instanceof OpenSSLRSAPrivateKey) {
            return getOpenSSLKey().equals(((OpenSSLRSAPrivateKey) o).getOpenSSLKey());
        }
        if (o instanceof RSAPrivateCrtKey) {
            ensureReadParams();
            RSAPrivateCrtKey other = (RSAPrivateCrtKey) o;
            if (!getModulus().equals(other.getModulus()) || !this.publicExponent.equals(other.getPublicExponent()) || !getPrivateExponent().equals(other.getPrivateExponent()) || !this.primeP.equals(other.getPrimeP()) || !this.primeQ.equals(other.getPrimeQ()) || !this.primeExponentP.equals(other.getPrimeExponentP()) || !this.primeExponentQ.equals(other.getPrimeExponentQ()) || !this.crtCoefficient.equals(other.getCrtCoefficient())) {
                z = false;
            }
            return z;
        } else if (!(o instanceof RSAPrivateKey)) {
            return false;
        } else {
            ensureReadParams();
            RSAPrivateKey other2 = (RSAPrivateKey) o;
            if (!getModulus().equals(other2.getModulus()) || !getPrivateExponent().equals(other2.getPrivateExponent())) {
                z = false;
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
        byte[] bArr;
        byte[] bArr2;
        byte[] bArr3;
        byte[] bArr4;
        stream.defaultReadObject();
        byte[] byteArray = this.modulus.toByteArray();
        byte[] bArr5 = null;
        byte[] byteArray2 = this.publicExponent == null ? null : this.publicExponent.toByteArray();
        byte[] byteArray3 = this.privateExponent.toByteArray();
        if (this.primeP == null) {
            bArr = null;
        } else {
            bArr = this.primeP.toByteArray();
        }
        if (this.primeQ == null) {
            bArr2 = null;
        } else {
            bArr2 = this.primeQ.toByteArray();
        }
        if (this.primeExponentP == null) {
            bArr3 = null;
        } else {
            bArr3 = this.primeExponentP.toByteArray();
        }
        if (this.primeExponentQ == null) {
            bArr4 = null;
        } else {
            bArr4 = this.primeExponentQ.toByteArray();
        }
        if (this.crtCoefficient != null) {
            bArr5 = this.crtCoefficient.toByteArray();
        }
        this.key = new OpenSSLKey(NativeCrypto.EVP_PKEY_new_RSA(byteArray, byteArray2, byteArray3, bArr, bArr2, bArr3, bArr4, bArr5));
        this.fetchedParams = true;
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        ensureReadParams();
        stream.defaultWriteObject();
    }
}

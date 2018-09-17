package com.android.org.bouncycastle.jcajce.provider.asymmetric.dh;

import com.android.org.bouncycastle.crypto.DerivationFunction;
import com.android.org.bouncycastle.jcajce.provider.asymmetric.util.BaseAgreementSpi;
import com.android.org.bouncycastle.jcajce.spec.UserKeyingMaterialSpec;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;
import javax.crypto.interfaces.DHPrivateKey;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class KeyAgreementSpi extends BaseAgreementSpi {
    private static final BigInteger ONE = BigInteger.valueOf(1);
    private static final BigInteger TWO = BigInteger.valueOf(2);
    private BigInteger g;
    private BigInteger p;
    private BigInteger result;
    private BigInteger x;

    public KeyAgreementSpi() {
        super("Diffie-Hellman", null);
    }

    public KeyAgreementSpi(String kaAlgorithm, DerivationFunction kdf) {
        super(kaAlgorithm, kdf);
    }

    protected byte[] bigIntToBytes(BigInteger r) {
        int expectedLength = (this.p.bitLength() + 7) / 8;
        byte[] tmp = r.toByteArray();
        if (tmp.length == expectedLength) {
            return tmp;
        }
        byte[] rv;
        if (tmp[0] == (byte) 0 && tmp.length == expectedLength + 1) {
            rv = new byte[(tmp.length - 1)];
            System.arraycopy(tmp, 1, rv, 0, rv.length);
            return rv;
        }
        rv = new byte[expectedLength];
        System.arraycopy(tmp, 0, rv, rv.length - tmp.length, tmp.length);
        return rv;
    }

    protected Key engineDoPhase(Key key, boolean lastPhase) throws InvalidKeyException, IllegalStateException {
        if (this.x == null) {
            throw new IllegalStateException("Diffie-Hellman not initialised.");
        } else if (key instanceof DHPublicKey) {
            DHPublicKey pubKey = (DHPublicKey) key;
            if (pubKey.getParams().getG().equals(this.g) && (pubKey.getParams().getP().equals(this.p) ^ 1) == 0) {
                BigInteger peerY = ((DHPublicKey) key).getY();
                if (peerY == null || peerY.compareTo(TWO) < 0 || peerY.compareTo(this.p.subtract(ONE)) >= 0) {
                    throw new InvalidKeyException("Invalid DH PublicKey");
                }
                this.result = peerY.modPow(this.x, this.p);
                if (this.result.compareTo(ONE) == 0) {
                    throw new InvalidKeyException("Shared key can't be 1");
                } else if (lastPhase) {
                    return null;
                } else {
                    return new BCDHPublicKey(this.result, pubKey.getParams());
                }
            }
            throw new InvalidKeyException("DHPublicKey not for this KeyAgreement!");
        } else {
            throw new InvalidKeyException("DHKeyAgreement doPhase requires DHPublicKey");
        }
    }

    protected byte[] engineGenerateSecret() throws IllegalStateException {
        if (this.x != null) {
            return super.engineGenerateSecret();
        }
        throw new IllegalStateException("Diffie-Hellman not initialised.");
    }

    protected int engineGenerateSecret(byte[] sharedSecret, int offset) throws IllegalStateException, ShortBufferException {
        if (this.x != null) {
            return super.engineGenerateSecret(sharedSecret, offset);
        }
        throw new IllegalStateException("Diffie-Hellman not initialised.");
    }

    protected SecretKey engineGenerateSecret(String algorithm) throws NoSuchAlgorithmException {
        if (this.x == null) {
            throw new IllegalStateException("Diffie-Hellman not initialised.");
        }
        byte[] res = bigIntToBytes(this.result);
        if (algorithm.equals("TlsPremasterSecret")) {
            return new SecretKeySpec(BaseAgreementSpi.trimZeroes(res), algorithm);
        }
        return super.engineGenerateSecret(algorithm);
    }

    protected void engineInit(Key key, AlgorithmParameterSpec params, SecureRandom random) throws InvalidKeyException, InvalidAlgorithmParameterException {
        if (key instanceof DHPrivateKey) {
            DHPrivateKey privKey = (DHPrivateKey) key;
            if (params == null) {
                this.p = privKey.getParams().getP();
                this.g = privKey.getParams().getG();
            } else if (params instanceof DHParameterSpec) {
                DHParameterSpec p = (DHParameterSpec) params;
                this.p = p.getP();
                this.g = p.getG();
            } else if (params instanceof UserKeyingMaterialSpec) {
                this.p = privKey.getParams().getP();
                this.g = privKey.getParams().getG();
                this.ukmParameters = ((UserKeyingMaterialSpec) params).getUserKeyingMaterial();
            } else {
                throw new InvalidAlgorithmParameterException("DHKeyAgreement only accepts DHParameterSpec");
            }
            BigInteger x = privKey.getX();
            this.result = x;
            this.x = x;
            return;
        }
        throw new InvalidKeyException("DHKeyAgreement requires DHPrivateKey for initialisation");
    }

    protected void engineInit(Key key, SecureRandom random) throws InvalidKeyException {
        if (key instanceof DHPrivateKey) {
            DHPrivateKey privKey = (DHPrivateKey) key;
            this.p = privKey.getParams().getP();
            this.g = privKey.getParams().getG();
            BigInteger x = privKey.getX();
            this.result = x;
            this.x = x;
            return;
        }
        throw new InvalidKeyException("DHKeyAgreement requires DHPrivateKey");
    }

    protected byte[] calcSecret() {
        return bigIntToBytes(this.result);
    }
}

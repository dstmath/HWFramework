package sun.security.util;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.interfaces.DSAKey;
import java.security.interfaces.DSAParams;
import java.security.interfaces.ECKey;
import java.security.interfaces.RSAKey;
import java.security.spec.ECParameterSpec;
import java.security.spec.KeySpec;
import javax.crypto.SecretKey;
import javax.crypto.interfaces.DHKey;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.DHPublicKeySpec;

public final class KeyUtil {
    public static final int getKeySize(Key key) {
        int size = -1;
        if (key instanceof Length) {
            try {
                size = ((Length) key).length();
            } catch (UnsupportedOperationException e) {
            }
            if (size >= 0) {
                return size;
            }
        }
        if (key instanceof SecretKey) {
            SecretKey sk = (SecretKey) key;
            if ("RAW".equals(sk.getFormat()) && sk.getEncoded() != null) {
                size = sk.getEncoded().length * 8;
            }
        } else if (key instanceof RSAKey) {
            size = ((RSAKey) key).getModulus().bitLength();
        } else if (key instanceof ECKey) {
            ECParameterSpec params = ((ECKey) key).getParams();
            if (params != null) {
                size = params.getOrder().bitLength();
            }
        } else if (key instanceof DSAKey) {
            DSAParams params2 = ((DSAKey) key).getParams();
            if (params2 != null) {
                size = params2.getP().bitLength();
            }
        } else if (key instanceof DHKey) {
            size = ((DHKey) key).getParams().getP().bitLength();
        }
        return size;
    }

    public static final void validate(Key key) throws InvalidKeyException {
        if (key == null) {
            throw new NullPointerException("The key to be validated cannot be null");
        } else if (key instanceof DHPublicKey) {
            validateDHPublicKey((DHPublicKey) key);
        }
    }

    public static final void validate(KeySpec keySpec) throws InvalidKeyException {
        if (keySpec == null) {
            throw new NullPointerException("The key spec to be validated cannot be null");
        } else if (keySpec instanceof DHPublicKeySpec) {
            validateDHPublicKey((DHPublicKeySpec) keySpec);
        }
    }

    public static final boolean isOracleJCEProvider(String providerName) {
        if (providerName == null) {
            return false;
        }
        if (providerName.equals("SunJCE")) {
            return true;
        }
        return providerName.startsWith("SunPKCS11");
    }

    private static void validateDHPublicKey(DHPublicKey publicKey) throws InvalidKeyException {
        DHParameterSpec paramSpec = publicKey.getParams();
        validateDHPublicKey(paramSpec.getP(), paramSpec.getG(), publicKey.getY());
    }

    private static void validateDHPublicKey(DHPublicKeySpec publicKeySpec) throws InvalidKeyException {
        validateDHPublicKey(publicKeySpec.getP(), publicKeySpec.getG(), publicKeySpec.getY());
    }

    private static void validateDHPublicKey(BigInteger p, BigInteger g, BigInteger y) throws InvalidKeyException {
        BigInteger leftOpen = BigInteger.ONE;
        BigInteger rightOpen = p.subtract(BigInteger.ONE);
        if (y.compareTo(leftOpen) <= 0) {
            throw new InvalidKeyException("Diffie-Hellman public key is too small");
        } else if (y.compareTo(rightOpen) >= 0) {
            throw new InvalidKeyException("Diffie-Hellman public key is too large");
        }
    }
}

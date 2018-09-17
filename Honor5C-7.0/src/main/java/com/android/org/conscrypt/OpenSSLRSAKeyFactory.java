package com.android.org.conscrypt;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactorySpi;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class OpenSSLRSAKeyFactory extends KeyFactorySpi {
    protected PublicKey engineGeneratePublic(KeySpec keySpec) throws InvalidKeySpecException {
        if (keySpec == null) {
            throw new InvalidKeySpecException("keySpec == null");
        } else if (keySpec instanceof RSAPublicKeySpec) {
            return new OpenSSLRSAPublicKey((RSAPublicKeySpec) keySpec);
        } else {
            if (keySpec instanceof X509EncodedKeySpec) {
                return OpenSSLKey.getPublicKey((X509EncodedKeySpec) keySpec, 6);
            }
            throw new InvalidKeySpecException("Must use RSAPublicKeySpec or X509EncodedKeySpec; was " + keySpec.getClass().getName());
        }
    }

    protected PrivateKey engineGeneratePrivate(KeySpec keySpec) throws InvalidKeySpecException {
        if (keySpec == null) {
            throw new InvalidKeySpecException("keySpec == null");
        } else if (keySpec instanceof RSAPrivateCrtKeySpec) {
            return new OpenSSLRSAPrivateCrtKey((RSAPrivateCrtKeySpec) keySpec);
        } else {
            if (keySpec instanceof RSAPrivateKeySpec) {
                return new OpenSSLRSAPrivateKey((RSAPrivateKeySpec) keySpec);
            }
            if (keySpec instanceof PKCS8EncodedKeySpec) {
                return OpenSSLKey.getPrivateKey((PKCS8EncodedKeySpec) keySpec, 6);
            }
            throw new InvalidKeySpecException("Must use RSAPublicKeySpec or PKCS8EncodedKeySpec; was " + keySpec.getClass().getName());
        }
    }

    protected <T extends KeySpec> T engineGetKeySpec(Key key, Class<T> keySpec) throws InvalidKeySpecException {
        if (key == null) {
            throw new InvalidKeySpecException("key == null");
        } else if (keySpec == null) {
            throw new InvalidKeySpecException("keySpec == null");
        } else if (!"RSA".equals(key.getAlgorithm())) {
            throw new InvalidKeySpecException("Key must be a RSA key");
        } else if ((key instanceof RSAPublicKey) && RSAPublicKeySpec.class.isAssignableFrom(keySpec)) {
            rsaKey = (RSAPublicKey) key;
            return new RSAPublicKeySpec(rsaKey.getModulus(), rsaKey.getPublicExponent());
        } else if ((key instanceof PublicKey) && RSAPublicKeySpec.class.isAssignableFrom(keySpec)) {
            encoded = key.getEncoded();
            if (!"X.509".equals(key.getFormat()) || encoded == null) {
                throw new InvalidKeySpecException("Not a valid X.509 encoding");
            }
            rsaKey = (RSAPublicKey) engineGeneratePublic(new X509EncodedKeySpec(encoded));
            return new RSAPublicKeySpec(rsaKey.getModulus(), rsaKey.getPublicExponent());
        } else if ((key instanceof RSAPrivateCrtKey) && RSAPrivateCrtKeySpec.class.isAssignableFrom(keySpec)) {
            rsaKey = (RSAPrivateCrtKey) key;
            return new RSAPrivateCrtKeySpec(rsaKey.getModulus(), rsaKey.getPublicExponent(), rsaKey.getPrivateExponent(), rsaKey.getPrimeP(), rsaKey.getPrimeQ(), rsaKey.getPrimeExponentP(), rsaKey.getPrimeExponentQ(), rsaKey.getCrtCoefficient());
        } else if ((key instanceof RSAPrivateCrtKey) && RSAPrivateKeySpec.class.isAssignableFrom(keySpec)) {
            rsaKey = (RSAPrivateCrtKey) key;
            return new RSAPrivateKeySpec(rsaKey.getModulus(), rsaKey.getPrivateExponent());
        } else if ((key instanceof RSAPrivateKey) && RSAPrivateKeySpec.class.isAssignableFrom(keySpec)) {
            rsaKey = (RSAPrivateKey) key;
            return new RSAPrivateKeySpec(rsaKey.getModulus(), rsaKey.getPrivateExponent());
        } else if ((key instanceof PrivateKey) && RSAPrivateCrtKeySpec.class.isAssignableFrom(keySpec)) {
            encoded = key.getEncoded();
            if (!"PKCS#8".equals(key.getFormat()) || encoded == null) {
                throw new InvalidKeySpecException("Not a valid PKCS#8 encoding");
            }
            RSAPrivateKey privKey = (RSAPrivateKey) engineGeneratePrivate(new PKCS8EncodedKeySpec(encoded));
            if (privKey instanceof RSAPrivateCrtKey) {
                rsaKey = (RSAPrivateCrtKey) privKey;
                return new RSAPrivateCrtKeySpec(rsaKey.getModulus(), rsaKey.getPublicExponent(), rsaKey.getPrivateExponent(), rsaKey.getPrimeP(), rsaKey.getPrimeQ(), rsaKey.getPrimeExponentP(), rsaKey.getPrimeExponentQ(), rsaKey.getCrtCoefficient());
            }
            throw new InvalidKeySpecException("Encoded key is not an RSAPrivateCrtKey");
        } else if ((key instanceof PrivateKey) && RSAPrivateKeySpec.class.isAssignableFrom(keySpec)) {
            encoded = key.getEncoded();
            if (!"PKCS#8".equals(key.getFormat()) || encoded == null) {
                throw new InvalidKeySpecException("Not a valid PKCS#8 encoding");
            }
            rsaKey = (RSAPrivateKey) engineGeneratePrivate(new PKCS8EncodedKeySpec(encoded));
            return new RSAPrivateKeySpec(rsaKey.getModulus(), rsaKey.getPrivateExponent());
        } else if ((key instanceof PrivateKey) && PKCS8EncodedKeySpec.class.isAssignableFrom(keySpec)) {
            encoded = key.getEncoded();
            if (!"PKCS#8".equals(key.getFormat())) {
                throw new InvalidKeySpecException("Encoding type must be PKCS#8; was " + key.getFormat());
            } else if (encoded != null) {
                return new PKCS8EncodedKeySpec(encoded);
            } else {
                throw new InvalidKeySpecException("Key is not encodable");
            }
        } else if ((key instanceof PublicKey) && X509EncodedKeySpec.class.isAssignableFrom(keySpec)) {
            encoded = key.getEncoded();
            if (!"X.509".equals(key.getFormat())) {
                throw new InvalidKeySpecException("Encoding type must be X.509; was " + key.getFormat());
            } else if (encoded != null) {
                return new X509EncodedKeySpec(encoded);
            } else {
                throw new InvalidKeySpecException("Key is not encodable");
            }
        } else {
            throw new InvalidKeySpecException("Unsupported key type and key spec combination; key=" + key.getClass().getName() + ", keySpec=" + keySpec.getName());
        }
    }

    protected Key engineTranslateKey(Key key) throws InvalidKeyException {
        if (key == null) {
            throw new InvalidKeyException("key == null");
        } else if ((key instanceof OpenSSLRSAPublicKey) || (key instanceof OpenSSLRSAPrivateKey)) {
            return key;
        } else {
            if (key instanceof RSAPublicKey) {
                RSAPublicKey rsaKey = (RSAPublicKey) key;
                try {
                    return engineGeneratePublic(new RSAPublicKeySpec(rsaKey.getModulus(), rsaKey.getPublicExponent()));
                } catch (InvalidKeySpecException e) {
                    throw new InvalidKeyException(e);
                }
            } else if (key instanceof RSAPrivateCrtKey) {
                RSAPrivateCrtKey rsaKey2 = (RSAPrivateCrtKey) key;
                try {
                    return engineGeneratePrivate(new RSAPrivateCrtKeySpec(rsaKey2.getModulus(), rsaKey2.getPublicExponent(), rsaKey2.getPrivateExponent(), rsaKey2.getPrimeP(), rsaKey2.getPrimeQ(), rsaKey2.getPrimeExponentP(), rsaKey2.getPrimeExponentQ(), rsaKey2.getCrtCoefficient()));
                } catch (InvalidKeySpecException e2) {
                    throw new InvalidKeyException(e2);
                }
            } else if (key instanceof RSAPrivateKey) {
                RSAPrivateKey rsaKey3 = (RSAPrivateKey) key;
                try {
                    return engineGeneratePrivate(new RSAPrivateKeySpec(rsaKey3.getModulus(), rsaKey3.getPrivateExponent()));
                } catch (InvalidKeySpecException e22) {
                    throw new InvalidKeyException(e22);
                }
            } else if ((key instanceof PrivateKey) && "PKCS#8".equals(key.getFormat())) {
                encoded = key.getEncoded();
                if (encoded == null) {
                    throw new InvalidKeyException("Key does not support encoding");
                }
                try {
                    return engineGeneratePrivate(new PKCS8EncodedKeySpec(encoded));
                } catch (InvalidKeySpecException e222) {
                    throw new InvalidKeyException(e222);
                }
            } else if ((key instanceof PublicKey) && "X.509".equals(key.getFormat())) {
                encoded = key.getEncoded();
                if (encoded == null) {
                    throw new InvalidKeyException("Key does not support encoding");
                }
                try {
                    return engineGeneratePublic(new X509EncodedKeySpec(encoded));
                } catch (InvalidKeySpecException e2222) {
                    throw new InvalidKeyException(e2222);
                }
            } else {
                throw new InvalidKeyException("Key must be an RSA public or private key; was " + key.getClass().getName());
            }
        }
    }
}

package com.android.org.conscrypt;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactorySpi;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECPrivateKeySpec;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class OpenSSLECKeyFactory extends KeyFactorySpi {
    protected PublicKey engineGeneratePublic(KeySpec keySpec) throws InvalidKeySpecException {
        if (keySpec == null) {
            throw new InvalidKeySpecException("keySpec == null");
        } else if (keySpec instanceof ECPublicKeySpec) {
            return new OpenSSLECPublicKey((ECPublicKeySpec) keySpec);
        } else {
            if (keySpec instanceof X509EncodedKeySpec) {
                return OpenSSLKey.getPublicKey((X509EncodedKeySpec) keySpec, NativeConstants.EVP_PKEY_EC);
            }
            throw new InvalidKeySpecException("Must use ECPublicKeySpec or X509EncodedKeySpec; was " + keySpec.getClass().getName());
        }
    }

    protected PrivateKey engineGeneratePrivate(KeySpec keySpec) throws InvalidKeySpecException {
        if (keySpec == null) {
            throw new InvalidKeySpecException("keySpec == null");
        } else if (keySpec instanceof ECPrivateKeySpec) {
            return new OpenSSLECPrivateKey((ECPrivateKeySpec) keySpec);
        } else {
            if (keySpec instanceof PKCS8EncodedKeySpec) {
                return OpenSSLKey.getPrivateKey((PKCS8EncodedKeySpec) keySpec, NativeConstants.EVP_PKEY_EC);
            }
            throw new InvalidKeySpecException("Must use ECPrivateKeySpec or PKCS8EncodedKeySpec; was " + keySpec.getClass().getName());
        }
    }

    protected <T extends KeySpec> T engineGetKeySpec(Key key, Class<T> keySpec) throws InvalidKeySpecException {
        if (key == null) {
            throw new InvalidKeySpecException("key == null");
        } else if (keySpec == null) {
            throw new InvalidKeySpecException("keySpec == null");
        } else if (!"EC".equals(key.getAlgorithm())) {
            throw new InvalidKeySpecException("Key must be an EC key");
        } else if ((key instanceof ECPublicKey) && ECPublicKeySpec.class.isAssignableFrom(keySpec)) {
            ecKey = (ECPublicKey) key;
            return new ECPublicKeySpec(ecKey.getW(), ecKey.getParams());
        } else if ((key instanceof PublicKey) && ECPublicKeySpec.class.isAssignableFrom(keySpec)) {
            encoded = key.getEncoded();
            if (!"X.509".equals(key.getFormat()) || encoded == null) {
                throw new InvalidKeySpecException("Not a valid X.509 encoding");
            }
            ecKey = (ECPublicKey) engineGeneratePublic(new X509EncodedKeySpec(encoded));
            return new ECPublicKeySpec(ecKey.getW(), ecKey.getParams());
        } else if ((key instanceof ECPrivateKey) && ECPrivateKeySpec.class.isAssignableFrom(keySpec)) {
            ecKey = (ECPrivateKey) key;
            return new ECPrivateKeySpec(ecKey.getS(), ecKey.getParams());
        } else if ((key instanceof PrivateKey) && ECPrivateKeySpec.class.isAssignableFrom(keySpec)) {
            encoded = key.getEncoded();
            if (!"PKCS#8".equals(key.getFormat()) || encoded == null) {
                throw new InvalidKeySpecException("Not a valid PKCS#8 encoding");
            }
            ecKey = (ECPrivateKey) engineGeneratePrivate(new PKCS8EncodedKeySpec(encoded));
            return new ECPrivateKeySpec(ecKey.getS(), ecKey.getParams());
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
        } else if ((key instanceof OpenSSLECPublicKey) || (key instanceof OpenSSLECPrivateKey)) {
            return key;
        } else {
            if (key instanceof ECPublicKey) {
                ECPublicKey ecKey = (ECPublicKey) key;
                try {
                    return engineGeneratePublic(new ECPublicKeySpec(ecKey.getW(), ecKey.getParams()));
                } catch (InvalidKeySpecException e) {
                    throw new InvalidKeyException(e);
                }
            } else if (key instanceof ECPrivateKey) {
                ECPrivateKey ecKey2 = (ECPrivateKey) key;
                try {
                    return engineGeneratePrivate(new ECPrivateKeySpec(ecKey2.getS(), ecKey2.getParams()));
                } catch (InvalidKeySpecException e2) {
                    throw new InvalidKeyException(e2);
                }
            } else if ((key instanceof PrivateKey) && "PKCS#8".equals(key.getFormat())) {
                encoded = key.getEncoded();
                if (encoded == null) {
                    throw new InvalidKeyException("Key does not support encoding");
                }
                try {
                    return engineGeneratePrivate(new PKCS8EncodedKeySpec(encoded));
                } catch (InvalidKeySpecException e22) {
                    throw new InvalidKeyException(e22);
                }
            } else if ((key instanceof PublicKey) && "X.509".equals(key.getFormat())) {
                encoded = key.getEncoded();
                if (encoded == null) {
                    throw new InvalidKeyException("Key does not support encoding");
                }
                try {
                    return engineGeneratePublic(new X509EncodedKeySpec(encoded));
                } catch (InvalidKeySpecException e222) {
                    throw new InvalidKeyException(e222);
                }
            } else {
                throw new InvalidKeyException("Key must be EC public or private key; was " + key.getClass().getName());
            }
        }
    }
}

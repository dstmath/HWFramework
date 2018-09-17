package com.android.org.bouncycastle.jcajce.provider.symmetric.util;

import com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import java.security.InvalidKeyException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactorySpi;
import javax.crypto.spec.SecretKeySpec;

public class BaseSecretKeyFactory extends SecretKeyFactorySpi implements PBE {
    protected String algName;
    protected ASN1ObjectIdentifier algOid;

    protected BaseSecretKeyFactory(String algName, ASN1ObjectIdentifier algOid) {
        this.algName = algName;
        this.algOid = algOid;
    }

    protected SecretKey engineGenerateSecret(KeySpec keySpec) throws InvalidKeySpecException {
        if (keySpec instanceof SecretKeySpec) {
            return new SecretKeySpec(((SecretKeySpec) keySpec).getEncoded(), this.algName);
        }
        throw new InvalidKeySpecException("Invalid KeySpec");
    }

    protected KeySpec engineGetKeySpec(SecretKey key, Class keySpec) throws InvalidKeySpecException {
        if (keySpec == null) {
            throw new InvalidKeySpecException("keySpec parameter is null");
        } else if (key == null) {
            throw new InvalidKeySpecException("key parameter is null");
        } else if (SecretKeySpec.class.isAssignableFrom(keySpec)) {
            return new SecretKeySpec(key.getEncoded(), this.algName);
        } else {
            try {
                return (KeySpec) keySpec.getConstructor(new Class[]{byte[].class}).newInstance(new Object[]{key.getEncoded()});
            } catch (Exception e) {
                throw new InvalidKeySpecException(e.toString());
            }
        }
    }

    protected SecretKey engineTranslateKey(SecretKey key) throws InvalidKeyException {
        if (key == null) {
            throw new InvalidKeyException("key parameter is null");
        } else if (key.getAlgorithm().equalsIgnoreCase(this.algName)) {
            return new SecretKeySpec(key.getEncoded(), this.algName);
        } else {
            throw new InvalidKeyException("Key not of type " + this.algName + ".");
        }
    }
}

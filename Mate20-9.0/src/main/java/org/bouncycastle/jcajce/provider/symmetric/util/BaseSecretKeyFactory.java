package org.bouncycastle.jcajce.provider.symmetric.util;

import java.security.InvalidKeyException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactorySpi;
import javax.crypto.spec.SecretKeySpec;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;

public class BaseSecretKeyFactory extends SecretKeyFactorySpi implements PBE {
    protected String algName;
    protected ASN1ObjectIdentifier algOid;

    protected BaseSecretKeyFactory(String str, ASN1ObjectIdentifier aSN1ObjectIdentifier) {
        this.algName = str;
        this.algOid = aSN1ObjectIdentifier;
    }

    /* access modifiers changed from: protected */
    public SecretKey engineGenerateSecret(KeySpec keySpec) throws InvalidKeySpecException {
        if (keySpec instanceof SecretKeySpec) {
            return new SecretKeySpec(((SecretKeySpec) keySpec).getEncoded(), this.algName);
        }
        throw new InvalidKeySpecException("Invalid KeySpec");
    }

    /* access modifiers changed from: protected */
    public KeySpec engineGetKeySpec(SecretKey secretKey, Class cls) throws InvalidKeySpecException {
        if (cls == null) {
            throw new InvalidKeySpecException("keySpec parameter is null");
        } else if (secretKey == null) {
            throw new InvalidKeySpecException("key parameter is null");
        } else if (SecretKeySpec.class.isAssignableFrom(cls)) {
            return new SecretKeySpec(secretKey.getEncoded(), this.algName);
        } else {
            try {
                return (KeySpec) cls.getConstructor(new Class[]{byte[].class}).newInstance(new Object[]{secretKey.getEncoded()});
            } catch (Exception e) {
                throw new InvalidKeySpecException(e.toString());
            }
        }
    }

    /* access modifiers changed from: protected */
    public SecretKey engineTranslateKey(SecretKey secretKey) throws InvalidKeyException {
        if (secretKey == null) {
            throw new InvalidKeyException("key parameter is null");
        } else if (secretKey.getAlgorithm().equalsIgnoreCase(this.algName)) {
            return new SecretKeySpec(secretKey.getEncoded(), this.algName);
        } else {
            throw new InvalidKeyException("Key not of type " + this.algName + ".");
        }
    }
}

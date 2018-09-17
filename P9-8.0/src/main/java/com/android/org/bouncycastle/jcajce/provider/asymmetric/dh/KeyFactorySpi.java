package com.android.org.bouncycastle.jcajce.provider.asymmetric.dh;

import com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import com.android.org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import com.android.org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import com.android.org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import com.android.org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import com.android.org.bouncycastle.jcajce.provider.asymmetric.util.BaseKeyFactorySpi;
import com.android.org.bouncycastle.jcajce.provider.asymmetric.util.ExtendedInvalidKeySpecException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import javax.crypto.interfaces.DHPrivateKey;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHPrivateKeySpec;
import javax.crypto.spec.DHPublicKeySpec;

public class KeyFactorySpi extends BaseKeyFactorySpi {
    protected KeySpec engineGetKeySpec(Key key, Class spec) throws InvalidKeySpecException {
        if (spec.isAssignableFrom(DHPrivateKeySpec.class) && (key instanceof DHPrivateKey)) {
            DHPrivateKey k = (DHPrivateKey) key;
            return new DHPrivateKeySpec(k.getX(), k.getParams().getP(), k.getParams().getG());
        } else if (!spec.isAssignableFrom(DHPublicKeySpec.class) || !(key instanceof DHPublicKey)) {
            return super.engineGetKeySpec(key, spec);
        } else {
            DHPublicKey k2 = (DHPublicKey) key;
            return new DHPublicKeySpec(k2.getY(), k2.getParams().getP(), k2.getParams().getG());
        }
    }

    protected Key engineTranslateKey(Key key) throws InvalidKeyException {
        if (key instanceof DHPublicKey) {
            return new BCDHPublicKey((DHPublicKey) key);
        }
        if (key instanceof DHPrivateKey) {
            return new BCDHPrivateKey((DHPrivateKey) key);
        }
        throw new InvalidKeyException("key type unknown");
    }

    protected PrivateKey engineGeneratePrivate(KeySpec keySpec) throws InvalidKeySpecException {
        if (keySpec instanceof DHPrivateKeySpec) {
            return new BCDHPrivateKey((DHPrivateKeySpec) keySpec);
        }
        return super.engineGeneratePrivate(keySpec);
    }

    protected PublicKey engineGeneratePublic(KeySpec keySpec) throws InvalidKeySpecException {
        if (!(keySpec instanceof DHPublicKeySpec)) {
            return super.engineGeneratePublic(keySpec);
        }
        try {
            return new BCDHPublicKey((DHPublicKeySpec) keySpec);
        } catch (IllegalArgumentException e) {
            throw new ExtendedInvalidKeySpecException(e.getMessage(), e);
        }
    }

    public PrivateKey generatePrivate(PrivateKeyInfo keyInfo) throws IOException {
        ASN1ObjectIdentifier algOid = keyInfo.getPrivateKeyAlgorithm().getAlgorithm();
        if (algOid.equals(PKCSObjectIdentifiers.dhKeyAgreement)) {
            return new BCDHPrivateKey(keyInfo);
        }
        if (algOid.equals(X9ObjectIdentifiers.dhpublicnumber)) {
            return new BCDHPrivateKey(keyInfo);
        }
        throw new IOException("algorithm identifier " + algOid + " in key not recognised");
    }

    public PublicKey generatePublic(SubjectPublicKeyInfo keyInfo) throws IOException {
        ASN1ObjectIdentifier algOid = keyInfo.getAlgorithm().getAlgorithm();
        if (algOid.equals(PKCSObjectIdentifiers.dhKeyAgreement)) {
            return new BCDHPublicKey(keyInfo);
        }
        if (algOid.equals(X9ObjectIdentifiers.dhpublicnumber)) {
            return new BCDHPublicKey(keyInfo);
        }
        throw new IOException("algorithm identifier " + algOid + " in key not recognised");
    }
}

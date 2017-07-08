package com.android.org.bouncycastle.jcajce.provider.symmetric.util;

import com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import com.android.org.bouncycastle.crypto.CipherParameters;
import com.android.org.bouncycastle.jcajce.provider.symmetric.util.PBE.Util;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import javax.crypto.SecretKey;
import javax.crypto.spec.PBEKeySpec;

public class PBESecretKeyFactory extends BaseSecretKeyFactory implements PBE {
    private int digest;
    private boolean forCipher;
    private int ivSize;
    private int keySize;
    private int scheme;

    public PBESecretKeyFactory(String algorithm, ASN1ObjectIdentifier oid, boolean forCipher, int scheme, int digest, int keySize, int ivSize) {
        super(algorithm, oid);
        this.forCipher = forCipher;
        this.scheme = scheme;
        this.digest = digest;
        this.keySize = keySize;
        this.ivSize = ivSize;
    }

    protected SecretKey engineGenerateSecret(KeySpec keySpec) throws InvalidKeySpecException {
        if (keySpec instanceof PBEKeySpec) {
            PBEKeySpec pbeSpec = (PBEKeySpec) keySpec;
            if (pbeSpec.getSalt() == null) {
                return new BCPBEKey(this.algName, this.algOid, this.scheme, this.digest, this.keySize, this.ivSize, pbeSpec, null);
            }
            CipherParameters param;
            if (this.forCipher) {
                param = Util.makePBEParameters(pbeSpec, this.scheme, this.digest, this.keySize, this.ivSize);
            } else {
                param = Util.makePBEMacParameters(pbeSpec, this.scheme, this.digest, this.keySize);
            }
            return new BCPBEKey(this.algName, this.algOid, this.scheme, this.digest, this.keySize, this.ivSize, pbeSpec, param);
        }
        throw new InvalidKeySpecException("Invalid KeySpec");
    }
}

package com.android.org.bouncycastle.jcajce.provider.symmetric.util;

import com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import com.android.org.bouncycastle.crypto.CipherParameters;
import com.android.org.bouncycastle.jcajce.provider.symmetric.util.PBE;
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

    public PBESecretKeyFactory(String algorithm, ASN1ObjectIdentifier oid, boolean forCipher2, int scheme2, int digest2, int keySize2, int ivSize2) {
        super(algorithm, oid);
        this.forCipher = forCipher2;
        this.scheme = scheme2;
        this.digest = digest2;
        this.keySize = keySize2;
        this.ivSize = ivSize2;
    }

    /* access modifiers changed from: protected */
    public SecretKey engineGenerateSecret(KeySpec keySpec) throws InvalidKeySpecException {
        CipherParameters param;
        if (keySpec instanceof PBEKeySpec) {
            PBEKeySpec pbeSpec = (PBEKeySpec) keySpec;
            if (pbeSpec.getSalt() == null) {
                BCPBEKey bCPBEKey = new BCPBEKey(this.algName, this.algOid, this.scheme, this.digest, this.keySize, this.ivSize, pbeSpec, null);
                return bCPBEKey;
            }
            if (this.forCipher) {
                param = PBE.Util.makePBEParameters(pbeSpec, this.scheme, this.digest, this.keySize, this.ivSize);
            } else {
                param = PBE.Util.makePBEMacParameters(pbeSpec, this.scheme, this.digest, this.keySize);
            }
            BCPBEKey bCPBEKey2 = new BCPBEKey(this.algName, this.algOid, this.scheme, this.digest, this.keySize, this.ivSize, pbeSpec, param);
            return bCPBEKey2;
        }
        throw new InvalidKeySpecException("Invalid KeySpec");
    }
}

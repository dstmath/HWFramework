package com.android.org.bouncycastle.jcajce.spec;

import com.android.org.bouncycastle.asn1.DERNull;
import com.android.org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import com.android.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import javax.crypto.spec.PBEKeySpec;

public class PBKDF2KeySpec extends PBEKeySpec {
    private static final AlgorithmIdentifier defaultPRF = new AlgorithmIdentifier(PKCSObjectIdentifiers.id_hmacWithSHA1, DERNull.INSTANCE);
    private AlgorithmIdentifier prf;

    public PBKDF2KeySpec(char[] password, byte[] salt, int iterationCount, int keySize, AlgorithmIdentifier prf) {
        super(password, salt, iterationCount, keySize);
        this.prf = prf;
    }

    public boolean isDefaultPrf() {
        return defaultPRF.equals(this.prf);
    }

    public AlgorithmIdentifier getPrf() {
        return this.prf;
    }
}

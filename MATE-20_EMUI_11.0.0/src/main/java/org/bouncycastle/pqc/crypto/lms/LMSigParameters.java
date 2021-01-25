package org.bouncycastle.pqc.crypto.lms;

import java.util.HashMap;
import java.util.Map;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;

public class LMSigParameters {
    public static final LMSigParameters lms_sha256_n32_h10 = new LMSigParameters(6, 32, 10, NISTObjectIdentifiers.id_sha256);
    public static final LMSigParameters lms_sha256_n32_h15 = new LMSigParameters(7, 32, 15, NISTObjectIdentifiers.id_sha256);
    public static final LMSigParameters lms_sha256_n32_h20 = new LMSigParameters(8, 32, 20, NISTObjectIdentifiers.id_sha256);
    public static final LMSigParameters lms_sha256_n32_h25 = new LMSigParameters(9, 32, 25, NISTObjectIdentifiers.id_sha256);
    public static final LMSigParameters lms_sha256_n32_h5 = new LMSigParameters(5, 32, 5, NISTObjectIdentifiers.id_sha256);
    private static Map<Object, LMSigParameters> paramBuilders = new HashMap<Object, LMSigParameters>() {
        /* class org.bouncycastle.pqc.crypto.lms.LMSigParameters.AnonymousClass1 */

        {
            put(Integer.valueOf(LMSigParameters.lms_sha256_n32_h5.type), LMSigParameters.lms_sha256_n32_h5);
            put(Integer.valueOf(LMSigParameters.lms_sha256_n32_h10.type), LMSigParameters.lms_sha256_n32_h10);
            put(Integer.valueOf(LMSigParameters.lms_sha256_n32_h15.type), LMSigParameters.lms_sha256_n32_h15);
            put(Integer.valueOf(LMSigParameters.lms_sha256_n32_h20.type), LMSigParameters.lms_sha256_n32_h20);
            put(Integer.valueOf(LMSigParameters.lms_sha256_n32_h25.type), LMSigParameters.lms_sha256_n32_h25);
        }
    };
    private final ASN1ObjectIdentifier digestOid;
    private final int h;
    private final int m;
    private final int type;

    protected LMSigParameters(int i, int i2, int i3, ASN1ObjectIdentifier aSN1ObjectIdentifier) {
        this.type = i;
        this.m = i2;
        this.h = i3;
        this.digestOid = aSN1ObjectIdentifier;
    }

    static LMSigParameters getParametersForType(int i) {
        return paramBuilders.get(Integer.valueOf(i));
    }

    public ASN1ObjectIdentifier getDigestOID() {
        return this.digestOid;
    }

    public int getH() {
        return this.h;
    }

    public int getM() {
        return this.m;
    }

    public int getType() {
        return this.type;
    }
}

package org.bouncycastle.asn1.ocsp;

import java.math.BigInteger;
import org.bouncycastle.asn1.ASN1Enumerated;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;

public class OCSPResponseStatus extends ASN1Object {
    public static final int INTERNAL_ERROR = 2;
    public static final int MALFORMED_REQUEST = 1;
    public static final int SIG_REQUIRED = 5;
    public static final int SUCCESSFUL = 0;
    public static final int TRY_LATER = 3;
    public static final int UNAUTHORIZED = 6;
    private ASN1Enumerated value;

    public OCSPResponseStatus(int i) {
        this(new ASN1Enumerated(i));
    }

    private OCSPResponseStatus(ASN1Enumerated aSN1Enumerated) {
        this.value = aSN1Enumerated;
    }

    public static OCSPResponseStatus getInstance(Object obj) {
        if (obj instanceof OCSPResponseStatus) {
            return (OCSPResponseStatus) obj;
        }
        if (obj != null) {
            return new OCSPResponseStatus(ASN1Enumerated.getInstance(obj));
        }
        return null;
    }

    public BigInteger getValue() {
        return this.value.getValue();
    }

    public ASN1Primitive toASN1Primitive() {
        return this.value;
    }
}

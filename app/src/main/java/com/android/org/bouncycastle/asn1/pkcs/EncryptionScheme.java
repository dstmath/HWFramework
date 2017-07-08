package com.android.org.bouncycastle.asn1.pkcs;

import com.android.org.bouncycastle.asn1.ASN1Encodable;
import com.android.org.bouncycastle.asn1.ASN1Object;
import com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import com.android.org.bouncycastle.asn1.ASN1Primitive;
import com.android.org.bouncycastle.asn1.ASN1Sequence;
import com.android.org.bouncycastle.asn1.x509.AlgorithmIdentifier;

public class EncryptionScheme extends ASN1Object {
    private AlgorithmIdentifier algId;

    public EncryptionScheme(ASN1ObjectIdentifier objectId, ASN1Encodable parameters) {
        this.algId = new AlgorithmIdentifier(objectId, parameters);
    }

    private EncryptionScheme(ASN1Sequence seq) {
        this.algId = AlgorithmIdentifier.getInstance(seq);
    }

    public static EncryptionScheme getInstance(Object obj) {
        if (obj instanceof EncryptionScheme) {
            return (EncryptionScheme) obj;
        }
        if (obj != null) {
            return new EncryptionScheme(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public ASN1ObjectIdentifier getAlgorithm() {
        return this.algId.getAlgorithm();
    }

    public ASN1Encodable getParameters() {
        return this.algId.getParameters();
    }

    public ASN1Primitive toASN1Primitive() {
        return this.algId.toASN1Primitive();
    }
}

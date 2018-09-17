package com.android.org.bouncycastle.asn1.pkcs;

import com.android.org.bouncycastle.asn1.ASN1Encodable;
import com.android.org.bouncycastle.asn1.ASN1EncodableVector;
import com.android.org.bouncycastle.asn1.ASN1Integer;
import com.android.org.bouncycastle.asn1.ASN1Object;
import com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import com.android.org.bouncycastle.asn1.ASN1OctetString;
import com.android.org.bouncycastle.asn1.ASN1Primitive;
import com.android.org.bouncycastle.asn1.ASN1Sequence;
import com.android.org.bouncycastle.asn1.ASN1TaggedObject;
import com.android.org.bouncycastle.asn1.BERSequence;
import com.android.org.bouncycastle.asn1.BERTaggedObject;
import com.android.org.bouncycastle.asn1.x509.AlgorithmIdentifier;

public class EncryptedData extends ASN1Object {
    ASN1ObjectIdentifier bagId;
    ASN1Primitive bagValue;
    ASN1Sequence data;

    public static EncryptedData getInstance(Object obj) {
        if (obj instanceof EncryptedData) {
            return (EncryptedData) obj;
        }
        if (obj != null) {
            return new EncryptedData(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    private EncryptedData(ASN1Sequence seq) {
        if (((ASN1Integer) seq.getObjectAt(0)).getValue().intValue() != 0) {
            throw new IllegalArgumentException("sequence not version 0");
        }
        this.data = ASN1Sequence.getInstance(seq.getObjectAt(1));
    }

    public EncryptedData(ASN1ObjectIdentifier contentType, AlgorithmIdentifier encryptionAlgorithm, ASN1Encodable content) {
        ASN1EncodableVector v = new ASN1EncodableVector();
        v.add(contentType);
        v.add(encryptionAlgorithm.toASN1Primitive());
        v.add(new BERTaggedObject(false, 0, content));
        this.data = new BERSequence(v);
    }

    public ASN1ObjectIdentifier getContentType() {
        return ASN1ObjectIdentifier.getInstance(this.data.getObjectAt(0));
    }

    public AlgorithmIdentifier getEncryptionAlgorithm() {
        return AlgorithmIdentifier.getInstance(this.data.getObjectAt(1));
    }

    public ASN1OctetString getContent() {
        if (this.data.size() == 3) {
            return ASN1OctetString.getInstance(ASN1TaggedObject.getInstance(this.data.getObjectAt(2)), false);
        }
        return null;
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector v = new ASN1EncodableVector();
        v.add(new ASN1Integer(0));
        v.add(this.data);
        return new BERSequence(v);
    }
}

package com.android.org.bouncycastle.asn1.x509;

import com.android.org.bouncycastle.asn1.ASN1EncodableVector;
import com.android.org.bouncycastle.asn1.ASN1Object;
import com.android.org.bouncycastle.asn1.ASN1OctetString;
import com.android.org.bouncycastle.asn1.ASN1Primitive;
import com.android.org.bouncycastle.asn1.ASN1Sequence;
import com.android.org.bouncycastle.asn1.ASN1TaggedObject;
import com.android.org.bouncycastle.asn1.DEROctetString;
import com.android.org.bouncycastle.asn1.DERSequence;
import java.util.Enumeration;

public class DigestInfo extends ASN1Object {
    private AlgorithmIdentifier algId;
    private byte[] digest;

    public static DigestInfo getInstance(ASN1TaggedObject obj, boolean explicit) {
        return getInstance(ASN1Sequence.getInstance(obj, explicit));
    }

    public static DigestInfo getInstance(Object obj) {
        if (obj instanceof DigestInfo) {
            return (DigestInfo) obj;
        }
        if (obj != null) {
            return new DigestInfo(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public DigestInfo(AlgorithmIdentifier algId, byte[] digest) {
        this.digest = digest;
        this.algId = algId;
    }

    public DigestInfo(ASN1Sequence obj) {
        Enumeration e = obj.getObjects();
        this.algId = AlgorithmIdentifier.getInstance(e.nextElement());
        this.digest = ASN1OctetString.getInstance(e.nextElement()).getOctets();
    }

    public AlgorithmIdentifier getAlgorithmId() {
        return this.algId;
    }

    public byte[] getDigest() {
        return this.digest;
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector v = new ASN1EncodableVector();
        v.add(this.algId);
        v.add(new DEROctetString(this.digest));
        return new DERSequence(v);
    }
}

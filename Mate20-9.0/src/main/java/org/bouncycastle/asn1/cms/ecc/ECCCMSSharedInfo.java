package org.bouncycastle.asn1.cms.ecc;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.util.Arrays;

public class ECCCMSSharedInfo extends ASN1Object {
    private final byte[] entityUInfo;
    private final AlgorithmIdentifier keyInfo;
    private final byte[] suppPubInfo;

    private ECCCMSSharedInfo(ASN1Sequence aSN1Sequence) {
        ASN1Encodable objectAt;
        this.keyInfo = AlgorithmIdentifier.getInstance(aSN1Sequence.getObjectAt(0));
        if (aSN1Sequence.size() == 2) {
            this.entityUInfo = null;
            objectAt = aSN1Sequence.getObjectAt(1);
        } else {
            this.entityUInfo = ASN1OctetString.getInstance((ASN1TaggedObject) aSN1Sequence.getObjectAt(1), true).getOctets();
            objectAt = aSN1Sequence.getObjectAt(2);
        }
        this.suppPubInfo = ASN1OctetString.getInstance((ASN1TaggedObject) objectAt, true).getOctets();
    }

    public ECCCMSSharedInfo(AlgorithmIdentifier algorithmIdentifier, byte[] bArr) {
        this.keyInfo = algorithmIdentifier;
        this.entityUInfo = null;
        this.suppPubInfo = Arrays.clone(bArr);
    }

    public ECCCMSSharedInfo(AlgorithmIdentifier algorithmIdentifier, byte[] bArr, byte[] bArr2) {
        this.keyInfo = algorithmIdentifier;
        this.entityUInfo = Arrays.clone(bArr);
        this.suppPubInfo = Arrays.clone(bArr2);
    }

    public static ECCCMSSharedInfo getInstance(Object obj) {
        if (obj instanceof ECCCMSSharedInfo) {
            return (ECCCMSSharedInfo) obj;
        }
        if (obj != null) {
            return new ECCCMSSharedInfo(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public static ECCCMSSharedInfo getInstance(ASN1TaggedObject aSN1TaggedObject, boolean z) {
        return getInstance(ASN1Sequence.getInstance(aSN1TaggedObject, z));
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        aSN1EncodableVector.add(this.keyInfo);
        if (this.entityUInfo != null) {
            aSN1EncodableVector.add(new DERTaggedObject(true, 0, new DEROctetString(this.entityUInfo)));
        }
        aSN1EncodableVector.add(new DERTaggedObject(true, 2, new DEROctetString(this.suppPubInfo)));
        return new DERSequence(aSN1EncodableVector);
    }
}

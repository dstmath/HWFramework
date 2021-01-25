package org.bouncycastle.asn1.crmf;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.cms.IssuerAndSerialNumber;
import org.bouncycastle.util.Arrays;

public class DhSigStatic extends ASN1Object {
    private final ASN1OctetString hashValue;
    private final IssuerAndSerialNumber issuerAndSerial;

    private DhSigStatic(ASN1Sequence aSN1Sequence) {
        ASN1Encodable objectAt;
        if (aSN1Sequence.size() == 1) {
            this.issuerAndSerial = null;
            objectAt = aSN1Sequence.getObjectAt(0);
        } else if (aSN1Sequence.size() == 2) {
            this.issuerAndSerial = IssuerAndSerialNumber.getInstance(aSN1Sequence.getObjectAt(0));
            objectAt = aSN1Sequence.getObjectAt(1);
        } else {
            throw new IllegalArgumentException("sequence wrong length for DhSigStatic");
        }
        this.hashValue = ASN1OctetString.getInstance(objectAt);
    }

    public DhSigStatic(IssuerAndSerialNumber issuerAndSerialNumber, byte[] bArr) {
        this.issuerAndSerial = issuerAndSerialNumber;
        this.hashValue = new DEROctetString(Arrays.clone(bArr));
    }

    public DhSigStatic(byte[] bArr) {
        this(null, bArr);
    }

    public static DhSigStatic getInstance(Object obj) {
        if (obj instanceof DhSigStatic) {
            return (DhSigStatic) obj;
        }
        if (obj != null) {
            return new DhSigStatic(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public byte[] getHashValue() {
        return Arrays.clone(this.hashValue.getOctets());
    }

    public IssuerAndSerialNumber getIssuerAndSerial() {
        return this.issuerAndSerial;
    }

    @Override // org.bouncycastle.asn1.ASN1Object, org.bouncycastle.asn1.ASN1Encodable
    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector(2);
        IssuerAndSerialNumber issuerAndSerialNumber = this.issuerAndSerial;
        if (issuerAndSerialNumber != null) {
            aSN1EncodableVector.add(issuerAndSerialNumber);
        }
        aSN1EncodableVector.add(this.hashValue);
        return new DERSequence(aSN1EncodableVector);
    }
}

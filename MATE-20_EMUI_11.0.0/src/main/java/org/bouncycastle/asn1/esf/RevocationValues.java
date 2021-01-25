package org.bouncycastle.asn1.esf;

import java.util.Enumeration;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.ocsp.BasicOCSPResponse;
import org.bouncycastle.asn1.x509.CertificateList;

public class RevocationValues extends ASN1Object {
    private ASN1Sequence crlVals;
    private ASN1Sequence ocspVals;
    private OtherRevVals otherRevVals;

    private RevocationValues(ASN1Sequence aSN1Sequence) {
        if (aSN1Sequence.size() <= 3) {
            Enumeration objects = aSN1Sequence.getObjects();
            while (objects.hasMoreElements()) {
                ASN1TaggedObject aSN1TaggedObject = (ASN1TaggedObject) objects.nextElement();
                int tagNo = aSN1TaggedObject.getTagNo();
                if (tagNo == 0) {
                    ASN1Sequence aSN1Sequence2 = (ASN1Sequence) aSN1TaggedObject.getObject();
                    Enumeration objects2 = aSN1Sequence2.getObjects();
                    while (objects2.hasMoreElements()) {
                        CertificateList.getInstance(objects2.nextElement());
                    }
                    this.crlVals = aSN1Sequence2;
                } else if (tagNo == 1) {
                    ASN1Sequence aSN1Sequence3 = (ASN1Sequence) aSN1TaggedObject.getObject();
                    Enumeration objects3 = aSN1Sequence3.getObjects();
                    while (objects3.hasMoreElements()) {
                        BasicOCSPResponse.getInstance(objects3.nextElement());
                    }
                    this.ocspVals = aSN1Sequence3;
                } else if (tagNo == 2) {
                    this.otherRevVals = OtherRevVals.getInstance(aSN1TaggedObject.getObject());
                } else {
                    throw new IllegalArgumentException("invalid tag: " + aSN1TaggedObject.getTagNo());
                }
            }
            return;
        }
        throw new IllegalArgumentException("Bad sequence size: " + aSN1Sequence.size());
    }

    public RevocationValues(CertificateList[] certificateListArr, BasicOCSPResponse[] basicOCSPResponseArr, OtherRevVals otherRevVals2) {
        if (certificateListArr != null) {
            this.crlVals = new DERSequence(certificateListArr);
        }
        if (basicOCSPResponseArr != null) {
            this.ocspVals = new DERSequence(basicOCSPResponseArr);
        }
        this.otherRevVals = otherRevVals2;
    }

    public static RevocationValues getInstance(Object obj) {
        if (obj instanceof RevocationValues) {
            return (RevocationValues) obj;
        }
        if (obj != null) {
            return new RevocationValues(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public CertificateList[] getCrlVals() {
        ASN1Sequence aSN1Sequence = this.crlVals;
        if (aSN1Sequence == null) {
            return new CertificateList[0];
        }
        CertificateList[] certificateListArr = new CertificateList[aSN1Sequence.size()];
        for (int i = 0; i < certificateListArr.length; i++) {
            certificateListArr[i] = CertificateList.getInstance(this.crlVals.getObjectAt(i));
        }
        return certificateListArr;
    }

    public BasicOCSPResponse[] getOcspVals() {
        ASN1Sequence aSN1Sequence = this.ocspVals;
        if (aSN1Sequence == null) {
            return new BasicOCSPResponse[0];
        }
        BasicOCSPResponse[] basicOCSPResponseArr = new BasicOCSPResponse[aSN1Sequence.size()];
        for (int i = 0; i < basicOCSPResponseArr.length; i++) {
            basicOCSPResponseArr[i] = BasicOCSPResponse.getInstance(this.ocspVals.getObjectAt(i));
        }
        return basicOCSPResponseArr;
    }

    public OtherRevVals getOtherRevVals() {
        return this.otherRevVals;
    }

    @Override // org.bouncycastle.asn1.ASN1Object, org.bouncycastle.asn1.ASN1Encodable
    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector(3);
        ASN1Sequence aSN1Sequence = this.crlVals;
        if (aSN1Sequence != null) {
            aSN1EncodableVector.add(new DERTaggedObject(true, 0, aSN1Sequence));
        }
        ASN1Sequence aSN1Sequence2 = this.ocspVals;
        if (aSN1Sequence2 != null) {
            aSN1EncodableVector.add(new DERTaggedObject(true, 1, aSN1Sequence2));
        }
        OtherRevVals otherRevVals2 = this.otherRevVals;
        if (otherRevVals2 != null) {
            aSN1EncodableVector.add(new DERTaggedObject(true, 2, otherRevVals2.toASN1Primitive()));
        }
        return new DERSequence(aSN1EncodableVector);
    }
}

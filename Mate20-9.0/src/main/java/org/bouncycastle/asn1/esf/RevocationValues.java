package org.bouncycastle.asn1.esf;

import java.util.Enumeration;
import org.bouncycastle.asn1.ASN1Encodable;
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
                switch (aSN1TaggedObject.getTagNo()) {
                    case 0:
                        ASN1Sequence aSN1Sequence2 = (ASN1Sequence) aSN1TaggedObject.getObject();
                        Enumeration objects2 = aSN1Sequence2.getObjects();
                        while (objects2.hasMoreElements()) {
                            CertificateList.getInstance(objects2.nextElement());
                        }
                        this.crlVals = aSN1Sequence2;
                        break;
                    case 1:
                        ASN1Sequence aSN1Sequence3 = (ASN1Sequence) aSN1TaggedObject.getObject();
                        Enumeration objects3 = aSN1Sequence3.getObjects();
                        while (objects3.hasMoreElements()) {
                            BasicOCSPResponse.getInstance(objects3.nextElement());
                        }
                        this.ocspVals = aSN1Sequence3;
                        break;
                    case 2:
                        this.otherRevVals = OtherRevVals.getInstance(aSN1TaggedObject.getObject());
                        break;
                    default:
                        throw new IllegalArgumentException("invalid tag: " + aSN1TaggedObject.getTagNo());
                }
            }
            return;
        }
        throw new IllegalArgumentException("Bad sequence size: " + aSN1Sequence.size());
    }

    public RevocationValues(CertificateList[] certificateListArr, BasicOCSPResponse[] basicOCSPResponseArr, OtherRevVals otherRevVals2) {
        if (certificateListArr != null) {
            this.crlVals = new DERSequence((ASN1Encodable[]) certificateListArr);
        }
        if (basicOCSPResponseArr != null) {
            this.ocspVals = new DERSequence((ASN1Encodable[]) basicOCSPResponseArr);
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
        if (this.crlVals == null) {
            return new CertificateList[0];
        }
        CertificateList[] certificateListArr = new CertificateList[this.crlVals.size()];
        for (int i = 0; i < certificateListArr.length; i++) {
            certificateListArr[i] = CertificateList.getInstance(this.crlVals.getObjectAt(i));
        }
        return certificateListArr;
    }

    public BasicOCSPResponse[] getOcspVals() {
        if (this.ocspVals == null) {
            return new BasicOCSPResponse[0];
        }
        BasicOCSPResponse[] basicOCSPResponseArr = new BasicOCSPResponse[this.ocspVals.size()];
        for (int i = 0; i < basicOCSPResponseArr.length; i++) {
            basicOCSPResponseArr[i] = BasicOCSPResponse.getInstance(this.ocspVals.getObjectAt(i));
        }
        return basicOCSPResponseArr;
    }

    public OtherRevVals getOtherRevVals() {
        return this.otherRevVals;
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        if (this.crlVals != null) {
            aSN1EncodableVector.add(new DERTaggedObject(true, 0, this.crlVals));
        }
        if (this.ocspVals != null) {
            aSN1EncodableVector.add(new DERTaggedObject(true, 1, this.ocspVals));
        }
        if (this.otherRevVals != null) {
            aSN1EncodableVector.add(new DERTaggedObject(true, 2, this.otherRevVals.toASN1Primitive()));
        }
        return new DERSequence(aSN1EncodableVector);
    }
}

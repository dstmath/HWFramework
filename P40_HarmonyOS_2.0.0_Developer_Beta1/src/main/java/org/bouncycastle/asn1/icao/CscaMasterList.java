package org.bouncycastle.asn1.icao;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.x509.Certificate;

public class CscaMasterList extends ASN1Object {
    private Certificate[] certList;
    private ASN1Integer version = new ASN1Integer(0);

    private CscaMasterList(ASN1Sequence aSN1Sequence) {
        if (aSN1Sequence == null || aSN1Sequence.size() == 0) {
            throw new IllegalArgumentException("null or empty sequence passed.");
        } else if (aSN1Sequence.size() == 2) {
            int i = 0;
            this.version = ASN1Integer.getInstance(aSN1Sequence.getObjectAt(0));
            ASN1Set instance = ASN1Set.getInstance(aSN1Sequence.getObjectAt(1));
            this.certList = new Certificate[instance.size()];
            while (true) {
                Certificate[] certificateArr = this.certList;
                if (i < certificateArr.length) {
                    certificateArr[i] = Certificate.getInstance(instance.getObjectAt(i));
                    i++;
                } else {
                    return;
                }
            }
        } else {
            throw new IllegalArgumentException("Incorrect sequence size: " + aSN1Sequence.size());
        }
    }

    public CscaMasterList(Certificate[] certificateArr) {
        this.certList = copyCertList(certificateArr);
    }

    private Certificate[] copyCertList(Certificate[] certificateArr) {
        Certificate[] certificateArr2 = new Certificate[certificateArr.length];
        for (int i = 0; i != certificateArr2.length; i++) {
            certificateArr2[i] = certificateArr[i];
        }
        return certificateArr2;
    }

    public static CscaMasterList getInstance(Object obj) {
        if (obj instanceof CscaMasterList) {
            return (CscaMasterList) obj;
        }
        if (obj != null) {
            return new CscaMasterList(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public Certificate[] getCertStructs() {
        return copyCertList(this.certList);
    }

    public int getVersion() {
        return this.version.intValueExact();
    }

    @Override // org.bouncycastle.asn1.ASN1Object, org.bouncycastle.asn1.ASN1Encodable
    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector(2);
        aSN1EncodableVector.add(this.version);
        aSN1EncodableVector.add(new DERSet(this.certList));
        return new DERSequence(aSN1EncodableVector);
    }
}

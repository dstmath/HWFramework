package org.bouncycastle.asn1.x509;

import java.math.BigInteger;
import java.util.Enumeration;
import java.util.Vector;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;

public class NoticeReference extends ASN1Object {
    private ASN1Sequence noticeNumbers;
    private DisplayText organization;

    public NoticeReference(String str, Vector vector) {
        this(str, convertVector(vector));
    }

    public NoticeReference(String str, ASN1EncodableVector aSN1EncodableVector) {
        this(new DisplayText(str), aSN1EncodableVector);
    }

    private NoticeReference(ASN1Sequence aSN1Sequence) {
        if (aSN1Sequence.size() == 2) {
            this.organization = DisplayText.getInstance(aSN1Sequence.getObjectAt(0));
            this.noticeNumbers = ASN1Sequence.getInstance(aSN1Sequence.getObjectAt(1));
            return;
        }
        throw new IllegalArgumentException("Bad sequence size: " + aSN1Sequence.size());
    }

    public NoticeReference(DisplayText displayText, ASN1EncodableVector aSN1EncodableVector) {
        this.organization = displayText;
        this.noticeNumbers = new DERSequence(aSN1EncodableVector);
    }

    private static ASN1EncodableVector convertVector(Vector vector) {
        ASN1Integer aSN1Integer;
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        Enumeration elements = vector.elements();
        while (elements.hasMoreElements()) {
            Object nextElement = elements.nextElement();
            if (nextElement instanceof BigInteger) {
                aSN1Integer = new ASN1Integer((BigInteger) nextElement);
            } else if (nextElement instanceof Integer) {
                aSN1Integer = new ASN1Integer((long) ((Integer) nextElement).intValue());
            } else {
                throw new IllegalArgumentException();
            }
            aSN1EncodableVector.add(aSN1Integer);
        }
        return aSN1EncodableVector;
    }

    public static NoticeReference getInstance(Object obj) {
        if (obj instanceof NoticeReference) {
            return (NoticeReference) obj;
        }
        if (obj != null) {
            return new NoticeReference(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public ASN1Integer[] getNoticeNumbers() {
        ASN1Integer[] aSN1IntegerArr = new ASN1Integer[this.noticeNumbers.size()];
        for (int i = 0; i != this.noticeNumbers.size(); i++) {
            aSN1IntegerArr[i] = ASN1Integer.getInstance(this.noticeNumbers.getObjectAt(i));
        }
        return aSN1IntegerArr;
    }

    public DisplayText getOrganization() {
        return this.organization;
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        aSN1EncodableVector.add(this.organization);
        aSN1EncodableVector.add(this.noticeNumbers);
        return new DERSequence(aSN1EncodableVector);
    }
}

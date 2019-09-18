package org.bouncycastle.asn1.cms;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1GeneralizedTime;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;

public class RecipientKeyIdentifier extends ASN1Object {
    private ASN1GeneralizedTime date;
    private OtherKeyAttribute other;
    private ASN1OctetString subjectKeyIdentifier;

    public RecipientKeyIdentifier(ASN1OctetString aSN1OctetString, ASN1GeneralizedTime aSN1GeneralizedTime, OtherKeyAttribute otherKeyAttribute) {
        this.subjectKeyIdentifier = aSN1OctetString;
        this.date = aSN1GeneralizedTime;
        this.other = otherKeyAttribute;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0046, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:5:0x0029, code lost:
        r3.other = org.bouncycastle.asn1.cms.OtherKeyAttribute.getInstance(r4.getObjectAt(2));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:6:0x0033, code lost:
        return;
     */
    public RecipientKeyIdentifier(ASN1Sequence aSN1Sequence) {
        this.subjectKeyIdentifier = ASN1OctetString.getInstance(aSN1Sequence.getObjectAt(0));
        switch (aSN1Sequence.size()) {
            case 1:
                break;
            case 2:
                if (aSN1Sequence.getObjectAt(1) instanceof ASN1GeneralizedTime) {
                    this.date = ASN1GeneralizedTime.getInstance(aSN1Sequence.getObjectAt(1));
                    break;
                }
                break;
            case 3:
                this.date = ASN1GeneralizedTime.getInstance(aSN1Sequence.getObjectAt(1));
                break;
            default:
                throw new IllegalArgumentException("Invalid RecipientKeyIdentifier");
        }
    }

    public RecipientKeyIdentifier(byte[] bArr) {
        this(bArr, (ASN1GeneralizedTime) null, (OtherKeyAttribute) null);
    }

    public RecipientKeyIdentifier(byte[] bArr, ASN1GeneralizedTime aSN1GeneralizedTime, OtherKeyAttribute otherKeyAttribute) {
        this.subjectKeyIdentifier = new DEROctetString(bArr);
        this.date = aSN1GeneralizedTime;
        this.other = otherKeyAttribute;
    }

    public static RecipientKeyIdentifier getInstance(Object obj) {
        if (obj instanceof RecipientKeyIdentifier) {
            return (RecipientKeyIdentifier) obj;
        }
        if (obj != null) {
            return new RecipientKeyIdentifier(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public static RecipientKeyIdentifier getInstance(ASN1TaggedObject aSN1TaggedObject, boolean z) {
        return getInstance(ASN1Sequence.getInstance(aSN1TaggedObject, z));
    }

    public ASN1GeneralizedTime getDate() {
        return this.date;
    }

    public OtherKeyAttribute getOtherKeyAttribute() {
        return this.other;
    }

    public ASN1OctetString getSubjectKeyIdentifier() {
        return this.subjectKeyIdentifier;
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        aSN1EncodableVector.add(this.subjectKeyIdentifier);
        if (this.date != null) {
            aSN1EncodableVector.add(this.date);
        }
        if (this.other != null) {
            aSN1EncodableVector.add(this.other);
        }
        return new DERSequence(aSN1EncodableVector);
    }
}

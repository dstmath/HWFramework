package org.bouncycastle.asn1.cmc;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;

public class PKIData extends ASN1Object {
    private final TaggedContentInfo[] cmsSequence;
    private final TaggedAttribute[] controlSequence;
    private final OtherMsg[] otherMsgSequence;
    private final TaggedRequest[] reqSequence;

    private PKIData(ASN1Sequence aSN1Sequence) {
        if (aSN1Sequence.size() == 4) {
            ASN1Sequence aSN1Sequence2 = (ASN1Sequence) aSN1Sequence.getObjectAt(0);
            this.controlSequence = new TaggedAttribute[aSN1Sequence2.size()];
            for (int i = 0; i < this.controlSequence.length; i++) {
                this.controlSequence[i] = TaggedAttribute.getInstance(aSN1Sequence2.getObjectAt(i));
            }
            ASN1Sequence aSN1Sequence3 = (ASN1Sequence) aSN1Sequence.getObjectAt(1);
            this.reqSequence = new TaggedRequest[aSN1Sequence3.size()];
            for (int i2 = 0; i2 < this.reqSequence.length; i2++) {
                this.reqSequence[i2] = TaggedRequest.getInstance(aSN1Sequence3.getObjectAt(i2));
            }
            ASN1Sequence aSN1Sequence4 = (ASN1Sequence) aSN1Sequence.getObjectAt(2);
            this.cmsSequence = new TaggedContentInfo[aSN1Sequence4.size()];
            for (int i3 = 0; i3 < this.cmsSequence.length; i3++) {
                this.cmsSequence[i3] = TaggedContentInfo.getInstance(aSN1Sequence4.getObjectAt(i3));
            }
            ASN1Sequence aSN1Sequence5 = (ASN1Sequence) aSN1Sequence.getObjectAt(3);
            this.otherMsgSequence = new OtherMsg[aSN1Sequence5.size()];
            for (int i4 = 0; i4 < this.otherMsgSequence.length; i4++) {
                this.otherMsgSequence[i4] = OtherMsg.getInstance(aSN1Sequence5.getObjectAt(i4));
            }
            return;
        }
        throw new IllegalArgumentException("Sequence not 4 elements.");
    }

    public PKIData(TaggedAttribute[] taggedAttributeArr, TaggedRequest[] taggedRequestArr, TaggedContentInfo[] taggedContentInfoArr, OtherMsg[] otherMsgArr) {
        this.controlSequence = taggedAttributeArr;
        this.reqSequence = taggedRequestArr;
        this.cmsSequence = taggedContentInfoArr;
        this.otherMsgSequence = otherMsgArr;
    }

    public static PKIData getInstance(Object obj) {
        if (obj instanceof PKIData) {
            return (PKIData) obj;
        }
        if (obj != null) {
            return new PKIData(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public TaggedContentInfo[] getCmsSequence() {
        return this.cmsSequence;
    }

    public TaggedAttribute[] getControlSequence() {
        return this.controlSequence;
    }

    public OtherMsg[] getOtherMsgSequence() {
        return this.otherMsgSequence;
    }

    public TaggedRequest[] getReqSequence() {
        return this.reqSequence;
    }

    public ASN1Primitive toASN1Primitive() {
        return new DERSequence(new ASN1Encodable[]{new DERSequence((ASN1Encodable[]) this.controlSequence), new DERSequence((ASN1Encodable[]) this.reqSequence), new DERSequence((ASN1Encodable[]) this.cmsSequence), new DERSequence((ASN1Encodable[]) this.otherMsgSequence)});
    }
}

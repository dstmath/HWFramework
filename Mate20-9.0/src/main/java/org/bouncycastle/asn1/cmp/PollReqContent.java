package org.bouncycastle.asn1.cmp;

import java.math.BigInteger;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;

public class PollReqContent extends ASN1Object {
    private ASN1Sequence content;

    public PollReqContent(BigInteger bigInteger) {
        this(new ASN1Integer(bigInteger));
    }

    public PollReqContent(ASN1Integer aSN1Integer) {
        this((ASN1Sequence) new DERSequence((ASN1Encodable) new DERSequence((ASN1Encodable) aSN1Integer)));
    }

    private PollReqContent(ASN1Sequence aSN1Sequence) {
        this.content = aSN1Sequence;
    }

    public PollReqContent(BigInteger[] bigIntegerArr) {
        this(intsToASN1(bigIntegerArr));
    }

    public PollReqContent(ASN1Integer[] aSN1IntegerArr) {
        this((ASN1Sequence) new DERSequence((ASN1Encodable[]) intsToSequence(aSN1IntegerArr)));
    }

    public static PollReqContent getInstance(Object obj) {
        if (obj instanceof PollReqContent) {
            return (PollReqContent) obj;
        }
        if (obj != null) {
            return new PollReqContent(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    private static ASN1Integer[] intsToASN1(BigInteger[] bigIntegerArr) {
        ASN1Integer[] aSN1IntegerArr = new ASN1Integer[bigIntegerArr.length];
        for (int i = 0; i != aSN1IntegerArr.length; i++) {
            aSN1IntegerArr[i] = new ASN1Integer(bigIntegerArr[i]);
        }
        return aSN1IntegerArr;
    }

    private static DERSequence[] intsToSequence(ASN1Integer[] aSN1IntegerArr) {
        DERSequence[] dERSequenceArr = new DERSequence[aSN1IntegerArr.length];
        for (int i = 0; i != dERSequenceArr.length; i++) {
            dERSequenceArr[i] = new DERSequence((ASN1Encodable) aSN1IntegerArr[i]);
        }
        return dERSequenceArr;
    }

    private static ASN1Integer[] sequenceToASN1IntegerArray(ASN1Sequence aSN1Sequence) {
        ASN1Integer[] aSN1IntegerArr = new ASN1Integer[aSN1Sequence.size()];
        for (int i = 0; i != aSN1IntegerArr.length; i++) {
            aSN1IntegerArr[i] = ASN1Integer.getInstance(aSN1Sequence.getObjectAt(i));
        }
        return aSN1IntegerArr;
    }

    public BigInteger[] getCertReqIdValues() {
        BigInteger[] bigIntegerArr = new BigInteger[this.content.size()];
        for (int i = 0; i != bigIntegerArr.length; i++) {
            bigIntegerArr[i] = ASN1Integer.getInstance(ASN1Sequence.getInstance(this.content.getObjectAt(i)).getObjectAt(0)).getValue();
        }
        return bigIntegerArr;
    }

    public ASN1Integer[][] getCertReqIds() {
        ASN1Integer[][] aSN1IntegerArr = new ASN1Integer[this.content.size()][];
        for (int i = 0; i != aSN1IntegerArr.length; i++) {
            aSN1IntegerArr[i] = sequenceToASN1IntegerArray((ASN1Sequence) this.content.getObjectAt(i));
        }
        return aSN1IntegerArr;
    }

    public ASN1Primitive toASN1Primitive() {
        return this.content;
    }
}

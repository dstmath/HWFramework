package org.bouncycastle.pqc.asn1;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;

public class XMSSKeyParams extends ASN1Object {
    private final int height;
    private final AlgorithmIdentifier treeDigest;
    private final ASN1Integer version;

    public XMSSKeyParams(int i, AlgorithmIdentifier algorithmIdentifier) {
        this.version = new ASN1Integer(0);
        this.height = i;
        this.treeDigest = algorithmIdentifier;
    }

    private XMSSKeyParams(ASN1Sequence aSN1Sequence) {
        this.version = ASN1Integer.getInstance(aSN1Sequence.getObjectAt(0));
        this.height = ASN1Integer.getInstance(aSN1Sequence.getObjectAt(1)).getValue().intValue();
        this.treeDigest = AlgorithmIdentifier.getInstance(aSN1Sequence.getObjectAt(2));
    }

    public static XMSSKeyParams getInstance(Object obj) {
        if (obj instanceof XMSSKeyParams) {
            return (XMSSKeyParams) obj;
        }
        if (obj != null) {
            return new XMSSKeyParams(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public int getHeight() {
        return this.height;
    }

    public AlgorithmIdentifier getTreeDigest() {
        return this.treeDigest;
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        aSN1EncodableVector.add(this.version);
        aSN1EncodableVector.add(new ASN1Integer((long) this.height));
        aSN1EncodableVector.add(this.treeDigest);
        return new DERSequence(aSN1EncodableVector);
    }
}

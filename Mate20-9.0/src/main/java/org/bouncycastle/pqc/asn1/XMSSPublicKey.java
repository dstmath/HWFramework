package org.bouncycastle.pqc.asn1;

import java.math.BigInteger;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.util.Arrays;

public class XMSSPublicKey extends ASN1Object {
    private final byte[] publicSeed;
    private final byte[] root;

    private XMSSPublicKey(ASN1Sequence aSN1Sequence) {
        if (ASN1Integer.getInstance(aSN1Sequence.getObjectAt(0)).getValue().equals(BigInteger.valueOf(0))) {
            this.publicSeed = Arrays.clone(DEROctetString.getInstance(aSN1Sequence.getObjectAt(1)).getOctets());
            this.root = Arrays.clone(DEROctetString.getInstance(aSN1Sequence.getObjectAt(2)).getOctets());
            return;
        }
        throw new IllegalArgumentException("unknown version of sequence");
    }

    public XMSSPublicKey(byte[] bArr, byte[] bArr2) {
        this.publicSeed = Arrays.clone(bArr);
        this.root = Arrays.clone(bArr2);
    }

    public static XMSSPublicKey getInstance(Object obj) {
        if (obj instanceof XMSSPublicKey) {
            return (XMSSPublicKey) obj;
        }
        if (obj != null) {
            return new XMSSPublicKey(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public byte[] getPublicSeed() {
        return Arrays.clone(this.publicSeed);
    }

    public byte[] getRoot() {
        return Arrays.clone(this.root);
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        aSN1EncodableVector.add(new ASN1Integer(0));
        aSN1EncodableVector.add(new DEROctetString(this.publicSeed));
        aSN1EncodableVector.add(new DEROctetString(this.root));
        return new DERSequence(aSN1EncodableVector);
    }
}

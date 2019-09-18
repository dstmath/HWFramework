package org.bouncycastle.asn1.misc;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.util.Arrays;

public class IDEACBCPar extends ASN1Object {
    ASN1OctetString iv;

    public IDEACBCPar(ASN1Sequence aSN1Sequence) {
        this.iv = aSN1Sequence.size() == 1 ? (ASN1OctetString) aSN1Sequence.getObjectAt(0) : null;
    }

    public IDEACBCPar(byte[] bArr) {
        this.iv = new DEROctetString(bArr);
    }

    public static IDEACBCPar getInstance(Object obj) {
        if (obj instanceof IDEACBCPar) {
            return (IDEACBCPar) obj;
        }
        if (obj != null) {
            return new IDEACBCPar(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public byte[] getIV() {
        if (this.iv != null) {
            return Arrays.clone(this.iv.getOctets());
        }
        return null;
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        if (this.iv != null) {
            aSN1EncodableVector.add(this.iv);
        }
        return new DERSequence(aSN1EncodableVector);
    }
}

package org.bouncycastle.asn1;

import java.io.IOException;

public class DERSet extends ASN1Set {
    private int bodyLength = -1;

    public DERSet() {
    }

    public DERSet(ASN1Encodable aSN1Encodable) {
        super(aSN1Encodable);
    }

    public DERSet(ASN1EncodableVector aSN1EncodableVector) {
        super(aSN1EncodableVector, true);
    }

    DERSet(boolean z, ASN1Encodable[] aSN1EncodableArr) {
        super(checkSorted(z), aSN1EncodableArr);
    }

    public DERSet(ASN1Encodable[] aSN1EncodableArr) {
        super(aSN1EncodableArr, true);
    }

    private static boolean checkSorted(boolean z) {
        if (z) {
            return z;
        }
        throw new IllegalStateException("DERSet elements should always be in sorted order");
    }

    public static DERSet convert(ASN1Set aSN1Set) {
        return (DERSet) aSN1Set.toDERObject();
    }

    private int getBodyLength() throws IOException {
        if (this.bodyLength < 0) {
            int length = this.elements.length;
            int i = 0;
            for (int i2 = 0; i2 < length; i2++) {
                i += this.elements[i2].toASN1Primitive().toDERObject().encodedLength();
            }
            this.bodyLength = i;
        }
        return this.bodyLength;
    }

    /* access modifiers changed from: package-private */
    @Override // org.bouncycastle.asn1.ASN1Set, org.bouncycastle.asn1.ASN1Primitive
    public void encode(ASN1OutputStream aSN1OutputStream, boolean z) throws IOException {
        if (z) {
            aSN1OutputStream.write(49);
        }
        DEROutputStream dERSubStream = aSN1OutputStream.getDERSubStream();
        int length = this.elements.length;
        int i = 0;
        if (this.bodyLength >= 0 || length > 16) {
            aSN1OutputStream.writeLength(getBodyLength());
            while (i < length) {
                this.elements[i].toASN1Primitive().toDERObject().encode(dERSubStream, true);
                i++;
            }
            return;
        }
        ASN1Primitive[] aSN1PrimitiveArr = new ASN1Primitive[length];
        int i2 = 0;
        for (int i3 = 0; i3 < length; i3++) {
            ASN1Primitive dERObject = this.elements[i3].toASN1Primitive().toDERObject();
            aSN1PrimitiveArr[i3] = dERObject;
            i2 += dERObject.encodedLength();
        }
        this.bodyLength = i2;
        aSN1OutputStream.writeLength(i2);
        while (i < length) {
            aSN1PrimitiveArr[i].encode(dERSubStream, true);
            i++;
        }
    }

    /* access modifiers changed from: package-private */
    @Override // org.bouncycastle.asn1.ASN1Primitive
    public int encodedLength() throws IOException {
        int bodyLength2 = getBodyLength();
        return StreamUtil.calculateBodyLength(bodyLength2) + 1 + bodyLength2;
    }

    /* access modifiers changed from: package-private */
    @Override // org.bouncycastle.asn1.ASN1Set, org.bouncycastle.asn1.ASN1Primitive
    public ASN1Primitive toDERObject() {
        return this.isSorted ? this : super.toDERObject();
    }

    /* access modifiers changed from: package-private */
    @Override // org.bouncycastle.asn1.ASN1Set, org.bouncycastle.asn1.ASN1Primitive
    public ASN1Primitive toDLObject() {
        return this;
    }
}

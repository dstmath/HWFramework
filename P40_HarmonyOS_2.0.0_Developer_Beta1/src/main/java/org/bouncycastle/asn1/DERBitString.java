package org.bouncycastle.asn1;

import java.io.IOException;
import org.bouncycastle.pqc.crypto.rainbow.util.GF2Field;

public class DERBitString extends ASN1BitString {
    protected DERBitString(byte b, int i) {
        super(b, i);
    }

    public DERBitString(int i) {
        super(getBytes(i), getPadBits(i));
    }

    public DERBitString(ASN1Encodable aSN1Encodable) throws IOException {
        super(aSN1Encodable.toASN1Primitive().getEncoded(ASN1Encoding.DER), 0);
    }

    public DERBitString(byte[] bArr) {
        this(bArr, 0);
    }

    public DERBitString(byte[] bArr, int i) {
        super(bArr, i);
    }

    static DERBitString fromOctetString(byte[] bArr) {
        if (bArr.length >= 1) {
            byte b = bArr[0];
            byte[] bArr2 = new byte[(bArr.length - 1)];
            if (bArr2.length != 0) {
                System.arraycopy(bArr, 1, bArr2, 0, bArr.length - 1);
            }
            return new DERBitString(bArr2, b);
        }
        throw new IllegalArgumentException("truncated BIT STRING detected");
    }

    public static DERBitString getInstance(Object obj) {
        if (obj == null || (obj instanceof DERBitString)) {
            return (DERBitString) obj;
        }
        if (obj instanceof DLBitString) {
            DLBitString dLBitString = (DLBitString) obj;
            return new DERBitString(dLBitString.data, dLBitString.padBits);
        } else if (obj instanceof byte[]) {
            try {
                return (DERBitString) fromByteArray((byte[]) obj);
            } catch (Exception e) {
                throw new IllegalArgumentException("encoding error in getInstance: " + e.toString());
            }
        } else {
            throw new IllegalArgumentException("illegal object in getInstance: " + obj.getClass().getName());
        }
    }

    public static DERBitString getInstance(ASN1TaggedObject aSN1TaggedObject, boolean z) {
        ASN1Primitive object = aSN1TaggedObject.getObject();
        return (z || (object instanceof DERBitString)) ? getInstance(object) : fromOctetString(ASN1OctetString.getInstance(object).getOctets());
    }

    /* access modifiers changed from: package-private */
    @Override // org.bouncycastle.asn1.ASN1BitString, org.bouncycastle.asn1.ASN1Primitive
    public void encode(ASN1OutputStream aSN1OutputStream, boolean z) throws IOException {
        int length = this.data.length;
        if (!(length == 0 || this.padBits == 0)) {
            int i = length - 1;
            if (this.data[i] != ((byte) (this.data[i] & (GF2Field.MASK << this.padBits)))) {
                aSN1OutputStream.writeEncoded(z, 3, (byte) this.padBits, this.data, 0, i, (byte) (this.data[i] & (GF2Field.MASK << this.padBits)));
                return;
            }
        }
        aSN1OutputStream.writeEncoded(z, 3, (byte) this.padBits, this.data);
    }

    /* access modifiers changed from: package-private */
    @Override // org.bouncycastle.asn1.ASN1Primitive
    public int encodedLength() {
        return StreamUtil.calculateBodyLength(this.data.length + 1) + 1 + this.data.length + 1;
    }

    /* access modifiers changed from: package-private */
    @Override // org.bouncycastle.asn1.ASN1Primitive
    public boolean isConstructed() {
        return false;
    }

    /* access modifiers changed from: package-private */
    @Override // org.bouncycastle.asn1.ASN1BitString, org.bouncycastle.asn1.ASN1Primitive
    public ASN1Primitive toDERObject() {
        return this;
    }

    /* access modifiers changed from: package-private */
    @Override // org.bouncycastle.asn1.ASN1BitString, org.bouncycastle.asn1.ASN1Primitive
    public ASN1Primitive toDLObject() {
        return this;
    }
}

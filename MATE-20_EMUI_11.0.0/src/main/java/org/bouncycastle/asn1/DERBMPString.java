package org.bouncycastle.asn1;

import java.io.IOException;
import org.bouncycastle.util.Arrays;

public class DERBMPString extends ASN1Primitive implements ASN1String {
    private final char[] string;

    public DERBMPString(String str) {
        if (str != null) {
            this.string = str.toCharArray();
            return;
        }
        throw new NullPointerException("'string' cannot be null");
    }

    DERBMPString(byte[] bArr) {
        if (bArr != null) {
            int length = bArr.length;
            if ((length & 1) == 0) {
                int i = length / 2;
                char[] cArr = new char[i];
                for (int i2 = 0; i2 != i; i2++) {
                    int i3 = i2 * 2;
                    cArr[i2] = (char) ((bArr[i3 + 1] & 255) | (bArr[i3] << 8));
                }
                this.string = cArr;
                return;
            }
            throw new IllegalArgumentException("malformed BMPString encoding encountered");
        }
        throw new NullPointerException("'string' cannot be null");
    }

    DERBMPString(char[] cArr) {
        if (cArr != null) {
            this.string = cArr;
            return;
        }
        throw new NullPointerException("'string' cannot be null");
    }

    public static DERBMPString getInstance(Object obj) {
        if (obj == null || (obj instanceof DERBMPString)) {
            return (DERBMPString) obj;
        }
        if (obj instanceof byte[]) {
            try {
                return (DERBMPString) fromByteArray((byte[]) obj);
            } catch (Exception e) {
                throw new IllegalArgumentException("encoding error in getInstance: " + e.toString());
            }
        } else {
            throw new IllegalArgumentException("illegal object in getInstance: " + obj.getClass().getName());
        }
    }

    public static DERBMPString getInstance(ASN1TaggedObject aSN1TaggedObject, boolean z) {
        ASN1Primitive object = aSN1TaggedObject.getObject();
        return (z || (object instanceof DERBMPString)) ? getInstance(object) : new DERBMPString(ASN1OctetString.getInstance(object).getOctets());
    }

    /* access modifiers changed from: protected */
    @Override // org.bouncycastle.asn1.ASN1Primitive
    public boolean asn1Equals(ASN1Primitive aSN1Primitive) {
        if (!(aSN1Primitive instanceof DERBMPString)) {
            return false;
        }
        return Arrays.areEqual(this.string, ((DERBMPString) aSN1Primitive).string);
    }

    /* access modifiers changed from: package-private */
    @Override // org.bouncycastle.asn1.ASN1Primitive
    public void encode(ASN1OutputStream aSN1OutputStream, boolean z) throws IOException {
        int length = this.string.length;
        if (z) {
            aSN1OutputStream.write(30);
        }
        aSN1OutputStream.writeLength(length * 2);
        byte[] bArr = new byte[8];
        int i = length & -4;
        int i2 = 0;
        while (i2 < i) {
            char[] cArr = this.string;
            char c = cArr[i2];
            char c2 = cArr[i2 + 1];
            char c3 = cArr[i2 + 2];
            char c4 = cArr[i2 + 3];
            i2 += 4;
            bArr[0] = (byte) (c >> '\b');
            bArr[1] = (byte) c;
            bArr[2] = (byte) (c2 >> '\b');
            bArr[3] = (byte) c2;
            bArr[4] = (byte) (c3 >> '\b');
            bArr[5] = (byte) c3;
            bArr[6] = (byte) (c4 >> '\b');
            bArr[7] = (byte) c4;
            aSN1OutputStream.write(bArr, 0, 8);
        }
        if (i2 < length) {
            int i3 = 0;
            do {
                char c5 = this.string[i2];
                i2++;
                int i4 = i3 + 1;
                bArr[i3] = (byte) (c5 >> '\b');
                i3 = i4 + 1;
                bArr[i4] = (byte) c5;
            } while (i2 < length);
            aSN1OutputStream.write(bArr, 0, i3);
        }
    }

    /* access modifiers changed from: package-private */
    @Override // org.bouncycastle.asn1.ASN1Primitive
    public int encodedLength() {
        return StreamUtil.calculateBodyLength(this.string.length * 2) + 1 + (this.string.length * 2);
    }

    @Override // org.bouncycastle.asn1.ASN1String
    public String getString() {
        return new String(this.string);
    }

    @Override // org.bouncycastle.asn1.ASN1Primitive, org.bouncycastle.asn1.ASN1Object
    public int hashCode() {
        return Arrays.hashCode(this.string);
    }

    /* access modifiers changed from: package-private */
    @Override // org.bouncycastle.asn1.ASN1Primitive
    public boolean isConstructed() {
        return false;
    }

    public String toString() {
        return getString();
    }
}

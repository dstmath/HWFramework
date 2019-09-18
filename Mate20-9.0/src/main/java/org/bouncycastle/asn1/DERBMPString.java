package org.bouncycastle.asn1;

import java.io.IOException;
import org.bouncycastle.util.Arrays;

public class DERBMPString extends ASN1Primitive implements ASN1String {
    private final char[] string;

    public DERBMPString(String str) {
        this.string = str.toCharArray();
    }

    DERBMPString(byte[] bArr) {
        char[] cArr = new char[(bArr.length / 2)];
        for (int i = 0; i != cArr.length; i++) {
            int i2 = 2 * i;
            cArr[i] = (char) ((bArr[i2 + 1] & 255) | (bArr[i2] << 8));
        }
        this.string = cArr;
    }

    DERBMPString(char[] cArr) {
        this.string = cArr;
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
    public boolean asn1Equals(ASN1Primitive aSN1Primitive) {
        if (!(aSN1Primitive instanceof DERBMPString)) {
            return false;
        }
        return Arrays.areEqual(this.string, ((DERBMPString) aSN1Primitive).string);
    }

    /* access modifiers changed from: package-private */
    public void encode(ASN1OutputStream aSN1OutputStream) throws IOException {
        aSN1OutputStream.write(30);
        aSN1OutputStream.writeLength(this.string.length * 2);
        for (int i = 0; i != this.string.length; i++) {
            char c = this.string[i];
            aSN1OutputStream.write((int) (byte) (c >> 8));
            aSN1OutputStream.write((int) (byte) c);
        }
    }

    /* access modifiers changed from: package-private */
    public int encodedLength() {
        return 1 + StreamUtil.calculateBodyLength(this.string.length * 2) + (this.string.length * 2);
    }

    public String getString() {
        return new String(this.string);
    }

    public int hashCode() {
        return Arrays.hashCode(this.string);
    }

    /* access modifiers changed from: package-private */
    public boolean isConstructed() {
        return false;
    }

    public String toString() {
        return getString();
    }
}

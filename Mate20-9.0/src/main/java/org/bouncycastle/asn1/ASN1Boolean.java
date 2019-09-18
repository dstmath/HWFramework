package org.bouncycastle.asn1;

import java.io.IOException;
import org.bouncycastle.util.Arrays;

public class ASN1Boolean extends ASN1Primitive {
    public static final ASN1Boolean FALSE = new ASN1Boolean(false);
    private static final byte[] FALSE_VALUE = {0};
    public static final ASN1Boolean TRUE = new ASN1Boolean(true);
    private static final byte[] TRUE_VALUE = {-1};
    private final byte[] value;

    public ASN1Boolean(boolean z) {
        this.value = z ? TRUE_VALUE : FALSE_VALUE;
    }

    ASN1Boolean(byte[] bArr) {
        if (bArr.length == 1) {
            this.value = bArr[0] == 0 ? FALSE_VALUE : (bArr[0] & 255) == 255 ? TRUE_VALUE : Arrays.clone(bArr);
            return;
        }
        throw new IllegalArgumentException("byte value should have 1 byte in it");
    }

    static ASN1Boolean fromOctetString(byte[] bArr) {
        if (bArr.length == 1) {
            return bArr[0] == 0 ? FALSE : (bArr[0] & 255) == 255 ? TRUE : new ASN1Boolean(bArr);
        }
        throw new IllegalArgumentException("BOOLEAN value should have 1 byte in it");
    }

    public static ASN1Boolean getInstance(int i) {
        return i != 0 ? TRUE : FALSE;
    }

    public static ASN1Boolean getInstance(Object obj) {
        if (obj == null || (obj instanceof ASN1Boolean)) {
            return (ASN1Boolean) obj;
        }
        if (obj instanceof byte[]) {
            try {
                return (ASN1Boolean) fromByteArray((byte[]) obj);
            } catch (IOException e) {
                throw new IllegalArgumentException("failed to construct boolean from byte[]: " + e.getMessage());
            }
        } else {
            throw new IllegalArgumentException("illegal object in getInstance: " + obj.getClass().getName());
        }
    }

    public static ASN1Boolean getInstance(ASN1TaggedObject aSN1TaggedObject, boolean z) {
        ASN1Primitive object = aSN1TaggedObject.getObject();
        return (z || (object instanceof ASN1Boolean)) ? getInstance((Object) object) : fromOctetString(((ASN1OctetString) object).getOctets());
    }

    public static ASN1Boolean getInstance(boolean z) {
        return z ? TRUE : FALSE;
    }

    /* access modifiers changed from: protected */
    public boolean asn1Equals(ASN1Primitive aSN1Primitive) {
        return (aSN1Primitive instanceof ASN1Boolean) && this.value[0] == ((ASN1Boolean) aSN1Primitive).value[0];
    }

    /* access modifiers changed from: package-private */
    public void encode(ASN1OutputStream aSN1OutputStream) throws IOException {
        aSN1OutputStream.writeEncoded(1, this.value);
    }

    /* access modifiers changed from: package-private */
    public int encodedLength() {
        return 3;
    }

    public int hashCode() {
        return this.value[0];
    }

    /* access modifiers changed from: package-private */
    public boolean isConstructed() {
        return false;
    }

    public boolean isTrue() {
        return this.value[0] != 0;
    }

    public String toString() {
        return this.value[0] != 0 ? "TRUE" : "FALSE";
    }
}

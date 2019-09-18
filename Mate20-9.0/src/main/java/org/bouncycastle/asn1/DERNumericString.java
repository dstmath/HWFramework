package org.bouncycastle.asn1;

import java.io.IOException;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Strings;

public class DERNumericString extends ASN1Primitive implements ASN1String {
    private final byte[] string;

    public DERNumericString(String str) {
        this(str, false);
    }

    public DERNumericString(String str, boolean z) {
        if (!z || isNumericString(str)) {
            this.string = Strings.toByteArray(str);
            return;
        }
        throw new IllegalArgumentException("string contains illegal characters");
    }

    DERNumericString(byte[] bArr) {
        this.string = bArr;
    }

    public static DERNumericString getInstance(Object obj) {
        if (obj == null || (obj instanceof DERNumericString)) {
            return (DERNumericString) obj;
        }
        if (obj instanceof byte[]) {
            try {
                return (DERNumericString) fromByteArray((byte[]) obj);
            } catch (Exception e) {
                throw new IllegalArgumentException("encoding error in getInstance: " + e.toString());
            }
        } else {
            throw new IllegalArgumentException("illegal object in getInstance: " + obj.getClass().getName());
        }
    }

    public static DERNumericString getInstance(ASN1TaggedObject aSN1TaggedObject, boolean z) {
        ASN1Primitive object = aSN1TaggedObject.getObject();
        return (z || (object instanceof DERNumericString)) ? getInstance(object) : new DERNumericString(ASN1OctetString.getInstance(object).getOctets());
    }

    public static boolean isNumericString(String str) {
        for (int length = str.length() - 1; length >= 0; length--) {
            char charAt = str.charAt(length);
            if (charAt > 127) {
                return false;
            }
            if (('0' > charAt || charAt > '9') && charAt != ' ') {
                return false;
            }
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean asn1Equals(ASN1Primitive aSN1Primitive) {
        if (!(aSN1Primitive instanceof DERNumericString)) {
            return false;
        }
        return Arrays.areEqual(this.string, ((DERNumericString) aSN1Primitive).string);
    }

    /* access modifiers changed from: package-private */
    public void encode(ASN1OutputStream aSN1OutputStream) throws IOException {
        aSN1OutputStream.writeEncoded(18, this.string);
    }

    /* access modifiers changed from: package-private */
    public int encodedLength() {
        return 1 + StreamUtil.calculateBodyLength(this.string.length) + this.string.length;
    }

    public byte[] getOctets() {
        return Arrays.clone(this.string);
    }

    public String getString() {
        return Strings.fromByteArray(this.string);
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

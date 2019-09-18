package com.android.org.bouncycastle.asn1;

import com.android.org.bouncycastle.util.Arrays;
import com.android.org.bouncycastle.util.Strings;
import java.io.IOException;

public class DERGraphicString extends ASN1Primitive implements ASN1String {
    private final byte[] string;

    public static DERGraphicString getInstance(Object obj) {
        if (obj == null || (obj instanceof DERGraphicString)) {
            return (DERGraphicString) obj;
        }
        if (obj instanceof byte[]) {
            try {
                return (DERGraphicString) fromByteArray((byte[]) obj);
            } catch (Exception e) {
                throw new IllegalArgumentException("encoding error in getInstance: " + e.toString());
            }
        } else {
            throw new IllegalArgumentException("illegal object in getInstance: " + obj.getClass().getName());
        }
    }

    public static DERGraphicString getInstance(ASN1TaggedObject obj, boolean explicit) {
        ASN1Primitive o = obj.getObject();
        if (explicit || (o instanceof DERGraphicString)) {
            return getInstance(o);
        }
        return new DERGraphicString(((ASN1OctetString) o).getOctets());
    }

    public DERGraphicString(byte[] string2) {
        this.string = Arrays.clone(string2);
    }

    public byte[] getOctets() {
        return Arrays.clone(this.string);
    }

    /* access modifiers changed from: package-private */
    public boolean isConstructed() {
        return false;
    }

    /* access modifiers changed from: package-private */
    public int encodedLength() {
        return 1 + StreamUtil.calculateBodyLength(this.string.length) + this.string.length;
    }

    /* access modifiers changed from: package-private */
    public void encode(ASN1OutputStream out) throws IOException {
        out.writeEncoded(25, this.string);
    }

    public int hashCode() {
        return Arrays.hashCode(this.string);
    }

    /* access modifiers changed from: package-private */
    public boolean asn1Equals(ASN1Primitive o) {
        if (!(o instanceof DERGraphicString)) {
            return false;
        }
        return Arrays.areEqual(this.string, ((DERGraphicString) o).string);
    }

    public String getString() {
        return Strings.fromByteArray(this.string);
    }
}

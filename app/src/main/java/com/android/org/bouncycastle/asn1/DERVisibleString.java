package com.android.org.bouncycastle.asn1;

import com.android.org.bouncycastle.util.Arrays;
import com.android.org.bouncycastle.util.Strings;
import java.io.IOException;

public class DERVisibleString extends ASN1Primitive implements ASN1String {
    private final byte[] string;

    public static DERVisibleString getInstance(Object obj) {
        if (obj == null || (obj instanceof DERVisibleString)) {
            return (DERVisibleString) obj;
        }
        if (obj instanceof byte[]) {
            try {
                return (DERVisibleString) ASN1Primitive.fromByteArray((byte[]) obj);
            } catch (Exception e) {
                throw new IllegalArgumentException("encoding error in getInstance: " + e.toString());
            }
        }
        throw new IllegalArgumentException("illegal object in getInstance: " + obj.getClass().getName());
    }

    public static DERVisibleString getInstance(ASN1TaggedObject obj, boolean explicit) {
        ASN1Primitive o = obj.getObject();
        if (explicit || (o instanceof DERVisibleString)) {
            return getInstance(o);
        }
        return new DERVisibleString(ASN1OctetString.getInstance(o).getOctets());
    }

    DERVisibleString(byte[] string) {
        this.string = string;
    }

    public DERVisibleString(String string) {
        this.string = Strings.toByteArray(string);
    }

    public String getString() {
        return Strings.fromByteArray(this.string);
    }

    public String toString() {
        return getString();
    }

    public byte[] getOctets() {
        return Arrays.clone(this.string);
    }

    boolean isConstructed() {
        return false;
    }

    int encodedLength() {
        return (StreamUtil.calculateBodyLength(this.string.length) + 1) + this.string.length;
    }

    void encode(ASN1OutputStream out) throws IOException {
        out.writeEncoded(26, this.string);
    }

    boolean asn1Equals(ASN1Primitive o) {
        if (o instanceof DERVisibleString) {
            return Arrays.areEqual(this.string, ((DERVisibleString) o).string);
        }
        return false;
    }

    public int hashCode() {
        return Arrays.hashCode(this.string);
    }
}

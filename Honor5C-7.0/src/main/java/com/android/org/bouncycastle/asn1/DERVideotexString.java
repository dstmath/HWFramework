package com.android.org.bouncycastle.asn1;

import com.android.org.bouncycastle.util.Arrays;
import com.android.org.bouncycastle.util.Strings;
import java.io.IOException;

public class DERVideotexString extends ASN1Primitive implements ASN1String {
    private final byte[] string;

    public static DERVideotexString getInstance(Object obj) {
        if (obj == null || (obj instanceof DERVideotexString)) {
            return (DERVideotexString) obj;
        }
        if (obj instanceof byte[]) {
            try {
                return (DERVideotexString) ASN1Primitive.fromByteArray((byte[]) obj);
            } catch (Exception e) {
                throw new IllegalArgumentException("encoding error in getInstance: " + e.toString());
            }
        }
        throw new IllegalArgumentException("illegal object in getInstance: " + obj.getClass().getName());
    }

    public static DERVideotexString getInstance(ASN1TaggedObject obj, boolean explicit) {
        ASN1Primitive o = obj.getObject();
        if (explicit || (o instanceof DERVideotexString)) {
            return getInstance(o);
        }
        return new DERVideotexString(((ASN1OctetString) o).getOctets());
    }

    public DERVideotexString(byte[] string) {
        this.string = Arrays.clone(string);
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
        out.writeEncoded(21, this.string);
    }

    public int hashCode() {
        return Arrays.hashCode(this.string);
    }

    boolean asn1Equals(ASN1Primitive o) {
        if (!(o instanceof DERVideotexString)) {
            return false;
        }
        return Arrays.areEqual(this.string, ((DERVideotexString) o).string);
    }

    public String getString() {
        return Strings.fromByteArray(this.string);
    }
}

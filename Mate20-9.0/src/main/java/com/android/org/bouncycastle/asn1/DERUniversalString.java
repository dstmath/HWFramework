package com.android.org.bouncycastle.asn1;

import com.android.org.bouncycastle.util.Arrays;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class DERUniversalString extends ASN1Primitive implements ASN1String {
    private static final char[] table = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    private final byte[] string;

    public static DERUniversalString getInstance(Object obj) {
        if (obj == null || (obj instanceof DERUniversalString)) {
            return (DERUniversalString) obj;
        }
        if (obj instanceof byte[]) {
            try {
                return (DERUniversalString) fromByteArray((byte[]) obj);
            } catch (Exception e) {
                throw new IllegalArgumentException("encoding error getInstance: " + e.toString());
            }
        } else {
            throw new IllegalArgumentException("illegal object in getInstance: " + obj.getClass().getName());
        }
    }

    public static DERUniversalString getInstance(ASN1TaggedObject obj, boolean explicit) {
        ASN1Primitive o = obj.getObject();
        if (explicit || (o instanceof DERUniversalString)) {
            return getInstance(o);
        }
        return new DERUniversalString(((ASN1OctetString) o).getOctets());
    }

    public DERUniversalString(byte[] string2) {
        this.string = Arrays.clone(string2);
    }

    public String getString() {
        StringBuffer buf = new StringBuffer("#");
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        try {
            new ASN1OutputStream(bOut).writeObject(this);
            byte[] string2 = bOut.toByteArray();
            for (int i = 0; i != string2.length; i++) {
                buf.append(table[(string2[i] >>> 4) & 15]);
                buf.append(table[string2[i] & 15]);
            }
            return buf.toString();
        } catch (IOException e) {
            throw new ASN1ParsingException("internal error encoding BitString");
        }
    }

    public String toString() {
        return getString();
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
        out.writeEncoded(28, getOctets());
    }

    /* access modifiers changed from: package-private */
    public boolean asn1Equals(ASN1Primitive o) {
        if (!(o instanceof DERUniversalString)) {
            return false;
        }
        return Arrays.areEqual(this.string, ((DERUniversalString) o).string);
    }

    public int hashCode() {
        return Arrays.hashCode(this.string);
    }
}

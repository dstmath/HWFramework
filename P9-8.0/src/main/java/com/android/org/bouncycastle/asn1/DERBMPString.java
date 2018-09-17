package com.android.org.bouncycastle.asn1;

import com.android.org.bouncycastle.util.Arrays;
import java.io.IOException;

public class DERBMPString extends ASN1Primitive implements ASN1String {
    private final char[] string;

    public static DERBMPString getInstance(Object obj) {
        if (obj == null || (obj instanceof DERBMPString)) {
            return (DERBMPString) obj;
        }
        if (obj instanceof byte[]) {
            try {
                return (DERBMPString) ASN1Primitive.fromByteArray((byte[]) obj);
            } catch (Exception e) {
                throw new IllegalArgumentException("encoding error in getInstance: " + e.toString());
            }
        }
        throw new IllegalArgumentException("illegal object in getInstance: " + obj.getClass().getName());
    }

    public static DERBMPString getInstance(ASN1TaggedObject obj, boolean explicit) {
        ASN1Primitive o = obj.getObject();
        if (explicit || (o instanceof DERBMPString)) {
            return getInstance(o);
        }
        return new DERBMPString(ASN1OctetString.getInstance(o).getOctets());
    }

    DERBMPString(byte[] string) {
        char[] cs = new char[(string.length / 2)];
        for (int i = 0; i != cs.length; i++) {
            cs[i] = (char) ((string[i * 2] << 8) | (string[(i * 2) + 1] & 255));
        }
        this.string = cs;
    }

    DERBMPString(char[] string) {
        this.string = string;
    }

    public DERBMPString(String string) {
        this.string = string.toCharArray();
    }

    public String getString() {
        return new String(this.string);
    }

    public String toString() {
        return getString();
    }

    public int hashCode() {
        return Arrays.hashCode(this.string);
    }

    protected boolean asn1Equals(ASN1Primitive o) {
        if (!(o instanceof DERBMPString)) {
            return false;
        }
        return Arrays.areEqual(this.string, ((DERBMPString) o).string);
    }

    boolean isConstructed() {
        return false;
    }

    int encodedLength() {
        return (StreamUtil.calculateBodyLength(this.string.length * 2) + 1) + (this.string.length * 2);
    }

    void encode(ASN1OutputStream out) throws IOException {
        out.write(30);
        out.writeLength(this.string.length * 2);
        for (int i = 0; i != this.string.length; i++) {
            char c = this.string[i];
            out.write((byte) (c >> 8));
            out.write((byte) c);
        }
    }
}

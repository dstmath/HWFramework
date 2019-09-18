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
                return (DERBMPString) fromByteArray((byte[]) obj);
            } catch (Exception e) {
                throw new IllegalArgumentException("encoding error in getInstance: " + e.toString());
            }
        } else {
            throw new IllegalArgumentException("illegal object in getInstance: " + obj.getClass().getName());
        }
    }

    public static DERBMPString getInstance(ASN1TaggedObject obj, boolean explicit) {
        ASN1Primitive o = obj.getObject();
        if (explicit || (o instanceof DERBMPString)) {
            return getInstance(o);
        }
        return new DERBMPString(ASN1OctetString.getInstance(o).getOctets());
    }

    DERBMPString(byte[] string2) {
        char[] cs = new char[(string2.length / 2)];
        for (int i = 0; i != cs.length; i++) {
            cs[i] = (char) ((string2[2 * i] << 8) | (string2[(2 * i) + 1] & 255));
        }
        this.string = cs;
    }

    DERBMPString(char[] string2) {
        this.string = string2;
    }

    public DERBMPString(String string2) {
        this.string = string2.toCharArray();
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

    /* access modifiers changed from: protected */
    public boolean asn1Equals(ASN1Primitive o) {
        if (!(o instanceof DERBMPString)) {
            return false;
        }
        return Arrays.areEqual(this.string, ((DERBMPString) o).string);
    }

    /* access modifiers changed from: package-private */
    public boolean isConstructed() {
        return false;
    }

    /* access modifiers changed from: package-private */
    public int encodedLength() {
        return 1 + StreamUtil.calculateBodyLength(this.string.length * 2) + (this.string.length * 2);
    }

    /* access modifiers changed from: package-private */
    public void encode(ASN1OutputStream out) throws IOException {
        out.write(30);
        out.writeLength(this.string.length * 2);
        for (int i = 0; i != this.string.length; i++) {
            char c = this.string[i];
            out.write((int) (byte) (c >> 8));
            out.write((int) (byte) c);
        }
    }
}

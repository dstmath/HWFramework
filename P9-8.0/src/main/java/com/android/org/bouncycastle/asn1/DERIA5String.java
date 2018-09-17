package com.android.org.bouncycastle.asn1;

import com.android.org.bouncycastle.util.Arrays;
import com.android.org.bouncycastle.util.Strings;
import java.io.IOException;

public class DERIA5String extends ASN1Primitive implements ASN1String {
    private final byte[] string;

    public static DERIA5String getInstance(Object obj) {
        if (obj == null || (obj instanceof DERIA5String)) {
            return (DERIA5String) obj;
        }
        if (obj instanceof byte[]) {
            try {
                return (DERIA5String) ASN1Primitive.fromByteArray((byte[]) obj);
            } catch (Exception e) {
                throw new IllegalArgumentException("encoding error in getInstance: " + e.toString());
            }
        }
        throw new IllegalArgumentException("illegal object in getInstance: " + obj.getClass().getName());
    }

    public static DERIA5String getInstance(ASN1TaggedObject obj, boolean explicit) {
        ASN1Primitive o = obj.getObject();
        if (explicit || (o instanceof DERIA5String)) {
            return getInstance(o);
        }
        return new DERIA5String(((ASN1OctetString) o).getOctets());
    }

    DERIA5String(byte[] string) {
        this.string = string;
    }

    public DERIA5String(String string) {
        this(string, false);
    }

    public DERIA5String(String string, boolean validate) {
        if (string == null) {
            throw new NullPointerException("string cannot be null");
        } else if (!validate || (isIA5String(string) ^ 1) == 0) {
            this.string = Strings.toByteArray(string);
        } else {
            throw new IllegalArgumentException("string contains illegal characters");
        }
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
        out.writeEncoded(22, this.string);
    }

    public int hashCode() {
        return Arrays.hashCode(this.string);
    }

    boolean asn1Equals(ASN1Primitive o) {
        if (!(o instanceof DERIA5String)) {
            return false;
        }
        return Arrays.areEqual(this.string, ((DERIA5String) o).string);
    }

    public static boolean isIA5String(String str) {
        for (int i = str.length() - 1; i >= 0; i--) {
            if (str.charAt(i) > 127) {
                return false;
            }
        }
        return true;
    }
}

package com.android.org.bouncycastle.asn1;

import com.android.org.bouncycastle.util.Arrays;
import com.android.org.bouncycastle.util.Strings;
import java.io.IOException;

public class DERPrintableString extends ASN1Primitive implements ASN1String {
    private final byte[] string;

    public static DERPrintableString getInstance(Object obj) {
        if (obj == null || (obj instanceof DERPrintableString)) {
            return (DERPrintableString) obj;
        }
        if (obj instanceof byte[]) {
            try {
                return (DERPrintableString) fromByteArray((byte[]) obj);
            } catch (Exception e) {
                throw new IllegalArgumentException("encoding error in getInstance: " + e.toString());
            }
        } else {
            throw new IllegalArgumentException("illegal object in getInstance: " + obj.getClass().getName());
        }
    }

    public static DERPrintableString getInstance(ASN1TaggedObject obj, boolean explicit) {
        ASN1Primitive o = obj.getObject();
        if (explicit || (o instanceof DERPrintableString)) {
            return getInstance(o);
        }
        return new DERPrintableString(ASN1OctetString.getInstance(o).getOctets());
    }

    DERPrintableString(byte[] string2) {
        this.string = string2;
    }

    public DERPrintableString(String string2) {
        this(string2, false);
    }

    public DERPrintableString(String string2, boolean validate) {
        if (!validate || isPrintableString(string2)) {
            this.string = Strings.toByteArray(string2);
            return;
        }
        throw new IllegalArgumentException("string contains illegal characters");
    }

    public String getString() {
        return Strings.fromByteArray(this.string);
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
        out.writeEncoded(19, this.string);
    }

    public int hashCode() {
        return Arrays.hashCode(this.string);
    }

    /* access modifiers changed from: package-private */
    public boolean asn1Equals(ASN1Primitive o) {
        if (!(o instanceof DERPrintableString)) {
            return false;
        }
        return Arrays.areEqual(this.string, ((DERPrintableString) o).string);
    }

    public String toString() {
        return getString();
    }

    public static boolean isPrintableString(String str) {
        for (int i = str.length() - 1; i >= 0; i--) {
            char ch = str.charAt(i);
            if (ch > 127) {
                return false;
            }
            if (('a' > ch || ch > 'z') && (('A' > ch || ch > 'Z') && !(('0' <= ch && ch <= '9') || ch == ' ' || ch == ':' || ch == '=' || ch == '?'))) {
                switch (ch) {
                    case '\'':
                    case '(':
                    case ')':
                        continue;
                    default:
                        switch (ch) {
                            case '+':
                            case ',':
                            case '-':
                            case '.':
                            case '/':
                                break;
                            default:
                                return false;
                        }
                }
            }
        }
        return true;
    }
}

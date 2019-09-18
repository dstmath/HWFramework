package com.android.org.bouncycastle.asn1;

import com.android.org.bouncycastle.util.Arrays;
import java.io.IOException;

public class ASN1Boolean extends ASN1Primitive {
    public static final ASN1Boolean FALSE = new ASN1Boolean(false);
    private static final byte[] FALSE_VALUE = {0};
    public static final ASN1Boolean TRUE = new ASN1Boolean(true);
    private static final byte[] TRUE_VALUE = {-1};
    private final byte[] value;

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

    public static ASN1Boolean getInstance(boolean value2) {
        return value2 ? TRUE : FALSE;
    }

    public static ASN1Boolean getInstance(int value2) {
        return value2 != 0 ? TRUE : FALSE;
    }

    public static ASN1Boolean getInstance(byte[] octets) {
        return octets[0] != 0 ? TRUE : FALSE;
    }

    public static ASN1Boolean getInstance(ASN1TaggedObject obj, boolean explicit) {
        ASN1Primitive o = obj.getObject();
        if (explicit || (o instanceof ASN1Boolean)) {
            return getInstance((Object) o);
        }
        return fromOctetString(((ASN1OctetString) o).getOctets());
    }

    ASN1Boolean(byte[] value2) {
        if (value2.length != 1) {
            throw new IllegalArgumentException("byte value should have 1 byte in it");
        } else if (value2[0] == 0) {
            this.value = FALSE_VALUE;
        } else if ((value2[0] & 255) == 255) {
            this.value = TRUE_VALUE;
        } else {
            this.value = Arrays.clone(value2);
        }
    }

    protected ASN1Boolean(boolean value2) {
        this.value = value2 ? TRUE_VALUE : FALSE_VALUE;
    }

    public boolean isTrue() {
        return this.value[0] != 0;
    }

    /* access modifiers changed from: package-private */
    public boolean isConstructed() {
        return false;
    }

    /* access modifiers changed from: package-private */
    public int encodedLength() {
        return 3;
    }

    /* access modifiers changed from: package-private */
    public void encode(ASN1OutputStream out) throws IOException {
        out.writeEncoded(1, this.value);
    }

    /* access modifiers changed from: protected */
    public boolean asn1Equals(ASN1Primitive o) {
        boolean z = false;
        if (!(o instanceof ASN1Boolean)) {
            return false;
        }
        if (this.value[0] == ((ASN1Boolean) o).value[0]) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        return this.value[0];
    }

    public String toString() {
        return this.value[0] != 0 ? "TRUE" : "FALSE";
    }

    static ASN1Boolean fromOctetString(byte[] value2) {
        if (value2.length != 1) {
            throw new IllegalArgumentException("BOOLEAN value should have 1 byte in it");
        } else if (value2[0] == 0) {
            return FALSE;
        } else {
            if ((value2[0] & 255) == 255) {
                return TRUE;
            }
            return new ASN1Boolean(value2);
        }
    }
}

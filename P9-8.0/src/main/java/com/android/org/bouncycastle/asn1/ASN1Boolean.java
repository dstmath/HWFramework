package com.android.org.bouncycastle.asn1;

import com.android.org.bouncycastle.util.Arrays;
import java.io.IOException;

public class ASN1Boolean extends ASN1Primitive {
    public static final ASN1Boolean FALSE = new ASN1Boolean(false);
    private static final byte[] FALSE_VALUE = new byte[]{(byte) 0};
    public static final ASN1Boolean TRUE = new ASN1Boolean(true);
    private static final byte[] TRUE_VALUE = new byte[]{(byte) -1};
    private final byte[] value;

    public static ASN1Boolean getInstance(Object obj) {
        if (obj == null || (obj instanceof ASN1Boolean)) {
            return (ASN1Boolean) obj;
        }
        if (obj instanceof byte[]) {
            try {
                return (ASN1Boolean) ASN1Primitive.fromByteArray((byte[]) obj);
            } catch (IOException e) {
                throw new IllegalArgumentException("failed to construct boolean from byte[]: " + e.getMessage());
            }
        }
        throw new IllegalArgumentException("illegal object in getInstance: " + obj.getClass().getName());
    }

    public static ASN1Boolean getInstance(boolean value) {
        return value ? TRUE : FALSE;
    }

    public static ASN1Boolean getInstance(int value) {
        return value != 0 ? TRUE : FALSE;
    }

    public static ASN1Boolean getInstance(byte[] octets) {
        return octets[0] != (byte) 0 ? TRUE : FALSE;
    }

    public static ASN1Boolean getInstance(ASN1TaggedObject obj, boolean explicit) {
        Object o = obj.getObject();
        if (explicit || (o instanceof ASN1Boolean)) {
            return getInstance(o);
        }
        return fromOctetString(((ASN1OctetString) o).getOctets());
    }

    protected ASN1Boolean(byte[] value) {
        if (value.length != 1) {
            throw new IllegalArgumentException("byte value should have 1 byte in it");
        } else if (value[0] == (byte) 0) {
            this.value = FALSE_VALUE;
        } else if ((value[0] & 255) == 255) {
            this.value = TRUE_VALUE;
        } else {
            this.value = Arrays.clone(value);
        }
    }

    protected ASN1Boolean(boolean value) {
        this.value = value ? TRUE_VALUE : FALSE_VALUE;
    }

    public boolean isTrue() {
        return this.value[0] != (byte) 0;
    }

    boolean isConstructed() {
        return false;
    }

    int encodedLength() {
        return 3;
    }

    void encode(ASN1OutputStream out) throws IOException {
        out.writeEncoded(1, this.value);
    }

    protected boolean asn1Equals(ASN1Primitive o) {
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
        return this.value[0] != (byte) 0 ? "TRUE" : "FALSE";
    }

    static ASN1Boolean fromOctetString(byte[] value) {
        if (value.length != 1) {
            throw new IllegalArgumentException("BOOLEAN value should have 1 byte in it");
        } else if (value[0] == (byte) 0) {
            return FALSE;
        } else {
            if ((value[0] & 255) == 255) {
                return TRUE;
            }
            return new ASN1Boolean(value);
        }
    }
}

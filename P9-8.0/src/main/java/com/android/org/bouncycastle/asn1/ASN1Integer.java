package com.android.org.bouncycastle.asn1;

import com.android.org.bouncycastle.util.Arrays;
import java.io.IOException;
import java.math.BigInteger;

public class ASN1Integer extends ASN1Primitive {
    private final byte[] bytes;

    public static ASN1Integer getInstance(Object obj) {
        if (obj == null || (obj instanceof ASN1Integer)) {
            return (ASN1Integer) obj;
        }
        if (obj instanceof byte[]) {
            try {
                return (ASN1Integer) ASN1Primitive.fromByteArray((byte[]) obj);
            } catch (Exception e) {
                throw new IllegalArgumentException("encoding error in getInstance: " + e.toString());
            }
        }
        throw new IllegalArgumentException("illegal object in getInstance: " + obj.getClass().getName());
    }

    public static ASN1Integer getInstance(ASN1TaggedObject obj, boolean explicit) {
        ASN1Primitive o = obj.getObject();
        if (explicit || (o instanceof ASN1Integer)) {
            return getInstance(o);
        }
        return new ASN1Integer(ASN1OctetString.getInstance(obj.getObject()).getOctets());
    }

    public ASN1Integer(long value) {
        this.bytes = BigInteger.valueOf(value).toByteArray();
    }

    public ASN1Integer(BigInteger value) {
        this.bytes = value.toByteArray();
    }

    public ASN1Integer(byte[] bytes) {
        this(bytes, true);
    }

    ASN1Integer(byte[] bytes, boolean clone) {
        if (bytes.length > 1) {
            if (bytes[0] == (byte) 0 && (bytes[1] & 128) == 0) {
                throw new IllegalArgumentException("malformed integer");
            } else if (bytes[0] == (byte) -1 && (bytes[1] & 128) != 0) {
                throw new IllegalArgumentException("malformed integer");
            }
        }
        if (clone) {
            bytes = Arrays.clone(bytes);
        }
        this.bytes = bytes;
    }

    public BigInteger getValue() {
        return new BigInteger(this.bytes);
    }

    public BigInteger getPositiveValue() {
        return new BigInteger(1, this.bytes);
    }

    boolean isConstructed() {
        return false;
    }

    int encodedLength() {
        return (StreamUtil.calculateBodyLength(this.bytes.length) + 1) + this.bytes.length;
    }

    void encode(ASN1OutputStream out) throws IOException {
        out.writeEncoded(2, this.bytes);
    }

    public int hashCode() {
        int value = 0;
        for (int i = 0; i != this.bytes.length; i++) {
            value ^= (this.bytes[i] & 255) << (i % 4);
        }
        return value;
    }

    boolean asn1Equals(ASN1Primitive o) {
        if (!(o instanceof ASN1Integer)) {
            return false;
        }
        return Arrays.areEqual(this.bytes, ((ASN1Integer) o).bytes);
    }

    public String toString() {
        return getValue().toString();
    }
}

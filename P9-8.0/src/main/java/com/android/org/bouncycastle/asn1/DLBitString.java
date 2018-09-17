package com.android.org.bouncycastle.asn1;

import java.io.IOException;

public class DLBitString extends ASN1BitString {
    public static ASN1BitString getInstance(Object obj) {
        if (obj == null || (obj instanceof DLBitString)) {
            return (DLBitString) obj;
        }
        if (obj instanceof DERBitString) {
            return (DERBitString) obj;
        }
        if (obj instanceof byte[]) {
            try {
                return (ASN1BitString) ASN1Primitive.fromByteArray((byte[]) obj);
            } catch (Exception e) {
                throw new IllegalArgumentException("encoding error in getInstance: " + e.toString());
            }
        }
        throw new IllegalArgumentException("illegal object in getInstance: " + obj.getClass().getName());
    }

    public static ASN1BitString getInstance(ASN1TaggedObject obj, boolean explicit) {
        ASN1Primitive o = obj.getObject();
        if (explicit || (o instanceof DLBitString)) {
            return getInstance(o);
        }
        return fromOctetString(((ASN1OctetString) o).getOctets());
    }

    protected DLBitString(byte data, int padBits) {
        this(toByteArray(data), padBits);
    }

    private static byte[] toByteArray(byte data) {
        return new byte[]{data};
    }

    public DLBitString(byte[] data, int padBits) {
        super(data, padBits);
    }

    public DLBitString(byte[] data) {
        this(data, 0);
    }

    public DLBitString(int value) {
        super(ASN1BitString.getBytes(value), ASN1BitString.getPadBits(value));
    }

    public DLBitString(ASN1Encodable obj) throws IOException {
        super(obj.toASN1Primitive().getEncoded(ASN1Encoding.DER), 0);
    }

    boolean isConstructed() {
        return false;
    }

    int encodedLength() {
        return ((StreamUtil.calculateBodyLength(this.data.length + 1) + 1) + this.data.length) + 1;
    }

    void encode(ASN1OutputStream out) throws IOException {
        byte[] string = this.data;
        byte[] bytes = new byte[(string.length + 1)];
        bytes[0] = (byte) getPadBits();
        System.arraycopy(string, 0, bytes, 1, bytes.length - 1);
        out.writeEncoded(3, bytes);
    }

    static DLBitString fromOctetString(byte[] bytes) {
        if (bytes.length < 1) {
            throw new IllegalArgumentException("truncated BIT STRING detected");
        }
        int padBits = bytes[0];
        byte[] data = new byte[(bytes.length - 1)];
        if (data.length != 0) {
            System.arraycopy(bytes, 1, data, 0, bytes.length - 1);
        }
        return new DLBitString(data, padBits);
    }
}

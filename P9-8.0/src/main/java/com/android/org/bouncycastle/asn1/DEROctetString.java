package com.android.org.bouncycastle.asn1;

import java.io.IOException;

public class DEROctetString extends ASN1OctetString {
    public DEROctetString(byte[] string) {
        super(string);
    }

    public DEROctetString(ASN1Encodable obj) throws IOException {
        super(obj.toASN1Primitive().getEncoded(ASN1Encoding.DER));
    }

    boolean isConstructed() {
        return false;
    }

    int encodedLength() {
        return (StreamUtil.calculateBodyLength(this.string.length) + 1) + this.string.length;
    }

    void encode(ASN1OutputStream out) throws IOException {
        out.writeEncoded(4, this.string);
    }

    static void encode(DEROutputStream derOut, byte[] bytes) throws IOException {
        derOut.writeEncoded(4, bytes);
    }
}

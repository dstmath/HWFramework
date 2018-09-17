package com.android.org.bouncycastle.asn1;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class DERApplicationSpecific extends ASN1ApplicationSpecific {
    DERApplicationSpecific(boolean isConstructed, int tag, byte[] octets) {
        super(isConstructed, tag, octets);
    }

    public DERApplicationSpecific(int tag, byte[] octets) {
        this(false, tag, octets);
    }

    public DERApplicationSpecific(int tag, ASN1Encodable object) throws IOException {
        this(true, tag, object);
    }

    public DERApplicationSpecific(boolean constructed, int tag, ASN1Encodable object) throws IOException {
        super(!constructed ? object.toASN1Primitive().isConstructed() : true, tag, getEncoding(constructed, object));
    }

    private static byte[] getEncoding(boolean explicit, ASN1Encodable object) throws IOException {
        byte[] data = object.toASN1Primitive().getEncoded(ASN1Encoding.DER);
        if (explicit) {
            return data;
        }
        int lenBytes = ASN1ApplicationSpecific.getLengthOfHeader(data);
        byte[] tmp = new byte[(data.length - lenBytes)];
        System.arraycopy(data, lenBytes, tmp, 0, tmp.length);
        return tmp;
    }

    public DERApplicationSpecific(int tagNo, ASN1EncodableVector vec) {
        super(true, tagNo, getEncodedVector(vec));
    }

    private static byte[] getEncodedVector(ASN1EncodableVector vec) {
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        int i = 0;
        while (i != vec.size()) {
            try {
                bOut.write(((ASN1Object) vec.get(i)).getEncoded(ASN1Encoding.DER));
                i++;
            } catch (IOException e) {
                throw new ASN1ParsingException("malformed object: " + e, e);
            }
        }
        return bOut.toByteArray();
    }

    void encode(ASN1OutputStream out) throws IOException {
        int classBits = 64;
        if (this.isConstructed) {
            classBits = 96;
        }
        out.writeEncoded(classBits, this.tag, this.octets);
    }
}

package com.android.org.bouncycastle.asn1;

import java.io.IOException;
import java.util.Enumeration;

class LazyEncodedSequence extends ASN1Sequence {
    private byte[] encoded;

    LazyEncodedSequence(byte[] encoded) throws IOException {
        this.encoded = encoded;
    }

    private void parse() {
        Enumeration en = new LazyConstructionEnumeration(this.encoded);
        while (en.hasMoreElements()) {
            this.seq.addElement(en.nextElement());
        }
        this.encoded = null;
    }

    public synchronized ASN1Encodable getObjectAt(int index) {
        if (this.encoded != null) {
            parse();
        }
        return super.getObjectAt(index);
    }

    public synchronized Enumeration getObjects() {
        if (this.encoded == null) {
            return super.getObjects();
        }
        return new LazyConstructionEnumeration(this.encoded);
    }

    public synchronized int size() {
        if (this.encoded != null) {
            parse();
        }
        return super.size();
    }

    ASN1Primitive toDERObject() {
        if (this.encoded != null) {
            parse();
        }
        return super.toDERObject();
    }

    ASN1Primitive toDLObject() {
        if (this.encoded != null) {
            parse();
        }
        return super.toDLObject();
    }

    int encodedLength() throws IOException {
        if (this.encoded != null) {
            return (StreamUtil.calculateBodyLength(this.encoded.length) + 1) + this.encoded.length;
        }
        return super.toDLObject().encodedLength();
    }

    void encode(ASN1OutputStream out) throws IOException {
        if (this.encoded != null) {
            out.writeEncoded(48, this.encoded);
        } else {
            super.toDLObject().encode(out);
        }
    }
}

package com.android.org.bouncycastle.asn1;

import java.io.IOException;
import java.util.Enumeration;

class LazyEncodedSequence extends ASN1Sequence {
    private byte[] encoded;

    LazyEncodedSequence(byte[] encoded2) throws IOException {
        this.encoded = encoded2;
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

    /* access modifiers changed from: package-private */
    public ASN1Primitive toDERObject() {
        if (this.encoded != null) {
            parse();
        }
        return super.toDERObject();
    }

    /* access modifiers changed from: package-private */
    public ASN1Primitive toDLObject() {
        if (this.encoded != null) {
            parse();
        }
        return super.toDLObject();
    }

    /* access modifiers changed from: package-private */
    public int encodedLength() throws IOException {
        if (this.encoded != null) {
            return 1 + StreamUtil.calculateBodyLength(this.encoded.length) + this.encoded.length;
        }
        return super.toDLObject().encodedLength();
    }

    /* access modifiers changed from: package-private */
    public void encode(ASN1OutputStream out) throws IOException {
        if (this.encoded != null) {
            out.writeEncoded(48, this.encoded);
        } else {
            super.toDLObject().encode(out);
        }
    }
}

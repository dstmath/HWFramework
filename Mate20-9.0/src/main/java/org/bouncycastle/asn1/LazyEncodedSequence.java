package org.bouncycastle.asn1;

import java.io.IOException;
import java.util.Enumeration;

class LazyEncodedSequence extends ASN1Sequence {
    private byte[] encoded;

    LazyEncodedSequence(byte[] bArr) throws IOException {
        this.encoded = bArr;
    }

    private void parse() {
        LazyConstructionEnumeration lazyConstructionEnumeration = new LazyConstructionEnumeration(this.encoded);
        while (lazyConstructionEnumeration.hasMoreElements()) {
            this.seq.addElement(lazyConstructionEnumeration.nextElement());
        }
        this.encoded = null;
    }

    /* access modifiers changed from: package-private */
    public void encode(ASN1OutputStream aSN1OutputStream) throws IOException {
        if (this.encoded != null) {
            aSN1OutputStream.writeEncoded(48, this.encoded);
        } else {
            super.toDLObject().encode(aSN1OutputStream);
        }
    }

    /* access modifiers changed from: package-private */
    public int encodedLength() throws IOException {
        return this.encoded != null ? 1 + StreamUtil.calculateBodyLength(this.encoded.length) + this.encoded.length : super.toDLObject().encodedLength();
    }

    public synchronized ASN1Encodable getObjectAt(int i) {
        if (this.encoded != null) {
            parse();
        }
        return super.getObjectAt(i);
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
}

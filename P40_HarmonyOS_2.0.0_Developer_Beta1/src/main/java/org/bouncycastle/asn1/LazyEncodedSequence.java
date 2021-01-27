package org.bouncycastle.asn1;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;

/* access modifiers changed from: package-private */
public class LazyEncodedSequence extends ASN1Sequence {
    private byte[] encoded;

    LazyEncodedSequence(byte[] bArr) throws IOException {
        this.encoded = bArr;
    }

    private void force() {
        if (this.encoded != null) {
            ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
            LazyConstructionEnumeration lazyConstructionEnumeration = new LazyConstructionEnumeration(this.encoded);
            while (lazyConstructionEnumeration.hasMoreElements()) {
                aSN1EncodableVector.add((ASN1Primitive) lazyConstructionEnumeration.nextElement());
            }
            this.elements = aSN1EncodableVector.takeElements();
            this.encoded = null;
        }
    }

    /* access modifiers changed from: package-private */
    @Override // org.bouncycastle.asn1.ASN1Sequence, org.bouncycastle.asn1.ASN1Primitive
    public synchronized void encode(ASN1OutputStream aSN1OutputStream, boolean z) throws IOException {
        if (this.encoded != null) {
            aSN1OutputStream.writeEncoded(z, 48, this.encoded);
        } else {
            super.toDLObject().encode(aSN1OutputStream, z);
        }
    }

    /* access modifiers changed from: package-private */
    @Override // org.bouncycastle.asn1.ASN1Primitive
    public synchronized int encodedLength() throws IOException {
        if (this.encoded != null) {
            return StreamUtil.calculateBodyLength(this.encoded.length) + 1 + this.encoded.length;
        }
        return super.toDLObject().encodedLength();
    }

    @Override // org.bouncycastle.asn1.ASN1Sequence
    public synchronized ASN1Encodable getObjectAt(int i) {
        force();
        return super.getObjectAt(i);
    }

    @Override // org.bouncycastle.asn1.ASN1Sequence
    public synchronized Enumeration getObjects() {
        if (this.encoded != null) {
            return new LazyConstructionEnumeration(this.encoded);
        }
        return super.getObjects();
    }

    @Override // org.bouncycastle.asn1.ASN1Sequence, org.bouncycastle.asn1.ASN1Primitive, org.bouncycastle.asn1.ASN1Object
    public synchronized int hashCode() {
        force();
        return super.hashCode();
    }

    @Override // org.bouncycastle.asn1.ASN1Sequence, org.bouncycastle.util.Iterable, java.lang.Iterable
    public synchronized Iterator<ASN1Encodable> iterator() {
        force();
        return super.iterator();
    }

    @Override // org.bouncycastle.asn1.ASN1Sequence
    public synchronized int size() {
        force();
        return super.size();
    }

    @Override // org.bouncycastle.asn1.ASN1Sequence
    public synchronized ASN1Encodable[] toArray() {
        force();
        return super.toArray();
    }

    /* access modifiers changed from: package-private */
    @Override // org.bouncycastle.asn1.ASN1Sequence
    public ASN1Encodable[] toArrayInternal() {
        force();
        return super.toArrayInternal();
    }

    /* access modifiers changed from: package-private */
    @Override // org.bouncycastle.asn1.ASN1Sequence, org.bouncycastle.asn1.ASN1Primitive
    public synchronized ASN1Primitive toDERObject() {
        force();
        return super.toDERObject();
    }

    /* access modifiers changed from: package-private */
    @Override // org.bouncycastle.asn1.ASN1Sequence, org.bouncycastle.asn1.ASN1Primitive
    public synchronized ASN1Primitive toDLObject() {
        force();
        return super.toDLObject();
    }
}

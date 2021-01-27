package org.bouncycastle.asn1;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Iterable;

public abstract class ASN1Sequence extends ASN1Primitive implements Iterable<ASN1Encodable> {
    ASN1Encodable[] elements;

    protected ASN1Sequence() {
        this.elements = ASN1EncodableVector.EMPTY_ELEMENTS;
    }

    protected ASN1Sequence(ASN1Encodable aSN1Encodable) {
        if (aSN1Encodable != null) {
            this.elements = new ASN1Encodable[]{aSN1Encodable};
            return;
        }
        throw new NullPointerException("'element' cannot be null");
    }

    protected ASN1Sequence(ASN1EncodableVector aSN1EncodableVector) {
        if (aSN1EncodableVector != null) {
            this.elements = aSN1EncodableVector.takeElements();
            return;
        }
        throw new NullPointerException("'elementVector' cannot be null");
    }

    protected ASN1Sequence(ASN1Encodable[] aSN1EncodableArr) {
        if (!Arrays.isNullOrContainsNull(aSN1EncodableArr)) {
            this.elements = ASN1EncodableVector.cloneElements(aSN1EncodableArr);
            return;
        }
        throw new NullPointerException("'elements' cannot be null, or contain null");
    }

    ASN1Sequence(ASN1Encodable[] aSN1EncodableArr, boolean z) {
        this.elements = z ? ASN1EncodableVector.cloneElements(aSN1EncodableArr) : aSN1EncodableArr;
    }

    public static ASN1Sequence getInstance(Object obj) {
        if (obj == null || (obj instanceof ASN1Sequence)) {
            return (ASN1Sequence) obj;
        }
        if (obj instanceof ASN1SequenceParser) {
            return getInstance(((ASN1SequenceParser) obj).toASN1Primitive());
        }
        if (obj instanceof byte[]) {
            try {
                return getInstance(fromByteArray((byte[]) obj));
            } catch (IOException e) {
                throw new IllegalArgumentException("failed to construct sequence from byte[]: " + e.getMessage());
            }
        } else {
            if (obj instanceof ASN1Encodable) {
                ASN1Primitive aSN1Primitive = ((ASN1Encodable) obj).toASN1Primitive();
                if (aSN1Primitive instanceof ASN1Sequence) {
                    return (ASN1Sequence) aSN1Primitive;
                }
            }
            throw new IllegalArgumentException("unknown object in getInstance: " + obj.getClass().getName());
        }
    }

    public static ASN1Sequence getInstance(ASN1TaggedObject aSN1TaggedObject, boolean z) {
        if (!z) {
            ASN1Primitive object = aSN1TaggedObject.getObject();
            if (aSN1TaggedObject.isExplicit()) {
                return aSN1TaggedObject instanceof BERTaggedObject ? new BERSequence(object) : new DLSequence(object);
            }
            if (object instanceof ASN1Sequence) {
                ASN1Sequence aSN1Sequence = (ASN1Sequence) object;
                return aSN1TaggedObject instanceof BERTaggedObject ? aSN1Sequence : (ASN1Sequence) aSN1Sequence.toDLObject();
            }
            throw new IllegalArgumentException("unknown object in getInstance: " + aSN1TaggedObject.getClass().getName());
        } else if (aSN1TaggedObject.isExplicit()) {
            return getInstance(aSN1TaggedObject.getObject());
        } else {
            throw new IllegalArgumentException("object implicit - explicit expected.");
        }
    }

    /* access modifiers changed from: package-private */
    @Override // org.bouncycastle.asn1.ASN1Primitive
    public boolean asn1Equals(ASN1Primitive aSN1Primitive) {
        if (!(aSN1Primitive instanceof ASN1Sequence)) {
            return false;
        }
        ASN1Sequence aSN1Sequence = (ASN1Sequence) aSN1Primitive;
        int size = size();
        if (aSN1Sequence.size() != size) {
            return false;
        }
        for (int i = 0; i < size; i++) {
            ASN1Primitive aSN1Primitive2 = this.elements[i].toASN1Primitive();
            ASN1Primitive aSN1Primitive3 = aSN1Sequence.elements[i].toASN1Primitive();
            if (!(aSN1Primitive2 == aSN1Primitive3 || aSN1Primitive2.asn1Equals(aSN1Primitive3))) {
                return false;
            }
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    @Override // org.bouncycastle.asn1.ASN1Primitive
    public abstract void encode(ASN1OutputStream aSN1OutputStream, boolean z) throws IOException;

    public ASN1Encodable getObjectAt(int i) {
        return this.elements[i];
    }

    public Enumeration getObjects() {
        return new Enumeration() {
            /* class org.bouncycastle.asn1.ASN1Sequence.AnonymousClass1 */
            private int pos = 0;

            @Override // java.util.Enumeration
            public boolean hasMoreElements() {
                return this.pos < ASN1Sequence.this.elements.length;
            }

            @Override // java.util.Enumeration
            public Object nextElement() {
                if (this.pos < ASN1Sequence.this.elements.length) {
                    ASN1Encodable[] aSN1EncodableArr = ASN1Sequence.this.elements;
                    int i = this.pos;
                    this.pos = i + 1;
                    return aSN1EncodableArr[i];
                }
                throw new NoSuchElementException("ASN1Sequence Enumeration");
            }
        };
    }

    @Override // org.bouncycastle.asn1.ASN1Primitive, org.bouncycastle.asn1.ASN1Object
    public int hashCode() {
        int length = this.elements.length;
        int i = length + 1;
        while (true) {
            length--;
            if (length < 0) {
                return i;
            }
            i = (i * 257) ^ this.elements[length].toASN1Primitive().hashCode();
        }
    }

    /* access modifiers changed from: package-private */
    @Override // org.bouncycastle.asn1.ASN1Primitive
    public boolean isConstructed() {
        return true;
    }

    @Override // org.bouncycastle.util.Iterable, java.lang.Iterable
    public Iterator<ASN1Encodable> iterator() {
        return new Arrays.Iterator(this.elements);
    }

    public ASN1SequenceParser parser() {
        final int size = size();
        return new ASN1SequenceParser() {
            /* class org.bouncycastle.asn1.ASN1Sequence.AnonymousClass2 */
            private int pos = 0;

            @Override // org.bouncycastle.asn1.InMemoryRepresentable
            public ASN1Primitive getLoadedObject() {
                return ASN1Sequence.this;
            }

            @Override // org.bouncycastle.asn1.ASN1SequenceParser
            public ASN1Encodable readObject() throws IOException {
                if (size == this.pos) {
                    return null;
                }
                ASN1Encodable[] aSN1EncodableArr = ASN1Sequence.this.elements;
                int i = this.pos;
                this.pos = i + 1;
                ASN1Encodable aSN1Encodable = aSN1EncodableArr[i];
                return aSN1Encodable instanceof ASN1Sequence ? ((ASN1Sequence) aSN1Encodable).parser() : aSN1Encodable instanceof ASN1Set ? ((ASN1Set) aSN1Encodable).parser() : aSN1Encodable;
            }

            @Override // org.bouncycastle.asn1.ASN1Encodable
            public ASN1Primitive toASN1Primitive() {
                return ASN1Sequence.this;
            }
        };
    }

    public int size() {
        return this.elements.length;
    }

    public ASN1Encodable[] toArray() {
        return ASN1EncodableVector.cloneElements(this.elements);
    }

    /* access modifiers changed from: package-private */
    public ASN1Encodable[] toArrayInternal() {
        return this.elements;
    }

    /* access modifiers changed from: package-private */
    @Override // org.bouncycastle.asn1.ASN1Primitive
    public ASN1Primitive toDERObject() {
        return new DERSequence(this.elements, false);
    }

    /* access modifiers changed from: package-private */
    @Override // org.bouncycastle.asn1.ASN1Primitive
    public ASN1Primitive toDLObject() {
        return new DLSequence(this.elements, false);
    }

    @Override // java.lang.Object
    public String toString() {
        int size = size();
        if (size == 0) {
            return "[]";
        }
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append('[');
        int i = 0;
        while (true) {
            stringBuffer.append(this.elements[i]);
            i++;
            if (i >= size) {
                stringBuffer.append(']');
                return stringBuffer.toString();
            }
            stringBuffer.append(", ");
        }
    }
}

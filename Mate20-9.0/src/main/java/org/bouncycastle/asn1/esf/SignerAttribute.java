package org.bouncycastle.asn1.esf;

import java.util.Enumeration;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.x509.Attribute;
import org.bouncycastle.asn1.x509.AttributeCertificate;

public class SignerAttribute extends ASN1Object {
    private Object[] values;

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v6, resolved type: java.lang.Object[]} */
    /* JADX WARNING: Multi-variable type inference failed */
    private SignerAttribute(ASN1Sequence aSN1Sequence) {
        this.values = new Object[aSN1Sequence.size()];
        Enumeration objects = aSN1Sequence.getObjects();
        int i = 0;
        while (objects.hasMoreElements()) {
            ASN1TaggedObject instance = ASN1TaggedObject.getInstance(objects.nextElement());
            if (instance.getTagNo() == 0) {
                ASN1Sequence instance2 = ASN1Sequence.getInstance(instance, true);
                Attribute[] attributeArr = new Attribute[instance2.size()];
                for (int i2 = 0; i2 != attributeArr.length; i2++) {
                    attributeArr[i2] = Attribute.getInstance(instance2.getObjectAt(i2));
                }
                this.values[i] = attributeArr;
            } else if (instance.getTagNo() == 1) {
                this.values[i] = AttributeCertificate.getInstance(ASN1Sequence.getInstance(instance, true));
            } else {
                throw new IllegalArgumentException("illegal tag: " + instance.getTagNo());
            }
            i++;
        }
    }

    public SignerAttribute(AttributeCertificate attributeCertificate) {
        this.values = new Object[1];
        this.values[0] = attributeCertificate;
    }

    public SignerAttribute(Attribute[] attributeArr) {
        this.values = new Object[1];
        this.values[0] = attributeArr;
    }

    public static SignerAttribute getInstance(Object obj) {
        if (obj instanceof SignerAttribute) {
            return (SignerAttribute) obj;
        }
        if (obj != null) {
            return new SignerAttribute(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public Object[] getValues() {
        Object[] objArr = new Object[this.values.length];
        System.arraycopy(this.values, 0, objArr, 0, objArr.length);
        return objArr;
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        for (int i = 0; i != this.values.length; i++) {
            aSN1EncodableVector.add(this.values[i] instanceof Attribute[] ? new DERTaggedObject(0, new DERSequence((ASN1Encodable[]) (Attribute[]) this.values[i])) : new DERTaggedObject(1, (AttributeCertificate) this.values[i]));
        }
        return new DERSequence(aSN1EncodableVector);
    }
}

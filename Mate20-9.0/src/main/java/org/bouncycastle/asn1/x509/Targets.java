package org.bouncycastle.asn1.x509;

import java.util.Enumeration;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;

public class Targets extends ASN1Object {
    private ASN1Sequence targets;

    private Targets(ASN1Sequence aSN1Sequence) {
        this.targets = aSN1Sequence;
    }

    public Targets(Target[] targetArr) {
        this.targets = new DERSequence((ASN1Encodable[]) targetArr);
    }

    public static Targets getInstance(Object obj) {
        if (obj instanceof Targets) {
            return (Targets) obj;
        }
        if (obj != null) {
            return new Targets(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public Target[] getTargets() {
        Target[] targetArr = new Target[this.targets.size()];
        Enumeration objects = this.targets.getObjects();
        int i = 0;
        while (objects.hasMoreElements()) {
            targetArr[i] = Target.getInstance(objects.nextElement());
            i++;
        }
        return targetArr;
    }

    public ASN1Primitive toASN1Primitive() {
        return this.targets;
    }
}

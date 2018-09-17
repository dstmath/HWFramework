package com.android.org.bouncycastle.asn1.x509;

import com.android.org.bouncycastle.asn1.ASN1EncodableVector;
import com.android.org.bouncycastle.asn1.ASN1Object;
import com.android.org.bouncycastle.asn1.ASN1Primitive;
import com.android.org.bouncycastle.asn1.ASN1Sequence;
import com.android.org.bouncycastle.asn1.ASN1TaggedObject;
import com.android.org.bouncycastle.asn1.DERSequence;
import com.android.org.bouncycastle.asn1.DERTaggedObject;
import java.util.Enumeration;

public class NameConstraints extends ASN1Object {
    private GeneralSubtree[] excluded;
    private GeneralSubtree[] permitted;

    public static NameConstraints getInstance(Object obj) {
        if (obj instanceof NameConstraints) {
            return (NameConstraints) obj;
        }
        if (obj != null) {
            return new NameConstraints(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    private NameConstraints(ASN1Sequence seq) {
        Enumeration e = seq.getObjects();
        while (e.hasMoreElements()) {
            ASN1TaggedObject o = ASN1TaggedObject.getInstance(e.nextElement());
            switch (o.getTagNo()) {
                case 0:
                    this.permitted = createArray(ASN1Sequence.getInstance(o, false));
                    break;
                case 1:
                    this.excluded = createArray(ASN1Sequence.getInstance(o, false));
                    break;
                default:
                    throw new IllegalArgumentException("Unknown tag encountered: " + o.getTagNo());
            }
        }
    }

    public NameConstraints(GeneralSubtree[] permitted, GeneralSubtree[] excluded) {
        this.permitted = cloneSubtree(permitted);
        this.excluded = cloneSubtree(excluded);
    }

    private GeneralSubtree[] createArray(ASN1Sequence subtree) {
        GeneralSubtree[] ar = new GeneralSubtree[subtree.size()];
        for (int i = 0; i != ar.length; i++) {
            ar[i] = GeneralSubtree.getInstance(subtree.getObjectAt(i));
        }
        return ar;
    }

    public GeneralSubtree[] getPermittedSubtrees() {
        return cloneSubtree(this.permitted);
    }

    public GeneralSubtree[] getExcludedSubtrees() {
        return cloneSubtree(this.excluded);
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector v = new ASN1EncodableVector();
        if (this.permitted != null) {
            v.add(new DERTaggedObject(false, 0, new DERSequence(this.permitted)));
        }
        if (this.excluded != null) {
            v.add(new DERTaggedObject(false, 1, new DERSequence(this.excluded)));
        }
        return new DERSequence(v);
    }

    private static GeneralSubtree[] cloneSubtree(GeneralSubtree[] subtrees) {
        if (subtrees == null) {
            return null;
        }
        GeneralSubtree[] rv = new GeneralSubtree[subtrees.length];
        System.arraycopy(subtrees, 0, rv, 0, rv.length);
        return rv;
    }
}

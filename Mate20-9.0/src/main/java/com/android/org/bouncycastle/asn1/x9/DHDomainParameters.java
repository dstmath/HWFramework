package com.android.org.bouncycastle.asn1.x9;

import com.android.org.bouncycastle.asn1.ASN1Encodable;
import com.android.org.bouncycastle.asn1.ASN1EncodableVector;
import com.android.org.bouncycastle.asn1.ASN1Integer;
import com.android.org.bouncycastle.asn1.ASN1Object;
import com.android.org.bouncycastle.asn1.ASN1Primitive;
import com.android.org.bouncycastle.asn1.ASN1Sequence;
import com.android.org.bouncycastle.asn1.ASN1TaggedObject;
import com.android.org.bouncycastle.asn1.DERSequence;
import java.math.BigInteger;
import java.util.Enumeration;

public class DHDomainParameters extends ASN1Object {
    private ASN1Integer g;
    private ASN1Integer j;
    private ASN1Integer p;
    private ASN1Integer q;
    private DHValidationParms validationParms;

    public static DHDomainParameters getInstance(ASN1TaggedObject obj, boolean explicit) {
        return getInstance(ASN1Sequence.getInstance(obj, explicit));
    }

    public static DHDomainParameters getInstance(Object obj) {
        if (obj == null || (obj instanceof DHDomainParameters)) {
            return (DHDomainParameters) obj;
        }
        if (obj instanceof ASN1Sequence) {
            return new DHDomainParameters((ASN1Sequence) obj);
        }
        throw new IllegalArgumentException("Invalid DHDomainParameters: " + obj.getClass().getName());
    }

    public DHDomainParameters(BigInteger p2, BigInteger g2, BigInteger q2, BigInteger j2, DHValidationParms validationParms2) {
        if (p2 == null) {
            throw new IllegalArgumentException("'p' cannot be null");
        } else if (g2 == null) {
            throw new IllegalArgumentException("'g' cannot be null");
        } else if (q2 != null) {
            this.p = new ASN1Integer(p2);
            this.g = new ASN1Integer(g2);
            this.q = new ASN1Integer(q2);
            this.j = new ASN1Integer(j2);
            this.validationParms = validationParms2;
        } else {
            throw new IllegalArgumentException("'q' cannot be null");
        }
    }

    public DHDomainParameters(ASN1Integer p2, ASN1Integer g2, ASN1Integer q2, ASN1Integer j2, DHValidationParms validationParms2) {
        if (p2 == null) {
            throw new IllegalArgumentException("'p' cannot be null");
        } else if (g2 == null) {
            throw new IllegalArgumentException("'g' cannot be null");
        } else if (q2 != null) {
            this.p = p2;
            this.g = g2;
            this.q = q2;
            this.j = j2;
            this.validationParms = validationParms2;
        } else {
            throw new IllegalArgumentException("'q' cannot be null");
        }
    }

    private DHDomainParameters(ASN1Sequence seq) {
        if (seq.size() < 3 || seq.size() > 5) {
            throw new IllegalArgumentException("Bad sequence size: " + seq.size());
        }
        Enumeration e = seq.getObjects();
        this.p = ASN1Integer.getInstance(e.nextElement());
        this.g = ASN1Integer.getInstance(e.nextElement());
        this.q = ASN1Integer.getInstance(e.nextElement());
        ASN1Encodable next = getNext(e);
        if (next != null && (next instanceof ASN1Integer)) {
            this.j = ASN1Integer.getInstance(next);
            next = getNext(e);
        }
        if (next != null) {
            this.validationParms = DHValidationParms.getInstance(next.toASN1Primitive());
        }
    }

    private static ASN1Encodable getNext(Enumeration e) {
        if (e.hasMoreElements()) {
            return (ASN1Encodable) e.nextElement();
        }
        return null;
    }

    public ASN1Integer getP() {
        return this.p;
    }

    public ASN1Integer getG() {
        return this.g;
    }

    public ASN1Integer getQ() {
        return this.q;
    }

    public ASN1Integer getJ() {
        return this.j;
    }

    public DHValidationParms getValidationParms() {
        return this.validationParms;
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector v = new ASN1EncodableVector();
        v.add(this.p);
        v.add(this.g);
        v.add(this.q);
        if (this.j != null) {
            v.add(this.j);
        }
        if (this.validationParms != null) {
            v.add(this.validationParms);
        }
        return new DERSequence(v);
    }
}

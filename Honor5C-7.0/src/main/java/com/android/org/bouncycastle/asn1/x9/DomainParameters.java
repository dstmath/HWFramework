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

public class DomainParameters extends ASN1Object {
    private final ASN1Integer g;
    private final ASN1Integer j;
    private final ASN1Integer p;
    private final ASN1Integer q;
    private final ValidationParams validationParams;

    public static DomainParameters getInstance(ASN1TaggedObject obj, boolean explicit) {
        return getInstance(ASN1Sequence.getInstance(obj, explicit));
    }

    public static DomainParameters getInstance(Object obj) {
        if (obj instanceof DomainParameters) {
            return (DomainParameters) obj;
        }
        if (obj != null) {
            return new DomainParameters(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public DomainParameters(BigInteger p, BigInteger g, BigInteger q, BigInteger j, ValidationParams validationParams) {
        if (p == null) {
            throw new IllegalArgumentException("'p' cannot be null");
        } else if (g == null) {
            throw new IllegalArgumentException("'g' cannot be null");
        } else if (q == null) {
            throw new IllegalArgumentException("'q' cannot be null");
        } else {
            this.p = new ASN1Integer(p);
            this.g = new ASN1Integer(g);
            this.q = new ASN1Integer(q);
            if (j != null) {
                this.j = new ASN1Integer(j);
            } else {
                this.j = null;
            }
            this.validationParams = validationParams;
        }
    }

    private DomainParameters(ASN1Sequence seq) {
        if (seq.size() < 3 || seq.size() > 5) {
            throw new IllegalArgumentException("Bad sequence size: " + seq.size());
        }
        Enumeration e = seq.getObjects();
        this.p = ASN1Integer.getInstance(e.nextElement());
        this.g = ASN1Integer.getInstance(e.nextElement());
        this.q = ASN1Integer.getInstance(e.nextElement());
        ASN1Encodable next = getNext(e);
        if (next == null || !(next instanceof ASN1Integer)) {
            this.j = null;
        } else {
            this.j = ASN1Integer.getInstance(next);
            next = getNext(e);
        }
        if (next != null) {
            this.validationParams = ValidationParams.getInstance(next.toASN1Primitive());
        } else {
            this.validationParams = null;
        }
    }

    private static ASN1Encodable getNext(Enumeration e) {
        return e.hasMoreElements() ? (ASN1Encodable) e.nextElement() : null;
    }

    public BigInteger getP() {
        return this.p.getPositiveValue();
    }

    public BigInteger getG() {
        return this.g.getPositiveValue();
    }

    public BigInteger getQ() {
        return this.q.getPositiveValue();
    }

    public BigInteger getJ() {
        if (this.j == null) {
            return null;
        }
        return this.j.getPositiveValue();
    }

    public ValidationParams getValidationParams() {
        return this.validationParams;
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector v = new ASN1EncodableVector();
        v.add(this.p);
        v.add(this.g);
        v.add(this.q);
        if (this.j != null) {
            v.add(this.j);
        }
        if (this.validationParams != null) {
            v.add(this.validationParams);
        }
        return new DERSequence(v);
    }
}

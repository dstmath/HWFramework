package com.android.org.bouncycastle.asn1.x509;

import com.android.org.bouncycastle.asn1.ASN1EncodableVector;
import com.android.org.bouncycastle.asn1.ASN1Integer;
import com.android.org.bouncycastle.asn1.ASN1Object;
import com.android.org.bouncycastle.asn1.ASN1Primitive;
import com.android.org.bouncycastle.asn1.ASN1Sequence;
import com.android.org.bouncycastle.asn1.ASN1TaggedObject;
import com.android.org.bouncycastle.asn1.DERSequence;
import com.android.org.bouncycastle.asn1.DERTaggedObject;
import java.math.BigInteger;

public class PolicyConstraints extends ASN1Object {
    private BigInteger inhibitPolicyMapping;
    private BigInteger requireExplicitPolicyMapping;

    public PolicyConstraints(BigInteger requireExplicitPolicyMapping, BigInteger inhibitPolicyMapping) {
        this.requireExplicitPolicyMapping = requireExplicitPolicyMapping;
        this.inhibitPolicyMapping = inhibitPolicyMapping;
    }

    private PolicyConstraints(ASN1Sequence seq) {
        for (int i = 0; i != seq.size(); i++) {
            ASN1TaggedObject to = ASN1TaggedObject.getInstance(seq.getObjectAt(i));
            if (to.getTagNo() == 0) {
                this.requireExplicitPolicyMapping = ASN1Integer.getInstance(to, false).getValue();
            } else if (to.getTagNo() == 1) {
                this.inhibitPolicyMapping = ASN1Integer.getInstance(to, false).getValue();
            } else {
                throw new IllegalArgumentException("Unknown tag encountered.");
            }
        }
    }

    public static PolicyConstraints getInstance(Object obj) {
        if (obj instanceof PolicyConstraints) {
            return (PolicyConstraints) obj;
        }
        if (obj != null) {
            return new PolicyConstraints(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public static PolicyConstraints fromExtensions(Extensions extensions) {
        return getInstance(extensions.getExtensionParsedValue(Extension.policyConstraints));
    }

    public BigInteger getRequireExplicitPolicyMapping() {
        return this.requireExplicitPolicyMapping;
    }

    public BigInteger getInhibitPolicyMapping() {
        return this.inhibitPolicyMapping;
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector v = new ASN1EncodableVector();
        if (this.requireExplicitPolicyMapping != null) {
            v.add(new DERTaggedObject(0, new ASN1Integer(this.requireExplicitPolicyMapping)));
        }
        if (this.inhibitPolicyMapping != null) {
            v.add(new DERTaggedObject(1, new ASN1Integer(this.inhibitPolicyMapping)));
        }
        return new DERSequence(v);
    }
}

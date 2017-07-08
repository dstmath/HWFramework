package com.android.org.bouncycastle.asn1.x509;

import com.android.org.bouncycastle.asn1.ASN1Encodable;
import com.android.org.bouncycastle.asn1.ASN1EncodableVector;
import com.android.org.bouncycastle.asn1.ASN1Object;
import com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import com.android.org.bouncycastle.asn1.ASN1Primitive;
import com.android.org.bouncycastle.asn1.ASN1Sequence;
import com.android.org.bouncycastle.asn1.DERIA5String;
import com.android.org.bouncycastle.asn1.DERSequence;

public class PolicyQualifierInfo extends ASN1Object {
    private ASN1ObjectIdentifier policyQualifierId;
    private ASN1Encodable qualifier;

    public PolicyQualifierInfo(ASN1ObjectIdentifier policyQualifierId, ASN1Encodable qualifier) {
        this.policyQualifierId = policyQualifierId;
        this.qualifier = qualifier;
    }

    public PolicyQualifierInfo(String cps) {
        this.policyQualifierId = PolicyQualifierId.id_qt_cps;
        this.qualifier = new DERIA5String(cps);
    }

    public PolicyQualifierInfo(ASN1Sequence as) {
        if (as.size() != 2) {
            throw new IllegalArgumentException("Bad sequence size: " + as.size());
        }
        this.policyQualifierId = ASN1ObjectIdentifier.getInstance(as.getObjectAt(0));
        this.qualifier = as.getObjectAt(1);
    }

    public static PolicyQualifierInfo getInstance(Object obj) {
        if (obj instanceof PolicyQualifierInfo) {
            return (PolicyQualifierInfo) obj;
        }
        if (obj != null) {
            return new PolicyQualifierInfo(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public ASN1ObjectIdentifier getPolicyQualifierId() {
        return this.policyQualifierId;
    }

    public ASN1Encodable getQualifier() {
        return this.qualifier;
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector dev = new ASN1EncodableVector();
        dev.add(this.policyQualifierId);
        dev.add(this.qualifier);
        return new DERSequence(dev);
    }
}

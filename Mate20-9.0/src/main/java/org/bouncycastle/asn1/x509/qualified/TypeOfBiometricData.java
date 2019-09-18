package org.bouncycastle.asn1.x509.qualified;

import org.bouncycastle.asn1.ASN1Choice;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;

public class TypeOfBiometricData extends ASN1Object implements ASN1Choice {
    public static final int HANDWRITTEN_SIGNATURE = 1;
    public static final int PICTURE = 0;
    ASN1Encodable obj;

    public TypeOfBiometricData(int i) {
        if (i == 0 || i == 1) {
            this.obj = new ASN1Integer((long) i);
            return;
        }
        throw new IllegalArgumentException("unknow PredefinedBiometricType : " + i);
    }

    public TypeOfBiometricData(ASN1ObjectIdentifier aSN1ObjectIdentifier) {
        this.obj = aSN1ObjectIdentifier;
    }

    public static TypeOfBiometricData getInstance(Object obj2) {
        if (obj2 == null || (obj2 instanceof TypeOfBiometricData)) {
            return (TypeOfBiometricData) obj2;
        }
        if (obj2 instanceof ASN1Integer) {
            return new TypeOfBiometricData(ASN1Integer.getInstance(obj2).getValue().intValue());
        }
        if (obj2 instanceof ASN1ObjectIdentifier) {
            return new TypeOfBiometricData(ASN1ObjectIdentifier.getInstance(obj2));
        }
        throw new IllegalArgumentException("unknown object in getInstance");
    }

    public ASN1ObjectIdentifier getBiometricDataOid() {
        return (ASN1ObjectIdentifier) this.obj;
    }

    public int getPredefinedBiometricType() {
        return ((ASN1Integer) this.obj).getValue().intValue();
    }

    public boolean isPredefined() {
        return this.obj instanceof ASN1Integer;
    }

    public ASN1Primitive toASN1Primitive() {
        return this.obj.toASN1Primitive();
    }
}

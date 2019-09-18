package org.bouncycastle.asn1.x509;

import org.bouncycastle.asn1.ASN1Choice;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.util.Strings;

public class DistributionPointName extends ASN1Object implements ASN1Choice {
    public static final int FULL_NAME = 0;
    public static final int NAME_RELATIVE_TO_CRL_ISSUER = 1;
    ASN1Encodable name;
    int type;

    public DistributionPointName(int i, ASN1Encodable aSN1Encodable) {
        this.type = i;
        this.name = aSN1Encodable;
    }

    public DistributionPointName(ASN1TaggedObject aSN1TaggedObject) {
        this.type = aSN1TaggedObject.getTagNo();
        this.name = this.type == 0 ? GeneralNames.getInstance(aSN1TaggedObject, false) : ASN1Set.getInstance(aSN1TaggedObject, false);
    }

    public DistributionPointName(GeneralNames generalNames) {
        this(0, generalNames);
    }

    private void appendObject(StringBuffer stringBuffer, String str, String str2, String str3) {
        stringBuffer.append("    ");
        stringBuffer.append(str2);
        stringBuffer.append(":");
        stringBuffer.append(str);
        stringBuffer.append("    ");
        stringBuffer.append("    ");
        stringBuffer.append(str3);
        stringBuffer.append(str);
    }

    public static DistributionPointName getInstance(Object obj) {
        if (obj == null || (obj instanceof DistributionPointName)) {
            return (DistributionPointName) obj;
        }
        if (obj instanceof ASN1TaggedObject) {
            return new DistributionPointName((ASN1TaggedObject) obj);
        }
        throw new IllegalArgumentException("unknown object in factory: " + obj.getClass().getName());
    }

    public static DistributionPointName getInstance(ASN1TaggedObject aSN1TaggedObject, boolean z) {
        return getInstance(ASN1TaggedObject.getInstance(aSN1TaggedObject, true));
    }

    public ASN1Encodable getName() {
        return this.name;
    }

    public int getType() {
        return this.type;
    }

    public ASN1Primitive toASN1Primitive() {
        return new DERTaggedObject(false, this.type, this.name);
    }

    public String toString() {
        String lineSeparator = Strings.lineSeparator();
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("DistributionPointName: [");
        stringBuffer.append(lineSeparator);
        appendObject(stringBuffer, lineSeparator, this.type == 0 ? "fullName" : "nameRelativeToCRLIssuer", this.name.toString());
        stringBuffer.append("]");
        stringBuffer.append(lineSeparator);
        return stringBuffer.toString();
    }
}

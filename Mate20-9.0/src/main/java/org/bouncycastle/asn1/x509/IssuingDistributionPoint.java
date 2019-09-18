package org.bouncycastle.asn1.x509;

import org.bouncycastle.asn1.ASN1Boolean;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.util.Strings;

public class IssuingDistributionPoint extends ASN1Object {
    private DistributionPointName distributionPoint;
    private boolean indirectCRL;
    private boolean onlyContainsAttributeCerts;
    private boolean onlyContainsCACerts;
    private boolean onlyContainsUserCerts;
    private ReasonFlags onlySomeReasons;
    private ASN1Sequence seq;

    private IssuingDistributionPoint(ASN1Sequence aSN1Sequence) {
        this.seq = aSN1Sequence;
        for (int i = 0; i != aSN1Sequence.size(); i++) {
            ASN1TaggedObject instance = ASN1TaggedObject.getInstance(aSN1Sequence.getObjectAt(i));
            switch (instance.getTagNo()) {
                case 0:
                    this.distributionPoint = DistributionPointName.getInstance(instance, true);
                    break;
                case 1:
                    this.onlyContainsUserCerts = ASN1Boolean.getInstance(instance, false).isTrue();
                    break;
                case 2:
                    this.onlyContainsCACerts = ASN1Boolean.getInstance(instance, false).isTrue();
                    break;
                case 3:
                    this.onlySomeReasons = new ReasonFlags(ReasonFlags.getInstance(instance, false));
                    break;
                case 4:
                    this.indirectCRL = ASN1Boolean.getInstance(instance, false).isTrue();
                    break;
                case 5:
                    this.onlyContainsAttributeCerts = ASN1Boolean.getInstance(instance, false).isTrue();
                    break;
                default:
                    throw new IllegalArgumentException("unknown tag in IssuingDistributionPoint");
            }
        }
    }

    public IssuingDistributionPoint(DistributionPointName distributionPointName, boolean z, boolean z2) {
        this(distributionPointName, false, false, null, z, z2);
    }

    public IssuingDistributionPoint(DistributionPointName distributionPointName, boolean z, boolean z2, ReasonFlags reasonFlags, boolean z3, boolean z4) {
        this.distributionPoint = distributionPointName;
        this.indirectCRL = z3;
        this.onlyContainsAttributeCerts = z4;
        this.onlyContainsCACerts = z2;
        this.onlyContainsUserCerts = z;
        this.onlySomeReasons = reasonFlags;
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        if (distributionPointName != null) {
            aSN1EncodableVector.add(new DERTaggedObject(true, 0, distributionPointName));
        }
        if (z) {
            aSN1EncodableVector.add(new DERTaggedObject(false, 1, ASN1Boolean.getInstance(true)));
        }
        if (z2) {
            aSN1EncodableVector.add(new DERTaggedObject(false, 2, ASN1Boolean.getInstance(true)));
        }
        if (reasonFlags != null) {
            aSN1EncodableVector.add(new DERTaggedObject(false, 3, reasonFlags));
        }
        if (z3) {
            aSN1EncodableVector.add(new DERTaggedObject(false, 4, ASN1Boolean.getInstance(true)));
        }
        if (z4) {
            aSN1EncodableVector.add(new DERTaggedObject(false, 5, ASN1Boolean.getInstance(true)));
        }
        this.seq = new DERSequence(aSN1EncodableVector);
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

    private String booleanToString(boolean z) {
        return z ? "true" : "false";
    }

    public static IssuingDistributionPoint getInstance(Object obj) {
        if (obj instanceof IssuingDistributionPoint) {
            return (IssuingDistributionPoint) obj;
        }
        if (obj != null) {
            return new IssuingDistributionPoint(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public static IssuingDistributionPoint getInstance(ASN1TaggedObject aSN1TaggedObject, boolean z) {
        return getInstance(ASN1Sequence.getInstance(aSN1TaggedObject, z));
    }

    public DistributionPointName getDistributionPoint() {
        return this.distributionPoint;
    }

    public ReasonFlags getOnlySomeReasons() {
        return this.onlySomeReasons;
    }

    public boolean isIndirectCRL() {
        return this.indirectCRL;
    }

    public boolean onlyContainsAttributeCerts() {
        return this.onlyContainsAttributeCerts;
    }

    public boolean onlyContainsCACerts() {
        return this.onlyContainsCACerts;
    }

    public boolean onlyContainsUserCerts() {
        return this.onlyContainsUserCerts;
    }

    public ASN1Primitive toASN1Primitive() {
        return this.seq;
    }

    public String toString() {
        String lineSeparator = Strings.lineSeparator();
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("IssuingDistributionPoint: [");
        stringBuffer.append(lineSeparator);
        if (this.distributionPoint != null) {
            appendObject(stringBuffer, lineSeparator, "distributionPoint", this.distributionPoint.toString());
        }
        if (this.onlyContainsUserCerts) {
            appendObject(stringBuffer, lineSeparator, "onlyContainsUserCerts", booleanToString(this.onlyContainsUserCerts));
        }
        if (this.onlyContainsCACerts) {
            appendObject(stringBuffer, lineSeparator, "onlyContainsCACerts", booleanToString(this.onlyContainsCACerts));
        }
        if (this.onlySomeReasons != null) {
            appendObject(stringBuffer, lineSeparator, "onlySomeReasons", this.onlySomeReasons.toString());
        }
        if (this.onlyContainsAttributeCerts) {
            appendObject(stringBuffer, lineSeparator, "onlyContainsAttributeCerts", booleanToString(this.onlyContainsAttributeCerts));
        }
        if (this.indirectCRL) {
            appendObject(stringBuffer, lineSeparator, "indirectCRL", booleanToString(this.indirectCRL));
        }
        stringBuffer.append("]");
        stringBuffer.append(lineSeparator);
        return stringBuffer.toString();
    }
}

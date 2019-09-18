package org.bouncycastle.asn1.x509;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.util.Strings;

public class GeneralNames extends ASN1Object {
    private final GeneralName[] names;

    private GeneralNames(ASN1Sequence aSN1Sequence) {
        this.names = new GeneralName[aSN1Sequence.size()];
        for (int i = 0; i != aSN1Sequence.size(); i++) {
            this.names[i] = GeneralName.getInstance(aSN1Sequence.getObjectAt(i));
        }
    }

    public GeneralNames(GeneralName generalName) {
        this.names = new GeneralName[]{generalName};
    }

    public GeneralNames(GeneralName[] generalNameArr) {
        this.names = generalNameArr;
    }

    public static GeneralNames fromExtensions(Extensions extensions, ASN1ObjectIdentifier aSN1ObjectIdentifier) {
        return getInstance(extensions.getExtensionParsedValue(aSN1ObjectIdentifier));
    }

    public static GeneralNames getInstance(Object obj) {
        if (obj instanceof GeneralNames) {
            return (GeneralNames) obj;
        }
        if (obj != null) {
            return new GeneralNames(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public static GeneralNames getInstance(ASN1TaggedObject aSN1TaggedObject, boolean z) {
        return getInstance(ASN1Sequence.getInstance(aSN1TaggedObject, z));
    }

    public GeneralName[] getNames() {
        GeneralName[] generalNameArr = new GeneralName[this.names.length];
        System.arraycopy(this.names, 0, generalNameArr, 0, this.names.length);
        return generalNameArr;
    }

    public ASN1Primitive toASN1Primitive() {
        return new DERSequence((ASN1Encodable[]) this.names);
    }

    public String toString() {
        StringBuffer stringBuffer = new StringBuffer();
        String lineSeparator = Strings.lineSeparator();
        stringBuffer.append("GeneralNames:");
        stringBuffer.append(lineSeparator);
        for (int i = 0; i != this.names.length; i++) {
            stringBuffer.append("    ");
            stringBuffer.append(this.names[i]);
            stringBuffer.append(lineSeparator);
        }
        return stringBuffer.toString();
    }
}

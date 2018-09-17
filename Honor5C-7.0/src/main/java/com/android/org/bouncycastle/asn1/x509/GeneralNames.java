package com.android.org.bouncycastle.asn1.x509;

import com.android.org.bouncycastle.asn1.ASN1Object;
import com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import com.android.org.bouncycastle.asn1.ASN1Primitive;
import com.android.org.bouncycastle.asn1.ASN1Sequence;
import com.android.org.bouncycastle.asn1.ASN1TaggedObject;
import com.android.org.bouncycastle.asn1.DERSequence;
import com.android.org.bouncycastle.util.Strings;

public class GeneralNames extends ASN1Object {
    private final GeneralName[] names;

    public static GeneralNames getInstance(Object obj) {
        if (obj instanceof GeneralNames) {
            return (GeneralNames) obj;
        }
        if (obj != null) {
            return new GeneralNames(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public static GeneralNames getInstance(ASN1TaggedObject obj, boolean explicit) {
        return getInstance(ASN1Sequence.getInstance(obj, explicit));
    }

    public static GeneralNames fromExtensions(Extensions extensions, ASN1ObjectIdentifier extOID) {
        return getInstance(extensions.getExtensionParsedValue(extOID));
    }

    public GeneralNames(GeneralName name) {
        this.names = new GeneralName[]{name};
    }

    public GeneralNames(GeneralName[] names) {
        this.names = names;
    }

    private GeneralNames(ASN1Sequence seq) {
        this.names = new GeneralName[seq.size()];
        for (int i = 0; i != seq.size(); i++) {
            this.names[i] = GeneralName.getInstance(seq.getObjectAt(i));
        }
    }

    public GeneralName[] getNames() {
        GeneralName[] tmp = new GeneralName[this.names.length];
        System.arraycopy(this.names, 0, tmp, 0, this.names.length);
        return tmp;
    }

    public ASN1Primitive toASN1Primitive() {
        return new DERSequence(this.names);
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        String sep = Strings.lineSeparator();
        buf.append("GeneralNames:");
        buf.append(sep);
        for (int i = 0; i != this.names.length; i++) {
            buf.append("    ");
            buf.append(this.names[i]);
            buf.append(sep);
        }
        return buf.toString();
    }
}

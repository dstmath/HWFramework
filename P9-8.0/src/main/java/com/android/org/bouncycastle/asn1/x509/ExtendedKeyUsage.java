package com.android.org.bouncycastle.asn1.x509;

import com.android.org.bouncycastle.asn1.ASN1Encodable;
import com.android.org.bouncycastle.asn1.ASN1EncodableVector;
import com.android.org.bouncycastle.asn1.ASN1Object;
import com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import com.android.org.bouncycastle.asn1.ASN1Primitive;
import com.android.org.bouncycastle.asn1.ASN1Sequence;
import com.android.org.bouncycastle.asn1.ASN1TaggedObject;
import com.android.org.bouncycastle.asn1.DERSequence;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class ExtendedKeyUsage extends ASN1Object {
    ASN1Sequence seq;
    Hashtable usageTable = new Hashtable();

    public static ExtendedKeyUsage getInstance(ASN1TaggedObject obj, boolean explicit) {
        return getInstance(ASN1Sequence.getInstance(obj, explicit));
    }

    public static ExtendedKeyUsage getInstance(Object obj) {
        if (obj instanceof ExtendedKeyUsage) {
            return (ExtendedKeyUsage) obj;
        }
        if (obj != null) {
            return new ExtendedKeyUsage(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public static ExtendedKeyUsage fromExtensions(Extensions extensions) {
        return getInstance(extensions.getExtensionParsedValue(Extension.extendedKeyUsage));
    }

    public ExtendedKeyUsage(KeyPurposeId usage) {
        this.seq = new DERSequence((ASN1Encodable) usage);
        this.usageTable.put(usage, usage);
    }

    private ExtendedKeyUsage(ASN1Sequence seq) {
        this.seq = seq;
        Enumeration e = seq.getObjects();
        while (e.hasMoreElements()) {
            ASN1Encodable o = (ASN1Encodable) e.nextElement();
            if (o.toASN1Primitive() instanceof ASN1ObjectIdentifier) {
                this.usageTable.put(o, o);
            } else {
                throw new IllegalArgumentException("Only ASN1ObjectIdentifiers allowed in ExtendedKeyUsage.");
            }
        }
    }

    public ExtendedKeyUsage(KeyPurposeId[] usages) {
        ASN1EncodableVector v = new ASN1EncodableVector();
        for (int i = 0; i != usages.length; i++) {
            v.add(usages[i]);
            this.usageTable.put(usages[i], usages[i]);
        }
        this.seq = new DERSequence(v);
    }

    public ExtendedKeyUsage(Vector usages) {
        ASN1EncodableVector v = new ASN1EncodableVector();
        Enumeration e = usages.elements();
        while (e.hasMoreElements()) {
            KeyPurposeId o = KeyPurposeId.getInstance(e.nextElement());
            v.add(o);
            this.usageTable.put(o, o);
        }
        this.seq = new DERSequence(v);
    }

    public boolean hasKeyPurposeId(KeyPurposeId keyPurposeId) {
        return this.usageTable.get(keyPurposeId) != null;
    }

    public KeyPurposeId[] getUsages() {
        KeyPurposeId[] temp = new KeyPurposeId[this.seq.size()];
        int i = 0;
        Enumeration it = this.seq.getObjects();
        while (it.hasMoreElements()) {
            int i2 = i + 1;
            temp[i] = KeyPurposeId.getInstance(it.nextElement());
            i = i2;
        }
        return temp;
    }

    public int size() {
        return this.usageTable.size();
    }

    public ASN1Primitive toASN1Primitive() {
        return this.seq;
    }
}

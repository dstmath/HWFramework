package com.android.org.bouncycastle.asn1.x500;

import com.android.org.bouncycastle.asn1.ASN1Choice;
import com.android.org.bouncycastle.asn1.ASN1Encodable;
import com.android.org.bouncycastle.asn1.ASN1Object;
import com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import com.android.org.bouncycastle.asn1.ASN1Primitive;
import com.android.org.bouncycastle.asn1.ASN1Sequence;
import com.android.org.bouncycastle.asn1.ASN1TaggedObject;
import com.android.org.bouncycastle.asn1.DERSequence;
import com.android.org.bouncycastle.asn1.x500.style.BCStyle;
import java.util.Enumeration;

public class X500Name extends ASN1Object implements ASN1Choice {
    private static X500NameStyle defaultStyle = BCStyle.INSTANCE;
    private int hashCodeValue;
    private boolean isHashCodeCalculated;
    private RDN[] rdns;
    private X500NameStyle style;

    public X500Name(X500NameStyle style2, X500Name name) {
        this.rdns = name.rdns;
        this.style = style2;
    }

    public static X500Name getInstance(ASN1TaggedObject obj, boolean explicit) {
        return getInstance(ASN1Sequence.getInstance(obj, true));
    }

    public static X500Name getInstance(Object obj) {
        if (obj instanceof X500Name) {
            return (X500Name) obj;
        }
        if (obj != null) {
            return new X500Name(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public static X500Name getInstance(X500NameStyle style2, Object obj) {
        if (obj instanceof X500Name) {
            return new X500Name(style2, (X500Name) obj);
        }
        if (obj != null) {
            return new X500Name(style2, ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    private X500Name(ASN1Sequence seq) {
        this(defaultStyle, seq);
    }

    private X500Name(X500NameStyle style2, ASN1Sequence seq) {
        this.style = style2;
        this.rdns = new RDN[seq.size()];
        int index = 0;
        Enumeration e = seq.getObjects();
        while (e.hasMoreElements()) {
            this.rdns[index] = RDN.getInstance(e.nextElement());
            index++;
        }
    }

    public X500Name(RDN[] rDNs) {
        this(defaultStyle, rDNs);
    }

    public X500Name(X500NameStyle style2, RDN[] rDNs) {
        this.rdns = rDNs;
        this.style = style2;
    }

    public X500Name(String dirName) {
        this(defaultStyle, dirName);
    }

    public X500Name(X500NameStyle style2, String dirName) {
        this(style2.fromString(dirName));
        this.style = style2;
    }

    public RDN[] getRDNs() {
        RDN[] tmp = new RDN[this.rdns.length];
        System.arraycopy(this.rdns, 0, tmp, 0, tmp.length);
        return tmp;
    }

    public ASN1ObjectIdentifier[] getAttributeTypes() {
        int count = 0;
        for (int i = 0; i != this.rdns.length; i++) {
            count += this.rdns[i].size();
        }
        ASN1ObjectIdentifier[] res = new ASN1ObjectIdentifier[count];
        int count2 = 0;
        for (int i2 = 0; i2 != this.rdns.length; i2++) {
            RDN rdn = this.rdns[i2];
            if (rdn.isMultiValued()) {
                AttributeTypeAndValue[] attr = rdn.getTypesAndValues();
                int count3 = count2;
                int j = 0;
                while (j != attr.length) {
                    res[count3] = attr[j].getType();
                    j++;
                    count3++;
                }
                count2 = count3;
            } else if (rdn.size() != 0) {
                res[count2] = rdn.getFirst().getType();
                count2++;
            }
        }
        return res;
    }

    public RDN[] getRDNs(ASN1ObjectIdentifier attributeType) {
        RDN[] res = new RDN[this.rdns.length];
        int count = 0;
        for (int i = 0; i != this.rdns.length; i++) {
            RDN rdn = this.rdns[i];
            if (rdn.isMultiValued()) {
                AttributeTypeAndValue[] attr = rdn.getTypesAndValues();
                int j = 0;
                while (true) {
                    if (j == attr.length) {
                        break;
                    } else if (attr[j].getType().equals(attributeType)) {
                        res[count] = rdn;
                        count++;
                        break;
                    } else {
                        j++;
                    }
                }
            } else if (rdn.getFirst().getType().equals(attributeType)) {
                res[count] = rdn;
                count++;
            }
        }
        RDN[] tmp = new RDN[count];
        System.arraycopy(res, 0, tmp, 0, tmp.length);
        return tmp;
    }

    public ASN1Primitive toASN1Primitive() {
        return new DERSequence((ASN1Encodable[]) this.rdns);
    }

    public int hashCode() {
        if (this.isHashCodeCalculated) {
            return this.hashCodeValue;
        }
        this.isHashCodeCalculated = true;
        this.hashCodeValue = this.style.calculateHashCode(this);
        return this.hashCodeValue;
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof X500Name) && !(obj instanceof ASN1Sequence)) {
            return false;
        }
        if (toASN1Primitive().equals(((ASN1Encodable) obj).toASN1Primitive())) {
            return true;
        }
        try {
            return this.style.areEqual(this, new X500Name(ASN1Sequence.getInstance(((ASN1Encodable) obj).toASN1Primitive())));
        } catch (Exception e) {
            return false;
        }
    }

    public String toString() {
        return this.style.toString(this);
    }

    public static void setDefaultStyle(X500NameStyle style2) {
        if (style2 != null) {
            defaultStyle = style2;
            return;
        }
        throw new NullPointerException("cannot set style to null");
    }

    public static X500NameStyle getDefaultStyle() {
        return defaultStyle;
    }
}

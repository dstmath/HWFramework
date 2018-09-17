package com.android.org.bouncycastle.asn1.cms;

import com.android.org.bouncycastle.asn1.ASN1Encodable;
import com.android.org.bouncycastle.asn1.ASN1EncodableVector;
import com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import com.android.org.bouncycastle.asn1.ASN1Set;
import com.android.org.bouncycastle.asn1.DERSet;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class AttributeTable {
    private Hashtable attributes;

    public AttributeTable(Hashtable attrs) {
        this.attributes = new Hashtable();
        this.attributes = copyTable(attrs);
    }

    public AttributeTable(ASN1EncodableVector v) {
        this.attributes = new Hashtable();
        for (int i = 0; i != v.size(); i++) {
            Attribute a = Attribute.getInstance(v.get(i));
            addAttribute(a.getAttrType(), a);
        }
    }

    public AttributeTable(ASN1Set s) {
        this.attributes = new Hashtable();
        for (int i = 0; i != s.size(); i++) {
            Attribute a = Attribute.getInstance(s.getObjectAt(i));
            addAttribute(a.getAttrType(), a);
        }
    }

    public AttributeTable(Attribute attr) {
        this.attributes = new Hashtable();
        addAttribute(attr.getAttrType(), attr);
    }

    public AttributeTable(Attributes attrs) {
        this(ASN1Set.getInstance(attrs.toASN1Primitive()));
    }

    private void addAttribute(ASN1ObjectIdentifier oid, Attribute a) {
        Object value = this.attributes.get(oid);
        if (value == null) {
            this.attributes.put(oid, a);
            return;
        }
        Vector v;
        if (value instanceof Attribute) {
            v = new Vector();
            v.addElement(value);
            v.addElement(a);
        } else {
            v = (Vector) value;
            v.addElement(a);
        }
        this.attributes.put(oid, v);
    }

    public Attribute get(ASN1ObjectIdentifier oid) {
        Object value = this.attributes.get(oid);
        if (value instanceof Vector) {
            return (Attribute) ((Vector) value).elementAt(0);
        }
        return (Attribute) value;
    }

    public ASN1EncodableVector getAll(ASN1ObjectIdentifier oid) {
        ASN1EncodableVector v = new ASN1EncodableVector();
        Object value = this.attributes.get(oid);
        if (value instanceof Vector) {
            Enumeration e = ((Vector) value).elements();
            while (e.hasMoreElements()) {
                v.add((Attribute) e.nextElement());
            }
        } else if (value != null) {
            v.add((Attribute) value);
        }
        return v;
    }

    public int size() {
        int size = 0;
        Enumeration en = this.attributes.elements();
        while (en.hasMoreElements()) {
            Object o = en.nextElement();
            if (o instanceof Vector) {
                size += ((Vector) o).size();
            } else {
                size++;
            }
        }
        return size;
    }

    public Hashtable toHashtable() {
        return copyTable(this.attributes);
    }

    public ASN1EncodableVector toASN1EncodableVector() {
        ASN1EncodableVector v = new ASN1EncodableVector();
        Enumeration e = this.attributes.elements();
        while (e.hasMoreElements()) {
            Object value = e.nextElement();
            if (value instanceof Vector) {
                Enumeration en = ((Vector) value).elements();
                while (en.hasMoreElements()) {
                    v.add(Attribute.getInstance(en.nextElement()));
                }
            } else {
                v.add(Attribute.getInstance(value));
            }
        }
        return v;
    }

    public Attributes toASN1Structure() {
        return new Attributes(toASN1EncodableVector());
    }

    private Hashtable copyTable(Hashtable in) {
        Hashtable out = new Hashtable();
        Enumeration e = in.keys();
        while (e.hasMoreElements()) {
            Object key = e.nextElement();
            out.put(key, in.get(key));
        }
        return out;
    }

    public AttributeTable add(ASN1ObjectIdentifier attrType, ASN1Encodable attrValue) {
        AttributeTable newTable = new AttributeTable(this.attributes);
        newTable.addAttribute(attrType, new Attribute(attrType, new DERSet(attrValue)));
        return newTable;
    }

    public AttributeTable remove(ASN1ObjectIdentifier attrType) {
        AttributeTable newTable = new AttributeTable(this.attributes);
        newTable.attributes.remove(attrType);
        return newTable;
    }
}

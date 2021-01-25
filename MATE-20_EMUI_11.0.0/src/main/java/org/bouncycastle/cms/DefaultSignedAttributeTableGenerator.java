package org.bouncycastle.cms;

import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.cms.Attribute;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.cms.CMSAlgorithmProtection;
import org.bouncycastle.asn1.cms.CMSAttributes;
import org.bouncycastle.asn1.cms.Time;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;

public class DefaultSignedAttributeTableGenerator implements CMSAttributeTableGenerator {
    private final Hashtable table;

    public DefaultSignedAttributeTableGenerator() {
        this.table = new Hashtable();
    }

    public DefaultSignedAttributeTableGenerator(AttributeTable attributeTable) {
        this.table = attributeTable != null ? attributeTable.toHashtable() : new Hashtable();
    }

    private static Hashtable copyHashTable(Hashtable hashtable) {
        Hashtable hashtable2 = new Hashtable();
        Enumeration keys = hashtable.keys();
        while (keys.hasMoreElements()) {
            Object nextElement = keys.nextElement();
            hashtable2.put(nextElement, hashtable.get(nextElement));
        }
        return hashtable2;
    }

    /* access modifiers changed from: protected */
    public Hashtable createStandardAttributeTable(Map map) {
        ASN1ObjectIdentifier instance;
        Hashtable copyHashTable = copyHashTable(this.table);
        if (!copyHashTable.containsKey(CMSAttributes.contentType) && (instance = ASN1ObjectIdentifier.getInstance(map.get(CMSAttributeTableGenerator.CONTENT_TYPE))) != null) {
            Attribute attribute = new Attribute(CMSAttributes.contentType, new DERSet(instance));
            copyHashTable.put(attribute.getAttrType(), attribute);
        }
        if (!copyHashTable.containsKey(CMSAttributes.signingTime)) {
            Attribute attribute2 = new Attribute(CMSAttributes.signingTime, new DERSet(new Time(new Date())));
            copyHashTable.put(attribute2.getAttrType(), attribute2);
        }
        if (!copyHashTable.containsKey(CMSAttributes.messageDigest)) {
            Attribute attribute3 = new Attribute(CMSAttributes.messageDigest, new DERSet(new DEROctetString((byte[]) map.get(CMSAttributeTableGenerator.DIGEST))));
            copyHashTable.put(attribute3.getAttrType(), attribute3);
        }
        if (!copyHashTable.contains(CMSAttributes.cmsAlgorithmProtect)) {
            Attribute attribute4 = new Attribute(CMSAttributes.cmsAlgorithmProtect, new DERSet(new CMSAlgorithmProtection((AlgorithmIdentifier) map.get(CMSAttributeTableGenerator.DIGEST_ALGORITHM_IDENTIFIER), 1, (AlgorithmIdentifier) map.get(CMSAttributeTableGenerator.SIGNATURE_ALGORITHM_IDENTIFIER))));
            copyHashTable.put(attribute4.getAttrType(), attribute4);
        }
        return copyHashTable;
    }

    @Override // org.bouncycastle.cms.CMSAttributeTableGenerator
    public AttributeTable getAttributes(Map map) {
        return new AttributeTable(createStandardAttributeTable(map));
    }
}

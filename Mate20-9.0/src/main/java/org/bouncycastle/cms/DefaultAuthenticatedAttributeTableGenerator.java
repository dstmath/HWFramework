package org.bouncycastle.cms;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.cms.Attribute;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.cms.CMSAlgorithmProtection;
import org.bouncycastle.asn1.cms.CMSAttributes;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;

public class DefaultAuthenticatedAttributeTableGenerator implements CMSAttributeTableGenerator {
    private final Hashtable table;

    public DefaultAuthenticatedAttributeTableGenerator() {
        this.table = new Hashtable();
    }

    public DefaultAuthenticatedAttributeTableGenerator(AttributeTable attributeTable) {
        this.table = attributeTable != null ? attributeTable.toHashtable() : new Hashtable();
    }

    /* access modifiers changed from: protected */
    public Hashtable createStandardAttributeTable(Map map) {
        Hashtable hashtable = new Hashtable();
        Enumeration keys = this.table.keys();
        while (keys.hasMoreElements()) {
            Object nextElement = keys.nextElement();
            hashtable.put(nextElement, this.table.get(nextElement));
        }
        if (!hashtable.containsKey(CMSAttributes.contentType)) {
            Attribute attribute = new Attribute(CMSAttributes.contentType, new DERSet((ASN1Encodable) ASN1ObjectIdentifier.getInstance(map.get(CMSAttributeTableGenerator.CONTENT_TYPE))));
            hashtable.put(attribute.getAttrType(), attribute);
        }
        if (!hashtable.containsKey(CMSAttributes.messageDigest)) {
            Attribute attribute2 = new Attribute(CMSAttributes.messageDigest, new DERSet((ASN1Encodable) new DEROctetString((byte[]) map.get(CMSAttributeTableGenerator.DIGEST))));
            hashtable.put(attribute2.getAttrType(), attribute2);
        }
        if (!hashtable.contains(CMSAttributes.cmsAlgorithmProtect)) {
            Attribute attribute3 = new Attribute(CMSAttributes.cmsAlgorithmProtect, new DERSet((ASN1Encodable) new CMSAlgorithmProtection((AlgorithmIdentifier) map.get(CMSAttributeTableGenerator.DIGEST_ALGORITHM_IDENTIFIER), 2, (AlgorithmIdentifier) map.get(CMSAttributeTableGenerator.MAC_ALGORITHM_IDENTIFIER))));
            hashtable.put(attribute3.getAttrType(), attribute3);
        }
        return hashtable;
    }

    public AttributeTable getAttributes(Map map) {
        return new AttributeTable(createStandardAttributeTable(map));
    }
}

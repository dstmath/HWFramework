package org.apache.harmony.xml.dom;

import org.w3c.dom.EntityReference;

public class EntityReferenceImpl extends LeafNodeImpl implements EntityReference {
    private String name;

    EntityReferenceImpl(DocumentImpl document, String name2) {
        super(document);
        this.name = name2;
    }

    public String getNodeName() {
        return this.name;
    }

    public short getNodeType() {
        return 5;
    }
}

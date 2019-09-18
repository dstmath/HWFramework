package org.apache.harmony.xml.dom;

import org.w3c.dom.Entity;

public class EntityImpl extends NodeImpl implements Entity {
    private String notationName;
    private String publicID;
    private String systemID;

    EntityImpl(DocumentImpl document, String notationName2, String publicID2, String systemID2) {
        super(document);
        this.notationName = notationName2;
        this.publicID = publicID2;
        this.systemID = systemID2;
    }

    public String getNodeName() {
        return getNotationName();
    }

    public short getNodeType() {
        return 6;
    }

    public String getNotationName() {
        return this.notationName;
    }

    public String getPublicId() {
        return this.publicID;
    }

    public String getSystemId() {
        return this.systemID;
    }

    public String getInputEncoding() {
        throw new UnsupportedOperationException();
    }

    public String getXmlEncoding() {
        throw new UnsupportedOperationException();
    }

    public String getXmlVersion() {
        throw new UnsupportedOperationException();
    }
}

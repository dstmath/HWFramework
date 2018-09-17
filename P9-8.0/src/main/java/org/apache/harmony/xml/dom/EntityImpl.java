package org.apache.harmony.xml.dom;

import org.w3c.dom.Entity;

public class EntityImpl extends NodeImpl implements Entity {
    private String notationName;
    private String publicID;
    private String systemID;

    EntityImpl(DocumentImpl document, String notationName, String publicID, String systemID) {
        super(document);
        this.notationName = notationName;
        this.publicID = publicID;
        this.systemID = systemID;
    }

    public String getNodeName() {
        return getNotationName();
    }

    public short getNodeType() {
        return (short) 6;
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

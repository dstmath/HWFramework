package org.apache.harmony.xml.dom;

import org.w3c.dom.Notation;

public class NotationImpl extends LeafNodeImpl implements Notation {
    private String notationName;
    private String publicID;
    private String systemID;

    NotationImpl(DocumentImpl document, String notationName2, String publicID2, String systemID2) {
        super(document);
    }

    public String getNodeName() {
        return this.notationName;
    }

    public short getNodeType() {
        return 12;
    }

    public String getPublicId() {
        return this.publicID;
    }

    public String getSystemId() {
        return this.systemID;
    }
}

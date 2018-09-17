package org.apache.harmony.xml.dom;

import org.w3c.dom.Comment;

public final class CommentImpl extends CharacterDataImpl implements Comment {
    CommentImpl(DocumentImpl document, String data) {
        super(document, data);
    }

    public String getNodeName() {
        return "#comment";
    }

    public short getNodeType() {
        return (short) 8;
    }

    public boolean containsDashDash() {
        return this.buffer.indexOf("--") != -1;
    }
}

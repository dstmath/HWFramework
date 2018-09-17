package org.w3c.dom;

public interface Attr extends Node {
    String getName();

    Element getOwnerElement();

    TypeInfo getSchemaTypeInfo();

    boolean getSpecified();

    String getValue();

    boolean isId();

    void setValue(String str) throws DOMException;
}

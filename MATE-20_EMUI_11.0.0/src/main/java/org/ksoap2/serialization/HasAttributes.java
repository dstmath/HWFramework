package org.ksoap2.serialization;

public interface HasAttributes {
    void getAttribute(int i, AttributeInfo attributeInfo);

    int getAttributeCount();

    void getAttributeInfo(int i, AttributeInfo attributeInfo);

    void setAttribute(AttributeInfo attributeInfo);
}

package org.w3c.dom;

public interface DOMLocator {
    int getByteOffset();

    int getColumnNumber();

    int getLineNumber();

    Node getRelatedNode();

    String getUri();

    int getUtf16Offset();
}

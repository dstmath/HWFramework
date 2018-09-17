package org.w3c.dom;

public interface DocumentType extends Node {
    NamedNodeMap getEntities();

    String getInternalSubset();

    String getName();

    NamedNodeMap getNotations();

    String getPublicId();

    String getSystemId();
}

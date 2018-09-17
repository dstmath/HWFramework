package org.w3c.dom;

public interface Entity extends Node {
    String getInputEncoding();

    String getNotationName();

    String getPublicId();

    String getSystemId();

    String getXmlEncoding();

    String getXmlVersion();
}

package org.w3c.dom;

public interface ProcessingInstruction extends Node {
    String getData();

    String getTarget();

    void setData(String str) throws DOMException;
}

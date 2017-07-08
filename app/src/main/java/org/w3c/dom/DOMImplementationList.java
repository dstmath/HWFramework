package org.w3c.dom;

public interface DOMImplementationList {
    int getLength();

    DOMImplementation item(int i);
}

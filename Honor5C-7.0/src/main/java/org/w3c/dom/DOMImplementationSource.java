package org.w3c.dom;

public interface DOMImplementationSource {
    DOMImplementation getDOMImplementation(String str);

    DOMImplementationList getDOMImplementationList(String str);
}

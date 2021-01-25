package ohos.org.w3c.dom.traversal;

import ohos.org.w3c.dom.DOMException;
import ohos.org.w3c.dom.Node;

public interface NodeIterator {
    void detach();

    boolean getExpandEntityReferences();

    NodeFilter getFilter();

    Node getRoot();

    int getWhatToShow();

    Node nextNode() throws DOMException;

    Node previousNode() throws DOMException;
}

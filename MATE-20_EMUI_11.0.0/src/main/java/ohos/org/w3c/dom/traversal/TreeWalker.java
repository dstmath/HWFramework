package ohos.org.w3c.dom.traversal;

import ohos.org.w3c.dom.DOMException;
import ohos.org.w3c.dom.Node;

public interface TreeWalker {
    Node firstChild();

    Node getCurrentNode();

    boolean getExpandEntityReferences();

    NodeFilter getFilter();

    Node getRoot();

    int getWhatToShow();

    Node lastChild();

    Node nextNode();

    Node nextSibling();

    Node parentNode();

    Node previousNode();

    Node previousSibling();

    void setCurrentNode(Node node) throws DOMException;
}

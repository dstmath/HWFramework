package ohos.org.w3c.dom.traversal;

import ohos.org.w3c.dom.DOMException;
import ohos.org.w3c.dom.Node;

public interface DocumentTraversal {
    NodeIterator createNodeIterator(Node node, int i, NodeFilter nodeFilter, boolean z) throws DOMException;

    TreeWalker createTreeWalker(Node node, int i, NodeFilter nodeFilter, boolean z) throws DOMException;
}

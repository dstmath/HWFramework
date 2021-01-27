package ohos.com.sun.org.apache.xerces.internal.dom;

import ohos.org.w3c.dom.DOMException;
import ohos.org.w3c.dom.Node;
import ohos.org.w3c.dom.traversal.NodeFilter;
import ohos.org.w3c.dom.traversal.NodeIterator;

public class NodeIteratorImpl implements NodeIterator {
    private Node fCurrentNode;
    private boolean fDetach = false;
    private DocumentImpl fDocument;
    private boolean fEntityReferenceExpansion;
    private boolean fForward = true;
    private NodeFilter fNodeFilter;
    private Node fRoot;
    private int fWhatToShow = -1;

    public NodeIteratorImpl(DocumentImpl documentImpl, Node node, int i, NodeFilter nodeFilter, boolean z) {
        this.fDocument = documentImpl;
        this.fRoot = node;
        this.fCurrentNode = null;
        this.fWhatToShow = i;
        this.fNodeFilter = nodeFilter;
        this.fEntityReferenceExpansion = z;
    }

    public Node getRoot() {
        return this.fRoot;
    }

    public int getWhatToShow() {
        return this.fWhatToShow;
    }

    public NodeFilter getFilter() {
        return this.fNodeFilter;
    }

    public boolean getExpandEntityReferences() {
        return this.fEntityReferenceExpansion;
    }

    public Node nextNode() {
        Node node;
        if (this.fDetach) {
            throw new DOMException(11, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "INVALID_STATE_ERR", null));
        } else if (this.fRoot == null) {
            return null;
        } else {
            Node node2 = this.fCurrentNode;
            boolean z = false;
            while (!z) {
                if (!this.fForward && node2 != null) {
                    node = this.fCurrentNode;
                } else if (this.fEntityReferenceExpansion || node2 == null || node2.getNodeType() != 5) {
                    node = nextNode(node2, true);
                } else {
                    node = nextNode(node2, false);
                }
                node2 = node;
                this.fForward = true;
                if (node2 == null) {
                    return null;
                }
                z = acceptNode(node2);
                if (z) {
                    this.fCurrentNode = node2;
                    return this.fCurrentNode;
                }
            }
            return null;
        }
    }

    public Node previousNode() {
        Node node;
        Node node2;
        if (!this.fDetach) {
            if (!(this.fRoot == null || (node = this.fCurrentNode) == null)) {
                Node node3 = node;
                boolean z = false;
                while (!z) {
                    if (!this.fForward || node3 == null) {
                        node2 = previousNode(node3);
                    } else {
                        node2 = this.fCurrentNode;
                    }
                    node3 = node2;
                    this.fForward = false;
                    if (node3 == null) {
                        return null;
                    }
                    z = acceptNode(node3);
                    if (z) {
                        this.fCurrentNode = node3;
                        return this.fCurrentNode;
                    }
                }
            }
            return null;
        }
        throw new DOMException(11, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "INVALID_STATE_ERR", null));
    }

    /* access modifiers changed from: package-private */
    public boolean acceptNode(Node node) {
        return this.fNodeFilter == null ? (this.fWhatToShow & (1 << (node.getNodeType() - 1))) != 0 : (this.fWhatToShow & (1 << (node.getNodeType() - 1))) != 0 && this.fNodeFilter.acceptNode(node) == 1;
    }

    /* access modifiers changed from: package-private */
    public Node matchNodeOrParent(Node node) {
        Node node2 = this.fCurrentNode;
        if (node2 == null) {
            return null;
        }
        while (node2 != this.fRoot) {
            if (node == node2) {
                return node2;
            }
            node2 = node2.getParentNode();
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public Node nextNode(Node node, boolean z) {
        if (node == null) {
            return this.fRoot;
        }
        if (z && node.hasChildNodes()) {
            return node.getFirstChild();
        }
        if (node == this.fRoot) {
            return null;
        }
        Node nextSibling = node.getNextSibling();
        if (nextSibling != null) {
            return nextSibling;
        }
        Node parentNode = node.getParentNode();
        while (parentNode != null && parentNode != this.fRoot) {
            Node nextSibling2 = parentNode.getNextSibling();
            if (nextSibling2 != null) {
                return nextSibling2;
            }
            parentNode = parentNode.getParentNode();
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public Node previousNode(Node node) {
        if (node == this.fRoot) {
            return null;
        }
        Node previousSibling = node.getPreviousSibling();
        if (previousSibling == null) {
            return node.getParentNode();
        }
        if (previousSibling.hasChildNodes() && (this.fEntityReferenceExpansion || previousSibling.getNodeType() != 5)) {
            while (previousSibling.hasChildNodes()) {
                previousSibling = previousSibling.getLastChild();
            }
        }
        return previousSibling;
    }

    public void removeNode(Node node) {
        Node matchNodeOrParent;
        if (node != null && (matchNodeOrParent = matchNodeOrParent(node)) != null) {
            if (this.fForward) {
                this.fCurrentNode = previousNode(matchNodeOrParent);
                return;
            }
            Node nextNode = nextNode(matchNodeOrParent, false);
            if (nextNode != null) {
                this.fCurrentNode = nextNode;
                return;
            }
            this.fCurrentNode = previousNode(matchNodeOrParent);
            this.fForward = true;
        }
    }

    public void detach() {
        this.fDetach = true;
        this.fDocument.removeNodeIterator(this);
    }
}

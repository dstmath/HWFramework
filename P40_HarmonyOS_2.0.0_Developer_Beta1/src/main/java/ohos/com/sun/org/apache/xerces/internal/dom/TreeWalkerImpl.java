package ohos.com.sun.org.apache.xerces.internal.dom;

import ohos.org.w3c.dom.DOMException;
import ohos.org.w3c.dom.Node;
import ohos.org.w3c.dom.traversal.NodeFilter;
import ohos.org.w3c.dom.traversal.TreeWalker;

public class TreeWalkerImpl implements TreeWalker {
    Node fCurrentNode;
    private boolean fEntityReferenceExpansion = false;
    NodeFilter fNodeFilter;
    Node fRoot;
    int fWhatToShow = -1;

    public TreeWalkerImpl(Node node, int i, NodeFilter nodeFilter, boolean z) {
        this.fCurrentNode = node;
        this.fRoot = node;
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

    public void setWhatShow(int i) {
        this.fWhatToShow = i;
    }

    public NodeFilter getFilter() {
        return this.fNodeFilter;
    }

    public boolean getExpandEntityReferences() {
        return this.fEntityReferenceExpansion;
    }

    public Node getCurrentNode() {
        return this.fCurrentNode;
    }

    public void setCurrentNode(Node node) {
        if (node != null) {
            this.fCurrentNode = node;
            return;
        }
        throw new DOMException(9, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NOT_SUPPORTED_ERR", null));
    }

    public Node parentNode() {
        Node node = this.fCurrentNode;
        if (node == null) {
            return null;
        }
        Node parentNode = getParentNode(node);
        if (parentNode != null) {
            this.fCurrentNode = parentNode;
        }
        return parentNode;
    }

    public Node firstChild() {
        Node node = this.fCurrentNode;
        if (node == null) {
            return null;
        }
        Node firstChild = getFirstChild(node);
        if (firstChild != null) {
            this.fCurrentNode = firstChild;
        }
        return firstChild;
    }

    public Node lastChild() {
        Node node = this.fCurrentNode;
        if (node == null) {
            return null;
        }
        Node lastChild = getLastChild(node);
        if (lastChild != null) {
            this.fCurrentNode = lastChild;
        }
        return lastChild;
    }

    public Node previousSibling() {
        Node node = this.fCurrentNode;
        if (node == null) {
            return null;
        }
        Node previousSibling = getPreviousSibling(node);
        if (previousSibling != null) {
            this.fCurrentNode = previousSibling;
        }
        return previousSibling;
    }

    public Node nextSibling() {
        Node node = this.fCurrentNode;
        if (node == null) {
            return null;
        }
        Node nextSibling = getNextSibling(node);
        if (nextSibling != null) {
            this.fCurrentNode = nextSibling;
        }
        return nextSibling;
    }

    public Node previousNode() {
        Node node = this.fCurrentNode;
        if (node == null) {
            return null;
        }
        Node previousSibling = getPreviousSibling(node);
        if (previousSibling == null) {
            Node parentNode = getParentNode(this.fCurrentNode);
            if (parentNode == null) {
                return null;
            }
            this.fCurrentNode = parentNode;
            return this.fCurrentNode;
        }
        Node lastChild = getLastChild(previousSibling);
        Node node2 = lastChild;
        while (lastChild != null) {
            node2 = lastChild;
            lastChild = getLastChild(lastChild);
        }
        if (node2 != null) {
            this.fCurrentNode = node2;
            return this.fCurrentNode;
        }
        this.fCurrentNode = previousSibling;
        return this.fCurrentNode;
    }

    public Node nextNode() {
        Node node = this.fCurrentNode;
        if (node == null) {
            return null;
        }
        Node firstChild = getFirstChild(node);
        if (firstChild != null) {
            this.fCurrentNode = firstChild;
            return firstChild;
        }
        Node nextSibling = getNextSibling(this.fCurrentNode);
        if (nextSibling != null) {
            this.fCurrentNode = nextSibling;
            return nextSibling;
        }
        Node parentNode = getParentNode(this.fCurrentNode);
        while (parentNode != null) {
            Node nextSibling2 = getNextSibling(parentNode);
            if (nextSibling2 != null) {
                this.fCurrentNode = nextSibling2;
                return nextSibling2;
            }
            parentNode = getParentNode(parentNode);
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public Node getParentNode(Node node) {
        Node parentNode;
        if (node == null || node == this.fRoot || (parentNode = node.getParentNode()) == null) {
            return null;
        }
        if (acceptNode(parentNode) == 1) {
            return parentNode;
        }
        return getParentNode(parentNode);
    }

    /* access modifiers changed from: package-private */
    public Node getNextSibling(Node node) {
        return getNextSibling(node, this.fRoot);
    }

    /* access modifiers changed from: package-private */
    public Node getNextSibling(Node node, Node node2) {
        if (node == null || node == node2) {
            return null;
        }
        Node nextSibling = node.getNextSibling();
        if (nextSibling == null) {
            Node parentNode = node.getParentNode();
            if (parentNode == null || parentNode == node2 || acceptNode(parentNode) != 3) {
                return null;
            }
            return getNextSibling(parentNode, node2);
        }
        short acceptNode = acceptNode(nextSibling);
        if (acceptNode == 1) {
            return nextSibling;
        }
        if (acceptNode != 3) {
            return getNextSibling(nextSibling, node2);
        }
        Node firstChild = getFirstChild(nextSibling);
        return firstChild == null ? getNextSibling(nextSibling, node2) : firstChild;
    }

    /* access modifiers changed from: package-private */
    public Node getPreviousSibling(Node node) {
        return getPreviousSibling(node, this.fRoot);
    }

    /* access modifiers changed from: package-private */
    public Node getPreviousSibling(Node node, Node node2) {
        if (node == null || node == node2) {
            return null;
        }
        Node previousSibling = node.getPreviousSibling();
        if (previousSibling == null) {
            Node parentNode = node.getParentNode();
            if (parentNode == null || parentNode == node2 || acceptNode(parentNode) != 3) {
                return null;
            }
            return getPreviousSibling(parentNode, node2);
        }
        short acceptNode = acceptNode(previousSibling);
        if (acceptNode == 1) {
            return previousSibling;
        }
        if (acceptNode != 3) {
            return getPreviousSibling(previousSibling, node2);
        }
        Node lastChild = getLastChild(previousSibling);
        return lastChild == null ? getPreviousSibling(previousSibling, node2) : lastChild;
    }

    /* access modifiers changed from: package-private */
    public Node getFirstChild(Node node) {
        Node firstChild;
        if (node == null) {
            return null;
        }
        if ((!this.fEntityReferenceExpansion && node.getNodeType() == 5) || (firstChild = node.getFirstChild()) == null) {
            return null;
        }
        short acceptNode = acceptNode(firstChild);
        if (acceptNode == 1) {
            return firstChild;
        }
        if (acceptNode != 3 || !firstChild.hasChildNodes()) {
            return getNextSibling(firstChild, node);
        }
        Node firstChild2 = getFirstChild(firstChild);
        return firstChild2 == null ? getNextSibling(firstChild, node) : firstChild2;
    }

    /* access modifiers changed from: package-private */
    public Node getLastChild(Node node) {
        Node lastChild;
        if (node == null) {
            return null;
        }
        if ((!this.fEntityReferenceExpansion && node.getNodeType() == 5) || (lastChild = node.getLastChild()) == null) {
            return null;
        }
        short acceptNode = acceptNode(lastChild);
        if (acceptNode == 1) {
            return lastChild;
        }
        if (acceptNode != 3 || !lastChild.hasChildNodes()) {
            return getPreviousSibling(lastChild, node);
        }
        Node lastChild2 = getLastChild(lastChild);
        return lastChild2 == null ? getPreviousSibling(lastChild, node) : lastChild2;
    }

    /* access modifiers changed from: package-private */
    public short acceptNode(Node node) {
        if (this.fNodeFilter == null) {
            return (this.fWhatToShow & (1 << (node.getNodeType() - 1))) != 0 ? (short) 1 : 3;
        }
        if ((this.fWhatToShow & (1 << (node.getNodeType() - 1))) != 0) {
            return this.fNodeFilter.acceptNode(node);
        }
        return 3;
    }
}

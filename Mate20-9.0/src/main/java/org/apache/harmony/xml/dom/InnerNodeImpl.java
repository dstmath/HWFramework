package org.apache.harmony.xml.dom;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.w3c.dom.DOMException;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class InnerNodeImpl extends LeafNodeImpl {
    List<LeafNodeImpl> children = new ArrayList();

    protected InnerNodeImpl(DocumentImpl document) {
        super(document);
    }

    public Node appendChild(Node newChild) throws DOMException {
        return insertChildAt(newChild, this.children.size());
    }

    public NodeList getChildNodes() {
        NodeListImpl list = new NodeListImpl();
        for (LeafNodeImpl node : this.children) {
            list.add(node);
        }
        return list;
    }

    public Node getFirstChild() {
        if (!this.children.isEmpty()) {
            return this.children.get(0);
        }
        return null;
    }

    public Node getLastChild() {
        if (!this.children.isEmpty()) {
            return this.children.get(this.children.size() - 1);
        }
        return null;
    }

    public Node getNextSibling() {
        if (this.parent == null || this.index + 1 >= this.parent.children.size()) {
            return null;
        }
        return this.parent.children.get(this.index + 1);
    }

    public boolean hasChildNodes() {
        return this.children.size() != 0;
    }

    public Node insertBefore(Node newChild, Node refChild) throws DOMException {
        LeafNodeImpl refChildImpl = (LeafNodeImpl) refChild;
        if (refChildImpl == null) {
            return appendChild(newChild);
        }
        if (refChildImpl.document != this.document) {
            throw new DOMException(4, null);
        } else if (refChildImpl.parent == this) {
            return insertChildAt(newChild, refChildImpl.index);
        } else {
            throw new DOMException(3, null);
        }
    }

    /* access modifiers changed from: package-private */
    public Node insertChildAt(Node newChild, int index) throws DOMException {
        if (newChild instanceof DocumentFragment) {
            NodeList toAdd = newChild.getChildNodes();
            for (int i = 0; i < toAdd.getLength(); i++) {
                insertChildAt(toAdd.item(i), index + i);
            }
            return newChild;
        }
        LeafNodeImpl toInsert = (LeafNodeImpl) newChild;
        if (toInsert.document != null && this.document != null && toInsert.document != this.document) {
            throw new DOMException(4, null);
        } else if (!toInsert.isParentOf(this)) {
            if (toInsert.parent != null) {
                int oldIndex = toInsert.index;
                toInsert.parent.children.remove(oldIndex);
                toInsert.parent.refreshIndices(oldIndex);
            }
            this.children.add(index, toInsert);
            toInsert.parent = this;
            refreshIndices(index);
            return newChild;
        } else {
            throw new DOMException(3, null);
        }
    }

    public boolean isParentOf(Node node) {
        for (LeafNodeImpl nodeImpl = (LeafNodeImpl) node; nodeImpl != null; nodeImpl = nodeImpl.parent) {
            if (nodeImpl == this) {
                return true;
            }
        }
        return false;
    }

    public final void normalize() {
        Node node = getFirstChild();
        while (node != null) {
            Node next = node.getNextSibling();
            node.normalize();
            if (node.getNodeType() == 3) {
                ((TextImpl) node).minimize();
            }
            node = next;
        }
    }

    private void refreshIndices(int fromIndex) {
        for (int i = fromIndex; i < this.children.size(); i++) {
            this.children.get(i).index = i;
        }
    }

    public Node removeChild(Node oldChild) throws DOMException {
        LeafNodeImpl oldChildImpl = (LeafNodeImpl) oldChild;
        if (oldChildImpl.document != this.document) {
            throw new DOMException(4, null);
        } else if (oldChildImpl.parent == this) {
            int index = oldChildImpl.index;
            this.children.remove(index);
            oldChildImpl.parent = null;
            refreshIndices(index);
            return oldChild;
        } else {
            throw new DOMException(3, null);
        }
    }

    public Node replaceChild(Node newChild, Node oldChild) throws DOMException {
        int index = ((LeafNodeImpl) oldChild).index;
        removeChild(oldChild);
        insertChildAt(newChild, index);
        return oldChild;
    }

    public String getTextContent() throws DOMException {
        Node child = getFirstChild();
        if (child == null) {
            return "";
        }
        if (child.getNextSibling() == null) {
            return hasTextContent(child) ? child.getTextContent() : "";
        }
        StringBuilder buf = new StringBuilder();
        getTextContent(buf);
        return buf.toString();
    }

    /* access modifiers changed from: package-private */
    public void getTextContent(StringBuilder buf) throws DOMException {
        for (Node child = getFirstChild(); child != null; child = child.getNextSibling()) {
            if (hasTextContent(child)) {
                ((NodeImpl) child).getTextContent(buf);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public final boolean hasTextContent(Node child) {
        return (child.getNodeType() == 8 || child.getNodeType() == 7) ? false : true;
    }

    /* access modifiers changed from: package-private */
    public void getElementsByTagName(NodeListImpl out, String name) {
        for (NodeImpl node : this.children) {
            if (node.getNodeType() == 1) {
                ElementImpl element = (ElementImpl) node;
                if (matchesNameOrWildcard(name, element.getNodeName())) {
                    out.add(element);
                }
                element.getElementsByTagName(out, name);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void getElementsByTagNameNS(NodeListImpl out, String namespaceURI, String localName) {
        for (NodeImpl node : this.children) {
            if (node.getNodeType() == 1) {
                ElementImpl element = (ElementImpl) node;
                if (matchesNameOrWildcard(namespaceURI, element.getNamespaceURI()) && matchesNameOrWildcard(localName, element.getLocalName())) {
                    out.add(element);
                }
                element.getElementsByTagNameNS(out, namespaceURI, localName);
            }
        }
    }

    private static boolean matchesNameOrWildcard(String pattern, String s) {
        return "*".equals(pattern) || Objects.equals(pattern, s);
    }
}

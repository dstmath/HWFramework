package org.apache.harmony.xml.dom;

import org.w3c.dom.Node;

public abstract class LeafNodeImpl extends NodeImpl {
    int index;
    InnerNodeImpl parent;

    LeafNodeImpl(DocumentImpl document) {
        super(document);
    }

    public Node getNextSibling() {
        if (this.parent == null || this.index + 1 >= this.parent.children.size()) {
            return null;
        }
        return (Node) this.parent.children.get(this.index + 1);
    }

    public Node getParentNode() {
        return this.parent;
    }

    public Node getPreviousSibling() {
        if (this.parent == null || this.index == 0) {
            return null;
        }
        return (Node) this.parent.children.get(this.index - 1);
    }

    boolean isParentOf(Node node) {
        return false;
    }
}

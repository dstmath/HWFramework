package ohos.com.sun.org.apache.xerces.internal.dom;

import ohos.org.w3c.dom.Node;

public abstract class ChildNode extends NodeImpl {
    static final long serialVersionUID = -6112455738802414002L;
    transient StringBuffer fBufferStr = null;
    protected ChildNode nextSibling;
    protected ChildNode previousSibling;

    protected ChildNode(CoreDocumentImpl coreDocumentImpl) {
        super(coreDocumentImpl);
    }

    public ChildNode() {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public Node cloneNode(boolean z) {
        ChildNode cloneNode = super.cloneNode(z);
        cloneNode.previousSibling = null;
        cloneNode.nextSibling = null;
        cloneNode.isFirstChild(false);
        return cloneNode;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public Node getParentNode() {
        if (isOwned()) {
            return this.ownerNode;
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public final NodeImpl parentNode() {
        if (isOwned()) {
            return this.ownerNode;
        }
        return null;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public Node getNextSibling() {
        return this.nextSibling;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public Node getPreviousSibling() {
        if (isFirstChild()) {
            return null;
        }
        return this.previousSibling;
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public final ChildNode previousSibling() {
        if (isFirstChild()) {
            return null;
        }
        return this.previousSibling;
    }
}

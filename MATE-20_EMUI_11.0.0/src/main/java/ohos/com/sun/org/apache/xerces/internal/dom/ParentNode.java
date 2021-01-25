package ohos.com.sun.org.apache.xerces.internal.dom;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import ohos.org.w3c.dom.DOMException;
import ohos.org.w3c.dom.Document;
import ohos.org.w3c.dom.Node;
import ohos.org.w3c.dom.NodeList;
import ohos.org.w3c.dom.UserDataHandler;

public abstract class ParentNode extends ChildNode {
    static final long serialVersionUID = 2815829867152120872L;
    protected transient NodeListCache fNodeListCache = null;
    protected ChildNode firstChild = null;
    protected CoreDocumentImpl ownerDocument;

    protected ParentNode(CoreDocumentImpl coreDocumentImpl) {
        super(coreDocumentImpl);
        this.ownerDocument = coreDocumentImpl;
    }

    public ParentNode() {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.ChildNode, ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public Node cloneNode(boolean z) {
        if (needsSyncChildren()) {
            synchronizeChildren();
        }
        ParentNode cloneNode = super.cloneNode(z);
        cloneNode.ownerDocument = this.ownerDocument;
        cloneNode.firstChild = null;
        cloneNode.fNodeListCache = null;
        if (z) {
            for (ChildNode childNode = this.firstChild; childNode != null; childNode = childNode.nextSibling) {
                cloneNode.appendChild(childNode.cloneNode(true));
            }
        }
        return cloneNode;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public Document getOwnerDocument() {
        return this.ownerDocument;
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public CoreDocumentImpl ownerDocument() {
        return this.ownerDocument;
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public void setOwnerDocument(CoreDocumentImpl coreDocumentImpl) {
        if (needsSyncChildren()) {
            synchronizeChildren();
        }
        for (ChildNode childNode = this.firstChild; childNode != null; childNode = childNode.nextSibling) {
            childNode.setOwnerDocument(coreDocumentImpl);
        }
        super.setOwnerDocument(coreDocumentImpl);
        this.ownerDocument = coreDocumentImpl;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public boolean hasChildNodes() {
        if (needsSyncChildren()) {
            synchronizeChildren();
        }
        return this.firstChild != null;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public NodeList getChildNodes() {
        if (needsSyncChildren()) {
            synchronizeChildren();
        }
        return this;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public Node getFirstChild() {
        if (needsSyncChildren()) {
            synchronizeChildren();
        }
        return this.firstChild;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public Node getLastChild() {
        if (needsSyncChildren()) {
            synchronizeChildren();
        }
        return lastChild();
    }

    /* access modifiers changed from: package-private */
    public final ChildNode lastChild() {
        ChildNode childNode = this.firstChild;
        if (childNode != null) {
            return childNode.previousSibling;
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public final void lastChild(ChildNode childNode) {
        ChildNode childNode2 = this.firstChild;
        if (childNode2 != null) {
            childNode2.previousSibling = childNode;
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public Node insertBefore(Node node, Node node2) throws DOMException {
        return internalInsertBefore(node, node2, false);
    }

    /* access modifiers changed from: package-private */
    public Node internalInsertBefore(Node node, Node node2, boolean z) throws DOMException {
        boolean z2 = this.ownerDocument.errorChecking;
        if (node.getNodeType() == 11) {
            if (z2) {
                for (Node firstChild2 = node.getFirstChild(); firstChild2 != null; firstChild2 = firstChild2.getNextSibling()) {
                    if (!this.ownerDocument.isKidOK(this, firstChild2)) {
                        throw new DOMException(3, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "HIERARCHY_REQUEST_ERR", null));
                    }
                }
            }
            while (node.hasChildNodes()) {
                insertBefore(node.getFirstChild(), node2);
            }
            return node;
        } else if (node == node2) {
            Node nextSibling = node2.getNextSibling();
            removeChild(node);
            insertBefore(node, nextSibling);
            return node;
        } else {
            if (needsSyncChildren()) {
                synchronizeChildren();
            }
            if (z2) {
                if (!isReadOnly()) {
                    CoreDocumentImpl ownerDocument2 = node.getOwnerDocument();
                    CoreDocumentImpl coreDocumentImpl = this.ownerDocument;
                    if (ownerDocument2 != coreDocumentImpl && node != coreDocumentImpl) {
                        throw new DOMException(4, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "WRONG_DOCUMENT_ERR", null));
                    } else if (!this.ownerDocument.isKidOK(this, node)) {
                        throw new DOMException(3, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "HIERARCHY_REQUEST_ERR", null));
                    } else if (node2 != null && node2.getParentNode() != this) {
                        throw new DOMException(8, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NOT_FOUND_ERR", null));
                    } else if (this.ownerDocument.ancestorChecking) {
                        NodeImpl nodeImpl = this;
                        boolean z3 = true;
                        while (z3 && nodeImpl != null) {
                            z3 = node != nodeImpl;
                            nodeImpl = nodeImpl.parentNode();
                        }
                        if (!z3) {
                            throw new DOMException(3, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "HIERARCHY_REQUEST_ERR", null));
                        }
                    }
                } else {
                    throw new DOMException(7, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NO_MODIFICATION_ALLOWED_ERR", null));
                }
            }
            this.ownerDocument.insertingNode(this, z);
            ChildNode childNode = (ChildNode) node;
            NodeImpl parentNode = childNode.parentNode();
            if (parentNode != null) {
                parentNode.removeChild(childNode);
            }
            ChildNode childNode2 = (ChildNode) node2;
            childNode.ownerNode = this;
            childNode.isOwned(true);
            ChildNode childNode3 = this.firstChild;
            if (childNode3 == null) {
                this.firstChild = childNode;
                childNode.isFirstChild(true);
                childNode.previousSibling = childNode;
            } else if (childNode2 == null) {
                ChildNode childNode4 = childNode3.previousSibling;
                childNode4.nextSibling = childNode;
                childNode.previousSibling = childNode4;
                this.firstChild.previousSibling = childNode;
            } else if (node2 == childNode3) {
                childNode3.isFirstChild(false);
                ChildNode childNode5 = this.firstChild;
                childNode.nextSibling = childNode5;
                childNode.previousSibling = childNode5.previousSibling;
                this.firstChild.previousSibling = childNode;
                this.firstChild = childNode;
                childNode.isFirstChild(true);
            } else {
                ChildNode childNode6 = childNode2.previousSibling;
                childNode.nextSibling = childNode2;
                childNode6.nextSibling = childNode;
                childNode2.previousSibling = childNode;
                childNode.previousSibling = childNode6;
            }
            changed();
            NodeListCache nodeListCache = this.fNodeListCache;
            if (nodeListCache != null) {
                if (nodeListCache.fLength != -1) {
                    this.fNodeListCache.fLength++;
                }
                if (this.fNodeListCache.fChildIndex != -1) {
                    if (this.fNodeListCache.fChild == childNode2) {
                        this.fNodeListCache.fChild = childNode;
                    } else {
                        this.fNodeListCache.fChildIndex = -1;
                    }
                }
            }
            this.ownerDocument.insertedNode(this, childNode, z);
            checkNormalizationAfterInsert(childNode);
            return node;
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public Node removeChild(Node node) throws DOMException {
        return internalRemoveChild(node, false);
    }

    /* access modifiers changed from: package-private */
    public Node internalRemoveChild(Node node, boolean z) throws DOMException {
        CoreDocumentImpl ownerDocument2 = ownerDocument();
        if (ownerDocument2.errorChecking) {
            if (isReadOnly()) {
                throw new DOMException(7, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NO_MODIFICATION_ALLOWED_ERR", null));
            } else if (!(node == null || node.getParentNode() == this)) {
                throw new DOMException(8, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NOT_FOUND_ERR", null));
            }
        }
        ChildNode childNode = (ChildNode) node;
        ownerDocument2.removingNode(this, childNode, z);
        NodeListCache nodeListCache = this.fNodeListCache;
        if (nodeListCache != null) {
            if (nodeListCache.fLength != -1) {
                this.fNodeListCache.fLength--;
            }
            if (this.fNodeListCache.fChildIndex != -1) {
                if (this.fNodeListCache.fChild == childNode) {
                    this.fNodeListCache.fChildIndex--;
                    this.fNodeListCache.fChild = childNode.previousSibling();
                } else {
                    this.fNodeListCache.fChildIndex = -1;
                }
            }
        }
        if (childNode == this.firstChild) {
            childNode.isFirstChild(false);
            this.firstChild = childNode.nextSibling;
            ChildNode childNode2 = this.firstChild;
            if (childNode2 != null) {
                childNode2.isFirstChild(true);
                this.firstChild.previousSibling = childNode.previousSibling;
            }
        } else {
            ChildNode childNode3 = childNode.previousSibling;
            ChildNode childNode4 = childNode.nextSibling;
            childNode3.nextSibling = childNode4;
            if (childNode4 == null) {
                this.firstChild.previousSibling = childNode3;
            } else {
                childNode4.previousSibling = childNode3;
            }
        }
        ChildNode previousSibling = childNode.previousSibling();
        childNode.ownerNode = ownerDocument2;
        childNode.isOwned(false);
        childNode.nextSibling = null;
        childNode.previousSibling = null;
        changed();
        ownerDocument2.removedNode(this, z);
        checkNormalizationAfterRemove(previousSibling);
        return childNode;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public Node replaceChild(Node node, Node node2) throws DOMException {
        this.ownerDocument.replacingNode(this);
        internalInsertBefore(node, node2, true);
        if (node != node2) {
            internalRemoveChild(node2, true);
        }
        this.ownerDocument.replacedNode(this);
        return node2;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public String getTextContent() throws DOMException {
        NodeImpl firstChild2 = getFirstChild();
        if (firstChild2 == null) {
            return "";
        }
        if (firstChild2.getNextSibling() != null) {
            if (this.fBufferStr == null) {
                this.fBufferStr = new StringBuffer();
            } else {
                this.fBufferStr.setLength(0);
            }
            getTextContent(this.fBufferStr);
            return this.fBufferStr.toString();
        } else if (hasTextContent(firstChild2)) {
            return firstChild2.getTextContent();
        } else {
            return "";
        }
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public void getTextContent(StringBuffer stringBuffer) throws DOMException {
        for (NodeImpl firstChild2 = getFirstChild(); firstChild2 != null; firstChild2 = firstChild2.getNextSibling()) {
            if (hasTextContent(firstChild2)) {
                firstChild2.getTextContent(stringBuffer);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public final boolean hasTextContent(Node node) {
        return (node.getNodeType() == 8 || node.getNodeType() == 7 || (node.getNodeType() == 3 && ((TextImpl) node).isIgnorableWhitespace())) ? false : true;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public void setTextContent(String str) throws DOMException {
        while (true) {
            Node firstChild2 = getFirstChild();
            if (firstChild2 == null) {
                break;
            }
            removeChild(firstChild2);
        }
        if (str != null && str.length() != 0) {
            appendChild(ownerDocument().createTextNode(str));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int nodeListGetLength() {
        ChildNode childNode;
        int i = 0;
        if (this.fNodeListCache == null) {
            ChildNode childNode2 = this.firstChild;
            if (childNode2 == null) {
                return 0;
            }
            if (childNode2 == lastChild()) {
                return 1;
            }
            this.fNodeListCache = this.ownerDocument.getNodeListCache(this);
        }
        if (this.fNodeListCache.fLength == -1) {
            if (this.fNodeListCache.fChildIndex == -1 || this.fNodeListCache.fChild == null) {
                childNode = this.firstChild;
            } else {
                i = this.fNodeListCache.fChildIndex;
                childNode = this.fNodeListCache.fChild;
            }
            while (childNode != null) {
                i++;
                childNode = childNode.nextSibling;
            }
            this.fNodeListCache.fLength = i;
        }
        return this.fNodeListCache.fLength;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public int getLength() {
        return nodeListGetLength();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private Node nodeListItem(int i) {
        if (this.fNodeListCache == null) {
            if (this.firstChild != lastChild()) {
                this.fNodeListCache = this.ownerDocument.getNodeListCache(this);
            } else if (i == 0) {
                return this.firstChild;
            } else {
                return null;
            }
        }
        int i2 = this.fNodeListCache.fChildIndex;
        ChildNode childNode = this.fNodeListCache.fChild;
        boolean z = false;
        if (i2 == -1 || childNode == null) {
            if (i < 0) {
                return null;
            }
            childNode = this.firstChild;
            i2 = 0;
            while (i2 < i && childNode != null) {
                childNode = childNode.nextSibling;
                i2++;
            }
            z = true;
        } else if (i2 < i) {
            while (i2 < i && childNode != null) {
                i2++;
                childNode = childNode.nextSibling;
            }
        } else if (i2 > i) {
            while (i2 > i && childNode != null) {
                i2--;
                childNode = childNode.previousSibling();
            }
        }
        if (z || !(childNode == this.firstChild || childNode == lastChild())) {
            NodeListCache nodeListCache = this.fNodeListCache;
            nodeListCache.fChildIndex = i2;
            nodeListCache.fChild = childNode;
        } else {
            NodeListCache nodeListCache2 = this.fNodeListCache;
            nodeListCache2.fChildIndex = -1;
            nodeListCache2.fChild = null;
            this.ownerDocument.freeNodeListCache(nodeListCache2);
        }
        return childNode;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public Node item(int i) {
        return nodeListItem(i);
    }

    /* access modifiers changed from: protected */
    public final NodeList getChildNodesUnoptimized() {
        if (needsSyncChildren()) {
            synchronizeChildren();
        }
        return new NodeList() {
            /* class ohos.com.sun.org.apache.xerces.internal.dom.ParentNode.AnonymousClass1 */

            public int getLength() {
                return ParentNode.this.nodeListGetLength();
            }

            public Node item(int i) {
                return ParentNode.this.nodeListItem(i);
            }
        };
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public void normalize() {
        if (!isNormalized()) {
            if (needsSyncChildren()) {
                synchronizeChildren();
            }
            for (ChildNode childNode = this.firstChild; childNode != null; childNode = childNode.nextSibling) {
                childNode.normalize();
            }
            isNormalized(true);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public boolean isEqualNode(Node node) {
        if (!super.isEqualNode(node)) {
            return false;
        }
        Node firstChild2 = getFirstChild();
        Node firstChild3 = node.getFirstChild();
        while (firstChild2 != null && firstChild3 != null) {
            if (!((NodeImpl) firstChild2).isEqualNode(firstChild3)) {
                return false;
            }
            firstChild2 = firstChild2.getNextSibling();
            firstChild3 = firstChild3.getNextSibling();
        }
        if (firstChild2 != firstChild3) {
            return false;
        }
        return true;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public void setReadOnly(boolean z, boolean z2) {
        super.setReadOnly(z, z2);
        if (z2) {
            if (needsSyncChildren()) {
                synchronizeChildren();
            }
            for (ChildNode childNode = this.firstChild; childNode != null; childNode = childNode.nextSibling) {
                if (childNode.getNodeType() != 5) {
                    childNode.setReadOnly(z, true);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void synchronizeChildren() {
        needsSyncChildren(false);
    }

    /* access modifiers changed from: package-private */
    public void checkNormalizationAfterInsert(ChildNode childNode) {
        if (childNode.getNodeType() == 3) {
            ChildNode previousSibling = childNode.previousSibling();
            ChildNode childNode2 = childNode.nextSibling;
            if ((previousSibling != null && previousSibling.getNodeType() == 3) || (childNode2 != null && childNode2.getNodeType() == 3)) {
                isNormalized(false);
            }
        } else if (!childNode.isNormalized()) {
            isNormalized(false);
        }
    }

    /* access modifiers changed from: package-private */
    public void checkNormalizationAfterRemove(ChildNode childNode) {
        ChildNode childNode2;
        if (childNode != null && childNode.getNodeType() == 3 && (childNode2 = childNode.nextSibling) != null && childNode2.getNodeType() == 3) {
            isNormalized(false);
        }
    }

    private void writeObject(ObjectOutputStream objectOutputStream) throws IOException {
        if (needsSyncChildren()) {
            synchronizeChildren();
        }
        objectOutputStream.defaultWriteObject();
    }

    private void readObject(ObjectInputStream objectInputStream) throws ClassNotFoundException, IOException {
        objectInputStream.defaultReadObject();
        needsSyncChildren(false);
    }

    /* access modifiers changed from: protected */
    public class UserDataRecord implements Serializable {
        private static final long serialVersionUID = 3258126977134310455L;
        Object fData;
        UserDataHandler fHandler;

        UserDataRecord(Object obj, UserDataHandler userDataHandler) {
            this.fData = obj;
            this.fHandler = userDataHandler;
        }
    }
}

package ohos.com.sun.org.apache.xerces.internal.dom;

import java.util.ArrayList;
import java.util.List;
import ohos.org.w3c.dom.DOMException;
import ohos.org.w3c.dom.Node;

public class AttributeMap extends NamedNodeMapImpl {
    static final long serialVersionUID = 8872606282138665383L;

    protected AttributeMap(ElementImpl elementImpl, NamedNodeMapImpl namedNodeMapImpl) {
        super(elementImpl);
        if (namedNodeMapImpl != null) {
            cloneContent(namedNodeMapImpl);
            if (this.nodes != null) {
                hasDefaults(true);
            }
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.NamedNodeMapImpl
    public Node setNamedItem(Node node) throws DOMException {
        boolean z = this.ownerNode.ownerDocument().errorChecking;
        AttrImpl attrImpl = null;
        if (z) {
            if (isReadOnly()) {
                throw new DOMException(7, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NO_MODIFICATION_ALLOWED_ERR", null));
            } else if (node.getOwnerDocument() != this.ownerNode.ownerDocument()) {
                throw new DOMException(4, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "WRONG_DOCUMENT_ERR", null));
            } else if (node.getNodeType() != 2) {
                throw new DOMException(3, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "HIERARCHY_REQUEST_ERR", null));
            }
        }
        AttrImpl attrImpl2 = (AttrImpl) node;
        if (!attrImpl2.isOwned()) {
            attrImpl2.ownerNode = this.ownerNode;
            attrImpl2.isOwned(true);
            int findNamePoint = findNamePoint(attrImpl2.getNodeName(), 0);
            if (findNamePoint >= 0) {
                attrImpl = (AttrImpl) this.nodes.get(findNamePoint);
                this.nodes.set(findNamePoint, node);
                attrImpl.ownerNode = this.ownerNode.ownerDocument();
                attrImpl.isOwned(false);
                attrImpl.isSpecified(true);
            } else {
                int i = -1 - findNamePoint;
                if (this.nodes == null) {
                    this.nodes = new ArrayList(5);
                }
                this.nodes.add(i, node);
            }
            this.ownerNode.ownerDocument().setAttrNode(attrImpl2, attrImpl);
            if (!attrImpl2.isNormalized()) {
                this.ownerNode.isNormalized(false);
            }
            return attrImpl;
        } else if (!z || attrImpl2.getOwnerElement() == this.ownerNode) {
            return node;
        } else {
            throw new DOMException(10, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "INUSE_ATTRIBUTE_ERR", null));
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.NamedNodeMapImpl
    public Node setNamedItemNS(Node node) throws DOMException {
        boolean z = this.ownerNode.ownerDocument().errorChecking;
        AttrImpl attrImpl = null;
        if (z) {
            if (isReadOnly()) {
                throw new DOMException(7, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NO_MODIFICATION_ALLOWED_ERR", null));
            } else if (node.getOwnerDocument() != this.ownerNode.ownerDocument()) {
                throw new DOMException(4, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "WRONG_DOCUMENT_ERR", null));
            } else if (node.getNodeType() != 2) {
                throw new DOMException(3, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "HIERARCHY_REQUEST_ERR", null));
            }
        }
        AttrImpl attrImpl2 = (AttrImpl) node;
        if (!attrImpl2.isOwned()) {
            attrImpl2.ownerNode = this.ownerNode;
            attrImpl2.isOwned(true);
            int findNamePoint = findNamePoint(attrImpl2.getNamespaceURI(), attrImpl2.getLocalName());
            if (findNamePoint >= 0) {
                attrImpl = (AttrImpl) this.nodes.get(findNamePoint);
                this.nodes.set(findNamePoint, node);
                attrImpl.ownerNode = this.ownerNode.ownerDocument();
                attrImpl.isOwned(false);
                attrImpl.isSpecified(true);
            } else {
                int findNamePoint2 = findNamePoint(node.getNodeName(), 0);
                if (findNamePoint2 >= 0) {
                    attrImpl = (AttrImpl) this.nodes.get(findNamePoint2);
                    this.nodes.add(findNamePoint2, node);
                } else {
                    int i = -1 - findNamePoint2;
                    if (this.nodes == null) {
                        this.nodes = new ArrayList(5);
                    }
                    this.nodes.add(i, node);
                }
            }
            this.ownerNode.ownerDocument().setAttrNode(attrImpl2, attrImpl);
            if (!attrImpl2.isNormalized()) {
                this.ownerNode.isNormalized(false);
            }
            return attrImpl;
        } else if (!z || attrImpl2.getOwnerElement() == this.ownerNode) {
            return node;
        } else {
            throw new DOMException(10, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "INUSE_ATTRIBUTE_ERR", null));
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.NamedNodeMapImpl
    public Node removeNamedItem(String str) throws DOMException {
        return internalRemoveNamedItem(str, true);
    }

    /* access modifiers changed from: package-private */
    public Node safeRemoveNamedItem(String str) {
        return internalRemoveNamedItem(str, false);
    }

    /* access modifiers changed from: protected */
    public Node removeItem(Node node, boolean z) throws DOMException {
        int i;
        if (this.nodes != null) {
            int size = this.nodes.size();
            i = 0;
            while (true) {
                if (i >= size) {
                    break;
                } else if (this.nodes.get(i) == node) {
                    break;
                } else {
                    i++;
                }
            }
        }
        i = -1;
        if (i >= 0) {
            return remove((AttrImpl) node, i, z);
        }
        throw new DOMException(8, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NOT_FOUND_ERR", null));
    }

    /* access modifiers changed from: protected */
    public final Node internalRemoveNamedItem(String str, boolean z) {
        if (!isReadOnly()) {
            int findNamePoint = findNamePoint(str, 0);
            if (findNamePoint >= 0) {
                return remove((AttrImpl) this.nodes.get(findNamePoint), findNamePoint, true);
            }
            if (!z) {
                return null;
            }
            throw new DOMException(8, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NOT_FOUND_ERR", null));
        }
        throw new DOMException(7, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NO_MODIFICATION_ALLOWED_ERR", null));
    }

    private final Node remove(AttrImpl attrImpl, int i, boolean z) {
        Node namedItem;
        CoreDocumentImpl ownerDocument = this.ownerNode.ownerDocument();
        String nodeName = attrImpl.getNodeName();
        if (attrImpl.isIdAttribute()) {
            ownerDocument.removeIdentifier(attrImpl.getValue());
        }
        if (!hasDefaults() || !z) {
            this.nodes.remove(i);
        } else {
            NamedNodeMapImpl defaultAttributes = ((ElementImpl) this.ownerNode).getDefaultAttributes();
            if (defaultAttributes == null || (namedItem = defaultAttributes.getNamedItem(nodeName)) == null || findNamePoint(nodeName, i + 1) >= 0) {
                this.nodes.remove(i);
            } else {
                NodeImpl cloneNode = namedItem.cloneNode(true);
                if (namedItem.getLocalName() != null) {
                    ((AttrNSImpl) cloneNode).namespaceURI = attrImpl.getNamespaceURI();
                }
                cloneNode.ownerNode = this.ownerNode;
                cloneNode.isOwned(true);
                cloneNode.isSpecified(false);
                this.nodes.set(i, cloneNode);
                if (attrImpl.isIdAttribute()) {
                    ownerDocument.putIdentifier(cloneNode.getNodeValue(), (ElementImpl) this.ownerNode);
                }
            }
        }
        attrImpl.ownerNode = ownerDocument;
        attrImpl.isOwned(false);
        attrImpl.isSpecified(true);
        attrImpl.isIdAttribute(false);
        ownerDocument.removedAttrNode(attrImpl, this.ownerNode, nodeName);
        return attrImpl;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.NamedNodeMapImpl
    public Node removeNamedItemNS(String str, String str2) throws DOMException {
        return internalRemoveNamedItemNS(str, str2, true);
    }

    /* access modifiers changed from: package-private */
    public Node safeRemoveNamedItemNS(String str, String str2) {
        return internalRemoveNamedItemNS(str, str2, false);
    }

    /* access modifiers changed from: protected */
    public final Node internalRemoveNamedItemNS(String str, String str2, boolean z) {
        Node namedItem;
        CoreDocumentImpl ownerDocument = this.ownerNode.ownerDocument();
        if (!ownerDocument.errorChecking || !isReadOnly()) {
            int findNamePoint = findNamePoint(str, str2);
            if (findNamePoint >= 0) {
                AttrImpl attrImpl = (AttrImpl) this.nodes.get(findNamePoint);
                if (attrImpl.isIdAttribute()) {
                    ownerDocument.removeIdentifier(attrImpl.getValue());
                }
                String nodeName = attrImpl.getNodeName();
                if (hasDefaults()) {
                    NamedNodeMapImpl defaultAttributes = ((ElementImpl) this.ownerNode).getDefaultAttributes();
                    if (defaultAttributes == null || (namedItem = defaultAttributes.getNamedItem(nodeName)) == null) {
                        this.nodes.remove(findNamePoint);
                    } else {
                        int findNamePoint2 = findNamePoint(nodeName, 0);
                        if (findNamePoint2 < 0 || findNamePoint(nodeName, findNamePoint2 + 1) >= 0) {
                            this.nodes.remove(findNamePoint);
                        } else {
                            NodeImpl cloneNode = namedItem.cloneNode(true);
                            cloneNode.ownerNode = this.ownerNode;
                            if (namedItem.getLocalName() != null) {
                                ((AttrNSImpl) cloneNode).namespaceURI = str;
                            }
                            cloneNode.isOwned(true);
                            cloneNode.isSpecified(false);
                            this.nodes.set(findNamePoint, cloneNode);
                            if (cloneNode.isIdAttribute()) {
                                ownerDocument.putIdentifier(cloneNode.getNodeValue(), (ElementImpl) this.ownerNode);
                            }
                        }
                    }
                } else {
                    this.nodes.remove(findNamePoint);
                }
                attrImpl.ownerNode = ownerDocument;
                attrImpl.isOwned(false);
                attrImpl.isSpecified(true);
                attrImpl.isIdAttribute(false);
                ownerDocument.removedAttrNode(attrImpl, this.ownerNode, str2);
                return attrImpl;
            } else if (!z) {
                return null;
            } else {
                throw new DOMException(8, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NOT_FOUND_ERR", null));
            }
        } else {
            throw new DOMException(7, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NO_MODIFICATION_ALLOWED_ERR", null));
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.NamedNodeMapImpl
    public NamedNodeMapImpl cloneMap(NodeImpl nodeImpl) {
        AttributeMap attributeMap = new AttributeMap((ElementImpl) nodeImpl, null);
        attributeMap.hasDefaults(hasDefaults());
        attributeMap.cloneContent(this);
        return attributeMap;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.dom.NamedNodeMapImpl
    public void cloneContent(NamedNodeMapImpl namedNodeMapImpl) {
        int size;
        List list = namedNodeMapImpl.nodes;
        if (!(list == null || (size = list.size()) == 0)) {
            if (this.nodes == null) {
                this.nodes = new ArrayList(size);
            } else {
                this.nodes.clear();
            }
            for (int i = 0; i < size; i++) {
                NodeImpl nodeImpl = (NodeImpl) list.get(i);
                NodeImpl cloneNode = nodeImpl.cloneNode(true);
                cloneNode.isSpecified(nodeImpl.isSpecified());
                this.nodes.add(cloneNode);
                cloneNode.ownerNode = this.ownerNode;
                cloneNode.isOwned(true);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void moveSpecifiedAttributes(AttributeMap attributeMap) {
        for (int size = (attributeMap.nodes != null ? attributeMap.nodes.size() : 0) - 1; size >= 0; size--) {
            AttrImpl attrImpl = (AttrImpl) attributeMap.nodes.get(size);
            if (attrImpl.isSpecified()) {
                attributeMap.remove(attrImpl, size, false);
                if (attrImpl.getLocalName() != null) {
                    setNamedItem(attrImpl);
                } else {
                    setNamedItemNS(attrImpl);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void reconcileDefaults(NamedNodeMapImpl namedNodeMapImpl) {
        for (int size = (this.nodes != null ? this.nodes.size() : 0) - 1; size >= 0; size--) {
            AttrImpl attrImpl = (AttrImpl) this.nodes.get(size);
            if (!attrImpl.isSpecified()) {
                remove(attrImpl, size, false);
            }
        }
        if (namedNodeMapImpl != null) {
            if (this.nodes == null || this.nodes.size() == 0) {
                cloneContent(namedNodeMapImpl);
                return;
            }
            int size2 = namedNodeMapImpl.nodes.size();
            for (int i = 0; i < size2; i++) {
                AttrImpl attrImpl2 = (AttrImpl) namedNodeMapImpl.nodes.get(i);
                int findNamePoint = findNamePoint(attrImpl2.getNodeName(), 0);
                if (findNamePoint < 0) {
                    NodeImpl cloneNode = attrImpl2.cloneNode(true);
                    cloneNode.ownerNode = this.ownerNode;
                    cloneNode.isOwned(true);
                    cloneNode.isSpecified(false);
                    this.nodes.add(-1 - findNamePoint, cloneNode);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.dom.NamedNodeMapImpl
    public final int addItem(Node node) {
        AttrImpl attrImpl = (AttrImpl) node;
        attrImpl.ownerNode = this.ownerNode;
        attrImpl.isOwned(true);
        int findNamePoint = findNamePoint(attrImpl.getNamespaceURI(), attrImpl.getLocalName());
        if (findNamePoint >= 0) {
            this.nodes.set(findNamePoint, node);
        } else {
            findNamePoint = findNamePoint(attrImpl.getNodeName(), 0);
            if (findNamePoint >= 0) {
                this.nodes.add(findNamePoint, node);
            } else {
                findNamePoint = -1 - findNamePoint;
                if (this.nodes == null) {
                    this.nodes = new ArrayList(5);
                }
                this.nodes.add(findNamePoint, node);
            }
        }
        this.ownerNode.ownerDocument().setAttrNode(attrImpl, null);
        return findNamePoint;
    }
}

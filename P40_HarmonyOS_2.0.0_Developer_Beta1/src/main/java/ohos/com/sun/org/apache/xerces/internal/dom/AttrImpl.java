package ohos.com.sun.org.apache.xerces.internal.dom;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import ohos.org.w3c.dom.Attr;
import ohos.org.w3c.dom.DOMException;
import ohos.org.w3c.dom.Document;
import ohos.org.w3c.dom.Element;
import ohos.org.w3c.dom.Node;
import ohos.org.w3c.dom.NodeList;
import ohos.org.w3c.dom.Text;
import ohos.org.w3c.dom.TypeInfo;

public class AttrImpl extends NodeImpl implements Attr, TypeInfo {
    static final String DTD_URI = "http://www.w3.org/TR/REC-xml";
    static final long serialVersionUID = 7277707688218972102L;
    protected String name;
    protected TextImpl textNode = null;
    transient Object type;
    protected Object value = null;

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public short getNodeType() {
        return 2;
    }

    public TypeInfo getSchemaTypeInfo() {
        return this;
    }

    public boolean isDerivedFrom(String str, String str2, int i) {
        return false;
    }

    protected AttrImpl(CoreDocumentImpl coreDocumentImpl, String str) {
        super(coreDocumentImpl);
        this.name = str;
        isSpecified(true);
        hasStringValue(true);
    }

    protected AttrImpl() {
    }

    /* access modifiers changed from: package-private */
    public void rename(String str) {
        if (needsSyncData()) {
            synchronizeData();
        }
        this.name = str;
    }

    /* access modifiers changed from: protected */
    public void makeChildNode() {
        if (hasStringValue()) {
            if (this.value != null) {
                TextImpl createTextNode = ownerDocument().createTextNode((String) this.value);
                this.value = createTextNode;
                createTextNode.isFirstChild(true);
                createTextNode.previousSibling = createTextNode;
                createTextNode.ownerNode = this;
                createTextNode.isOwned(true);
            }
            hasStringValue(false);
        }
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public void setOwnerDocument(CoreDocumentImpl coreDocumentImpl) {
        if (needsSyncChildren()) {
            synchronizeChildren();
        }
        super.setOwnerDocument(coreDocumentImpl);
        if (!hasStringValue()) {
            for (ChildNode childNode = (ChildNode) this.value; childNode != null; childNode = childNode.nextSibling) {
                childNode.setOwnerDocument(coreDocumentImpl);
            }
        }
    }

    public void setIdAttribute(boolean z) {
        if (needsSyncData()) {
            synchronizeData();
        }
        isIdAttribute(z);
    }

    public boolean isId() {
        return isIdAttribute();
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public Node cloneNode(boolean z) {
        if (needsSyncChildren()) {
            synchronizeChildren();
        }
        AttrImpl cloneNode = super.cloneNode(z);
        if (!cloneNode.hasStringValue()) {
            cloneNode.value = null;
            for (Node node = (Node) this.value; node != null; node = node.getNextSibling()) {
                cloneNode.appendChild(node.cloneNode(true));
            }
        }
        cloneNode.isSpecified(true);
        return cloneNode;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public String getNodeName() {
        if (needsSyncData()) {
            synchronizeData();
        }
        return this.name;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public void setNodeValue(String str) throws DOMException {
        setValue(str);
    }

    public String getTypeName() {
        return (String) this.type;
    }

    public String getTypeNamespace() {
        if (this.type != null) {
            return "http://www.w3.org/TR/REC-xml";
        }
        return null;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public String getNodeValue() {
        return getValue();
    }

    public String getName() {
        if (needsSyncData()) {
            synchronizeData();
        }
        return this.name;
    }

    public void setValue(String str) {
        String str2;
        String str3;
        CoreDocumentImpl ownerDocument = ownerDocument();
        if (!ownerDocument.errorChecking || !isReadOnly()) {
            Element ownerElement = getOwnerElement();
            if (needsSyncData()) {
                synchronizeData();
            }
            if (needsSyncChildren()) {
                synchronizeChildren();
            }
            if (this.value != null) {
                if (ownerDocument.getMutationEvents()) {
                    if (!hasStringValue()) {
                        str2 = getValue();
                        while (true) {
                            Object obj = this.value;
                            if (obj == null) {
                                break;
                            }
                            internalRemoveChild((Node) obj, true);
                        }
                    } else {
                        Object obj2 = this.value;
                        str2 = (String) obj2;
                        TextImpl textImpl = this.textNode;
                        if (textImpl == null) {
                            this.textNode = ownerDocument.createTextNode((String) obj2);
                        } else {
                            textImpl.data = (String) obj2;
                        }
                        TextImpl textImpl2 = this.textNode;
                        this.value = textImpl2;
                        textImpl2.isFirstChild(true);
                        TextImpl textImpl3 = this.textNode;
                        textImpl3.previousSibling = textImpl3;
                        textImpl3.ownerNode = this;
                        textImpl3.isOwned(true);
                        hasStringValue(false);
                        internalRemoveChild(this.textNode, true);
                    }
                } else {
                    if (hasStringValue()) {
                        str3 = (String) this.value;
                    } else {
                        str3 = getValue();
                        ChildNode childNode = (ChildNode) this.value;
                        childNode.previousSibling = null;
                        childNode.isFirstChild(false);
                        childNode.ownerNode = ownerDocument;
                    }
                    str2 = str3;
                    this.value = null;
                    needsSyncChildren(false);
                }
                if (isIdAttribute() && ownerElement != null) {
                    ownerDocument.removeIdentifier(str2);
                }
            } else {
                str2 = "";
            }
            isSpecified(true);
            if (ownerDocument.getMutationEvents()) {
                internalInsertBefore(ownerDocument.createTextNode(str), null, true);
                hasStringValue(false);
                ownerDocument.modifiedAttrValue(this, str2);
            } else {
                this.value = str;
                hasStringValue(true);
                changed();
            }
            if (isIdAttribute() && ownerElement != null) {
                ownerDocument.putIdentifier(str, ownerElement);
                return;
            }
            return;
        }
        throw new DOMException(7, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NO_MODIFICATION_ALLOWED_ERR", null));
    }

    public String getValue() {
        String str;
        if (needsSyncData()) {
            synchronizeData();
        }
        if (needsSyncChildren()) {
            synchronizeChildren();
        }
        if (this.value == null) {
            return "";
        }
        if (hasStringValue()) {
            return (String) this.value;
        }
        ChildNode childNode = (ChildNode) this.value;
        if (childNode.getNodeType() == 5) {
            str = ((EntityReferenceImpl) childNode).getEntityRefValue();
        } else {
            str = childNode.getNodeValue();
        }
        ChildNode childNode2 = childNode.nextSibling;
        if (childNode2 != null && str != null) {
            StringBuffer stringBuffer = new StringBuffer(str);
            while (childNode2 != null) {
                if (childNode2.getNodeType() == 5) {
                    String entityRefValue = ((EntityReferenceImpl) childNode2).getEntityRefValue();
                    if (entityRefValue == null) {
                        return "";
                    }
                    stringBuffer.append(entityRefValue);
                } else {
                    stringBuffer.append(childNode2.getNodeValue());
                }
                childNode2 = childNode2.nextSibling;
            }
            return stringBuffer.toString();
        } else if (str == null) {
            return "";
        } else {
            return str;
        }
    }

    public boolean getSpecified() {
        if (needsSyncData()) {
            synchronizeData();
        }
        return isSpecified();
    }

    public Element getElement() {
        return (Element) (isOwned() ? this.ownerNode : null);
    }

    public Element getOwnerElement() {
        return (Element) (isOwned() ? this.ownerNode : null);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public void normalize() {
        if (!isNormalized() && !hasStringValue()) {
            Text text = (ChildNode) this.value;
            while (text != null) {
                Text nextSibling = text.getNextSibling();
                if (text.getNodeType() == 3) {
                    if (nextSibling != null && nextSibling.getNodeType() == 3) {
                        text.appendData(nextSibling.getNodeValue());
                        removeChild(nextSibling);
                    } else if (text.getNodeValue() == null || text.getNodeValue().length() == 0) {
                        removeChild(text);
                    }
                }
                text = nextSibling;
            }
            isNormalized(true);
        }
    }

    public void setSpecified(boolean z) {
        if (needsSyncData()) {
            synchronizeData();
        }
        isSpecified(z);
    }

    public void setType(Object obj) {
        this.type = obj;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl, java.lang.Object
    public String toString() {
        return getName() + "=\"" + getValue() + "\"";
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public boolean hasChildNodes() {
        if (needsSyncChildren()) {
            synchronizeChildren();
        }
        return this.value != null;
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
        makeChildNode();
        return (Node) this.value;
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
        makeChildNode();
        Object obj = this.value;
        if (obj != null) {
            return ((ChildNode) obj).previousSibling;
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public final void lastChild(ChildNode childNode) {
        Object obj = this.value;
        if (obj != null) {
            ((ChildNode) obj).previousSibling = childNode;
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public Node insertBefore(Node node, Node node2) throws DOMException {
        return internalInsertBefore(node, node2, false);
    }

    /* access modifiers changed from: package-private */
    public Node internalInsertBefore(Node node, Node node2, boolean z) throws DOMException {
        Document ownerDocument = ownerDocument();
        boolean z2 = ownerDocument.errorChecking;
        if (node.getNodeType() == 11) {
            if (z2) {
                for (Node firstChild = node.getFirstChild(); firstChild != null; firstChild = firstChild.getNextSibling()) {
                    if (!ownerDocument.isKidOK(this, firstChild)) {
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
                if (isReadOnly()) {
                    throw new DOMException(7, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NO_MODIFICATION_ALLOWED_ERR", null));
                } else if (node.getOwnerDocument() != ownerDocument) {
                    throw new DOMException(4, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "WRONG_DOCUMENT_ERR", null));
                } else if (!ownerDocument.isKidOK(this, node)) {
                    throw new DOMException(3, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "HIERARCHY_REQUEST_ERR", null));
                } else if (node2 == null || node2.getParentNode() == this) {
                    NodeImpl nodeImpl = this;
                    boolean z3 = true;
                    while (z3 && nodeImpl != null) {
                        z3 = node != nodeImpl;
                        nodeImpl = nodeImpl.parentNode();
                    }
                    if (!z3) {
                        throw new DOMException(3, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "HIERARCHY_REQUEST_ERR", null));
                    }
                } else {
                    throw new DOMException(8, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NOT_FOUND_ERR", null));
                }
            }
            makeChildNode();
            ownerDocument.insertingNode(this, z);
            ChildNode childNode = (ChildNode) node;
            NodeImpl parentNode = childNode.parentNode();
            if (parentNode != null) {
                parentNode.removeChild(childNode);
            }
            ChildNode childNode2 = (ChildNode) node2;
            childNode.ownerNode = this;
            childNode.isOwned(true);
            ChildNode childNode3 = (ChildNode) this.value;
            if (childNode3 == null) {
                this.value = childNode;
                childNode.isFirstChild(true);
                childNode.previousSibling = childNode;
            } else if (childNode2 == null) {
                ChildNode childNode4 = childNode3.previousSibling;
                childNode4.nextSibling = childNode;
                childNode.previousSibling = childNode4;
                childNode3.previousSibling = childNode;
            } else if (node2 == childNode3) {
                childNode3.isFirstChild(false);
                childNode.nextSibling = childNode3;
                childNode.previousSibling = childNode3.previousSibling;
                childNode3.previousSibling = childNode;
                this.value = childNode;
                childNode.isFirstChild(true);
            } else {
                ChildNode childNode5 = childNode2.previousSibling;
                childNode.nextSibling = childNode2;
                childNode5.nextSibling = childNode;
                childNode2.previousSibling = childNode;
                childNode.previousSibling = childNode5;
            }
            changed();
            ownerDocument.insertedNode(this, childNode, z);
            checkNormalizationAfterInsert(childNode);
            return node;
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public Node removeChild(Node node) throws DOMException {
        if (!hasStringValue()) {
            return internalRemoveChild(node, false);
        }
        throw new DOMException(8, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NOT_FOUND_ERR", null));
    }

    /* access modifiers changed from: package-private */
    public Node internalRemoveChild(Node node, boolean z) throws DOMException {
        CoreDocumentImpl ownerDocument = ownerDocument();
        if (ownerDocument.errorChecking) {
            if (isReadOnly()) {
                throw new DOMException(7, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NO_MODIFICATION_ALLOWED_ERR", null));
            } else if (!(node == null || node.getParentNode() == this)) {
                throw new DOMException(8, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NOT_FOUND_ERR", null));
            }
        }
        ChildNode childNode = (ChildNode) node;
        ownerDocument.removingNode(this, childNode, z);
        if (childNode == this.value) {
            childNode.isFirstChild(false);
            this.value = childNode.nextSibling;
            ChildNode childNode2 = (ChildNode) this.value;
            if (childNode2 != null) {
                childNode2.isFirstChild(true);
                childNode2.previousSibling = childNode.previousSibling;
            }
        } else {
            ChildNode childNode3 = childNode.previousSibling;
            ChildNode childNode4 = childNode.nextSibling;
            childNode3.nextSibling = childNode4;
            if (childNode4 == null) {
                ((ChildNode) this.value).previousSibling = childNode3;
            } else {
                childNode4.previousSibling = childNode3;
            }
        }
        ChildNode previousSibling = childNode.previousSibling();
        childNode.ownerNode = ownerDocument;
        childNode.isOwned(false);
        childNode.nextSibling = null;
        childNode.previousSibling = null;
        changed();
        ownerDocument.removedNode(this, z);
        checkNormalizationAfterRemove(previousSibling);
        return childNode;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public Node replaceChild(Node node, Node node2) throws DOMException {
        makeChildNode();
        CoreDocumentImpl ownerDocument = ownerDocument();
        ownerDocument.replacingNode(this);
        internalInsertBefore(node, node2, true);
        if (node != node2) {
            internalRemoveChild(node2, true);
        }
        ownerDocument.replacedNode(this);
        return node2;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public int getLength() {
        if (hasStringValue()) {
            return 1;
        }
        int i = 0;
        for (ChildNode childNode = (ChildNode) this.value; childNode != null; childNode = childNode.nextSibling) {
            i++;
        }
        return i;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public Node item(int i) {
        if (hasStringValue()) {
            if (i != 0 || this.value == null) {
                return null;
            }
            makeChildNode();
            return (Node) this.value;
        } else if (i < 0) {
            return null;
        } else {
            ChildNode childNode = (ChildNode) this.value;
            for (int i2 = 0; i2 < i && childNode != null; i2++) {
                childNode = childNode.nextSibling;
            }
            return childNode;
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public boolean isEqualNode(Node node) {
        return super.isEqualNode(node);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.dom.NodeImpl
    public void setReadOnly(boolean z, boolean z2) {
        super.setReadOnly(z, z2);
        if (z2) {
            if (needsSyncChildren()) {
                synchronizeChildren();
            }
            if (!hasStringValue()) {
                for (ChildNode childNode = (ChildNode) this.value; childNode != null; childNode = childNode.nextSibling) {
                    if (childNode.getNodeType() != 5) {
                        childNode.setReadOnly(z, true);
                    }
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
}

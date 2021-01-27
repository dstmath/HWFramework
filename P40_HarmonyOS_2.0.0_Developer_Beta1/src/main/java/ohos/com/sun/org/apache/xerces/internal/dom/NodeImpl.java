package ohos.com.sun.org.apache.xerces.internal.dom;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;
import ohos.com.sun.org.apache.xerces.internal.dom.ParentNode;
import ohos.org.w3c.dom.DOMException;
import ohos.org.w3c.dom.Document;
import ohos.org.w3c.dom.DocumentType;
import ohos.org.w3c.dom.NamedNodeMap;
import ohos.org.w3c.dom.Node;
import ohos.org.w3c.dom.NodeList;
import ohos.org.w3c.dom.UserDataHandler;
import ohos.org.w3c.dom.events.Event;
import ohos.org.w3c.dom.events.EventListener;
import ohos.org.w3c.dom.events.EventTarget;

public abstract class NodeImpl implements Node, NodeList, EventTarget, Cloneable, Serializable {
    public static final short DOCUMENT_POSITION_CONTAINS = 8;
    public static final short DOCUMENT_POSITION_DISCONNECTED = 1;
    public static final short DOCUMENT_POSITION_FOLLOWING = 4;
    public static final short DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC = 32;
    public static final short DOCUMENT_POSITION_IS_CONTAINED = 16;
    public static final short DOCUMENT_POSITION_PRECEDING = 2;
    public static final short ELEMENT_DEFINITION_NODE = 21;
    protected static final short FIRSTCHILD = 16;
    protected static final short HASSTRING = 128;
    protected static final short ID = 512;
    protected static final short IGNORABLEWS = 64;
    protected static final short NORMALIZED = 256;
    protected static final short OWNED = 8;
    protected static final short READONLY = 1;
    protected static final short SPECIFIED = 32;
    protected static final short SYNCCHILDREN = 4;
    protected static final short SYNCDATA = 2;
    public static final short TREE_POSITION_ANCESTOR = 4;
    public static final short TREE_POSITION_DESCENDANT = 8;
    public static final short TREE_POSITION_DISCONNECTED = 0;
    public static final short TREE_POSITION_EQUIVALENT = 16;
    public static final short TREE_POSITION_FOLLOWING = 2;
    public static final short TREE_POSITION_PRECEDING = 1;
    public static final short TREE_POSITION_SAME_NODE = 32;
    static final long serialVersionUID = -6316591992167219696L;
    protected short flags;
    protected NodeImpl ownerNode;

    public NamedNodeMap getAttributes() {
        return null;
    }

    public String getBaseURI() {
        return null;
    }

    public NodeList getChildNodes() {
        return this;
    }

    /* access modifiers changed from: protected */
    public Node getContainer() {
        return null;
    }

    public Node getFirstChild() {
        return null;
    }

    public Node getLastChild() {
        return null;
    }

    public int getLength() {
        return 0;
    }

    public String getLocalName() {
        return null;
    }

    public String getNamespaceURI() {
        return null;
    }

    public Node getNextSibling() {
        return null;
    }

    public abstract String getNodeName();

    public abstract short getNodeType();

    public String getNodeValue() throws DOMException {
        return null;
    }

    public Node getParentNode() {
        return null;
    }

    public String getPrefix() {
        return null;
    }

    public Node getPreviousSibling() {
        return null;
    }

    public boolean hasAttributes() {
        return false;
    }

    public boolean hasChildNodes() {
        return false;
    }

    public boolean isSameNode(Node node) {
        return this == node;
    }

    public Node item(int i) {
        return null;
    }

    public void normalize() {
    }

    /* access modifiers changed from: package-private */
    public NodeImpl parentNode() {
        return null;
    }

    /* access modifiers changed from: package-private */
    public ChildNode previousSibling() {
        return null;
    }

    public void setNodeValue(String str) throws DOMException {
    }

    protected NodeImpl(CoreDocumentImpl coreDocumentImpl) {
        this.ownerNode = coreDocumentImpl;
    }

    public NodeImpl() {
    }

    public Node appendChild(Node node) throws DOMException {
        return insertBefore(node, null);
    }

    public Node cloneNode(boolean z) {
        if (needsSyncData()) {
            synchronizeData();
        }
        try {
            NodeImpl nodeImpl = (NodeImpl) clone();
            nodeImpl.ownerNode = ownerDocument();
            nodeImpl.isOwned(false);
            nodeImpl.isReadOnly(false);
            ownerDocument().callUserDataHandlers(this, nodeImpl, 1);
            return nodeImpl;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("**Internal Error**" + e);
        }
    }

    public Document getOwnerDocument() {
        if (isOwned()) {
            return this.ownerNode.ownerDocument();
        }
        return this.ownerNode;
    }

    /* access modifiers changed from: package-private */
    public CoreDocumentImpl ownerDocument() {
        if (isOwned()) {
            return this.ownerNode.ownerDocument();
        }
        return (CoreDocumentImpl) this.ownerNode;
    }

    /* access modifiers changed from: package-private */
    public void setOwnerDocument(CoreDocumentImpl coreDocumentImpl) {
        if (needsSyncData()) {
            synchronizeData();
        }
        if (!isOwned()) {
            this.ownerNode = coreDocumentImpl;
        }
    }

    /* access modifiers changed from: protected */
    public int getNodeNumber() {
        return getOwnerDocument().getNodeNumber(this);
    }

    public Node insertBefore(Node node, Node node2) throws DOMException {
        throw new DOMException(3, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "HIERARCHY_REQUEST_ERR", null));
    }

    public Node removeChild(Node node) throws DOMException {
        throw new DOMException(8, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NOT_FOUND_ERR", null));
    }

    public Node replaceChild(Node node, Node node2) throws DOMException {
        throw new DOMException(3, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "HIERARCHY_REQUEST_ERR", null));
    }

    public boolean isSupported(String str, String str2) {
        return ownerDocument().getImplementation().hasFeature(str, str2);
    }

    public void setPrefix(String str) throws DOMException {
        throw new DOMException(14, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NAMESPACE_ERR", null));
    }

    public void addEventListener(String str, EventListener eventListener, boolean z) {
        ownerDocument().addEventListener(this, str, eventListener, z);
    }

    public void removeEventListener(String str, EventListener eventListener, boolean z) {
        ownerDocument().removeEventListener(this, str, eventListener, z);
    }

    public boolean dispatchEvent(Event event) {
        return ownerDocument().dispatchEvent(this, event);
    }

    public short compareTreePosition(Node node) {
        if (this == node) {
            return 48;
        }
        short nodeType = getNodeType();
        short nodeType2 = node.getNodeType();
        if (!(nodeType == 6 || nodeType == 12 || nodeType2 == 6 || nodeType2 == 12)) {
            NodeImpl nodeImpl = this;
            NodeImpl nodeImpl2 = nodeImpl;
            int i = 0;
            while (nodeImpl != null) {
                i++;
                if (nodeImpl == node) {
                    return 5;
                }
                nodeImpl2 = nodeImpl;
                nodeImpl = nodeImpl.getParentNode();
            }
            Node node2 = node;
            Node node3 = node2;
            int i2 = 0;
            while (node2 != null) {
                i2++;
                if (node2 == this) {
                    return 10;
                }
                node3 = node2;
                node2 = node2.getParentNode();
            }
            short nodeType3 = nodeImpl2.getNodeType();
            short nodeType4 = node3.getNodeType();
            if (nodeType3 == 2) {
                this = ((AttrImpl) nodeImpl2).getOwnerElement();
            }
            if (nodeType4 == 2) {
                node = ((AttrImpl) node3).getOwnerElement();
            }
            if (nodeType3 == 2 && nodeType4 == 2 && this == node) {
                return 16;
            }
            if (nodeType3 == 2) {
                i = 0;
                for (NodeImpl nodeImpl3 = this; nodeImpl3 != null; nodeImpl3 = nodeImpl3.getParentNode()) {
                    i++;
                    if (nodeImpl3 == node) {
                        return 1;
                    }
                    nodeImpl2 = nodeImpl3;
                }
            }
            if (nodeType4 == 2) {
                i2 = 0;
                for (Node node4 = node; node4 != null; node4 = node4.getParentNode()) {
                    i2++;
                    if (node4 == this) {
                        return 2;
                    }
                    node3 = node4;
                }
            }
            if (nodeImpl2 != node3) {
                return 0;
            }
            if (i > i2) {
                NodeImpl nodeImpl4 = this;
                for (int i3 = 0; i3 < i - i2; i3++) {
                    nodeImpl4 = nodeImpl4.getParentNode();
                }
                if (nodeImpl4 == node) {
                    return 1;
                }
                this = nodeImpl4;
            } else {
                Node node5 = node;
                for (int i4 = 0; i4 < i2 - i; i4++) {
                    node5 = node5.getParentNode();
                }
                if (node5 == this) {
                    return 2;
                }
                node = node5;
            }
            Node parentNode = this.getParentNode();
            Node parentNode2 = node.getParentNode();
            while (true) {
                this = parentNode;
                node = parentNode2;
                if (this == node) {
                    break;
                }
                parentNode = this.getParentNode();
                parentNode2 = node.getParentNode();
            }
            for (Node firstChild = this.getFirstChild(); firstChild != null; firstChild = firstChild.getNextSibling()) {
                if (firstChild == node) {
                    return 1;
                }
                if (firstChild == this) {
                    return 2;
                }
            }
        }
        return 0;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:40:0x0087, code lost:
        if (r7 != 12) goto L_0x0092;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:84:0x0105, code lost:
        if (r14 != 12) goto L_0x0115;
     */
    public short compareDocumentPosition(Node node) throws DOMException {
        Document document;
        Document document2;
        Document document3;
        Document document4 = node;
        if (this == document4) {
            return 0;
        }
        try {
            NodeImpl nodeImpl = (NodeImpl) document4;
            if (getNodeType() == 9) {
                document = (Document) this;
            } else {
                document = getOwnerDocument();
            }
            if (node.getNodeType() == 9) {
                document2 = (Document) document4;
            } else {
                document2 = node.getOwnerDocument();
            }
            if (!(document == document2 || document == null || document2 == null)) {
                return ((CoreDocumentImpl) document2).getNodeNumber() > ((CoreDocumentImpl) document).getNodeNumber() ? (short) 37 : 35;
            }
            NodeImpl nodeImpl2 = this;
            Document document5 = nodeImpl2;
            int i = 0;
            while (nodeImpl2 != null) {
                i++;
                if (nodeImpl2 == document4) {
                    return 10;
                }
                document5 = nodeImpl2;
                nodeImpl2 = nodeImpl2.getParentNode();
            }
            Node node2 = document4;
            Document document6 = node2;
            int i2 = 0;
            while (node2 != null) {
                i2++;
                if (node2 == this) {
                    return 20;
                }
                document6 = node2;
                node2 = node2.getParentNode();
            }
            short nodeType = document5.getNodeType();
            short nodeType2 = document6.getNodeType();
            if (nodeType != 2) {
                if (nodeType != 6) {
                    if (nodeType == 10) {
                        if (document4 == document) {
                            return 10;
                        }
                        if (document != null && document == document2) {
                            return 4;
                        }
                    }
                    document3 = this;
                }
                DocumentType doctype = document.getDoctype();
                if (doctype == document6) {
                    return 10;
                }
                if (nodeType2 == 6 || nodeType2 == 12) {
                    return nodeType != nodeType2 ? nodeType > nodeType2 ? (short) 2 : 4 : nodeType == 12 ? doctype.getNotations().precedes(document6, document5) ? (short) 34 : 36 : doctype.getEntities().precedes(document6, document5) ? (short) 34 : 36;
                }
                document3 = document;
                document5 = document3;
            } else {
                document3 = ((AttrImpl) document5).getOwnerElement();
                if (nodeType2 == 2) {
                    Document ownerElement = ((AttrImpl) document6).getOwnerElement();
                    if (ownerElement == document3) {
                        return document3.getAttributes().precedes(document4, this) ? (short) 34 : 36;
                    }
                    document4 = ownerElement;
                }
                Document document7 = document5;
                int i3 = 0;
                for (Document document8 = document3; document8 != null; document8 = document8.getParentNode()) {
                    i3++;
                    if (document8 == document4) {
                        return 10;
                    }
                    document7 = document8;
                }
                i = i3;
                document5 = document7;
            }
            if (nodeType2 != 2) {
                if (nodeType2 != 6) {
                    if (nodeType2 == 10) {
                        if (document3 == document2) {
                            return 20;
                        }
                        if (document2 != null && document == document2) {
                            return 2;
                        }
                    }
                    document = document4;
                }
                if (document.getDoctype() == this) {
                    return 20;
                }
                document6 = document;
            } else {
                document = ((AttrImpl) document6).getOwnerElement();
                i2 = 0;
                for (Document document9 = document; document9 != null; document9 = document9.getParentNode()) {
                    i2++;
                    if (document9 == document3) {
                        return 20;
                    }
                    document6 = document9;
                }
            }
            if (document5 != document6) {
                return document5.getNodeNumber() > ((NodeImpl) document6).getNodeNumber() ? (short) 37 : 35;
            }
            if (i > i2) {
                for (int i4 = 0; i4 < i - i2; i4++) {
                    document3 = document3.getParentNode();
                }
                if (document3 == document) {
                    return 2;
                }
            } else {
                for (int i5 = 0; i5 < i2 - i; i5++) {
                    document = document.getParentNode();
                }
                if (document == document3) {
                    return 4;
                }
            }
            Document parentNode = document3.getParentNode();
            Document parentNode2 = document.getParentNode();
            while (true) {
                document3 = parentNode;
                document = parentNode2;
                if (document3 == document) {
                    break;
                }
                parentNode = document3.getParentNode();
                parentNode2 = document.getParentNode();
            }
            for (Document firstChild = document3.getFirstChild(); firstChild != null; firstChild = firstChild.getNextSibling()) {
                if (firstChild == document) {
                    return 2;
                }
                if (firstChild == document3) {
                    return 4;
                }
            }
            return 0;
        } catch (ClassCastException unused) {
            throw new DOMException(9, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NOT_SUPPORTED_ERR", null));
        }
    }

    public String getTextContent() throws DOMException {
        return getNodeValue();
    }

    /* access modifiers changed from: package-private */
    public void getTextContent(StringBuffer stringBuffer) throws DOMException {
        String nodeValue = getNodeValue();
        if (nodeValue != null) {
            stringBuffer.append(nodeValue);
        }
    }

    public void setTextContent(String str) throws DOMException {
        setNodeValue(str);
    }

    public boolean isDefaultNamespace(String str) {
        NodeImpl attributeNodeNS;
        short nodeType = getNodeType();
        if (nodeType == 1) {
            String namespaceURI = getNamespaceURI();
            String prefix = getPrefix();
            if (prefix == null || prefix.length() == 0) {
                if (str == null) {
                    return namespaceURI == str;
                }
                return str.equals(namespaceURI);
            } else if (!hasAttributes() || (attributeNodeNS = ((ElementImpl) this).getAttributeNodeNS("http://www.w3.org/2000/xmlns/", "xmlns")) == null) {
                NodeImpl elementAncestor = getElementAncestor(this);
                if (elementAncestor != null) {
                    return elementAncestor.isDefaultNamespace(str);
                }
                return false;
            } else {
                String nodeValue = attributeNodeNS.getNodeValue();
                if (str == null) {
                    return namespaceURI == nodeValue;
                }
                return str.equals(nodeValue);
            }
        } else if (nodeType != 2) {
            if (nodeType != 6) {
                switch (nodeType) {
                    case 9:
                        return ((Document) this).getDocumentElement().isDefaultNamespace(str);
                    case 10:
                    case 11:
                    case 12:
                        break;
                    default:
                        NodeImpl elementAncestor2 = getElementAncestor(this);
                        if (elementAncestor2 != null) {
                            return elementAncestor2.isDefaultNamespace(str);
                        }
                        return false;
                }
            }
            return false;
        } else if (this.ownerNode.getNodeType() == 1) {
            return this.ownerNode.isDefaultNamespace(str);
        } else {
            return false;
        }
    }

    public String lookupPrefix(String str) {
        if (str == null) {
            return null;
        }
        short nodeType = getNodeType();
        if (nodeType == 1) {
            getNamespaceURI();
            return lookupNamespacePrefix(str, (ElementImpl) this);
        } else if (nodeType != 2) {
            if (nodeType != 6) {
                switch (nodeType) {
                    case 9:
                        return ((Document) this).getDocumentElement().lookupPrefix(str);
                    case 10:
                    case 11:
                    case 12:
                        break;
                    default:
                        NodeImpl elementAncestor = getElementAncestor(this);
                        if (elementAncestor != null) {
                            return elementAncestor.lookupPrefix(str);
                        }
                        return null;
                }
            }
            return null;
        } else if (this.ownerNode.getNodeType() == 1) {
            return this.ownerNode.lookupPrefix(str);
        } else {
            return null;
        }
    }

    public String lookupNamespaceURI(String str) {
        short nodeType = getNodeType();
        if (nodeType == 1) {
            String namespaceURI = getNamespaceURI();
            String prefix = getPrefix();
            if (namespaceURI != null) {
                if (str == null && prefix == str) {
                    return namespaceURI;
                }
                if (prefix != null && prefix.equals(str)) {
                    return namespaceURI;
                }
            }
            if (hasAttributes()) {
                NamedNodeMap attributes = getAttributes();
                int length = attributes.getLength();
                for (int i = 0; i < length; i++) {
                    Node item = attributes.item(i);
                    String prefix2 = item.getPrefix();
                    String nodeValue = item.getNodeValue();
                    String namespaceURI2 = item.getNamespaceURI();
                    if (namespaceURI2 != null && namespaceURI2.equals("http://www.w3.org/2000/xmlns/")) {
                        if (str == null && item.getNodeName().equals("xmlns")) {
                            return nodeValue;
                        }
                        if (prefix2 != null && prefix2.equals("xmlns") && item.getLocalName().equals(str)) {
                            return nodeValue;
                        }
                    }
                }
            }
            NodeImpl elementAncestor = getElementAncestor(this);
            if (elementAncestor != null) {
                return elementAncestor.lookupNamespaceURI(str);
            }
            return null;
        } else if (nodeType != 2) {
            if (nodeType != 6) {
                switch (nodeType) {
                    case 9:
                        return ((Document) this).getDocumentElement().lookupNamespaceURI(str);
                    case 10:
                    case 11:
                    case 12:
                        break;
                    default:
                        NodeImpl elementAncestor2 = getElementAncestor(this);
                        if (elementAncestor2 != null) {
                            return elementAncestor2.lookupNamespaceURI(str);
                        }
                        return null;
                }
            }
            return null;
        } else if (this.ownerNode.getNodeType() == 1) {
            return this.ownerNode.lookupNamespaceURI(str);
        } else {
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    public Node getElementAncestor(Node node) {
        Node parentNode = node.getParentNode();
        if (parentNode == null) {
            return null;
        }
        if (parentNode.getNodeType() == 1) {
            return parentNode;
        }
        return getElementAncestor(parentNode);
    }

    /* access modifiers changed from: package-private */
    public String lookupNamespacePrefix(String str, ElementImpl elementImpl) {
        String localName;
        String lookupNamespaceURI;
        String lookupNamespaceURI2;
        String namespaceURI = getNamespaceURI();
        String prefix = getPrefix();
        if (!(namespaceURI == null || !namespaceURI.equals(str) || prefix == null || (lookupNamespaceURI2 = elementImpl.lookupNamespaceURI(prefix)) == null || !lookupNamespaceURI2.equals(str))) {
            return prefix;
        }
        if (hasAttributes()) {
            NamedNodeMap attributes = getAttributes();
            int length = attributes.getLength();
            for (int i = 0; i < length; i++) {
                Node item = attributes.item(i);
                String prefix2 = item.getPrefix();
                String nodeValue = item.getNodeValue();
                String namespaceURI2 = item.getNamespaceURI();
                if (namespaceURI2 != null && namespaceURI2.equals("http://www.w3.org/2000/xmlns/") && ((item.getNodeName().equals("xmlns") || (prefix2 != null && prefix2.equals("xmlns") && nodeValue.equals(str))) && (lookupNamespaceURI = elementImpl.lookupNamespaceURI((localName = item.getLocalName()))) != null && lookupNamespaceURI.equals(str))) {
                    return localName;
                }
            }
        }
        NodeImpl elementAncestor = getElementAncestor(this);
        if (elementAncestor != null) {
            return elementAncestor.lookupNamespacePrefix(str, elementImpl);
        }
        return null;
    }

    public boolean isEqualNode(Node node) {
        if (node == this) {
            return true;
        }
        if (node.getNodeType() != getNodeType()) {
            return false;
        }
        if (getNodeName() == null) {
            if (node.getNodeName() != null) {
                return false;
            }
        } else if (!getNodeName().equals(node.getNodeName())) {
            return false;
        }
        if (getLocalName() == null) {
            if (node.getLocalName() != null) {
                return false;
            }
        } else if (!getLocalName().equals(node.getLocalName())) {
            return false;
        }
        if (getNamespaceURI() == null) {
            if (node.getNamespaceURI() != null) {
                return false;
            }
        } else if (!getNamespaceURI().equals(node.getNamespaceURI())) {
            return false;
        }
        if (getPrefix() == null) {
            if (node.getPrefix() != null) {
                return false;
            }
        } else if (!getPrefix().equals(node.getPrefix())) {
            return false;
        }
        if (getNodeValue() == null) {
            if (node.getNodeValue() != null) {
                return false;
            }
        } else if (!getNodeValue().equals(node.getNodeValue())) {
            return false;
        }
        return true;
    }

    public Object getFeature(String str, String str2) {
        if (isSupported(str, str2)) {
            return this;
        }
        return null;
    }

    public Object setUserData(String str, Object obj, UserDataHandler userDataHandler) {
        return ownerDocument().setUserData(this, str, obj, userDataHandler);
    }

    public Object getUserData(String str) {
        return ownerDocument().getUserData(this, str);
    }

    /* access modifiers changed from: protected */
    public Map<String, ParentNode.UserDataRecord> getUserDataRecord() {
        return ownerDocument().getUserDataRecord(this);
    }

    public void setReadOnly(boolean z, boolean z2) {
        if (needsSyncData()) {
            synchronizeData();
        }
        isReadOnly(z);
    }

    public boolean getReadOnly() {
        if (needsSyncData()) {
            synchronizeData();
        }
        return isReadOnly();
    }

    public void setUserData(Object obj) {
        ownerDocument().setUserData(this, obj);
    }

    public Object getUserData() {
        return ownerDocument().getUserData(this);
    }

    /* access modifiers changed from: protected */
    public void changed() {
        ownerDocument().changed();
    }

    /* access modifiers changed from: protected */
    public int changes() {
        return ownerDocument().changes();
    }

    /* access modifiers changed from: protected */
    public void synchronizeData() {
        needsSyncData(false);
    }

    /* access modifiers changed from: package-private */
    public final boolean isReadOnly() {
        return (this.flags & 1) != 0;
    }

    /* access modifiers changed from: package-private */
    public final void isReadOnly(boolean z) {
        this.flags = (short) (z ? this.flags | 1 : this.flags & -2);
    }

    /* access modifiers changed from: package-private */
    public final boolean needsSyncData() {
        return (this.flags & 2) != 0;
    }

    /* access modifiers changed from: package-private */
    public final void needsSyncData(boolean z) {
        this.flags = (short) (z ? this.flags | 2 : this.flags & -3);
    }

    /* access modifiers changed from: package-private */
    public final boolean needsSyncChildren() {
        return (this.flags & 4) != 0;
    }

    public final void needsSyncChildren(boolean z) {
        this.flags = (short) (z ? this.flags | 4 : this.flags & -5);
    }

    /* access modifiers changed from: package-private */
    public final boolean isOwned() {
        return (this.flags & 8) != 0;
    }

    /* access modifiers changed from: package-private */
    public final void isOwned(boolean z) {
        this.flags = (short) (z ? this.flags | 8 : this.flags & -9);
    }

    /* access modifiers changed from: package-private */
    public final boolean isFirstChild() {
        return (this.flags & 16) != 0;
    }

    /* access modifiers changed from: package-private */
    public final void isFirstChild(boolean z) {
        this.flags = (short) (z ? this.flags | 16 : this.flags & -17);
    }

    /* access modifiers changed from: package-private */
    public final boolean isSpecified() {
        return (this.flags & 32) != 0;
    }

    /* access modifiers changed from: package-private */
    public final void isSpecified(boolean z) {
        this.flags = (short) (z ? this.flags | 32 : this.flags & -33);
    }

    /* access modifiers changed from: package-private */
    public final boolean internalIsIgnorableWhitespace() {
        return (this.flags & 64) != 0;
    }

    /* access modifiers changed from: package-private */
    public final void isIgnorableWhitespace(boolean z) {
        this.flags = (short) (z ? this.flags | 64 : this.flags & -65);
    }

    /* access modifiers changed from: package-private */
    public final boolean hasStringValue() {
        return (this.flags & 128) != 0;
    }

    /* access modifiers changed from: package-private */
    public final void hasStringValue(boolean z) {
        this.flags = (short) (z ? this.flags | 128 : this.flags & -129);
    }

    /* access modifiers changed from: package-private */
    public final boolean isNormalized() {
        return (this.flags & 256) != 0;
    }

    /* access modifiers changed from: package-private */
    public final void isNormalized(boolean z) {
        NodeImpl nodeImpl;
        if (!z && isNormalized() && (nodeImpl = this.ownerNode) != null) {
            nodeImpl.isNormalized(false);
        }
        this.flags = (short) (z ? this.flags | 256 : this.flags & -257);
    }

    /* access modifiers changed from: package-private */
    public final boolean isIdAttribute() {
        return (this.flags & 512) != 0;
    }

    /* access modifiers changed from: package-private */
    public final void isIdAttribute(boolean z) {
        this.flags = (short) (z ? this.flags | 512 : this.flags & -513);
    }

    @Override // java.lang.Object
    public String toString() {
        return "[" + getNodeName() + ": " + getNodeValue() + "]";
    }

    private void writeObject(ObjectOutputStream objectOutputStream) throws IOException {
        if (needsSyncData()) {
            synchronizeData();
        }
        objectOutputStream.defaultWriteObject();
    }
}

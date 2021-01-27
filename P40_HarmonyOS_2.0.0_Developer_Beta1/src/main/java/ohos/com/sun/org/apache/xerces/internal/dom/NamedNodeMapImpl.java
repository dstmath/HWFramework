package ohos.com.sun.org.apache.xerces.internal.dom;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import ohos.org.w3c.dom.DOMException;
import ohos.org.w3c.dom.Document;
import ohos.org.w3c.dom.NamedNodeMap;
import ohos.org.w3c.dom.Node;

public class NamedNodeMapImpl implements NamedNodeMap, Serializable {
    protected static final short CHANGED = 2;
    protected static final short HASDEFAULTS = 4;
    protected static final short READONLY = 1;
    static final long serialVersionUID = -7039242451046758020L;
    protected short flags;
    protected List nodes;
    protected NodeImpl ownerNode;

    protected NamedNodeMapImpl(NodeImpl nodeImpl) {
        this.ownerNode = nodeImpl;
    }

    public int getLength() {
        List list = this.nodes;
        if (list != null) {
            return list.size();
        }
        return 0;
    }

    public Node item(int i) {
        List list = this.nodes;
        if (list == null || i >= list.size()) {
            return null;
        }
        return (Node) this.nodes.get(i);
    }

    public Node getNamedItem(String str) {
        int findNamePoint = findNamePoint(str, 0);
        if (findNamePoint < 0) {
            return null;
        }
        return (Node) this.nodes.get(findNamePoint);
    }

    public Node getNamedItemNS(String str, String str2) {
        int findNamePoint = findNamePoint(str, str2);
        if (findNamePoint < 0) {
            return null;
        }
        return (Node) this.nodes.get(findNamePoint);
    }

    public Node setNamedItem(Node node) throws DOMException {
        Document ownerDocument = this.ownerNode.ownerDocument();
        if (ownerDocument.errorChecking) {
            if (isReadOnly()) {
                throw new DOMException(7, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NO_MODIFICATION_ALLOWED_ERR", null));
            } else if (node.getOwnerDocument() != ownerDocument) {
                throw new DOMException(4, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "WRONG_DOCUMENT_ERR", null));
            }
        }
        int findNamePoint = findNamePoint(node.getNodeName(), 0);
        if (findNamePoint >= 0) {
            NodeImpl nodeImpl = (NodeImpl) this.nodes.get(findNamePoint);
            this.nodes.set(findNamePoint, node);
            return nodeImpl;
        }
        int i = -1 - findNamePoint;
        if (this.nodes == null) {
            this.nodes = new ArrayList(5);
        }
        this.nodes.add(i, node);
        return null;
    }

    public Node setNamedItemNS(Node node) throws DOMException {
        Document ownerDocument = this.ownerNode.ownerDocument();
        if (ownerDocument.errorChecking) {
            if (isReadOnly()) {
                throw new DOMException(7, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NO_MODIFICATION_ALLOWED_ERR", null));
            } else if (node.getOwnerDocument() != ownerDocument) {
                throw new DOMException(4, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "WRONG_DOCUMENT_ERR", null));
            }
        }
        int findNamePoint = findNamePoint(node.getNamespaceURI(), node.getLocalName());
        if (findNamePoint >= 0) {
            NodeImpl nodeImpl = (NodeImpl) this.nodes.get(findNamePoint);
            this.nodes.set(findNamePoint, node);
            return nodeImpl;
        }
        int findNamePoint2 = findNamePoint(node.getNodeName(), 0);
        if (findNamePoint2 >= 0) {
            NodeImpl nodeImpl2 = (NodeImpl) this.nodes.get(findNamePoint2);
            this.nodes.add(findNamePoint2, node);
            return nodeImpl2;
        }
        int i = -1 - findNamePoint2;
        if (this.nodes == null) {
            this.nodes = new ArrayList(5);
        }
        this.nodes.add(i, node);
        return null;
    }

    public Node removeNamedItem(String str) throws DOMException {
        if (!isReadOnly()) {
            int findNamePoint = findNamePoint(str, 0);
            if (findNamePoint >= 0) {
                NodeImpl nodeImpl = (NodeImpl) this.nodes.get(findNamePoint);
                this.nodes.remove(findNamePoint);
                return nodeImpl;
            }
            throw new DOMException(8, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NOT_FOUND_ERR", null));
        }
        throw new DOMException(7, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NO_MODIFICATION_ALLOWED_ERR", null));
    }

    public Node removeNamedItemNS(String str, String str2) throws DOMException {
        if (!isReadOnly()) {
            int findNamePoint = findNamePoint(str, str2);
            if (findNamePoint >= 0) {
                NodeImpl nodeImpl = (NodeImpl) this.nodes.get(findNamePoint);
                this.nodes.remove(findNamePoint);
                return nodeImpl;
            }
            throw new DOMException(8, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NOT_FOUND_ERR", null));
        }
        throw new DOMException(7, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NO_MODIFICATION_ALLOWED_ERR", null));
    }

    public NamedNodeMapImpl cloneMap(NodeImpl nodeImpl) {
        NamedNodeMapImpl namedNodeMapImpl = new NamedNodeMapImpl(nodeImpl);
        namedNodeMapImpl.cloneContent(this);
        return namedNodeMapImpl;
    }

    /* access modifiers changed from: protected */
    public void cloneContent(NamedNodeMapImpl namedNodeMapImpl) {
        int size;
        List list = namedNodeMapImpl.nodes;
        if (!(list == null || (size = list.size()) == 0)) {
            List list2 = this.nodes;
            if (list2 == null) {
                this.nodes = new ArrayList(size);
            } else {
                list2.clear();
            }
            for (int i = 0; i < size; i++) {
                NodeImpl nodeImpl = (NodeImpl) namedNodeMapImpl.nodes.get(i);
                NodeImpl cloneNode = nodeImpl.cloneNode(true);
                cloneNode.isSpecified(nodeImpl.isSpecified());
                this.nodes.add(cloneNode);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setReadOnly(boolean z, boolean z2) {
        List list;
        isReadOnly(z);
        if (z2 && (list = this.nodes) != null) {
            for (int size = list.size() - 1; size >= 0; size--) {
                ((NodeImpl) this.nodes.get(size)).setReadOnly(z, z2);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean getReadOnly() {
        return isReadOnly();
    }

    /* access modifiers changed from: protected */
    public void setOwnerDocument(CoreDocumentImpl coreDocumentImpl) {
        List list = this.nodes;
        if (list != null) {
            int size = list.size();
            for (int i = 0; i < size; i++) {
                item(i).setOwnerDocument(coreDocumentImpl);
            }
        }
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
    public final boolean changed() {
        return (this.flags & 2) != 0;
    }

    /* access modifiers changed from: package-private */
    public final void changed(boolean z) {
        this.flags = (short) (z ? this.flags | 2 : this.flags & -3);
    }

    /* access modifiers changed from: package-private */
    public final boolean hasDefaults() {
        return (this.flags & 4) != 0;
    }

    /* access modifiers changed from: package-private */
    public final void hasDefaults(boolean z) {
        this.flags = (short) (z ? this.flags | 4 : this.flags & -5);
    }

    /* access modifiers changed from: protected */
    public int findNamePoint(String str, int i) {
        List list = this.nodes;
        int i2 = 0;
        if (list != null) {
            int size = list.size() - 1;
            i2 = i;
            int i3 = 0;
            while (i2 <= size) {
                i3 = (i2 + size) / 2;
                int compareTo = str.compareTo(((Node) this.nodes.get(i3)).getNodeName());
                if (compareTo == 0) {
                    return i3;
                }
                if (compareTo < 0) {
                    size = i3 - 1;
                } else {
                    i2 = i3 + 1;
                }
            }
            if (i2 <= i3) {
                i2 = i3;
            }
        }
        return -1 - i2;
    }

    /* access modifiers changed from: protected */
    public int findNamePoint(String str, String str2) {
        List list = this.nodes;
        if (list == null || str2 == null) {
            return -1;
        }
        int size = list.size();
        for (int i = 0; i < size; i++) {
            NodeImpl nodeImpl = (NodeImpl) this.nodes.get(i);
            String namespaceURI = nodeImpl.getNamespaceURI();
            String localName = nodeImpl.getLocalName();
            if (str == null) {
                if (namespaceURI == null && (str2.equals(localName) || (localName == null && str2.equals(nodeImpl.getNodeName())))) {
                    return i;
                }
            } else if (str.equals(namespaceURI) && str2.equals(localName)) {
                return i;
            }
        }
        return -1;
    }

    /* access modifiers changed from: protected */
    public boolean precedes(Node node, Node node2) {
        List list = this.nodes;
        if (list != null) {
            int size = list.size();
            for (int i = 0; i < size; i++) {
                Node node3 = (Node) this.nodes.get(i);
                if (node3 == node) {
                    return true;
                }
                if (node3 == node2) {
                    return false;
                }
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public void removeItem(int i) {
        List list = this.nodes;
        if (list != null && i < list.size()) {
            this.nodes.remove(i);
        }
    }

    /* access modifiers changed from: protected */
    public Object getItem(int i) {
        List list = this.nodes;
        if (list != null) {
            return list.get(i);
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public int addItem(Node node) {
        int findNamePoint = findNamePoint(node.getNamespaceURI(), node.getLocalName());
        if (findNamePoint >= 0) {
            this.nodes.set(findNamePoint, node);
        } else {
            findNamePoint = findNamePoint(node.getNodeName(), 0);
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
        return findNamePoint;
    }

    /* access modifiers changed from: protected */
    public ArrayList cloneMap(ArrayList arrayList) {
        if (arrayList == null) {
            arrayList = new ArrayList(5);
        }
        arrayList.clear();
        List list = this.nodes;
        if (list != null) {
            int size = list.size();
            for (int i = 0; i < size; i++) {
                arrayList.add(this.nodes.get(i));
            }
        }
        return arrayList;
    }

    /* access modifiers changed from: protected */
    public int getNamedItemIndex(String str, String str2) {
        return findNamePoint(str, str2);
    }

    public void removeAll() {
        List list = this.nodes;
        if (list != null) {
            list.clear();
        }
    }

    private void readObject(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
        objectInputStream.defaultReadObject();
        List list = this.nodes;
        if (list != null) {
            this.nodes = new ArrayList((Vector) list);
        }
    }

    private void writeObject(ObjectOutputStream objectOutputStream) throws IOException {
        Vector vector = this.nodes;
        if (vector != null) {
            try {
                vector = new Vector(vector);
            } finally {
                this.nodes = vector;
            }
        }
        objectOutputStream.defaultWriteObject();
        this.nodes = vector;
    }
}

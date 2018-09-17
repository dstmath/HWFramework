package org.apache.harmony.xml.dom;

import java.util.ArrayList;
import java.util.List;
import libcore.util.Objects;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.TypeInfo;

public class ElementImpl extends InnerNodeImpl implements Element {
    private List<AttrImpl> attributes = new ArrayList();
    String localName;
    boolean namespaceAware;
    String namespaceURI;
    String prefix;

    public class ElementAttrNamedNodeMapImpl implements NamedNodeMap {
        public int getLength() {
            return ElementImpl.this.attributes.size();
        }

        private int indexOfItem(String name) {
            return ElementImpl.this.indexOfAttribute(name);
        }

        private int indexOfItemNS(String namespaceURI, String localName) {
            return ElementImpl.this.indexOfAttributeNS(namespaceURI, localName);
        }

        public Node getNamedItem(String name) {
            return ElementImpl.this.getAttributeNode(name);
        }

        public Node getNamedItemNS(String namespaceURI, String localName) {
            return ElementImpl.this.getAttributeNodeNS(namespaceURI, localName);
        }

        public Node item(int index) {
            return (Node) ElementImpl.this.attributes.get(index);
        }

        public Node removeNamedItem(String name) throws DOMException {
            int i = indexOfItem(name);
            if (i != -1) {
                return (Node) ElementImpl.this.attributes.remove(i);
            }
            throw new DOMException((short) 8, null);
        }

        public Node removeNamedItemNS(String namespaceURI, String localName) throws DOMException {
            int i = indexOfItemNS(namespaceURI, localName);
            if (i != -1) {
                return (Node) ElementImpl.this.attributes.remove(i);
            }
            throw new DOMException((short) 8, null);
        }

        public Node setNamedItem(Node arg) throws DOMException {
            if (arg instanceof Attr) {
                return ElementImpl.this.setAttributeNode((Attr) arg);
            }
            throw new DOMException((short) 3, null);
        }

        public Node setNamedItemNS(Node arg) throws DOMException {
            if (arg instanceof Attr) {
                return ElementImpl.this.setAttributeNodeNS((Attr) arg);
            }
            throw new DOMException((short) 3, null);
        }
    }

    ElementImpl(DocumentImpl document, String namespaceURI, String qualifiedName) {
        super(document);
        NodeImpl.setNameNS(this, namespaceURI, qualifiedName);
    }

    ElementImpl(DocumentImpl document, String name) {
        super(document);
        NodeImpl.setName(this, name);
    }

    private int indexOfAttribute(String name) {
        for (int i = 0; i < this.attributes.size(); i++) {
            if (Objects.equal(name, ((AttrImpl) this.attributes.get(i)).getNodeName())) {
                return i;
            }
        }
        return -1;
    }

    private int indexOfAttributeNS(String namespaceURI, String localName) {
        for (int i = 0; i < this.attributes.size(); i++) {
            AttrImpl attr = (AttrImpl) this.attributes.get(i);
            if (Objects.equal(namespaceURI, attr.getNamespaceURI()) && Objects.equal(localName, attr.getLocalName())) {
                return i;
            }
        }
        return -1;
    }

    public String getAttribute(String name) {
        Attr attr = getAttributeNode(name);
        if (attr == null) {
            return "";
        }
        return attr.getValue();
    }

    public String getAttributeNS(String namespaceURI, String localName) {
        Attr attr = getAttributeNodeNS(namespaceURI, localName);
        if (attr == null) {
            return "";
        }
        return attr.getValue();
    }

    public AttrImpl getAttributeNode(String name) {
        int i = indexOfAttribute(name);
        if (i == -1) {
            return null;
        }
        return (AttrImpl) this.attributes.get(i);
    }

    public AttrImpl getAttributeNodeNS(String namespaceURI, String localName) {
        int i = indexOfAttributeNS(namespaceURI, localName);
        if (i == -1) {
            return null;
        }
        return (AttrImpl) this.attributes.get(i);
    }

    public NamedNodeMap getAttributes() {
        return new ElementAttrNamedNodeMapImpl();
    }

    Element getElementById(String name) {
        for (AttrImpl attr : this.attributes) {
            if (attr.isId() && name.equals(attr.getValue())) {
                return this;
            }
        }
        if (name.equals(getAttribute("id"))) {
            return this;
        }
        for (LeafNodeImpl node : this.children) {
            if (node.getNodeType() == (short) 1) {
                Element element = ((ElementImpl) node).getElementById(name);
                if (element != null) {
                    return element;
                }
            }
        }
        return null;
    }

    public NodeList getElementsByTagName(String name) {
        NodeListImpl result = new NodeListImpl();
        getElementsByTagName(result, name);
        return result;
    }

    public NodeList getElementsByTagNameNS(String namespaceURI, String localName) {
        NodeListImpl result = new NodeListImpl();
        getElementsByTagNameNS(result, namespaceURI, localName);
        return result;
    }

    public String getLocalName() {
        return this.namespaceAware ? this.localName : null;
    }

    public String getNamespaceURI() {
        return this.namespaceURI;
    }

    public String getNodeName() {
        return getTagName();
    }

    public short getNodeType() {
        return (short) 1;
    }

    public String getPrefix() {
        return this.prefix;
    }

    public String getTagName() {
        if (this.prefix != null) {
            return this.prefix + ":" + this.localName;
        }
        return this.localName;
    }

    public boolean hasAttribute(String name) {
        return indexOfAttribute(name) != -1;
    }

    public boolean hasAttributeNS(String namespaceURI, String localName) {
        return indexOfAttributeNS(namespaceURI, localName) != -1;
    }

    public boolean hasAttributes() {
        return this.attributes.isEmpty() ^ 1;
    }

    public void removeAttribute(String name) throws DOMException {
        int i = indexOfAttribute(name);
        if (i != -1) {
            this.attributes.remove(i);
        }
    }

    public void removeAttributeNS(String namespaceURI, String localName) throws DOMException {
        int i = indexOfAttributeNS(namespaceURI, localName);
        if (i != -1) {
            this.attributes.remove(i);
        }
    }

    public Attr removeAttributeNode(Attr oldAttr) throws DOMException {
        AttrImpl oldAttrImpl = (AttrImpl) oldAttr;
        if (oldAttrImpl.getOwnerElement() != this) {
            throw new DOMException((short) 8, null);
        }
        this.attributes.remove(oldAttrImpl);
        oldAttrImpl.ownerElement = null;
        return oldAttrImpl;
    }

    public void setAttribute(String name, String value) throws DOMException {
        Attr attr = getAttributeNode(name);
        if (attr == null) {
            attr = this.document.createAttribute(name);
            setAttributeNode(attr);
        }
        attr.setValue(value);
    }

    public void setAttributeNS(String namespaceURI, String qualifiedName, String value) throws DOMException {
        Attr attr = getAttributeNodeNS(namespaceURI, qualifiedName);
        if (attr == null) {
            attr = this.document.createAttributeNS(namespaceURI, qualifiedName);
            setAttributeNodeNS(attr);
        }
        attr.setValue(value);
    }

    public Attr setAttributeNode(Attr newAttr) throws DOMException {
        AttrImpl newAttrImpl = (AttrImpl) newAttr;
        if (newAttrImpl.document != this.document) {
            throw new DOMException((short) 4, null);
        } else if (newAttrImpl.getOwnerElement() != null) {
            throw new DOMException((short) 10, null);
        } else {
            Attr oldAttrImpl = null;
            int i = indexOfAttribute(newAttr.getName());
            if (i != -1) {
                oldAttrImpl = (AttrImpl) this.attributes.get(i);
                this.attributes.remove(i);
            }
            this.attributes.add(newAttrImpl);
            newAttrImpl.ownerElement = this;
            return oldAttrImpl;
        }
    }

    public Attr setAttributeNodeNS(Attr newAttr) throws DOMException {
        AttrImpl newAttrImpl = (AttrImpl) newAttr;
        if (newAttrImpl.document != this.document) {
            throw new DOMException((short) 4, null);
        } else if (newAttrImpl.getOwnerElement() != null) {
            throw new DOMException((short) 10, null);
        } else {
            Attr oldAttrImpl = null;
            int i = indexOfAttributeNS(newAttr.getNamespaceURI(), newAttr.getLocalName());
            if (i != -1) {
                oldAttrImpl = (AttrImpl) this.attributes.get(i);
                this.attributes.remove(i);
            }
            this.attributes.add(newAttrImpl);
            newAttrImpl.ownerElement = this;
            return oldAttrImpl;
        }
    }

    public void setPrefix(String prefix) {
        this.prefix = NodeImpl.validatePrefix(prefix, this.namespaceAware, this.namespaceURI);
    }

    public TypeInfo getSchemaTypeInfo() {
        return NULL_TYPE_INFO;
    }

    public void setIdAttribute(String name, boolean isId) throws DOMException {
        AttrImpl attr = getAttributeNode(name);
        if (attr == null) {
            throw new DOMException((short) 8, "No such attribute: " + name);
        }
        attr.isId = isId;
    }

    public void setIdAttributeNS(String namespaceURI, String localName, boolean isId) throws DOMException {
        AttrImpl attr = getAttributeNodeNS(namespaceURI, localName);
        if (attr == null) {
            throw new DOMException((short) 8, "No such attribute: " + namespaceURI + " " + localName);
        }
        attr.isId = isId;
    }

    public void setIdAttributeNode(Attr idAttr, boolean isId) throws DOMException {
        ((AttrImpl) idAttr).isId = isId;
    }
}

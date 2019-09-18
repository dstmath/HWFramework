package org.apache.harmony.xml.dom;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import org.apache.harmony.xml.dom.NodeImpl;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

public final class DocumentImpl extends InnerNodeImpl implements Document {
    private String documentUri;
    private DOMConfigurationImpl domConfiguration;
    private DOMImplementation domImplementation;
    private String inputEncoding;
    private WeakHashMap<NodeImpl, Map<String, NodeImpl.UserData>> nodeToUserData;
    private boolean strictErrorChecking = true;
    private boolean xmlStandalone = false;
    private String xmlVersion = "1.0";

    public DocumentImpl(DOMImplementationImpl impl, String namespaceURI, String qualifiedName, DocumentType doctype, String inputEncoding2) {
        super(null);
        this.document = this;
        this.domImplementation = impl;
        this.inputEncoding = inputEncoding2;
        if (doctype != null) {
            appendChild(doctype);
        }
        if (qualifiedName != null) {
            appendChild(createElementNS(namespaceURI, qualifiedName));
        }
    }

    private static boolean isXMLIdentifierStart(char c) {
        return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || c == '_';
    }

    private static boolean isXMLIdentifierPart(char c) {
        return isXMLIdentifierStart(c) || (c >= '0' && c <= '9') || c == '-' || c == '.';
    }

    static boolean isXMLIdentifier(String s) {
        if (s.length() == 0 || !isXMLIdentifierStart(s.charAt(0))) {
            return false;
        }
        for (int i = 1; i < s.length(); i++) {
            if (!isXMLIdentifierPart(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private NodeImpl shallowCopy(short operation, Node node) {
        ElementImpl elementCopy;
        AttrImpl attrCopy;
        switch (node.getNodeType()) {
            case 1:
                ElementImpl element = (ElementImpl) node;
                if (element.namespaceAware) {
                    elementCopy = createElementNS(element.getNamespaceURI(), element.getLocalName());
                    elementCopy.setPrefix(element.getPrefix());
                } else {
                    elementCopy = createElement(element.getTagName());
                }
                NamedNodeMap attributes = element.getAttributes();
                for (int i = 0; i < attributes.getLength(); i++) {
                    AttrImpl elementAttr = (AttrImpl) attributes.item(i);
                    AttrImpl elementAttrCopy = (AttrImpl) shallowCopy(operation, elementAttr);
                    notifyUserDataHandlers(operation, elementAttr, elementAttrCopy);
                    if (elementAttr.namespaceAware) {
                        elementCopy.setAttributeNodeNS(elementAttrCopy);
                    } else {
                        elementCopy.setAttributeNode(elementAttrCopy);
                    }
                }
                return elementCopy;
            case 2:
                AttrImpl attr = (AttrImpl) node;
                if (attr.namespaceAware) {
                    attrCopy = createAttributeNS(attr.getNamespaceURI(), attr.getLocalName());
                    attrCopy.setPrefix(attr.getPrefix());
                } else {
                    attrCopy = createAttribute(attr.getName());
                }
                attrCopy.setNodeValue(attr.getValue());
                return attrCopy;
            case 3:
                return createTextNode(((Text) node).getData());
            case 4:
                return createCDATASection(((CharacterData) node).getData());
            case 5:
                return createEntityReference(node.getNodeName());
            case 6:
            case 12:
                throw new UnsupportedOperationException();
            case 7:
                ProcessingInstruction pi = (ProcessingInstruction) node;
                return createProcessingInstruction(pi.getTarget(), pi.getData());
            case 8:
                return createComment(((Comment) node).getData());
            case 9:
            case 10:
                throw new DOMException(9, "Cannot copy node of type " + node.getNodeType());
            case 11:
                return createDocumentFragment();
            default:
                throw new DOMException(9, "Unsupported node type " + node.getNodeType());
        }
    }

    /* access modifiers changed from: package-private */
    public Node cloneOrImportNode(short operation, Node node, boolean deep) {
        NodeImpl copy = shallowCopy(operation, node);
        if (deep) {
            NodeList list = node.getChildNodes();
            for (int i = 0; i < list.getLength(); i++) {
                copy.appendChild(cloneOrImportNode(operation, list.item(i), deep));
            }
        }
        notifyUserDataHandlers(operation, node, copy);
        return copy;
    }

    public Node importNode(Node importedNode, boolean deep) {
        return cloneOrImportNode(2, importedNode, deep);
    }

    public Node adoptNode(Node node) {
        if (!(node instanceof NodeImpl)) {
            return null;
        }
        NodeImpl nodeImpl = (NodeImpl) node;
        switch (nodeImpl.getNodeType()) {
            case 1:
            case 3:
            case 4:
            case 5:
            case 7:
            case 8:
            case 11:
                break;
            case 2:
                AttrImpl attr = (AttrImpl) node;
                if (attr.ownerElement != null) {
                    attr.ownerElement.removeAttributeNode(attr);
                    break;
                }
                break;
            case 6:
            case 9:
            case 10:
            case 12:
                throw new DOMException(9, "Cannot adopt nodes of type " + nodeImpl.getNodeType());
            default:
                throw new DOMException(9, "Unsupported node type " + node.getNodeType());
        }
        Node parent = nodeImpl.getParentNode();
        if (parent != null) {
            parent.removeChild(nodeImpl);
        }
        changeDocumentToThis(nodeImpl);
        notifyUserDataHandlers(5, node, null);
        return nodeImpl;
    }

    private void changeDocumentToThis(NodeImpl node) {
        Map<String, NodeImpl.UserData> userData = node.document.getUserDataMapForRead(node);
        if (!userData.isEmpty()) {
            getUserDataMap(node).putAll(userData);
        }
        node.document = this;
        NodeList list = node.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            changeDocumentToThis((NodeImpl) list.item(i));
        }
        if (node.getNodeType() == 1) {
            NamedNodeMap attributes = node.getAttributes();
            for (int i2 = 0; i2 < attributes.getLength(); i2++) {
                changeDocumentToThis((AttrImpl) attributes.item(i2));
            }
        }
    }

    public Node renameNode(Node node, String namespaceURI, String qualifiedName) {
        if (node.getOwnerDocument() == this) {
            setNameNS((NodeImpl) node, namespaceURI, qualifiedName);
            notifyUserDataHandlers(4, node, null);
            return node;
        }
        throw new DOMException(4, null);
    }

    public AttrImpl createAttribute(String name) {
        return new AttrImpl(this, name);
    }

    public AttrImpl createAttributeNS(String namespaceURI, String qualifiedName) {
        return new AttrImpl(this, namespaceURI, qualifiedName);
    }

    public CDATASectionImpl createCDATASection(String data) {
        return new CDATASectionImpl(this, data);
    }

    public CommentImpl createComment(String data) {
        return new CommentImpl(this, data);
    }

    public DocumentFragmentImpl createDocumentFragment() {
        return new DocumentFragmentImpl(this);
    }

    public ElementImpl createElement(String tagName) {
        return new ElementImpl(this, tagName);
    }

    public ElementImpl createElementNS(String namespaceURI, String qualifiedName) {
        return new ElementImpl(this, namespaceURI, qualifiedName);
    }

    public EntityReferenceImpl createEntityReference(String name) {
        return new EntityReferenceImpl(this, name);
    }

    public ProcessingInstructionImpl createProcessingInstruction(String target, String data) {
        return new ProcessingInstructionImpl(this, target, data);
    }

    public TextImpl createTextNode(String data) {
        return new TextImpl(this, data);
    }

    public DocumentType getDoctype() {
        for (LeafNodeImpl child : this.children) {
            if (child instanceof DocumentType) {
                return (DocumentType) child;
            }
        }
        return null;
    }

    public Element getDocumentElement() {
        for (LeafNodeImpl child : this.children) {
            if (child instanceof Element) {
                return (Element) child;
            }
        }
        return null;
    }

    public Element getElementById(String elementId) {
        ElementImpl root = (ElementImpl) getDocumentElement();
        if (root == null) {
            return null;
        }
        return root.getElementById(elementId);
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

    public DOMImplementation getImplementation() {
        return this.domImplementation;
    }

    public String getNodeName() {
        return "#document";
    }

    public short getNodeType() {
        return 9;
    }

    public Node insertChildAt(Node toInsert, int index) {
        if ((toInsert instanceof Element) && getDocumentElement() != null) {
            throw new DOMException(3, "Only one root element allowed");
        } else if (!(toInsert instanceof DocumentType) || getDoctype() == null) {
            return super.insertChildAt(toInsert, index);
        } else {
            throw new DOMException(3, "Only one DOCTYPE element allowed");
        }
    }

    public String getTextContent() {
        return null;
    }

    public String getInputEncoding() {
        return this.inputEncoding;
    }

    public String getXmlEncoding() {
        return null;
    }

    public boolean getXmlStandalone() {
        return this.xmlStandalone;
    }

    public void setXmlStandalone(boolean xmlStandalone2) {
        this.xmlStandalone = xmlStandalone2;
    }

    public String getXmlVersion() {
        return this.xmlVersion;
    }

    public void setXmlVersion(String xmlVersion2) {
        this.xmlVersion = xmlVersion2;
    }

    public boolean getStrictErrorChecking() {
        return this.strictErrorChecking;
    }

    public void setStrictErrorChecking(boolean strictErrorChecking2) {
        this.strictErrorChecking = strictErrorChecking2;
    }

    public String getDocumentURI() {
        return this.documentUri;
    }

    public void setDocumentURI(String documentUri2) {
        this.documentUri = documentUri2;
    }

    public DOMConfiguration getDomConfig() {
        if (this.domConfiguration == null) {
            this.domConfiguration = new DOMConfigurationImpl();
        }
        return this.domConfiguration;
    }

    public void normalizeDocument() {
        Element root = getDocumentElement();
        if (root != null) {
            ((DOMConfigurationImpl) getDomConfig()).normalize(root);
        }
    }

    /* access modifiers changed from: package-private */
    public Map<String, NodeImpl.UserData> getUserDataMap(NodeImpl node) {
        if (this.nodeToUserData == null) {
            this.nodeToUserData = new WeakHashMap<>();
        }
        Map<String, NodeImpl.UserData> userDataMap = this.nodeToUserData.get(node);
        if (userDataMap != null) {
            return userDataMap;
        }
        Map<String, NodeImpl.UserData> userDataMap2 = new HashMap<>();
        this.nodeToUserData.put(node, userDataMap2);
        return userDataMap2;
    }

    /* access modifiers changed from: package-private */
    public Map<String, NodeImpl.UserData> getUserDataMapForRead(NodeImpl node) {
        Map<String, NodeImpl.UserData> map;
        if (this.nodeToUserData == null) {
            return Collections.emptyMap();
        }
        Map<String, NodeImpl.UserData> userDataMap = this.nodeToUserData.get(node);
        if (userDataMap == null) {
            map = Collections.emptyMap();
        } else {
            map = userDataMap;
        }
        return map;
    }

    private static void notifyUserDataHandlers(short operation, Node source, NodeImpl destination) {
        if (source instanceof NodeImpl) {
            NodeImpl srcImpl = (NodeImpl) source;
            if (srcImpl.document != null) {
                for (Map.Entry<String, NodeImpl.UserData> entry : srcImpl.document.getUserDataMapForRead(srcImpl).entrySet()) {
                    NodeImpl.UserData userData = entry.getValue();
                    if (userData.handler != null) {
                        userData.handler.handle(operation, entry.getKey(), userData.value, source, destination);
                    }
                }
            }
        }
    }
}

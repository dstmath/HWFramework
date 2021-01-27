package ohos.com.sun.org.apache.xml.internal.dtm.ref.dom2dtm;

import java.util.Vector;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMManager;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMWSFilter;
import ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBaseIterators;
import ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMManagerDefault;
import ohos.com.sun.org.apache.xml.internal.dtm.ref.ExpandedNameTable;
import ohos.com.sun.org.apache.xml.internal.dtm.ref.IncrementalSAXSource;
import ohos.com.sun.org.apache.xml.internal.res.XMLMessages;
import ohos.com.sun.org.apache.xml.internal.utils.FastStringBuffer;
import ohos.com.sun.org.apache.xml.internal.utils.QName;
import ohos.com.sun.org.apache.xml.internal.utils.StringBufferPool;
import ohos.com.sun.org.apache.xml.internal.utils.SuballocatedIntVector;
import ohos.com.sun.org.apache.xml.internal.utils.TreeWalker;
import ohos.com.sun.org.apache.xml.internal.utils.XMLCharacterRecognizer;
import ohos.com.sun.org.apache.xml.internal.utils.XMLString;
import ohos.com.sun.org.apache.xml.internal.utils.XMLStringFactory;
import ohos.javax.xml.transform.SourceLocator;
import ohos.javax.xml.transform.dom.DOMSource;
import ohos.org.w3c.dom.Attr;
import ohos.org.w3c.dom.Document;
import ohos.org.w3c.dom.DocumentType;
import ohos.org.w3c.dom.Element;
import ohos.org.w3c.dom.Entity;
import ohos.org.w3c.dom.NamedNodeMap;
import ohos.org.w3c.dom.Node;
import ohos.org.xml.sax.ContentHandler;
import ohos.org.xml.sax.DTDHandler;
import ohos.org.xml.sax.EntityResolver;
import ohos.org.xml.sax.ErrorHandler;
import ohos.org.xml.sax.SAXException;
import ohos.org.xml.sax.ext.DeclHandler;
import ohos.org.xml.sax.ext.LexicalHandler;

public class DOM2DTM extends DTMDefaultBaseIterators {
    static final boolean JJK_DEBUG = false;
    static final boolean JJK_NEWCODE = true;
    static final String NAMESPACE_DECL_NS = "http://www.w3.org/XML/1998/namespace";
    private int m_last_kid = -1;
    private int m_last_parent = 0;
    protected Vector m_nodes = new Vector();
    private transient boolean m_nodesAreProcessed;
    private transient Node m_pos;
    boolean m_processedFirstElement = false;
    private transient Node m_root;
    TreeWalker m_walker = new TreeWalker(null);

    public interface CharacterNodeHandler {
        void characters(Node node) throws SAXException;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public ContentHandler getContentHandler() {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public DTDHandler getDTDHandler() {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public DeclHandler getDeclHandler() {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public EntityResolver getEntityResolver() {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public ErrorHandler getErrorHandler() {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public LexicalHandler getLexicalHandler() {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public SourceLocator getSourceLocatorFor(int i) {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public boolean needsTwoThreads() {
        return false;
    }

    public void setIncrementalSAXSource(IncrementalSAXSource incrementalSAXSource) {
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public void setProperty(String str, Object obj) {
    }

    public DOM2DTM(DTMManager dTMManager, DOMSource dOMSource, int i, DTMWSFilter dTMWSFilter, XMLStringFactory xMLStringFactory, boolean z) {
        super(dTMManager, dOMSource, i, dTMWSFilter, xMLStringFactory, z);
        Node node = dOMSource.getNode();
        this.m_root = node;
        this.m_pos = node;
        this.m_last_kid = -1;
        this.m_last_parent = -1;
        this.m_last_kid = addNode(this.m_root, this.m_last_parent, this.m_last_kid, -1);
        if (1 == this.m_root.getNodeType()) {
            NamedNodeMap attributes = this.m_root.getAttributes();
            int length = attributes == null ? 0 : attributes.getLength();
            if (length > 0) {
                int i2 = -1;
                for (int i3 = 0; i3 < length; i3++) {
                    i2 = addNode(attributes.item(i3), 0, i2, -1);
                    this.m_firstch.setElementAt(-1, i2);
                }
                this.m_nextsib.setElementAt(-1, i2);
            }
        }
        this.m_nodesAreProcessed = false;
    }

    /* access modifiers changed from: protected */
    public int addNode(Node node, int i, int i2, int i3) {
        String str;
        int i4;
        int size = this.m_nodes.size();
        if (this.m_dtmIdent.size() == (size >>> 16)) {
            try {
                if (this.m_mgr != null) {
                    DTMManagerDefault dTMManagerDefault = (DTMManagerDefault) this.m_mgr;
                    int firstFreeDTMID = dTMManagerDefault.getFirstFreeDTMID();
                    dTMManagerDefault.addDTM(this, firstFreeDTMID, size);
                    this.m_dtmIdent.addElement(firstFreeDTMID << 16);
                } else {
                    throw new ClassCastException();
                }
            } catch (ClassCastException unused) {
                error(XMLMessages.createXMLMessage("ER_NO_DTMIDS_AVAIL", null));
            }
        }
        this.m_size++;
        if (-1 == i3) {
            i3 = node.getNodeType();
        }
        if (2 == i3) {
            String nodeName = node.getNodeName();
            if (nodeName.startsWith("xmlns:") || nodeName.equals("xmlns")) {
                i3 = 13;
            }
        }
        this.m_nodes.addElement(node);
        this.m_firstch.setElementAt(-2, size);
        this.m_nextsib.setElementAt(-2, size);
        this.m_prevsib.setElementAt(i2, size);
        this.m_parent.setElementAt(i, size);
        if (!(-1 == i || i3 == 2 || i3 == 13 || -2 != this.m_firstch.elementAt(i))) {
            this.m_firstch.setElementAt(size, i);
        }
        String namespaceURI = node.getNamespaceURI();
        if (i3 == 7) {
            str = node.getNodeName();
        } else {
            str = node.getLocalName();
        }
        if ((i3 == 1 || i3 == 2) && str == null) {
            str = node.getNodeName();
        }
        ExpandedNameTable expandedNameTable = this.m_expandedNameTable;
        node.getLocalName();
        if (str != null) {
            i4 = expandedNameTable.getExpandedTypeID(namespaceURI, str, i3);
        } else {
            i4 = expandedNameTable.getExpandedTypeID(i3);
        }
        this.m_exptype.setElementAt(i4, size);
        indexNode(i4, size);
        if (-1 != i2) {
            this.m_nextsib.setElementAt(size, i2);
        }
        if (i3 == 13) {
            declareNamespaceInContext(i, size);
        }
        return size;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBase
    public int getNumberOfNodes() {
        return this.m_nodes.size();
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBase
    public boolean nextNode() {
        boolean z;
        if (this.m_nodesAreProcessed) {
            return false;
        }
        Node node = this.m_pos;
        Node node2 = null;
        Node node3 = null;
        short s = -1;
        do {
            if (node.hasChildNodes()) {
                node3 = node.getFirstChild();
                if (node3 != null && 10 == node3.getNodeType()) {
                    node3 = node3.getNextSibling();
                }
                if (5 != node.getNodeType()) {
                    this.m_last_parent = this.m_last_kid;
                    this.m_last_kid = -1;
                    if (this.m_wsfilter != null) {
                        short shouldStripSpace = this.m_wsfilter.getShouldStripSpace(makeNodeHandle(this.m_last_parent), this);
                        pushShouldStripWhitespace(3 == shouldStripSpace ? getShouldStripWhitespace() : 2 == shouldStripSpace);
                    }
                }
            } else {
                if (this.m_last_kid != -1 && this.m_firstch.elementAt(this.m_last_kid) == -2) {
                    this.m_firstch.setElementAt(-1, this.m_last_kid);
                }
                while (this.m_last_parent != -1) {
                    node3 = node.getNextSibling();
                    if (node3 != null && 10 == node3.getNodeType()) {
                        node3 = node3.getNextSibling();
                    }
                    if (node3 != null) {
                        break;
                    }
                    node = node.getParentNode();
                    if (node == null || 5 != node.getNodeType()) {
                        popShouldStripWhitespace();
                        if (this.m_last_kid == -1) {
                            this.m_firstch.setElementAt(-1, this.m_last_parent);
                        } else {
                            this.m_nextsib.setElementAt(-1, this.m_last_kid);
                        }
                        SuballocatedIntVector suballocatedIntVector = this.m_parent;
                        int i = this.m_last_parent;
                        this.m_last_kid = i;
                        this.m_last_parent = suballocatedIntVector.elementAt(i);
                    }
                }
                if (this.m_last_parent == -1) {
                    node3 = null;
                }
            }
            if (node3 != null) {
                s = node3.getNodeType();
            }
            if (5 == s) {
                node = node3;
                continue;
            }
        } while (5 == s);
        if (node3 == null) {
            this.m_nextsib.setElementAt(-1, 0);
            this.m_nodesAreProcessed = true;
            this.m_pos = null;
            return false;
        }
        short nodeType = node3.getNodeType();
        if (3 == nodeType || 4 == nodeType) {
            z = this.m_wsfilter != null && getShouldStripWhitespace();
            Node node4 = null;
            Node node5 = node3;
            while (node5 != null) {
                if (3 == node5.getNodeType()) {
                    nodeType = 3;
                }
                z &= XMLCharacterRecognizer.isWhiteSpace(node5.getNodeValue());
                node4 = node5;
                node5 = logicalNextDOMTextNode(node5);
            }
            node2 = node4;
        } else {
            z = 7 == nodeType ? node.getNodeName().toLowerCase().equals("xml") : false;
        }
        if (!z) {
            int addNode = addNode(node3, this.m_last_parent, this.m_last_kid, nodeType);
            this.m_last_kid = addNode;
            if (1 == nodeType) {
                NamedNodeMap attributes = node3.getAttributes();
                int length = attributes == null ? 0 : attributes.getLength();
                int i2 = -1;
                if (length > 0) {
                    for (int i3 = 0; i3 < length; i3++) {
                        i2 = addNode(attributes.item(i3), addNode, i2, -1);
                        this.m_firstch.setElementAt(-1, i2);
                        if (!this.m_processedFirstElement && "xmlns:xml".equals(attributes.item(i3).getNodeName())) {
                            this.m_processedFirstElement = true;
                        }
                    }
                }
                if (!this.m_processedFirstElement) {
                    i2 = addNode(new DOM2DTMdefaultNamespaceDeclarationNode((Element) node3, "xml", "http://www.w3.org/XML/1998/namespace", makeNodeHandle((i2 == -1 ? addNode : i2) + 1)), addNode, i2, -1);
                    this.m_firstch.setElementAt(-1, i2);
                    this.m_processedFirstElement = true;
                }
                if (i2 != -1) {
                    this.m_nextsib.setElementAt(-1, i2);
                }
            }
        }
        if (!(3 == nodeType || 4 == nodeType)) {
            node2 = node3;
        }
        this.m_pos = node2;
        return true;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBase, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public Node getNode(int i) {
        return (Node) this.m_nodes.elementAt(makeNodeIdentity(i));
    }

    /* access modifiers changed from: protected */
    public Node lookupNode(int i) {
        return (Node) this.m_nodes.elementAt(i);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBase
    public int getNextNodeIdentity(int i) {
        int i2 = i + 1;
        if (i2 < this.m_nodes.size() || nextNode()) {
            return i2;
        }
        return -1;
    }

    private int getHandleFromNode(Node node) {
        if (node == null) {
            return -1;
        }
        int size = this.m_nodes.size();
        int i = 0;
        while (true) {
            if (i >= size) {
                boolean nextNode = nextNode();
                int size2 = this.m_nodes.size();
                if (!nextNode && i >= size2) {
                    return -1;
                }
                size = size2;
            } else if (this.m_nodes.elementAt(i) == node) {
                return makeNodeHandle(i);
            } else {
                i++;
            }
        }
    }

    public int getHandleOfNode(Node node) {
        if (node == null) {
            return -1;
        }
        Node node2 = this.m_root;
        if (node2 != node && ((node2.getNodeType() != 9 || this.m_root != node.getOwnerDocument()) && (this.m_root.getNodeType() == 9 || this.m_root.getOwnerDocument() != node.getOwnerDocument()))) {
            return -1;
        }
        Node node3 = node;
        while (node3 != null) {
            if (node3 == this.m_root) {
                return getHandleFromNode(node);
            }
            if (node3.getNodeType() != 2) {
                node3 = node3.getParentNode();
            } else {
                node3 = ((Attr) node3).getOwnerElement();
            }
        }
        return -1;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBase, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public int getAttributeNode(int i, String str, String str2) {
        short _type;
        if (str == null) {
            str = "";
        }
        if (1 == getNodeType(i)) {
            int makeNodeIdentity = makeNodeIdentity(i);
            while (true) {
                makeNodeIdentity = getNextNodeIdentity(makeNodeIdentity);
                if (-1 == makeNodeIdentity || ((_type = _type(makeNodeIdentity)) != 2 && _type != 13)) {
                    break;
                }
                Node lookupNode = lookupNode(makeNodeIdentity);
                String namespaceURI = lookupNode.getNamespaceURI();
                if (namespaceURI == null) {
                    namespaceURI = "";
                }
                String localName = lookupNode.getLocalName();
                if (namespaceURI.equals(str) && str2.equals(localName)) {
                    return makeNodeHandle(makeNodeIdentity);
                }
            }
        }
        return -1;
    }

    /* JADX INFO: finally extract failed */
    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBase, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public XMLString getStringValue(int i) {
        short nodeType = getNodeType(i);
        Node node = getNode(i);
        String str = "";
        if (1 == nodeType || 9 == nodeType || 11 == nodeType) {
            FastStringBuffer fastStringBuffer = StringBufferPool.get();
            try {
                getNodeData(node, fastStringBuffer);
                if (fastStringBuffer.length() > 0) {
                    str = fastStringBuffer.toString();
                }
                StringBufferPool.free(fastStringBuffer);
                return this.m_xstrf.newstr(str);
            } catch (Throwable th) {
                StringBufferPool.free(fastStringBuffer);
                throw th;
            }
        } else if (3 != nodeType && 4 != nodeType) {
            return this.m_xstrf.newstr(node.getNodeValue());
        } else {
            FastStringBuffer fastStringBuffer2 = StringBufferPool.get();
            while (node != null) {
                fastStringBuffer2.append(node.getNodeValue());
                node = logicalNextDOMTextNode(node);
            }
            if (fastStringBuffer2.length() > 0) {
                str = fastStringBuffer2.toString();
            }
            StringBufferPool.free(fastStringBuffer2);
            return this.m_xstrf.newstr(str);
        }
    }

    public boolean isWhitespace(int i) {
        short nodeType = getNodeType(i);
        Node node = getNode(i);
        if (3 != nodeType && 4 != nodeType) {
            return false;
        }
        FastStringBuffer fastStringBuffer = StringBufferPool.get();
        while (node != null) {
            fastStringBuffer.append(node.getNodeValue());
            node = logicalNextDOMTextNode(node);
        }
        boolean isWhitespace = fastStringBuffer.isWhitespace(0, fastStringBuffer.length());
        StringBufferPool.free(fastStringBuffer);
        return isWhitespace;
    }

    protected static void getNodeData(Node node, FastStringBuffer fastStringBuffer) {
        short nodeType = node.getNodeType();
        if (nodeType != 1) {
            if (nodeType == 2 || nodeType == 3 || nodeType == 4) {
                fastStringBuffer.append(node.getNodeValue());
                return;
            } else if (nodeType != 7) {
                if (!(nodeType == 9 || nodeType == 11)) {
                    return;
                }
            } else {
                return;
            }
        }
        for (Node firstChild = node.getFirstChild(); firstChild != null; firstChild = firstChild.getNextSibling()) {
            getNodeData(firstChild, fastStringBuffer);
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBase, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public String getNodeName(int i) {
        return getNode(i).getNodeName();
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBase, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public String getNodeNameX(int i) {
        short nodeType = getNodeType(i);
        if (nodeType == 1 || nodeType == 2 || nodeType == 5 || nodeType == 7) {
            return getNode(i).getNodeName();
        }
        if (nodeType != 13) {
            return "";
        }
        String nodeName = getNode(i).getNodeName();
        if (nodeName.startsWith("xmlns:")) {
            nodeName = QName.getLocalPart(nodeName);
        } else if (nodeName.equals("xmlns")) {
            return "";
        }
        return nodeName;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBase, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public String getLocalName(int i) {
        int makeNodeIdentity = makeNodeIdentity(i);
        if (-1 == makeNodeIdentity) {
            return null;
        }
        Node node = (Node) this.m_nodes.elementAt(makeNodeIdentity);
        String localName = node.getLocalName();
        if (localName != null) {
            return localName;
        }
        String nodeName = node.getNodeName();
        if ('#' == nodeName.charAt(0)) {
            return "";
        }
        int indexOf = nodeName.indexOf(58);
        if (indexOf >= 0) {
            nodeName = nodeName.substring(indexOf + 1);
        }
        return nodeName;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBase, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public String getPrefix(int i) {
        String substring;
        String nodeName;
        int indexOf;
        short nodeType = getNodeType(i);
        if (nodeType == 1 || nodeType == 2) {
            String nodeName2 = getNode(i).getNodeName();
            int indexOf2 = nodeName2.indexOf(58);
            if (indexOf2 < 0) {
                return "";
            }
            substring = nodeName2.substring(0, indexOf2);
        } else if (nodeType != 13 || (indexOf = (nodeName = getNode(i).getNodeName()).indexOf(58)) < 0) {
            return "";
        } else {
            substring = nodeName.substring(indexOf + 1);
        }
        return substring;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBase, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public String getNamespaceURI(int i) {
        int makeNodeIdentity = makeNodeIdentity(i);
        if (makeNodeIdentity == -1) {
            return null;
        }
        return ((Node) this.m_nodes.elementAt(makeNodeIdentity)).getNamespaceURI();
    }

    private Node logicalNextDOMTextNode(Node node) {
        short nodeType;
        Node nextSibling = node.getNextSibling();
        if (nextSibling == null) {
            Node parentNode = node.getParentNode();
            while (parentNode != null && 5 == parentNode.getNodeType()) {
                nextSibling = parentNode.getNextSibling();
                if (nextSibling != null) {
                    break;
                }
                parentNode = parentNode.getParentNode();
            }
        }
        while (nextSibling != null && 5 == nextSibling.getNodeType()) {
            if (nextSibling.hasChildNodes()) {
                nextSibling = nextSibling.getFirstChild();
            } else {
                nextSibling = nextSibling.getNextSibling();
            }
        }
        if (nextSibling == null || 3 == (nodeType = nextSibling.getNodeType()) || 4 == nodeType) {
            return nextSibling;
        }
        return null;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBase, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public String getNodeValue(int i) {
        short s = -1;
        if (-1 != _exptype(makeNodeIdentity(i))) {
            s = getNodeType(i);
        }
        if (3 != s && 4 != s) {
            return getNode(i).getNodeValue();
        }
        Node node = getNode(i);
        Node logicalNextDOMTextNode = logicalNextDOMTextNode(node);
        if (logicalNextDOMTextNode == null) {
            return node.getNodeValue();
        }
        FastStringBuffer fastStringBuffer = StringBufferPool.get();
        fastStringBuffer.append(node.getNodeValue());
        while (logicalNextDOMTextNode != null) {
            fastStringBuffer.append(logicalNextDOMTextNode.getNodeValue());
            logicalNextDOMTextNode = logicalNextDOMTextNode(logicalNextDOMTextNode);
        }
        String fastStringBuffer2 = fastStringBuffer.length() > 0 ? fastStringBuffer.toString() : "";
        StringBufferPool.free(fastStringBuffer);
        return fastStringBuffer2;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBase, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public String getDocumentTypeDeclarationSystemIdentifier() {
        Document document;
        DocumentType doctype;
        if (this.m_root.getNodeType() == 9) {
            document = (Document) this.m_root;
        } else {
            document = this.m_root.getOwnerDocument();
        }
        if (document == null || (doctype = document.getDoctype()) == null) {
            return null;
        }
        return doctype.getSystemId();
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBase, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public String getDocumentTypeDeclarationPublicIdentifier() {
        Document document;
        DocumentType doctype;
        if (this.m_root.getNodeType() == 9) {
            document = (Document) this.m_root;
        } else {
            document = this.m_root.getOwnerDocument();
        }
        if (document == null || (doctype = document.getDoctype()) == null) {
            return null;
        }
        return doctype.getPublicId();
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBase, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public int getElementById(String str) {
        Node elementById;
        Document ownerDocument = this.m_root.getNodeType() == 9 ? (Document) this.m_root : this.m_root.getOwnerDocument();
        if (ownerDocument == null || (elementById = ownerDocument.getElementById(str)) == null) {
            return -1;
        }
        int handleFromNode = getHandleFromNode(elementById);
        if (-1 != handleFromNode) {
            return handleFromNode;
        }
        int size = this.m_nodes.size() - 1;
        do {
            size = getNextNodeIdentity(size);
            if (-1 == size) {
                return handleFromNode;
            }
        } while (getNode(size) != elementById);
        return getHandleFromNode(elementById);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBase, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public String getUnparsedEntityURI(String str) {
        DocumentType doctype;
        NamedNodeMap entities;
        Entity namedItem;
        Document ownerDocument = this.m_root.getNodeType() == 9 ? (Document) this.m_root : this.m_root.getOwnerDocument();
        if (ownerDocument == null || (doctype = ownerDocument.getDoctype()) == null || (entities = doctype.getEntities()) == null || (namedItem = entities.getNamedItem(str)) == null || namedItem.getNotationName() == null) {
            return "";
        }
        String systemId = namedItem.getSystemId();
        return systemId == null ? namedItem.getPublicId() : systemId;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBase, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public boolean isAttributeSpecified(int i) {
        if (2 == getNodeType(i)) {
            return getNode(i).getSpecified();
        }
        return false;
    }

    private static boolean isSpace(char c) {
        return XMLCharacterRecognizer.isWhiteSpace(c);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBase, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public void dispatchCharactersEvents(int i, ContentHandler contentHandler, boolean z) throws SAXException {
        if (z) {
            getStringValue(i).fixWhiteSpace(true, true, false).dispatchCharactersEvents(contentHandler);
            return;
        }
        short nodeType = getNodeType(i);
        Node node = getNode(i);
        dispatchNodeData(node, contentHandler, 0);
        if (3 == nodeType || 4 == nodeType) {
            while (true) {
                node = logicalNextDOMTextNode(node);
                if (node != null) {
                    dispatchNodeData(node, contentHandler, 0);
                } else {
                    return;
                }
            }
        }
    }

    protected static void dispatchNodeData(Node node, ContentHandler contentHandler, int i) throws SAXException {
        short nodeType = node.getNodeType();
        if (nodeType != 1) {
            if (!(nodeType == 2 || nodeType == 3 || nodeType == 4)) {
                if (nodeType == 7 || nodeType == 8) {
                    if (i != 0) {
                        return;
                    }
                } else if (!(nodeType == 9 || nodeType == 11)) {
                    return;
                }
            }
            String nodeValue = node.getNodeValue();
            if (contentHandler instanceof CharacterNodeHandler) {
                ((CharacterNodeHandler) contentHandler).characters(node);
                return;
            } else {
                contentHandler.characters(nodeValue.toCharArray(), 0, nodeValue.length());
                return;
            }
        }
        for (Node firstChild = node.getFirstChild(); firstChild != null; firstChild = firstChild.getNextSibling()) {
            dispatchNodeData(firstChild, contentHandler, i + 1);
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBase, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public void dispatchToEvents(int i, ContentHandler contentHandler) throws SAXException {
        TreeWalker treeWalker = this.m_walker;
        if (treeWalker.getContentHandler() != null) {
            treeWalker = new TreeWalker(null);
        }
        treeWalker.setContentHandler(contentHandler);
        try {
            treeWalker.traverseFragment(getNode(i));
        } finally {
            treeWalker.setContentHandler(null);
        }
    }
}

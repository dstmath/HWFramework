package org.apache.xml.dtm.ref.dom2dtm;

import java.util.Vector;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.dom.DOMSource;
import org.apache.xalan.templates.Constants;
import org.apache.xml.dtm.DTMManager;
import org.apache.xml.dtm.DTMWSFilter;
import org.apache.xml.dtm.ref.DTMDefaultBaseIterators;
import org.apache.xml.dtm.ref.DTMManagerDefault;
import org.apache.xml.dtm.ref.ExpandedNameTable;
import org.apache.xml.dtm.ref.IncrementalSAXSource;
import org.apache.xml.res.XMLErrorResources;
import org.apache.xml.res.XMLMessages;
import org.apache.xml.utils.FastStringBuffer;
import org.apache.xml.utils.QName;
import org.apache.xml.utils.StringBufferPool;
import org.apache.xml.utils.SuballocatedIntVector;
import org.apache.xml.utils.TreeWalker;
import org.apache.xml.utils.XMLCharacterRecognizer;
import org.apache.xml.utils.XMLString;
import org.apache.xml.utils.XMLStringFactory;
import org.apache.xpath.compiler.OpCodes;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Entity;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DeclHandler;
import org.xml.sax.ext.LexicalHandler;

public class DOM2DTM extends DTMDefaultBaseIterators {
    static final boolean JJK_DEBUG = false;
    static final boolean JJK_NEWCODE = true;
    static final String NAMESPACE_DECL_NS = "http://www.w3.org/XML/1998/namespace";
    private int m_last_kid;
    private int m_last_parent;
    protected Vector m_nodes;
    private transient boolean m_nodesAreProcessed;
    private transient Node m_pos;
    boolean m_processedFirstElement;
    private transient Node m_root;
    TreeWalker m_walker;

    public interface CharacterNodeHandler {
        void characters(Node node) throws SAXException;
    }

    public DOM2DTM(DTMManager mgr, DOMSource domSource, int dtmIdentity, DTMWSFilter whiteSpaceFilter, XMLStringFactory xstringfactory, boolean doIndexing) {
        super(mgr, domSource, dtmIdentity, whiteSpaceFilter, xstringfactory, doIndexing);
        this.m_last_parent = 0;
        this.m_last_kid = -1;
        this.m_processedFirstElement = JJK_DEBUG;
        this.m_nodes = new Vector();
        this.m_walker = new TreeWalker(null);
        Node node = domSource.getNode();
        this.m_root = node;
        this.m_pos = node;
        this.m_last_kid = -1;
        this.m_last_parent = -1;
        this.m_last_kid = addNode(this.m_root, this.m_last_parent, this.m_last_kid, -1);
        if ((short) 1 == this.m_root.getNodeType()) {
            NamedNodeMap attrs = this.m_root.getAttributes();
            int attrsize = attrs == null ? 0 : attrs.getLength();
            if (attrsize > 0) {
                int attrIndex = -1;
                for (int i = 0; i < attrsize; i++) {
                    attrIndex = addNode(attrs.item(i), 0, attrIndex, -1);
                    this.m_firstch.setElementAt(-1, attrIndex);
                }
                this.m_nextsib.setElementAt(-1, attrIndex);
            }
        }
        this.m_nodesAreProcessed = JJK_DEBUG;
    }

    protected int addNode(Node node, int parentIndex, int previousSibling, int forceNodeType) {
        int type;
        String localName;
        int nodeIndex = this.m_nodes.size();
        if (this.m_dtmIdent.size() == (nodeIndex >>> 16)) {
            try {
                if (this.m_mgr == null) {
                    throw new ClassCastException();
                }
                DTMManagerDefault mgrD = this.m_mgr;
                int id = mgrD.getFirstFreeDTMID();
                mgrD.addDTM(this, id, nodeIndex);
                this.m_dtmIdent.addElement(id << 16);
            } catch (ClassCastException e) {
                error(XMLMessages.createXMLMessage(XMLErrorResources.ER_NO_DTMIDS_AVAIL, null));
            }
        }
        this.m_size++;
        if (-1 == forceNodeType) {
            type = node.getNodeType();
        } else {
            type = forceNodeType;
        }
        if (2 == type) {
            String name = node.getNodeName();
            if (name.startsWith(Constants.ATTRNAME_XMLNS) || name.equals(SerializerConstants.XMLNS_PREFIX)) {
                type = 13;
            }
        }
        this.m_nodes.addElement(node);
        this.m_firstch.setElementAt(-2, nodeIndex);
        this.m_nextsib.setElementAt(-2, nodeIndex);
        this.m_prevsib.setElementAt(previousSibling, nodeIndex);
        this.m_parent.setElementAt(parentIndex, nodeIndex);
        if (!(-1 == parentIndex || type == 2 || type == 13 || -2 != this.m_firstch.elementAt(parentIndex))) {
            this.m_firstch.setElementAt(nodeIndex, parentIndex);
        }
        String nsURI = node.getNamespaceURI();
        if (type == 7) {
            localName = node.getNodeName();
        } else {
            localName = node.getLocalName();
        }
        if ((type == 1 || type == 2) && localName == null) {
            localName = node.getNodeName();
        }
        ExpandedNameTable exnt = this.m_expandedNameTable;
        int expandedNameID;
        if (node.getLocalName() != null || type == 1 || type == 2) {
            if (localName == null) {
                expandedNameID = exnt.getExpandedTypeID(nsURI, localName, type);
            } else {
                expandedNameID = exnt.getExpandedTypeID(type);
            }
            this.m_exptype.setElementAt(expandedNameID, nodeIndex);
            indexNode(expandedNameID, nodeIndex);
            if (-1 != previousSibling) {
                this.m_nextsib.setElementAt(nodeIndex, previousSibling);
            }
            if (type == 13) {
                declareNamespaceInContext(parentIndex, nodeIndex);
            }
            return nodeIndex;
        }
        if (localName == null) {
            expandedNameID = exnt.getExpandedTypeID(type);
        } else {
            expandedNameID = exnt.getExpandedTypeID(nsURI, localName, type);
        }
        this.m_exptype.setElementAt(expandedNameID, nodeIndex);
        indexNode(expandedNameID, nodeIndex);
        if (-1 != previousSibling) {
            this.m_nextsib.setElementAt(nodeIndex, previousSibling);
        }
        if (type == 13) {
            declareNamespaceInContext(parentIndex, nodeIndex);
        }
        return nodeIndex;
    }

    public int getNumberOfNodes() {
        return this.m_nodes.size();
    }

    protected boolean nextNode() {
        if (this.m_nodesAreProcessed) {
            return JJK_DEBUG;
        }
        int i;
        Node pos = this.m_pos;
        Node next = null;
        int nexttype = -1;
        do {
            if (pos.hasChildNodes()) {
                next = pos.getFirstChild();
                if (next != null && (short) 10 == next.getNodeType()) {
                    next = next.getNextSibling();
                }
                if ((short) 5 != pos.getNodeType()) {
                    this.m_last_parent = this.m_last_kid;
                    this.m_last_kid = -1;
                    if (this.m_wsfilter != null) {
                        short wsv = this.m_wsfilter.getShouldStripSpace(makeNodeHandle(this.m_last_parent), this);
                        boolean shouldStrip = (short) 3 == wsv ? getShouldStripWhitespace() : (short) 2 == wsv ? JJK_NEWCODE : JJK_DEBUG;
                        pushShouldStripWhitespace(shouldStrip);
                    }
                }
            } else {
                int i2 = this.m_last_kid;
                if (r0 != -1) {
                    if (this.m_firstch.elementAt(this.m_last_kid) == -2) {
                        this.m_firstch.setElementAt(-1, this.m_last_kid);
                    }
                }
                while (true) {
                    i2 = this.m_last_parent;
                    if (r0 == -1) {
                        break;
                    }
                    next = pos.getNextSibling();
                    if (next != null && (short) 10 == next.getNodeType()) {
                        next = next.getNextSibling();
                    }
                    if (next != null) {
                        break;
                    }
                    pos = pos.getParentNode();
                    SuballocatedIntVector suballocatedIntVector;
                    if (pos != null) {
                        if (pos != null) {
                        }
                        popShouldStripWhitespace();
                        i2 = this.m_last_kid;
                        if (r0 != -1) {
                            this.m_nextsib.setElementAt(-1, this.m_last_kid);
                        } else {
                            this.m_firstch.setElementAt(-1, this.m_last_parent);
                        }
                        suballocatedIntVector = this.m_parent;
                        i = this.m_last_parent;
                        this.m_last_kid = i;
                        this.m_last_parent = suballocatedIntVector.elementAt(i);
                    } else if (pos != null || (short) 5 != pos.getNodeType()) {
                        popShouldStripWhitespace();
                        i2 = this.m_last_kid;
                        if (r0 != -1) {
                            this.m_firstch.setElementAt(-1, this.m_last_parent);
                        } else {
                            this.m_nextsib.setElementAt(-1, this.m_last_kid);
                        }
                        suballocatedIntVector = this.m_parent;
                        i = this.m_last_parent;
                        this.m_last_kid = i;
                        this.m_last_parent = suballocatedIntVector.elementAt(i);
                    }
                }
                i2 = this.m_last_parent;
                if (r0 == -1) {
                    next = null;
                }
            }
            if (next != null) {
                nexttype = next.getNodeType();
            }
            if (5 == nexttype) {
                pos = next;
            }
        } while (5 == nexttype);
        if (next == null) {
            this.m_nextsib.setElementAt(-1, 0);
            this.m_nodesAreProcessed = JJK_NEWCODE;
            this.m_pos = null;
            return JJK_DEBUG;
        }
        boolean suppressNode = JJK_DEBUG;
        Node node = null;
        nexttype = next.getNodeType();
        if (3 == nexttype || 4 == nexttype) {
            suppressNode = this.m_wsfilter != null ? getShouldStripWhitespace() : JJK_DEBUG;
            Node n = next;
            while (n != null) {
                node = n;
                if ((short) 3 == n.getNodeType()) {
                    nexttype = 3;
                }
                suppressNode &= XMLCharacterRecognizer.isWhiteSpace(n.getNodeValue());
                n = logicalNextDOMTextNode(n);
            }
        } else if (7 == nexttype) {
            suppressNode = pos.getNodeName().toLowerCase().equals(SerializerConstants.XML_PREFIX);
        }
        if (!suppressNode) {
            int nextindex = addNode(next, this.m_last_parent, this.m_last_kid, nexttype);
            this.m_last_kid = nextindex;
            if (1 == nexttype) {
                int attrIndex = -1;
                NamedNodeMap attrs = next.getAttributes();
                int attrsize = attrs == null ? 0 : attrs.getLength();
                if (attrsize > 0) {
                    int i3 = 0;
                    while (i3 < attrsize) {
                        attrIndex = addNode(attrs.item(i3), nextindex, attrIndex, -1);
                        this.m_firstch.setElementAt(-1, attrIndex);
                        if (!this.m_processedFirstElement && "xmlns:xml".equals(attrs.item(i3).getNodeName())) {
                            this.m_processedFirstElement = JJK_NEWCODE;
                        }
                        i3++;
                    }
                }
                if (!this.m_processedFirstElement) {
                    Element element = (Element) next;
                    String str = SerializerConstants.XML_PREFIX;
                    String str2 = NAMESPACE_DECL_NS;
                    if (attrIndex == -1) {
                        i = nextindex;
                    } else {
                        i = attrIndex;
                    }
                    attrIndex = addNode(new DOM2DTMdefaultNamespaceDeclarationNode(element, str, str2, makeNodeHandle(i + 1)), nextindex, attrIndex, -1);
                    this.m_firstch.setElementAt(-1, attrIndex);
                    this.m_processedFirstElement = JJK_NEWCODE;
                }
                if (attrIndex != -1) {
                    this.m_nextsib.setElementAt(-1, attrIndex);
                }
            }
        }
        if (3 == nexttype || 4 == nexttype) {
            next = node;
        }
        this.m_pos = next;
        return JJK_NEWCODE;
    }

    public Node getNode(int nodeHandle) {
        return (Node) this.m_nodes.elementAt(makeNodeIdentity(nodeHandle));
    }

    protected Node lookupNode(int nodeIdentity) {
        return (Node) this.m_nodes.elementAt(nodeIdentity);
    }

    protected int getNextNodeIdentity(int identity) {
        identity++;
        if (identity < this.m_nodes.size() || nextNode()) {
            return identity;
        }
        return -1;
    }

    private int getHandleFromNode(Node node) {
        if (node != null) {
            int len = this.m_nodes.size();
            int i = 0;
            while (true) {
                if (i >= len) {
                    boolean isMore = nextNode();
                    len = this.m_nodes.size();
                    if (!isMore && i >= len) {
                        break;
                    }
                } else if (this.m_nodes.elementAt(i) == node) {
                    return makeNodeHandle(i);
                } else {
                    i++;
                }
            }
        }
        return -1;
    }

    public int getHandleOfNode(Node node) {
        if (node != null && (this.m_root == node || ((this.m_root.getNodeType() == (short) 9 && this.m_root == node.getOwnerDocument()) || (this.m_root.getNodeType() != (short) 9 && this.m_root.getOwnerDocument() == node.getOwnerDocument())))) {
            Node cursor = node;
            while (cursor != null) {
                if (cursor == this.m_root) {
                    return getHandleFromNode(node);
                }
                if (cursor.getNodeType() != (short) 2) {
                    cursor = cursor.getParentNode();
                } else {
                    cursor = ((Attr) cursor).getOwnerElement();
                }
            }
        }
        return -1;
    }

    public int getAttributeNode(int nodeHandle, String namespaceURI, String name) {
        if (namespaceURI == null) {
            namespaceURI = SerializerConstants.EMPTYSTRING;
        }
        if (1 == getNodeType(nodeHandle)) {
            int identity = makeNodeIdentity(nodeHandle);
            while (true) {
                identity = getNextNodeIdentity(identity);
                if (-1 == identity) {
                    break;
                }
                int type = _type(identity);
                if (type != 2 && type != 13) {
                    break;
                }
                Node node = lookupNode(identity);
                String nodeuri = node.getNamespaceURI();
                if (nodeuri == null) {
                    nodeuri = SerializerConstants.EMPTYSTRING;
                }
                String nodelocalname = node.getLocalName();
                if (nodeuri.equals(namespaceURI) && name.equals(nodelocalname)) {
                    return makeNodeHandle(identity);
                }
            }
        }
        return -1;
    }

    public XMLString getStringValue(int nodeHandle) {
        int type = getNodeType(nodeHandle);
        Node node = getNode(nodeHandle);
        FastStringBuffer buf;
        String s;
        if (1 == type || 9 == type || 11 == type) {
            buf = StringBufferPool.get();
            try {
                getNodeData(node, buf);
                s = buf.length() > 0 ? buf.toString() : SerializerConstants.EMPTYSTRING;
                StringBufferPool.free(buf);
                return this.m_xstrf.newstr(s);
            } catch (Throwable th) {
                StringBufferPool.free(buf);
            }
        } else if (3 != type && 4 != type) {
            return this.m_xstrf.newstr(node.getNodeValue());
        } else {
            buf = StringBufferPool.get();
            while (node != null) {
                buf.append(node.getNodeValue());
                node = logicalNextDOMTextNode(node);
            }
            s = buf.length() > 0 ? buf.toString() : SerializerConstants.EMPTYSTRING;
            StringBufferPool.free(buf);
            return this.m_xstrf.newstr(s);
        }
    }

    public boolean isWhitespace(int nodeHandle) {
        int type = getNodeType(nodeHandle);
        Node node = getNode(nodeHandle);
        if (3 != type && 4 != type) {
            return JJK_DEBUG;
        }
        FastStringBuffer buf = StringBufferPool.get();
        while (node != null) {
            buf.append(node.getNodeValue());
            node = logicalNextDOMTextNode(node);
        }
        boolean b = buf.isWhitespace(0, buf.length());
        StringBufferPool.free(buf);
        return b;
    }

    protected static void getNodeData(Node node, FastStringBuffer buf) {
        switch (node.getNodeType()) {
            case OpCodes.OP_XPATH /*1*/:
            case OpCodes.OP_GT /*9*/:
            case OpCodes.OP_MINUS /*11*/:
                for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
                    getNodeData(child, buf);
                }
            case OpCodes.OP_OR /*2*/:
            case OpCodes.OP_AND /*3*/:
            case OpCodes.OP_NOTEQUALS /*4*/:
                buf.append(node.getNodeValue());
            default:
        }
    }

    public String getNodeName(int nodeHandle) {
        return getNode(nodeHandle).getNodeName();
    }

    public String getNodeNameX(int nodeHandle) {
        switch (getNodeType(nodeHandle)) {
            case OpCodes.OP_XPATH /*1*/:
            case OpCodes.OP_OR /*2*/:
            case OpCodes.OP_EQUALS /*5*/:
            case OpCodes.OP_LT /*7*/:
                return getNode(nodeHandle).getNodeName();
            case OpCodes.OP_DIV /*13*/:
                String name = getNode(nodeHandle).getNodeName();
                if (name.startsWith(Constants.ATTRNAME_XMLNS)) {
                    return QName.getLocalPart(name);
                }
                if (name.equals(SerializerConstants.XMLNS_PREFIX)) {
                    return SerializerConstants.EMPTYSTRING;
                }
                return name;
            default:
                return SerializerConstants.EMPTYSTRING;
        }
    }

    public String getLocalName(int nodeHandle) {
        int id = makeNodeIdentity(nodeHandle);
        if (-1 == id) {
            return null;
        }
        Node newnode = (Node) this.m_nodes.elementAt(id);
        String newname = newnode.getLocalName();
        if (newname == null) {
            String qname = newnode.getNodeName();
            if ('#' == qname.charAt(0)) {
                newname = SerializerConstants.EMPTYSTRING;
            } else {
                int index = qname.indexOf(58);
                newname = index < 0 ? qname : qname.substring(index + 1);
            }
        }
        return newname;
    }

    public String getPrefix(int nodeHandle) {
        String qname;
        int index;
        switch (getNodeType(nodeHandle)) {
            case OpCodes.OP_XPATH /*1*/:
            case OpCodes.OP_OR /*2*/:
                qname = getNode(nodeHandle).getNodeName();
                index = qname.indexOf(58);
                return index < 0 ? SerializerConstants.EMPTYSTRING : qname.substring(0, index);
            case OpCodes.OP_DIV /*13*/:
                qname = getNode(nodeHandle).getNodeName();
                index = qname.indexOf(58);
                return index < 0 ? SerializerConstants.EMPTYSTRING : qname.substring(index + 1);
            default:
                return SerializerConstants.EMPTYSTRING;
        }
    }

    public String getNamespaceURI(int nodeHandle) {
        int id = makeNodeIdentity(nodeHandle);
        if (id == -1) {
            return null;
        }
        return ((Node) this.m_nodes.elementAt(id)).getNamespaceURI();
    }

    private Node logicalNextDOMTextNode(Node n) {
        Node p = n.getNextSibling();
        if (p == null) {
            n = n.getParentNode();
            while (n != null && (short) 5 == n.getNodeType()) {
                p = n.getNextSibling();
                if (p != null) {
                    break;
                }
                n = n.getParentNode();
            }
        }
        n = p;
        while (n != null && (short) 5 == n.getNodeType()) {
            if (n.hasChildNodes()) {
                n = n.getFirstChild();
            } else {
                n = n.getNextSibling();
            }
        }
        if (n == null) {
            return n;
        }
        int ntype = n.getNodeType();
        if (3 == ntype || 4 == ntype) {
            return n;
        }
        return null;
    }

    public String getNodeValue(int nodeHandle) {
        int type;
        if (-1 != _exptype(makeNodeIdentity(nodeHandle))) {
            type = getNodeType(nodeHandle);
        } else {
            type = -1;
        }
        if (3 != type && 4 != type) {
            return getNode(nodeHandle).getNodeValue();
        }
        Node node = getNode(nodeHandle);
        Node n = logicalNextDOMTextNode(node);
        if (n == null) {
            return node.getNodeValue();
        }
        FastStringBuffer buf = StringBufferPool.get();
        buf.append(node.getNodeValue());
        while (n != null) {
            buf.append(n.getNodeValue());
            n = logicalNextDOMTextNode(n);
        }
        String s = buf.length() > 0 ? buf.toString() : SerializerConstants.EMPTYSTRING;
        StringBufferPool.free(buf);
        return s;
    }

    public String getDocumentTypeDeclarationSystemIdentifier() {
        Document doc;
        if (this.m_root.getNodeType() == (short) 9) {
            doc = (Document) this.m_root;
        } else {
            doc = this.m_root.getOwnerDocument();
        }
        if (doc != null) {
            DocumentType dtd = doc.getDoctype();
            if (dtd != null) {
                return dtd.getSystemId();
            }
        }
        return null;
    }

    public String getDocumentTypeDeclarationPublicIdentifier() {
        Document doc;
        if (this.m_root.getNodeType() == (short) 9) {
            doc = (Document) this.m_root;
        } else {
            doc = this.m_root.getOwnerDocument();
        }
        if (doc != null) {
            DocumentType dtd = doc.getDoctype();
            if (dtd != null) {
                return dtd.getPublicId();
            }
        }
        return null;
    }

    public int getElementById(String elementId) {
        Document doc;
        if (this.m_root.getNodeType() == (short) 9) {
            doc = (Document) this.m_root;
        } else {
            doc = this.m_root.getOwnerDocument();
        }
        if (doc != null) {
            Node elem = doc.getElementById(elementId);
            if (elem != null) {
                int elemHandle = getHandleFromNode(elem);
                if (-1 == elemHandle) {
                    int identity = this.m_nodes.size() - 1;
                    do {
                        identity = getNextNodeIdentity(identity);
                        if (-1 == identity) {
                            break;
                        }
                    } while (getNode(identity) != elem);
                    elemHandle = getHandleFromNode(elem);
                }
                return elemHandle;
            }
        }
        return -1;
    }

    public String getUnparsedEntityURI(String name) {
        Document doc;
        String url = SerializerConstants.EMPTYSTRING;
        if (this.m_root.getNodeType() == (short) 9) {
            doc = (Document) this.m_root;
        } else {
            doc = this.m_root.getOwnerDocument();
        }
        if (doc != null) {
            DocumentType doctype = doc.getDoctype();
            if (doctype != null) {
                NamedNodeMap entities = doctype.getEntities();
                if (entities == null) {
                    return url;
                }
                Entity entity = (Entity) entities.getNamedItem(name);
                if (entity == null) {
                    return url;
                }
                if (entity.getNotationName() != null) {
                    url = entity.getSystemId();
                    if (url == null) {
                        url = entity.getPublicId();
                    }
                }
            }
        }
        return url;
    }

    public boolean isAttributeSpecified(int attributeHandle) {
        if (2 == getNodeType(attributeHandle)) {
            return ((Attr) getNode(attributeHandle)).getSpecified();
        }
        return JJK_DEBUG;
    }

    public void setIncrementalSAXSource(IncrementalSAXSource source) {
    }

    public ContentHandler getContentHandler() {
        return null;
    }

    public LexicalHandler getLexicalHandler() {
        return null;
    }

    public EntityResolver getEntityResolver() {
        return null;
    }

    public DTDHandler getDTDHandler() {
        return null;
    }

    public ErrorHandler getErrorHandler() {
        return null;
    }

    public DeclHandler getDeclHandler() {
        return null;
    }

    public boolean needsTwoThreads() {
        return JJK_DEBUG;
    }

    private static boolean isSpace(char ch) {
        return XMLCharacterRecognizer.isWhiteSpace(ch);
    }

    public void dispatchCharactersEvents(int nodeHandle, ContentHandler ch, boolean normalize) throws SAXException {
        if (normalize) {
            getStringValue(nodeHandle).fixWhiteSpace(JJK_NEWCODE, JJK_NEWCODE, JJK_DEBUG).dispatchCharactersEvents(ch);
            return;
        }
        int type = getNodeType(nodeHandle);
        Node node = getNode(nodeHandle);
        dispatchNodeData(node, ch, 0);
        if (3 == type || 4 == type) {
            while (true) {
                node = logicalNextDOMTextNode(node);
                if (node != null) {
                    dispatchNodeData(node, ch, 0);
                } else {
                    return;
                }
            }
        }
    }

    protected static void dispatchNodeData(Node node, ContentHandler ch, int depth) throws SAXException {
        switch (node.getNodeType()) {
            case OpCodes.OP_XPATH /*1*/:
            case OpCodes.OP_GT /*9*/:
            case OpCodes.OP_MINUS /*11*/:
                for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
                    dispatchNodeData(child, ch, depth + 1);
                }
                return;
            case OpCodes.OP_OR /*2*/:
            case OpCodes.OP_AND /*3*/:
            case OpCodes.OP_NOTEQUALS /*4*/:
                break;
            case OpCodes.OP_LT /*7*/:
            case OpCodes.OP_GTE /*8*/:
                if (depth != 0) {
                    return;
                }
                break;
            default:
                return;
        }
        String str = node.getNodeValue();
        if (ch instanceof CharacterNodeHandler) {
            ((CharacterNodeHandler) ch).characters(node);
        } else {
            ch.characters(str.toCharArray(), 0, str.length());
        }
    }

    public void dispatchToEvents(int nodeHandle, ContentHandler ch) throws SAXException {
        TreeWalker treeWalker = this.m_walker;
        if (treeWalker.getContentHandler() != null) {
            treeWalker = new TreeWalker(null);
        }
        treeWalker.setContentHandler(ch);
        try {
            treeWalker.traverseFragment(getNode(nodeHandle));
        } finally {
            treeWalker.setContentHandler(null);
        }
    }

    public void setProperty(String property, Object value) {
    }

    public SourceLocator getSourceLocatorFor(int node) {
        return null;
    }
}

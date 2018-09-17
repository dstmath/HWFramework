package org.apache.xml.dtm.ref.sax2dtm;

import java.util.Hashtable;
import java.util.Vector;
import javax.xml.transform.Source;
import javax.xml.transform.SourceLocator;
import org.apache.xalan.templates.Constants;
import org.apache.xml.dtm.DTMManager;
import org.apache.xml.dtm.DTMWSFilter;
import org.apache.xml.dtm.ref.DTMDefaultBaseIterators;
import org.apache.xml.dtm.ref.DTMManagerDefault;
import org.apache.xml.dtm.ref.DTMStringPool;
import org.apache.xml.dtm.ref.DTMTreeWalker;
import org.apache.xml.dtm.ref.IncrementalSAXSource;
import org.apache.xml.dtm.ref.IncrementalSAXSource_Filter;
import org.apache.xml.dtm.ref.NodeLocator;
import org.apache.xml.res.XMLErrorResources;
import org.apache.xml.res.XMLMessages;
import org.apache.xml.utils.FastStringBuffer;
import org.apache.xml.utils.IntStack;
import org.apache.xml.utils.IntVector;
import org.apache.xml.utils.StringVector;
import org.apache.xml.utils.SuballocatedIntVector;
import org.apache.xml.utils.SystemIDResolver;
import org.apache.xml.utils.WrappedRuntimeException;
import org.apache.xml.utils.XMLString;
import org.apache.xml.utils.XMLStringFactory;
import org.apache.xpath.compiler.PsuedoNames;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ext.DeclHandler;
import org.xml.sax.ext.LexicalHandler;

public class SAX2DTM extends DTMDefaultBaseIterators implements EntityResolver, DTDHandler, ContentHandler, ErrorHandler, DeclHandler, LexicalHandler {
    private static final boolean DEBUG = false;
    private static final int ENTITY_FIELDS_PER = 4;
    private static final int ENTITY_FIELD_NAME = 3;
    private static final int ENTITY_FIELD_NOTATIONNAME = 2;
    private static final int ENTITY_FIELD_PUBLICID = 0;
    private static final int ENTITY_FIELD_SYSTEMID = 1;
    private static final String[] m_fixednames = new String[]{null, null, null, PsuedoNames.PSEUDONAME_TEXT, "#cdata_section", null, null, null, PsuedoNames.PSEUDONAME_COMMENT, "#document", null, "#document-fragment", null};
    protected FastStringBuffer m_chars;
    protected transient int m_coalescedTextType;
    protected transient IntStack m_contextIndexes;
    protected SuballocatedIntVector m_data;
    protected SuballocatedIntVector m_dataOrQName;
    protected boolean m_endDocumentOccured;
    private Vector m_entities;
    protected Hashtable m_idAttributes;
    private IncrementalSAXSource m_incrementalSAXSource;
    protected transient boolean m_insideDTD;
    protected transient Locator m_locator;
    protected transient IntStack m_parents;
    boolean m_pastFirstElement;
    protected transient Vector m_prefixMappings;
    protected transient int m_previous;
    protected IntVector m_sourceColumn;
    protected IntVector m_sourceLine;
    protected StringVector m_sourceSystemId;
    private transient String m_systemId;
    protected int m_textPendingStart;
    protected transient int m_textType;
    protected boolean m_useSourceLocationProperty;
    protected DTMStringPool m_valuesOrPrefixes;
    protected DTMTreeWalker m_walker;

    public SAX2DTM(DTMManager mgr, Source source, int dtmIdentity, DTMWSFilter whiteSpaceFilter, XMLStringFactory xstringfactory, boolean doIndexing) {
        this(mgr, source, dtmIdentity, whiteSpaceFilter, xstringfactory, doIndexing, 512, true, false);
    }

    public SAX2DTM(DTMManager mgr, Source source, int dtmIdentity, DTMWSFilter whiteSpaceFilter, XMLStringFactory xstringfactory, boolean doIndexing, int blocksize, boolean usePrevsib, boolean newNameTable) {
        IntVector intVector;
        IntVector intVector2 = null;
        super(mgr, source, dtmIdentity, whiteSpaceFilter, xstringfactory, doIndexing, blocksize, usePrevsib, newNameTable);
        this.m_incrementalSAXSource = null;
        this.m_previous = 0;
        this.m_prefixMappings = new Vector();
        this.m_textType = 3;
        this.m_coalescedTextType = 3;
        this.m_locator = null;
        this.m_systemId = null;
        this.m_insideDTD = false;
        this.m_walker = new DTMTreeWalker();
        this.m_endDocumentOccured = false;
        this.m_idAttributes = new Hashtable();
        this.m_entities = null;
        this.m_textPendingStart = -1;
        this.m_useSourceLocationProperty = false;
        this.m_pastFirstElement = false;
        if (blocksize <= 64) {
            this.m_data = new SuballocatedIntVector(blocksize, 4);
            this.m_dataOrQName = new SuballocatedIntVector(blocksize, 4);
            this.m_valuesOrPrefixes = new DTMStringPool(16);
            this.m_chars = new FastStringBuffer(7, 10);
            this.m_contextIndexes = new IntStack(4);
            this.m_parents = new IntStack(4);
        } else {
            this.m_data = new SuballocatedIntVector(blocksize, 32);
            this.m_dataOrQName = new SuballocatedIntVector(blocksize, 32);
            this.m_valuesOrPrefixes = new DTMStringPool();
            this.m_chars = new FastStringBuffer(10, 13);
            this.m_contextIndexes = new IntStack();
            this.m_parents = new IntStack();
        }
        this.m_data.addElement(0);
        this.m_useSourceLocationProperty = mgr.getSource_location();
        this.m_sourceSystemId = this.m_useSourceLocationProperty ? new StringVector() : null;
        if (this.m_useSourceLocationProperty) {
            intVector = new IntVector();
        } else {
            intVector = null;
        }
        this.m_sourceLine = intVector;
        if (this.m_useSourceLocationProperty) {
            intVector2 = new IntVector();
        }
        this.m_sourceColumn = intVector2;
    }

    public void setUseSourceLocation(boolean useSourceLocation) {
        this.m_useSourceLocationProperty = useSourceLocation;
    }

    protected int _dataOrQName(int identity) {
        if (identity < this.m_size) {
            return this.m_dataOrQName.elementAt(identity);
        }
        while (nextNode()) {
            if (identity < this.m_size) {
                return this.m_dataOrQName.elementAt(identity);
            }
        }
        return -1;
    }

    public void clearCoRoutine() {
        clearCoRoutine(true);
    }

    public void clearCoRoutine(boolean callDoTerminate) {
        if (this.m_incrementalSAXSource != null) {
            if (callDoTerminate) {
                this.m_incrementalSAXSource.deliverMoreNodes(false);
            }
            this.m_incrementalSAXSource = null;
        }
    }

    public void setIncrementalSAXSource(IncrementalSAXSource incrementalSAXSource) {
        this.m_incrementalSAXSource = incrementalSAXSource;
        incrementalSAXSource.setContentHandler(this);
        incrementalSAXSource.setLexicalHandler(this);
        incrementalSAXSource.setDTDHandler(this);
    }

    public ContentHandler getContentHandler() {
        if (this.m_incrementalSAXSource instanceof IncrementalSAXSource_Filter) {
            return (ContentHandler) this.m_incrementalSAXSource;
        }
        return this;
    }

    public LexicalHandler getLexicalHandler() {
        if (this.m_incrementalSAXSource instanceof IncrementalSAXSource_Filter) {
            return (LexicalHandler) this.m_incrementalSAXSource;
        }
        return this;
    }

    public EntityResolver getEntityResolver() {
        return this;
    }

    public DTDHandler getDTDHandler() {
        return this;
    }

    public ErrorHandler getErrorHandler() {
        return this;
    }

    public DeclHandler getDeclHandler() {
        return this;
    }

    public boolean needsTwoThreads() {
        return this.m_incrementalSAXSource != null;
    }

    public void dispatchCharactersEvents(int nodeHandle, ContentHandler ch, boolean normalize) throws SAXException {
        int identity = makeNodeIdentity(nodeHandle);
        if (identity != -1) {
            int type = _type(identity);
            int dataIndex;
            int offset;
            int length;
            if (isTextType(type)) {
                dataIndex = this.m_dataOrQName.elementAt(identity);
                offset = this.m_data.elementAt(dataIndex);
                length = this.m_data.elementAt(dataIndex + 1);
                if (normalize) {
                    this.m_chars.sendNormalizedSAXcharacters(ch, offset, length);
                } else {
                    this.m_chars.sendSAXcharacters(ch, offset, length);
                }
            } else {
                int firstChild = _firstch(identity);
                if (-1 != firstChild) {
                    offset = -1;
                    length = 0;
                    int startNode = identity;
                    identity = firstChild;
                    do {
                        if (isTextType(_type(identity))) {
                            dataIndex = _dataOrQName(identity);
                            if (-1 == offset) {
                                offset = this.m_data.elementAt(dataIndex);
                            }
                            length += this.m_data.elementAt(dataIndex + 1);
                        }
                        identity = getNextNodeIdentity(identity);
                        if (-1 == identity) {
                            break;
                        }
                    } while (_parent(identity) >= startNode);
                    if (length > 0) {
                        if (normalize) {
                            this.m_chars.sendNormalizedSAXcharacters(ch, offset, length);
                        } else {
                            this.m_chars.sendSAXcharacters(ch, offset, length);
                        }
                    }
                } else if (type != 1) {
                    dataIndex = _dataOrQName(identity);
                    if (dataIndex < 0) {
                        dataIndex = this.m_data.elementAt((-dataIndex) + 1);
                    }
                    String str = this.m_valuesOrPrefixes.indexToString(dataIndex);
                    if (normalize) {
                        FastStringBuffer.sendNormalizedSAXcharacters(str.toCharArray(), 0, str.length(), ch);
                    } else {
                        ch.characters(str.toCharArray(), 0, str.length());
                    }
                }
            }
        }
    }

    public String getNodeName(int nodeHandle) {
        int expandedTypeID = getExpandedTypeID(nodeHandle);
        if (this.m_expandedNameTable.getNamespaceID(expandedTypeID) == 0) {
            int type = getNodeType(nodeHandle);
            if (type == 13) {
                if (this.m_expandedNameTable.getLocalName(expandedTypeID) == null) {
                    return "xmlns";
                }
                return Constants.ATTRNAME_XMLNS + this.m_expandedNameTable.getLocalName(expandedTypeID);
            } else if (this.m_expandedNameTable.getLocalNameID(expandedTypeID) == 0) {
                return m_fixednames[type];
            } else {
                return this.m_expandedNameTable.getLocalName(expandedTypeID);
            }
        }
        int qnameIndex = this.m_dataOrQName.elementAt(makeNodeIdentity(nodeHandle));
        if (qnameIndex < 0) {
            qnameIndex = this.m_data.elementAt(-qnameIndex);
        }
        return this.m_valuesOrPrefixes.indexToString(qnameIndex);
    }

    public String getNodeNameX(int nodeHandle) {
        int expandedTypeID = getExpandedTypeID(nodeHandle);
        if (this.m_expandedNameTable.getNamespaceID(expandedTypeID) == 0) {
            String name = this.m_expandedNameTable.getLocalName(expandedTypeID);
            if (name == null) {
                return "";
            }
            return name;
        }
        int qnameIndex = this.m_dataOrQName.elementAt(makeNodeIdentity(nodeHandle));
        if (qnameIndex < 0) {
            qnameIndex = this.m_data.elementAt(-qnameIndex);
        }
        return this.m_valuesOrPrefixes.indexToString(qnameIndex);
    }

    public boolean isAttributeSpecified(int attributeHandle) {
        return true;
    }

    public String getDocumentTypeDeclarationSystemIdentifier() {
        error(XMLMessages.createXMLMessage(XMLErrorResources.ER_METHOD_NOT_SUPPORTED, null));
        return null;
    }

    protected int getNextNodeIdentity(int identity) {
        identity++;
        while (identity >= this.m_size) {
            if (this.m_incrementalSAXSource == null) {
                return -1;
            }
            nextNode();
        }
        return identity;
    }

    public void dispatchToEvents(int nodeHandle, ContentHandler ch) throws SAXException {
        DTMTreeWalker treeWalker = this.m_walker;
        if (treeWalker.getcontentHandler() != null) {
            treeWalker = new DTMTreeWalker();
        }
        treeWalker.setcontentHandler(ch);
        treeWalker.setDTM(this);
        try {
            treeWalker.traverse(nodeHandle);
        } finally {
            treeWalker.setcontentHandler(null);
        }
    }

    public int getNumberOfNodes() {
        return this.m_size;
    }

    protected boolean nextNode() {
        if (this.m_incrementalSAXSource == null) {
            return false;
        }
        if (this.m_endDocumentOccured) {
            clearCoRoutine();
            return false;
        }
        Boolean gotMore = this.m_incrementalSAXSource.deliverMoreNodes(true);
        if (gotMore instanceof Boolean) {
            if (gotMore != Boolean.TRUE) {
                clearCoRoutine();
            }
            return true;
        } else if (gotMore instanceof RuntimeException) {
            throw ((RuntimeException) gotMore);
        } else if (gotMore instanceof Exception) {
            throw new WrappedRuntimeException((Exception) gotMore);
        } else {
            clearCoRoutine();
            return false;
        }
    }

    private final boolean isTextType(int type) {
        return 3 == type || 4 == type;
    }

    protected int addNode(int type, int expandedTypeID, int parentIndex, int previousSibling, int dataOrPrefix, boolean canHaveFirstChild) {
        int nodeIndex = this.m_size;
        this.m_size = nodeIndex + 1;
        if (this.m_dtmIdent.size() == (nodeIndex >>> 16)) {
            addNewDTMID(nodeIndex);
        }
        this.m_firstch.addElement(canHaveFirstChild ? -2 : -1);
        this.m_nextsib.addElement(-2);
        this.m_parent.addElement(parentIndex);
        this.m_exptype.addElement(expandedTypeID);
        this.m_dataOrQName.addElement(dataOrPrefix);
        if (this.m_prevsib != null) {
            this.m_prevsib.addElement(previousSibling);
        }
        if (-1 != previousSibling) {
            this.m_nextsib.setElementAt(nodeIndex, previousSibling);
        }
        if (this.m_locator != null && this.m_useSourceLocationProperty) {
            setSourceLocation();
        }
        switch (type) {
            case 2:
                break;
            case 13:
                declareNamespaceInContext(parentIndex, nodeIndex);
                break;
            default:
                if (-1 == previousSibling && -1 != parentIndex) {
                    this.m_firstch.setElementAt(nodeIndex, parentIndex);
                    break;
                }
        }
        return nodeIndex;
    }

    protected void addNewDTMID(int nodeIndex) {
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

    public void migrateTo(DTMManager manager) {
        super.migrateTo(manager);
        int numDTMs = this.m_dtmIdent.size();
        int dtmId = this.m_mgrDefault.getFirstFreeDTMID();
        int nodeIndex = 0;
        for (int i = 0; i < numDTMs; i++) {
            this.m_dtmIdent.setElementAt(dtmId << 16, i);
            this.m_mgrDefault.addDTM(this, dtmId, nodeIndex);
            dtmId++;
            nodeIndex += 65536;
        }
    }

    protected void setSourceLocation() {
        this.m_sourceSystemId.addElement(this.m_locator.getSystemId());
        this.m_sourceLine.addElement(this.m_locator.getLineNumber());
        this.m_sourceColumn.addElement(this.m_locator.getColumnNumber());
        if (this.m_sourceSystemId.size() != this.m_size) {
            String msg = "CODING ERROR in Source Location: " + this.m_size + " != " + this.m_sourceSystemId.size();
            System.err.println(msg);
            throw new RuntimeException(msg);
        }
    }

    public String getNodeValue(int nodeHandle) {
        int identity = makeNodeIdentity(nodeHandle);
        int type = _type(identity);
        int dataIndex;
        if (isTextType(type)) {
            dataIndex = _dataOrQName(identity);
            return this.m_chars.getString(this.m_data.elementAt(dataIndex), this.m_data.elementAt(dataIndex + 1));
        } else if (1 == type || 11 == type || 9 == type) {
            return null;
        } else {
            dataIndex = _dataOrQName(identity);
            if (dataIndex < 0) {
                dataIndex = this.m_data.elementAt((-dataIndex) + 1);
            }
            return this.m_valuesOrPrefixes.indexToString(dataIndex);
        }
    }

    public String getLocalName(int nodeHandle) {
        return this.m_expandedNameTable.getLocalName(_exptype(makeNodeIdentity(nodeHandle)));
    }

    public String getUnparsedEntityURI(String name) {
        String url = "";
        if (this.m_entities == null) {
            return url;
        }
        int n = this.m_entities.size();
        int i = 0;
        while (i < n) {
            String ename = (String) this.m_entities.elementAt(i + 3);
            if (ename == null || !ename.equals(name)) {
                i += 4;
            } else {
                if (((String) this.m_entities.elementAt(i + 2)) != null) {
                    url = (String) this.m_entities.elementAt(i + 1);
                    if (url == null) {
                        url = (String) this.m_entities.elementAt(i + 0);
                    }
                }
                return url;
            }
        }
        return url;
    }

    public String getPrefix(int nodeHandle) {
        int identity = makeNodeIdentity(nodeHandle);
        int type = _type(identity);
        int prefixIndex;
        if (1 == type) {
            prefixIndex = _dataOrQName(identity);
            if (prefixIndex == 0) {
                return "";
            }
            return getPrefix(this.m_valuesOrPrefixes.indexToString(prefixIndex), null);
        }
        if (2 == type) {
            prefixIndex = _dataOrQName(identity);
            if (prefixIndex < 0) {
                return getPrefix(this.m_valuesOrPrefixes.indexToString(this.m_data.elementAt(-prefixIndex)), null);
            }
        }
        return "";
    }

    public int getAttributeNode(int nodeHandle, String namespaceURI, String name) {
        int attrH = getFirstAttribute(nodeHandle);
        while (-1 != attrH) {
            boolean nsMatch;
            String attrNS = getNamespaceURI(attrH);
            String attrName = getLocalName(attrH);
            if (namespaceURI == attrNS) {
                nsMatch = true;
            } else if (namespaceURI != null) {
                nsMatch = namespaceURI.equals(attrNS);
            } else {
                nsMatch = false;
            }
            if (nsMatch && name.equals(attrName)) {
                return attrH;
            }
            attrH = getNextAttribute(attrH);
        }
        return -1;
    }

    public String getDocumentTypeDeclarationPublicIdentifier() {
        error(XMLMessages.createXMLMessage(XMLErrorResources.ER_METHOD_NOT_SUPPORTED, null));
        return null;
    }

    public String getNamespaceURI(int nodeHandle) {
        return this.m_expandedNameTable.getNamespace(_exptype(makeNodeIdentity(nodeHandle)));
    }

    public XMLString getStringValue(int nodeHandle) {
        int type;
        int identity = makeNodeIdentity(nodeHandle);
        if (identity == -1) {
            type = -1;
        } else {
            type = _type(identity);
        }
        int dataIndex;
        if (isTextType(type)) {
            dataIndex = _dataOrQName(identity);
            return this.m_xstrf.newstr(this.m_chars, this.m_data.elementAt(dataIndex), this.m_data.elementAt(dataIndex + 1));
        }
        int firstChild = _firstch(identity);
        if (-1 != firstChild) {
            int offset = -1;
            int length = 0;
            int startNode = identity;
            identity = firstChild;
            do {
                if (isTextType(_type(identity))) {
                    dataIndex = _dataOrQName(identity);
                    if (-1 == offset) {
                        offset = this.m_data.elementAt(dataIndex);
                    }
                    length += this.m_data.elementAt(dataIndex + 1);
                }
                identity = getNextNodeIdentity(identity);
                if (-1 == identity) {
                    break;
                }
            } while (_parent(identity) >= startNode);
            if (length > 0) {
                return this.m_xstrf.newstr(this.m_chars, offset, length);
            }
        } else if (type != 1) {
            dataIndex = _dataOrQName(identity);
            if (dataIndex < 0) {
                dataIndex = this.m_data.elementAt((-dataIndex) + 1);
            }
            return this.m_xstrf.newstr(this.m_valuesOrPrefixes.indexToString(dataIndex));
        }
        return this.m_xstrf.emptystr();
    }

    public boolean isWhitespace(int nodeHandle) {
        int type;
        int identity = makeNodeIdentity(nodeHandle);
        if (identity == -1) {
            type = -1;
        } else {
            type = _type(identity);
        }
        if (!isTextType(type)) {
            return false;
        }
        int dataIndex = _dataOrQName(identity);
        return this.m_chars.isWhitespace(this.m_data.elementAt(dataIndex), this.m_data.elementAt(dataIndex + 1));
    }

    public int getElementById(String elementId) {
        boolean isMore = true;
        while (true) {
            Integer intObj = (Integer) this.m_idAttributes.get(elementId);
            if (intObj == null) {
                if (!isMore || this.m_endDocumentOccured) {
                    break;
                }
                isMore = nextNode();
                if (intObj != null) {
                    break;
                }
            } else {
                return makeNodeHandle(intObj.intValue());
            }
        }
        return -1;
    }

    public String getPrefix(String qname, String uri) {
        int uriIndex = -1;
        int indexOfNSSep;
        if (uri != null && uri.length() > 0) {
            do {
                uriIndex = this.m_prefixMappings.indexOf(uri, uriIndex + 1);
            } while ((uriIndex & 1) == 0);
            if (uriIndex >= 0) {
                return (String) this.m_prefixMappings.elementAt(uriIndex - 1);
            }
            if (qname == null) {
                return null;
            }
            indexOfNSSep = qname.indexOf(58);
            if (qname.equals("xmlns")) {
                return "";
            }
            if (qname.startsWith(Constants.ATTRNAME_XMLNS)) {
                return qname.substring(indexOfNSSep + 1);
            }
            return indexOfNSSep > 0 ? qname.substring(0, indexOfNSSep) : null;
        } else if (qname == null) {
            return null;
        } else {
            indexOfNSSep = qname.indexOf(58);
            if (indexOfNSSep > 0) {
                if (qname.startsWith(Constants.ATTRNAME_XMLNS)) {
                    return qname.substring(indexOfNSSep + 1);
                }
                return qname.substring(0, indexOfNSSep);
            } else if (qname.equals("xmlns")) {
                return "";
            } else {
                return null;
            }
        }
    }

    public int getIdForNamespace(String uri) {
        return this.m_valuesOrPrefixes.stringToIndex(uri);
    }

    public String getNamespaceURI(String prefix) {
        String uri = "";
        int prefixIndex = this.m_contextIndexes.peek() - 1;
        if (prefix == null) {
            prefix = "";
        }
        do {
            prefixIndex = this.m_prefixMappings.indexOf(prefix, prefixIndex + 1);
            if (prefixIndex < 0) {
                break;
            }
        } while ((prefixIndex & 1) == 1);
        if (prefixIndex > -1) {
            return (String) this.m_prefixMappings.elementAt(prefixIndex + 1);
        }
        return uri;
    }

    public void setIDAttribute(String id, int elem) {
        this.m_idAttributes.put(id, new Integer(elem));
    }

    protected void charactersFlush() {
        if (this.m_textPendingStart >= 0) {
            int length = this.m_chars.size() - this.m_textPendingStart;
            boolean doStrip = false;
            if (getShouldStripWhitespace()) {
                doStrip = this.m_chars.isWhitespace(this.m_textPendingStart, length);
            }
            if (doStrip) {
                this.m_chars.setLength(this.m_textPendingStart);
            } else if (length > 0) {
                this.m_previous = addNode(this.m_coalescedTextType, this.m_expandedNameTable.getExpandedTypeID(3), this.m_parents.peek(), this.m_previous, this.m_data.size(), false);
                this.m_data.addElement(this.m_textPendingStart);
                this.m_data.addElement(length);
            }
            this.m_textPendingStart = -1;
            this.m_coalescedTextType = 3;
            this.m_textType = 3;
        }
    }

    public InputSource resolveEntity(String publicId, String systemId) throws SAXException {
        return null;
    }

    public void notationDecl(String name, String publicId, String systemId) throws SAXException {
    }

    public void unparsedEntityDecl(String name, String publicId, String systemId, String notationName) throws SAXException {
        if (this.m_entities == null) {
            this.m_entities = new Vector();
        }
        try {
            systemId = SystemIDResolver.getAbsoluteURI(systemId, getDocumentBaseURI());
            this.m_entities.addElement(publicId);
            this.m_entities.addElement(systemId);
            this.m_entities.addElement(notationName);
            this.m_entities.addElement(name);
        } catch (Exception e) {
            throw new SAXException(e);
        }
    }

    public void setDocumentLocator(Locator locator) {
        this.m_locator = locator;
        this.m_systemId = locator.getSystemId();
    }

    public void startDocument() throws SAXException {
        this.m_parents.push(addNode(9, this.m_expandedNameTable.getExpandedTypeID(9), -1, -1, 0, true));
        this.m_previous = -1;
        this.m_contextIndexes.push(this.m_prefixMappings.size());
    }

    public void endDocument() throws SAXException {
        charactersFlush();
        this.m_nextsib.setElementAt(-1, 0);
        if (this.m_firstch.elementAt(0) == -2) {
            this.m_firstch.setElementAt(-1, 0);
        }
        if (-1 != this.m_previous) {
            this.m_nextsib.setElementAt(-1, this.m_previous);
        }
        this.m_parents = null;
        this.m_prefixMappings = null;
        this.m_contextIndexes = null;
        this.m_endDocumentOccured = true;
        this.m_locator = null;
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        Object prefix2;
        if (prefix2 == null) {
            prefix2 = "";
        }
        this.m_prefixMappings.addElement(prefix2);
        this.m_prefixMappings.addElement(uri);
    }

    public void endPrefixMapping(String prefix) throws SAXException {
        if (prefix == null) {
            prefix = "";
        }
        int index = this.m_contextIndexes.peek() - 1;
        do {
            index = this.m_prefixMappings.indexOf(prefix, index + 1);
            if (index < 0) {
                break;
            }
        } while ((index & 1) == 1);
        if (index > -1) {
            this.m_prefixMappings.setElementAt("%@$#^@#", index);
            this.m_prefixMappings.setElementAt("%@$#^@#", index + 1);
        }
    }

    protected boolean declAlreadyDeclared(String prefix) {
        int startDecls = this.m_contextIndexes.peek();
        Vector prefixMappings = this.m_prefixMappings;
        int nDecls = prefixMappings.size();
        for (int i = startDecls; i < nDecls; i += 2) {
            String prefixDecl = (String) prefixMappings.elementAt(i);
            if (prefixDecl != null && prefixDecl.equals(prefix)) {
                return true;
            }
        }
        return false;
    }

    /* JADX WARNING: Removed duplicated region for block: B:31:0x0138  */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x0147  */
    /* JADX WARNING: Missing block: B:25:0x0125, code:
            if (r17.startsWith(org.apache.xalan.templates.Constants.ATTRNAME_XMLNS) != false) goto L_0x0127;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        int i;
        String prefix;
        charactersFlush();
        int exName = this.m_expandedNameTable.getExpandedTypeID(uri, localName, 1);
        int elemNode = addNode(1, exName, this.m_parents.peek(), this.m_previous, getPrefix(qName, uri) != null ? this.m_valuesOrPrefixes.stringToIndex(qName) : 0, true);
        if (this.m_indexing) {
            indexNode(exName, elemNode);
        }
        this.m_parents.push(elemNode);
        int startDecls = this.m_contextIndexes.peek();
        int nDecls = this.m_prefixMappings.size();
        int prev = -1;
        if (!this.m_pastFirstElement) {
            prev = addNode(13, this.m_expandedNameTable.getExpandedTypeID(null, "xml", 13), elemNode, -1, this.m_valuesOrPrefixes.stringToIndex("http://www.w3.org/XML/1998/namespace"), false);
            this.m_pastFirstElement = true;
        }
        for (i = startDecls; i < nDecls; i += 2) {
            prefix = (String) this.m_prefixMappings.elementAt(i);
            if (prefix != null) {
                prev = addNode(13, this.m_expandedNameTable.getExpandedTypeID(null, prefix, 13), elemNode, prev, this.m_valuesOrPrefixes.stringToIndex((String) this.m_prefixMappings.elementAt(i + 1)), false);
            }
        }
        int n = attributes.getLength();
        for (i = 0; i < n; i++) {
            int nodeType;
            int val;
            String attrUri = attributes.getURI(i);
            String attrQName = attributes.getQName(i);
            String valString = attributes.getValue(i);
            prefix = getPrefix(attrQName, attrUri);
            String attrLocalName = attributes.getLocalName(i);
            if (attrQName != null) {
                if (!attrQName.equals("xmlns")) {
                }
                if (declAlreadyDeclared(prefix)) {
                } else {
                    nodeType = 13;
                    if (valString == null) {
                        valString = "";
                    }
                    val = this.m_valuesOrPrefixes.stringToIndex(valString);
                    if (prefix != null) {
                        int prefixIndex = this.m_valuesOrPrefixes.stringToIndex(attrQName);
                        int dataIndex = this.m_data.size();
                        this.m_data.addElement(prefixIndex);
                        this.m_data.addElement(val);
                        val = -dataIndex;
                    }
                    prev = addNode(nodeType, this.m_expandedNameTable.getExpandedTypeID(attrUri, attrLocalName, nodeType), elemNode, prev, val, false);
                }
            }
            nodeType = 2;
            if (attributes.getType(i).equalsIgnoreCase("ID")) {
                setIDAttribute(valString, elemNode);
            }
            if (valString == null) {
            }
            val = this.m_valuesOrPrefixes.stringToIndex(valString);
            if (prefix != null) {
            }
            prev = addNode(nodeType, this.m_expandedNameTable.getExpandedTypeID(attrUri, attrLocalName, nodeType), elemNode, prev, val, false);
        }
        if (-1 != prev) {
            this.m_nextsib.setElementAt(-1, prev);
        }
        if (this.m_wsfilter != null) {
            short wsv = this.m_wsfilter.getShouldStripSpace(makeNodeHandle(elemNode), this);
            boolean shouldStrip = (short) 3 == wsv ? getShouldStripWhitespace() : (short) 2 == wsv;
            pushShouldStripWhitespace(shouldStrip);
        }
        this.m_previous = -1;
        this.m_contextIndexes.push(this.m_prefixMappings.size());
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        charactersFlush();
        this.m_contextIndexes.quickPop(1);
        int topContextIndex = this.m_contextIndexes.peek();
        if (topContextIndex != this.m_prefixMappings.size()) {
            this.m_prefixMappings.setSize(topContextIndex);
        }
        int lastNode = this.m_previous;
        this.m_previous = this.m_parents.pop();
        if (-1 == lastNode) {
            this.m_firstch.setElementAt(-1, this.m_previous);
        } else {
            this.m_nextsib.setElementAt(-1, lastNode);
        }
        popShouldStripWhitespace();
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        if (this.m_textPendingStart == -1) {
            this.m_textPendingStart = this.m_chars.size();
            this.m_coalescedTextType = this.m_textType;
        } else if (this.m_textType == 3) {
            this.m_coalescedTextType = 3;
        }
        this.m_chars.append(ch, start, length);
    }

    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        characters(ch, start, length);
    }

    public void processingInstruction(String target, String data) throws SAXException {
        charactersFlush();
        this.m_previous = addNode(7, this.m_expandedNameTable.getExpandedTypeID(null, target, 7), this.m_parents.peek(), this.m_previous, this.m_valuesOrPrefixes.stringToIndex(data), false);
    }

    public void skippedEntity(String name) throws SAXException {
    }

    public void warning(SAXParseException e) throws SAXException {
        System.err.println(e.getMessage());
    }

    public void error(SAXParseException e) throws SAXException {
        throw e;
    }

    public void fatalError(SAXParseException e) throws SAXException {
        throw e;
    }

    public void elementDecl(String name, String model) throws SAXException {
    }

    public void attributeDecl(String eName, String aName, String type, String valueDefault, String value) throws SAXException {
    }

    public void internalEntityDecl(String name, String value) throws SAXException {
    }

    public void externalEntityDecl(String name, String publicId, String systemId) throws SAXException {
    }

    public void startDTD(String name, String publicId, String systemId) throws SAXException {
        this.m_insideDTD = true;
    }

    public void endDTD() throws SAXException {
        this.m_insideDTD = false;
    }

    public void startEntity(String name) throws SAXException {
    }

    public void endEntity(String name) throws SAXException {
    }

    public void startCDATA() throws SAXException {
        this.m_textType = 4;
    }

    public void endCDATA() throws SAXException {
        this.m_textType = 3;
    }

    public void comment(char[] ch, int start, int length) throws SAXException {
        if (!this.m_insideDTD) {
            charactersFlush();
            this.m_previous = addNode(8, this.m_expandedNameTable.getExpandedTypeID(8), this.m_parents.peek(), this.m_previous, this.m_valuesOrPrefixes.stringToIndex(new String(ch, start, length)), false);
        }
    }

    public void setProperty(String property, Object value) {
    }

    public SourceLocator getSourceLocatorFor(int node) {
        if (this.m_useSourceLocationProperty) {
            node = makeNodeIdentity(node);
            return new NodeLocator(null, this.m_sourceSystemId.elementAt(node), this.m_sourceLine.elementAt(node), this.m_sourceColumn.elementAt(node));
        } else if (this.m_locator != null) {
            return new NodeLocator(null, this.m_locator.getSystemId(), -1, -1);
        } else {
            if (this.m_systemId != null) {
                return new NodeLocator(null, this.m_systemId, -1, -1);
            }
            return null;
        }
    }

    public String getFixedNames(int type) {
        return m_fixednames[type];
    }
}

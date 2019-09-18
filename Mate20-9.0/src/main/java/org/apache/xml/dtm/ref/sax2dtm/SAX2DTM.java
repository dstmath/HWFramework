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
    private static final String[] m_fixednames = {null, null, null, PsuedoNames.PSEUDONAME_TEXT, "#cdata_section", null, null, null, PsuedoNames.PSEUDONAME_COMMENT, "#document", null, "#document-fragment", null};
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
        super(mgr, source, dtmIdentity, whiteSpaceFilter, xstringfactory, doIndexing, blocksize, usePrevsib, newNameTable);
        IntVector intVector = null;
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
        this.m_sourceLine = this.m_useSourceLocationProperty ? new IntVector() : null;
        this.m_sourceColumn = this.m_useSourceLocationProperty ? new IntVector() : intVector;
    }

    public void setUseSourceLocation(boolean useSourceLocation) {
        this.m_useSourceLocationProperty = useSourceLocation;
    }

    /* access modifiers changed from: protected */
    public int _dataOrQName(int identity) {
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
            if (isTextType(type)) {
                int dataIndex = this.m_dataOrQName.elementAt(identity);
                int offset = this.m_data.elementAt(dataIndex);
                int length = this.m_data.elementAt(dataIndex + 1);
                if (normalize) {
                    this.m_chars.sendNormalizedSAXcharacters(ch, offset, length);
                } else {
                    this.m_chars.sendSAXcharacters(ch, offset, length);
                }
            } else {
                int firstChild = _firstch(identity);
                if (-1 != firstChild) {
                    int offset2 = -1;
                    int length2 = 0;
                    int startNode = identity;
                    int identity2 = firstChild;
                    do {
                        if (isTextType(_type(identity2))) {
                            int dataIndex2 = _dataOrQName(identity2);
                            if (-1 == offset2) {
                                offset2 = this.m_data.elementAt(dataIndex2);
                            }
                            length2 += this.m_data.elementAt(dataIndex2 + 1);
                        }
                        identity2 = getNextNodeIdentity(identity2);
                        if (-1 == identity2) {
                            break;
                        }
                    } while (_parent(identity2) >= startNode);
                    if (length2 > 0) {
                        if (normalize) {
                            this.m_chars.sendNormalizedSAXcharacters(ch, offset2, length2);
                        } else {
                            this.m_chars.sendSAXcharacters(ch, offset2, length2);
                        }
                    }
                } else if (type != 1) {
                    int dataIndex3 = _dataOrQName(identity);
                    if (dataIndex3 < 0) {
                        dataIndex3 = this.m_data.elementAt((-dataIndex3) + 1);
                    }
                    String str = this.m_valuesOrPrefixes.indexToString(dataIndex3);
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
        } else {
            int qnameIndex = this.m_dataOrQName.elementAt(makeNodeIdentity(nodeHandle));
            if (qnameIndex < 0) {
                qnameIndex = this.m_data.elementAt(-qnameIndex);
            }
            return this.m_valuesOrPrefixes.indexToString(qnameIndex);
        }
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

    /* access modifiers changed from: protected */
    public int getNextNodeIdentity(int identity) {
        int identity2 = identity + 1;
        while (identity2 >= this.m_size) {
            if (this.m_incrementalSAXSource == null) {
                return -1;
            }
            nextNode();
        }
        return identity2;
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

    /* access modifiers changed from: protected */
    public boolean nextNode() {
        if (this.m_incrementalSAXSource == null) {
            return false;
        }
        if (this.m_endDocumentOccured) {
            clearCoRoutine();
            return false;
        }
        Object gotMore = this.m_incrementalSAXSource.deliverMoreNodes(true);
        if (gotMore instanceof Boolean) {
            if (gotMore != Boolean.TRUE) {
                clearCoRoutine();
            }
            return true;
        } else if (gotMore instanceof RuntimeException) {
            throw ((RuntimeException) gotMore);
        } else if (!(gotMore instanceof Exception)) {
            clearCoRoutine();
            return false;
        } else {
            throw new WrappedRuntimeException((Exception) gotMore);
        }
    }

    private final boolean isTextType(int type) {
        return 3 == type || 4 == type;
    }

    /* access modifiers changed from: protected */
    public int addNode(int type, int expandedTypeID, int parentIndex, int previousSibling, int dataOrPrefix, boolean canHaveFirstChild) {
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
        if (type != 2) {
            if (type == 13) {
                declareNamespaceInContext(parentIndex, nodeIndex);
            } else if (-1 == previousSibling && -1 != parentIndex) {
                this.m_firstch.setElementAt(nodeIndex, parentIndex);
            }
        }
        return nodeIndex;
    }

    /* access modifiers changed from: protected */
    public void addNewDTMID(int nodeIndex) {
        try {
            if (this.m_mgr != null) {
                DTMManagerDefault mgrD = (DTMManagerDefault) this.m_mgr;
                int id = mgrD.getFirstFreeDTMID();
                mgrD.addDTM(this, id, nodeIndex);
                this.m_dtmIdent.addElement(id << 16);
                return;
            }
            throw new ClassCastException();
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

    /* access modifiers changed from: protected */
    public void setSourceLocation() {
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
        if (isTextType(type)) {
            int dataIndex = _dataOrQName(identity);
            return this.m_chars.getString(this.m_data.elementAt(dataIndex), this.m_data.elementAt(dataIndex + 1));
        } else if (1 == type || 11 == type || 9 == type) {
            return null;
        } else {
            int dataIndex2 = _dataOrQName(identity);
            if (dataIndex2 < 0) {
                dataIndex2 = this.m_data.elementAt((-dataIndex2) + 1);
            }
            return this.m_valuesOrPrefixes.indexToString(dataIndex2);
        }
    }

    public String getLocalName(int nodeHandle) {
        return this.m_expandedNameTable.getLocalName(_exptype(makeNodeIdentity(nodeHandle)));
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v2, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v2, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v4, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v4, resolved type: java.lang.String} */
    /* JADX WARNING: Multi-variable type inference failed */
    public String getUnparsedEntityURI(String name) {
        String url = "";
        if (this.m_entities == null) {
            return url;
        }
        int n = this.m_entities.size();
        int i = 0;
        while (true) {
            if (i >= n) {
                break;
            }
            String ename = (String) this.m_entities.elementAt(i + 3);
            if (ename == null || !ename.equals(name)) {
                i += 4;
            } else if (((String) this.m_entities.elementAt(i + 2)) != null) {
                url = this.m_entities.elementAt(i + 1);
                if (url == null) {
                    url = this.m_entities.elementAt(i + 0);
                }
            }
        }
        return url;
    }

    public String getPrefix(int nodeHandle) {
        int identity = makeNodeIdentity(nodeHandle);
        int type = _type(identity);
        if (1 == type) {
            int prefixIndex = _dataOrQName(identity);
            if (prefixIndex == 0) {
                return "";
            }
            return getPrefix(this.m_valuesOrPrefixes.indexToString(prefixIndex), null);
        }
        if (2 == type) {
            int prefixIndex2 = _dataOrQName(identity);
            if (prefixIndex2 < 0) {
                return getPrefix(this.m_valuesOrPrefixes.indexToString(this.m_data.elementAt(-prefixIndex2)), null);
            }
        }
        return "";
    }

    public int getAttributeNode(int nodeHandle, String namespaceURI, String name) {
        int attrH = getFirstAttribute(nodeHandle);
        while (-1 != attrH) {
            String attrNS = getNamespaceURI(attrH);
            String attrName = getLocalName(attrH);
            if ((namespaceURI == attrNS || (namespaceURI != null && namespaceURI.equals(attrNS))) && name.equals(attrName)) {
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
        if (isTextType(type)) {
            int dataIndex = _dataOrQName(identity);
            return this.m_xstrf.newstr(this.m_chars, this.m_data.elementAt(dataIndex), this.m_data.elementAt(dataIndex + 1));
        }
        int firstChild = _firstch(identity);
        if (-1 != firstChild) {
            int offset = -1;
            int length = 0;
            int startNode = identity;
            int identity2 = firstChild;
            do {
                if (isTextType(_type(identity2))) {
                    int dataIndex2 = _dataOrQName(identity2);
                    if (-1 == offset) {
                        offset = this.m_data.elementAt(dataIndex2);
                    }
                    length += this.m_data.elementAt(dataIndex2 + 1);
                }
                identity2 = getNextNodeIdentity(identity2);
                if (-1 == identity2) {
                    break;
                }
            } while (_parent(identity2) >= startNode);
            if (length > 0) {
                return this.m_xstrf.newstr(this.m_chars, offset, length);
            }
        } else if (type != 1) {
            int dataIndex3 = _dataOrQName(identity);
            if (dataIndex3 < 0) {
                dataIndex3 = this.m_data.elementAt((-dataIndex3) + 1);
            }
            return this.m_xstrf.newstr(this.m_valuesOrPrefixes.indexToString(dataIndex3));
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
        Integer intObj;
        boolean isMore = true;
        do {
            intObj = (Integer) this.m_idAttributes.get(elementId);
            if (intObj != null) {
                return makeNodeHandle(intObj.intValue());
            }
            if (isMore && !this.m_endDocumentOccured) {
                isMore = nextNode();
            }
        } while (intObj == null);
        return -1;
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v11, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v11, resolved type: java.lang.String} */
    /* JADX WARNING: Multi-variable type inference failed */
    public String getPrefix(String qname, String uri) {
        String prefix;
        int uriIndex = -1;
        String prefix2 = null;
        if (uri != null && uri.length() > 0) {
            do {
                uriIndex = this.m_prefixMappings.indexOf(uri, uriIndex + 1);
            } while ((uriIndex & 1) == 0);
            if (uriIndex >= 0) {
                prefix2 = this.m_prefixMappings.elementAt(uriIndex - 1);
            } else if (qname != null) {
                int indexOfNSSep = qname.indexOf(58);
                if (qname.equals("xmlns")) {
                    prefix = "";
                } else if (qname.startsWith(Constants.ATTRNAME_XMLNS)) {
                    prefix = qname.substring(indexOfNSSep + 1);
                } else {
                    if (indexOfNSSep > 0) {
                        prefix2 = qname.substring(0, indexOfNSSep);
                    }
                    prefix = prefix2;
                }
                prefix2 = prefix;
            } else {
                prefix2 = null;
            }
        } else if (qname != null) {
            int indexOfNSSep2 = qname.indexOf(58);
            if (indexOfNSSep2 > 0) {
                prefix2 = qname.startsWith(Constants.ATTRNAME_XMLNS) ? qname.substring(indexOfNSSep2 + 1) : qname.substring(0, indexOfNSSep2);
            } else if (qname.equals("xmlns")) {
                prefix2 = "";
            }
        }
        return prefix2;
    }

    public int getIdForNamespace(String uri) {
        return this.m_valuesOrPrefixes.stringToIndex(uri);
    }

    public String getNamespaceURI(String prefix) {
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
        return "";
    }

    public void setIDAttribute(String id, int elem) {
        this.m_idAttributes.put(id, new Integer(elem));
    }

    /* access modifiers changed from: protected */
    public void charactersFlush() {
        if (this.m_textPendingStart >= 0) {
            int length = this.m_chars.size() - this.m_textPendingStart;
            boolean doStrip = false;
            if (getShouldStripWhitespace()) {
                doStrip = this.m_chars.isWhitespace(this.m_textPendingStart, length);
            }
            if (doStrip) {
                this.m_chars.setLength(this.m_textPendingStart);
            } else if (length > 0) {
                int exName = this.m_expandedNameTable.getExpandedTypeID(3);
                int i = exName;
                this.m_previous = addNode(this.m_coalescedTextType, i, this.m_parents.peek(), this.m_previous, this.m_data.size(), false);
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
            String systemId2 = SystemIDResolver.getAbsoluteURI(systemId, getDocumentBaseURI());
            this.m_entities.addElement(publicId);
            this.m_entities.addElement(systemId2);
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
        if (prefix == null) {
            prefix = "";
        }
        this.m_prefixMappings.addElement(prefix);
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

    /* access modifiers changed from: protected */
    public boolean declAlreadyDeclared(String prefix) {
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

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        boolean z;
        boolean shouldStrip;
        int n;
        String prefix;
        int nDecls;
        boolean z2;
        int n2;
        int nDecls2;
        int nDecls3;
        String prefix2;
        String valString;
        int prefixIndex;
        int val;
        String str = uri;
        String str2 = qName;
        Attributes attributes2 = attributes;
        charactersFlush();
        int exName = this.m_expandedNameTable.getExpandedTypeID(str, localName, 1);
        String prefix3 = getPrefix(str2, str);
        int prefixIndex2 = prefix3 != null ? this.m_valuesOrPrefixes.stringToIndex(str2) : 0;
        int elemNode = addNode(1, exName, this.m_parents.peek(), this.m_previous, prefixIndex2, true);
        if (this.m_indexing) {
            indexNode(exName, elemNode);
        }
        this.m_parents.push(elemNode);
        int startDecls = this.m_contextIndexes.peek();
        int nDecls4 = this.m_prefixMappings.size();
        int prev = -1;
        if (!this.m_pastFirstElement) {
            Object obj = "http://www.w3.org/XML/1998/namespace";
            z = true;
            prev = addNode(13, this.m_expandedNameTable.getExpandedTypeID(null, "xml", 13), elemNode, -1, this.m_valuesOrPrefixes.stringToIndex("http://www.w3.org/XML/1998/namespace"), false);
            this.m_pastFirstElement = true;
        } else {
            z = true;
            int i = exName;
            String str3 = prefix3;
        }
        int prev2 = prev;
        for (int i2 = startDecls; i2 < nDecls4; i2 += 2) {
            String prefix4 = (String) this.m_prefixMappings.elementAt(i2);
            if (prefix4 != null) {
                String declURL = (String) this.m_prefixMappings.elementAt(i2 + 1);
                String str4 = declURL;
                prev2 = addNode(13, this.m_expandedNameTable.getExpandedTypeID(null, prefix4, 13), elemNode, prev2, this.m_valuesOrPrefixes.stringToIndex(declURL), false);
            }
        }
        int elemNode2 = attributes.getLength();
        int i3 = 0;
        int prev3 = prev2;
        while (true) {
            int i4 = i3;
            if (i4 >= elemNode2) {
                break;
            }
            Attributes attributes3 = attributes;
            String attrUri = attributes3.getURI(i4);
            String attrQName = attributes3.getQName(i4);
            String valString2 = attributes3.getValue(i4);
            String prefix5 = getPrefix(attrQName, attrUri);
            String prefix6 = attributes3.getLocalName(i4);
            if (attrQName == null || (!attrQName.equals("xmlns") && !attrQName.startsWith(Constants.ATTRNAME_XMLNS))) {
                nDecls2 = nDecls4;
                if (attributes3.getType(i4).equalsIgnoreCase("ID")) {
                    setIDAttribute(valString2, elemNode);
                }
                nDecls3 = 2;
            } else if (declAlreadyDeclared(prefix5)) {
                nDecls = nDecls4;
                prefix = prefix5;
                n = elemNode2;
                z2 = true;
                n2 = elemNode;
                i3 = i4 + 1;
                elemNode = n2;
                z = z2;
                nDecls4 = nDecls;
                String str5 = prefix;
                elemNode2 = n;
            } else {
                nDecls2 = nDecls4;
                nDecls3 = 13;
            }
            if (valString2 == null) {
                valString2 = "";
            }
            String valString3 = valString2;
            int val2 = this.m_valuesOrPrefixes.stringToIndex(valString3);
            if (prefix5 != null) {
                valString = valString3;
                int prefixIndex3 = this.m_valuesOrPrefixes.stringToIndex(attrQName);
                int dataIndex = this.m_data.size();
                prefix2 = prefix5;
                this.m_data.addElement(prefixIndex3);
                this.m_data.addElement(val2);
                val = -dataIndex;
                prefixIndex = prefixIndex3;
            } else {
                valString = valString3;
                prefix2 = prefix5;
                val = val2;
                prefixIndex = prefixIndex2;
            }
            String str6 = prefix6;
            String str7 = valString;
            z2 = true;
            int i5 = nDecls3;
            nDecls = nDecls2;
            n = elemNode2;
            n2 = elemNode;
            prefix = prefix2;
            prev3 = addNode(nDecls3, this.m_expandedNameTable.getExpandedTypeID(attrUri, prefix6, nDecls3), elemNode, prev3, val, false);
            prefixIndex2 = prefixIndex;
            i3 = i4 + 1;
            elemNode = n2;
            z = z2;
            nDecls4 = nDecls;
            String str52 = prefix;
            elemNode2 = n;
        }
        Attributes attributes4 = attributes;
        boolean z3 = z;
        int i6 = nDecls4;
        int i7 = elemNode2;
        int elemNode3 = elemNode;
        if (-1 != prev3) {
            this.m_nextsib.setElementAt(-1, prev3);
        }
        if (this.m_wsfilter != null) {
            short wsv = this.m_wsfilter.getShouldStripSpace(makeNodeHandle(elemNode3), this);
            if (3 == wsv) {
                shouldStrip = getShouldStripWhitespace();
            } else {
                shouldStrip = 2 == wsv ? z3 : false;
            }
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
        int exName = this.m_expandedNameTable.getExpandedTypeID(null, target, 7);
        int i = exName;
        this.m_previous = addNode(7, i, this.m_parents.peek(), this.m_previous, this.m_valuesOrPrefixes.stringToIndex(data), false);
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
            int exName = this.m_expandedNameTable.getExpandedTypeID(8);
            int dataIndex = this.m_valuesOrPrefixes.stringToIndex(new String(ch, start, length));
            this.m_previous = addNode(8, exName, this.m_parents.peek(), this.m_previous, dataIndex, false);
        }
    }

    public void setProperty(String property, Object value) {
    }

    public SourceLocator getSourceLocatorFor(int node) {
        if (this.m_useSourceLocationProperty) {
            int node2 = makeNodeIdentity(node);
            return new NodeLocator(null, this.m_sourceSystemId.elementAt(node2), this.m_sourceLine.elementAt(node2), this.m_sourceColumn.elementAt(node2));
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

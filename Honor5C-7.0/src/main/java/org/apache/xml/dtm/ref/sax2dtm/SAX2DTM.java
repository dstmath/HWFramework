package org.apache.xml.dtm.ref.sax2dtm;

import java.util.Hashtable;
import java.util.Vector;
import javax.xml.transform.Source;
import javax.xml.transform.SourceLocator;
import org.apache.xalan.templates.Constants;
import org.apache.xml.dtm.DTMManager;
import org.apache.xml.dtm.DTMWSFilter;
import org.apache.xml.dtm.ref.DTMDefaultBase;
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
import org.apache.xpath.axes.WalkerFactory;
import org.apache.xpath.compiler.OpCodes;
import org.apache.xpath.jaxp.JAXPPrefixResolver;
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
    private static final String[] m_fixednames = null;
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: org.apache.xml.dtm.ref.sax2dtm.SAX2DTM.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: org.apache.xml.dtm.ref.sax2dtm.SAX2DTM.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.xml.dtm.ref.sax2dtm.SAX2DTM.<clinit>():void");
    }

    public SAX2DTM(DTMManager mgr, Source source, int dtmIdentity, DTMWSFilter whiteSpaceFilter, XMLStringFactory xstringfactory, boolean doIndexing) {
        this(mgr, source, dtmIdentity, whiteSpaceFilter, xstringfactory, doIndexing, DTMDefaultBase.DEFAULT_BLOCKSIZE, true, DEBUG);
    }

    public SAX2DTM(DTMManager mgr, Source source, int dtmIdentity, DTMWSFilter whiteSpaceFilter, XMLStringFactory xstringfactory, boolean doIndexing, int blocksize, boolean usePrevsib, boolean newNameTable) {
        IntVector intVector;
        IntVector intVector2 = null;
        super(mgr, source, dtmIdentity, whiteSpaceFilter, xstringfactory, doIndexing, blocksize, usePrevsib, newNameTable);
        this.m_incrementalSAXSource = null;
        this.m_previous = ENTITY_FIELD_PUBLICID;
        this.m_prefixMappings = new Vector();
        this.m_textType = ENTITY_FIELD_NAME;
        this.m_coalescedTextType = ENTITY_FIELD_NAME;
        this.m_locator = null;
        this.m_systemId = null;
        this.m_insideDTD = DEBUG;
        this.m_walker = new DTMTreeWalker();
        this.m_endDocumentOccured = DEBUG;
        this.m_idAttributes = new Hashtable();
        this.m_entities = null;
        this.m_textPendingStart = -1;
        this.m_useSourceLocationProperty = DEBUG;
        this.m_pastFirstElement = DEBUG;
        if (blocksize <= 64) {
            this.m_data = new SuballocatedIntVector(blocksize, ENTITY_FIELDS_PER);
            this.m_dataOrQName = new SuballocatedIntVector(blocksize, ENTITY_FIELDS_PER);
            this.m_valuesOrPrefixes = new DTMStringPool(16);
            this.m_chars = new FastStringBuffer(7, 10);
            this.m_contextIndexes = new IntStack((int) ENTITY_FIELDS_PER);
            this.m_parents = new IntStack((int) ENTITY_FIELDS_PER);
        } else {
            this.m_data = new SuballocatedIntVector(blocksize, 32);
            this.m_dataOrQName = new SuballocatedIntVector(blocksize, 32);
            this.m_valuesOrPrefixes = new DTMStringPool();
            this.m_chars = new FastStringBuffer(10, 13);
            this.m_contextIndexes = new IntStack();
            this.m_parents = new IntStack();
        }
        this.m_data.addElement(ENTITY_FIELD_PUBLICID);
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
                this.m_incrementalSAXSource.deliverMoreNodes(DEBUG);
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
        return this.m_incrementalSAXSource != null ? true : DEBUG;
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
                length = this.m_data.elementAt(dataIndex + ENTITY_FIELD_SYSTEMID);
                if (normalize) {
                    this.m_chars.sendNormalizedSAXcharacters(ch, offset, length);
                } else {
                    this.m_chars.sendSAXcharacters(ch, offset, length);
                }
            } else {
                int firstChild = _firstch(identity);
                if (-1 != firstChild) {
                    offset = -1;
                    length = ENTITY_FIELD_PUBLICID;
                    int startNode = identity;
                    identity = firstChild;
                    do {
                        if (isTextType(_type(identity))) {
                            dataIndex = _dataOrQName(identity);
                            if (-1 == offset) {
                                offset = this.m_data.elementAt(dataIndex);
                            }
                            length += this.m_data.elementAt(dataIndex + ENTITY_FIELD_SYSTEMID);
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
                } else if (type != ENTITY_FIELD_SYSTEMID) {
                    dataIndex = _dataOrQName(identity);
                    if (dataIndex < 0) {
                        dataIndex = this.m_data.elementAt((-dataIndex) + ENTITY_FIELD_SYSTEMID);
                    }
                    String str = this.m_valuesOrPrefixes.indexToString(dataIndex);
                    if (normalize) {
                        FastStringBuffer.sendNormalizedSAXcharacters(str.toCharArray(), ENTITY_FIELD_PUBLICID, str.length(), ch);
                    } else {
                        ch.characters(str.toCharArray(), ENTITY_FIELD_PUBLICID, str.length());
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
                    return SerializerConstants.XMLNS_PREFIX;
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
                return SerializerConstants.EMPTYSTRING;
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
        identity += ENTITY_FIELD_SYSTEMID;
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
            return DEBUG;
        }
        if (this.m_endDocumentOccured) {
            clearCoRoutine();
            return DEBUG;
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
            return DEBUG;
        }
    }

    private final boolean isTextType(int type) {
        return (ENTITY_FIELD_NAME == type || ENTITY_FIELDS_PER == type) ? true : DEBUG;
    }

    protected int addNode(int type, int expandedTypeID, int parentIndex, int previousSibling, int dataOrPrefix, boolean canHaveFirstChild) {
        int nodeIndex = this.m_size;
        this.m_size = nodeIndex + ENTITY_FIELD_SYSTEMID;
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
            case ENTITY_FIELD_NOTATIONNAME /*2*/:
                break;
            case OpCodes.OP_DIV /*13*/:
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
        int nodeIndex = ENTITY_FIELD_PUBLICID;
        for (int i = ENTITY_FIELD_PUBLICID; i < numDTMs; i += ENTITY_FIELD_SYSTEMID) {
            this.m_dtmIdent.setElementAt(dtmId << 16, i);
            this.m_mgrDefault.addDTM(this, dtmId, nodeIndex);
            dtmId += ENTITY_FIELD_SYSTEMID;
            nodeIndex += WalkerFactory.BIT_CHILD;
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
            return this.m_chars.getString(this.m_data.elementAt(dataIndex), this.m_data.elementAt(dataIndex + ENTITY_FIELD_SYSTEMID));
        } else if (ENTITY_FIELD_SYSTEMID == type || 11 == type || 9 == type) {
            return null;
        } else {
            dataIndex = _dataOrQName(identity);
            if (dataIndex < 0) {
                dataIndex = this.m_data.elementAt((-dataIndex) + ENTITY_FIELD_SYSTEMID);
            }
            return this.m_valuesOrPrefixes.indexToString(dataIndex);
        }
    }

    public String getLocalName(int nodeHandle) {
        return this.m_expandedNameTable.getLocalName(_exptype(makeNodeIdentity(nodeHandle)));
    }

    public String getUnparsedEntityURI(String name) {
        String url = SerializerConstants.EMPTYSTRING;
        if (this.m_entities == null) {
            return url;
        }
        int n = this.m_entities.size();
        int i = ENTITY_FIELD_PUBLICID;
        while (i < n) {
            String ename = (String) this.m_entities.elementAt(i + ENTITY_FIELD_NAME);
            if (ename == null || !ename.equals(name)) {
                i += ENTITY_FIELDS_PER;
            } else {
                if (((String) this.m_entities.elementAt(i + ENTITY_FIELD_NOTATIONNAME)) != null) {
                    url = (String) this.m_entities.elementAt(i + ENTITY_FIELD_SYSTEMID);
                    if (url == null) {
                        url = (String) this.m_entities.elementAt(i + ENTITY_FIELD_PUBLICID);
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
        if (ENTITY_FIELD_SYSTEMID == type) {
            prefixIndex = _dataOrQName(identity);
            if (prefixIndex == 0) {
                return SerializerConstants.EMPTYSTRING;
            }
            return getPrefix(this.m_valuesOrPrefixes.indexToString(prefixIndex), null);
        }
        if (ENTITY_FIELD_NOTATIONNAME == type) {
            prefixIndex = _dataOrQName(identity);
            if (prefixIndex < 0) {
                return getPrefix(this.m_valuesOrPrefixes.indexToString(this.m_data.elementAt(-prefixIndex)), null);
            }
        }
        return SerializerConstants.EMPTYSTRING;
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
                nsMatch = DEBUG;
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
        if (isTextType(type)) {
            int dataIndex = _dataOrQName(identity);
            return this.m_xstrf.newstr(this.m_chars, this.m_data.elementAt(dataIndex), this.m_data.elementAt(dataIndex + ENTITY_FIELD_SYSTEMID));
        }
        int firstChild = _firstch(identity);
        if (-1 != firstChild) {
            int offset = -1;
            int length = ENTITY_FIELD_PUBLICID;
            int startNode = identity;
            identity = firstChild;
            do {
                if (isTextType(_type(identity))) {
                    dataIndex = _dataOrQName(identity);
                    if (-1 == offset) {
                        offset = this.m_data.elementAt(dataIndex);
                    }
                    length += this.m_data.elementAt(dataIndex + ENTITY_FIELD_SYSTEMID);
                }
                identity = getNextNodeIdentity(identity);
                if (-1 == identity) {
                    break;
                }
            } while (_parent(identity) >= startNode);
            if (length > 0) {
                return this.m_xstrf.newstr(this.m_chars, offset, length);
            }
        } else if (type != ENTITY_FIELD_SYSTEMID) {
            dataIndex = _dataOrQName(identity);
            if (dataIndex < 0) {
                dataIndex = this.m_data.elementAt((-dataIndex) + ENTITY_FIELD_SYSTEMID);
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
            return DEBUG;
        }
        int dataIndex = _dataOrQName(identity);
        return this.m_chars.isWhitespace(this.m_data.elementAt(dataIndex), this.m_data.elementAt(dataIndex + ENTITY_FIELD_SYSTEMID));
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int getElementById(String elementId) {
        boolean z = true;
        while (true) {
            Integer intObj = (Integer) this.m_idAttributes.get(elementId);
            if (intObj == null) {
                if (z && !this.m_endDocumentOccured) {
                    z = nextNode();
                    if (intObj != null) {
                        break;
                    }
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
                uriIndex = this.m_prefixMappings.indexOf(uri, uriIndex + ENTITY_FIELD_SYSTEMID);
            } while ((uriIndex & ENTITY_FIELD_SYSTEMID) == 0);
            if (uriIndex >= 0) {
                return (String) this.m_prefixMappings.elementAt(uriIndex - 1);
            }
            if (qname == null) {
                return null;
            }
            indexOfNSSep = qname.indexOf(58);
            if (qname.equals(SerializerConstants.XMLNS_PREFIX)) {
                return SerializerConstants.EMPTYSTRING;
            }
            if (qname.startsWith(Constants.ATTRNAME_XMLNS)) {
                return qname.substring(indexOfNSSep + ENTITY_FIELD_SYSTEMID);
            }
            return indexOfNSSep > 0 ? qname.substring(ENTITY_FIELD_PUBLICID, indexOfNSSep) : null;
        } else if (qname == null) {
            return null;
        } else {
            indexOfNSSep = qname.indexOf(58);
            if (indexOfNSSep > 0) {
                if (qname.startsWith(Constants.ATTRNAME_XMLNS)) {
                    return qname.substring(indexOfNSSep + ENTITY_FIELD_SYSTEMID);
                }
                return qname.substring(ENTITY_FIELD_PUBLICID, indexOfNSSep);
            } else if (qname.equals(SerializerConstants.XMLNS_PREFIX)) {
                return SerializerConstants.EMPTYSTRING;
            } else {
                return null;
            }
        }
    }

    public int getIdForNamespace(String uri) {
        return this.m_valuesOrPrefixes.stringToIndex(uri);
    }

    public String getNamespaceURI(String prefix) {
        String uri = SerializerConstants.EMPTYSTRING;
        int prefixIndex = this.m_contextIndexes.peek() - 1;
        if (prefix == null) {
            prefix = SerializerConstants.EMPTYSTRING;
        }
        do {
            prefixIndex = this.m_prefixMappings.indexOf(prefix, prefixIndex + ENTITY_FIELD_SYSTEMID);
            if (prefixIndex < 0) {
                break;
            }
        } while ((prefixIndex & ENTITY_FIELD_SYSTEMID) == ENTITY_FIELD_SYSTEMID);
        if (prefixIndex > -1) {
            return (String) this.m_prefixMappings.elementAt(prefixIndex + ENTITY_FIELD_SYSTEMID);
        }
        return uri;
    }

    public void setIDAttribute(String id, int elem) {
        this.m_idAttributes.put(id, new Integer(elem));
    }

    protected void charactersFlush() {
        if (this.m_textPendingStart >= 0) {
            int length = this.m_chars.size() - this.m_textPendingStart;
            boolean doStrip = DEBUG;
            if (getShouldStripWhitespace()) {
                doStrip = this.m_chars.isWhitespace(this.m_textPendingStart, length);
            }
            if (doStrip) {
                this.m_chars.setLength(this.m_textPendingStart);
            } else if (length > 0) {
                this.m_previous = addNode(this.m_coalescedTextType, this.m_expandedNameTable.getExpandedTypeID(ENTITY_FIELD_NAME), this.m_parents.peek(), this.m_previous, this.m_data.size(), DEBUG);
                this.m_data.addElement(this.m_textPendingStart);
                this.m_data.addElement(length);
            }
            this.m_textPendingStart = -1;
            this.m_coalescedTextType = ENTITY_FIELD_NAME;
            this.m_textType = ENTITY_FIELD_NAME;
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
        this.m_parents.push(addNode(9, this.m_expandedNameTable.getExpandedTypeID(9), -1, -1, ENTITY_FIELD_PUBLICID, true));
        this.m_previous = -1;
        this.m_contextIndexes.push(this.m_prefixMappings.size());
    }

    public void endDocument() throws SAXException {
        charactersFlush();
        this.m_nextsib.setElementAt(-1, ENTITY_FIELD_PUBLICID);
        if (this.m_firstch.elementAt(ENTITY_FIELD_PUBLICID) == -2) {
            this.m_firstch.setElementAt(-1, ENTITY_FIELD_PUBLICID);
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
            prefix = SerializerConstants.EMPTYSTRING;
        }
        this.m_prefixMappings.addElement(prefix);
        this.m_prefixMappings.addElement(uri);
    }

    public void endPrefixMapping(String prefix) throws SAXException {
        if (prefix == null) {
            prefix = SerializerConstants.EMPTYSTRING;
        }
        int index = this.m_contextIndexes.peek() - 1;
        do {
            index = this.m_prefixMappings.indexOf(prefix, index + ENTITY_FIELD_SYSTEMID);
            if (index < 0) {
                break;
            }
        } while ((index & ENTITY_FIELD_SYSTEMID) == ENTITY_FIELD_SYSTEMID);
        if (index > -1) {
            this.m_prefixMappings.setElementAt("%@$#^@#", index);
            this.m_prefixMappings.setElementAt("%@$#^@#", index + ENTITY_FIELD_SYSTEMID);
        }
    }

    protected boolean declAlreadyDeclared(String prefix) {
        int startDecls = this.m_contextIndexes.peek();
        Vector prefixMappings = this.m_prefixMappings;
        int nDecls = prefixMappings.size();
        for (int i = startDecls; i < nDecls; i += ENTITY_FIELD_NOTATIONNAME) {
            String prefixDecl = (String) prefixMappings.elementAt(i);
            if (prefixDecl != null && prefixDecl.equals(prefix)) {
                return true;
            }
        }
        return DEBUG;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        int i;
        charactersFlush();
        int exName = this.m_expandedNameTable.getExpandedTypeID(uri, localName, ENTITY_FIELD_SYSTEMID);
        int elemNode = addNode(ENTITY_FIELD_SYSTEMID, exName, this.m_parents.peek(), this.m_previous, getPrefix(qName, uri) != null ? this.m_valuesOrPrefixes.stringToIndex(qName) : ENTITY_FIELD_PUBLICID, true);
        if (this.m_indexing) {
            indexNode(exName, elemNode);
        }
        this.m_parents.push(elemNode);
        int startDecls = this.m_contextIndexes.peek();
        int nDecls = this.m_prefixMappings.size();
        int prev = -1;
        if (!this.m_pastFirstElement) {
            prev = addNode(13, this.m_expandedNameTable.getExpandedTypeID(null, SerializerConstants.XML_PREFIX, 13), elemNode, -1, this.m_valuesOrPrefixes.stringToIndex(JAXPPrefixResolver.S_XMLNAMESPACEURI), DEBUG);
            this.m_pastFirstElement = true;
        }
        for (i = startDecls; i < nDecls; i += ENTITY_FIELD_NOTATIONNAME) {
            String prefix = (String) this.m_prefixMappings.elementAt(i);
            if (prefix != null) {
                prev = addNode(13, this.m_expandedNameTable.getExpandedTypeID(null, prefix, 13), elemNode, prev, this.m_valuesOrPrefixes.stringToIndex((String) this.m_prefixMappings.elementAt(i + ENTITY_FIELD_SYSTEMID)), DEBUG);
            }
        }
        int n = attributes.getLength();
        for (i = ENTITY_FIELD_PUBLICID; i < n; i += ENTITY_FIELD_SYSTEMID) {
            int nodeType;
            int val;
            String attrUri = attributes.getURI(i);
            String attrQName = attributes.getQName(i);
            String valString = attributes.getValue(i);
            prefix = getPrefix(attrQName, attrUri);
            String attrLocalName = attributes.getLocalName(i);
            if (attrQName != null) {
                if (!attrQName.equals(SerializerConstants.XMLNS_PREFIX)) {
                }
                if (declAlreadyDeclared(prefix)) {
                } else {
                    nodeType = 13;
                    if (valString == null) {
                        valString = SerializerConstants.EMPTYSTRING;
                    }
                    val = this.m_valuesOrPrefixes.stringToIndex(valString);
                    if (prefix != null) {
                        int prefixIndex = this.m_valuesOrPrefixes.stringToIndex(attrQName);
                        int dataIndex = this.m_data.size();
                        this.m_data.addElement(prefixIndex);
                        this.m_data.addElement(val);
                        val = -dataIndex;
                    }
                    prev = addNode(nodeType, this.m_expandedNameTable.getExpandedTypeID(attrUri, attrLocalName, nodeType), elemNode, prev, val, DEBUG);
                }
            }
            nodeType = ENTITY_FIELD_NOTATIONNAME;
            if (attributes.getType(i).equalsIgnoreCase("ID")) {
                setIDAttribute(valString, elemNode);
            }
            if (valString == null) {
                valString = SerializerConstants.EMPTYSTRING;
            }
            val = this.m_valuesOrPrefixes.stringToIndex(valString);
            if (prefix != null) {
                int prefixIndex2 = this.m_valuesOrPrefixes.stringToIndex(attrQName);
                int dataIndex2 = this.m_data.size();
                this.m_data.addElement(prefixIndex2);
                this.m_data.addElement(val);
                val = -dataIndex2;
            }
            prev = addNode(nodeType, this.m_expandedNameTable.getExpandedTypeID(attrUri, attrLocalName, nodeType), elemNode, prev, val, DEBUG);
        }
        if (-1 != prev) {
            this.m_nextsib.setElementAt(-1, prev);
        }
        if (this.m_wsfilter != null) {
            short wsv = this.m_wsfilter.getShouldStripSpace(makeNodeHandle(elemNode), this);
            boolean shouldStrip = ENTITY_FIELD_NAME == wsv ? getShouldStripWhitespace() : ENTITY_FIELD_NOTATIONNAME == wsv ? true : DEBUG;
            pushShouldStripWhitespace(shouldStrip);
        }
        this.m_previous = -1;
        this.m_contextIndexes.push(this.m_prefixMappings.size());
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        charactersFlush();
        this.m_contextIndexes.quickPop(ENTITY_FIELD_SYSTEMID);
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
        } else if (this.m_textType == ENTITY_FIELD_NAME) {
            this.m_coalescedTextType = ENTITY_FIELD_NAME;
        }
        this.m_chars.append(ch, start, length);
    }

    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        characters(ch, start, length);
    }

    public void processingInstruction(String target, String data) throws SAXException {
        charactersFlush();
        this.m_previous = addNode(7, this.m_expandedNameTable.getExpandedTypeID(null, target, 7), this.m_parents.peek(), this.m_previous, this.m_valuesOrPrefixes.stringToIndex(data), DEBUG);
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
        this.m_insideDTD = DEBUG;
    }

    public void startEntity(String name) throws SAXException {
    }

    public void endEntity(String name) throws SAXException {
    }

    public void startCDATA() throws SAXException {
        this.m_textType = ENTITY_FIELDS_PER;
    }

    public void endCDATA() throws SAXException {
        this.m_textType = ENTITY_FIELD_NAME;
    }

    public void comment(char[] ch, int start, int length) throws SAXException {
        if (!this.m_insideDTD) {
            charactersFlush();
            this.m_previous = addNode(8, this.m_expandedNameTable.getExpandedTypeID(8), this.m_parents.peek(), this.m_previous, this.m_valuesOrPrefixes.stringToIndex(new String(ch, start, length)), DEBUG);
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

package ohos.com.sun.org.apache.xml.internal.dtm.ref.sax2dtm;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaSymbols;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMManager;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMWSFilter;
import ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBaseIterators;
import ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMManagerDefault;
import ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMStringPool;
import ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMTreeWalker;
import ohos.com.sun.org.apache.xml.internal.dtm.ref.IncrementalSAXSource;
import ohos.com.sun.org.apache.xml.internal.dtm.ref.NodeLocator;
import ohos.com.sun.org.apache.xml.internal.res.XMLMessages;
import ohos.com.sun.org.apache.xml.internal.utils.FastStringBuffer;
import ohos.com.sun.org.apache.xml.internal.utils.IntStack;
import ohos.com.sun.org.apache.xml.internal.utils.IntVector;
import ohos.com.sun.org.apache.xml.internal.utils.StringVector;
import ohos.com.sun.org.apache.xml.internal.utils.SuballocatedIntVector;
import ohos.com.sun.org.apache.xml.internal.utils.SystemIDResolver;
import ohos.com.sun.org.apache.xml.internal.utils.WrappedRuntimeException;
import ohos.com.sun.org.apache.xml.internal.utils.XMLString;
import ohos.com.sun.org.apache.xml.internal.utils.XMLStringFactory;
import ohos.com.sun.org.apache.xpath.internal.compiler.PsuedoNames;
import ohos.javax.xml.transform.Source;
import ohos.javax.xml.transform.SourceLocator;
import ohos.org.xml.sax.Attributes;
import ohos.org.xml.sax.ContentHandler;
import ohos.org.xml.sax.DTDHandler;
import ohos.org.xml.sax.EntityResolver;
import ohos.org.xml.sax.ErrorHandler;
import ohos.org.xml.sax.InputSource;
import ohos.org.xml.sax.Locator;
import ohos.org.xml.sax.SAXException;
import ohos.org.xml.sax.SAXParseException;
import ohos.org.xml.sax.ext.DeclHandler;
import ohos.org.xml.sax.ext.LexicalHandler;

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
    protected Map<String, Integer> m_idAttributes;
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

    private final boolean isTextType(int i) {
        return 3 == i || 4 == i;
    }

    public void attributeDecl(String str, String str2, String str3, String str4, String str5) throws SAXException {
    }

    public void elementDecl(String str, String str2) throws SAXException {
    }

    public void endEntity(String str) throws SAXException {
    }

    public void externalEntityDecl(String str, String str2, String str3) throws SAXException {
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public DTDHandler getDTDHandler() {
        return this;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public DeclHandler getDeclHandler() {
        return this;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public EntityResolver getEntityResolver() {
        return this;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public ErrorHandler getErrorHandler() {
        return this;
    }

    public void internalEntityDecl(String str, String str2) throws SAXException {
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBase, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public boolean isAttributeSpecified(int i) {
        return true;
    }

    public void notationDecl(String str, String str2, String str3) throws SAXException {
    }

    public InputSource resolveEntity(String str, String str2) throws SAXException {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public void setProperty(String str, Object obj) {
    }

    public void skippedEntity(String str) throws SAXException {
    }

    public void startEntity(String str) throws SAXException {
    }

    public SAX2DTM(DTMManager dTMManager, Source source, int i, DTMWSFilter dTMWSFilter, XMLStringFactory xMLStringFactory, boolean z) {
        this(dTMManager, source, i, dTMWSFilter, xMLStringFactory, z, 512, true, false);
    }

    public SAX2DTM(DTMManager dTMManager, Source source, int i, DTMWSFilter dTMWSFilter, XMLStringFactory xMLStringFactory, boolean z, int i2, boolean z2, boolean z3) {
        super(dTMManager, source, i, dTMWSFilter, xMLStringFactory, z, i2, z2, z3);
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
        this.m_idAttributes = new HashMap();
        this.m_entities = null;
        this.m_textPendingStart = -1;
        this.m_useSourceLocationProperty = false;
        this.m_pastFirstElement = false;
        if (i2 <= 64) {
            this.m_data = new SuballocatedIntVector(i2, 4);
            this.m_dataOrQName = new SuballocatedIntVector(i2, 4);
            this.m_valuesOrPrefixes = new DTMStringPool(16);
            this.m_chars = new FastStringBuffer(7, 10);
            this.m_contextIndexes = new IntStack(4);
            this.m_parents = new IntStack(4);
        } else {
            this.m_data = new SuballocatedIntVector(i2, 32);
            this.m_dataOrQName = new SuballocatedIntVector(i2, 32);
            this.m_valuesOrPrefixes = new DTMStringPool();
            this.m_chars = new FastStringBuffer(10, 13);
            this.m_contextIndexes = new IntStack();
            this.m_parents = new IntStack();
        }
        this.m_data.addElement(0);
        this.m_useSourceLocationProperty = dTMManager.getSource_location();
        this.m_sourceSystemId = this.m_useSourceLocationProperty ? new StringVector() : null;
        this.m_sourceLine = this.m_useSourceLocationProperty ? new IntVector() : null;
        this.m_sourceColumn = this.m_useSourceLocationProperty ? new IntVector() : intVector;
    }

    public void setUseSourceLocation(boolean z) {
        this.m_useSourceLocationProperty = z;
    }

    /* access modifiers changed from: protected */
    public int _dataOrQName(int i) {
        if (i < this.m_size) {
            return this.m_dataOrQName.elementAt(i);
        }
        while (nextNode()) {
            if (i < this.m_size) {
                return this.m_dataOrQName.elementAt(i);
            }
        }
        return -1;
    }

    public void clearCoRoutine() {
        clearCoRoutine(true);
    }

    public void clearCoRoutine(boolean z) {
        IncrementalSAXSource incrementalSAXSource = this.m_incrementalSAXSource;
        if (incrementalSAXSource != null) {
            if (z) {
                incrementalSAXSource.deliverMoreNodes(false);
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

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public ContentHandler getContentHandler() {
        return this.m_incrementalSAXSource.getClass().getName().equals("ohos.com.sun.org.apache.xml.internal.dtm.ref.IncrementalSAXSource_Filter") ? this.m_incrementalSAXSource : this;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public LexicalHandler getLexicalHandler() {
        return this.m_incrementalSAXSource.getClass().getName().equals("ohos.com.sun.org.apache.xml.internal.dtm.ref.IncrementalSAXSource_Filter") ? this.m_incrementalSAXSource : this;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public boolean needsTwoThreads() {
        return this.m_incrementalSAXSource != null;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBase, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public void dispatchCharactersEvents(int i, ContentHandler contentHandler, boolean z) throws SAXException {
        int makeNodeIdentity = makeNodeIdentity(i);
        if (makeNodeIdentity != -1) {
            short _type = _type(makeNodeIdentity);
            if (isTextType(_type)) {
                int elementAt = this.m_dataOrQName.elementAt(makeNodeIdentity);
                int elementAt2 = this.m_data.elementAt(elementAt);
                int elementAt3 = this.m_data.elementAt(elementAt + 1);
                if (z) {
                    this.m_chars.sendNormalizedSAXcharacters(contentHandler, elementAt2, elementAt3);
                } else {
                    this.m_chars.sendSAXcharacters(contentHandler, elementAt2, elementAt3);
                }
            } else {
                int _firstch = _firstch(makeNodeIdentity);
                int i2 = 0;
                if (-1 != _firstch) {
                    int i3 = -1;
                    do {
                        if (isTextType(_type(_firstch))) {
                            int _dataOrQName = _dataOrQName(_firstch);
                            if (-1 == i3) {
                                i3 = this.m_data.elementAt(_dataOrQName);
                            }
                            i2 += this.m_data.elementAt(_dataOrQName + 1);
                        }
                        _firstch = getNextNodeIdentity(_firstch);
                        if (-1 == _firstch) {
                            break;
                        }
                    } while (_parent(_firstch) >= makeNodeIdentity);
                    if (i2 <= 0) {
                        return;
                    }
                    if (z) {
                        this.m_chars.sendNormalizedSAXcharacters(contentHandler, i3, i2);
                    } else {
                        this.m_chars.sendSAXcharacters(contentHandler, i3, i2);
                    }
                } else if (_type != 1) {
                    int _dataOrQName2 = _dataOrQName(makeNodeIdentity);
                    if (_dataOrQName2 < 0) {
                        _dataOrQName2 = this.m_data.elementAt((-_dataOrQName2) + 1);
                    }
                    String indexToString = this.m_valuesOrPrefixes.indexToString(_dataOrQName2);
                    if (z) {
                        FastStringBuffer.sendNormalizedSAXcharacters(indexToString.toCharArray(), 0, indexToString.length(), contentHandler);
                    } else {
                        contentHandler.characters(indexToString.toCharArray(), 0, indexToString.length());
                    }
                }
            }
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBase, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public String getNodeName(int i) {
        int expandedTypeID = getExpandedTypeID(i);
        if (this.m_expandedNameTable.getNamespaceID(expandedTypeID) == 0) {
            short nodeType = getNodeType(i);
            if (nodeType == 13) {
                if (this.m_expandedNameTable.getLocalName(expandedTypeID) == null) {
                    return "xmlns";
                }
                return "xmlns:" + this.m_expandedNameTable.getLocalName(expandedTypeID);
            } else if (this.m_expandedNameTable.getLocalNameID(expandedTypeID) == 0) {
                return m_fixednames[nodeType];
            } else {
                return this.m_expandedNameTable.getLocalName(expandedTypeID);
            }
        } else {
            int elementAt = this.m_dataOrQName.elementAt(makeNodeIdentity(i));
            if (elementAt < 0) {
                elementAt = this.m_data.elementAt(-elementAt);
            }
            return this.m_valuesOrPrefixes.indexToString(elementAt);
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBase, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public String getNodeNameX(int i) {
        int expandedTypeID = getExpandedTypeID(i);
        if (this.m_expandedNameTable.getNamespaceID(expandedTypeID) == 0) {
            String localName = this.m_expandedNameTable.getLocalName(expandedTypeID);
            return localName == null ? "" : localName;
        }
        int elementAt = this.m_dataOrQName.elementAt(makeNodeIdentity(i));
        if (elementAt < 0) {
            elementAt = this.m_data.elementAt(-elementAt);
        }
        return this.m_valuesOrPrefixes.indexToString(elementAt);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBase, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public String getDocumentTypeDeclarationSystemIdentifier() {
        error(XMLMessages.createXMLMessage("ER_METHOD_NOT_SUPPORTED", null));
        return null;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBase
    public int getNextNodeIdentity(int i) {
        int i2 = i + 1;
        while (i2 >= this.m_size) {
            if (this.m_incrementalSAXSource == null) {
                return -1;
            }
            nextNode();
        }
        return i2;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBase, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public void dispatchToEvents(int i, ContentHandler contentHandler) throws SAXException {
        DTMTreeWalker dTMTreeWalker = this.m_walker;
        if (dTMTreeWalker.getcontentHandler() != null) {
            dTMTreeWalker = new DTMTreeWalker();
        }
        dTMTreeWalker.setcontentHandler(contentHandler);
        dTMTreeWalker.setDTM(this);
        try {
            dTMTreeWalker.traverse(i);
        } finally {
            dTMTreeWalker.setcontentHandler(null);
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBase
    public int getNumberOfNodes() {
        return this.m_size;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBase
    public boolean nextNode() {
        IncrementalSAXSource incrementalSAXSource = this.m_incrementalSAXSource;
        if (incrementalSAXSource == null) {
            return false;
        }
        if (this.m_endDocumentOccured) {
            clearCoRoutine();
            return false;
        }
        Object deliverMoreNodes = incrementalSAXSource.deliverMoreNodes(true);
        if (deliverMoreNodes instanceof Boolean) {
            if (deliverMoreNodes != Boolean.TRUE) {
                clearCoRoutine();
            }
            return true;
        } else if (deliverMoreNodes instanceof RuntimeException) {
            throw ((RuntimeException) deliverMoreNodes);
        } else if (!(deliverMoreNodes instanceof Exception)) {
            clearCoRoutine();
            return false;
        } else {
            throw new WrappedRuntimeException((Exception) deliverMoreNodes);
        }
    }

    /* access modifiers changed from: protected */
    public int addNode(int i, int i2, int i3, int i4, int i5, boolean z) {
        int i6 = this.m_size;
        this.m_size = i6 + 1;
        if (this.m_dtmIdent.size() == (i6 >>> 16)) {
            addNewDTMID(i6);
        }
        this.m_firstch.addElement(z ? -2 : -1);
        this.m_nextsib.addElement(-2);
        this.m_parent.addElement(i3);
        this.m_exptype.addElement(i2);
        this.m_dataOrQName.addElement(i5);
        if (this.m_prevsib != null) {
            this.m_prevsib.addElement(i4);
        }
        if (-1 != i4) {
            this.m_nextsib.setElementAt(i6, i4);
        }
        if (this.m_locator != null && this.m_useSourceLocationProperty) {
            setSourceLocation();
        }
        if (i != 2) {
            if (i == 13) {
                declareNamespaceInContext(i3, i6);
            } else if (-1 == i4 && -1 != i3) {
                this.m_firstch.setElementAt(i6, i3);
            }
        }
        return i6;
    }

    /* access modifiers changed from: protected */
    public void addNewDTMID(int i) {
        try {
            if (this.m_mgr != null) {
                DTMManagerDefault dTMManagerDefault = (DTMManagerDefault) this.m_mgr;
                int firstFreeDTMID = dTMManagerDefault.getFirstFreeDTMID();
                dTMManagerDefault.addDTM(this, firstFreeDTMID, i);
                this.m_dtmIdent.addElement(firstFreeDTMID << 16);
                return;
            }
            throw new ClassCastException();
        } catch (ClassCastException unused) {
            error(XMLMessages.createXMLMessage("ER_NO_DTMIDS_AVAIL", null));
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBase, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public void migrateTo(DTMManager dTMManager) {
        super.migrateTo(dTMManager);
        int size = this.m_dtmIdent.size();
        int firstFreeDTMID = this.m_mgrDefault.getFirstFreeDTMID();
        int i = 0;
        for (int i2 = 0; i2 < size; i2++) {
            this.m_dtmIdent.setElementAt(firstFreeDTMID << 16, i2);
            this.m_mgrDefault.addDTM(this, firstFreeDTMID, i);
            firstFreeDTMID++;
            i += 65536;
        }
    }

    /* access modifiers changed from: protected */
    public void setSourceLocation() {
        this.m_sourceSystemId.addElement(this.m_locator.getSystemId());
        this.m_sourceLine.addElement(this.m_locator.getLineNumber());
        this.m_sourceColumn.addElement(this.m_locator.getColumnNumber());
        if (this.m_sourceSystemId.size() != this.m_size) {
            String str = "CODING ERROR in Source Location: " + this.m_size + " != " + this.m_sourceSystemId.size();
            System.err.println(str);
            throw new RuntimeException(str);
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBase, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public String getNodeValue(int i) {
        int makeNodeIdentity = makeNodeIdentity(i);
        short _type = _type(makeNodeIdentity);
        if (isTextType(_type)) {
            int _dataOrQName = _dataOrQName(makeNodeIdentity);
            return this.m_chars.getString(this.m_data.elementAt(_dataOrQName), this.m_data.elementAt(_dataOrQName + 1));
        } else if (1 == _type || 11 == _type || 9 == _type) {
            return null;
        } else {
            int _dataOrQName2 = _dataOrQName(makeNodeIdentity);
            if (_dataOrQName2 < 0) {
                _dataOrQName2 = this.m_data.elementAt((-_dataOrQName2) + 1);
            }
            return this.m_valuesOrPrefixes.indexToString(_dataOrQName2);
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBase, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public String getLocalName(int i) {
        return this.m_expandedNameTable.getLocalName(_exptype(makeNodeIdentity(i)));
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBase, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public String getUnparsedEntityURI(String str) {
        Vector vector = this.m_entities;
        if (vector == null) {
            return "";
        }
        int size = vector.size();
        for (int i = 0; i < size; i += 4) {
            String str2 = (String) this.m_entities.elementAt(i + 3);
            if (str2 != null && str2.equals(str)) {
                if (((String) this.m_entities.elementAt(i + 2)) == null) {
                    return "";
                } else {
                    String str3 = (String) this.m_entities.elementAt(i + 1);
                    return str3 == null ? (String) this.m_entities.elementAt(i + 0) : str3;
                }
            }
        }
        return "";
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBase, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public String getPrefix(int i) {
        int _dataOrQName;
        int makeNodeIdentity = makeNodeIdentity(i);
        short _type = _type(makeNodeIdentity);
        if (1 == _type) {
            int _dataOrQName2 = _dataOrQName(makeNodeIdentity);
            if (_dataOrQName2 == 0) {
                return "";
            }
            return getPrefix(this.m_valuesOrPrefixes.indexToString(_dataOrQName2), null);
        } else if (2 != _type || (_dataOrQName = _dataOrQName(makeNodeIdentity)) >= 0) {
            return "";
        } else {
            return getPrefix(this.m_valuesOrPrefixes.indexToString(this.m_data.elementAt(-_dataOrQName)), null);
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBase, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public int getAttributeNode(int i, String str, String str2) {
        int firstAttribute = getFirstAttribute(i);
        while (-1 != firstAttribute) {
            String namespaceURI = getNamespaceURI(firstAttribute);
            String localName = getLocalName(firstAttribute);
            if ((str == namespaceURI || (str != null && str.equals(namespaceURI))) && str2.equals(localName)) {
                return firstAttribute;
            }
            firstAttribute = getNextAttribute(firstAttribute);
        }
        return -1;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBase, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public String getDocumentTypeDeclarationPublicIdentifier() {
        error(XMLMessages.createXMLMessage("ER_METHOD_NOT_SUPPORTED", null));
        return null;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBase, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public String getNamespaceURI(int i) {
        return this.m_expandedNameTable.getNamespace(_exptype(makeNodeIdentity(i)));
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBase, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public XMLString getStringValue(int i) {
        short s;
        int makeNodeIdentity = makeNodeIdentity(i);
        if (makeNodeIdentity == -1) {
            s = -1;
        } else {
            s = _type(makeNodeIdentity);
        }
        if (isTextType(s)) {
            int _dataOrQName = _dataOrQName(makeNodeIdentity);
            return this.m_xstrf.newstr(this.m_chars, this.m_data.elementAt(_dataOrQName), this.m_data.elementAt(_dataOrQName + 1));
        }
        int _firstch = _firstch(makeNodeIdentity);
        if (-1 != _firstch) {
            int i2 = 0;
            int i3 = -1;
            do {
                if (isTextType(_type(_firstch))) {
                    int _dataOrQName2 = _dataOrQName(_firstch);
                    if (-1 == i3) {
                        i3 = this.m_data.elementAt(_dataOrQName2);
                    }
                    i2 += this.m_data.elementAt(_dataOrQName2 + 1);
                }
                _firstch = getNextNodeIdentity(_firstch);
                if (-1 == _firstch) {
                    break;
                }
            } while (_parent(_firstch) >= makeNodeIdentity);
            if (i2 > 0) {
                return this.m_xstrf.newstr(this.m_chars, i3, i2);
            }
        } else if (s != 1) {
            int _dataOrQName3 = _dataOrQName(makeNodeIdentity);
            if (_dataOrQName3 < 0) {
                _dataOrQName3 = this.m_data.elementAt((-_dataOrQName3) + 1);
            }
            return this.m_xstrf.newstr(this.m_valuesOrPrefixes.indexToString(_dataOrQName3));
        }
        return this.m_xstrf.emptystr();
    }

    public boolean isWhitespace(int i) {
        int makeNodeIdentity = makeNodeIdentity(i);
        short s = -1;
        if (makeNodeIdentity != -1) {
            s = _type(makeNodeIdentity);
        }
        if (!isTextType(s)) {
            return false;
        }
        int _dataOrQName = _dataOrQName(makeNodeIdentity);
        return this.m_chars.isWhitespace(this.m_data.elementAt(_dataOrQName), this.m_data.elementAt(_dataOrQName + 1));
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.ref.DTMDefaultBase, ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public int getElementById(String str) {
        Integer num;
        boolean z = true;
        do {
            num = this.m_idAttributes.get(str);
            if (num != null) {
                return makeNodeHandle(num.intValue());
            }
            if (!z || this.m_endDocumentOccured) {
                return -1;
            }
            z = nextNode();
        } while (num == null);
        return -1;
    }

    public String getPrefix(String str, String str2) {
        String str3 = "";
        if (str2 != null && str2.length() > 0) {
            int i = -1;
            do {
                i = this.m_prefixMappings.indexOf(str2, i + 1);
            } while ((i & 1) == 0);
            if (i >= 0) {
                return (String) this.m_prefixMappings.elementAt(i - 1);
            }
            if (str == null) {
                return null;
            }
            int indexOf = str.indexOf(58);
            if (!str.equals("xmlns")) {
                str3 = str.startsWith("xmlns:") ? str.substring(indexOf + 1) : indexOf > 0 ? str.substring(0, indexOf) : null;
            }
        } else if (str == null) {
            return null;
        } else {
            int indexOf2 = str.indexOf(58);
            if (indexOf2 > 0) {
                str3 = str.startsWith("xmlns:") ? str.substring(indexOf2 + 1) : str.substring(0, indexOf2);
            } else if (!str.equals("xmlns")) {
                return null;
            }
        }
        return str3;
    }

    public int getIdForNamespace(String str) {
        return this.m_valuesOrPrefixes.stringToIndex(str);
    }

    public String getNamespaceURI(String str) {
        int peek = this.m_contextIndexes.peek() - 1;
        if (str == null) {
            str = "";
        }
        do {
            peek = this.m_prefixMappings.indexOf(str, peek + 1);
            if (peek < 0) {
                break;
            }
        } while ((peek & 1) == 1);
        if (peek > -1) {
            return (String) this.m_prefixMappings.elementAt(peek + 1);
        }
        return "";
    }

    public void setIDAttribute(String str, int i) {
        this.m_idAttributes.put(str, Integer.valueOf(i));
    }

    /* access modifiers changed from: protected */
    public void charactersFlush() {
        if (this.m_textPendingStart >= 0) {
            int size = this.m_chars.size() - this.m_textPendingStart;
            boolean z = false;
            if (getShouldStripWhitespace()) {
                z = this.m_chars.isWhitespace(this.m_textPendingStart, size);
            }
            if (z) {
                this.m_chars.setLength(this.m_textPendingStart);
            } else if (size > 0) {
                this.m_previous = addNode(this.m_coalescedTextType, this.m_expandedNameTable.getExpandedTypeID(3), this.m_parents.peek(), this.m_previous, this.m_data.size(), false);
                this.m_data.addElement(this.m_textPendingStart);
                this.m_data.addElement(size);
            }
            this.m_textPendingStart = -1;
            this.m_coalescedTextType = 3;
            this.m_textType = 3;
        }
    }

    public void unparsedEntityDecl(String str, String str2, String str3, String str4) throws SAXException {
        if (this.m_entities == null) {
            this.m_entities = new Vector();
        }
        try {
            String absoluteURI = SystemIDResolver.getAbsoluteURI(str3, getDocumentBaseURI());
            this.m_entities.addElement(str2);
            this.m_entities.addElement(absoluteURI);
            this.m_entities.addElement(str4);
            this.m_entities.addElement(str);
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

    public void startPrefixMapping(String str, String str2) throws SAXException {
        if (str == null) {
            str = "";
        }
        this.m_prefixMappings.addElement(str);
        this.m_prefixMappings.addElement(str2);
    }

    public void endPrefixMapping(String str) throws SAXException {
        if (str == null) {
            str = "";
        }
        int peek = this.m_contextIndexes.peek() - 1;
        do {
            peek = this.m_prefixMappings.indexOf(str, peek + 1);
            if (peek < 0) {
                break;
            }
        } while ((peek & 1) == 1);
        if (peek > -1) {
            this.m_prefixMappings.setElementAt("%@$#^@#", peek);
            this.m_prefixMappings.setElementAt("%@$#^@#", peek + 1);
        }
    }

    /* access modifiers changed from: protected */
    public boolean declAlreadyDeclared(String str) {
        Vector vector = this.m_prefixMappings;
        int size = vector.size();
        for (int peek = this.m_contextIndexes.peek(); peek < size; peek += 2) {
            String str2 = (String) vector.elementAt(peek);
            if (str2 != null && str2.equals(str)) {
                return true;
            }
        }
        return false;
    }

    public void startElement(String str, String str2, String str3, Attributes attributes) throws SAXException {
        int i;
        charactersFlush();
        int expandedTypeID = this.m_expandedNameTable.getExpandedTypeID(str, str2, 1);
        boolean z = false;
        int addNode = addNode(1, expandedTypeID, this.m_parents.peek(), this.m_previous, getPrefix(str3, str) != null ? this.m_valuesOrPrefixes.stringToIndex(str3) : 0, true);
        if (this.m_indexing) {
            indexNode(expandedTypeID, addNode);
        }
        this.m_parents.push(addNode);
        int size = this.m_prefixMappings.size();
        int i2 = -1;
        if (!this.m_pastFirstElement) {
            i2 = addNode(13, this.m_expandedNameTable.getExpandedTypeID(null, "xml", 13), addNode, -1, this.m_valuesOrPrefixes.stringToIndex("http://www.w3.org/XML/1998/namespace"), false);
            this.m_pastFirstElement = true;
        }
        for (int peek = this.m_contextIndexes.peek(); peek < size; peek += 2) {
            String str4 = (String) this.m_prefixMappings.elementAt(peek);
            if (str4 != null) {
                i2 = addNode(13, this.m_expandedNameTable.getExpandedTypeID(null, str4, 13), addNode, i2, this.m_valuesOrPrefixes.stringToIndex((String) this.m_prefixMappings.elementAt(peek + 1)), false);
            }
        }
        int length = attributes.getLength();
        for (int i3 = 0; i3 < length; i3++) {
            String uri = attributes.getURI(i3);
            String qName = attributes.getQName(i3);
            String value = attributes.getValue(i3);
            String prefix = getPrefix(qName, uri);
            String localName = attributes.getLocalName(i3);
            if (qName == null || (!qName.equals("xmlns") && !qName.startsWith("xmlns:"))) {
                if (attributes.getType(i3).equalsIgnoreCase(SchemaSymbols.ATTVAL_ID)) {
                    setIDAttribute(value, addNode);
                }
                i = 2;
            } else if (!declAlreadyDeclared(prefix)) {
                i = 13;
            }
            if (value == null) {
                value = "";
            }
            int stringToIndex = this.m_valuesOrPrefixes.stringToIndex(value);
            if (prefix != null) {
                int stringToIndex2 = this.m_valuesOrPrefixes.stringToIndex(qName);
                int size2 = this.m_data.size();
                this.m_data.addElement(stringToIndex2);
                this.m_data.addElement(stringToIndex);
                stringToIndex = -size2;
            }
            i2 = addNode(i, this.m_expandedNameTable.getExpandedTypeID(uri, localName, i), addNode, i2, stringToIndex, false);
        }
        if (-1 != i2) {
            this.m_nextsib.setElementAt(-1, i2);
        }
        if (this.m_wsfilter != null) {
            short shouldStripSpace = this.m_wsfilter.getShouldStripSpace(makeNodeHandle(addNode), this);
            if (3 == shouldStripSpace) {
                z = getShouldStripWhitespace();
            } else if (2 == shouldStripSpace) {
                z = true;
            }
            pushShouldStripWhitespace(z);
        }
        this.m_previous = -1;
        this.m_contextIndexes.push(this.m_prefixMappings.size());
    }

    public void endElement(String str, String str2, String str3) throws SAXException {
        charactersFlush();
        this.m_contextIndexes.quickPop(1);
        int peek = this.m_contextIndexes.peek();
        if (peek != this.m_prefixMappings.size()) {
            this.m_prefixMappings.setSize(peek);
        }
        int i = this.m_previous;
        this.m_previous = this.m_parents.pop();
        if (-1 == i) {
            this.m_firstch.setElementAt(-1, this.m_previous);
        } else {
            this.m_nextsib.setElementAt(-1, i);
        }
        popShouldStripWhitespace();
    }

    public void characters(char[] cArr, int i, int i2) throws SAXException {
        if (this.m_textPendingStart == -1) {
            this.m_textPendingStart = this.m_chars.size();
            this.m_coalescedTextType = this.m_textType;
        } else if (this.m_textType == 3) {
            this.m_coalescedTextType = 3;
        }
        this.m_chars.append(cArr, i, i2);
    }

    public void ignorableWhitespace(char[] cArr, int i, int i2) throws SAXException {
        characters(cArr, i, i2);
    }

    public void processingInstruction(String str, String str2) throws SAXException {
        charactersFlush();
        this.m_previous = addNode(7, this.m_expandedNameTable.getExpandedTypeID(null, str, 7), this.m_parents.peek(), this.m_previous, this.m_valuesOrPrefixes.stringToIndex(str2), false);
    }

    public void warning(SAXParseException sAXParseException) throws SAXException {
        System.err.println(sAXParseException.getMessage());
    }

    public void error(SAXParseException sAXParseException) throws SAXException {
        throw sAXParseException;
    }

    public void fatalError(SAXParseException sAXParseException) throws SAXException {
        throw sAXParseException;
    }

    public void startDTD(String str, String str2, String str3) throws SAXException {
        this.m_insideDTD = true;
    }

    public void endDTD() throws SAXException {
        this.m_insideDTD = false;
    }

    public void startCDATA() throws SAXException {
        this.m_textType = 4;
    }

    public void endCDATA() throws SAXException {
        this.m_textType = 3;
    }

    public void comment(char[] cArr, int i, int i2) throws SAXException {
        if (!this.m_insideDTD) {
            charactersFlush();
            this.m_previous = addNode(8, this.m_expandedNameTable.getExpandedTypeID(8), this.m_parents.peek(), this.m_previous, this.m_valuesOrPrefixes.stringToIndex(new String(cArr, i, i2)), false);
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public SourceLocator getSourceLocatorFor(int i) {
        if (this.m_useSourceLocationProperty) {
            int makeNodeIdentity = makeNodeIdentity(i);
            return new NodeLocator(null, this.m_sourceSystemId.elementAt(makeNodeIdentity), this.m_sourceLine.elementAt(makeNodeIdentity), this.m_sourceColumn.elementAt(makeNodeIdentity));
        }
        Locator locator = this.m_locator;
        if (locator != null) {
            return new NodeLocator(null, locator.getSystemId(), -1, -1);
        }
        String str = this.m_systemId;
        if (str != null) {
            return new NodeLocator(null, str, -1, -1);
        }
        return null;
    }

    public String getFixedNames(int i) {
        return m_fixednames[i];
    }
}

package org.apache.xml.dtm.ref;

import javax.xml.transform.SourceLocator;
import org.apache.xalan.templates.Constants;
import org.apache.xml.dtm.DTM;
import org.apache.xml.dtm.DTMAxisIterator;
import org.apache.xml.dtm.DTMAxisTraverser;
import org.apache.xml.dtm.DTMManager;
import org.apache.xml.dtm.DTMWSFilter;
import org.apache.xml.utils.FastStringBuffer;
import org.apache.xml.utils.XMLString;
import org.apache.xml.utils.XMLStringFactory;
import org.apache.xpath.axes.WalkerFactory;
import org.apache.xpath.compiler.PsuedoNames;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DeclHandler;
import org.xml.sax.ext.LexicalHandler;

public class DTMDocumentImpl implements DTM, ContentHandler, LexicalHandler {
    protected static final int DOCHANDLE_MASK = -8388608;
    protected static final byte DOCHANDLE_SHIFT = (byte) 22;
    protected static final int NODEHANDLE_MASK = 8388607;
    private static final String[] fixednames = new String[]{null, null, null, PsuedoNames.PSEUDONAME_TEXT, "#cdata_section", null, null, null, PsuedoNames.PSEUDONAME_COMMENT, "#document", null, "#document-fragment", null};
    private final boolean DEBUG = false;
    int currentParent = 0;
    private boolean done = false;
    int[] gotslot = new int[4];
    private FastStringBuffer m_char = new FastStringBuffer();
    private int m_char_current_start = 0;
    protected int m_currentNode = -1;
    int m_docElement = -1;
    int m_docHandle = -1;
    protected String m_documentBaseURI;
    private ExpandedNameTable m_expandedNames = new ExpandedNameTable();
    private IncrementalSAXSource m_incrSAXSource = null;
    boolean m_isError = false;
    private DTMStringPool m_localNames = new DTMStringPool();
    private DTMStringPool m_nsNames = new DTMStringPool();
    private DTMStringPool m_prefixNames = new DTMStringPool();
    private XMLStringFactory m_xsf;
    ChunkedIntArray nodes = new ChunkedIntArray(4);
    int previousSibling = 0;
    private boolean previousSiblingWasParent = false;

    public DTMDocumentImpl(DTMManager mgr, int documentNumber, DTMWSFilter whiteSpaceFilter, XMLStringFactory xstringfactory) {
        initDocument(documentNumber);
        this.m_xsf = xstringfactory;
    }

    public void setIncrementalSAXSource(IncrementalSAXSource source) {
        this.m_incrSAXSource = source;
        source.setContentHandler(this);
        source.setLexicalHandler(this);
    }

    private final int appendNode(int w0, int w1, int w2, int w3) {
        int slotnumber = this.nodes.appendSlot(w0, w1, w2, w3);
        if (this.previousSiblingWasParent) {
            this.nodes.writeEntry(this.previousSibling, 2, slotnumber);
        }
        this.previousSiblingWasParent = false;
        return slotnumber;
    }

    public void setFeature(String featureId, boolean state) {
    }

    public void setLocalNameTable(DTMStringPool poolRef) {
        this.m_localNames = poolRef;
    }

    public DTMStringPool getLocalNameTable() {
        return this.m_localNames;
    }

    public void setNsNameTable(DTMStringPool poolRef) {
        this.m_nsNames = poolRef;
    }

    public DTMStringPool getNsNameTable() {
        return this.m_nsNames;
    }

    public void setPrefixNameTable(DTMStringPool poolRef) {
        this.m_prefixNames = poolRef;
    }

    public DTMStringPool getPrefixNameTable() {
        return this.m_prefixNames;
    }

    void setContentBuffer(FastStringBuffer buffer) {
        this.m_char = buffer;
    }

    FastStringBuffer getContentBuffer() {
        return this.m_char;
    }

    public ContentHandler getContentHandler() {
        if (this.m_incrSAXSource instanceof IncrementalSAXSource_Filter) {
            return (ContentHandler) this.m_incrSAXSource;
        }
        return this;
    }

    public LexicalHandler getLexicalHandler() {
        if (this.m_incrSAXSource instanceof IncrementalSAXSource_Filter) {
            return (LexicalHandler) this.m_incrSAXSource;
        }
        return this;
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
        return this.m_incrSAXSource != null;
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        this.m_char.append(ch, start, length);
    }

    private void processAccumulatedText() {
        int len = this.m_char.length();
        if (len != this.m_char_current_start) {
            appendTextChild(this.m_char_current_start, len - this.m_char_current_start);
            this.m_char_current_start = len;
        }
    }

    public void endDocument() throws SAXException {
        appendEndDocument();
    }

    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
        processAccumulatedText();
        appendEndElement();
    }

    public void endPrefixMapping(String prefix) throws SAXException {
    }

    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
    }

    public void processingInstruction(String target, String data) throws SAXException {
        processAccumulatedText();
    }

    public void setDocumentLocator(Locator locator) {
    }

    public void skippedEntity(String name) throws SAXException {
        processAccumulatedText();
    }

    public void startDocument() throws SAXException {
        appendStartDocument();
    }

    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
        int i;
        processAccumulatedText();
        String prefix = null;
        int colon = qName.indexOf(58);
        if (colon > 0) {
            prefix = qName.substring(0, colon);
        }
        System.out.println("Prefix=" + prefix + " index=" + this.m_prefixNames.stringToIndex(prefix));
        appendStartElement(this.m_nsNames.stringToIndex(namespaceURI), this.m_localNames.stringToIndex(localName), this.m_prefixNames.stringToIndex(prefix));
        int nAtts = atts == null ? 0 : atts.getLength();
        for (i = nAtts - 1; i >= 0; i--) {
            qName = atts.getQName(i);
            if (qName.startsWith(Constants.ATTRNAME_XMLNS) || "xmlns".equals(qName)) {
                colon = qName.indexOf(58);
                if (colon > 0) {
                    prefix = qName.substring(0, colon);
                } else {
                    prefix = null;
                }
                appendNSDeclaration(this.m_prefixNames.stringToIndex(prefix), this.m_nsNames.stringToIndex(atts.getValue(i)), atts.getType(i).equalsIgnoreCase("ID"));
            }
        }
        for (i = nAtts - 1; i >= 0; i--) {
            qName = atts.getQName(i);
            if (!(!qName.startsWith(Constants.ATTRNAME_XMLNS) ? "xmlns".equals(qName) : true)) {
                colon = qName.indexOf(58);
                if (colon > 0) {
                    prefix = qName.substring(0, colon);
                    localName = qName.substring(colon + 1);
                } else {
                    prefix = "";
                    localName = qName;
                }
                this.m_char.append(atts.getValue(i));
                int contentEnd = this.m_char.length();
                if (!(!"xmlns".equals(prefix) ? "xmlns".equals(qName) : true)) {
                    appendAttribute(this.m_nsNames.stringToIndex(atts.getURI(i)), this.m_localNames.stringToIndex(localName), this.m_prefixNames.stringToIndex(prefix), atts.getType(i).equalsIgnoreCase("ID"), this.m_char_current_start, contentEnd - this.m_char_current_start);
                }
                this.m_char_current_start = contentEnd;
            }
        }
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
    }

    public void comment(char[] ch, int start, int length) throws SAXException {
        processAccumulatedText();
        this.m_char.append(ch, start, length);
        appendComment(this.m_char_current_start, length);
        this.m_char_current_start += length;
    }

    public void endCDATA() throws SAXException {
    }

    public void endDTD() throws SAXException {
    }

    public void endEntity(String name) throws SAXException {
    }

    public void startCDATA() throws SAXException {
    }

    public void startDTD(String name, String publicId, String systemId) throws SAXException {
    }

    public void startEntity(String name) throws SAXException {
    }

    final void initDocument(int documentNumber) {
        this.m_docHandle = documentNumber << 22;
        this.nodes.writeSlot(0, 9, -1, -1, 0);
        this.done = false;
    }

    public boolean hasChildNodes(int nodeHandle) {
        return getFirstChild(nodeHandle) != -1;
    }

    public int getFirstChild(int nodeHandle) {
        nodeHandle &= NODEHANDLE_MASK;
        this.nodes.readSlot(nodeHandle, this.gotslot);
        short type = (short) (this.gotslot[0] & DTMManager.IDENT_NODE_DEFAULT);
        if (type == (short) 1 || type == (short) 9 || type == (short) 5) {
            int kid = nodeHandle + 1;
            this.nodes.readSlot(kid, this.gotslot);
            while (2 == (this.gotslot[0] & DTMManager.IDENT_NODE_DEFAULT)) {
                kid = this.gotslot[2];
                if (kid == -1) {
                    return -1;
                }
                this.nodes.readSlot(kid, this.gotslot);
            }
            if (this.gotslot[1] == nodeHandle) {
                return kid | this.m_docHandle;
            }
        }
        return -1;
    }

    public int getLastChild(int nodeHandle) {
        int lastChild = -1;
        int nextkid = getFirstChild(nodeHandle & NODEHANDLE_MASK);
        while (nextkid != -1) {
            lastChild = nextkid;
            nextkid = getNextSibling(nextkid);
        }
        return this.m_docHandle | lastChild;
    }

    public int getAttributeNode(int nodeHandle, String namespaceURI, String name) {
        int nsIndex = this.m_nsNames.stringToIndex(namespaceURI);
        int nameIndex = this.m_localNames.stringToIndex(name);
        nodeHandle &= NODEHANDLE_MASK;
        this.nodes.readSlot(nodeHandle, this.gotslot);
        short type = (short) (this.gotslot[0] & DTMManager.IDENT_NODE_DEFAULT);
        if (type == (short) 1) {
            nodeHandle++;
        }
        while (type == (short) 2) {
            if (nsIndex == (this.gotslot[0] << 16) && this.gotslot[3] == nameIndex) {
                return this.m_docHandle | nodeHandle;
            }
            nodeHandle = this.gotslot[2];
            this.nodes.readSlot(nodeHandle, this.gotslot);
        }
        return -1;
    }

    public int getFirstAttribute(int nodeHandle) {
        int i = -1;
        nodeHandle &= NODEHANDLE_MASK;
        if (1 != (this.nodes.readEntry(nodeHandle, 0) & DTMManager.IDENT_NODE_DEFAULT)) {
            return -1;
        }
        nodeHandle++;
        if (2 == (this.nodes.readEntry(nodeHandle, 0) & DTMManager.IDENT_NODE_DEFAULT)) {
            i = this.m_docHandle | nodeHandle;
        }
        return i;
    }

    public int getFirstNamespaceNode(int nodeHandle, boolean inScope) {
        return -1;
    }

    public int getNextSibling(int nodeHandle) {
        nodeHandle &= NODEHANDLE_MASK;
        if (nodeHandle == 0) {
            return -1;
        }
        short type = (short) (this.nodes.readEntry(nodeHandle, 0) & DTMManager.IDENT_NODE_DEFAULT);
        if (type == (short) 1 || type == (short) 2 || type == (short) 5) {
            int nextSib = this.nodes.readEntry(nodeHandle, 2);
            if (nextSib == -1) {
                return -1;
            }
            if (nextSib != 0) {
                return this.m_docHandle | nextSib;
            }
        }
        int thisParent = this.nodes.readEntry(nodeHandle, 1);
        nodeHandle++;
        if (this.nodes.readEntry(nodeHandle, 1) == thisParent) {
            return this.m_docHandle | nodeHandle;
        }
        return -1;
    }

    public int getPreviousSibling(int nodeHandle) {
        nodeHandle &= NODEHANDLE_MASK;
        if (nodeHandle == 0) {
            return -1;
        }
        int kid = -1;
        int nextkid = getFirstChild(this.nodes.readEntry(nodeHandle, 1));
        while (nextkid != nodeHandle) {
            kid = nextkid;
            nextkid = getNextSibling(nextkid);
        }
        return this.m_docHandle | kid;
    }

    public int getNextAttribute(int nodeHandle) {
        nodeHandle &= NODEHANDLE_MASK;
        this.nodes.readSlot(nodeHandle, this.gotslot);
        short type = (short) (this.gotslot[0] & DTMManager.IDENT_NODE_DEFAULT);
        if (type == (short) 1) {
            return getFirstAttribute(nodeHandle);
        }
        if (type != (short) 2 || this.gotslot[2] == -1) {
            return -1;
        }
        return this.m_docHandle | this.gotslot[2];
    }

    public int getNextNamespaceNode(int baseHandle, int namespaceHandle, boolean inScope) {
        return -1;
    }

    public int getNextDescendant(int subtreeRootHandle, int nodeHandle) {
        subtreeRootHandle &= NODEHANDLE_MASK;
        nodeHandle &= NODEHANDLE_MASK;
        if (nodeHandle == 0) {
            return -1;
        }
        while (!this.m_isError && (!this.done || nodeHandle <= this.nodes.slotsUsed())) {
            if (nodeHandle > subtreeRootHandle) {
                this.nodes.readSlot(nodeHandle + 1, this.gotslot);
                if (this.gotslot[2] == 0) {
                    if (this.done) {
                        break;
                    }
                } else if (((short) (this.gotslot[0] & DTMManager.IDENT_NODE_DEFAULT)) == (short) 2) {
                    nodeHandle += 2;
                } else if (this.gotslot[1] >= subtreeRootHandle) {
                    return this.m_docHandle | (nodeHandle + 1);
                }
            } else {
                nodeHandle++;
            }
        }
        return -1;
    }

    public int getNextFollowing(int axisContextHandle, int nodeHandle) {
        return -1;
    }

    public int getNextPreceding(int axisContextHandle, int nodeHandle) {
        nodeHandle &= NODEHANDLE_MASK;
        while (nodeHandle > 1) {
            nodeHandle--;
            if (2 != (this.nodes.readEntry(nodeHandle, 0) & DTMManager.IDENT_NODE_DEFAULT)) {
                return this.m_docHandle | this.nodes.specialFind(axisContextHandle, nodeHandle);
            }
        }
        return -1;
    }

    public int getParent(int nodeHandle) {
        return this.m_docHandle | this.nodes.readEntry(nodeHandle, 1);
    }

    public int getDocumentRoot() {
        return this.m_docHandle | this.m_docElement;
    }

    public int getDocument() {
        return this.m_docHandle;
    }

    public int getOwnerDocument(int nodeHandle) {
        if ((NODEHANDLE_MASK & nodeHandle) == 0) {
            return -1;
        }
        return DOCHANDLE_MASK & nodeHandle;
    }

    public int getDocumentRoot(int nodeHandle) {
        if ((NODEHANDLE_MASK & nodeHandle) == 0) {
            return -1;
        }
        return DOCHANDLE_MASK & nodeHandle;
    }

    public XMLString getStringValue(int nodeHandle) {
        this.nodes.readSlot(nodeHandle, this.gotslot);
        String value = null;
        switch (this.gotslot[0] & WalkerFactory.BITS_COUNT) {
            case 3:
            case 4:
            case 8:
                value = this.m_char.getString(this.gotslot[2], this.gotslot[3]);
                break;
        }
        return this.m_xsf.newstr(value);
    }

    public int getStringValueChunkCount(int nodeHandle) {
        return 0;
    }

    public char[] getStringValueChunk(int nodeHandle, int chunkIndex, int[] startAndLen) {
        return new char[0];
    }

    public int getExpandedTypeID(int nodeHandle) {
        this.nodes.readSlot(nodeHandle, this.gotslot);
        String qName = this.m_localNames.indexToString(this.gotslot[3]);
        return this.m_nsNames.stringToIndex(this.m_nsNames.indexToString(this.gotslot[0] << 16) + ":" + qName.substring(qName.indexOf(":") + 1));
    }

    public int getExpandedTypeID(String namespace, String localName, int type) {
        return this.m_nsNames.stringToIndex(namespace + ":" + localName);
    }

    public String getLocalNameFromExpandedNameID(int ExpandedNameID) {
        String expandedName = this.m_localNames.indexToString(ExpandedNameID);
        return expandedName.substring(expandedName.indexOf(":") + 1);
    }

    public String getNamespaceFromExpandedNameID(int ExpandedNameID) {
        String expandedName = this.m_localNames.indexToString(ExpandedNameID);
        return expandedName.substring(0, expandedName.indexOf(":"));
    }

    public String getNodeName(int nodeHandle) {
        this.nodes.readSlot(nodeHandle, this.gotslot);
        String name = fixednames[(short) (this.gotslot[0] & DTMManager.IDENT_NODE_DEFAULT)];
        if (name != null) {
            return name;
        }
        int i = this.gotslot[3];
        System.out.println("got i=" + i + " " + (i >> 16) + PsuedoNames.PSEUDONAME_ROOT + (i & DTMManager.IDENT_NODE_DEFAULT));
        name = this.m_localNames.indexToString(i & DTMManager.IDENT_NODE_DEFAULT);
        String prefix = this.m_prefixNames.indexToString(i >> 16);
        if (prefix == null || prefix.length() <= 0) {
            return name;
        }
        return prefix + ":" + name;
    }

    public String getNodeNameX(int nodeHandle) {
        return null;
    }

    public String getLocalName(int nodeHandle) {
        this.nodes.readSlot(nodeHandle, this.gotslot);
        short type = (short) (this.gotslot[0] & DTMManager.IDENT_NODE_DEFAULT);
        String name = "";
        if (type != (short) 1 && type != (short) 2) {
            return name;
        }
        name = this.m_localNames.indexToString(this.gotslot[3] & DTMManager.IDENT_NODE_DEFAULT);
        if (name == null) {
            return "";
        }
        return name;
    }

    public String getPrefix(int nodeHandle) {
        this.nodes.readSlot(nodeHandle, this.gotslot);
        short type = (short) (this.gotslot[0] & DTMManager.IDENT_NODE_DEFAULT);
        String name = "";
        if (type != (short) 1 && type != (short) 2) {
            return name;
        }
        name = this.m_prefixNames.indexToString(this.gotslot[3] >> 16);
        if (name == null) {
            return "";
        }
        return name;
    }

    public String getNamespaceURI(int nodeHandle) {
        return null;
    }

    public String getNodeValue(int nodeHandle) {
        this.nodes.readSlot(nodeHandle, this.gotslot);
        switch (this.gotslot[0] & WalkerFactory.BITS_COUNT) {
            case 2:
                this.nodes.readSlot(nodeHandle + 1, this.gotslot);
                break;
            case 3:
            case 4:
            case 8:
                break;
            default:
                return null;
        }
        return this.m_char.getString(this.gotslot[2], this.gotslot[3]);
    }

    public short getNodeType(int nodeHandle) {
        return (short) (this.nodes.readEntry(nodeHandle, 0) & DTMManager.IDENT_NODE_DEFAULT);
    }

    public short getLevel(int nodeHandle) {
        short count = (short) 0;
        while (nodeHandle != 0) {
            count = (short) (count + 1);
            nodeHandle = this.nodes.readEntry(nodeHandle, 1);
        }
        return count;
    }

    public boolean isSupported(String feature, String version) {
        return false;
    }

    public String getDocumentBaseURI() {
        return this.m_documentBaseURI;
    }

    public void setDocumentBaseURI(String baseURI) {
        this.m_documentBaseURI = baseURI;
    }

    public String getDocumentSystemIdentifier(int nodeHandle) {
        return null;
    }

    public String getDocumentEncoding(int nodeHandle) {
        return null;
    }

    public String getDocumentStandalone(int nodeHandle) {
        return null;
    }

    public String getDocumentVersion(int documentHandle) {
        return null;
    }

    public boolean getDocumentAllDeclarationsProcessed() {
        return false;
    }

    public String getDocumentTypeDeclarationSystemIdentifier() {
        return null;
    }

    public String getDocumentTypeDeclarationPublicIdentifier() {
        return null;
    }

    public int getElementById(String elementId) {
        return 0;
    }

    public String getUnparsedEntityURI(String name) {
        return null;
    }

    public boolean supportsPreStripping() {
        return false;
    }

    public boolean isNodeAfter(int nodeHandle1, int nodeHandle2) {
        return false;
    }

    public boolean isCharacterElementContentWhitespace(int nodeHandle) {
        return false;
    }

    public boolean isDocumentAllDeclarationsProcessed(int documentHandle) {
        return false;
    }

    public boolean isAttributeSpecified(int attributeHandle) {
        return false;
    }

    public void dispatchCharactersEvents(int nodeHandle, ContentHandler ch, boolean normalize) throws SAXException {
    }

    public void dispatchToEvents(int nodeHandle, ContentHandler ch) throws SAXException {
    }

    public Node getNode(int nodeHandle) {
        return null;
    }

    public void appendChild(int newChild, boolean clone, boolean cloneDepth) {
        boolean sameDoc = (DOCHANDLE_MASK & newChild) == this.m_docHandle;
        if (!clone) {
            int i = sameDoc ^ 1;
        }
    }

    public void appendTextChild(String str) {
    }

    void appendTextChild(int m_char_current_start, int contentLength) {
        int w2 = m_char_current_start;
        int w3 = contentLength;
        this.previousSibling = appendNode(3, this.currentParent, m_char_current_start, contentLength);
    }

    void appendComment(int m_char_current_start, int contentLength) {
        int w2 = m_char_current_start;
        int w3 = contentLength;
        this.previousSibling = appendNode(8, this.currentParent, m_char_current_start, contentLength);
    }

    void appendStartElement(int namespaceIndex, int localNameIndex, int prefixIndex) {
        int w0 = (namespaceIndex << 16) | 1;
        int w1 = this.currentParent;
        int w3 = localNameIndex | (prefixIndex << 16);
        System.out.println("set w3=" + w3 + " " + (w3 >> 16) + PsuedoNames.PSEUDONAME_ROOT + (DTMManager.IDENT_NODE_DEFAULT & w3));
        int ourslot = appendNode(w0, w1, 0, w3);
        this.currentParent = ourslot;
        this.previousSibling = 0;
        if (this.m_docElement == -1) {
            this.m_docElement = ourslot;
        }
    }

    void appendNSDeclaration(int prefixIndex, int namespaceIndex, boolean isID) {
        int namespaceForNamespaces = this.m_nsNames.stringToIndex(SerializerConstants.XMLNS_URI);
        int w3 = namespaceIndex;
        this.previousSibling = appendNode((this.m_nsNames.stringToIndex(SerializerConstants.XMLNS_URI) << 16) | 13, this.currentParent, 0, namespaceIndex);
        this.previousSiblingWasParent = false;
    }

    void appendAttribute(int namespaceIndex, int localNameIndex, int prefixIndex, boolean isID, int m_char_current_start, int contentLength) {
        int w0 = (namespaceIndex << 16) | 2;
        int w1 = this.currentParent;
        int w3 = localNameIndex | (prefixIndex << 16);
        System.out.println("set w3=" + w3 + " " + (w3 >> 16) + PsuedoNames.PSEUDONAME_ROOT + (DTMManager.IDENT_NODE_DEFAULT & w3));
        int ourslot = appendNode(w0, w1, 0, w3);
        this.previousSibling = ourslot;
        w1 = ourslot;
        int w2 = m_char_current_start;
        w3 = contentLength;
        appendNode(3, ourslot, m_char_current_start, contentLength);
        this.previousSiblingWasParent = true;
    }

    public DTMAxisTraverser getAxisTraverser(int axis) {
        return null;
    }

    public DTMAxisIterator getAxisIterator(int axis) {
        return null;
    }

    public DTMAxisIterator getTypedAxisIterator(int axis, int type) {
        return null;
    }

    void appendEndElement() {
        if (this.previousSiblingWasParent) {
            this.nodes.writeEntry(this.previousSibling, 2, -1);
        }
        this.previousSibling = this.currentParent;
        this.nodes.readSlot(this.currentParent, this.gotslot);
        this.currentParent = this.gotslot[1] & DTMManager.IDENT_NODE_DEFAULT;
        this.previousSiblingWasParent = true;
    }

    void appendStartDocument() {
        this.m_docElement = -1;
        initDocument(0);
    }

    void appendEndDocument() {
        this.done = true;
    }

    public void setProperty(String property, Object value) {
    }

    public SourceLocator getSourceLocatorFor(int node) {
        return null;
    }

    public void documentRegistration() {
    }

    public void documentRelease() {
    }

    public void migrateTo(DTMManager manager) {
    }
}

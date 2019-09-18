package org.apache.xml.dtm.ref;

import java.io.PrintStream;
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
    protected static final byte DOCHANDLE_SHIFT = 22;
    protected static final int NODEHANDLE_MASK = 8388607;
    private static final String[] fixednames = {null, null, null, PsuedoNames.PSEUDONAME_TEXT, "#cdata_section", null, null, null, PsuedoNames.PSEUDONAME_COMMENT, "#document", null, "#document-fragment", null};
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

    /* access modifiers changed from: package-private */
    public void setContentBuffer(FastStringBuffer buffer) {
        this.m_char = buffer;
    }

    /* access modifiers changed from: package-private */
    public FastStringBuffer getContentBuffer() {
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
        String prefix;
        String localName2;
        String prefix2;
        String localName3;
        int contentEnd;
        String prefix3;
        String str = qName;
        Attributes attributes = atts;
        processAccumulatedText();
        String prefix4 = null;
        int i = 58;
        int colon = str.indexOf(58);
        int i2 = 0;
        if (colon > 0) {
            prefix4 = str.substring(0, colon);
        }
        System.out.println("Prefix=" + prefix4 + " index=" + this.m_prefixNames.stringToIndex(prefix4));
        appendStartElement(this.m_nsNames.stringToIndex(namespaceURI), this.m_localNames.stringToIndex(localName), this.m_prefixNames.stringToIndex(prefix4));
        int nAtts = attributes == null ? 0 : atts.getLength();
        for (int i3 = nAtts - 1; i3 >= 0; i3--) {
            String qName2 = attributes.getQName(i3);
            if (qName2.startsWith(Constants.ATTRNAME_XMLNS) || "xmlns".equals(qName2)) {
                int colon2 = qName2.indexOf(58);
                if (colon2 > 0) {
                    prefix3 = qName2.substring(0, colon2);
                } else {
                    prefix3 = null;
                }
                appendNSDeclaration(this.m_prefixNames.stringToIndex(prefix3), this.m_nsNames.stringToIndex(attributes.getValue(i3)), attributes.getType(i3).equalsIgnoreCase("ID"));
            }
        }
        int i4 = nAtts - 1;
        while (true) {
            int i5 = i4;
            if (i5 >= 0) {
                String qName3 = attributes.getQName(i5);
                if (!qName3.startsWith(Constants.ATTRNAME_XMLNS) && !"xmlns".equals(qName3)) {
                    int colon3 = qName3.indexOf(i);
                    if (colon3 > 0) {
                        prefix = qName3.substring(i2, colon3);
                        localName2 = qName3.substring(colon3 + 1);
                    } else {
                        prefix = "";
                        localName2 = qName3;
                    }
                    String prefix5 = prefix;
                    String localName4 = localName2;
                    this.m_char.append(attributes.getValue(i5));
                    int contentEnd2 = this.m_char.length();
                    if ("xmlns".equals(prefix5) || "xmlns".equals(qName3)) {
                        contentEnd = contentEnd2;
                        localName3 = localName4;
                        prefix2 = prefix5;
                    } else {
                        int stringToIndex = this.m_nsNames.stringToIndex(attributes.getURI(i5));
                        int stringToIndex2 = this.m_localNames.stringToIndex(localName4);
                        int stringToIndex3 = this.m_prefixNames.stringToIndex(prefix5);
                        boolean equalsIgnoreCase = attributes.getType(i5).equalsIgnoreCase("ID");
                        int i6 = this.m_char_current_start;
                        int i7 = contentEnd2 - this.m_char_current_start;
                        contentEnd = contentEnd2;
                        boolean z = equalsIgnoreCase;
                        localName3 = localName4;
                        prefix2 = prefix5;
                        appendAttribute(stringToIndex, stringToIndex2, stringToIndex3, z, i6, i7);
                    }
                    this.m_char_current_start = contentEnd;
                    String str2 = localName3;
                    int i8 = colon3;
                    String str3 = prefix2;
                }
                i4 = i5 - 1;
                String str4 = qName3;
                attributes = atts;
                i = 58;
                i2 = 0;
            } else {
                return;
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

    /* access modifiers changed from: package-private */
    public final void initDocument(int documentNumber) {
        this.m_docHandle = documentNumber << 22;
        this.nodes.writeSlot(0, 9, -1, -1, 0);
        this.done = false;
    }

    public boolean hasChildNodes(int nodeHandle) {
        return getFirstChild(nodeHandle) != -1;
    }

    public int getFirstChild(int nodeHandle) {
        int nodeHandle2 = nodeHandle & NODEHANDLE_MASK;
        this.nodes.readSlot(nodeHandle2, this.gotslot);
        short type = (short) (this.gotslot[0] & DTMManager.IDENT_NODE_DEFAULT);
        if (type == 1 || type == 9 || type == 5) {
            int kid = nodeHandle2 + 1;
            this.nodes.readSlot(kid, this.gotslot);
            while (2 == (this.gotslot[0] & DTMManager.IDENT_NODE_DEFAULT)) {
                kid = this.gotslot[2];
                if (kid == -1) {
                    return -1;
                }
                this.nodes.readSlot(kid, this.gotslot);
            }
            if (this.gotslot[1] == nodeHandle2) {
                return this.m_docHandle | kid;
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
        int nodeHandle2 = nodeHandle & NODEHANDLE_MASK;
        this.nodes.readSlot(nodeHandle2, this.gotslot);
        short type = (short) (this.gotslot[0] & DTMManager.IDENT_NODE_DEFAULT);
        if (type == 1) {
            nodeHandle2++;
        }
        while (type == 2) {
            if (nsIndex == (this.gotslot[0] << 16) && this.gotslot[3] == nameIndex) {
                return this.m_docHandle | nodeHandle2;
            }
            nodeHandle2 = this.gotslot[2];
            this.nodes.readSlot(nodeHandle2, this.gotslot);
        }
        return -1;
    }

    public int getFirstAttribute(int nodeHandle) {
        int nodeHandle2 = nodeHandle & NODEHANDLE_MASK;
        int i = -1;
        if (1 != (this.nodes.readEntry(nodeHandle2, 0) & DTMManager.IDENT_NODE_DEFAULT)) {
            return -1;
        }
        int nodeHandle3 = nodeHandle2 + 1;
        if (2 == (this.nodes.readEntry(nodeHandle3, 0) & DTMManager.IDENT_NODE_DEFAULT)) {
            i = nodeHandle3 | this.m_docHandle;
        }
        return i;
    }

    public int getFirstNamespaceNode(int nodeHandle, boolean inScope) {
        return -1;
    }

    public int getNextSibling(int nodeHandle) {
        int nodeHandle2 = nodeHandle & NODEHANDLE_MASK;
        if (nodeHandle2 == 0) {
            return -1;
        }
        short type = (short) (this.nodes.readEntry(nodeHandle2, 0) & DTMManager.IDENT_NODE_DEFAULT);
        if (type == 1 || type == 2 || type == 5) {
            int nextSib = this.nodes.readEntry(nodeHandle2, 2);
            if (nextSib == -1) {
                return -1;
            }
            if (nextSib != 0) {
                return this.m_docHandle | nextSib;
            }
        }
        int thisParent = this.nodes.readEntry(nodeHandle2, 1);
        int nodeHandle3 = nodeHandle2 + 1;
        if (this.nodes.readEntry(nodeHandle3, 1) == thisParent) {
            return this.m_docHandle | nodeHandle3;
        }
        return -1;
    }

    public int getPreviousSibling(int nodeHandle) {
        int nodeHandle2 = nodeHandle & NODEHANDLE_MASK;
        if (nodeHandle2 == 0) {
            return -1;
        }
        int kid = -1;
        int nextkid = getFirstChild(this.nodes.readEntry(nodeHandle2, 1));
        while (nextkid != nodeHandle2) {
            kid = nextkid;
            nextkid = getNextSibling(nextkid);
        }
        return this.m_docHandle | kid;
    }

    public int getNextAttribute(int nodeHandle) {
        int nodeHandle2 = nodeHandle & NODEHANDLE_MASK;
        this.nodes.readSlot(nodeHandle2, this.gotslot);
        short type = (short) (this.gotslot[0] & DTMManager.IDENT_NODE_DEFAULT);
        if (type == 1) {
            return getFirstAttribute(nodeHandle2);
        }
        if (type != 2 || this.gotslot[2] == -1) {
            return -1;
        }
        return this.m_docHandle | this.gotslot[2];
    }

    public int getNextNamespaceNode(int baseHandle, int namespaceHandle, boolean inScope) {
        return -1;
    }

    public int getNextDescendant(int subtreeRootHandle, int nodeHandle) {
        int subtreeRootHandle2 = subtreeRootHandle & NODEHANDLE_MASK;
        int nodeHandle2 = nodeHandle & NODEHANDLE_MASK;
        if (nodeHandle2 == 0) {
            return -1;
        }
        while (true) {
            if (this.m_isError || (this.done && nodeHandle2 > this.nodes.slotsUsed())) {
                break;
            } else if (nodeHandle2 > subtreeRootHandle2) {
                this.nodes.readSlot(nodeHandle2 + 1, this.gotslot);
                if (this.gotslot[2] == 0) {
                    if (this.done != 0) {
                        break;
                    }
                } else if (((short) (this.gotslot[0] & DTMManager.IDENT_NODE_DEFAULT)) == 2) {
                    nodeHandle2 += 2;
                } else if (this.gotslot[1] >= subtreeRootHandle2) {
                    return this.m_docHandle | (nodeHandle2 + 1);
                }
            } else {
                nodeHandle2++;
            }
        }
        return -1;
    }

    public int getNextFollowing(int axisContextHandle, int nodeHandle) {
        return -1;
    }

    public int getNextPreceding(int axisContextHandle, int nodeHandle) {
        int nodeHandle2 = nodeHandle & NODEHANDLE_MASK;
        while (nodeHandle2 > 1) {
            nodeHandle2--;
            if (2 != (this.nodes.readEntry(nodeHandle2, 0) & DTMManager.IDENT_NODE_DEFAULT)) {
                return this.m_docHandle | this.nodes.specialFind(axisContextHandle, nodeHandle2);
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
        int nodetype = this.gotslot[0] & WalkerFactory.BITS_COUNT;
        String value = null;
        if (nodetype != 8) {
            switch (nodetype) {
                case 3:
                case 4:
                    break;
            }
        }
        value = this.m_char.getString(this.gotslot[2], this.gotslot[3]);
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
        String localName = qName.substring(qName.indexOf(":") + 1);
        String namespace = this.m_nsNames.indexToString(this.gotslot[0] << 16);
        return this.m_nsNames.stringToIndex(namespace + ":" + localName);
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
        PrintStream printStream = System.out;
        printStream.println("got i=" + i + " " + (i >> 16) + PsuedoNames.PSEUDONAME_ROOT + (i & DTMManager.IDENT_NODE_DEFAULT));
        String name2 = this.m_localNames.indexToString(65535 & i);
        String prefix = this.m_prefixNames.indexToString(i >> 16);
        if (prefix == null || prefix.length() <= 0) {
            return name2;
        }
        return prefix + ":" + name2;
    }

    public String getNodeNameX(int nodeHandle) {
        return null;
    }

    public String getLocalName(int nodeHandle) {
        this.nodes.readSlot(nodeHandle, this.gotslot);
        short type = (short) (this.gotslot[0] & DTMManager.IDENT_NODE_DEFAULT);
        if (type != 1 && type != 2) {
            return "";
        }
        String name = this.m_localNames.indexToString(65535 & this.gotslot[3]);
        if (name == null) {
            return "";
        }
        return name;
    }

    public String getPrefix(int nodeHandle) {
        this.nodes.readSlot(nodeHandle, this.gotslot);
        short type = (short) (this.gotslot[0] & DTMManager.IDENT_NODE_DEFAULT);
        if (type != 1 && type != 2) {
            return "";
        }
        String name = this.m_prefixNames.indexToString(this.gotslot[3] >> 16);
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
        int nodetype = this.gotslot[0] & WalkerFactory.BITS_COUNT;
        if (nodetype != 8) {
            switch (nodetype) {
                case 2:
                    this.nodes.readSlot(nodeHandle + 1, this.gotslot);
                    break;
                case 3:
                case 4:
                    break;
                default:
                    return null;
            }
        }
        return this.m_char.getString(this.gotslot[2], this.gotslot[3]);
    }

    public short getNodeType(int nodeHandle) {
        return (short) (this.nodes.readEntry(nodeHandle, 0) & DTMManager.IDENT_NODE_DEFAULT);
    }

    public short getLevel(int nodeHandle) {
        short count = 0;
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
        if ((DOCHANDLE_MASK & newChild) != this.m_docHandle) {
        }
    }

    public void appendTextChild(String str) {
    }

    /* access modifiers changed from: package-private */
    public void appendTextChild(int m_char_current_start2, int contentLength) {
        this.previousSibling = appendNode(3, this.currentParent, m_char_current_start2, contentLength);
    }

    /* access modifiers changed from: package-private */
    public void appendComment(int m_char_current_start2, int contentLength) {
        this.previousSibling = appendNode(8, this.currentParent, m_char_current_start2, contentLength);
    }

    /* access modifiers changed from: package-private */
    public void appendStartElement(int namespaceIndex, int localNameIndex, int prefixIndex) {
        int w1 = this.currentParent;
        int w3 = (prefixIndex << 16) | localNameIndex;
        PrintStream printStream = System.out;
        printStream.println("set w3=" + w3 + " " + (w3 >> 16) + PsuedoNames.PSEUDONAME_ROOT + (65535 & w3));
        int ourslot = appendNode((namespaceIndex << 16) | 1, w1, 0, w3);
        this.currentParent = ourslot;
        this.previousSibling = 0;
        if (this.m_docElement == -1) {
            this.m_docElement = ourslot;
        }
    }

    /* access modifiers changed from: package-private */
    public void appendNSDeclaration(int prefixIndex, int namespaceIndex, boolean isID) {
        int stringToIndex = this.m_nsNames.stringToIndex(SerializerConstants.XMLNS_URI);
        this.previousSibling = appendNode((this.m_nsNames.stringToIndex(SerializerConstants.XMLNS_URI) << 16) | 13, this.currentParent, 0, namespaceIndex);
        this.previousSiblingWasParent = false;
    }

    /* access modifiers changed from: package-private */
    public void appendAttribute(int namespaceIndex, int localNameIndex, int prefixIndex, boolean isID, int m_char_current_start2, int contentLength) {
        int w1 = this.currentParent;
        int w3 = (prefixIndex << 16) | localNameIndex;
        PrintStream printStream = System.out;
        printStream.println("set w3=" + w3 + " " + (w3 >> 16) + PsuedoNames.PSEUDONAME_ROOT + (65535 & w3));
        int ourslot = appendNode((namespaceIndex << 16) | 2, w1, 0, w3);
        this.previousSibling = ourslot;
        appendNode(3, ourslot, m_char_current_start2, contentLength);
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

    /* access modifiers changed from: package-private */
    public void appendEndElement() {
        if (this.previousSiblingWasParent) {
            this.nodes.writeEntry(this.previousSibling, 2, -1);
        }
        this.previousSibling = this.currentParent;
        this.nodes.readSlot(this.currentParent, this.gotslot);
        this.currentParent = this.gotslot[1] & DTMManager.IDENT_NODE_DEFAULT;
        this.previousSiblingWasParent = true;
    }

    /* access modifiers changed from: package-private */
    public void appendStartDocument() {
        this.m_docElement = -1;
        initDocument(0);
    }

    /* access modifiers changed from: package-private */
    public void appendEndDocument() {
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

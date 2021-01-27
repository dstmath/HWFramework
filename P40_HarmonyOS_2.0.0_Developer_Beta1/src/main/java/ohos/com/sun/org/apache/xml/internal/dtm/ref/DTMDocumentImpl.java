package ohos.com.sun.org.apache.xml.internal.dtm.ref;

import java.io.PrintStream;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaSymbols;
import ohos.com.sun.org.apache.xml.internal.dtm.DTM;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisTraverser;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMManager;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMWSFilter;
import ohos.com.sun.org.apache.xml.internal.utils.FastStringBuffer;
import ohos.com.sun.org.apache.xml.internal.utils.XMLString;
import ohos.com.sun.org.apache.xml.internal.utils.XMLStringFactory;
import ohos.com.sun.org.apache.xpath.internal.compiler.PsuedoNames;
import ohos.javax.xml.transform.SourceLocator;
import ohos.org.w3c.dom.Node;
import ohos.org.xml.sax.Attributes;
import ohos.org.xml.sax.ContentHandler;
import ohos.org.xml.sax.DTDHandler;
import ohos.org.xml.sax.EntityResolver;
import ohos.org.xml.sax.ErrorHandler;
import ohos.org.xml.sax.Locator;
import ohos.org.xml.sax.SAXException;
import ohos.org.xml.sax.ext.DeclHandler;
import ohos.org.xml.sax.ext.LexicalHandler;

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

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public void appendTextChild(String str) {
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public void dispatchCharactersEvents(int i, ContentHandler contentHandler, boolean z) throws SAXException {
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public void dispatchToEvents(int i, ContentHandler contentHandler) throws SAXException {
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public void documentRegistration() {
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public void documentRelease() {
    }

    public void endCDATA() throws SAXException {
    }

    public void endDTD() throws SAXException {
    }

    public void endEntity(String str) throws SAXException {
    }

    public void endPrefixMapping(String str) throws SAXException {
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public DTMAxisIterator getAxisIterator(int i) {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public DTMAxisTraverser getAxisTraverser(int i) {
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
    public boolean getDocumentAllDeclarationsProcessed() {
        return false;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public String getDocumentEncoding(int i) {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public int getDocumentRoot(int i) {
        if ((NODEHANDLE_MASK & i) == 0) {
            return -1;
        }
        return DOCHANDLE_MASK & i;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public String getDocumentStandalone(int i) {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public String getDocumentSystemIdentifier(int i) {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public String getDocumentTypeDeclarationPublicIdentifier() {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public String getDocumentTypeDeclarationSystemIdentifier() {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public String getDocumentVersion(int i) {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public int getElementById(String str) {
        return 0;
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
    public int getFirstNamespaceNode(int i, boolean z) {
        return -1;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public String getNamespaceURI(int i) {
        return null;
    }

    public int getNextFollowing(int i, int i2) {
        return -1;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public int getNextNamespaceNode(int i, int i2, boolean z) {
        return -1;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public Node getNode(int i) {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public String getNodeNameX(int i) {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public int getOwnerDocument(int i) {
        if ((NODEHANDLE_MASK & i) == 0) {
            return -1;
        }
        return DOCHANDLE_MASK & i;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public SourceLocator getSourceLocatorFor(int i) {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public char[] getStringValueChunk(int i, int i2, int[] iArr) {
        return new char[0];
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public int getStringValueChunkCount(int i) {
        return 0;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public DTMAxisIterator getTypedAxisIterator(int i, int i2) {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public String getUnparsedEntityURI(String str) {
        return null;
    }

    public void ignorableWhitespace(char[] cArr, int i, int i2) throws SAXException {
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public boolean isAttributeSpecified(int i) {
        return false;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public boolean isCharacterElementContentWhitespace(int i) {
        return false;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public boolean isDocumentAllDeclarationsProcessed(int i) {
        return false;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public boolean isNodeAfter(int i, int i2) {
        return false;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public boolean isSupported(String str, String str2) {
        return false;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public void migrateTo(DTMManager dTMManager) {
    }

    public void setDocumentLocator(Locator locator) {
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public void setFeature(String str, boolean z) {
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public void setProperty(String str, Object obj) {
    }

    public void startCDATA() throws SAXException {
    }

    public void startDTD(String str, String str2, String str3) throws SAXException {
    }

    public void startEntity(String str) throws SAXException {
    }

    public void startPrefixMapping(String str, String str2) throws SAXException {
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public boolean supportsPreStripping() {
        return false;
    }

    public DTMDocumentImpl(DTMManager dTMManager, int i, DTMWSFilter dTMWSFilter, XMLStringFactory xMLStringFactory) {
        initDocument(i);
        this.m_xsf = xMLStringFactory;
    }

    public void setIncrementalSAXSource(IncrementalSAXSource incrementalSAXSource) {
        this.m_incrSAXSource = incrementalSAXSource;
        incrementalSAXSource.setContentHandler(this);
        incrementalSAXSource.setLexicalHandler(this);
    }

    private final int appendNode(int i, int i2, int i3, int i4) {
        int appendSlot = this.nodes.appendSlot(i, i2, i3, i4);
        if (this.previousSiblingWasParent) {
            this.nodes.writeEntry(this.previousSibling, 2, appendSlot);
        }
        this.previousSiblingWasParent = false;
        return appendSlot;
    }

    public void setLocalNameTable(DTMStringPool dTMStringPool) {
        this.m_localNames = dTMStringPool;
    }

    public DTMStringPool getLocalNameTable() {
        return this.m_localNames;
    }

    public void setNsNameTable(DTMStringPool dTMStringPool) {
        this.m_nsNames = dTMStringPool;
    }

    public DTMStringPool getNsNameTable() {
        return this.m_nsNames;
    }

    public void setPrefixNameTable(DTMStringPool dTMStringPool) {
        this.m_prefixNames = dTMStringPool;
    }

    public DTMStringPool getPrefixNameTable() {
        return this.m_prefixNames;
    }

    /* access modifiers changed from: package-private */
    public void setContentBuffer(FastStringBuffer fastStringBuffer) {
        this.m_char = fastStringBuffer;
    }

    /* access modifiers changed from: package-private */
    public FastStringBuffer getContentBuffer() {
        return this.m_char;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public ContentHandler getContentHandler() {
        ContentHandler contentHandler = this.m_incrSAXSource;
        return contentHandler instanceof IncrementalSAXSource_Filter ? contentHandler : this;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public LexicalHandler getLexicalHandler() {
        LexicalHandler lexicalHandler = this.m_incrSAXSource;
        return lexicalHandler instanceof IncrementalSAXSource_Filter ? lexicalHandler : this;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public boolean needsTwoThreads() {
        return this.m_incrSAXSource != null;
    }

    public void characters(char[] cArr, int i, int i2) throws SAXException {
        this.m_char.append(cArr, i, i2);
    }

    private void processAccumulatedText() {
        int length = this.m_char.length();
        int i = this.m_char_current_start;
        if (length != i) {
            appendTextChild(i, length - i);
            this.m_char_current_start = length;
        }
    }

    public void endDocument() throws SAXException {
        appendEndDocument();
    }

    public void endElement(String str, String str2, String str3) throws SAXException {
        processAccumulatedText();
        appendEndElement();
    }

    public void processingInstruction(String str, String str2) throws SAXException {
        processAccumulatedText();
    }

    public void skippedEntity(String str) throws SAXException {
        processAccumulatedText();
    }

    public void startDocument() throws SAXException {
        appendStartDocument();
    }

    public void startElement(String str, String str2, String str3, Attributes attributes) throws SAXException {
        String str4;
        String str5;
        processAccumulatedText();
        int indexOf = str3.indexOf(58);
        String substring = indexOf > 0 ? str3.substring(0, indexOf) : null;
        System.out.println("Prefix=" + substring + " index=" + this.m_prefixNames.stringToIndex(substring));
        appendStartElement(this.m_nsNames.stringToIndex(str), this.m_localNames.stringToIndex(str2), this.m_prefixNames.stringToIndex(substring));
        int length = (attributes == null ? 0 : attributes.getLength()) - 1;
        for (int i = length; i >= 0; i--) {
            String qName = attributes.getQName(i);
            if (qName.startsWith("xmlns:") || "xmlns".equals(qName)) {
                int indexOf2 = qName.indexOf(58);
                appendNSDeclaration(this.m_prefixNames.stringToIndex(indexOf2 > 0 ? qName.substring(0, indexOf2) : null), this.m_nsNames.stringToIndex(attributes.getValue(i)), attributes.getType(i).equalsIgnoreCase(SchemaSymbols.ATTVAL_ID));
            }
        }
        for (int i2 = length; i2 >= 0; i2--) {
            String qName2 = attributes.getQName(i2);
            if (!qName2.startsWith("xmlns:") && !"xmlns".equals(qName2)) {
                int indexOf3 = qName2.indexOf(58);
                if (indexOf3 > 0) {
                    str4 = qName2.substring(0, indexOf3);
                    str5 = qName2.substring(indexOf3 + 1);
                } else {
                    str4 = "";
                    str5 = qName2;
                }
                this.m_char.append(attributes.getValue(i2));
                int length2 = this.m_char.length();
                if (!"xmlns".equals(str4) && !"xmlns".equals(qName2)) {
                    int stringToIndex = this.m_nsNames.stringToIndex(attributes.getURI(i2));
                    int stringToIndex2 = this.m_localNames.stringToIndex(str5);
                    int stringToIndex3 = this.m_prefixNames.stringToIndex(str4);
                    boolean equalsIgnoreCase = attributes.getType(i2).equalsIgnoreCase(SchemaSymbols.ATTVAL_ID);
                    int i3 = this.m_char_current_start;
                    appendAttribute(stringToIndex, stringToIndex2, stringToIndex3, equalsIgnoreCase, i3, length2 - i3);
                }
                this.m_char_current_start = length2;
            }
        }
    }

    public void comment(char[] cArr, int i, int i2) throws SAXException {
        processAccumulatedText();
        this.m_char.append(cArr, i, i2);
        appendComment(this.m_char_current_start, i2);
        this.m_char_current_start += i2;
    }

    /* access modifiers changed from: package-private */
    public final void initDocument(int i) {
        this.m_docHandle = i << 22;
        this.nodes.writeSlot(0, 9, -1, -1, 0);
        this.done = false;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public boolean hasChildNodes(int i) {
        return getFirstChild(i) != -1;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public int getFirstChild(int i) {
        int i2 = i & NODEHANDLE_MASK;
        this.nodes.readSlot(i2, this.gotslot);
        short s = (short) (this.gotslot[0] & 65535);
        if (s == 1 || s == 9 || s == 5) {
            int i3 = i2 + 1;
            this.nodes.readSlot(i3, this.gotslot);
            while (true) {
                int[] iArr = this.gotslot;
                if (2 == (iArr[0] & 65535)) {
                    i3 = iArr[2];
                    if (i3 == -1) {
                        return -1;
                    }
                    this.nodes.readSlot(i3, iArr);
                } else if (iArr[1] == i2) {
                    return this.m_docHandle | i3;
                }
            }
        }
        return -1;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public int getLastChild(int i) {
        int firstChild = getFirstChild(i & NODEHANDLE_MASK);
        int i2 = -1;
        while (firstChild != -1) {
            i2 = firstChild;
            firstChild = getNextSibling(firstChild);
        }
        return this.m_docHandle | i2;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public int getAttributeNode(int i, String str, String str2) {
        int stringToIndex = this.m_nsNames.stringToIndex(str);
        int stringToIndex2 = this.m_localNames.stringToIndex(str2);
        int i2 = i & NODEHANDLE_MASK;
        this.nodes.readSlot(i2, this.gotslot);
        short s = (short) (this.gotslot[0] & 65535);
        if (s == 1) {
            i2++;
        }
        while (s == 2) {
            int[] iArr = this.gotslot;
            if (stringToIndex == (iArr[0] << 16) && iArr[3] == stringToIndex2) {
                return this.m_docHandle | i2;
            }
            int[] iArr2 = this.gotslot;
            int i3 = iArr2[2];
            this.nodes.readSlot(i3, iArr2);
            i2 = i3;
        }
        return -1;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public int getFirstAttribute(int i) {
        int i2 = i & NODEHANDLE_MASK;
        if (1 != (this.nodes.readEntry(i2, 0) & 65535)) {
            return -1;
        }
        int i3 = i2 + 1;
        if (2 == (this.nodes.readEntry(i3, 0) & 65535)) {
            return i3 | this.m_docHandle;
        }
        return -1;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public int getNextSibling(int i) {
        int i2 = i & NODEHANDLE_MASK;
        if (i2 == 0) {
            return -1;
        }
        short readEntry = (short) (this.nodes.readEntry(i2, 0) & 65535);
        if (readEntry == 1 || readEntry == 2 || readEntry == 5) {
            int readEntry2 = this.nodes.readEntry(i2, 2);
            if (readEntry2 == -1) {
                return -1;
            }
            if (readEntry2 != 0) {
                return this.m_docHandle | readEntry2;
            }
        }
        int readEntry3 = this.nodes.readEntry(i2, 1);
        int i3 = i2 + 1;
        if (this.nodes.readEntry(i3, 1) == readEntry3) {
            return this.m_docHandle | i3;
        }
        return -1;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public int getPreviousSibling(int i) {
        int i2 = i & NODEHANDLE_MASK;
        int i3 = -1;
        if (i2 == 0) {
            return -1;
        }
        int firstChild = getFirstChild(this.nodes.readEntry(i2, 1));
        while (true) {
            i3 = firstChild;
            if (i3 == i2) {
                return this.m_docHandle | i3;
            }
            firstChild = getNextSibling(i3);
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public int getNextAttribute(int i) {
        int i2 = i & NODEHANDLE_MASK;
        this.nodes.readSlot(i2, this.gotslot);
        int[] iArr = this.gotslot;
        short s = (short) (iArr[0] & 65535);
        if (s == 1) {
            return getFirstAttribute(i2);
        }
        if (s != 2 || iArr[2] == -1) {
            return -1;
        }
        return this.m_docHandle | iArr[2];
    }

    public int getNextDescendant(int i, int i2) {
        int i3 = i & NODEHANDLE_MASK;
        int i4 = i2 & NODEHANDLE_MASK;
        if (i4 == 0) {
            return -1;
        }
        while (true) {
            if (this.m_isError || (this.done && i4 > this.nodes.slotsUsed())) {
                break;
            } else if (i4 > i3) {
                int i5 = i4 + 1;
                this.nodes.readSlot(i5, this.gotslot);
                int[] iArr = this.gotslot;
                if (iArr[2] == 0) {
                    if (this.done) {
                        break;
                    }
                } else if (((short) (iArr[0] & 65535)) == 2) {
                    i4 += 2;
                } else if (iArr[1] >= i3) {
                    return this.m_docHandle | i5;
                }
            } else {
                i4++;
            }
        }
        return -1;
    }

    public int getNextPreceding(int i, int i2) {
        int i3 = i2 & NODEHANDLE_MASK;
        while (i3 > 1) {
            i3--;
            if (2 != (this.nodes.readEntry(i3, 0) & 65535)) {
                return this.nodes.specialFind(i, i3) | this.m_docHandle;
            }
        }
        return -1;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public int getParent(int i) {
        return this.nodes.readEntry(i, 1) | this.m_docHandle;
    }

    public int getDocumentRoot() {
        return this.m_docElement | this.m_docHandle;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public int getDocument() {
        return this.m_docHandle;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public XMLString getStringValue(int i) {
        String str;
        this.nodes.readSlot(i, this.gotslot);
        int i2 = this.gotslot[0] & 255;
        if (i2 == 3 || i2 == 4 || i2 == 8) {
            FastStringBuffer fastStringBuffer = this.m_char;
            int[] iArr = this.gotslot;
            str = fastStringBuffer.getString(iArr[2], iArr[3]);
        } else {
            str = null;
        }
        return this.m_xsf.newstr(str);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public int getExpandedTypeID(int i) {
        this.nodes.readSlot(i, this.gotslot);
        String indexToString = this.m_localNames.indexToString(this.gotslot[3]);
        String substring = indexToString.substring(indexToString.indexOf(":") + 1);
        String indexToString2 = this.m_nsNames.indexToString(this.gotslot[0] << 16);
        return this.m_nsNames.stringToIndex(indexToString2 + ":" + substring);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public int getExpandedTypeID(String str, String str2, int i) {
        return this.m_nsNames.stringToIndex(str + ":" + str2);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public String getLocalNameFromExpandedNameID(int i) {
        String indexToString = this.m_localNames.indexToString(i);
        return indexToString.substring(indexToString.indexOf(":") + 1);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public String getNamespaceFromExpandedNameID(int i) {
        String indexToString = this.m_localNames.indexToString(i);
        return indexToString.substring(0, indexToString.indexOf(":"));
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public String getNodeName(int i) {
        this.nodes.readSlot(i, this.gotslot);
        int[] iArr = this.gotslot;
        String str = fixednames[(short) (iArr[0] & 65535)];
        if (str != null) {
            return str;
        }
        int i2 = iArr[3];
        PrintStream printStream = System.out;
        StringBuilder sb = new StringBuilder();
        sb.append("got i=");
        sb.append(i2);
        sb.append(" ");
        int i3 = i2 >> 16;
        sb.append(i3);
        sb.append(PsuedoNames.PSEUDONAME_ROOT);
        int i4 = i2 & 65535;
        sb.append(i4);
        printStream.println(sb.toString());
        String indexToString = this.m_localNames.indexToString(i4);
        String indexToString2 = this.m_prefixNames.indexToString(i3);
        if (indexToString2 == null || indexToString2.length() <= 0) {
            return indexToString;
        }
        return indexToString2 + ":" + indexToString;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public String getLocalName(int i) {
        this.nodes.readSlot(i, this.gotslot);
        short s = (short) (this.gotslot[0] & 65535);
        if (s != 1 && s != 2) {
            return "";
        }
        String indexToString = this.m_localNames.indexToString(this.gotslot[3] & 65535);
        if (indexToString == null) {
            return "";
        }
        return indexToString;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public String getPrefix(int i) {
        this.nodes.readSlot(i, this.gotslot);
        short s = (short) (this.gotslot[0] & 65535);
        if (s != 1 && s != 2) {
            return "";
        }
        String indexToString = this.m_prefixNames.indexToString(this.gotslot[3] >> 16);
        if (indexToString == null) {
            return "";
        }
        return indexToString;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public String getNodeValue(int i) {
        this.nodes.readSlot(i, this.gotslot);
        int[] iArr = this.gotslot;
        int i2 = iArr[0] & 255;
        if (i2 == 2) {
            this.nodes.readSlot(i + 1, iArr);
        } else if (!(i2 == 3 || i2 == 4 || i2 == 8)) {
            return null;
        }
        FastStringBuffer fastStringBuffer = this.m_char;
        int[] iArr2 = this.gotslot;
        return fastStringBuffer.getString(iArr2[2], iArr2[3]);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public short getNodeType(int i) {
        return (short) (this.nodes.readEntry(i, 0) & 65535);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public short getLevel(int i) {
        short s = 0;
        while (i != 0) {
            s = (short) (s + 1);
            i = this.nodes.readEntry(i, 1);
        }
        return s;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public String getDocumentBaseURI() {
        return this.m_documentBaseURI;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public void setDocumentBaseURI(String str) {
        this.m_documentBaseURI = str;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public void appendChild(int i, boolean z, boolean z2) {
        int i2 = i & DOCHANDLE_MASK;
        int i3 = this.m_docHandle;
    }

    /* access modifiers changed from: package-private */
    public void appendTextChild(int i, int i2) {
        this.previousSibling = appendNode(3, this.currentParent, i, i2);
    }

    /* access modifiers changed from: package-private */
    public void appendComment(int i, int i2) {
        this.previousSibling = appendNode(8, this.currentParent, i, i2);
    }

    /* access modifiers changed from: package-private */
    public void appendStartElement(int i, int i2, int i3) {
        int i4 = this.currentParent;
        int i5 = i2 | (i3 << 16);
        PrintStream printStream = System.out;
        printStream.println("set w3=" + i5 + " " + (i5 >> 16) + PsuedoNames.PSEUDONAME_ROOT + (65535 & i5));
        int appendNode = appendNode((i << 16) | 1, i4, 0, i5);
        this.currentParent = appendNode;
        this.previousSibling = 0;
        if (this.m_docElement == -1) {
            this.m_docElement = appendNode;
        }
    }

    /* access modifiers changed from: package-private */
    public void appendNSDeclaration(int i, int i2, boolean z) {
        this.m_nsNames.stringToIndex("http://www.w3.org/2000/xmlns/");
        this.previousSibling = appendNode((this.m_nsNames.stringToIndex("http://www.w3.org/2000/xmlns/") << 16) | 13, this.currentParent, 0, i2);
        this.previousSiblingWasParent = false;
    }

    /* access modifiers changed from: package-private */
    public void appendAttribute(int i, int i2, int i3, boolean z, int i4, int i5) {
        int i6 = this.currentParent;
        int i7 = i2 | (i3 << 16);
        PrintStream printStream = System.out;
        printStream.println("set w3=" + i7 + " " + (i7 >> 16) + PsuedoNames.PSEUDONAME_ROOT + (65535 & i7));
        int appendNode = appendNode((i << 16) | 2, i6, 0, i7);
        this.previousSibling = appendNode;
        appendNode(3, appendNode, i4, i5);
        this.previousSiblingWasParent = true;
    }

    /* access modifiers changed from: package-private */
    public void appendEndElement() {
        if (this.previousSiblingWasParent) {
            this.nodes.writeEntry(this.previousSibling, 2, -1);
        }
        int i = this.currentParent;
        this.previousSibling = i;
        this.nodes.readSlot(i, this.gotslot);
        this.currentParent = this.gotslot[1] & 65535;
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
}

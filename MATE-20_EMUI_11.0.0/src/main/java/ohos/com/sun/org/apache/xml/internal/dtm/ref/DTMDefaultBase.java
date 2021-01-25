package ohos.com.sun.org.apache.xml.internal.dtm.ref;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Vector;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaSymbols;
import ohos.com.sun.org.apache.xml.internal.dtm.DTM;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMAxisTraverser;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMException;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMManager;
import ohos.com.sun.org.apache.xml.internal.dtm.DTMWSFilter;
import ohos.com.sun.org.apache.xml.internal.res.XMLMessages;
import ohos.com.sun.org.apache.xml.internal.utils.BoolStack;
import ohos.com.sun.org.apache.xml.internal.utils.SuballocatedIntVector;
import ohos.com.sun.org.apache.xml.internal.utils.XMLString;
import ohos.com.sun.org.apache.xml.internal.utils.XMLStringFactory;
import ohos.global.icu.text.PluralRules;
import ohos.javax.xml.transform.Source;
import ohos.org.w3c.dom.Node;
import ohos.org.xml.sax.ContentHandler;
import ohos.org.xml.sax.SAXException;

public abstract class DTMDefaultBase implements DTM {
    public static final int DEFAULT_BLOCKSIZE = 512;
    public static final int DEFAULT_NUMBLOCKS = 32;
    public static final int DEFAULT_NUMBLOCKS_SMALL = 4;
    static final boolean JJK_DEBUG = false;
    protected static final int NOTPROCESSED = -2;
    public static final int ROOTNODE = 0;
    protected String m_documentBaseURI;
    protected SuballocatedIntVector m_dtmIdent;
    protected int[][][] m_elemIndexes;
    protected ExpandedNameTable m_expandedNameTable;
    protected SuballocatedIntVector m_exptype;
    protected SuballocatedIntVector m_firstch;
    protected boolean m_indexing;
    public DTMManager m_mgr;
    protected DTMManagerDefault m_mgrDefault;
    protected SuballocatedIntVector m_namespaceDeclSetElements;
    protected Vector m_namespaceDeclSets;
    private Vector m_namespaceLists;
    protected SuballocatedIntVector m_nextsib;
    protected SuballocatedIntVector m_parent;
    protected SuballocatedIntVector m_prevsib;
    protected boolean m_shouldStripWS;
    protected BoolStack m_shouldStripWhitespaceStack;
    protected int m_size;
    protected DTMAxisTraverser[] m_traversers;
    protected DTMWSFilter m_wsfilter;
    protected XMLStringFactory m_xstrf;

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public abstract void dispatchCharactersEvents(int i, ContentHandler contentHandler, boolean z) throws SAXException;

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public abstract void dispatchToEvents(int i, ContentHandler contentHandler) throws SAXException;

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public void documentRegistration() {
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public void documentRelease() {
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public abstract int getAttributeNode(int i, String str, String str2);

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public boolean getDocumentAllDeclarationsProcessed() {
        return true;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public String getDocumentEncoding(int i) {
        return "UTF-8";
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public String getDocumentStandalone(int i) {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public abstract String getDocumentTypeDeclarationPublicIdentifier();

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public abstract String getDocumentTypeDeclarationSystemIdentifier();

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public String getDocumentVersion(int i) {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public abstract int getElementById(String str);

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public abstract String getLocalName(int i);

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public abstract String getNamespaceURI(int i);

    /* access modifiers changed from: protected */
    public abstract int getNextNodeIdentity(int i);

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public abstract String getNodeName(int i);

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public abstract String getNodeValue(int i);

    /* access modifiers changed from: protected */
    public abstract int getNumberOfNodes();

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public abstract String getPrefix(int i);

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public abstract XMLString getStringValue(int i);

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public abstract String getUnparsedEntityURI(String str);

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public abstract boolean isAttributeSpecified(int i);

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public boolean isCharacterElementContentWhitespace(int i) {
        return false;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public boolean isDocumentAllDeclarationsProcessed(int i) {
        return true;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public boolean isSupported(String str, String str2) {
        return false;
    }

    /* access modifiers changed from: protected */
    public abstract boolean nextNode();

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public void setFeature(String str, boolean z) {
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public boolean supportsPreStripping() {
        return true;
    }

    public DTMDefaultBase(DTMManager dTMManager, Source source, int i, DTMWSFilter dTMWSFilter, XMLStringFactory xMLStringFactory, boolean z) {
        this(dTMManager, source, i, dTMWSFilter, xMLStringFactory, z, 512, true, false);
    }

    public DTMDefaultBase(DTMManager dTMManager, Source source, int i, DTMWSFilter dTMWSFilter, XMLStringFactory xMLStringFactory, boolean z, int i2, boolean z2, boolean z3) {
        this.m_size = 0;
        String str = null;
        this.m_namespaceDeclSets = null;
        this.m_namespaceDeclSetElements = null;
        this.m_mgrDefault = null;
        this.m_shouldStripWS = false;
        this.m_namespaceLists = null;
        int i3 = 4;
        if (i2 <= 64) {
            this.m_dtmIdent = new SuballocatedIntVector(4, 1);
        } else {
            this.m_dtmIdent = new SuballocatedIntVector(32);
            i3 = 32;
        }
        this.m_exptype = new SuballocatedIntVector(i2, i3);
        this.m_firstch = new SuballocatedIntVector(i2, i3);
        this.m_nextsib = new SuballocatedIntVector(i2, i3);
        this.m_parent = new SuballocatedIntVector(i2, i3);
        if (z2) {
            this.m_prevsib = new SuballocatedIntVector(i2, i3);
        }
        this.m_mgr = dTMManager;
        if (dTMManager instanceof DTMManagerDefault) {
            this.m_mgrDefault = (DTMManagerDefault) dTMManager;
        }
        this.m_documentBaseURI = source != null ? source.getSystemId() : str;
        this.m_dtmIdent.setElementAt(i, 0);
        this.m_wsfilter = dTMWSFilter;
        this.m_xstrf = xMLStringFactory;
        this.m_indexing = z;
        if (z) {
            this.m_expandedNameTable = new ExpandedNameTable();
        } else {
            this.m_expandedNameTable = this.m_mgrDefault.getExpandedNameTable(this);
        }
        if (dTMWSFilter != null) {
            this.m_shouldStripWhitespaceStack = new BoolStack();
            pushShouldStripWhitespace(false);
        }
    }

    /* access modifiers changed from: protected */
    public void ensureSizeOfIndex(int i, int i2) {
        int[][][] iArr = this.m_elemIndexes;
        if (iArr == null) {
            this.m_elemIndexes = new int[(i + 20)][][];
        } else if (iArr.length <= i) {
            this.m_elemIndexes = new int[(i + 20)][][];
            System.arraycopy(iArr, 0, this.m_elemIndexes, 0, iArr.length);
        }
        int[][][] iArr2 = this.m_elemIndexes;
        int[][] iArr3 = iArr2[i];
        if (iArr3 == null) {
            iArr3 = new int[(i2 + 100)][];
            iArr2[i] = iArr3;
        } else if (iArr3.length <= i2) {
            int[][] iArr4 = new int[(i2 + 100)][];
            System.arraycopy(iArr3, 0, iArr4, 0, iArr3.length);
            this.m_elemIndexes[i] = iArr4;
            iArr3 = iArr4;
        }
        int[] iArr5 = iArr3[i2];
        if (iArr5 == null) {
            int[] iArr6 = new int[128];
            iArr3[i2] = iArr6;
            iArr6[0] = 1;
        } else if (iArr5.length <= iArr5[0] + 1) {
            int[] iArr7 = new int[(iArr5[0] + 1024)];
            System.arraycopy(iArr5, 0, iArr7, 0, iArr5.length);
            iArr3[i2] = iArr7;
        }
    }

    /* access modifiers changed from: protected */
    public void indexNode(int i, int i2) {
        ExpandedNameTable expandedNameTable = this.m_expandedNameTable;
        if (1 == expandedNameTable.getType(i)) {
            int namespaceID = expandedNameTable.getNamespaceID(i);
            int localNameID = expandedNameTable.getLocalNameID(i);
            ensureSizeOfIndex(namespaceID, localNameID);
            int[] iArr = this.m_elemIndexes[namespaceID][localNameID];
            iArr[iArr[0]] = i2;
            iArr[0] = iArr[0] + 1;
        }
    }

    /* access modifiers changed from: protected */
    public int findGTE(int[] iArr, int i, int i2, int i3) {
        int i4 = (i2 - 1) + i;
        int i5 = i4;
        while (i <= i5) {
            int i6 = (i + i5) >>> 1;
            int i7 = iArr[i6];
            if (i7 > i3) {
                i5 = i6 - 1;
            } else if (i7 >= i3) {
                return i6;
            } else {
                i = i6 + 1;
            }
        }
        if (i > i4 || iArr[i] <= i3) {
            return -1;
        }
        return i;
    }

    /* access modifiers changed from: package-private */
    public int findElementFromIndex(int i, int i2, int i3) {
        int[][] iArr;
        int[] iArr2;
        int findGTE;
        int[][][] iArr3 = this.m_elemIndexes;
        if (iArr3 == null || i >= iArr3.length || (iArr = iArr3[i]) == null || i2 >= iArr.length || (iArr2 = iArr[i2]) == null || (findGTE = findGTE(iArr2, 1, iArr2[0], i3)) <= -1) {
            return -2;
        }
        return iArr2[findGTE];
    }

    /* access modifiers changed from: protected */
    public short _type(int i) {
        int _exptype = _exptype(i);
        if (-1 != _exptype) {
            return this.m_expandedNameTable.getType(_exptype);
        }
        return -1;
    }

    /* access modifiers changed from: protected */
    public int _exptype(int i) {
        if (i == -1) {
            return -1;
        }
        while (i >= this.m_size) {
            if (!nextNode() && i >= this.m_size) {
                return -1;
            }
        }
        return this.m_exptype.elementAt(i);
    }

    /* access modifiers changed from: protected */
    public int _level(int i) {
        while (i >= this.m_size) {
            if (!nextNode() && i >= this.m_size) {
                return -1;
            }
        }
        int i2 = 0;
        while (true) {
            i = _parent(i);
            if (-1 == i) {
                return i2;
            }
            i2++;
        }
    }

    /* access modifiers changed from: protected */
    public int _firstch(int i) {
        int elementAt = i >= this.m_size ? -2 : this.m_firstch.elementAt(i);
        while (elementAt == -2) {
            boolean nextNode = nextNode();
            if (i >= this.m_size && !nextNode) {
                return -1;
            }
            int elementAt2 = this.m_firstch.elementAt(i);
            if (elementAt2 == -2 && !nextNode) {
                return -1;
            }
            elementAt = elementAt2;
        }
        return elementAt;
    }

    /* access modifiers changed from: protected */
    public int _nextsib(int i) {
        int elementAt = i >= this.m_size ? -2 : this.m_nextsib.elementAt(i);
        while (elementAt == -2) {
            boolean nextNode = nextNode();
            if (i >= this.m_size && !nextNode) {
                return -1;
            }
            int elementAt2 = this.m_nextsib.elementAt(i);
            if (elementAt2 == -2 && !nextNode) {
                return -1;
            }
            elementAt = elementAt2;
        }
        return elementAt;
    }

    /* access modifiers changed from: protected */
    public int _prevsib(int i) {
        if (i < this.m_size) {
            return this.m_prevsib.elementAt(i);
        }
        do {
            boolean nextNode = nextNode();
            if (i >= this.m_size && !nextNode) {
                return -1;
            }
        } while (i >= this.m_size);
        return this.m_prevsib.elementAt(i);
    }

    /* access modifiers changed from: protected */
    public int _parent(int i) {
        if (i < this.m_size) {
            return this.m_parent.elementAt(i);
        }
        do {
            boolean nextNode = nextNode();
            if (i >= this.m_size && !nextNode) {
                return -1;
            }
        } while (i >= this.m_size);
        return this.m_parent.elementAt(i);
    }

    public void dumpDTM(OutputStream outputStream) {
        if (outputStream == null) {
            try {
                File file = new File("DTMDump" + hashCode() + ".txt");
                PrintStream printStream = System.err;
                printStream.println("Dumping... " + file.getAbsolutePath());
                outputStream = new FileOutputStream(file);
            } catch (IOException e) {
                e.printStackTrace(System.err);
                throw new RuntimeException(e.getMessage());
            }
        }
        PrintStream printStream2 = new PrintStream(outputStream);
        while (nextNode()) {
        }
        int i = this.m_size;
        printStream2.println("Total nodes: " + i);
        for (int i2 = 0; i2 < i; i2++) {
            int makeNodeHandle = makeNodeHandle(i2);
            printStream2.println("=========== index=" + i2 + " handle=" + makeNodeHandle + " ===========");
            StringBuilder sb = new StringBuilder();
            sb.append("NodeName: ");
            sb.append(getNodeName(makeNodeHandle));
            printStream2.println(sb.toString());
            printStream2.println("NodeNameX: " + getNodeNameX(makeNodeHandle));
            printStream2.println("LocalName: " + getLocalName(makeNodeHandle));
            printStream2.println("NamespaceURI: " + getNamespaceURI(makeNodeHandle));
            printStream2.println("Prefix: " + getPrefix(makeNodeHandle));
            int _exptype = _exptype(i2);
            printStream2.println("Expanded Type ID: " + Integer.toHexString(_exptype));
            String str = "DOCUMENT_NODE";
            switch (_type(i2)) {
                case -1:
                    str = "NULL";
                    break;
                case 0:
                default:
                    str = "Unknown!";
                    break;
                case 1:
                    str = "ELEMENT_NODE";
                    break;
                case 2:
                    str = "ATTRIBUTE_NODE";
                    break;
                case 3:
                    str = "TEXT_NODE";
                    break;
                case 4:
                    str = "CDATA_SECTION_NODE";
                    break;
                case 5:
                    str = "ENTITY_REFERENCE_NODE";
                    break;
                case 6:
                    str = "ENTITY_NODE";
                    break;
                case 7:
                    str = "PROCESSING_INSTRUCTION_NODE";
                    break;
                case 8:
                    str = "COMMENT_NODE";
                    break;
                case 9:
                case 10:
                    break;
                case 11:
                    str = "DOCUMENT_FRAGMENT_NODE";
                    break;
                case 12:
                    str = "NOTATION_NODE";
                    break;
                case 13:
                    str = "NAMESPACE_NODE";
                    break;
            }
            printStream2.println("Type: " + str);
            int _firstch = _firstch(i2);
            if (-1 == _firstch) {
                printStream2.println("First child: DTM.NULL");
            } else if (-2 == _firstch) {
                printStream2.println("First child: NOTPROCESSED");
            } else {
                printStream2.println("First child: " + _firstch);
            }
            if (this.m_prevsib != null) {
                int _prevsib = _prevsib(i2);
                if (-1 == _prevsib) {
                    printStream2.println("Prev sibling: DTM.NULL");
                } else if (-2 == _prevsib) {
                    printStream2.println("Prev sibling: NOTPROCESSED");
                } else {
                    printStream2.println("Prev sibling: " + _prevsib);
                }
            }
            int _nextsib = _nextsib(i2);
            if (-1 == _nextsib) {
                printStream2.println("Next sibling: DTM.NULL");
            } else if (-2 == _nextsib) {
                printStream2.println("Next sibling: NOTPROCESSED");
            } else {
                printStream2.println("Next sibling: " + _nextsib);
            }
            int _parent = _parent(i2);
            if (-1 == _parent) {
                printStream2.println("Parent: DTM.NULL");
            } else if (-2 == _parent) {
                printStream2.println("Parent: NOTPROCESSED");
            } else {
                printStream2.println("Parent: " + _parent);
            }
            int _level = _level(i2);
            printStream2.println("Level: " + _level);
            printStream2.println("Node Value: " + getNodeValue(makeNodeHandle));
            printStream2.println("String Value: " + getStringValue(makeNodeHandle));
        }
    }

    public String dumpNode(int i) {
        String str;
        if (i == -1) {
            return "[null]";
        }
        switch (getNodeType(i)) {
            case -1:
                str = "null";
                break;
            case 0:
            default:
                str = "Unknown!";
                break;
            case 1:
                str = "ELEMENT";
                break;
            case 2:
                str = "ATTR";
                break;
            case 3:
                str = "TEXT";
                break;
            case 4:
                str = "CDATA";
                break;
            case 5:
                str = "ENT_REF";
                break;
            case 6:
                str = SchemaSymbols.ATTVAL_ENTITY;
                break;
            case 7:
                str = "PI";
                break;
            case 8:
                str = "COMMENT";
                break;
            case 9:
                str = "DOC";
                break;
            case 10:
                str = "DOC_TYPE";
                break;
            case 11:
                str = "DOC_FRAG";
                break;
            case 12:
                str = SchemaSymbols.ATTVAL_NOTATION;
                break;
            case 13:
                str = "NAMESPACE";
                break;
        }
        return "[" + i + PluralRules.KEYWORD_RULE_SEPARATOR + str + "(0x" + Integer.toHexString(getExpandedTypeID(i)) + ") " + getNodeNameX(i) + " {" + getNamespaceURI(i) + "}=\"" + getNodeValue(i) + "\"]";
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public boolean hasChildNodes(int i) {
        return _firstch(makeNodeIdentity(i)) != -1;
    }

    public final int makeNodeHandle(int i) {
        if (-1 == i) {
            return -1;
        }
        return this.m_dtmIdent.elementAt(i >>> 16) + (i & 65535);
    }

    public final int makeNodeIdentity(int i) {
        if (-1 == i) {
            return -1;
        }
        DTMManagerDefault dTMManagerDefault = this.m_mgrDefault;
        if (dTMManagerDefault != null) {
            int i2 = i >>> 16;
            if (dTMManagerDefault.m_dtms[i2] != this) {
                return -1;
            }
            return this.m_mgrDefault.m_dtm_offsets[i2] | (i & 65535);
        }
        int indexOf = this.m_dtmIdent.indexOf(-65536 & i);
        if (indexOf == -1) {
            return -1;
        }
        return (indexOf << 16) + (i & 65535);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public int getFirstChild(int i) {
        return makeNodeHandle(_firstch(makeNodeIdentity(i)));
    }

    public int getTypedFirstChild(int i, int i2) {
        if (i2 < 14) {
            int _firstch = _firstch(makeNodeIdentity(i));
            while (_firstch != -1) {
                int _exptype = _exptype(_firstch);
                if (_exptype == i2 || (_exptype >= 14 && this.m_expandedNameTable.getType(_exptype) == i2)) {
                    return makeNodeHandle(_firstch);
                }
                _firstch = _nextsib(_firstch);
            }
        } else {
            int _firstch2 = _firstch(makeNodeIdentity(i));
            while (_firstch2 != -1) {
                if (_exptype(_firstch2) == i2) {
                    return makeNodeHandle(_firstch2);
                }
                _firstch2 = _nextsib(_firstch2);
            }
        }
        return -1;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public int getLastChild(int i) {
        int _firstch = _firstch(makeNodeIdentity(i));
        int i2 = -1;
        while (_firstch != -1) {
            i2 = _firstch;
            _firstch = _nextsib(_firstch);
        }
        return makeNodeHandle(i2);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public int getFirstAttribute(int i) {
        return makeNodeHandle(getFirstAttributeIdentity(makeNodeIdentity(i)));
    }

    /* access modifiers changed from: protected */
    public int getFirstAttributeIdentity(int i) {
        short _type;
        if (1 == _type(i)) {
            do {
                i = getNextNodeIdentity(i);
                if (-1 == i) {
                    break;
                }
                _type = _type(i);
                if (_type == 2) {
                    return i;
                }
            } while (13 == _type);
        }
        return -1;
    }

    /* access modifiers changed from: protected */
    public int getTypedAttribute(int i, int i2) {
        if (1 == getNodeType(i)) {
            int makeNodeIdentity = makeNodeIdentity(i);
            while (true) {
                makeNodeIdentity = getNextNodeIdentity(makeNodeIdentity);
                if (-1 == makeNodeIdentity) {
                    break;
                }
                short _type = _type(makeNodeIdentity);
                if (_type == 2) {
                    if (_exptype(makeNodeIdentity) == i2) {
                        return makeNodeHandle(makeNodeIdentity);
                    }
                } else if (13 != _type) {
                    break;
                }
            }
        }
        return -1;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public int getNextSibling(int i) {
        if (i == -1) {
            return -1;
        }
        return makeNodeHandle(_nextsib(makeNodeIdentity(i)));
    }

    public int getTypedNextSibling(int i, int i2) {
        int _exptype;
        if (i == -1) {
            return -1;
        }
        int makeNodeIdentity = makeNodeIdentity(i);
        do {
            makeNodeIdentity = _nextsib(makeNodeIdentity);
            if (makeNodeIdentity == -1 || (_exptype = _exptype(makeNodeIdentity)) == i2) {
                break;
            }
        } while (this.m_expandedNameTable.getType(_exptype) != i2);
        if (makeNodeIdentity == -1) {
            return -1;
        }
        return makeNodeHandle(makeNodeIdentity);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public int getPreviousSibling(int i) {
        int i2 = -1;
        if (i == -1) {
            return -1;
        }
        if (this.m_prevsib != null) {
            return makeNodeHandle(_prevsib(makeNodeIdentity(i)));
        }
        int makeNodeIdentity = makeNodeIdentity(i);
        int _firstch = _firstch(_parent(makeNodeIdentity));
        while (true) {
            i2 = _firstch;
            if (i2 == makeNodeIdentity) {
                return makeNodeHandle(i2);
            }
            _firstch = _nextsib(i2);
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public int getNextAttribute(int i) {
        int makeNodeIdentity = makeNodeIdentity(i);
        if (_type(makeNodeIdentity) == 2) {
            return makeNodeHandle(getNextAttributeIdentity(makeNodeIdentity));
        }
        return -1;
    }

    /* access modifiers changed from: protected */
    public int getNextAttributeIdentity(int i) {
        short _type;
        do {
            i = getNextNodeIdentity(i);
            if (-1 == i) {
                break;
            }
            _type = _type(i);
            if (_type == 2) {
                return i;
            }
        } while (_type == 13);
        return -1;
    }

    /* access modifiers changed from: protected */
    public void declareNamespaceInContext(int i, int i2) {
        SuballocatedIntVector suballocatedIntVector;
        if (this.m_namespaceDeclSets == null) {
            this.m_namespaceDeclSetElements = new SuballocatedIntVector(32);
            this.m_namespaceDeclSetElements.addElement(i);
            this.m_namespaceDeclSets = new Vector();
            suballocatedIntVector = new SuballocatedIntVector(32);
            this.m_namespaceDeclSets.addElement(suballocatedIntVector);
        } else {
            int size = this.m_namespaceDeclSetElements.size() - 1;
            suballocatedIntVector = (size < 0 || i != this.m_namespaceDeclSetElements.elementAt(size)) ? null : (SuballocatedIntVector) this.m_namespaceDeclSets.elementAt(size);
        }
        if (suballocatedIntVector == null) {
            this.m_namespaceDeclSetElements.addElement(i);
            SuballocatedIntVector findNamespaceContext = findNamespaceContext(_parent(i));
            if (findNamespaceContext != null) {
                int size2 = findNamespaceContext.size();
                SuballocatedIntVector suballocatedIntVector2 = new SuballocatedIntVector(Math.max(Math.min(size2 + 16, 2048), 32));
                for (int i3 = 0; i3 < size2; i3++) {
                    suballocatedIntVector2.addElement(findNamespaceContext.elementAt(i3));
                }
                suballocatedIntVector = suballocatedIntVector2;
            } else {
                suballocatedIntVector = new SuballocatedIntVector(32);
            }
            this.m_namespaceDeclSets.addElement(suballocatedIntVector);
        }
        int _exptype = _exptype(i2);
        for (int size3 = suballocatedIntVector.size() - 1; size3 >= 0; size3--) {
            if (_exptype == getExpandedTypeID(suballocatedIntVector.elementAt(size3))) {
                suballocatedIntVector.setElementAt(makeNodeHandle(i2), size3);
                return;
            }
        }
        suballocatedIntVector.addElement(makeNodeHandle(i2));
    }

    /* access modifiers changed from: protected */
    public SuballocatedIntVector findNamespaceContext(int i) {
        int _firstch;
        SuballocatedIntVector suballocatedIntVector = this.m_namespaceDeclSetElements;
        if (suballocatedIntVector != null) {
            int findInSortedSuballocatedIntVector = findInSortedSuballocatedIntVector(suballocatedIntVector, i);
            if (findInSortedSuballocatedIntVector < 0) {
                if (findInSortedSuballocatedIntVector != -1) {
                    int i2 = (-1 - findInSortedSuballocatedIntVector) - 1;
                    int elementAt = this.m_namespaceDeclSetElements.elementAt(i2);
                    int _parent = _parent(i);
                    if (i2 == 0 && elementAt < _parent) {
                        int documentRoot = getDocumentRoot(makeNodeHandle(i));
                        int makeNodeIdentity = makeNodeIdentity(documentRoot);
                        if (getNodeType(documentRoot) == 9 && (_firstch = _firstch(makeNodeIdentity)) != -1) {
                            makeNodeIdentity = _firstch;
                        }
                        if (elementAt == makeNodeIdentity) {
                            return (SuballocatedIntVector) this.m_namespaceDeclSets.elementAt(i2);
                        }
                    }
                    while (i2 >= 0 && _parent > 0) {
                        if (elementAt != _parent) {
                            if (elementAt >= _parent) {
                                if (i2 <= 0) {
                                    break;
                                }
                                i2--;
                                elementAt = this.m_namespaceDeclSetElements.elementAt(i2);
                            } else {
                                do {
                                    _parent = _parent(_parent);
                                } while (elementAt < _parent);
                            }
                        } else {
                            return (SuballocatedIntVector) this.m_namespaceDeclSets.elementAt(i2);
                        }
                    }
                } else {
                    return null;
                }
            } else {
                return (SuballocatedIntVector) this.m_namespaceDeclSets.elementAt(findInSortedSuballocatedIntVector);
            }
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public int findInSortedSuballocatedIntVector(SuballocatedIntVector suballocatedIntVector, int i) {
        int i2 = 0;
        if (suballocatedIntVector != null) {
            int size = suballocatedIntVector.size() - 1;
            int i3 = 0;
            while (i2 <= size) {
                i3 = (i2 + size) / 2;
                int elementAt = i - suballocatedIntVector.elementAt(i3);
                if (elementAt == 0) {
                    return i3;
                }
                if (elementAt < 0) {
                    size = i3 - 1;
                } else {
                    i2 = i3 + 1;
                }
            }
            if (i2 <= i3) {
                i2 = i3;
            }
        }
        return -1 - i2;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public int getFirstNamespaceNode(int i, boolean z) {
        short _type;
        SuballocatedIntVector findNamespaceContext;
        if (z) {
            int makeNodeIdentity = makeNodeIdentity(i);
            if (_type(makeNodeIdentity) != 1 || (findNamespaceContext = findNamespaceContext(makeNodeIdentity)) == null || findNamespaceContext.size() < 1) {
                return -1;
            }
            return findNamespaceContext.elementAt(0);
        }
        int makeNodeIdentity2 = makeNodeIdentity(i);
        if (_type(makeNodeIdentity2) == 1) {
            do {
                makeNodeIdentity2 = getNextNodeIdentity(makeNodeIdentity2);
                if (-1 == makeNodeIdentity2) {
                    break;
                }
                _type = _type(makeNodeIdentity2);
                if (_type == 13) {
                    return makeNodeHandle(makeNodeIdentity2);
                }
            } while (2 == _type);
        }
        return -1;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public int getNextNamespaceNode(int i, int i2, boolean z) {
        short _type;
        int indexOf;
        if (z) {
            SuballocatedIntVector findNamespaceContext = findNamespaceContext(makeNodeIdentity(i));
            if (findNamespaceContext == null || (indexOf = findNamespaceContext.indexOf(i2) + 1) <= 0 || indexOf == findNamespaceContext.size()) {
                return -1;
            }
            return findNamespaceContext.elementAt(indexOf);
        }
        int makeNodeIdentity = makeNodeIdentity(i2);
        do {
            makeNodeIdentity = getNextNodeIdentity(makeNodeIdentity);
            if (-1 == makeNodeIdentity) {
                break;
            }
            _type = _type(makeNodeIdentity);
            if (_type == 13) {
                return makeNodeHandle(makeNodeIdentity);
            }
        } while (_type == 2);
        return -1;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public int getParent(int i) {
        int makeNodeIdentity = makeNodeIdentity(i);
        if (makeNodeIdentity > 0) {
            return makeNodeHandle(_parent(makeNodeIdentity));
        }
        return -1;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public int getDocument() {
        return this.m_dtmIdent.elementAt(0);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public int getOwnerDocument(int i) {
        if (9 == getNodeType(i)) {
            return -1;
        }
        return getDocumentRoot(i);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public int getDocumentRoot(int i) {
        return getManager().getDTM(i).getDocument();
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public int getStringValueChunkCount(int i) {
        error(XMLMessages.createXMLMessage("ER_METHOD_NOT_SUPPORTED", null));
        return 0;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public char[] getStringValueChunk(int i, int i2, int[] iArr) {
        error(XMLMessages.createXMLMessage("ER_METHOD_NOT_SUPPORTED", null));
        return null;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public int getExpandedTypeID(int i) {
        int makeNodeIdentity = makeNodeIdentity(i);
        if (makeNodeIdentity == -1) {
            return -1;
        }
        return _exptype(makeNodeIdentity);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public int getExpandedTypeID(String str, String str2, int i) {
        return this.m_expandedNameTable.getExpandedTypeID(str, str2, i);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public String getLocalNameFromExpandedNameID(int i) {
        return this.m_expandedNameTable.getLocalName(i);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public String getNamespaceFromExpandedNameID(int i) {
        return this.m_expandedNameTable.getNamespace(i);
    }

    @Override // ohos.com.sun.org.apache.xalan.internal.xsltc.DOM
    public int getNamespaceType(int i) {
        return this.m_expandedNameTable.getNamespaceID(_exptype(makeNodeIdentity(i)));
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public String getNodeNameX(int i) {
        error(XMLMessages.createXMLMessage("ER_METHOD_NOT_SUPPORTED", null));
        return null;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public short getNodeType(int i) {
        if (i == -1) {
            return -1;
        }
        return this.m_expandedNameTable.getType(_exptype(makeNodeIdentity(i)));
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public short getLevel(int i) {
        return (short) (_level(makeNodeIdentity(i)) + 1);
    }

    public int getNodeIdent(int i) {
        return makeNodeIdentity(i);
    }

    public int getNodeHandle(int i) {
        return makeNodeHandle(i);
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
    public String getDocumentSystemIdentifier(int i) {
        return this.m_documentBaseURI;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public boolean isNodeAfter(int i, int i2) {
        int makeNodeIdentity = makeNodeIdentity(i);
        int makeNodeIdentity2 = makeNodeIdentity(i2);
        return (makeNodeIdentity == -1 || makeNodeIdentity2 == -1 || makeNodeIdentity > makeNodeIdentity2) ? false : true;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public Node getNode(int i) {
        return new DTMNodeProxy(this, i);
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public void appendChild(int i, boolean z, boolean z2) {
        error(XMLMessages.createXMLMessage("ER_METHOD_NOT_SUPPORTED", null));
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public void appendTextChild(String str) {
        error(XMLMessages.createXMLMessage("ER_METHOD_NOT_SUPPORTED", null));
    }

    /* access modifiers changed from: protected */
    public void error(String str) {
        throw new DTMException(str);
    }

    /* access modifiers changed from: protected */
    public boolean getShouldStripWhitespace() {
        return this.m_shouldStripWS;
    }

    /* access modifiers changed from: protected */
    public void pushShouldStripWhitespace(boolean z) {
        this.m_shouldStripWS = z;
        BoolStack boolStack = this.m_shouldStripWhitespaceStack;
        if (boolStack != null) {
            boolStack.push(z);
        }
    }

    /* access modifiers changed from: protected */
    public void popShouldStripWhitespace() {
        BoolStack boolStack = this.m_shouldStripWhitespaceStack;
        if (boolStack != null) {
            this.m_shouldStripWS = boolStack.popAndTop();
        }
    }

    /* access modifiers changed from: protected */
    public void setShouldStripWhitespace(boolean z) {
        this.m_shouldStripWS = z;
        BoolStack boolStack = this.m_shouldStripWhitespaceStack;
        if (boolStack != null) {
            boolStack.setTop(z);
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.dtm.DTM
    public void migrateTo(DTMManager dTMManager) {
        this.m_mgr = dTMManager;
        if (dTMManager instanceof DTMManagerDefault) {
            this.m_mgrDefault = (DTMManagerDefault) dTMManager;
        }
    }

    public DTMManager getManager() {
        return this.m_mgr;
    }

    public SuballocatedIntVector getDTMIDs() {
        if (this.m_mgr == null) {
            return null;
        }
        return this.m_dtmIdent;
    }
}

package ohos.com.sun.org.apache.xerces.internal.impl.dtd;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import ohos.com.sun.org.apache.xerces.internal.impl.dtd.models.CMAny;
import ohos.com.sun.org.apache.xerces.internal.impl.dtd.models.CMBinOp;
import ohos.com.sun.org.apache.xerces.internal.impl.dtd.models.CMLeaf;
import ohos.com.sun.org.apache.xerces.internal.impl.dtd.models.CMNode;
import ohos.com.sun.org.apache.xerces.internal.impl.dtd.models.CMUniOp;
import ohos.com.sun.org.apache.xerces.internal.impl.dtd.models.ContentModelValidator;
import ohos.com.sun.org.apache.xerces.internal.impl.dtd.models.DFAContentModel;
import ohos.com.sun.org.apache.xerces.internal.impl.dtd.models.MixedContentModel;
import ohos.com.sun.org.apache.xerces.internal.impl.dtd.models.SimpleContentModel;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.DatatypeValidator;
import ohos.com.sun.org.apache.xerces.internal.impl.validation.EntityState;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaSymbols;
import ohos.com.sun.org.apache.xerces.internal.util.SymbolTable;
import ohos.com.sun.org.apache.xerces.internal.xni.Augmentations;
import ohos.com.sun.org.apache.xerces.internal.xni.QName;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDContentModelHandler;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLLocator;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLResourceIdentifier;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLString;
import ohos.com.sun.org.apache.xerces.internal.xni.XNIException;
import ohos.com.sun.org.apache.xerces.internal.xni.grammars.Grammar;
import ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarDescription;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLDTDContentModelSource;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLDTDSource;

public class DTDGrammar implements XMLDTDHandler, XMLDTDContentModelHandler, EntityState, Grammar {
    private static final int CHUNK_MASK = 255;
    private static final int CHUNK_SHIFT = 8;
    private static final int CHUNK_SIZE = 256;
    private static final boolean DEBUG = false;
    private static final int INITIAL_CHUNK_COUNT = 4;
    private static final short LIST_FLAG = 128;
    private static final short LIST_MASK = -129;
    public static final int TOP_LEVEL_SCOPE = -1;
    protected final XMLAttributeDecl fAttributeDecl = new XMLAttributeDecl();
    private int fAttributeDeclCount = 0;
    private DatatypeValidator[][] fAttributeDeclDatatypeValidator = new DatatypeValidator[4][];
    private short[][] fAttributeDeclDefaultType = new short[4][];
    private String[][] fAttributeDeclDefaultValue = new String[4][];
    private String[][][] fAttributeDeclEnumeration = new String[4][][];
    private int[][] fAttributeDeclIsExternal = new int[4][];
    private QName[][] fAttributeDeclName = new QName[4][];
    private int[][] fAttributeDeclNextAttributeDeclIndex = new int[4][];
    private String[][] fAttributeDeclNonNormalizedDefaultValue = new String[4][];
    private short[][] fAttributeDeclType = new short[4][];
    private XMLContentSpec fContentSpec = new XMLContentSpec();
    private int fContentSpecCount = 0;
    private Object[][] fContentSpecOtherValue = new Object[4][];
    private short[][] fContentSpecType = new short[4][];
    private Object[][] fContentSpecValue = new Object[4][];
    protected int fCurrentAttributeIndex;
    protected int fCurrentElementIndex;
    protected XMLDTDContentModelSource fDTDContentModelSource = null;
    protected XMLDTDSource fDTDSource = null;
    private int fDepth = 0;
    private XMLElementDecl fElementDecl = new XMLElementDecl();
    private ContentModelValidator[][] fElementDeclContentModelValidator = new ContentModelValidator[4][];
    private int[][] fElementDeclContentSpecIndex = new int[4][];
    private int fElementDeclCount = 0;
    private int[][] fElementDeclFirstAttributeDeclIndex = new int[4][];
    private int[][] fElementDeclIsExternal = new int[4][];
    private int[][] fElementDeclLastAttributeDeclIndex = new int[4][];
    private QName[][] fElementDeclName = new QName[4][];
    Map<String, XMLElementDecl> fElementDeclTab = new HashMap();
    private short[][] fElementDeclType = new short[4][];
    private final Map<String, Integer> fElementIndexMap = new HashMap();
    private String[][] fEntityBaseSystemId = new String[4][];
    private int fEntityCount = 0;
    private XMLEntityDecl fEntityDecl = new XMLEntityDecl();
    private byte[][] fEntityInExternal = new byte[4][];
    private final Map<String, Integer> fEntityIndexMap = new HashMap();
    private byte[][] fEntityIsPE = new byte[4][];
    private String[][] fEntityName = new String[4][];
    private String[][] fEntityNotation = new String[4][];
    private String[][] fEntityPublicId = new String[4][];
    private String[][] fEntitySystemId = new String[4][];
    private String[][] fEntityValue = new String[4][];
    private int fEpsilonIndex = -1;
    protected XMLDTDDescription fGrammarDescription = null;
    private boolean fIsImmutable = false;
    private int fLeafCount = 0;
    private boolean fMixed;
    private int[] fNodeIndexStack = null;
    private String[][] fNotationBaseSystemId = new String[4][];
    private int fNotationCount = 0;
    private final Map<String, Integer> fNotationIndexMap = new HashMap();
    private String[][] fNotationName = new String[4][];
    private String[][] fNotationPublicId = new String[4][];
    private String[][] fNotationSystemId = new String[4][];
    private short[] fOpStack = null;
    private int fPEDepth = 0;
    private boolean[] fPEntityStack = new boolean[4];
    private int[] fPrevNodeIndexStack = null;
    private final QName fQName = new QName();
    private final QName fQName2 = new QName();
    protected boolean fReadingExternalDTD = false;
    private XMLSimpleType fSimpleType = new XMLSimpleType();
    private SymbolTable fSymbolTable;
    int nodeIndex = -1;
    int prevNodeIndex = -1;
    int valueIndex = -1;

    /* access modifiers changed from: private */
    public static class ChildrenList {
        public int length = 0;
        public QName[] qname = new QName[2];
        public int[] type = new int[2];
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDContentModelHandler
    public void any(Augmentations augmentations) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void comment(XMLString xMLString, Augmentations augmentations) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDContentModelHandler
    public void empty(Augmentations augmentations) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void endAttlist(Augmentations augmentations) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void endConditional(Augmentations augmentations) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDContentModelHandler
    public void endContentModel(Augmentations augmentations) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void ignoredCharacters(XMLString xMLString, Augmentations augmentations) throws XNIException {
    }

    public boolean isNamespaceAware() {
        return false;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void processingInstruction(String str, XMLString xMLString, Augmentations augmentations) throws XNIException {
    }

    /* access modifiers changed from: protected */
    public void putElementNameMapping(QName qName, int i, int i2) {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void startAttlist(String str, Augmentations augmentations) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void startConditional(short s, Augmentations augmentations) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void textDecl(String str, String str2, Augmentations augmentations) throws XNIException {
    }

    public DTDGrammar(SymbolTable symbolTable, XMLDTDDescription xMLDTDDescription) {
        this.fSymbolTable = symbolTable;
        this.fGrammarDescription = xMLDTDDescription;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.grammars.Grammar
    public XMLGrammarDescription getGrammarDescription() {
        return this.fGrammarDescription;
    }

    public boolean getElementDeclIsExternal(int i) {
        if (i < 0) {
            return false;
        }
        return this.fElementDeclIsExternal[i >> 8][i & 255] != 0;
    }

    public boolean getAttributeDeclIsExternal(int i) {
        if (i < 0) {
            return false;
        }
        return this.fAttributeDeclIsExternal[i >> 8][i & 255] != 0;
    }

    public int getAttributeDeclIndex(int i, String str) {
        if (i == -1) {
            return -1;
        }
        int firstAttributeDeclIndex = getFirstAttributeDeclIndex(i);
        while (firstAttributeDeclIndex != -1) {
            getAttributeDecl(firstAttributeDeclIndex, this.fAttributeDecl);
            if (this.fAttributeDecl.name.rawname == str || str.equals(this.fAttributeDecl.name.rawname)) {
                return firstAttributeDeclIndex;
            }
            firstAttributeDeclIndex = getNextAttributeDeclIndex(firstAttributeDeclIndex);
        }
        return -1;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void startDTD(XMLLocator xMLLocator, Augmentations augmentations) throws XNIException {
        this.fOpStack = null;
        this.fNodeIndexStack = null;
        this.fPrevNodeIndexStack = null;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void startParameterEntity(String str, XMLResourceIdentifier xMLResourceIdentifier, String str2, Augmentations augmentations) throws XNIException {
        int i = this.fPEDepth;
        boolean[] zArr = this.fPEntityStack;
        if (i == zArr.length) {
            boolean[] zArr2 = new boolean[(zArr.length * 2)];
            System.arraycopy(zArr, 0, zArr2, 0, zArr.length);
            this.fPEntityStack = zArr2;
        }
        boolean[] zArr3 = this.fPEntityStack;
        int i2 = this.fPEDepth;
        zArr3[i2] = this.fReadingExternalDTD;
        this.fPEDepth = i2 + 1;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void startExternalSubset(XMLResourceIdentifier xMLResourceIdentifier, Augmentations augmentations) throws XNIException {
        this.fReadingExternalDTD = true;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void endParameterEntity(String str, Augmentations augmentations) throws XNIException {
        this.fPEDepth--;
        this.fReadingExternalDTD = this.fPEntityStack[this.fPEDepth];
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void endExternalSubset(Augmentations augmentations) throws XNIException {
        this.fReadingExternalDTD = false;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void elementDecl(String str, String str2, Augmentations augmentations) throws XNIException {
        XMLElementDecl xMLElementDecl = this.fElementDeclTab.get(str);
        if (xMLElementDecl == null) {
            this.fCurrentElementIndex = createElementDecl();
        } else if (xMLElementDecl.type == -1) {
            this.fCurrentElementIndex = getElementDeclIndex(str);
        } else {
            return;
        }
        XMLElementDecl xMLElementDecl2 = new XMLElementDecl();
        this.fQName.setValues(null, str, str, null);
        xMLElementDecl2.name.setValues(this.fQName);
        xMLElementDecl2.contentModelValidator = null;
        xMLElementDecl2.scope = -1;
        int i = 0;
        if (str2.equals("EMPTY")) {
            xMLElementDecl2.type = 1;
        } else if (str2.equals("ANY")) {
            xMLElementDecl2.type = 0;
        } else if (str2.startsWith("(")) {
            if (str2.indexOf("#PCDATA") > 0) {
                xMLElementDecl2.type = 2;
            } else {
                xMLElementDecl2.type = 3;
            }
        }
        this.fElementDeclTab.put(str, xMLElementDecl2);
        this.fElementDecl = xMLElementDecl2;
        addContentSpecToElement(xMLElementDecl2);
        setElementDecl(this.fCurrentElementIndex, this.fElementDecl);
        int i2 = this.fCurrentElementIndex;
        int i3 = i2 >> 8;
        int i4 = i2 & 255;
        ensureElementDeclCapacity(i3);
        int[] iArr = this.fElementDeclIsExternal[i3];
        if (this.fReadingExternalDTD || this.fPEDepth > 0) {
            i = 1;
        }
        iArr[i4] = i;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void attributeDecl(String str, String str2, String str3, String[] strArr, String str4, XMLString xMLString, XMLString xMLString2, Augmentations augmentations) throws XNIException {
        if (!this.fElementDeclTab.containsKey(str)) {
            this.fCurrentElementIndex = createElementDecl();
            XMLElementDecl xMLElementDecl = new XMLElementDecl();
            xMLElementDecl.name.setValues(null, str, str, null);
            xMLElementDecl.scope = -1;
            this.fElementDeclTab.put(str, xMLElementDecl);
            setElementDecl(this.fCurrentElementIndex, xMLElementDecl);
        }
        int elementDeclIndex = getElementDeclIndex(str);
        if (getAttributeDeclIndex(elementDeclIndex, str2) == -1) {
            this.fCurrentAttributeIndex = createAttributeDecl();
            this.fSimpleType.clear();
            int i = 0;
            if (str4 != null) {
                if (str4.equals("#FIXED")) {
                    this.fSimpleType.defaultType = 1;
                } else if (str4.equals("#IMPLIED")) {
                    this.fSimpleType.defaultType = 0;
                } else if (str4.equals("#REQUIRED")) {
                    this.fSimpleType.defaultType = 2;
                }
            }
            this.fSimpleType.defaultValue = xMLString != null ? xMLString.toString() : null;
            this.fSimpleType.nonNormalizedDefaultValue = xMLString2 != null ? xMLString2.toString() : null;
            this.fSimpleType.enumeration = strArr;
            if (str3.equals("CDATA")) {
                this.fSimpleType.type = 0;
            } else if (str3.equals(SchemaSymbols.ATTVAL_ID)) {
                this.fSimpleType.type = 3;
            } else if (str3.startsWith(SchemaSymbols.ATTVAL_IDREF)) {
                this.fSimpleType.type = 4;
                if (str3.indexOf("S") > 0) {
                    this.fSimpleType.list = true;
                }
            } else if (str3.equals(SchemaSymbols.ATTVAL_ENTITIES)) {
                XMLSimpleType xMLSimpleType = this.fSimpleType;
                xMLSimpleType.type = 1;
                xMLSimpleType.list = true;
            } else if (str3.equals(SchemaSymbols.ATTVAL_ENTITY)) {
                this.fSimpleType.type = 1;
            } else if (str3.equals(SchemaSymbols.ATTVAL_NMTOKENS)) {
                XMLSimpleType xMLSimpleType2 = this.fSimpleType;
                xMLSimpleType2.type = 5;
                xMLSimpleType2.list = true;
            } else if (str3.equals(SchemaSymbols.ATTVAL_NMTOKEN)) {
                this.fSimpleType.type = 5;
            } else if (str3.startsWith(SchemaSymbols.ATTVAL_NOTATION)) {
                this.fSimpleType.type = 6;
            } else if (str3.startsWith("ENUMERATION")) {
                this.fSimpleType.type = 2;
            } else {
                System.err.println("!!! unknown attribute type " + str3);
            }
            this.fQName.setValues(null, str2, str2, null);
            this.fAttributeDecl.setValues(this.fQName, this.fSimpleType, false);
            setAttributeDecl(elementDeclIndex, this.fCurrentAttributeIndex, this.fAttributeDecl);
            int i2 = this.fCurrentAttributeIndex;
            int i3 = i2 >> 8;
            int i4 = i2 & 255;
            ensureAttributeDeclCapacity(i3);
            int[] iArr = this.fAttributeDeclIsExternal[i3];
            if (this.fReadingExternalDTD || this.fPEDepth > 0) {
                i = 1;
            }
            iArr[i4] = i;
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void internalEntityDecl(String str, XMLString xMLString, XMLString xMLString2, Augmentations augmentations) throws XNIException {
        if (getEntityDeclIndex(str) == -1) {
            int createEntityDecl = createEntityDecl();
            boolean startsWith = str.startsWith("%");
            boolean z = this.fReadingExternalDTD || this.fPEDepth > 0;
            XMLEntityDecl xMLEntityDecl = new XMLEntityDecl();
            xMLEntityDecl.setValues(str, null, null, null, null, xMLString.toString(), startsWith, z);
            setEntityDecl(createEntityDecl, xMLEntityDecl);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void externalEntityDecl(String str, XMLResourceIdentifier xMLResourceIdentifier, Augmentations augmentations) throws XNIException {
        if (getEntityDeclIndex(str) == -1) {
            int createEntityDecl = createEntityDecl();
            boolean startsWith = str.startsWith("%");
            boolean z = this.fReadingExternalDTD || this.fPEDepth > 0;
            XMLEntityDecl xMLEntityDecl = new XMLEntityDecl();
            xMLEntityDecl.setValues(str, xMLResourceIdentifier.getPublicId(), xMLResourceIdentifier.getLiteralSystemId(), xMLResourceIdentifier.getBaseSystemId(), null, null, startsWith, z);
            setEntityDecl(createEntityDecl, xMLEntityDecl);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void unparsedEntityDecl(String str, XMLResourceIdentifier xMLResourceIdentifier, String str2, Augmentations augmentations) throws XNIException {
        XMLEntityDecl xMLEntityDecl = new XMLEntityDecl();
        xMLEntityDecl.setValues(str, xMLResourceIdentifier.getPublicId(), xMLResourceIdentifier.getLiteralSystemId(), xMLResourceIdentifier.getBaseSystemId(), str2, null, str.startsWith("%"), this.fReadingExternalDTD || this.fPEDepth > 0);
        if (getEntityDeclIndex(str) == -1) {
            setEntityDecl(createEntityDecl(), xMLEntityDecl);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void notationDecl(String str, XMLResourceIdentifier xMLResourceIdentifier, Augmentations augmentations) throws XNIException {
        XMLNotationDecl xMLNotationDecl = new XMLNotationDecl();
        xMLNotationDecl.setValues(str, xMLResourceIdentifier.getPublicId(), xMLResourceIdentifier.getLiteralSystemId(), xMLResourceIdentifier.getBaseSystemId());
        if (getNotationDeclIndex(str) == -1) {
            setNotationDecl(createNotationDecl(), xMLNotationDecl);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void endDTD(Augmentations augmentations) throws XNIException {
        this.fIsImmutable = true;
        if (this.fGrammarDescription.getRootName() == null) {
            int i = this.fElementDeclCount;
            ArrayList arrayList = new ArrayList(i);
            for (int i2 = 0; i2 < i; i2++) {
                arrayList.add(this.fElementDeclName[i2 >> 8][i2 & 255].rawname);
            }
            this.fGrammarDescription.setPossibleRoots(arrayList);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void setDTDSource(XMLDTDSource xMLDTDSource) {
        this.fDTDSource = xMLDTDSource;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public XMLDTDSource getDTDSource() {
        return this.fDTDSource;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDContentModelHandler
    public void setDTDContentModelSource(XMLDTDContentModelSource xMLDTDContentModelSource) {
        this.fDTDContentModelSource = xMLDTDContentModelSource;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDContentModelHandler
    public XMLDTDContentModelSource getDTDContentModelSource() {
        return this.fDTDContentModelSource;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDContentModelHandler
    public void startContentModel(String str, Augmentations augmentations) throws XNIException {
        XMLElementDecl xMLElementDecl = this.fElementDeclTab.get(str);
        if (xMLElementDecl != null) {
            this.fElementDecl = xMLElementDecl;
        }
        this.fDepth = 0;
        initializeContentModelStack();
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDContentModelHandler
    public void startGroup(Augmentations augmentations) throws XNIException {
        this.fDepth++;
        initializeContentModelStack();
        this.fMixed = false;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDContentModelHandler
    public void pcdata(Augmentations augmentations) throws XNIException {
        this.fMixed = true;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDContentModelHandler
    public void element(String str, Augmentations augmentations) throws XNIException {
        if (this.fMixed) {
            int[] iArr = this.fNodeIndexStack;
            int i = this.fDepth;
            if (iArr[i] == -1) {
                iArr[i] = addUniqueLeafNode(str);
            } else {
                iArr[i] = addContentSpecNode(4, iArr[i], addUniqueLeafNode(str));
            }
        } else {
            this.fNodeIndexStack[this.fDepth] = addContentSpecNode(0, str);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDContentModelHandler
    public void separator(short s, Augmentations augmentations) throws XNIException {
        if (!this.fMixed) {
            short[] sArr = this.fOpStack;
            int i = this.fDepth;
            if (sArr[i] == 5 || s != 0) {
                short[] sArr2 = this.fOpStack;
                int i2 = this.fDepth;
                if (sArr2[i2] != 4 && s == 1) {
                    int[] iArr = this.fPrevNodeIndexStack;
                    if (iArr[i2] != -1) {
                        int[] iArr2 = this.fNodeIndexStack;
                        iArr2[i2] = addContentSpecNode(sArr2[i2], iArr[i2], iArr2[i2]);
                    }
                    int[] iArr3 = this.fPrevNodeIndexStack;
                    int i3 = this.fDepth;
                    iArr3[i3] = this.fNodeIndexStack[i3];
                    this.fOpStack[i3] = 5;
                    return;
                }
                return;
            }
            int[] iArr4 = this.fPrevNodeIndexStack;
            if (iArr4[i] != -1) {
                int[] iArr5 = this.fNodeIndexStack;
                iArr5[i] = addContentSpecNode(sArr[i], iArr4[i], iArr5[i]);
            }
            int[] iArr6 = this.fPrevNodeIndexStack;
            int i4 = this.fDepth;
            iArr6[i4] = this.fNodeIndexStack[i4];
            this.fOpStack[i4] = 4;
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDContentModelHandler
    public void occurrence(short s, Augmentations augmentations) throws XNIException {
        if (this.fMixed) {
            return;
        }
        if (s == 2) {
            int[] iArr = this.fNodeIndexStack;
            int i = this.fDepth;
            iArr[i] = addContentSpecNode(1, iArr[i], -1);
        } else if (s == 3) {
            int[] iArr2 = this.fNodeIndexStack;
            int i2 = this.fDepth;
            iArr2[i2] = addContentSpecNode(2, iArr2[i2], -1);
        } else if (s == 4) {
            int[] iArr3 = this.fNodeIndexStack;
            int i3 = this.fDepth;
            iArr3[i3] = addContentSpecNode(3, iArr3[i3], -1);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDContentModelHandler
    public void endGroup(Augmentations augmentations) throws XNIException {
        if (!this.fMixed) {
            int[] iArr = this.fPrevNodeIndexStack;
            int i = this.fDepth;
            if (iArr[i] != -1) {
                int[] iArr2 = this.fNodeIndexStack;
                iArr2[i] = addContentSpecNode(this.fOpStack[i], iArr[i], iArr2[i]);
            }
            int[] iArr3 = this.fNodeIndexStack;
            int i2 = this.fDepth;
            this.fDepth = i2 - 1;
            iArr3[this.fDepth] = iArr3[i2];
        }
    }

    public SymbolTable getSymbolTable() {
        return this.fSymbolTable;
    }

    public int getFirstElementDeclIndex() {
        return this.fElementDeclCount >= 0 ? 0 : -1;
    }

    public int getNextElementDeclIndex(int i) {
        if (i < this.fElementDeclCount - 1) {
            return i + 1;
        }
        return -1;
    }

    public int getElementDeclIndex(String str) {
        Integer num = this.fElementIndexMap.get(str);
        if (num == null) {
            num = -1;
        }
        return num.intValue();
    }

    public int getElementDeclIndex(QName qName) {
        return getElementDeclIndex(qName.rawname);
    }

    public short getContentSpecType(int i) {
        if (i < 0 || i >= this.fElementDeclCount) {
            return -1;
        }
        int i2 = i >> 8;
        int i3 = i & 255;
        short[][] sArr = this.fElementDeclType;
        if (sArr[i2][i3] == -1) {
            return -1;
        }
        return (short) (sArr[i2][i3] & LIST_MASK);
    }

    public boolean getElementDecl(int i, XMLElementDecl xMLElementDecl) {
        boolean z = false;
        if (i < 0 || i >= this.fElementDeclCount) {
            return false;
        }
        int i2 = i >> 8;
        int i3 = i & 255;
        xMLElementDecl.name.setValues(this.fElementDeclName[i2][i3]);
        short[][] sArr = this.fElementDeclType;
        if (sArr[i2][i3] == -1) {
            xMLElementDecl.type = -1;
            xMLElementDecl.simpleType.list = false;
        } else {
            xMLElementDecl.type = (short) (sArr[i2][i3] & LIST_MASK);
            XMLSimpleType xMLSimpleType = xMLElementDecl.simpleType;
            if ((this.fElementDeclType[i2][i3] & 128) != 0) {
                z = true;
            }
            xMLSimpleType.list = z;
        }
        if (xMLElementDecl.type == 3 || xMLElementDecl.type == 2) {
            xMLElementDecl.contentModelValidator = getElementContentModelValidator(i);
        }
        xMLElementDecl.simpleType.datatypeValidator = null;
        xMLElementDecl.simpleType.defaultType = -1;
        xMLElementDecl.simpleType.defaultValue = null;
        return true;
    }

    /* access modifiers changed from: package-private */
    public QName getElementDeclName(int i) {
        if (i < 0 || i >= this.fElementDeclCount) {
            return null;
        }
        return this.fElementDeclName[i >> 8][i & 255];
    }

    public int getFirstAttributeDeclIndex(int i) {
        return this.fElementDeclFirstAttributeDeclIndex[i >> 8][i & 255];
    }

    public int getNextAttributeDeclIndex(int i) {
        return this.fAttributeDeclNextAttributeDeclIndex[i >> 8][i & 255];
    }

    public boolean getAttributeDecl(int i, XMLAttributeDecl xMLAttributeDecl) {
        boolean z = false;
        if (i < 0 || i >= this.fAttributeDeclCount) {
            return false;
        }
        int i2 = i >> 8;
        int i3 = i & 255;
        xMLAttributeDecl.name.setValues(this.fAttributeDeclName[i2][i3]);
        short[][] sArr = this.fAttributeDeclType;
        short s = -1;
        if (sArr[i2][i3] != -1) {
            s = (short) (sArr[i2][i3] & LIST_MASK);
            if ((sArr[i2][i3] & 128) != 0) {
                z = true;
            }
        }
        xMLAttributeDecl.simpleType.setValues(s, this.fAttributeDeclName[i2][i3].localpart, this.fAttributeDeclEnumeration[i2][i3], z, this.fAttributeDeclDefaultType[i2][i3], this.fAttributeDeclDefaultValue[i2][i3], this.fAttributeDeclNonNormalizedDefaultValue[i2][i3], this.fAttributeDeclDatatypeValidator[i2][i3]);
        return true;
    }

    public boolean isCDATAAttribute(QName qName, QName qName2) {
        return !getAttributeDecl(getElementDeclIndex(qName), this.fAttributeDecl) || this.fAttributeDecl.simpleType.type == 0;
    }

    public int getEntityDeclIndex(String str) {
        if (str == null || this.fEntityIndexMap.get(str) == null) {
            return -1;
        }
        return this.fEntityIndexMap.get(str).intValue();
    }

    public boolean getEntityDecl(int i, XMLEntityDecl xMLEntityDecl) {
        if (i < 0 || i >= this.fEntityCount) {
            return false;
        }
        int i2 = i >> 8;
        int i3 = i & 255;
        xMLEntityDecl.setValues(this.fEntityName[i2][i3], this.fEntityPublicId[i2][i3], this.fEntitySystemId[i2][i3], this.fEntityBaseSystemId[i2][i3], this.fEntityNotation[i2][i3], this.fEntityValue[i2][i3], this.fEntityIsPE[i2][i3] != 0, this.fEntityInExternal[i2][i3] != 0);
        return true;
    }

    public int getNotationDeclIndex(String str) {
        if (str == null || this.fNotationIndexMap.get(str) == null) {
            return -1;
        }
        return this.fNotationIndexMap.get(str).intValue();
    }

    public boolean getNotationDecl(int i, XMLNotationDecl xMLNotationDecl) {
        if (i < 0 || i >= this.fNotationCount) {
            return false;
        }
        int i2 = i >> 8;
        int i3 = i & 255;
        xMLNotationDecl.setValues(this.fNotationName[i2][i3], this.fNotationPublicId[i2][i3], this.fNotationSystemId[i2][i3], this.fNotationBaseSystemId[i2][i3]);
        return true;
    }

    public boolean getContentSpec(int i, XMLContentSpec xMLContentSpec) {
        if (i < 0 || i >= this.fContentSpecCount) {
            return false;
        }
        int i2 = i >> 8;
        int i3 = i & 255;
        xMLContentSpec.type = this.fContentSpecType[i2][i3];
        xMLContentSpec.value = this.fContentSpecValue[i2][i3];
        xMLContentSpec.otherValue = this.fContentSpecOtherValue[i2][i3];
        return true;
    }

    public int getContentSpecIndex(int i) {
        if (i < 0 || i >= this.fElementDeclCount) {
            return -1;
        }
        return this.fElementDeclContentSpecIndex[i >> 8][i & 255];
    }

    public String getContentSpecAsString(int i) {
        if (i >= 0 && i < this.fElementDeclCount) {
            int i2 = this.fElementDeclContentSpecIndex[i >> 8][i & 255];
            XMLContentSpec xMLContentSpec = new XMLContentSpec();
            if (getContentSpec(i2, xMLContentSpec)) {
                StringBuffer stringBuffer = new StringBuffer();
                int i3 = xMLContentSpec.type & 15;
                switch (i3) {
                    case 0:
                        stringBuffer.append('(');
                        if (xMLContentSpec.value == null && xMLContentSpec.otherValue == null) {
                            stringBuffer.append("#PCDATA");
                        } else {
                            stringBuffer.append(xMLContentSpec.value);
                        }
                        stringBuffer.append(')');
                        break;
                    case 1:
                        getContentSpec(((int[]) xMLContentSpec.value)[0], xMLContentSpec);
                        short s = xMLContentSpec.type;
                        if (s == 0) {
                            stringBuffer.append('(');
                            stringBuffer.append(xMLContentSpec.value);
                            stringBuffer.append(')');
                        } else if (s == 3 || s == 2 || s == 1) {
                            stringBuffer.append('(');
                            appendContentSpec(xMLContentSpec, stringBuffer, true, i3);
                            stringBuffer.append(')');
                        } else {
                            appendContentSpec(xMLContentSpec, stringBuffer, true, i3);
                        }
                        stringBuffer.append('?');
                        break;
                    case 2:
                        getContentSpec(((int[]) xMLContentSpec.value)[0], xMLContentSpec);
                        short s2 = xMLContentSpec.type;
                        if (s2 == 0) {
                            stringBuffer.append('(');
                            if (xMLContentSpec.value == null && xMLContentSpec.otherValue == null) {
                                stringBuffer.append("#PCDATA");
                            } else if (xMLContentSpec.otherValue != null) {
                                stringBuffer.append("##any:uri=");
                                stringBuffer.append(xMLContentSpec.otherValue);
                            } else if (xMLContentSpec.value == null) {
                                stringBuffer.append(SchemaSymbols.ATTVAL_TWOPOUNDANY);
                            } else {
                                appendContentSpec(xMLContentSpec, stringBuffer, true, i3);
                            }
                            stringBuffer.append(')');
                        } else if (s2 == 3 || s2 == 2 || s2 == 1) {
                            stringBuffer.append('(');
                            appendContentSpec(xMLContentSpec, stringBuffer, true, i3);
                            stringBuffer.append(')');
                        } else {
                            appendContentSpec(xMLContentSpec, stringBuffer, true, i3);
                        }
                        stringBuffer.append('*');
                        break;
                    case 3:
                        getContentSpec(((int[]) xMLContentSpec.value)[0], xMLContentSpec);
                        short s3 = xMLContentSpec.type;
                        if (s3 == 0) {
                            stringBuffer.append('(');
                            if (xMLContentSpec.value == null && xMLContentSpec.otherValue == null) {
                                stringBuffer.append("#PCDATA");
                            } else if (xMLContentSpec.otherValue != null) {
                                stringBuffer.append("##any:uri=");
                                stringBuffer.append(xMLContentSpec.otherValue);
                            } else if (xMLContentSpec.value == null) {
                                stringBuffer.append(SchemaSymbols.ATTVAL_TWOPOUNDANY);
                            } else {
                                stringBuffer.append(xMLContentSpec.value);
                            }
                            stringBuffer.append(')');
                        } else if (s3 == 3 || s3 == 2 || s3 == 1) {
                            stringBuffer.append('(');
                            appendContentSpec(xMLContentSpec, stringBuffer, true, i3);
                            stringBuffer.append(')');
                        } else {
                            appendContentSpec(xMLContentSpec, stringBuffer, true, i3);
                        }
                        stringBuffer.append('+');
                        break;
                    case 4:
                    case 5:
                        appendContentSpec(xMLContentSpec, stringBuffer, true, i3);
                        break;
                    case 6:
                        stringBuffer.append(SchemaSymbols.ATTVAL_TWOPOUNDANY);
                        if (xMLContentSpec.otherValue != null) {
                            stringBuffer.append(":uri=");
                            stringBuffer.append(xMLContentSpec.otherValue);
                            break;
                        }
                        break;
                    case 7:
                        stringBuffer.append("##other:uri=");
                        stringBuffer.append(xMLContentSpec.otherValue);
                        break;
                    case 8:
                        stringBuffer.append(SchemaSymbols.ATTVAL_TWOPOUNDLOCAL);
                        break;
                    default:
                        stringBuffer.append("???");
                        break;
                }
                return stringBuffer.toString();
            }
        }
        return null;
    }

    public void printElements() {
        XMLElementDecl xMLElementDecl = new XMLElementDecl();
        int i = 0;
        while (true) {
            int i2 = i + 1;
            if (getElementDecl(i, xMLElementDecl)) {
                PrintStream printStream = System.out;
                printStream.println("element decl: " + xMLElementDecl.name + ", " + xMLElementDecl.name.rawname);
                i = i2;
            } else {
                return;
            }
        }
    }

    public void printAttributes(int i) {
        int firstAttributeDeclIndex = getFirstAttributeDeclIndex(i);
        System.out.print(i);
        System.out.print(" [");
        while (firstAttributeDeclIndex != -1) {
            System.out.print(' ');
            System.out.print(firstAttributeDeclIndex);
            printAttribute(firstAttributeDeclIndex);
            firstAttributeDeclIndex = getNextAttributeDeclIndex(firstAttributeDeclIndex);
            if (firstAttributeDeclIndex != -1) {
                System.out.print(",");
            }
        }
        System.out.println(" ]");
    }

    /* access modifiers changed from: protected */
    public void addContentSpecToElement(XMLElementDecl xMLElementDecl) {
        int i = this.fDepth;
        if ((i == 0 || (i == 1 && xMLElementDecl.type == 2)) && this.fNodeIndexStack != null) {
            if (xMLElementDecl.type == 2) {
                int addUniqueLeafNode = addUniqueLeafNode(null);
                int[] iArr = this.fNodeIndexStack;
                if (iArr[0] == -1) {
                    iArr[0] = addUniqueLeafNode;
                } else {
                    iArr[0] = addContentSpecNode(4, addUniqueLeafNode, iArr[0]);
                }
            }
            setContentSpecIndex(this.fCurrentElementIndex, this.fNodeIndexStack[this.fDepth]);
        }
    }

    /* access modifiers changed from: protected */
    public ContentModelValidator getElementContentModelValidator(int i) {
        ContentModelValidator contentModelValidator;
        int i2 = i >> 8;
        int i3 = i & 255;
        ContentModelValidator contentModelValidator2 = this.fElementDeclContentModelValidator[i2][i3];
        if (contentModelValidator2 != null) {
            return contentModelValidator2;
        }
        short s = this.fElementDeclType[i2][i3];
        if (s == 4) {
            return null;
        }
        int i4 = this.fElementDeclContentSpecIndex[i2][i3];
        XMLContentSpec xMLContentSpec = new XMLContentSpec();
        getContentSpec(i4, xMLContentSpec);
        if (s == 2) {
            ChildrenList childrenList = new ChildrenList();
            contentSpecTree(i4, xMLContentSpec, childrenList);
            contentModelValidator = new MixedContentModel(childrenList.qname, childrenList.type, 0, childrenList.length, false);
        } else if (s == 3) {
            contentModelValidator = createChildModel(i4);
        } else {
            throw new RuntimeException("Unknown content type for a element decl in getElementContentModelValidator() in AbstractDTDGrammar class");
        }
        this.fElementDeclContentModelValidator[i2][i3] = contentModelValidator;
        return contentModelValidator;
    }

    /* access modifiers changed from: protected */
    public int createElementDecl() {
        int i = this.fElementDeclCount;
        int i2 = i >> 8;
        int i3 = i & 255;
        ensureElementDeclCapacity(i2);
        this.fElementDeclName[i2][i3] = new QName();
        this.fElementDeclType[i2][i3] = -1;
        this.fElementDeclContentModelValidator[i2][i3] = null;
        this.fElementDeclFirstAttributeDeclIndex[i2][i3] = -1;
        this.fElementDeclLastAttributeDeclIndex[i2][i3] = -1;
        int i4 = this.fElementDeclCount;
        this.fElementDeclCount = i4 + 1;
        return i4;
    }

    /* access modifiers changed from: protected */
    public void setElementDecl(int i, XMLElementDecl xMLElementDecl) {
        if (i >= 0 && i < this.fElementDeclCount) {
            int i2 = i >> 8;
            int i3 = i & 255;
            this.fElementDeclName[i2][i3].setValues(xMLElementDecl.name);
            this.fElementDeclType[i2][i3] = xMLElementDecl.type;
            this.fElementDeclContentModelValidator[i2][i3] = xMLElementDecl.contentModelValidator;
            if (xMLElementDecl.simpleType.list) {
                short[] sArr = this.fElementDeclType[i2];
                sArr[i3] = (short) (sArr[i3] | 128);
            }
            this.fElementIndexMap.put(xMLElementDecl.name.rawname, Integer.valueOf(i));
        }
    }

    /* access modifiers changed from: protected */
    public void setFirstAttributeDeclIndex(int i, int i2) {
        if (i >= 0 && i < this.fElementDeclCount) {
            this.fElementDeclFirstAttributeDeclIndex[i >> 8][i & 255] = i2;
        }
    }

    /* access modifiers changed from: protected */
    public void setContentSpecIndex(int i, int i2) {
        if (i >= 0 && i < this.fElementDeclCount) {
            this.fElementDeclContentSpecIndex[i >> 8][i & 255] = i2;
        }
    }

    /* access modifiers changed from: protected */
    public int createAttributeDecl() {
        int i = this.fAttributeDeclCount;
        int i2 = i >> 8;
        int i3 = i & 255;
        ensureAttributeDeclCapacity(i2);
        this.fAttributeDeclName[i2][i3] = new QName();
        this.fAttributeDeclType[i2][i3] = -1;
        this.fAttributeDeclDatatypeValidator[i2][i3] = null;
        this.fAttributeDeclEnumeration[i2][i3] = null;
        this.fAttributeDeclDefaultType[i2][i3] = 0;
        this.fAttributeDeclDefaultValue[i2][i3] = null;
        this.fAttributeDeclNonNormalizedDefaultValue[i2][i3] = null;
        this.fAttributeDeclNextAttributeDeclIndex[i2][i3] = -1;
        int i4 = this.fAttributeDeclCount;
        this.fAttributeDeclCount = i4 + 1;
        return i4;
    }

    /* access modifiers changed from: protected */
    public void setAttributeDecl(int i, int i2, XMLAttributeDecl xMLAttributeDecl) {
        int i3 = i2 >> 8;
        int i4 = i2 & 255;
        this.fAttributeDeclName[i3][i4].setValues(xMLAttributeDecl.name);
        this.fAttributeDeclType[i3][i4] = xMLAttributeDecl.simpleType.type;
        if (xMLAttributeDecl.simpleType.list) {
            short[] sArr = this.fAttributeDeclType[i3];
            sArr[i4] = (short) (sArr[i4] | 128);
        }
        this.fAttributeDeclEnumeration[i3][i4] = xMLAttributeDecl.simpleType.enumeration;
        this.fAttributeDeclDefaultType[i3][i4] = xMLAttributeDecl.simpleType.defaultType;
        this.fAttributeDeclDatatypeValidator[i3][i4] = xMLAttributeDecl.simpleType.datatypeValidator;
        this.fAttributeDeclDefaultValue[i3][i4] = xMLAttributeDecl.simpleType.defaultValue;
        this.fAttributeDeclNonNormalizedDefaultValue[i3][i4] = xMLAttributeDecl.simpleType.nonNormalizedDefaultValue;
        int i5 = i >> 8;
        int i6 = i & 255;
        int i7 = this.fElementDeclFirstAttributeDeclIndex[i5][i6];
        while (i7 != -1 && i7 != i2) {
            i7 = this.fAttributeDeclNextAttributeDeclIndex[i7 >> 8][i7 & 255];
        }
        if (i7 == -1) {
            int[][] iArr = this.fElementDeclFirstAttributeDeclIndex;
            if (iArr[i5][i6] == -1) {
                iArr[i5][i6] = i2;
            } else {
                int i8 = this.fElementDeclLastAttributeDeclIndex[i5][i6];
                this.fAttributeDeclNextAttributeDeclIndex[i8 >> 8][i8 & 255] = i2;
            }
            this.fElementDeclLastAttributeDeclIndex[i5][i6] = i2;
        }
    }

    /* access modifiers changed from: protected */
    public int createContentSpec() {
        int i = this.fContentSpecCount;
        int i2 = i >> 8;
        int i3 = i & 255;
        ensureContentSpecCapacity(i2);
        this.fContentSpecType[i2][i3] = -1;
        this.fContentSpecValue[i2][i3] = null;
        this.fContentSpecOtherValue[i2][i3] = null;
        int i4 = this.fContentSpecCount;
        this.fContentSpecCount = i4 + 1;
        return i4;
    }

    /* access modifiers changed from: protected */
    public void setContentSpec(int i, XMLContentSpec xMLContentSpec) {
        int i2 = i >> 8;
        int i3 = i & 255;
        this.fContentSpecType[i2][i3] = xMLContentSpec.type;
        this.fContentSpecValue[i2][i3] = xMLContentSpec.value;
        this.fContentSpecOtherValue[i2][i3] = xMLContentSpec.otherValue;
    }

    /* access modifiers changed from: protected */
    public int createEntityDecl() {
        int i = this.fEntityCount;
        int i2 = i >> 8;
        int i3 = i & 255;
        ensureEntityDeclCapacity(i2);
        this.fEntityIsPE[i2][i3] = 0;
        this.fEntityInExternal[i2][i3] = 0;
        int i4 = this.fEntityCount;
        this.fEntityCount = i4 + 1;
        return i4;
    }

    /* access modifiers changed from: protected */
    public void setEntityDecl(int i, XMLEntityDecl xMLEntityDecl) {
        int i2 = i >> 8;
        int i3 = i & 255;
        this.fEntityName[i2][i3] = xMLEntityDecl.name;
        this.fEntityValue[i2][i3] = xMLEntityDecl.value;
        this.fEntityPublicId[i2][i3] = xMLEntityDecl.publicId;
        this.fEntitySystemId[i2][i3] = xMLEntityDecl.systemId;
        this.fEntityBaseSystemId[i2][i3] = xMLEntityDecl.baseSystemId;
        this.fEntityNotation[i2][i3] = xMLEntityDecl.notation;
        this.fEntityIsPE[i2][i3] = xMLEntityDecl.isPE;
        this.fEntityInExternal[i2][i3] = xMLEntityDecl.inExternal;
        this.fEntityIndexMap.put(xMLEntityDecl.name, Integer.valueOf(i));
    }

    /* access modifiers changed from: protected */
    public int createNotationDecl() {
        ensureNotationDeclCapacity(this.fNotationCount >> 8);
        int i = this.fNotationCount;
        this.fNotationCount = i + 1;
        return i;
    }

    /* access modifiers changed from: protected */
    public void setNotationDecl(int i, XMLNotationDecl xMLNotationDecl) {
        int i2 = i >> 8;
        int i3 = i & 255;
        this.fNotationName[i2][i3] = xMLNotationDecl.name;
        this.fNotationPublicId[i2][i3] = xMLNotationDecl.publicId;
        this.fNotationSystemId[i2][i3] = xMLNotationDecl.systemId;
        this.fNotationBaseSystemId[i2][i3] = xMLNotationDecl.baseSystemId;
        this.fNotationIndexMap.put(xMLNotationDecl.name, Integer.valueOf(i));
    }

    /* access modifiers changed from: protected */
    public int addContentSpecNode(short s, String str) {
        int createContentSpec = createContentSpec();
        this.fContentSpec.setValues(s, str, null);
        setContentSpec(createContentSpec, this.fContentSpec);
        return createContentSpec;
    }

    /* access modifiers changed from: protected */
    public int addUniqueLeafNode(String str) {
        int createContentSpec = createContentSpec();
        this.fContentSpec.setValues(0, str, null);
        setContentSpec(createContentSpec, this.fContentSpec);
        return createContentSpec;
    }

    /* access modifiers changed from: protected */
    public int addContentSpecNode(short s, int i, int i2) {
        int createContentSpec = createContentSpec();
        this.fContentSpec.setValues(s, new int[]{i}, new int[]{i2});
        setContentSpec(createContentSpec, this.fContentSpec);
        return createContentSpec;
    }

    /* access modifiers changed from: protected */
    public void initializeContentModelStack() {
        short[] sArr = this.fOpStack;
        if (sArr == null) {
            this.fOpStack = new short[8];
            this.fNodeIndexStack = new int[8];
            this.fPrevNodeIndexStack = new int[8];
        } else {
            int i = this.fDepth;
            if (i == sArr.length) {
                short[] sArr2 = new short[(i * 2)];
                System.arraycopy(sArr, 0, sArr2, 0, i);
                this.fOpStack = sArr2;
                int i2 = this.fDepth;
                int[] iArr = new int[(i2 * 2)];
                System.arraycopy(this.fNodeIndexStack, 0, iArr, 0, i2);
                this.fNodeIndexStack = iArr;
                int i3 = this.fDepth;
                int[] iArr2 = new int[(i3 * 2)];
                System.arraycopy(this.fPrevNodeIndexStack, 0, iArr2, 0, i3);
                this.fPrevNodeIndexStack = iArr2;
            }
        }
        short[] sArr3 = this.fOpStack;
        int i4 = this.fDepth;
        sArr3[i4] = -1;
        this.fNodeIndexStack[i4] = -1;
        this.fPrevNodeIndexStack[i4] = -1;
    }

    /* access modifiers changed from: package-private */
    public boolean isImmutable() {
        return this.fIsImmutable;
    }

    private void appendContentSpec(XMLContentSpec xMLContentSpec, StringBuffer stringBuffer, boolean z, int i) {
        int i2 = xMLContentSpec.type & 15;
        boolean z2 = false;
        switch (i2) {
            case 0:
                if (xMLContentSpec.value == null && xMLContentSpec.otherValue == null) {
                    stringBuffer.append("#PCDATA");
                    return;
                } else if (xMLContentSpec.value == null && xMLContentSpec.otherValue != null) {
                    stringBuffer.append("##any:uri=");
                    stringBuffer.append(xMLContentSpec.otherValue);
                    return;
                } else if (xMLContentSpec.value == null) {
                    stringBuffer.append(SchemaSymbols.ATTVAL_TWOPOUNDANY);
                    return;
                } else {
                    stringBuffer.append(xMLContentSpec.value);
                    return;
                }
            case 1:
                if (i == 3 || i == 2 || i == 1) {
                    getContentSpec(((int[]) xMLContentSpec.value)[0], xMLContentSpec);
                    stringBuffer.append('(');
                    appendContentSpec(xMLContentSpec, stringBuffer, true, i2);
                    stringBuffer.append(')');
                } else {
                    getContentSpec(((int[]) xMLContentSpec.value)[0], xMLContentSpec);
                    appendContentSpec(xMLContentSpec, stringBuffer, true, i2);
                }
                stringBuffer.append('?');
                return;
            case 2:
                if (i == 3 || i == 2 || i == 1) {
                    getContentSpec(((int[]) xMLContentSpec.value)[0], xMLContentSpec);
                    stringBuffer.append('(');
                    appendContentSpec(xMLContentSpec, stringBuffer, true, i2);
                    stringBuffer.append(')');
                } else {
                    getContentSpec(((int[]) xMLContentSpec.value)[0], xMLContentSpec);
                    appendContentSpec(xMLContentSpec, stringBuffer, true, i2);
                }
                stringBuffer.append('*');
                return;
            case 3:
                if (i == 3 || i == 2 || i == 1) {
                    stringBuffer.append('(');
                    getContentSpec(((int[]) xMLContentSpec.value)[0], xMLContentSpec);
                    appendContentSpec(xMLContentSpec, stringBuffer, true, i2);
                    stringBuffer.append(')');
                } else {
                    getContentSpec(((int[]) xMLContentSpec.value)[0], xMLContentSpec);
                    appendContentSpec(xMLContentSpec, stringBuffer, true, i2);
                }
                stringBuffer.append('+');
                return;
            case 4:
            case 5:
                if (z) {
                    stringBuffer.append('(');
                }
                short s = xMLContentSpec.type;
                int i3 = ((int[]) xMLContentSpec.otherValue)[0];
                getContentSpec(((int[]) xMLContentSpec.value)[0], xMLContentSpec);
                if (xMLContentSpec.type != s) {
                    z2 = true;
                }
                appendContentSpec(xMLContentSpec, stringBuffer, z2, i2);
                if (s == 4) {
                    stringBuffer.append('|');
                } else {
                    stringBuffer.append(',');
                }
                getContentSpec(i3, xMLContentSpec);
                appendContentSpec(xMLContentSpec, stringBuffer, true, i2);
                if (z) {
                    stringBuffer.append(')');
                    return;
                }
                return;
            case 6:
                stringBuffer.append(SchemaSymbols.ATTVAL_TWOPOUNDANY);
                if (xMLContentSpec.otherValue != null) {
                    stringBuffer.append(":uri=");
                    stringBuffer.append(xMLContentSpec.otherValue);
                    return;
                }
                return;
            case 7:
                stringBuffer.append("##other:uri=");
                stringBuffer.append(xMLContentSpec.otherValue);
                return;
            case 8:
                stringBuffer.append(SchemaSymbols.ATTVAL_TWOPOUNDLOCAL);
                return;
            default:
                stringBuffer.append("???");
                return;
        }
    }

    private void printAttribute(int i) {
        XMLAttributeDecl xMLAttributeDecl = new XMLAttributeDecl();
        if (getAttributeDecl(i, xMLAttributeDecl)) {
            System.out.print(" { ");
            System.out.print(xMLAttributeDecl.name.localpart);
            System.out.print(" }");
        }
    }

    private synchronized ContentModelValidator createChildModel(int i) {
        XMLContentSpec xMLContentSpec = new XMLContentSpec();
        getContentSpec(i, xMLContentSpec);
        if (!((xMLContentSpec.type & 15) == 6 || (xMLContentSpec.type & 15) == 7)) {
            if ((xMLContentSpec.type & 15) != 8) {
                if (xMLContentSpec.type == 0) {
                    if (xMLContentSpec.value == null) {
                        if (xMLContentSpec.otherValue == null) {
                            throw new RuntimeException("ImplementationMessages.VAL_NPCD");
                        }
                    }
                    this.fQName.setValues(null, (String) xMLContentSpec.value, (String) xMLContentSpec.value, (String) xMLContentSpec.otherValue);
                    return new SimpleContentModel(xMLContentSpec.type, this.fQName, null);
                } else if (xMLContentSpec.type == 4 || xMLContentSpec.type == 5) {
                    XMLContentSpec xMLContentSpec2 = new XMLContentSpec();
                    XMLContentSpec xMLContentSpec3 = new XMLContentSpec();
                    getContentSpec(((int[]) xMLContentSpec.value)[0], xMLContentSpec2);
                    getContentSpec(((int[]) xMLContentSpec.otherValue)[0], xMLContentSpec3);
                    if (xMLContentSpec2.type == 0 && xMLContentSpec3.type == 0) {
                        this.fQName.setValues(null, (String) xMLContentSpec2.value, (String) xMLContentSpec2.value, (String) xMLContentSpec2.otherValue);
                        this.fQName2.setValues(null, (String) xMLContentSpec3.value, (String) xMLContentSpec3.value, (String) xMLContentSpec3.otherValue);
                        return new SimpleContentModel(xMLContentSpec.type, this.fQName, this.fQName2);
                    }
                } else if (xMLContentSpec.type == 1 || xMLContentSpec.type == 2 || xMLContentSpec.type == 3) {
                    XMLContentSpec xMLContentSpec4 = new XMLContentSpec();
                    getContentSpec(((int[]) xMLContentSpec.value)[0], xMLContentSpec4);
                    if (xMLContentSpec4.type == 0) {
                        this.fQName.setValues(null, (String) xMLContentSpec4.value, (String) xMLContentSpec4.value, (String) xMLContentSpec4.otherValue);
                        return new SimpleContentModel(xMLContentSpec.type, this.fQName, null);
                    }
                } else {
                    throw new RuntimeException("ImplementationMessages.VAL_CST");
                }
            }
        }
        this.fLeafCount = 0;
        this.fLeafCount = 0;
        return new DFAContentModel(buildSyntaxTree(i, xMLContentSpec), this.fLeafCount, false);
    }

    private final CMNode buildSyntaxTree(int i, XMLContentSpec xMLContentSpec) {
        CMUniOp cMUniOp;
        getContentSpec(i, xMLContentSpec);
        if ((xMLContentSpec.type & 15) == 6) {
            int i2 = this.fLeafCount;
            this.fLeafCount = i2 + 1;
            return new CMAny(xMLContentSpec.type, (String) xMLContentSpec.otherValue, i2);
        } else if ((xMLContentSpec.type & 15) == 7) {
            int i3 = this.fLeafCount;
            this.fLeafCount = i3 + 1;
            return new CMAny(xMLContentSpec.type, (String) xMLContentSpec.otherValue, i3);
        } else if ((xMLContentSpec.type & 15) == 8) {
            short s = xMLContentSpec.type;
            int i4 = this.fLeafCount;
            this.fLeafCount = i4 + 1;
            return new CMAny(s, null, i4);
        } else if (xMLContentSpec.type == 0) {
            this.fQName.setValues(null, (String) xMLContentSpec.value, (String) xMLContentSpec.value, (String) xMLContentSpec.otherValue);
            QName qName = this.fQName;
            int i5 = this.fLeafCount;
            this.fLeafCount = i5 + 1;
            return new CMLeaf(qName, i5);
        } else {
            int i6 = ((int[]) xMLContentSpec.value)[0];
            int i7 = ((int[]) xMLContentSpec.otherValue)[0];
            if (xMLContentSpec.type == 4 || xMLContentSpec.type == 5) {
                return new CMBinOp(xMLContentSpec.type, buildSyntaxTree(i6, xMLContentSpec), buildSyntaxTree(i7, xMLContentSpec));
            }
            if (xMLContentSpec.type == 2) {
                cMUniOp = new CMUniOp(xMLContentSpec.type, buildSyntaxTree(i6, xMLContentSpec));
            } else if (xMLContentSpec.type == 2 || xMLContentSpec.type == 1 || xMLContentSpec.type == 3) {
                cMUniOp = new CMUniOp(xMLContentSpec.type, buildSyntaxTree(i6, xMLContentSpec));
            } else {
                throw new RuntimeException("ImplementationMessages.VAL_CST");
            }
            return cMUniOp;
        }
    }

    private void contentSpecTree(int i, XMLContentSpec xMLContentSpec, ChildrenList childrenList) {
        getContentSpec(i, xMLContentSpec);
        if (xMLContentSpec.type == 0 || (xMLContentSpec.type & 15) == 6 || (xMLContentSpec.type & 15) == 8 || (xMLContentSpec.type & 15) == 7) {
            if (childrenList.length == childrenList.qname.length) {
                QName[] qNameArr = new QName[(childrenList.length * 2)];
                System.arraycopy(childrenList.qname, 0, qNameArr, 0, childrenList.length);
                childrenList.qname = qNameArr;
                int[] iArr = new int[(childrenList.length * 2)];
                System.arraycopy(childrenList.type, 0, iArr, 0, childrenList.length);
                childrenList.type = iArr;
            }
            childrenList.qname[childrenList.length] = new QName(null, (String) xMLContentSpec.value, (String) xMLContentSpec.value, (String) xMLContentSpec.otherValue);
            childrenList.type[childrenList.length] = xMLContentSpec.type;
            childrenList.length++;
            return;
        }
        int i2 = xMLContentSpec.value != null ? ((int[]) xMLContentSpec.value)[0] : -1;
        if (xMLContentSpec.otherValue != null) {
            int i3 = ((int[]) xMLContentSpec.otherValue)[0];
            if (xMLContentSpec.type == 4 || xMLContentSpec.type == 5) {
                contentSpecTree(i2, xMLContentSpec, childrenList);
                contentSpecTree(i3, xMLContentSpec, childrenList);
            } else if (xMLContentSpec.type == 1 || xMLContentSpec.type == 2 || xMLContentSpec.type == 3) {
                contentSpecTree(i2, xMLContentSpec, childrenList);
            } else {
                throw new RuntimeException("Invalid content spec type seen in contentSpecTree() method of AbstractDTDGrammar class : " + ((int) xMLContentSpec.type));
            }
        }
    }

    private void ensureElementDeclCapacity(int i) {
        QName[][] qNameArr = this.fElementDeclName;
        if (i >= qNameArr.length) {
            int[][] iArr = this.fElementDeclIsExternal;
            this.fElementDeclIsExternal = resize(iArr, iArr.length * 2);
            QName[][] qNameArr2 = this.fElementDeclName;
            this.fElementDeclName = resize(qNameArr2, qNameArr2.length * 2);
            short[][] sArr = this.fElementDeclType;
            this.fElementDeclType = resize(sArr, sArr.length * 2);
            ContentModelValidator[][] contentModelValidatorArr = this.fElementDeclContentModelValidator;
            this.fElementDeclContentModelValidator = resize(contentModelValidatorArr, contentModelValidatorArr.length * 2);
            int[][] iArr2 = this.fElementDeclContentSpecIndex;
            this.fElementDeclContentSpecIndex = resize(iArr2, iArr2.length * 2);
            int[][] iArr3 = this.fElementDeclFirstAttributeDeclIndex;
            this.fElementDeclFirstAttributeDeclIndex = resize(iArr3, iArr3.length * 2);
            int[][] iArr4 = this.fElementDeclLastAttributeDeclIndex;
            this.fElementDeclLastAttributeDeclIndex = resize(iArr4, iArr4.length * 2);
        } else if (qNameArr[i] != null) {
            return;
        }
        this.fElementDeclIsExternal[i] = new int[256];
        this.fElementDeclName[i] = new QName[256];
        this.fElementDeclType[i] = new short[256];
        this.fElementDeclContentModelValidator[i] = new ContentModelValidator[256];
        this.fElementDeclContentSpecIndex[i] = new int[256];
        this.fElementDeclFirstAttributeDeclIndex[i] = new int[256];
        this.fElementDeclLastAttributeDeclIndex[i] = new int[256];
    }

    private void ensureAttributeDeclCapacity(int i) {
        QName[][] qNameArr = this.fAttributeDeclName;
        if (i >= qNameArr.length) {
            int[][] iArr = this.fAttributeDeclIsExternal;
            this.fAttributeDeclIsExternal = resize(iArr, iArr.length * 2);
            QName[][] qNameArr2 = this.fAttributeDeclName;
            this.fAttributeDeclName = resize(qNameArr2, qNameArr2.length * 2);
            short[][] sArr = this.fAttributeDeclType;
            this.fAttributeDeclType = resize(sArr, sArr.length * 2);
            String[][][] strArr = this.fAttributeDeclEnumeration;
            this.fAttributeDeclEnumeration = resize(strArr, strArr.length * 2);
            short[][] sArr2 = this.fAttributeDeclDefaultType;
            this.fAttributeDeclDefaultType = resize(sArr2, sArr2.length * 2);
            DatatypeValidator[][] datatypeValidatorArr = this.fAttributeDeclDatatypeValidator;
            this.fAttributeDeclDatatypeValidator = resize(datatypeValidatorArr, datatypeValidatorArr.length * 2);
            String[][] strArr2 = this.fAttributeDeclDefaultValue;
            this.fAttributeDeclDefaultValue = resize(strArr2, strArr2.length * 2);
            String[][] strArr3 = this.fAttributeDeclNonNormalizedDefaultValue;
            this.fAttributeDeclNonNormalizedDefaultValue = resize(strArr3, strArr3.length * 2);
            int[][] iArr2 = this.fAttributeDeclNextAttributeDeclIndex;
            this.fAttributeDeclNextAttributeDeclIndex = resize(iArr2, iArr2.length * 2);
        } else if (qNameArr[i] != null) {
            return;
        }
        this.fAttributeDeclIsExternal[i] = new int[256];
        this.fAttributeDeclName[i] = new QName[256];
        this.fAttributeDeclType[i] = new short[256];
        this.fAttributeDeclEnumeration[i] = new String[256][];
        this.fAttributeDeclDefaultType[i] = new short[256];
        this.fAttributeDeclDatatypeValidator[i] = new DatatypeValidator[256];
        this.fAttributeDeclDefaultValue[i] = new String[256];
        this.fAttributeDeclNonNormalizedDefaultValue[i] = new String[256];
        this.fAttributeDeclNextAttributeDeclIndex[i] = new int[256];
    }

    private void ensureEntityDeclCapacity(int i) {
        String[][] strArr = this.fEntityName;
        if (i >= strArr.length) {
            this.fEntityName = resize(strArr, strArr.length * 2);
            String[][] strArr2 = this.fEntityValue;
            this.fEntityValue = resize(strArr2, strArr2.length * 2);
            String[][] strArr3 = this.fEntityPublicId;
            this.fEntityPublicId = resize(strArr3, strArr3.length * 2);
            String[][] strArr4 = this.fEntitySystemId;
            this.fEntitySystemId = resize(strArr4, strArr4.length * 2);
            String[][] strArr5 = this.fEntityBaseSystemId;
            this.fEntityBaseSystemId = resize(strArr5, strArr5.length * 2);
            String[][] strArr6 = this.fEntityNotation;
            this.fEntityNotation = resize(strArr6, strArr6.length * 2);
            byte[][] bArr = this.fEntityIsPE;
            this.fEntityIsPE = resize(bArr, bArr.length * 2);
            byte[][] bArr2 = this.fEntityInExternal;
            this.fEntityInExternal = resize(bArr2, bArr2.length * 2);
        } else if (strArr[i] != null) {
            return;
        }
        this.fEntityName[i] = new String[256];
        this.fEntityValue[i] = new String[256];
        this.fEntityPublicId[i] = new String[256];
        this.fEntitySystemId[i] = new String[256];
        this.fEntityBaseSystemId[i] = new String[256];
        this.fEntityNotation[i] = new String[256];
        this.fEntityIsPE[i] = new byte[256];
        this.fEntityInExternal[i] = new byte[256];
    }

    private void ensureNotationDeclCapacity(int i) {
        String[][] strArr = this.fNotationName;
        if (i >= strArr.length) {
            this.fNotationName = resize(strArr, strArr.length * 2);
            String[][] strArr2 = this.fNotationPublicId;
            this.fNotationPublicId = resize(strArr2, strArr2.length * 2);
            String[][] strArr3 = this.fNotationSystemId;
            this.fNotationSystemId = resize(strArr3, strArr3.length * 2);
            String[][] strArr4 = this.fNotationBaseSystemId;
            this.fNotationBaseSystemId = resize(strArr4, strArr4.length * 2);
        } else if (strArr[i] != null) {
            return;
        }
        this.fNotationName[i] = new String[256];
        this.fNotationPublicId[i] = new String[256];
        this.fNotationSystemId[i] = new String[256];
        this.fNotationBaseSystemId[i] = new String[256];
    }

    private void ensureContentSpecCapacity(int i) {
        short[][] sArr = this.fContentSpecType;
        if (i >= sArr.length) {
            this.fContentSpecType = resize(sArr, sArr.length * 2);
            Object[][] objArr = this.fContentSpecValue;
            this.fContentSpecValue = resize(objArr, objArr.length * 2);
            Object[][] objArr2 = this.fContentSpecOtherValue;
            this.fContentSpecOtherValue = resize(objArr2, objArr2.length * 2);
        } else if (sArr[i] != null) {
            return;
        }
        this.fContentSpecType[i] = new short[256];
        this.fContentSpecValue[i] = new Object[256];
        this.fContentSpecOtherValue[i] = new Object[256];
    }

    private static byte[][] resize(byte[][] bArr, int i) {
        byte[][] bArr2 = new byte[i][];
        System.arraycopy(bArr, 0, bArr2, 0, bArr.length);
        return bArr2;
    }

    private static short[][] resize(short[][] sArr, int i) {
        short[][] sArr2 = new short[i][];
        System.arraycopy(sArr, 0, sArr2, 0, sArr.length);
        return sArr2;
    }

    private static int[][] resize(int[][] iArr, int i) {
        int[][] iArr2 = new int[i][];
        System.arraycopy(iArr, 0, iArr2, 0, iArr.length);
        return iArr2;
    }

    private static DatatypeValidator[][] resize(DatatypeValidator[][] datatypeValidatorArr, int i) {
        DatatypeValidator[][] datatypeValidatorArr2 = new DatatypeValidator[i][];
        System.arraycopy(datatypeValidatorArr, 0, datatypeValidatorArr2, 0, datatypeValidatorArr.length);
        return datatypeValidatorArr2;
    }

    private static ContentModelValidator[][] resize(ContentModelValidator[][] contentModelValidatorArr, int i) {
        ContentModelValidator[][] contentModelValidatorArr2 = new ContentModelValidator[i][];
        System.arraycopy(contentModelValidatorArr, 0, contentModelValidatorArr2, 0, contentModelValidatorArr.length);
        return contentModelValidatorArr2;
    }

    private static Object[][] resize(Object[][] objArr, int i) {
        Object[][] objArr2 = new Object[i][];
        System.arraycopy(objArr, 0, objArr2, 0, objArr.length);
        return objArr2;
    }

    private static QName[][] resize(QName[][] qNameArr, int i) {
        QName[][] qNameArr2 = new QName[i][];
        System.arraycopy(qNameArr, 0, qNameArr2, 0, qNameArr.length);
        return qNameArr2;
    }

    private static String[][] resize(String[][] strArr, int i) {
        String[][] strArr2 = new String[i][];
        System.arraycopy(strArr, 0, strArr2, 0, strArr.length);
        return strArr2;
    }

    private static String[][][] resize(String[][][] strArr, int i) {
        String[][][] strArr2 = new String[i][][];
        System.arraycopy(strArr, 0, strArr2, 0, strArr.length);
        return strArr2;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.validation.EntityState
    public boolean isEntityDeclared(String str) {
        return getEntityDeclIndex(str) != -1;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.validation.EntityState
    public boolean isEntityUnparsed(String str) {
        int entityDeclIndex = getEntityDeclIndex(str);
        if (entityDeclIndex <= -1) {
            return false;
        }
        if (this.fEntityNotation[entityDeclIndex >> 8][entityDeclIndex & 255] != null) {
            return true;
        }
        return false;
    }
}

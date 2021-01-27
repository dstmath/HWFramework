package ohos.com.sun.xml.internal.stream.dtd;

import ohos.com.sun.org.apache.xerces.internal.util.NamespaceSupport;
import ohos.com.sun.org.apache.xerces.internal.util.SymbolTable;
import ohos.com.sun.org.apache.xerces.internal.util.XMLChar;
import ohos.com.sun.org.apache.xerces.internal.util.XMLSymbols;
import ohos.com.sun.org.apache.xerces.internal.xni.Augmentations;
import ohos.com.sun.org.apache.xerces.internal.xni.NamespaceContext;
import ohos.com.sun.org.apache.xerces.internal.xni.QName;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLString;
import ohos.com.sun.org.apache.xerces.internal.xni.XNIException;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponentManager;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLConfigurationException;
import ohos.com.sun.xml.internal.stream.dtd.nonvalidating.DTDGrammar;
import ohos.com.sun.xml.internal.stream.dtd.nonvalidating.XMLAttributeDecl;

public class DTDGrammarUtil {
    private static final boolean DEBUG_ATTRIBUTES = false;
    private static final boolean DEBUG_ELEMENT_CHILDREN = false;
    protected static final String NAMESPACES = "http://xml.org/sax/features/namespaces";
    protected static final String SYMBOL_TABLE = "http://apache.org/xml/properties/internal/symbol-table";
    private StringBuffer fBuffer;
    private int fCurrentContentSpecType;
    private int fCurrentElementIndex;
    protected DTDGrammar fDTDGrammar;
    private boolean[] fElementContentState;
    private int fElementDepth;
    private boolean fInElementContent;
    private NamespaceContext fNamespaceContext;
    protected boolean fNamespaces;
    protected SymbolTable fSymbolTable;
    private XMLAttributeDecl fTempAttDecl;
    private QName fTempQName;

    public void endCDATA(Augmentations augmentations) throws XNIException {
    }

    public void startCDATA(Augmentations augmentations) throws XNIException {
    }

    public DTDGrammarUtil(SymbolTable symbolTable) {
        this.fDTDGrammar = null;
        this.fSymbolTable = null;
        this.fCurrentElementIndex = -1;
        this.fCurrentContentSpecType = -1;
        this.fElementContentState = new boolean[8];
        this.fElementDepth = -1;
        this.fInElementContent = false;
        this.fTempAttDecl = new XMLAttributeDecl();
        this.fTempQName = new QName();
        this.fBuffer = new StringBuffer();
        this.fNamespaceContext = null;
        this.fSymbolTable = symbolTable;
    }

    public DTDGrammarUtil(DTDGrammar dTDGrammar, SymbolTable symbolTable) {
        this.fDTDGrammar = null;
        this.fSymbolTable = null;
        this.fCurrentElementIndex = -1;
        this.fCurrentContentSpecType = -1;
        this.fElementContentState = new boolean[8];
        this.fElementDepth = -1;
        this.fInElementContent = false;
        this.fTempAttDecl = new XMLAttributeDecl();
        this.fTempQName = new QName();
        this.fBuffer = new StringBuffer();
        this.fNamespaceContext = null;
        this.fDTDGrammar = dTDGrammar;
        this.fSymbolTable = symbolTable;
    }

    public DTDGrammarUtil(DTDGrammar dTDGrammar, SymbolTable symbolTable, NamespaceContext namespaceContext) {
        this.fDTDGrammar = null;
        this.fSymbolTable = null;
        this.fCurrentElementIndex = -1;
        this.fCurrentContentSpecType = -1;
        this.fElementContentState = new boolean[8];
        this.fElementDepth = -1;
        this.fInElementContent = false;
        this.fTempAttDecl = new XMLAttributeDecl();
        this.fTempQName = new QName();
        this.fBuffer = new StringBuffer();
        this.fNamespaceContext = null;
        this.fDTDGrammar = dTDGrammar;
        this.fSymbolTable = symbolTable;
        this.fNamespaceContext = namespaceContext;
    }

    public void reset(XMLComponentManager xMLComponentManager) throws XMLConfigurationException {
        this.fDTDGrammar = null;
        this.fInElementContent = false;
        this.fCurrentElementIndex = -1;
        this.fCurrentContentSpecType = -1;
        this.fNamespaces = xMLComponentManager.getFeature("http://xml.org/sax/features/namespaces", true);
        this.fSymbolTable = (SymbolTable) xMLComponentManager.getProperty("http://apache.org/xml/properties/internal/symbol-table");
        this.fElementDepth = -1;
    }

    public void startElement(QName qName, XMLAttributes xMLAttributes) throws XNIException {
        handleStartElement(qName, xMLAttributes);
    }

    public void endElement(QName qName) throws XNIException {
        handleEndElement(qName);
    }

    public void addDTDDefaultAttrs(QName qName, XMLAttributes xMLAttributes) throws XNIException {
        DTDGrammar dTDGrammar;
        boolean z;
        int indexOf;
        int elementDeclIndex = this.fDTDGrammar.getElementDeclIndex(qName);
        if (!(elementDeclIndex == -1 || (dTDGrammar = this.fDTDGrammar) == null)) {
            int firstAttributeDeclIndex = dTDGrammar.getFirstAttributeDeclIndex(elementDeclIndex);
            while (true) {
                boolean z2 = true;
                if (firstAttributeDeclIndex == -1) {
                    break;
                }
                this.fDTDGrammar.getAttributeDecl(firstAttributeDeclIndex, this.fTempAttDecl);
                String str = this.fTempAttDecl.name.prefix;
                String str2 = this.fTempAttDecl.name.localpart;
                String str3 = this.fTempAttDecl.name.rawname;
                String attributeTypeName = getAttributeTypeName(this.fTempAttDecl);
                short s = this.fTempAttDecl.simpleType.defaultType;
                String str4 = null;
                if (this.fTempAttDecl.simpleType.defaultValue != null) {
                    str4 = this.fTempAttDecl.simpleType.defaultValue;
                }
                boolean z3 = s == 2;
                if (!(attributeTypeName == XMLSymbols.fCDATASymbol) || z3 || str4 != null) {
                    if (this.fNamespaceContext == null || !str3.startsWith("xmlns")) {
                        int length = xMLAttributes.getLength();
                        int i = 0;
                        while (true) {
                            if (i >= length) {
                                break;
                            } else if (xMLAttributes.getQName(i) == str3) {
                                break;
                            } else {
                                i++;
                            }
                        }
                        if (!z2 && str4 != null) {
                            if (this.fNamespaces && (indexOf = str3.indexOf(58)) != -1) {
                                str = this.fSymbolTable.addSymbol(str3.substring(0, indexOf));
                                str2 = this.fSymbolTable.addSymbol(str3.substring(indexOf + 1));
                            }
                            this.fTempQName.setValues(str, str2, str3, this.fTempAttDecl.name.uri);
                            xMLAttributes.addAttribute(this.fTempQName, attributeTypeName, str4);
                        }
                        firstAttributeDeclIndex = this.fDTDGrammar.getNextAttributeDeclIndex(firstAttributeDeclIndex);
                    } else {
                        int indexOf2 = str3.indexOf(58);
                        String addSymbol = this.fSymbolTable.addSymbol(indexOf2 != -1 ? str3.substring(0, indexOf2) : str3);
                        if (!((NamespaceSupport) this.fNamespaceContext).containsPrefixInCurrentContext(addSymbol)) {
                            this.fNamespaceContext.declarePrefix(addSymbol, str4);
                        }
                        str = this.fSymbolTable.addSymbol(str3.substring(0, indexOf));
                        str2 = this.fSymbolTable.addSymbol(str3.substring(indexOf + 1));
                        this.fTempQName.setValues(str, str2, str3, this.fTempAttDecl.name.uri);
                        xMLAttributes.addAttribute(this.fTempQName, attributeTypeName, str4);
                        firstAttributeDeclIndex = this.fDTDGrammar.getNextAttributeDeclIndex(firstAttributeDeclIndex);
                    }
                }
                z2 = false;
                str = this.fSymbolTable.addSymbol(str3.substring(0, indexOf));
                str2 = this.fSymbolTable.addSymbol(str3.substring(indexOf + 1));
                this.fTempQName.setValues(str, str2, str3, this.fTempAttDecl.name.uri);
                xMLAttributes.addAttribute(this.fTempQName, attributeTypeName, str4);
                firstAttributeDeclIndex = this.fDTDGrammar.getNextAttributeDeclIndex(firstAttributeDeclIndex);
            }
            int length2 = xMLAttributes.getLength();
            for (int i2 = 0; i2 < length2; i2++) {
                String qName2 = xMLAttributes.getQName(i2);
                int firstAttributeDeclIndex2 = this.fDTDGrammar.getFirstAttributeDeclIndex(elementDeclIndex);
                while (true) {
                    if (firstAttributeDeclIndex2 == -1) {
                        z = false;
                        break;
                    }
                    this.fDTDGrammar.getAttributeDecl(firstAttributeDeclIndex2, this.fTempAttDecl);
                    if (this.fTempAttDecl.name.rawname == qName2) {
                        z = true;
                        break;
                    }
                    firstAttributeDeclIndex2 = this.fDTDGrammar.getNextAttributeDeclIndex(firstAttributeDeclIndex2);
                }
                if (z) {
                    String attributeTypeName2 = getAttributeTypeName(this.fTempAttDecl);
                    xMLAttributes.setType(i2, attributeTypeName2);
                    if (xMLAttributes.isSpecified(i2) && attributeTypeName2 != XMLSymbols.fCDATASymbol) {
                        normalizeAttrValue(xMLAttributes, i2);
                    }
                }
            }
        }
    }

    private boolean normalizeAttrValue(XMLAttributes xMLAttributes, int i) {
        String value = xMLAttributes.getValue(i);
        char[] cArr = new char[value.length()];
        this.fBuffer.setLength(0);
        value.getChars(0, value.length(), cArr, 0);
        boolean z = true;
        int i2 = 0;
        boolean z2 = false;
        boolean z3 = false;
        for (int i3 = 0; i3 < cArr.length; i3++) {
            if (cArr[i3] == ' ') {
                if (z2) {
                    z3 = true;
                    z2 = false;
                }
                if (z3 && !z) {
                    this.fBuffer.append(cArr[i3]);
                    i2++;
                    z3 = false;
                }
            } else {
                this.fBuffer.append(cArr[i3]);
                i2++;
                z2 = true;
                z3 = false;
                z = false;
            }
        }
        if (i2 > 0) {
            int i4 = i2 - 1;
            if (this.fBuffer.charAt(i4) == ' ') {
                this.fBuffer.setLength(i4);
            }
        }
        String stringBuffer = this.fBuffer.toString();
        xMLAttributes.setValue(i, stringBuffer);
        return !value.equals(stringBuffer);
    }

    private String getAttributeTypeName(XMLAttributeDecl xMLAttributeDecl) {
        switch (xMLAttributeDecl.simpleType.type) {
            case 1:
                if (xMLAttributeDecl.simpleType.list) {
                    return XMLSymbols.fENTITIESSymbol;
                }
                return XMLSymbols.fENTITYSymbol;
            case 2:
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append('(');
                for (int i = 0; i < xMLAttributeDecl.simpleType.enumeration.length; i++) {
                    if (i > 0) {
                        stringBuffer.append("|");
                    }
                    stringBuffer.append(xMLAttributeDecl.simpleType.enumeration[i]);
                }
                stringBuffer.append(')');
                return this.fSymbolTable.addSymbol(stringBuffer.toString());
            case 3:
                return XMLSymbols.fIDSymbol;
            case 4:
                if (xMLAttributeDecl.simpleType.list) {
                    return XMLSymbols.fIDREFSSymbol;
                }
                return XMLSymbols.fIDREFSymbol;
            case 5:
                if (xMLAttributeDecl.simpleType.list) {
                    return XMLSymbols.fNMTOKENSSymbol;
                }
                return XMLSymbols.fNMTOKENSymbol;
            case 6:
                return XMLSymbols.fNOTATIONSymbol;
            default:
                return XMLSymbols.fCDATASymbol;
        }
    }

    private void ensureStackCapacity(int i) {
        boolean[] zArr = this.fElementContentState;
        if (i == zArr.length) {
            boolean[] zArr2 = new boolean[(i * 2)];
            System.arraycopy(zArr, 0, zArr2, 0, i);
            this.fElementContentState = zArr2;
        }
    }

    /* access modifiers changed from: protected */
    public void handleStartElement(QName qName, XMLAttributes xMLAttributes) throws XNIException {
        DTDGrammar dTDGrammar = this.fDTDGrammar;
        boolean z = false;
        if (dTDGrammar == null) {
            this.fCurrentElementIndex = -1;
            this.fCurrentContentSpecType = -1;
            this.fInElementContent = false;
            return;
        }
        this.fCurrentElementIndex = dTDGrammar.getElementDeclIndex(qName);
        this.fCurrentContentSpecType = this.fDTDGrammar.getContentSpecType(this.fCurrentElementIndex);
        addDTDDefaultAttrs(qName, xMLAttributes);
        if (this.fCurrentContentSpecType == 3) {
            z = true;
        }
        this.fInElementContent = z;
        this.fElementDepth++;
        ensureStackCapacity(this.fElementDepth);
        this.fElementContentState[this.fElementDepth] = this.fInElementContent;
    }

    /* access modifiers changed from: protected */
    public void handleEndElement(QName qName) throws XNIException {
        if (this.fDTDGrammar != null) {
            this.fElementDepth--;
            int i = this.fElementDepth;
            if (i < -1) {
                throw new RuntimeException("FWK008 Element stack underflow");
            } else if (i < 0) {
                this.fCurrentElementIndex = -1;
                this.fCurrentContentSpecType = -1;
                this.fInElementContent = false;
            } else {
                this.fInElementContent = this.fElementContentState[i];
            }
        }
    }

    public boolean isInElementContent() {
        return this.fInElementContent;
    }

    public boolean isIgnorableWhiteSpace(XMLString xMLString) {
        if (!isInElementContent()) {
            return false;
        }
        for (int i = xMLString.offset; i < xMLString.offset + xMLString.length; i++) {
            if (!XMLChar.isSpace(xMLString.ch[i])) {
                return false;
            }
        }
        return true;
    }
}

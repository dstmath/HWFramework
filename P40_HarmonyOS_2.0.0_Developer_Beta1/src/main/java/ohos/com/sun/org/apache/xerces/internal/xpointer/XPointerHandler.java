package ohos.com.sun.org.apache.xerces.internal.xpointer;

import java.util.Hashtable;
import java.util.Vector;
import ohos.com.sun.org.apache.xerces.internal.impl.XMLErrorReporter;
import ohos.com.sun.org.apache.xerces.internal.util.SymbolTable;
import ohos.com.sun.org.apache.xerces.internal.util.XMLChar;
import ohos.com.sun.org.apache.xerces.internal.util.XMLSymbols;
import ohos.com.sun.org.apache.xerces.internal.xinclude.XIncludeHandler;
import ohos.com.sun.org.apache.xerces.internal.xinclude.XIncludeNamespaceSupport;
import ohos.com.sun.org.apache.xerces.internal.xni.Augmentations;
import ohos.com.sun.org.apache.xerces.internal.xni.QName;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLString;
import ohos.com.sun.org.apache.xerces.internal.xni.XNIException;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLConfigurationException;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLErrorHandler;

public final class XPointerHandler extends XIncludeHandler implements XPointerProcessor {
    private final String ELEMENT_SCHEME_NAME;
    protected XMLErrorHandler fErrorHandler;
    protected boolean fFixupBase;
    protected boolean fFixupLang;
    protected boolean fFoundMatchingPtrPart;
    protected boolean fIsXPointerResolved;
    protected SymbolTable fSymbolTable;
    protected XMLErrorReporter fXPointerErrorReporter;
    protected XPointerPart fXPointerPart;
    protected Vector fXPointerParts;

    public XPointerHandler() {
        this.fXPointerParts = null;
        this.fXPointerPart = null;
        this.fFoundMatchingPtrPart = false;
        this.fSymbolTable = null;
        this.ELEMENT_SCHEME_NAME = "element";
        this.fIsXPointerResolved = false;
        this.fFixupBase = false;
        this.fFixupLang = false;
        this.fXPointerParts = new Vector();
        this.fSymbolTable = new SymbolTable();
    }

    public XPointerHandler(SymbolTable symbolTable, XMLErrorHandler xMLErrorHandler, XMLErrorReporter xMLErrorReporter) {
        this.fXPointerParts = null;
        this.fXPointerPart = null;
        this.fFoundMatchingPtrPart = false;
        this.fSymbolTable = null;
        this.ELEMENT_SCHEME_NAME = "element";
        this.fIsXPointerResolved = false;
        this.fFixupBase = false;
        this.fFixupLang = false;
        this.fXPointerParts = new Vector();
        this.fSymbolTable = symbolTable;
        this.fErrorHandler = xMLErrorHandler;
        this.fXPointerErrorReporter = xMLErrorReporter;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xpointer.XPointerProcessor
    public void parseXPointer(String str) throws XNIException {
        init();
        Tokens tokens = new Tokens(this.fSymbolTable);
        if (!new Scanner(this.fSymbolTable) {
            /* class ohos.com.sun.org.apache.xerces.internal.xpointer.XPointerHandler.AnonymousClass1 */

            /* access modifiers changed from: protected */
            @Override // ohos.com.sun.org.apache.xerces.internal.xpointer.XPointerHandler.Scanner
            public void addToken(Tokens tokens, int i) throws XNIException {
                if (i == 0 || i == 1 || i == 3 || i == 4 || i == 2) {
                    super.addToken(tokens, i);
                } else {
                    XPointerHandler.this.reportError("InvalidXPointerToken", new Object[]{tokens.getTokenString(i)});
                }
            }
        }.scanExpr(this.fSymbolTable, tokens, str, 0, str.length())) {
            reportError("InvalidXPointerExpression", new Object[]{str});
        }
        while (tokens.hasMore()) {
            int nextToken = tokens.nextToken();
            if (nextToken == 2) {
                String tokenString = tokens.getTokenString(tokens.nextToken());
                if (tokenString == null) {
                    reportError("InvalidXPointerExpression", new Object[]{str});
                }
                ShortHandPointer shortHandPointer = new ShortHandPointer(this.fSymbolTable);
                shortHandPointer.setSchemeName(tokenString);
                this.fXPointerParts.add(shortHandPointer);
            } else if (nextToken != 3) {
                reportError("InvalidXPointerExpression", new Object[]{str});
            } else {
                String str2 = tokens.getTokenString(tokens.nextToken()) + tokens.getTokenString(tokens.nextToken());
                int nextToken2 = tokens.nextToken();
                if (tokens.getTokenString(nextToken2) != "XPTRTOKEN_OPEN_PAREN") {
                    if (nextToken2 == 2) {
                        reportError("MultipleShortHandPointers", new Object[]{str});
                    } else {
                        reportError("InvalidXPointerExpression", new Object[]{str});
                    }
                }
                int i = 1;
                while (tokens.hasMore() && tokens.getTokenString(tokens.nextToken()) == "XPTRTOKEN_OPEN_PAREN") {
                    i++;
                }
                String tokenString2 = tokens.getTokenString(tokens.nextToken());
                if (tokens.getTokenString(tokens.nextToken()) != "XPTRTOKEN_CLOSE_PAREN") {
                    reportError("SchemeDataNotFollowedByCloseParenthesis", new Object[]{str});
                }
                int i2 = 1;
                while (tokens.hasMore() && tokens.getTokenString(tokens.peekToken()) == "XPTRTOKEN_OPEN_PAREN") {
                    i2++;
                }
                if (i != i2) {
                    reportError("UnbalancedParenthesisInXPointerExpression", new Object[]{str, new Integer(i), new Integer(i2)});
                }
                if (str2.equals("element")) {
                    ElementSchemePointer elementSchemePointer = new ElementSchemePointer(this.fSymbolTable, this.fErrorReporter);
                    elementSchemePointer.setSchemeName(str2);
                    elementSchemePointer.setSchemeData(tokenString2);
                    try {
                        elementSchemePointer.parseXPointer(tokenString2);
                        this.fXPointerParts.add(elementSchemePointer);
                    } catch (XNIException e) {
                        throw new XNIException(e);
                    }
                } else {
                    reportWarning("SchemeUnsupported", new Object[]{str2});
                }
            }
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xpointer.XPointerProcessor
    public boolean resolveXPointer(QName qName, XMLAttributes xMLAttributes, Augmentations augmentations, int i) throws XNIException {
        boolean z;
        if (!this.fFoundMatchingPtrPart) {
            z = false;
            for (int i2 = 0; i2 < this.fXPointerParts.size(); i2++) {
                this.fXPointerPart = (XPointerPart) this.fXPointerParts.get(i2);
                if (this.fXPointerPart.resolveXPointer(qName, xMLAttributes, augmentations, i)) {
                    this.fFoundMatchingPtrPart = true;
                    z = true;
                }
            }
        } else {
            z = this.fXPointerPart.resolveXPointer(qName, xMLAttributes, augmentations, i);
        }
        if (!this.fIsXPointerResolved) {
            this.fIsXPointerResolved = z;
        }
        return z;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xpointer.XPointerProcessor
    public boolean isFragmentResolved() throws XNIException {
        XPointerPart xPointerPart = this.fXPointerPart;
        boolean isFragmentResolved = xPointerPart != null ? xPointerPart.isFragmentResolved() : false;
        if (!this.fIsXPointerResolved) {
            this.fIsXPointerResolved = isFragmentResolved;
        }
        return isFragmentResolved;
    }

    public boolean isChildFragmentResolved() throws XNIException {
        XPointerPart xPointerPart = this.fXPointerPart;
        if (xPointerPart != null) {
            return xPointerPart.isChildFragmentResolved();
        }
        return false;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xpointer.XPointerProcessor
    public boolean isXPointerResolved() throws XNIException {
        return this.fIsXPointerResolved;
    }

    public XPointerPart getXPointerPart() {
        return this.fXPointerPart;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void reportError(String str, Object[] objArr) throws XNIException {
        throw new XNIException(this.fErrorReporter.getMessageFormatter(XPointerMessageFormatter.XPOINTER_DOMAIN).formatMessage(this.fErrorReporter.getLocale(), str, objArr));
    }

    private void reportWarning(String str, Object[] objArr) throws XNIException {
        this.fXPointerErrorReporter.reportError(XPointerMessageFormatter.XPOINTER_DOMAIN, str, objArr, 0);
    }

    /* access modifiers changed from: protected */
    public void initErrorReporter() {
        if (this.fXPointerErrorReporter == null) {
            this.fXPointerErrorReporter = new XMLErrorReporter();
        }
        if (this.fErrorHandler == null) {
            this.fErrorHandler = new XPointerErrorHandler();
        }
        this.fXPointerErrorReporter.putMessageFormatter(XPointerMessageFormatter.XPOINTER_DOMAIN, new XPointerMessageFormatter());
    }

    /* access modifiers changed from: protected */
    public void init() {
        this.fXPointerParts.clear();
        this.fXPointerPart = null;
        this.fFoundMatchingPtrPart = false;
        this.fIsXPointerResolved = false;
        initErrorReporter();
    }

    public Vector getPointerParts() {
        return this.fXPointerParts;
    }

    /* access modifiers changed from: private */
    public final class Tokens {
        private static final int INITIAL_TOKEN_COUNT = 256;
        private static final int XPTRTOKEN_CLOSE_PAREN = 1;
        private static final int XPTRTOKEN_OPEN_PAREN = 0;
        private static final int XPTRTOKEN_SCHEMEDATA = 4;
        private static final int XPTRTOKEN_SCHEMENAME = 3;
        private static final int XPTRTOKEN_SHORTHAND = 2;
        private int fCurrentTokenIndex;
        private SymbolTable fSymbolTable;
        private int fTokenCount;
        private Hashtable fTokenNames;
        private int[] fTokens;
        private final String[] fgTokenNames;

        private Tokens(SymbolTable symbolTable) {
            this.fgTokenNames = new String[]{"XPTRTOKEN_OPEN_PAREN", "XPTRTOKEN_CLOSE_PAREN", "XPTRTOKEN_SHORTHAND", "XPTRTOKEN_SCHEMENAME", "XPTRTOKEN_SCHEMEDATA"};
            this.fTokens = new int[256];
            this.fTokenCount = 0;
            this.fTokenNames = new Hashtable();
            this.fSymbolTable = symbolTable;
            this.fTokenNames.put(new Integer(0), "XPTRTOKEN_OPEN_PAREN");
            this.fTokenNames.put(new Integer(1), "XPTRTOKEN_CLOSE_PAREN");
            this.fTokenNames.put(new Integer(2), "XPTRTOKEN_SHORTHAND");
            this.fTokenNames.put(new Integer(3), "XPTRTOKEN_SCHEMENAME");
            this.fTokenNames.put(new Integer(4), "XPTRTOKEN_SCHEMEDATA");
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private String getTokenString(int i) {
            return (String) this.fTokenNames.get(new Integer(i));
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void addToken(String str) {
            Integer num = (Integer) this.fTokenNames.get(str);
            if (num == null) {
                num = new Integer(this.fTokenNames.size());
                this.fTokenNames.put(num, str);
            }
            addToken(num.intValue());
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void addToken(int i) {
            try {
                this.fTokens[this.fTokenCount] = i;
            } catch (ArrayIndexOutOfBoundsException unused) {
                int[] iArr = this.fTokens;
                int i2 = this.fTokenCount;
                this.fTokens = new int[(i2 << 1)];
                System.arraycopy(iArr, 0, this.fTokens, 0, i2);
                this.fTokens[this.fTokenCount] = i;
            }
            this.fTokenCount++;
        }

        private void rewind() {
            this.fCurrentTokenIndex = 0;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean hasMore() {
            return this.fCurrentTokenIndex < this.fTokenCount;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private int nextToken() throws XNIException {
            if (this.fCurrentTokenIndex == this.fTokenCount) {
                XPointerHandler.this.reportError("XPointerProcessingError", null);
            }
            int[] iArr = this.fTokens;
            int i = this.fCurrentTokenIndex;
            this.fCurrentTokenIndex = i + 1;
            return iArr[i];
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private int peekToken() throws XNIException {
            if (this.fCurrentTokenIndex == this.fTokenCount) {
                XPointerHandler.this.reportError("XPointerProcessingError", null);
            }
            return this.fTokens[this.fCurrentTokenIndex];
        }

        private String nextTokenAsString() throws XNIException {
            String tokenString = getTokenString(nextToken());
            if (tokenString == null) {
                XPointerHandler.this.reportError("XPointerProcessingError", null);
            }
            return tokenString;
        }
    }

    private class Scanner {
        private static final byte CHARTYPE_CARRET = 3;
        private static final byte CHARTYPE_CLOSE_PAREN = 5;
        private static final byte CHARTYPE_COLON = 10;
        private static final byte CHARTYPE_DIGIT = 9;
        private static final byte CHARTYPE_EQUAL = 11;
        private static final byte CHARTYPE_INVALID = 0;
        private static final byte CHARTYPE_LETTER = 12;
        private static final byte CHARTYPE_MINUS = 6;
        private static final byte CHARTYPE_NONASCII = 14;
        private static final byte CHARTYPE_OPEN_PAREN = 4;
        private static final byte CHARTYPE_OTHER = 1;
        private static final byte CHARTYPE_PERIOD = 7;
        private static final byte CHARTYPE_SLASH = 8;
        private static final byte CHARTYPE_UNDERSCORE = 13;
        private static final byte CHARTYPE_WHITESPACE = 2;
        private final byte[] fASCIICharMap;
        private SymbolTable fSymbolTable;

        private Scanner(SymbolTable symbolTable) {
            this.fASCIICharMap = new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 2, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 1, 1, 1, 1, 1, 1, 1, 4, 5, 1, 1, 1, 6, 7, 8, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 10, 1, 1, 11, 1, 1, 1, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 1, 1, 1, 3, 13, 1, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 1, 1, 1, 1, 1};
            this.fSymbolTable = symbolTable;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean scanExpr(SymbolTable symbolTable, Tokens tokens, String str, int i, int i2) throws XNIException {
            byte b;
            StringBuffer stringBuffer = new StringBuffer();
            int i3 = 0;
            String str2 = null;
            int i4 = 0;
            while (i != i2) {
                char charAt = str.charAt(i);
                while (true) {
                    if ((charAt == ' ' || charAt == '\n' || charAt == '\t' || charAt == '\r') && (i = i + 1) != i2) {
                        charAt = str.charAt(i);
                    }
                }
                if (i == i2) {
                    return true;
                }
                if (charAt >= 128) {
                    b = 14;
                } else {
                    b = this.fASCIICharMap[charAt];
                }
                switch (b) {
                    case 1:
                    case 2:
                    case 3:
                    case 6:
                    case 7:
                    case 8:
                    case 9:
                    case 10:
                    case 11:
                    case 12:
                    case 13:
                    case 14:
                        if (i4 == 0) {
                            int scanNCName = scanNCName(str, i2, i);
                            if (scanNCName == i) {
                                XPointerHandler.this.reportError("InvalidShortHandPointer", new Object[]{str});
                                return false;
                            }
                            char charAt2 = scanNCName < i2 ? str.charAt(scanNCName) : 65535;
                            String addSymbol = symbolTable.addSymbol(str.substring(i, scanNCName));
                            String str3 = XMLSymbols.EMPTY_STRING;
                            if (charAt2 == ':') {
                                int i5 = scanNCName + 1;
                                if (i5 == i2) {
                                    return false;
                                }
                                str.charAt(i5);
                                int scanNCName2 = scanNCName(str, i2, i5);
                                if (scanNCName2 == i5) {
                                    return false;
                                }
                                if (scanNCName2 < i2) {
                                    str.charAt(scanNCName2);
                                }
                                str2 = symbolTable.addSymbol(str.substring(i5, scanNCName2));
                                scanNCName = scanNCName2;
                            } else {
                                str2 = addSymbol;
                                addSymbol = str3;
                            }
                            if (scanNCName != i2) {
                                addToken(tokens, 3);
                                tokens.addToken(addSymbol);
                                tokens.addToken(str2);
                            } else if (scanNCName == i2) {
                                addToken(tokens, 2);
                                tokens.addToken(str2);
                            }
                            i = scanNCName;
                            i3 = 0;
                            continue;
                        } else if (i4 <= 0 || i3 != 0 || str2 == null) {
                            return false;
                        } else {
                            int scanData = scanData(str, stringBuffer, i2, i);
                            if (scanData == i) {
                                XPointerHandler.this.reportError("InvalidSchemeDataInXPointer", new Object[]{str});
                                return false;
                            }
                            if (scanData < i2) {
                                str.charAt(scanData);
                            }
                            String addSymbol2 = symbolTable.addSymbol(stringBuffer.toString());
                            addToken(tokens, 4);
                            tokens.addToken(addSymbol2);
                            stringBuffer.delete(0, stringBuffer.length());
                            i = scanData;
                            i4 = 0;
                        }
                        break;
                    case 4:
                        addToken(tokens, 0);
                        i4++;
                        break;
                    case 5:
                        addToken(tokens, 1);
                        i3++;
                        break;
                }
                i++;
            }
            return true;
        }

        private int scanNCName(String str, int i, int i2) {
            char charAt = str.charAt(i2);
            if (charAt < 128) {
                byte b = this.fASCIICharMap[charAt];
                if (!(b == 12 || b == 13)) {
                    return i2;
                }
            } else if (!XMLChar.isNameStart(charAt)) {
                return i2;
            }
            while (true) {
                i2++;
                if (i2 < i) {
                    char charAt2 = str.charAt(i2);
                    if (charAt2 < 128) {
                        byte b2 = this.fASCIICharMap[charAt2];
                        if (!(b2 == 12 || b2 == 9 || b2 == 7 || b2 == 6 || b2 == 13)) {
                            break;
                        }
                    } else if (!XMLChar.isName(charAt2)) {
                        break;
                    }
                } else {
                    break;
                }
            }
            return i2;
        }

        private int scanData(String str, StringBuffer stringBuffer, int i, int i2) {
            byte b;
            while (i2 != i) {
                char charAt = str.charAt(i2);
                byte b2 = 14;
                if (charAt >= 128) {
                    b = 14;
                } else {
                    b = this.fASCIICharMap[charAt];
                }
                if (b == 4) {
                    stringBuffer.append((int) charAt);
                    i2 = scanData(str, stringBuffer, i, i2 + 1);
                    if (i2 == i) {
                        return i2;
                    }
                    char charAt2 = str.charAt(i2);
                    if (charAt2 < 128) {
                        b2 = this.fASCIICharMap[charAt2];
                    }
                    if (b2 != 5) {
                        return i;
                    }
                    stringBuffer.append((char) charAt2);
                } else if (b == 5) {
                    return i2;
                } else {
                    if (b == 3) {
                        i2++;
                        char charAt3 = str.charAt(i2);
                        if (charAt3 < 128) {
                            b2 = this.fASCIICharMap[charAt3];
                        }
                        if (b2 != 3 && b2 != 4 && b2 != 5) {
                            break;
                        }
                        stringBuffer.append((char) charAt3);
                    } else {
                        stringBuffer.append((char) charAt);
                    }
                }
                i2++;
            }
            return i2;
        }

        /* access modifiers changed from: protected */
        public void addToken(Tokens tokens, int i) throws XNIException {
            tokens.addToken(i);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xinclude.XIncludeHandler, ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void comment(XMLString xMLString, Augmentations augmentations) throws XNIException {
        if (isChildFragmentResolved()) {
            super.comment(xMLString, augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xinclude.XIncludeHandler, ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void processingInstruction(String str, XMLString xMLString, Augmentations augmentations) throws XNIException {
        if (isChildFragmentResolved()) {
            super.processingInstruction(str, xMLString, augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xinclude.XIncludeHandler, ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void startElement(QName qName, XMLAttributes xMLAttributes, Augmentations augmentations) throws XNIException {
        if (!resolveXPointer(qName, xMLAttributes, augmentations, 0)) {
            if (this.fFixupBase) {
                processXMLBaseAttributes(xMLAttributes);
            }
            if (this.fFixupLang) {
                processXMLLangAttributes(xMLAttributes);
            }
            this.fNamespaceContext.setContextInvalid();
            return;
        }
        super.startElement(qName, xMLAttributes, augmentations);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xinclude.XIncludeHandler, ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void emptyElement(QName qName, XMLAttributes xMLAttributes, Augmentations augmentations) throws XNIException {
        if (!resolveXPointer(qName, xMLAttributes, augmentations, 2)) {
            if (this.fFixupBase) {
                processXMLBaseAttributes(xMLAttributes);
            }
            if (this.fFixupLang) {
                processXMLLangAttributes(xMLAttributes);
            }
            this.fNamespaceContext.setContextInvalid();
            return;
        }
        super.emptyElement(qName, xMLAttributes, augmentations);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xinclude.XIncludeHandler, ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void characters(XMLString xMLString, Augmentations augmentations) throws XNIException {
        if (isChildFragmentResolved()) {
            super.characters(xMLString, augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xinclude.XIncludeHandler, ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void ignorableWhitespace(XMLString xMLString, Augmentations augmentations) throws XNIException {
        if (isChildFragmentResolved()) {
            super.ignorableWhitespace(xMLString, augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xinclude.XIncludeHandler, ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void endElement(QName qName, Augmentations augmentations) throws XNIException {
        if (resolveXPointer(qName, null, augmentations, 1)) {
            super.endElement(qName, augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xinclude.XIncludeHandler, ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void startCDATA(Augmentations augmentations) throws XNIException {
        if (isChildFragmentResolved()) {
            super.startCDATA(augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xinclude.XIncludeHandler, ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void endCDATA(Augmentations augmentations) throws XNIException {
        if (isChildFragmentResolved()) {
            super.endCDATA(augmentations);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xinclude.XIncludeHandler, ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent
    public void setProperty(String str, Object obj) throws XMLConfigurationException {
        if (str == "http://apache.org/xml/properties/internal/error-reporter") {
            if (obj != null) {
                this.fXPointerErrorReporter = (XMLErrorReporter) obj;
            } else {
                this.fXPointerErrorReporter = null;
            }
        }
        if (str == "http://apache.org/xml/properties/internal/error-handler") {
            if (obj != null) {
                this.fErrorHandler = (XMLErrorHandler) obj;
            } else {
                this.fErrorHandler = null;
            }
        }
        if (str == "http://apache.org/xml/features/xinclude/fixup-language") {
            if (obj != null) {
                this.fFixupLang = ((Boolean) obj).booleanValue();
            } else {
                this.fFixupLang = false;
            }
        }
        if (str == "http://apache.org/xml/features/xinclude/fixup-base-uris") {
            if (obj != null) {
                this.fFixupBase = ((Boolean) obj).booleanValue();
            } else {
                this.fFixupBase = false;
            }
        }
        if (str == "http://apache.org/xml/properties/internal/namespace-context") {
            this.fNamespaceContext = (XIncludeNamespaceSupport) obj;
        }
        super.setProperty(str, obj);
    }
}

package ohos.com.sun.org.apache.xerces.internal.xpointer;

import java.util.Hashtable;
import ohos.com.sun.org.apache.xerces.internal.impl.XMLErrorReporter;
import ohos.com.sun.org.apache.xerces.internal.util.SymbolTable;
import ohos.com.sun.org.apache.xerces.internal.util.XMLChar;
import ohos.com.sun.org.apache.xerces.internal.xni.Augmentations;
import ohos.com.sun.org.apache.xerces.internal.xni.QName;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes;
import ohos.com.sun.org.apache.xerces.internal.xni.XNIException;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLErrorHandler;

/* access modifiers changed from: package-private */
public class ElementSchemePointer implements XPointerPart {
    private int[] fChildSequence;
    private int fCurrentChildDepth = 0;
    private int fCurrentChildPosition = 1;
    private int[] fCurrentChildSequence;
    protected XMLErrorHandler fErrorHandler;
    protected XMLErrorReporter fErrorReporter;
    int fFoundDepth = 0;
    private boolean fIsElementFound = false;
    private boolean fIsFragmentResolved = false;
    private boolean fIsResolveElement = false;
    boolean fIsShortHand = false;
    private String fSchemeData;
    private String fSchemeName;
    private ShortHandPointer fShortHandPointer;
    private String fShortHandPointerName;
    private SymbolTable fSymbolTable;
    private boolean fWasOnlyEmptyElementFound = false;

    public ElementSchemePointer() {
    }

    public ElementSchemePointer(SymbolTable symbolTable) {
        this.fSymbolTable = symbolTable;
    }

    public ElementSchemePointer(SymbolTable symbolTable, XMLErrorReporter xMLErrorReporter) {
        this.fSymbolTable = symbolTable;
        this.fErrorReporter = xMLErrorReporter;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xpointer.XPointerPart
    public void parseXPointer(String str) throws XNIException {
        init();
        Tokens tokens = new Tokens(this.fSymbolTable);
        if (!new Scanner(this.fSymbolTable) {
            /* class ohos.com.sun.org.apache.xerces.internal.xpointer.ElementSchemePointer.AnonymousClass1 */

            /* access modifiers changed from: protected */
            @Override // ohos.com.sun.org.apache.xerces.internal.xpointer.ElementSchemePointer.Scanner
            public void addToken(Tokens tokens, int i) throws XNIException {
                if (i == 1 || i == 0) {
                    super.addToken(tokens, i);
                } else {
                    ElementSchemePointer.this.reportError("InvalidElementSchemeToken", new Object[]{tokens.getTokenString(i)});
                }
            }
        }.scanExpr(this.fSymbolTable, tokens, str, 0, str.length())) {
            reportError("InvalidElementSchemeXPointer", new Object[]{str});
        }
        int[] iArr = new int[((tokens.getTokenCount() / 2) + 1)];
        int i = 0;
        while (tokens.hasMore()) {
            int nextToken = tokens.nextToken();
            if (nextToken == 0) {
                this.fShortHandPointerName = tokens.getTokenString(tokens.nextToken());
                this.fShortHandPointer = new ShortHandPointer(this.fSymbolTable);
                this.fShortHandPointer.setSchemeName(this.fShortHandPointerName);
            } else if (nextToken != 1) {
                reportError("InvalidElementSchemeXPointer", new Object[]{str});
            } else {
                iArr[i] = tokens.nextToken();
                i++;
            }
        }
        this.fChildSequence = new int[i];
        this.fCurrentChildSequence = new int[i];
        System.arraycopy(iArr, 0, this.fChildSequence, 0, i);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xpointer.XPointerPart
    public String getSchemeName() {
        return this.fSchemeName;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xpointer.XPointerPart
    public String getSchemeData() {
        return this.fSchemeData;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xpointer.XPointerPart
    public void setSchemeName(String str) {
        this.fSchemeName = str;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xpointer.XPointerPart
    public void setSchemeData(String str) {
        this.fSchemeData = str;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xpointer.XPointerPart
    public boolean resolveXPointer(QName qName, XMLAttributes xMLAttributes, Augmentations augmentations, int i) throws XNIException {
        boolean z;
        if (this.fShortHandPointerName != null) {
            z = this.fShortHandPointer.resolveXPointer(qName, xMLAttributes, augmentations, i);
            if (z) {
                this.fIsResolveElement = true;
                this.fIsShortHand = true;
            } else {
                this.fIsResolveElement = false;
            }
        } else {
            this.fIsResolveElement = true;
            z = false;
        }
        int[] iArr = this.fChildSequence;
        if (iArr.length > 0) {
            this.fIsFragmentResolved = matchChildSequence(qName, i);
        } else if (!z || iArr.length > 0) {
            this.fIsFragmentResolved = false;
        } else {
            this.fIsFragmentResolved = z;
        }
        return this.fIsFragmentResolved;
    }

    /* access modifiers changed from: protected */
    public boolean matchChildSequence(QName qName, int i) throws XNIException {
        int i2;
        int i3 = this.fCurrentChildDepth;
        int[] iArr = this.fCurrentChildSequence;
        if (i3 >= iArr.length) {
            int[] iArr2 = new int[iArr.length];
            System.arraycopy(iArr, 0, iArr2, 0, iArr.length);
            this.fCurrentChildSequence = new int[(this.fCurrentChildDepth * 2)];
            System.arraycopy(iArr2, 0, this.fCurrentChildSequence, 0, iArr2.length);
        }
        if (this.fIsResolveElement) {
            this.fWasOnlyEmptyElementFound = false;
            if (i == 0) {
                int[] iArr3 = this.fCurrentChildSequence;
                int i4 = this.fCurrentChildDepth;
                iArr3[i4] = this.fCurrentChildPosition;
                this.fCurrentChildDepth = i4 + 1;
                this.fCurrentChildPosition = 1;
                int i5 = this.fCurrentChildDepth;
                int i6 = this.fFoundDepth;
                if (i5 <= i6 || i6 == 0) {
                    if (checkMatch()) {
                        this.fIsElementFound = true;
                        this.fFoundDepth = this.fCurrentChildDepth;
                    } else {
                        this.fIsElementFound = false;
                        this.fFoundDepth = 0;
                    }
                }
            } else if (i == 1) {
                int i7 = this.fCurrentChildDepth;
                int i8 = this.fFoundDepth;
                if (i7 == i8) {
                    this.fIsElementFound = true;
                } else if ((i7 < i8 && i8 != 0) || (this.fCurrentChildDepth > (i2 = this.fFoundDepth) && i2 == 0)) {
                    this.fIsElementFound = false;
                }
                int[] iArr4 = this.fCurrentChildSequence;
                int i9 = this.fCurrentChildDepth;
                iArr4[i9] = 0;
                this.fCurrentChildDepth = i9 - 1;
                this.fCurrentChildPosition = iArr4[this.fCurrentChildDepth] + 1;
            } else if (i == 2) {
                int[] iArr5 = this.fCurrentChildSequence;
                int i10 = this.fCurrentChildDepth;
                int i11 = this.fCurrentChildPosition;
                iArr5[i10] = i11;
                this.fCurrentChildPosition = i11 + 1;
                if (checkMatch()) {
                    this.fIsElementFound = true;
                    this.fWasOnlyEmptyElementFound = true;
                } else {
                    this.fIsElementFound = false;
                }
            }
        }
        return this.fIsElementFound;
    }

    /* access modifiers changed from: protected */
    public boolean checkMatch() {
        int[] iArr;
        int i;
        if (!this.fIsShortHand) {
            if (this.fChildSequence.length <= this.fCurrentChildDepth + 1) {
                int i2 = 0;
                while (true) {
                    int[] iArr2 = this.fChildSequence;
                    if (i2 >= iArr2.length) {
                        break;
                    } else if (iArr2[i2] != this.fCurrentChildSequence[i2]) {
                        return false;
                    } else {
                        i2++;
                    }
                }
            } else {
                return false;
            }
        } else if (this.fChildSequence.length > this.fCurrentChildDepth + 1) {
            return false;
        } else {
            int i3 = 0;
            do {
                int[] iArr3 = this.fChildSequence;
                if (i3 < iArr3.length) {
                    iArr = this.fCurrentChildSequence;
                    if (iArr.length < i3 + 2) {
                        return false;
                    }
                    i = iArr3[i3];
                    i3++;
                }
            } while (i == iArr[i3]);
            return false;
        }
        return true;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xpointer.XPointerPart
    public boolean isFragmentResolved() throws XNIException {
        return this.fIsFragmentResolved;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xpointer.XPointerPart
    public boolean isChildFragmentResolved() {
        ShortHandPointer shortHandPointer;
        if (this.fIsShortHand && (shortHandPointer = this.fShortHandPointer) != null && this.fChildSequence.length <= 0) {
            return shortHandPointer.isChildFragmentResolved();
        }
        boolean z = this.fWasOnlyEmptyElementFound;
        if (z) {
            if (!z) {
                return true;
            }
        } else if (this.fIsFragmentResolved && this.fCurrentChildDepth >= this.fFoundDepth) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public void reportError(String str, Object[] objArr) throws XNIException {
        throw new XNIException(this.fErrorReporter.getMessageFormatter(XPointerMessageFormatter.XPOINTER_DOMAIN).formatMessage(this.fErrorReporter.getLocale(), str, objArr));
    }

    /* access modifiers changed from: protected */
    public void initErrorReporter() {
        if (this.fErrorReporter == null) {
            this.fErrorReporter = new XMLErrorReporter();
        }
        if (this.fErrorHandler == null) {
            this.fErrorHandler = new XPointerErrorHandler();
        }
        this.fErrorReporter.putMessageFormatter(XPointerMessageFormatter.XPOINTER_DOMAIN, new XPointerMessageFormatter());
    }

    /* access modifiers changed from: protected */
    public void init() {
        this.fSchemeName = null;
        this.fSchemeData = null;
        this.fShortHandPointerName = null;
        this.fIsResolveElement = false;
        this.fIsElementFound = false;
        this.fWasOnlyEmptyElementFound = false;
        this.fFoundDepth = 0;
        this.fCurrentChildPosition = 1;
        this.fCurrentChildDepth = 0;
        this.fIsFragmentResolved = false;
        this.fShortHandPointer = null;
        initErrorReporter();
    }

    /* access modifiers changed from: private */
    public final class Tokens {
        private static final int INITIAL_TOKEN_COUNT = 256;
        private static final int XPTRTOKEN_ELEM_CHILD = 1;
        private static final int XPTRTOKEN_ELEM_NCNAME = 0;
        private int fCurrentTokenIndex;
        private SymbolTable fSymbolTable;
        private int fTokenCount;
        private Hashtable fTokenNames;
        private int[] fTokens;
        private final String[] fgTokenNames;

        private Tokens(SymbolTable symbolTable) {
            this.fgTokenNames = new String[]{"XPTRTOKEN_ELEM_NCNAME", "XPTRTOKEN_ELEM_CHILD"};
            this.fTokens = new int[256];
            this.fTokenCount = 0;
            this.fTokenNames = new Hashtable();
            this.fSymbolTable = symbolTable;
            this.fTokenNames.put(new Integer(0), "XPTRTOKEN_ELEM_NCNAME");
            this.fTokenNames.put(new Integer(1), "XPTRTOKEN_ELEM_CHILD");
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private String getTokenString(int i) {
            return (String) this.fTokenNames.get(new Integer(i));
        }

        private Integer getToken(int i) {
            return (Integer) this.fTokenNames.get(new Integer(i));
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
                ElementSchemePointer.this.reportError("XPointerElementSchemeProcessingError", null);
            }
            int[] iArr = this.fTokens;
            int i = this.fCurrentTokenIndex;
            this.fCurrentTokenIndex = i + 1;
            return iArr[i];
        }

        private int peekToken() throws XNIException {
            if (this.fCurrentTokenIndex == this.fTokenCount) {
                ElementSchemePointer.this.reportError("XPointerElementSchemeProcessingError", null);
            }
            return this.fTokens[this.fCurrentTokenIndex];
        }

        private String nextTokenAsString() throws XNIException {
            String tokenString = getTokenString(nextToken());
            if (tokenString == null) {
                ElementSchemePointer.this.reportError("XPointerElementSchemeProcessingError", null);
            }
            return tokenString;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private int getTokenCount() {
            return this.fTokenCount;
        }
    }

    private class Scanner {
        private static final byte CHARTYPE_DIGIT = 5;
        private static final byte CHARTYPE_INVALID = 0;
        private static final byte CHARTYPE_LETTER = 6;
        private static final byte CHARTYPE_MINUS = 2;
        private static final byte CHARTYPE_NONASCII = 8;
        private static final byte CHARTYPE_OTHER = 1;
        private static final byte CHARTYPE_PERIOD = 3;
        private static final byte CHARTYPE_SLASH = 4;
        private static final byte CHARTYPE_UNDERSCORE = 7;
        private final byte[] fASCIICharMap;
        private SymbolTable fSymbolTable;

        private Scanner(SymbolTable symbolTable) {
            this.fASCIICharMap = new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 4, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 1, 1, 1, 1, 1, 1, 1, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 1, 1, 1, 1, 7, 1, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 1, 1, 1, 1, 1};
            this.fSymbolTable = symbolTable;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean scanExpr(SymbolTable symbolTable, Tokens tokens, String str, int i, int i2) throws XNIException {
            byte b;
            while (i != i2) {
                char charAt = str.charAt(i);
                if (charAt >= 128) {
                    b = 8;
                } else {
                    b = this.fASCIICharMap[charAt];
                }
                switch (b) {
                    case 1:
                    case 2:
                    case 3:
                    case 5:
                    case 6:
                    case 7:
                    case 8:
                        int scanNCName = scanNCName(str, i2, i);
                        if (scanNCName != i) {
                            if (scanNCName < i2) {
                                str.charAt(scanNCName);
                            }
                            String addSymbol = symbolTable.addSymbol(str.substring(i, scanNCName));
                            addToken(tokens, 0);
                            tokens.addToken(addSymbol);
                            i = scanNCName;
                            break;
                        } else {
                            ElementSchemePointer.this.reportError("InvalidNCNameInElementSchemeData", new Object[]{str});
                            return false;
                        }
                    case 4:
                        int i3 = i + 1;
                        if (i3 != i2) {
                            addToken(tokens, 1);
                            char charAt2 = str.charAt(i3);
                            int i4 = i3;
                            int i5 = 0;
                            while (charAt2 >= '0' && charAt2 <= '9') {
                                i5 = (i5 * 10) + (charAt2 - '0');
                                i4++;
                                if (i4 != i2) {
                                    charAt2 = str.charAt(i4);
                                }
                            }
                            if (i5 != 0) {
                                tokens.addToken(i5);
                                i = i4;
                                break;
                            } else {
                                ElementSchemePointer.this.reportError("InvalidChildSequenceCharacter", new Object[]{new Character((char) charAt2)});
                                return false;
                            }
                        } else {
                            return false;
                        }
                }
            }
            return true;
        }

        private int scanNCName(String str, int i, int i2) {
            char charAt = str.charAt(i2);
            if (charAt < 128) {
                byte b = this.fASCIICharMap[charAt];
                if (!(b == 6 || b == 7)) {
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
                        if (!(b2 == 6 || b2 == 5 || b2 == 3 || b2 == 2 || b2 == 7)) {
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

        /* access modifiers changed from: protected */
        public void addToken(Tokens tokens, int i) throws XNIException {
            tokens.addToken(i);
        }
    }
}

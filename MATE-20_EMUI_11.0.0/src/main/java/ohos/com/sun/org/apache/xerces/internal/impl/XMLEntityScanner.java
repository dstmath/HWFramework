package ohos.com.sun.org.apache.xerces.internal.impl;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Locale;
import ohos.com.sun.org.apache.xerces.internal.impl.XMLScanner;
import ohos.com.sun.org.apache.xerces.internal.impl.io.ASCIIReader;
import ohos.com.sun.org.apache.xerces.internal.impl.io.UCSReader;
import ohos.com.sun.org.apache.xerces.internal.impl.io.UTF8Reader;
import ohos.com.sun.org.apache.xerces.internal.util.EncodingMap;
import ohos.com.sun.org.apache.xerces.internal.util.SymbolTable;
import ohos.com.sun.org.apache.xerces.internal.util.XMLChar;
import ohos.com.sun.org.apache.xerces.internal.util.XMLStringBuffer;
import ohos.com.sun.org.apache.xerces.internal.utils.XMLLimitAnalyzer;
import ohos.com.sun.org.apache.xerces.internal.utils.XMLSecurityManager;
import ohos.com.sun.org.apache.xerces.internal.xni.QName;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLLocator;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLString;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponentManager;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLConfigurationException;
import ohos.com.sun.xml.internal.stream.Entity;
import ohos.com.sun.xml.internal.stream.XMLBufferListener;

public class XMLEntityScanner implements XMLLocator {
    protected static final String ALLOW_JAVA_ENCODINGS = "http://apache.org/xml/features/allow-java-encodings";
    private static final boolean DEBUG_BUFFER = false;
    private static final boolean DEBUG_ENCODINGS = false;
    private static final boolean DEBUG_SKIP_STRING = false;
    private static final EOFException END_OF_DOCUMENT_ENTITY = new EOFException() {
        /* class ohos.com.sun.org.apache.xerces.internal.impl.XMLEntityScanner.AnonymousClass1 */
        private static final long serialVersionUID = 980337771224675268L;

        @Override // java.lang.Throwable
        public Throwable fillInStackTrace() {
            return this;
        }
    };
    protected static final String ERROR_REPORTER = "http://apache.org/xml/properties/internal/error-reporter";
    protected static final String SYMBOL_TABLE = "http://apache.org/xml/properties/internal/symbol-table";
    private static final boolean[] VALID_NAMES = new boolean[127];
    boolean detectingVersion;
    protected boolean fAllowJavaEncodings;
    protected int fBufferSize;
    protected Entity.ScannedEntity fCurrentEntity;
    protected XMLEntityManager fEntityManager;
    protected XMLErrorReporter fErrorReporter;
    protected XMLLimitAnalyzer fLimitAnalyzer;
    protected PropertyManager fPropertyManager;
    protected XMLSecurityManager fSecurityManager;
    protected SymbolTable fSymbolTable;
    boolean isExternal;
    private ArrayList<XMLBufferListener> listeners;
    boolean whiteSpaceInfoNeeded;
    int whiteSpaceLen;
    int[] whiteSpaceLookup;
    protected boolean xmlVersionSetExplicitly;

    public boolean isSpace(char c) {
        return c == ' ' || c == '\n' || c == '\t' || c == '\r';
    }

    /* access modifiers changed from: package-private */
    public final void print() {
    }

    public void setBaseSystemId(String str) {
    }

    public void setColumnNumber(int i) {
    }

    public void setExpandedSystemId(String str) {
    }

    public void setLineNumber(int i) {
    }

    public void setLiteralSystemId(String str) {
    }

    public void setPublicId(String str) {
    }

    static {
        for (int i = 65; i <= 90; i++) {
            VALID_NAMES[i] = true;
        }
        for (int i2 = 97; i2 <= 122; i2++) {
            VALID_NAMES[i2] = true;
        }
        for (int i3 = 48; i3 <= 57; i3++) {
            VALID_NAMES[i3] = true;
        }
        boolean[] zArr = VALID_NAMES;
        zArr[45] = true;
        zArr[46] = true;
        zArr[58] = true;
        zArr[95] = true;
    }

    public XMLEntityScanner() {
        this.fCurrentEntity = null;
        this.fBufferSize = 8192;
        this.fSecurityManager = null;
        this.fLimitAnalyzer = null;
        this.listeners = new ArrayList<>();
        this.fSymbolTable = null;
        this.fErrorReporter = null;
        this.whiteSpaceLookup = new int[100];
        this.whiteSpaceLen = 0;
        this.whiteSpaceInfoNeeded = true;
        this.fPropertyManager = null;
        this.isExternal = false;
        this.xmlVersionSetExplicitly = false;
        this.detectingVersion = false;
    }

    public XMLEntityScanner(PropertyManager propertyManager, XMLEntityManager xMLEntityManager) {
        this.fCurrentEntity = null;
        this.fBufferSize = 8192;
        this.fSecurityManager = null;
        this.fLimitAnalyzer = null;
        this.listeners = new ArrayList<>();
        this.fSymbolTable = null;
        this.fErrorReporter = null;
        this.whiteSpaceLookup = new int[100];
        this.whiteSpaceLen = 0;
        this.whiteSpaceInfoNeeded = true;
        this.fPropertyManager = null;
        this.isExternal = false;
        this.xmlVersionSetExplicitly = false;
        this.detectingVersion = false;
        this.fEntityManager = xMLEntityManager;
        reset(propertyManager);
    }

    public final void setBufferSize(int i) {
        this.fBufferSize = i;
    }

    public void reset(PropertyManager propertyManager) {
        this.fSymbolTable = (SymbolTable) propertyManager.getProperty("http://apache.org/xml/properties/internal/symbol-table");
        this.fErrorReporter = (XMLErrorReporter) propertyManager.getProperty("http://apache.org/xml/properties/internal/error-reporter");
        resetCommon();
    }

    public void reset(XMLComponentManager xMLComponentManager) throws XMLConfigurationException {
        this.fAllowJavaEncodings = xMLComponentManager.getFeature(ALLOW_JAVA_ENCODINGS, false);
        this.fSymbolTable = (SymbolTable) xMLComponentManager.getProperty("http://apache.org/xml/properties/internal/symbol-table");
        this.fErrorReporter = (XMLErrorReporter) xMLComponentManager.getProperty("http://apache.org/xml/properties/internal/error-reporter");
        resetCommon();
    }

    public final void reset(SymbolTable symbolTable, XMLEntityManager xMLEntityManager, XMLErrorReporter xMLErrorReporter) {
        this.fCurrentEntity = null;
        this.fSymbolTable = symbolTable;
        this.fEntityManager = xMLEntityManager;
        this.fErrorReporter = xMLErrorReporter;
        this.fLimitAnalyzer = this.fEntityManager.fLimitAnalyzer;
        this.fSecurityManager = this.fEntityManager.fSecurityManager;
    }

    private void resetCommon() {
        this.fCurrentEntity = null;
        this.whiteSpaceLen = 0;
        this.whiteSpaceInfoNeeded = true;
        this.listeners.clear();
        this.fLimitAnalyzer = this.fEntityManager.fLimitAnalyzer;
        this.fSecurityManager = this.fEntityManager.fSecurityManager;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLLocator
    public final String getXMLVersion() {
        Entity.ScannedEntity scannedEntity = this.fCurrentEntity;
        if (scannedEntity != null) {
            return scannedEntity.xmlVersion;
        }
        return null;
    }

    public final void setXMLVersion(String str) {
        this.xmlVersionSetExplicitly = true;
        this.fCurrentEntity.xmlVersion = str;
    }

    public final void setCurrentEntity(Entity.ScannedEntity scannedEntity) {
        this.fCurrentEntity = scannedEntity;
        Entity.ScannedEntity scannedEntity2 = this.fCurrentEntity;
        if (scannedEntity2 != null) {
            this.isExternal = scannedEntity2.isExternal();
        }
    }

    public Entity.ScannedEntity getCurrentEntity() {
        return this.fCurrentEntity;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLLocator
    public final String getBaseSystemId() {
        Entity.ScannedEntity scannedEntity = this.fCurrentEntity;
        if (scannedEntity == null || scannedEntity.entityLocation == null) {
            return null;
        }
        return this.fCurrentEntity.entityLocation.getExpandedSystemId();
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLLocator
    public final int getLineNumber() {
        Entity.ScannedEntity scannedEntity = this.fCurrentEntity;
        if (scannedEntity != null) {
            return scannedEntity.lineNumber;
        }
        return -1;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLLocator
    public final int getColumnNumber() {
        Entity.ScannedEntity scannedEntity = this.fCurrentEntity;
        if (scannedEntity != null) {
            return scannedEntity.columnNumber;
        }
        return -1;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLLocator
    public final int getCharacterOffset() {
        Entity.ScannedEntity scannedEntity = this.fCurrentEntity;
        if (scannedEntity != null) {
            return scannedEntity.fTotalCountTillLastLoad + this.fCurrentEntity.position;
        }
        return -1;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLLocator
    public final String getExpandedSystemId() {
        Entity.ScannedEntity scannedEntity = this.fCurrentEntity;
        if (scannedEntity == null || scannedEntity.entityLocation == null) {
            return null;
        }
        return this.fCurrentEntity.entityLocation.getExpandedSystemId();
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLLocator
    public final String getLiteralSystemId() {
        Entity.ScannedEntity scannedEntity = this.fCurrentEntity;
        if (scannedEntity == null || scannedEntity.entityLocation == null) {
            return null;
        }
        return this.fCurrentEntity.entityLocation.getLiteralSystemId();
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLLocator
    public final String getPublicId() {
        Entity.ScannedEntity scannedEntity = this.fCurrentEntity;
        if (scannedEntity == null || scannedEntity.entityLocation == null) {
            return null;
        }
        return this.fCurrentEntity.entityLocation.getPublicId();
    }

    public void setVersion(String str) {
        this.fCurrentEntity.version = str;
    }

    public String getVersion() {
        Entity.ScannedEntity scannedEntity = this.fCurrentEntity;
        if (scannedEntity != null) {
            return scannedEntity.version;
        }
        return null;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLLocator
    public final String getEncoding() {
        Entity.ScannedEntity scannedEntity = this.fCurrentEntity;
        if (scannedEntity != null) {
            return scannedEntity.encoding;
        }
        return null;
    }

    public final void setEncoding(String str) throws IOException {
        if (this.fCurrentEntity.stream == null) {
            return;
        }
        if (this.fCurrentEntity.encoding == null || !this.fCurrentEntity.encoding.equals(str)) {
            if (this.fCurrentEntity.encoding != null && this.fCurrentEntity.encoding.startsWith("UTF-16")) {
                String upperCase = str.toUpperCase(Locale.ENGLISH);
                if (!upperCase.equals("UTF-16")) {
                    if (upperCase.equals("ISO-10646-UCS-4")) {
                        if (this.fCurrentEntity.encoding.equals("UTF-16BE")) {
                            Entity.ScannedEntity scannedEntity = this.fCurrentEntity;
                            scannedEntity.reader = new UCSReader(scannedEntity.stream, 8);
                            return;
                        }
                        Entity.ScannedEntity scannedEntity2 = this.fCurrentEntity;
                        scannedEntity2.reader = new UCSReader(scannedEntity2.stream, 4);
                        return;
                    } else if (upperCase.equals("ISO-10646-UCS-2")) {
                        if (this.fCurrentEntity.encoding.equals("UTF-16BE")) {
                            Entity.ScannedEntity scannedEntity3 = this.fCurrentEntity;
                            scannedEntity3.reader = new UCSReader(scannedEntity3.stream, 2);
                            return;
                        }
                        Entity.ScannedEntity scannedEntity4 = this.fCurrentEntity;
                        scannedEntity4.reader = new UCSReader(scannedEntity4.stream, 1);
                        return;
                    }
                } else {
                    return;
                }
            }
            Entity.ScannedEntity scannedEntity5 = this.fCurrentEntity;
            scannedEntity5.reader = createReader(scannedEntity5.stream, str, null);
            this.fCurrentEntity.encoding = str;
        }
    }

    public final boolean isExternal() {
        return this.fCurrentEntity.isExternal();
    }

    public int getChar(int i) throws IOException {
        if (arrangeCapacity(i + 1, false)) {
            return this.fCurrentEntity.ch[this.fCurrentEntity.position + i];
        }
        return -1;
    }

    public int peekChar() throws IOException {
        if (this.fCurrentEntity.position == this.fCurrentEntity.count) {
            load(0, true, true);
        }
        char c = this.fCurrentEntity.ch[this.fCurrentEntity.position];
        if (!this.isExternal || c != '\r') {
            return c;
        }
        return 10;
    }

    /* access modifiers changed from: protected */
    public int scanChar(XMLScanner.NameType nameType) throws IOException {
        if (this.fCurrentEntity.position == this.fCurrentEntity.count) {
            load(0, true, true);
        }
        int i = this.fCurrentEntity.position;
        char[] cArr = this.fCurrentEntity.ch;
        Entity.ScannedEntity scannedEntity = this.fCurrentEntity;
        int i2 = scannedEntity.position;
        scannedEntity.position = i2 + 1;
        char c = cArr[i2];
        if (c == '\n' || (c == '\r' && this.isExternal)) {
            this.fCurrentEntity.lineNumber++;
            Entity.ScannedEntity scannedEntity2 = this.fCurrentEntity;
            scannedEntity2.columnNumber = 1;
            if (scannedEntity2.position == this.fCurrentEntity.count) {
                invokeListeners(1);
                this.fCurrentEntity.ch[0] = (char) c;
                load(1, false, false);
                i = 0;
            }
            if (c == '\r' && this.isExternal) {
                char[] cArr2 = this.fCurrentEntity.ch;
                Entity.ScannedEntity scannedEntity3 = this.fCurrentEntity;
                int i3 = scannedEntity3.position;
                scannedEntity3.position = i3 + 1;
                if (cArr2[i3] != '\n') {
                    this.fCurrentEntity.position--;
                }
                c = '\n';
            }
        }
        this.fCurrentEntity.columnNumber++;
        if (!this.detectingVersion) {
            Entity.ScannedEntity scannedEntity4 = this.fCurrentEntity;
            checkEntityLimit(nameType, scannedEntity4, i, scannedEntity4.position - i);
        }
        return c;
    }

    /* access modifiers changed from: protected */
    public String scanNmtoken() throws IOException {
        boolean z;
        if (this.fCurrentEntity.position == this.fCurrentEntity.count) {
            load(0, true, true);
        }
        int i = this.fCurrentEntity.position;
        while (true) {
            char c = this.fCurrentEntity.ch[this.fCurrentEntity.position];
            if (c < 127) {
                z = VALID_NAMES[c];
            } else {
                z = XMLChar.isName(c);
            }
            if (!z) {
                break;
            }
            Entity.ScannedEntity scannedEntity = this.fCurrentEntity;
            int i2 = scannedEntity.position + 1;
            scannedEntity.position = i2;
            if (i2 == this.fCurrentEntity.count) {
                int i3 = this.fCurrentEntity.position - i;
                invokeListeners(i3);
                if (i3 == this.fCurrentEntity.fBufferSize) {
                    char[] cArr = new char[(this.fCurrentEntity.fBufferSize * 2)];
                    System.arraycopy(this.fCurrentEntity.ch, i, cArr, 0, i3);
                    Entity.ScannedEntity scannedEntity2 = this.fCurrentEntity;
                    scannedEntity2.ch = cArr;
                    scannedEntity2.fBufferSize *= 2;
                } else {
                    System.arraycopy(this.fCurrentEntity.ch, i, this.fCurrentEntity.ch, 0, i3);
                }
                if (load(i3, false, false)) {
                    i = 0;
                    break;
                }
                i = 0;
            }
        }
        int i4 = this.fCurrentEntity.position - i;
        this.fCurrentEntity.columnNumber += i4;
        if (i4 > 0) {
            return this.fSymbolTable.addSymbol(this.fCurrentEntity.ch, i, i4);
        }
        return null;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x007a, code lost:
        if (load(r1, false, false) == false) goto L_0x0053;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x007c, code lost:
        r0 = 0;
     */
    public String scanName(XMLScanner.NameType nameType) throws IOException {
        boolean z;
        if (this.fCurrentEntity.position == this.fCurrentEntity.count) {
            load(0, true, true);
        }
        int i = this.fCurrentEntity.position;
        if (XMLChar.isNameStart(this.fCurrentEntity.ch[i])) {
            Entity.ScannedEntity scannedEntity = this.fCurrentEntity;
            int i2 = scannedEntity.position + 1;
            scannedEntity.position = i2;
            if (i2 == this.fCurrentEntity.count) {
                invokeListeners(1);
                this.fCurrentEntity.ch[0] = this.fCurrentEntity.ch[i];
                if (load(1, false, false)) {
                    this.fCurrentEntity.columnNumber++;
                    return this.fSymbolTable.addSymbol(this.fCurrentEntity.ch, 0, 1);
                }
                i = 0;
            }
            while (true) {
                char c = this.fCurrentEntity.ch[this.fCurrentEntity.position];
                if (c < 127) {
                    z = VALID_NAMES[c];
                } else {
                    z = XMLChar.isName(c);
                }
                if (!z) {
                    break;
                }
                int checkBeforeLoad = checkBeforeLoad(this.fCurrentEntity, i, i);
                if (checkBeforeLoad > 0) {
                    break;
                }
            }
        }
        int i3 = this.fCurrentEntity.position - i;
        this.fCurrentEntity.columnNumber += i3;
        if (i3 <= 0) {
            return null;
        }
        checkLimit(XMLSecurityManager.Limit.MAX_NAME_LIMIT, this.fCurrentEntity, i, i3);
        checkEntityLimit(nameType, this.fCurrentEntity, i, i3);
        return this.fSymbolTable.addSymbol(this.fCurrentEntity.ch, i, i3);
    }

    /* access modifiers changed from: protected */
    public boolean scanQName(QName qName, XMLScanner.NameType nameType) throws IOException {
        boolean z;
        String str;
        String str2;
        if (this.fCurrentEntity.position == this.fCurrentEntity.count) {
            load(0, true, true);
        }
        int i = this.fCurrentEntity.position;
        if (XMLChar.isNameStart(this.fCurrentEntity.ch[i])) {
            Entity.ScannedEntity scannedEntity = this.fCurrentEntity;
            int i2 = scannedEntity.position + 1;
            scannedEntity.position = i2;
            if (i2 == this.fCurrentEntity.count) {
                invokeListeners(1);
                this.fCurrentEntity.ch[0] = this.fCurrentEntity.ch[i];
                if (load(1, false, false)) {
                    this.fCurrentEntity.columnNumber++;
                    String addSymbol = this.fSymbolTable.addSymbol(this.fCurrentEntity.ch, 0, 1);
                    qName.setValues(null, addSymbol, addSymbol, null);
                    checkEntityLimit(nameType, this.fCurrentEntity, 0, 1);
                    return true;
                }
                i = 0;
            }
            int i3 = i;
            int i4 = -1;
            while (true) {
                char c = this.fCurrentEntity.ch[this.fCurrentEntity.position];
                if (c < 127) {
                    z = VALID_NAMES[c];
                } else {
                    z = XMLChar.isName(c);
                }
                if (!z) {
                    break;
                }
                if (c == ':') {
                    if (i4 != -1) {
                        break;
                    }
                    i4 = this.fCurrentEntity.position;
                    checkLimit(XMLSecurityManager.Limit.MAX_NAME_LIMIT, this.fCurrentEntity, i3, i4 - i3);
                }
                int checkBeforeLoad = checkBeforeLoad(this.fCurrentEntity, i3, i4);
                if (checkBeforeLoad > 0) {
                    if (i4 != -1) {
                        i4 -= i3;
                    }
                    if (load(checkBeforeLoad, false, false)) {
                        i3 = 0;
                        break;
                    }
                    i3 = 0;
                }
            }
            int i5 = this.fCurrentEntity.position - i3;
            this.fCurrentEntity.columnNumber += i5;
            if (i5 > 0) {
                String addSymbol2 = this.fSymbolTable.addSymbol(this.fCurrentEntity.ch, i3, i5);
                if (i4 != -1) {
                    int i6 = i4 - i3;
                    checkLimit(XMLSecurityManager.Limit.MAX_NAME_LIMIT, this.fCurrentEntity, i3, i6);
                    str = this.fSymbolTable.addSymbol(this.fCurrentEntity.ch, i3, i6);
                    int i7 = (i5 - i6) - 1;
                    int i8 = i4 + 1;
                    checkLimit(XMLSecurityManager.Limit.MAX_NAME_LIMIT, this.fCurrentEntity, i8, i7);
                    str2 = this.fSymbolTable.addSymbol(this.fCurrentEntity.ch, i8, i7);
                } else {
                    checkLimit(XMLSecurityManager.Limit.MAX_NAME_LIMIT, this.fCurrentEntity, i3, i5);
                    str2 = addSymbol2;
                    str = null;
                }
                qName.setValues(str, str2, addSymbol2, null);
                checkEntityLimit(nameType, this.fCurrentEntity, i3, i5);
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public int checkBeforeLoad(Entity.ScannedEntity scannedEntity, int i, int i2) throws IOException {
        int i3;
        int i4;
        int i5 = scannedEntity.position + 1;
        scannedEntity.position = i5;
        if (i5 != scannedEntity.count) {
            return 0;
        }
        int i6 = scannedEntity.position - i;
        if (i2 != -1) {
            i3 = i2 - i;
            i4 = i6 - i3;
        } else {
            i3 = i;
            i4 = i6;
        }
        checkLimit(XMLSecurityManager.Limit.MAX_NAME_LIMIT, scannedEntity, i3, i4);
        invokeListeners(i6);
        if (i6 == scannedEntity.ch.length) {
            char[] cArr = new char[(scannedEntity.fBufferSize * 2)];
            System.arraycopy(scannedEntity.ch, i, cArr, 0, i6);
            scannedEntity.ch = cArr;
            scannedEntity.fBufferSize *= 2;
            return i6;
        }
        System.arraycopy(scannedEntity.ch, i, scannedEntity.ch, 0, i6);
        return i6;
    }

    /* access modifiers changed from: protected */
    public void checkEntityLimit(XMLScanner.NameType nameType, Entity.ScannedEntity scannedEntity, int i, int i2) {
        if (scannedEntity != null && scannedEntity.isGE) {
            if (nameType != XMLScanner.NameType.REFERENCE) {
                checkLimit(XMLSecurityManager.Limit.GENERAL_ENTITY_SIZE_LIMIT, scannedEntity, i, i2);
            }
            if (nameType == XMLScanner.NameType.ELEMENTSTART || nameType == XMLScanner.NameType.ATTRIBUTENAME) {
                checkNodeCount(scannedEntity);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void checkNodeCount(Entity.ScannedEntity scannedEntity) {
        if (scannedEntity != null && scannedEntity.isGE) {
            checkLimit(XMLSecurityManager.Limit.ENTITY_REPLACEMENT_LIMIT, scannedEntity, 0, 1);
        }
    }

    /* access modifiers changed from: protected */
    public void checkLimit(XMLSecurityManager.Limit limit, Entity.ScannedEntity scannedEntity, int i, int i2) {
        this.fLimitAnalyzer.addValue(limit, scannedEntity.name, i2);
        if (this.fSecurityManager.isOverLimit(limit, this.fLimitAnalyzer)) {
            this.fSecurityManager.debugPrint(this.fLimitAnalyzer);
            this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", limit.key(), limit == XMLSecurityManager.Limit.ENTITY_REPLACEMENT_LIMIT ? new Object[]{Integer.valueOf(this.fLimitAnalyzer.getValue(limit)), Integer.valueOf(this.fSecurityManager.getLimit(limit)), this.fSecurityManager.getStateLiteral(limit)} : new Object[]{scannedEntity.name, Integer.valueOf(this.fLimitAnalyzer.getValue(limit)), Integer.valueOf(this.fSecurityManager.getLimit(limit)), this.fSecurityManager.getStateLiteral(limit)}, 2);
        }
        if (this.fSecurityManager.isOverLimit(XMLSecurityManager.Limit.TOTAL_ENTITY_SIZE_LIMIT, this.fLimitAnalyzer)) {
            this.fSecurityManager.debugPrint(this.fLimitAnalyzer);
            this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "TotalEntitySizeLimit", new Object[]{Integer.valueOf(this.fLimitAnalyzer.getTotalValue(XMLSecurityManager.Limit.TOTAL_ENTITY_SIZE_LIMIT)), Integer.valueOf(this.fSecurityManager.getLimit(XMLSecurityManager.Limit.TOTAL_ENTITY_SIZE_LIMIT)), this.fSecurityManager.getStateLiteral(XMLSecurityManager.Limit.TOTAL_ENTITY_SIZE_LIMIT)}, 2);
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x00cd, code lost:
        r1 = true;
        r2 = r0;
        r0 = 0;
     */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x00e6 A[EDGE_INSN: B:63:0x00e6->B:36:0x00e6 ?: BREAK  , SYNTHETIC] */
    public int scanContent(XMLString xMLString) throws IOException {
        boolean z;
        int i = 0;
        if (this.fCurrentEntity.position == this.fCurrentEntity.count) {
            load(0, true, true);
        } else if (this.fCurrentEntity.position == this.fCurrentEntity.count - 1) {
            invokeListeners(1);
            this.fCurrentEntity.ch[0] = this.fCurrentEntity.ch[this.fCurrentEntity.count - 1];
            load(1, false, false);
            this.fCurrentEntity.position = 0;
        }
        int i2 = this.fCurrentEntity.position;
        char c = this.fCurrentEntity.ch[i2];
        if (c == '\n' || (c == '\r' && this.isExternal)) {
            int i3 = i2;
            int i4 = 0;
            while (true) {
                char[] cArr = this.fCurrentEntity.ch;
                Entity.ScannedEntity scannedEntity = this.fCurrentEntity;
                int i5 = scannedEntity.position;
                scannedEntity.position = i5 + 1;
                char c2 = cArr[i5];
                if (c2 != '\r' || !this.isExternal) {
                    if (c2 != '\n') {
                        this.fCurrentEntity.position--;
                        break;
                    }
                    i4++;
                    this.fCurrentEntity.lineNumber++;
                    Entity.ScannedEntity scannedEntity2 = this.fCurrentEntity;
                    scannedEntity2.columnNumber = 1;
                    if (scannedEntity2.position == this.fCurrentEntity.count) {
                        checkEntityLimit(null, this.fCurrentEntity, i3, i4);
                        this.fCurrentEntity.position = i4;
                        if (load(i4, false, true)) {
                            break;
                        }
                        i3 = 0;
                    }
                    if (this.fCurrentEntity.position >= this.fCurrentEntity.count - 1) {
                        break;
                    }
                } else {
                    i4++;
                    this.fCurrentEntity.lineNumber++;
                    Entity.ScannedEntity scannedEntity3 = this.fCurrentEntity;
                    scannedEntity3.columnNumber = 1;
                    if (scannedEntity3.position == this.fCurrentEntity.count) {
                        checkEntityLimit(null, this.fCurrentEntity, i3, i4);
                        this.fCurrentEntity.position = i4;
                        if (load(i4, false, true)) {
                            break;
                        }
                        i3 = 0;
                    }
                    if (this.fCurrentEntity.ch[this.fCurrentEntity.position] == '\n') {
                        this.fCurrentEntity.position++;
                        i3++;
                    } else {
                        i4++;
                    }
                    if (this.fCurrentEntity.position >= this.fCurrentEntity.count - 1) {
                    }
                }
            }
            i = i4;
            i2 = i3;
            z = false;
            for (int i6 = i2; i6 < this.fCurrentEntity.position; i6++) {
                this.fCurrentEntity.ch[i6] = '\n';
            }
            int i7 = this.fCurrentEntity.position - i2;
            if (this.fCurrentEntity.position == this.fCurrentEntity.count - 1) {
                checkEntityLimit(null, this.fCurrentEntity, i2, i7);
                xMLString.setValues(this.fCurrentEntity.ch, i2, i7);
                return -1;
            }
        } else {
            z = false;
        }
        while (true) {
            if (this.fCurrentEntity.position >= this.fCurrentEntity.count) {
                break;
            }
            char[] cArr2 = this.fCurrentEntity.ch;
            Entity.ScannedEntity scannedEntity4 = this.fCurrentEntity;
            int i8 = scannedEntity4.position;
            scannedEntity4.position = i8 + 1;
            if (!XMLChar.isContent(cArr2[i8])) {
                this.fCurrentEntity.position--;
                break;
            }
        }
        int i9 = this.fCurrentEntity.position - i2;
        this.fCurrentEntity.columnNumber += i9 - i;
        if (!z) {
            checkEntityLimit(null, this.fCurrentEntity, i2, i9);
        }
        xMLString.setValues(this.fCurrentEntity.ch, i2, i9);
        if (this.fCurrentEntity.position == this.fCurrentEntity.count) {
            return -1;
        }
        char c3 = this.fCurrentEntity.ch[this.fCurrentEntity.position];
        if (c3 != '\r' || !this.isExternal) {
            return c3;
        }
        return 10;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x00c4, code lost:
        r2 = r0;
        r0 = 0;
     */
    /* JADX WARNING: Removed duplicated region for block: B:78:0x00dc A[EDGE_INSN: B:78:0x00dc->B:38:0x00dc ?: BREAK  , SYNTHETIC] */
    public int scanLiteral(int i, XMLString xMLString, boolean z) throws IOException {
        char c;
        int i2 = 0;
        if (this.fCurrentEntity.position == this.fCurrentEntity.count) {
            load(0, true, true);
        } else if (this.fCurrentEntity.position == this.fCurrentEntity.count - 1) {
            invokeListeners(1);
            this.fCurrentEntity.ch[0] = this.fCurrentEntity.ch[this.fCurrentEntity.count - 1];
            load(1, false, false);
            this.fCurrentEntity.position = 0;
        }
        int i3 = this.fCurrentEntity.position;
        char c2 = this.fCurrentEntity.ch[i3];
        if (this.whiteSpaceInfoNeeded) {
            this.whiteSpaceLen = 0;
        }
        if (c2 == '\n' || (c2 == '\r' && this.isExternal)) {
            int i4 = i3;
            int i5 = 0;
            while (true) {
                char[] cArr = this.fCurrentEntity.ch;
                Entity.ScannedEntity scannedEntity = this.fCurrentEntity;
                int i6 = scannedEntity.position;
                scannedEntity.position = i6 + 1;
                char c3 = cArr[i6];
                if (c3 != '\r' || !this.isExternal) {
                    if (c3 != '\n') {
                        this.fCurrentEntity.position--;
                        break;
                    }
                    i5++;
                    this.fCurrentEntity.lineNumber++;
                    Entity.ScannedEntity scannedEntity2 = this.fCurrentEntity;
                    scannedEntity2.columnNumber = 1;
                    if (scannedEntity2.position == this.fCurrentEntity.count) {
                        this.fCurrentEntity.position = i5;
                        if (load(i5, false, true)) {
                            break;
                        }
                        i4 = 0;
                    }
                    if (this.fCurrentEntity.position >= this.fCurrentEntity.count - 1) {
                        break;
                    }
                } else {
                    i5++;
                    this.fCurrentEntity.lineNumber++;
                    Entity.ScannedEntity scannedEntity3 = this.fCurrentEntity;
                    scannedEntity3.columnNumber = 1;
                    if (scannedEntity3.position == this.fCurrentEntity.count) {
                        this.fCurrentEntity.position = i5;
                        if (load(i5, false, true)) {
                            break;
                        }
                        i4 = 0;
                    }
                    if (this.fCurrentEntity.ch[this.fCurrentEntity.position] == '\n') {
                        this.fCurrentEntity.position++;
                        i4++;
                    } else {
                        i5++;
                    }
                    if (this.fCurrentEntity.position >= this.fCurrentEntity.count - 1) {
                    }
                }
            }
            i2 = i5;
            i3 = i4;
            for (int i7 = i3; i7 < this.fCurrentEntity.position; i7++) {
                this.fCurrentEntity.ch[i7] = '\n';
                storeWhiteSpace(i7);
            }
            int i8 = this.fCurrentEntity.position - i3;
            if (this.fCurrentEntity.position == this.fCurrentEntity.count - 1) {
                xMLString.setValues(this.fCurrentEntity.ch, i3, i8);
                return -1;
            }
        }
        while (this.fCurrentEntity.position < this.fCurrentEntity.count && (((c = this.fCurrentEntity.ch[this.fCurrentEntity.position]) != i || (this.fCurrentEntity.literal && !this.isExternal)) && c != '%' && XMLChar.isContent(c))) {
            if (this.whiteSpaceInfoNeeded && c == '\t') {
                storeWhiteSpace(this.fCurrentEntity.position);
            }
            this.fCurrentEntity.position++;
        }
        int i9 = this.fCurrentEntity.position - i3;
        this.fCurrentEntity.columnNumber += i9 - i2;
        checkEntityLimit(null, this.fCurrentEntity, i3, i9);
        if (z) {
            checkLimit(XMLSecurityManager.Limit.MAX_NAME_LIMIT, this.fCurrentEntity, i3, i9);
        }
        xMLString.setValues(this.fCurrentEntity.ch, i3, i9);
        if (this.fCurrentEntity.position == this.fCurrentEntity.count) {
            return -1;
        }
        char c4 = this.fCurrentEntity.ch[this.fCurrentEntity.position];
        if (c4 != i || !this.fCurrentEntity.literal) {
            return c4;
        }
        return -1;
    }

    private void storeWhiteSpace(int i) {
        int i2 = this.whiteSpaceLen;
        int[] iArr = this.whiteSpaceLookup;
        if (i2 >= iArr.length) {
            int[] iArr2 = new int[(iArr.length + 100)];
            System.arraycopy(iArr, 0, iArr2, 0, iArr.length);
            this.whiteSpaceLookup = iArr2;
        }
        int[] iArr3 = this.whiteSpaceLookup;
        int i3 = this.whiteSpaceLen;
        this.whiteSpaceLen = i3 + 1;
        iArr3[i3] = i;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r17v0, resolved type: ohos.com.sun.org.apache.xerces.internal.impl.XMLEntityScanner */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r4v0 */
    /* JADX WARN: Type inference failed for: r4v1, types: [boolean, int] */
    /* JADX WARN: Type inference failed for: r4v6 */
    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x014f, code lost:
        r8 = r7;
        r7 = r8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:66:0x01de, code lost:
        if (r17.fCurrentEntity.position != (r12 + r3)) goto L_0x0217;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:0x01e0, code lost:
        r6 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:77:0x021c, code lost:
        r17.fCurrentEntity.position--;
     */
    /* JADX WARNING: Removed duplicated region for block: B:92:0x014f A[EDGE_INSN: B:92:0x014f->B:43:0x014f ?: BREAK  , SYNTHETIC] */
    /* JADX WARNING: Unknown variable types count: 1 */
    public boolean scanData(String str, XMLStringBuffer xMLStringBuffer) throws IOException {
        int i;
        int length = str.length();
        ?? r4 = 0;
        char charAt = str.charAt(0);
        boolean z = false;
        while (true) {
            int i2 = 1;
            if (this.fCurrentEntity.position == this.fCurrentEntity.count) {
                load(r4, true, r4);
            }
            boolean z2 = r4 == true ? 1 : 0;
            Object[] objArr = r4 == true ? 1 : 0;
            Object[] objArr2 = r4 == true ? 1 : 0;
            boolean z3 = z2;
            while (this.fCurrentEntity.position > this.fCurrentEntity.count - length && !z3) {
                System.arraycopy(this.fCurrentEntity.ch, this.fCurrentEntity.position, this.fCurrentEntity.ch, r4, this.fCurrentEntity.count - this.fCurrentEntity.position);
                z3 = load(this.fCurrentEntity.count - this.fCurrentEntity.position, r4, r4);
                Entity.ScannedEntity scannedEntity = this.fCurrentEntity;
                scannedEntity.position = r4;
                scannedEntity.startPosition = r4;
            }
            if (this.fCurrentEntity.position > this.fCurrentEntity.count - length) {
                int i3 = this.fCurrentEntity.count - this.fCurrentEntity.position;
                XMLScanner.NameType nameType = XMLScanner.NameType.COMMENT;
                Entity.ScannedEntity scannedEntity2 = this.fCurrentEntity;
                checkEntityLimit(nameType, scannedEntity2, scannedEntity2.position, i3);
                xMLStringBuffer.append(this.fCurrentEntity.ch, this.fCurrentEntity.position, i3);
                this.fCurrentEntity.columnNumber += this.fCurrentEntity.count;
                this.fCurrentEntity.baseCharOffset += this.fCurrentEntity.position - this.fCurrentEntity.startPosition;
                Entity.ScannedEntity scannedEntity3 = this.fCurrentEntity;
                scannedEntity3.position = scannedEntity3.count;
                Entity.ScannedEntity scannedEntity4 = this.fCurrentEntity;
                scannedEntity4.startPosition = scannedEntity4.count;
                int i4 = r4 == true ? 1 : 0;
                int i5 = r4 == true ? 1 : 0;
                int i6 = r4 == true ? 1 : 0;
                load(i4, true, r4);
                return r4;
            }
            int i7 = this.fCurrentEntity.position;
            char c = this.fCurrentEntity.ch[i7];
            if (c == '\n' || (c == '\r' && this.isExternal)) {
                int i8 = i7;
                int i9 = r4;
                while (true) {
                    char[] cArr = this.fCurrentEntity.ch;
                    Entity.ScannedEntity scannedEntity5 = this.fCurrentEntity;
                    int i10 = scannedEntity5.position;
                    scannedEntity5.position = i10 + 1;
                    char c2 = cArr[i10];
                    if (c2 != '\r' || !this.isExternal) {
                        if (c2 != '\n') {
                            this.fCurrentEntity.position--;
                            break;
                        }
                        i9++;
                        this.fCurrentEntity.lineNumber++;
                        Entity.ScannedEntity scannedEntity6 = this.fCurrentEntity;
                        scannedEntity6.columnNumber = 1;
                        if (scannedEntity6.position == this.fCurrentEntity.count) {
                            Entity.ScannedEntity scannedEntity7 = this.fCurrentEntity;
                            scannedEntity7.position = i9;
                            scannedEntity7.count = i9;
                            if (load(i9, r4, true)) {
                                break;
                            }
                            i8 = r4;
                        }
                        if (this.fCurrentEntity.position >= this.fCurrentEntity.count - 1) {
                            break;
                        }
                    } else {
                        i9++;
                        this.fCurrentEntity.lineNumber++;
                        Entity.ScannedEntity scannedEntity8 = this.fCurrentEntity;
                        scannedEntity8.columnNumber = 1;
                        if (scannedEntity8.position == this.fCurrentEntity.count) {
                            this.fCurrentEntity.position = i9;
                            if (load(i9, r4, true)) {
                                break;
                            }
                            i8 = r4;
                        }
                        if (this.fCurrentEntity.ch[this.fCurrentEntity.position] == '\n') {
                            this.fCurrentEntity.position++;
                            i8++;
                        } else {
                            i9++;
                        }
                        if (this.fCurrentEntity.position >= this.fCurrentEntity.count - 1) {
                        }
                    }
                }
                i = i9;
                i7 = r4;
                for (int i11 = i7; i11 < this.fCurrentEntity.position; i11++) {
                    this.fCurrentEntity.ch[i11] = '\n';
                }
                int i12 = this.fCurrentEntity.position - i7;
                if (this.fCurrentEntity.position == this.fCurrentEntity.count - 1) {
                    checkEntityLimit(XMLScanner.NameType.COMMENT, this.fCurrentEntity, i7, i12);
                    xMLStringBuffer.append(this.fCurrentEntity.ch, i7, i12);
                    return true;
                }
            } else {
                i = r4;
            }
            while (true) {
                if (this.fCurrentEntity.position >= this.fCurrentEntity.count) {
                    break;
                }
                char[] cArr2 = this.fCurrentEntity.ch;
                Entity.ScannedEntity scannedEntity9 = this.fCurrentEntity;
                int i13 = scannedEntity9.position;
                scannedEntity9.position = i13 + 1;
                char c3 = cArr2[i13];
                if (c3 == charAt) {
                    int i14 = this.fCurrentEntity.position - i2;
                    int i15 = i2;
                    while (true) {
                        if (i15 >= length) {
                            break;
                        } else if (this.fCurrentEntity.position == this.fCurrentEntity.count) {
                            this.fCurrentEntity.position -= i15;
                            break;
                        } else {
                            char[] cArr3 = this.fCurrentEntity.ch;
                            Entity.ScannedEntity scannedEntity10 = this.fCurrentEntity;
                            int i16 = scannedEntity10.position;
                            scannedEntity10.position = i16 + 1;
                            if (str.charAt(i15) != cArr3[i16]) {
                                this.fCurrentEntity.position -= i15;
                                break;
                            }
                            i15++;
                        }
                    }
                } else if (c3 == '\n' || (this.isExternal && c3 == '\r')) {
                    break;
                } else if (XMLChar.isInvalid(c3)) {
                    this.fCurrentEntity.position--;
                    int i17 = this.fCurrentEntity.position - i7;
                    this.fCurrentEntity.columnNumber += i17 - i;
                    checkEntityLimit(XMLScanner.NameType.COMMENT, this.fCurrentEntity, i7, i17);
                    xMLStringBuffer.append(this.fCurrentEntity.ch, i7, i17);
                    return true;
                }
                i2 = 1;
            }
            int i18 = this.fCurrentEntity.position - i7;
            this.fCurrentEntity.columnNumber += i18 - i;
            checkEntityLimit(XMLScanner.NameType.COMMENT, this.fCurrentEntity, i7, i18);
            if (z) {
                i18 -= length;
            }
            xMLStringBuffer.append(this.fCurrentEntity.ch, i7, i18);
            if (z) {
                return true ^ z;
            }
            r4 = 0;
        }
    }

    /* access modifiers changed from: protected */
    public boolean skipChar(int i, XMLScanner.NameType nameType) throws IOException {
        if (this.fCurrentEntity.position == this.fCurrentEntity.count) {
            load(0, true, true);
        }
        int i2 = this.fCurrentEntity.position;
        char c = this.fCurrentEntity.ch[this.fCurrentEntity.position];
        if (c == i) {
            this.fCurrentEntity.position++;
            if (i == 10) {
                this.fCurrentEntity.lineNumber++;
                this.fCurrentEntity.columnNumber = 1;
            } else {
                this.fCurrentEntity.columnNumber++;
            }
            Entity.ScannedEntity scannedEntity = this.fCurrentEntity;
            checkEntityLimit(nameType, scannedEntity, i2, scannedEntity.position - i2);
            return true;
        } else if (i != 10 || c != '\r' || !this.isExternal) {
            return false;
        } else {
            if (this.fCurrentEntity.position == this.fCurrentEntity.count) {
                invokeListeners(1);
                this.fCurrentEntity.ch[0] = (char) c;
                load(1, false, false);
            }
            this.fCurrentEntity.position++;
            if (this.fCurrentEntity.ch[this.fCurrentEntity.position] == '\n') {
                this.fCurrentEntity.position++;
            }
            this.fCurrentEntity.lineNumber++;
            Entity.ScannedEntity scannedEntity2 = this.fCurrentEntity;
            scannedEntity2.columnNumber = 1;
            checkEntityLimit(nameType, scannedEntity2, i2, scannedEntity2.position - i2);
            return true;
        }
    }

    /* access modifiers changed from: protected */
    public boolean skipSpaces() throws IOException {
        boolean z;
        if (this.fCurrentEntity.position == this.fCurrentEntity.count) {
            load(0, true, true);
        }
        Entity.ScannedEntity scannedEntity = this.fCurrentEntity;
        if (scannedEntity == null) {
            return false;
        }
        char c = scannedEntity.ch[this.fCurrentEntity.position];
        int i = this.fCurrentEntity.position - 1;
        if (!XMLChar.isSpace(c)) {
            return false;
        }
        do {
            if (c == '\n' || (this.isExternal && c == '\r')) {
                this.fCurrentEntity.lineNumber++;
                Entity.ScannedEntity scannedEntity2 = this.fCurrentEntity;
                scannedEntity2.columnNumber = 1;
                if (scannedEntity2.position == this.fCurrentEntity.count - 1) {
                    invokeListeners(1);
                    this.fCurrentEntity.ch[0] = (char) c;
                    z = load(1, true, false);
                    if (!z) {
                        this.fCurrentEntity.position = 0;
                    } else if (this.fCurrentEntity == null) {
                        return true;
                    }
                } else {
                    z = false;
                }
                if (c == '\r' && this.isExternal) {
                    char[] cArr = this.fCurrentEntity.ch;
                    Entity.ScannedEntity scannedEntity3 = this.fCurrentEntity;
                    int i2 = scannedEntity3.position + 1;
                    scannedEntity3.position = i2;
                    if (cArr[i2] != '\n') {
                        this.fCurrentEntity.position--;
                    }
                }
            } else {
                this.fCurrentEntity.columnNumber++;
                z = false;
            }
            Entity.ScannedEntity scannedEntity4 = this.fCurrentEntity;
            checkEntityLimit(null, scannedEntity4, i, scannedEntity4.position - i);
            i = this.fCurrentEntity.position;
            if (!z) {
                this.fCurrentEntity.position++;
            }
            if (this.fCurrentEntity.position == this.fCurrentEntity.count) {
                load(0, true, true);
                if (this.fCurrentEntity == null) {
                    return true;
                }
            }
            c = this.fCurrentEntity.ch[this.fCurrentEntity.position];
        } while (XMLChar.isSpace(c));
        return true;
    }

    public boolean arrangeCapacity(int i) throws IOException {
        return arrangeCapacity(i, false);
    }

    public boolean arrangeCapacity(int i, boolean z) throws IOException {
        if (this.fCurrentEntity.count - this.fCurrentEntity.position >= i) {
            return true;
        }
        while (this.fCurrentEntity.count - this.fCurrentEntity.position < i) {
            if (this.fCurrentEntity.ch.length - this.fCurrentEntity.position < i) {
                invokeListeners(0);
                System.arraycopy(this.fCurrentEntity.ch, this.fCurrentEntity.position, this.fCurrentEntity.ch, 0, this.fCurrentEntity.count - this.fCurrentEntity.position);
                this.fCurrentEntity.count -= this.fCurrentEntity.position;
                this.fCurrentEntity.position = 0;
            }
            if (this.fCurrentEntity.count - this.fCurrentEntity.position < i) {
                int i2 = this.fCurrentEntity.position;
                invokeListeners(i2);
                boolean load = load(this.fCurrentEntity.count, z, false);
                this.fCurrentEntity.position = i2;
                if (load) {
                    break;
                }
            }
        }
        if (this.fCurrentEntity.count - this.fCurrentEntity.position >= i) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean skipString(String str) throws IOException {
        int length = str.length();
        if (arrangeCapacity(length, false)) {
            int i = this.fCurrentEntity.position;
            int i2 = (this.fCurrentEntity.position + length) - 1;
            int i3 = length - 1;
            while (true) {
                int i4 = i3 - 1;
                if (str.charAt(i3) != this.fCurrentEntity.ch[i2]) {
                    break;
                }
                int i5 = i2 - 1;
                if (i2 == i) {
                    this.fCurrentEntity.position += length;
                    this.fCurrentEntity.columnNumber += length;
                    if (!this.detectingVersion) {
                        checkEntityLimit(null, this.fCurrentEntity, i, length);
                    }
                    return true;
                }
                i2 = i5;
                i3 = i4;
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean skipString(char[] cArr) throws IOException {
        int length = cArr.length;
        if (!arrangeCapacity(length, false)) {
            return false;
        }
        int i = this.fCurrentEntity.position;
        int i2 = 0;
        while (i2 < length) {
            int i3 = i + 1;
            if (this.fCurrentEntity.ch[i] != cArr[i2]) {
                return false;
            }
            i2++;
            i = i3;
        }
        this.fCurrentEntity.position += length;
        this.fCurrentEntity.columnNumber += length;
        if (this.detectingVersion) {
            return true;
        }
        checkEntityLimit(null, this.fCurrentEntity, i, length);
        return true;
    }

    /* access modifiers changed from: package-private */
    public final boolean load(int i, boolean z, boolean z2) throws IOException {
        if (z2) {
            invokeListeners(i);
        }
        this.fCurrentEntity.fTotalCountTillLastLoad += this.fCurrentEntity.fLastCount;
        int length = this.fCurrentEntity.ch.length - i;
        if (!this.fCurrentEntity.mayReadChunks && length > 64) {
            length = 64;
        }
        int read = this.fCurrentEntity.reader.read(this.fCurrentEntity.ch, i, length);
        if (read != -1) {
            if (read != 0) {
                Entity.ScannedEntity scannedEntity = this.fCurrentEntity;
                scannedEntity.fLastCount = read;
                scannedEntity.count = read + i;
                scannedEntity.position = i;
            }
            return false;
        }
        Entity.ScannedEntity scannedEntity2 = this.fCurrentEntity;
        scannedEntity2.count = i;
        scannedEntity2.position = i;
        if (!z) {
            return true;
        }
        this.fEntityManager.endEntity();
        Entity.ScannedEntity scannedEntity3 = this.fCurrentEntity;
        if (scannedEntity3 == null) {
            throw END_OF_DOCUMENT_ENTITY;
        } else if (scannedEntity3.position != this.fCurrentEntity.count) {
            return true;
        } else {
            load(0, true, false);
            return true;
        }
    }

    /* access modifiers changed from: protected */
    public Reader createReader(InputStream inputStream, String str, Boolean bool) throws IOException {
        if (str == null) {
            str = "UTF-8";
        }
        String upperCase = str.toUpperCase(Locale.ENGLISH);
        if (upperCase.equals("UTF-8")) {
            return new UTF8Reader(inputStream, this.fCurrentEntity.fBufferSize, this.fErrorReporter.getMessageFormatter("http://www.w3.org/TR/1998/REC-xml-19980210"), this.fErrorReporter.getLocale());
        }
        if (upperCase.equals("US-ASCII")) {
            return new ASCIIReader(inputStream, this.fCurrentEntity.fBufferSize, this.fErrorReporter.getMessageFormatter("http://www.w3.org/TR/1998/REC-xml-19980210"), this.fErrorReporter.getLocale());
        }
        if (upperCase.equals("ISO-10646-UCS-4")) {
            if (bool == null) {
                this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "EncodingByteOrderUnsupported", new Object[]{str}, 2);
            } else if (bool.booleanValue()) {
                return new UCSReader(inputStream, 8);
            } else {
                return new UCSReader(inputStream, 4);
            }
        }
        if (upperCase.equals("ISO-10646-UCS-2")) {
            if (bool == null) {
                this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "EncodingByteOrderUnsupported", new Object[]{str}, 2);
            } else if (bool.booleanValue()) {
                return new UCSReader(inputStream, 2);
            } else {
                return new UCSReader(inputStream, 1);
            }
        }
        boolean isValidIANAEncoding = XMLChar.isValidIANAEncoding(str);
        boolean isValidJavaEncoding = XMLChar.isValidJavaEncoding(str);
        if (!isValidIANAEncoding || (this.fAllowJavaEncodings && !isValidJavaEncoding)) {
            this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "EncodingDeclInvalid", new Object[]{str}, 2);
            str = "ISO-8859-1";
        }
        String iANA2JavaMapping = EncodingMap.getIANA2JavaMapping(upperCase);
        if (iANA2JavaMapping == null) {
            if (!this.fAllowJavaEncodings) {
                this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "EncodingDeclInvalid", new Object[]{str}, 2);
                str = "ISO8859_1";
            }
        } else if (iANA2JavaMapping.equals("ASCII")) {
            return new ASCIIReader(inputStream, this.fBufferSize, this.fErrorReporter.getMessageFormatter("http://www.w3.org/TR/1998/REC-xml-19980210"), this.fErrorReporter.getLocale());
        } else {
            str = iANA2JavaMapping;
        }
        return new InputStreamReader(inputStream, str);
    }

    /* access modifiers changed from: protected */
    public Object[] getEncodingName(byte[] bArr, int i) {
        if (i < 2) {
            return new Object[]{"UTF-8", null};
        }
        int i2 = bArr[0] & 255;
        int i3 = bArr[1] & 255;
        if (i2 == 254 && i3 == 255) {
            return new Object[]{"UTF-16BE", new Boolean(true)};
        }
        if (i2 == 255 && i3 == 254) {
            return new Object[]{"UTF-16LE", new Boolean(false)};
        }
        if (i < 3) {
            return new Object[]{"UTF-8", null};
        }
        int i4 = bArr[2] & 255;
        if (i2 == 239 && i3 == 187 && i4 == 191) {
            return new Object[]{"UTF-8", null};
        }
        if (i < 4) {
            return new Object[]{"UTF-8", null};
        }
        int i5 = bArr[3] & 255;
        return (i2 == 0 && i3 == 0 && i4 == 0 && i5 == 60) ? new Object[]{"ISO-10646-UCS-4", new Boolean(true)} : (i2 == 60 && i3 == 0 && i4 == 0 && i5 == 0) ? new Object[]{"ISO-10646-UCS-4", new Boolean(false)} : (i2 == 0 && i3 == 0 && i4 == 60 && i5 == 0) ? new Object[]{"ISO-10646-UCS-4", null} : (i2 == 0 && i3 == 60 && i4 == 0 && i5 == 0) ? new Object[]{"ISO-10646-UCS-4", null} : (i2 == 0 && i3 == 60 && i4 == 0 && i5 == 63) ? new Object[]{"UTF-16BE", new Boolean(true)} : (i2 == 60 && i3 == 0 && i4 == 63 && i5 == 0) ? new Object[]{"UTF-16LE", new Boolean(false)} : (i2 == 76 && i3 == 111 && i4 == 167 && i5 == 148) ? new Object[]{"CP037", null} : new Object[]{"UTF-8", null};
    }

    public void registerListener(XMLBufferListener xMLBufferListener) {
        if (!this.listeners.contains(xMLBufferListener)) {
            this.listeners.add(xMLBufferListener);
        }
    }

    public void invokeListeners(int i) {
        for (int i2 = 0; i2 < this.listeners.size(); i2++) {
            this.listeners.get(i2).refresh(i);
        }
    }

    /* access modifiers changed from: protected */
    public final boolean skipDeclSpaces() throws IOException {
        boolean z;
        if (this.fCurrentEntity.position == this.fCurrentEntity.count) {
            load(0, true, false);
        }
        char c = this.fCurrentEntity.ch[this.fCurrentEntity.position];
        if (!XMLChar.isSpace(c)) {
            return false;
        }
        boolean isExternal2 = this.fCurrentEntity.isExternal();
        do {
            if (c == '\n' || (isExternal2 && c == '\r')) {
                this.fCurrentEntity.lineNumber++;
                Entity.ScannedEntity scannedEntity = this.fCurrentEntity;
                scannedEntity.columnNumber = 1;
                if (scannedEntity.position == this.fCurrentEntity.count - 1) {
                    this.fCurrentEntity.ch[0] = (char) c;
                    z = load(1, true, false);
                    if (!z) {
                        this.fCurrentEntity.position = 0;
                    }
                } else {
                    z = false;
                }
                if (c == '\r' && isExternal2) {
                    char[] cArr = this.fCurrentEntity.ch;
                    Entity.ScannedEntity scannedEntity2 = this.fCurrentEntity;
                    int i = scannedEntity2.position + 1;
                    scannedEntity2.position = i;
                    if (cArr[i] != '\n') {
                        this.fCurrentEntity.position--;
                    }
                }
            } else {
                this.fCurrentEntity.columnNumber++;
                z = false;
            }
            if (!z) {
                this.fCurrentEntity.position++;
            }
            if (this.fCurrentEntity.position == this.fCurrentEntity.count) {
                load(0, true, false);
            }
            c = this.fCurrentEntity.ch[this.fCurrentEntity.position];
        } while (XMLChar.isSpace(c));
        return true;
    }
}

package ohos.com.sun.org.apache.xerces.internal.impl;

import java.io.EOFException;
import java.io.IOException;
import ohos.com.sun.org.apache.xerces.internal.util.SymbolTable;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLString;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponentManager;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLConfigurationException;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;
import ohos.com.sun.xml.internal.stream.Entity;
import ohos.global.icu.impl.UCharacterProperty;

public class XMLVersionDetector {
    protected static final String ENTITY_MANAGER = "http://apache.org/xml/properties/internal/entity-manager";
    protected static final String ERROR_REPORTER = "http://apache.org/xml/properties/internal/error-reporter";
    protected static final String SYMBOL_TABLE = "http://apache.org/xml/properties/internal/symbol-table";
    private static final char[] XML11_VERSION = {'1', '.', '1'};
    protected static final String fVersionSymbol = "version".intern();
    protected static final String fXMLSymbol = "[xml]".intern();
    protected String fEncoding = null;
    protected XMLEntityManager fEntityManager;
    protected XMLErrorReporter fErrorReporter;
    private final char[] fExpectedVersionString = {'<', '?', 'x', 'm', 'l', ' ', 'v', 'e', 'r', 's', UCharacterProperty.LATIN_SMALL_LETTER_I_, 'o', 'n', '=', ' ', ' ', ' ', ' ', ' '};
    protected SymbolTable fSymbolTable;
    private XMLString fVersionNum = new XMLString();

    public void reset(XMLComponentManager xMLComponentManager) throws XMLConfigurationException {
        this.fSymbolTable = (SymbolTable) xMLComponentManager.getProperty("http://apache.org/xml/properties/internal/symbol-table");
        this.fErrorReporter = (XMLErrorReporter) xMLComponentManager.getProperty("http://apache.org/xml/properties/internal/error-reporter");
        this.fEntityManager = (XMLEntityManager) xMLComponentManager.getProperty(ENTITY_MANAGER);
        int i = 14;
        while (true) {
            char[] cArr = this.fExpectedVersionString;
            if (i < cArr.length) {
                cArr[i] = ' ';
                i++;
            } else {
                return;
            }
        }
    }

    public void startDocumentParsing(XMLEntityHandler xMLEntityHandler, short s) {
        if (s == 1) {
            this.fEntityManager.setScannerVersion(1);
        } else {
            this.fEntityManager.setScannerVersion(2);
        }
        this.fErrorReporter.setDocumentLocator(this.fEntityManager.getEntityScanner());
        this.fEntityManager.setEntityHandler(xMLEntityHandler);
        xMLEntityHandler.startEntity(fXMLSymbol, this.fEntityManager.getCurrentResourceIdentifier(), this.fEncoding, null);
    }

    public short determineDocVersion(XMLInputSource xMLInputSource) throws IOException {
        this.fEncoding = this.fEntityManager.setupCurrentEntity(false, fXMLSymbol, xMLInputSource, false, true);
        this.fEntityManager.setScannerVersion(1);
        XMLEntityScanner entityScanner = this.fEntityManager.getEntityScanner();
        entityScanner.detectingVersion = true;
        try {
            if (!entityScanner.skipString("<?xml")) {
                entityScanner.detectingVersion = false;
                return 1;
            } else if (!entityScanner.skipDeclSpaces()) {
                fixupCurrentEntity(this.fEntityManager, this.fExpectedVersionString, 5);
                entityScanner.detectingVersion = false;
                return 1;
            } else if (!entityScanner.skipString("version")) {
                fixupCurrentEntity(this.fEntityManager, this.fExpectedVersionString, 6);
                entityScanner.detectingVersion = false;
                return 1;
            } else {
                entityScanner.skipDeclSpaces();
                if (entityScanner.peekChar() != 61) {
                    fixupCurrentEntity(this.fEntityManager, this.fExpectedVersionString, 13);
                    entityScanner.detectingVersion = false;
                    return 1;
                }
                entityScanner.scanChar(null);
                entityScanner.skipDeclSpaces();
                this.fExpectedVersionString[14] = (char) entityScanner.scanChar(null);
                for (int i = 0; i < XML11_VERSION.length; i++) {
                    this.fExpectedVersionString[i + 15] = (char) entityScanner.scanChar(null);
                }
                this.fExpectedVersionString[18] = (char) entityScanner.scanChar(null);
                fixupCurrentEntity(this.fEntityManager, this.fExpectedVersionString, 19);
                int i2 = 0;
                while (true) {
                    if (i2 >= XML11_VERSION.length) {
                        break;
                    } else if (this.fExpectedVersionString[i2 + 15] != XML11_VERSION[i2]) {
                        break;
                    } else {
                        i2++;
                    }
                }
                entityScanner.detectingVersion = false;
                if (i2 == XML11_VERSION.length) {
                    return 2;
                }
                return 1;
            }
        } catch (EOFException unused) {
            this.fErrorReporter.reportError("http://www.w3.org/TR/1998/REC-xml-19980210", "PrematureEOF", null, 2);
            entityScanner.detectingVersion = false;
            return 1;
        }
    }

    private void fixupCurrentEntity(XMLEntityManager xMLEntityManager, char[] cArr, int i) {
        Entity.ScannedEntity currentEntity = xMLEntityManager.getCurrentEntity();
        if ((currentEntity.count - currentEntity.position) + i > currentEntity.ch.length) {
            char[] cArr2 = currentEntity.ch;
            currentEntity.ch = new char[(((currentEntity.count + i) - currentEntity.position) + 1)];
            System.arraycopy(cArr2, 0, currentEntity.ch, 0, cArr2.length);
        }
        if (currentEntity.position < i) {
            System.arraycopy(currentEntity.ch, currentEntity.position, currentEntity.ch, i, currentEntity.count - currentEntity.position);
            currentEntity.count += i - currentEntity.position;
        } else {
            for (int i2 = i; i2 < currentEntity.position; i2++) {
                currentEntity.ch[i2] = ' ';
            }
        }
        System.arraycopy(cArr, 0, currentEntity.ch, 0, i);
        currentEntity.position = 0;
        currentEntity.baseCharOffset = 0;
        currentEntity.startPosition = 0;
        currentEntity.lineNumber = 1;
        currentEntity.columnNumber = 1;
    }
}

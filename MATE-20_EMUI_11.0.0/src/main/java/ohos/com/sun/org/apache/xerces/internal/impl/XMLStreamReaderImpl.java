package ohos.com.sun.org.apache.xerces.internal.impl;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.NoSuchElementException;
import ohos.com.sun.org.apache.xerces.internal.util.NamespaceContextWrapper;
import ohos.com.sun.org.apache.xerces.internal.util.NamespaceSupport;
import ohos.com.sun.org.apache.xerces.internal.util.SymbolTable;
import ohos.com.sun.org.apache.xerces.internal.util.XMLAttributesIteratorImpl;
import ohos.com.sun.org.apache.xerces.internal.util.XMLChar;
import ohos.com.sun.org.apache.xerces.internal.xni.NamespaceContext;
import ohos.com.sun.org.apache.xerces.internal.xni.XNIException;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;
import ohos.com.sun.org.apache.xml.internal.serializer.SerializerConstants;
import ohos.com.sun.xml.internal.stream.Entity;
import ohos.com.sun.xml.internal.stream.StaxErrorReporter;
import ohos.com.sun.xml.internal.stream.XMLEntityStorage;
import ohos.com.sun.xml.internal.stream.dtd.nonvalidating.DTDGrammar;
import ohos.com.sun.xml.internal.stream.dtd.nonvalidating.XMLNotationDecl;
import ohos.com.sun.xml.internal.stream.events.EntityDeclarationImpl;
import ohos.com.sun.xml.internal.stream.events.NotationDeclarationImpl;
import ohos.javax.xml.namespace.QName;
import ohos.javax.xml.stream.Location;
import ohos.javax.xml.stream.XMLStreamException;
import ohos.javax.xml.stream.XMLStreamReader;

public class XMLStreamReaderImpl implements XMLStreamReader {
    static final boolean DEBUG = false;
    protected static final String ENTITY_MANAGER = "http://apache.org/xml/properties/internal/entity-manager";
    protected static final String ERROR_REPORTER = "http://apache.org/xml/properties/internal/error-reporter";
    protected static final String READER_IN_DEFINED_STATE = "http://java.sun.com/xml/stream/properties/reader-in-defined-state";
    protected static final String SYMBOL_TABLE = "http://apache.org/xml/properties/internal/symbol-table";
    private boolean fBindNamespaces = true;
    private String fDTDDecl = null;
    protected XMLEntityManager fEntityManager = new XMLEntityManager();
    protected XMLEntityScanner fEntityScanner = null;
    protected StaxErrorReporter fErrorReporter = new StaxErrorReporter();
    private int fEventType;
    protected XMLInputSource fInputSource = null;
    protected NamespaceContextWrapper fNamespaceContextWrapper = new NamespaceContextWrapper((NamespaceSupport) this.fScanner.getNamespaceContext());
    protected PropertyManager fPropertyManager = null;
    private boolean fReaderInDefinedState = true;
    private boolean fReuse = true;
    protected XMLDocumentScannerImpl fScanner = new XMLNSDocumentScannerImpl();
    private SymbolTable fSymbolTable = new SymbolTable();
    private String versionStr = null;

    public XMLStreamReaderImpl(InputStream inputStream, PropertyManager propertyManager) throws XMLStreamException {
        init(propertyManager);
        setInputSource(new XMLInputSource((String) null, (String) null, (String) null, inputStream, (String) null));
    }

    public XMLDocumentScannerImpl getScanner() {
        System.out.println("returning scanner");
        return this.fScanner;
    }

    public XMLStreamReaderImpl(String str, PropertyManager propertyManager) throws XMLStreamException {
        init(propertyManager);
        setInputSource(new XMLInputSource(null, str, null));
    }

    public XMLStreamReaderImpl(InputStream inputStream, String str, PropertyManager propertyManager) throws XMLStreamException {
        init(propertyManager);
        setInputSource(new XMLInputSource((String) null, (String) null, (String) null, new BufferedInputStream(inputStream), str));
    }

    public XMLStreamReaderImpl(Reader reader, PropertyManager propertyManager) throws XMLStreamException {
        init(propertyManager);
        setInputSource(new XMLInputSource((String) null, (String) null, (String) null, new BufferedReader(reader), (String) null));
    }

    public XMLStreamReaderImpl(XMLInputSource xMLInputSource, PropertyManager propertyManager) throws XMLStreamException {
        init(propertyManager);
        setInputSource(xMLInputSource);
    }

    public void setInputSource(XMLInputSource xMLInputSource) throws XMLStreamException {
        this.fReuse = false;
        try {
            this.fScanner.setInputSource(xMLInputSource);
            if (this.fReaderInDefinedState) {
                this.fEventType = this.fScanner.next();
                if (this.versionStr == null) {
                    this.versionStr = getVersion();
                }
                if (this.fEventType == 7 && this.versionStr != null && this.versionStr.equals(SerializerConstants.XMLVERSION11)) {
                    switchToXML11Scanner();
                }
            }
        } catch (IOException e) {
            throw new XMLStreamException(e);
        } catch (XNIException e2) {
            throw new XMLStreamException(e2.getMessage(), getLocation(), e2.getException());
        }
    }

    /* access modifiers changed from: package-private */
    public void init(PropertyManager propertyManager) throws XMLStreamException {
        this.fPropertyManager = propertyManager;
        propertyManager.setProperty("http://apache.org/xml/properties/internal/symbol-table", this.fSymbolTable);
        propertyManager.setProperty("http://apache.org/xml/properties/internal/error-reporter", this.fErrorReporter);
        propertyManager.setProperty(ENTITY_MANAGER, this.fEntityManager);
        reset();
    }

    public boolean canReuse() {
        return this.fReuse;
    }

    public void reset() {
        this.fReuse = true;
        this.fEventType = 0;
        this.fEntityManager.reset(this.fPropertyManager);
        this.fScanner.reset(this.fPropertyManager);
        this.fDTDDecl = null;
        this.fEntityScanner = this.fEntityManager.getEntityScanner();
        this.fReaderInDefinedState = ((Boolean) this.fPropertyManager.getProperty("http://java.sun.com/xml/stream/properties/reader-in-defined-state")).booleanValue();
        this.fBindNamespaces = ((Boolean) this.fPropertyManager.getProperty("javax.xml.stream.isNamespaceAware")).booleanValue();
        this.versionStr = null;
    }

    public void close() throws XMLStreamException {
        this.fReuse = true;
    }

    public String getCharacterEncodingScheme() {
        return this.fScanner.getCharacterEncodingScheme();
    }

    public int getColumnNumber() {
        return this.fEntityScanner.getColumnNumber();
    }

    public String getEncoding() {
        return this.fEntityScanner.getEncoding();
    }

    public int getEventType() {
        return this.fEventType;
    }

    public int getLineNumber() {
        return this.fEntityScanner.getLineNumber();
    }

    public String getLocalName() {
        int i = this.fEventType;
        if (i == 1 || i == 2) {
            return this.fScanner.getElementQName().localpart;
        }
        if (i == 9) {
            return this.fScanner.getEntityName();
        }
        throw new IllegalStateException("Method getLocalName() cannot be called for " + getEventTypeString(this.fEventType) + " event.");
    }

    public String getNamespaceURI() {
        int i = this.fEventType;
        if (i == 1 || i == 2) {
            return this.fScanner.getElementQName().uri;
        }
        return null;
    }

    public String getPIData() {
        if (this.fEventType == 3) {
            return this.fScanner.getPIData().toString();
        }
        throw new IllegalStateException("Current state of the parser is " + getEventTypeString(this.fEventType) + " But Expected state is 3");
    }

    public String getPITarget() {
        if (this.fEventType == 3) {
            return this.fScanner.getPITarget();
        }
        throw new IllegalStateException("Current state of the parser is " + getEventTypeString(this.fEventType) + " But Expected state is 3");
    }

    public String getPrefix() {
        int i = this.fEventType;
        if (i != 1 && i != 2) {
            return null;
        }
        String str = this.fScanner.getElementQName().prefix;
        return str == null ? "" : str;
    }

    public char[] getTextCharacters() {
        int i = this.fEventType;
        if (i == 4 || i == 5 || i == 12 || i == 6) {
            return this.fScanner.getCharacterData().ch;
        }
        throw new IllegalStateException("Current state = " + getEventTypeString(this.fEventType) + " is not among the states " + getEventTypeString(4) + " , " + getEventTypeString(5) + " , " + getEventTypeString(12) + " , " + getEventTypeString(6) + " valid for getTextCharacters() ");
    }

    public int getTextLength() {
        int i = this.fEventType;
        if (i == 4 || i == 5 || i == 12 || i == 6) {
            return this.fScanner.getCharacterData().length;
        }
        throw new IllegalStateException("Current state = " + getEventTypeString(this.fEventType) + " is not among the states " + getEventTypeString(4) + " , " + getEventTypeString(5) + " , " + getEventTypeString(12) + " , " + getEventTypeString(6) + " valid for getTextLength() ");
    }

    public int getTextStart() {
        int i = this.fEventType;
        if (i == 4 || i == 5 || i == 12 || i == 6) {
            return this.fScanner.getCharacterData().offset;
        }
        throw new IllegalStateException("Current state = " + getEventTypeString(this.fEventType) + " is not among the states " + getEventTypeString(4) + " , " + getEventTypeString(5) + " , " + getEventTypeString(12) + " , " + getEventTypeString(6) + " valid for getTextStart() ");
    }

    public String getValue() {
        int i = this.fEventType;
        if (i == 3) {
            return this.fScanner.getPIData().toString();
        }
        if (i == 5) {
            return this.fScanner.getComment();
        }
        if (i == 1 || i == 2) {
            return this.fScanner.getElementQName().localpart;
        }
        if (i == 4) {
            return this.fScanner.getCharacterData().toString();
        }
        return null;
    }

    public String getVersion() {
        String xMLVersion = this.fEntityScanner.getXMLVersion();
        if (!"1.0".equals(xMLVersion) || this.fEntityScanner.xmlVersionSetExplicitly) {
            return xMLVersion;
        }
        return null;
    }

    public boolean hasAttributes() {
        return this.fScanner.getAttributeIterator().getLength() > 0;
    }

    public boolean hasName() {
        int i = this.fEventType;
        return i == 1 || i == 2;
    }

    public boolean hasNext() throws XMLStreamException {
        int i = this.fEventType;
        return (i == -1 || i == 8) ? false : true;
    }

    public boolean hasValue() {
        int i = this.fEventType;
        return i == 1 || i == 2 || i == 9 || i == 3 || i == 5 || i == 4;
    }

    public boolean isEndElement() {
        return this.fEventType == 2;
    }

    public boolean isStandalone() {
        return this.fScanner.isStandAlone();
    }

    public boolean isStartElement() {
        return this.fEventType == 1;
    }

    public boolean isWhiteSpace() {
        if (!isCharacters() && this.fEventType != 12) {
            return false;
        }
        char[] textCharacters = getTextCharacters();
        int textStart = getTextStart();
        int textLength = getTextLength() + textStart;
        while (textStart < textLength) {
            if (!XMLChar.isSpace(textCharacters[textStart])) {
                return false;
            }
            textStart++;
        }
        return true;
    }

    public int next() throws XMLStreamException {
        Boolean bool;
        if (hasNext()) {
            try {
                this.fEventType = this.fScanner.next();
                if (this.versionStr == null) {
                    this.versionStr = getVersion();
                }
                if (this.fEventType == 7 && this.versionStr != null && this.versionStr.equals(SerializerConstants.XMLVERSION11)) {
                    switchToXML11Scanner();
                }
                if (this.fEventType == 4 || this.fEventType == 9 || this.fEventType == 3 || this.fEventType == 5 || this.fEventType == 12) {
                    this.fEntityScanner.checkNodeCount(this.fEntityScanner.fCurrentEntity);
                }
                return this.fEventType;
            } catch (IOException e) {
                int i = this.fScanner.fScannerState;
                XMLDocumentScannerImpl xMLDocumentScannerImpl = this.fScanner;
                if (i != 46 || (bool = (Boolean) this.fPropertyManager.getProperty("javax.xml.stream.isValidating")) == null || bool.booleanValue()) {
                    throw new XMLStreamException(e.getMessage(), getLocation(), e);
                }
                this.fEventType = 11;
                this.fScanner.setScannerState(43);
                XMLDocumentScannerImpl xMLDocumentScannerImpl2 = this.fScanner;
                xMLDocumentScannerImpl2.setDriver(xMLDocumentScannerImpl2.fPrologDriver);
                String str = this.fDTDDecl;
                if (str == null || str.length() == 0) {
                    this.fDTDDecl = "<!-- Exception scanning External DTD Subset.  True contents of DTD cannot be determined.  Processing will continue as XMLInputFactory.IS_VALIDATING == false. -->";
                }
                return 11;
            } catch (XNIException e2) {
                throw new XMLStreamException(e2.getMessage(), getLocation(), e2.getException());
            }
        } else if (this.fEventType != -1) {
            throw new NoSuchElementException("END_DOCUMENT reached: no more elements on the stream.");
        } else {
            throw new XMLStreamException("Error processing input source. The input stream is not complete.");
        }
    }

    private void switchToXML11Scanner() throws IOException {
        int i = this.fScanner.fEntityDepth;
        NamespaceContext namespaceContext = this.fScanner.fNamespaceContext;
        this.fScanner = new XML11NSDocumentScannerImpl();
        this.fScanner.reset(this.fPropertyManager);
        this.fScanner.setPropertyManager(this.fPropertyManager);
        this.fEntityScanner = this.fEntityManager.getEntityScanner();
        this.fEntityManager.fCurrentEntity.mayReadChunks = true;
        this.fScanner.setScannerState(7);
        XMLDocumentScannerImpl xMLDocumentScannerImpl = this.fScanner;
        xMLDocumentScannerImpl.fEntityDepth = i;
        xMLDocumentScannerImpl.fNamespaceContext = namespaceContext;
        this.fEventType = xMLDocumentScannerImpl.next();
    }

    static final String getEventTypeString(int i) {
        switch (i) {
            case 1:
                return "START_ELEMENT";
            case 2:
                return "END_ELEMENT";
            case 3:
                return "PROCESSING_INSTRUCTION";
            case 4:
                return "CHARACTERS";
            case 5:
                return "COMMENT";
            case 6:
                return "SPACE";
            case 7:
                return "START_DOCUMENT";
            case 8:
                return "END_DOCUMENT";
            case 9:
                return "ENTITY_REFERENCE";
            case 10:
                return "ATTRIBUTE";
            case 11:
                return "DTD";
            case 12:
                return "CDATA";
            default:
                return "UNKNOWN_EVENT_TYPE, " + String.valueOf(i);
        }
    }

    public int getAttributeCount() {
        int i = this.fEventType;
        if (i == 1 || i == 10) {
            return this.fScanner.getAttributeIterator().getLength();
        }
        throw new IllegalStateException("Current state is not among the states " + getEventTypeString(1) + " , " + getEventTypeString(10) + "valid for getAttributeCount()");
    }

    public QName getAttributeName(int i) {
        int i2 = this.fEventType;
        if (i2 == 1 || i2 == 10) {
            return convertXNIQNametoJavaxQName(this.fScanner.getAttributeIterator().getQualifiedName(i));
        }
        throw new IllegalStateException("Current state is not among the states " + getEventTypeString(1) + " , " + getEventTypeString(10) + "valid for getAttributeName()");
    }

    public String getAttributeLocalName(int i) {
        int i2 = this.fEventType;
        if (i2 == 1 || i2 == 10) {
            return this.fScanner.getAttributeIterator().getLocalName(i);
        }
        throw new IllegalStateException();
    }

    public String getAttributeNamespace(int i) {
        int i2 = this.fEventType;
        if (i2 == 1 || i2 == 10) {
            return this.fScanner.getAttributeIterator().getURI(i);
        }
        throw new IllegalStateException("Current state is not among the states " + getEventTypeString(1) + " , " + getEventTypeString(10) + "valid for getAttributeNamespace()");
    }

    public String getAttributePrefix(int i) {
        int i2 = this.fEventType;
        if (i2 == 1 || i2 == 10) {
            return this.fScanner.getAttributeIterator().getPrefix(i);
        }
        throw new IllegalStateException("Current state is not among the states " + getEventTypeString(1) + " , " + getEventTypeString(10) + "valid for getAttributePrefix()");
    }

    public QName getAttributeQName(int i) {
        int i2 = this.fEventType;
        if (i2 == 1 || i2 == 10) {
            return new QName(this.fScanner.getAttributeIterator().getURI(i), this.fScanner.getAttributeIterator().getLocalName(i));
        }
        throw new IllegalStateException("Current state is not among the states " + getEventTypeString(1) + " , " + getEventTypeString(10) + "valid for getAttributeQName()");
    }

    public String getAttributeType(int i) {
        int i2 = this.fEventType;
        if (i2 == 1 || i2 == 10) {
            return this.fScanner.getAttributeIterator().getType(i);
        }
        throw new IllegalStateException("Current state is not among the states " + getEventTypeString(1) + " , " + getEventTypeString(10) + "valid for getAttributeType()");
    }

    public String getAttributeValue(int i) {
        int i2 = this.fEventType;
        if (i2 == 1 || i2 == 10) {
            return this.fScanner.getAttributeIterator().getValue(i);
        }
        throw new IllegalStateException("Current state is not among the states " + getEventTypeString(1) + " , " + getEventTypeString(10) + "valid for getAttributeValue()");
    }

    public String getAttributeValue(String str, String str2) {
        int i = this.fEventType;
        if (i == 1 || i == 10) {
            XMLAttributesIteratorImpl attributeIterator = this.fScanner.getAttributeIterator();
            if (str == null) {
                return attributeIterator.getValue(attributeIterator.getIndexByLocalName(str2));
            }
            XMLAttributesIteratorImpl attributeIterator2 = this.fScanner.getAttributeIterator();
            if (str.length() == 0) {
                str = null;
            }
            return attributeIterator2.getValue(str, str2);
        }
        throw new IllegalStateException("Current state is not among the states " + getEventTypeString(1) + " , " + getEventTypeString(10) + "valid for getAttributeValue()");
    }

    public String getElementText() throws XMLStreamException {
        if (getEventType() == 1) {
            int next = next();
            StringBuffer stringBuffer = new StringBuffer();
            while (next != 2) {
                if (next == 4 || next == 12 || next == 6 || next == 9) {
                    stringBuffer.append(getText());
                } else if (!(next == 3 || next == 5)) {
                    if (next == 8) {
                        throw new XMLStreamException("unexpected end of document when reading element text content");
                    } else if (next == 1) {
                        throw new XMLStreamException("elementGetText() function expects text only elment but START_ELEMENT was encountered.", getLocation());
                    } else {
                        throw new XMLStreamException("Unexpected event type " + next, getLocation());
                    }
                }
                next = next();
            }
            return stringBuffer.toString();
        }
        throw new XMLStreamException("parser must be on START_ELEMENT to read next text", getLocation());
    }

    public Location getLocation() {
        return new Location() {
            /* class ohos.com.sun.org.apache.xerces.internal.impl.XMLStreamReaderImpl.AnonymousClass1 */
            int _columnNumber = XMLStreamReaderImpl.this.fEntityScanner.getColumnNumber();
            int _lineNumber = XMLStreamReaderImpl.this.fEntityScanner.getLineNumber();
            int _offset = XMLStreamReaderImpl.this.fEntityScanner.getCharacterOffset();
            String _publicId = XMLStreamReaderImpl.this.fEntityScanner.getPublicId();
            String _systemId = XMLStreamReaderImpl.this.fEntityScanner.getExpandedSystemId();

            public String getLocationURI() {
                return this._systemId;
            }

            public int getCharacterOffset() {
                return this._offset;
            }

            public int getColumnNumber() {
                return this._columnNumber;
            }

            public int getLineNumber() {
                return this._lineNumber;
            }

            public String getPublicId() {
                return this._publicId;
            }

            public String getSystemId() {
                return this._systemId;
            }

            public String toString() {
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append("Line number = " + getLineNumber());
                stringBuffer.append("\n");
                stringBuffer.append("Column number = " + getColumnNumber());
                stringBuffer.append("\n");
                stringBuffer.append("System Id = " + getSystemId());
                stringBuffer.append("\n");
                stringBuffer.append("Public Id = " + getPublicId());
                stringBuffer.append("\n");
                stringBuffer.append("Location Uri= " + getLocationURI());
                stringBuffer.append("\n");
                stringBuffer.append("CharacterOffset = " + getCharacterOffset());
                stringBuffer.append("\n");
                return stringBuffer.toString();
            }
        };
    }

    public QName getName() {
        int i = this.fEventType;
        if (i == 1 || i == 2) {
            return convertXNIQNametoJavaxQName(this.fScanner.getElementQName());
        }
        throw new IllegalStateException("Illegal to call getName() when event type is " + getEventTypeString(this.fEventType) + ". Valid states are " + getEventTypeString(1) + ", " + getEventTypeString(2));
    }

    public ohos.javax.xml.namespace.NamespaceContext getNamespaceContext() {
        return this.fNamespaceContextWrapper;
    }

    public int getNamespaceCount() {
        int i = this.fEventType;
        if (i == 1 || i == 2 || i == 13) {
            return this.fScanner.getNamespaceContext().getDeclaredPrefixCount();
        }
        throw new IllegalStateException("Current event state is " + getEventTypeString(this.fEventType) + " is not among the states " + getEventTypeString(1) + ", " + getEventTypeString(2) + ", " + getEventTypeString(13) + " valid for getNamespaceCount().");
    }

    public String getNamespacePrefix(int i) {
        int i2 = this.fEventType;
        if (i2 == 1 || i2 == 2 || i2 == 13) {
            String declaredPrefixAt = this.fScanner.getNamespaceContext().getDeclaredPrefixAt(i);
            if (declaredPrefixAt.equals("")) {
                return null;
            }
            return declaredPrefixAt;
        }
        throw new IllegalStateException("Current state " + getEventTypeString(this.fEventType) + " is not among the states " + getEventTypeString(1) + ", " + getEventTypeString(2) + ", " + getEventTypeString(13) + " valid for getNamespacePrefix().");
    }

    public String getNamespaceURI(int i) {
        int i2 = this.fEventType;
        if (i2 == 1 || i2 == 2 || i2 == 13) {
            return this.fScanner.getNamespaceContext().getURI(this.fScanner.getNamespaceContext().getDeclaredPrefixAt(i));
        }
        throw new IllegalStateException("Current state " + getEventTypeString(this.fEventType) + " is not among the states " + getEventTypeString(1) + ", " + getEventTypeString(2) + ", " + getEventTypeString(13) + " valid for getNamespaceURI().");
    }

    public Object getProperty(String str) throws IllegalArgumentException {
        if (str == null) {
            throw new IllegalArgumentException();
        } else if (this.fPropertyManager == null) {
            return null;
        } else {
            if (str.equals(PropertyManager.STAX_NOTATIONS)) {
                return getNotationDecls();
            }
            PropertyManager propertyManager = this.fPropertyManager;
            if (str.equals(PropertyManager.STAX_ENTITIES)) {
                return getEntityDecls();
            }
            return this.fPropertyManager.getProperty(str);
        }
    }

    public String getText() {
        int i = this.fEventType;
        if (i == 4 || i == 5 || i == 12 || i == 6) {
            return this.fScanner.getCharacterData().toString();
        }
        if (i == 9) {
            String entityName = this.fScanner.getEntityName();
            if (entityName == null) {
                return null;
            }
            if (this.fScanner.foundBuiltInRefs) {
                return this.fScanner.getCharacterData().toString();
            }
            Entity entity = this.fEntityManager.getEntityStore().getEntity(entityName);
            if (entity == null) {
                return null;
            }
            if (entity.isExternal()) {
                return ((Entity.ExternalEntity) entity).entityLocation.getExpandedSystemId();
            }
            return ((Entity.InternalEntity) entity).text;
        } else if (i == 11) {
            String str = this.fDTDDecl;
            if (str != null) {
                return str;
            }
            this.fDTDDecl = this.fScanner.getDTDDecl().toString();
            return this.fDTDDecl;
        } else {
            throw new IllegalStateException("Current state " + getEventTypeString(this.fEventType) + " is not among the states" + getEventTypeString(4) + ", " + getEventTypeString(5) + ", " + getEventTypeString(12) + ", " + getEventTypeString(6) + ", " + getEventTypeString(9) + ", " + getEventTypeString(11) + " valid for getText() ");
        }
    }

    public void require(int i, String str, String str2) throws XMLStreamException {
        if (i != this.fEventType) {
            throw new XMLStreamException("Event type " + getEventTypeString(i) + " specified did not match with current parser event " + getEventTypeString(this.fEventType));
        } else if (str != null && !str.equals(getNamespaceURI())) {
            throw new XMLStreamException("Namespace URI " + str + " specified did not match with current namespace URI");
        } else if (str2 != null && !str2.equals(getLocalName())) {
            throw new XMLStreamException("LocalName " + str2 + " specified did not match with current local name");
        }
    }

    public int getTextCharacters(int i, char[] cArr, int i2, int i3) throws XMLStreamException {
        if (cArr == null) {
            throw new NullPointerException("target char array can't be null");
        } else if (i2 < 0 || i3 < 0 || i < 0 || i2 >= cArr.length || i2 + i3 > cArr.length) {
            throw new IndexOutOfBoundsException();
        } else {
            int textLength = getTextLength() - i;
            if (textLength >= 0) {
                if (textLength < i3) {
                    i3 = textLength;
                }
                System.arraycopy(getTextCharacters(), getTextStart() + i, cArr, i2, i3);
                return i3;
            }
            throw new IndexOutOfBoundsException("sourceStart is greater thannumber of characters associated with this event");
        }
    }

    public boolean hasText() {
        int i = this.fEventType;
        if (i == 4 || i == 5 || i == 12) {
            return this.fScanner.getCharacterData().length > 0;
        }
        if (i == 9) {
            String entityName = this.fScanner.getEntityName();
            if (entityName == null) {
                return false;
            }
            if (this.fScanner.foundBuiltInRefs) {
                return true;
            }
            Entity entity = this.fEntityManager.getEntityStore().getEntity(entityName);
            if (entity == null) {
                return false;
            }
            return entity.isExternal() ? ((Entity.ExternalEntity) entity).entityLocation.getExpandedSystemId() != null : ((Entity.InternalEntity) entity).text != null;
        } else if (i == 11) {
            return this.fScanner.fSeenDoctypeDecl;
        } else {
            return false;
        }
    }

    public boolean isAttributeSpecified(int i) {
        int i2 = this.fEventType;
        if (i2 == 1 || i2 == 10) {
            return this.fScanner.getAttributeIterator().isSpecified(i);
        }
        throw new IllegalStateException("Current state is not among the states " + getEventTypeString(1) + " , " + getEventTypeString(10) + "valid for isAttributeSpecified()");
    }

    public boolean isCharacters() {
        return this.fEventType == 4;
    }

    public int nextTag() throws XMLStreamException {
        int next = next();
        while (true) {
            if ((next != 4 || !isWhiteSpace()) && !((next == 12 && isWhiteSpace()) || next == 6 || next == 3 || next == 5)) {
                break;
            }
            next = next();
        }
        if (next == 1 || next == 2) {
            return next;
        }
        throw new XMLStreamException("found: " + getEventTypeString(next) + ", expected " + getEventTypeString(1) + " or " + getEventTypeString(2), getLocation());
    }

    public boolean standaloneSet() {
        return this.fScanner.standaloneSet();
    }

    public QName convertXNIQNametoJavaxQName(ohos.com.sun.org.apache.xerces.internal.xni.QName qName) {
        if (qName == null) {
            return null;
        }
        if (qName.prefix == null) {
            return new QName(qName.uri, qName.localpart);
        }
        return new QName(qName.uri, qName.localpart, qName.prefix);
    }

    public String getNamespaceURI(String str) {
        if (str != null) {
            return this.fScanner.getNamespaceContext().getURI(this.fSymbolTable.addSymbol(str));
        }
        throw new IllegalArgumentException("prefix cannot be null.");
    }

    /* access modifiers changed from: protected */
    public void setPropertyManager(PropertyManager propertyManager) {
        this.fPropertyManager = propertyManager;
        this.fScanner.setProperty(Constants.STAX_PROPERTIES, propertyManager);
        this.fScanner.setPropertyManager(propertyManager);
    }

    /* access modifiers changed from: protected */
    public PropertyManager getPropertyManager() {
        return this.fPropertyManager;
    }

    static void pr(String str) {
        System.out.println(str);
    }

    /* access modifiers changed from: protected */
    public List getEntityDecls() {
        ArrayList arrayList = null;
        if (this.fEventType == 11) {
            XMLEntityStorage entityStore = this.fEntityManager.getEntityStore();
            if (entityStore.hasEntities()) {
                arrayList = new ArrayList(entityStore.getEntitySize());
                Enumeration entityKeys = entityStore.getEntityKeys();
                while (entityKeys.hasMoreElements()) {
                    String str = (String) entityKeys.nextElement();
                    Entity entity = entityStore.getEntity(str);
                    EntityDeclarationImpl entityDeclarationImpl = new EntityDeclarationImpl();
                    entityDeclarationImpl.setEntityName(str);
                    if (entity.isExternal()) {
                        Entity.ExternalEntity externalEntity = (Entity.ExternalEntity) entity;
                        entityDeclarationImpl.setXMLResourceIdentifier(externalEntity.entityLocation);
                        entityDeclarationImpl.setNotationName(externalEntity.notation);
                    } else {
                        entityDeclarationImpl.setEntityReplacementText(((Entity.InternalEntity) entity).text);
                    }
                    arrayList.add(entityDeclarationImpl);
                }
            }
        }
        return arrayList;
    }

    /* access modifiers changed from: protected */
    public List getNotationDecls() {
        DTDGrammar grammar;
        if (this.fEventType != 11 || this.fScanner.fDTDScanner == null || (grammar = ((XMLDTDScannerImpl) this.fScanner.fDTDScanner).getGrammar()) == null) {
            return null;
        }
        ArrayList arrayList = new ArrayList();
        for (XMLNotationDecl xMLNotationDecl : grammar.getNotationDecls()) {
            if (xMLNotationDecl != null) {
                arrayList.add(new NotationDeclarationImpl(xMLNotationDecl));
            }
        }
        return arrayList;
    }
}

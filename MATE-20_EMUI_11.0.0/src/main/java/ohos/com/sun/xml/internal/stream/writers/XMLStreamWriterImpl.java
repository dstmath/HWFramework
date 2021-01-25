package ohos.com.sun.xml.internal.stream.writers;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.Vector;
import ohos.com.sun.org.apache.xerces.internal.impl.Constants;
import ohos.com.sun.org.apache.xerces.internal.impl.PropertyManager;
import ohos.com.sun.org.apache.xerces.internal.util.NamespaceSupport;
import ohos.com.sun.org.apache.xerces.internal.util.SymbolTable;
import ohos.com.sun.org.apache.xerces.internal.utils.SecuritySupport;
import ohos.com.sun.org.apache.xerces.internal.xni.QName;
import ohos.com.sun.org.apache.xml.internal.serializer.SerializerConstants;
import ohos.com.sun.xml.internal.stream.util.ReadOnlyIterator;
import ohos.javax.xml.namespace.NamespaceContext;
import ohos.javax.xml.stream.XMLStreamException;
import ohos.javax.xml.stream.XMLStreamWriter;
import ohos.javax.xml.transform.stream.StreamResult;

public final class XMLStreamWriterImpl extends AbstractMap implements XMLStreamWriter {
    public static final String CLOSE_EMPTY_ELEMENT = "/>";
    public static final char CLOSE_END_TAG = '>';
    public static final char CLOSE_START_TAG = '>';
    public static final String DEFAULT_ENCODING = " encoding=\"utf-8\"";
    public static final String DEFAULT_XMLDECL = "<?xml version=\"1.0\" ?>";
    public static final String DEFAULT_XML_VERSION = "1.0";
    public static final String END_CDATA = "]]>";
    public static final String END_COMMENT = "-->";
    public static final String OPEN_END_TAG = "</";
    public static final char OPEN_START_TAG = '<';
    public static final String OUTPUTSTREAM_PROPERTY = "sjsxp-outputstream";
    public static final String SPACE = " ";
    public static final String START_CDATA = "<![CDATA[";
    public static final String START_COMMENT = "<!--";
    public static final String UTF_8 = "UTF-8";
    private final String DEFAULT_PREFIX;
    HashMap fAttrNamespace;
    private ArrayList fAttributeCache;
    private ElementStack fElementStack;
    private CharsetEncoder fEncoder;
    boolean fEscapeCharacters;
    private NamespaceSupport fInternalNamespaceContext;
    private boolean fIsRepairingNamespace;
    private NamespaceContextImpl fNamespaceContext;
    private ArrayList fNamespaceDecls;
    private OutputStream fOutputStream;
    private Random fPrefixGen;
    private PropertyManager fPropertyManager;
    private final ReadOnlyIterator fReadOnlyIterator;
    private boolean fReuse;
    private boolean fStartTagOpened;
    private SymbolTable fSymbolTable;
    private Writer fWriter;

    @Override // java.util.AbstractMap, java.util.Map, java.lang.Object
    public boolean equals(Object obj) {
        return this == obj;
    }

    @Override // java.util.AbstractMap, java.util.Map
    public boolean isEmpty() {
        return false;
    }

    @Override // java.util.AbstractMap, java.util.Map
    public int size() {
        return 1;
    }

    public XMLStreamWriterImpl(OutputStream outputStream, PropertyManager propertyManager) throws IOException {
        this(new OutputStreamWriter(outputStream), propertyManager);
    }

    public XMLStreamWriterImpl(OutputStream outputStream, String str, PropertyManager propertyManager) throws IOException {
        this(new StreamResult(outputStream), str, propertyManager);
    }

    public XMLStreamWriterImpl(Writer writer, PropertyManager propertyManager) throws IOException {
        this(new StreamResult(writer), (String) null, propertyManager);
    }

    public XMLStreamWriterImpl(StreamResult streamResult, String str, PropertyManager propertyManager) throws IOException {
        this.fEscapeCharacters = true;
        this.fIsRepairingNamespace = false;
        this.fOutputStream = null;
        this.fNamespaceContext = null;
        this.fInternalNamespaceContext = null;
        this.fPrefixGen = null;
        this.fPropertyManager = null;
        this.fStartTagOpened = false;
        this.fSymbolTable = new SymbolTable();
        this.fElementStack = new ElementStack();
        this.DEFAULT_PREFIX = this.fSymbolTable.addSymbol("");
        this.fReadOnlyIterator = new ReadOnlyIterator();
        this.fEncoder = null;
        this.fAttrNamespace = null;
        setOutput(streamResult, str);
        this.fPropertyManager = propertyManager;
        init();
    }

    private void init() {
        this.fReuse = false;
        this.fNamespaceDecls = new ArrayList();
        this.fPrefixGen = new Random();
        this.fAttributeCache = new ArrayList();
        this.fInternalNamespaceContext = new NamespaceSupport();
        this.fInternalNamespaceContext.reset();
        this.fNamespaceContext = new NamespaceContextImpl();
        this.fNamespaceContext.internalContext = this.fInternalNamespaceContext;
        this.fIsRepairingNamespace = ((Boolean) this.fPropertyManager.getProperty("javax.xml.stream.isRepairingNamespaces")).booleanValue();
        setEscapeCharacters(((Boolean) this.fPropertyManager.getProperty(Constants.ESCAPE_CHARACTERS)).booleanValue());
    }

    public void reset() {
        reset(false);
    }

    /* access modifiers changed from: package-private */
    public void reset(boolean z) {
        if (this.fReuse) {
            this.fReuse = false;
            this.fNamespaceDecls.clear();
            this.fAttributeCache.clear();
            this.fElementStack.clear();
            this.fInternalNamespaceContext.reset();
            this.fStartTagOpened = false;
            this.fNamespaceContext.userContext = null;
            if (z) {
                this.fIsRepairingNamespace = ((Boolean) this.fPropertyManager.getProperty("javax.xml.stream.isRepairingNamespaces")).booleanValue();
                setEscapeCharacters(((Boolean) this.fPropertyManager.getProperty(Constants.ESCAPE_CHARACTERS)).booleanValue());
                return;
            }
            return;
        }
        throw new IllegalStateException("close() Must be called before calling reset()");
    }

    public void setOutput(StreamResult streamResult, String str) throws IOException {
        if (streamResult.getOutputStream() != null) {
            setOutputUsingStream(streamResult.getOutputStream(), str);
        } else if (streamResult.getWriter() != null) {
            setOutputUsingWriter(streamResult.getWriter());
        } else if (streamResult.getSystemId() != null) {
            setOutputUsingStream(new FileOutputStream(streamResult.getSystemId()), str);
        }
    }

    private void setOutputUsingWriter(Writer writer) throws IOException {
        String encoding;
        this.fWriter = writer;
        if ((writer instanceof OutputStreamWriter) && (encoding = ((OutputStreamWriter) writer).getEncoding()) != null && !encoding.equalsIgnoreCase(WriterUtility.UTF_8)) {
            this.fEncoder = Charset.forName(encoding).newEncoder();
        }
    }

    private void setOutputUsingStream(OutputStream outputStream, String str) throws IOException {
        this.fOutputStream = outputStream;
        if (str == null) {
            String systemProperty = SecuritySupport.getSystemProperty("file.encoding");
            if (systemProperty == null || !systemProperty.equalsIgnoreCase(WriterUtility.UTF_8)) {
                this.fWriter = new XMLWriter(new OutputStreamWriter(outputStream));
            } else {
                this.fWriter = new UTF8OutputStreamWriter(outputStream);
            }
        } else if (str.equalsIgnoreCase(WriterUtility.UTF_8)) {
            this.fWriter = new UTF8OutputStreamWriter(outputStream);
        } else {
            this.fWriter = new XMLWriter(new OutputStreamWriter(outputStream, str));
            this.fEncoder = Charset.forName(str).newEncoder();
        }
    }

    public boolean canReuse() {
        return this.fReuse;
    }

    public void setEscapeCharacters(boolean z) {
        this.fEscapeCharacters = z;
    }

    public boolean getEscapeCharacters() {
        return this.fEscapeCharacters;
    }

    public void close() throws XMLStreamException {
        Writer writer = this.fWriter;
        if (writer != null) {
            try {
                writer.flush();
            } catch (IOException e) {
                throw new XMLStreamException(e);
            }
        }
        this.fWriter = null;
        this.fOutputStream = null;
        this.fNamespaceDecls.clear();
        this.fAttributeCache.clear();
        this.fElementStack.clear();
        this.fInternalNamespaceContext.reset();
        this.fReuse = true;
        this.fStartTagOpened = false;
        this.fNamespaceContext.userContext = null;
    }

    public void flush() throws XMLStreamException {
        try {
            this.fWriter.flush();
        } catch (IOException e) {
            throw new XMLStreamException(e);
        }
    }

    public NamespaceContext getNamespaceContext() {
        return this.fNamespaceContext;
    }

    public String getPrefix(String str) throws XMLStreamException {
        return this.fNamespaceContext.getPrefix(str);
    }

    public Object getProperty(String str) throws IllegalArgumentException {
        if (str == null) {
            throw new NullPointerException();
        } else if (this.fPropertyManager.containsProperty(str)) {
            return this.fPropertyManager.getProperty(str);
        } else {
            throw new IllegalArgumentException("Property '" + str + "' is not supported");
        }
    }

    public void setDefaultNamespace(String str) throws XMLStreamException {
        if (str != null) {
            str = this.fSymbolTable.addSymbol(str);
        }
        if (!this.fIsRepairingNamespace) {
            this.fInternalNamespaceContext.declarePrefix(this.DEFAULT_PREFIX, str);
        } else if (!isDefaultNamespace(str)) {
            QName qName = new QName();
            qName.setValues(this.DEFAULT_PREFIX, "xmlns", null, str);
            this.fNamespaceDecls.add(qName);
        }
    }

    public void setNamespaceContext(NamespaceContext namespaceContext) throws XMLStreamException {
        this.fNamespaceContext.userContext = namespaceContext;
    }

    public void setPrefix(String str, String str2) throws XMLStreamException {
        if (str == null) {
            throw new XMLStreamException("Prefix cannot be null");
        } else if (str2 != null) {
            String addSymbol = this.fSymbolTable.addSymbol(str);
            String addSymbol2 = this.fSymbolTable.addSymbol(str2);
            if (this.fIsRepairingNamespace) {
                String uri = this.fInternalNamespaceContext.getURI(addSymbol);
                if ((uri == null || uri != addSymbol2) && !checkUserNamespaceContext(addSymbol, addSymbol2)) {
                    QName qName = new QName();
                    qName.setValues(addSymbol, "xmlns", null, addSymbol2);
                    this.fNamespaceDecls.add(qName);
                    return;
                }
                return;
            }
            this.fInternalNamespaceContext.declarePrefix(addSymbol, addSymbol2);
        } else {
            throw new XMLStreamException("URI cannot be null");
        }
    }

    public void writeAttribute(String str, String str2) throws XMLStreamException {
        try {
            if (!this.fStartTagOpened) {
                throw new XMLStreamException("Attribute not associated with any element");
            } else if (this.fIsRepairingNamespace) {
                Attribute attribute = new Attribute(str2);
                attribute.setValues(null, str, null, null);
                this.fAttributeCache.add(attribute);
            } else {
                this.fWriter.write(" ");
                this.fWriter.write(str);
                this.fWriter.write("=\"");
                writeXMLContent(str2, true, true);
                this.fWriter.write("\"");
            }
        } catch (IOException e) {
            throw new XMLStreamException(e);
        }
    }

    public void writeAttribute(String str, String str2, String str3) throws XMLStreamException {
        try {
            if (!this.fStartTagOpened) {
                throw new XMLStreamException("Attribute not associated with any element");
            } else if (str != null) {
                String addSymbol = this.fSymbolTable.addSymbol(str);
                String prefix = this.fInternalNamespaceContext.getPrefix(addSymbol);
                if (this.fIsRepairingNamespace) {
                    Attribute attribute = new Attribute(str3);
                    attribute.setValues(null, str2, null, addSymbol);
                    this.fAttributeCache.add(attribute);
                } else if (prefix != null) {
                    writeAttributeWithPrefix(prefix, str2, str3);
                } else {
                    throw new XMLStreamException("Prefix cannot be null");
                }
            } else {
                throw new XMLStreamException("NamespaceURI cannot be null");
            }
        } catch (IOException e) {
            throw new XMLStreamException(e);
        }
    }

    private void writeAttributeWithPrefix(String str, String str2, String str3) throws IOException {
        this.fWriter.write(" ");
        if (!(str == null || str == "")) {
            this.fWriter.write(str);
            this.fWriter.write(":");
        }
        this.fWriter.write(str2);
        this.fWriter.write("=\"");
        writeXMLContent(str3, true, true);
        this.fWriter.write("\"");
    }

    public void writeAttribute(String str, String str2, String str3, String str4) throws XMLStreamException {
        String uri;
        try {
            if (!this.fStartTagOpened) {
                throw new XMLStreamException("Attribute not associated with any element");
            } else if (str2 == null) {
                throw new XMLStreamException("NamespaceURI cannot be null");
            } else if (str3 == null) {
                throw new XMLStreamException("Local name cannot be null");
            } else if (this.fIsRepairingNamespace) {
                if (str != null) {
                    str = this.fSymbolTable.addSymbol(str);
                }
                String addSymbol = this.fSymbolTable.addSymbol(str2);
                Attribute attribute = new Attribute(str4);
                attribute.setValues(str, str3, null, addSymbol);
                this.fAttributeCache.add(attribute);
            } else if (str != null && !str.equals("")) {
                if (!str.equals("xml") || !str2.equals("http://www.w3.org/XML/1998/namespace")) {
                    str = this.fSymbolTable.addSymbol(str);
                    String addSymbol2 = this.fSymbolTable.addSymbol(str2);
                    if (!this.fInternalNamespaceContext.containsPrefixInCurrentContext(str) || (uri = this.fInternalNamespaceContext.getURI(str)) == null || uri == addSymbol2) {
                        this.fInternalNamespaceContext.declarePrefix(str, addSymbol2);
                    } else {
                        throw new XMLStreamException("Prefix " + str + " is already bound to " + uri + ". Trying to rebind it to " + addSymbol2 + " is an error.");
                    }
                }
                writeAttributeWithPrefix(str, str3, str4);
            } else if (str2.equals("")) {
                writeAttributeWithPrefix(null, str3, str4);
            } else {
                throw new XMLStreamException("prefix cannot be null or empty");
            }
        } catch (IOException e) {
            throw new XMLStreamException(e);
        }
    }

    public void writeCData(String str) throws XMLStreamException {
        if (str != null) {
            try {
                if (this.fStartTagOpened) {
                    closeStartTag();
                }
                this.fWriter.write("<![CDATA[");
                this.fWriter.write(str);
                this.fWriter.write("]]>");
            } catch (IOException e) {
                throw new XMLStreamException(e);
            }
        } else {
            throw new XMLStreamException("cdata cannot be null");
        }
    }

    public void writeCharacters(String str) throws XMLStreamException {
        try {
            if (this.fStartTagOpened) {
                closeStartTag();
            }
            writeXMLContent(str);
        } catch (IOException e) {
            throw new XMLStreamException(e);
        }
    }

    public void writeCharacters(char[] cArr, int i, int i2) throws XMLStreamException {
        try {
            if (this.fStartTagOpened) {
                closeStartTag();
            }
            writeXMLContent(cArr, i, i2, this.fEscapeCharacters);
        } catch (IOException e) {
            throw new XMLStreamException(e);
        }
    }

    public void writeComment(String str) throws XMLStreamException {
        try {
            if (this.fStartTagOpened) {
                closeStartTag();
            }
            this.fWriter.write("<!--");
            if (str != null) {
                this.fWriter.write(str);
            }
            this.fWriter.write("-->");
        } catch (IOException e) {
            throw new XMLStreamException(e);
        }
    }

    public void writeDTD(String str) throws XMLStreamException {
        try {
            if (this.fStartTagOpened) {
                closeStartTag();
            }
            this.fWriter.write(str);
        } catch (IOException e) {
            throw new XMLStreamException(e);
        }
    }

    public void writeDefaultNamespace(String str) throws XMLStreamException {
        String uri;
        if (str == null) {
            str = "";
        }
        try {
            if (!this.fStartTagOpened) {
                throw new IllegalStateException("Namespace Attribute not associated with any element");
            } else if (this.fIsRepairingNamespace) {
                QName qName = new QName();
                qName.setValues("", "xmlns", null, str);
                this.fNamespaceDecls.add(qName);
            } else {
                String addSymbol = this.fSymbolTable.addSymbol(str);
                if (this.fInternalNamespaceContext.containsPrefixInCurrentContext("") && (uri = this.fInternalNamespaceContext.getURI("")) != null) {
                    if (uri != addSymbol) {
                        throw new XMLStreamException("xmlns has been already bound to " + uri + ". Rebinding it to " + addSymbol + " is an error");
                    }
                }
                this.fInternalNamespaceContext.declarePrefix("", addSymbol);
                writenamespace(null, addSymbol);
            }
        } catch (IOException e) {
            throw new XMLStreamException(e);
        }
    }

    public void writeEmptyElement(String str) throws XMLStreamException {
        try {
            if (this.fStartTagOpened) {
                closeStartTag();
            }
            openStartTag();
            this.fElementStack.push(null, str, null, null, true);
            this.fInternalNamespaceContext.pushContext();
            if (!this.fIsRepairingNamespace) {
                this.fWriter.write(str);
            }
        } catch (IOException e) {
            throw new XMLStreamException(e);
        }
    }

    public void writeEmptyElement(String str, String str2) throws XMLStreamException {
        if (str != null) {
            String addSymbol = this.fSymbolTable.addSymbol(str);
            writeEmptyElement(this.fNamespaceContext.getPrefix(addSymbol), str2, addSymbol);
            return;
        }
        throw new XMLStreamException("NamespaceURI cannot be null");
    }

    public void writeEmptyElement(String str, String str2, String str3) throws XMLStreamException {
        if (str2 == null) {
            throw new XMLStreamException("Local Name cannot be null");
        } else if (str3 != null) {
            if (str != null) {
                try {
                    str = this.fSymbolTable.addSymbol(str);
                } catch (IOException e) {
                    throw new XMLStreamException(e);
                }
            }
            String addSymbol = this.fSymbolTable.addSymbol(str3);
            if (this.fStartTagOpened) {
                closeStartTag();
            }
            openStartTag();
            this.fElementStack.push(str, str2, null, addSymbol, true);
            this.fInternalNamespaceContext.pushContext();
            if (this.fIsRepairingNamespace) {
                return;
            }
            if (str != null) {
                if (str != "") {
                    this.fWriter.write(str);
                    this.fWriter.write(":");
                }
                this.fWriter.write(str2);
                return;
            }
            throw new XMLStreamException("NamespaceURI " + addSymbol + " has not been bound to any prefix");
        } else {
            throw new XMLStreamException("NamespaceURI cannot be null");
        }
    }

    public void writeEndDocument() throws XMLStreamException {
        try {
            if (this.fStartTagOpened) {
                closeStartTag();
            }
            while (!this.fElementStack.empty()) {
                ElementState pop = this.fElementStack.pop();
                this.fInternalNamespaceContext.popContext();
                if (!pop.isEmpty) {
                    this.fWriter.write("</");
                    if (pop.prefix != null && !pop.prefix.equals("")) {
                        this.fWriter.write(pop.prefix);
                        this.fWriter.write(":");
                    }
                    this.fWriter.write(pop.localpart);
                    this.fWriter.write(62);
                }
            }
        } catch (IOException e) {
            throw new XMLStreamException(e);
        } catch (ArrayIndexOutOfBoundsException unused) {
            throw new XMLStreamException("No more elements to write");
        }
    }

    public void writeEndElement() throws XMLStreamException {
        try {
            if (this.fStartTagOpened) {
                closeStartTag();
            }
            ElementState pop = this.fElementStack.pop();
            if (pop == null) {
                throw new XMLStreamException("No element was found to write");
            } else if (!pop.isEmpty) {
                this.fWriter.write("</");
                if (pop.prefix != null && !pop.prefix.equals("")) {
                    this.fWriter.write(pop.prefix);
                    this.fWriter.write(":");
                }
                this.fWriter.write(pop.localpart);
                this.fWriter.write(62);
                this.fInternalNamespaceContext.popContext();
            }
        } catch (IOException e) {
            throw new XMLStreamException(e);
        } catch (ArrayIndexOutOfBoundsException e2) {
            throw new XMLStreamException("No element was found to write: " + e2.toString(), e2);
        }
    }

    public void writeEntityRef(String str) throws XMLStreamException {
        try {
            if (this.fStartTagOpened) {
                closeStartTag();
            }
            this.fWriter.write(38);
            this.fWriter.write(str);
            this.fWriter.write(59);
        } catch (IOException e) {
            throw new XMLStreamException(e);
        }
    }

    public void writeNamespace(String str, String str2) throws XMLStreamException {
        String uri;
        if (str2 == null) {
            str2 = "";
        }
        try {
            if (this.fStartTagOpened) {
                if (str != null && !str.equals("")) {
                    if (!str.equals("xmlns")) {
                        if (!str.equals("xml") || !str2.equals("http://www.w3.org/XML/1998/namespace")) {
                            String addSymbol = this.fSymbolTable.addSymbol(str);
                            String addSymbol2 = this.fSymbolTable.addSymbol(str2);
                            if (this.fIsRepairingNamespace) {
                                String uri2 = this.fInternalNamespaceContext.getURI(addSymbol);
                                if (uri2 == null || uri2 != addSymbol2) {
                                    QName qName = new QName();
                                    qName.setValues(addSymbol, "xmlns", null, addSymbol2);
                                    this.fNamespaceDecls.add(qName);
                                    return;
                                }
                                return;
                            }
                            if (this.fInternalNamespaceContext.containsPrefixInCurrentContext(addSymbol) && (uri = this.fInternalNamespaceContext.getURI(addSymbol)) != null) {
                                if (uri != addSymbol2) {
                                    throw new XMLStreamException("prefix " + addSymbol + " has been already bound to " + uri + ". Rebinding it to " + addSymbol2 + " is an error");
                                }
                            }
                            this.fInternalNamespaceContext.declarePrefix(addSymbol, addSymbol2);
                            writenamespace(addSymbol, addSymbol2);
                            return;
                        }
                        return;
                    }
                }
                writeDefaultNamespace(str2);
                return;
            }
            throw new IllegalStateException("Invalid state: start tag is not opened at writeNamespace(" + str + ", " + str2 + ")");
        } catch (IOException e) {
            throw new XMLStreamException(e);
        }
    }

    private void writenamespace(String str, String str2) throws IOException {
        this.fWriter.write(" xmlns");
        if (!(str == null || str == "")) {
            this.fWriter.write(":");
            this.fWriter.write(str);
        }
        this.fWriter.write("=\"");
        writeXMLContent(str2, true, true);
        this.fWriter.write("\"");
    }

    public void writeProcessingInstruction(String str) throws XMLStreamException {
        try {
            if (this.fStartTagOpened) {
                closeStartTag();
            }
            if (str != null) {
                this.fWriter.write("<?");
                this.fWriter.write(str);
                this.fWriter.write("?>");
                return;
            }
            throw new XMLStreamException("PI target cannot be null");
        } catch (IOException e) {
            throw new XMLStreamException(e);
        }
    }

    public void writeProcessingInstruction(String str, String str2) throws XMLStreamException {
        try {
            if (this.fStartTagOpened) {
                closeStartTag();
            }
            if (str == null || str2 == null) {
                throw new XMLStreamException("PI target cannot be null");
            }
            this.fWriter.write("<?");
            this.fWriter.write(str);
            this.fWriter.write(" ");
            this.fWriter.write(str2);
            this.fWriter.write("?>");
        } catch (IOException e) {
            throw new XMLStreamException(e);
        }
    }

    public void writeStartDocument() throws XMLStreamException {
        try {
            this.fWriter.write("<?xml version=\"1.0\" ?>");
        } catch (IOException e) {
            throw new XMLStreamException(e);
        }
    }

    public void writeStartDocument(String str) throws XMLStreamException {
        if (str != null) {
            try {
                if (!str.equals("")) {
                    this.fWriter.write("<?xml version=\"");
                    this.fWriter.write(str);
                    this.fWriter.write("\"");
                    this.fWriter.write("?>");
                    return;
                }
            } catch (IOException e) {
                throw new XMLStreamException(e);
            }
        }
        writeStartDocument();
    }

    public void writeStartDocument(String str, String str2) throws XMLStreamException {
        if (str == null && str2 == null) {
            try {
                writeStartDocument();
            } catch (IOException e) {
                throw new XMLStreamException(e);
            }
        } else if (str == null) {
            writeStartDocument(str2);
        } else {
            String str3 = null;
            if (this.fWriter instanceof OutputStreamWriter) {
                str3 = ((OutputStreamWriter) this.fWriter).getEncoding();
            } else if (this.fWriter instanceof UTF8OutputStreamWriter) {
                str3 = ((UTF8OutputStreamWriter) this.fWriter).getEncoding();
            } else if (this.fWriter instanceof XMLWriter) {
                str3 = ((OutputStreamWriter) ((XMLWriter) this.fWriter).getWriter()).getEncoding();
            }
            if (str3 != null && !str3.equalsIgnoreCase(str)) {
                boolean z = false;
                Iterator<String> it = Charset.forName(str).aliases().iterator();
                while (!z && it.hasNext()) {
                    if (str3.equalsIgnoreCase(it.next())) {
                        z = true;
                    }
                }
                if (!z) {
                    throw new XMLStreamException("Underlying stream encoding '" + str3 + "' and input paramter for writeStartDocument() method '" + str + "' do not match.");
                }
            }
            this.fWriter.write("<?xml version=\"");
            if (str2 == null || str2.equals("")) {
                this.fWriter.write("1.0");
            } else {
                this.fWriter.write(str2);
            }
            if (!str.equals("")) {
                this.fWriter.write("\" encoding=\"");
                this.fWriter.write(str);
            }
            this.fWriter.write("\"?>");
        }
    }

    public void writeStartElement(String str) throws XMLStreamException {
        if (str != null) {
            try {
                if (this.fStartTagOpened) {
                    closeStartTag();
                }
                openStartTag();
                this.fElementStack.push(null, str, null, null, false);
                this.fInternalNamespaceContext.pushContext();
                if (!this.fIsRepairingNamespace) {
                    this.fWriter.write(str);
                }
            } catch (IOException e) {
                throw new XMLStreamException(e);
            }
        } else {
            throw new XMLStreamException("Local Name cannot be null");
        }
    }

    public void writeStartElement(String str, String str2) throws XMLStreamException {
        if (str2 == null) {
            throw new XMLStreamException("Local Name cannot be null");
        } else if (str != null) {
            String addSymbol = this.fSymbolTable.addSymbol(str);
            String str3 = null;
            if (!this.fIsRepairingNamespace && (str3 = this.fNamespaceContext.getPrefix(addSymbol)) != null) {
                str3 = this.fSymbolTable.addSymbol(str3);
            }
            writeStartElement(str3, str2, addSymbol);
        } else {
            throw new XMLStreamException("NamespaceURI cannot be null");
        }
    }

    public void writeStartElement(String str, String str2, String str3) throws XMLStreamException {
        if (str2 == null) {
            throw new XMLStreamException("Local Name cannot be null");
        } else if (str3 != null) {
            try {
                if (!this.fIsRepairingNamespace) {
                    if (str == null) {
                        throw new XMLStreamException("Prefix cannot be null");
                    }
                }
                if (this.fStartTagOpened) {
                    closeStartTag();
                }
                openStartTag();
                String addSymbol = this.fSymbolTable.addSymbol(str3);
                if (str != null) {
                    str = this.fSymbolTable.addSymbol(str);
                }
                this.fElementStack.push(str, str2, null, addSymbol, false);
                this.fInternalNamespaceContext.pushContext();
                String prefix = this.fNamespaceContext.getPrefix(addSymbol);
                if (str != null && (prefix == null || !str.equals(prefix))) {
                    this.fInternalNamespaceContext.declarePrefix(str, addSymbol);
                }
                if (!this.fIsRepairingNamespace) {
                    if (!(str == null || str == "")) {
                        this.fWriter.write(str);
                        this.fWriter.write(":");
                    }
                    this.fWriter.write(str2);
                } else if (str == null) {
                } else {
                    if (prefix == null || !str.equals(prefix)) {
                        QName qName = new QName();
                        qName.setValues(str, "xmlns", null, addSymbol);
                        this.fNamespaceDecls.add(qName);
                    }
                }
            } catch (IOException e) {
                throw new XMLStreamException(e);
            }
        } else {
            throw new XMLStreamException("NamespaceURI cannot be null");
        }
    }

    private void writeCharRef(int i) throws IOException {
        this.fWriter.write("&#x");
        this.fWriter.write(Integer.toHexString(i));
        this.fWriter.write(59);
    }

    private void writeXMLContent(char[] cArr, int i, int i2, boolean z) throws IOException {
        if (!z) {
            this.fWriter.write(cArr, i, i2);
            return;
        }
        int i3 = i2 + i;
        int i4 = i;
        while (i < i3) {
            char c = cArr[i];
            CharsetEncoder charsetEncoder = this.fEncoder;
            if (charsetEncoder != null && !charsetEncoder.canEncode(c)) {
                this.fWriter.write(cArr, i4, i - i4);
                if (i != i3 - 1) {
                    int i5 = i + 1;
                    if (Character.isSurrogatePair(c, cArr[i5])) {
                        writeCharRef(Character.toCodePoint(c, cArr[i5]));
                        i = i5;
                    }
                }
                writeCharRef(c);
            } else if (c == '&') {
                this.fWriter.write(cArr, i4, i - i4);
                this.fWriter.write(SerializerConstants.ENTITY_AMP);
            } else if (c == '<') {
                this.fWriter.write(cArr, i4, i - i4);
                this.fWriter.write(SerializerConstants.ENTITY_LT);
            } else if (c != '>') {
                i++;
            } else {
                this.fWriter.write(cArr, i4, i - i4);
                this.fWriter.write(SerializerConstants.ENTITY_GT);
            }
            i4 = i + 1;
            i++;
        }
        this.fWriter.write(cArr, i4, i3 - i4);
    }

    private void writeXMLContent(String str) throws IOException {
        if (str != null && str.length() > 0) {
            writeXMLContent(str, this.fEscapeCharacters, false);
        }
    }

    private void writeXMLContent(String str, boolean z, boolean z2) throws IOException {
        if (!z) {
            this.fWriter.write(str);
            return;
        }
        int length = str.length();
        int i = 0;
        int i2 = 0;
        while (i < length) {
            char charAt = str.charAt(i);
            CharsetEncoder charsetEncoder = this.fEncoder;
            if (charsetEncoder != null && !charsetEncoder.canEncode(charAt)) {
                this.fWriter.write(str, i2, i - i2);
                if (i != length - 1) {
                    int i3 = i + 1;
                    if (Character.isSurrogatePair(charAt, str.charAt(i3))) {
                        writeCharRef(Character.toCodePoint(charAt, str.charAt(i3)));
                        i = i3;
                    }
                }
                writeCharRef(charAt);
            } else if (charAt == '\"') {
                this.fWriter.write(str, i2, i - i2);
                if (z2) {
                    this.fWriter.write(SerializerConstants.ENTITY_QUOT);
                } else {
                    this.fWriter.write(34);
                }
            } else if (charAt == '&') {
                this.fWriter.write(str, i2, i - i2);
                this.fWriter.write(SerializerConstants.ENTITY_AMP);
            } else if (charAt == '<') {
                this.fWriter.write(str, i2, i - i2);
                this.fWriter.write(SerializerConstants.ENTITY_LT);
            } else if (charAt != '>') {
                i++;
            } else {
                this.fWriter.write(str, i2, i - i2);
                this.fWriter.write(SerializerConstants.ENTITY_GT);
            }
            i2 = i + 1;
            i++;
        }
        this.fWriter.write(str, i2, length - i2);
    }

    private void closeStartTag() throws XMLStreamException {
        String prefix;
        try {
            ElementState peek = this.fElementStack.peek();
            if (this.fIsRepairingNamespace) {
                repair();
                correctPrefix(peek, 1);
                if (!(peek.prefix == null || peek.prefix == "")) {
                    this.fWriter.write(peek.prefix);
                    this.fWriter.write(":");
                }
                this.fWriter.write(peek.localpart);
                int size = this.fNamespaceDecls.size();
                for (int i = 0; i < size; i++) {
                    QName qName = (QName) this.fNamespaceDecls.get(i);
                    if (qName != null && this.fInternalNamespaceContext.declarePrefix(qName.prefix, qName.uri)) {
                        writenamespace(qName.prefix, qName.uri);
                    }
                }
                this.fNamespaceDecls.clear();
                for (int i2 = 0; i2 < this.fAttributeCache.size(); i2++) {
                    Attribute attribute = (Attribute) this.fAttributeCache.get(i2);
                    if (attribute.prefix != null && attribute.uri != null && !attribute.prefix.equals("") && !attribute.uri.equals("") && ((prefix = this.fInternalNamespaceContext.getPrefix(attribute.uri)) == null || prefix != attribute.prefix)) {
                        if (getAttrPrefix(attribute.uri) != null) {
                            writenamespace(attribute.prefix, attribute.uri);
                        } else if (this.fInternalNamespaceContext.declarePrefix(attribute.prefix, attribute.uri)) {
                            writenamespace(attribute.prefix, attribute.uri);
                        }
                    }
                    writeAttributeWithPrefix(attribute.prefix, attribute.localpart, attribute.value);
                }
                this.fAttrNamespace = null;
                this.fAttributeCache.clear();
            }
            if (peek.isEmpty) {
                this.fElementStack.pop();
                this.fInternalNamespaceContext.popContext();
                this.fWriter.write("/>");
            } else {
                this.fWriter.write(62);
            }
            this.fStartTagOpened = false;
        } catch (IOException e) {
            this.fStartTagOpened = false;
            throw new XMLStreamException(e);
        }
    }

    private void openStartTag() throws IOException {
        this.fStartTagOpened = true;
        this.fWriter.write(60);
    }

    /* JADX WARNING: Removed duplicated region for block: B:28:0x0057  */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x0078  */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x0080  */
    private void correctPrefix(QName qName, int i) {
        boolean z;
        String str;
        String str2 = qName.prefix;
        String str3 = qName.uri;
        if (str2 == null || str2.equals("")) {
            if (str3 == null) {
                return;
            }
            if (str2 != "" || str3 != "") {
                String addSymbol = this.fSymbolTable.addSymbol(str3);
                for (int i2 = 0; i2 < this.fNamespaceDecls.size(); i2++) {
                    QName qName2 = (QName) this.fNamespaceDecls.get(i2);
                    if (qName2 != null && qName2.uri == qName.uri) {
                        qName.prefix = qName2.prefix;
                        return;
                    }
                }
                String prefix = this.fNamespaceContext.getPrefix(addSymbol);
                if (prefix == "") {
                    if (i == 1) {
                        return;
                    }
                    if (i == 10) {
                        prefix = getAttrPrefix(addSymbol);
                        z = true;
                        if (prefix != null) {
                            StringBuffer stringBuffer = new StringBuffer("zdef");
                            for (int i3 = 0; i3 < 1; i3++) {
                                stringBuffer.append(this.fPrefixGen.nextInt());
                            }
                            str = this.fSymbolTable.addSymbol(stringBuffer.toString());
                        } else {
                            str = this.fSymbolTable.addSymbol(prefix);
                        }
                        if (prefix == null) {
                            if (z) {
                                addAttrNamespace(str, addSymbol);
                            } else {
                                QName qName3 = new QName();
                                qName3.setValues(str, "xmlns", null, addSymbol);
                                this.fNamespaceDecls.add(qName3);
                                this.fInternalNamespaceContext.declarePrefix(this.fSymbolTable.addSymbol(str), addSymbol);
                            }
                        }
                        str2 = str;
                    }
                }
                z = false;
                if (prefix != null) {
                }
                if (prefix == null) {
                }
                str2 = str;
            } else {
                return;
            }
        }
        qName.prefix = str2;
    }

    private String getAttrPrefix(String str) {
        HashMap hashMap = this.fAttrNamespace;
        if (hashMap != null) {
            return (String) hashMap.get(str);
        }
        return null;
    }

    private void addAttrNamespace(String str, String str2) {
        if (this.fAttrNamespace == null) {
            this.fAttrNamespace = new HashMap();
        }
        this.fAttrNamespace.put(str, str2);
    }

    private boolean isDefaultNamespace(String str) {
        return str == this.fInternalNamespaceContext.getURI(this.DEFAULT_PREFIX);
    }

    private boolean checkUserNamespaceContext(String str, String str2) {
        String namespaceURI;
        return (this.fNamespaceContext.userContext == null || (namespaceURI = this.fNamespaceContext.userContext.getNamespaceURI(str)) == null || !namespaceURI.equals(str2)) ? false : true;
    }

    /* access modifiers changed from: protected */
    public void repair() {
        ElementState peek = this.fElementStack.peek();
        removeDuplicateDecls();
        for (int i = 0; i < this.fAttributeCache.size(); i++) {
            Attribute attribute = (Attribute) this.fAttributeCache.get(i);
            if ((attribute.prefix != null && !attribute.prefix.equals("")) || (attribute.uri != null && !attribute.uri.equals(""))) {
                correctPrefix(peek, attribute);
            }
        }
        if (!isDeclared(peek) && peek.prefix != null && peek.uri != null && !peek.prefix.equals("") && !peek.uri.equals("")) {
            this.fNamespaceDecls.add(peek);
        }
        int i2 = 0;
        while (i2 < this.fAttributeCache.size()) {
            Attribute attribute2 = (Attribute) this.fAttributeCache.get(i2);
            i2++;
            for (int i3 = i2; i3 < this.fAttributeCache.size(); i3++) {
                Attribute attribute3 = (Attribute) this.fAttributeCache.get(i3);
                if (!"".equals(attribute2.prefix) && !"".equals(attribute3.prefix)) {
                    correctPrefix(attribute2, attribute3);
                }
            }
        }
        repairNamespaceDecl(peek);
        for (int i4 = 0; i4 < this.fAttributeCache.size(); i4++) {
            Attribute attribute4 = (Attribute) this.fAttributeCache.get(i4);
            if (attribute4.prefix != null && attribute4.prefix.equals("") && attribute4.uri != null && attribute4.uri.equals("")) {
                repairNamespaceDecl(attribute4);
            }
        }
        for (int i5 = 0; i5 < this.fNamespaceDecls.size(); i5++) {
            QName qName = (QName) this.fNamespaceDecls.get(i5);
            if (qName != null) {
                this.fInternalNamespaceContext.declarePrefix(qName.prefix, qName.uri);
            }
        }
        for (int i6 = 0; i6 < this.fAttributeCache.size(); i6++) {
            correctPrefix((Attribute) this.fAttributeCache.get(i6), 10);
        }
    }

    /* access modifiers changed from: package-private */
    public void correctPrefix(QName qName, QName qName2) {
        checkForNull(qName);
        checkForNull(qName2);
        if (qName.prefix.equals(qName2.prefix) && !qName.uri.equals(qName2.uri)) {
            String prefix = this.fNamespaceContext.getPrefix(qName2.uri);
            if (prefix != null) {
                qName2.prefix = this.fSymbolTable.addSymbol(prefix);
                return;
            }
            for (int i = 0; i < this.fNamespaceDecls.size(); i++) {
                QName qName3 = (QName) this.fNamespaceDecls.get(i);
                if (qName3 != null && qName3.uri == qName2.uri) {
                    qName2.prefix = qName3.prefix;
                    return;
                }
            }
            StringBuffer stringBuffer = new StringBuffer("zdef");
            for (int i2 = 0; i2 < 1; i2++) {
                stringBuffer.append(this.fPrefixGen.nextInt());
            }
            String addSymbol = this.fSymbolTable.addSymbol(stringBuffer.toString());
            qName2.prefix = addSymbol;
            QName qName4 = new QName();
            qName4.setValues(addSymbol, "xmlns", null, qName2.uri);
            this.fNamespaceDecls.add(qName4);
        }
    }

    /* access modifiers changed from: package-private */
    public void checkForNull(QName qName) {
        if (qName.prefix == null) {
            qName.prefix = "";
        }
        if (qName.uri == null) {
            qName.uri = "";
        }
    }

    /* access modifiers changed from: package-private */
    public void removeDuplicateDecls() {
        for (int i = 0; i < this.fNamespaceDecls.size(); i++) {
            QName qName = (QName) this.fNamespaceDecls.get(i);
            if (qName != null) {
                for (int i2 = i + 1; i2 < this.fNamespaceDecls.size(); i2++) {
                    QName qName2 = (QName) this.fNamespaceDecls.get(i2);
                    if (qName2 != null && qName.prefix.equals(qName2.prefix) && qName.uri.equals(qName2.uri)) {
                        this.fNamespaceDecls.remove(i2);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void repairNamespaceDecl(QName qName) {
        String namespaceURI;
        for (int i = 0; i < this.fNamespaceDecls.size(); i++) {
            QName qName2 = (QName) this.fNamespaceDecls.get(i);
            if (!(qName2 == null || qName.prefix == null || !qName.prefix.equals(qName2.prefix) || qName.uri.equals(qName2.uri) || (namespaceURI = this.fNamespaceContext.getNamespaceURI(qName.prefix)) == null)) {
                if (namespaceURI.equals(qName.uri)) {
                    this.fNamespaceDecls.set(i, null);
                } else {
                    qName2.uri = qName.uri;
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isDeclared(QName qName) {
        for (int i = 0; i < this.fNamespaceDecls.size(); i++) {
            QName qName2 = (QName) this.fNamespaceDecls.get(i);
            if (qName.prefix != null && qName.prefix == qName2.prefix && qName2.uri == qName.uri) {
                return true;
            }
        }
        return (qName.uri == null || this.fNamespaceContext.getPrefix(qName.uri) == null) ? false : true;
    }

    /* access modifiers changed from: protected */
    public class ElementStack {
        protected short fDepth;
        protected ElementState[] fElements = new ElementState[10];

        public ElementStack() {
            int i = 0;
            while (true) {
                ElementState[] elementStateArr = this.fElements;
                if (i < elementStateArr.length) {
                    elementStateArr[i] = new ElementState();
                    i++;
                } else {
                    return;
                }
            }
        }

        public ElementState push(ElementState elementState) {
            short s = this.fDepth;
            ElementState[] elementStateArr = this.fElements;
            if (s == elementStateArr.length) {
                ElementState[] elementStateArr2 = new ElementState[(elementStateArr.length * 2)];
                System.arraycopy(elementStateArr, 0, elementStateArr2, 0, s);
                this.fElements = elementStateArr2;
                int i = this.fDepth;
                while (true) {
                    ElementState[] elementStateArr3 = this.fElements;
                    if (i >= elementStateArr3.length) {
                        break;
                    }
                    elementStateArr3[i] = new ElementState();
                    i++;
                }
            }
            this.fElements[this.fDepth].setValues(elementState);
            ElementState[] elementStateArr4 = this.fElements;
            short s2 = this.fDepth;
            this.fDepth = (short) (s2 + 1);
            return elementStateArr4[s2];
        }

        public ElementState push(String str, String str2, String str3, String str4, boolean z) {
            short s = this.fDepth;
            ElementState[] elementStateArr = this.fElements;
            if (s == elementStateArr.length) {
                ElementState[] elementStateArr2 = new ElementState[(elementStateArr.length * 2)];
                System.arraycopy(elementStateArr, 0, elementStateArr2, 0, s);
                this.fElements = elementStateArr2;
                int i = this.fDepth;
                while (true) {
                    ElementState[] elementStateArr3 = this.fElements;
                    if (i >= elementStateArr3.length) {
                        break;
                    }
                    elementStateArr3[i] = new ElementState();
                    i++;
                }
            }
            this.fElements[this.fDepth].setValues(str, str2, str3, str4, z);
            ElementState[] elementStateArr4 = this.fElements;
            short s2 = this.fDepth;
            this.fDepth = (short) (s2 + 1);
            return elementStateArr4[s2];
        }

        public ElementState pop() {
            ElementState[] elementStateArr = this.fElements;
            short s = (short) (this.fDepth - 1);
            this.fDepth = s;
            return elementStateArr[s];
        }

        public void clear() {
            this.fDepth = 0;
        }

        public ElementState peek() {
            return this.fElements[this.fDepth - 1];
        }

        public boolean empty() {
            return this.fDepth <= 0;
        }
    }

    /* access modifiers changed from: package-private */
    public class ElementState extends QName {
        public boolean isEmpty = false;

        public ElementState() {
        }

        public ElementState(String str, String str2, String str3, String str4) {
            super(str, str2, str3, str4);
        }

        public void setValues(String str, String str2, String str3, String str4, boolean z) {
            super.setValues(str, str2, str3, str4);
            this.isEmpty = z;
        }
    }

    /* access modifiers changed from: package-private */
    public class Attribute extends QName {
        String value;

        Attribute(String str) {
            this.value = str;
        }
    }

    /* access modifiers changed from: package-private */
    public class NamespaceContextImpl implements NamespaceContext {
        NamespaceSupport internalContext = null;
        NamespaceContext userContext = null;

        NamespaceContextImpl() {
        }

        public String getNamespaceURI(String str) {
            String uri;
            if (str != null) {
                str = XMLStreamWriterImpl.this.fSymbolTable.addSymbol(str);
            }
            NamespaceSupport namespaceSupport = this.internalContext;
            if (namespaceSupport != null && (uri = namespaceSupport.getURI(str)) != null) {
                return uri;
            }
            NamespaceContext namespaceContext = this.userContext;
            if (namespaceContext != null) {
                return namespaceContext.getNamespaceURI(str);
            }
            return null;
        }

        public String getPrefix(String str) {
            String prefix;
            if (str != null) {
                str = XMLStreamWriterImpl.this.fSymbolTable.addSymbol(str);
            }
            NamespaceSupport namespaceSupport = this.internalContext;
            if (namespaceSupport != null && (prefix = namespaceSupport.getPrefix(str)) != null) {
                return prefix;
            }
            NamespaceContext namespaceContext = this.userContext;
            if (namespaceContext != null) {
                return namespaceContext.getPrefix(str);
            }
            return null;
        }

        public Iterator getPrefixes(String str) {
            if (str != null) {
                str = XMLStreamWriterImpl.this.fSymbolTable.addSymbol(str);
            }
            NamespaceContext namespaceContext = this.userContext;
            Vector vector = null;
            Iterator prefixes = namespaceContext != null ? namespaceContext.getPrefixes(str) : null;
            NamespaceSupport namespaceSupport = this.internalContext;
            if (namespaceSupport != null) {
                vector = namespaceSupport.getPrefixes(str);
            }
            if (vector == null && prefixes != null) {
                return prefixes;
            }
            if (vector != null && prefixes == null) {
                return new ReadOnlyIterator(vector.iterator());
            }
            if (vector == null || prefixes == null) {
                return XMLStreamWriterImpl.this.fReadOnlyIterator;
            }
            while (prefixes.hasNext()) {
                String str2 = (String) prefixes.next();
                if (str2 != null) {
                    str2 = XMLStreamWriterImpl.this.fSymbolTable.addSymbol(str2);
                }
                if (!vector.contains(str2)) {
                    vector.add(str2);
                }
            }
            return new ReadOnlyIterator(vector.iterator());
        }
    }

    @Override // java.util.AbstractMap, java.util.Map
    public boolean containsKey(Object obj) {
        return obj.equals(OUTPUTSTREAM_PROPERTY);
    }

    @Override // java.util.AbstractMap, java.util.Map
    public Object get(Object obj) {
        if (obj.equals(OUTPUTSTREAM_PROPERTY)) {
            return this.fOutputStream;
        }
        return null;
    }

    @Override // java.util.AbstractMap, java.util.Map
    public Set entrySet() {
        throw new UnsupportedOperationException();
    }

    @Override // java.util.AbstractMap, java.lang.Object
    public String toString() {
        return getClass().getName() + "@" + Integer.toHexString(hashCode());
    }

    @Override // java.util.AbstractMap, java.util.Map, java.lang.Object
    public int hashCode() {
        return this.fElementStack.hashCode();
    }
}

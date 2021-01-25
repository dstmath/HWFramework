package ohos.com.sun.org.apache.xml.internal.serialize;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import ohos.com.sun.org.apache.xalan.internal.templates.Constants;
import ohos.com.sun.org.apache.xerces.internal.dom.DOMErrorImpl;
import ohos.com.sun.org.apache.xerces.internal.dom.DOMLocatorImpl;
import ohos.com.sun.org.apache.xerces.internal.dom.DOMMessageFormatter;
import ohos.com.sun.org.apache.xerces.internal.util.XMLChar;
import ohos.com.sun.org.apache.xml.internal.serializer.SerializerConstants;
import ohos.global.icu.impl.Normalizer2Impl;
import ohos.global.icu.impl.UCharacterProperty;
import ohos.global.icu.text.UTF16;
import ohos.org.w3c.dom.DOMError;
import ohos.org.w3c.dom.DOMErrorHandler;
import ohos.org.w3c.dom.Document;
import ohos.org.w3c.dom.DocumentFragment;
import ohos.org.w3c.dom.DocumentType;
import ohos.org.w3c.dom.Element;
import ohos.org.w3c.dom.Node;
import ohos.org.w3c.dom.ls.LSException;
import ohos.org.w3c.dom.ls.LSSerializerFilter;
import ohos.org.xml.sax.ContentHandler;
import ohos.org.xml.sax.DTDHandler;
import ohos.org.xml.sax.DocumentHandler;
import ohos.org.xml.sax.Locator;
import ohos.org.xml.sax.SAXException;
import ohos.org.xml.sax.ext.DeclHandler;
import ohos.org.xml.sax.ext.LexicalHandler;

public abstract class BaseMarkupSerializer implements ContentHandler, DocumentHandler, LexicalHandler, DTDHandler, DeclHandler, DOMSerializer, Serializer {
    protected String _docTypePublicId;
    protected String _docTypeSystemId;
    private int _elementStateCount;
    private ElementState[] _elementStates = new ElementState[10];
    protected EncodingInfo _encodingInfo;
    protected OutputFormat _format;
    protected boolean _indenting;
    private OutputStream _output;
    private Vector _preRoot;
    protected Map<String, String> _prefixes;
    private boolean _prepared;
    protected Printer _printer;
    protected boolean _started;
    private Writer _writer;
    protected Node fCurrentNode = null;
    protected final DOMErrorImpl fDOMError = new DOMErrorImpl();
    protected DOMErrorHandler fDOMErrorHandler;
    protected LSSerializerFilter fDOMFilter;
    protected final StringBuffer fStrBuffer = new StringBuffer(40);
    protected short features = -1;

    /* access modifiers changed from: protected */
    public void checkUnboundNamespacePrefixedNode(Node node) throws IOException {
    }

    public void endDTD() {
    }

    public void endEntity(String str) {
    }

    public void endPrefixMapping(String str) throws SAXException {
    }

    /* access modifiers changed from: protected */
    public abstract String getEntityRef(int i);

    /* access modifiers changed from: protected */
    public abstract void serializeElement(Element element) throws IOException;

    public void setDocumentLocator(Locator locator) {
    }

    public void startEntity(String str) {
    }

    protected BaseMarkupSerializer(OutputFormat outputFormat) {
        int i = 0;
        while (true) {
            ElementState[] elementStateArr = this._elementStates;
            if (i < elementStateArr.length) {
                elementStateArr[i] = new ElementState();
                i++;
            } else {
                this._format = outputFormat;
                return;
            }
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serialize.Serializer
    public DocumentHandler asDocumentHandler() throws IOException {
        prepare();
        return this;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serialize.Serializer
    public ContentHandler asContentHandler() throws IOException {
        prepare();
        return this;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serialize.Serializer
    public DOMSerializer asDOMSerializer() throws IOException {
        prepare();
        return this;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serialize.Serializer
    public void setOutputByteStream(OutputStream outputStream) {
        if (outputStream != null) {
            this._output = outputStream;
            this._writer = null;
            reset();
            return;
        }
        throw new NullPointerException(DOMMessageFormatter.formatMessage(DOMMessageFormatter.SERIALIZER_DOMAIN, "ArgumentIsNull", new Object[]{Constants.ELEMNAME_OUTPUT_STRING}));
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serialize.Serializer
    public void setOutputCharStream(Writer writer) {
        if (writer != null) {
            this._writer = writer;
            this._output = null;
            reset();
            return;
        }
        throw new NullPointerException(DOMMessageFormatter.formatMessage(DOMMessageFormatter.SERIALIZER_DOMAIN, "ArgumentIsNull", new Object[]{"writer"}));
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serialize.Serializer
    public void setOutputFormat(OutputFormat outputFormat) {
        if (outputFormat != null) {
            this._format = outputFormat;
            reset();
            return;
        }
        throw new NullPointerException(DOMMessageFormatter.formatMessage(DOMMessageFormatter.SERIALIZER_DOMAIN, "ArgumentIsNull", new Object[]{"format"}));
    }

    public boolean reset() {
        if (this._elementStateCount <= 1) {
            this._prepared = false;
            this.fCurrentNode = null;
            this.fStrBuffer.setLength(0);
            return true;
        }
        throw new IllegalStateException(DOMMessageFormatter.formatMessage(DOMMessageFormatter.SERIALIZER_DOMAIN, "ResetInMiddle", null));
    }

    /* access modifiers changed from: protected */
    public void prepare() throws IOException {
        if (!this._prepared) {
            if (this._writer == null && this._output == null) {
                throw new IOException(DOMMessageFormatter.formatMessage(DOMMessageFormatter.SERIALIZER_DOMAIN, "NoWriterSupplied", null));
            }
            this._encodingInfo = this._format.getEncodingInfo();
            OutputStream outputStream = this._output;
            if (outputStream != null) {
                this._writer = this._encodingInfo.getWriter(outputStream);
            }
            if (this._format.getIndenting()) {
                this._indenting = true;
                this._printer = new IndentPrinter(this._writer, this._format);
            } else {
                this._indenting = false;
                this._printer = new Printer(this._writer, this._format);
            }
            this._elementStateCount = 0;
            ElementState elementState = this._elementStates[0];
            elementState.namespaceURI = null;
            elementState.localName = null;
            elementState.rawName = null;
            elementState.preserveSpace = this._format.getPreserveSpace();
            elementState.empty = true;
            elementState.afterElement = false;
            elementState.afterComment = false;
            elementState.inCData = false;
            elementState.doCData = false;
            elementState.prefixes = null;
            this._docTypePublicId = this._format.getDoctypePublic();
            this._docTypeSystemId = this._format.getDoctypeSystem();
            this._started = false;
            this._prepared = true;
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serialize.DOMSerializer
    public void serialize(Element element) throws IOException {
        reset();
        prepare();
        serializeNode(element);
        this._printer.flush();
        if (this._printer.getException() != null) {
            throw this._printer.getException();
        }
    }

    public void serialize(Node node) throws IOException {
        reset();
        prepare();
        serializeNode(node);
        serializePreRoot();
        this._printer.flush();
        if (this._printer.getException() != null) {
            throw this._printer.getException();
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serialize.DOMSerializer
    public void serialize(DocumentFragment documentFragment) throws IOException {
        reset();
        prepare();
        serializeNode(documentFragment);
        this._printer.flush();
        if (this._printer.getException() != null) {
            throw this._printer.getException();
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serialize.DOMSerializer
    public void serialize(Document document) throws IOException {
        reset();
        prepare();
        serializeNode(document);
        serializePreRoot();
        this._printer.flush();
        if (this._printer.getException() != null) {
            throw this._printer.getException();
        }
    }

    public void startDocument() throws SAXException {
        try {
            prepare();
        } catch (IOException e) {
            throw new SAXException(e.toString());
        }
    }

    public void characters(char[] cArr, int i, int i2) throws SAXException {
        int i3;
        try {
            ElementState content = content();
            if (!content.inCData) {
                if (!content.doCData) {
                    if (content.preserveSpace) {
                        int nextIndent = this._printer.getNextIndent();
                        this._printer.setNextIndent(0);
                        printText(cArr, i, i2, true, content.unescaped);
                        this._printer.setNextIndent(nextIndent);
                        return;
                    }
                    printText(cArr, i, i2, false, content.unescaped);
                    return;
                }
            }
            if (!content.inCData) {
                this._printer.printText("<![CDATA[");
                content.inCData = true;
            }
            int nextIndent2 = this._printer.getNextIndent();
            this._printer.setNextIndent(0);
            int i4 = i2 + i;
            while (i < i4) {
                char c = cArr[i];
                if (c == ']' && (i3 = i + 2) < i4 && cArr[i + 1] == ']' && cArr[i3] == '>') {
                    this._printer.printText(SerializerConstants.CDATA_CONTINUE);
                    i = i3;
                } else if (!XMLChar.isValid(c)) {
                    i++;
                    if (i < i4) {
                        surrogates(c, cArr[i]);
                    } else {
                        fatalError("The character '" + c + "' is an invalid XML character");
                    }
                } else {
                    if (!((c >= ' ' && this._encodingInfo.isPrintable(c) && c != 247) || c == '\n' || c == '\r')) {
                        if (c != '\t') {
                            this._printer.printText("]]>&#x");
                            this._printer.printText(Integer.toHexString(c));
                            this._printer.printText(";<![CDATA[");
                        }
                    }
                    this._printer.printText(c);
                }
                i++;
            }
            this._printer.setNextIndent(nextIndent2);
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    public void ignorableWhitespace(char[] cArr, int i, int i2) throws SAXException {
        try {
            content();
            if (this._indenting) {
                this._printer.setThisIndent(0);
                while (true) {
                    int i3 = i2 - 1;
                    if (i2 > 0) {
                        this._printer.printText(cArr[i]);
                        i++;
                        i2 = i3;
                    } else {
                        return;
                    }
                }
            }
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    public final void processingInstruction(String str, String str2) throws SAXException {
        try {
            processingInstructionIO(str, str2);
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    public void processingInstructionIO(String str, String str2) throws IOException {
        ElementState content = content();
        int indexOf = str.indexOf("?>");
        if (indexOf >= 0) {
            StringBuffer stringBuffer = this.fStrBuffer;
            stringBuffer.append("<?");
            stringBuffer.append(str.substring(0, indexOf));
        } else {
            StringBuffer stringBuffer2 = this.fStrBuffer;
            stringBuffer2.append("<?");
            stringBuffer2.append(str);
        }
        if (str2 != null) {
            this.fStrBuffer.append(' ');
            int indexOf2 = str2.indexOf("?>");
            if (indexOf2 >= 0) {
                this.fStrBuffer.append(str2.substring(0, indexOf2));
            } else {
                this.fStrBuffer.append(str2);
            }
        }
        this.fStrBuffer.append("?>");
        if (isDocumentState()) {
            if (this._preRoot == null) {
                this._preRoot = new Vector();
            }
            this._preRoot.addElement(this.fStrBuffer.toString());
        } else {
            this._printer.indent();
            printText(this.fStrBuffer.toString(), true, true);
            this._printer.unindent();
            if (this._indenting) {
                content.afterElement = true;
            }
        }
        this.fStrBuffer.setLength(0);
    }

    public void comment(char[] cArr, int i, int i2) throws SAXException {
        try {
            comment(new String(cArr, i, i2));
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    public void comment(String str) throws IOException {
        if (!this._format.getOmitComments()) {
            ElementState content = content();
            int indexOf = str.indexOf("-->");
            if (indexOf >= 0) {
                StringBuffer stringBuffer = this.fStrBuffer;
                stringBuffer.append("<!--");
                stringBuffer.append(str.substring(0, indexOf));
                stringBuffer.append("-->");
            } else {
                StringBuffer stringBuffer2 = this.fStrBuffer;
                stringBuffer2.append("<!--");
                stringBuffer2.append(str);
                stringBuffer2.append("-->");
            }
            if (isDocumentState()) {
                if (this._preRoot == null) {
                    this._preRoot = new Vector();
                }
                this._preRoot.addElement(this.fStrBuffer.toString());
            } else {
                if (this._indenting && !content.preserveSpace) {
                    this._printer.breakLine();
                }
                this._printer.indent();
                printText(this.fStrBuffer.toString(), true, true);
                this._printer.unindent();
                if (this._indenting) {
                    content.afterElement = true;
                }
            }
            this.fStrBuffer.setLength(0);
            content.afterComment = true;
            content.afterElement = false;
        }
    }

    public void startCDATA() {
        getElementState().doCData = true;
    }

    public void endCDATA() {
        getElementState().doCData = false;
    }

    public void startNonEscaping() {
        getElementState().unescaped = true;
    }

    public void endNonEscaping() {
        getElementState().unescaped = false;
    }

    public void startPreserving() {
        getElementState().preserveSpace = true;
    }

    public void endPreserving() {
        getElementState().preserveSpace = false;
    }

    public void endDocument() throws SAXException {
        try {
            serializePreRoot();
            this._printer.flush();
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    public void skippedEntity(String str) throws SAXException {
        try {
            endCDATA();
            content();
            this._printer.printText('&');
            this._printer.printText(str);
            this._printer.printText(';');
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    public void startPrefixMapping(String str, String str2) throws SAXException {
        if (this._prefixes == null) {
            this._prefixes = new HashMap();
        }
        Map<String, String> map = this._prefixes;
        if (str == null) {
            str = "";
        }
        map.put(str2, str);
    }

    public final void startDTD(String str, String str2, String str3) throws SAXException {
        try {
            this._printer.enterDTD();
            this._docTypePublicId = str2;
            this._docTypeSystemId = str3;
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    public void elementDecl(String str, String str2) throws SAXException {
        try {
            this._printer.enterDTD();
            this._printer.printText("<!ELEMENT ");
            this._printer.printText(str);
            this._printer.printText(' ');
            this._printer.printText(str2);
            this._printer.printText('>');
            if (this._indenting) {
                this._printer.breakLine();
            }
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    public void attributeDecl(String str, String str2, String str3, String str4, String str5) throws SAXException {
        try {
            this._printer.enterDTD();
            this._printer.printText("<!ATTLIST ");
            this._printer.printText(str);
            this._printer.printText(' ');
            this._printer.printText(str2);
            this._printer.printText(' ');
            this._printer.printText(str3);
            if (str4 != null) {
                this._printer.printText(' ');
                this._printer.printText(str4);
            }
            if (str5 != null) {
                this._printer.printText(" \"");
                printEscaped(str5);
                this._printer.printText('\"');
            }
            this._printer.printText('>');
            if (this._indenting) {
                this._printer.breakLine();
            }
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    public void internalEntityDecl(String str, String str2) throws SAXException {
        try {
            this._printer.enterDTD();
            this._printer.printText("<!ENTITY ");
            this._printer.printText(str);
            this._printer.printText(" \"");
            printEscaped(str2);
            this._printer.printText("\">");
            if (this._indenting) {
                this._printer.breakLine();
            }
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    public void externalEntityDecl(String str, String str2, String str3) throws SAXException {
        try {
            this._printer.enterDTD();
            unparsedEntityDecl(str, str2, str3, null);
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    public void unparsedEntityDecl(String str, String str2, String str3, String str4) throws SAXException {
        try {
            this._printer.enterDTD();
            if (str2 == null) {
                this._printer.printText("<!ENTITY ");
                this._printer.printText(str);
                this._printer.printText(" SYSTEM ");
                printDoctypeURL(str3);
            } else {
                this._printer.printText("<!ENTITY ");
                this._printer.printText(str);
                this._printer.printText(" PUBLIC ");
                printDoctypeURL(str2);
                this._printer.printText(' ');
                printDoctypeURL(str3);
            }
            if (str4 != null) {
                this._printer.printText(" NDATA ");
                this._printer.printText(str4);
            }
            this._printer.printText('>');
            if (this._indenting) {
                this._printer.breakLine();
            }
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    public void notationDecl(String str, String str2, String str3) throws SAXException {
        try {
            this._printer.enterDTD();
            if (str2 != null) {
                this._printer.printText("<!NOTATION ");
                this._printer.printText(str);
                this._printer.printText(" PUBLIC ");
                printDoctypeURL(str2);
                if (str3 != null) {
                    this._printer.printText(' ');
                    printDoctypeURL(str3);
                }
            } else {
                this._printer.printText("<!NOTATION ");
                this._printer.printText(str);
                this._printer.printText(" SYSTEM ");
                printDoctypeURL(str3);
            }
            this._printer.printText('>');
            if (this._indenting) {
                this._printer.breakLine();
            }
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x0093 A[Catch:{ Exception -> 0x009d }] */
    public void serializeNode(Node node) throws IOException {
        short acceptNode;
        short acceptNode2;
        String nodeValue;
        short acceptNode3;
        String str;
        Method method;
        this.fCurrentNode = node;
        short nodeType = node.getNodeType();
        if (nodeType != 1) {
            if (nodeType != 11) {
                if (nodeType == 3) {
                    String nodeValue2 = node.getNodeValue();
                    if (nodeValue2 != null) {
                        LSSerializerFilter lSSerializerFilter = this.fDOMFilter;
                        if (lSSerializerFilter != null && (lSSerializerFilter.getWhatToShow() & 4) != 0) {
                            short acceptNode4 = this.fDOMFilter.acceptNode(node);
                            if (!(acceptNode4 == 2 || acceptNode4 == 3)) {
                                characters(nodeValue2);
                                return;
                            }
                            return;
                        } else if (!this._indenting || getElementState().preserveSpace || nodeValue2.replace('\n', ' ').trim().length() != 0) {
                            characters(nodeValue2);
                            return;
                        } else {
                            return;
                        }
                    } else {
                        return;
                    }
                } else if (nodeType == 4) {
                    String nodeValue3 = node.getNodeValue();
                    if ((this.features & 8) == 0) {
                        characters(nodeValue3);
                        return;
                    } else if (nodeValue3 != null) {
                        LSSerializerFilter lSSerializerFilter2 = this.fDOMFilter;
                        if (lSSerializerFilter2 == null || (lSSerializerFilter2.getWhatToShow() & 8) == 0 || !((acceptNode = this.fDOMFilter.acceptNode(node)) == 2 || acceptNode == 3)) {
                            startCDATA();
                            characters(nodeValue3);
                            endCDATA();
                            return;
                        }
                        return;
                    } else {
                        return;
                    }
                } else if (nodeType == 5) {
                    endCDATA();
                    content();
                    if ((this.features & 4) != 0 || node.getFirstChild() == null) {
                        LSSerializerFilter lSSerializerFilter3 = this.fDOMFilter;
                        if (!(lSSerializerFilter3 == null || (lSSerializerFilter3.getWhatToShow() & 16) == 0)) {
                            short acceptNode5 = this.fDOMFilter.acceptNode(node);
                            if (acceptNode5 == 2) {
                                return;
                            }
                            if (acceptNode5 == 3) {
                                for (Node firstChild = node.getFirstChild(); firstChild != null; firstChild = firstChild.getNextSibling()) {
                                    serializeNode(firstChild);
                                }
                                return;
                            }
                        }
                        checkUnboundNamespacePrefixedNode(node);
                        this._printer.printText("&");
                        this._printer.printText(node.getNodeName());
                        this._printer.printText(";");
                        return;
                    }
                    for (Node firstChild2 = node.getFirstChild(); firstChild2 != null; firstChild2 = firstChild2.getNextSibling()) {
                        serializeNode(firstChild2);
                    }
                    return;
                } else if (nodeType == 7) {
                    LSSerializerFilter lSSerializerFilter4 = this.fDOMFilter;
                    if (lSSerializerFilter4 == null || (lSSerializerFilter4.getWhatToShow() & 64) == 0 || !((acceptNode2 = this.fDOMFilter.acceptNode(node)) == 2 || acceptNode2 == 3)) {
                        processingInstructionIO(node.getNodeName(), node.getNodeValue());
                        return;
                    }
                    return;
                } else if (nodeType != 8) {
                    if (nodeType == 9) {
                        serializeDocument();
                        Document document = (Document) node;
                        DocumentType doctype = document.getDoctype();
                        if (doctype != null) {
                            document.getImplementation();
                            try {
                                this._printer.enterDTD();
                                this._docTypePublicId = doctype.getPublicId();
                                this._docTypeSystemId = doctype.getSystemId();
                                String internalSubset = doctype.getInternalSubset();
                                if (internalSubset != null && internalSubset.length() > 0) {
                                    this._printer.printText(internalSubset);
                                }
                                endDTD();
                            } catch (NoSuchMethodError unused) {
                                Class<?> cls = doctype.getClass();
                                String str2 = null;
                                try {
                                    Method method2 = cls.getMethod("getPublicId", null);
                                    if (method2.getReturnType().equals(String.class)) {
                                        str = (String) method2.invoke(doctype, null);
                                        method = cls.getMethod("getSystemId", null);
                                        if (method.getReturnType().equals(String.class)) {
                                            str2 = (String) method.invoke(doctype, null);
                                        }
                                        this._printer.enterDTD();
                                        this._docTypePublicId = str;
                                        this._docTypeSystemId = str2;
                                        endDTD();
                                    }
                                } catch (Exception unused2) {
                                }
                                str = null;
                                try {
                                    method = cls.getMethod("getSystemId", null);
                                    if (method.getReturnType().equals(String.class)) {
                                    }
                                } catch (Exception unused3) {
                                }
                                this._printer.enterDTD();
                                this._docTypePublicId = str;
                                this._docTypeSystemId = str2;
                                endDTD();
                            }
                            serializeDTD(doctype.getName());
                        }
                        this._started = true;
                    } else {
                        return;
                    }
                } else if (!this._format.getOmitComments() && (nodeValue = node.getNodeValue()) != null) {
                    LSSerializerFilter lSSerializerFilter5 = this.fDOMFilter;
                    if (lSSerializerFilter5 == null || (lSSerializerFilter5.getWhatToShow() & 128) == 0 || !((acceptNode3 = this.fDOMFilter.acceptNode(node)) == 2 || acceptNode3 == 3)) {
                        comment(nodeValue);
                        return;
                    }
                    return;
                } else {
                    return;
                }
            }
            for (Node firstChild3 = node.getFirstChild(); firstChild3 != null; firstChild3 = firstChild3.getNextSibling()) {
                serializeNode(firstChild3);
            }
            return;
        }
        LSSerializerFilter lSSerializerFilter6 = this.fDOMFilter;
        if (!(lSSerializerFilter6 == null || (lSSerializerFilter6.getWhatToShow() & 1) == 0)) {
            short acceptNode6 = this.fDOMFilter.acceptNode(node);
            if (acceptNode6 == 2) {
                return;
            }
            if (acceptNode6 == 3) {
                for (Node firstChild4 = node.getFirstChild(); firstChild4 != null; firstChild4 = firstChild4.getNextSibling()) {
                    serializeNode(firstChild4);
                }
                return;
            }
        }
        serializeElement((Element) node);
    }

    /* access modifiers changed from: protected */
    public void serializeDocument() throws IOException {
        this._printer.leaveDTD();
        if (!this._started && !this._format.getOmitXMLDeclaration()) {
            StringBuffer stringBuffer = new StringBuffer("<?xml version=\"");
            if (this._format.getVersion() != null) {
                stringBuffer.append(this._format.getVersion());
            } else {
                stringBuffer.append("1.0");
            }
            stringBuffer.append('\"');
            String encoding = this._format.getEncoding();
            if (encoding != null) {
                stringBuffer.append(" encoding=\"");
                stringBuffer.append(encoding);
                stringBuffer.append('\"');
            }
            if (this._format.getStandalone() && this._docTypeSystemId == null && this._docTypePublicId == null) {
                stringBuffer.append(" standalone=\"yes\"");
            }
            stringBuffer.append("?>");
            this._printer.printText(stringBuffer);
            this._printer.breakLine();
        }
        serializePreRoot();
    }

    /* access modifiers changed from: protected */
    public void serializeDTD(String str) throws IOException {
        String leaveDTD = this._printer.leaveDTD();
        if (this._format.getOmitDocumentType()) {
            return;
        }
        if (this._docTypeSystemId != null) {
            this._printer.printText("<!DOCTYPE ");
            this._printer.printText(str);
            if (this._docTypePublicId != null) {
                this._printer.printText(" PUBLIC ");
                printDoctypeURL(this._docTypePublicId);
                if (this._indenting) {
                    this._printer.breakLine();
                    for (int i = 0; i < str.length() + 18; i++) {
                        this._printer.printText(" ");
                    }
                } else {
                    this._printer.printText(" ");
                }
                printDoctypeURL(this._docTypeSystemId);
            } else {
                this._printer.printText(" SYSTEM ");
                printDoctypeURL(this._docTypeSystemId);
            }
            if (leaveDTD != null && leaveDTD.length() > 0) {
                this._printer.printText(" [");
                printText(leaveDTD, true, true);
                this._printer.printText(']');
            }
            this._printer.printText(">");
            this._printer.breakLine();
        } else if (leaveDTD != null && leaveDTD.length() > 0) {
            this._printer.printText("<!DOCTYPE ");
            this._printer.printText(str);
            this._printer.printText(" [");
            printText(leaveDTD, true, true);
            this._printer.printText("]>");
            this._printer.breakLine();
        }
    }

    /* access modifiers changed from: protected */
    public ElementState content() throws IOException {
        ElementState elementState = getElementState();
        if (!isDocumentState()) {
            if (elementState.inCData && !elementState.doCData) {
                this._printer.printText("]]>");
                elementState.inCData = false;
            }
            if (elementState.empty) {
                this._printer.printText('>');
                elementState.empty = false;
            }
            elementState.afterElement = false;
            elementState.afterComment = false;
        }
        return elementState;
    }

    /* access modifiers changed from: protected */
    public void characters(String str) throws IOException {
        ElementState content = content();
        if (content.inCData || content.doCData) {
            if (!content.inCData) {
                this._printer.printText("<![CDATA[");
                content.inCData = true;
            }
            int nextIndent = this._printer.getNextIndent();
            this._printer.setNextIndent(0);
            printCDATAText(str);
            this._printer.setNextIndent(nextIndent);
        } else if (content.preserveSpace) {
            int nextIndent2 = this._printer.getNextIndent();
            this._printer.setNextIndent(0);
            printText(str, true, content.unescaped);
            this._printer.setNextIndent(nextIndent2);
        } else {
            printText(str, false, content.unescaped);
        }
    }

    /* access modifiers changed from: protected */
    public void serializePreRoot() throws IOException {
        if (this._preRoot != null) {
            for (int i = 0; i < this._preRoot.size(); i++) {
                printText((String) this._preRoot.elementAt(i), true, true);
                if (this._indenting) {
                    this._printer.breakLine();
                }
            }
            this._preRoot.removeAllElements();
        }
    }

    /* access modifiers changed from: protected */
    public void printCDATAText(String str) throws IOException {
        int i;
        int length = str.length();
        int i2 = 0;
        while (i2 < length) {
            char charAt = str.charAt(i2);
            if (charAt == ']' && (i = i2 + 2) < length && str.charAt(i2 + 1) == ']' && str.charAt(i) == '>') {
                if (this.fDOMErrorHandler != null) {
                    if ((this.features & 16) == 0) {
                        String formatMessage = DOMMessageFormatter.formatMessage(DOMMessageFormatter.SERIALIZER_DOMAIN, "EndingCDATA", null);
                        if ((this.features & 2) == 0) {
                            modifyDOMError(formatMessage, 2, "cdata-section-not-splitted", this.fCurrentNode);
                            if (!this.fDOMErrorHandler.handleError(this.fDOMError)) {
                                throw new LSException(82, formatMessage);
                            }
                        } else {
                            modifyDOMError(formatMessage, 3, "wf-invalid-character", this.fCurrentNode);
                            this.fDOMErrorHandler.handleError(this.fDOMError);
                            throw new LSException(82, formatMessage);
                        }
                    } else {
                        modifyDOMError(DOMMessageFormatter.formatMessage(DOMMessageFormatter.SERIALIZER_DOMAIN, "SplittingCDATA", null), 1, null, this.fCurrentNode);
                        this.fDOMErrorHandler.handleError(this.fDOMError);
                    }
                }
                this._printer.printText(SerializerConstants.CDATA_CONTINUE);
                i2 = i;
            } else if (!XMLChar.isValid(charAt)) {
                i2++;
                if (i2 < length) {
                    surrogates(charAt, str.charAt(i2));
                } else {
                    fatalError("The character '" + charAt + "' is an invalid XML character");
                }
            } else if ((charAt >= ' ' && this._encodingInfo.isPrintable(charAt) && charAt != 247) || charAt == '\n' || charAt == '\r' || charAt == '\t') {
                this._printer.printText(charAt);
            } else {
                this._printer.printText("]]>&#x");
                this._printer.printText(Integer.toHexString(charAt));
                this._printer.printText(";<![CDATA[");
            }
            i2++;
        }
    }

    /* access modifiers changed from: protected */
    public void surrogates(int i, int i2) throws IOException {
        if (!XMLChar.isHighSurrogate(i)) {
            fatalError("The character '" + ((char) i) + "' is an invalid XML character");
        } else if (!XMLChar.isLowSurrogate(i2)) {
            fatalError("The character '" + ((char) i2) + "' is an invalid XML character");
        } else {
            int supplemental = XMLChar.supplemental((char) i, (char) i2);
            if (!XMLChar.isValid(supplemental)) {
                fatalError("The character '" + ((char) supplemental) + "' is an invalid XML character");
            } else if (content().inCData) {
                this._printer.printText("]]>&#x");
                this._printer.printText(Integer.toHexString(supplemental));
                this._printer.printText(";<![CDATA[");
            } else {
                printHex(supplemental);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void printText(char[] cArr, int i, int i2, boolean z, boolean z2) throws IOException {
        if (z) {
            while (true) {
                int i3 = i2 - 1;
                if (i2 > 0) {
                    char c = cArr[i];
                    i++;
                    if (c == '\n' || c == '\r' || z2) {
                        this._printer.printText(c);
                    } else {
                        printEscaped(c);
                    }
                    i2 = i3;
                } else {
                    return;
                }
            }
        } else {
            while (true) {
                int i4 = i2 - 1;
                if (i2 > 0) {
                    char c2 = cArr[i];
                    i++;
                    if (c2 == ' ' || c2 == '\f' || c2 == '\t' || c2 == '\n' || c2 == '\r') {
                        this._printer.printSpace();
                    } else if (z2) {
                        this._printer.printText(c2);
                    } else {
                        printEscaped(c2);
                    }
                    i2 = i4;
                } else {
                    return;
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void printText(String str, boolean z, boolean z2) throws IOException {
        int i = 0;
        if (z) {
            while (i < str.length()) {
                char charAt = str.charAt(i);
                if (charAt == '\n' || charAt == '\r' || z2) {
                    this._printer.printText(charAt);
                } else {
                    printEscaped(charAt);
                }
                i++;
            }
            return;
        }
        while (i < str.length()) {
            char charAt2 = str.charAt(i);
            if (charAt2 == ' ' || charAt2 == '\f' || charAt2 == '\t' || charAt2 == '\n' || charAt2 == '\r') {
                this._printer.printSpace();
            } else if (z2) {
                this._printer.printText(charAt2);
            } else {
                printEscaped(charAt2);
            }
            i++;
        }
    }

    /* access modifiers changed from: protected */
    public void printDoctypeURL(String str) throws IOException {
        this._printer.printText('\"');
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == '\"' || str.charAt(i) < ' ' || str.charAt(i) > 127) {
                this._printer.printText('%');
                this._printer.printText(Integer.toHexString(str.charAt(i)));
            } else {
                this._printer.printText(str.charAt(i));
            }
        }
        this._printer.printText('\"');
    }

    /* access modifiers changed from: protected */
    public void printEscaped(int i) throws IOException {
        String entityRef = getEntityRef(i);
        if (entityRef != null) {
            this._printer.printText('&');
            this._printer.printText(entityRef);
            this._printer.printText(';');
        } else if ((i < 32 || !this._encodingInfo.isPrintable((char) i) || i == 247) && i != 10 && i != 13 && i != 9) {
            printHex(i);
        } else if (i < 65536) {
            this._printer.printText((char) i);
        } else {
            int i2 = i - 65536;
            this._printer.printText((char) ((i2 >> 10) + 55296));
            this._printer.printText((char) ((i2 & UCharacterProperty.MAX_SCRIPT) + UTF16.TRAIL_SURROGATE_MIN_VALUE));
        }
    }

    /* access modifiers changed from: package-private */
    public final void printHex(int i) throws IOException {
        this._printer.printText("&#x");
        this._printer.printText(Integer.toHexString(i));
        this._printer.printText(';');
    }

    /* access modifiers changed from: protected */
    public void printEscaped(String str) throws IOException {
        int i;
        int i2 = 0;
        while (i2 < str.length()) {
            int charAt = str.charAt(i2);
            if ((charAt & Normalizer2Impl.MIN_NORMAL_MAYBE_YES) == 55296 && (i = i2 + 1) < str.length()) {
                char charAt2 = str.charAt(i);
                if ((64512 & charAt2) == 56320) {
                    charAt = ((((charAt - 55296) << 10) + 65536) + charAt2) - UTF16.TRAIL_SURROGATE_MIN_VALUE;
                    i2 = i;
                }
            }
            printEscaped(charAt);
            i2++;
        }
    }

    /* access modifiers changed from: protected */
    public ElementState getElementState() {
        return this._elementStates[this._elementStateCount];
    }

    /* access modifiers changed from: protected */
    public ElementState enterElementState(String str, String str2, String str3, boolean z) {
        ElementState[] elementStateArr;
        int i = this._elementStateCount + 1;
        ElementState[] elementStateArr2 = this._elementStates;
        if (i == elementStateArr2.length) {
            ElementState[] elementStateArr3 = new ElementState[(elementStateArr2.length + 10)];
            int i2 = 0;
            while (true) {
                elementStateArr = this._elementStates;
                if (i2 >= elementStateArr.length) {
                    break;
                }
                elementStateArr3[i2] = elementStateArr[i2];
                i2++;
            }
            for (int length = elementStateArr.length; length < elementStateArr3.length; length++) {
                elementStateArr3[length] = new ElementState();
            }
            this._elementStates = elementStateArr3;
        }
        this._elementStateCount++;
        ElementState elementState = this._elementStates[this._elementStateCount];
        elementState.namespaceURI = str;
        elementState.localName = str2;
        elementState.rawName = str3;
        elementState.preserveSpace = z;
        elementState.empty = true;
        elementState.afterElement = false;
        elementState.afterComment = false;
        elementState.inCData = false;
        elementState.doCData = false;
        elementState.unescaped = false;
        elementState.prefixes = this._prefixes;
        this._prefixes = null;
        return elementState;
    }

    /* access modifiers changed from: protected */
    public ElementState leaveElementState() {
        int i = this._elementStateCount;
        if (i > 0) {
            this._prefixes = null;
            this._elementStateCount = i - 1;
            return this._elementStates[this._elementStateCount];
        }
        throw new IllegalStateException(DOMMessageFormatter.formatMessage(DOMMessageFormatter.SERIALIZER_DOMAIN, "Internal", null));
    }

    /* access modifiers changed from: protected */
    public boolean isDocumentState() {
        return this._elementStateCount == 0;
    }

    /* access modifiers changed from: protected */
    public String getPrefix(String str) {
        String str2;
        String str3;
        Map<String, String> map = this._prefixes;
        if (map != null && (str3 = map.get(str)) != null) {
            return str3;
        }
        int i = this._elementStateCount;
        if (i == 0) {
            return null;
        }
        while (i > 0) {
            if (this._elementStates[i].prefixes != null && (str2 = this._elementStates[i].prefixes.get(str)) != null) {
                return str2;
            }
            i--;
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public DOMError modifyDOMError(String str, short s, String str2, Node node) {
        this.fDOMError.reset();
        DOMErrorImpl dOMErrorImpl = this.fDOMError;
        dOMErrorImpl.fMessage = str;
        dOMErrorImpl.fType = str2;
        dOMErrorImpl.fSeverity = s;
        dOMErrorImpl.fLocator = new DOMLocatorImpl(-1, -1, -1, node, null);
        return this.fDOMError;
    }

    /* access modifiers changed from: protected */
    public void fatalError(String str) throws IOException {
        if (this.fDOMErrorHandler != null) {
            modifyDOMError(str, 3, null, this.fCurrentNode);
            this.fDOMErrorHandler.handleError(this.fDOMError);
            return;
        }
        throw new IOException(str);
    }
}

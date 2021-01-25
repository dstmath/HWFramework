package ohos.com.sun.org.apache.xerces.internal.jaxp.validation;

import java.io.IOException;
import java.util.Enumeration;
import ohos.com.sun.org.apache.xerces.internal.impl.XMLErrorReporter;
import ohos.com.sun.org.apache.xerces.internal.impl.validation.EntityState;
import ohos.com.sun.org.apache.xerces.internal.impl.validation.ValidationManager;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XMLSchemaValidator;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.util.SimpleLocator;
import ohos.com.sun.org.apache.xerces.internal.util.NamespaceSupport;
import ohos.com.sun.org.apache.xerces.internal.util.SymbolTable;
import ohos.com.sun.org.apache.xerces.internal.util.XMLAttributesImpl;
import ohos.com.sun.org.apache.xerces.internal.util.XMLSymbols;
import ohos.com.sun.org.apache.xerces.internal.xni.NamespaceContext;
import ohos.com.sun.org.apache.xerces.internal.xni.QName;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLString;
import ohos.com.sun.org.apache.xerces.internal.xni.XNIException;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLParseException;
import ohos.javax.xml.parsers.ParserConfigurationException;
import ohos.javax.xml.transform.Result;
import ohos.javax.xml.transform.Source;
import ohos.javax.xml.transform.dom.DOMResult;
import ohos.javax.xml.transform.dom.DOMSource;
import ohos.jdk.xml.internal.JdkXmlUtils;
import ohos.org.w3c.dom.Attr;
import ohos.org.w3c.dom.CDATASection;
import ohos.org.w3c.dom.Comment;
import ohos.org.w3c.dom.Document;
import ohos.org.w3c.dom.DocumentType;
import ohos.org.w3c.dom.Entity;
import ohos.org.w3c.dom.NamedNodeMap;
import ohos.org.w3c.dom.Node;
import ohos.org.w3c.dom.ProcessingInstruction;
import ohos.org.w3c.dom.Text;
import ohos.org.xml.sax.SAXException;

/* access modifiers changed from: package-private */
public final class DOMValidatorHelper implements ValidatorHelper, EntityState {
    private static final int CHUNK_MASK = 1023;
    private static final int CHUNK_SIZE = 1024;
    private static final String ERROR_REPORTER = "http://apache.org/xml/properties/internal/error-reporter";
    private static final String NAMESPACE_CONTEXT = "http://apache.org/xml/properties/internal/namespace-context";
    private static final String SCHEMA_VALIDATOR = "http://apache.org/xml/properties/internal/validator/schema";
    private static final String SYMBOL_TABLE = "http://apache.org/xml/properties/internal/symbol-table";
    private static final String VALIDATION_MANAGER = "http://apache.org/xml/properties/internal/validation-manager";
    final QName fAttributeQName = new QName();
    final XMLAttributesImpl fAttributes = new XMLAttributesImpl();
    private char[] fCharBuffer = new char[1024];
    private XMLSchemaValidatorComponentManager fComponentManager;
    private Node fCurrentElement;
    private DOMNamespaceContext fDOMNamespaceContext = new DOMNamespaceContext();
    private final DOMResultAugmentor fDOMResultAugmentor = new DOMResultAugmentor(this);
    private final DOMResultBuilder fDOMResultBuilder = new DOMResultBuilder();
    private DOMDocumentHandler fDOMValidatorHandler;
    final QName fElementQName = new QName();
    private NamedNodeMap fEntities = null;
    private XMLErrorReporter fErrorReporter;
    private NamespaceSupport fNamespaceContext;
    private Node fRoot;
    private XMLSchemaValidator fSchemaValidator;
    private SymbolTable fSymbolTable;
    final XMLString fTempString = new XMLString();
    private ValidationManager fValidationManager;
    private final SimpleLocator fXMLLocator = new SimpleLocator(null, null, -1, -1, -1);

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.validation.EntityState
    public boolean isEntityDeclared(String str) {
        return false;
    }

    public DOMValidatorHelper(XMLSchemaValidatorComponentManager xMLSchemaValidatorComponentManager) {
        this.fComponentManager = xMLSchemaValidatorComponentManager;
        this.fErrorReporter = (XMLErrorReporter) this.fComponentManager.getProperty("http://apache.org/xml/properties/internal/error-reporter");
        this.fNamespaceContext = (NamespaceSupport) this.fComponentManager.getProperty(NAMESPACE_CONTEXT);
        this.fSchemaValidator = (XMLSchemaValidator) this.fComponentManager.getProperty(SCHEMA_VALIDATOR);
        this.fSymbolTable = (SymbolTable) this.fComponentManager.getProperty("http://apache.org/xml/properties/internal/symbol-table");
        this.fValidationManager = (ValidationManager) this.fComponentManager.getProperty(VALIDATION_MANAGER);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.jaxp.validation.ValidatorHelper
    public void validate(Source source, Result result) throws SAXException, IOException {
        if ((result instanceof DOMResult) || result == null) {
            DOMSource dOMSource = (DOMSource) source;
            DOMResult dOMResult = (DOMResult) result;
            Node node = dOMSource.getNode();
            this.fRoot = node;
            if (node != null) {
                this.fComponentManager.reset();
                this.fValidationManager.setEntityState(this);
                this.fDOMNamespaceContext.reset();
                String systemId = dOMSource.getSystemId();
                this.fXMLLocator.setLiteralSystemId(systemId);
                this.fXMLLocator.setExpandedSystemId(systemId);
                this.fErrorReporter.setDocumentLocator(this.fXMLLocator);
                try {
                    setupEntityMap(node.getNodeType() == 9 ? (Document) node : node.getOwnerDocument());
                    setupDOMResultHandler(dOMSource, dOMResult);
                    this.fSchemaValidator.startDocument(this.fXMLLocator, null, this.fDOMNamespaceContext, null);
                    validate(node);
                    this.fSchemaValidator.endDocument(null);
                    this.fRoot = null;
                    this.fEntities = null;
                    DOMDocumentHandler dOMDocumentHandler = this.fDOMValidatorHandler;
                    if (dOMDocumentHandler != null) {
                        dOMDocumentHandler.setDOMResult(null);
                    }
                } catch (XMLParseException e) {
                    throw Util.toSAXParseException(e);
                } catch (XNIException e2) {
                    throw Util.toSAXException(e2);
                } catch (Throwable th) {
                    this.fRoot = null;
                    this.fEntities = null;
                    DOMDocumentHandler dOMDocumentHandler2 = this.fDOMValidatorHandler;
                    if (dOMDocumentHandler2 != null) {
                        dOMDocumentHandler2.setDOMResult(null);
                    }
                    throw th;
                }
            }
        } else {
            throw new IllegalArgumentException(JAXPValidationMessageFormatter.formatMessage(this.fComponentManager.getLocale(), "SourceResultMismatch", new Object[]{source.getClass().getName(), result.getClass().getName()}));
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.validation.EntityState
    public boolean isEntityUnparsed(String str) {
        Entity namedItem;
        NamedNodeMap namedNodeMap = this.fEntities;
        if (namedNodeMap == null || (namedItem = namedNodeMap.getNamedItem(str)) == null || namedItem.getNotationName() == null) {
            return false;
        }
        return true;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0020, code lost:
        if (r0 == null) goto L_0x0025;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0022, code lost:
        finishNode(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0025, code lost:
        r0 = null;
     */
    private void validate(Node node) {
        Node node2 = node;
        while (node2 != null) {
            beginNode(node2);
            Node firstChild = node2.getFirstChild();
            while (true) {
                if (firstChild != null) {
                    break;
                }
                finishNode(node2);
                if (node == node2) {
                    break;
                }
                firstChild = node2.getNextSibling();
                if (!(firstChild == null && ((node2 = node2.getParentNode()) == null || node == node2))) {
                }
            }
            node2 = firstChild;
        }
    }

    private void beginNode(Node node) {
        DOMDocumentHandler dOMDocumentHandler;
        short nodeType = node.getNodeType();
        if (nodeType == 1) {
            this.fCurrentElement = node;
            this.fNamespaceContext.pushContext();
            fillQName(this.fElementQName, node);
            processAttributes(node.getAttributes());
            this.fSchemaValidator.startElement(this.fElementQName, this.fAttributes, null);
        } else if (nodeType == 10) {
            DOMDocumentHandler dOMDocumentHandler2 = this.fDOMValidatorHandler;
            if (dOMDocumentHandler2 != null) {
                dOMDocumentHandler2.doctypeDecl((DocumentType) node);
            }
        } else if (nodeType == 3) {
            DOMDocumentHandler dOMDocumentHandler3 = this.fDOMValidatorHandler;
            if (dOMDocumentHandler3 != null) {
                dOMDocumentHandler3.setIgnoringCharacters(true);
                sendCharactersToValidator(node.getNodeValue());
                this.fDOMValidatorHandler.setIgnoringCharacters(false);
                this.fDOMValidatorHandler.characters((Text) node);
                return;
            }
            sendCharactersToValidator(node.getNodeValue());
        } else if (nodeType == 4) {
            DOMDocumentHandler dOMDocumentHandler4 = this.fDOMValidatorHandler;
            if (dOMDocumentHandler4 != null) {
                dOMDocumentHandler4.setIgnoringCharacters(true);
                this.fSchemaValidator.startCDATA(null);
                sendCharactersToValidator(node.getNodeValue());
                this.fSchemaValidator.endCDATA(null);
                this.fDOMValidatorHandler.setIgnoringCharacters(false);
                this.fDOMValidatorHandler.cdata((CDATASection) node);
                return;
            }
            this.fSchemaValidator.startCDATA(null);
            sendCharactersToValidator(node.getNodeValue());
            this.fSchemaValidator.endCDATA(null);
        } else if (nodeType == 7) {
            DOMDocumentHandler dOMDocumentHandler5 = this.fDOMValidatorHandler;
            if (dOMDocumentHandler5 != null) {
                dOMDocumentHandler5.processingInstruction((ProcessingInstruction) node);
            }
        } else if (nodeType == 8 && (dOMDocumentHandler = this.fDOMValidatorHandler) != null) {
            dOMDocumentHandler.comment((Comment) node);
        }
    }

    private void finishNode(Node node) {
        if (node.getNodeType() == 1) {
            this.fCurrentElement = node;
            fillQName(this.fElementQName, node);
            this.fSchemaValidator.endElement(this.fElementQName, null);
            this.fNamespaceContext.popContext();
        }
    }

    private void setupEntityMap(Document document) {
        DocumentType doctype;
        if (document == null || (doctype = document.getDoctype()) == null) {
            this.fEntities = null;
        } else {
            this.fEntities = doctype.getEntities();
        }
    }

    private void setupDOMResultHandler(DOMSource dOMSource, DOMResult dOMResult) throws SAXException {
        if (dOMResult == null) {
            this.fDOMValidatorHandler = null;
            this.fSchemaValidator.setDocumentHandler(null);
            return;
        }
        if (dOMSource.getNode() == dOMResult.getNode()) {
            DOMResultAugmentor dOMResultAugmentor = this.fDOMResultAugmentor;
            this.fDOMValidatorHandler = dOMResultAugmentor;
            dOMResultAugmentor.setDOMResult(dOMResult);
            this.fSchemaValidator.setDocumentHandler(this.fDOMResultAugmentor);
            return;
        }
        if (dOMResult.getNode() == null) {
            try {
                dOMResult.setNode(JdkXmlUtils.getDOMFactory(this.fComponentManager.getFeature("jdk.xml.overrideDefaultParser")).newDocumentBuilder().newDocument());
            } catch (ParserConfigurationException e) {
                throw new SAXException(e);
            }
        }
        DOMResultBuilder dOMResultBuilder = this.fDOMResultBuilder;
        this.fDOMValidatorHandler = dOMResultBuilder;
        dOMResultBuilder.setDOMResult(dOMResult);
        this.fSchemaValidator.setDocumentHandler(this.fDOMResultBuilder);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void fillQName(QName qName, Node node) {
        String prefix = node.getPrefix();
        String localName = node.getLocalName();
        String nodeName = node.getNodeName();
        String namespaceURI = node.getNamespaceURI();
        qName.uri = (namespaceURI == null || namespaceURI.length() <= 0) ? null : this.fSymbolTable.addSymbol(namespaceURI);
        qName.rawname = nodeName != null ? this.fSymbolTable.addSymbol(nodeName) : XMLSymbols.EMPTY_STRING;
        if (localName == null) {
            int indexOf = nodeName.indexOf(58);
            if (indexOf > 0) {
                qName.prefix = this.fSymbolTable.addSymbol(nodeName.substring(0, indexOf));
                qName.localpart = this.fSymbolTable.addSymbol(nodeName.substring(indexOf + 1));
                return;
            }
            qName.prefix = XMLSymbols.EMPTY_STRING;
            qName.localpart = qName.rawname;
            return;
        }
        qName.prefix = prefix != null ? this.fSymbolTable.addSymbol(prefix) : XMLSymbols.EMPTY_STRING;
        qName.localpart = this.fSymbolTable.addSymbol(localName);
    }

    private void processAttributes(NamedNodeMap namedNodeMap) {
        int length = namedNodeMap.getLength();
        this.fAttributes.removeAllAttributes();
        for (int i = 0; i < length; i++) {
            Attr item = namedNodeMap.item(i);
            String value = item.getValue();
            if (value == null) {
                value = XMLSymbols.EMPTY_STRING;
            }
            fillQName(this.fAttributeQName, item);
            this.fAttributes.addAttributeNS(this.fAttributeQName, XMLSymbols.fCDATASymbol, value);
            this.fAttributes.setSpecified(i, item.getSpecified());
            if (this.fAttributeQName.uri == NamespaceContext.XMLNS_URI) {
                String str = null;
                if (this.fAttributeQName.prefix == XMLSymbols.PREFIX_XMLNS) {
                    NamespaceSupport namespaceSupport = this.fNamespaceContext;
                    String str2 = this.fAttributeQName.localpart;
                    if (value.length() != 0) {
                        str = this.fSymbolTable.addSymbol(value);
                    }
                    namespaceSupport.declarePrefix(str2, str);
                } else {
                    NamespaceSupport namespaceSupport2 = this.fNamespaceContext;
                    String str3 = XMLSymbols.EMPTY_STRING;
                    if (value.length() != 0) {
                        str = this.fSymbolTable.addSymbol(value);
                    }
                    namespaceSupport2.declarePrefix(str3, str);
                }
            }
        }
    }

    private void sendCharactersToValidator(String str) {
        if (str != null) {
            int length = str.length();
            int i = length & 1023;
            if (i > 0) {
                str.getChars(0, i, this.fCharBuffer, 0);
                this.fTempString.setValues(this.fCharBuffer, 0, i);
                this.fSchemaValidator.characters(this.fTempString, null);
            }
            while (i < length) {
                int i2 = i + 1024;
                str.getChars(i, i2, this.fCharBuffer, 0);
                this.fTempString.setValues(this.fCharBuffer, 0, 1024);
                this.fSchemaValidator.characters(this.fTempString, null);
                i = i2;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public Node getCurrentElement() {
        return this.fCurrentElement;
    }

    /* access modifiers changed from: package-private */
    public final class DOMNamespaceContext implements NamespaceContext {
        protected boolean fDOMContextBuilt = false;
        protected String[] fNamespace = new String[32];
        protected int fNamespaceSize = 0;

        DOMNamespaceContext() {
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.NamespaceContext
        public void pushContext() {
            DOMValidatorHelper.this.fNamespaceContext.pushContext();
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.NamespaceContext
        public void popContext() {
            DOMValidatorHelper.this.fNamespaceContext.popContext();
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.NamespaceContext
        public boolean declarePrefix(String str, String str2) {
            return DOMValidatorHelper.this.fNamespaceContext.declarePrefix(str, str2);
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.NamespaceContext
        public String getURI(String str) {
            String uri = DOMValidatorHelper.this.fNamespaceContext.getURI(str);
            if (uri != null) {
                return uri;
            }
            if (!this.fDOMContextBuilt) {
                fillNamespaceContext();
                this.fDOMContextBuilt = true;
            }
            return (this.fNamespaceSize <= 0 || DOMValidatorHelper.this.fNamespaceContext.containsPrefix(str)) ? uri : getURI0(str);
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.NamespaceContext
        public String getPrefix(String str) {
            return DOMValidatorHelper.this.fNamespaceContext.getPrefix(str);
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.NamespaceContext
        public int getDeclaredPrefixCount() {
            return DOMValidatorHelper.this.fNamespaceContext.getDeclaredPrefixCount();
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.NamespaceContext
        public String getDeclaredPrefixAt(int i) {
            return DOMValidatorHelper.this.fNamespaceContext.getDeclaredPrefixAt(i);
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.NamespaceContext
        public Enumeration getAllPrefixes() {
            return DOMValidatorHelper.this.fNamespaceContext.getAllPrefixes();
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.NamespaceContext
        public void reset() {
            this.fDOMContextBuilt = false;
            this.fNamespaceSize = 0;
        }

        private void fillNamespaceContext() {
            if (DOMValidatorHelper.this.fRoot != null) {
                for (Node parentNode = DOMValidatorHelper.this.fRoot.getParentNode(); parentNode != null; parentNode = parentNode.getParentNode()) {
                    if (1 == parentNode.getNodeType()) {
                        NamedNodeMap attributes = parentNode.getAttributes();
                        int length = attributes.getLength();
                        for (int i = 0; i < length; i++) {
                            Attr item = attributes.item(i);
                            String value = item.getValue();
                            if (value == null) {
                                value = XMLSymbols.EMPTY_STRING;
                            }
                            DOMValidatorHelper dOMValidatorHelper = DOMValidatorHelper.this;
                            dOMValidatorHelper.fillQName(dOMValidatorHelper.fAttributeQName, item);
                            if (DOMValidatorHelper.this.fAttributeQName.uri == NamespaceContext.XMLNS_URI) {
                                String str = null;
                                if (DOMValidatorHelper.this.fAttributeQName.prefix == XMLSymbols.PREFIX_XMLNS) {
                                    String str2 = DOMValidatorHelper.this.fAttributeQName.localpart;
                                    if (value.length() != 0) {
                                        str = DOMValidatorHelper.this.fSymbolTable.addSymbol(value);
                                    }
                                    declarePrefix0(str2, str);
                                } else {
                                    String str3 = XMLSymbols.EMPTY_STRING;
                                    if (value.length() != 0) {
                                        str = DOMValidatorHelper.this.fSymbolTable.addSymbol(value);
                                    }
                                    declarePrefix0(str3, str);
                                }
                            }
                        }
                    }
                }
            }
        }

        private void declarePrefix0(String str, String str2) {
            int i = this.fNamespaceSize;
            String[] strArr = this.fNamespace;
            if (i == strArr.length) {
                String[] strArr2 = new String[(i * 2)];
                System.arraycopy(strArr, 0, strArr2, 0, i);
                this.fNamespace = strArr2;
            }
            String[] strArr3 = this.fNamespace;
            int i2 = this.fNamespaceSize;
            this.fNamespaceSize = i2 + 1;
            strArr3[i2] = str;
            int i3 = this.fNamespaceSize;
            this.fNamespaceSize = i3 + 1;
            strArr3[i3] = str2;
        }

        private String getURI0(String str) {
            for (int i = 0; i < this.fNamespaceSize; i += 2) {
                String[] strArr = this.fNamespace;
                if (strArr[i] == str) {
                    return strArr[i + 1];
                }
            }
            return null;
        }
    }
}

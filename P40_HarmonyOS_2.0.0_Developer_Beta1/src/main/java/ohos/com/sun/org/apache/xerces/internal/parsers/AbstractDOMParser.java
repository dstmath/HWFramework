package ohos.com.sun.org.apache.xerces.internal.parsers;

import java.util.Locale;
import java.util.Stack;
import ohos.com.sun.org.apache.xerces.internal.dom.AttrImpl;
import ohos.com.sun.org.apache.xerces.internal.dom.CoreDocumentImpl;
import ohos.com.sun.org.apache.xerces.internal.dom.DOMErrorImpl;
import ohos.com.sun.org.apache.xerces.internal.dom.DOMMessageFormatter;
import ohos.com.sun.org.apache.xerces.internal.dom.DeferredDocumentImpl;
import ohos.com.sun.org.apache.xerces.internal.dom.DocumentImpl;
import ohos.com.sun.org.apache.xerces.internal.dom.ElementDefinitionImpl;
import ohos.com.sun.org.apache.xerces.internal.dom.ElementNSImpl;
import ohos.com.sun.org.apache.xerces.internal.dom.EntityImpl;
import ohos.com.sun.org.apache.xerces.internal.dom.EntityReferenceImpl;
import ohos.com.sun.org.apache.xerces.internal.dom.NotationImpl;
import ohos.com.sun.org.apache.xerces.internal.dom.PSVIAttrNSImpl;
import ohos.com.sun.org.apache.xerces.internal.dom.PSVIDocumentImpl;
import ohos.com.sun.org.apache.xerces.internal.dom.TextImpl;
import ohos.com.sun.org.apache.xerces.internal.impl.Constants;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.XSSimpleType;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaSymbols;
import ohos.com.sun.org.apache.xerces.internal.util.DOMErrorHandlerWrapper;
import ohos.com.sun.org.apache.xerces.internal.utils.ObjectFactory;
import ohos.com.sun.org.apache.xerces.internal.xni.Augmentations;
import ohos.com.sun.org.apache.xerces.internal.xni.NamespaceContext;
import ohos.com.sun.org.apache.xerces.internal.xni.QName;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLLocator;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLResourceIdentifier;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLString;
import ohos.com.sun.org.apache.xerces.internal.xni.XNIException;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLParserConfiguration;
import ohos.com.sun.org.apache.xerces.internal.xs.AttributePSVI;
import ohos.com.sun.org.apache.xerces.internal.xs.ElementPSVI;
import ohos.com.sun.org.apache.xerces.internal.xs.XSSimpleTypeDefinition;
import ohos.com.sun.org.apache.xerces.internal.xs.XSTypeDefinition;
import ohos.global.icu.impl.PatternTokenizer;
import ohos.org.w3c.dom.Attr;
import ohos.org.w3c.dom.CDATASection;
import ohos.org.w3c.dom.Comment;
import ohos.org.w3c.dom.Document;
import ohos.org.w3c.dom.DocumentType;
import ohos.org.w3c.dom.Element;
import ohos.org.w3c.dom.NamedNodeMap;
import ohos.org.w3c.dom.Node;
import ohos.org.w3c.dom.NodeList;
import ohos.org.w3c.dom.ProcessingInstruction;
import ohos.org.w3c.dom.Text;
import ohos.org.w3c.dom.ls.LSParserFilter;

public class AbstractDOMParser extends AbstractXMLDocumentParser {
    protected static final String CORE_DOCUMENT_CLASS_NAME = "ohos.com.sun.org.apache.xerces.internal.dom.CoreDocumentImpl";
    protected static final String CREATE_CDATA_NODES_FEATURE = "http://apache.org/xml/features/create-cdata-nodes";
    protected static final String CREATE_ENTITY_REF_NODES = "http://apache.org/xml/features/dom/create-entity-ref-nodes";
    protected static final String CURRENT_ELEMENT_NODE = "http://apache.org/xml/properties/dom/current-element-node";
    private static final boolean DEBUG_BASEURI = false;
    private static final boolean DEBUG_EVENTS = false;
    protected static final String DEFAULT_DOCUMENT_CLASS_NAME = "ohos.com.sun.org.apache.xerces.internal.dom.DocumentImpl";
    protected static final String DEFER_NODE_EXPANSION = "http://apache.org/xml/features/dom/defer-node-expansion";
    protected static final String DOCUMENT_CLASS_NAME = "http://apache.org/xml/properties/dom/document-class-name";
    protected static final String INCLUDE_COMMENTS_FEATURE = "http://apache.org/xml/features/include-comments";
    protected static final String INCLUDE_IGNORABLE_WHITESPACE = "http://apache.org/xml/features/dom/include-ignorable-whitespace";
    protected static final String NAMESPACES = "http://xml.org/sax/features/namespaces";
    protected static final String PSVI_DOCUMENT_CLASS_NAME = "ohos.com.sun.org.apache.xerces.internal.dom.PSVIDocumentImpl";
    private static final String[] RECOGNIZED_FEATURES = {"http://xml.org/sax/features/namespaces", CREATE_ENTITY_REF_NODES, INCLUDE_COMMENTS_FEATURE, CREATE_CDATA_NODES_FEATURE, INCLUDE_IGNORABLE_WHITESPACE, DEFER_NODE_EXPANSION};
    private static final String[] RECOGNIZED_PROPERTIES = {DOCUMENT_CLASS_NAME, CURRENT_ELEMENT_NODE};
    private final QName fAttrQName = new QName();
    protected final Stack fBaseURIStack = new Stack();
    protected boolean fCreateCDATANodes;
    protected boolean fCreateEntityRefNodes;
    protected CDATASection fCurrentCDATASection;
    protected int fCurrentCDATASectionIndex;
    protected EntityImpl fCurrentEntityDecl;
    protected Node fCurrentNode;
    protected int fCurrentNodeIndex;
    protected LSParserFilter fDOMFilter = null;
    protected boolean fDeferNodeExpansion;
    protected DeferredDocumentImpl fDeferredDocumentImpl;
    protected int fDeferredEntityDecl;
    protected Document fDocument;
    protected String fDocumentClassName;
    protected CoreDocumentImpl fDocumentImpl;
    protected int fDocumentIndex;
    protected DocumentType fDocumentType;
    protected int fDocumentTypeIndex;
    protected DOMErrorHandlerWrapper fErrorHandler = null;
    protected boolean fFilterReject = false;
    protected boolean fFirstChunk = false;
    protected boolean fInCDATASection;
    protected boolean fInDTD;
    protected boolean fInDTDExternalSubset;
    protected boolean fInEntityRef = false;
    protected boolean fIncludeComments;
    protected boolean fIncludeIgnorableWhitespace;
    protected StringBuilder fInternalSubset;
    private XMLLocator fLocator;
    protected boolean fNamespaceAware;
    protected int fRejectedElementDepth = 0;
    protected Node fRoot;
    protected Stack fSkippedElemStack = null;
    protected boolean fStorePSVI;
    protected final StringBuilder fStringBuilder = new StringBuilder(50);

    @Override // ohos.com.sun.org.apache.xerces.internal.parsers.AbstractXMLDocumentParser, ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void endAttlist(Augmentations augmentations) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.parsers.AbstractXMLDocumentParser, ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void endConditional(Augmentations augmentations) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.parsers.AbstractXMLDocumentParser, ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void ignoredCharacters(XMLString xMLString, Augmentations augmentations) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.parsers.AbstractXMLDocumentParser, ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void startAttlist(String str, Augmentations augmentations) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.parsers.AbstractXMLDocumentParser, ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void startConditional(short s, Augmentations augmentations) throws XNIException {
    }

    /* access modifiers changed from: package-private */
    public static final class Abort extends RuntimeException {
        static final Abort INSTANCE = new Abort();
        private static final long serialVersionUID = 1687848994976808490L;

        @Override // java.lang.Throwable
        public Throwable fillInStackTrace() {
            return this;
        }

        private Abort() {
        }
    }

    protected AbstractDOMParser(XMLParserConfiguration xMLParserConfiguration) {
        super(xMLParserConfiguration);
        this.fConfiguration.addRecognizedFeatures(RECOGNIZED_FEATURES);
        this.fConfiguration.setFeature(CREATE_ENTITY_REF_NODES, true);
        this.fConfiguration.setFeature(INCLUDE_IGNORABLE_WHITESPACE, true);
        this.fConfiguration.setFeature(DEFER_NODE_EXPANSION, true);
        this.fConfiguration.setFeature(INCLUDE_COMMENTS_FEATURE, true);
        this.fConfiguration.setFeature(CREATE_CDATA_NODES_FEATURE, true);
        this.fConfiguration.addRecognizedProperties(RECOGNIZED_PROPERTIES);
        this.fConfiguration.setProperty(DOCUMENT_CLASS_NAME, DEFAULT_DOCUMENT_CLASS_NAME);
    }

    /* access modifiers changed from: protected */
    public String getDocumentClassName() {
        return this.fDocumentClassName;
    }

    /* access modifiers changed from: protected */
    public void setDocumentClassName(String str) {
        if (str == null) {
            str = DEFAULT_DOCUMENT_CLASS_NAME;
        }
        if (!str.equals(DEFAULT_DOCUMENT_CLASS_NAME) && !str.equals(PSVI_DOCUMENT_CLASS_NAME)) {
            try {
                if (!Document.class.isAssignableFrom(ObjectFactory.findProviderClass(str, true))) {
                    throw new IllegalArgumentException(DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "InvalidDocumentClassName", new Object[]{str}));
                }
            } catch (ClassNotFoundException unused) {
                throw new IllegalArgumentException(DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "MissingDocumentClassName", new Object[]{str}));
            }
        }
        this.fDocumentClassName = str;
        if (!str.equals(DEFAULT_DOCUMENT_CLASS_NAME)) {
            this.fDeferNodeExpansion = false;
        }
    }

    public Document getDocument() {
        return this.fDocument;
    }

    public final void dropDocumentReferences() {
        this.fDocument = null;
        this.fDocumentImpl = null;
        this.fDeferredDocumentImpl = null;
        this.fDocumentType = null;
        this.fCurrentNode = null;
        this.fCurrentCDATASection = null;
        this.fCurrentEntityDecl = null;
        this.fRoot = null;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.parsers.AbstractXMLDocumentParser, ohos.com.sun.org.apache.xerces.internal.parsers.XMLParser
    public void reset() throws XNIException {
        super.reset();
        this.fCreateEntityRefNodes = this.fConfiguration.getFeature(CREATE_ENTITY_REF_NODES);
        this.fIncludeIgnorableWhitespace = this.fConfiguration.getFeature(INCLUDE_IGNORABLE_WHITESPACE);
        this.fDeferNodeExpansion = this.fConfiguration.getFeature(DEFER_NODE_EXPANSION);
        this.fNamespaceAware = this.fConfiguration.getFeature("http://xml.org/sax/features/namespaces");
        this.fIncludeComments = this.fConfiguration.getFeature(INCLUDE_COMMENTS_FEATURE);
        this.fCreateCDATANodes = this.fConfiguration.getFeature(CREATE_CDATA_NODES_FEATURE);
        setDocumentClassName((String) this.fConfiguration.getProperty(DOCUMENT_CLASS_NAME));
        this.fDocument = null;
        this.fDocumentImpl = null;
        this.fStorePSVI = false;
        this.fDocumentType = null;
        this.fDocumentTypeIndex = -1;
        this.fDeferredDocumentImpl = null;
        this.fCurrentNode = null;
        this.fStringBuilder.setLength(0);
        this.fRoot = null;
        this.fInDTD = false;
        this.fInDTDExternalSubset = false;
        this.fInCDATASection = false;
        this.fFirstChunk = false;
        this.fCurrentCDATASection = null;
        this.fCurrentCDATASectionIndex = -1;
        this.fBaseURIStack.removeAllElements();
    }

    public void setLocale(Locale locale) {
        this.fConfiguration.setLocale(locale);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.parsers.AbstractXMLDocumentParser, ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void startGeneralEntity(String str, XMLResourceIdentifier xMLResourceIdentifier, String str2, Augmentations augmentations) throws XNIException {
        if (this.fDeferNodeExpansion) {
            int createDeferredEntityReference = this.fDeferredDocumentImpl.createDeferredEntityReference(str, xMLResourceIdentifier.getExpandedSystemId());
            int i = this.fDocumentTypeIndex;
            if (i != -1) {
                int lastChild = this.fDeferredDocumentImpl.getLastChild(i, false);
                while (true) {
                    if (lastChild != -1) {
                        if (this.fDeferredDocumentImpl.getNodeType(lastChild, false) == 6 && this.fDeferredDocumentImpl.getNodeName(lastChild, false).equals(str)) {
                            this.fDeferredEntityDecl = lastChild;
                            this.fDeferredDocumentImpl.setInputEncoding(lastChild, str2);
                            break;
                        }
                        lastChild = this.fDeferredDocumentImpl.getRealPrevSibling(lastChild, false);
                    } else {
                        break;
                    }
                }
            }
            this.fDeferredDocumentImpl.appendChild(this.fCurrentNodeIndex, createDeferredEntityReference);
            this.fCurrentNodeIndex = createDeferredEntityReference;
        } else if (!this.fFilterReject) {
            setCharacterData(true);
            EntityReferenceImpl createEntityReference = this.fDocument.createEntityReference(str);
            if (this.fDocumentImpl != null) {
                EntityReferenceImpl entityReferenceImpl = createEntityReference;
                entityReferenceImpl.setBaseURI(xMLResourceIdentifier.getExpandedSystemId());
                DocumentType documentType = this.fDocumentType;
                if (documentType != null) {
                    this.fCurrentEntityDecl = documentType.getEntities().getNamedItem(str);
                    EntityImpl entityImpl = this.fCurrentEntityDecl;
                    if (entityImpl != null) {
                        entityImpl.setInputEncoding(str2);
                    }
                }
                entityReferenceImpl.needsSyncChildren(false);
            }
            this.fInEntityRef = true;
            this.fCurrentNode.appendChild(createEntityReference);
            this.fCurrentNode = createEntityReference;
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.parsers.AbstractXMLDocumentParser, ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void textDecl(String str, String str2, Augmentations augmentations) throws XNIException {
        if (!this.fInDTD) {
            if (!this.fDeferNodeExpansion) {
                EntityImpl entityImpl = this.fCurrentEntityDecl;
                if (entityImpl != null && !this.fFilterReject) {
                    entityImpl.setXmlEncoding(str2);
                    if (str != null) {
                        this.fCurrentEntityDecl.setXmlVersion(str);
                        return;
                    }
                    return;
                }
                return;
            }
            int i = this.fDeferredEntityDecl;
            if (i != -1) {
                this.fDeferredDocumentImpl.setEntityInfo(i, str, str2);
            }
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.parsers.AbstractXMLDocumentParser, ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void comment(XMLString xMLString, Augmentations augmentations) throws XNIException {
        if (this.fInDTD) {
            StringBuilder sb = this.fInternalSubset;
            if (sb != null && !this.fInDTDExternalSubset) {
                sb.append("<!--");
                if (xMLString.length > 0) {
                    this.fInternalSubset.append(xMLString.ch, xMLString.offset, xMLString.length);
                }
                this.fInternalSubset.append("-->");
            }
        } else if (this.fIncludeComments && !this.fFilterReject) {
            if (!this.fDeferNodeExpansion) {
                Comment createComment = this.fDocument.createComment(xMLString.toString());
                setCharacterData(false);
                this.fCurrentNode.appendChild(createComment);
                LSParserFilter lSParserFilter = this.fDOMFilter;
                if (lSParserFilter != null && !this.fInEntityRef && (lSParserFilter.getWhatToShow() & 128) != 0) {
                    short acceptNode = this.fDOMFilter.acceptNode(createComment);
                    if (acceptNode == 2 || acceptNode == 3) {
                        this.fCurrentNode.removeChild(createComment);
                        this.fFirstChunk = true;
                    } else if (acceptNode == 4) {
                        throw Abort.INSTANCE;
                    }
                }
            } else {
                this.fDeferredDocumentImpl.appendChild(this.fCurrentNodeIndex, this.fDeferredDocumentImpl.createDeferredComment(xMLString.toString()));
            }
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.parsers.AbstractXMLDocumentParser, ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void processingInstruction(String str, XMLString xMLString, Augmentations augmentations) throws XNIException {
        if (this.fInDTD) {
            StringBuilder sb = this.fInternalSubset;
            if (sb != null && !this.fInDTDExternalSubset) {
                sb.append("<?");
                this.fInternalSubset.append(str);
                if (xMLString.length > 0) {
                    StringBuilder sb2 = this.fInternalSubset;
                    sb2.append(' ');
                    sb2.append(xMLString.ch, xMLString.offset, xMLString.length);
                }
                this.fInternalSubset.append("?>");
            }
        } else if (this.fDeferNodeExpansion) {
            this.fDeferredDocumentImpl.appendChild(this.fCurrentNodeIndex, this.fDeferredDocumentImpl.createDeferredProcessingInstruction(str, xMLString.toString()));
        } else if (!this.fFilterReject) {
            ProcessingInstruction createProcessingInstruction = this.fDocument.createProcessingInstruction(str, xMLString.toString());
            setCharacterData(false);
            this.fCurrentNode.appendChild(createProcessingInstruction);
            LSParserFilter lSParserFilter = this.fDOMFilter;
            if (lSParserFilter != null && !this.fInEntityRef && (lSParserFilter.getWhatToShow() & 64) != 0) {
                short acceptNode = this.fDOMFilter.acceptNode(createProcessingInstruction);
                if (acceptNode == 2 || acceptNode == 3) {
                    this.fCurrentNode.removeChild(createProcessingInstruction);
                    this.fFirstChunk = true;
                } else if (acceptNode == 4) {
                    throw Abort.INSTANCE;
                }
            }
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.parsers.AbstractXMLDocumentParser, ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void startDocument(XMLLocator xMLLocator, String str, NamespaceContext namespaceContext, Augmentations augmentations) throws XNIException {
        this.fLocator = xMLLocator;
        if (!this.fDeferNodeExpansion) {
            if (this.fDocumentClassName.equals(DEFAULT_DOCUMENT_CLASS_NAME)) {
                this.fDocument = new DocumentImpl();
                this.fDocumentImpl = this.fDocument;
                this.fDocumentImpl.setStrictErrorChecking(false);
                this.fDocumentImpl.setInputEncoding(str);
                this.fDocumentImpl.setDocumentURI(xMLLocator.getExpandedSystemId());
            } else if (this.fDocumentClassName.equals(PSVI_DOCUMENT_CLASS_NAME)) {
                this.fDocument = new PSVIDocumentImpl();
                this.fDocumentImpl = this.fDocument;
                this.fStorePSVI = true;
                this.fDocumentImpl.setStrictErrorChecking(false);
                this.fDocumentImpl.setInputEncoding(str);
                this.fDocumentImpl.setDocumentURI(xMLLocator.getExpandedSystemId());
            } else {
                try {
                    Class<?> findProviderClass = ObjectFactory.findProviderClass(this.fDocumentClassName, true);
                    this.fDocument = (Document) findProviderClass.newInstance();
                    if (ObjectFactory.findProviderClass(CORE_DOCUMENT_CLASS_NAME, true).isAssignableFrom(findProviderClass)) {
                        this.fDocumentImpl = this.fDocument;
                        if (ObjectFactory.findProviderClass(PSVI_DOCUMENT_CLASS_NAME, true).isAssignableFrom(findProviderClass)) {
                            this.fStorePSVI = true;
                        }
                        this.fDocumentImpl.setStrictErrorChecking(false);
                        this.fDocumentImpl.setInputEncoding(str);
                        if (xMLLocator != null) {
                            this.fDocumentImpl.setDocumentURI(xMLLocator.getExpandedSystemId());
                        }
                    }
                } catch (ClassNotFoundException unused) {
                } catch (Exception unused2) {
                    throw new RuntimeException(DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "CannotCreateDocumentClass", new Object[]{this.fDocumentClassName}));
                }
            }
            this.fCurrentNode = this.fDocument;
            return;
        }
        this.fDeferredDocumentImpl = new DeferredDocumentImpl(this.fNamespaceAware);
        DeferredDocumentImpl deferredDocumentImpl = this.fDeferredDocumentImpl;
        this.fDocument = deferredDocumentImpl;
        this.fDocumentIndex = deferredDocumentImpl.createDeferredDocument();
        this.fDeferredDocumentImpl.setInputEncoding(str);
        this.fDeferredDocumentImpl.setDocumentURI(xMLLocator.getExpandedSystemId());
        this.fCurrentNodeIndex = this.fDocumentIndex;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.parsers.AbstractXMLDocumentParser, ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void xmlDecl(String str, String str2, String str3, Augmentations augmentations) throws XNIException {
        if (!this.fDeferNodeExpansion) {
            CoreDocumentImpl coreDocumentImpl = this.fDocumentImpl;
            if (coreDocumentImpl != null) {
                if (str != null) {
                    coreDocumentImpl.setXmlVersion(str);
                }
                this.fDocumentImpl.setXmlEncoding(str2);
                this.fDocumentImpl.setXmlStandalone("yes".equals(str3));
                return;
            }
            return;
        }
        if (str != null) {
            this.fDeferredDocumentImpl.setXmlVersion(str);
        }
        this.fDeferredDocumentImpl.setXmlEncoding(str2);
        this.fDeferredDocumentImpl.setXmlStandalone("yes".equals(str3));
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.parsers.AbstractXMLDocumentParser, ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void doctypeDecl(String str, String str2, String str3, Augmentations augmentations) throws XNIException {
        if (!this.fDeferNodeExpansion) {
            CoreDocumentImpl coreDocumentImpl = this.fDocumentImpl;
            if (coreDocumentImpl != null) {
                this.fDocumentType = coreDocumentImpl.createDocumentType(str, str2, str3);
                this.fCurrentNode.appendChild(this.fDocumentType);
                return;
            }
            return;
        }
        this.fDocumentTypeIndex = this.fDeferredDocumentImpl.createDeferredDocumentType(str, str2, str3);
        this.fDeferredDocumentImpl.appendChild(this.fCurrentNodeIndex, this.fDocumentTypeIndex);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.parsers.AbstractXMLDocumentParser, ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void startElement(QName qName, XMLAttributes xMLAttributes, Augmentations augmentations) throws XNIException {
        boolean z;
        boolean z2;
        Object obj;
        ElementPSVI elementPSVI;
        boolean z3;
        String str;
        if (this.fDeferNodeExpansion) {
            int createDeferredElement = this.fDeferredDocumentImpl.createDeferredElement(this.fNamespaceAware ? qName.uri : null, qName.rawname);
            XSSimpleTypeDefinition xSSimpleTypeDefinition = null;
            for (int length = xMLAttributes.getLength() - 1; length >= 0; length--) {
                AttributePSVI attributePSVI = (AttributePSVI) xMLAttributes.getAugmentations(length).getItem(Constants.ATTRIBUTE_PSVI);
                if (attributePSVI == null || !this.fNamespaceAware) {
                    if (Boolean.TRUE.equals(xMLAttributes.getAugmentations(length).getItem(Constants.ATTRIBUTE_DECLARED))) {
                        obj = xMLAttributes.getType(length);
                        z2 = SchemaSymbols.ATTVAL_ID.equals(obj);
                    }
                    z = false;
                    this.fDeferredDocumentImpl.setDeferredAttribute(createDeferredElement, xMLAttributes.getQName(length), xMLAttributes.getURI(length), xMLAttributes.getValue(length), xMLAttributes.isSpecified(length), z, xSSimpleTypeDefinition);
                } else {
                    XSSimpleTypeDefinition memberTypeDefinition = attributePSVI.getMemberTypeDefinition();
                    if (memberTypeDefinition == null) {
                        obj = attributePSVI.getTypeDefinition();
                        if (obj != null) {
                            z2 = ((XSSimpleType) obj).isIDType();
                        } else {
                            xSSimpleTypeDefinition = obj;
                            z = false;
                            this.fDeferredDocumentImpl.setDeferredAttribute(createDeferredElement, xMLAttributes.getQName(length), xMLAttributes.getURI(length), xMLAttributes.getValue(length), xMLAttributes.isSpecified(length), z, xSSimpleTypeDefinition);
                        }
                    } else {
                        z = ((XSSimpleType) memberTypeDefinition).isIDType();
                        xSSimpleTypeDefinition = memberTypeDefinition;
                        this.fDeferredDocumentImpl.setDeferredAttribute(createDeferredElement, xMLAttributes.getQName(length), xMLAttributes.getURI(length), xMLAttributes.getValue(length), xMLAttributes.isSpecified(length), z, xSSimpleTypeDefinition);
                    }
                }
                xSSimpleTypeDefinition = obj;
                z = z2;
                this.fDeferredDocumentImpl.setDeferredAttribute(createDeferredElement, xMLAttributes.getQName(length), xMLAttributes.getURI(length), xMLAttributes.getValue(length), xMLAttributes.isSpecified(length), z, xSSimpleTypeDefinition);
            }
            this.fDeferredDocumentImpl.appendChild(this.fCurrentNodeIndex, createDeferredElement);
            this.fCurrentNodeIndex = createDeferredElement;
        } else if (this.fFilterReject) {
            this.fRejectedElementDepth++;
        } else {
            ElementNSImpl createElementNode = createElementNode(qName);
            int length2 = xMLAttributes.getLength();
            boolean z4 = false;
            for (int i = 0; i < length2; i++) {
                xMLAttributes.getName(i, this.fAttrQName);
                PSVIAttrNSImpl createAttrNode = createAttrNode(this.fAttrQName);
                String value = xMLAttributes.getValue(i);
                AttributePSVI attributePSVI2 = (AttributePSVI) xMLAttributes.getAugmentations(i).getItem(Constants.ATTRIBUTE_PSVI);
                if (this.fStorePSVI && attributePSVI2 != null) {
                    createAttrNode.setPSVI(attributePSVI2);
                }
                createAttrNode.setValue(value);
                boolean isSpecified = xMLAttributes.isSpecified(i);
                if (isSpecified || (!z4 && (this.fAttrQName.uri == null || this.fAttrQName.uri == NamespaceContext.XMLNS_URI || this.fAttrQName.prefix != null))) {
                    createElementNode.setAttributeNode(createAttrNode);
                } else {
                    createElementNode.setAttributeNodeNS(createAttrNode);
                    z4 = true;
                }
                if (this.fDocumentImpl != null) {
                    PSVIAttrNSImpl pSVIAttrNSImpl = createAttrNode;
                    if (attributePSVI2 == null || !this.fNamespaceAware) {
                        if (Boolean.TRUE.equals(xMLAttributes.getAugmentations(i).getItem(Constants.ATTRIBUTE_DECLARED))) {
                            str = xMLAttributes.getType(i);
                            z3 = SchemaSymbols.ATTVAL_ID.equals(str);
                        } else {
                            str = null;
                            z3 = false;
                        }
                        pSVIAttrNSImpl.setType(str);
                    } else {
                        XSSimpleTypeDefinition memberTypeDefinition2 = attributePSVI2.getMemberTypeDefinition();
                        if (memberTypeDefinition2 == null) {
                            XSTypeDefinition typeDefinition = attributePSVI2.getTypeDefinition();
                            if (typeDefinition != null) {
                                z3 = ((XSSimpleType) typeDefinition).isIDType();
                                pSVIAttrNSImpl.setType(typeDefinition);
                            } else {
                                z3 = false;
                            }
                        } else {
                            z3 = ((XSSimpleType) memberTypeDefinition2).isIDType();
                            pSVIAttrNSImpl.setType(memberTypeDefinition2);
                        }
                    }
                    if (z3) {
                        createElementNode.setIdAttributeNode(createAttrNode, true);
                    }
                    pSVIAttrNSImpl.setSpecified(isSpecified);
                }
            }
            setCharacterData(false);
            if (!(augmentations == null || (elementPSVI = (ElementPSVI) augmentations.getItem(Constants.ELEMENT_PSVI)) == null || !this.fNamespaceAware)) {
                XSTypeDefinition memberTypeDefinition3 = elementPSVI.getMemberTypeDefinition();
                if (memberTypeDefinition3 == null) {
                    memberTypeDefinition3 = elementPSVI.getTypeDefinition();
                }
                createElementNode.setType(memberTypeDefinition3);
            }
            LSParserFilter lSParserFilter = this.fDOMFilter;
            if (lSParserFilter != null && !this.fInEntityRef) {
                if (this.fRoot == null) {
                    this.fRoot = createElementNode;
                } else {
                    short startElement = lSParserFilter.startElement(createElementNode);
                    if (startElement == 2) {
                        this.fFilterReject = true;
                        this.fRejectedElementDepth = 0;
                        return;
                    } else if (startElement == 3) {
                        this.fFirstChunk = true;
                        this.fSkippedElemStack.push(Boolean.TRUE);
                        return;
                    } else if (startElement == 4) {
                        throw Abort.INSTANCE;
                    } else if (!this.fSkippedElemStack.isEmpty()) {
                        this.fSkippedElemStack.push(Boolean.FALSE);
                    }
                }
            }
            this.fCurrentNode.appendChild(createElementNode);
            this.fCurrentNode = createElementNode;
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.parsers.AbstractXMLDocumentParser, ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void emptyElement(QName qName, XMLAttributes xMLAttributes, Augmentations augmentations) throws XNIException {
        startElement(qName, xMLAttributes, augmentations);
        endElement(qName, augmentations);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.parsers.AbstractXMLDocumentParser, ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void characters(XMLString xMLString, Augmentations augmentations) throws XNIException {
        if (!this.fDeferNodeExpansion) {
            if (!this.fFilterReject) {
                if (this.fInCDATASection && this.fCreateCDATANodes) {
                    CDATASection cDATASection = this.fCurrentCDATASection;
                    if (cDATASection == null) {
                        this.fCurrentCDATASection = this.fDocument.createCDATASection(xMLString.toString());
                        this.fCurrentNode.appendChild(this.fCurrentCDATASection);
                        this.fCurrentNode = this.fCurrentCDATASection;
                        return;
                    }
                    cDATASection.appendData(xMLString.toString());
                } else if (!this.fInDTD && xMLString.length != 0) {
                    TextImpl lastChild = this.fCurrentNode.getLastChild();
                    if (lastChild == null || lastChild.getNodeType() != 3) {
                        this.fFirstChunk = true;
                        this.fCurrentNode.appendChild(this.fDocument.createTextNode(xMLString.toString()));
                        return;
                    }
                    if (this.fFirstChunk) {
                        if (this.fDocumentImpl != null) {
                            this.fStringBuilder.append(lastChild.removeData());
                        } else {
                            Text text = (Text) lastChild;
                            this.fStringBuilder.append(text.getData());
                            text.setNodeValue((String) null);
                        }
                        this.fFirstChunk = false;
                    }
                    if (xMLString.length > 0) {
                        this.fStringBuilder.append(xMLString.ch, xMLString.offset, xMLString.length);
                    }
                }
            }
        } else if (!this.fInCDATASection || !this.fCreateCDATANodes) {
            if (!this.fInDTD && xMLString.length != 0) {
                this.fDeferredDocumentImpl.appendChild(this.fCurrentNodeIndex, this.fDeferredDocumentImpl.createDeferredTextNode(xMLString.toString(), false));
            }
        } else if (this.fCurrentCDATASectionIndex == -1) {
            int createDeferredCDATASection = this.fDeferredDocumentImpl.createDeferredCDATASection(xMLString.toString());
            this.fDeferredDocumentImpl.appendChild(this.fCurrentNodeIndex, createDeferredCDATASection);
            this.fCurrentCDATASectionIndex = createDeferredCDATASection;
            this.fCurrentNodeIndex = createDeferredCDATASection;
        } else {
            this.fDeferredDocumentImpl.appendChild(this.fCurrentNodeIndex, this.fDeferredDocumentImpl.createDeferredTextNode(xMLString.toString(), false));
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.parsers.AbstractXMLDocumentParser, ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void ignorableWhitespace(XMLString xMLString, Augmentations augmentations) throws XNIException {
        if (this.fIncludeIgnorableWhitespace && !this.fFilterReject) {
            if (!this.fDeferNodeExpansion) {
                Text lastChild = this.fCurrentNode.getLastChild();
                if (lastChild == null || lastChild.getNodeType() != 3) {
                    TextImpl createTextNode = this.fDocument.createTextNode(xMLString.toString());
                    if (this.fDocumentImpl != null) {
                        createTextNode.setIgnorableWhitespace(true);
                    }
                    this.fCurrentNode.appendChild(createTextNode);
                    return;
                }
                lastChild.appendData(xMLString.toString());
                return;
            }
            this.fDeferredDocumentImpl.appendChild(this.fCurrentNodeIndex, this.fDeferredDocumentImpl.createDeferredTextNode(xMLString.toString(), true));
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.parsers.AbstractXMLDocumentParser, ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void endElement(QName qName, Augmentations augmentations) throws XNIException {
        ElementPSVI elementPSVI;
        ElementPSVI elementPSVI2;
        if (!this.fDeferNodeExpansion) {
            if (!(augmentations == null || this.fDocumentImpl == null || (!(this.fNamespaceAware || this.fStorePSVI) || (elementPSVI2 = (ElementPSVI) augmentations.getItem(Constants.ELEMENT_PSVI)) == null))) {
                if (this.fNamespaceAware) {
                    XSTypeDefinition memberTypeDefinition = elementPSVI2.getMemberTypeDefinition();
                    if (memberTypeDefinition == null) {
                        memberTypeDefinition = elementPSVI2.getTypeDefinition();
                    }
                    this.fCurrentNode.setType(memberTypeDefinition);
                }
                if (this.fStorePSVI) {
                    this.fCurrentNode.setPSVI(elementPSVI2);
                }
            }
            if (this.fDOMFilter == null) {
                setCharacterData(false);
                this.fCurrentNode = this.fCurrentNode.getParentNode();
            } else if (this.fFilterReject) {
                int i = this.fRejectedElementDepth;
                this.fRejectedElementDepth = i - 1;
                if (i == 0) {
                    this.fFilterReject = false;
                }
            } else if (this.fSkippedElemStack.isEmpty() || this.fSkippedElemStack.pop() != Boolean.TRUE) {
                setCharacterData(false);
                if (!(this.fCurrentNode == this.fRoot || this.fInEntityRef || (this.fDOMFilter.getWhatToShow() & 1) == 0)) {
                    short acceptNode = this.fDOMFilter.acceptNode(this.fCurrentNode);
                    if (acceptNode == 2) {
                        Node parentNode = this.fCurrentNode.getParentNode();
                        parentNode.removeChild(this.fCurrentNode);
                        this.fCurrentNode = parentNode;
                        return;
                    } else if (acceptNode == 3) {
                        this.fFirstChunk = true;
                        Node parentNode2 = this.fCurrentNode.getParentNode();
                        NodeList childNodes = this.fCurrentNode.getChildNodes();
                        int length = childNodes.getLength();
                        for (int i2 = 0; i2 < length; i2++) {
                            parentNode2.appendChild(childNodes.item(0));
                        }
                        parentNode2.removeChild(this.fCurrentNode);
                        this.fCurrentNode = parentNode2;
                        return;
                    } else if (acceptNode == 4) {
                        throw Abort.INSTANCE;
                    }
                }
                this.fCurrentNode = this.fCurrentNode.getParentNode();
            }
        } else {
            if (!(augmentations == null || (elementPSVI = (ElementPSVI) augmentations.getItem(Constants.ELEMENT_PSVI)) == null)) {
                XSTypeDefinition memberTypeDefinition2 = elementPSVI.getMemberTypeDefinition();
                if (memberTypeDefinition2 == null) {
                    memberTypeDefinition2 = elementPSVI.getTypeDefinition();
                }
                this.fDeferredDocumentImpl.setTypeInfo(this.fCurrentNodeIndex, memberTypeDefinition2);
            }
            this.fCurrentNodeIndex = this.fDeferredDocumentImpl.getParentNode(this.fCurrentNodeIndex, false);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.parsers.AbstractXMLDocumentParser, ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void startCDATA(Augmentations augmentations) throws XNIException {
        this.fInCDATASection = true;
        if (!this.fDeferNodeExpansion && !this.fFilterReject && this.fCreateCDATANodes) {
            setCharacterData(false);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.parsers.AbstractXMLDocumentParser, ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void endCDATA(Augmentations augmentations) throws XNIException {
        this.fInCDATASection = false;
        if (!this.fDeferNodeExpansion) {
            if (!this.fFilterReject && this.fCurrentCDATASection != null) {
                LSParserFilter lSParserFilter = this.fDOMFilter;
                if (!(lSParserFilter == null || this.fInEntityRef || (lSParserFilter.getWhatToShow() & 8) == 0)) {
                    short acceptNode = this.fDOMFilter.acceptNode(this.fCurrentCDATASection);
                    if (acceptNode == 2 || acceptNode == 3) {
                        Node parentNode = this.fCurrentNode.getParentNode();
                        parentNode.removeChild(this.fCurrentCDATASection);
                        this.fCurrentNode = parentNode;
                        return;
                    } else if (acceptNode == 4) {
                        throw Abort.INSTANCE;
                    }
                }
                this.fCurrentNode = this.fCurrentNode.getParentNode();
                this.fCurrentCDATASection = null;
            }
        } else if (this.fCurrentCDATASectionIndex != -1) {
            this.fCurrentNodeIndex = this.fDeferredDocumentImpl.getParentNode(this.fCurrentNodeIndex, false);
            this.fCurrentCDATASectionIndex = -1;
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.parsers.AbstractXMLDocumentParser, ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void endDocument(Augmentations augmentations) throws XNIException {
        if (!this.fDeferNodeExpansion) {
            if (this.fDocumentImpl != null) {
                XMLLocator xMLLocator = this.fLocator;
                if (!(xMLLocator == null || xMLLocator.getEncoding() == null)) {
                    this.fDocumentImpl.setInputEncoding(this.fLocator.getEncoding());
                }
                this.fDocumentImpl.setStrictErrorChecking(true);
            }
            this.fCurrentNode = null;
            return;
        }
        XMLLocator xMLLocator2 = this.fLocator;
        if (!(xMLLocator2 == null || xMLLocator2.getEncoding() == null)) {
            this.fDeferredDocumentImpl.setInputEncoding(this.fLocator.getEncoding());
        }
        this.fCurrentNodeIndex = -1;
    }

    /* JADX WARNING: Removed duplicated region for block: B:44:0x00ba  */
    @Override // ohos.com.sun.org.apache.xerces.internal.parsers.AbstractXMLDocumentParser, ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void endGeneralEntity(String str, Augmentations augmentations) throws XNIException {
        boolean z;
        int length;
        if (this.fDeferNodeExpansion) {
            int i = this.fDocumentTypeIndex;
            if (i != -1) {
                int lastChild = this.fDeferredDocumentImpl.getLastChild(i, false);
                while (true) {
                    if (lastChild == -1) {
                        break;
                    }
                    if (this.fDeferredDocumentImpl.getNodeType(lastChild, false) == 6 && this.fDeferredDocumentImpl.getNodeName(lastChild, false).equals(str)) {
                        this.fDeferredEntityDecl = lastChild;
                        break;
                    }
                    lastChild = this.fDeferredDocumentImpl.getRealPrevSibling(lastChild, false);
                }
            }
            int i2 = this.fDeferredEntityDecl;
            if (i2 != -1 && this.fDeferredDocumentImpl.getLastChild(i2, false) == -1) {
                int lastChild2 = this.fDeferredDocumentImpl.getLastChild(this.fCurrentNodeIndex, false);
                int i3 = -1;
                while (lastChild2 != -1) {
                    int cloneNode = this.fDeferredDocumentImpl.cloneNode(lastChild2, true);
                    this.fDeferredDocumentImpl.insertBefore(this.fDeferredEntityDecl, cloneNode, i3);
                    lastChild2 = this.fDeferredDocumentImpl.getRealPrevSibling(lastChild2, false);
                    i3 = cloneNode;
                }
            }
            if (this.fCreateEntityRefNodes) {
                this.fCurrentNodeIndex = this.fDeferredDocumentImpl.getParentNode(this.fCurrentNodeIndex, false);
            } else {
                int lastChild3 = this.fDeferredDocumentImpl.getLastChild(this.fCurrentNodeIndex, false);
                int parentNode = this.fDeferredDocumentImpl.getParentNode(this.fCurrentNodeIndex, false);
                int i4 = this.fCurrentNodeIndex;
                int i5 = lastChild3;
                while (i5 != -1) {
                    handleBaseURI(i5);
                    int realPrevSibling = this.fDeferredDocumentImpl.getRealPrevSibling(i5, false);
                    this.fDeferredDocumentImpl.insertBefore(parentNode, i5, i4);
                    i4 = i5;
                    i5 = realPrevSibling;
                }
                if (lastChild3 != -1) {
                    this.fDeferredDocumentImpl.setAsLastChild(parentNode, lastChild3);
                } else {
                    this.fDeferredDocumentImpl.setAsLastChild(parentNode, this.fDeferredDocumentImpl.getRealPrevSibling(i4, false));
                }
                this.fCurrentNodeIndex = parentNode;
            }
            this.fDeferredEntityDecl = -1;
        } else if (!this.fFilterReject) {
            setCharacterData(true);
            DocumentType documentType = this.fDocumentType;
            if (documentType != null) {
                this.fCurrentEntityDecl = documentType.getEntities().getNamedItem(str);
                EntityImpl entityImpl = this.fCurrentEntityDecl;
                if (entityImpl != null) {
                    if (entityImpl != null && entityImpl.getFirstChild() == null) {
                        this.fCurrentEntityDecl.setReadOnly(false, true);
                        for (Node firstChild = this.fCurrentNode.getFirstChild(); firstChild != null; firstChild = firstChild.getNextSibling()) {
                            this.fCurrentEntityDecl.appendChild(firstChild.cloneNode(true));
                        }
                        this.fCurrentEntityDecl.setReadOnly(true, true);
                    }
                    this.fCurrentEntityDecl = null;
                }
            }
            this.fInEntityRef = false;
            if (this.fCreateEntityRefNodes) {
                if (this.fDocumentImpl != null) {
                    this.fCurrentNode.setReadOnly(true, true);
                }
                LSParserFilter lSParserFilter = this.fDOMFilter;
                if (lSParserFilter == null || (lSParserFilter.getWhatToShow() & 16) == 0) {
                    this.fCurrentNode = this.fCurrentNode.getParentNode();
                } else {
                    short acceptNode = this.fDOMFilter.acceptNode(this.fCurrentNode);
                    if (acceptNode == 2) {
                        Node parentNode2 = this.fCurrentNode.getParentNode();
                        parentNode2.removeChild(this.fCurrentNode);
                        this.fCurrentNode = parentNode2;
                        return;
                    } else if (acceptNode == 3) {
                        this.fFirstChunk = true;
                        z = true;
                        if (this.fCreateEntityRefNodes || z) {
                            NodeList childNodes = this.fCurrentNode.getChildNodes();
                            Node parentNode3 = this.fCurrentNode.getParentNode();
                            length = childNodes.getLength();
                            if (length > 0) {
                                Text previousSibling = this.fCurrentNode.getPreviousSibling();
                                Node item = childNodes.item(0);
                                if (previousSibling != null && previousSibling.getNodeType() == 3 && item.getNodeType() == 3) {
                                    previousSibling.appendData(item.getNodeValue());
                                    this.fCurrentNode.removeChild(item);
                                } else {
                                    handleBaseURI(parentNode3.insertBefore(item, this.fCurrentNode));
                                }
                                for (int i6 = 1; i6 < length; i6++) {
                                    handleBaseURI(parentNode3.insertBefore(childNodes.item(0), this.fCurrentNode));
                                }
                            }
                            parentNode3.removeChild(this.fCurrentNode);
                            this.fCurrentNode = parentNode3;
                        }
                        return;
                    } else if (acceptNode != 4) {
                        this.fCurrentNode = this.fCurrentNode.getParentNode();
                    } else {
                        throw Abort.INSTANCE;
                    }
                }
            }
            z = false;
            if (this.fCreateEntityRefNodes) {
            }
            NodeList childNodes2 = this.fCurrentNode.getChildNodes();
            Node parentNode32 = this.fCurrentNode.getParentNode();
            length = childNodes2.getLength();
            if (length > 0) {
            }
            parentNode32.removeChild(this.fCurrentNode);
            this.fCurrentNode = parentNode32;
        }
    }

    /* access modifiers changed from: protected */
    public final void handleBaseURI(Node node) {
        String baseURI;
        if (this.fDocumentImpl != null) {
            short nodeType = node.getNodeType();
            if (nodeType == 1) {
                if (this.fNamespaceAware) {
                    if (((Element) node).getAttributeNodeNS("http://www.w3.org/XML/1998/namespace", "base") != null) {
                        return;
                    }
                } else if (((Element) node).getAttributeNode("xml:base") != null) {
                    return;
                }
                String baseURI2 = this.fCurrentNode.getBaseURI();
                if (baseURI2 != null && !baseURI2.equals(this.fDocumentImpl.getDocumentURI())) {
                    if (this.fNamespaceAware) {
                        ((Element) node).setAttributeNS("http://www.w3.org/XML/1998/namespace", "xml:base", baseURI2);
                    } else {
                        ((Element) node).setAttribute("xml:base", baseURI2);
                    }
                }
            } else if (nodeType == 7 && (baseURI = this.fCurrentNode.getBaseURI()) != null && this.fErrorHandler != null) {
                DOMErrorImpl dOMErrorImpl = new DOMErrorImpl();
                dOMErrorImpl.fType = "pi-base-uri-not-preserved";
                dOMErrorImpl.fRelatedData = baseURI;
                dOMErrorImpl.fSeverity = 1;
                this.fErrorHandler.getErrorHandler().handleError(dOMErrorImpl);
            }
        }
    }

    /* access modifiers changed from: protected */
    public final void handleBaseURI(int i) {
        short nodeType = this.fDeferredDocumentImpl.getNodeType(i, false);
        if (nodeType == 1) {
            String nodeValueString = this.fDeferredDocumentImpl.getNodeValueString(this.fCurrentNodeIndex, false);
            if (nodeValueString == null) {
                nodeValueString = this.fDeferredDocumentImpl.getDeferredEntityBaseURI(this.fDeferredEntityDecl);
            }
            if (nodeValueString != null && !nodeValueString.equals(this.fDeferredDocumentImpl.getDocumentURI())) {
                this.fDeferredDocumentImpl.setDeferredAttribute(i, "xml:base", "http://www.w3.org/XML/1998/namespace", nodeValueString, true);
            }
        } else if (nodeType == 7) {
            String nodeValueString2 = this.fDeferredDocumentImpl.getNodeValueString(this.fCurrentNodeIndex, false);
            if (nodeValueString2 == null) {
                nodeValueString2 = this.fDeferredDocumentImpl.getDeferredEntityBaseURI(this.fDeferredEntityDecl);
            }
            if (nodeValueString2 != null && this.fErrorHandler != null) {
                DOMErrorImpl dOMErrorImpl = new DOMErrorImpl();
                dOMErrorImpl.fType = "pi-base-uri-not-preserved";
                dOMErrorImpl.fRelatedData = nodeValueString2;
                dOMErrorImpl.fSeverity = 1;
                this.fErrorHandler.getErrorHandler().handleError(dOMErrorImpl);
            }
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.parsers.AbstractXMLDocumentParser, ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void startDTD(XMLLocator xMLLocator, Augmentations augmentations) throws XNIException {
        this.fInDTD = true;
        if (xMLLocator != null) {
            this.fBaseURIStack.push(xMLLocator.getBaseSystemId());
        }
        if (this.fDeferNodeExpansion || this.fDocumentImpl != null) {
            this.fInternalSubset = new StringBuilder(1024);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.parsers.AbstractXMLDocumentParser, ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void endDTD(Augmentations augmentations) throws XNIException {
        this.fInDTD = false;
        if (!this.fBaseURIStack.isEmpty()) {
            this.fBaseURIStack.pop();
        }
        StringBuilder sb = this.fInternalSubset;
        String sb2 = (sb == null || sb.length() <= 0) ? null : this.fInternalSubset.toString();
        if (this.fDeferNodeExpansion) {
            if (sb2 != null) {
                this.fDeferredDocumentImpl.setInternalSubset(this.fDocumentTypeIndex, sb2);
            }
        } else if (this.fDocumentImpl != null && sb2 != null) {
            this.fDocumentType.setInternalSubset(sb2);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.parsers.AbstractXMLDocumentParser, ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void startExternalSubset(XMLResourceIdentifier xMLResourceIdentifier, Augmentations augmentations) throws XNIException {
        this.fBaseURIStack.push(xMLResourceIdentifier.getBaseSystemId());
        this.fInDTDExternalSubset = true;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.parsers.AbstractXMLDocumentParser, ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void endExternalSubset(Augmentations augmentations) throws XNIException {
        this.fInDTDExternalSubset = false;
        this.fBaseURIStack.pop();
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.parsers.AbstractXMLDocumentParser, ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void internalEntityDecl(String str, XMLString xMLString, XMLString xMLString2, Augmentations augmentations) throws XNIException {
        StringBuilder sb = this.fInternalSubset;
        boolean z = true;
        if (sb != null && !this.fInDTDExternalSubset) {
            sb.append("<!ENTITY ");
            if (str.startsWith("%")) {
                this.fInternalSubset.append("% ");
                this.fInternalSubset.append(str.substring(1));
            } else {
                this.fInternalSubset.append(str);
            }
            this.fInternalSubset.append(' ');
            String xMLString3 = xMLString2.toString();
            char c = PatternTokenizer.SINGLE_QUOTE;
            boolean z2 = xMLString3.indexOf(39) == -1;
            this.fInternalSubset.append(z2 ? '\'' : '\"');
            this.fInternalSubset.append(xMLString3);
            StringBuilder sb2 = this.fInternalSubset;
            if (!z2) {
                c = '\"';
            }
            sb2.append(c);
            this.fInternalSubset.append(">\n");
        }
        if (!str.startsWith("%")) {
            DocumentType documentType = this.fDocumentType;
            if (documentType != null) {
                NamedNodeMap entities = documentType.getEntities();
                if (entities.getNamedItem(str) == null) {
                    EntityImpl createEntity = this.fDocumentImpl.createEntity(str);
                    createEntity.setBaseURI((String) this.fBaseURIStack.peek());
                    entities.setNamedItem(createEntity);
                }
            }
            int i = this.fDocumentTypeIndex;
            if (i != -1) {
                int lastChild = this.fDeferredDocumentImpl.getLastChild(i, false);
                while (true) {
                    if (lastChild != -1) {
                        if (this.fDeferredDocumentImpl.getNodeType(lastChild, false) == 6 && this.fDeferredDocumentImpl.getNodeName(lastChild, false).equals(str)) {
                            break;
                        }
                        lastChild = this.fDeferredDocumentImpl.getRealPrevSibling(lastChild, false);
                    } else {
                        z = false;
                        break;
                    }
                }
                if (!z) {
                    this.fDeferredDocumentImpl.appendChild(this.fDocumentTypeIndex, this.fDeferredDocumentImpl.createDeferredEntity(str, null, null, null, (String) this.fBaseURIStack.peek()));
                }
            }
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.parsers.AbstractXMLDocumentParser, ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void externalEntityDecl(String str, XMLResourceIdentifier xMLResourceIdentifier, Augmentations augmentations) throws XNIException {
        String publicId = xMLResourceIdentifier.getPublicId();
        String literalSystemId = xMLResourceIdentifier.getLiteralSystemId();
        StringBuilder sb = this.fInternalSubset;
        boolean z = true;
        if (sb != null && !this.fInDTDExternalSubset) {
            sb.append("<!ENTITY ");
            if (str.startsWith("%")) {
                this.fInternalSubset.append("% ");
                this.fInternalSubset.append(str.substring(1));
            } else {
                this.fInternalSubset.append(str);
            }
            this.fInternalSubset.append(' ');
            if (publicId != null) {
                this.fInternalSubset.append("PUBLIC '");
                this.fInternalSubset.append(publicId);
                this.fInternalSubset.append("' '");
            } else {
                this.fInternalSubset.append("SYSTEM '");
            }
            this.fInternalSubset.append(literalSystemId);
            this.fInternalSubset.append("'>\n");
        }
        if (!str.startsWith("%")) {
            DocumentType documentType = this.fDocumentType;
            if (documentType != null) {
                NamedNodeMap entities = documentType.getEntities();
                if (entities.getNamedItem(str) == null) {
                    EntityImpl createEntity = this.fDocumentImpl.createEntity(str);
                    createEntity.setPublicId(publicId);
                    createEntity.setSystemId(literalSystemId);
                    createEntity.setBaseURI(xMLResourceIdentifier.getBaseSystemId());
                    entities.setNamedItem(createEntity);
                }
            }
            int i = this.fDocumentTypeIndex;
            if (i != -1) {
                int lastChild = this.fDeferredDocumentImpl.getLastChild(i, false);
                while (true) {
                    if (lastChild != -1) {
                        if (this.fDeferredDocumentImpl.getNodeType(lastChild, false) == 6 && this.fDeferredDocumentImpl.getNodeName(lastChild, false).equals(str)) {
                            break;
                        }
                        lastChild = this.fDeferredDocumentImpl.getRealPrevSibling(lastChild, false);
                    } else {
                        z = false;
                        break;
                    }
                }
                if (!z) {
                    this.fDeferredDocumentImpl.appendChild(this.fDocumentTypeIndex, this.fDeferredDocumentImpl.createDeferredEntity(str, publicId, literalSystemId, null, xMLResourceIdentifier.getBaseSystemId()));
                }
            }
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.parsers.AbstractXMLDocumentParser, ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void startParameterEntity(String str, XMLResourceIdentifier xMLResourceIdentifier, String str2, Augmentations augmentations) throws XNIException {
        if (augmentations != null && this.fInternalSubset != null && !this.fInDTDExternalSubset && Boolean.TRUE.equals(augmentations.getItem(Constants.ENTITY_SKIPPED))) {
            StringBuilder sb = this.fInternalSubset;
            sb.append(str);
            sb.append(";\n");
        }
        this.fBaseURIStack.push(xMLResourceIdentifier.getExpandedSystemId());
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.parsers.AbstractXMLDocumentParser, ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void endParameterEntity(String str, Augmentations augmentations) throws XNIException {
        this.fBaseURIStack.pop();
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.parsers.AbstractXMLDocumentParser, ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void unparsedEntityDecl(String str, XMLResourceIdentifier xMLResourceIdentifier, String str2, Augmentations augmentations) throws XNIException {
        String publicId = xMLResourceIdentifier.getPublicId();
        String literalSystemId = xMLResourceIdentifier.getLiteralSystemId();
        StringBuilder sb = this.fInternalSubset;
        if (sb != null && !this.fInDTDExternalSubset) {
            sb.append("<!ENTITY ");
            this.fInternalSubset.append(str);
            this.fInternalSubset.append(' ');
            if (publicId != null) {
                this.fInternalSubset.append("PUBLIC '");
                this.fInternalSubset.append(publicId);
                if (literalSystemId != null) {
                    this.fInternalSubset.append("' '");
                    this.fInternalSubset.append(literalSystemId);
                }
            } else {
                this.fInternalSubset.append("SYSTEM '");
                this.fInternalSubset.append(literalSystemId);
            }
            this.fInternalSubset.append("' NDATA ");
            this.fInternalSubset.append(str2);
            this.fInternalSubset.append(">\n");
        }
        DocumentType documentType = this.fDocumentType;
        if (documentType != null) {
            NamedNodeMap entities = documentType.getEntities();
            if (entities.getNamedItem(str) == null) {
                EntityImpl createEntity = this.fDocumentImpl.createEntity(str);
                createEntity.setPublicId(publicId);
                createEntity.setSystemId(literalSystemId);
                createEntity.setNotationName(str2);
                createEntity.setBaseURI(xMLResourceIdentifier.getBaseSystemId());
                entities.setNamedItem(createEntity);
            }
        }
        int i = this.fDocumentTypeIndex;
        if (i != -1) {
            boolean z = false;
            int lastChild = this.fDeferredDocumentImpl.getLastChild(i, false);
            while (true) {
                if (lastChild != -1) {
                    if (this.fDeferredDocumentImpl.getNodeType(lastChild, false) == 6 && this.fDeferredDocumentImpl.getNodeName(lastChild, false).equals(str)) {
                        z = true;
                        break;
                    }
                    lastChild = this.fDeferredDocumentImpl.getRealPrevSibling(lastChild, false);
                } else {
                    break;
                }
            }
            if (!z) {
                this.fDeferredDocumentImpl.appendChild(this.fDocumentTypeIndex, this.fDeferredDocumentImpl.createDeferredEntity(str, publicId, literalSystemId, str2, xMLResourceIdentifier.getBaseSystemId()));
            }
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.parsers.AbstractXMLDocumentParser, ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void notationDecl(String str, XMLResourceIdentifier xMLResourceIdentifier, Augmentations augmentations) throws XNIException {
        DocumentType documentType;
        String publicId = xMLResourceIdentifier.getPublicId();
        String literalSystemId = xMLResourceIdentifier.getLiteralSystemId();
        StringBuilder sb = this.fInternalSubset;
        if (sb != null && !this.fInDTDExternalSubset) {
            sb.append("<!NOTATION ");
            this.fInternalSubset.append(str);
            if (publicId != null) {
                this.fInternalSubset.append(" PUBLIC '");
                this.fInternalSubset.append(publicId);
                if (literalSystemId != null) {
                    this.fInternalSubset.append("' '");
                    this.fInternalSubset.append(literalSystemId);
                }
            } else {
                this.fInternalSubset.append(" SYSTEM '");
                this.fInternalSubset.append(literalSystemId);
            }
            this.fInternalSubset.append("'>\n");
        }
        if (!(this.fDocumentImpl == null || (documentType = this.fDocumentType) == null)) {
            NamedNodeMap notations = documentType.getNotations();
            if (notations.getNamedItem(str) == null) {
                NotationImpl createNotation = this.fDocumentImpl.createNotation(str);
                createNotation.setPublicId(publicId);
                createNotation.setSystemId(literalSystemId);
                createNotation.setBaseURI(xMLResourceIdentifier.getBaseSystemId());
                notations.setNamedItem(createNotation);
            }
        }
        int i = this.fDocumentTypeIndex;
        if (i != -1) {
            boolean z = false;
            int lastChild = this.fDeferredDocumentImpl.getLastChild(i, false);
            while (true) {
                if (lastChild != -1) {
                    if (this.fDeferredDocumentImpl.getNodeType(lastChild, false) == 12 && this.fDeferredDocumentImpl.getNodeName(lastChild, false).equals(str)) {
                        z = true;
                        break;
                    }
                    lastChild = this.fDeferredDocumentImpl.getPrevSibling(lastChild, false);
                } else {
                    break;
                }
            }
            if (!z) {
                this.fDeferredDocumentImpl.appendChild(this.fDocumentTypeIndex, this.fDeferredDocumentImpl.createDeferredNotation(str, publicId, literalSystemId, xMLResourceIdentifier.getBaseSystemId()));
            }
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.parsers.AbstractXMLDocumentParser, ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void elementDecl(String str, String str2, Augmentations augmentations) throws XNIException {
        StringBuilder sb = this.fInternalSubset;
        if (sb != null && !this.fInDTDExternalSubset) {
            sb.append("<!ELEMENT ");
            this.fInternalSubset.append(str);
            this.fInternalSubset.append(' ');
            this.fInternalSubset.append(str2);
            this.fInternalSubset.append(">\n");
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.parsers.AbstractXMLDocumentParser, ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void attributeDecl(String str, String str2, String str3, String[] strArr, String str4, XMLString xMLString, XMLString xMLString2, Augmentations augmentations) throws XNIException {
        AttrImpl attrImpl;
        StringBuilder sb = this.fInternalSubset;
        if (sb != null && !this.fInDTDExternalSubset) {
            sb.append("<!ATTLIST ");
            this.fInternalSubset.append(str);
            this.fInternalSubset.append(' ');
            this.fInternalSubset.append(str2);
            this.fInternalSubset.append(' ');
            if (str3.equals("ENUMERATION")) {
                this.fInternalSubset.append('(');
                for (int i = 0; i < strArr.length; i++) {
                    if (i > 0) {
                        this.fInternalSubset.append('|');
                    }
                    this.fInternalSubset.append(strArr[i]);
                }
                this.fInternalSubset.append(')');
            } else {
                this.fInternalSubset.append(str3);
            }
            if (str4 != null) {
                this.fInternalSubset.append(' ');
                this.fInternalSubset.append(str4);
            }
            if (xMLString != null) {
                this.fInternalSubset.append(" '");
                for (int i2 = 0; i2 < xMLString.length; i2++) {
                    char c = xMLString.ch[xMLString.offset + i2];
                    if (c == '\'') {
                        this.fInternalSubset.append("&apos;");
                    } else {
                        this.fInternalSubset.append(c);
                    }
                }
                this.fInternalSubset.append(PatternTokenizer.SINGLE_QUOTE);
            }
            this.fInternalSubset.append(">\n");
        }
        DeferredDocumentImpl deferredDocumentImpl = this.fDeferredDocumentImpl;
        String str5 = null;
        if (deferredDocumentImpl != null) {
            if (xMLString != null) {
                int lookupElementDefinition = deferredDocumentImpl.lookupElementDefinition(str);
                if (lookupElementDefinition == -1) {
                    lookupElementDefinition = this.fDeferredDocumentImpl.createDeferredElementDefinition(str);
                    this.fDeferredDocumentImpl.appendChild(this.fDocumentTypeIndex, lookupElementDefinition);
                }
                if (this.fNamespaceAware) {
                    if (str2.startsWith("xmlns:") || str2.equals("xmlns")) {
                        str5 = NamespaceContext.XMLNS_URI;
                    } else if (str2.startsWith("xml:")) {
                        str5 = NamespaceContext.XML_URI;
                    }
                }
                int createDeferredAttribute = this.fDeferredDocumentImpl.createDeferredAttribute(str2, str5, xMLString.toString(), false);
                if (SchemaSymbols.ATTVAL_ID.equals(str3)) {
                    this.fDeferredDocumentImpl.setIdAttribute(createDeferredAttribute);
                }
                this.fDeferredDocumentImpl.appendChild(lookupElementDefinition, createDeferredAttribute);
            }
        } else if (this.fDocumentImpl != null && xMLString != null) {
            ElementDefinitionImpl namedItem = this.fDocumentType.getElements().getNamedItem(str);
            if (namedItem == null) {
                namedItem = this.fDocumentImpl.createElementDefinition(str);
                this.fDocumentType.getElements().setNamedItem(namedItem);
            }
            boolean z = this.fNamespaceAware;
            if (z) {
                if (str2.startsWith("xmlns:") || str2.equals("xmlns")) {
                    str5 = NamespaceContext.XMLNS_URI;
                } else if (str2.startsWith("xml:")) {
                    str5 = NamespaceContext.XML_URI;
                }
                attrImpl = (AttrImpl) this.fDocumentImpl.createAttributeNS(str5, str2);
            } else {
                attrImpl = (AttrImpl) this.fDocumentImpl.createAttribute(str2);
            }
            attrImpl.setValue(xMLString.toString());
            attrImpl.setSpecified(false);
            attrImpl.setIdAttribute(SchemaSymbols.ATTVAL_ID.equals(str3));
            if (z) {
                namedItem.getAttributes().setNamedItemNS(attrImpl);
            } else {
                namedItem.getAttributes().setNamedItem(attrImpl);
            }
        }
    }

    /* access modifiers changed from: protected */
    public Element createElementNode(QName qName) {
        if (!this.fNamespaceAware) {
            return this.fDocument.createElement(qName.rawname);
        }
        CoreDocumentImpl coreDocumentImpl = this.fDocumentImpl;
        if (coreDocumentImpl != null) {
            return coreDocumentImpl.createElementNS(qName.uri, qName.rawname, qName.localpart);
        }
        return this.fDocument.createElementNS(qName.uri, qName.rawname);
    }

    /* access modifiers changed from: protected */
    public Attr createAttrNode(QName qName) {
        if (!this.fNamespaceAware) {
            return this.fDocument.createAttribute(qName.rawname);
        }
        CoreDocumentImpl coreDocumentImpl = this.fDocumentImpl;
        if (coreDocumentImpl != null) {
            return coreDocumentImpl.createAttributeNS(qName.uri, qName.rawname, qName.localpart);
        }
        return this.fDocument.createAttributeNS(qName.uri, qName.rawname);
    }

    /* access modifiers changed from: protected */
    public void setCharacterData(boolean z) {
        this.fFirstChunk = z;
        TextImpl lastChild = this.fCurrentNode.getLastChild();
        if (lastChild != null) {
            if (this.fStringBuilder.length() > 0) {
                if (lastChild.getNodeType() == 3) {
                    if (this.fDocumentImpl != null) {
                        lastChild.replaceData(this.fStringBuilder.toString());
                    } else {
                        ((Text) lastChild).setData(this.fStringBuilder.toString());
                    }
                }
                this.fStringBuilder.setLength(0);
            }
            if (this.fDOMFilter != null && !this.fInEntityRef && lastChild.getNodeType() == 3 && (this.fDOMFilter.getWhatToShow() & 4) != 0) {
                short acceptNode = this.fDOMFilter.acceptNode(lastChild);
                if (acceptNode == 2 || acceptNode == 3) {
                    this.fCurrentNode.removeChild(lastChild);
                } else if (acceptNode == 4) {
                    throw Abort.INSTANCE;
                }
            }
        }
    }

    public void abort() {
        throw Abort.INSTANCE;
    }
}

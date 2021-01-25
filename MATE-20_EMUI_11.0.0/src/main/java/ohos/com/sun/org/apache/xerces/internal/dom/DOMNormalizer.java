package ohos.com.sun.org.apache.xerces.internal.dom;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Vector;
import ohos.com.sun.org.apache.xerces.internal.impl.Constants;
import ohos.com.sun.org.apache.xerces.internal.impl.RevalidationHandler;
import ohos.com.sun.org.apache.xerces.internal.impl.dtd.DTDGrammar;
import ohos.com.sun.org.apache.xerces.internal.impl.dtd.XMLDTDDescription;
import ohos.com.sun.org.apache.xerces.internal.impl.dtd.XMLDTDValidator;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.XSSimpleType;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.util.SimpleLocator;
import ohos.com.sun.org.apache.xerces.internal.jaxp.JAXPConstants;
import ohos.com.sun.org.apache.xerces.internal.parsers.XMLGrammarPreparser;
import ohos.com.sun.org.apache.xerces.internal.util.AugmentationsImpl;
import ohos.com.sun.org.apache.xerces.internal.util.NamespaceSupport;
import ohos.com.sun.org.apache.xerces.internal.util.SymbolTable;
import ohos.com.sun.org.apache.xerces.internal.util.XML11Char;
import ohos.com.sun.org.apache.xerces.internal.util.XMLChar;
import ohos.com.sun.org.apache.xerces.internal.util.XMLGrammarPoolImpl;
import ohos.com.sun.org.apache.xerces.internal.util.XMLSymbols;
import ohos.com.sun.org.apache.xerces.internal.xni.Augmentations;
import ohos.com.sun.org.apache.xerces.internal.xni.NamespaceContext;
import ohos.com.sun.org.apache.xerces.internal.xni.QName;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLLocator;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLResourceIdentifier;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLString;
import ohos.com.sun.org.apache.xerces.internal.xni.XNIException;
import ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarDescription;
import ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarPool;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLComponent;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLDocumentSource;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;
import ohos.com.sun.org.apache.xerces.internal.xs.AttributePSVI;
import ohos.com.sun.org.apache.xerces.internal.xs.ElementPSVI;
import ohos.com.sun.org.apache.xerces.internal.xs.XSSimpleTypeDefinition;
import ohos.com.sun.org.apache.xerces.internal.xs.XSTypeDefinition;
import ohos.org.w3c.dom.Attr;
import ohos.org.w3c.dom.CDATASection;
import ohos.org.w3c.dom.Comment;
import ohos.org.w3c.dom.DOMErrorHandler;
import ohos.org.w3c.dom.Document;
import ohos.org.w3c.dom.DocumentType;
import ohos.org.w3c.dom.Element;
import ohos.org.w3c.dom.Entity;
import ohos.org.w3c.dom.NamedNodeMap;
import ohos.org.w3c.dom.Node;
import ohos.org.w3c.dom.NodeList;
import ohos.org.w3c.dom.ProcessingInstruction;
import ohos.org.w3c.dom.Text;

public class DOMNormalizer implements XMLDocumentHandler {
    protected static final boolean DEBUG = false;
    protected static final boolean DEBUG_EVENTS = false;
    protected static final boolean DEBUG_ND = false;
    protected static final String PREFIX = "NS";
    private boolean allWhitespace = false;
    protected final XMLAttributesProxy fAttrProxy = new XMLAttributesProxy();
    private QName fAttrQName = new QName();
    protected final ArrayList fAttributeList = new ArrayList(5);
    protected DOMConfigurationImpl fConfiguration = null;
    protected Node fCurrentNode = null;
    private XMLDTDValidator fDTDValidator;
    protected CoreDocumentImpl fDocument = null;
    private final DOMErrorImpl fError = new DOMErrorImpl();
    protected DOMErrorHandler fErrorHandler;
    protected final NamespaceContext fLocalNSBinder = new NamespaceSupport();
    protected final DOMLocatorImpl fLocator = new DOMLocatorImpl();
    protected final NamespaceContext fNamespaceContext = new NamespaceSupport();
    protected boolean fNamespaceValidation = false;
    final XMLString fNormalizedValue = new XMLString(new char[16], 0, 0);
    protected boolean fPSVI = false;
    protected final QName fQName = new QName();
    protected SymbolTable fSymbolTable;
    protected RevalidationHandler fValidationHandler;

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void characters(XMLString xMLString, Augmentations augmentations) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void comment(XMLString xMLString, Augmentations augmentations) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void doctypeDecl(String str, String str2, String str3, Augmentations augmentations) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void endCDATA(Augmentations augmentations) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void endDocument(Augmentations augmentations) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void endGeneralEntity(String str, Augmentations augmentations) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public XMLDocumentSource getDocumentSource() {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void processingInstruction(String str, XMLString xMLString, Augmentations augmentations) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void setDocumentSource(XMLDocumentSource xMLDocumentSource) {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void startCDATA(Augmentations augmentations) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void startDocument(XMLLocator xMLLocator, String str, NamespaceContext namespaceContext, Augmentations augmentations) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void startGeneralEntity(String str, XMLResourceIdentifier xMLResourceIdentifier, String str2, Augmentations augmentations) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void textDecl(String str, String str2, Augmentations augmentations) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void xmlDecl(String str, String str2, String str3, Augmentations augmentations) throws XNIException {
    }

    /* access modifiers changed from: protected */
    public void normalizeDocument(CoreDocumentImpl coreDocumentImpl, DOMConfigurationImpl dOMConfigurationImpl) {
        this.fDocument = coreDocumentImpl;
        this.fConfiguration = dOMConfigurationImpl;
        this.fSymbolTable = (SymbolTable) this.fConfiguration.getProperty("http://apache.org/xml/properties/internal/symbol-table");
        this.fNamespaceContext.reset();
        this.fNamespaceContext.declarePrefix(XMLSymbols.EMPTY_STRING, XMLSymbols.EMPTY_STRING);
        if ((this.fConfiguration.features & 64) != 0) {
            String str = (String) this.fConfiguration.getProperty(JAXPConstants.JAXP_SCHEMA_LANGUAGE);
            if (str != null && str.equals(Constants.NS_XMLSCHEMA)) {
                this.fValidationHandler = CoreDOMImplementationImpl.singleton.getValidator("http://www.w3.org/2001/XMLSchema");
                this.fConfiguration.setFeature("http://apache.org/xml/features/validation/schema", true);
                this.fConfiguration.setFeature("http://apache.org/xml/features/validation/schema-full-checking", true);
                this.fNamespaceValidation = true;
                this.fPSVI = (this.fConfiguration.features & 128) != 0;
            }
            this.fConfiguration.setFeature("http://xml.org/sax/features/validation", true);
            this.fDocument.clearIdentifiers();
            RevalidationHandler revalidationHandler = this.fValidationHandler;
            if (revalidationHandler != null) {
                ((XMLComponent) revalidationHandler).reset(this.fConfiguration);
            }
        }
        this.fErrorHandler = (DOMErrorHandler) this.fConfiguration.getParameter(Constants.DOM_ERROR_HANDLER);
        RevalidationHandler revalidationHandler2 = this.fValidationHandler;
        if (revalidationHandler2 != null) {
            revalidationHandler2.setDocumentHandler(this);
            this.fValidationHandler.startDocument(new SimpleLocator(this.fDocument.fDocumentURI, this.fDocument.fDocumentURI, -1, -1), this.fDocument.encoding, this.fNamespaceContext, null);
        }
        try {
            Node firstChild = this.fDocument.getFirstChild();
            while (firstChild != null) {
                Node nextSibling = firstChild.getNextSibling();
                firstChild = normalizeNode(firstChild);
                if (firstChild == null) {
                    firstChild = nextSibling;
                }
            }
            if (this.fValidationHandler != null) {
                this.fValidationHandler.endDocument(null);
                CoreDOMImplementationImpl.singleton.releaseValidator("http://www.w3.org/2001/XMLSchema", this.fValidationHandler);
                this.fValidationHandler = null;
            }
        } catch (AbortException unused) {
        } catch (RuntimeException e) {
            throw e;
        }
    }

    /* access modifiers changed from: protected */
    public Node normalizeNode(Node node) {
        int i;
        boolean z;
        boolean z2;
        TextImpl nextSibling;
        CDATASection cDATASection = node;
        short nodeType = node.getNodeType();
        this.fLocator.fRelatedNode = cDATASection;
        int i2 = 0;
        if (nodeType == 1) {
            if (this.fDocument.errorChecking && (this.fConfiguration.features & 256) != 0 && this.fDocument.isXMLVersionChanged()) {
                if (this.fNamespaceValidation) {
                    z = CoreDocumentImpl.isValidQName(node.getPrefix(), node.getLocalName(), this.fDocument.isXML11Version());
                } else {
                    z = CoreDocumentImpl.isXMLName(node.getNodeName(), this.fDocument.isXML11Version());
                }
                if (!z) {
                    reportDOMError(this.fErrorHandler, this.fError, this.fLocator, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "wf-invalid-character-in-node-name", new Object[]{"Element", node.getNodeName()}), 2, "wf-invalid-character-in-node-name");
                }
            }
            this.fNamespaceContext.pushContext();
            this.fLocalNSBinder.reset();
            ElementImpl elementImpl = (ElementImpl) cDATASection;
            if (elementImpl.needsSyncChildren()) {
                elementImpl.synchronizeChildren();
            }
            AttributeMap attributeMap = elementImpl.hasAttributes() ? (AttributeMap) elementImpl.getAttributes() : null;
            if ((this.fConfiguration.features & 1) != 0) {
                namespaceFixUp(elementImpl, attributeMap);
                if ((this.fConfiguration.features & 512) == 0 && attributeMap != null) {
                    while (i2 < attributeMap.getLength()) {
                        Attr attr = (Attr) attributeMap.getItem(i2);
                        if (XMLSymbols.PREFIX_XMLNS.equals(attr.getPrefix()) || XMLSymbols.PREFIX_XMLNS.equals(attr.getName())) {
                            elementImpl.removeAttributeNode(attr);
                            i2--;
                        }
                        i2++;
                    }
                }
            } else if (attributeMap != null) {
                int i3 = 0;
                while (i3 < attributeMap.getLength()) {
                    AttrImpl attrImpl = (Attr) attributeMap.item(i3);
                    attrImpl.normalize();
                    if (!this.fDocument.errorChecking || (this.fConfiguration.features & 256) == 0) {
                        i = i3;
                    } else {
                        i = i3;
                        isAttrValueWF(this.fErrorHandler, this.fError, this.fLocator, attributeMap, attrImpl, attrImpl.getValue(), this.fDocument.isXML11Version());
                        if (this.fDocument.isXMLVersionChanged() && !CoreDocumentImpl.isXMLName(node.getNodeName(), this.fDocument.isXML11Version())) {
                            reportDOMError(this.fErrorHandler, this.fError, this.fLocator, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "wf-invalid-character-in-node-name", new Object[]{"Attr", node.getNodeName()}), 2, "wf-invalid-character-in-node-name");
                        }
                    }
                    i3 = i + 1;
                }
            }
            if (this.fValidationHandler != null) {
                this.fAttrProxy.setAttributes(attributeMap, this.fDocument, elementImpl);
                updateQName(elementImpl, this.fQName);
                this.fConfiguration.fErrorHandlerWrapper.fCurrentNode = cDATASection;
                this.fCurrentNode = cDATASection;
                this.fValidationHandler.startElement(this.fQName, this.fAttrProxy, null);
            }
            if (this.fDTDValidator != null) {
                this.fAttrProxy.setAttributes(attributeMap, this.fDocument, elementImpl);
                updateQName(elementImpl, this.fQName);
                this.fConfiguration.fErrorHandlerWrapper.fCurrentNode = cDATASection;
                this.fCurrentNode = cDATASection;
                this.fDTDValidator.startElement(this.fQName, this.fAttrProxy, null);
            }
            Node firstChild = elementImpl.getFirstChild();
            while (firstChild != null) {
                Node nextSibling2 = firstChild.getNextSibling();
                firstChild = normalizeNode(firstChild);
                if (firstChild == null) {
                    firstChild = nextSibling2;
                }
            }
            if (this.fValidationHandler != null) {
                updateQName(elementImpl, this.fQName);
                this.fConfiguration.fErrorHandlerWrapper.fCurrentNode = cDATASection;
                this.fCurrentNode = cDATASection;
                this.fValidationHandler.endElement(this.fQName, null);
            }
            if (this.fDTDValidator != null) {
                updateQName(elementImpl, this.fQName);
                this.fConfiguration.fErrorHandlerWrapper.fCurrentNode = cDATASection;
                this.fCurrentNode = cDATASection;
                this.fDTDValidator.endElement(this.fQName, null);
            }
            this.fNamespaceContext.popContext();
        } else if (nodeType == 10) {
            DocumentTypeImpl documentTypeImpl = (DocumentTypeImpl) cDATASection;
            this.fDTDValidator = (XMLDTDValidator) CoreDOMImplementationImpl.singleton.getValidator(XMLGrammarDescription.XML_DTD);
            this.fDTDValidator.setDocumentHandler(this);
            this.fConfiguration.setProperty("http://apache.org/xml/properties/internal/grammar-pool", createGrammarPool(documentTypeImpl));
            this.fDTDValidator.reset(this.fConfiguration);
            this.fDTDValidator.startDocument(new SimpleLocator(this.fDocument.fDocumentURI, this.fDocument.fDocumentURI, -1, -1), this.fDocument.encoding, this.fNamespaceContext, null);
            this.fDTDValidator.doctypeDecl(documentTypeImpl.getName(), documentTypeImpl.getPublicId(), documentTypeImpl.getSystemId(), null);
        } else if (nodeType == 3) {
            Node nextSibling3 = node.getNextSibling();
            if (nextSibling3 != null && nextSibling3.getNodeType() == 3) {
                ((Text) cDATASection).appendData(nextSibling3.getNodeValue());
                node.getParentNode().removeChild(nextSibling3);
                return cDATASection;
            } else if (node.getNodeValue().length() == 0) {
                node.getParentNode().removeChild(cDATASection);
            } else {
                short nodeType2 = nextSibling3 != null ? nextSibling3.getNodeType() : -1;
                if (nodeType2 == -1 || !(((this.fConfiguration.features & 4) == 0 && nodeType2 == 6) || (((this.fConfiguration.features & 32) == 0 && nodeType2 == 8) || ((this.fConfiguration.features & 8) == 0 && nodeType2 == 4)))) {
                    if (this.fDocument.errorChecking && (this.fConfiguration.features & 256) != 0) {
                        isXMLCharWF(this.fErrorHandler, this.fError, this.fLocator, node.getNodeValue(), this.fDocument.isXML11Version());
                    }
                    if (this.fValidationHandler != null) {
                        this.fConfiguration.fErrorHandlerWrapper.fCurrentNode = cDATASection;
                        this.fCurrentNode = cDATASection;
                        this.fValidationHandler.characterData(node.getNodeValue(), null);
                    }
                    if (this.fDTDValidator != null) {
                        this.fConfiguration.fErrorHandlerWrapper.fCurrentNode = cDATASection;
                        this.fCurrentNode = cDATASection;
                        this.fDTDValidator.characterData(node.getNodeValue(), null);
                        if (this.allWhitespace) {
                            this.allWhitespace = false;
                            ((TextImpl) cDATASection).setIgnorableWhitespace(true);
                        }
                    }
                }
            }
        } else if (nodeType != 4) {
            if (nodeType != 5) {
                if (nodeType != 7) {
                    if (nodeType == 8) {
                        if ((this.fConfiguration.features & 32) == 0) {
                            Node previousSibling = node.getPreviousSibling();
                            Node parentNode = node.getParentNode();
                            parentNode.removeChild(cDATASection);
                            if (previousSibling != null && previousSibling.getNodeType() == 3 && (nextSibling = previousSibling.getNextSibling()) != null && nextSibling.getNodeType() == 3) {
                                nextSibling.insertData(0, previousSibling.getNodeValue());
                                parentNode.removeChild(previousSibling);
                                return nextSibling;
                            }
                        } else if (this.fDocument.errorChecking && (this.fConfiguration.features & 256) != 0) {
                            isCommentWF(this.fErrorHandler, this.fError, this.fLocator, ((Comment) cDATASection).getData(), this.fDocument.isXML11Version());
                        }
                    }
                } else if (this.fDocument.errorChecking && (this.fConfiguration.features & 256) != 0) {
                    ProcessingInstruction processingInstruction = (ProcessingInstruction) cDATASection;
                    String target = processingInstruction.getTarget();
                    if (this.fDocument.isXML11Version()) {
                        z2 = XML11Char.isXML11ValidName(target);
                    } else {
                        z2 = XMLChar.isValidName(target);
                    }
                    if (!z2) {
                        reportDOMError(this.fErrorHandler, this.fError, this.fLocator, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "wf-invalid-character-in-node-name", new Object[]{"Element", node.getNodeName()}), 2, "wf-invalid-character-in-node-name");
                    }
                    isXMLCharWF(this.fErrorHandler, this.fError, this.fLocator, processingInstruction.getData(), this.fDocument.isXML11Version());
                }
            } else if ((this.fConfiguration.features & 4) == 0) {
                Node previousSibling2 = node.getPreviousSibling();
                Node parentNode2 = node.getParentNode();
                ((EntityReferenceImpl) cDATASection).setReadOnly(false, true);
                expandEntityRef(parentNode2, cDATASection);
                parentNode2.removeChild(cDATASection);
                Node nextSibling4 = previousSibling2 != null ? previousSibling2.getNextSibling() : parentNode2.getFirstChild();
                return (previousSibling2 == null || nextSibling4 == null || previousSibling2.getNodeType() != 3 || nextSibling4.getNodeType() != 3) ? nextSibling4 : previousSibling2;
            } else if (this.fDocument.errorChecking && (this.fConfiguration.features & 256) != 0 && this.fDocument.isXMLVersionChanged()) {
                CoreDocumentImpl.isXMLName(node.getNodeName(), this.fDocument.isXML11Version());
            }
        } else if ((this.fConfiguration.features & 8) == 0) {
            Text previousSibling3 = node.getPreviousSibling();
            if (previousSibling3 == null || previousSibling3.getNodeType() != 3) {
                Text createTextNode = this.fDocument.createTextNode(node.getNodeValue());
                node.getParentNode().replaceChild(createTextNode, cDATASection);
                return createTextNode;
            }
            previousSibling3.appendData(node.getNodeValue());
            node.getParentNode().removeChild(cDATASection);
            return previousSibling3;
        } else {
            if (this.fValidationHandler != null) {
                this.fConfiguration.fErrorHandlerWrapper.fCurrentNode = cDATASection;
                this.fCurrentNode = cDATASection;
                this.fValidationHandler.startCDATA(null);
                this.fValidationHandler.characterData(node.getNodeValue(), null);
                this.fValidationHandler.endCDATA(null);
            }
            if (this.fDTDValidator != null) {
                this.fConfiguration.fErrorHandlerWrapper.fCurrentNode = cDATASection;
                this.fCurrentNode = cDATASection;
                this.fDTDValidator.startCDATA(null);
                this.fDTDValidator.characterData(node.getNodeValue(), null);
                this.fDTDValidator.endCDATA(null);
            }
            String nodeValue = node.getNodeValue();
            if ((this.fConfiguration.features & 16) != 0) {
                Node parentNode3 = node.getParentNode();
                if (this.fDocument.errorChecking) {
                    isXMLCharWF(this.fErrorHandler, this.fError, this.fLocator, node.getNodeValue(), this.fDocument.isXML11Version());
                }
                while (true) {
                    int indexOf = nodeValue.indexOf("]]>");
                    if (indexOf < 0) {
                        break;
                    }
                    int i4 = indexOf + 2;
                    cDATASection.setNodeValue(nodeValue.substring(0, i4));
                    nodeValue = nodeValue.substring(i4);
                    CDATASection createCDATASection = this.fDocument.createCDATASection(nodeValue);
                    parentNode3.insertBefore(createCDATASection, cDATASection.getNextSibling());
                    this.fLocator.fRelatedNode = cDATASection;
                    reportDOMError(this.fErrorHandler, this.fError, this.fLocator, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "cdata-sections-splitted", null), 1, "cdata-sections-splitted");
                    cDATASection = createCDATASection;
                }
            } else if (this.fDocument.errorChecking) {
                isCDataWF(this.fErrorHandler, this.fError, this.fLocator, nodeValue, this.fDocument.isXML11Version());
            }
        }
        return null;
    }

    private XMLGrammarPool createGrammarPool(DocumentTypeImpl documentTypeImpl) {
        XMLGrammarPoolImpl xMLGrammarPoolImpl = new XMLGrammarPoolImpl();
        XMLGrammarPreparser xMLGrammarPreparser = new XMLGrammarPreparser(this.fSymbolTable);
        xMLGrammarPreparser.registerPreparser(XMLGrammarDescription.XML_DTD, null);
        xMLGrammarPreparser.setFeature("http://apache.org/xml/features/namespaces", true);
        xMLGrammarPreparser.setFeature("http://apache.org/xml/features/validation", true);
        xMLGrammarPreparser.setProperty("http://apache.org/xml/properties/internal/grammar-pool", xMLGrammarPoolImpl);
        String internalSubset = documentTypeImpl.getInternalSubset();
        XMLInputSource xMLInputSource = new XMLInputSource(documentTypeImpl.getPublicId(), documentTypeImpl.getSystemId(), null);
        if (internalSubset != null) {
            xMLInputSource.setCharacterStream(new StringReader(internalSubset));
        }
        try {
            ((XMLDTDDescription) ((DTDGrammar) xMLGrammarPreparser.preparseGrammar(XMLGrammarDescription.XML_DTD, xMLInputSource)).getGrammarDescription()).setRootName(documentTypeImpl.getName());
            xMLInputSource.setCharacterStream(null);
            ((XMLDTDDescription) ((DTDGrammar) xMLGrammarPreparser.preparseGrammar(XMLGrammarDescription.XML_DTD, xMLInputSource)).getGrammarDescription()).setRootName(documentTypeImpl.getName());
        } catch (IOException | XNIException unused) {
        }
        return xMLGrammarPoolImpl;
    }

    /* access modifiers changed from: protected */
    public final void expandEntityRef(Node node, Node node2) {
        Node firstChild = node2.getFirstChild();
        while (firstChild != null) {
            Node nextSibling = firstChild.getNextSibling();
            node.insertBefore(firstChild, node2);
            firstChild = nextSibling;
        }
    }

    /* access modifiers changed from: protected */
    public final void namespaceFixUp(ElementImpl elementImpl, AttributeMap attributeMap) {
        boolean z;
        String str;
        String str2;
        int i;
        if (attributeMap != null) {
            for (int i2 = 0; i2 < attributeMap.getLength(); i2++) {
                Attr attr = (Attr) attributeMap.getItem(i2);
                if (this.fDocument.errorChecking && (this.fConfiguration.features & 256) != 0 && this.fDocument.isXMLVersionChanged()) {
                    this.fDocument.checkQName(attr.getPrefix(), attr.getLocalName());
                }
                String namespaceURI = attr.getNamespaceURI();
                if (!(namespaceURI == null || !namespaceURI.equals(NamespaceContext.XMLNS_URI) || (this.fConfiguration.features & 512) == 0)) {
                    String nodeValue = attr.getNodeValue();
                    if (nodeValue == null) {
                        nodeValue = XMLSymbols.EMPTY_STRING;
                    }
                    if (!this.fDocument.errorChecking || !nodeValue.equals(NamespaceContext.XMLNS_URI)) {
                        String prefix = attr.getPrefix();
                        String addSymbol = (prefix == null || prefix.length() == 0) ? XMLSymbols.EMPTY_STRING : this.fSymbolTable.addSymbol(prefix);
                        String addSymbol2 = this.fSymbolTable.addSymbol(attr.getLocalName());
                        if (addSymbol == XMLSymbols.PREFIX_XMLNS) {
                            String addSymbol3 = this.fSymbolTable.addSymbol(nodeValue);
                            if (addSymbol3.length() != 0) {
                                this.fNamespaceContext.declarePrefix(addSymbol2, addSymbol3);
                            }
                        } else {
                            this.fNamespaceContext.declarePrefix(XMLSymbols.EMPTY_STRING, this.fSymbolTable.addSymbol(nodeValue));
                        }
                    } else {
                        this.fLocator.fRelatedNode = attr;
                        reportDOMError(this.fErrorHandler, this.fError, this.fLocator, DOMMessageFormatter.formatMessage("http://www.w3.org/TR/1998/REC-xml-19980210", "CantBindXMLNS", null), 2, "CantBindXMLNS");
                    }
                }
            }
        }
        String namespaceURI2 = elementImpl.getNamespaceURI();
        String prefix2 = elementImpl.getPrefix();
        boolean z2 = true;
        if ((this.fConfiguration.features & 512) != 0) {
            if (namespaceURI2 != null) {
                String addSymbol4 = this.fSymbolTable.addSymbol(namespaceURI2);
                String addSymbol5 = (prefix2 == null || prefix2.length() == 0) ? XMLSymbols.EMPTY_STRING : this.fSymbolTable.addSymbol(prefix2);
                if (this.fNamespaceContext.getURI(addSymbol5) != addSymbol4) {
                    addNamespaceDecl(addSymbol5, addSymbol4, elementImpl);
                    this.fLocalNSBinder.declarePrefix(addSymbol5, addSymbol4);
                    this.fNamespaceContext.declarePrefix(addSymbol5, addSymbol4);
                }
            } else if (elementImpl.getLocalName() != null) {
                String uri = this.fNamespaceContext.getURI(XMLSymbols.EMPTY_STRING);
                if (uri != null && uri.length() > 0) {
                    addNamespaceDecl(XMLSymbols.EMPTY_STRING, XMLSymbols.EMPTY_STRING, elementImpl);
                    this.fLocalNSBinder.declarePrefix(XMLSymbols.EMPTY_STRING, XMLSymbols.EMPTY_STRING);
                    this.fNamespaceContext.declarePrefix(XMLSymbols.EMPTY_STRING, XMLSymbols.EMPTY_STRING);
                }
            } else if (this.fNamespaceValidation) {
                reportDOMError(this.fErrorHandler, this.fError, this.fLocator, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NullLocalElementName", new Object[]{elementImpl.getNodeName()}), 3, "NullLocalElementName");
            } else {
                reportDOMError(this.fErrorHandler, this.fError, this.fLocator, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NullLocalElementName", new Object[]{elementImpl.getNodeName()}), 2, "NullLocalElementName");
            }
        }
        if (attributeMap != null) {
            attributeMap.cloneMap(this.fAttributeList);
            int i3 = 0;
            while (i3 < this.fAttributeList.size()) {
                AttrImpl attrImpl = (Attr) this.fAttributeList.get(i3);
                this.fLocator.fRelatedNode = attrImpl;
                attrImpl.normalize();
                String value = attrImpl.getValue();
                attrImpl.getNodeName();
                String namespaceURI3 = attrImpl.getNamespaceURI();
                if (value == null) {
                    value = XMLSymbols.EMPTY_STRING;
                }
                if (namespaceURI3 != null) {
                    String prefix3 = attrImpl.getPrefix();
                    String addSymbol6 = (prefix3 == null || prefix3.length() == 0) ? XMLSymbols.EMPTY_STRING : this.fSymbolTable.addSymbol(prefix3);
                    this.fSymbolTable.addSymbol(attrImpl.getLocalName());
                    if (namespaceURI3.equals(NamespaceContext.XMLNS_URI)) {
                        z = z2;
                        i3++;
                        z2 = z;
                    } else {
                        if (!this.fDocument.errorChecking || (this.fConfiguration.features & 256) == 0) {
                            i = 2;
                            str2 = addSymbol6;
                            str = value;
                        } else {
                            i = 2;
                            str2 = addSymbol6;
                            str = value;
                            isAttrValueWF(this.fErrorHandler, this.fError, this.fLocator, attributeMap, attrImpl, attrImpl.getValue(), this.fDocument.isXML11Version());
                            if (this.fDocument.isXMLVersionChanged() && !CoreDocumentImpl.isXMLName(attrImpl.getNodeName(), this.fDocument.isXML11Version())) {
                                reportDOMError(this.fErrorHandler, this.fError, this.fLocator, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "wf-invalid-character-in-node-name", new Object[]{"Attribute", attrImpl.getNodeName()}), 2, "wf-invalid-character-in-node-name");
                            }
                        }
                        attrImpl.setIdAttribute(false);
                        String addSymbol7 = this.fSymbolTable.addSymbol(namespaceURI3);
                        String uri2 = this.fNamespaceContext.getURI(str2);
                        if (str2 == XMLSymbols.EMPTY_STRING || uri2 != addSymbol7) {
                            attrImpl.getNodeName();
                            String prefix4 = this.fNamespaceContext.getPrefix(addSymbol7);
                            if (prefix4 == null || prefix4 == XMLSymbols.EMPTY_STRING) {
                                if (str2 == XMLSymbols.EMPTY_STRING || this.fLocalNSBinder.getURI(str2) != null) {
                                    String addSymbol8 = this.fSymbolTable.addSymbol(PREFIX + 1);
                                    while (this.fLocalNSBinder.getURI(addSymbol8) != null) {
                                        addSymbol8 = this.fSymbolTable.addSymbol(PREFIX + i);
                                        i++;
                                    }
                                    prefix4 = addSymbol8;
                                } else {
                                    prefix4 = str2;
                                }
                                addNamespaceDecl(prefix4, addSymbol7, elementImpl);
                                this.fLocalNSBinder.declarePrefix(prefix4, this.fSymbolTable.addSymbol(str));
                                this.fNamespaceContext.declarePrefix(prefix4, addSymbol7);
                            }
                            attrImpl.setPrefix(prefix4);
                        }
                    }
                } else {
                    attrImpl.setIdAttribute(false);
                    if (attrImpl.getLocalName() == null) {
                        if (this.fNamespaceValidation) {
                            reportDOMError(this.fErrorHandler, this.fError, this.fLocator, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NullLocalAttrName", new Object[]{attrImpl.getNodeName()}), 3, "NullLocalAttrName");
                        } else {
                            z = true;
                            reportDOMError(this.fErrorHandler, this.fError, this.fLocator, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NullLocalAttrName", new Object[]{attrImpl.getNodeName()}), 2, "NullLocalAttrName");
                            i3++;
                            z2 = z;
                        }
                    }
                }
                z = true;
                i3++;
                z2 = z;
            }
        }
    }

    /* access modifiers changed from: protected */
    public final void addNamespaceDecl(String str, String str2, ElementImpl elementImpl) {
        if (str == XMLSymbols.EMPTY_STRING) {
            elementImpl.setAttributeNS(NamespaceContext.XMLNS_URI, XMLSymbols.PREFIX_XMLNS, str2);
            return;
        }
        String str3 = NamespaceContext.XMLNS_URI;
        elementImpl.setAttributeNS(str3, "xmlns:" + str, str2);
    }

    public static final void isCDataWF(DOMErrorHandler dOMErrorHandler, DOMErrorImpl dOMErrorImpl, DOMLocatorImpl dOMLocatorImpl, String str, boolean z) {
        if (str != null && str.length() != 0) {
            char[] charArray = str.toCharArray();
            int length = charArray.length;
            if (z) {
                int i = 0;
                while (i < length) {
                    int i2 = i + 1;
                    char c = charArray[i];
                    if (XML11Char.isXML11Invalid(c)) {
                        if (XMLChar.isHighSurrogate(c) && i2 < length) {
                            int i3 = i2 + 1;
                            char c2 = charArray[i2];
                            if (!XMLChar.isLowSurrogate(c2) || !XMLChar.isSupplemental(XMLChar.supplemental(c, c2))) {
                                i2 = i3;
                            } else {
                                i = i3;
                            }
                        }
                        reportDOMError(dOMErrorHandler, dOMErrorImpl, dOMLocatorImpl, DOMMessageFormatter.formatMessage("http://www.w3.org/TR/1998/REC-xml-19980210", "InvalidCharInCDSect", new Object[]{Integer.toString(c, 16)}), 2, "wf-invalid-character");
                    } else if (c == ']' && i2 < length && charArray[i2] == ']') {
                        int i4 = i2;
                        do {
                            i4++;
                            if (i4 >= length) {
                                break;
                            }
                        } while (charArray[i4] == ']');
                        if (i4 < length && charArray[i4] == '>') {
                            reportDOMError(dOMErrorHandler, dOMErrorImpl, dOMLocatorImpl, DOMMessageFormatter.formatMessage("http://www.w3.org/TR/1998/REC-xml-19980210", "CDEndInContent", null), 2, "wf-invalid-character");
                        }
                    }
                    i = i2;
                }
                return;
            }
            int i5 = 0;
            while (i5 < length) {
                int i6 = i5 + 1;
                char c3 = charArray[i5];
                if (XMLChar.isInvalid(c3)) {
                    if (XMLChar.isHighSurrogate(c3) && i6 < length) {
                        int i7 = i6 + 1;
                        char c4 = charArray[i6];
                        if (!XMLChar.isLowSurrogate(c4) || !XMLChar.isSupplemental(XMLChar.supplemental(c3, c4))) {
                            i6 = i7;
                        } else {
                            i5 = i7;
                        }
                    }
                    reportDOMError(dOMErrorHandler, dOMErrorImpl, dOMLocatorImpl, DOMMessageFormatter.formatMessage("http://www.w3.org/TR/1998/REC-xml-19980210", "InvalidCharInCDSect", new Object[]{Integer.toString(c3, 16)}), 2, "wf-invalid-character");
                } else if (c3 == ']' && i6 < length && charArray[i6] == ']') {
                    int i8 = i6;
                    do {
                        i8++;
                        if (i8 >= length) {
                            break;
                        }
                    } while (charArray[i8] == ']');
                    if (i8 < length && charArray[i8] == '>') {
                        reportDOMError(dOMErrorHandler, dOMErrorImpl, dOMLocatorImpl, DOMMessageFormatter.formatMessage("http://www.w3.org/TR/1998/REC-xml-19980210", "CDEndInContent", null), 2, "wf-invalid-character");
                    }
                }
                i5 = i6;
            }
        }
    }

    public static final void isXMLCharWF(DOMErrorHandler dOMErrorHandler, DOMErrorImpl dOMErrorImpl, DOMLocatorImpl dOMLocatorImpl, String str, boolean z) {
        if (str != null && str.length() != 0) {
            char[] charArray = str.toCharArray();
            int length = charArray.length;
            if (z) {
                int i = 0;
                while (i < length) {
                    int i2 = i + 1;
                    if (XML11Char.isXML11Invalid(charArray[i])) {
                        char c = charArray[i2 - 1];
                        if (!XMLChar.isHighSurrogate(c) || i2 >= length) {
                            i = i2;
                        } else {
                            int i3 = i2 + 1;
                            char c2 = charArray[i2];
                            if (!XMLChar.isLowSurrogate(c2) || !XMLChar.isSupplemental(XMLChar.supplemental(c, c2))) {
                                i = i3;
                            } else {
                                i = i3;
                            }
                        }
                        reportDOMError(dOMErrorHandler, dOMErrorImpl, dOMLocatorImpl, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "InvalidXMLCharInDOM", new Object[]{Integer.toString(charArray[i - 1], 16)}), 2, "wf-invalid-character");
                    } else {
                        i = i2;
                    }
                }
                return;
            }
            int i4 = 0;
            while (i4 < length) {
                int i5 = i4 + 1;
                if (XMLChar.isInvalid(charArray[i4])) {
                    char c3 = charArray[i5 - 1];
                    if (!XMLChar.isHighSurrogate(c3) || i5 >= length) {
                        i4 = i5;
                    } else {
                        int i6 = i5 + 1;
                        char c4 = charArray[i5];
                        if (!XMLChar.isLowSurrogate(c4) || !XMLChar.isSupplemental(XMLChar.supplemental(c3, c4))) {
                            i4 = i6;
                        } else {
                            i4 = i6;
                        }
                    }
                    reportDOMError(dOMErrorHandler, dOMErrorImpl, dOMLocatorImpl, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "InvalidXMLCharInDOM", new Object[]{Integer.toString(charArray[i4 - 1], 16)}), 2, "wf-invalid-character");
                } else {
                    i4 = i5;
                }
            }
        }
    }

    public static final void isCommentWF(DOMErrorHandler dOMErrorHandler, DOMErrorImpl dOMErrorImpl, DOMLocatorImpl dOMLocatorImpl, String str, boolean z) {
        if (str != null && str.length() != 0) {
            char[] charArray = str.toCharArray();
            int length = charArray.length;
            if (z) {
                int i = 0;
                while (i < length) {
                    int i2 = i + 1;
                    char c = charArray[i];
                    if (XML11Char.isXML11Invalid(c)) {
                        if (XMLChar.isHighSurrogate(c) && i2 < length) {
                            int i3 = i2 + 1;
                            char c2 = charArray[i2];
                            if (!XMLChar.isLowSurrogate(c2) || !XMLChar.isSupplemental(XMLChar.supplemental(c, c2))) {
                                i2 = i3;
                            } else {
                                i = i3;
                            }
                        }
                        reportDOMError(dOMErrorHandler, dOMErrorImpl, dOMLocatorImpl, DOMMessageFormatter.formatMessage("http://www.w3.org/TR/1998/REC-xml-19980210", "InvalidCharInComment", new Object[]{Integer.toString(charArray[i2 - 1], 16)}), 2, "wf-invalid-character");
                    } else if (c == '-' && i2 < length && charArray[i2] == '-') {
                        reportDOMError(dOMErrorHandler, dOMErrorImpl, dOMLocatorImpl, DOMMessageFormatter.formatMessage("http://www.w3.org/TR/1998/REC-xml-19980210", "DashDashInComment", null), 2, "wf-invalid-character");
                    }
                    i = i2;
                }
                return;
            }
            int i4 = 0;
            while (i4 < length) {
                int i5 = i4 + 1;
                char c3 = charArray[i4];
                if (XMLChar.isInvalid(c3)) {
                    if (XMLChar.isHighSurrogate(c3) && i5 < length) {
                        int i6 = i5 + 1;
                        char c4 = charArray[i5];
                        if (!XMLChar.isLowSurrogate(c4) || !XMLChar.isSupplemental(XMLChar.supplemental(c3, c4))) {
                            i5 = i6;
                        } else {
                            i4 = i6;
                        }
                    }
                    reportDOMError(dOMErrorHandler, dOMErrorImpl, dOMLocatorImpl, DOMMessageFormatter.formatMessage("http://www.w3.org/TR/1998/REC-xml-19980210", "InvalidCharInComment", new Object[]{Integer.toString(charArray[i5 - 1], 16)}), 2, "wf-invalid-character");
                } else if (c3 == '-' && i5 < length && charArray[i5] == '-') {
                    reportDOMError(dOMErrorHandler, dOMErrorImpl, dOMLocatorImpl, DOMMessageFormatter.formatMessage("http://www.w3.org/TR/1998/REC-xml-19980210", "DashDashInComment", null), 2, "wf-invalid-character");
                }
                i4 = i5;
            }
        }
    }

    public static final void isAttrValueWF(DOMErrorHandler dOMErrorHandler, DOMErrorImpl dOMErrorImpl, DOMLocatorImpl dOMLocatorImpl, NamedNodeMap namedNodeMap, Attr attr, String str, boolean z) {
        DocumentType doctype;
        if (!(attr instanceof AttrImpl) || !((AttrImpl) attr).hasStringValue()) {
            NodeList childNodes = attr.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node item = childNodes.item(i);
                if (item.getNodeType() == 5) {
                    Document ownerDocument = attr.getOwnerDocument();
                    Entity entity = null;
                    if (!(ownerDocument == null || (doctype = ownerDocument.getDoctype()) == null)) {
                        entity = (Entity) doctype.getEntities().getNamedItemNS("*", item.getNodeName());
                    }
                    if (entity == null) {
                        reportDOMError(dOMErrorHandler, dOMErrorImpl, dOMLocatorImpl, DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "UndeclaredEntRefInAttrValue", new Object[]{attr.getNodeName()}), 2, "UndeclaredEntRefInAttrValue");
                    }
                } else {
                    isXMLCharWF(dOMErrorHandler, dOMErrorImpl, dOMLocatorImpl, item.getNodeValue(), z);
                }
            }
            return;
        }
        isXMLCharWF(dOMErrorHandler, dOMErrorImpl, dOMLocatorImpl, str, z);
    }

    public static final void reportDOMError(DOMErrorHandler dOMErrorHandler, DOMErrorImpl dOMErrorImpl, DOMLocatorImpl dOMLocatorImpl, String str, short s, String str2) {
        if (dOMErrorHandler != null) {
            dOMErrorImpl.reset();
            dOMErrorImpl.fMessage = str;
            dOMErrorImpl.fSeverity = s;
            dOMErrorImpl.fLocator = dOMLocatorImpl;
            dOMErrorImpl.fType = str2;
            dOMErrorImpl.fRelatedData = dOMLocatorImpl.fRelatedNode;
            if (!dOMErrorHandler.handleError(dOMErrorImpl)) {
                throw new AbortException();
            }
        }
        if (s == 3) {
            throw new AbortException();
        }
    }

    /* access modifiers changed from: protected */
    public final void updateQName(Node node, QName qName) {
        String prefix = node.getPrefix();
        String namespaceURI = node.getNamespaceURI();
        String localName = node.getLocalName();
        String str = null;
        qName.prefix = (prefix == null || prefix.length() == 0) ? null : this.fSymbolTable.addSymbol(prefix);
        qName.localpart = localName != null ? this.fSymbolTable.addSymbol(localName) : null;
        qName.rawname = this.fSymbolTable.addSymbol(node.getNodeName());
        if (namespaceURI != null) {
            str = this.fSymbolTable.addSymbol(namespaceURI);
        }
        qName.uri = str;
    }

    /* access modifiers changed from: package-private */
    public final String normalizeAttributeValue(String str, Attr attr) {
        if (!attr.getSpecified()) {
            return str;
        }
        int length = str.length();
        if (this.fNormalizedValue.ch.length < length) {
            this.fNormalizedValue.ch = new char[length];
        }
        int i = 0;
        this.fNormalizedValue.length = 0;
        boolean z = false;
        while (i < length) {
            char charAt = str.charAt(i);
            if (charAt == '\t' || charAt == '\n') {
                char[] cArr = this.fNormalizedValue.ch;
                XMLString xMLString = this.fNormalizedValue;
                int i2 = xMLString.length;
                xMLString.length = i2 + 1;
                cArr[i2] = ' ';
            } else if (charAt == '\r') {
                char[] cArr2 = this.fNormalizedValue.ch;
                XMLString xMLString2 = this.fNormalizedValue;
                int i3 = xMLString2.length;
                xMLString2.length = i3 + 1;
                cArr2[i3] = ' ';
                int i4 = i + 1;
                if (i4 < length && str.charAt(i4) == '\n') {
                    i = i4;
                }
            } else {
                char[] cArr3 = this.fNormalizedValue.ch;
                XMLString xMLString3 = this.fNormalizedValue;
                int i5 = xMLString3.length;
                xMLString3.length = i5 + 1;
                cArr3[i5] = charAt;
                i++;
            }
            z = true;
            i++;
        }
        if (!z) {
            return str;
        }
        String xMLString4 = this.fNormalizedValue.toString();
        attr.setValue(xMLString4);
        return xMLString4;
    }

    /* access modifiers changed from: protected */
    public final class XMLAttributesProxy implements XMLAttributes {
        protected AttributeMap fAttributes;
        protected final Vector fAugmentations = new Vector(5);
        protected CoreDocumentImpl fDocument;
        protected ElementImpl fElement;

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes
        public Augmentations getAugmentations(String str) {
            return null;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes
        public Augmentations getAugmentations(String str, String str2) {
            return null;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes
        public int getIndex(String str) {
            return -1;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes
        public int getIndex(String str, String str2) {
            return -1;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes
        public String getLocalName(int i) {
            return null;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes
        public String getNonNormalizedValue(int i) {
            return null;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes
        public String getPrefix(int i) {
            return null;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes
        public String getQName(int i) {
            return null;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes
        public QName getQualifiedName(int i) {
            return null;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes
        public String getType(int i) {
            return "CDATA";
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes
        public String getType(String str) {
            return "CDATA";
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes
        public String getType(String str, String str2) {
            return "CDATA";
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes
        public String getURI(int i) {
            return null;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes
        public String getValue(String str) {
            return null;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes
        public void removeAllAttributes() {
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes
        public void removeAttributeAt(int i) {
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes
        public void setName(int i, QName qName) {
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes
        public void setNonNormalizedValue(int i, String str) {
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes
        public void setType(int i, String str) {
        }

        protected XMLAttributesProxy() {
        }

        public void setAttributes(AttributeMap attributeMap, CoreDocumentImpl coreDocumentImpl, ElementImpl elementImpl) {
            this.fDocument = coreDocumentImpl;
            this.fAttributes = attributeMap;
            this.fElement = elementImpl;
            if (attributeMap != null) {
                int length = attributeMap.getLength();
                this.fAugmentations.setSize(length);
                for (int i = 0; i < length; i++) {
                    this.fAugmentations.setElementAt(new AugmentationsImpl(), i);
                }
                return;
            }
            this.fAugmentations.setSize(0);
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes
        public int addAttribute(QName qName, String str, String str2) {
            int xercesAttribute = this.fElement.getXercesAttribute(qName.uri, qName.localpart);
            if (xercesAttribute >= 0) {
                return xercesAttribute;
            }
            Attr attr = (AttrImpl) this.fElement.getOwnerDocument().createAttributeNS(qName.uri, qName.rawname, qName.localpart);
            attr.setNodeValue(str2);
            int xercesAttributeNode = this.fElement.setXercesAttributeNode(attr);
            this.fAugmentations.insertElementAt(new AugmentationsImpl(), xercesAttributeNode);
            attr.setSpecified(false);
            return xercesAttributeNode;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes
        public int getLength() {
            AttributeMap attributeMap = this.fAttributes;
            if (attributeMap != null) {
                return attributeMap.getLength();
            }
            return 0;
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes
        public void getName(int i, QName qName) {
            AttributeMap attributeMap = this.fAttributes;
            if (attributeMap != null) {
                DOMNormalizer.this.updateQName((Node) attributeMap.getItem(i), qName);
            }
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes
        public void setValue(int i, String str) {
            AttributeMap attributeMap = this.fAttributes;
            if (attributeMap != null) {
                AttrImpl attrImpl = (AttrImpl) attributeMap.getItem(i);
                boolean specified = attrImpl.getSpecified();
                attrImpl.setValue(str);
                attrImpl.setSpecified(specified);
            }
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes
        public void setValue(int i, String str, XMLString xMLString) {
            setValue(i, xMLString.toString());
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes
        public String getValue(int i) {
            AttributeMap attributeMap = this.fAttributes;
            return attributeMap != null ? attributeMap.item(i).getNodeValue() : "";
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes
        public String getValue(String str, String str2) {
            Node namedItemNS;
            AttributeMap attributeMap = this.fAttributes;
            if (attributeMap == null || (namedItemNS = attributeMap.getNamedItemNS(str, str2)) == null) {
                return null;
            }
            return namedItemNS.getNodeValue();
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes
        public void setSpecified(int i, boolean z) {
            ((AttrImpl) this.fAttributes.getItem(i)).setSpecified(z);
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes
        public boolean isSpecified(int i) {
            return ((Attr) this.fAttributes.getItem(i)).getSpecified();
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes
        public Augmentations getAugmentations(int i) {
            return (Augmentations) this.fAugmentations.elementAt(i);
        }

        @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes
        public void setAugmentations(int i, Augmentations augmentations) {
            this.fAugmentations.setElementAt(augmentations, i);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void startElement(QName qName, XMLAttributes xMLAttributes, Augmentations augmentations) throws XNIException {
        boolean z;
        ElementImpl elementImpl = (Element) this.fCurrentNode;
        int length = xMLAttributes.getLength();
        for (int i = 0; i < length; i++) {
            xMLAttributes.getName(i, this.fAttrQName);
            Attr attributeNodeNS = elementImpl.getAttributeNodeNS(this.fAttrQName.uri, this.fAttrQName.localpart);
            AttributePSVI attributePSVI = (AttributePSVI) xMLAttributes.getAugmentations(i).getItem(Constants.ATTRIBUTE_PSVI);
            if (attributePSVI != null) {
                XSSimpleTypeDefinition memberTypeDefinition = attributePSVI.getMemberTypeDefinition();
                if (memberTypeDefinition != null) {
                    z = ((XSSimpleType) memberTypeDefinition).isIDType();
                } else {
                    XSTypeDefinition typeDefinition = attributePSVI.getTypeDefinition();
                    z = typeDefinition != null ? ((XSSimpleType) typeDefinition).isIDType() : false;
                }
                if (z) {
                    elementImpl.setIdAttributeNode(attributeNodeNS, true);
                }
                if (this.fPSVI) {
                    ((PSVIAttrNSImpl) attributeNodeNS).setPSVI(attributePSVI);
                }
                if ((this.fConfiguration.features & 2) != 0) {
                    boolean specified = attributeNodeNS.getSpecified();
                    attributeNodeNS.setValue(attributePSVI.getSchemaNormalizedValue());
                    if (!specified) {
                        ((AttrImpl) attributeNodeNS).setSpecified(specified);
                    }
                }
            }
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void emptyElement(QName qName, XMLAttributes xMLAttributes, Augmentations augmentations) throws XNIException {
        startElement(qName, xMLAttributes, augmentations);
        endElement(qName, augmentations);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void ignorableWhitespace(XMLString xMLString, Augmentations augmentations) throws XNIException {
        this.allWhitespace = true;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void endElement(QName qName, Augmentations augmentations) throws XNIException {
        ElementPSVI elementPSVI;
        if (augmentations != null && (elementPSVI = (ElementPSVI) augmentations.getItem(Constants.ELEMENT_PSVI)) != null) {
            PSVIElementNSImpl pSVIElementNSImpl = this.fCurrentNode;
            PSVIElementNSImpl pSVIElementNSImpl2 = pSVIElementNSImpl;
            if (this.fPSVI) {
                pSVIElementNSImpl.setPSVI(elementPSVI);
            }
            String schemaNormalizedValue = elementPSVI.getSchemaNormalizedValue();
            if ((this.fConfiguration.features & 2) != 0) {
                if (schemaNormalizedValue != null) {
                    pSVIElementNSImpl2.setTextContent(schemaNormalizedValue);
                }
            } else if (pSVIElementNSImpl2.getTextContent().length() == 0 && schemaNormalizedValue != null) {
                pSVIElementNSImpl2.setTextContent(schemaNormalizedValue);
            }
        }
    }
}

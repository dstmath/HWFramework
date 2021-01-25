package ohos.com.sun.org.apache.xerces.internal.jaxp.validation;

import java.util.ArrayList;
import ohos.com.sun.org.apache.xerces.internal.dom.AttrImpl;
import ohos.com.sun.org.apache.xerces.internal.dom.CoreDocumentImpl;
import ohos.com.sun.org.apache.xerces.internal.dom.DOMMessageFormatter;
import ohos.com.sun.org.apache.xerces.internal.dom.DocumentTypeImpl;
import ohos.com.sun.org.apache.xerces.internal.dom.ElementImpl;
import ohos.com.sun.org.apache.xerces.internal.dom.EntityImpl;
import ohos.com.sun.org.apache.xerces.internal.dom.NotationImpl;
import ohos.com.sun.org.apache.xerces.internal.dom.PSVIAttrNSImpl;
import ohos.com.sun.org.apache.xerces.internal.dom.PSVIDocumentImpl;
import ohos.com.sun.org.apache.xerces.internal.impl.Constants;
import ohos.com.sun.org.apache.xerces.internal.impl.dv.XSSimpleType;
import ohos.com.sun.org.apache.xerces.internal.xni.Augmentations;
import ohos.com.sun.org.apache.xerces.internal.xni.NamespaceContext;
import ohos.com.sun.org.apache.xerces.internal.xni.QName;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLLocator;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLResourceIdentifier;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLString;
import ohos.com.sun.org.apache.xerces.internal.xni.XNIException;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLDocumentSource;
import ohos.com.sun.org.apache.xerces.internal.xs.AttributePSVI;
import ohos.com.sun.org.apache.xerces.internal.xs.ElementPSVI;
import ohos.com.sun.org.apache.xerces.internal.xs.XSSimpleTypeDefinition;
import ohos.com.sun.org.apache.xerces.internal.xs.XSTypeDefinition;
import ohos.javax.xml.transform.dom.DOMResult;
import ohos.org.w3c.dom.Attr;
import ohos.org.w3c.dom.CDATASection;
import ohos.org.w3c.dom.Comment;
import ohos.org.w3c.dom.Document;
import ohos.org.w3c.dom.DocumentType;
import ohos.org.w3c.dom.Element;
import ohos.org.w3c.dom.Entity;
import ohos.org.w3c.dom.NamedNodeMap;
import ohos.org.w3c.dom.Node;
import ohos.org.w3c.dom.Notation;
import ohos.org.w3c.dom.ProcessingInstruction;
import ohos.org.w3c.dom.Text;

final class DOMResultBuilder implements DOMDocumentHandler {
    private static final int[] kidOK = new int[13];
    private final QName fAttributeQName = new QName();
    private Node fCurrentNode;
    private Document fDocument;
    private CoreDocumentImpl fDocumentImpl;
    private Node fFragmentRoot;
    private boolean fIgnoreChars;
    private Node fNextSibling;
    private boolean fStorePSVI;
    private Node fTarget;
    private final ArrayList fTargetChildren = new ArrayList();

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

    static {
        int[] iArr = kidOK;
        iArr[9] = 1410;
        iArr[1] = 442;
        iArr[5] = 442;
        iArr[6] = 442;
        iArr[11] = 442;
        iArr[2] = 40;
        iArr[10] = 0;
        iArr[7] = 0;
        iArr[8] = 0;
        iArr[3] = 0;
        iArr[4] = 0;
        iArr[12] = 0;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.jaxp.validation.DOMDocumentHandler
    public void setDOMResult(DOMResult dOMResult) {
        CoreDocumentImpl coreDocumentImpl = null;
        this.fCurrentNode = null;
        this.fFragmentRoot = null;
        this.fIgnoreChars = false;
        this.fTargetChildren.clear();
        if (dOMResult != null) {
            this.fTarget = dOMResult.getNode();
            this.fNextSibling = dOMResult.getNextSibling();
            this.fDocument = this.fTarget.getNodeType() == 9 ? (Document) this.fTarget : this.fTarget.getOwnerDocument();
            Document document = this.fDocument;
            if (document instanceof CoreDocumentImpl) {
                coreDocumentImpl = (CoreDocumentImpl) document;
            }
            this.fDocumentImpl = coreDocumentImpl;
            this.fStorePSVI = this.fDocument instanceof PSVIDocumentImpl;
            return;
        }
        this.fTarget = null;
        this.fNextSibling = null;
        this.fDocument = null;
        this.fDocumentImpl = null;
        this.fStorePSVI = false;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.jaxp.validation.DOMDocumentHandler
    public void doctypeDecl(DocumentType documentType) throws XNIException {
        CoreDocumentImpl coreDocumentImpl = this.fDocumentImpl;
        if (coreDocumentImpl != null) {
            DocumentTypeImpl createDocumentType = coreDocumentImpl.createDocumentType(documentType.getName(), documentType.getPublicId(), documentType.getSystemId());
            String internalSubset = documentType.getInternalSubset();
            if (internalSubset != null) {
                createDocumentType.setInternalSubset(internalSubset);
            }
            NamedNodeMap entities = documentType.getEntities();
            NamedNodeMap entities2 = createDocumentType.getEntities();
            int length = entities.getLength();
            for (int i = 0; i < length; i++) {
                Entity item = entities.item(i);
                EntityImpl createEntity = this.fDocumentImpl.createEntity(item.getNodeName());
                createEntity.setPublicId(item.getPublicId());
                createEntity.setSystemId(item.getSystemId());
                createEntity.setNotationName(item.getNotationName());
                entities2.setNamedItem(createEntity);
            }
            NamedNodeMap notations = documentType.getNotations();
            NamedNodeMap notations2 = createDocumentType.getNotations();
            int length2 = notations.getLength();
            for (int i2 = 0; i2 < length2; i2++) {
                Notation item2 = notations.item(i2);
                NotationImpl createNotation = this.fDocumentImpl.createNotation(item2.getNodeName());
                createNotation.setPublicId(item2.getPublicId());
                createNotation.setSystemId(item2.getSystemId());
                notations2.setNamedItem(createNotation);
            }
            append(createDocumentType);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.jaxp.validation.DOMDocumentHandler
    public void characters(Text text) throws XNIException {
        append(this.fDocument.createTextNode(text.getNodeValue()));
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.jaxp.validation.DOMDocumentHandler
    public void cdata(CDATASection cDATASection) throws XNIException {
        append(this.fDocument.createCDATASection(cDATASection.getNodeValue()));
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.jaxp.validation.DOMDocumentHandler
    public void comment(Comment comment) throws XNIException {
        append(this.fDocument.createComment(comment.getNodeValue()));
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.jaxp.validation.DOMDocumentHandler
    public void processingInstruction(ProcessingInstruction processingInstruction) throws XNIException {
        append(this.fDocument.createProcessingInstruction(processingInstruction.getTarget(), processingInstruction.getData()));
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.jaxp.validation.DOMDocumentHandler
    public void setIgnoringCharacters(boolean z) {
        this.fIgnoreChars = z;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void startElement(QName qName, XMLAttributes xMLAttributes, Augmentations augmentations) throws XNIException {
        Element element;
        int length = xMLAttributes.getLength();
        CoreDocumentImpl coreDocumentImpl = this.fDocumentImpl;
        int i = 0;
        if (coreDocumentImpl == null) {
            element = this.fDocument.createElementNS(qName.uri, qName.rawname);
            while (i < length) {
                xMLAttributes.getName(i, this.fAttributeQName);
                element.setAttributeNS(this.fAttributeQName.uri, this.fAttributeQName.rawname, xMLAttributes.getValue(i));
                i++;
            }
        } else {
            element = coreDocumentImpl.createElementNS(qName.uri, qName.rawname, qName.localpart);
            while (i < length) {
                xMLAttributes.getName(i, this.fAttributeQName);
                Attr attr = (AttrImpl) this.fDocumentImpl.createAttributeNS(this.fAttributeQName.uri, this.fAttributeQName.rawname, this.fAttributeQName.localpart);
                attr.setValue(xMLAttributes.getValue(i));
                AttributePSVI attributePSVI = (AttributePSVI) xMLAttributes.getAugmentations(i).getItem(Constants.ATTRIBUTE_PSVI);
                if (attributePSVI != null) {
                    if (this.fStorePSVI) {
                        ((PSVIAttrNSImpl) attr).setPSVI(attributePSVI);
                    }
                    XSSimpleTypeDefinition memberTypeDefinition = attributePSVI.getMemberTypeDefinition();
                    if (memberTypeDefinition == null) {
                        XSTypeDefinition typeDefinition = attributePSVI.getTypeDefinition();
                        if (typeDefinition != null) {
                            attr.setType(typeDefinition);
                            if (((XSSimpleType) typeDefinition).isIDType()) {
                                ((ElementImpl) element).setIdAttributeNode(attr, true);
                            }
                        }
                    } else {
                        attr.setType(memberTypeDefinition);
                        if (((XSSimpleType) memberTypeDefinition).isIDType()) {
                            ((ElementImpl) element).setIdAttributeNode(attr, true);
                        }
                    }
                }
                attr.setSpecified(xMLAttributes.isSpecified(i));
                element.setAttributeNode(attr);
                i++;
            }
        }
        append(element);
        this.fCurrentNode = element;
        if (this.fFragmentRoot == null) {
            this.fFragmentRoot = element;
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void emptyElement(QName qName, XMLAttributes xMLAttributes, Augmentations augmentations) throws XNIException {
        startElement(qName, xMLAttributes, augmentations);
        endElement(qName, augmentations);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void characters(XMLString xMLString, Augmentations augmentations) throws XNIException {
        if (!this.fIgnoreChars) {
            append(this.fDocument.createTextNode(xMLString.toString()));
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void ignorableWhitespace(XMLString xMLString, Augmentations augmentations) throws XNIException {
        characters(xMLString, augmentations);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void endElement(QName qName, Augmentations augmentations) throws XNIException {
        ElementPSVI elementPSVI;
        if (!(augmentations == null || this.fDocumentImpl == null || (elementPSVI = (ElementPSVI) augmentations.getItem(Constants.ELEMENT_PSVI)) == null)) {
            if (this.fStorePSVI) {
                this.fCurrentNode.setPSVI(elementPSVI);
            }
            XSTypeDefinition memberTypeDefinition = elementPSVI.getMemberTypeDefinition();
            if (memberTypeDefinition == null) {
                memberTypeDefinition = elementPSVI.getTypeDefinition();
            }
            this.fCurrentNode.setType(memberTypeDefinition);
        }
        Node node = this.fCurrentNode;
        if (node == this.fFragmentRoot) {
            this.fCurrentNode = null;
            this.fFragmentRoot = null;
            return;
        }
        this.fCurrentNode = node.getParentNode();
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void endDocument(Augmentations augmentations) throws XNIException {
        int size = this.fTargetChildren.size();
        int i = 0;
        if (this.fNextSibling == null) {
            while (i < size) {
                this.fTarget.appendChild((Node) this.fTargetChildren.get(i));
                i++;
            }
            return;
        }
        while (i < size) {
            this.fTarget.insertBefore((Node) this.fTargetChildren.get(i), this.fNextSibling);
            i++;
        }
    }

    private void append(Node node) throws XNIException {
        Node node2 = this.fCurrentNode;
        if (node2 != null) {
            node2.appendChild(node);
        } else if ((kidOK[this.fTarget.getNodeType()] & (1 << node.getNodeType())) != 0) {
            this.fTargetChildren.add(node);
        } else {
            throw new XNIException(DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "HIERARCHY_REQUEST_ERR", null));
        }
    }
}

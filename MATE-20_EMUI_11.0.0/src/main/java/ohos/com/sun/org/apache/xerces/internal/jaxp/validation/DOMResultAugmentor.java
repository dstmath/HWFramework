package ohos.com.sun.org.apache.xerces.internal.jaxp.validation;

import ohos.com.sun.org.apache.xerces.internal.dom.AttrImpl;
import ohos.com.sun.org.apache.xerces.internal.dom.CoreDocumentImpl;
import ohos.com.sun.org.apache.xerces.internal.dom.ElementImpl;
import ohos.com.sun.org.apache.xerces.internal.dom.PSVIAttrNSImpl;
import ohos.com.sun.org.apache.xerces.internal.dom.PSVIDocumentImpl;
import ohos.com.sun.org.apache.xerces.internal.dom.PSVIElementNSImpl;
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
import ohos.org.w3c.dom.NamedNodeMap;
import ohos.org.w3c.dom.ProcessingInstruction;
import ohos.org.w3c.dom.Text;

final class DOMResultAugmentor implements DOMDocumentHandler {
    private final QName fAttributeQName = new QName();
    private DOMValidatorHelper fDOMValidatorHelper;
    private Document fDocument;
    private CoreDocumentImpl fDocumentImpl;
    private boolean fIgnoreChars;
    private boolean fStorePSVI;

    @Override // ohos.com.sun.org.apache.xerces.internal.jaxp.validation.DOMDocumentHandler
    public void cdata(CDATASection cDATASection) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.jaxp.validation.DOMDocumentHandler
    public void characters(Text text) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void comment(XMLString xMLString, Augmentations augmentations) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.jaxp.validation.DOMDocumentHandler
    public void comment(Comment comment) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void doctypeDecl(String str, String str2, String str3, Augmentations augmentations) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.jaxp.validation.DOMDocumentHandler
    public void doctypeDecl(DocumentType documentType) throws XNIException {
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

    @Override // ohos.com.sun.org.apache.xerces.internal.jaxp.validation.DOMDocumentHandler
    public void processingInstruction(ProcessingInstruction processingInstruction) throws XNIException {
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

    public DOMResultAugmentor(DOMValidatorHelper dOMValidatorHelper) {
        this.fDOMValidatorHelper = dOMValidatorHelper;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.jaxp.validation.DOMDocumentHandler
    public void setDOMResult(DOMResult dOMResult) {
        this.fIgnoreChars = false;
        CoreDocumentImpl coreDocumentImpl = null;
        if (dOMResult != null) {
            Document node = dOMResult.getNode();
            this.fDocument = node.getNodeType() == 9 ? node : node.getOwnerDocument();
            Document document = this.fDocument;
            if (document instanceof CoreDocumentImpl) {
                coreDocumentImpl = (CoreDocumentImpl) document;
            }
            this.fDocumentImpl = coreDocumentImpl;
            this.fStorePSVI = this.fDocument instanceof PSVIDocumentImpl;
            return;
        }
        this.fDocument = null;
        this.fDocumentImpl = null;
        this.fStorePSVI = false;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.jaxp.validation.DOMDocumentHandler
    public void setIgnoringCharacters(boolean z) {
        this.fIgnoreChars = z;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void startElement(QName qName, XMLAttributes xMLAttributes, Augmentations augmentations) throws XNIException {
        ElementImpl elementImpl = (Element) this.fDOMValidatorHelper.getCurrentElement();
        NamedNodeMap attributes = elementImpl.getAttributes();
        int length = attributes.getLength();
        if (this.fDocumentImpl != null) {
            for (int i = 0; i < length; i++) {
                Attr attr = (AttrImpl) attributes.item(i);
                AttributePSVI attributePSVI = (AttributePSVI) xMLAttributes.getAugmentations(i).getItem(Constants.ATTRIBUTE_PSVI);
                if (attributePSVI != null && processAttributePSVI(attr, attributePSVI)) {
                    elementImpl.setIdAttributeNode(attr, true);
                }
            }
        }
        int length2 = xMLAttributes.getLength();
        if (length2 <= length) {
            return;
        }
        if (this.fDocumentImpl == null) {
            while (length < length2) {
                xMLAttributes.getName(length, this.fAttributeQName);
                elementImpl.setAttributeNS(this.fAttributeQName.uri, this.fAttributeQName.rawname, xMLAttributes.getValue(length));
                length++;
            }
            return;
        }
        while (length < length2) {
            xMLAttributes.getName(length, this.fAttributeQName);
            Attr attr2 = (AttrImpl) this.fDocumentImpl.createAttributeNS(this.fAttributeQName.uri, this.fAttributeQName.rawname, this.fAttributeQName.localpart);
            attr2.setValue(xMLAttributes.getValue(length));
            AttributePSVI attributePSVI2 = (AttributePSVI) xMLAttributes.getAugmentations(length).getItem(Constants.ATTRIBUTE_PSVI);
            if (attributePSVI2 != null && processAttributePSVI(attr2, attributePSVI2)) {
                elementImpl.setIdAttributeNode(attr2, true);
            }
            attr2.setSpecified(false);
            elementImpl.setAttributeNode(attr2);
            length++;
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
            this.fDOMValidatorHelper.getCurrentElement().appendChild(this.fDocument.createTextNode(xMLString.toString()));
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void ignorableWhitespace(XMLString xMLString, Augmentations augmentations) throws XNIException {
        characters(xMLString, augmentations);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void endElement(QName qName, Augmentations augmentations) throws XNIException {
        ElementPSVI elementPSVI;
        PSVIElementNSImpl currentElement = this.fDOMValidatorHelper.getCurrentElement();
        if (augmentations != null && this.fDocumentImpl != null && (elementPSVI = (ElementPSVI) augmentations.getItem(Constants.ELEMENT_PSVI)) != null) {
            if (this.fStorePSVI) {
                currentElement.setPSVI(elementPSVI);
            }
            XSTypeDefinition memberTypeDefinition = elementPSVI.getMemberTypeDefinition();
            if (memberTypeDefinition == null) {
                memberTypeDefinition = elementPSVI.getTypeDefinition();
            }
            currentElement.setType(memberTypeDefinition);
        }
    }

    private boolean processAttributePSVI(AttrImpl attrImpl, AttributePSVI attributePSVI) {
        if (this.fStorePSVI) {
            ((PSVIAttrNSImpl) attrImpl).setPSVI(attributePSVI);
        }
        XSSimpleTypeDefinition memberTypeDefinition = attributePSVI.getMemberTypeDefinition();
        if (memberTypeDefinition == null) {
            XSTypeDefinition typeDefinition = attributePSVI.getTypeDefinition();
            if (typeDefinition == null) {
                return false;
            }
            attrImpl.setType(typeDefinition);
            return ((XSSimpleType) typeDefinition).isIDType();
        }
        attrImpl.setType(memberTypeDefinition);
        return ((XSSimpleType) memberTypeDefinition).isIDType();
    }
}

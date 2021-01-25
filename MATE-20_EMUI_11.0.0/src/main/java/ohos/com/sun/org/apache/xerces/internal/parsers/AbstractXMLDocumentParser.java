package ohos.com.sun.org.apache.xerces.internal.parsers;

import ohos.com.sun.org.apache.xerces.internal.xni.Augmentations;
import ohos.com.sun.org.apache.xerces.internal.xni.NamespaceContext;
import ohos.com.sun.org.apache.xerces.internal.xni.QName;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDContentModelHandler;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLLocator;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLResourceIdentifier;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLString;
import ohos.com.sun.org.apache.xerces.internal.xni.XNIException;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLDTDContentModelSource;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLDTDSource;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLDocumentSource;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLParserConfiguration;

public abstract class AbstractXMLDocumentParser extends XMLParser implements XMLDocumentHandler, XMLDTDHandler, XMLDTDContentModelHandler {
    protected XMLDTDContentModelSource fDTDContentModelSource;
    protected XMLDTDSource fDTDSource;
    protected XMLDocumentSource fDocumentSource;
    protected boolean fInDTD;

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDContentModelHandler
    public void any(Augmentations augmentations) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void attributeDecl(String str, String str2, String str3, String[] strArr, String str4, XMLString xMLString, XMLString xMLString2, Augmentations augmentations) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void characters(XMLString xMLString, Augmentations augmentations) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void comment(XMLString xMLString, Augmentations augmentations) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void doctypeDecl(String str, String str2, String str3, Augmentations augmentations) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDContentModelHandler
    public void element(String str, Augmentations augmentations) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void elementDecl(String str, String str2, Augmentations augmentations) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDContentModelHandler
    public void empty(Augmentations augmentations) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void endAttlist(Augmentations augmentations) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void endCDATA(Augmentations augmentations) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void endConditional(Augmentations augmentations) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDContentModelHandler
    public void endContentModel(Augmentations augmentations) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void endDocument(Augmentations augmentations) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void endElement(QName qName, Augmentations augmentations) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void endExternalSubset(Augmentations augmentations) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void endGeneralEntity(String str, Augmentations augmentations) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDContentModelHandler
    public void endGroup(Augmentations augmentations) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void endParameterEntity(String str, Augmentations augmentations) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void externalEntityDecl(String str, XMLResourceIdentifier xMLResourceIdentifier, Augmentations augmentations) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void ignorableWhitespace(XMLString xMLString, Augmentations augmentations) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void ignoredCharacters(XMLString xMLString, Augmentations augmentations) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void internalEntityDecl(String str, XMLString xMLString, XMLString xMLString2, Augmentations augmentations) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void notationDecl(String str, XMLResourceIdentifier xMLResourceIdentifier, Augmentations augmentations) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDContentModelHandler
    public void occurrence(short s, Augmentations augmentations) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDContentModelHandler
    public void pcdata(Augmentations augmentations) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void processingInstruction(String str, XMLString xMLString, Augmentations augmentations) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDContentModelHandler
    public void separator(short s, Augmentations augmentations) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void startAttlist(String str, Augmentations augmentations) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void startCDATA(Augmentations augmentations) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void startConditional(short s, Augmentations augmentations) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDContentModelHandler
    public void startContentModel(String str, Augmentations augmentations) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void startDocument(XMLLocator xMLLocator, String str, NamespaceContext namespaceContext, Augmentations augmentations) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void startElement(QName qName, XMLAttributes xMLAttributes, Augmentations augmentations) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void startExternalSubset(XMLResourceIdentifier xMLResourceIdentifier, Augmentations augmentations) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void startGeneralEntity(String str, XMLResourceIdentifier xMLResourceIdentifier, String str2, Augmentations augmentations) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDContentModelHandler
    public void startGroup(Augmentations augmentations) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void startParameterEntity(String str, XMLResourceIdentifier xMLResourceIdentifier, String str2, Augmentations augmentations) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void textDecl(String str, String str2, Augmentations augmentations) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void unparsedEntityDecl(String str, XMLResourceIdentifier xMLResourceIdentifier, String str2, Augmentations augmentations) throws XNIException {
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void xmlDecl(String str, String str2, String str3, Augmentations augmentations) throws XNIException {
    }

    protected AbstractXMLDocumentParser(XMLParserConfiguration xMLParserConfiguration) {
        super(xMLParserConfiguration);
        xMLParserConfiguration.setDocumentHandler(this);
        xMLParserConfiguration.setDTDHandler(this);
        xMLParserConfiguration.setDTDContentModelHandler(this);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void emptyElement(QName qName, XMLAttributes xMLAttributes, Augmentations augmentations) throws XNIException {
        startElement(qName, xMLAttributes, augmentations);
        endElement(qName, augmentations);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void setDocumentSource(XMLDocumentSource xMLDocumentSource) {
        this.fDocumentSource = xMLDocumentSource;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public XMLDocumentSource getDocumentSource() {
        return this.fDocumentSource;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void startDTD(XMLLocator xMLLocator, Augmentations augmentations) throws XNIException {
        this.fInDTD = true;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void endDTD(Augmentations augmentations) throws XNIException {
        this.fInDTD = false;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public void setDTDSource(XMLDTDSource xMLDTDSource) {
        this.fDTDSource = xMLDTDSource;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDHandler
    public XMLDTDSource getDTDSource() {
        return this.fDTDSource;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDContentModelHandler
    public void setDTDContentModelSource(XMLDTDContentModelSource xMLDTDContentModelSource) {
        this.fDTDContentModelSource = xMLDTDContentModelSource;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLDTDContentModelHandler
    public XMLDTDContentModelSource getDTDContentModelSource() {
        return this.fDTDContentModelSource;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.com.sun.org.apache.xerces.internal.parsers.XMLParser
    public void reset() throws XNIException {
        super.reset();
        this.fInDTD = false;
    }
}

package ohos.com.sun.org.apache.xalan.internal.xsltc.trax;

import java.io.IOException;
import ohos.com.sun.org.apache.xml.internal.serializer.NamespaceMappings;
import ohos.com.sun.org.apache.xml.internal.serializer.SerializationHandler;
import ohos.org.w3c.dom.Document;
import ohos.org.w3c.dom.NamedNodeMap;
import ohos.org.w3c.dom.Node;
import ohos.org.xml.sax.ContentHandler;
import ohos.org.xml.sax.DTDHandler;
import ohos.org.xml.sax.EntityResolver;
import ohos.org.xml.sax.ErrorHandler;
import ohos.org.xml.sax.InputSource;
import ohos.org.xml.sax.SAXException;
import ohos.org.xml.sax.SAXNotRecognizedException;
import ohos.org.xml.sax.SAXNotSupportedException;
import ohos.org.xml.sax.XMLReader;
import ohos.org.xml.sax.ext.Locator2;

public class DOM2TO implements XMLReader, Locator2 {
    private static final String EMPTYSTRING = "";
    private static final String XMLNS_PREFIX = "xmlns";
    private Node _dom;
    private SerializationHandler _handler;
    private String xmlEncoding = null;
    private String xmlVersion = null;

    private String getNodeTypeFromCode(short s) {
        switch (s) {
            case 1:
                return "ELEMENT_NODE";
            case 2:
                return "ATTRIBUTE_NODE";
            case 3:
                return "TEXT_NODE";
            case 4:
                return "CDATA_SECTION_NODE";
            case 5:
                return "ENTITY_REFERENCE_NODE";
            case 6:
                return "ENTITY_NODE";
            case 7:
                return "PROCESSING_INSTRUCTION_NODE";
            case 8:
                return "COMMENT_NODE";
            case 9:
                return "DOCUMENT_NODE";
            case 10:
                return "DOCUMENT_TYPE_NODE";
            case 11:
                return "DOCUMENT_FRAGMENT_NODE";
            case 12:
                return "NOTATION_NODE";
            default:
                return null;
        }
    }

    public int getColumnNumber() {
        return 0;
    }

    public ContentHandler getContentHandler() {
        return null;
    }

    public DTDHandler getDTDHandler() {
        return null;
    }

    public EntityResolver getEntityResolver() {
        return null;
    }

    public ErrorHandler getErrorHandler() {
        return null;
    }

    public boolean getFeature(String str) throws SAXNotRecognizedException, SAXNotSupportedException {
        return false;
    }

    public int getLineNumber() {
        return 0;
    }

    public Object getProperty(String str) throws SAXNotRecognizedException, SAXNotSupportedException {
        return null;
    }

    public String getPublicId() {
        return null;
    }

    public String getSystemId() {
        return null;
    }

    public void setContentHandler(ContentHandler contentHandler) {
    }

    public void setDTDHandler(DTDHandler dTDHandler) throws NullPointerException {
    }

    public void setEntityResolver(EntityResolver entityResolver) throws NullPointerException {
    }

    public void setErrorHandler(ErrorHandler errorHandler) throws NullPointerException {
    }

    public void setFeature(String str, boolean z) throws SAXNotRecognizedException, SAXNotSupportedException {
    }

    public void setProperty(String str, Object obj) throws SAXNotRecognizedException, SAXNotSupportedException {
    }

    public DOM2TO(Node node, SerializationHandler serializationHandler) {
        this._dom = node;
        this._handler = serializationHandler;
    }

    public void parse(InputSource inputSource) throws IOException, SAXException {
        parse(this._dom);
    }

    public void parse() throws IOException, SAXException {
        Node node = this._dom;
        if (node != null) {
            if (node.getNodeType() != 9) {
                this._handler.startDocument();
                parse(this._dom);
                this._handler.endDocument();
                return;
            }
            parse(this._dom);
        }
    }

    private void parse(Node node) throws IOException, SAXException {
        if (node != null) {
            switch (node.getNodeType()) {
                case 1:
                    String nodeName = node.getNodeName();
                    this._handler.startElement(null, null, nodeName);
                    NamedNodeMap attributes = node.getAttributes();
                    int length = attributes.getLength();
                    int i = 0;
                    while (true) {
                        String str = "";
                        if (i < length) {
                            Node item = attributes.item(i);
                            String nodeName2 = item.getNodeName();
                            if (nodeName2.startsWith("xmlns")) {
                                String nodeValue = item.getNodeValue();
                                int lastIndexOf = nodeName2.lastIndexOf(58);
                                if (lastIndexOf > 0) {
                                    str = nodeName2.substring(lastIndexOf + 1);
                                }
                                this._handler.namespaceAfterStartElement(str, nodeValue);
                            }
                            i++;
                        } else {
                            NamespaceMappings namespaceMappings = new NamespaceMappings();
                            for (int i2 = 0; i2 < length; i2++) {
                                Node item2 = attributes.item(i2);
                                String nodeName3 = item2.getNodeName();
                                if (!nodeName3.startsWith("xmlns")) {
                                    String namespaceURI = item2.getNamespaceURI();
                                    if (namespaceURI == null || namespaceURI.equals(str)) {
                                        this._handler.addAttribute(nodeName3, item2.getNodeValue());
                                    } else {
                                        int lastIndexOf2 = nodeName3.lastIndexOf(58);
                                        String lookupPrefix = namespaceMappings.lookupPrefix(namespaceURI);
                                        if (lookupPrefix == null) {
                                            lookupPrefix = namespaceMappings.generateNextPrefix();
                                        }
                                        if (lastIndexOf2 > 0) {
                                            lookupPrefix = nodeName3.substring(0, lastIndexOf2);
                                        }
                                        this._handler.namespaceAfterStartElement(lookupPrefix, namespaceURI);
                                        this._handler.addAttribute(lookupPrefix + ":" + nodeName3, item2.getNodeValue());
                                    }
                                }
                            }
                            String namespaceURI2 = node.getNamespaceURI();
                            String localName = node.getLocalName();
                            if (namespaceURI2 != null) {
                                int lastIndexOf3 = nodeName.lastIndexOf(58);
                                if (lastIndexOf3 > 0) {
                                    str = nodeName.substring(0, lastIndexOf3);
                                }
                                this._handler.namespaceAfterStartElement(str, namespaceURI2);
                            } else if (namespaceURI2 == null && localName != null) {
                                this._handler.namespaceAfterStartElement(str, str);
                            }
                            for (Node firstChild = node.getFirstChild(); firstChild != null; firstChild = firstChild.getNextSibling()) {
                                parse(firstChild);
                            }
                            this._handler.endElement(nodeName);
                            return;
                        }
                    }
                    break;
                case 2:
                case 5:
                case 6:
                case 10:
                case 12:
                default:
                    return;
                case 3:
                    this._handler.characters(node.getNodeValue());
                    return;
                case 4:
                    this._handler.startCDATA();
                    this._handler.characters(node.getNodeValue());
                    this._handler.endCDATA();
                    return;
                case 7:
                    this._handler.processingInstruction(node.getNodeName(), node.getNodeValue());
                    return;
                case 8:
                    this._handler.comment(node.getNodeValue());
                    return;
                case 9:
                    setDocumentInfo((Document) node);
                    this._handler.setDocumentLocator(this);
                    this._handler.startDocument();
                    for (Node firstChild2 = node.getFirstChild(); firstChild2 != null; firstChild2 = firstChild2.getNextSibling()) {
                        parse(firstChild2);
                    }
                    this._handler.endDocument();
                    return;
                case 11:
                    for (Node firstChild3 = node.getFirstChild(); firstChild3 != null; firstChild3 = firstChild3.getNextSibling()) {
                        parse(firstChild3);
                    }
                    return;
            }
        }
    }

    public void parse(String str) throws IOException, SAXException {
        throw new IOException("This method is not yet implemented.");
    }

    private void setDocumentInfo(Document document) {
        if (!document.getXmlStandalone()) {
            this._handler.setStandalone(Boolean.toString(document.getXmlStandalone()));
        }
        setXMLVersion(document.getXmlVersion());
        setEncoding(document.getXmlEncoding());
    }

    public String getXMLVersion() {
        return this.xmlVersion;
    }

    private void setXMLVersion(String str) {
        if (str != null) {
            this.xmlVersion = str;
            this._handler.setVersion(this.xmlVersion);
        }
    }

    public String getEncoding() {
        return this.xmlEncoding;
    }

    private void setEncoding(String str) {
        if (str != null) {
            this.xmlEncoding = str;
            this._handler.setEncoding(str);
        }
    }
}

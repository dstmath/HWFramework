package ohos.com.sun.org.apache.xalan.internal.xsltc.trax;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.Vector;
import ohos.com.sun.org.apache.xalan.internal.xsltc.dom.SAXImpl;
import ohos.com.sun.org.apache.xalan.internal.xsltc.runtime.BasisLibrary;
import ohos.org.w3c.dom.NamedNodeMap;
import ohos.org.w3c.dom.Node;
import ohos.org.xml.sax.Attributes;
import ohos.org.xml.sax.ContentHandler;
import ohos.org.xml.sax.DTDHandler;
import ohos.org.xml.sax.EntityResolver;
import ohos.org.xml.sax.ErrorHandler;
import ohos.org.xml.sax.InputSource;
import ohos.org.xml.sax.Locator;
import ohos.org.xml.sax.SAXException;
import ohos.org.xml.sax.SAXNotRecognizedException;
import ohos.org.xml.sax.SAXNotSupportedException;
import ohos.org.xml.sax.XMLReader;
import ohos.org.xml.sax.ext.LexicalHandler;
import ohos.org.xml.sax.helpers.AttributesImpl;

public class DOM2SAX implements XMLReader, Locator {
    private static final String EMPTYSTRING = "";
    private static final String XMLNS_PREFIX = "xmlns";
    private Node _dom = null;
    private LexicalHandler _lex = null;
    private Map<String, Stack> _nsPrefixes = new HashMap();
    private ContentHandler _sax = null;
    private SAXImpl _saxImpl = null;

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

    public DOM2SAX(Node node) {
        this._dom = node;
    }

    public ContentHandler getContentHandler() {
        return this._sax;
    }

    public void setContentHandler(ContentHandler contentHandler) throws NullPointerException {
        this._sax = contentHandler;
        if (contentHandler instanceof LexicalHandler) {
            this._lex = (LexicalHandler) contentHandler;
        }
        if (contentHandler instanceof SAXImpl) {
            this._saxImpl = (SAXImpl) contentHandler;
        }
    }

    private boolean startPrefixMapping(String str, String str2) throws SAXException {
        Stack stack = this._nsPrefixes.get(str);
        if (stack == null) {
            this._sax.startPrefixMapping(str, str2);
            Map<String, Stack> map = this._nsPrefixes;
            Stack stack2 = new Stack();
            map.put(str, stack2);
            stack2.push(str2);
            return true;
        } else if (stack.isEmpty()) {
            this._sax.startPrefixMapping(str, str2);
            stack.push(str2);
            return true;
        } else if (((String) stack.peek()).equals(str2)) {
            return false;
        } else {
            this._sax.startPrefixMapping(str, str2);
            stack.push(str2);
            return true;
        }
    }

    private void endPrefixMapping(String str) throws SAXException {
        Stack stack = this._nsPrefixes.get(str);
        if (stack != null) {
            this._sax.endPrefixMapping(str);
            stack.pop();
        }
    }

    private static String getLocalName(Node node) {
        String localName = node.getLocalName();
        if (localName != null) {
            return localName;
        }
        String nodeName = node.getNodeName();
        int lastIndexOf = nodeName.lastIndexOf(58);
        return lastIndexOf > 0 ? nodeName.substring(lastIndexOf + 1) : nodeName;
    }

    public void parse(InputSource inputSource) throws IOException, SAXException {
        parse(this._dom);
    }

    public void parse() throws IOException, SAXException {
        Node node = this._dom;
        if (node != null) {
            if (node.getNodeType() != 9) {
                this._sax.startDocument();
                parse(this._dom);
                this._sax.endDocument();
                return;
            }
            parse(this._dom);
        }
    }

    private void parse(Node node) throws IOException, SAXException {
        String str;
        if (node != null) {
            switch (node.getNodeType()) {
                case 1:
                    Vector vector = new Vector();
                    Attributes attributesImpl = new AttributesImpl();
                    NamedNodeMap attributes = node.getAttributes();
                    int length = attributes.getLength();
                    int i = 0;
                    while (true) {
                        String str2 = "";
                        if (i < length) {
                            Node item = attributes.item(i);
                            String nodeName = item.getNodeName();
                            if (nodeName.startsWith("xmlns")) {
                                String nodeValue = item.getNodeValue();
                                int lastIndexOf = nodeName.lastIndexOf(58);
                                if (lastIndexOf > 0) {
                                    str2 = nodeName.substring(lastIndexOf + 1);
                                }
                                if (startPrefixMapping(str2, nodeValue)) {
                                    vector.addElement(str2);
                                }
                            }
                            i++;
                        } else {
                            for (int i2 = 0; i2 < length; i2++) {
                                Node item2 = attributes.item(i2);
                                String nodeName2 = item2.getNodeName();
                                if (!nodeName2.startsWith("xmlns")) {
                                    String namespaceURI = item2.getNamespaceURI();
                                    getLocalName(item2);
                                    if (namespaceURI != null) {
                                        int lastIndexOf2 = nodeName2.lastIndexOf(58);
                                        if (lastIndexOf2 > 0) {
                                            str = nodeName2.substring(0, lastIndexOf2);
                                        } else {
                                            str = BasisLibrary.generatePrefix();
                                            nodeName2 = str + ':' + nodeName2;
                                        }
                                        if (startPrefixMapping(str, namespaceURI)) {
                                            vector.addElement(str);
                                        }
                                    }
                                    attributesImpl.addAttribute(item2.getNamespaceURI(), getLocalName(item2), nodeName2, "CDATA", item2.getNodeValue());
                                }
                            }
                            String nodeName3 = node.getNodeName();
                            String namespaceURI2 = node.getNamespaceURI();
                            String localName = getLocalName(node);
                            if (namespaceURI2 != null) {
                                int lastIndexOf3 = nodeName3.lastIndexOf(58);
                                if (lastIndexOf3 > 0) {
                                    str2 = nodeName3.substring(0, lastIndexOf3);
                                }
                                if (startPrefixMapping(str2, namespaceURI2)) {
                                    vector.addElement(str2);
                                }
                            }
                            SAXImpl sAXImpl = this._saxImpl;
                            if (sAXImpl != null) {
                                sAXImpl.startElement(namespaceURI2, localName, nodeName3, attributesImpl, node);
                            } else {
                                this._sax.startElement(namespaceURI2, localName, nodeName3, attributesImpl);
                            }
                            for (Node firstChild = node.getFirstChild(); firstChild != null; firstChild = firstChild.getNextSibling()) {
                                parse(firstChild);
                            }
                            this._sax.endElement(namespaceURI2, localName, nodeName3);
                            int size = vector.size();
                            for (int i3 = 0; i3 < size; i3++) {
                                endPrefixMapping((String) vector.elementAt(i3));
                            }
                            return;
                        }
                    }
                case 2:
                case 5:
                case 6:
                case 10:
                case 11:
                case 12:
                default:
                    return;
                case 3:
                    String nodeValue2 = node.getNodeValue();
                    this._sax.characters(nodeValue2.toCharArray(), 0, nodeValue2.length());
                    return;
                case 4:
                    String nodeValue3 = node.getNodeValue();
                    LexicalHandler lexicalHandler = this._lex;
                    if (lexicalHandler != null) {
                        lexicalHandler.startCDATA();
                        this._sax.characters(nodeValue3.toCharArray(), 0, nodeValue3.length());
                        this._lex.endCDATA();
                        return;
                    }
                    this._sax.characters(nodeValue3.toCharArray(), 0, nodeValue3.length());
                    return;
                case 7:
                    this._sax.processingInstruction(node.getNodeName(), node.getNodeValue());
                    return;
                case 8:
                    if (this._lex != null) {
                        String nodeValue4 = node.getNodeValue();
                        this._lex.comment(nodeValue4.toCharArray(), 0, nodeValue4.length());
                        return;
                    }
                    return;
                case 9:
                    this._sax.setDocumentLocator(this);
                    this._sax.startDocument();
                    for (Node firstChild2 = node.getFirstChild(); firstChild2 != null; firstChild2 = firstChild2.getNextSibling()) {
                        parse(firstChild2);
                    }
                    this._sax.endDocument();
                    return;
            }
        }
    }

    public void parse(String str) throws IOException, SAXException {
        throw new IOException("This method is not yet implemented.");
    }
}

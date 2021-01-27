package ohos.com.sun.org.apache.xml.internal.utils;

import java.io.Writer;
import java.util.Stack;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaSymbols;
import ohos.com.sun.org.apache.xml.internal.res.XMLMessages;
import ohos.org.w3c.dom.Document;
import ohos.org.w3c.dom.DocumentFragment;
import ohos.org.w3c.dom.Element;
import ohos.org.w3c.dom.Node;
import ohos.org.w3c.dom.Text;
import ohos.org.xml.sax.Attributes;
import ohos.org.xml.sax.ContentHandler;
import ohos.org.xml.sax.Locator;
import ohos.org.xml.sax.SAXException;
import ohos.org.xml.sax.ext.LexicalHandler;

public class DOMBuilder implements ContentHandler, LexicalHandler {
    protected Node m_currentNode = null;
    public Document m_doc;
    public DocumentFragment m_docFrag = null;
    protected Stack m_elemStack = new Stack();
    protected boolean m_inCData = false;
    protected Node m_nextSibling = null;
    protected Node m_root = null;

    public void endDTD() throws SAXException {
    }

    public void endDocument() throws SAXException {
    }

    public void endEntity(String str) throws SAXException {
    }

    public void endPrefixMapping(String str) throws SAXException {
    }

    public Writer getWriter() {
        return null;
    }

    public void setDocumentLocator(Locator locator) {
    }

    public void setIDAttribute(String str, Element element) {
    }

    public void skippedEntity(String str) throws SAXException {
    }

    public void startDTD(String str, String str2, String str3) throws SAXException {
    }

    public void startDocument() throws SAXException {
    }

    public void startEntity(String str) throws SAXException {
    }

    public void startPrefixMapping(String str, String str2) throws SAXException {
    }

    public DOMBuilder(Document document, Node node) {
        this.m_doc = document;
        this.m_root = node;
        this.m_currentNode = node;
        if (node instanceof Element) {
            this.m_elemStack.push(node);
        }
    }

    public DOMBuilder(Document document, DocumentFragment documentFragment) {
        this.m_doc = document;
        this.m_docFrag = documentFragment;
    }

    public DOMBuilder(Document document) {
        this.m_doc = document;
    }

    public Node getRootDocument() {
        DocumentFragment documentFragment = this.m_docFrag;
        return documentFragment != null ? documentFragment : this.m_doc;
    }

    public Node getRootNode() {
        return this.m_root;
    }

    public Node getCurrentNode() {
        return this.m_currentNode;
    }

    public void setNextSibling(Node node) {
        this.m_nextSibling = node;
    }

    public Node getNextSibling() {
        return this.m_nextSibling;
    }

    /* access modifiers changed from: protected */
    public void append(Node node) throws SAXException {
        Node node2;
        Node node3 = this.m_currentNode;
        if (node3 == null) {
            DocumentFragment documentFragment = this.m_docFrag;
            if (documentFragment != null) {
                Node node4 = this.m_nextSibling;
                if (node4 != null) {
                    documentFragment.insertBefore(node, node4);
                } else {
                    documentFragment.appendChild(node);
                }
            } else {
                short nodeType = node.getNodeType();
                boolean z = true;
                if (nodeType == 3) {
                    String nodeValue = node.getNodeValue();
                    if (nodeValue == null || nodeValue.trim().length() <= 0) {
                        z = false;
                    } else {
                        throw new SAXException(XMLMessages.createXMLMessage("ER_CANT_OUTPUT_TEXT_BEFORE_DOC", null));
                    }
                } else if (nodeType == 1 && this.m_doc.getDocumentElement() != null) {
                    throw new SAXException(XMLMessages.createXMLMessage("ER_CANT_HAVE_MORE_THAN_ONE_ROOT", null));
                }
                if (z) {
                    Node node5 = this.m_nextSibling;
                    if (node5 != null) {
                        this.m_doc.insertBefore(node, node5);
                    } else {
                        this.m_doc.appendChild(node);
                    }
                }
            }
        } else if (node3 != this.m_root || (node2 = this.m_nextSibling) == null) {
            node3.appendChild(node);
        } else {
            node3.insertBefore(node, node2);
        }
    }

    public void startElement(String str, String str2, String str3, Attributes attributes) throws SAXException {
        Element element;
        if (str == null || str.length() == 0) {
            element = this.m_doc.createElementNS((String) null, str3);
        } else {
            element = this.m_doc.createElementNS(str, str3);
        }
        append(element);
        try {
            int length = attributes.getLength();
            if (length != 0) {
                for (int i = 0; i < length; i++) {
                    if (attributes.getType(i).equalsIgnoreCase(SchemaSymbols.ATTVAL_ID)) {
                        setIDAttribute(attributes.getValue(i), element);
                    }
                    String uri = attributes.getURI(i);
                    if ("".equals(uri)) {
                        uri = null;
                    }
                    String qName = attributes.getQName(i);
                    if (qName.startsWith("xmlns:") || qName.equals("xmlns")) {
                        uri = "http://www.w3.org/2000/xmlns/";
                    }
                    element.setAttributeNS(uri, qName, attributes.getValue(i));
                }
            }
            this.m_elemStack.push(element);
            this.m_currentNode = element;
        } catch (Exception e) {
            throw new SAXException(e);
        }
    }

    public void endElement(String str, String str2, String str3) throws SAXException {
        this.m_elemStack.pop();
        this.m_currentNode = this.m_elemStack.isEmpty() ? null : (Node) this.m_elemStack.peek();
    }

    public void characters(char[] cArr, int i, int i2) throws SAXException {
        if (isOutsideDocElem() && XMLCharacterRecognizer.isWhiteSpace(cArr, i, i2)) {
            return;
        }
        if (this.m_inCData) {
            cdata(cArr, i, i2);
            return;
        }
        String str = new String(cArr, i, i2);
        Node node = this.m_currentNode;
        Node lastChild = node != null ? node.getLastChild() : null;
        if (lastChild == null || lastChild.getNodeType() != 3) {
            append(this.m_doc.createTextNode(str));
        } else {
            ((Text) lastChild).appendData(str);
        }
    }

    public void charactersRaw(char[] cArr, int i, int i2) throws SAXException {
        if (!isOutsideDocElem() || !XMLCharacterRecognizer.isWhiteSpace(cArr, i, i2)) {
            String str = new String(cArr, i, i2);
            append(this.m_doc.createProcessingInstruction("xslt-next-is-raw", "formatter-to-dom"));
            append(this.m_doc.createTextNode(str));
        }
    }

    public void entityReference(String str) throws SAXException {
        append(this.m_doc.createEntityReference(str));
    }

    public void ignorableWhitespace(char[] cArr, int i, int i2) throws SAXException {
        if (!isOutsideDocElem()) {
            append(this.m_doc.createTextNode(new String(cArr, i, i2)));
        }
    }

    private boolean isOutsideDocElem() {
        Node node;
        return this.m_docFrag == null && this.m_elemStack.size() == 0 && ((node = this.m_currentNode) == null || node.getNodeType() == 9);
    }

    public void processingInstruction(String str, String str2) throws SAXException {
        append(this.m_doc.createProcessingInstruction(str, str2));
    }

    public void comment(char[] cArr, int i, int i2) throws SAXException {
        append(this.m_doc.createComment(new String(cArr, i, i2)));
    }

    public void startCDATA() throws SAXException {
        this.m_inCData = true;
        append(this.m_doc.createCDATASection(""));
    }

    public void endCDATA() throws SAXException {
        this.m_inCData = false;
    }

    public void cdata(char[] cArr, int i, int i2) throws SAXException {
        if (!isOutsideDocElem() || !XMLCharacterRecognizer.isWhiteSpace(cArr, i, i2)) {
            this.m_currentNode.getLastChild().appendData(new String(cArr, i, i2));
        }
    }
}

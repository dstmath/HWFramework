package ohos.com.sun.org.apache.xalan.internal.xsltc.trax;

import java.util.Stack;
import java.util.Vector;
import ohos.com.sun.org.apache.xalan.internal.xsltc.runtime.Constants;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaSymbols;
import ohos.com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl;
import ohos.javax.xml.parsers.DocumentBuilderFactory;
import ohos.javax.xml.parsers.ParserConfigurationException;
import ohos.jdk.xml.internal.JdkXmlUtils;
import ohos.org.w3c.dom.Comment;
import ohos.org.w3c.dom.Document;
import ohos.org.w3c.dom.Element;
import ohos.org.w3c.dom.Node;
import ohos.org.w3c.dom.ProcessingInstruction;
import ohos.org.xml.sax.Attributes;
import ohos.org.xml.sax.ContentHandler;
import ohos.org.xml.sax.Locator;
import ohos.org.xml.sax.SAXException;
import ohos.org.xml.sax.ext.LexicalHandler;
import ohos.org.xml.sax.ext.Locator2;

public class SAX2DOM implements ContentHandler, LexicalHandler, Constants {
    private Document _document;
    private DocumentBuilderFactory _factory;
    private boolean _internal;
    private Node _lastSibling;
    private Vector _namespaceDecls;
    private Node _nextSibling;
    private Node _nextSiblingCache;
    private Stack _nodeStk;
    private Node _root;
    private StringBuilder _textBuffer;
    private Locator locator;
    private boolean needToSetDocumentInfo;

    public void endCDATA() {
    }

    public void endDTD() {
    }

    public void endEntity(String str) {
    }

    public void endPrefixMapping(String str) {
    }

    public void ignorableWhitespace(char[] cArr, int i, int i2) {
    }

    public void skippedEntity(String str) {
    }

    public void startCDATA() {
    }

    public void startDTD(String str, String str2, String str3) throws SAXException {
    }

    public void startEntity(String str) {
    }

    public SAX2DOM(boolean z) throws ParserConfigurationException {
        this._root = null;
        this._document = null;
        this._nextSibling = null;
        this._nodeStk = new Stack();
        this._namespaceDecls = null;
        this._lastSibling = null;
        this.locator = null;
        this.needToSetDocumentInfo = true;
        this._textBuffer = new StringBuilder();
        this._nextSiblingCache = null;
        this._internal = true;
        this._document = createDocument(z);
        this._root = this._document;
    }

    public SAX2DOM(Node node, Node node2, boolean z) throws ParserConfigurationException {
        this._root = null;
        this._document = null;
        this._nextSibling = null;
        this._nodeStk = new Stack();
        this._namespaceDecls = null;
        this._lastSibling = null;
        this.locator = null;
        this.needToSetDocumentInfo = true;
        this._textBuffer = new StringBuilder();
        this._nextSiblingCache = null;
        this._internal = true;
        this._root = node;
        if (node instanceof Document) {
            this._document = (Document) node;
        } else if (node != null) {
            this._document = node.getOwnerDocument();
        } else {
            this._document = createDocument(z);
            this._root = this._document;
        }
        this._nextSibling = node2;
    }

    public SAX2DOM(Node node, boolean z) throws ParserConfigurationException {
        this(node, null, z);
    }

    public Node getDOM() {
        return this._root;
    }

    public void characters(char[] cArr, int i, int i2) {
        if (i2 != 0 && ((Node) this._nodeStk.peek()) != this._document) {
            this._nextSiblingCache = this._nextSibling;
            this._textBuffer.append(cArr, i, i2);
        }
    }

    private void appendTextNode() {
        if (this._textBuffer.length() > 0) {
            Node node = (Node) this._nodeStk.peek();
            if (node != this._root || this._nextSiblingCache == null) {
                this._lastSibling = node.appendChild(this._document.createTextNode(this._textBuffer.toString()));
            } else {
                this._lastSibling = node.insertBefore(this._document.createTextNode(this._textBuffer.toString()), this._nextSiblingCache);
            }
            this._textBuffer.setLength(0);
        }
    }

    public void startDocument() {
        this._nodeStk.push(this._root);
    }

    public void endDocument() {
        this._nodeStk.pop();
    }

    private void setDocumentInfo() {
        Locator2 locator2 = this.locator;
        if (locator2 != null) {
            try {
                this._document.setXmlVersion(locator2.getXMLVersion());
            } catch (ClassCastException unused) {
            }
        }
    }

    public void startElement(String str, String str2, String str3, Attributes attributes) {
        Node node;
        appendTextNode();
        if (this.needToSetDocumentInfo) {
            setDocumentInfo();
            this.needToSetDocumentInfo = false;
        }
        Element createElementNS = this._document.createElementNS(str, str3);
        Vector vector = this._namespaceDecls;
        if (vector != null) {
            int size = vector.size();
            int i = 0;
            while (i < size) {
                int i2 = i + 1;
                String str4 = (String) this._namespaceDecls.elementAt(i);
                if (str4 == null || str4.equals("")) {
                    createElementNS.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns", (String) this._namespaceDecls.elementAt(i2));
                } else {
                    createElementNS.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:" + str4, (String) this._namespaceDecls.elementAt(i2));
                }
                i = i2 + 1;
            }
            this._namespaceDecls.clear();
        }
        int length = attributes.getLength();
        for (int i3 = 0; i3 < length; i3++) {
            String qName = attributes.getQName(i3);
            String uri = attributes.getURI(i3);
            if (attributes.getLocalName(i3).equals("")) {
                createElementNS.setAttribute(qName, attributes.getValue(i3));
                if (attributes.getType(i3).equals(SchemaSymbols.ATTVAL_ID)) {
                    createElementNS.setIdAttribute(qName, true);
                }
            } else {
                createElementNS.setAttributeNS(uri, qName, attributes.getValue(i3));
                if (attributes.getType(i3).equals(SchemaSymbols.ATTVAL_ID)) {
                    createElementNS.setIdAttributeNS(uri, attributes.getLocalName(i3), true);
                }
            }
        }
        Node node2 = (Node) this._nodeStk.peek();
        if (node2 != this._root || (node = this._nextSibling) == null) {
            node2.appendChild(createElementNS);
        } else {
            node2.insertBefore(createElementNS, node);
        }
        this._nodeStk.push(createElementNS);
        this._lastSibling = null;
    }

    public void endElement(String str, String str2, String str3) {
        appendTextNode();
        this._nodeStk.pop();
        this._lastSibling = null;
    }

    public void startPrefixMapping(String str, String str2) {
        if (this._namespaceDecls == null) {
            this._namespaceDecls = new Vector(2);
        }
        this._namespaceDecls.addElement(str);
        this._namespaceDecls.addElement(str2);
    }

    public void processingInstruction(String str, String str2) {
        Node node;
        appendTextNode();
        Node node2 = (Node) this._nodeStk.peek();
        ProcessingInstruction createProcessingInstruction = this._document.createProcessingInstruction(str, str2);
        if (createProcessingInstruction != null) {
            if (node2 != this._root || (node = this._nextSibling) == null) {
                node2.appendChild(createProcessingInstruction);
            } else {
                node2.insertBefore(createProcessingInstruction, node);
            }
            this._lastSibling = createProcessingInstruction;
        }
    }

    public void setDocumentLocator(Locator locator2) {
        this.locator = locator2;
    }

    public void comment(char[] cArr, int i, int i2) {
        Node node;
        appendTextNode();
        Node node2 = (Node) this._nodeStk.peek();
        Comment createComment = this._document.createComment(new String(cArr, i, i2));
        if (createComment != null) {
            if (node2 != this._root || (node = this._nextSibling) == null) {
                node2.appendChild(createComment);
            } else {
                node2.insertBefore(createComment, node);
            }
            this._lastSibling = createComment;
        }
    }

    private Document createDocument(boolean z) throws ParserConfigurationException {
        Document newDocument;
        if (this._factory == null) {
            this._factory = JdkXmlUtils.getDOMFactory(z);
            this._internal = true;
            if (!(this._factory instanceof DocumentBuilderFactoryImpl)) {
                this._internal = false;
            }
        }
        if (this._internal) {
            return this._factory.newDocumentBuilder().newDocument();
        }
        synchronized (SAX2DOM.class) {
            newDocument = this._factory.newDocumentBuilder().newDocument();
        }
        return newDocument;
    }
}
